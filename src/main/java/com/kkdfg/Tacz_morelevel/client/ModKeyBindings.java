package com.kkdfg.Tacz_morelevel.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModKeyBindings {

    public static final String CATEGORY = "key.categories." + Tacz_MoreLevel.MOD_ID;

    /** 盔甲传说技能（默认 X）；具体技能由客户端发送的 skillId 决定。 */
    public static KeyMapping ARMOR_LEGENDARY_SKILL;

    /** 盔甲夜视开关（默认 O）；时空 / 动力机甲 / 特种 / 爆破 / 冲击波头盔。 */
    public static KeyMapping ARMOR_NIGHT_VISION;

    private ModKeyBindings() {}

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        ARMOR_LEGENDARY_SKILL =
                new KeyMapping(
                        "key." + Tacz_MoreLevel.MOD_ID + ".armor_legendary_skill",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_X,
                        CATEGORY);
        event.register(ARMOR_LEGENDARY_SKILL);

        ARMOR_NIGHT_VISION =
                new KeyMapping(
                        "key." + Tacz_MoreLevel.MOD_ID + ".armor_night_vision",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_O,
                        CATEGORY);
        event.register(ARMOR_NIGHT_VISION);
    }
}
