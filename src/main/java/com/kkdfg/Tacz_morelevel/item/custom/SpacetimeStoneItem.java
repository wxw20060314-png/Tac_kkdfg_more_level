package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.armor.skill.impl.SpacetimeBlinkHelper;
import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
 * 时空之石：右键使用旧版闪现——站立视线落点，蹲下随机 30–60 格。
 * 冷却 20s，仅成功传送后进入；名称样式与时空套一致（蓝+粗体）。
 * Gecko：{@code geo/item/spacetime_stone.geo.json}，动画 {@code xuan}。
 */
public class SpacetimeStoneItem extends Item implements GeoItem {

    /** 20 秒。 */
    public static final int COOLDOWN_TICKS = 400;

    private static final RawAnimation XUAN = RawAnimation.begin().thenLoop("xuan");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SpacetimeStoneItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack))
                .withStyle(SpacetimeBulletproofArmorItem.NAME_STYLE);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerSpacetimeStone(consumer);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "xuan", 0, state -> {
                    state.setAnimation(XUAN);
                    return PlayState.CONTINUE;
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(stack);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        Optional<Vec3> dest =
                player.isShiftKeyDown()
                        ? SpacetimeBlinkHelper.resolveRandomBlink(serverLevel, player)
                        : SpacetimeBlinkHelper.resolvePlayerLookBlink(serverLevel, player);
        if (dest.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable(
                                    player.isShiftKeyDown()
                                            ? "message.tacz_morelevel.spacetime_stone.no_random"
                                            : "message.tacz_morelevel.spacetime_stone.no_look")
                            .withStyle(ChatFormatting.RED),
                    true);
            return InteractionResultHolder.fail(stack);
        }
        if (!SpacetimeBlinkHelper.blinkInSameDimension(serverLevel, player, dest.get())) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.spacetime_stone.failed")
                            .withStyle(ChatFormatting.RED),
                    true);
            return InteractionResultHolder.fail(stack);
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        player.displayClientMessage(
                Component.translatable(
                                "message.tacz_morelevel.spacetime_stone.success",
                                String.format("%.1f", dest.get().x),
                                String.format("%.1f", dest.get().y),
                                String.format("%.1f", dest.get().z))
                        .withStyle(ChatFormatting.AQUA),
                true);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.spacetime_stone")
                        .withStyle(ChatFormatting.AQUA));
    }
}
