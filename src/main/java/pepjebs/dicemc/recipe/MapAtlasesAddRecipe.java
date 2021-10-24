package pepjebs.dicemc.recipe;

import com.google.common.primitives.Ints;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.registries.ObjectHolder;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.config.Config;
import pepjebs.dicemc.setup.Registration;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;

import java.util.*;

public class MapAtlasesAddRecipe extends SpecialRecipe {

	private World world = null;

	public MapAtlasesAddRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		this.world = world;
		List<ItemStack> itemStacks = MapAtlasesAccessUtils.getItemStacksFromGrid(inv);
		ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromItemStacks(itemStacks);

		// Ensure there's an Atlas
		if (atlas.isEmpty()) return false;
		MapData sampleMap = MapAtlasesAccessUtils.getFirstMapDataFromAtlas(world, atlas);

		// Ensure only correct ingredients are present
		List<Item> additems = new ArrayList<>(Arrays.asList(Items.FILLED_MAP, Registration.MAP_ATLAS.get()));
		if (Config.getEnableEmptyMapEntryAndFill())
			additems.add(Items.MAP);
		if (!(itemStacks.size() > 1 && MapAtlasesAccessUtils.isListOnlyIngredients(
				itemStacks,
				additems)))
			return false;
		List<MapData> mapStates = MapAtlasesAccessUtils.getMapDatasFromItemStacks(world, itemStacks);

		// Ensure we're not trying to add too many Maps
		int empties = MapAtlasesAccessUtils.getEmptyMapCountFromItemStack(atlas);
		int mapCount = MapAtlasesAccessUtils.getMapCountFromItemStack(atlas);
		if (empties + mapCount + itemStacks.size() - 1 > Config.getMaxMapCount()) return false;

		// Ensure Filled Maps are all same Scale & Dimension
		if(!(MapAtlasesAccessUtils.areMapsSameScale(sampleMap, mapStates) &&
				MapAtlasesAccessUtils.areMapsSameDimension(sampleMap, mapStates))) return false;

		// Ensure there's only one Atlas
		return itemStacks.stream().filter(i ->
				i.sameItem(new ItemStack(Registration.MAP_ATLAS.get()))).count() == 1;
	}

	@Override
	public ItemStack assemble(CraftingInventory inv) {
		if (world == null) return ItemStack.EMPTY;
		List<ItemStack> itemStacks = MapAtlasesAccessUtils.getItemStacksFromGrid(inv);
		// Grab the Atlas in the Grid
		ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromItemStacks(itemStacks);
		// Get the Map Ids in the Grid
		Set<Integer> mapIds = MapAtlasesAccessUtils.getMapIdsFromItemStacks(world, itemStacks);
		// Set NBT Data
		int emptyMapCount = (int)itemStacks.stream().filter(i -> i.sameItem(new ItemStack(Items.MAP))).count();
		CompoundNBT compoundTag = atlas.getOrCreateTag();
		Set<Integer> existingMaps = new HashSet<>(Ints.asList(compoundTag.getIntArray("maps")));
		existingMaps.addAll(mapIds);
		compoundTag.putIntArray("maps", existingMaps.stream().mapToInt(i->i).toArray());
		compoundTag.putInt("empty", emptyMapCount + compoundTag.getInt("empty"));
		atlas.setTag(compoundTag);
		return atlas;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {return SERIALIZER;}

	@ObjectHolder(MapAtlases.MOD_ID+":atlas_add")
	public static SpecialRecipeSerializer<MapAtlasesAddRecipe> SERIALIZER;

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}
}
