package mei.arisuwu.deermod.entity.deer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import java.util.EnumSet;

public class MoveToPositionGoal extends Goal {
    private final DeerEntity deer;
    private final double speed;
    private final PathNavigation navigation;
    private int stuckTimer;
    private BlockPos lastPos;

    public MoveToPositionGoal(DeerEntity deer, double speed) {
        this.deer = deer;
        this.speed = speed;
        this.navigation = deer.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return deer.getState() == DeerState.MOVING && deer.getCurrentTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return deer.getState() == DeerState.MOVING && deer.getCurrentTarget() != null && !navigation.isDone();
    }

    @Override
    public void start() {
        BlockPos target = deer.getCurrentTarget();
        if (target != null) {
            navigation.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed);
            stuckTimer = 0;
            lastPos = deer.blockPosition();
        }
    }

    @Override
    public void tick() {
        BlockPos target = deer.getCurrentTarget();
        if (target == null) return;

        double distanceSquared = deer.blockPosition().distSqr(target);
        if (distanceSquared <= 4.0) {
            deer.setState(DeerState.ARRIVED);
            navigation.stop();
            return;
        }

        BlockPos currentPos = deer.blockPosition();
        if (currentPos.equals(lastPos)) {
            stuckTimer++;
        } else {
            stuckTimer = 0;
            lastPos = currentPos;
        }

        if (stuckTimer > 40) {
            if (!deer.selectNextReachableTarget()) {
                deer.setState(DeerState.IDLE);
            } else {
                start();
            }
        }

        if (navigation.isDone() && deer.getState() == DeerState.MOVING) {
            navigation.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed);
        }
    }

    @Override
    public void stop() {
        navigation.stop();
        stuckTimer = 0;
    }
}
