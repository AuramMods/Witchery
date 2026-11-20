package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.NightmareModel;
import art.arcane.witchery.entity.Nightmare;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class NightmareRenderer extends MobRenderer<Nightmare, NightmareModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/nightmare.png");

    public NightmareRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareModel(context.bakeLayer(NightmareModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Nightmare entity) {
        return TEXTURE;
    }
}
