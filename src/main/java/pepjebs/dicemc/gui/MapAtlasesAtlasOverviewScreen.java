package pepjebs.dicemc.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.events.ClientEvents;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;

import java.util.*;

import com.mojang.blaze3d.vertex.PoseStack;

public class MapAtlasesAtlasOverviewScreen extends AbstractContainerScreen<MapAtlasesAtlasOverviewScreenHandler> {
	private Minecraft client = Minecraft.getInstance();

	private static final int ZOOM_BUCKET = 4;
	private static final int PAN_BUCKET = 25;

	private final ItemStack atlas;
	public Map<Integer, List<Integer>> idsToCenters;
	private int mouseXOffset = 0;
	private int mouseYOffset = 0;
	private int zoomValue = ZOOM_BUCKET;

	private Map<Integer, List<Double>> zoomMapping;

    public MapAtlasesAtlasOverviewScreen(MapAtlasesAtlasOverviewScreenHandler handler, Inventory inventory, Component title) {
    	super(handler, inventory, title);
        atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(inventory);
        idsToCenters = handler.idsToCenters;
        zoomMapping = new HashMap<>();
        // mapTextureTranslate, mapTextureScale
        zoomMapping.put(1, Arrays.asList(1.0, 1.29));
        zoomMapping.put(3, Arrays.asList(55.0, 0.43));
        zoomMapping.put(5, Arrays.asList(33.0, 0.26));
        zoomMapping.put(7, Arrays.asList(24.0, 0.19));
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        renderBg(matrices, delta, mouseX, mouseY);
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
		zoomValue = Math.min(zoomValue, zoomMapping.size() * ZOOM_BUCKET);
		return true;
	}

	private int round(int num, int mod) {
		int t = num % mod;
		if (t < (int) Math.floor(mod / 2.0))
			return num - t;
		else
			return num + mod - t;
	}

	@Override
	protected void renderBg(PoseStack matrices, float p_230450_2_, int mouseX, int mouseY) {
		if (client == null || client.player == null || client.level == null) return;
        // Handle zooming
        int zoomLevel = round(zoomValue, ZOOM_BUCKET) / ZOOM_BUCKET;
        zoomLevel = Math.max(zoomLevel, 0);
        zoomLevel = Math.min(zoomLevel, zoomMapping.size() - 1);
        int loopBegin = -1 * zoomLevel;
        int loopEnd = zoomLevel + 1;
        zoomLevel = (2 * zoomLevel) + 1;
        List<Double> zoomingInfo = zoomMapping.get(zoomLevel);
        int mapTextureTranslate = zoomingInfo.get(0).intValue();
        float mapTextureScale = zoomingInfo.get(1).floatValue();
        // Draw map background
        double y = (height - getYSize()) / 2.0;
        double x = (width - getXSize()) / 2.0;
        client.getTextureManager().bindForSetup(HudEventHandler.MAP_CHKRBRD);
        blit(matrices, (int) x, (int) y,180,180, 0, 0);
        // Draw maps, putting active map in middle of grid
        List<MapItemSavedData> mapStates = MapAtlasesAccessUtils.getAllMapDatasFromAtlas(client.level, atlas);
        MapItemSavedData activeState = client.level.getMapData(MapItem.makeKey(ClientEvents.currentMapStateId));
        if (activeState == null) {
            if (!mapStates.isEmpty())
                activeState = mapStates.get(0);
            else
                return;
        }
        int activeMapId = ClientEvents.currentMapStateId;
        if (!idsToCenters.containsKey(activeMapId)) {
            MapAtlases.LOGGER.warn("Client didn't have idsToCenters entry.");
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
        for (int i = loopBegin; i < loopEnd; i++) {
            for (int j = loopBegin; j < loopEnd; j++) {
                double mapTextY = (height - getYSize()) / 2.0 + 8;
                double mapTextX = (width - getXSize()) / 2.0 + 8;
                // Get the map for the GUI idx
                int reqXCenter = activeXCenter + (j * (1 << activeState.scale) * 128);
                int reqZCenter = activeZCenter + (i * (1 << activeState.scale) * 128); 
                MapItemSavedData state = mapStates.stream()
                        .filter(m -> idsToCenters.get(MapAtlasesAccessUtils.getMapIntFromState(client.player.level, m)).get(0) == reqXCenter
                                && idsToCenters.get(MapAtlasesAccessUtils.getMapIntFromState(client.player.level, m)).get(1) == reqZCenter)
                        .findFirst().orElse(null);
                if (state == null) continue;
                int stateID = MapAtlasesAccessUtils.getMapIntFromState(client.player.level, state);
                // Draw the map
                mapTextX += (mapTextureTranslate * (j + loopEnd - 1));
                mapTextY += (mapTextureTranslate * (i + loopEnd - 1));
                MultiBufferSource.BufferSource vcp;
                vcp = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                matrices.pushPose();
                matrices.translate(mapTextX, mapTextY, 0.0);
                matrices.scale(mapTextureScale, mapTextureScale, 0);
                // Remove the off-map player icons temporarily during render
                Iterator<Map.Entry<String, MapDecoration>> it = state.decorations.entrySet().iterator();
                List<Map.Entry<String, MapDecoration>> removed = new ArrayList<>();
                if (stateID == MapAtlasesAccessUtils.getMapIntFromState(client.player.level, activeState)) {
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
                        .render(matrices, vcp, stateID, state, false, Integer.parseInt("0000000011110000", 2));
                vcp.endBatch();
                matrices.popPose();
                // Re-add the off-map player icons after render
                for (Map.Entry<String, MapDecoration> e : removed) {
                    state.decorations.put(e.getKey(), e.getValue());
                }
            }
        }
	}
}
