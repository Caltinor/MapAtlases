package pepjebs.dicemc.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

import javax.annotation.Nullable;

public class DummyFilledMap extends Item {

    public DummyFilledMap() {
        super(new Item.Properties().tab(ItemGroup.TAB_TOOLS));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        tooltip.add(new TranslationTextComponent("item.map_atlases.dummy_filled_map.dummy")
                .withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
        tooltip.add(new TranslationTextComponent("item.map_atlases.dummy_filled_map.desc")
                .withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
    }
}
