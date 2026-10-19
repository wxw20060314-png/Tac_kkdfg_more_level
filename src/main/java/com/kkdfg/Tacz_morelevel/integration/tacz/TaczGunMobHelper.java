package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TaczGunMobHelper {

    public static final ResourceLocation MODERN_KINETIC_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "modern_kinetic_gun");

    private TaczGunMobHelper() {}

    public static ItemStack createPlayerGunStack(ResourceLocation gunDataId) {
        Item item = BuiltInRegistries.ITEM.get(MODERN_KINETIC_GUN);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return ItemStack.EMPTY;
        }
        gun.setGunId(stack, gunDataId);
        Optional<CommonGunIndex> indexOpt = TimelessAPI.getCommonGunIndex(gunDataId);
        if (indexOpt.isEmpty()) {
            gun.setCurrentAmmoCount(stack, 1);
            gun.setBulletInBarrel(stack, false);
            return stack;
        }
        GunData gunData = indexOpt.get().getGunData();
        gun.setFireMode(stack, pickDefaultFireMode(gunData));
        int mag = Math.max(1, gunData.getAmmoAmount());
        gun.setCurrentAmmoCount(stack, mag);
        Bolt bolt = gunData.getBolt();
        if (bolt == Bolt.CLOSED_BOLT || bolt == Bolt.MANUAL_ACTION) {
            gun.setBulletInBarrel(stack, true);
        } else {
            gun.setBulletInBarrel(stack, false);
        }
        return stack;
    }

    public static ItemStack createGunStack(ResourceLocation gunDataId) {
        return createPlayerGunStack(gunDataId);
    }

    public static ItemStack createGunStackWithAttachments(
            ResourceLocation gunDataId, Map<AttachmentType, ResourceLocation> attachments) {
        ItemStack stack = createGunStack(gunDataId);
        if (stack.isEmpty() || attachments == null || attachments.isEmpty()) {
            return stack;
        }
        installAttachments(stack, attachments);
        return stack;
    }

    private static void installAttachments(
            ItemStack gunStack, Map<AttachmentType, ResourceLocation> attachments) {
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) {
            return;
        }
        for (ResourceLocation attachmentId : attachments.values()) {
            ItemStack attachmentStack = AttachmentItemBuilder.create().setId(attachmentId).build();
            if (!attachmentStack.isEmpty()) {
                gun.installAttachment(gunStack, attachmentStack);
            }
        }
        refillMagForAttachments(gunStack);
        preferAutoFireModeForMobMainHandGun(gunStack);
    }

    private static void refillMagForAttachments(ItemStack gunStack) {
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) {
            return;
        }
        Optional<CommonGunIndex> indexOpt = TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack));
        if (indexOpt.isEmpty()) {
            return;
        }
        GunData gunData = indexOpt.get().getGunData();
        int maxMag = Math.max(1, AttachmentDataUtils.getAmmoCountWithAttachment(gunStack, gunData));
        gun.setCurrentAmmoCount(gunStack, maxMag);
        Bolt bolt = gunData.getBolt();
        if (bolt == Bolt.CLOSED_BOLT || bolt == Bolt.MANUAL_ACTION) {
            gun.setBulletInBarrel(gunStack, true);
        }
    }

    /**
     * 怪/掉落生成用：在合法模式里优先 {@link FireMode#AUTO}、其次 {@link FireMode#BURST}，再取首个非
     * {@link FireMode#UNKNOWN}（避免 JSON 写 {@code [SEMI, AUTO]} 时仍落到半自动导致 AI 只能「断断续续」点射）。
     */
    private static FireMode pickDefaultFireMode(GunData gunData) {
        List<?> modeList = gunData.getFireModeSet();
        if (modeList == null || modeList.isEmpty()) {
            return FireMode.SEMI;
        }
        FireMode firstNonUnknown = null;
        for (Object o : modeList) {
            if (o instanceof FireMode fm && fm != FireMode.UNKNOWN) {
                if (firstNonUnknown == null) {
                    firstNonUnknown = fm;
                }
            }
        }
        for (Object o : modeList) {
            if (o == FireMode.AUTO) {
                return FireMode.AUTO;
            }
        }
        for (Object o : modeList) {
            if (o == FireMode.BURST) {
                return FireMode.BURST;
            }
        }
        return firstNonUnknown != null ? firstNonUnknown : FireMode.SEMI;
    }

    private static boolean fireModeAllowed(GunData gunData, FireMode mode) {
        if (mode == FireMode.UNKNOWN) {
            return false;
        }
        List<?> modeList = gunData.getFireModeSet();
        if (modeList == null) {
            return false;
        }
        for (Object o : modeList) {
            if (o == mode) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当栈上火控为 {@link FireMode#UNKNOWN} 或不在该枪 {@link GunData#getFireModeSet()} 内时，写回 {@link #pickDefaultFireMode}。
     * 用于缓解：TaCZ 换弹/脚本把模式写坏、拾取怪掉落枪后不能连发等。
     */
    public static void normalizeFireModeIfInvalid(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return;
        }
        Optional<CommonGunIndex> indexOpt = TimelessAPI.getCommonGunIndex(gun.getGunId(stack));
        if (indexOpt.isEmpty()) {
            return;
        }
        GunData gunData = indexOpt.get().getGunData();
        if (fireModeAllowed(gunData, gun.getFireMode(stack))) {
            return;
        }
        gun.setFireMode(stack, pickDefaultFireMode(gunData));
    }

    /**
     * 若枪械数据支持 {@link FireMode#AUTO} 且当前不是 AUTO，则切到 AUTO（供人形怪等连发 AI；纯半自动数据不受影响）。
     */
    public static void preferAutoFireModeForMobMainHandGun(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return;
        }
        Optional<CommonGunIndex> indexOpt = TimelessAPI.getCommonGunIndex(gun.getGunId(stack));
        if (indexOpt.isEmpty()) {
            return;
        }
        GunData gunData = indexOpt.get().getGunData();
        if (!fireModeAllowed(gunData, FireMode.AUTO)) {
            return;
        }
        if (gun.getFireMode(stack) == FireMode.AUTO) {
            return;
        }
        gun.setFireMode(stack, FireMode.AUTO);
    }

    /** 服务端：背包直读枪每 tick 补满弹匣。 */
    public static void tickInfiniteMobAmmo(LivingEntity entity) {
        if (entity.level().isClientSide() || !IGun.mainHandHoldGun(entity)) {
            return;
        }
        ItemStack stack = entity.getMainHandItem();
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null || !gun.useInventoryAmmo(stack)) {
            return;
        }
        Optional<CommonGunIndex> indexOpt = TimelessAPI.getCommonGunIndex(gun.getGunId(stack));
        if (indexOpt.isEmpty()) {
            return;
        }
        GunData gunData = indexOpt.get().getGunData();
        int maxMag = Math.max(1, AttachmentDataUtils.getAmmoCountWithAttachment(stack, gunData));
        if (gun.getCurrentAmmoCount(stack) < maxMag) {
            gun.setCurrentAmmoCount(stack, maxMag);
        }
    }

    /** @return 是否成功在主手放置有效枪械栈 */
    public static boolean equipMainHand(LivingEntity entity, ResourceLocation gunDataId) {
        return equipMainHandInternal(entity, createGunStack(gunDataId));
    }

    public static boolean equipMainHandWithAttachments(
            LivingEntity entity,
            ResourceLocation gunDataId,
            Map<AttachmentType, ResourceLocation> attachments) {
        return equipMainHandInternal(entity, createGunStackWithAttachments(gunDataId, attachments));
    }

    /**
     * 用已有枪栈装备主手（保留配件、匣内弹药等 NBT）；不重建、不强制满匣。
     * 再 {@code initialData}+{@code draw}。
     */
    public static boolean equipMainHandFromStack(LivingEntity entity, ItemStack gunStack) {
        if (entity.level().isClientSide() || gunStack == null || gunStack.isEmpty()) {
            return false;
        }
        if (IGun.getIGunOrNull(gunStack) == null) {
            return false;
        }
        return equipMainHandInternal(entity, gunStack.copy());
    }

    private static boolean equipMainHandInternal(LivingEntity entity, ItemStack gunStack) {
        if (entity.level().isClientSide()) {
            return false;
        }
        entity.setItemSlot(EquipmentSlot.MAINHAND, gunStack);
        if (gunStack.isEmpty()) {
            return false;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(entity);
        op.initialData();
        op.draw(entity::getMainHandItem);
        return true;
    }
}
