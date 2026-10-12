package com.kkdfg.Tacz_morelevel.entity.ai.war_machine;

import com.kkdfg.Tacz_morelevel.entity.custom.AbstractDualGunSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import com.tacz.guns.api.item.IGun;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 战争机器切枪：严格看见持续 → 加特林；否则穿透档持续 → RPG。
 * 不占 Flag，与 Fire/Move 并行。
 */
public final class WarMachineWeaponSwapGoal extends Goal {

    public static final int STRICT_SEE_TICKS_FOR_MINIGUN = 3;
    public static final int NO_STRICT_SEE_TICKS_FOR_RPG = 10;
    public static final int RPG_SEE_TICKS_TO_SHOOT = 3;

    private static final int SWAP_COOLDOWN_TICKS = 4;

    private final WarMachineEntity mob;
    private int swapCooldown;
    private int strictSeeTime;
    private int noStrictSeeTime;
    private boolean lastStrictSee;

    public WarMachineWeaponSwapGoal(WarMachineEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() && IGun.mainHandHoldGun(this.mob);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.swapCooldown = 0;
        this.strictSeeTime = 0;
        this.noStrictSeeTime = 0;
        this.lastStrictSee = false;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        if (this.swapCooldown > 0) {
            this.swapCooldown--;
        }
        WarMachineGunSight.Result sight = WarMachineGunSight.evaluate(this.mob, target);
        boolean strictSee = sight.strictLineOfSight();
        boolean rpgSee = sight.rpgLineOfSight();
        if (strictSee != this.lastStrictSee) {
            this.strictSeeTime = 0;
            this.noStrictSeeTime = 0;
        } else if (strictSee) {
            this.strictSeeTime++;
        } else {
            this.noStrictSeeTime++;
        }
        this.lastStrictSee = strictSee;

        boolean wantMinigun = strictSee && this.strictSeeTime >= STRICT_SEE_TICKS_FOR_MINIGUN;
        boolean wantRpg =
                rpgSee && !wantMinigun && this.noStrictSeeTime >= NO_STRICT_SEE_TICKS_FOR_RPG;
        if (this.mob.level().isClientSide() || this.swapCooldown > 0) {
            return;
        }
        boolean holdingMinigun = isMainHandGun(this.mob, WarMachineEntity.MAIN_GUN);
        boolean holdingRpg = isMainHandGun(this.mob, WarMachineEntity.BACK_GUN);
        if (wantMinigun && !holdingMinigun && holdingRpg) {
            if (this.mob.swapActiveAndBackGun()) {
                this.swapCooldown = SWAP_COOLDOWN_TICKS;
            }
        } else if (wantRpg && !holdingRpg && holdingMinigun) {
            if (this.mob.swapActiveAndBackGun()) {
                this.swapCooldown = SWAP_COOLDOWN_TICKS;
            }
        }
    }

    public static boolean isMainHandGun(AbstractDualGunSoldierEntity mob, ResourceLocation gunId) {
        ResourceLocation held = mainHandGunId(mob);
        return gunId.equals(held);
    }

    @Nullable
    private static ResourceLocation mainHandGunId(AbstractDualGunSoldierEntity mob) {
        IGun gun = IGun.getIGunOrNull(mob.getMainHandItem());
        return gun == null ? null : gun.getGunId(mob.getMainHandItem());
    }
}
