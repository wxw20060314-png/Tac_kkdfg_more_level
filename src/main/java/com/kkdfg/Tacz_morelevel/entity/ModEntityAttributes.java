package com.kkdfg.Tacz_morelevel.entity;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.AnnihilatorEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.ScorpionEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.SlimeCommanderEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.SlimeWarriorEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.BombBabyEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.BombCartEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.AbstractShoulderCannonEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.BombSightEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.CeilingDuelEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.AssaultSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.LurkerEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MercenarySoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MysteriousMerchantEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MilitiaEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.PunisherEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.RefugeeEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.SpecialForcesSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.WarMachineEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.SuperSpecialSoldierEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.LevMarshalEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MarshalGuardEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.DemonLordBodyguardEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.EliteBodyguardEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.BeautyBodyguardEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.GuardEntity;
import com.kkdfg.Tacz_morelevel.entity.companion.StandardBodyguardEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntityAttributes {

    @SubscribeEvent
    public static void onCreateAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MILITIA.get(), MilitiaEntity.createAttributes().build());
        event.put(ModEntities.REFUGEE.get(), RefugeeEntity.createAttributes().build());
        event.put(ModEntities.MERCENARY.get(), MercenarySoldierEntity.createAttributes().build());
        event.put(ModEntities.MYSTERIOUS_MERCHANT.get(), MysteriousMerchantEntity.createAttributes().build());
        event.put(ModEntities.ASSAULT_SOLDIER.get(), AssaultSoldierEntity.createAttributes().build());
        event.put(
                ModEntities.LURKER.get(),
                LurkerEntity.createAttributes().build());
        event.put(ModEntities.SPECIAL_FORCES.get(), SpecialForcesSoldierEntity.createAttributes().build());
        event.put(
                ModEntities.SUPER_SPECIAL_SOLDIER.get(),
                SuperSpecialSoldierEntity.createAttributes().build());
        event.put(ModEntities.LEV_MARSHAL.get(), LevMarshalEntity.createAttributes().build());
        event.put(ModEntities.MARSHAL_GUARD.get(), MarshalGuardEntity.createAttributes().build());
        event.put(ModEntities.WAR_MACHINE.get(), WarMachineEntity.createAttributes().build());
        event.put(ModEntities.ANNIHILATOR.get(), AnnihilatorEntity.createAttributes().build());
        event.put(ModEntities.SCORPION.get(), ScorpionEntity.createAttributes().build());
        event.put(ModEntities.SLIME_COMMANDER.get(),
                SlimeCommanderEntity.createAttributes().build());
        event.put(ModEntities.SLIME_WARRIOR.get(), SlimeWarriorEntity.createAttributes().build());
        event.put(ModEntities.BOMB_BABY.get(), BombBabyEntity.createAttributes().build());
        event.put(ModEntities.CEILING_DUEL.get(), CeilingDuelEntity.createAttributes().build());
        event.put(ModEntities.BOMB_SIGHT.get(), BombSightEntity.createAttributes().build());
        event.put(ModEntities.PUNISHER.get(), PunisherEntity.createAttributes().build());
        event.put(ModEntities.BOMB_CART.get(), BombCartEntity.createAttributes().build());
        event.put(
                ModEntities.PUNISHER_SHOULDER_CANNON.get(),
                AbstractShoulderCannonEntity.createAttributes().build());
        event.put(ModEntities.BEAUTY_BODYGUARD.get(), BeautyBodyguardEntity.createAttributes().build());
        event.put(ModEntities.STANDARD_BODYGUARD.get(), StandardBodyguardEntity.createAttributes().build());
        event.put(ModEntities.ELITE_BODYGUARD.get(), EliteBodyguardEntity.createAttributes().build());
        event.put(ModEntities.DEMON_LORD_BODYGUARD.get(), DemonLordBodyguardEntity.createAttributes().build());
        event.put(ModEntities.GUARD.get(), GuardEntity.createAttributes().build());
    }

    private ModEntityAttributes() {}
}

