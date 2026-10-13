package com.kkdfg.Tacz_morelevel.entity;

import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.tacz.guns.api.item.IGun;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

/**
 * 士兵物品等级与战斗偏好（闲置/战斗整理、丢物等共用）。
 * <p>
 * 等级：50 偏好枪 / 40 其它枪 / 30 雷·燃·闪 / 20 近战加成武器 / 10 其它 / 0 定枪后多余枪。
 * 单偏好：{@link #score}；双枪两偏好：{@link #scoreDual}（动态，每次整理现场算）。
 */
public final class SoldierCombatItemPreference {

    public static final int GRADE_PREFERRED_GUN = 50;
    public static final int GRADE_OTHER_GUN = 40;
    public static final int GRADE_THROWABLE = 30;
    public static final int GRADE_MELEE_BONUS = 20;
    public static final int GRADE_OTHER = 10;
    public static final int GRADE_SURPLUS_GUN = 0;

    /** 空槽占位分，不参与「最高」比较。 */
    public static final int GRADE_EMPTY = Integer.MIN_VALUE;

    private SoldierCombatItemPreference() {}

    /**
     * 战斗偏好函数：先传入多少等级就出多少；后续可改实现做加权。
     */
    public static int combatPreference(int grade) {
        if(grade>=GRADE_MELEE_BONUS&&grade<GRADE_THROWABLE){
            return grade+10;
        }
        else if(grade>=GRADE_THROWABLE&&grade<GRADE_OTHER_GUN){
            return grade-10;
        }
        return grade;

    }

    /** 基础等级（不定多余枪）。空 → {@link #GRADE_EMPTY}。 */
    public static int baseGrade(ItemStack stack, @Nullable ResourceLocation preferredGunId) {
        if (stack == null || stack.isEmpty()) {
            return GRADE_EMPTY;
        }
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun != null) {
            ResourceLocation id = gun.getGunId(stack);
            if (preferredGunId != null && preferredGunId.equals(id)) {
                return GRADE_PREFERRED_GUN;
            }
            return GRADE_OTHER_GUN;
        }
        return nonGunBaseGrade(stack);
    }

    /**
     * 双枪基础等级：偏好主 / 偏好副均为 {@link #GRADE_PREFERRED_GUN}；其它枪 40。
     * 不定多余枪。空 → {@link #GRADE_EMPTY}。
     */
    public static int baseGradeDual(
            ItemStack stack,
            @Nullable ResourceLocation preferredMainGunId,
            @Nullable ResourceLocation preferredOffhandGunId) {
        if (stack == null || stack.isEmpty()) {
            return GRADE_EMPTY;
        }
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun != null) {
            ResourceLocation id = gun.getGunId(stack);
            if ((preferredMainGunId != null && preferredMainGunId.equals(id))
                    || (preferredOffhandGunId != null && preferredOffhandGunId.equals(id))) {
                return GRADE_PREFERRED_GUN;
            }
            return GRADE_OTHER_GUN;
        }
        return nonGunBaseGrade(stack);
    }

    private static int nonGunBaseGrade(ItemStack stack) {
        Item item = stack.getItem();
        if (item == ModItems.GRENADE.get()
                || item == ModItems.MOLOTOV.get()
                || item == ModItems.FLASHBANG.get()) {
            return GRADE_THROWABLE;
        }
        if (hasExtraMeleeDamage(stack)) {
            return GRADE_MELEE_BONUS;
        }
        return GRADE_OTHER;
    }

    /**
     * 定枪后等级：非 {@code keeperGunId} 的枪 → {@link #GRADE_SURPLUS_GUN}。
     */
    public static int grade(
            ItemStack stack,
            @Nullable ResourceLocation preferredGunId,
            @Nullable ResourceLocation keeperGunId) {
        int base = baseGrade(stack, preferredGunId);
        if (base == GRADE_EMPTY) {
            return GRADE_EMPTY;
        }
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun != null && keeperGunId != null) {
            ResourceLocation id = gun.getGunId(stack);
            if (!keeperGunId.equals(id)) {
                return GRADE_SURPLUS_GUN;
            }
        }
        return base;
    }

    /**
     * 双枪定枪后等级：枪不在 {@code keeperMainGunId}/{@code keeperOffhandGunId}（非 null）内 → 0。
     * 两个 keeper 都为 null 时不定多余。
     */
    public static int gradeDual(
            ItemStack stack,
            @Nullable ResourceLocation preferredMainGunId,
            @Nullable ResourceLocation preferredOffhandGunId,
            @Nullable ResourceLocation keeperMainGunId,
            @Nullable ResourceLocation keeperOffhandGunId) {
        int base = baseGradeDual(stack, preferredMainGunId, preferredOffhandGunId);
        if (base == GRADE_EMPTY) {
            return GRADE_EMPTY;
        }
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun != null && (keeperMainGunId != null || keeperOffhandGunId != null)) {
            ResourceLocation id = gun.getGunId(stack);
            boolean kept =
                    (keeperMainGunId != null && keeperMainGunId.equals(id))
                            || (keeperOffhandGunId != null && keeperOffhandGunId.equals(id));
            if (!kept) {
                return GRADE_SURPLUS_GUN;
            }
        }
        return base;
    }

    /** {@link #combatPreference}({@link #grade})。 */
    public static int score(
            ItemStack stack,
            @Nullable ResourceLocation preferredGunId,
            @Nullable ResourceLocation keeperGunId) {
        int g = grade(stack, preferredGunId, keeperGunId);
        if (g == GRADE_EMPTY) {
            return GRADE_EMPTY;
        }
        return combatPreference(g);
    }

    /** {@link #combatPreference}({@link #gradeDual})。 */
    public static int scoreDual(
            ItemStack stack,
            @Nullable ResourceLocation preferredMainGunId,
            @Nullable ResourceLocation preferredOffhandGunId,
            @Nullable ResourceLocation keeperMainGunId,
            @Nullable ResourceLocation keeperOffhandGunId) {
        int g =
                gradeDual(
                        stack,
                        preferredMainGunId,
                        preferredOffhandGunId,
                        keeperMainGunId,
                        keeperOffhandGunId);
        if (g == GRADE_EMPTY) {
            return GRADE_EMPTY;
        }
        return combatPreference(g);
    }

    public static boolean hasExtraMeleeDamage(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem) {
            return true;
        }
        Collection<AttributeModifier> mods =
                stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE);
        if (mods == null || mods.isEmpty()) {
            return false;
        }
        double sum = 0.0D;
        for (AttributeModifier mod : mods) {
            sum += mod.getAmount();
        }
        return sum > 0.0D;
    }
}
