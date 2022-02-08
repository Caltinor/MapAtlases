package lilypuree.mapatlases.client;

import lilypuree.mapatlases.MapAtlasesMod;
import lilypuree.mapatlases.util.MapAtlasesAccessUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapAtlasesHUD extends GuiComponent {

    public static final ResourceLocation MAP_CHKRBRD =
            new ResourceLocation("minecraft:textures/map/map_background_checkerboard.png");
    private static Minecraft client;
    private static MapRenderer mapRenderer;
    private static String currentMapId = "";

    public MapAtlasesHUD() {
        client = Minecraft.getInstance();
        mapRenderer = client.gameRenderer.getMapRenderer();
    }

    public void render(PoseStack matrices) {
        ItemStack atlas;
        if (!(atlas = shouldDraw(client)).isEmpty()) {
            renderMapHUDFromItemStack(matrices, atlas);
        }
    }

    private ItemStack shouldDraw(Minecraft client) {
        // Forcibly only render on Overworld since player trackers don't disappear from Overworld
        // in other dimensions in vanilla MC
        if (client.player == null || !client.player.level.dimension().equals(Level.OVERWORLD)) return ItemStack.EMPTY;
        Inventory inv = client.player.getInventory();
        // Check config disable
        if (MapAtlasesMod.CONFIG != null && !MapAtlasesMod.CONFIG.drawMiniMapHUD.get()) return ItemStack.EMPTY;
        // Check F3 menu displayed
        if (client.options.renderDebug) return ItemStack.EMPTY;
        // Check the hot-bar for an Atlas
        return MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(client.player.getInventory());
    }

    private void renderMapHUDFromItemStack(PoseStack matrices, ItemStack atlas) {
        // Handle early returns
        if (client.level == null || client.player == null) {
            MapAtlasesMod.LOGGER.warn("renderMapHUDFromItemStack: Current map id - null (client.world)");
            return;
        }
        String curMapId = MapAtlasesClient.currentMapStateId;
        MapItemSavedData state = client.level.getMapData(MapAtlasesClient.currentMapStateId);
        if (curMapId == null || state == null) {
            if (currentMapId != null) {
                MapAtlasesMod.LOGGER.warn("renderMapHUDFromItemStack: Current map id - null (state)");
                currentMapId = null;
            }
            return;
        }
        // Update client current map id
        if (currentMapId == null || curMapId.compareTo(currentMapId) != 0) {
            if (currentMapId != null && currentMapId.compareTo("") != 0) {
                client.level.playLocalSound(client.player.getX(), client.player.getY(), client.player.getZ(),
                        MapAtlasesMod.ATLAS_PAGE_TURN_SOUND_EVENT, SoundSource.PLAYERS, 1.0F, 1.0F, false);
            }
            currentMapId = curMapId;
        }
        // Set zoom-level for map icons
        MapAtlasesClient.setWorldMapZoomLevel(1);
        // Draw map background
        int mapScaling = 64;
        if (MapAtlasesMod.CONFIG != null) {
            mapScaling = MapAtlasesMod.CONFIG.forceMiniMapScaling.get();
        }
        int y = 0;
        if (!client.player.getActiveEffects().isEmpty()) {
            y = 26;
        }
        if (MapAtlasesMod.CONFIG.miniMapAnchoring.get().isLower()) {
            y = client.getWindow().getGuiScaledHeight() - mapScaling;
        }
        y += MapAtlasesMod.CONFIG.miniMapVerticalOffset.get();
        int x = client.getWindow().getGuiScaledWidth() - mapScaling;
        if (MapAtlasesMod.CONFIG.miniMapAnchoring.get().isLeft()) {
            x = 0;
        }
        x += MapAtlasesMod.CONFIG.miniMapHorizontalOffset.get();
        RenderSystem.setShaderTexture(0, MAP_CHKRBRD);
        blit(matrices, x, y, 0, 0, mapScaling, mapScaling, mapScaling, mapScaling);

        // Draw map data
        x += (mapScaling / 16) - (mapScaling / 64);
        y += (mapScaling / 16) - (mapScaling / 64);
        MultiBufferSource.BufferSource vcp;
        vcp = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        matrices.pushPose();
        matrices.translate(x, y, 0.0);
        // Prepare yourself for some magic numbers
        matrices.scale((float) mapScaling / 142, (float) mapScaling / 142, 0);
        mapRenderer.render(
                matrices,
                vcp,
                MapAtlasesAccessUtils.getMapIntFromString(curMapId),
                state,
                false,
                Integer.parseInt("0000000011110000", 2)
        );
        vcp.endBatch();
        matrices.popPose();
    }


    private double mapRange(double a1, double a2, double b1, double b2, double s) {
        return b1 + ((s - a1) * (b2 - b1)) / (a2 - a1);
    }
}
