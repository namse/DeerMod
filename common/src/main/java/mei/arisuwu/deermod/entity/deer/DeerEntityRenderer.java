package mei.arisuwu.deermod.entity.deer;

import mei.arisuwu.deermod.ModResourceLocation;
import mei.arisuwu.deermod.ModModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class DeerEntityRenderer extends MobRenderer<DeerEntity, DeerEntityRenderState, DeerEntityModel>
{
    private final static float BASE_SHADOW_RADIUS = 0.75f;

    public DeerEntityRenderer(EntityRendererProvider.Context context)
    {
        super(context, new DeerEntityModel(context.bakeLayer(ModModelLayers.DEER)), BASE_SHADOW_RADIUS);
    }

    @Override
    public @NotNull Identifier getTextureLocation(DeerEntityRenderState state)
    {
        return ModResourceLocation.of("textures/entity/deer/deer.png");
    }

    @Override
    public @NotNull DeerEntityRenderState createRenderState()
    {
        return new DeerEntityRenderState();
    }

    @Override
    public void extractRenderState(DeerEntity deerEntity, DeerEntityRenderState deerEntityRenderState, float delta)
    {
        super.extractRenderState(deerEntity, deerEntityRenderState, delta);
        deerEntityRenderState.hasRedNose = deerEntity.hasRedNose();
        deerEntityRenderState.sheared = deerEntity.isSheared();
        deerEntityRenderState.saddled = deerEntity.isSaddled();
        deerEntityRenderState.eatGrassAnimationState.copyFrom(deerEntity.eatGrassAnimationState);
    }

}
