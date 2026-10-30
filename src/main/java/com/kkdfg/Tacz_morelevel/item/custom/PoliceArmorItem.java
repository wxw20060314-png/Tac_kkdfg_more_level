package com.kkdfg.Tacz_morelevel.item.custom;

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
 * 警官套：数值同四级防弹；穿戴 Gecko 见 {@code geo/armor/police.geo.json}。
 *
 * <p>全套效果：子弹限制伤害（上限 {@link #BULLET_DAMAGE_CAP}），见
 * {@link com.kkdfg.Tacz_morelevel.armor.skill.ArmorPunisherDamageCapHandler}。
 */
public class PoliceArmorItem extends BulletproofArmor implements GeoItem {

    public static final int FULL_SET_PIECES = 4;

    /** 子弹实伤上限（血量点）：4。 */
    public static final float BULLET_DAMAGE_CAP = 4.0F;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PoliceArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.FOURTH_BULLETPROOF, type, properties);
        this.setLevel(4);
    }

    public static int countEquippedPieces(LivingEntity entity) {
        int count = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof PoliceArmorItem) {
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
        ModGeoItemClientExtensions.registerPoliceArmor(consumer);
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
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        TieredBulletproofGeoArmorItem.appendBulletproofTooltipLines(
                tooltip,
                null,
                this.getLevel(),
                TieredBulletproofGeoArmorItem.styleForTier(BulletproofArmorTier.FOURTH));
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.police.damage_cap")
                        .withStyle(
                                TieredBulletproofGeoArmorItem.styleForTier(
                                        BulletproofArmorTier.FOURTH)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
