package com.kkdfg.Tacz_morelevel.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.skill.power_mech.PowerMechLegendarySkills;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.BlastSummonBombCartSkill;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.ShockwaveDashSkill;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.SpacetimeBlinkSkill;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.StealthCloakSkill;
import com.kkdfg.Tacz_morelevel.armor.skill.impl.StealthSeventhSkill;
import com.kkdfg.Tacz_morelevel.armor.skill.officer.OfficerSummonGuardSkill;
import com.kkdfg.Tacz_morelevel.network.ModNetwork;
import com.kkdfg.Tacz_morelevel.network.packet.ArmorLegendarySkillPacket;
import com.kkdfg.Tacz_morelevel.network.packet.ShockwaveDashPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ArmorLegendarySkillClientEvents {

    private static boolean shockwaveKeyWasDown;

    private ArmorLegendarySkillClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            if (shockwaveKeyWasDown) {
                shockwaveKeyWasDown = false;
            }
            return;
        }
        if (ModKeyBindings.ARMOR_LEGENDARY_SKILL == null) {
            return;
        }

        if (ShockwaveDashSkill.hasFullSet(mc.player)) {
            tickShockwaveHold(mc);
            // 冲击波不用点按包；仍吞掉 click 避免串到其它逻辑
            while (ModKeyBindings.ARMOR_LEGENDARY_SKILL.consumeClick()) {
                // discard
            }
            return;
        }

        shockwaveKeyWasDown = false;
        while (ModKeyBindings.ARMOR_LEGENDARY_SKILL.consumeClick()) {
            ResourceLocation skillId = resolveNonShockwaveSkillId(mc);
            if (skillId == null) {
                continue;
            }
            ModNetwork.CHANNEL.send(PacketDistributor.SERVER.noArg(), new ArmorLegendarySkillPacket(skillId));
        }
    }

    private static void tickShockwaveHold(Minecraft mc) {
        boolean down = ModKeyBindings.ARMOR_LEGENDARY_SKILL.isDown();
        if (down && !shockwaveKeyWasDown) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.SERVER.noArg(),
                    new ShockwaveDashPacket(ShockwaveDashPacket.Action.START));
        } else if (!down && shockwaveKeyWasDown) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.SERVER.noArg(),
                    new ShockwaveDashPacket(ShockwaveDashPacket.Action.RELEASE));
        }
        shockwaveKeyWasDown = down;
    }

    private static ResourceLocation resolveNonShockwaveSkillId(Minecraft mc) {
        if (BlastSummonBombCartSkill.hasFullSet(mc.player)) {
            return BlastSummonBombCartSkill.ID;
        }
        if (SpacetimeBlinkSkill.hasFullSet(mc.player)) {
            return SpacetimeBlinkSkill.ID;
        }
        if (PowerMechLegendarySkills.hasFullSet(mc.player)) {
            return PowerMechLegendarySkills.OVERLOAD;
        }
        if (OfficerSummonGuardSkill.hasFullSet(mc.player)) {
            return OfficerSummonGuardSkill.ID;
        }
        if (StealthCloakSkill.hasFullSet(mc.player)) {
            return StealthCloakSkill.ID;
        }
        if (StealthSeventhSkill.hasFullSet(mc.player)) {
            return StealthSeventhSkill.ID;
        }
        return null;
    }
}
