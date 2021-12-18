package pepjebs.dicemc.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ObjectHolder;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapAtlasesAtlasOverviewScreenHandler extends AbstractContainerMenu {
	@ObjectHolder(MapAtlases.MOD_ID+":gui_container")
	public static MenuType<MapAtlasesAtlasOverviewScreenHandler> TYPE;

	public Map<Integer, List<Integer>> idsToCenters = new HashMap<>();

    public MapAtlasesAtlasOverviewScreenHandler(int syncId, Inventory _playerInventory, FriendlyByteBuf buf) {
        super(TYPE, syncId);
        if (!buf.isReadable()) return;
        int numToRead = buf.readInt();
        for (int i = 0; i < numToRead; i++) {
            idsToCenters.put(buf.readInt(), Arrays.asList(buf.readInt(), buf.readInt()));
        }
    }

    public MapAtlasesAtlasOverviewScreenHandler(int syncId, Inventory _playerInventory, Map<Integer, List<Integer>> idsToCenters1) {
        super(TYPE, syncId);
        idsToCenters = idsToCenters1;
    }

	@Override
	public boolean stillValid(Player player) {
		return MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.inventory) != ItemStack.EMPTY;
	}
}
