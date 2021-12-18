package pepjebs.dicemc.network;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.network.NetworkEvent.Context;

public class MapAtlasesInitAtlasS2CPacket{
    private MapItemSavedData mapData;
    private int id;

    public MapAtlasesInitAtlasS2CPacket(FriendlyByteBuf buf){
    	id = buf.readInt();
        mapData = MapItemSavedData.load(buf.readAnySizeNbt());
    }

    public MapAtlasesInitAtlasS2CPacket(MapItemSavedData mapData, int id) {
    	this.id = id;
        this.mapData = mapData;
    }

    public void toBytes(FriendlyByteBuf buf) {
    	buf.writeInt(id);
        buf.writeNbt(mapData.save(new CompoundTag()));
    }

    public MapItemSavedData getMapState(Level level) {return mapData;}

	public boolean handle(Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (ctx.get().getSender() != null)
				ctx.get().getSender().level.setMapData(MapItem.makeKey(id), mapData);
		});
		return true;
	}
}
