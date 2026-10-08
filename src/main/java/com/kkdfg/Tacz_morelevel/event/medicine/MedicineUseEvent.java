package com.kkdfg.Tacz_morelevel.event.medicine;

import com.kkdfg.Tacz_morelevel.item.medicine.MedicineContext;
import com.kkdfg.Tacz_morelevel.item.medicine.MedicineSpec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/** 用药读条流程事件（开始 / 读条中 / 结束 / 取消）。 */
public abstract class MedicineUseEvent extends PlayerEvent {

    private final ItemStack stack;
    private final MedicineSpec spec;
    private final InteractionHand hand;

    protected MedicineUseEvent(Player player, ItemStack stack, MedicineSpec spec, InteractionHand hand) {
        super(player);
        this.stack = stack;
        this.spec = spec;
        this.hand = hand;
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

    /** 开始读条前；可取消本次用药。 */
    @Cancelable
    public static final class Start extends MedicineUseEvent {
        public Start(Player player, ItemStack stack, MedicineSpec spec, InteractionHand hand) {
            super(player, stack, spec, hand);
        }
    }

    /** 读条过程中每次 channel 脉冲后。 */
    public static final class Tick extends MedicineUseEvent {
        private final MedicineContext context;

        public Tick(Player player, ItemStack stack, MedicineSpec spec, InteractionHand hand, MedicineContext context) {
            super(player, stack, spec, hand);
            this.context = context;
        }

        public MedicineContext getContext() {
            return context;
        }
    }

    /** 读条正常结束后；可 {@link #setSkipDefault(boolean)} 跳过 spec 默认逻辑。 */
    @Cancelable
    public static final class Finish extends MedicineUseEvent {
        private final MedicineContext context;
        private boolean skipDefault;

        public Finish(Player player, ItemStack stack, MedicineSpec spec, InteractionHand hand, MedicineContext context) {
            super(player, stack, spec, hand);
            this.context = context;
        }

        public MedicineContext getContext() {
            return context;
        }

        public boolean isSkipDefault() {
            return skipDefault;
        }

        public void setSkipDefault(boolean skipDefault) {
            this.skipDefault = skipDefault;
        }
    }

    /** 读条被打断或中途停止。 */
    public static final class Cancel extends MedicineUseEvent {
        private final MedicineContext context;

        public Cancel(Player player, ItemStack stack, MedicineSpec spec, InteractionHand hand, MedicineContext context) {
            super(player, stack, spec, hand);
            this.context = context;
        }

        public MedicineContext getContext() {
            return context;
        }
    }
}
