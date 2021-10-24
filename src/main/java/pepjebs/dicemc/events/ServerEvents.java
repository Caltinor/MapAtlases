package pepjebs.dicemc.events;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
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

	//TODO Figure out what this is for
	/*public static void openGuiEvent(
			MinecraftServer server,
			ServerPlayerEntity player,
			ServerPlayNetworkHandler _handler,
			PacketByteBuf buf,
			PacketSender _responseSender) {
		MapAtlasesOpenGUIC2SPacket p = new MapAtlasesOpenGUIC2SPacket();
		p.read(buf);
		server.execute(() -> {
			ItemStack atlas = p.atlas;
			if (!(player.getInventory().offHand.get(0).getTag() != null && atlas.getTag() != null &&
					player.getInventory().offHand.get(0).getTag().toString().compareTo(atlas.getTag().toString()) == 0)) {
				int atlasIdx = player.getInventory().main.size();
				for (int i = 0; i < player.getInventory().main.size(); i++) {
					if (player.getInventory().main.get(i).getItem() == atlas.getItem() &&
							player.getInventory().main.get(i).getTag() != null &&
							atlas.getTag() != null &&
							player.getInventory().main.get(i).getTag().toString().compareTo(atlas.getTag().toString()) == 0) {
						atlasIdx = i;
						break;
					}
				}
				if (atlasIdx < PlayerInventory.getHotbarSize()) {
					player.getInventory().selectedSlot = atlasIdx;
					player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(atlasIdx));
				}
			}
			player.openHandledScreen((MapAtlasItem) atlas.getItem());
			player.getServerWorld().playSound(null, player.getBlockPos(),
					MapAtlasesMod.ATLAS_OPEN_SOUND_EVENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
		});
	}*/

	@SubscribeEvent
	public static void onServerStart(FMLServerAboutToStartEvent event) {
		server = event.getServer();
	}

	@SubscribeEvent
	public static void mapAtlasPlayerJoin(PlayerLoggedInEvent event) {
		if (!event.getPlayer().level.isClientSide) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.inventory);
			if (atlas.isEmpty()) return;
			List<MapData> mapStates = MapAtlasesAccessUtils.getAllMapDatasFromAtlas(player.level, atlas);
			for (MapData state : mapStates) {
				state.tickCarriedBy(player, atlas);
				state.getHoldingPlayer(player);
				Networking.sendToClient(new MapAtlasesInitAtlasS2CPacket(state), player);
				MapAtlases.LOGGER.info("Server Sent MapState: " + state.getId());
			}
		}
	}

	@SubscribeEvent
	public static void mapAtlasServerTick(ServerTickEvent event) {
		for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
			ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.inventory);
			if (!atlas.isEmpty()) {
				MapData activeState = MapAtlasesAccessUtils.getActiveAtlasMapData(
								player.getCommandSenderWorld(), atlas, player.getName().getString());
				if (activeState != null) {
					String playerName = player.getName().getString();
					if (!playerToActiveMapId.containsKey(playerName)
							|| playerToActiveMapId.get(playerName).compareTo(activeState.getId()) != 0) {
						playerToActiveMapId.put(playerName, activeState.getId());
						Networking.sendToClient(new MapAtlasesActiveStateChangePacket(activeState.getId()), player);
					}
				} else if (MapAtlasesAccessUtils.getEmptyMapCountFromItemStack(atlas) != 0) {
					MapAtlases.LOGGER.info("Null active MapData with non-empty Atlas");
				}


				List<MapData> mapStates = MapAtlasesAccessUtils.getAllMapDatasFromAtlas(player.getCommandSenderWorld(), atlas);

				// Maps are 128x128
				int playX = player.blockPosition().getX();
				int playZ = player.blockPosition().getZ();
				int minDist = Integer.MAX_VALUE;
				int scale = -1;

				for (MapData state : mapStates) {
					state.tickCarriedBy(player, atlas);
					((FilledMapItem) Items.FILLED_MAP).update(player.getCommandSenderWorld(), player, state);
					ItemStack map = MapAtlasesAccessUtils.createMapItemStackFromStrId(state.getId());
					IPacket<?> p = null;
					int tries = 0;
					while (p == null && tries < 10) {
						p = state.getUpdatePacket(map, player.getCommandSenderWorld(), player);
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

				if (!Config.getEnableEmptyMapEntryAndFill()) continue;
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
						ItemStack newMap = FilledMapItem.create(
								player.level,
								MathHelper.floor(player.getX()),
								MathHelper.floor(player.getZ()),
								(byte) scale,
								true,
								false);
						mapIds.add(FilledMapItem.getMapId(newMap));
						atlas.getTag().putIntArray("maps", mapIds);

						// Update the reference in the inventory
						MapAtlasesAccessUtils.setAllMatchingItemStacks(
								player.inventory.offhand, 1, Registration.MAP_ATLAS.get(), oldAtlasTagState, atlas);
						MapAtlasesAccessUtils.setAllMatchingItemStacks(
								player.inventory.items, 9, Registration.MAP_ATLAS.get(), oldAtlasTagState, atlas);

						// Play the sound
						player.level.playSound(null, player.blockPosition(),
								Registration.ATLAS_CREATE_MAP_SOUND_EVENT.get(),
								SoundCategory.PLAYERS, 1.0F, 1.0F);
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
