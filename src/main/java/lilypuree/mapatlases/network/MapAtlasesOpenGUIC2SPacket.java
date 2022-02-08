package lilypuree.mapatlases.network;

import lilypuree.mapatlases.MapAtlasesMod;
import lilypuree.mapatlases.item.MapAtlasItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class MapAtlasesOpenGUIC2SPacket {

    public ItemStack atlas;

    public MapAtlasesOpenGUIC2SPacket(ItemStack atlas1) {
        atlas = atlas1;
    }

    public MapAtlasesOpenGUIC2SPacket(FriendlyByteBuf buf) {
        atlas = buf.readItem();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(atlas);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            NetworkHooks.openGui(player, new SimpleMenuProvider(
                    (i, playerInventory, playerEntity) -> ((MapAtlasItem) atlas.getItem()).createMenu(i, playerInventory, playerEntity),
                    new TextComponent("atlas_gui")
            ), (b) -> ((MapAtlasItem) atlas.getItem()).writeScreenOpeningData((ServerPlayer) player, b));
            player.getLevel().playSound(null, player.blockPosition(),
                    MapAtlasesMod.ATLAS_OPEN_SOUND_EVENT, SoundSource.PLAYERS, 1.0f, 1.0f);
        });
        ctx.get().setPacketHandled(true);
    }
}
