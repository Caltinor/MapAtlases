package pepjebs.dicemc.network;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.network.NetworkEvent;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;

public class MapAtlasesInitAtlasS2CPacket{
    private MapData mapState;

    public MapAtlasesInitAtlasS2CPacket(PacketBuffer buf){
    	int mapId = buf.readInt();
        mapState = new MapData("map_" + mapId);
        mapState.deserializeNBT(buf.readAnySizeNbt());
    }

    public MapAtlasesInitAtlasS2CPacket(MapData mapState1) {
        mapState = mapState1;
    }

    public void toBytes(PacketBuffer buf) {
        CompoundNBT mapAsTag = new CompoundNBT();
        mapState.save(mapAsTag);
        buf.writeInt(MapAtlasesAccessUtils.getMapIntFromState(mapState));
        buf.writeNbt(mapAsTag);
    }

    public MapData getMapState() {return this.mapState;}

	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (ctx.get().getSender() != null)
				ctx.get().getSender().level.setMapData(mapState);
		});
		return true;
	}
}
