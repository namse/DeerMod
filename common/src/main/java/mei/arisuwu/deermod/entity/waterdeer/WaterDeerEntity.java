package mei.arisuwu.deermod.entity.waterdeer;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class WaterDeerEntity extends Animal
{
    private static final EntityDataAccessor<Integer> WATER_DEER_STATE = SynchedEntityData.defineId(WaterDeerEntity.class, EntityDataSerializers.INT);

    private int homeChunkX;
    private int homeChunkZ;
    private boolean homeChunkSet = false;
    private PanicGoal panicGoal;

    public static AttributeSupplier.Builder createAttributes()
    {
        return Animal.createAnimalAttributes()
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.MOVEMENT_SPEED, 0.35f);
    }

    public WaterDeerEntity(EntityType<? extends Animal> entityType, Level world)
    {
        super(entityType, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData(builder);
        builder.define(WATER_DEER_STATE, WaterDeerState.IDLE.ordinal());
    }

    public WaterDeerState getWaterDeerState()
    {
        int ordinal = entityData.get(WATER_DEER_STATE);
        if (ordinal >= 0 && ordinal < WaterDeerState.values().length)
        {
            return WaterDeerState.values()[ordinal];
        }
        return WaterDeerState.IDLE;
    }

    public void setWaterDeerState(WaterDeerState state)
    {
        entityData.set(WATER_DEER_STATE, state.ordinal());
    }

    @Override
    protected void registerGoals()
    {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, panicGoal = new PanicGoal(this, 2.0));
        goalSelector.addGoal(2, new WaterDeerEatCropGoal(this));
        goalSelector.addGoal(3, new ChunkBoundedStrollGoal(this, 1.0));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick()
    {
        super.tick();

        if (!level().isClientSide() && !homeChunkSet)
        {
            homeChunkX = blockPosition().getX() >> 4;
            homeChunkZ = blockPosition().getZ() >> 4;
            homeChunkSet = true;
        }

        if (!level().isClientSide() && homeChunkSet)
        {
            int currentChunkX = blockPosition().getX() >> 4;
            int currentChunkZ = blockPosition().getZ() >> 4;

            if (currentChunkX != homeChunkX || currentChunkZ != homeChunkZ)
            {
                double targetX = (homeChunkX << 4) + 8;
                double targetZ = (homeChunkZ << 4) + 8;
                getNavigation().moveTo(targetX, blockPosition().getY(), targetZ, 1.5);
            }

            updateState(currentChunkX, currentChunkZ);
        }
    }

    private void updateState(int currentChunkX, int currentChunkZ)
    {
        if (panicGoal != null && panicGoal.isRunning())
        {
            setWaterDeerState(WaterDeerState.FLEEING);
            return;
        }

        if (currentChunkX != homeChunkX || currentChunkZ != homeChunkZ)
        {
            setWaterDeerState(WaterDeerState.RETURNING_HOME);
            return;
        }

        if (getWaterDeerState() == WaterDeerState.EATING)
        {
            return;
        }

        if (getNavigation().isInProgress())
        {
            setWaterDeerState(WaterDeerState.WANDERING);
            return;
        }

        setWaterDeerState(WaterDeerState.IDLE);
    }

    public int getHomeChunkX() { return homeChunkX; }
    public int getHomeChunkZ() { return homeChunkZ; }
    public boolean isHomeChunkSet() { return homeChunkSet; }

    @Override
    protected void addAdditionalSaveData(ValueOutput output)
    {
        super.addAdditionalSaveData(output);
        output.putInt("HomeChunkX", homeChunkX);
        output.putInt("HomeChunkZ", homeChunkZ);
        output.putBoolean("HomeChunkSet", homeChunkSet);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input)
    {
        super.readAdditionalSaveData(input);
        homeChunkX = input.getIntOr("HomeChunkX", 0);
        homeChunkZ = input.getIntOr("HomeChunkZ", 0);
        homeChunkSet = input.getBooleanOr("HomeChunkSet", false);
    }

    @Override
    public boolean isFood(ItemStack stack) { return false; }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel world, AgeableMob entity) { return null; }
}
