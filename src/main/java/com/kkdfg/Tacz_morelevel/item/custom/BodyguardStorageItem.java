package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.companion.StoredCompanionData;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;

import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
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
 * 护航存储器（物品）：空时右键己方护航存入；对地面 {@link #useOn} 放出。
 */
public final class BodyguardStorageItem extends Item implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BodyguardStorageItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerBodyguardStorage(consumer);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "idle", 0, state -> {
                    state.setAnimation(IDLE);
                    return PlayState.CONTINUE;
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(target instanceof AbstractFriendlyCompanionEntity companion)) {
            return InteractionResult.PASS;
        }
        if (!StoredCompanionData.fromStack(stack).isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.bodyguard_storage.already_full"), true);
            return InteractionResult.FAIL;
        }
        if (!companion.isTame() || !companion.isOwnedBy(player)) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.bodyguard_storage.not_yours"), true);
            return InteractionResult.FAIL;
        }
        StoredCompanionData data = StoredCompanionData.fromCompanion(companion);
        if (data.isEmpty()) {
            return InteractionResult.FAIL;
        }
        ItemStack updated = stack.copy();
        data.writeToStack(updated);
        if (StoredCompanionData.fromStack(updated).isEmpty()) {
            return InteractionResult.FAIL;
        }
        player.setItemInHand(hand, updated);
        BlockPos soundPos = companion.blockPosition();
        player.level().playSound(
                null,
                soundPos,
                ModSounds.SOUND_BODYGUARD_STORAGE_XIU.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        companion.discard();
        player.displayClientMessage(
                Component.translatable("message.tacz_morelevel.bodyguard_storage.stored"), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = context.getItemInHand();
        StoredCompanionData data = StoredCompanionData.fromStack(stack);
        if (data.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.bodyguard_storage.empty"), true);
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }
        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        if (data.spawn(serverLevel, spawnPos, player.getYRot()) == null) {
            player.displayClientMessage(
                    Component.translatable("message.tacz_morelevel.bodyguard_storage.no_space"), true);
            return InteractionResult.FAIL;
        }
        ItemStack updated = stack.copy();
        StoredCompanionData.clearStack(updated);
        player.setItemInHand(context.getHand(), updated);
        player.displayClientMessage(
                Component.translatable("message.tacz_morelevel.bodyguard_storage.withdrawn"), true);
        serverLevel.playSound(
            null,
            spawnPos,
            ModSounds.SOUND_BODYGUARD_STORAGE_BIU.get(),
            SoundSource.PLAYERS,
            1.0F,
            1.0F);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        StoredCompanionData data = StoredCompanionData.fromStack(stack);
        if (!data.isEmpty()) {
            String name = data.displayName();
            if (name != null && !name.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.tacz_morelevel.bodyguard_storage.stored", name));
            } else {
                tooltip.add(Component.translatable("tooltip.tacz_morelevel.bodyguard_storage.stored_unknown"));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.tacz_morelevel.bodyguard_storage.empty"));
        }
    }
}
