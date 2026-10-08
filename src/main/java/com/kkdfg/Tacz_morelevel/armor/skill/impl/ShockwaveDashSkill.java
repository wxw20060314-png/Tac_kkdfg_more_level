package com.kkdfg.Tacz_morelevel.armor.skill.impl;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkill;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkillRegistry;
import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import com.kkdfg.Tacz_morelevel.effect.ShockwaveImpactEffect;
import com.kkdfg.Tacz_morelevel.item.custom.ShockwaveBulletproofArmorItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 冲击波套传说技能：蓄力将能量转入动能池，松开（或能量耗尽）释放冲量。
 *
 * <p><b>玩家</b>：按住/松开见 {@link com.kkdfg.Tacz_morelevel.network.packet.ShockwaveDashPacket}；
 * {@link #tickCharge} 在能量耗尽时自动释放并写冷却。
 *
 * <p><b>NPC（炸弹视野等）</b>：勿用 {@link #tickCharge} 的自动释放。改用 {@link #tickEnergyRegen}、
 * {@link #tickConvertKineticNoAutoRelease}、预发射时 {@link #startCooldown}，冲量用
 * {@link #applyImpulseFromKinetic}（可不再写冷却）。
 */
public final class ShockwaveDashSkill implements ArmorLegendarySkill {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "shockwave_dash");

    private static final ShockwaveDashSkill INSTANCE = new ShockwaveDashSkill();

    private ShockwaveDashSkill() {}

    public static void register() {
        ArmorLegendarySkillRegistry.register(INSTANCE);
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return ShockwaveBulletproofArmorItem.hasFullSet(entity);
    }

    public static ItemStack chest(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST);
    }

    public static boolean isOnCooldown(LivingEntity entity) {
        if (!hasFullSet(entity)) {
            return false;
        }
        return ShockwaveBulletproofArmorItem.isDashOnCooldown(
                chest(entity), entity.level().getGameTime());
    }

    private static boolean isSpectatorBlocked(LivingEntity entity) {
        return entity instanceof Player player && player.isSpectator();
    }

    /** 按下边沿：TTL 就绪时动能池归 0 并开始蓄力。玩家与生物共用。 */
    public static boolean beginCharge(LivingEntity entity) {
        if (entity.level().isClientSide() || isSpectatorBlocked(entity)) {
            return false;
        }
        if (!hasFullSet(entity) || isOnCooldown(entity)) {
            return false;
        }
        ItemStack stack = chest(entity);
        if (!ShockwaveBulletproofArmorItem.isShockwaveChest(stack)) {
            return false;
        }
        ShockwaveBulletproofArmorItem.setKinetic(stack, 0);
        ShockwaveBulletproofArmorItem.setCharging(stack, true);
        ShockwaveChargeEffects.startChargeSound(entity);
        return true;
    }

    /** 松开：若正在蓄力则释放。玩家路径（释放时写冷却）。 */
    public static boolean releaseCharge(LivingEntity entity) {
        if (entity.level().isClientSide() || isSpectatorBlocked(entity)) {
            return false;
        }
        if (!hasFullSet(entity) || isOnCooldown(entity)) {
            return false;
        }
        ItemStack stack = chest(entity);
        if (!ShockwaveBulletproofArmorItem.isCharging(stack)) {
            return false;
        }
        return releaseNow(entity, stack);
    }

    /**
     * 玩家服务端每 tick：全套回能；蓄力中转化并喷粒子；能量到 0 自动释放。
     *
     * @return 本 tick 是否触发了自动释放
     */
    public static boolean tickCharge(LivingEntity entity) {
        if (entity.level().isClientSide() || isSpectatorBlocked(entity)) {
            return false;
        }
        if (!hasFullSet(entity)) {
            clearChargeSession(entity);
            return false;
        }
        ItemStack stack = chest(entity);
        if (!ShockwaveBulletproofArmorItem.isShockwaveChest(stack)) {
            return false;
        }

        tickEnergyRegen(entity);

        if (!ShockwaveBulletproofArmorItem.isCharging(stack)) {
            return false;
        }
        if (isOnCooldown(entity)) {
            clearChargeSession(entity);
            return false;
        }

        tickConvertKineticNoAutoRelease(entity);

        if (ShockwaveBulletproofArmorItem.getEnergy(stack) <= 0) {
            return releaseNow(entity, stack);
        }
        return false;
    }

    /** 全套胸甲回能（玩家 / NPC；不转动能、不释放）。NPC 回能见 {@link ShockwaveBulletproofArmorItem#NPC_ENERGY_REGEN_MULTIPLIER}。 */
    public static void tickEnergyRegen(LivingEntity entity) {
        if (entity.level().isClientSide() || !hasFullSet(entity)) {
            return;
        }
        ItemStack stack = chest(entity);
        if (!ShockwaveBulletproofArmorItem.isShockwaveChest(stack)) {
            return;
        }
        int energy = ShockwaveBulletproofArmorItem.getEnergy(stack);
        int regen = ShockwaveBulletproofArmorItem.energyRegenPerTick(entity);
        energy = Math.min(ShockwaveBulletproofArmorItem.ENERGY_MAX, energy + regen);
        ShockwaveBulletproofArmorItem.setEnergy(stack, energy);
    }

    /**
     * NPC：蓄力中把能量转入动能并喷粒子；能量耗尽时只停转，不释放、不写冷却。
     * 需已 {@link #beginCharge} 且不在冷却。
     */
    public static void tickConvertKineticNoAutoRelease(LivingEntity entity) {
        if (entity.level().isClientSide() || !hasFullSet(entity)) {
            return;
        }
        ItemStack stack = chest(entity);
        if (!ShockwaveBulletproofArmorItem.isShockwaveChest(stack)
                || !ShockwaveBulletproofArmorItem.isCharging(stack)) {
            return;
        }
        if (isOnCooldown(entity)) {
            clearChargeSession(entity);
            return;
        }
        int energy = ShockwaveBulletproofArmorItem.getEnergy(stack);
        int spend = Math.min(ShockwaveBulletproofArmorItem.DRAIN_PER_TICK, energy);
        if (spend > 0) {
            energy -= spend;
            ShockwaveBulletproofArmorItem.setEnergy(stack, energy);
            int kinetic = ShockwaveBulletproofArmorItem.getKinetic(stack) + spend;
            ShockwaveBulletproofArmorItem.setKinetic(stack, kinetic);
        }
        ShockwaveChargeEffects.tickEmit(entity, stack);
    }

    /** 立即开始冲刺冷却（NPC 打开预发射时调用，视为技能已用）。 */
    public static void startCooldown(LivingEntity entity) {
        if (entity.level().isClientSide() || !hasFullSet(entity)) {
            return;
        }
        ItemStack stack = chest(entity);
        if (!ShockwaveBulletproofArmorItem.isShockwaveChest(stack)) {
            return;
        }
        ShockwaveBulletproofArmorItem.startDashCooldown(
                stack, entity.level().getGameTime(), ShockwaveBulletproofArmorItem.COOLDOWN_TICKS);
    }

    /**
     * 用给定动能施加冲量与冲击效果；清胸甲动能/蓄力标记。
     *
     * @param writeCooldown true=玩家松开路径；NPC 预发射已写过冷却时应 false
     */
    public static boolean applyImpulseFromKinetic(
            LivingEntity entity, int kinetic, boolean writeCooldown) {
        if (entity.level().isClientSide() || !(entity.level() instanceof ServerLevel)) {
            return false;
        }
        if (!hasFullSet(entity)) {
            return false;
        }
        ItemStack stack = chest(entity);
        if (!ShockwaveBulletproofArmorItem.isShockwaveChest(stack)) {
            return false;
        }
        ShockwaveChargeEffects.playReleaseSound(entity);
        ShockwaveBulletproofArmorItem.setKinetic(stack, 0);
        ShockwaveBulletproofArmorItem.setCharging(stack, false);
        if (writeCooldown) {
            ShockwaveBulletproofArmorItem.startDashCooldown(
                    stack, entity.level().getGameTime(), ShockwaveBulletproofArmorItem.COOLDOWN_TICKS);
        }
        double impulse = ShockwaveBulletproofArmorItem.impulseFromKinetic(kinetic);
        if (impulse <= 0.0D) {
            return true;
        }
        Vec3 look = entity.getLookAngle();
        if (look.lengthSqr() >= 1.0E-6D) {
            Vec3 delta = look.normalize().scale(impulse);
            entity.setDeltaMovement(entity.getDeltaMovement().add(delta));
            entity.hurtMarked = true;
            entity.hasImpulse = true;
            entity.fallDistance = 0.0F;
        }
        if (!entity.onGround()) {
            float radius = ShockwaveImpactEffect.resolveExplosionRadius(entity, impulse);
            entity.addEffect(
                    new MobEffectInstance(
                            ModMobEffects.SHOCKWAVE_IMPACT.get(),
                            ShockwaveImpactEffect.DURATION_TICKS,
                            ShockwaveImpactEffect.amplifierForRadius(radius),
                            false,
                            false,
                            false));
        }
        return true;
    }

    private static boolean releaseNow(LivingEntity entity, ItemStack stack) {
        int kinetic = ShockwaveBulletproofArmorItem.getKinetic(stack);
        return applyImpulseFromKinetic(entity, kinetic, true);
    }

    /** 非全套 / 打断：清会话并停蓄力音，不写冷却。 */
    public static void clearChargeSession(LivingEntity entity) {
        ShockwaveChargeEffects.stopChargeSound(entity);
        clearChargeSession(chest(entity));
    }

    /** 非全套 / 打断：清会话，不写冷却。 */
    public static void clearChargeSession(ItemStack stack) {
        if (!ShockwaveBulletproofArmorItem.isShockwaveChest(stack)) {
            return;
        }
        ShockwaveBulletproofArmorItem.setCharging(stack, false);
        ShockwaveBulletproofArmorItem.setKinetic(stack, 0);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public boolean canActivate(LivingEntity entity) {
        // 冲击波走按住/松开，不走点按 Registry 激活
        return false;
    }

    @Override
    public boolean activate(LivingEntity entity) {
        return false;
    }
}
