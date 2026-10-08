package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.entity.custom.FlashbangEntity;
import com.kkdfg.Tacz_morelevel.entity.custom.GrenadeEntity;
import com.kkdfg.Tacz_morelevel.item.custom.FlashbangItem;
import com.kkdfg.Tacz_morelevel.item.custom.GrenadeItem;
import com.kkdfg.Tacz_morelevel.network.ModNetwork;
import com.kkdfg.Tacz_morelevel.network.packet.GrenadeControlPacket;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GrenadeControlEvents {
    private static boolean prevLeftDown = false;
    private static boolean clientPriming = false;
    /** 本机正在捏的物品（仅 {@link ClientInput} 写入）。 */
    @Nullable
    private static Item clientPrimedItem;
    /** 客户端捏雷开始时的 {@code level.getGameTime()}，供 HUD 引信条使用。 */
    private static long clientPrimeStartGameTime = -1L;

    private GrenadeControlEvents() {}

    public static boolean isClientPriming() {
        return clientPriming;
    }

    public static long getClientPrimeStartGameTime() {
        return clientPrimeStartGameTime;
    }

    @Mod.EventBusSubscriber(
            modid = Tacz_MoreLevel.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT)
    public static final class ClientInput {
        private ClientInput() {}

        @SubscribeEvent
        public static void onAttackKeyTriggered(InputEvent.InteractionKeyMappingTriggered event) {
            if (!event.isAttack()) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || !clientPriming) {
                return;
            }
            // 捏雷期间，左键对方块/实体不触发挥手动作
            event.setSwingHand(false);
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                prevLeftDown = false;
                clearClientPrime();
                return;
            }
            boolean leftDown = mc.options.keyAttack.isDown();
            Item heldPrimable = heldPrimableItem(mc.player);
            boolean holdingGrenade = heldPrimable != null;

            if (holdingGrenade && leftDown && !prevLeftDown) {
                ModNetwork.CHANNEL.sendToServer(new GrenadeControlPacket(GrenadeControlPacket.Action.START));
                setClientPrime(heldPrimable, mc.level.getGameTime());
            }
            if (holdingGrenade && !leftDown && prevLeftDown) {
                ModNetwork.CHANNEL.sendToServer(new GrenadeControlPacket(GrenadeControlPacket.Action.RELEASE));
                clearClientPrime();
            }
            if (!holdingGrenade && prevLeftDown) {
                ModNetwork.CHANNEL.sendToServer(new GrenadeControlPacket(GrenadeControlPacket.Action.CANCEL));
                clearClientPrime();
            }
            if (holdingGrenade && leftDown) {
                long start =
                        clientPrimeStartGameTime >= 0L
                                ? clientPrimeStartGameTime
                                : mc.level.getGameTime();
                setClientPrime(heldPrimable, start);
            } else {
                clearClientPrime();
            }
            prevLeftDown = leftDown;
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (victim.getPersistentData().getBoolean(GrenadeEntity.TAG_SKIP_EXPLOSION_SELF_DAMAGE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player p = event.player;
        Level level = p.level();
        if (level.isClientSide || !(p instanceof ServerPlayer sp)) {
            return;
        }
        if (!GrenadeControlPacket.isPrimed(sp)) {
            return;
        }
        InteractionHand hand = GrenadeControlPacket.findThrowableHand(sp);
        if (hand == null) {
            GrenadeControlPacket.clearPrimed(sp);
            return;
        }
        ItemStack held = sp.getItemInHand(hand);
        boolean flashbang = held.getItem() instanceof FlashbangItem;
        int maxFuse = flashbang ? FlashbangEntity.MAX_FUSE_TICKS : GrenadeItem.MAX_FUSE_TICKS;
        long elapsed = level.getGameTime() - GrenadeControlPacket.getPrimeStart(sp);
        if (elapsed < maxFuse) {
            return;
        }
        ServerLevel sl = (ServerLevel) level;
        if (flashbang) {
            FlashbangEntity.flashAt(sl, new Vec3(sp.getX(), sp.getY(0.0625D), sp.getZ()));
        } else {
            DamageSource blast = sp.damageSources().explosion(sp, sp);
            sp.getPersistentData().putBoolean(GrenadeEntity.TAG_SKIP_EXPLOSION_SELF_DAMAGE, true);
            try {
                sl.explode(
                        sp,
                        blast,
                        GrenadeEntity.grenadeExplosionDamageCalculator(null),
                        sp.getX(),
                        sp.getY(0.0625D),
                        sp.getZ(),
                        6.0F,
                        false,
                        Level.ExplosionInteraction.MOB);
            } finally {
                sp.getPersistentData().remove(GrenadeEntity.TAG_SKIP_EXPLOSION_SELF_DAMAGE);
            }
        }
        if (!sp.getAbilities().instabuild) {
            held.shrink(1);
        }
        GrenadeControlPacket.clearPrimed(sp);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player p = event.getEntity();
        if (p.level().isClientSide) {
            if (clientPriming && holdingPrimableClient(p)) {
                event.setCanceled(true);
            }
            return;
        }
        if (p instanceof ServerPlayer sp && GrenadeControlPacket.isPrimed(sp)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp && GrenadeControlPacket.isPrimed(sp)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer sp && GrenadeControlPacket.isPrimed(sp)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer sp && GrenadeControlPacket.isPrimed(sp)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer sp) || !GrenadeControlPacket.isPrimed(sp)) {
            return;
        }
        ItemStack stack = event.getItemStack();
        // 保留“右键取消”入口：手里是可捏雷时允许 use() 执行，其他一律禁用
        if (!(stack.getItem() instanceof GrenadeItem)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player p = event.getEntity();
        if (p.level().isClientSide) {
            if (clientPriming && holdingPrimableClient(p)) {
                event.setNewSpeed(0.0F);
            }
            return;
        }
        if (p instanceof ServerPlayer sp && GrenadeControlPacket.isPrimed(sp)) {
            event.setNewSpeed(0.0F);
        }
    }

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer sp && GrenadeControlPacket.isPrimed(sp)) {
            event.setCanceled(true);
        }
    }

    public static boolean isClientPrimingForItem(Item item) {
        return clientPriming && item != null && item == clientPrimedItem;
    }

    private static boolean holdingPrimableClient(Player player) {
        return heldPrimableItem(player) != null;
    }

    @Nullable
    private static Item heldPrimableItem(Player player) {
        Item main = player.getMainHandItem().getItem();
        if (main instanceof GrenadeItem) {
            return main;
        }
        Item off = player.getOffhandItem().getItem();
        return off instanceof GrenadeItem ? off : null;
    }

    private static void setClientPrime(@Nullable Item item, long startGameTime) {
        clientPriming = true;
        clientPrimedItem = item;
        clientPrimeStartGameTime = startGameTime;
    }

    private static void clearClientPrime() {
        clientPriming = false;
        clientPrimedItem = null;
        clientPrimeStartGameTime = -1L;
    }
}
