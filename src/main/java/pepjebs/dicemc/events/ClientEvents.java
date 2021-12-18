package pepjebs.dicemc.events;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.network.MapAtlasesOpenGUIC2SPacket;
import pepjebs.dicemc.setup.ClientSetup;
import pepjebs.dicemc.setup.Networking;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;

@Mod.EventBusSubscriber(modid=MapAtlases.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {
	public static Integer currentMapStateId = null;

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void mapAtlasClientTick(ClientTickEvent event) {
        if (ClientSetup.displayMapGUIBinding.isDown()) {
            if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) return;
            ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByHotbar(Minecraft.getInstance().player.getInventory());
            if (atlas.isEmpty()) return;
            Networking.sendToServer(new MapAtlasesOpenGUIC2SPacket(atlas));
        }
    }

}
