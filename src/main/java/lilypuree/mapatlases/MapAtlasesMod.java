package lilypuree.mapatlases;

import lilypuree.mapatlases.client.MapAtlasesClient;
import lilypuree.mapatlases.item.DummyFilledMap;
import lilypuree.mapatlases.item.MapAtlasItem;
import lilypuree.mapatlases.network.ModPacketHandler;
import lilypuree.mapatlases.recipe.MapAtlasCreateRecipe;
import lilypuree.mapatlases.recipe.MapAtlasesAddRecipe;
import lilypuree.mapatlases.recipe.MapAtlasesCutExistingRecipe;
import lilypuree.mapatlases.screen.MapAtlasesAtlasOverviewScreenHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MapAtlasesMod.MOD_ID)
public class MapAtlasesMod {

    public static final String MOD_ID = "map_atlases";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static MapAtlasesConfig CONFIG;

    public MapAtlasesMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(MapAtlasesClient::init);
        modEventBus.addGenericListener(RecipeSerializer.class, this::registerRecipeSerializers);
        modEventBus.addGenericListener(MenuType.class, this::registerContainers);
        modEventBus.addGenericListener(SoundEvent.class, this::registerSounds);
        modEventBus.addGenericListener(Item.class, this::registerItems);

        CONFIG = new MapAtlasesConfig();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MapAtlasesConfig.COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MapAtlasesConfig.CLIENT_CONFIG);
    }

    public static MapAtlasItem MAP_ATLAS;
    public static boolean enableMultiDimMaps = false;
    public static SimpleRecipeSerializer<MapAtlasCreateRecipe> MAP_ATLAS_CREATE_RECIPE;
    public static SimpleRecipeSerializer<MapAtlasesAddRecipe> MAP_ATLAS_ADD_RECIPE;
    public static SimpleRecipeSerializer<MapAtlasesCutExistingRecipe> MAP_ATLAS_CUT_RECIPE;
    public static MenuType<MapAtlasesAtlasOverviewScreenHandler> ATLAS_OVERVIEW_HANDLER;

    private static final ResourceLocation ATLAS_OPEN_SOUND_ID = new ResourceLocation(MOD_ID, "atlas_open");
    public static SoundEvent ATLAS_OPEN_SOUND_EVENT;
    private static final ResourceLocation ATLAS_PAGE_TURN_SOUND_ID = new ResourceLocation(MOD_ID, "atlas_page_turn");
    public static SoundEvent ATLAS_PAGE_TURN_SOUND_EVENT;

    private static final ResourceLocation ATLAS_CREATE_MAP_SOUND_ID = new ResourceLocation(MOD_ID, "atlas_create_map");
    public static SoundEvent ATLAS_CREATE_MAP_SOUND_EVENT;


    public void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
        ATLAS_OVERVIEW_HANDLER = IForgeMenuType.create(MapAtlasesAtlasOverviewScreenHandler::new);
        event.getRegistry().register(ATLAS_OVERVIEW_HANDLER.setRegistryName("gui_container"));
    }

    public void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        MAP_ATLAS_CREATE_RECIPE = new SimpleRecipeSerializer<>(MapAtlasCreateRecipe::new);
        MAP_ATLAS_ADD_RECIPE = new SimpleRecipeSerializer<>(MapAtlasesAddRecipe::new);
        MAP_ATLAS_CUT_RECIPE = new SimpleRecipeSerializer<>(MapAtlasesCutExistingRecipe::new);
        event.getRegistry().registerAll(
                MAP_ATLAS_CREATE_RECIPE.setRegistryName("crafting_atlas"),
                MAP_ATLAS_ADD_RECIPE.setRegistryName("adding_atlas"),
                MAP_ATLAS_CUT_RECIPE.setRegistryName("cutting_atlas")
        );
    }

    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        ATLAS_OPEN_SOUND_EVENT = new SoundEvent(ATLAS_OPEN_SOUND_ID);
        ATLAS_PAGE_TURN_SOUND_EVENT = new SoundEvent(ATLAS_PAGE_TURN_SOUND_ID);
        ATLAS_CREATE_MAP_SOUND_EVENT = new SoundEvent(ATLAS_CREATE_MAP_SOUND_ID);
        event.getRegistry().registerAll(
                ATLAS_OPEN_SOUND_EVENT.setRegistryName(ATLAS_OPEN_SOUND_ID),
                ATLAS_PAGE_TURN_SOUND_EVENT.setRegistryName(ATLAS_PAGE_TURN_SOUND_ID),
                ATLAS_CREATE_MAP_SOUND_EVENT.setRegistryName(ATLAS_CREATE_MAP_SOUND_ID)
        );
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1);
        MAP_ATLAS = new MapAtlasItem(properties);
        event.getRegistry().register(MAP_ATLAS.setRegistryName("atlas"));
        if (enableMultiDimMaps) {
            event.getRegistry().register(new MapAtlasItem(properties).setRegistryName("end_atlas"));
            event.getRegistry().register(new MapAtlasItem(properties).setRegistryName("nether_atlas"));
        }
        event.getRegistry().register(new DummyFilledMap(new Item.Properties()).setRegistryName("dummy_filled_map"));
    }

    public void setup(FMLCommonSetupEvent event) {
        ModPacketHandler.registerMessages();
    }
}
