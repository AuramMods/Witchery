package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.MindrakeModel;
import art.arcane.witchery.entity.Mindrake;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class MindrakeRenderer extends MobRenderer<Mindrake, MindrakeModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/mindrake.png");

    public MindrakeRenderer(EntityRendererProvider.Context context) {
        super(context, new MindrakeModel(context.bakeLayer(MindrakeModel.LAYER)), 0.25F);
    }

    @Override
    protected void setupRotations(Mindrake entity, PoseStack pose, float age, float yaw, float partialTick) {
        super.setupRotations(entity, pose, age, yaw, partialTick);
        MandrakeRenderer.swayBody(entity, pose, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(Mindrake entity) {
        return TEXTURE;
    }
}
