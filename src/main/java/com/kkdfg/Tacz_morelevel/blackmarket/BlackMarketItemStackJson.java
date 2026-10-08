package com.kkdfg.Tacz_morelevel.blackmarket;

import com.google.gson.annotations.SerializedName;
import com.kkdfg.Tacz_morelevel.integration.tacz.TaczGunMobHelper;
import com.tacz.guns.api.item.IGun;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * JSON 物品描述。普通物品用 {@code item}；TaCZ 枪械用 {@code gun}（如 {@code "tacz:ak47"}），
 * 会生成带 NBT 的 {@code tacz:modern_kinetic_gun}。
 */
public final class BlackMarketItemStackJson {

    @SerializedName("item")
    private String itemId;

    /** TaCZ 枪械数据 id，例如 {@code tacz:ak47}、{@code tacz:m4a1}。 */
    @SerializedName("gun")
    private String gunId;

    @SerializedName("count")
    private int count = 1;

    /** 可选 NBT，例如 TaCZ 全类型创造弹药盒 {@code {"AllTypeCreative": true}}。 */
    @SerializedName("nbt")
    private Map<String, Object> nbt;

    public ItemStack toStack() {
        if (this.gunId != null && !this.gunId.isBlank()) {
            ResourceLocation gunDataId = ResourceLocation.tryParse(this.gunId);
            if (gunDataId != null) {
                ItemStack gunStack = TaczGunMobHelper.createPlayerGunStack(gunDataId);
                if (!gunStack.isEmpty()) {
                    return gunStack;
                }
            }
        }
        if (this.itemId == null || this.itemId.isBlank()) {
            return ItemStack.EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(this.itemId);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, Math.max(1, this.count));
        applyNbt(stack);
        return stack;
    }

    private void applyNbt(ItemStack stack) {
        if (this.nbt == null || this.nbt.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        for (Map.Entry<String, Object> entry : this.nbt.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Boolean bool) {
                tag.putBoolean(entry.getKey(), bool);
            } else if (value instanceof Number number) {
                tag.putInt(entry.getKey(), number.intValue());
            } else if (value instanceof String string) {
                tag.putString(entry.getKey(), string);
            }
        }
    }

    /** 按栈内 GunId 重新生成 TaCZ 枪（API 晚于数据包加载时补救空栈/半成品栈）。 */
    public static ItemStack rematerializeGunStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null) {
            return stack;
        }
        ResourceLocation gunId = gun.getGunId(stack);
        if (gunId == null) {
            return stack;
        }
        ItemStack fresh = TaczGunMobHelper.createPlayerGunStack(gunId);
        return fresh.isEmpty() ? stack : fresh;
    }
}
