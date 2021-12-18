package pepjebs.dicemc.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.setup.Registration;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;

public class MapAtlasesCutExistingRecipe extends CustomRecipe {

	public MapAtlasesCutExistingRecipe(ResourceLocation id) {
		super(id);
	}

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> list = NonNullList.create();
        for(int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack cur = inv.getItem(i).copy();
            if (cur.getItem().equals(Items.SHEARS)) {
                cur.hurt(1, new Random(), null);
            } else if (cur.getItem().equals(Registration.MAP_ATLAS.get()) && cur.getTag() != null) {
                boolean didRemoveFilled = false;
                if (MapAtlasesAccessUtils.getMapCountFromItemStack(cur) > 1) {
                    List<Integer> mapIds = Arrays.stream(cur.getTag()
                            .getIntArray("maps")).boxed().collect(Collectors.toList());
                    if (mapIds.size() > 0) {
                        mapIds.remove(mapIds.size() - 1);
                        cur.getTag().putIntArray("maps", mapIds);
                        didRemoveFilled = true;
                    }

				}
				if (MapAtlasesAccessUtils.getEmptyMapCountFromItemStack(cur) > 0 && !didRemoveFilled) {
					cur.getTag().putInt("empty", cur.getTag().getInt("empty") - 1);
				}
			}
			list.add(cur);
		}
		return list;
	}

	@Override
	public boolean matches(CraftingContainer inv, Level world) {
		ItemStack atlas = ItemStack.EMPTY;
		ItemStack shears = ItemStack.EMPTY;
		int size = 0;
		for(int i = 0; i < inv.getContainerSize(); i++) {
			if (!inv.getItem(i).isEmpty()) {
				size++;
				if (inv.getItem(i).getItem().equals(Registration.MAP_ATLAS.get())) {
					atlas = inv.getItem(i);
				} else if (inv.getItem(i).getItem().equals(Items.SHEARS)) {
					shears = inv.getItem(i);
				}
			}
		}
		return !atlas.isEmpty() && !shears.isEmpty() && size == 2;
	}

	@Override
	public ItemStack assemble(CraftingContainer inv) {
		ItemStack atlas = ItemStack.EMPTY;
		for(int i = 0; i < inv.getContainerSize(); i++) {
			if (!inv.getItem(i).isEmpty()) {
				if (inv.getItem(i).getItem().equals(Registration.MAP_ATLAS.get())) {
					atlas = inv.getItem(i);
				}
			}
		}
		if (atlas.getTag() == null) return ItemStack.EMPTY;
		if (MapAtlasesAccessUtils.getMapCountFromItemStack(atlas) > 1) {
			List<Integer> mapIds = Arrays.stream(atlas.getTag()
					.getIntArray("maps")).boxed().collect(Collectors.toList());
			if (mapIds.size() > 0) {
				int lastId = mapIds.remove(mapIds.size() - 1);
				return MapAtlasesAccessUtils.createMapItemStackFromId(lastId);
			}
		}
		if (MapAtlasesAccessUtils.getEmptyMapCountFromItemStack(atlas) > 0) {
			return new ItemStack(Items.MAP);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width + height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {return SERIALIZER;}
	
	@ObjectHolder(MapAtlases.MOD_ID+":atlas_cut")
	public static SimpleRecipeSerializer<MapAtlasesCutExistingRecipe> SERIALIZER;
}
