package com.kkdfg.Tacz_morelevel.entity.ai.riding;

import java.lang.reflect.Field;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/** NPC 接管期间占住马的移动控制器，避免原版 WAIT 清掉 NPC 写入的输入。 */
public final class PausedHorseMoveControl extends MoveControl {

    private static final Field MOVE_CONTROL_FIELD =
            ObfuscationReflectionHelper.findField(Mob.class, "f_21342_");

    private PausedHorseMoveControl(AbstractHorse horse) {
        super(horse);
    }

    public static PausedHorseMoveControl pause(AbstractHorse horse) {
        PausedHorseMoveControl control = new PausedHorseMoveControl(horse);
        try {
            MOVE_CONTROL_FIELD.set(horse, control);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot pause horse move control", exception);
        }
        return control;
    }

    public boolean isInstalledOn(AbstractHorse horse) {
        try {
            return this.mob == horse && MOVE_CONTROL_FIELD.get(horse) == this;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot inspect horse move control", exception);
        }
    }

    @Override
    public void tick() {
        // 移动输入由骑手的 RidingMoveControl 写入，留给马下一拍的 travel 使用。
    }

    /** 只恢复本次接管安装的对象，旧 Goal 结束时不会覆盖后来的接管。 */
    public void restore() {
        try {
            if (MOVE_CONTROL_FIELD.get(this.mob) != this) {
                return;
            }
            MOVE_CONTROL_FIELD.set(this.mob, new MoveControl(this.mob));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot restore horse move control", exception);
        }
        this.mob.setSpeed(0.0F);
        this.mob.setXxa(0.0F);
        this.mob.goalSelector.setControlFlag(Goal.Flag.MOVE, true);
    }

    /** 供无乘客时的定期检查恢复遗留的暂停控制器。 */
    public static void restoreIfPaused(AbstractHorse horse) {
        try {
            if (MOVE_CONTROL_FIELD.get(horse) instanceof PausedHorseMoveControl control) {
                control.restore();
            }
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot inspect horse move control", exception);
        }
    }
}
