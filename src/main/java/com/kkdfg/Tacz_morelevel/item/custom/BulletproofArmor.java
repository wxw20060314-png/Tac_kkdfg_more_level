package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.Config;
import com.kkdfg.Tacz_morelevel.armor.plugin.ArmorPluginItem;
import com.kkdfg.Tacz_morelevel.armor.plugin.ArmorPluginTooltip;
import com.kkdfg.Tacz_morelevel.armor.plugin.BallisticSteelArmorPlateItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * 本模组防弹盔甲：原版护甲点数来自 {@link ModArmorMaterials}；档位由 {@link #setLevel(int)} 写入（1–8）。
 * 档位总和见 {@code config/tacz_morelevel/armor.toml} 的 {@code bulletproofArmorTiers}
 *（{@link Config#bulletproofTierForLevel}）。
 *
 * <p>对 TaCZ 子弹：减伤 / 穿甲减免在受伤时按当前 Config <strong>动态</strong>按档位计算（单件 =
 * 套装总和÷4），不依赖物品构造时拷贝的数值，因此改 toml 并加载后下一枪即生效。
 *
 * <p>全套减伤比例 = 各件之和，伤害×(1−和)，和上限 90%；穿甲减免之和从子弹 {@code armorIgnore} 直接减去，&lt;0 按 0。
 *
 * <p>插件槽：默认 {@link #DEFAULT_PLUGIN_SLOTS} 格，内容存在本件 {@link ItemStack} 的
 * {@link #TAG_PLUGINS} ListTag；无 tag 视为全空。安装约束见 {@link ArmorPluginItem}。
 */
public class BulletproofArmor extends ArmorItem {

    private static final float PER_PIECE_FRAC = 0.25f;

    /** NBT list of installed {@link ArmorPluginItem} stacks (index = slot). */
    public static final String TAG_PLUGINS = "ArmorPlugins";

    /** Default plugin capacity per armor piece (expand later without ctor changes). */
    public static final int DEFAULT_PLUGIN_SLOTS = 1;

    /** 1–8，由 {@link #setLevel(int)} 写入（8 幽匿七级套）。 */
    private int level;

    /**
     * @deprecated 战斗已改为 {@link #sumBulletproofLevel} 动态读 Config；字段仅在 {@link #setLevel} 时刷新，供调试/兼容。
     */
    @Deprecated
    public float Bulletproof_Level;

    /**
     * @deprecated 战斗已改为 {@link #sumPenetrationReduction} 动态读 Config；字段仅在 {@link #setLevel} 时刷新，供调试/兼容。
     */
    @Deprecated
    public float Bullet_Penetration_reduction;

    private final ModArmorMaterials modMaterial;

    public BulletproofArmor(ModArmorMaterials material, Type type, Properties properties) {
        super(material, type, properties);
        this.modMaterial = material;
    }

    public int getLevel() {
        return this.level;
    }

    /**
     * @param level 1–8（8 为幽匿七级变种），越高整套越强；越界钳到 1–8。
     */
    public void setLevel(int level) {
        int tier = Mth.clamp(level, 1, 8);
        this.level = tier;
        // 兼容旧字段；真正战斗数值见 sum* 对 Config 的动态读取
        Config.BulletproofTierStats stats = Config.bulletproofTierForLevel(tier);
        this.Bulletproof_Level = stats.totalBulletproof * PER_PIECE_FRAC;
        this.Bullet_Penetration_reduction = stats.totalPenReduction * PER_PIECE_FRAC;
    }

    public ModArmorMaterials getModMaterial() {
        return this.modMaterial;
    }

    /** 当前 Config 下该件提供的子弹减伤比例（套装总和÷4）。 */
    public float bulletproofPerPieceFromConfig() {
        return Config.bulletproofTierForLevel(this.level).totalBulletproof * PER_PIECE_FRAC;
    }

    /** 当前 Config 下该件提供的穿甲减免（套装总和÷4）。 */
    public float penetrationReductionPerPieceFromConfig() {
        return Config.bulletproofTierForLevel(this.level).totalPenReduction * PER_PIECE_FRAC;
    }

    /** 遍历盔甲槽，按各件档位从 Config 累加子弹减伤比例。 */
    public static float sumBulletproofLevel(LivingEntity entity) {
        float sum = 0f;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof BulletproofArmor a) {
                sum += a.bulletproofPerPieceFromConfig();
            }
        }
        sum += BallisticSteelArmorPlateItem.sumBulletproofBonus(entity);
        return sum;
    }

    /** 遍历盔甲槽，按各件档位从 Config 累加穿甲减免。 */
    public static float sumPenetrationReduction(LivingEntity entity) {
        float sum = 0f;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof BulletproofArmor a) {
                sum += a.penetrationReductionPerPieceFromConfig();
            }
        }
        sum += BallisticSteelArmorPlateItem.sumPenetrationReductionBonus(entity);
        return sum;
    }

    // —— armor plugin NBT (per ItemStack) ——

    /**
     * Shows plugin slot frame(s) in the item tooltip (empty slots still draw the frame).
     */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        int slots = pluginSlotCount(stack);
        List<ItemStack> plugins = new ArrayList<>(slots);
        for (int i = 0; i < slots; i++) {
            plugins.add(getPlugin(stack, i));
        }
        return Optional.of(new ArmorPluginTooltip(plugins));
    }

    public static int pluginSlotCount(ItemStack armorStack) {
        return DEFAULT_PLUGIN_SLOTS;
    }

    public static ItemStack getPlugin(ItemStack armorStack, int slot) {
        if (!isBulletproofStack(armorStack) || slot < 0 || slot >= pluginSlotCount(armorStack)) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = armorStack.getTag();
        if (tag == null || !tag.contains(TAG_PLUGINS, Tag.TAG_LIST)) {
            return ItemStack.EMPTY;
        }
        ListTag list = tag.getList(TAG_PLUGINS, Tag.TAG_COMPOUND);
        if (slot >= list.size()) {
            return ItemStack.EMPTY;
        }
        CompoundTag entry = list.getCompound(slot);
        if (entry.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(entry);
    }

    public static void setPlugin(ItemStack armorStack, int slot, ItemStack plugin) {
        if (!isBulletproofStack(armorStack) || slot < 0 || slot >= pluginSlotCount(armorStack)) {
            return;
        }
        ItemStack toStore = plugin == null || plugin.isEmpty() ? ItemStack.EMPTY : plugin.copy();
        if (!toStore.isEmpty()) {
            toStore.setCount(1);
        }

        int slots = pluginSlotCount(armorStack);
        CompoundTag tag = armorStack.getOrCreateTag();
        ListTag list = tag.contains(TAG_PLUGINS, Tag.TAG_LIST)
                ? tag.getList(TAG_PLUGINS, Tag.TAG_COMPOUND)
                : new ListTag();

        while (list.size() < slots) {
            list.add(new CompoundTag());
        }
        list.set(slot, toStore.isEmpty() ? new CompoundTag() : toStore.save(new CompoundTag()));

        if (isPluginListEmpty(list, slots)) {
            tag.remove(TAG_PLUGINS);
            if (tag.isEmpty()) {
                armorStack.setTag(null);
            }
        } else {
            tag.put(TAG_PLUGINS, list);
        }
    }

    /**
     * Soft check: plugin must be {@link ArmorPluginItem} allowed on this armor piece type.
     * Workbench / events may add harder rules later.
     */
    public static boolean mayInsertPlugin(ItemStack armorStack, ItemStack plugin) {
        if (!isBulletproofStack(armorStack) || plugin == null || plugin.isEmpty()) {
            return false;
        }
        if (!(plugin.getItem() instanceof ArmorPluginItem pluginItem)) {
            return false;
        }
        if (!(armorStack.getItem() instanceof BulletproofArmor armor)) {
            return false;
        }
        return pluginItem.canInstallOn(armor.getType());
    }

    public static boolean hasAnyPlugin(ItemStack armorStack) {
        if (!isBulletproofStack(armorStack)) {
            return false;
        }
        int slots = pluginSlotCount(armorStack);
        for (int i = 0; i < slots; i++) {
            if (!getPlugin(armorStack, i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBulletproofStack(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof BulletproofArmor;
    }

    private static boolean isPluginListEmpty(ListTag list, int slots) {
        int limit = Math.min(list.size(), slots);
        for (int i = 0; i < limit; i++) {
            if (!ItemStack.of(list.getCompound(i)).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
