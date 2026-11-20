package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.LilithModel;
import art.arcane.witchery.entity.Lilith;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class LilithRenderer extends MobRenderer<Lilith, LilithModel> {
    private static final ResourceLocation SKIN = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/lilith.png");

    public LilithRenderer(EntityRendererProvider.Context context) {
        super(context, new LilithModel(context.bakeLayer(LilithModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Lilith lilith) {
        return SKIN;
    }
}
