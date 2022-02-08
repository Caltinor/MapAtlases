package lilypuree.mapatlases.events;

import lilypuree.mapatlases.MapAtlasesMod;
import lilypuree.mapatlases.client.MapAtlasesClient;
import lilypuree.mapatlases.network.MapAtlasesOpenGUIC2SPacket;
import lilypuree.mapatlases.network.ModPacketHandler;
import lilypuree.mapatlases.util.MapAtlasesAccessUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MapAtlasesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    @SubscribeEvent
    public static void mapAtlasClientTick(TickEvent.ClientTickEvent event) {
        if (MapAtlasesClient.displayMapGUIBinding.isDown()) {
            if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) return;
            ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByHotbar(Minecraft.getInstance().player.getInventory());
            if (atlas.isEmpty()) return;

            ModPacketHandler.INSTANCE.sendToServer(new MapAtlasesOpenGUIC2SPacket(atlas));
        }
    }
}
