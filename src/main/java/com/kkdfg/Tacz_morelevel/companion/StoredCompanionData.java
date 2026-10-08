package com.kkdfg.Tacz_morelevel.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** 护航快照：实体类型 + {@link Entity#saveWithoutId} 数据，可写入物品 NBT。 */
public final class StoredCompanionData {

    public static final String STACK_TAG = "StoredCompanion";

    private static final String TAG_ENTITY_TYPE = "EntityType";
    private static final String TAG_ENTITY_DATA = "EntityData";
    private static final String TAG_DISPLAY_NAME = "DisplayName";

    @Nullable
    private ResourceLocation entityTypeId;

    @Nullable
    private CompoundTag entityData;

    @Nullable
    private String displayName;

    public boolean isEmpty() {
        return this.entityTypeId == null || this.entityData == null || this.entityData.isEmpty();
    }

    @Nullable
    public String displayName() {
        return this.displayName;
    }

    public static StoredCompanionData fromStack(ItemStack stack) {
        StoredCompanionData data = new StoredCompanionData();
        if (stack.hasTag() && stack.getTag().contains(STACK_TAG, Tag.TAG_COMPOUND)) {
            data.load(stack.getTag().getCompound(STACK_TAG));
        }
        return data;
    }

    public static StoredCompanionData fromCompanion(AbstractFriendlyCompanionEntity companion) {
        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(companion.getType());
        if (typeId == null) {
            return new StoredCompanionData();
        }
        CompoundTag data = new CompoundTag();
        companion.saveWithoutId(data);
        return new StoredCompanionData(typeId, data, companion.getName().getString());
    }

    public void writeToStack(ItemStack stack) {
        stack.getOrCreateTag().put(STACK_TAG, this.save());
    }

    public static void clearStack(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove(STACK_TAG);
            if (stack.getTag().isEmpty()) {
                stack.setTag(null);
            }
        }
    }

    private StoredCompanionData() {}

    private StoredCompanionData(
            @Nullable ResourceLocation entityTypeId,
            @Nullable CompoundTag entityData,
            @Nullable String displayName) {
        this.entityTypeId = entityTypeId;
        this.entityData = entityData;
        this.displayName = displayName;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!this.isEmpty()) {
            tag.putString(TAG_ENTITY_TYPE, this.entityTypeId.toString());
            tag.put(TAG_ENTITY_DATA, this.entityData.copy());
            if (this.displayName != null) {
                tag.putString(TAG_DISPLAY_NAME, this.displayName);
            }
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        this.entityTypeId = null;
        this.entityData = null;
        this.displayName = null;
        if (tag == null || tag.isEmpty()) {
            return;
        }
        if (tag.contains(TAG_ENTITY_TYPE, Tag.TAG_STRING)) {
            this.entityTypeId = ResourceLocation.tryParse(tag.getString(TAG_ENTITY_TYPE));
        }
        if (tag.contains(TAG_ENTITY_DATA, Tag.TAG_COMPOUND)) {
            this.entityData = tag.getCompound(TAG_ENTITY_DATA).copy();
        }
        if (tag.contains(TAG_DISPLAY_NAME, Tag.TAG_STRING)) {
            this.displayName = tag.getString(TAG_DISPLAY_NAME);
        }
        if (this.entityTypeId == null || this.entityData == null || this.entityData.isEmpty()) {
            this.clear();
        }
    }

    public void clear() {
        this.entityTypeId = null;
        this.entityData = null;
        this.displayName = null;
    }

    @Nullable
    public AbstractFriendlyCompanionEntity spawn(ServerLevel level, BlockPos spawnPos, float yaw) {
        if (this.isEmpty()) {
            return null;
        }
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(this.entityTypeId);
        if (entityType == null) {
            return null;
        }
        Entity created = entityType.create(level);
        if (!(created instanceof AbstractFriendlyCompanionEntity companion)) {
            return null;
        }
        // finalizeSpawn 会为新生成护航随机皮肤；存储放出须在之后 load，才能恢复存入时的 SkinIdx。
        companion.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.SPAWN_EGG,
                null,
                null);
        companion.load(this.entityData);
        // load 可能恢复存档里的 LeftHanded；TaCZ 第三人称左主手不画枪，与 finalizeSpawn 一致强制右撇子
        companion.setLeftHanded(false);
        companion.moveTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                yaw,
                0.0F);
        if (!level.noCollision(companion)) {
            return null;
        }
        if (!level.addFreshEntity(companion)) {
            return null;
        }
        companion.reassessWeaponGoal();
        companion.level().broadcastEntityEvent(companion, (byte) 7);
        return companion;
    }
}
