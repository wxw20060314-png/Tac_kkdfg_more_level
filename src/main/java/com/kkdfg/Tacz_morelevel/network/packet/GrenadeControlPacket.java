package com.kkdfg.Tacz_morelevel.network.packet;

import com.kkdfg.Tacz_morelevel.entity.custom.FlashbangEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.GrenadeEntity;
import com.kkdfg.Tacz_morelevel.item.custom.FlashbangItem;
import com.kkdfg.Tacz_morelevel.item.custom.GrenadeItem;
import com.kkdfg.Tacz_morelevel.sound.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class GrenadeControlPacket {
    private static final String TAG_PRIMED = "GrenadePrimed";
    private static final String TAG_PRIME_TIME = "GrenadePrimeGameTime";
    private final Action action;

    public GrenadeControlPacket(Action action) {
        this.action = action;
    }

    public static void encode(GrenadeControlPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
    }

    public static GrenadeControlPacket decode(FriendlyByteBuf buf) {
        return new GrenadeControlPacket(buf.readEnum(Action.class));
    }

    public static void handle(GrenadeControlPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(
                () -> {
                    ServerPlayer sender = ctx.getSender();
                    if (sender != null) {
                        handleServer(sender, msg.action);
                    }
                });
        ctx.setPacketHandled(true);
    }

    public static void handleServer(ServerPlayer player, Action action) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        switch (action) {
            case START -> {
                if (findThrowableHand(player) == null) {
                    return;
                }
                if (!data.getBoolean(TAG_PRIMED)) {
                    data.putBoolean(TAG_PRIMED, true);
                    data.putLong(TAG_PRIME_TIME, level.getGameTime());
                    level.playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            ModSounds.SOUND_GRENADE_PIN.get(),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);
                }
            }
            case RELEASE -> {
                InteractionHand throwHand = findThrowableHand(player);
                if (!data.getBoolean(TAG_PRIMED) || throwHand == null) {
                    return;
                }
                long started = data.getLong(TAG_PRIME_TIME);
                int held = (int) Math.max(0L, level.getGameTime() - started);
                ItemStack sourceStack = player.getItemInHand(throwHand);
                boolean flashbang = sourceStack.getItem() instanceof FlashbangItem;
                int maxFuse = flashbang ? FlashbangEntity.MAX_FUSE_TICKS : GrenadeItem.MAX_FUSE_TICKS;
                int clampedHeld = Math.min(maxFuse, held);
                int remainingFuse = Math.max(1, maxFuse - clampedHeld);
                float speed = 1.0F;

                GrenadeEntity projectile =
                        flashbang
                                ? new FlashbangEntity(level, player)
                                : new GrenadeEntity(level, player);
                projectile.setFuseTicks(remainingFuse);
                projectile.setItem(new ItemStack(sourceStack.getItem()));
                projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, speed, 1.0F);
                level.addFreshEntity(projectile);
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ModSounds.SOUND_GRENADE_THROW.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);
                player.swing(throwHand, true);

                if (!player.getAbilities().instabuild) {
                    sourceStack.shrink(1);
                }
                clearPrimed(data);
            }
            case CANCEL -> clearPrimed(data);
        }
    }

    public static boolean isPrimed(ServerPlayer player) {
        return player.getPersistentData().getBoolean(TAG_PRIMED);
    }

    public static long getPrimeStart(ServerPlayer player) {
        return player.getPersistentData().getLong(TAG_PRIME_TIME);
    }

    public static void clearPrimed(ServerPlayer player) {
        clearPrimed(player.getPersistentData());
    }

    private static void clearPrimed(CompoundTag data) {
        data.putBoolean(TAG_PRIMED, false);
        data.remove(TAG_PRIME_TIME);
    }

    /** 主/副手是否握可捏雷物品（破片手雷或闪光弹）。 */
    public static InteractionHand findThrowableHand(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof GrenadeItem) {
                return hand;
            }
        }
        return null;
    }

    public enum Action {
        START,
        RELEASE,
        CANCEL
    }
}
