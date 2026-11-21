package art.arcane.witchery.client.renderer;

import art.arcane.witchery.client.model.LeonardModel;
import art.arcane.witchery.entity.Leonard;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

public final class LeonardRenderer extends MobRenderer<Leonard, LeonardModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("witchery", "textures/entities/leonard.png");

    public LeonardRenderer(EntityRendererProvider.Context context) {
        super(context, new LeonardModel(context.bakeLayer(LeonardModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Leonard entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(Leonard entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected void scale(Leonard entity, PoseStack pose, float partialTick) {
        if (entity.enlarged()) pose.scale(3, 3, 3);
    }
}
