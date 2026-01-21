package mei.arisuwu.deermod.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface WaterDeerCropCallback {
    boolean isCrop(Level level, BlockPos pos);
    /**
     * @return true if crop was destroyed, false if crop still exists
     */
    boolean onCropEaten(Level level, BlockPos pos, Entity waterDeer);
}
