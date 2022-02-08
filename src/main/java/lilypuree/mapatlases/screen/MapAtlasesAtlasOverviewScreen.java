package lilypuree.mapatlases.screen;

import lilypuree.mapatlases.MapAtlasesMod;
import lilypuree.mapatlases.client.MapAtlasesClient;
import lilypuree.mapatlases.client.MapAtlasesHUD;
import lilypuree.mapatlases.mixin.MapStateMemberAccessor;
import lilypuree.mapatlases.util.MapAtlasesAccessUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapAtlasesAtlasOverviewScreen extends AbstractContainerScreen<MapAtlasesAtlasOverviewScreenHandler> {

    private static final int ZOOM_BUCKET = 4;
    private static final int PAN_BUCKET = 25;

    private final ItemStack atlas;
    public Map<Integer, List<Integer>> idsToCenters;
    private int mouseXOffset = 0;
    private int mouseYOffset = 0;
    private int zoomValue = ZOOM_BUCKET;

    public MapAtlasesAtlasOverviewScreen(MapAtlasesAtlasOverviewScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(inventory);
        idsToCenters = menu.idsToCenters;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        renderBg(pPoseStack, pPartialTick, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        Minecraft client = this.minecraft;
        if (client == null || client.player == null || client.level == null) return;
        // Handle zooming
        int worldMapScaling = MapAtlasesMod.CONFIG.forceWorldMapScaling.get();
        // zoomLevel can be any of 0,1,2,3
        int zoomLevel = round(zoomValue, ZOOM_BUCKET) / ZOOM_BUCKET;
        zoomLevel = Math.max(zoomLevel, 0);
        zoomLevel = Math.min(zoomLevel, 3);
        // zoomLevelDim can be any of 1,3,5,7
        int zoomLevelDim = (2 * zoomLevel) + 1;
        MapAtlasesClient.setWorldMapZoomLevel(zoomLevelDim);
        // a function of worldMapScaling, zoomLevel, and textureSize
        float mapTextureScale = (float) ((worldMapScaling - (worldMapScaling / 8.0)) / (128.0 * zoomLevelDim));
        // Draw map background
        double y = (height / 2.0) - (worldMapScaling / 2.0);
        double x = (width / 2.0) - (worldMapScaling / 2.0);
        RenderSystem.setShaderTexture(0, MapAtlasesHUD.MAP_CHKRBRD);
        blit(
                matrices,
                (int) x,
                (int) y,
                0,
                0,
                worldMapScaling,
                worldMapScaling,
                worldMapScaling,
                worldMapScaling
        );
        // Draw maps, putting active map in middle of grid
        Map<String, MapItemSavedData> mapInfos = MapAtlasesAccessUtils.getAllMapInfoFromAtlas(client.level, atlas);
        MapItemSavedData activeState = client.level.getMapData(MapAtlasesClient.currentMapStateId);
        ItemStack activeFilledMap = MapAtlasesAccessUtils.createMapItemStackFromStrId(MapAtlasesClient.currentMapStateId);
        if (activeState == null) {
            if (!mapInfos.isEmpty())
                activeState = mapInfos.entrySet().stream().findFirst().get().getValue();
            else
                return;
        }
        int activeMapId = MapItem.getMapId(activeFilledMap);
        if (!idsToCenters.containsKey(activeMapId)) {
            MapAtlasesMod.LOGGER.warn("Client didn't have idsToCenters entry.");
            if (idsToCenters.isEmpty())
                return;
            activeMapId = idsToCenters.keySet().stream().findAny().get();
        }
        int activeXCenter = idsToCenters.get(activeMapId).get(0);
        int activeZCenter = idsToCenters.get(activeMapId).get(1);
        activeXCenter = activeXCenter +
                (round(mouseXOffset, PAN_BUCKET) / PAN_BUCKET * (1 << activeState.scale) * -128);
        activeZCenter = activeZCenter +
                (round(mouseYOffset, PAN_BUCKET) / PAN_BUCKET * (1 << activeState.scale) * -128);
        double mapTextY = y + (worldMapScaling / 18.0);
        double mapTextX = x + (worldMapScaling / 18.0);
        for (int i = zoomLevelDim - 1; i >= 0; i--) {
            for (int j = zoomLevelDim - 1; j >= 0; j--) {
                // Get the map for the GUI idx
                int iXIdx = i - (zoomLevelDim / 2);
                int jYIdx = j - (zoomLevelDim / 2);
                int reqXCenter = activeXCenter + (jYIdx * (1 << activeState.scale) * 128);
                int reqZCenter = activeZCenter + (iXIdx * (1 << activeState.scale) * 128);
                Map.Entry<String, MapItemSavedData> state = mapInfos.entrySet().stream()
                        .filter(m -> idsToCenters.get(MapAtlasesAccessUtils.getMapIntFromString(m.getKey())).get(0) == reqXCenter
                                && idsToCenters.get(MapAtlasesAccessUtils.getMapIntFromString(m.getKey())).get(1) == reqZCenter)
                        .findFirst().orElse(null);
                if (state == null) continue;
                // Draw the map
                double curMapTextX = mapTextX + (mapTextureScale * 128 * j);
                double curMapTextY = mapTextY + (mapTextureScale * 128 * i);
                MultiBufferSource.BufferSource vcp;
                vcp = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                matrices.pushPose();
                matrices.translate(curMapTextX, curMapTextY, 0.0);
                matrices.scale(mapTextureScale, mapTextureScale, 0);
                // Remove the off-map player icons temporarily during render
                Iterator<Map.Entry<String, MapDecoration>> it = ((MapStateMemberAccessor) state.getValue()).getDecoration().entrySet().iterator();
                List<Map.Entry<String, MapDecoration>> removed = new ArrayList<>();
                if (state.getKey().compareTo(MapItem.makeKey(activeMapId)) != 0) {
                    // Only remove the off-map icon if it's not the active map
                    while (it.hasNext()) {
                        Map.Entry<String, MapDecoration> e = it.next();
                        if (e.getValue().getType() == MapDecoration.Type.PLAYER_OFF_MAP
                                || e.getValue().getType() == MapDecoration.Type.PLAYER_OFF_LIMITS) {
                            it.remove();
                            removed.add(e);
                        }
                    }
                }
                client.gameRenderer.getMapRenderer()
                        .render(
                                matrices,
                                vcp,
                                activeMapId,
                                state.getValue(),
                                false,
                                Integer.parseInt("0000000011110000", 2)
                        );
                vcp.endBatch();
                matrices.popPose();
                // Re-add the off-map player icons after render
                for (Map.Entry<String, MapDecoration> e : removed) {
                    ((MapStateMemberAccessor) state.getValue()).getDecoration().put(e.getKey(), e.getValue());
                }
            }
        }
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            mouseXOffset += deltaX;
            mouseYOffset += deltaY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        zoomValue += -1 * amount;
        zoomValue = Math.max(zoomValue, -1 * ZOOM_BUCKET);
        zoomValue = Math.min(zoomValue, 4 * ZOOM_BUCKET);
        return true;
    }


    private int round(int num, int mod) {
        int t = num % mod;
        if (t < (int) Math.floor(mod / 2.0))
            return num - t;
        else
            return num + mod - t;
    }
}
