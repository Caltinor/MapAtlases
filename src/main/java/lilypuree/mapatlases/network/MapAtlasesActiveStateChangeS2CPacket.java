package lilypuree.mapatlases.network;

import lilypuree.mapatlases.client.MapAtlasesClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MapAtlasesActiveStateChangeS2CPacket {
    public String activeMap;

    public MapAtlasesActiveStateChangeS2CPacket(String activeMap) {this.activeMap = activeMap;}
    public MapAtlasesActiveStateChangeS2CPacket(FriendlyByteBuf buf) {this.activeMap = buf.readUtf();}
    public void toBytes(FriendlyByteBuf buf) {buf.writeUtf(activeMap);}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            MapAtlasesClient.currentMapStateId = activeMap;});
        ctx.get().setPacketHandled(true);
    }
}
