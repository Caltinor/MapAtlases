package lilypuree.mapatlases.client;

import lilypuree.mapatlases.MapAtlasesMod;
import lilypuree.mapatlases.screen.MapAtlasesAtlasOverviewScreen;
import com.mojang.blaze3d.platform.InputConstants;
import lilypuree.mapatlases.util.MapAtlasesAccessUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

public class MapAtlasesClient {
    private static final ThreadLocal<Integer> worldMapZoomLevel = new ThreadLocal<>();

    public static KeyMapping displayMapGUIBinding;
    public static String currentMapStateId = null;

    public static void init(final FMLClientSetupEvent event) {
        // Register client screen
        MenuScreens.register(MapAtlasesMod.ATLAS_OVERVIEW_HANDLER, MapAtlasesAtlasOverviewScreen::new);
        // Register Keybind
        displayMapGUIBinding = new KeyMapping("key.map_atlases.open_minimap", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.map_atlases.minimap");
        ClientRegistry.registerKeyBinding(displayMapGUIBinding);

        MapAtlasHUDHandler.mapAtlasesAtlasHUD = new MapAtlasesHUD();
    }



    public static int getWorldMapZoomLevel() {
        if (worldMapZoomLevel.get() == null) return 1;
        return worldMapZoomLevel.get();
    }

    public static void setWorldMapZoomLevel(int i) {
        worldMapZoomLevel.set(i);
    }
}
