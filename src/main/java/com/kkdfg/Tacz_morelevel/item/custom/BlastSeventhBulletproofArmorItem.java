package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 爆破七级套：与七级防弹相同的 {@link ModArmorMaterials#SEVENTH_BULLETPROOF} 与 TaCZ 防弹数值；
 * 独立 Gecko 模型 {@code geo/armor/seventh_blast_bulletproof.geo.json}；
 * 发光层 {@code seventh_blast_bulletproof_glowmask.png}（{@link com.kkdfg.Tacz_morelevel.item.client.BlastSeventhBulletproofArmorRenderer}）。
 *
 * <p>每件爆炸减伤见 {@code armor.toml} {@code explosionReductionPerPiece.blast}（默认
 * {@link #EXPLOSION_REDUCTION_PER_PIECE}；运行时 {@link ExplosionArmorReduction}）。
 * <p>头盔可按键夜视（见 {@link com.kkdfg.Tacz_morelevel.armor.skill.ArmorNightVisionHandler}）。
 * <p>传说技能：爆炸小车 — 全套按 X 召唤认主炸弹小车（见 {@link
 * com.kkdfg.Tacz_morelevel.armor.skill.impl.BlastSummonBombCartSkill}）。
 */
public class BlastSeventhBulletproofArmorItem extends BulletproofArmor implements GeoItem {

    public static final int FULL_SET_PIECES = 4;

    /** 每件爆炸伤害减免比例（四件合计 40%）。 */
    public static final float EXPLOSION_REDUCTION_PER_PIECE = 0.1f;

    /**
     * 胸甲 NBT：爆炸小车冷却结束的游戏时间（绝对 TTL，参照动力机甲能量存胸甲）。
     * 判定：{@code level.getGameTime() < until} 即冷却中，无需每 tick 递减。
     */
    public static final String SUMMON_COOLDOWN_UNTIL_TAG = "BlastSummonCooldownUntil";

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final Style BLAST_TOOLTIP_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xAA0000));

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BlastSeventhBulletproofArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.SEVENTH_BULLETPROOF, type, properties);
        this.setLevel(7);
    }

    public static int countEquippedPieces(LivingEntity entity) {
        int count = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof BlastSeventhBulletproofArmorItem) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return countEquippedPieces(entity) >= FULL_SET_PIECES;
    }

    /** 读取胸甲上记录的冷却结束 gameTime；无标记视为 0（可用）。 */
    public static long getSummonCooldownUntil(ItemStack chestStack) {
        if (!(chestStack.getItem() instanceof BlastSeventhBulletproofArmorItem)) {
            return 0L;
        }
        CompoundTag tag = chestStack.getTag();
        if (tag == null || !tag.contains(SUMMON_COOLDOWN_UNTIL_TAG)) {
            return 0L;
        }
        return tag.getLong(SUMMON_COOLDOWN_UNTIL_TAG);
    }

    public static boolean isSummonOnCooldown(ItemStack chestStack, long gameTime) {
        return gameTime < getSummonCooldownUntil(chestStack);
    }

    /** 从 {@code gameTime} 起再冷却 {@code cooldownTicks}（写入 until = gameTime + cooldownTicks）。 */
    public static void startSummonCooldown(ItemStack chestStack, long gameTime, int cooldownTicks) {
        if (!(chestStack.getItem() instanceof BlastSeventhBulletproofArmorItem)) {
            return;
        }
        long until = gameTime + Math.max(0, cooldownTicks);
        chestStack.getOrCreateTag().putLong(SUMMON_COOLDOWN_UNTIL_TAG, until);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerBlastSeventhArmor(consumer);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.seventh_blast_bulletproof.legendary")
                        .withStyle(BLAST_TOOLTIP_STYLE));
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.seventh_blast_bulletproof.legendary_2")
                        .withStyle(BLAST_TOOLTIP_STYLE));
        tooltip.add(
                Component.translatable(
                                "tooltip.tacz_morelevel.bulletproof_level",
                                TieredBulletproofGeoArmorItem.toRomanNumeral(this.getLevel()))
                        .withStyle(BLAST_TOOLTIP_STYLE));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "idle", 10, state -> {
                    state.setAnimation(IDLE);
                    return PlayState.CONTINUE;
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
