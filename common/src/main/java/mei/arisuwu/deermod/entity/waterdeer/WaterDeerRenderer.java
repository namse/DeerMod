package mei.arisuwu.deermod.entity.waterdeer;

import mei.arisuwu.deermod.ModModelLayers;
import mei.arisuwu.deermod.ModResourceLocation;
import mei.arisuwu.deermod.entity.deer.DeerEntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class WaterDeerRenderer extends MobRenderer<WaterDeerEntity, WaterDeerRenderState, DeerEntityModel>
{
    private static final float SHADOW_RADIUS = 0.4f;

    public WaterDeerRenderer(EntityRendererProvider.Context context)
    {
        super(context, new DeerEntityModel(context.bakeLayer(ModModelLayers.DEER)), SHADOW_RADIUS);
    }

    @Override
    public @NotNull Identifier getTextureLocation(WaterDeerRenderState state)
    {
        return ModResourceLocation.of("textures/entity/water_deer/water_deer.png");
    }

    @Override
    public @NotNull WaterDeerRenderState createRenderState()
    {
        return new WaterDeerRenderState();
    }

    @Override
    public void extractRenderState(WaterDeerEntity entity, WaterDeerRenderState state, float delta)
    {
        super.extractRenderState(entity, state, delta);
        state.eatGrassAnimationState.copyFrom(entity.eatGrassAnimationState);
    }
}
