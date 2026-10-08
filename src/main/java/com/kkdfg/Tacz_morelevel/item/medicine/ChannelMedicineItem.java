package com.kkdfg.Tacz_morelevel.item.medicine;

import com.kkdfg.Tacz_morelevel.event.medicine.MedicineApplyEvent;
import com.kkdfg.Tacz_morelevel.event.medicine.MedicineUseEvent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 读条用药基类：{@link UseAnim#BOW} 动作，支持回血、效果、音效与 Forge 事件扩展。
 *
 * <p>具体药品在物品注册时传入 {@link MedicineSpec}；特殊结束逻辑可实现
 * {@link MedicineFinishHandler} 或监听 {@link MedicineUseEvent.Finish}。
 */
public class ChannelMedicineItem extends Item {

    private static final String TAG_CHANNEL_PULSES = "tacz_morelevel.medicine_channel_pulses";
    private static final String TAG_HEALED = "tacz_morelevel.medicine_healed";

    private final MedicineSpec spec;

    public ChannelMedicineItem(Properties properties, MedicineSpec spec) {
        super(properties);
        this.spec = spec;
    }

    public MedicineSpec spec() {
        return spec;
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(this);
        if (id != null) {
            tooltip.add(
                    Component.translatable("tooltip." + id.getNamespace() + "." + id.getPath())
                            .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canStartUse(level, player, stack)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            MedicineUseEvent.Start start = new MedicineUseEvent.Start(player, stack, spec, hand);
            MinecraftForge.EVENT_BUS.post(start);
            if (start.isCanceled()) {
                return InteractionResultHolder.fail(stack);
            }
            resetChannelState(stack);
            MedicineSoundEffects.play(level, player, spec.sounds().onStart());
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!level.isClientSide
                && entity instanceof ServerPlayer player
                && player.isUsingItem()
                && player.getUseItem() == stack) {
            MedicineContext context = createContext(player, stack, player.getUsedItemHand());
            MinecraftForge.EVENT_BUS.post(new MedicineUseEvent.Cancel(player, stack, spec, player.getUsedItemHand(), context));
            MedicineSoundEffects.stopDuringUse(player, spec.sounds());
            clearChannelState(stack);
        }
        super.releaseUsing(stack, level, entity, timeCharged);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            InteractionHand hand = player.getUsedItemHand();
            MedicineContext context = createContext(player, stack, hand);

            catchUpChannelPulses(level, player, stack, hand, context);

            MedicineUseEvent.Finish finish = new MedicineUseEvent.Finish(player, stack, spec, hand, context);
            MinecraftForge.EVENT_BUS.post(finish);

            boolean handled = false;
            if (this instanceof MedicineFinishHandler handler) {
                handled = handler.onMedicineFinish(context) == MedicineFinishHandler.Result.HANDLED;
            }

            if (!handled && !finish.isCanceled() && !finish.isSkipDefault()) {
                applyFinishHeal(level, player, stack, hand, context);
                applyFinishEffects(level, player, stack, hand, context);
                consumeOnFinish(stack, player);
                MedicineSoundEffects.play(level, player, spec.sounds().onFinish());
            }

            MedicineSoundEffects.stopDuringUse(player, spec.sounds());
            clearChannelState(stack);
        }
        return stack;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, entity, stack, remainingUseDuration);
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }

        int elapsedTicks = Math.max(0, getUseDuration(stack) - Math.max(0, remainingUseDuration));
        HealSpec heal = spec.heal();
        if (heal.channelsDuringUse()
                && elapsedTicks > 0
                && elapsedTicks % heal.intervalTicks() == 0) {
            if (!performChannelPulse(level, player, stack, player.getUsedItemHand())) {
                player.stopUsingItem();
                return;
            }
            if (stack.isEmpty()
                    || (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage())
                    || (spec.requiresHealableTarget() && !needsHealing(player))) {
                player.stopUsingItem();
            }
        }

        if (spec.showActionBarProgress()) {
            showProgress(player, elapsedTicks);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return Math.max(1, spec.useTicks());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    protected boolean canStartUse(Level level, Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage()) {
            return false;
        }
        if (spec.requiresHealableTarget() && !needsHealing(player)) {
            return false;
        }
        return true;
    }

    private void catchUpChannelPulses(
            Level level, ServerPlayer player, ItemStack stack, InteractionHand hand, MedicineContext context) {
        if (!spec.heal().channelsDuringUse()) {
            return;
        }
        int expectedPulses = Math.max(1, spec.useTicks() / spec.heal().intervalTicks());
        while (channelPulseCount(stack) < expectedPulses
                && performChannelPulse(level, player, stack, hand)) {
            if (stack.isEmpty()
                    || (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage())
                    || (spec.requiresHealableTarget() && !needsHealing(player))) {
                break;
            }
        }
    }

    private boolean performChannelPulse(Level level, ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (spec.requiresHealableTarget() && !needsHealing(player)) {
            return false;
        }
        if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage()) {
            return false;
        }

        MedicineContext context = createContext(player, stack, hand);
        if (!applyChannelHeal(level, player, stack, hand, context)) {
            return false;
        }

        applyEffectsForPhase(
                level, player, stack, hand, context, MedicineEffectSpec.EffectPhase.ON_CHANNEL_TICK);

        incrementChannelPulseCount(stack);
        context = createContext(player, stack, hand);
        MinecraftForge.EVENT_BUS.post(new MedicineUseEvent.Tick(player, stack, spec, hand, context));
        MedicineSoundEffects.play(level, player, spec.sounds().onChannel());
        return true;
    }

    private boolean applyChannelHeal(
            Level level, ServerPlayer player, ItemStack stack, InteractionHand hand, MedicineContext context) {
        HealSpec heal = spec.heal();
        if (!heal.channelsDuringUse()) {
            return true;
        }

        int cost = heal.durabilityCostPerPulse();
        if (stack.isDamageableItem() && cost > 0) {
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            if (remaining <= 0) {
                return false;
            }
            cost = Math.min(cost, remaining);
            stack.hurtAndBreak(cost, player, broken -> broken.broadcastBreakEvent(hand));
        }

        float amount = resolveHealAmount(player, heal);
        if (amount <= 0F) {
            return true;
        }

        MedicineApplyEvent apply = new MedicineApplyEvent(
                player, stack, spec, hand, MedicineApplyEvent.Phase.CHANNEL_HEAL, context, null, amount, false);
        MinecraftForge.EVENT_BUS.post(apply);
        if (apply.isCanceled()) {
            return true;
        }

        player.heal(apply.getHealAmount());
        markHealed(stack);
        MinecraftForge.EVENT_BUS.post(
                new MedicineApplyEvent(
                        player,
                        stack,
                        spec,
                        hand,
                        MedicineApplyEvent.Phase.CHANNEL_HEAL,
                        context,
                        null,
                        apply.getHealAmount(),
                        true));
        return true;
    }

    private void applyFinishHeal(
            Level level, ServerPlayer player, ItemStack stack, InteractionHand hand, MedicineContext context) {
        HealSpec heal = spec.heal();
        if (!heal.healsOnFinish()) {
            return;
        }
        float amount = resolveHealAmount(player, heal);
        if (amount <= 0F) {
            return;
        }

        MedicineApplyEvent apply = new MedicineApplyEvent(
                player, stack, spec, hand, MedicineApplyEvent.Phase.FINISH_HEAL, context, null, amount, false);
        MinecraftForge.EVENT_BUS.post(apply);
        if (apply.isCanceled()) {
            return;
        }
        player.heal(apply.getHealAmount());
        markHealed(stack);
        MinecraftForge.EVENT_BUS.post(
                new MedicineApplyEvent(
                        player,
                        stack,
                        spec,
                        hand,
                        MedicineApplyEvent.Phase.FINISH_HEAL,
                        context,
                        null,
                        apply.getHealAmount(),
                        true));
    }

    private void applyFinishEffects(
            Level level, ServerPlayer player, ItemStack stack, InteractionHand hand, MedicineContext context) {
        applyEffectsForPhase(level, player, stack, hand, context, MedicineEffectSpec.EffectPhase.ON_FINISH);
        if (healedDuringUse(stack)) {
            applyEffectsForPhase(
                    level, player, stack, hand, context, MedicineEffectSpec.EffectPhase.ON_FINISH_IF_HEALED);
        }
    }

    private void applyEffectsForPhase(
            Level level,
            ServerPlayer player,
            ItemStack stack,
            InteractionHand hand,
            MedicineContext context,
            MedicineEffectSpec.EffectPhase phase) {
        for (MedicineEffectSpec effectSpec : spec.effects()) {
            if (effectSpec.phase() != phase) {
                continue;
            }
            if (player.getRandom().nextFloat() > effectSpec.applyChance()) {
                continue;
            }
            MobEffect effect = effectSpec.effect().get();
            if (effect == null) {
                continue;
            }

            MedicineApplyEvent apply = new MedicineApplyEvent(
                    player, stack, spec, hand, MedicineApplyEvent.Phase.EFFECT, context, effectSpec, 0F, false);
            MinecraftForge.EVENT_BUS.post(apply);
            if (apply.isCanceled()) {
                continue;
            }

            player.addEffect(
                    new MobEffectInstance(
                            effect,
                            effectSpec.durationTicks(),
                            effectSpec.amplifier(),
                            false,
                            effectSpec.showParticles(),
                            true),
                    player);
            MinecraftForge.EVENT_BUS.post(
                    new MedicineApplyEvent(
                            player, stack, spec, hand, MedicineApplyEvent.Phase.EFFECT, context, effectSpec, 0F, true));
        }
    }

    private void consumeOnFinish(ItemStack stack, ServerPlayer player) {
        if (player.getAbilities().instabuild) {
            return;
        }
        if (spec.consumeMode() == MedicineConsumeMode.SHRINK_ONE) {
            stack.shrink(1);
        } else if (spec.consumeMode() == MedicineConsumeMode.DURABILITY_ON_FINISH && stack.isDamageableItem()) {
            stack.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(player.getUsedItemHand()));
        }
    }

    private static float resolveHealAmount(Player player, HealSpec heal) {
        return heal.resolveAmount(player.getMaxHealth());
    }

    private static boolean needsHealing(LivingEntity entity) {
        return entity.getHealth() < entity.getMaxHealth();
    }

    private MedicineContext createContext(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        return new MedicineContext(
                player, stack, spec, hand, channelPulseCount(stack), healedDuringUse(stack));
    }

    private void showProgress(ServerPlayer player, int elapsedTicks) {
        ItemStack stack = player.getUseItem();
        int total = Math.max(1, getUseDuration(stack));
        if (elapsedTicks <= 0 || (elapsedTicks % 5 != 0 && elapsedTicks < total)) {
            return;
        }
        int percent = Math.min(100, Math.round(elapsedTicks * 100.0F / total));
        player.displayClientMessage(
                Component.translatable(
                        "message.tacz_morelevel.medicine.use_progress",
                        progressBar(elapsedTicks, total),
                        percent),
                true);
    }

    private static String progressBar(int ticks, int required) {
        int width = 10;
        int filled = Math.min(width, Math.max(0, Math.round(width * (ticks / (float) Math.max(1, required)))));
        return "[" + "#".repeat(filled) + ".".repeat(width - filled) + "]";
    }

    private static void resetChannelState(ItemStack stack) {
        stack.getOrCreateTag().putInt(TAG_CHANNEL_PULSES, 0);
        stack.getOrCreateTag().putBoolean(TAG_HEALED, false);
    }

    private static void clearChannelState(ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }
        stack.getTag().remove(TAG_CHANNEL_PULSES);
        stack.getTag().remove(TAG_HEALED);
        if (stack.getTag().isEmpty()) {
            stack.setTag(null);
        }
    }

    private static int channelPulseCount(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(TAG_CHANNEL_PULSES) : 0;
    }

    private static void incrementChannelPulseCount(ItemStack stack) {
        stack.getOrCreateTag().putInt(TAG_CHANNEL_PULSES, channelPulseCount(stack) + 1);
    }

    private static boolean healedDuringUse(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(TAG_HEALED);
    }

    private static void markHealed(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(TAG_HEALED, true);
    }
}
