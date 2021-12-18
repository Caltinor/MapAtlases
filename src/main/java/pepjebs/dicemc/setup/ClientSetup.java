package pepjebs.dicemc.setup;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pepjebs.dicemc.gui.MapAtlasesAtlasOverviewScreen;
import pepjebs.dicemc.gui.MapAtlasesAtlasOverviewScreenHandler;

public class ClientSetup {
	
	public static KeyMapping displayMapGUIBinding;
    
	public static void init(final FMLClientSetupEvent event) {
		// Register client screen
        MenuScreens.register(MapAtlasesAtlasOverviewScreenHandler.TYPE, MapAtlasesAtlasOverviewScreen::new);
        // Register Keybind
        displayMapGUIBinding = new KeyMapping("key.map_atlases.open_minimap", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.map_atlases.minimap");
        ClientRegistry.registerKeyBinding(displayMapGUIBinding);
    }
}
