package pepjebs.dicemc.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.config.Config;
import pepjebs.dicemc.events.ClientEvents;
import pepjebs.dicemc.setup.Registration;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;

@Mod.EventBusSubscriber(modid=MapAtlases.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class HudEventHandler {
	public static final ResourceLocation MAP_CHKRBRD =
            new ResourceLocation("textures/map/map_background_checkerboard.png");
    private static Minecraft client = Minecraft.getInstance();
    private static Integer currentMapId = null;
	
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onHUD(RenderGameOverlayEvent.Post event) {
		if (event.getType() == ElementType.ALL) {
			ItemStack atlas = ItemStack.EMPTY;
			if (!(atlas = shouldDraw(Minecraft.getInstance())).isEmpty()) {
				renderMapHUDFromItemStack(event.getMatrixStack(), atlas, Minecraft.getInstance().gameRenderer.getMapRenderer());
			}
		}
	}
	
	private static ItemStack shouldDraw(Minecraft mc) {
        // Forcibly only render on Overworld since player trackers don't disappear from Overworld
        // in other dimensions in vanilla MC
        if (mc.player == null || !mc.player.level.dimension().equals(Level.OVERWORLD)) return ItemStack.EMPTY;
        // Check config disable
        if (!Config.DRAW_MINIMAP_HUD.get()) return ItemStack.EMPTY;
        // Check F3 menu displayed
        if (mc.options.renderDebug) return ItemStack.EMPTY;
        // Check the hot-bar for an Atlas
        return MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(mc.player.getInventory());
    }
	
    private static void renderMapHUDFromItemStack(PoseStack matrices, ItemStack atlas, MapRenderer mapRenderer) {
        if (client.level == null ||client.player == null) {
            MapAtlases.LOGGER.warn("renderMapHUDFromItemStack: Current map id - null (client.world)");
            return;
        }
        Integer mapId = ClientEvents.currentMapStateId;
        MapItemSavedData state = client.level.getMapData(MapItem.makeKey(mapId));
        if (state == null) {
            if (currentMapId != null) {
                MapAtlases.LOGGER.warn("renderMapHUDFromItemStack: Current map id - null (state)");
                currentMapId = null;
            }
            return;
        }
        if (currentMapId == null || mapId.compareTo(currentMapId) != 0) {
            if (currentMapId != null) {
                client.level.playLocalSound(client.player.getX(), client.player.getY(), client.player.getZ(),
                        Registration.ATLAS_PAGE_TURN_SOUND_EVENT.get(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
            }
            currentMapId = mapId;
        }
        // Draw map background
        int mapScaling = 64;
        mapScaling = Config.FORCE_MINIMAP_SCALING.get();
        int y = 0;
        if (!client.player.getActiveEffects().isEmpty()) {
            y = 26;
        }
        int x = client.getWindow().getGuiScaledWidth()-mapScaling;
        client.getTextureManager().bindForSetup(MAP_CHKRBRD);
        GuiComponent.blit(matrices,x,y,0,0,mapScaling,mapScaling, mapScaling, mapScaling);
        client.getTextureManager().bindForSetup(GuiComponent.GUI_ICONS_LOCATION);
        // Draw map data
        x += (mapScaling / 16) - (mapScaling / 64);
        y += (mapScaling / 16) - (mapScaling / 64);;
        matrices.pushPose();        
        MultiBufferSource.BufferSource vcp;
        vcp = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        matrices.translate(x, y, 0.0);
        // Prepare yourself for some magic numbers
        matrices.scale((float) mapScaling / 142, (float) mapScaling / 142, 0);
        mapRenderer.render(matrices, vcp, mapId, state, false, Integer.parseInt("0000000011110000", 2));
        vcp.endBatch();
        matrices.popPose();
    }
}
