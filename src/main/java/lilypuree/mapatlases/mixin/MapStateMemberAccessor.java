package lilypuree.mapatlases.mixin;

import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MapItemSavedData.class)
public interface MapStateMemberAccessor {

    @Accessor(value = "decorations")
    public abstract Map<String, MapDecoration> getDecoration();
}
