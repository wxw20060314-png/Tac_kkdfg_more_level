package com.kkdfg.Tacz_morelevel.bounty;

import com.kkdfg.Tacz_morelevel.effect.ModMobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public final class BountyWantedHelper {

    private static final int INFINITE_TICKS = 36000;

    private BountyWantedHelper() {}

    /** @return 0 表示无悬赏；1 起为等级。 */
    public static int getLevel(ServerPlayer player) {
        MobEffectInstance instance = player.getEffect(ModMobEffects.BOUNTY_WANTED.get());
        if (instance == null) {
            return 0;
        }
        return instance.getAmplifier() + 1;
    }

    public static void setLevel(ServerPlayer player, int level) {
        if (level <= 0) {
            player.removeEffect(ModMobEffects.BOUNTY_WANTED.get());
            return;
        }
        player.addEffect(
                new MobEffectInstance(
                        ModMobEffects.BOUNTY_WANTED.get(),
                        INFINITE_TICKS,
                        level - 1,
                        false,
                        false,
                        false));
    }

    public static int incrementLevel(ServerPlayer player) {
        int next = getLevel(player) + 1;
        setLevel(player, next);
        return next;
    }
}
