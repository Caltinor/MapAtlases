package lilypuree.mapatlases.network;

import lilypuree.mapatlases.MapAtlasesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPacketHandler {

    public static SimpleChannel INSTANCE;
    private static final String PROTOCOL_VERSION = "1";
    private static int ID = 0;

    private static int nextID() {
        return ID++;
    }


    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(MapAtlasesMod.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals);

        INSTANCE.messageBuilder(MapAtlasesInitAtlasS2CPacket.class, nextID())
                .encoder(MapAtlasesInitAtlasS2CPacket::toBytes)
                .decoder(MapAtlasesInitAtlasS2CPacket::new)
                .consumer(MapAtlasesInitAtlasS2CPacket::handle)
                .add();

        INSTANCE.messageBuilder(MapAtlasesOpenGUIC2SPacket.class, nextID())
                .encoder(MapAtlasesOpenGUIC2SPacket::toBytes)
                .decoder(MapAtlasesOpenGUIC2SPacket::new)
                .consumer(MapAtlasesOpenGUIC2SPacket::handle)
                .add();

        INSTANCE.messageBuilder(MapAtlasesActiveStateChangeS2CPacket.class, nextID())
                .encoder(MapAtlasesActiveStateChangeS2CPacket::toBytes)
                .decoder(MapAtlasesActiveStateChangeS2CPacket::new)
                .consumer(MapAtlasesActiveStateChangeS2CPacket::handle)
                .add();
    }
}
