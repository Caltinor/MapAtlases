package pepjebs.dicemc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import pepjebs.dicemc.config.Config;
import pepjebs.dicemc.setup.Registration;

public class MapAtlasesAccessUtils {

    public static Map<String, MapData> previousMapDatas = new HashMap<>();

    public static boolean areMapsSameScale(MapData testAgainst, List<MapData> newMaps) {
        return newMaps.stream().filter(m -> m.scale == testAgainst.scale).count() == newMaps.size();
    }

    public static boolean areMapsSameDimension(MapData testAgainst, List<MapData> newMaps) {
        return newMaps.stream().filter(m -> m.dimension == testAgainst.dimension).count() == newMaps.size();
    }

    public static MapData getFirstMapDataFromAtlas(World world, ItemStack atlas) {
        return getMapDataByIndexFromAtlas(world, atlas, 0);
    }

    public static MapData getMapDataByIndexFromAtlas(World world, ItemStack atlas, int i) {
        if (atlas.getTag() == null) return null;
        int[] mapIds = Arrays.stream(atlas.getTag().getIntArray("maps")).toArray();
        if (i < 0 || i >= mapIds.length) return null;
        ItemStack map = createMapItemStackFromId(mapIds[i]);
        return FilledMapItem.getSavedData(map, world);
    }

    public static ItemStack createMapItemStackFromId(int id) {
        ItemStack map = new ItemStack(Items.FILLED_MAP);
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("map", id);
        map.setTag(tag);
        return map;
    }

    public static ItemStack createMapItemStackFromStrId(String id) {
        ItemStack map = new ItemStack(Items.FILLED_MAP);
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("map", Integer.parseInt(id.substring(4)));
        map.setTag(tag);
        return map;
    }

    public static List<MapData> getAllMapDatasFromAtlas(World world, ItemStack atlas) {
        if (atlas.getTag() == null) return new ArrayList<>();
        int[] mapIds = Arrays.stream(atlas.getTag().getIntArray("maps")).toArray();
        List<MapData> MapDatas = new ArrayList<>();
        for (int mapId : mapIds) {
            MapData state = world.getMapData(FilledMapItem.makeKey(mapId));
            if (state == null && world instanceof ServerWorld) {
                ItemStack map = createMapItemStackFromId(mapId);
                state = FilledMapItem.getOrCreateSavedData(map, world);
            }
            if (state != null) {
                MapDatas.add(state);
            }
        }
        return MapDatas;
    }

    public static ItemStack getAtlasFromPlayerByConfig(PlayerInventory inventory) {
        ItemStack itemStack =  inventory.items.stream()
                .limit(9)
                .filter(i -> i.sameItemStackIgnoreDurability(new ItemStack(Registration.MAP_ATLAS.get())))
                .findFirst().orElse(null);

        if (Config.FORCE_USE_IN_HANDS.get()) {
            itemStack = null;
            ItemStack mainHand = inventory.items.get(inventory.selected);
            if (mainHand.getItem() == Registration.MAP_ATLAS.get())
                itemStack = mainHand;
        }
        if (itemStack == null && inventory.offhand.get(0).getItem() == Registration.MAP_ATLAS.get())
            itemStack = inventory.offhand.get(0);
        return itemStack != null ? itemStack.copy() : ItemStack.EMPTY;
    }

    public static ItemStack getAtlasFromPlayerByHotbar(PlayerInventory inventory) {
        ItemStack itemStack =  inventory.items.stream()
                .limit(9)
                .filter(i -> i.sameItemStackIgnoreDurability(new ItemStack(Registration.MAP_ATLAS.get())))
                .findFirst().orElse(null);

        if (itemStack == null && inventory.offhand.get(0).getItem() == Registration.MAP_ATLAS.get())
            itemStack = inventory.offhand.get(0);
        return itemStack != null ? itemStack.copy() : ItemStack.EMPTY;
    }

    public static ItemStack getAtlasFromItemStacks(List<ItemStack> itemStacks) {
        Optional<ItemStack> item =  itemStacks.stream()
                .filter(i -> i.sameItem(new ItemStack(Registration.MAP_ATLAS.get()))).findFirst();
        return item.orElse(ItemStack.EMPTY).copy();
    }

    public static List<MapData> getMapDatasFromItemStacks(World world, List<ItemStack> itemStacks) {
        return itemStacks.stream()
                .filter(i -> i.sameItem(new ItemStack(Items.FILLED_MAP)))
                .map(m -> FilledMapItem.getOrCreateSavedData(m, world))
                .collect(Collectors.toList());
    }

    public static Set<Integer> getMapIdsFromItemStacks(World world, List<ItemStack> itemStacks) {
        return getMapDatasFromItemStacks(world, itemStacks).stream()
                .map(MapAtlasesAccessUtils::getMapIntFromState).collect(Collectors.toSet());
    }

    public static List<ItemStack> getItemStacksFromGrid(CraftingInventory inv) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for(int i = 0; i < inv.getContainerSize(); i++) {
            if (!inv.getItem(i).isEmpty()) {
                itemStacks.add(inv.getItem(i).copy());
            }
        }
        return itemStacks;
    }

    public static boolean isListOnlyIngredients(List<ItemStack> itemStacks, List<Item> items) {
        return itemStacks.stream().filter(is -> {
            for (Item i : items) {
                if (i == is.getItem()) return true;
            }
            return false;
        }).count() == itemStacks.size();
    }

    public static int getMapIntFromState(MapData MapData) {
        String mapId = MapData.getId();
        return Integer.parseInt(mapId.substring(4));
    }

    public static MapData getActiveAtlasMapData(World world, ItemStack atlas, String playerName) {
        List<MapData> MapDatas = getAllMapDatasFromAtlas(world, atlas);
        for (MapData state : MapDatas) {
            for (Map.Entry<String, MapDecoration> entry : state.decorations.entrySet()) {
                MapDecoration icon = entry.getValue();
                // Entry.getKey is "icon-0" on client
                if (icon.getType() == MapDecoration.Type.PLAYER && entry.getKey().compareTo(playerName) == 0) {
                    previousMapDatas.put(playerName, state);
                    return state;
                }
            }
        }
        if (previousMapDatas.containsKey(playerName)) return previousMapDatas.get(playerName);
        for (MapData state : MapDatas) {
            for (Map.Entry<String, MapDecoration> entry : state.decorations.entrySet()) {
                if (entry.getValue().getType() == MapDecoration.Type.PLAYER_OFF_MAP
                        && entry.getKey().compareTo(playerName) == 0) {
                    previousMapDatas.put(playerName, state);
                    return state;
                }
            }
        }
        return null;
    }

    public static int getEmptyMapCountFromItemStack(ItemStack atlas) {
        CompoundNBT tag = atlas.getTag();
        return tag != null && tag.contains("empty") ? tag.getInt("empty") : 0;
    }

    public static int getMapCountFromItemStack(ItemStack atlas) {
        CompoundNBT tag = atlas.getTag();
        return tag != null && tag.contains("maps") ? tag.getIntArray("maps").length : 0;
    }

    public static NonNullList<ItemStack> setAllMatchingItemStacks(
            NonNullList<ItemStack> itemStacks,
            int size,
            Item searchingItem,
            String searchingTag,
            ItemStack newItemStack) {
        for (int i = 0; i < size; i++) {
            if (itemStacks.get(i).getItem() == searchingItem
                    && itemStacks.get(i)
                    .getOrCreateTag().toString().compareTo(searchingTag) == 0) {
                itemStacks.set(i, newItemStack);
            }
        }
        return itemStacks;
    }
}
