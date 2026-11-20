package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.DarkMarkModel;
import art.arcane.witchery.entity.DarkMark;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class DarkMarkRenderer extends MobRenderer<DarkMark, DarkMarkModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/darkmark.png");

    public DarkMarkRenderer(EntityRendererProvider.Context context) {
        super(context, new DarkMarkModel(context.bakeLayer(DarkMarkModel.LAYER)), 0.5F);
    }

    @Override
    protected void scale(DarkMark mark, PoseStack pose, float partialTick) {
        pose.scale(2, 2, 2);
    }

    @Override
    public ResourceLocation getTextureLocation(DarkMark mark) {
        return TEXTURE;
    }
}
