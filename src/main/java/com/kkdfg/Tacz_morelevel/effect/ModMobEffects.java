package com.kkdfg.Tacz_morelevel.effect;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Tacz_MoreLevel.MOD_ID);

    /** 悬赏通缉：amplifier 0 = 1 级，2 = 3 级。 */
    public static final RegistryObject<MobEffect> BOUNTY_WANTED =
            MOB_EFFECTS.register(
                    "bounty_wanted",
                    () -> new BountyWantedEffect());

    /**
     * 自定义减伤：显示等级 x = amplifier + 1，承伤为原伤害 / (x + 1)；
     * {@link net.minecraftforge.event.entity.living.LivingHurtEvent} {@code NORMAL}，先于防弹（{@code LOW}）。
     */
    public static final RegistryObject<MobEffect> MOD_DEFFENCE =
            MOB_EFFECTS.register("mod_deffence", ModDeffenceEffect::new);

    /** 冲击波冲刺冲击态：不可见；撞块爆炸。 */
    public static final RegistryObject<MobEffect> SHOCKWAVE_IMPACT =
            MOB_EFFECTS.register("shockwave_impact", ShockwaveImpactEffect::new);

    /** 硬化：减伤 (x+1)*20%，减速 (x+1)*10%。 */
    public static final RegistryObject<MobEffect> HARDENING =
            MOB_EFFECTS.register("hardening", HardeningEffect::new);

    /** 综合免疫：每 20 tick 清除等级 ≤ amplifier 的常见 debuff。 */
    public static final RegistryObject<MobEffect> COMPREHENSIVE_IMMUNITY =
            MOB_EFFECTS.register("comprehensive_immunity", ComprehensiveImmunityEffect::new);

    /** 辐射：每 10 tick 损失当前生命值的 i%（i = amplifier + 1）。 */
    public static final RegistryObject<MobEffect> RADIATION =
            MOB_EFFECTS.register("radiation", RadiationEffect::new);

    /** 闪光：无图标；玩家白屏；NPC 由 FlashedStunGoal 占通道。 */
    public static final RegistryObject<MobEffect> FLASHED =
            MOB_EFFECTS.register("flashed", FlashedEffect::new);

    /** 燃烧瓶燃烧：每 20 tick 5 点；无视护甲与保护附魔。 */
    public static final RegistryObject<MobEffect> MOLOTOV_BURN =
            MOB_EFFECTS.register("molotov_burn", MolotovBurnEffect::new);

    private ModMobEffects() {}

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
