package lilypuree.mapatlases.client;

import lilypuree.mapatlases.MapAtlasesMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MapAtlasesMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MapAtlasHUDHandler {
    public static MapAtlasesHUD mapAtlasesAtlasHUD;

    @SubscribeEvent
    public static void onHUD(RenderGameOverlayEvent.Post event){
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL){
            mapAtlasesAtlasHUD.render(event.getMatrixStack());
        }
    }
}
