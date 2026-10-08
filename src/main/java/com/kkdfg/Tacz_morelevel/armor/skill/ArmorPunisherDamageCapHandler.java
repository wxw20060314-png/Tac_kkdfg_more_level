package com.kkdfg.Tacz_morelevel.armor.skill;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.PunisherBulletproofArmorItem;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 判罚者传说技能：限制伤害。 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArmorPunisherDamageCapHandler {

    private static final TagKey<DamageType> TACZ_BULLETS =
            TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("tacz:bullets"));

    private static final String SUPERB_WARFARE_MOD_ID = "superbwarfare";

    private ArmorPunisherDamageCapHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        if (!PunisherBulletproofArmorItem.hasFullSet(target)) {
            return;
        }
        if (!isBulletDamage(event.getSource(), target)) {
            return;
        }
        float amount = event.getAmount();
        float cap = PunisherBulletproofArmorItem.BULLET_DAMAGE_CAP;
        if (amount <= cap) {
            return;
        }
        float excess = 5 * (amount - cap);
        event.setAmount(cap);

        int pieces = PunisherBulletproofArmorItem.countEquippedPieces(target);
        if (pieces <= 0) {
            return;
        }
        int perPiece = Mth.floor(excess) / pieces;
        if (perPiece <= 0) {
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = target.getItemBySlot(slot);
            if (!(stack.getItem() instanceof PunisherBulletproofArmorItem)) {
                continue;
            }
            stack.hurtAndBreak(perPiece, target, living -> living.broadcastBreakEvent(slot));
        }
    }

    private static boolean isBulletDamage(DamageSource source, LivingEntity target) {
        if (source.typeHolder().is(TACZ_BULLETS)) {
            return true;
        }
        Optional<ResourceKey<DamageType>> key = source.typeHolder().unwrapKey();
        if (key.isEmpty()) {
            key =
                    target.level()
                            .registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getResourceKey(source.type());
        }
        return key.map(
                        k -> {
                            ResourceLocation id = k.location();
                            if (SUPERB_WARFARE_MOD_ID.equals(id.getNamespace())) {
                                return true;
                            }
                            return "tacz".equals(id.getNamespace())
                                    && id.getPath().startsWith("bullet");
                        })
                .orElse(false);
    }
}
