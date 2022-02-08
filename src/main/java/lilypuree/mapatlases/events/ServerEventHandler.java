package lilypuree.mapatlases.events;

import lilypuree.mapatlases.MapAtlasesMod;
import lilypuree.mapatlases.network.MapAtlasesActiveStateChangeS2CPacket;
import lilypuree.mapatlases.network.MapAtlasesInitAtlasS2CPacket;
import lilypuree.mapatlases.network.ModPacketHandler;
import lilypuree.mapatlases.util.MapAtlasesAccessUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = MapAtlasesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    // Value minimum of 64, since maps are 128
    private static final int NEW_MAP_CENTER_DISTANCE = 90;
    // Holds the current MapState ID for each player
    private static final Map<String, String> playerToActiveMapId = new HashMap<>();
    // Used to prevent Map creation spam consuming all Empty Maps on auto-create
    private static final Semaphore mutex = new Semaphore(1);
    private static MinecraftServer server;


    @SubscribeEvent
    public static void onServerStart(ServerAboutToStartEvent event) {
        server = event.getServer();
    }

    @SubscribeEvent
    public static void mapAtlasPlayerJoin(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getPlayer().level.isClientSide) {
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.getInventory());
            if (atlas.isEmpty()) return;
            Map<String, MapItemSavedData> mapInfos = MapAtlasesAccessUtils.getAllMapInfoFromAtlas(player.level, atlas);
            for (Map.Entry<String, MapItemSavedData> info : mapInfos.entrySet()) {
                String mapId = info.getKey();
                MapItemSavedData state = info.getValue();
                state.tickCarriedBy(player, atlas);
                state.getHoldingPlayer(player);

                ModPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new MapAtlasesInitAtlasS2CPacket(mapId, state));
                MapAtlasesMod.LOGGER.info("Server Sent MapState: " + mapId);
            }
        }
    }

    @SubscribeEvent
    public static void mapAtlasServerTick(TickEvent.ServerTickEvent event) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.getInventory());
            if (!atlas.isEmpty()) {
                Map.Entry<String, MapItemSavedData> activeInfo =
                        MapAtlasesAccessUtils.getActiveAtlasMapState(
                                player.getLevel(), atlas, player.getName().getString());
                if (activeInfo != null) {
                    String playerName = player.getName().getString();
                    if (!playerToActiveMapId.containsKey(playerName)
                            || playerToActiveMapId.get(playerName).compareTo(activeInfo.getKey()) != 0) {
                        playerToActiveMapId.put(playerName, activeInfo.getKey());

                        ModPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new MapAtlasesActiveStateChangeS2CPacket(activeInfo.getKey()));
                    }
                } else if (MapAtlasesAccessUtils.getEmptyMapCountFromItemStack(atlas) != 0) {
                    MapAtlasesMod.LOGGER.info("Null active MapState with non-empty Atlas");
                }


                Map<String, MapItemSavedData> mapInfos = MapAtlasesAccessUtils.getAllMapInfoFromAtlas(player.level, atlas);

                // Maps are 128x128
                int playX = player.blockPosition().getX();
                int playZ = player.blockPosition().getZ();
                int minDist = Integer.MAX_VALUE;
                int scale = -1;

                for (Map.Entry<String, MapItemSavedData> info : mapInfos.entrySet()) {
                    MapItemSavedData state = info.getValue();
                    state.tickCarriedBy(player, atlas);
                    ((MapItem) Items.FILLED_MAP).update(player.getLevel(), player, state);
                    int mapId = MapAtlasesAccessUtils.getMapIntFromString(info.getKey());
                    Packet<?> p = null;
                    int tries = 0;
                    while (p == null && tries < 10) {
                        p = state.getUpdatePacket(mapId, player);
                        tries++;
                    }
                    if (p != null) {
                        player.connection.send(p);
                    }

                    int mapCX = state.x;
                    int mapCZ = state.z;
                    minDist = Math.min(minDist, (int) Math.hypot(playX - mapCX, playZ - mapCZ));
                    scale = state.scale;
                }

                if (MapAtlasesMod.CONFIG != null && !MapAtlasesMod.CONFIG.enableEmptyMapEntryAndFill.get()) continue;
                if (atlas.getTag() == null) continue;
                String oldAtlasTagState = atlas.getTag().toString();
                List<Integer> mapIds = Arrays.stream(
                        atlas.getTag().getIntArray("maps")).boxed().collect(Collectors.toList());
                int emptyCount = MapAtlasesAccessUtils.getEmptyMapCountFromItemStack(atlas);
                if (mutex.availablePermits() > 0 && minDist != -1 &&
                        scale != -1 && minDist > (NEW_MAP_CENTER_DISTANCE * (1 << scale)) && emptyCount > 0) {
                    try {
                        mutex.acquire();

                        // Make the new map
                        atlas.getTag().putInt("empty", atlas.getTag().getInt("empty") - 1);
                        ItemStack newMap = MapItem.create(
                                player.getLevel(),
                                Mth.floor(player.getX()),
                                Mth.floor(player.getZ()),
                                (byte) scale,
                                true,
                                false);
                        mapIds.add(MapItem.getMapId(newMap));
                        atlas.getTag().putIntArray("maps", mapIds);

                        // Update the reference in the inventory
                        MapAtlasesAccessUtils.setAllMatchingItemStacks(
                                player.getInventory().offhand, 1, MapAtlasesMod.MAP_ATLAS, oldAtlasTagState, atlas);
                        MapAtlasesAccessUtils.setAllMatchingItemStacks(
                                player.getInventory().items, 9, MapAtlasesMod.MAP_ATLAS, oldAtlasTagState, atlas);

                        // Play the sound
                        player.getLevel().playSound(null, player.blockPosition(),
                                MapAtlasesMod.ATLAS_CREATE_MAP_SOUND_EVENT,
                                SoundSource.PLAYERS, 1.0F, 1.0F);
                    } catch (InterruptedException e) {
                        MapAtlasesMod.LOGGER.warn(e);
                    } finally {
                        mutex.release();
                    }
                }
            }
        }
    }
}
