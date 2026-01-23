package mei.arisuwu.deermod.fabric.gametest;

import mei.arisuwu.deermod.ModEntities;
import mei.arisuwu.deermod.entity.waterdeer.WaterDeerEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
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

    @GameTest(structure = "fabric-gametest-api-v1:empty", maxTicks = 600, required = false)
    public void waterDeerEatCropTest(GameTestHelper helper)
    {
        for (int x = 0; x < 9; x++)
        {
            for (int z = 0; z < 9; z++)
            {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.GRASS_BLOCK);
            }
        }

        final WaterDeerEntity[] deerHolder = new WaterDeerEntity[1];
        final BlockPos[] cropPosHolder = new BlockPos[1];

        helper.runAfterDelay(10, () -> {
            BlockPos spawnPos = new BlockPos(4, 2, 4);
            deerHolder[0] = helper.spawn(ModEntities.WATER_DEER.get(), spawnPos);
        });

        helper.runAfterDelay(50, () -> {
            WaterDeerEntity waterDeer = deerHolder[0];
            Level level = helper.getLevel();
            BlockPos deerPos = waterDeer.blockPosition();

            BlockPos farmlandPos = deerPos.offset(2, -1, 0);
            cropPosHolder[0] = deerPos.offset(2, 0, 0);

            level.setBlock(farmlandPos, Blocks.FARMLAND.defaultBlockState(), 3);
            BlockState wheatState = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7);
            level.setBlock(cropPosHolder[0], wheatState, 3);
        });

        helper.runAfterDelay(550, () -> {
            if (cropPosHolder[0] == null)
            {
                helper.fail("Crop was not placed");
                return;
            }
            Level level = helper.getLevel();
            BlockState currentState = level.getBlockState(cropPosHolder[0]);
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
