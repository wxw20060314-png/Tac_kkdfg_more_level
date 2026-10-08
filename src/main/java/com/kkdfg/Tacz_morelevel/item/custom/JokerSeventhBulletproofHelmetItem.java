package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
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

/**
 * 小丑七级防弹头盔：数值同七级防弹；穿戴 Gecko 见 {@code geo/armor/xiaocou_armor.geo.json}。
 *
 * <p>物品注释「（天花板的认证）」；传说技能「头部防御」：爆头按正常倍率结算后，再在防弹前将伤害 ÷2。
 * 见 {@link com.kkdfg.Tacz_morelevel.event.JokerSeventhHelmetSkillEvents}。
 */
public class JokerSeventhBulletproofHelmetItem extends BulletproofArmor implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public JokerSeventhBulletproofHelmetItem(Properties properties) {
        super(ModArmorMaterials.SEVENTH_BULLETPROOF, ArmorItem.Type.HELMET, properties);
        this.setLevel(7);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerJokerSeventhHelmet(consumer);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.joker_seventh_bulletproof_helmet.lore")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.joker_seventh_bulletproof_helmet.legendary")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
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
