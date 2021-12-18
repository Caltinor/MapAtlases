package pepjebs.dicemc;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pepjebs.dicemc.gui.MapAtlasesAtlasOverviewScreenHandler;
import pepjebs.dicemc.recipe.MapAtlasCreateRecipe;
import pepjebs.dicemc.recipe.MapAtlasesAddRecipe;
import pepjebs.dicemc.recipe.MapAtlasesCutExistingRecipe;
import pepjebs.dicemc.setup.ClientSetup;
import pepjebs.dicemc.setup.Networking;
import pepjebs.dicemc.setup.Registration;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MapAtlases.MOD_ID)
public class MapAtlases
{
	public static final String MOD_ID = "mapatlases";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public MapAtlases() {    	
    	Registration.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(RecipeSerializer.class, this::registerRecipeSerializers);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(MenuType.class, this::registerContainers);
        
       // MinecraftForge.EVENT_BUS.register(this);
    }
    
    public void setup(final FMLCommonSetupEvent event) {
    	Networking.registerMessages();
    }
    
    public void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        event.getRegistry().registerAll(
                new SimpleRecipeSerializer<>(MapAtlasesCutExistingRecipe::new).setRegistryName("atlas_cut"),
                new SimpleRecipeSerializer<>(MapAtlasesAddRecipe::new).setRegistryName("atlas_add"),
                new SimpleRecipeSerializer<>(MapAtlasCreateRecipe::new).setRegistryName("atlas_create")
        );
    }
    
    public void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
    	event.getRegistry().registerAll(IForgeContainerType.create(MapAtlasesAtlasOverviewScreenHandler::new).setRegistryName("gui_container"));
    }

    
}
