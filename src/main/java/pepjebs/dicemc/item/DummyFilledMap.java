package pepjebs.dicemc.item;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import java.util.List;

import javax.annotation.Nullable;

public class DummyFilledMap extends Item {

    public DummyFilledMap() {
        super(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        tooltip.add(new TranslatableComponent("item.map_atlases.dummy_filled_map.dummy")
                .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("item.map_atlases.dummy_filled_map.desc")
                .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }
}
