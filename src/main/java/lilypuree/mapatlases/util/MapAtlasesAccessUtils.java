package lilypuree.mapatlases.util;

import lilypuree.mapatlases.MapAtlasesMod;
import lilypuree.mapatlases.mixin.MapStateMemberAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.*;
import java.util.stream.Collectors;

public class MapAtlasesAccessUtils {
    public static Map<String, Map.Entry<String, MapItemSavedData>> previousMapStates = new HashMap<>();

    public static boolean areMapsSameScale(MapItemSavedData testAgainst, List<MapItemSavedData> newMaps) {
        return newMaps.stream().filter(m -> m.scale == testAgainst.scale).count() == newMaps.size();
    }

    public static boolean areMapsSameDimension(MapItemSavedData testAgainst, List<MapItemSavedData> newMaps) {
        return newMaps.stream().filter(m -> m.dimension == testAgainst.dimension).count() == newMaps.size();
    }

    public static MapItemSavedData getFirstMapStateFromAtlas(Level world, ItemStack atlas) {
        return getMapStateByIndexFromAtlas(world, atlas, 0);
    }

    public static MapItemSavedData getMapStateByIndexFromAtlas(Level world, ItemStack atlas, int i) {
        if (atlas.getTag() == null) return null;
        int[] mapIds = Arrays.stream(atlas.getTag().getIntArray("maps")).toArray();
        if (i < 0 || i >= mapIds.length) return null;
        ItemStack map = createMapItemStackFromId(mapIds[i]);
        return MapItem.getSavedData(MapItem.getMapId(map), world);
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
        tag.putInt("map", MapAtlasesAccessUtils.getMapIntFromString(id));
        map.setTag(tag);
        return map;
    }

    public static int getMapIntFromString(String id) {
        if (id.isEmpty()) return -1;
        return Integer.parseInt(id.substring(4));
    }

    public static Map<String, MapItemSavedData> getAllMapInfoFromAtlas(Level world, ItemStack atlas) {
        if (atlas.getTag() == null) return new HashMap<>();
        int[] mapIds = Arrays.stream(atlas.getTag().getIntArray("maps")).toArray();
        Map<String, MapItemSavedData> mapStates = new HashMap<>();
        for (int mapId : mapIds) {
            String mapName = MapItem.makeKey(mapId);
            MapItemSavedData state = world.getMapData(mapName);
            if (state == null && world instanceof ServerLevel) {
                ItemStack map = createMapItemStackFromId(mapId);
                state = MapItem.getSavedData(map, world);
            }
            if (state != null) {
                mapStates.put(mapName, state);
            }
        }
        return mapStates;
    }

    public static List<String> getAllMapIdsFromAtlas(Level world, ItemStack atlas) {
        if (atlas.getTag() == null) return new ArrayList<>();
        String[] mapIds = (String[]) Arrays.stream(atlas.getTag().getIntArray("maps"))
                .mapToObj(m -> MapItem.makeKey(m)).toArray();
        return List.of(mapIds);
    }


    public static List<MapItemSavedData> getAllMapStatesFromAtlas(Level world, ItemStack atlas) {
        if (atlas.getTag() == null) return new ArrayList<>();
        int[] mapIds = Arrays.stream(atlas.getTag().getIntArray("maps")).toArray();
        List<MapItemSavedData> mapStates = new ArrayList<>();
        for (int mapId : mapIds) {
            MapItemSavedData state = world.getMapData(MapItem.makeKey(mapId));
            if (state == null && world instanceof ServerLevel) {
                ItemStack map = createMapItemStackFromId(mapId);
                state = MapItem.getSavedData(map, world);
            }
            if (state != null) {
                mapStates.add(state);
            }
        }
        return mapStates;
    }

