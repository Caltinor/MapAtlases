package lilypuree.mapatlases.screen;

import lilypuree.mapatlases.MapAtlasesMod;
import lilypuree.mapatlases.util.MapAtlasesAccessUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapAtlasesAtlasOverviewScreenHandler extends AbstractContainerMenu {
    public Map<Integer, List<Integer>> idsToCenters = new HashMap<>();

    public MapAtlasesAtlasOverviewScreenHandler(int syncID, Inventory inventory, FriendlyByteBuf buf) {
        super(MapAtlasesMod.ATLAS_OVERVIEW_HANDLER, syncID);
        if (!buf.isReadable()) return;
        int numToRead = buf.readInt();
        for (int i = 0; i < numToRead; i++) {
            idsToCenters.put(buf.readInt(), Arrays.asList(buf.readInt(), buf.readInt()));
        }
    }

    public MapAtlasesAtlasOverviewScreenHandler(int syncId, Inventory _playerInventory, Map<Integer, List<Integer>> idsToCenters1) {
        super(MapAtlasesMod.ATLAS_OVERVIEW_HANDLER, syncId);
        idsToCenters = idsToCenters1;
    }

    @Override
    public boolean stillValid(Player player) {
        return MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.getInventory()) != ItemStack.EMPTY;
    }
}
