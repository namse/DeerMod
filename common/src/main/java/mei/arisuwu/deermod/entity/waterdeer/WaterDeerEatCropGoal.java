package mei.arisuwu.deermod.entity.waterdeer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.EnumSet;

public class WaterDeerEatCropGoal extends Goal
{
    private static final int SEARCH_RANGE = 8;
    private static final int EAT_TIME = 60;
    private static final float GROWTH_DECREASE = 0.15f;

    private final WaterDeerEntity waterDeer;
    private BlockPos targetCrop;
    private int eatingTimer;

    public WaterDeerEatCropGoal(WaterDeerEntity waterDeer)
    {
        this.waterDeer = waterDeer;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse()
    {
        if (!waterDeer.isHomeChunkSet()) return false;
        targetCrop = findCropInHomeChunk();
        return targetCrop != null;
    }

    @Override
    public boolean canContinueToUse()
    {
        if (targetCrop == null) return false;
        Level world = waterDeer.level();
        BlockState state = world.getBlockState(targetCrop);
        if (!(state.getBlock() instanceof CropBlock)) return false;
        double distance = waterDeer.distanceToSqr(targetCrop.getX() + 0.5, targetCrop.getY(), targetCrop.getZ() + 0.5);
        return distance < 4.0;
    }

    @Override
    public void start()
    {
        eatingTimer = 0;
        if (targetCrop != null)
            waterDeer.getNavigation().moveTo(targetCrop.getX() + 0.5, targetCrop.getY(), targetCrop.getZ() + 0.5, 1.0);
    }

    @Override
    public void stop()
    {
        targetCrop = null;
        eatingTimer = 0;
    }

    @Override
    public void tick()
    {
        if (targetCrop == null) return;
        waterDeer.getLookControl().setLookAt(targetCrop.getX() + 0.5, targetCrop.getY(), targetCrop.getZ() + 0.5);
        double distance = waterDeer.distanceToSqr(targetCrop.getX() + 0.5, targetCrop.getY(), targetCrop.getZ() + 0.5);
        if (distance > 2.0)
        {
            waterDeer.getNavigation().moveTo(targetCrop.getX() + 0.5, targetCrop.getY(), targetCrop.getZ() + 0.5, 1.0);
            return;
        }
        eatingTimer++;
        if (eatingTimer >= EAT_TIME)
        {
            eatCrop();
            targetCrop = null;
        }
    }

    private void eatCrop()
    {
        if (targetCrop == null) return;
        Level world = waterDeer.level();
        BlockState state = world.getBlockState(targetCrop);
        if (!(state.getBlock() instanceof CropBlock cropBlock)) return;
        int maxAge = cropBlock.getMaxAge();
        int currentAge = cropBlock.getAge(state);
        int decrease = Math.max(1, (int) (maxAge * GROWTH_DECREASE));
        int newAge = Math.max(0, currentAge - decrease);
        if (newAge == 0)
            world.destroyBlock(targetCrop, false);
        else
            world.setBlock(targetCrop, cropBlock.getStateForAge(newAge), 2);
    }

    private BlockPos findCropInHomeChunk()
    {
        Level world = waterDeer.level();
        BlockPos entityPos = waterDeer.blockPosition();
        int homeChunkX = waterDeer.getHomeChunkX();
        int homeChunkZ = waterDeer.getHomeChunkZ();
        int chunkMinX = homeChunkX << 4;
        int chunkMaxX = chunkMinX + 15;
        int chunkMinZ = homeChunkZ << 4;
        int chunkMaxZ = chunkMinZ + 15;
        int searchMinX = Math.max(chunkMinX, entityPos.getX() - SEARCH_RANGE);
        int searchMaxX = Math.min(chunkMaxX, entityPos.getX() + SEARCH_RANGE);
        int searchMinZ = Math.max(chunkMinZ, entityPos.getZ() - SEARCH_RANGE);
        int searchMaxZ = Math.min(chunkMaxZ, entityPos.getZ() + SEARCH_RANGE);

        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (int x = searchMinX; x <= searchMaxX; x++)
        {
            for (int z = searchMinZ; z <= searchMaxZ; z++)
            {
                for (int y = entityPos.getY() - 2; y <= entityPos.getY() + 2; y++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (state.getBlock() instanceof CropBlock)
                    {
                        double distance = entityPos.distSqr(pos);
                        if (distance < closestDistance)
                        {
                            closestDistance = distance;
                            closest = pos;
                        }
                    }
                }
            }
        }
        return closest;
    }
}
