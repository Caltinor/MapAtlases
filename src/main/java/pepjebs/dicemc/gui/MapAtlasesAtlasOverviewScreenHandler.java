package pepjebs.dicemc.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ObjectHolder;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapAtlasesAtlasOverviewScreenHandler extends Container {
	@ObjectHolder(MapAtlases.MOD_ID+":gui_container")
	public static ContainerType<MapAtlasesAtlasOverviewScreenHandler> TYPE;

	public Map<Integer, List<Integer>> idsToCenters = new HashMap<>();

	public MapAtlasesAtlasOverviewScreenHandler(int syncId, PlayerInventory _playerInventory, PacketBuffer buf) {
		super(TYPE, syncId);
		if (!buf.isReadable()) return;
		int numToRead = buf.readInt();
		for (int i = 0; i < numToRead; i++) {
			idsToCenters.put(buf.readInt(), Arrays.asList(buf.readInt(), buf.readInt()));
		}
	}

	public MapAtlasesAtlasOverviewScreenHandler(int syncId, PlayerInventory _playerInventory, Map<Integer, List<Integer>> idsToCenters1) {
		super(TYPE, syncId);
		idsToCenters = idsToCenters1;
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.inventory) != ItemStack.EMPTY;
	}
}
