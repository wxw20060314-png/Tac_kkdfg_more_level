package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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
 * 分级 GeckoLib 防弹甲（I～VII 套共用）；穿戴模型见 {@link com.kkdfg.Tacz_morelevel.item.client.TierBulletproofArmorGeoModel}。
 * <p>
 * 防弹等级悬停文案 API 亦集中在此类，供七级变种套复用。
 */
public class TieredBulletproofGeoArmorItem extends BulletproofArmor implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final BulletproofArmorTier tier;

    public TieredBulletproofGeoArmorItem(BulletproofArmorTier tier, ArmorItem.Type type, Properties properties) {
        super(tier.material(), type, properties);
        this.tier = tier;
        this.setLevel(tier.level());
    }

    public BulletproofArmorTier tier() {
        return this.tier;
    }

    /** 1–8 档防弹等级对应的罗马数字。 */
    public static String toRomanNumeral(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            default -> String.valueOf(level);
        };
    }

    public static Style styleForTier(BulletproofArmorTier tier) {
        return switch (tier) {
            case FIRST, SECOND, THIRD -> Style.EMPTY.withColor(ChatFormatting.GRAY);
            case FOURTH -> Style.EMPTY.withColor(ChatFormatting.GREEN);
            case FIFTH -> Style.EMPTY.withColor(ChatFormatting.BLUE);
            case SIXTH -> Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE);
            case SEVENTH -> Style.EMPTY.withColor(ChatFormatting.RED);
        };
    }

    /** 先可选说明行、后防弹等级行（罗马数字）。 */
    public static void appendBulletproofTooltipLines(
            List<Component> tooltip, @Nullable Component specialLine, int level, Style style) {
        if (specialLine != null) {
            tooltip.add(specialLine);
        }
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.bulletproof_level", toRomanNumeral(level))
                        .withStyle(style));
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        appendBulletproofTooltipLines(tooltip, null, this.getLevel(), styleForTier(this.tier));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerTieredBulletproofArmor(consumer);
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
