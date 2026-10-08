package com.kkdfg.Tacz_morelevel.block.entity;

import com.kkdfg.Tacz_morelevel.block.ModBlockEntities;
import com.kkdfg.Tacz_morelevel.signal.SignalSummoningCatalog;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class SignalSummoningBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final int NOTICE_INTERVAL_TICKS = 20;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<BlockPos> candidatePositions = new ArrayList<>();
    private EntityType<? extends Mob> pendingEntityType;
    private int summonTicksRemaining;
    private int summonCount;

    public SignalSummoningBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SIGNAL_SUMMONING_BLOCK_ENTITY.get(), pos, state);
    }

    public InteractionResult tryStartSummoning(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Optional<SignalSummoningCatalog.Offering> offering =
                SignalSummoningCatalog.get().findOffering(stack);
        if (offering.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (this.level == null || this.level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (this.isSummoning()) {
            player.displayClientMessage(Component.translatable("message.tacz_morelevel.signal_summoning.busy"), true);
            return InteractionResult.CONSUME;
        }
        SignalSummoningCatalog catalog = SignalSummoningCatalog.get();
        this.candidatePositions.clear();
        this.collectCandidatePositions((ServerLevel) this.level, catalog);
        if (this.candidatePositions.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.tacz_morelevel.signal_summoning.no_space",
                            catalog.searchRadius() * 2 + 1,
                            catalog.searchRadius() * 2 + 1,
                            catalog.searchUpBlocks(),
                            catalog.airBlocks()),
                    true);
            return InteractionResult.CONSUME;
        }

        RandomSource random = this.level.getRandom();
        SignalSummoningCatalog.Offering chosen = offering.get();
        this.pendingEntityType = chosen.entityType();
        this.summonTicksRemaining = catalog.countdownTicks();
        this.summonCount = chosen.rollCount(random);
        this.setChanged();
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        this.noticeNearbyPlayers(
                Component.translatable(
                        "message.tacz_morelevel.signal_summoning.started",
                        countdownSeconds(catalog.countdownTicks()),
                        this.summonCount));
        this.playCountdownSound((ServerLevel) this.level);
        return InteractionResult.CONSUME;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SignalSummoningBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel) || !blockEntity.isSummoning()) {
            return;
        }
        if (blockEntity.summonTicksRemaining % NOTICE_INTERVAL_TICKS == 0) {
            int seconds = Math.max(1, blockEntity.summonTicksRemaining / 20);
            blockEntity.noticeNearbyPlayers(
                    Component.translatable("message.tacz_morelevel.signal_summoning.countdown", seconds));
        }
        blockEntity.spawnCountdownSmoke(serverLevel);
        blockEntity.summonTicksRemaining--;
        if (blockEntity.summonTicksRemaining <= 0) {
            blockEntity.spawnPendingEntities(serverLevel);
            blockEntity.clearSummoningState();
        }
    }

    private boolean isSummoning() {
        return this.pendingEntityType != null && this.summonTicksRemaining > 0;
    }

    private static int countdownSeconds(int countdownTicks) {
        return Math.max(1, countdownTicks / 20);
    }

    private void collectCandidatePositions(ServerLevel level, SignalSummoningCatalog catalog) {
        BlockPos origin = this.worldPosition;
        int searchRadius = catalog.searchRadius();
        int searchUpBlocks = catalog.searchUpBlocks();
        int airBlocks = catalog.airBlocks();
        for (int dy = 0; dy <= searchUpBlocks; dy++) {
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (candidate.getY() < level.getMinBuildHeight()
                            || candidate.getY() + airBlocks - 1 >= level.getMaxBuildHeight()) {
                        continue;
                    }
                    if (hasRequiredAirSpace(level, candidate, airBlocks)) {
                        this.candidatePositions.add(candidate.immutable());
                    }
                }
            }
            if (!this.candidatePositions.isEmpty()) {
                return;
            }
        }
    }

    private void spawnPendingEntities(ServerLevel level) {
        if (this.pendingEntityType == null || this.candidatePositions.isEmpty()) {
            return;
        }
        int airBlocks = SignalSummoningCatalog.get().airBlocks();
        RandomSource random = level.getRandom();
        int spawned = 0;
        int attempts = Math.max(this.candidatePositions.size(), this.summonCount * 8);
        while (spawned < this.summonCount && attempts-- > 0 && !this.candidatePositions.isEmpty()) {
            BlockPos spawnPos = this.candidatePositions.remove(random.nextInt(this.candidatePositions.size()));
            if (!hasRequiredAirSpace(level, spawnPos, airBlocks)) {
                continue;
            }
            Mob mob = this.pendingEntityType.create(level);
            if (mob == null) {
                continue;
            }
            mob.moveTo(
                    spawnPos.getX() + 0.5D,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5D,
                    random.nextFloat() * 360.0F,
                    0.0F);
            if (!level.noCollision(mob)) {
                continue;
            }
            mob.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(spawnPos),
                    MobSpawnType.SPAWNER,
                    null,
                    null);
            level.addFreshEntityWithPassengers(mob);
            this.spawnSummonSmoke(level, mob.position());
            spawned++;
        }
        if (spawned > 0) {
            this.playSummonBurstSound(level);
        }
        this.noticeNearbyPlayers(
                Component.translatable("message.tacz_morelevel.signal_summoning.finished", spawned));
    }

    private static boolean hasRequiredAirSpace(ServerLevel level, BlockPos pos, int airBlocks) {
        for (int airOffset = 0; airOffset < airBlocks; airOffset++) {
            if (!level.isEmptyBlock(pos.above(airOffset))) {
                return false;
            }
        }
        return true;
    }

    private void playCountdownSound(ServerLevel level) {
        level.playSound(
                null,
                this.worldPosition,
                ModSounds.SOUND_SIGNAL_SUMMONING_COUNTDOWN.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F);
    }

    private void spawnSummonSmoke(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x(), pos.y() + 0.5D, pos.z(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x(), pos.y() + 0.6D, pos.z(), 12, 0.45D, 0.25D, 0.45D, 0.02D);
    }

    private void playSummonBurstSound(ServerLevel level) {
        level.playSound(null, this.worldPosition, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void spawnCountdownSmoke(ServerLevel level) {
        if ((this.summonTicksRemaining & 1) != 0) {
            return;
        }
        RandomSource random = level.getRandom();
        level.sendParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                this.worldPosition.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D,
                this.worldPosition.getY() + 1.0D,
                this.worldPosition.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D,
                1,
                0.08D,
                0.05D,
                0.08D,
                0.005D);
    }

    private void noticeNearbyPlayers(Component message) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB area = AABB.ofSize(Vec3.atCenterOf(this.worldPosition), 64.0D, 64.0D, 64.0D);
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, area)) {
            player.displayClientMessage(message, true);
        }
    }

    private void clearSummoningState() {
        this.pendingEntityType = null;
        this.summonTicksRemaining = 0;
        this.summonCount = 0;
        this.candidatePositions.clear();
        this.setChanged();
    }

    private void interruptSummoning(ServerLevel level) {
        if (!this.isSummoning()) {
            return;
        }
        this.stopCountdownSound(level);
        this.pendingEntityType = null;
        this.summonTicksRemaining = 0;
        this.summonCount = 0;
        this.candidatePositions.clear();
    }

    private void stopCountdownSound(ServerLevel level) {
        ResourceLocation soundId = ModSounds.SOUND_SIGNAL_SUMMONING_COUNTDOWN.get().getLocation();
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(soundId, SoundSource.BLOCKS);
        AABB area = AABB.ofSize(Vec3.atCenterOf(this.worldPosition), 64.0D, 64.0D, 64.0D);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, area)) {
            player.connection.send(packet);
        }
    }

    @Override
    public void setRemoved() {
        if (this.level instanceof ServerLevel serverLevel) {
            this.interruptSummoning(serverLevel);
        }
        super.setRemoved();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "idle", 0, state -> {
                    state.setAnimation(IDLE);
                    return PlayState.CONTINUE;
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
