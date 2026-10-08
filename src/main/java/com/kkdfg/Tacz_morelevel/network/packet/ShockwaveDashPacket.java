package com.kkdfg.Tacz_morelevel.network.packet;

import com.kkdfg.Tacz_morelevel.armor.skill.impl.ShockwaveDashSkill;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** 冲击波蓄力：按下边沿 START / 松开 RELEASE。 */
public final class ShockwaveDashPacket {

    public enum Action {
        START,
        RELEASE
    }

    private final Action action;

    public ShockwaveDashPacket(Action action) {
        this.action = action;
    }

    public static void encode(ShockwaveDashPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
    }

    public static ShockwaveDashPacket decode(FriendlyByteBuf buf) {
        return new ShockwaveDashPacket(buf.readEnum(Action.class));
    }

    public static void handle(ShockwaveDashPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(
                () -> {
                    ServerPlayer sender = ctx.getSender();
                    if (sender == null) {
                        return;
                    }
                    if (msg.action == Action.START) {
                        ShockwaveDashSkill.beginCharge(sender);
                    } else if (msg.action == Action.RELEASE) {
                        ShockwaveDashSkill.releaseCharge(sender);
                    }
                });
        ctx.setPacketHandled(true);
    }
}
