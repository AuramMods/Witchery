package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.MandrakeModel;
import art.arcane.witchery.entity.Mandrake;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class MandrakeRenderer extends MobRenderer<Mandrake, MandrakeModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/mandrake.png");

    public MandrakeRenderer(EntityRendererProvider.Context context) {
        super(context, new MandrakeModel(context.bakeLayer(MandrakeModel.LAYER)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(Mandrake entity) {
        return TEXTURE;
    }
}
