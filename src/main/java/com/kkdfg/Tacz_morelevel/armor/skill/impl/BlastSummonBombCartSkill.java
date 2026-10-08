package com.kkdfg.Tacz_morelevel.armor.skill.impl;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkill;
import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkillRegistry;
import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.entity.custom.BombCartEntity;
import com.kkdfg.Tacz_morelevel.item.custom.BlastSeventhBulletproofArmorItem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 传说技能：爆炸小车。
 *
 * <p>爆破七级全套可召唤认主 {@link BombCartEntity}（玩家按传说键；Boss/AI 走 {@link #trySummon} /
 * {@link ArmorLegendarySkillRegistry#tryActivate}）。
 *
 * <p>冷却参照动力机甲：状态存胸甲 NBT（{@link BlastSeventhBulletproofArmorItem#SUMMON_COOLDOWN_UNTIL_TAG}），
 * 玩家与生物同一套逻辑；用 {@code gameTime} 与 until 比较，不按 tick 递减。
 */
public final class BlastSummonBombCartSkill implements ArmorLegendarySkill {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "blast_summon_bomb_cart");

    /** 技能冷却：5 秒 = 100 tick。 */
    public static final int COOLDOWN_TICKS = 100;

    private static final ThreadLocal<Vec3> PENDING_DIRECTION = new ThreadLocal<>();

    private static final BlastSummonBombCartSkill INSTANCE = new BlastSummonBombCartSkill();

    private BlastSummonBombCartSkill() {}

    public static void register() {
        ArmorLegendarySkillRegistry.register(INSTANCE);
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return BlastSeventhBulletproofArmorItem.hasFullSet(entity);
    }

    /**
     * 服务端尝试召唤；{@code direction} 为 null 时用实体水平朝向。
     * 走 Registry（Gate + 事件 + 冷却）。
     */
    public static boolean trySummon(LivingEntity entity, @Nullable Vec3 direction) {
        PENDING_DIRECTION.set(direction);
        try {
            return ArmorLegendarySkillRegistry.tryActivate(entity, ID);
        } finally {
            PENDING_DIRECTION.remove();
        }
    }

    public static boolean isOnCooldown(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return BlastSeventhBulletproofArmorItem.isSummonOnCooldown(
                chest, entity.level().getGameTime());
    }

    public static void startCooldown(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        BlastSeventhBulletproofArmorItem.startSummonCooldown(
                chest, entity.level().getGameTime(), COOLDOWN_TICKS);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public boolean canActivate(LivingEntity entity) {
        if (entity instanceof Player player && player.isSpectator()) {
            return false;
        }
        if (!hasFullSet(entity)) {
            return false;
        }
        return !isOnCooldown(entity);
    }

    @Override
    public boolean activate(LivingEntity entity) {
        Vec3 direction = PENDING_DIRECTION.get();
        return summonBombCart(entity, direction);
    }

    /**
     * 核心召唤逻辑（不含 Gate/事件）。成功则写入冷却。
     *
     * @param direction 世界空间方向；null 或近似零向量时用实体 yaw 水平朝向
     */
    public static boolean summonBombCart(LivingEntity entity, @Nullable Vec3 direction) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        if (!hasFullSet(entity) || isOnCooldown(entity)) {
            return false;
        }
        BombCartEntity cart = ModEntities.BOMB_CART.get().create(level);
        if (cart == null) {
            return false;
        }
        Vec3 dir = normalizeHorizontal(direction);
        if (dir == null) {
            float yawRad = entity.getYRot() * Mth.DEG_TO_RAD;
            dir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        }
        float yRot = (float) (Mth.atan2(-dir.x, dir.z) * Mth.RAD_TO_DEG);
        cart.moveTo(
                entity.getX() + dir.x * 1.2D,
                entity.getY(),
                entity.getZ() + dir.z * 1.2D,
                yRot,
                0.0F);
        cart.setYBodyRot(yRot);
        cart.setYHeadRot(yRot);
        cart.setTtl(BombCartEntity.DEFAULT_TTL);
        cart.setOwner(entity);
        if (entity instanceof Mob mob) {
            LivingEntity target = mob.getTarget();
            if (target != null && target.isAlive()) {
                cart.setTarget(target);
            }
        }
        if (!level.addFreshEntity(cart)) {
            return false;
        }
        startCooldown(entity);
        return true;
    }

    @Nullable
    private static Vec3 normalizeHorizontal(@Nullable Vec3 direction) {
        if (direction == null) {
            return null;
        }
        Vec3 flat = new Vec3(direction.x, 0.0D, direction.z);
        double len2 = flat.lengthSqr();
        if (len2 < 1.0E-6D) {
            return null;
        }
        return flat.scale(1.0D / Math.sqrt(len2));
    }
}
