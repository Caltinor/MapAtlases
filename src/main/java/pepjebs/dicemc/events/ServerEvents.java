package pepjebs.dicemc.events;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.config.Config;
import pepjebs.dicemc.network.MapAtlasesInitAtlasS2CPacket;
import pepjebs.dicemc.network.MapAtlasesActiveStateChangePacket;
import pepjebs.dicemc.setup.Networking;
import pepjebs.dicemc.setup.Registration;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;

@Mod.EventBusSubscriber(modid=MapAtlases.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
	
	public static final ResourceLocation MAP_ATLAS_ACTIVE_STATE_CHANGE = new ResourceLocation(
            MapAtlases.MOD_ID, "active_state_change");

    // Value minimum of 64, since maps are 128
    private static final int NEW_MAP_CENTER_DISTANCE = 90;

    // Used to prevent Map creation spam consuming all Empty Maps on auto-create
    private static final Semaphore mutex = new Semaphore(1);

    // Holds the current MapState ID for each player
    private static final Map<String, String> playerToActiveMapId = new HashMap<>();
    
    public static MinecraftServer server;

    
    @SubscribeEvent
    public static void onServerStart(ServerAboutToStartEvent event) {
    	server = event.getServer();
    }
    
    @SubscribeEvent
    public static void mapAtlasPlayerJoin(PlayerLoggedInEvent event) {
    	if (!event.getPlayer().level.isClientSide) {
	        ServerPlayer player = (ServerPlayer) event.getPlayer();
	        ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.getInventory());
	        if (atlas.isEmpty()) return;
	        List<Integer> mapIds = MapAtlasesAccessUtils.getMapIdsFromAtlas(player.level, atlas);
	        for (Integer id : mapIds) {
	        	MapItemSavedData state = player.level.getMapData(MapItem.makeKey(id));
	            state.tickCarriedBy(player, atlas);
	            state.getHoldingPlayer(player);
	            Networking.sendToClient(new MapAtlasesInitAtlasS2CPacket(state, id), player);
	            MapAtlases.LOGGER.info("Server Sent MapState: " + id);
	        }
    	}
    }
    
	@SubscribeEvent
	public static void mapAtlasServerTick(ServerTickEvent event) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.getInventory());
            if (!atlas.isEmpty()) {
                MapItemSavedData activeState = MapAtlasesAccessUtils.getActiveAtlasMapData(
                                player.getCommandSenderWorld(), atlas, player.getName().getString());
                int activeID = MapAtlasesAccessUtils.getMapIntFromState(player.level, activeState);
                if (activeState != null) {
                    String playerName = player.getName().getString();
                    if (!playerToActiveMapId.containsKey(playerName)
                            || playerToActiveMapId.get(playerName).compareTo(MapItem.makeKey(activeID)) != 0) {
                        playerToActiveMapId.put(playerName, MapItem.makeKey(activeID));
                        Networking.sendToClient(new MapAtlasesActiveStateChangePacket(activeID), player);
                    }
                } else if (MapAtlasesAccessUtils.getEmptyMapCountFromItemStack(atlas) != 0) {
                    MapAtlases.LOGGER.info("Null active MapData with non-empty Atlas");
                }


                List<MapItemSavedData> mapStates = MapAtlasesAccessUtils.getAllMapDatasFromAtlas(player.getCommandSenderWorld(), atlas);

                // Maps are 128x128
                int playX = player.blockPosition().getX();
                int playZ = player.blockPosition().getZ();
                int minDist = Integer.MAX_VALUE;
                int scale = -1;

                for (MapItemSavedData state : mapStates) {
                    state.tickCarriedBy(player, atlas);
                    ((MapItem) Items.FILLED_MAP).update(player.getCommandSenderWorld(), player, state);
                    int stateID = MapAtlasesAccessUtils.getMapIntFromState(player.level, state);
                    //ItemStack map = MapAtlasesAccessUtils.createMapItemStackFromStrId(stateID);
                    Packet<?> p = null;
                    int tries = 0;
                    while (p == null && tries < 10) {
                        p = state.getUpdatePacket(stateID, player);
                        tries++;
                    }
                    if (p != null) {
                        player.connection.send(p);
                    }

                    int mapCX = state.x;
                    int mapCZ = state.z;
                    minDist = Math.min(minDist, (int) Math.hypot(playX-mapCX, playZ-mapCZ));
                    scale = state.scale;
                }

                if (!Config.ENABLE_EMPTY_MAP_ENTRY_AND_FILL.get()) continue;
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
                                player.level,
                                Mth.floor(player.getX()),
                                Mth.floor(player.getZ()),
                                (byte) scale,
                                true,
                                false);
                        mapIds.add(MapItem.getMapId(newMap));
                        atlas.getTag().putIntArray("maps", mapIds);

                        // Update the reference in the inventory
                        MapAtlasesAccessUtils.setAllMatchingItemStacks(
                                player.getInventory().offhand, 1, Registration.MAP_ATLAS.get(), oldAtlasTagState, atlas);
                        MapAtlasesAccessUtils.setAllMatchingItemStacks(
                                player.getInventory().items, 9, Registration.MAP_ATLAS.get(), oldAtlasTagState, atlas);

                        // Play the sound
                        player.level.playSound(null, player.blockPosition(),
                                Registration.ATLAS_CREATE_MAP_SOUND_EVENT.get(),
                                SoundSource.PLAYERS, 1.0F, 1.0F);
                    } catch (InterruptedException e) {
                        MapAtlases.LOGGER.warn(e);
                    } finally {
                        mutex.release();
                    }
                }
            }
        }
    }
}
