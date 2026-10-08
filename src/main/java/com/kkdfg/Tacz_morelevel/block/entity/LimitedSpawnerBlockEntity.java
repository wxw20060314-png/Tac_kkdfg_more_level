package com.kkdfg.Tacz_morelevel.block.entity;

import com.kkdfg.Tacz_morelevel.block.ModBlockEntities;
import com.kkdfg.Tacz_morelevel.block.ModBlocks;
import com.kkdfg.Tacz_morelevel.spawner.LimitedBaseSpawner;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 限次刷怪笼：外观与逻辑对齐原版，另具剩余次数与和平开关。 */
public final class LimitedSpawnerBlockEntity extends BlockEntity {

    private final LimitedBaseSpawner spawner =
            new LimitedBaseSpawner() {
                @Override
                public void broadcastEvent(Level level, BlockPos pos, int id) {
                    level.blockEvent(pos, ModBlocks.LIMITED_SPAWNER.get(), id, 0);
                }

                @Override
                protected void setNextSpawnData(
                        @Nullable Level level, BlockPos pos, SpawnData next) {
                    super.setNextSpawnData(level, pos, next);
                    if (level != null) {
                        BlockState state = level.getBlockState(pos);
                        level.sendBlockUpdated(pos, state, state, 4);
                    }
                }

                @Override
                protected void onSpawnBatchSuccess() {
                    super.onSpawnBatchSuccess();
                    LimitedSpawnerBlockEntity.this.setChanged();
                    Level level = LimitedSpawnerBlockEntity.this.level;
                    if (level != null && !level.isClientSide()) {
                        if (this.getSpawnsRemaining() <= 0) {
                            level.removeBlock(LimitedSpawnerBlockEntity.this.worldPosition, false);
                            return;
                        }
                        BlockState state = LimitedSpawnerBlockEntity.this.getBlockState();
                        level.sendBlockUpdated(
                                LimitedSpawnerBlockEntity.this.worldPosition, state, state, 3);
                    }
                }
            };

    public LimitedSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIMITED_SPAWNER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.spawner.load(this.level, this.worldPosition, tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.spawner.save(tag);
    }

    public static void clientTick(
            Level level, BlockPos pos, BlockState state, LimitedSpawnerBlockEntity blockEntity) {
        blockEntity.spawner.clientTick(level, pos);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, LimitedSpawnerBlockEntity blockEntity) {
        blockEntity.spawner.serverTick((ServerLevel) level, pos);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = this.saveWithoutMetadata();
        tag.remove("SpawnPotentials");
        return tag;
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        return this.spawner.onEventTriggered(this.level, id) || super.triggerEvent(id, type);
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public void setEntityId(EntityType<?> type, RandomSource random) {
        this.spawner.setEntityId(type, this.level, random, this.worldPosition);
    }

    public LimitedBaseSpawner getSpawner() {
        return this.spawner;
    }
}
