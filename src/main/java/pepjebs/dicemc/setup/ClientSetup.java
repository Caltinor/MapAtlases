package pepjebs.dicemc.setup;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.gui.MapAtlasesAtlasOverviewScreen;
import pepjebs.dicemc.gui.MapAtlasesAtlasOverviewScreenHandler;

public class ClientSetup {

	public static KeyBinding displayMapGUIBinding;

	public static void init(final FMLClientSetupEvent event) {
		// Register client screen
		ScreenManager.register(MapAtlasesAtlasOverviewScreenHandler.TYPE, MapAtlasesAtlasOverviewScreen::new);
		// Register Keybind
		displayMapGUIBinding = new KeyBinding("key." + MapAtlases.MOD_ID + ".open_minimap", InputMappings.Type.KEYSYM,
				GLFW.GLFW_KEY_M, "category." + MapAtlases.MOD_ID + ".minimap");
		ClientRegistry.registerKeyBinding(displayMapGUIBinding);
	}
}
