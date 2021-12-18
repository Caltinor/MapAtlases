package pepjebs.dicemc.recipe;

import com.google.common.primitives.Ints;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.registries.ObjectHolder;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.config.Config;
import pepjebs.dicemc.item.MapAtlasItem;
import pepjebs.dicemc.setup.Registration;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;

import java.util.*;

public class MapAtlasesAddRecipe extends CustomRecipe {

    private Level world = null;

    public MapAtlasesAddRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        this.world = world;
        List<ItemStack> itemStacks = MapAtlasesAccessUtils.getItemStacksFromGrid(inv);
        ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromItemStacks(itemStacks);

        // Ensure there's an Atlas
        if (atlas.isEmpty()) return false;
        MapItemSavedData sampleMap = MapAtlasesAccessUtils.getFirstMapDataFromAtlas(world, atlas);

        // Ensure only correct ingredients are present
        List<Item> additems = new ArrayList<>(Arrays.asList(Items.FILLED_MAP, Registration.MAP_ATLAS.get()));
        if (Config.ENABLE_EMPTY_MAP_ENTRY_AND_FILL.get())
            additems.add(Items.MAP);
        if (!(itemStacks.size() > 1 && MapAtlasesAccessUtils.isListOnlyIngredients(
                itemStacks,
                additems)))
            return false;
        List<MapItemSavedData> mapStates = MapAtlasesAccessUtils.getMapDatasFromItemStacks(world, itemStacks);

        // Ensure we're not trying to add too many Maps
        int empties = MapAtlasesAccessUtils.getEmptyMapCountFromItemStack(atlas);
        int mapCount = MapAtlasesAccessUtils.getMapCountFromItemStack(atlas);
        if (empties + mapCount + itemStacks.size() - 1 > MapAtlasItem.getMaxMapCount()) return false;

        // Ensure Filled Maps are all same Scale & Dimension
        if(!(MapAtlasesAccessUtils.areMapsSameScale(sampleMap, mapStates) &&
                MapAtlasesAccessUtils.areMapsSameDimension(sampleMap, mapStates))) return false;

        // Ensure there's only one Atlas
        return itemStacks.stream().filter(i ->
                i.sameItem(new ItemStack(Registration.MAP_ATLAS.get()))).count() == 1;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        if (world == null) return ItemStack.EMPTY;
        List<ItemStack> itemStacks = MapAtlasesAccessUtils.getItemStacksFromGrid(inv);
        // Grab the Atlas in the Grid
        ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromItemStacks(itemStacks);
        // Get the Map Ids in the Grid
        Set<Integer> mapIds = MapAtlasesAccessUtils.getMapIdsFromItemStacks(world, itemStacks);
        // Set NBT Data
        int emptyMapCount = (int)itemStacks.stream().filter(i -> i.sameItem(new ItemStack(Items.MAP))).count();
        CompoundTag compoundTag = atlas.getOrCreateTag();
        Set<Integer> existingMaps = new HashSet<>(Ints.asList(compoundTag.getIntArray("maps")));
        existingMaps.addAll(mapIds);
        compoundTag.putIntArray("maps", existingMaps.stream().mapToInt(i->i).toArray());
        compoundTag.putInt("empty", emptyMapCount + compoundTag.getInt("empty"));
        atlas.setTag(compoundTag);
        return atlas;
    }

    @Override
	public RecipeSerializer<?> getSerializer() {return SERIALIZER;}
	
	@ObjectHolder(MapAtlases.MOD_ID+":atlas_add")
	public static SimpleRecipeSerializer<MapAtlasesAddRecipe> SERIALIZER;

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
}
