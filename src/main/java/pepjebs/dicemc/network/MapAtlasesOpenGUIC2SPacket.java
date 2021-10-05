package pepjebs.dicemc.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import pepjebs.dicemc.item.MapAtlasItem;
import pepjebs.dicemc.setup.Registration;


public class MapAtlasesOpenGUIC2SPacket{
	public ItemStack atlas;

	public MapAtlasesOpenGUIC2SPacket(ItemStack atlas1) {
		atlas = atlas1;
	}

	public  MapAtlasesOpenGUIC2SPacket(PacketBuffer buf) {
		atlas = buf.readItem();
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeItem(atlas);
	}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (!atlas.getItem().equals(Registration.MAP_ATLAS.get())) return;
			ServerPlayerEntity player = ctx.get().getSender();
			NetworkHooks.openGui(player, new SimpleNamedContainerProvider(
					(i, playerInventory, playerEntity) -> ((MapAtlasItem)atlas.getItem()).createMenu(i, playerInventory, playerEntity),
					new StringTextComponent("atlas_gui")
					), (b) -> ((MapAtlasItem)atlas.getItem()).writeScreenOpeningData((ServerPlayerEntity)player, b));
		});
	}
}
