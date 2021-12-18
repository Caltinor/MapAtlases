package pepjebs.dicemc.recipe;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.MapItem;
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
import pepjebs.dicemc.setup.Registration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MapAtlasCreateRecipe extends CustomRecipe{

    private Level world = null;

	public MapAtlasCreateRecipe(ResourceLocation id) {
		super(id);
	}

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        this.world = world;
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        ItemStack filledMap = ItemStack.EMPTY;
        for(int i = 0; i < inv.getContainerSize(); i++) {
            if (!inv.getItem(i).isEmpty()) {
                itemStacks.add(inv.getItem(i));
                if (inv.getItem(i).getItem() == Items.FILLED_MAP) {
                    filledMap = inv.getItem(i);
                }
            }
        }
        if (itemStacks.size() == 3) {
            List<Item> items = itemStacks.stream().map(ItemStack::getItem).collect(Collectors.toList());
            boolean hasAllCrafting =
                    items.containsAll(Arrays.asList(Items.FILLED_MAP, Items.SLIME_BALL, Items.BOOK)) ||
                            items.containsAll(Arrays.asList(Items.FILLED_MAP, Items.HONEY_BOTTLE, Items.BOOK));
            if (hasAllCrafting && !filledMap.isEmpty()) {
                MapItemSavedData state = MapItem.getSavedData(filledMap, world);
                if (state != null) return state.dimension == Level.OVERWORLD;
                /*if (state == null) return false;
                if (false) {
                    return state.dimension == World.OVERWORLD || state.dimension == World.END
                            || state.dimension == World.NETHER;
                } else {
                    return state.dimension == World.OVERWORLD;
                }*/
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack mapItemStack = null;
        for(int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).sameItem(new ItemStack(Items.FILLED_MAP))) {
                mapItemStack = inv.getItem(i);
            }
        }
        if (mapItemStack == null || world == null) {
            return ItemStack.EMPTY;
        }
        MapItemSavedData mapState = MapItem.getSavedData(mapItemStack, world);
        if (mapState == null) return ItemStack.EMPTY;
        Item mapAtlasItem = Registration.MAP_ATLAS.get();
        /*if (MapAtlasesMod.enableMultiDimMaps && mapState.dimension == World.END) {
            mapAtlasItem = Registry.ITEM.get(new ResourceLocation(MapAtlasesMod.MOD_ID, "end_atlas"));
        } else if (MapAtlasesMod.enableMultiDimMaps && mapState.dimension == World.NETHER) {
            mapAtlasItem = Registry.ITEM.get(new ResourceLocation(MapAtlasesMod.MOD_ID, "nether_atlas"));
        } else {
            mapAtlasItem = Registry.ITEM.get(new ResourceLocation(MapAtlasesMod.MOD_ID, "atlas"));
        }*/
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putIntArray("maps", new int[]{MapItem.getMapId(mapItemStack)});
        ItemStack atlasItemStack = new ItemStack(mapAtlasItem);
        atlasItemStack.setTag(compoundTag);
        return atlasItemStack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {return SERIALIZER;}
    
    @ObjectHolder(MapAtlases.MOD_ID+":atlas_create")
	public static SimpleRecipeSerializer<MapAtlasCreateRecipe> SERIALIZER;

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 3;
	}
}
