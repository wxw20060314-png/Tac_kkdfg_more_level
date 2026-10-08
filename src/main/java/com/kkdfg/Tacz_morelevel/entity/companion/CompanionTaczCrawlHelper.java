package com.kkdfg.Tacz_morelevel.entity.companion;

import com.kkdfg.Tacz_morelevel.entity.SoldierTaczCrawlState;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;

/**
 * {@link com.kkdfg.Tacz_morelevel.integration.tacz.SoldierTaczCrawlHelper} 同伴版：
 * 对齐同步旗 / Pose / holder；起身被拒则三态回趴。
 */
public final class CompanionTaczCrawlHelper {

    private CompanionTaczCrawlHelper() {}

    public static boolean isCrawling(AbstractFriendEntity companion) {
        return companion.isTaczCrawlingSynced();
    }

    public static SoldierTaczCrawlState getCrawlState(AbstractFriendEntity companion) {
        return isCrawling(companion) ? SoldierTaczCrawlState.CRAWLING : SoldierTaczCrawlState.STANDING;
    }

    public static void setCrawlIntent(AbstractFriendEntity companion, boolean crawl) {
        if (companion.level().isClientSide()) {
            return;
        }
        companion.syncTaczCrawlingFlag(crawl);
    }

    public static boolean canCompanionUseCrawl(AbstractFriendEntity companion) {
        if (companion.level().isClientSide()) {
            return false;
        }
        if (!SyncConfig.ENABLE_CRAWL.get()) {
            return false;
        }
        return IGun.mainHandHoldGun(companion);
    }

    public static boolean canEnterCrawl(AbstractFriendEntity companion) {
        if (!canCompanionUseCrawl(companion)) {
            return false;
        }
        ItemStack main = companion.getMainHandItem();
        IGun gun = IGun.getIGunOrNull(main);
        return gun != null && gun.isCanCrawl(main);
    }

    public static boolean applyCrawl(AbstractFriendEntity companion, boolean crawling) {
        if (companion.level().isClientSide()) {
            return false;
        }
        if (crawling) {
            if (!canCompanionUseCrawl(companion)) {
                setCrawlIntent(companion, false);
                return false;
            }
            alignCrawl(companion);
            return true;
        }
        return tryAlignStand(companion);
    }

    public static void maintainCrawlFromSync(AbstractFriendEntity companion) {
        if (companion.level().isClientSide()) {
            return;
        }
        if (!isCrawling(companion)) {
            tryAlignStand(companion);
            return;
        }
        if (!canCompanionUseCrawl(companion)) {
            tryAlignStand(companion);
            return;
        }
        companion.setJumping(false);
        ensureGunDrawn(companion);
        alignCrawl(companion);
    }

    public static void applyCrawlPose(AbstractFriendEntity companion) {
        if (isCrawling(companion)) {
            if (companion.getPose() != Pose.SWIMMING) {
                companion.setPose(Pose.SWIMMING);
            }
        } else if (companion.getPose() == Pose.SWIMMING) {
            companion.setPose(Pose.STANDING);
        }
    }

    /** @return 是否成功非趴（Pose 不是 SWIMMING） */
    private static boolean tryAlignStand(AbstractFriendEntity companion) {
        setCrawlIntent(companion, false);
        pushStandToTacz(companion);
        if (companion.getPose() == Pose.SWIMMING) {
            companion.setPose(Pose.STANDING);
        }
        if (companion.getPose() == Pose.SWIMMING) {
            alignCrawl(companion);
            return false;
        }
        return true;
    }

    private static void alignCrawl(AbstractFriendEntity companion) {
        setCrawlIntent(companion, true);
        pushCrawlToTacz(companion);
        if (companion.getPose() != Pose.SWIMMING) {
            companion.setPose(Pose.SWIMMING);
        }
    }

    private static void pushCrawlToTacz(AbstractFriendEntity companion) {
        IGunOperator op = IGunOperator.fromLivingEntity(companion);
        op.crawl(true);
        ShooterDataHolder holder = op.getDataHolder();
        if (holder != null) {
            holder.isCrawling = true;
        }
    }

    private static void pushStandToTacz(AbstractFriendEntity companion) {
        IGunOperator op = IGunOperator.fromLivingEntity(companion);
        op.crawl(false);
        ShooterDataHolder holder = op.getDataHolder();
        if (holder != null) {
            holder.isCrawling = false;
        }
    }

    private static void ensureGunDrawn(AbstractFriendEntity companion) {
        if (!IGun.mainHandHoldGun(companion)) {
            return;
        }
        IGunOperator op = IGunOperator.fromLivingEntity(companion);
        ShooterDataHolder holder = op.getDataHolder();
        if (holder == null || holder.currentGunItem != null) {
            return;
        }
        op.draw(companion::getMainHandItem);
    }
}
