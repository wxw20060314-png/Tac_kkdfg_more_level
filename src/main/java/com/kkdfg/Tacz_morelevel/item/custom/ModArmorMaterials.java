package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.ModItems;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.function.Supplier;

/**
 * 本模组自定义 {@link ArmorMaterial}，供 {@link ArmorItem} 注册使用。
 * <p>
 * 与原版 {@link ArmorMaterials} 相同算法：
 * <ul>
 *   <li>各部位耐久 = {@link #HEALTH_FUNCTION_FOR_TYPE} 中该部位基础值 × {@link #durabilityMultiplier}</li>
 *   <li>各部位护甲值由枚举常量里的 {@code EnumMap} 给出</li>
 *   <li>{@link #getName()} 返回 {@code modid:材质名}，客户端盔甲纹理通常放在
 *       {@code assets/&lt;modid&gt;/textures/models/armor/&lt;材质名&gt;_layer_1.png}（及需要时的 {@code _layer_2}）</li>
 * </ul>
 */
public enum ModArmorMaterials implements StringRepresentable, ArmorMaterial {
    /**
     * 黑曜石套：高耐久倍率、下界合金级附魔能力、较高韧性与击退抗性；
     */
    Obsidian("obsidian", 100, Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266655_) -> {
        p_266655_.put(ArmorItem.Type.BOOTS, 4);
        p_266655_.put(ArmorItem.Type.LEGGINGS, 8);
        p_266655_.put(ArmorItem.Type.CHESTPLATE, 10);
        p_266655_.put(ArmorItem.Type.HELMET, 5);
    }), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 5.0F, 0.3F, () -> {
        return Ingredient.of(ModItems.OBSIDIAN_ITEM.get());
    }),
    FIRST_BULLETPROOF("first_bulletproof", 10, Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266652_) -> {
        p_266652_.put(ArmorItem.Type.BOOTS, 1);
        p_266652_.put(ArmorItem.Type.LEGGINGS, 2);
        p_266652_.put(ArmorItem.Type.CHESTPLATE, 3);
        p_266652_.put(ArmorItem.Type.HELMET, 1);
    }), 5, SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 0.0F, () -> {
        return Ingredient.of(Items.LEATHER);
    }),
    SECOND_BULLETPROOF("second_bulletproof", 20, Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266651_) -> {
        p_266651_.put(ArmorItem.Type.BOOTS, 1);
        p_266651_.put(ArmorItem.Type.LEGGINGS, 4);
        p_266651_.put(ArmorItem.Type.CHESTPLATE, 5);
        p_266651_.put(ArmorItem.Type.HELMET, 2);
    }), 10, SoundEvents.ARMOR_EQUIP_CHAIN, 2.0F, 0.0F, () -> {
        return Ingredient.of(Items.COPPER_INGOT);
    }),
    THIRD_BULLETPROOF("third_bulletproof", 35, Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266654_) -> {
        p_266654_.put(ArmorItem.Type.BOOTS, 2);
        p_266654_.put(ArmorItem.Type.LEGGINGS, 5);
        p_266654_.put(ArmorItem.Type.CHESTPLATE, 6);
        p_266654_.put(ArmorItem.Type.HELMET, 2);
    }), 9, SoundEvents.ARMOR_EQUIP_IRON, 3.0F, 0.05F, () -> {
        return Ingredient.of(Items.IRON_INGOT);
    }),
    FOURTH_BULLETPROOF("fourth_bulletproof", 60, Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266654_) -> {
        p_266654_.put(ArmorItem.Type.BOOTS, 3);
        p_266654_.put(ArmorItem.Type.LEGGINGS, 6);
        p_266654_.put(ArmorItem.Type.CHESTPLATE, 8);
        p_266654_.put(ArmorItem.Type.HELMET, 3);
    }), 10, SoundEvents.ARMOR_EQUIP_IRON, 5.0F, 0.1F, () -> {
        return Ingredient.of(Items.DIAMOND);
    }),
    FIFTH_BULLETPROOF("fifth_bulletproof", 100, Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266654_) -> {
        p_266654_.put(ArmorItem.Type.BOOTS, 4);
        p_266654_.put(ArmorItem.Type.LEGGINGS, 7);
        p_266654_.put(ArmorItem.Type.CHESTPLATE, 9);
        p_266654_.put(ArmorItem.Type.HELMET, 5);
    }), 15, SoundEvents.ARMOR_EQUIP_IRON, 8.0F, 0.12F, () -> {
        return Ingredient.of(Items.NETHERITE_INGOT);
    }),
    SIXTH_BULLETPROOF("sixth_bulletproof", 175, Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266654_) -> {
        p_266654_.put(ArmorItem.Type.BOOTS, 6);
        p_266654_.put(ArmorItem.Type.LEGGINGS, 8);
        p_266654_.put(ArmorItem.Type.CHESTPLATE, 10);
        p_266654_.put(ArmorItem.Type.HELMET, 6);
    }), 15, SoundEvents.ARMOR_EQUIP_IRON, 12.0F, 0.12F, () -> {
        return Ingredient.of(ModItems.OBSIDIAN_ITEM.get());
    }),
    SEVENTH_BULLETPROOF("seventh_bulletproof", 300, Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266654_) -> {
        p_266654_.put(ArmorItem.Type.BOOTS, 7);
        p_266654_.put(ArmorItem.Type.LEGGINGS, 9);
        p_266654_.put(ArmorItem.Type.CHESTPLATE, 12);
        p_266654_.put(ArmorItem.Type.HELMET, 7);
    }), 10, SoundEvents.ARMOR_EQUIP_IRON, 15.0F, 0.2F, () -> {
        return Ingredient.of(ModItems.SEVENTH_STEEL_INGOT.get());
    }),
   ;

    /** 序列化/命令等场景用；须指向本枚举而非原版 {@link ArmorMaterials} */
    public static final EnumCodec<ModArmorMaterials> CODEC = StringRepresentable.fromEnum(ModArmorMaterials::values);

    /**
     * 与原版一致：各槽位「基础耐久」系数，实际耐久 = 该值 × {@link #durabilityMultiplier}。
     * 靴子 13、护腿 15、胸甲 16、头盔 11。
     */
    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (p_266653_) -> {
        p_266653_.put(ArmorItem.Type.BOOTS, 13);
        p_266653_.put(ArmorItem.Type.LEGGINGS, 15);
        p_266653_.put(ArmorItem.Type.CHESTPLATE, 16);
        p_266653_.put(ArmorItem.Type.HELMET, 11);
    });
    /** 资源名片段（不含 modid），{@link #getName()} 会拼成 {@code test1_mod:obsidian} 等 */
    private final String name;
    /** 耐久倍率，与 {@link #HEALTH_FUNCTION_FOR_TYPE} 相乘得到各部位最大耐久 */
    private final int durabilityMultiplier;
    /** 各部位护甲值（护甲点数） */
    private final EnumMap<ArmorItem.Type, Integer> protectionFunctionForType;
    /** 附魔能力（越高越容易出高等级附魔） */
    private final int enchantmentValue;
    private final SoundEvent sound;
    /** 盔甲韧性（减轻高伤害） */
    private final float toughness;
    /** 击退抗性比例（0~1，与原版下界合金套类似） */
    private final float knockbackResistance;
    /** 延迟加载的修理材料，避免注册阶段循环引用 */
    private final LazyLoadedValue<Ingredient> repairIngredient;

    /**
     * @param pName 材质短名，用于纹理路径与 {@link #getSerializedName()}
     * @param pDurabilityMultiplier 耐久倍率
     * @param pProtectionFunctionForType 各部位护甲值
     * @param pEnchantmentValue 附魔能力
     * @param pSound 穿戴音效
     * @param pToughness 韧性
     * @param pKnockbackResistance 击退抗性
     * @param pRepairIngredient 铁砧修理用材料
     */
    private ModArmorMaterials(String pName, int pDurabilityMultiplier, EnumMap<ArmorItem.Type, Integer> pProtectionFunctionForType, int pEnchantmentValue, SoundEvent pSound, float pToughness, float pKnockbackResistance, Supplier<Ingredient> pRepairIngredient) {
        this.name = pName;
        this.durabilityMultiplier = pDurabilityMultiplier;
        this.protectionFunctionForType = pProtectionFunctionForType;
        this.enchantmentValue = pEnchantmentValue;
        this.sound = pSound;
        this.toughness = pToughness;
        this.knockbackResistance = pKnockbackResistance;
        this.repairIngredient = new LazyLoadedValue<>(pRepairIngredient);
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type pType) {
        return HEALTH_FUNCTION_FOR_TYPE.get(pType) * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type pType) {
        return this.protectionFunctionForType.get(pType);
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.sound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    /** 返回 {@code modid:材质名}，客户端据此解析盔甲纹理等资源 */
    @Override
    public String getName() {
        return Tacz_MoreLevel.MOD_ID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    /** 击退抗性比例（0~1），与原版 {@link ArmorMaterial#getKnockbackResistance()} 含义一致 */
    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
