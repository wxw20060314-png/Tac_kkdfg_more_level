package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.entity.ModEntities;
import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.function.Consumer;

import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
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
 * 护航联络器：右键方块召唤对应档位护航并绑定玩家为主人。
 * <p>
 * 档位 0 美女 / 1 标准 / 2 高级 / 3 魔王；客户端资源名见 {@link #tierAssetName(int)}。
 */
public class BodyguardContactItem extends Item implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final int tier;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BodyguardContactItem(Properties properties, int tier) {
        super(properties.stacksTo(16));
        this.tier = clampK(tier);
    }

    public static int clampK(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 3) {
            return 3;
        }
        return value;
    }

    public int getTier() {
        return this.tier;
    }

    /** 与 {@code geo/item/}、{@code textures/item/}、{@code animations/item/} 下文件名一致（无后缀）。 */
    public static String tierAssetName(int tier) {
        return switch (clampK(tier)) {
            case 0 -> "beauty_bodyguard_contact";
            case 1 -> "standard_bodyguard_contact";
            case 2 -> "elite_bodyguard_contact";
            default -> "demon_lord_bodyguard_contact";
        };
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerBodyguardContact(consumer);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "idle", 0, state -> {
                    state.setAnimation(IDLE);
                    return PlayState.CONTINUE;
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        ServerLevel server = (ServerLevel) level;
        ItemStack stack = context.getItemInHand();
        AbstractFriendlyCompanionEntity companion = createBodyguard(server, this.tier);
        if (companion == null) {
            return InteractionResult.FAIL;
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        companion.moveTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                player.getYRot(),
                0.0F);
        if (!server.noCollision(companion)) {
            companion.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
            if (!server.noCollision(companion)) {
                return InteractionResult.FAIL;
            }
        }

        companion.finalizeSpawn(
                server,
                server.getCurrentDifficultyAt(companion.blockPosition()),
                MobSpawnType.SPAWN_EGG,
                null,
                null);
        companion.setTame(true);
        companion.setOwnerUUID(player.getUUID());
        server.addFreshEntity(companion);
        companion.level().broadcastEntityEvent(companion, (byte) 7);
        companion.onTameSuccess(player);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        server.playSound(
            null,
            spawnPos,
            ModSounds.SOUND_BODYGUARD_STORAGE_BIU.get(),
            SoundSource.PLAYERS,
            1.0F,
            1.0F);
        return InteractionResult.CONSUME;
    }

    @Nullable
    private static AbstractFriendlyCompanionEntity createBodyguard(ServerLevel level, int tier) {
        int index = clampK(tier);
        return switch (index) {
            case 0 -> ModEntities.BEAUTY_BODYGUARD.get().create(level);
            case 1 -> ModEntities.STANDARD_BODYGUARD.get().create(level);
            case 2 -> ModEntities.ELITE_BODYGUARD.get().create(level);
            default -> ModEntities.DEMON_LORD_BODYGUARD.get().create(level);
        };
    }
}
