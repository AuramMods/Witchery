package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.EntModel;
import art.arcane.witchery.entity.Ent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class EntRenderer extends MobRenderer<Ent, EntModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/ent.png");

    public EntRenderer(EntityRendererProvider.Context context) {
        super(context, new EntModel(context.bakeLayer(EntModel.LAYER)), 0.5F);
    }

    @Override
    protected void setupRotations(Ent ent, PoseStack pose, float age, float yaw, float partialTick) {
        super.setupRotations(ent, pose, age, yaw, partialTick);
        if (ent.walkAnimation.speed() >= 0.01F) {
            float step = ent.walkAnimation.position(partialTick) + 6;
            float sway = (Math.abs(step % 13 - 6.5F) - 3.25F) / 3.25F;
            pose.mulPose(Axis.ZP.rotationDegrees(6.5F * sway));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Ent ent) {
        return TEXTURE;
    }
}
