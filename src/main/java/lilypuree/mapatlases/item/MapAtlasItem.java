package lilypuree.mapatlases.item;

import lilypuree.mapatlases.MapAtlasesMod;
import lilypuree.mapatlases.screen.MapAtlasesAtlasOverviewScreenHandler;
import lilypuree.mapatlases.util.MapAtlasesAccessUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapAtlasItem extends Item {

    public MapAtlasItem(Properties pProperties) {
        super(pProperties);
    }

    public static int getMaxMapCount() {
        if (MapAtlasesMod.CONFIG != null) {
            return MapAtlasesMod.CONFIG.maxMapCount.get();
        }
        return 128;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        if (world != null && world.isClientSide) {
            MapItemSavedData mapState = MapAtlasesAccessUtils.getFirstMapStateFromAtlas(world, stack);
            if (mapState == null) {
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
            if (MapAtlasesMod.CONFIG == null || MapAtlasesMod.CONFIG.enableEmptyMapEntryAndFill.get()) {
                tooltip.add(new TranslatableComponent("item.map_atlases.atlas.tooltip_2", empties)
                        .withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(new TranslatableComponent("item.map_atlases.atlas.tooltip_3", 1 << mapState.scale)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            NetworkHooks.openGui((ServerPlayer) player,
                    new SimpleMenuProvider(this::createMenu, new TextComponent("atlas_gui")),
                    buffer -> this.writeScreenOpeningData((ServerPlayer) player, buffer)
            );
        }
        world.playLocalSound(player.getX(), player.getY(), player.getZ(), MapAtlasesMod.ATLAS_OPEN_SOUND_EVENT,
                SoundSource.PLAYERS, 1.0F, 1.0F, false);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null || context.getLevel().isClientSide) return super.useOn(context);
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (blockState.is(BlockTags.BANNERS)) {
            if (!context.getLevel().isClientSide) {
                MapItemSavedData mapState =
                        MapAtlasesAccessUtils.getActiveAtlasMapState(
                                context.getLevel(),
                                context.getItemInHand(),
                                context.getPlayer().getName().getString()).getValue();
                if (mapState != null) {
                    mapState.toggleBanner(context.getLevel(), context.getClickedPos());
                }
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        } else {
            return super.useOn(context);
        }
    }


    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(player.getInventory());
        Map<Integer, List<Integer>> idsToCenters = new HashMap<>();
        Map<String, MapItemSavedData> mapInfos = MapAtlasesAccessUtils.getAllMapInfoFromAtlas(player.level, atlas);
        for (Map.Entry<String, MapItemSavedData> state : mapInfos.entrySet()) {
            idsToCenters.put(
                    MapAtlasesAccessUtils.getMapIntFromString(state.getKey()),
                    Arrays.asList(state.getValue().x, state.getValue().z));
        }
        return new MapAtlasesAtlasOverviewScreenHandler(syncId, inv, idsToCenters);
    }

    public void writeScreenOpeningData(ServerPlayer serverPlayerEntity, FriendlyByteBuf packetByteBuf) {
        ItemStack atlas = MapAtlasesAccessUtils.getAtlasFromPlayerByConfig(serverPlayerEntity.getInventory());
        if (atlas.isEmpty()) return;
        Map<String, MapItemSavedData> mapInfos = MapAtlasesAccessUtils.getAllMapInfoFromAtlas(serverPlayerEntity.level, atlas);
        if (mapInfos.isEmpty()) return;
        packetByteBuf.writeInt(mapInfos.size());
        for (Map.Entry<String, MapItemSavedData> state : mapInfos.entrySet()) {
            packetByteBuf.writeInt(MapAtlasesAccessUtils.getMapIntFromString(state.getKey()));
            packetByteBuf.writeInt(state.getValue().x);
            packetByteBuf.writeInt(state.getValue().z);
        }
    }
}
