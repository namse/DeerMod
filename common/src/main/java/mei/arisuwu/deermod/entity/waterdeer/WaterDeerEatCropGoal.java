package mei.arisuwu.deermod.entity.waterdeer;

import mei.arisuwu.deermod.api.WaterDeerCropCallback;
import mei.arisuwu.deermod.api.WaterDeerCropCallbackRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.EnumSet;

public class WaterDeerEatCropGoal extends Goal
{
    private enum EatingPhase
    {
        APPROACHING,
        EATING
    }

    private static final int SEARCH_RANGE = 8;
    private static final int ANIMATION_DURATION = 40;
    private static final int EAT_EFFECT_TICK = 20;
    private static final float GROWTH_DECREASE = 0.15f;

    private final WaterDeerEntity waterDeer;
    private BlockPos targetCrop;
    private EatingPhase phase;
    private int eatingTicks;

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

        if (!isCropBlock(world, targetCrop, state)) return false;

        double distanceXZ = Math.pow(waterDeer.getX() - (targetCrop.getX() + 0.5), 2) + Math.pow(waterDeer.getZ() - (targetCrop.getZ() + 0.5), 2);
        return distanceXZ < 4.0 || waterDeer.getNavigation().isInProgress();
    }

    @Override
    public void start()
    {
        phase = EatingPhase.APPROACHING;
        eatingTicks = 0;
        waterDeer.setWaterDeerState(WaterDeerState.EATING);
        if (targetCrop != null)
            moveToTarget();
    }

    private void moveToTarget()
    {
        if (targetCrop == null) return;
        waterDeer.getNavigation().moveTo(targetCrop.getX() + 0.5, targetCrop.getY(), targetCrop.getZ() + 0.5, 1.0);
    }

    @Override
    public void stop()
    {
        targetCrop = null;
        phase = EatingPhase.APPROACHING;
        eatingTicks = 0;
        waterDeer.setWaterDeerState(WaterDeerState.IDLE);
    }

    @Override
    public void tick()
    {
        if (targetCrop == null) return;

        switch (phase)
        {
            case APPROACHING -> tickApproaching();
            case EATING -> tickEating();
        }
    }

    private void tickApproaching()
    {
        waterDeer.getLookControl().setLookAt(targetCrop.getX() + 0.5, targetCrop.getY(), targetCrop.getZ() + 0.5);
        double distanceXZ = Math.pow(waterDeer.getX() - (targetCrop.getX() + 0.5), 2) + Math.pow(waterDeer.getZ() - (targetCrop.getZ() + 0.5), 2);
        double distanceY = Math.abs(waterDeer.getY() - targetCrop.getY());

        if (distanceXZ > 1.0 || distanceY > 0.5)
        {
            moveToTarget();
            return;
        }

        phase = EatingPhase.EATING;
        eatingTicks = 0;
        waterDeer.triggerEatAnimation();
    }

    private void tickEating()
    {
        waterDeer.getLookControl().setLookAt(targetCrop.getX() + 0.5, targetCrop.getY(), targetCrop.getZ() + 0.5);
        eatingTicks++;

        if (eatingTicks == EAT_EFFECT_TICK)
        {
            boolean cropDestroyed = eatCrop();
            if (cropDestroyed)
            {
                targetCrop = null;
                return;
            }
        }

        if (eatingTicks >= ANIMATION_DURATION && targetCrop != null)
        {
            eatingTicks = 0;
            waterDeer.triggerEatAnimation();
        }
    }

    private boolean eatCrop()
    {
        if (targetCrop == null) return true;
        Level world = waterDeer.level();
        BlockState state = world.getBlockState(targetCrop);

        WaterDeerCropCallback callback = WaterDeerCropCallbackRegistry.get();
        if (callback != null && callback.isCrop(world, targetCrop))
        {
            playEatEffects(world);
            return callback.onCropEaten(world, targetCrop, waterDeer);
        }

        if (!(state.getBlock() instanceof CropBlock cropBlock)) return true;
        int maxAge = cropBlock.getMaxAge();
        int currentAge = cropBlock.getAge(state);
        int decrease = Math.max(1, (int) (maxAge * GROWTH_DECREASE));
        int newAge = Math.max(0, currentAge - decrease);

        playEatEffects(world);

        if (newAge == 0)
        {
            world.destroyBlock(targetCrop, false);
            return true;
        }
        else
        {
            world.setBlock(targetCrop, cropBlock.getStateForAge(newAge), 2);
            return false;
        }
    }

    private void playEatEffects(Level world)
    {
        if (world instanceof ServerLevel serverLevel)
        {
            double x = waterDeer.getX();
            double y = waterDeer.getY() + waterDeer.getEyeHeight() * 0.8;
            double z = waterDeer.getZ();

            serverLevel.playSound(
                null,
                waterDeer.blockPosition(),
                SoundEvents.GENERIC_EAT.value(),
                SoundSource.NEUTRAL,
                1.0f,
                1.0f + (waterDeer.getRandom().nextFloat() - 0.5f) * 0.2f
            );

            serverLevel.sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.WHEAT_SEEDS)),
                x, y, z,
                8,
                0.2, 0.2, 0.2,
                0.05
            );
        }
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
                    if (isCropBlock(world, pos, state))
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

    private boolean isCropBlock(Level world, BlockPos pos, BlockState state)
    {
        WaterDeerCropCallback callback = WaterDeerCropCallbackRegistry.get();
        if (callback != null && callback.isCrop(world, pos))
        {
            return true;
        }
        return state.getBlock() instanceof CropBlock;
    }
}
