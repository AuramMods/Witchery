package art.arcane.witchery.client.renderer;

import art.arcane.witchery.client.model.DeathModel;
import art.arcane.witchery.entity.DeathBoss;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class DeathRenderer extends MobRenderer<DeathBoss, DeathModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("witchery", "textures/entities/death.png");

    public DeathRenderer(EntityRendererProvider.Context context) {
        super(context, new DeathModel(context.bakeLayer(DeathModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DeathBoss entity) {
        return TEXTURE;
    }
}
