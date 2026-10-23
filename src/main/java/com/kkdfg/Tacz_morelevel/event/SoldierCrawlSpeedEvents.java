package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.api.TaczCrawlCapableMob;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 非天花板可趴 NPC：趴下时挂 MOVEMENT_SPEED 修饰符（当前为原速 ×0.3）。
 * 每 tick 校正（不该有 / 该有却无 / amount 不符则 remove+add）。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SoldierCrawlSpeedEvents {

    private static final UUID CRAWL_SPEED_MODIFIER_UUID =
            UUID.fromString("a7c3e91b-4d2f-4e08-9b1a-6f0c8d5e2a11");
    private static final String CRAWL_SPEED_MODIFIER_NAME = "tacz_morelevel.crawl_movement_speed";
    /** 趴下移速相对原速的倍率。 */
    private static final double CRAWL_SPEED_MULTIPLIER = 0.5D;
    private static final double AMOUNT_EPS = 1.0E-6D;

    private SoldierCrawlSpeedEvents() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (!(entity instanceof TaczCrawlCapableMob crawlMob)) {
            return;
        }
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }

        // 掏天花板：不挂，并清掉误挂
        if (entity instanceof CeilingDuelEntity) {
            removeCrawlSpeedModifier(speed);
            return;
        }

        double amount = CRAWL_SPEED_MULTIPLIER - 1.0D; // MULTIPLY_TOTAL: 0.3 → -0.7
        boolean shouldApply = crawlMob.isTaczCrawling() && CRAWL_SPEED_MULTIPLIER < 1.0D - AMOUNT_EPS;
        if (!shouldApply) {
            removeCrawlSpeedModifier(speed);
            return;
        }

        AttributeModifier existing = speed.getModifier(CRAWL_SPEED_MODIFIER_UUID);
        if (existing != null
                && existing.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL
                && Math.abs(existing.getAmount() - amount) <= AMOUNT_EPS) {
            return;
        }
        removeCrawlSpeedModifier(speed);
        speed.addTransientModifier(
                new AttributeModifier(
                        CRAWL_SPEED_MODIFIER_UUID,
                        CRAWL_SPEED_MODIFIER_NAME,
                        amount,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void removeCrawlSpeedModifier(AttributeInstance speed) {
        if (speed.getModifier(CRAWL_SPEED_MODIFIER_UUID) != null) {
            speed.removeModifier(CRAWL_SPEED_MODIFIER_UUID);
        }
    }
}