    public static ItemStack getAtlasFromPlayerByConfig(Inventory inventory) {
        ItemStack itemStack = inventory.items.stream()
                .limit(9)
                .filter(i -> i.sameItem(new ItemStack(MapAtlasesMod.MAP_ATLAS)))
                .findFirst().orElse(null);

        if (MapAtlasesMod.CONFIG != null) {
            if (MapAtlasesMod.CONFIG.activationLocation.equals("INVENTORY")) {
                itemStack = inventory.items.stream()
                        .filter(i -> i.sameItem(new ItemStack(MapAtlasesMod.MAP_ATLAS)))
                        .findFirst().orElse(null);
            } else if (MapAtlasesMod.CONFIG.activationLocation.equals("HANDS")) {
                itemStack = null;
                ItemStack mainHand = inventory.items.get(inventory.selected);
                if (mainHand.getItem() == MapAtlasesMod.MAP_ATLAS)
                    itemStack = mainHand;
            }
        }
        if (itemStack == null && inventory.offhand.get(0).getItem() == MapAtlasesMod.MAP_ATLAS)
            itemStack = inventory.offhand.get(0);
        return itemStack != null ? itemStack.copy() : ItemStack.EMPTY;
    }


    public static ItemStack getAtlasFromPlayerByHotbar(Inventory inventory) {
        ItemStack itemStack = inventory.items.stream()
                .limit(9)
                .filter(i -> i.sameItem(new ItemStack(MapAtlasesMod.MAP_ATLAS)))
                .findFirst().orElse(null);

        if (itemStack == null && inventory.offhand.get(0).getItem() == MapAtlasesMod.MAP_ATLAS)
            itemStack = inventory.offhand.get(0);
        return itemStack != null ? itemStack.copy() : ItemStack.EMPTY;
    }

    public static ItemStack getAtlasFromItemStacks(List<ItemStack> itemStacks) {
        Optional<ItemStack> item = itemStacks.stream()
                .filter(i -> i.sameItem(new ItemStack(MapAtlasesMod.MAP_ATLAS))).findFirst();
        return item.orElse(ItemStack.EMPTY).copy();
    }

    public static List<MapItemSavedData> getMapStatesFromItemStacks(Level world, List<ItemStack> itemStacks) {
        return itemStacks.stream()
                .filter(i -> i.sameItem(new ItemStack(Items.FILLED_MAP)))
                .map(m -> MapItem.getSavedData(m, world))
                .collect(Collectors.toList());
    }

    public static Set<Integer> getMapIdsFromItemStacks(Level world, List<ItemStack> itemStacks) {
        return itemStacks.stream().map(MapItem::getMapId).collect(Collectors.toSet());
    }

    public static List<ItemStack> getItemStacksFromGrid(CraftingContainer inv) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
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

    public static Map.Entry<String, MapItemSavedData> getActiveAtlasMapState(Level world, ItemStack atlas, String playerName) {
        Map<String, MapItemSavedData> mapInfos = MapAtlasesAccessUtils.getAllMapInfoFromAtlas(world, atlas);
        for (Map.Entry<String, MapItemSavedData> state : mapInfos.entrySet()) {
            for (Map.Entry<String, MapDecoration> entry : ((MapStateMemberAccessor) state.getValue()).getDecoration().entrySet()) {
                MapDecoration icon = entry.getValue();
                // Entry.getKey is "icon-0" on client
                if (icon.getType() == MapDecoration.Type.PLAYER && entry.getKey().compareTo(playerName) == 0) {
                    previousMapStates.put(playerName, state);
                    return state;
                }
            }
        }
        if (previousMapStates.containsKey(playerName)) return previousMapStates.get(playerName);
        for (Map.Entry<String, MapItemSavedData> state : mapInfos.entrySet()) {
            for (Map.Entry<String, MapDecoration> entry : ((MapStateMemberAccessor) state.getValue()).getDecoration().entrySet()) {
                if (entry.getValue().getType() == MapDecoration.Type.PLAYER_OFF_MAP
                        && entry.getKey().compareTo(playerName) == 0) {
                    previousMapStates.put(playerName, state);
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
