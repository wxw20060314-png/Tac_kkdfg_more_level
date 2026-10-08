package com.kkdfg.Tacz_morelevel.armor.skill.officer;

import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.entity.companion.GuardEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.MarshalFactionHelper;
import com.kkdfg.Tacz_morelevel.item.custom.OfficerUniformArmorItem;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 军官胸甲护卫名册：槽位 / 链路 TTL / 储备回补 / BondId / 召唤，气质对齐列夫大帅小弟系统。
 *
 * <p>数据写在军官<strong>胸甲</strong> NBT；按 X 召唤，不自动空槽放出。
 */
public final class OfficerGuardRosterHelper {

    public static final int MAX_SLOTS = 4;
    public static final int MAX_RESERVE = 10;
    /** 与列夫相同：15s 回补 1。 */
    public static final int RESERVE_REFILL_TTL_TICKS = MarshalFactionHelper.RESERVE_REFILL_TTL_TICKS;
    /** 与列夫相同：5s 链路宽限。 */
    public static final int LINK_TIMEOUT_TICKS = MarshalFactionHelper.LINK_TIMEOUT_TICKS;
    public static final int SPAWN_SCAN_RANGE = MarshalFactionHelper.SPAWN_SCAN_RANGE;
    public static final int SPAWN_ATTEMPTS = MarshalFactionHelper.SPAWN_ATTEMPTS;
    public static final double RECALL_DIST_IDLE = MarshalFactionHelper.RECALL_DIST_IDLE;
    public static final double RECALL_DIST_COMBAT = MarshalFactionHelper.RECALL_DIST_COMBAT;

    private static final String TAG_BOND_ID = "OfficerBondId";
    private static final String TAG_RESERVE = "OfficerReserve";
    private static final String TAG_RESERVE_TTL = "OfficerReserveTtl";
    private static final String TAG_SLOTS = "OfficerGuardSlots";

    private OfficerGuardRosterHelper() {}

    public static boolean isOfficerChest(ItemStack stack) {
        return stack.getItem() instanceof OfficerUniformArmorItem armor
                && armor.getType() == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE;
    }

    @Nullable
    public static ItemStack getEquippedChest(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return isOfficerChest(chest) ? chest : null;
    }

    /** 确保胸甲有 BondId；储备缺省填满。返回 BondId。 */
    public static UUID ensureInitialized(ItemStack chest) {
        CompoundTag tag = chest.getOrCreateTag();
        if (!tag.hasUUID(TAG_BOND_ID)) {
            tag.putUUID(TAG_BOND_ID, UUID.randomUUID());
        }
        if (!tag.contains(TAG_RESERVE, Tag.TAG_INT)) {
            tag.putInt(TAG_RESERVE, MAX_RESERVE);
        }
        if (!tag.contains(TAG_SLOTS, Tag.TAG_LIST)) {
            tag.put(TAG_SLOTS, emptySlotsList());
        }
        return tag.getUUID(TAG_BOND_ID);
    }

    @Nullable
    public static UUID getBondId(ItemStack chest) {
        if (!isOfficerChest(chest) || !chest.hasTag()) {
            return null;
        }
        CompoundTag tag = chest.getTag();
        return tag != null && tag.hasUUID(TAG_BOND_ID) ? tag.getUUID(TAG_BOND_ID) : null;
    }

    public static int getReserve(ItemStack chest) {
        if (!chest.hasTag()) {
            return MAX_RESERVE;
        }
        CompoundTag tag = chest.getTag();
        if (tag == null || !tag.contains(TAG_RESERVE, Tag.TAG_INT)) {
            return MAX_RESERVE;
        }
        return Mth.clamp(tag.getInt(TAG_RESERVE), 0, MAX_RESERVE);
    }

    public static int firstEmptySlot(ItemStack chest) {
        ensureInitialized(chest);
        ListTag slots = chest.getTag().getList(TAG_SLOTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < MAX_SLOTS; i++) {
            CompoundTag entry = i < slots.size() ? slots.getCompound(i) : new CompoundTag();
            if (!entry.hasUUID("Id")) {
                return i;
            }
        }
        return -1;
    }

    /** 名册槽中链路 TTL 仍 &gt; 0 的数量（含未加载宽限，作防护 z）。 */
    public static int countLinkedSlots(ItemStack chest) {
        if (!isOfficerChest(chest) || !chest.hasTag()) {
            return 0;
        }
        CompoundTag tag = chest.getTag();
        if (tag == null || !tag.contains(TAG_SLOTS, Tag.TAG_LIST)) {
            return 0;
        }
        ListTag slots = tag.getList(TAG_SLOTS, Tag.TAG_COMPOUND);
        int n = 0;
        for (int i = 0; i < Math.min(slots.size(), MAX_SLOTS); i++) {
            CompoundTag entry = slots.getCompound(i);
            if (entry.hasUUID("Id") && entry.getInt("Ttl") > 0) {
                n++;
            }
        }
        return n;
    }

