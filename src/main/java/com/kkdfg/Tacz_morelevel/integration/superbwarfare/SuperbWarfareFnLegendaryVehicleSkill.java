package com.kkdfg.Tacz_morelevel.integration.superbwarfare;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * {@code tacz:fn_evolys_legendary} 传说技能：载具破坏（卓越前线）。
 *
 * <p>只在 {@link SuperbWarfareCompat#initWhenPresent()} 里注册。命中载具时在 TaCZ 自身伤害之外，
 * 直接扣 {@link #EXTRA_DURABILITY} 点车体耐久，不经过载具的 {@code DamageModifiers}。
 * {@link EntityHurtByGunEvent.Pre} 在穿甲拆分之前每个被命中实体只发一次。
 */
public final class SuperbWarfareFnLegendaryVehicleSkill {

    public static final ResourceLocation GUN_ID =
            ResourceLocation.fromNamespaceAndPath("tacz", "fn_evolys_legendary");

    private static final float EXTRA_DURABILITY = 8f;

    private SuperbWarfareFnLegendaryVehicleSkill() {}

    static void register() {
        MinecraftForge.EVENT_BUS.register(SuperbWarfareFnLegendaryVehicleSkill.class);
    }

    @SubscribeEvent
    public static void onGunHurtPre(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide() != LogicalSide.SERVER || event.isCanceled()) {
            return;
        }
        if (!GUN_ID.equals(event.getGunId())) {
            return;
        }
        VehicleEntity vehicle = resolveVehicle(event.getHurtEntity());
        if (vehicle == null || vehicle.isRemoved()) {
            return;
        }
        vehicle.setHealth(vehicle.getHealth() - EXTRA_DURABILITY);
    }

    private static VehicleEntity resolveVehicle(Entity hurt) {
        if (hurt instanceof VehicleEntity vehicle) {
            return vehicle;
        }
        if (hurt instanceof PartEntity<?> part && part.getParent() instanceof VehicleEntity vehicle) {
            return vehicle;
        }
        return null;
    }
}
