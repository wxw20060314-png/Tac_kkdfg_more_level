package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
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

/** 牛仔套：数值同四级防弹；穿戴 Gecko 见 {@code geo/armor/dark_cowboy.geo.json}。 */
public class DarkCowboyArmorItem extends BulletproofArmor implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DarkCowboyArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.FOURTH_BULLETPROOF, type, properties);
        this.setLevel(4);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerDarkCowboyArmor(consumer);
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
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
