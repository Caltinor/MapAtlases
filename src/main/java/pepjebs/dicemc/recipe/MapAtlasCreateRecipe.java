package pepjebs.dicemc.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.FilledMapItem;
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
import pepjebs.dicemc.setup.Registration;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MapAtlasCreateRecipe extends SpecialRecipe{

    private World world = null;

    public MapAtlasCreateRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
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
                MapData state = FilledMapItem.getOrCreateSavedData(filledMap, world);
                if (state != null) return state.dimension == World.OVERWORLD;
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
    public ItemStack assemble(CraftingInventory inv) {
        ItemStack mapItemStack = null;
        for(int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).sameItem(new ItemStack(Items.FILLED_MAP))) {
                mapItemStack = inv.getItem(i);
            }
        }
        if (mapItemStack == null || world == null) {
            return ItemStack.EMPTY;
        }
        MapData mapState = FilledMapItem.getSavedData(mapItemStack, world);
        if (mapState == null) return ItemStack.EMPTY;
        Item mapAtlasItem = Registration.MAP_ATLAS.get();
        /*if (MapAtlasesMod.enableMultiDimMaps && mapState.dimension == World.END) {
            mapAtlasItem = Registry.ITEM.get(new ResourceLocation(MapAtlasesMod.MOD_ID, "end_atlas"));
        } else if (MapAtlasesMod.enableMultiDimMaps && mapState.dimension == World.NETHER) {
            mapAtlasItem = Registry.ITEM.get(new ResourceLocation(MapAtlasesMod.MOD_ID, "nether_atlas"));
        } else {
            mapAtlasItem = Registry.ITEM.get(new ResourceLocation(MapAtlasesMod.MOD_ID, "atlas"));
        }*/
        CompoundNBT compoundTag = new CompoundNBT();
        compoundTag.putIntArray("maps", new int[]{MapAtlasesAccessUtils.getMapIntFromState(mapState)});
        ItemStack atlasItemStack = new ItemStack(mapAtlasItem);
        atlasItemStack.setTag(compoundTag);
        return atlasItemStack;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {return SERIALIZER;}
    
    @ObjectHolder(MapAtlases.MOD_ID+":atlas_create")
	public static SpecialRecipeSerializer<MapAtlasCreateRecipe> SERIALIZER;

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }
}
