package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.event.GrenadeControlEvents;
import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import com.kkdfg.Tacz_morelevel.network.ModNetwork;
import com.kkdfg.Tacz_morelevel.network.packet.GrenadeControlPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class GrenadeItem extends Item implements GeoItem {
    public static final int MAX_FUSE_TICKS = 100; // 5s
    private static final RawAnimation USE = RawAnimation.begin().thenLoop("use");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GrenadeItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerGrenade(consumer);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "grenade_use", 0, state -> {
                    if (GrenadeControlEvents.isClientPrimingForItem(this)) {
                        state.setAnimation(USE);
                        return PlayState.CONTINUE;
                    }
                    return PlayState.STOP;
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    /**
     * 右键：取消左键捏雷状态（解除引信，不投掷）。
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            if (player instanceof ServerPlayer sp) {
                GrenadeControlPacket.handleServer(sp, GrenadeControlPacket.Action.CANCEL);
            }
        } else {
            ModNetwork.CHANNEL.sendToServer(new GrenadeControlPacket(GrenadeControlPacket.Action.CANCEL));
        }
        // 取消引信不做右键挥手动作
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }
}
