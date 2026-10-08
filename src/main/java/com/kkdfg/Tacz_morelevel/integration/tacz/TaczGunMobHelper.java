package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IAmmo;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TaczGunMobHelper {

    public static final ResourceLocation MODERN_KINETIC_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "modern_kinetic_gun");

    /** TaCZ 通用弹药物品；具体口径由 {@link IAmmo#setAmmoId} 写入。 */
    public static final ResourceLocation TACZ_AMMO_ITEM =
            ResourceLocation.fromNamespaceAndPath("tacz", "ammo");

    /** 主手枪械弹匣子弹掉落：弹药 ID + 数量（向下取整）。 */
    public record MagazineAmmoDrop(ResourceLocation ammoId, int amount) {}

    private TaczGunMobHelper() {}

    /** 玩家交易/奖励用：满匣 TaCZ 枪，无 NPC 虚拟备弹。 */
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
        ItemStack stack = createPlayerGunStack(gunDataId);
        if (!stack.isEmpty()) {
            configureMobInfiniteAmmo(stack);
        }
        return stack;
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
     * NPC 死亡掉落替换用：与 {@link #createGunStack} 相同枪械 ID 与默认射击模式，但匣内 0 发、膛内无弹，
     * 且不写入怪用虚拟备弹，便于玩家拾取后按 TaCZ 正常背包弹药换弹。
     */
    public static ItemStack createFreshDroppedGunStackEmptyMag(ResourceLocation gunDataId) {
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
            return ItemStack.EMPTY;
        }
        GunData gunData = indexOpt.get().getGunData();
        gun.setFireMode(stack, pickDefaultFireMode(gunData));
        gun.setCurrentAmmoCount(stack, 0);
        gun.setBulletInBarrel(stack, false);
        return stack;
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

    /**
     * 非背包直读枪械启用 TaCZ「虚拟备弹」，供 {@link com.tacz.guns.api.entity.IGunOperator#reload()} 扣备弹填匣；
     * 数据里已是 {@code isInfinite} 的不再写 NBT。背包直读在 {@link #tickInfiniteMobAmmo} 中每 tick 补满弹匣。
     */
    public static void configureMobInfiniteAmmo(ItemStack gunStack) {
        IGun gun = IGun.getIGunOrNull(gunStack);
        if (gun == null) {
            return;
        }
        if (gun.useInventoryAmmo(gunStack)) {
            return;
        }
        boolean dataInfinite = TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack))
                .map(idx -> idx.getGunData().getReloadData().isInfinite())
                .orElse(false);
        if (dataInfinite) {
            return;
        }
        gun.setDummyAmmoAmount(gunStack, 1_000_000);
    }

    /**
     * 死亡掉落物：清空 TaCZ「虚拟备弹」{@link IGun#setDummyAmmoAmount}，避免玩家拾取怪掉的枪时自带巨额备用子弹。
     * 匣内当前弹 {@link IGun#getCurrentAmmoCount} 保留；背包直读枪无虚拟备弹，通常无需处理。
     */
    public static void stripDummyReserveAmmoForDrop(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return;
        }
        if (gun.useDummyAmmo(stack)) {
            gun.setDummyAmmoAmount(stack, 0);
        }
    }

    /** TaCZ 弹药物品；战争机器、歼灭者、神秘商人等死亡时不应掉落。 */
    public static boolean isTaczAmmoItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IAmmo;
    }

    /**
     * 死亡掉落物：TaCZ 枪替换为「白板」栈（空匣、无膛弹、无怪用虚拟备弹）；
     * 若无法按枪 ID 重建则回退为仅清空虚拟备弹。
     */
    public static void replaceDroppedGunStacksWithEmptyMag(Collection<ItemEntity> drops) {
        for (ItemEntity dropEntity : drops) {
            ItemStack stack = dropEntity.getItem();
            IGun iGun = IGun.getIGunOrNull(stack);
            if (iGun == null) {
                continue;
            }
            ResourceLocation gunId = iGun.getGunId(stack);
            if (gunId != null) {
                ItemStack fresh = createFreshDroppedGunStackEmptyMag(gunId);
                if (!fresh.isEmpty()) {
                    dropEntity.setItem(fresh);
                    continue;
                }
            }
            stripDummyReserveAmmoForDrop(stack);
        }
    }

    /**
     * 按当前主手枪械（含配件）有效弹匣容量与百分比计算应掉落的弹药；百分比为相对满匣子弹数，如 {@code 120}
     * 表示 1.2 倍弹匣容量（向下取整）。
     */
    public static Optional<MagazineAmmoDrop> computeMagazineAmmoDrop(ItemStack gunStack, int magazinePercent100) {
        if (magazinePercent100 <= 0) {
            return Optional.empty();
        }
        IGun iGun = IGun.getIGunOrNull(gunStack);
        if (iGun == null) {
            return Optional.empty();
        }
        Optional<CommonGunIndex> indexOpt = TimelessAPI.getCommonGunIndex(iGun.getGunId(gunStack));
        if (indexOpt.isEmpty()) {
            return Optional.empty();
        }
        GunData gunData = indexOpt.get().getGunData();
        ResourceLocation ammoId = gunData.getAmmoId();
        if (ammoId == null || TimelessAPI.getCommonAmmoIndex(ammoId).isEmpty()) {
            return Optional.empty();
        }
        int mag = Math.max(1, AttachmentDataUtils.getAmmoCountWithAttachment(gunStack, gunData));
        int amount = Mth.floor(mag * (long) magazinePercent100 / 100L);
        if (amount <= 0) {
            return Optional.empty();
        }
        return Optional.of(new MagazineAmmoDrop(ammoId, amount));
    }

    private static ItemStack createAmmoStackTemplate(ResourceLocation ammoId) {
        Item item = BuiltInRegistries.ITEM.get(TACZ_AMMO_ITEM);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        IAmmo ammo = IAmmo.getIAmmoOrNull(stack);
        if (ammo == null) {
            return ItemStack.EMPTY;
        }
        ammo.setAmmoId(stack, ammoId);
        return stack;
    }

    /**
     * 死亡等：在主手为 TaCZ 枪且弹药数据有效时，在实体位置生成与枪匹配的弹药掉落（超堆叠上限时拆成多个掉落物）。
     */
    public static void spawnMagazineAmmoDeathDrop(LivingEntity entity, int magazinePercent100) {
        if (entity.level().isClientSide() || !IGun.mainHandHoldGun(entity)) {
            return;
        }
        Optional<MagazineAmmoDrop> plan = computeMagazineAmmoDrop(entity.getMainHandItem(), magazinePercent100);
        if (plan.isEmpty()) {
            return;
        }
        MagazineAmmoDrop drop = plan.get();
        ItemStack template = createAmmoStackTemplate(drop.ammoId());
        if (template.isEmpty()) {
            return;
        }
        int maxStack = template.getMaxStackSize();
        int remaining = drop.amount();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        while (remaining > 0) {
            int n = Math.min(remaining, maxStack);
            ItemStack stack = template.copy();
            stack.setCount(n);
            entity.level().addFreshEntity(new ItemEntity(entity.level(), x, y, z, stack));
            remaining -= n;
        }
    }

    /** 服务端：背包直读枪补满匣；虚拟备弹过低时补回，避免换弹 AI 哑火。 */
    public static void tickInfiniteMobAmmo(LivingEntity entity) {
        if (entity.level().isClientSide() || !IGun.mainHandHoldGun(entity)) {
            return;
        }
        ItemStack stack = entity.getMainHandItem();
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return;
        }
        Optional<CommonGunIndex> indexOpt = TimelessAPI.getCommonGunIndex(gun.getGunId(stack));
        if (indexOpt.isEmpty()) {
            return;
        }
        GunData gunData = indexOpt.get().getGunData();
        if (!gun.useInventoryAmmo(stack) && !gunData.getReloadData().isInfinite() && !gun.useDummyAmmo(stack)) {
            configureMobInfiniteAmmo(stack);
        }
        int maxMag = Math.max(1, AttachmentDataUtils.getAmmoCountWithAttachment(stack, gunData));
        int cur = gun.getCurrentAmmoCount(stack);
        if (gun.useInventoryAmmo(stack)) {
            if (cur < maxMag) {
                gun.setCurrentAmmoCount(stack, maxMag);
            }
            return;
        }
        if (gun.useDummyAmmo(stack) && gun.getDummyAmmoAmount(stack) < maxMag * 4) {
            gun.setDummyAmmoAmount(stack, 1_000_000);
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
     * 仍补虚拟备弹（怪用），再 {@code initialData}+{@code draw}。
     */
    public static boolean equipMainHandFromStack(LivingEntity entity, ItemStack gunStack) {
        if (entity.level().isClientSide() || gunStack == null || gunStack.isEmpty()) {
            return false;
        }
        if (IGun.getIGunOrNull(gunStack) == null) {
            return false;
        }
        ItemStack copy = gunStack.copy();
        configureMobInfiniteAmmo(copy);
        return equipMainHandInternal(entity, copy);
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
