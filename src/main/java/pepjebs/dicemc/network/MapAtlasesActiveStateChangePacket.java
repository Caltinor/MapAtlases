package pepjebs.dicemc.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import pepjebs.dicemc.events.ClientEvents;

public class MapAtlasesActiveStateChangePacket {
	public int activeMap;
	
	public MapAtlasesActiveStateChangePacket(int activeMap) {this.activeMap = activeMap;}
	public MapAtlasesActiveStateChangePacket(FriendlyByteBuf buf) {this.activeMap = buf.readInt();}
	public void toBytes(FriendlyByteBuf buf) {buf.writeInt(activeMap);}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {ClientEvents.currentMapStateId = activeMap;});
		ctx.get().setPacketHandled(true);
	}
}
