package pepjebs.dicemc.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.config.Config;
import pepjebs.dicemc.setup.Registration;

import java.util.*;
import java.util.stream.Collectors;

public class MapAtlasesAccessUtils {

    public static Map<String, MapItemSavedData> previousMapDatas = new HashMap<>();

    public static boolean areMapsSameScale(MapItemSavedData testAgainst, List<MapItemSavedData> newMaps) {
        return newMaps.stream().filter(m -> m.scale == testAgainst.scale).count() == newMaps.size();
    }

    public static boolean areMapsSameDimension(MapItemSavedData testAgainst, List<MapItemSavedData> newMaps) {
        return newMaps.stream().filter(m -> m.dimension == testAgainst.dimension).count() == newMaps.size();
    }

    public static MapItemSavedData getFirstMapDataFromAtlas(Level world, ItemStack atlas) {
        return getMapDataByIndexFromAtlas(world, atlas, 0);
    }

    public static MapItemSavedData getMapDataByIndexFromAtlas(Level world, ItemStack atlas, int i) {
        if (atlas.getTag() == null) return null;
        int[] mapIds = Arrays.stream(atlas.getTag().getIntArray("maps")).toArray();
        if (i < 0 || i >= mapIds.length) return null;
        ItemStack map = createMapItemStackFromId(mapIds[i]);
        return MapItem.getSavedData(map, world);
    }

    public static ItemStack createMapItemStackFromId(int id) {
        ItemStack map = new ItemStack(Items.FILLED_MAP);
        CompoundTag tag = new CompoundTag();
        tag.putInt("map", id);
        map.setTag(tag);
        return map;
    }

    public static ItemStack createMapItemStackFromStrId(String id) {
        ItemStack map = new ItemStack(Items.FILLED_MAP);
        CompoundTag tag = new CompoundTag();
        tag.putInt("map", Integer.parseInt(id.substring(4)));
        map.setTag(tag);
        return map;
    }

    public static List<MapItemSavedData> getAllMapDatasFromAtlas(Level world, ItemStack atlas) {
        if (atlas.getTag() == null) return new ArrayList<>();
        int[] mapIds = Arrays.stream(atlas.getTag().getIntArray("maps")).toArray();
        List<MapItemSavedData> MapDatas = new ArrayList<>();
        for (int mapId : mapIds) {
            MapItemSavedData state = world.getMapData(MapItem.makeKey(mapId));
            if (state == null && world instanceof ServerLevel) {
                ItemStack map = createMapItemStackFromId(mapId);
                state = MapItem.getSavedData(map, world);
            }
            if (state != null) {
                MapDatas.add(state);
            }
        }
        return MapDatas;
    }

    public static ItemStack getAtlasFromPlayerByConfig(Inventory inventory) {
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

    public static ItemStack getAtlasFromPlayerByHotbar(Inventory inventory) {
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

    public static List<MapItemSavedData> getMapDatasFromItemStacks(Level world, List<ItemStack> itemStacks) {
        return itemStacks.stream()
                .filter(i -> i.sameItem(new ItemStack(Items.FILLED_MAP)))
                .map(m -> MapItem.getSavedData(m, world))
                .collect(Collectors.toList());
    }

    public static Set<Integer> getMapIdsFromItemStacks(Level world, List<ItemStack> itemStacks) {
        return itemStacks.stream().map((i) -> MapItem.getMapId(i)).collect(Collectors.toSet());
    }
    
    public static List<Integer> getMapIdsFromAtlas(Level world, ItemStack atlas) {
    	if (!atlas.getItem().equals(Registration.MAP_ATLAS.get())) return new ArrayList<>();
    	return atlas.hasTag() ? Arrays.stream(atlas.getTag().getIntArray("maps")).boxed().collect(Collectors.toList()) : new ArrayList<>();
    }

    public static List<ItemStack> getItemStacksFromGrid(CraftingContainer inv) {
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

    public static MapItemSavedData getActiveAtlasMapData(Level world, ItemStack atlas, String playerName) {
        List<MapItemSavedData> MapDatas = getAllMapDatasFromAtlas(world, atlas);
        for (MapItemSavedData state : MapDatas) {
            for (Map.Entry<String, MapDecoration> entry : state.getDecorations()) {
                MapDecoration icon = entry.getValue();
                // Entry.getKey is "icon-0" on client
                if (icon.getType() == MapDecoration.Type.PLAYER && entry.getKey().compareTo(playerName) == 0) {
                    previousMapDatas.put(playerName, state);
                    return state;
                }
            }
        }
        if (previousMapDatas.containsKey(playerName)) return previousMapDatas.get(playerName);
        for (MapItemSavedData state : MapDatas) {
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
        CompoundTag tag = atlas.getTag();
        return tag != null && tag.contains("empty") ? tag.getInt("empty") : 0;
    }

    public static int getMapCountFromItemStack(ItemStack atlas) {
        CompoundTag tag = atlas.getTag();
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
