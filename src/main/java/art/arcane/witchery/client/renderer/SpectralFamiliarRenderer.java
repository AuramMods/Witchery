package art.arcane.witchery.client.renderer;

import art.arcane.witchery.entity.SpectralFamiliar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class SpectralFamiliarRenderer extends MobRenderer<SpectralFamiliar, PigModel<SpectralFamiliar>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/pig/pig.png");

    public SpectralFamiliarRenderer(EntityRendererProvider.Context context) {
        super(context, new SpectralPigModel(context.bakeLayer(ModelLayers.PIG)), 0);
    }

    @Override
    public ResourceLocation getTextureLocation(SpectralFamiliar familiar) { return TEXTURE; }

    @Override
    protected void scale(SpectralFamiliar familiar, PoseStack pose, float partialTick) {
        if (familiar.isTame()) pose.scale(0.8F, 0.8F, 0.8F);
    }

    @Nullable
    @Override
    protected RenderType getRenderType(SpectralFamiliar familiar, boolean visible, boolean translucent, boolean glowing) {
        return visible || translucent ? RenderType.entityTranslucent(TEXTURE) : super.getRenderType(familiar, false, false, glowing);
    }

    private static final class SpectralPigModel extends PigModel<SpectralFamiliar> {
        private boolean searching;

        SpectralPigModel(ModelPart root) { super(root); }

        @Override
        public void setupAnim(SpectralFamiliar familiar, float walk, float speed, float age, float yaw, float pitch) {
            bodyParts().forEach(ModelPart::resetPose);
            super.setupAnim(familiar, walk, speed / 1.4F, age, yaw, pitch);
            searching = familiar.isSearching();
            if (familiar.isInSittingPose()) {
                body.xRot = (float) Math.PI / 4;
                body.y += 3.5F;
                poseLeg(rightHindLeg, 15.8F, -7, -(float) Math.PI / 20);
                poseLeg(leftHindLeg, 15.8F, -7, -(float) Math.PI / 20);
                poseLeg(rightFrontLeg, 21, 1, -(float) Math.PI / 2);
                poseLeg(leftFrontLeg, 21, 1, -(float) Math.PI / 2);
            }
        }

        private static void poseLeg(ModelPart leg, float y, float z, float angle) {
            leg.setPos(leg.x, y, z);
            leg.xRot = angle;
        }

        @Override
        public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                                   float red, float green, float blue, float alpha) {
            super.renderToBuffer(pose, vertices, light, overlay, red * (searching ? 0.7F : 0.5F),
                    green * (searching ? 0.3F : 0.5F), blue, alpha * 0.51F);
        }
    }
}
