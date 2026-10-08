package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.plugin.CarbonFiberArmorPlateItem;
import com.kkdfg.Tacz_morelevel.item.custom.AbyssSeventhBulletproofArmorItem;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 深渊七级套 + 已安装碳钎维护板：移速 MULTIPLY_BASE、跳跃 Δvy。
 * 摔落减伤见 {@link FallArmorEvents}。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AbyssSeventhArmorAttributeEvents {

    private static final UUID ABYSS_SPEED_MODIFIER_UUID = UUID.fromString("2d2c1e00-9a00-4f00-a000-0000000ab701");
    private static final String ABYSS_SPEED_MODIFIER_NAME = "tacz_morelevel.abyss_seventh_speed";

    /**
     * 每件深渊盔甲在起跳时附加的竖直速度增量；标定后约 +0.25 格顶点高度/件。
     */
    private static final double JUMP_DELTA_VY_PER_PIECE = 0.08;

    private AbyssSeventhArmorAttributeEvents() {}

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        int n = AbyssSeventhBulletproofArmorItem.countEquippedPieces(entity);
        double plateJump = CarbonFiberArmorPlateItem.sumJumpDeltaVy(entity);
        double extra = n * JUMP_DELTA_VY_PER_PIECE + plateJump;
        if (extra <= 0d) {
            return;
        }
        Vec3 v = entity.getDeltaMovement();
        entity.setDeltaMovement(v.x, v.y + extra, v.z);
    }

    @SubscribeEvent
    public static void onArmorChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        EquipmentSlot slot = event.getSlot();
        if (slot != EquipmentSlot.HEAD
                && slot != EquipmentSlot.CHEST
                && slot != EquipmentSlot.LEGS
                && slot != EquipmentSlot.FEET) {
            return;
        }
        refreshAbyssSpeedModifier(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        refreshAbyssSpeedModifier(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        LivingEntity neo = event.getEntity();
        if (neo.level().isClientSide() || neo.level().getServer() == null) {
            return;
        }
        neo.level().getServer().execute(() -> refreshAbyssSpeedModifier(neo));
    }

    static void refreshAbyssSpeedModifier(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        AttributeInstance inst = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (inst == null) {
            return;
        }
        inst.removeModifier(ABYSS_SPEED_MODIFIER_UUID);
        int n = AbyssSeventhBulletproofArmorItem.countEquippedPieces(entity);
        double plateSpeed = CarbonFiberArmorPlateItem.sumSpeedMultiplyBase(entity);
        double amount = n * AbyssSeventhBulletproofArmorItem.SPEED_MULTIPLY_BASE_PER_PIECE + plateSpeed;
        if (amount <= 0d) {
            return;
        }
        inst.addTransientModifier(
                new AttributeModifier(
                        ABYSS_SPEED_MODIFIER_UUID,
                        ABYSS_SPEED_MODIFIER_NAME,
                        amount,
                        AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
