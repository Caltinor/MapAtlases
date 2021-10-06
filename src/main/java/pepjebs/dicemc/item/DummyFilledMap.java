package pepjebs.dicemc.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import pepjebs.dicemc.MapAtlases;

import java.util.List;

import javax.annotation.Nullable;

public class DummyFilledMap extends Item {

	public DummyFilledMap() {
		super(new Item.Properties().tab(ItemGroup.TAB_TOOLS));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag context) {
		super.appendHoverText(stack, world, tooltip, context);
		String dummy_filled_map_item_id = "item."+MapAtlases.MOD_ID+".dummy_filled_map";
		tooltip.add(new TranslationTextComponent(dummy_filled_map_item_id+".dummy")
				.withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
		tooltip.add(new TranslationTextComponent(dummy_filled_map_item_id+".desc")
				.withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
	}
}
