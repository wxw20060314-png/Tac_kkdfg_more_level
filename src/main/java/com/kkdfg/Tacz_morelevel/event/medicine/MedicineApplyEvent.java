package com.kkdfg.Tacz_morelevel.event.medicine;

import com.kkdfg.Tacz_morelevel.item.medicine.MedicineContext;
import com.kkdfg.Tacz_morelevel.item.medicine.MedicineEffectSpec;
import com.kkdfg.Tacz_morelevel.item.medicine.MedicineSpec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * 回血或效果实际生效前/后；可取消或修改回血量。
 */
@Cancelable
public class MedicineApplyEvent extends PlayerEvent {

    public enum Phase {
        CHANNEL_HEAL,
        FINISH_HEAL,
        EFFECT
    }

    private final ItemStack stack;
    private final MedicineSpec spec;
    private final InteractionHand hand;
    private final Phase phase;
    private final MedicineContext context;
    private final MedicineEffectSpec effectSpec;
    private float healAmount;
    private final boolean post;

    public MedicineApplyEvent(
            Player player,
            ItemStack stack,
            MedicineSpec spec,
            InteractionHand hand,
            Phase phase,
            MedicineContext context,
            MedicineEffectSpec effectSpec,
            float healAmount,
            boolean post) {
        super(player);
        this.stack = stack;
        this.spec = spec;
        this.hand = hand;
        this.phase = phase;
        this.context = context;
        this.effectSpec = effectSpec;
        this.healAmount = healAmount;
        this.post = post;
    }

    public ItemStack getStack() {
        return stack;
    }

    public MedicineSpec getSpec() {
        return spec;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public Phase getApplyPhase() {
        return phase;
    }

    public MedicineContext getContext() {
        return context;
    }

    public MedicineEffectSpec getEffectSpec() {
        return effectSpec;
    }

    public float getHealAmount() {
        return healAmount;
    }

    public void setHealAmount(float healAmount) {
        this.healAmount = Math.max(0F, healAmount);
    }

    public boolean isPost() {
        return post;
    }
}
