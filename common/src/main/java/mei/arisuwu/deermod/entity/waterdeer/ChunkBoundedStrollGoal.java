package mei.arisuwu.deermod.entity.waterdeer;

import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ChunkBoundedStrollGoal extends RandomStrollGoal
{
    private final WaterDeerEntity waterDeer;

    public ChunkBoundedStrollGoal(WaterDeerEntity waterDeer, double speed)
    {
        super(waterDeer, speed);
        this.waterDeer = waterDeer;
    }

    @Override
    protected @Nullable Vec3 getPosition()
    {
        if (!waterDeer.isHomeChunkSet()) return super.getPosition();
        Vec3 pos = super.getPosition();
        if (pos == null) return null;

        int homeChunkX = waterDeer.getHomeChunkX();
        int homeChunkZ = waterDeer.getHomeChunkZ();
        int chunkMinX = homeChunkX << 4;
        int chunkMaxX = chunkMinX + 15;
        int chunkMinZ = homeChunkZ << 4;
        int chunkMaxZ = chunkMinZ + 15;

        double clampedX = Math.max(chunkMinX, Math.min(chunkMaxX, pos.x));
        double clampedZ = Math.max(chunkMinZ, Math.min(chunkMaxZ, pos.z));

        if (clampedX != pos.x || clampedZ != pos.z)
            return new Vec3(clampedX, pos.y, clampedZ);
        return pos;
    }
}
