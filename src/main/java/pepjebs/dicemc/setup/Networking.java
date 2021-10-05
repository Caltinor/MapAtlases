package pepjebs.dicemc.setup;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.network.MapAtlasesActiveStateChangePacket;
import pepjebs.dicemc.network.MapAtlasesInitAtlasS2CPacket;
import pepjebs.dicemc.network.MapAtlasesOpenGUIC2SPacket;

public class Networking {
		private static SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(MapAtlases.MOD_ID, "net"),
			() -> "1.0", 
			s -> true, 
			s -> true);
		
		public static void registerMessages() { 
			int ID = 0;
			//Packet list all packets need to be registered
			INSTANCE.messageBuilder(MapAtlasesInitAtlasS2CPacket.class, ID++)
				.encoder(MapAtlasesInitAtlasS2CPacket::toBytes) 
				.decoder(MapAtlasesInitAtlasS2CPacket::new)
				.consumer(MapAtlasesInitAtlasS2CPacket::handle)
				.add();
			INSTANCE.messageBuilder(MapAtlasesOpenGUIC2SPacket.class, ID++)
				.encoder(MapAtlasesOpenGUIC2SPacket::toBytes) 
				.decoder(MapAtlasesOpenGUIC2SPacket::new) 
				.consumer(MapAtlasesOpenGUIC2SPacket::handle)
				.add();
			INSTANCE.messageBuilder(MapAtlasesActiveStateChangePacket.class, ID++)
				.encoder(MapAtlasesActiveStateChangePacket::toBytes)
				.decoder(MapAtlasesActiveStateChangePacket::new)
				.consumer(MapAtlasesActiveStateChangePacket::handle)
				.add();
		}
		
		public static void sendToClient(Object packet, ServerPlayerEntity player) {
			INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
		public static void sendToServer(Object packet) {
			INSTANCE.sendToServer(packet);
		}
}
