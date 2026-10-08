package com.kkdfg.Tacz_morelevel.block.custom;

import com.kkdfg.Tacz_morelevel.block.ModBlockEntities;
import com.kkdfg.Tacz_morelevel.block.entity.LimitedSpawnerBlockEntity;
import com.kkdfg.Tacz_morelevel.spawner.LimitedBaseSpawner;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.RandomSource;

/** 限次刷怪笼：贴图/模型同原版刷怪笼。 */
public final class LimitedSpawnerBlock extends BaseEntityBlock {

    public LimitedSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LimitedSpawnerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(
                type,
                ModBlockEntities.LIMITED_SPAWNER_BLOCK_ENTITY.get(),
                level.isClientSide()
                        ? LimitedSpawnerBlockEntity::clientTick
                        : LimitedSpawnerBlockEntity::serverTick);
    }

    @Override
    public void spawnAfterBreak(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            ItemStack stack,
            boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
    }

    @Override
    public int getExpDrop(
            BlockState state,
            LevelReader level,
            RandomSource random,
            BlockPos pos,
            int fortune,
            int silkTouch) {
        return 15 + random.nextInt(15) + random.nextInt(15);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof SpawnEggItem egg)) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof LimitedSpawnerBlockEntity blockEntity)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        blockEntity.setEntityId(egg.getType(stack.getTag()), level.getRandom());
        blockEntity.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable BlockGetter level,
            List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Optional<Component> entityName = getSpawnEntityDisplayName(stack);
        if (entityName.isPresent()) {
            tooltip.add(entityName.get());
        } else {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(
                    Component.translatable("block.minecraft.spawner.desc1")
                            .withStyle(ChatFormatting.GRAY));
            tooltip.add(
                    CommonComponents.space()
                            .append(
                                    Component.translatable("block.minecraft.spawner.desc2")
                                            .withStyle(ChatFormatting.BLUE)));
        }

        CompoundTag beTag = BlockItem.getBlockEntityData(stack);
        int remaining =
                beTag != null && beTag.contains(LimitedBaseSpawner.TAG_SPAWNS_REMAINING)
                        ? beTag.getInt(LimitedBaseSpawner.TAG_SPAWNS_REMAINING)
                        : 1;
        tooltip.add(
                Component.translatable(
                                "block.tacz_morelevel.limited_spawner.spawns_remaining", remaining)
                        .withStyle(ChatFormatting.GRAY));
        boolean peaceful =
                beTag != null && beTag.getBoolean(LimitedBaseSpawner.TAG_SPAWN_IN_PEACEFUL);
        tooltip.add(
                Component.translatable(
                                peaceful
                                        ? "block.tacz_morelevel.limited_spawner.peaceful_yes"
                                        : "block.tacz_morelevel.limited_spawner.peaceful_no")
                        .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Optional<Component> getSpawnEntityDisplayName(ItemStack stack) {
        CompoundTag beTag = BlockItem.getBlockEntityData(stack);
        if (beTag != null && beTag.contains("SpawnData", 10)) {
            String id =
                    beTag.getCompound("SpawnData").getCompound("entity").getString("id");
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location != null) {
                return BuiltInRegistries.ENTITY_TYPE
                        .getOptional(location)
                        .map(
                                type ->
                                        Component.translatable(type.getDescriptionId())
                                                .withStyle(ChatFormatting.GRAY));
            }
        }
        return Optional.empty();
    }
}
