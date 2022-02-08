package lilypuree.mapatlases.network;

import lilypuree.mapatlases.util.MapAtlasesAccessUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MapAtlasesInitAtlasS2CPacket {
    private String mapId;
    private MapItemSavedData mapState;

    public MapAtlasesInitAtlasS2CPacket(FriendlyByteBuf buf) {
        mapId = buf.readUtf();
        mapState = MapItemSavedData.load(buf.readNbt());
    }

    public MapAtlasesInitAtlasS2CPacket(String mapId1, MapItemSavedData mapState1) {
        mapId = mapId1;
        mapState = mapState1;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(mapId);
        buf.writeNbt(mapState.save(new CompoundTag()));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
//                Minecraft client = Minecraft.getInstance();
                ServerPlayer player = ctx.get().getSender();
                ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.getInventory());
                mapState.tickCarriedBy(player, atlas);
                mapState.getHoldingPlayer(player);
                player.level.setMapData(mapId, mapState);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
