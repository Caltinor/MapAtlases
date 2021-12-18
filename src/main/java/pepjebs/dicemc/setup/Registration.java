package pepjebs.dicemc.setup;

import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.item.DummyFilledMap;
import pepjebs.dicemc.item.MapAtlasItem;

public class Registration {
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MapAtlases.MOD_ID);
	private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MapAtlases.MOD_ID);
	
	public static void init() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
		SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	//Item Registration
	public static final RegistryObject<MapAtlasItem> MAP_ATLAS = ITEMS.register("atlas", MapAtlasItem::new);
	public static final RegistryObject<DummyFilledMap> DUMMY_FILLED_MAP = ITEMS.register("dummy_filled_map", DummyFilledMap::new);
	
	//Sound Registration
	public static final RegistryObject<SoundEvent> ATLAS_CREATE_MAP_SOUND_EVENT = SOUNDS.register("atlas_create_map" , () -> new SoundEvent(new ResourceLocation(MapAtlases.MOD_ID, "atlas_create_map")));
	public static final RegistryObject<SoundEvent> ATLAS_PAGE_TURN_SOUND_EVENT = SOUNDS.register("atlas_page_turn" , () -> new SoundEvent(new ResourceLocation(MapAtlases.MOD_ID, "atlas_page_turn")));
	public static final RegistryObject<SoundEvent> ATLAS_OPEN_SOUND_EVENT = SOUNDS.register("atlas_open" , () -> new SoundEvent(new ResourceLocation(MapAtlases.MOD_ID, "atlas_open")));
}
