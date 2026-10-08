package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ArmorNightVisionAnimationController;
import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
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
 * 特种作战盔甲：数值同六级防弹；穿戴 Gecko 见 {@code geo/armor/special_ops_bulletproof.geo.json}。
 * 每件爆炸减伤见 {@code armor.toml} {@code explosionReductionPerPiece.specialOps}（默认
 * {@link #EXPLOSION_REDUCTION_PER_PIECE}；运行时 {@link ExplosionArmorReduction}）。
 * 全套夜视见 {@link com.kkdfg.Tacz_morelevel.armor.skill.ArmorNightVisionHandler}。
 */
public class SpecialOpsBulletproofArmorItem extends BulletproofArmor implements GeoItem {

    /** 每件爆炸伤害减免比例。 */
    public static final float EXPLOSION_REDUCTION_PER_PIECE = 0.08f;
    public static final int FULL_SET_PIECES = 4;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final ArmorNightVisionAnimationController FANZ_CONTROLLER =
            ArmorNightVisionAnimationController.forClip("FANZ");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SpecialOpsBulletproofArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.SIXTH_BULLETPROOF, type, properties);
        this.setLevel(6);
    }

    public static int countEquippedPieces(LivingEntity entity) {
        int count = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof SpecialOpsBulletproofArmorItem) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return countEquippedPieces(entity) >= FULL_SET_PIECES;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerSpecialOpsBulletproofArmor(consumer);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        var style = TieredBulletproofGeoArmorItem.styleForTier(BulletproofArmorTier.SIXTH);
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.special_ops_bulletproof.trait")
                        .withStyle(style));
        TieredBulletproofGeoArmorItem.appendBulletproofTooltipLines(
                tooltip, null, this.getLevel(), style);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "idle", 10, state -> {
                    state.setAnimation(IDLE);
                    return PlayState.CONTINUE;
                }));
        controllers.add(
                new AnimationController<>(this, "FANZ", 0, FANZ_CONTROLLER::tick));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
