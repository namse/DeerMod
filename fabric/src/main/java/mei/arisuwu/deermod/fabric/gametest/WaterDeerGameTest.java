package mei.arisuwu.deermod.fabric.gametest;

import mei.arisuwu.deermod.ModEntities;
import mei.arisuwu.deermod.entity.waterdeer.WaterDeerEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WaterDeerGameTest
{
    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void waterDeerSpawnTest(GameTestHelper helper)
    {
        BlockPos spawnPos = new BlockPos(1, 2, 1);
        WaterDeerEntity waterDeer = helper.spawn(ModEntities.WATER_DEER.get(), spawnPos);
        helper.assertEntityPresent(ModEntities.WATER_DEER.get(), spawnPos);
        helper.succeed();
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 40)
    public void waterDeerHomeChunkSetTest(GameTestHelper helper)
    {
        BlockPos spawnPos = new BlockPos(1, 2, 1);
        WaterDeerEntity waterDeer = helper.spawn(ModEntities.WATER_DEER.get(), spawnPos);

        helper.runAfterDelay(20, () -> {
            if (waterDeer.isHomeChunkSet())
            {
                helper.succeed();
            }
            else
            {
                helper.fail("Home chunk was not set");
            }
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 400, required = false)
    public void waterDeerEatCropTest(GameTestHelper helper)
    {
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.FARMLAND);
        BlockPos cropPos = new BlockPos(2, 2, 2);
        BlockState wheatState = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7);
        helper.setBlock(cropPos, wheatState);

        BlockPos spawnPos = new BlockPos(2, 2, 3);
        WaterDeerEntity waterDeer = helper.spawn(ModEntities.WATER_DEER.get(), spawnPos);

        helper.runAfterDelay(350, () -> {
            BlockState currentState = helper.getBlockState(cropPos);
            if (!(currentState.getBlock() instanceof CropBlock))
            {
                helper.succeed();
                return;
            }
            int currentAge = ((CropBlock) currentState.getBlock()).getAge(currentState);
            if (currentAge < 7)
            {
                helper.succeed();
            }
            else
            {
                helper.fail("Water deer did not eat the crop (optional test)");
            }
        });
    }

    @GameTest(structure = "fabric-gametest-api-v1:empty")
    public void waterDeerAttributesTest(GameTestHelper helper)
    {
        BlockPos spawnPos = new BlockPos(1, 2, 1);
        WaterDeerEntity waterDeer = helper.spawn(ModEntities.WATER_DEER.get(), spawnPos);

        double maxHealth = waterDeer.getMaxHealth();

        if (maxHealth == 10.0)
        {
            helper.succeed();
        }
        else
        {
            helper.fail("Expected max health 10.0, but got " + maxHealth);
        }
    }
}
