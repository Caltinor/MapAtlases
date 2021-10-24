package pepjebs.dicemc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pepjebs.dicemc.config.Config;
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
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializers);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(ContainerType.class, this::registerContainers);

       // MinecraftForge.EVENT_BUS.register(this);
    }

    public void setup(final FMLCommonSetupEvent event) {
    	Networking.registerMessages();
        Config.register(FMLJavaModLoadingContext.get().getModEventBus(), ModLoadingContext.get());
    }

    public void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        event.getRegistry().registerAll(
                new SpecialRecipeSerializer<>(MapAtlasesCutExistingRecipe::new).setRegistryName("atlas_cut"),
                new SpecialRecipeSerializer<>(MapAtlasesAddRecipe::new).setRegistryName("atlas_add"),
                new SpecialRecipeSerializer<>(MapAtlasCreateRecipe::new).setRegistryName("atlas_create")
        );
    }

    public void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
    	event.getRegistry().registerAll(IForgeContainerType.create(MapAtlasesAtlasOverviewScreenHandler::new).setRegistryName("gui_container"));
    }
}
