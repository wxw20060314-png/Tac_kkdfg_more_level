package com.kkdfg.Tacz_morelevel.entity.ai;

import java.util.EnumSet;

import com.kkdfg.Tacz_morelevel.entity.custom.GrenadeEntity;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * NPC 向当前攻击目标投掷手雷：每次激活时在 {@code [0, randomPreThrowMaxTicks]} 内随机取整数 tick 作为出手前倒计时，
 * 默认上界 {@value #DEFAULT_RANDOM_PRE_THROW_MAX_TICKS}（含 0，即可能当 tick 就掷）；倒计时归零后掷出。
 * 掷出瞬间播放 {@link ModSounds#SOUND_GRENADE_PIN}；掷出的实体通过 {@link GrenadeEntity#setFuseTicks(int)} 设为
 * {@value #LAUNCHED_GRENADE_TICKS_UNTIL_DETONATION}，即<strong>发射后再过 2 秒（40 tick）即爆炸</strong>（与
 * {@link GrenadeEntity} 每 tick 递减引信直至 0 的逻辑一致）。不读取、不消耗物品栏，掷出的实体使用 {@link ModItems#GRENADE}。
 * <p>
 * 仅提供 Goal 类，不在任何实体上默认注册。
 */
public final class NpcThrowGrenadeGoal extends Goal {
    /** 出手前随机延迟的上界（tick，含）；{@link #start()} 内取 {@code nextInt(该值 + 1)}，默认 60。 */
    public static final int DEFAULT_RANDOM_PRE_THROW_MAX_TICKS = 10;
    /**
     * 掷出后手雷引信：{@link GrenadeEntity} 每 tick {@code fuseTicks--}，归零即爆。
     * 此处为「发射后再经过多少 tick 爆炸」，40 = 2 秒（20 TPS）。
     */
    public static final int LAUNCHED_GRENADE_TICKS_UNTIL_DETONATION = 10;
    public static final int CHANCE_TO_THROW_GRENADE = 20;

    private static final float THROW_SPEED = 1.15F;

    private final Mob mob;
    private final double maxAttackDistSqr;
    /** 出手前随机延迟上界（含），由构造函数传入。 */
    private final int randomPreThrowMaxTicks;
    private int chancetofire;
    /** 本次激活剩余出手前等待 tick，减到 0 的当 tick 掷出。 */
    private int preThrowTicksRemaining;
    private boolean threwThisCycle;

    public NpcThrowGrenadeGoal(Mob mob, double maxAttackDistance) {
        this(mob, maxAttackDistance, DEFAULT_RANDOM_PRE_THROW_MAX_TICKS, CHANCE_TO_THROW_GRENADE);
    }

    /**
     * @param randomPreThrowMaxTicks 出手前等待随机上界（含），实际等待为 {@code [0, randomPreThrowMaxTicks]} 内均匀随机整数 tick
     */
    public NpcThrowGrenadeGoal(Mob mob, double maxAttackDistance, int randomPreThrowMaxTicks, int chancetofire) {
        this.mob = mob;
        this.maxAttackDistSqr = maxAttackDistance * maxAttackDistance;
        this.chancetofire = chancetofire;
        this.randomPreThrowMaxTicks = Math.max(0, randomPreThrowMaxTicks);
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide() || !(this.mob.level() instanceof ServerLevel)) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || !MobGunPlayerLikeAimHelper.hasCombatLineOfSight(this.mob, target)) {
            return false;
        }
        return this.mob.distanceToSqr(target) <= this.maxAttackDistSqr;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.threwThisCycle) {
            return false;
        }
        return this.canUse();
    }

    @Override
    public void start() {
        this.preThrowTicksRemaining = this.mob.getRandom().nextInt(this.randomPreThrowMaxTicks + 1)/2+randomPreThrowMaxTicks/2;
        this.threwThisCycle = false;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (!(this.mob.level() instanceof ServerLevel level) || target == null || !target.isAlive()) {
            return;
        }
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        int preThrowRemaining = this.preThrowTicksRemaining;
        if (preThrowRemaining > 0) {
            this.preThrowTicksRemaining = preThrowRemaining - 1;
            if (this.preThrowTicksRemaining > 0) {
                return;
            }
        }
        if (this.threwThisCycle) {
            return;
        }
        if(this.mob.getRandom().nextInt(this.chancetofire) != 1) {
            return;
        }
        MobGunPlayerLikeAimHelper.applyPlayerLikeView(this.mob, target);
        level.playSound(
                null,
                this.mob.getX(),
                this.mob.getY(),
                this.mob.getZ(),
                ModSounds.SOUND_GRENADE_PIN.get(),
                SoundSource.HOSTILE,
                1.0F,
                1.0F);
        GrenadeEntity grenade = new GrenadeEntity(level, this.mob);
        grenade.setFuseTicks(LAUNCHED_GRENADE_TICKS_UNTIL_DETONATION);
        grenade.setItem(new ItemStack(ModItems.GRENADE.get()));
        grenade.shootFromRotation(this.mob, this.mob.getXRot(), this.mob.getYRot(), 0.0F, THROW_SPEED, 1.0F);
        level.addFreshEntity(grenade);
        this.mob.swing(InteractionHand.MAIN_HAND, true);
        this.threwThisCycle = true;
    }

    @Override
    public void stop() {
        this.preThrowTicksRemaining = 0;
        this.threwThisCycle = false;
    }
}
