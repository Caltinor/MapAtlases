package pepjebs.dicemc.network;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import pepjebs.dicemc.item.MapAtlasItem;
import pepjebs.dicemc.setup.Registration;


public class MapAtlasesOpenGUIC2SPacket{
	public ItemStack atlas;

	public MapAtlasesOpenGUIC2SPacket(ItemStack atlas1) {
		atlas = atlas1;
	}

    public  MapAtlasesOpenGUIC2SPacket(FriendlyByteBuf buf) {
        atlas = buf.readItem();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(atlas);
    }
    
	public void handle(Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (!atlas.getItem().equals(Registration.MAP_ATLAS.get())) return;
			ServerPlayer player = ctx.get().getSender();
			NetworkHooks.openGui(player, new SimpleMenuProvider(
    				(i, playerInventory, playerEntity) -> ((MapAtlasItem)atlas.getItem()).createMenu(i, playerInventory, playerEntity),
    				new TextComponent("atlas_gui")
    				), (b) -> ((MapAtlasItem)atlas.getItem()).writeScreenOpeningData((ServerPlayer)player, b));
		});
		ctx.get().setPacketHandled(true);
	}
}
