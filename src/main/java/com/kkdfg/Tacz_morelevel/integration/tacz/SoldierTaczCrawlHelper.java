package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.kkdfg.Tacz_morelevel.api.TaczCombatCrawlMob;
import com.kkdfg.Tacz_morelevel.entity.SoldierTaczCrawlState;
import net.minecraft.world.entity.LivingEntity;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;

/**
 * 士兵 TaCZ 趴下：以同步字段为意图，在 TaCZ {@code tickCrawling()} 之后
 * {@link #maintainCrawlFromSync} 对齐三态：同步旗 / {@link Pose} / holder.isCrawling。
 * 起身若被碰撞降级仍为 {@link Pose#SWIMMING}，则三态全部保持为趴。
 */
public final class SoldierTaczCrawlHelper {

    private SoldierTaczCrawlHelper() {}

    /** 双端：是否应处于趴下（读同步意图，不读会被 TaCZ 每 tick 清掉的 holder）。 */
    public static boolean isCrawling(TaczCombatCrawlMob soldier) {
        return soldier.isTaczCrawlingSynced();
    }

    public static SoldierTaczCrawlState getCrawlState(TaczCombatCrawlMob soldier) {
        return isCrawling(soldier) ? SoldierTaczCrawlState.CRAWLING : SoldierTaczCrawlState.STANDING;
    }

    /** 仅设置趴下意图（供 AI Goal 调用）；实际 holder / 姿势在 tick 末尾 maintain。 */
    public static void setCrawlIntent(TaczCombatCrawlMob soldier, boolean crawl) {
        if (!(soldier instanceof LivingEntity living) || living.level().isClientSide()) {
            return;
        }
        soldier.syncTaczCrawlingFlag(crawl);
    }

    /**
     * 服务端：士兵是否允许趴下（仅 {@code EnableCrawl}）。
     * 路径洞进趴不要求主手枪；战斗趴另走 {@link #canEnterCrawl}。
     */
    public static boolean canSoldierUseCrawl(LivingEntity soldier) {
        if (soldier.level().isClientSide()) {
            return false;
        }
        return SyncConfig.ENABLE_CRAWL.get();
    }

    /**
     * 战斗趴进入条件：{@link #canSoldierUseCrawl} + 主手 TaCZ 枪且 {@link IGun#isCanCrawl(ItemStack)}。
     */
    public static boolean canEnterCrawl(LivingEntity soldier) {
        if (!canSoldierUseCrawl(soldier)) {
            return false;
        }
        ItemStack main = soldier.getMainHandItem();
        IGun gun = IGun.getIGunOrNull(main);
        return gun != null && gun.isCanCrawl(main);
    }

    /**
     * 服务端：设置意图并立即尝试进入/退出趴下（start/stop、命令等一次性调用）。
     * 起身失败（仍为 {@link Pose#SWIMMING}）时三态回趴。
     */
    public static boolean applyCrawl(TaczCombatCrawlMob soldier, boolean crawling) {
        if (!(soldier instanceof LivingEntity living) || living.level().isClientSide()) {
            return false;
        }
        if (crawling) {
            if (!canSoldierUseCrawl(living)) {
                setCrawlIntent(soldier, false);
                return false;
            }
            alignCrawl(soldier, living);
            return true;
        }
        return tryAlignStand(soldier, living);
    }

    /**
     * 服务端：在 {@link net.minecraft.world.entity.LivingEntity#tick()} 末尾（TaCZ mixin 已跑完）调用，
     * 按同步意图对齐三态；起身被拒则强制回趴。
     */
    public static void maintainCrawlFromSync(TaczCombatCrawlMob soldier) {
        if (!(soldier instanceof LivingEntity living)) {
            return;
        }
        if (living.level().isClientSide()) {
            return;
        }
        if (!isCrawling(soldier)) {
            tryAlignStand(soldier, living);
            return;
        }
        if (!canSoldierUseCrawl(living)) {
            tryAlignStand(soldier, living);
            return;
        }
        living.setJumping(false);
        ensureGunDrawn(living);
        alignCrawl(soldier, living);
    }

    /**
     * 双端：按同步意图设 Pose。服务端完整三态对齐见 {@link #maintainCrawlFromSync} /
     * {@link #tryAlignStand}。
     */
    public static void applyCrawlPose(LivingEntity soldier) {
        if (!(soldier instanceof TaczCombatCrawlMob crawlMob)) {
            return;
        }
        if (isCrawling(crawlMob)) {
            if (soldier.getPose() != Pose.SWIMMING) {
                soldier.setPose(Pose.SWIMMING);
            }
        } else if (soldier.getPose() == Pose.SWIMMING) {
            soldier.setPose(Pose.STANDING);
        }
    }

    /**
     * 尝试起立：意图 false + holder 站 + Pose 站/蹲。
     * 若碰撞仍为 {@link Pose#SWIMMING}，三态回趴并返回 {@code false}。
     *
     * @return 是否成功处于非趴（Pose 不是 SWIMMING）
     */
    private static boolean tryAlignStand(TaczCombatCrawlMob soldier, LivingEntity living) {
        setCrawlIntent(soldier, false);
        pushStandToTacz(living);
        if (living.getPose() == Pose.SWIMMING) {
            living.setPose(Pose.STANDING);
        }
        if (living.getPose() == Pose.SWIMMING) {
            alignCrawl(soldier, living);
            return false;
        }
        return true;
    }

    /** 三态强制为趴：同步旗 + holder + {@link Pose#SWIMMING}。 */
    private static void alignCrawl(TaczCombatCrawlMob soldier, LivingEntity living) {
        setCrawlIntent(soldier, true);
        pushCrawlToTacz(living);
        if (living.getPose() != Pose.SWIMMING) {
            living.setPose(Pose.SWIMMING);
        }
    }

    private static void pushCrawlToTacz(LivingEntity soldier) {
        IGunOperator op = IGunOperator.fromLivingEntity(soldier);
        op.crawl(true);
        ShooterDataHolder holder = op.getDataHolder();
        if (holder != null) {
            holder.isCrawling = true;
        }
    }

    private static void pushStandToTacz(LivingEntity soldier) {
        IGunOperator op = IGunOperator.fromLivingEntity(soldier);
        op.crawl(false);
        ShooterDataHolder holder = op.getDataHolder();
        if (holder != null) {
            holder.isCrawling = false;
        }
    }

    private static void ensureGunDrawn(LivingEntity soldier) {
        if (!IGun.mainHandHoldGun(soldier)) {
            return;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(soldier);
        ShooterDataHolder holder = op.getDataHolder();
        if (holder == null || holder.currentGunItem != null) {
            return;
        }
        op.draw(soldier::getMainHandItem);
    }
}
