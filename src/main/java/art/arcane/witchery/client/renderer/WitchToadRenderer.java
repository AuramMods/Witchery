package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.WitchToadModel;
import art.arcane.witchery.entity.WitchToad;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class WitchToadRenderer extends MobRenderer<WitchToad, WitchToadModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/toad.png");

    public WitchToadRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchToadModel(context.bakeLayer(WitchToadModel.LAYER)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(WitchToad toad) {
        return TEXTURE;
    }
}
