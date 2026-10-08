package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.armor.skill.officer.OfficerGuardRosterHelper;
import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 军官制服套（帽/军衣/裤/鞋）：数值暂同四级防弹；穿戴 Gecko 见 {@code geo/armor/officer_uniform.geo.json}。
 * 展示名为传说档橙色（语言文件 {@code §6§o}），灰斜体注释「堕落元帅的遗物」。
 * <p>传说技能见 {@link com.kkdfg.Tacz_morelevel.armor.skill.officer.OfficerUniformLegendarySkills}。
 */
public class OfficerUniformArmorItem extends BulletproofArmor implements GeoItem {

    public static final int FULL_SET_PIECES = 4;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public OfficerUniformArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.FOURTH_BULLETPROOF, type, properties);
        this.setLevel(4);
    }

    public static int countEquippedPieces(LivingEntity entity) {
        int n = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof OfficerUniformArmorItem) {
                n++;
            }
        }
        return n;
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return countEquippedPieces(entity) >= FULL_SET_PIECES;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerOfficerUniformArmor(consumer);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.officer_uniform.lore")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.tacz_morelevel.officer_uniform.legendary"));
        if (this.getType() == ArmorItem.Type.CHESTPLATE && stack.hasTag()) {
            int reserve = OfficerGuardRosterHelper.getReserve(stack);
            int linked = OfficerGuardRosterHelper.countLinkedSlots(stack);
            tooltip.add(
                    Component.translatable(
                                    "tooltip.tacz_morelevel.officer_uniform.roster",
                                    linked,
                                    OfficerGuardRosterHelper.MAX_SLOTS,
                                    reserve,
                                    OfficerGuardRosterHelper.MAX_RESERVE)
                            .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "idle", 10, state -> {
                    state.setAnimation(IDLE);
                    return PlayState.CONTINUE;
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
