package pepjebs.dicemc.network;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import pepjebs.dicemc.events.ClientEvents;

public class MapAtlasesActiveStateChangePacket {
	public String activeMap;
	
	public MapAtlasesActiveStateChangePacket(String activeMap) {this.activeMap = activeMap;}
	public MapAtlasesActiveStateChangePacket(PacketBuffer buf) {this.activeMap = buf.readUtf();}
	public void toBytes(PacketBuffer buf) {buf.writeUtf(activeMap);}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {ClientEvents.currentMapStateId = activeMap;});
	}
}
