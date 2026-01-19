package mei.arisuwu.deermod.fabric;

import mei.arisuwu.deermod.ModEntities;
import mei.arisuwu.deermod.ModItems;
import mei.arisuwu.deermod.entity.deer.DeerEntity;
import mei.arisuwu.deermod.entity.waterdeer.WaterDeerEntity;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;


public final class FabricMod implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        new ModEntities();
        new ModItems();
        new FabricModCreativeTabs();
        FabricDefaultAttributeRegistry.register(ModEntities.DEER.get(), DeerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.WATER_DEER.get(), WaterDeerEntity.createAttributes());
    }
}
