package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.ParasyticLouseModel;
import art.arcane.witchery.entity.ParasyticLouse;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class ParasyticLouseRenderer extends MobRenderer<ParasyticLouse, ParasyticLouseModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/louse.png");

    public ParasyticLouseRenderer(EntityRendererProvider.Context context) {
        super(context, new ParasyticLouseModel(context.bakeLayer(ParasyticLouseModel.LAYER)), 0.3F);
    }

    @Override
    protected float getFlipDegrees(ParasyticLouse louse) {
        return 180;
    }

    @Override
    public ResourceLocation getTextureLocation(ParasyticLouse louse) {
        return TEXTURE;
    }
}
