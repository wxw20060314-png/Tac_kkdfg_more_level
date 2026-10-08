package com.kkdfg.Tacz_morelevel.network.packet;

import com.kkdfg.Tacz_morelevel.armor.skill.ArmorLegendarySkillRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ArmorLegendarySkillPacket {

    private final ResourceLocation skillId;

    public ArmorLegendarySkillPacket(ResourceLocation skillId) {
        this.skillId = skillId;
    }

    public static void encode(ArmorLegendarySkillPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.skillId);
    }

    public static ArmorLegendarySkillPacket decode(FriendlyByteBuf buf) {
        return new ArmorLegendarySkillPacket(buf.readResourceLocation());
    }

    public static void handle(ArmorLegendarySkillPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(
                () -> {
                    ServerPlayer sender = ctx.getSender();
                    if (sender != null) {
                        ArmorLegendarySkillRegistry.tryActivate(sender, msg.skillId);
                    }
                });
        ctx.setPacketHandled(true);
    }
}