    /** 护卫是否仍登记在胸甲名册（有 UUID 槽位）。 */
    public static boolean rosterContains(ItemStack chest, UUID guardId) {
        if (guardId == null || !isOfficerChest(chest) || !chest.hasTag()) {
            return false;
        }
        CompoundTag tag = chest.getTag();
        if (tag == null || !tag.contains(TAG_SLOTS, Tag.TAG_LIST)) {
            return false;
        }
        ListTag slots = tag.getList(TAG_SLOTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(slots.size(), MAX_SLOTS); i++) {
            CompoundTag entry = slots.getCompound(i);
            if (guardId.equals(entry.hasUUID("Id") ? entry.getUUID("Id") : null)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 胸甲同意与该护卫保活：满套、Bond 一致、名册有槽。
     * 用于护卫侧刷新自身 linkTtl。
     */
    public static boolean chestAgreesLink(Player owner, GuardEntity guard) {
        if (!OfficerUniformArmorItem.hasFullSet(owner)) {
            return false;
        }
        ItemStack chest = getEquippedChest(owner);
        if (chest == null) {
            return false;
        }
        UUID bond = getBondId(chest);
        UUID guardBond = guard.getOfficerBondId();
        if (bond == null || guardBond == null || !bond.equals(guardBond)) {
            return false;
        }
        if (!owner.getUUID().equals(guard.getOwnerUUID())) {
            return false;
        }
        return rosterContains(chest, guard.getUUID());
    }

    /**
     * 服务端：满套玩家穿戴的军官胸甲每 tick 维护名册（刷新/倒计时/清槽/储备回补）。
     * 不含自动召唤。
     */
    public static void tickRoster(Player player, ItemStack chest) {
        if (!(player.level() instanceof ServerLevel level) || !isOfficerChest(chest)) {
            return;
        }
        if (!OfficerUniformArmorItem.hasFullSet(player)) {
            return;
        }
        ensureInitialized(chest);
        CompoundTag tag = chest.getTag();
        ListTag slots = tag.getList(TAG_SLOTS, Tag.TAG_COMPOUND);
        normalizeSlots(slots);

        double recallLimit = RECALL_DIST_IDLE;
        if ((player.getLastHurtMob() != null && player.getLastHurtMob().isAlive())
                || (player.getLastHurtByMob() != null && player.getLastHurtByMob().isAlive())) {
            recallLimit = RECALL_DIST_COMBAT;
        }
        double recallLimitSqr = recallLimit * recallLimit;
        UUID ownerId = player.getUUID();
        UUID bond = tag.getUUID(TAG_BOND_ID);

        boolean dirty = false;
        for (int i = 0; i < MAX_SLOTS; i++) {
            CompoundTag entry = slots.getCompound(i);
            if (!entry.hasUUID("Id")) {
                continue;
            }
            UUID id = entry.getUUID("Id");
            Entity entity = level.getEntity(id);
            if (entity instanceof GuardEntity guard
                    && guard.isAlive()
                    && ownerId.equals(guard.getOwnerUUID())
                    && bond.equals(guard.getOfficerBondId())) {
                entry.putInt("Ttl", LINK_TIMEOUT_TICKS);
                dirty = true;
                if (player.distanceToSqr(guard) > recallLimitSqr) {
                    recallGuard(guard, player);
                    guard.resetOfficerLinkTimeout();
                }
            } else if (entity != null) {
                clearSlotEntry(entry);
                dirty = true;
            } else {
                int ttl = entry.getInt("Ttl") - 1;
                if (ttl <= 0) {
                    clearSlotEntry(entry);
                } else {
                    entry.putInt("Ttl", ttl);
                }
                dirty = true;
            }
        }
        if (dirty) {
            tag.put(TAG_SLOTS, slots);
        }
        tickReserveRefill(tag);
    }

    /** 按 X：空槽且储备 &gt; 0 时召唤一只护卫并入册。 */
    public static boolean trySummonGuard(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return false;
        }
        if (!OfficerUniformArmorItem.hasFullSet(player)) {
            return false;
        }
        ItemStack chest = getEquippedChest(player);
        if (chest == null) {
            return false;
        }
        UUID bond = ensureInitialized(chest);
        CompoundTag tag = chest.getTag();
        int reserve = getReserve(chest);
        int slot = firstEmptySlot(chest);
        if (slot < 0 || reserve <= 0) {
            return false;
        }
        GuardEntity spawned = spawnGuardNear(player, bond);
        if (spawned == null) {
            return false;
        }
        ListTag slots = tag.getList(TAG_SLOTS, Tag.TAG_COMPOUND);
        normalizeSlots(slots);
        CompoundTag entry = slots.getCompound(slot);
        entry.putUUID("Id", spawned.getUUID());
        entry.putInt("Ttl", LINK_TIMEOUT_TICKS);
        tag.put(TAG_SLOTS, slots);
        tag.putInt(TAG_RESERVE, reserve - 1);
        if (reserve - 1 < MAX_RESERVE && tag.getInt(TAG_RESERVE_TTL) <= 0) {
            tag.putInt(TAG_RESERVE_TTL, RESERVE_REFILL_TTL_TICKS);
        }
        return true;
    }

    @Nullable
    public static GuardEntity spawnGuardNear(Player player, UUID bondId) {
        if (!(player.level() instanceof ServerLevel level)) {
            return null;
        }
        RandomSource random = player.getRandom();
        Set<BlockPos> seen = new HashSet<>();
        for (int t = 0; t < SPAWN_ATTEMPTS; t++) {
            double x =
                    player.getX()
                            + (random.nextDouble() - random.nextDouble()) * (double) SPAWN_SCAN_RANGE;
            double z =
                    player.getZ()
                            + (random.nextDouble() - random.nextDouble()) * (double) SPAWN_SCAN_RANGE;
            int y = Mth.floor(player.getY()) + random.nextInt(3) - 1;
            BlockPos key = BlockPos.containing(x, y, z);
            if (!seen.add(key)) {
                continue;
            }
            GuardEntity spawned = tryPlaceGuard(level, player, bondId, x, y, z, false);
            if (spawned != null) {
                return spawned;
            }
        }
        return tryPlaceGuard(level, player, bondId, player.getX(), player.getY(), player.getZ(), true);
    }

    @Nullable
    private static GuardEntity tryPlaceGuard(
            ServerLevel level,
            Player player,
            UUID bondId,
            double x,
            double y,
            double z,
            boolean force) {
        var type = ModEntities.GUARD.get();
        if (!force && !level.noCollision(type.getAABB(x, y, z))) {
            return null;
        }
        GuardEntity guard = type.create(level);
        if (guard == null) {
            return null;
        }
        guard.moveTo(x, y, z, player.getYRot(), 0.0F);
        guard.setTame(true);
        guard.setOwnerUUID(player.getUUID());
        guard.setOfficerBondId(bondId);
        guard.resetOfficerLinkTimeout();
        BlockPos spawnPos = force ? BlockPos.containing(x, y, z) : guard.blockPosition();
        guard.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.MOB_SUMMONED,
                null,
                null);
        if (!level.addFreshEntity(guard)) {
            return null;
        }
        return guard;
    }

    public static void recallGuard(GuardEntity guard, LivingEntity owner) {
        if (!(guard.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos base = owner.blockPosition();
        RandomSource random = guard.getRandom();
        for (int i = 0; i < 10; i++) {
            int dx = Mth.nextInt(random, -3, 3);
            int dz = Mth.nextInt(random, -3, 3);
            int dy = Mth.nextInt(random, -1, 1);
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                continue;
            }
            BlockPos pos = base.offset(dx, dy, dz);
            if (!level.noCollision(guard.getType().getAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))) {
                continue;
            }
            MarshalFactionHelper.enderTeleport(guard, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            return;
        }
        MarshalFactionHelper.enderTeleport(guard, owner.getX(), owner.getY(), owner.getZ());
    }

    private static void tickReserveRefill(CompoundTag tag) {
        int reserve = Mth.clamp(tag.getInt(TAG_RESERVE), 0, MAX_RESERVE);
        if (reserve >= MAX_RESERVE) {
            tag.putInt(TAG_RESERVE_TTL, 0);
            tag.putInt(TAG_RESERVE, MAX_RESERVE);
            return;
        }
        int ttl = tag.getInt(TAG_RESERVE_TTL);
        if (ttl <= 0) {
            ttl = RESERVE_REFILL_TTL_TICKS;
        }
        ttl--;
        if (ttl <= 0) {
            reserve++;
            tag.putInt(TAG_RESERVE, reserve);
            if (reserve < MAX_RESERVE) {
                tag.putInt(TAG_RESERVE_TTL, RESERVE_REFILL_TTL_TICKS);
            } else {
                tag.putInt(TAG_RESERVE_TTL, 0);
            }
        } else {
            tag.putInt(TAG_RESERVE_TTL, ttl);
        }
    }

    private static ListTag emptySlotsList() {
        ListTag slots = new ListTag();
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots.add(new CompoundTag());
        }
        return slots;
    }

    private static void normalizeSlots(ListTag slots) {
        while (slots.size() < MAX_SLOTS) {
            slots.add(new CompoundTag());
        }
        while (slots.size() > MAX_SLOTS) {
            slots.remove(slots.size() - 1);
        }
    }

    private static void clearSlotEntry(CompoundTag entry) {
        entry.remove("Id");
        entry.remove("Ttl");
    }
}
