package pepjebs.dicemc.item;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.network.NetworkHooks;
import pepjebs.dicemc.MapAtlases;
import pepjebs.dicemc.config.Config;
import pepjebs.dicemc.gui.MapAtlasesAtlasOverviewScreenHandler;
import pepjebs.dicemc.setup.Registration;
import pepjebs.dicemc.util.MapAtlasesAccessUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class MapAtlasItem extends Item{

    public MapAtlasItem() {
        super(new Item.Properties().tab(ItemGroup.TAB_TOOLS));
    }

    public static int getMaxMapCount() {
        return Config.MAX_MAP_COUNT.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);

        if (world != null && world.isClientSide()) {
            MapData MapData = MapAtlasesAccessUtils.getFirstMapDataFromAtlas(world, stack);
            String atlas_item_id = "item."+MapAtlases.MOD_ID+".atlas";
            if (MapData == null) {
                tooltip.add(new TranslationTextComponent(atlas_item_id+".tooltip_err")
                        .withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
                return;
            }
            int mapSize = MapAtlasesAccessUtils.getMapCountFromItemStack(stack);
            int empties = MapAtlasesAccessUtils.getEmptyMapCountFromItemStack(stack);
            if (mapSize + empties >= getMaxMapCount()) {
                tooltip.add(new TranslationTextComponent(atlas_item_id+".tooltip_full")
                        .withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
            }
            tooltip.add(new TranslationTextComponent(atlas_item_id+".tooltip_1", mapSize)
                    .withStyle(TextFormatting.GRAY));
            if (Config.ENABLE_EMPTY_MAP_ENTRY_AND_FILL.get()) {
                tooltip.add(new TranslationTextComponent(atlas_item_id+".tooltip_2", empties)
                        .withStyle(TextFormatting.GRAY));
            }
            tooltip.add(new TranslationTextComponent(atlas_item_id+".tooltip_3", 1 << MapData.scale)
                    .withStyle(TextFormatting.GRAY));
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
    	if (!world.isClientSide) {
    		NetworkHooks.openGui((ServerPlayerEntity)player, new SimpleNamedContainerProvider(
    				(i, playerInventory, playerEntity) -> this.createMenu(i, playerInventory, playerEntity),
    				new StringTextComponent("atlas_gui")
    				), (b) -> this.writeScreenOpeningData((ServerPlayerEntity)player, b));
    	}
        world.playLocalSound(player.getX(), player.getY(), player.getZ(), Registration.ATLAS_OPEN_SOUND_EVENT.get(),
                SoundCategory.PLAYERS, 1.0F, 1.0F, false);
        return ActionResult.consume(player.getItemInHand(hand));
    }

    @Override
    public ITextComponent getDescription() {
        return new TranslationTextComponent(getDescriptionId());
    }

    public MapAtlasesAtlasOverviewScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.inventory);
        Map<Integer, List<Integer>> idsToCenters = new HashMap<>();
        List<MapData> MapDatas = MapAtlasesAccessUtils.getAllMapDatasFromAtlas(player.level, atlas);
        for (MapData state : MapDatas) {
            idsToCenters.put(MapAtlasesAccessUtils.getMapIntFromState(state), Arrays.asList(state.x, state.z));
        }
        return new MapAtlasesAtlasOverviewScreenHandler(syncId, inv, idsToCenters);
    }

    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketBuffer packetByteBuf) {
        ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(serverPlayerEntity.inventory);
        if (atlas.isEmpty()) return;
        List<MapData> MapDatas =
                MapAtlasesAccessUtils.getAllMapDatasFromAtlas(serverPlayerEntity.level, atlas);
        if (MapDatas.isEmpty()) return;
        packetByteBuf.writeInt(MapDatas.size());
        for (MapData state : MapDatas) {
            packetByteBuf.writeInt(MapAtlasesAccessUtils.getMapIntFromState(state));
            packetByteBuf.writeInt(state.x);
            packetByteBuf.writeInt(state.z);
        }
    }

    @SuppressWarnings("resource")
	@Override
    public ActionResultType useOn(ItemUseContext context) {
        if (context.getPlayer() == null || context.getLevel().isClientSide) return super.useOn(context);
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (blockState.is(BlockTags.BANNERS)) {
            if (!context.getLevel().isClientSide) {
                MapData MapData =
                        MapAtlasesAccessUtils.getActiveAtlasMapData(
                                context.getLevel(), context.getItemInHand(), context.getPlayer().getName().getString());
                if (MapData != null) {
                    MapData.toggleBanner(context.getLevel(), context.getClickedPos());
                }
            }
            return ActionResultType.sidedSuccess(context.getLevel().isClientSide);
        } else {
            return super.useOn(context);
        }
    }
}
