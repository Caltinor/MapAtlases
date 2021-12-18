package pepjebs.dicemc.item;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
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
        super(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS));
    }

	public static int getMaxMapCount() {
		return Config.MAX_MAP_COUNT.get();
	}

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);

        if (world != null && world.isClientSide()) {
            MapItemSavedData MapData = MapAtlasesAccessUtils.getFirstMapDataFromAtlas(world, stack);
            if (MapData == null) {
                tooltip.add(new TranslatableComponent("item.map_atlases.atlas.tooltip_err")
                        .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
                return;
            }
            int mapSize = MapAtlasesAccessUtils.getMapCountFromItemStack(stack);
            int empties = MapAtlasesAccessUtils.getEmptyMapCountFromItemStack(stack);
            if (mapSize + empties >= getMaxMapCount()) {
                tooltip.add(new TranslatableComponent("item.map_atlases.atlas.tooltip_full")
                        .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(new TranslatableComponent("item.map_atlases.atlas.tooltip_1", mapSize)
                    .withStyle(ChatFormatting.GRAY));
            if (Config.ENABLE_EMPTY_MAP_ENTRY_AND_FILL.get()) {
                tooltip.add(new TranslatableComponent("item.map_atlases.atlas.tooltip_2", empties)
                        .withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(new TranslatableComponent("item.map_atlases.atlas.tooltip_3", 1 << MapData.scale)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
    	if (!world.isClientSide) {
    		NetworkHooks.openGui((ServerPlayer)player, new SimpleMenuProvider(
    				(i, playerInventory, playerEntity) -> this.createMenu(i, playerInventory, playerEntity),
    				new TextComponent("atlas_gui")
    				), (b) -> this.writeScreenOpeningData((ServerPlayer)player, b));
    	}
        world.playLocalSound(player.getX(), player.getY(), player.getZ(), Registration.ATLAS_OPEN_SOUND_EVENT.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F, false);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public Component getDescription() {
        return new TranslatableComponent(getDescriptionId());
    }

    public MapAtlasesAtlasOverviewScreenHandler createMenu(int syncId, Inventory inv, Player player) {
        ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.getInventory());
        Map<Integer, List<Integer>> idsToCenters = new HashMap<>();
        List<ItemStack> Maps = MapAtlasesAccessUtils.getAllMapDatasFromAtlas(player.level, atlas);
        for (ItemStack state : Maps) {
            idsToCenters.put(MapAtlasesAccessUtils.getMapIntFromState(state), Arrays.asList(state.x, state.z));
        }
        return new MapAtlasesAtlasOverviewScreenHandler(syncId, inv, idsToCenters);
    }

    public void writeScreenOpeningData(ServerPlayer serverPlayerEntity, FriendlyByteBuf packetByteBuf) {
        ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(serverPlayerEntity.getInventory());
        if (atlas.isEmpty()) return;
        List<MapItemSavedData> MapDatas =
                MapAtlasesAccessUtils.getAllMapDatasFromAtlas(serverPlayerEntity.level, atlas);
        if (MapDatas.isEmpty()) return;
        packetByteBuf.writeInt(MapDatas.size());
        for (MapItemSavedData state : MapDatas) {
            packetByteBuf.writeInt(MapAtlasesAccessUtils.getMapIntFromState(state));
            packetByteBuf.writeInt(state.x);
            packetByteBuf.writeInt(state.z);
        }
    }

	@SuppressWarnings("resource")
	@Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null || context.getLevel().isClientSide) return super.useOn(context);
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (blockState.is(BlockTags.BANNERS)) {
            if (!context.getLevel().isClientSide) {
                MapItemSavedData MapData =
                        MapAtlasesAccessUtils.getActiveAtlasMapData(
                                context.getLevel(), context.getItemInHand(), context.getPlayer().getName().getString());
                if (MapData != null) {
                    MapData.toggleBanner(context.getLevel(), context.getClickedPos());
                }
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        } else {
            return super.useOn(context);
        }
    }
}
