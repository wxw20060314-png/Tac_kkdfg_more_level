package com.kkdfg.Tacz_morelevel.armor.skill;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.item.custom.PoliceArmorItem;
import com.kkdfg.Tacz_morelevel.item.custom.PunisherBulletproofArmorItem;
import java.util.Optional;
import java.util.function.Predicate;
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

/**
 * 限制伤害：全套时子弹实伤封顶，溢出部分按倍率分摊磨损各件盔甲。
 *
 * <ul>
 *   <li>判罚者套：{@link PunisherBulletproofArmorItem#BULLET_DAMAGE_CAP}
 *   <li>警官套：{@link PoliceArmorItem#BULLET_DAMAGE_CAP}
 * </ul>
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArmorPunisherDamageCapHandler {

    private static final TagKey<DamageType> TACZ_BULLETS =
            TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("tacz:bullets"));

    private static final String SUPERB_WARFARE_MOD_ID = "superbwarfare";

    /**
     * 溢出耐久倍率：超出 cap 的伤害量 × 本系数后均摊到各甲，再 {@code hurtAndBreak}。
     * 判罚者 / 警官共用。
     */
    private static final float EXCESS_DURABILITY_FACTOR = 5.0F;

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
        if (!isBulletDamage(event.getSource(), target)) {
            return;
        }

        final float cap;
        final Predicate<ItemStack> pieceMatch;
        final int pieces;
        if (PunisherBulletproofArmorItem.hasFullSet(target)) {
            cap = PunisherBulletproofArmorItem.BULLET_DAMAGE_CAP;
            pieceMatch = stack -> stack.getItem() instanceof PunisherBulletproofArmorItem;
            pieces = PunisherBulletproofArmorItem.countEquippedPieces(target);
        } else if (PoliceArmorItem.hasFullSet(target)) {
            cap = PoliceArmorItem.BULLET_DAMAGE_CAP;
            pieceMatch = stack -> stack.getItem() instanceof PoliceArmorItem;
            pieces = PoliceArmorItem.countEquippedPieces(target);
        } else {
            return;
        }

        float amount = event.getAmount();
        if (amount <= cap) {
            return;
        }
        float excess = EXCESS_DURABILITY_FACTOR * (amount - cap);
        event.setAmount(cap);

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
            if (!pieceMatch.test(stack)) {
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
