package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.ImpModel;
import art.arcane.witchery.entity.Imp;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class ImpRenderer extends MobRenderer<Imp, ImpModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/imp.png");

    public ImpRenderer(EntityRendererProvider.Context context) {
        super(context, new ImpModel(context.bakeLayer(ImpModel.LAYER)), 0.5F);
    }

    @Override
    protected void scale(Imp imp, PoseStack pose, float partialTick) {
        if (imp.isPowered()) pose.scale(1.5F, 1, 1.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Imp imp) {
        return TEXTURE;
    }
}
