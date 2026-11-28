package art.arcane.witchery.client.renderer;

import art.arcane.witchery.entity.Illusion;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class IllusionRenderer<M extends EntityModel<Illusion>> extends MobRenderer<Illusion, M> {
    private final ResourceLocation texture;

    public IllusionRenderer(EntityRendererProvider.Context context, M model, String variant) {
        super(context, model, 0);
        texture = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/" + variant + "/" + variant + ".png");
    }

    public static IllusionRenderer<?> zombie(EntityRendererProvider.Context context) {
        return new IllusionRenderer<>(context, new ZombieApparitionModel(context.bakeLayer(ModelLayers.ZOMBIE)), "zombie");
    }

    @Override
    public ResourceLocation getTextureLocation(Illusion entity) {
        return texture;
    }

    @Override
    public boolean shouldRender(Illusion entity, Frustum frustum, double x, double y, double z) {
        return isVictim(entity) && super.shouldRender(entity, frustum, x, y, z);
    }

    @Override
    public void render(Illusion entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        if (isVictim(entity)) super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    private static boolean isVictim(Illusion entity) {
        var player = Minecraft.getInstance().player;
        return player != null && player.getGameProfile().getName().equals(entity.victimName());
    }

    private static final class ZombieApparitionModel extends LegacyBipedModel<Illusion> {
        private ZombieApparitionModel(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(Illusion mob, float swing, float amount, float age, float yaw, float pitch) {
            super.setupAnim(mob, swing, amount, age, yaw, pitch);
            // The original biped moved its shoulders around a right-handed swing.
            body.yRot = Mth.sin(Mth.sqrt(attackTime) * Mth.TWO_PI) * 0.2F;
            rightArm.x = -Mth.cos(body.yRot) * 5;
            leftArm.x = -rightArm.x;
            rightArm.z = Mth.sin(body.yRot) * 5;
            leftArm.z = -rightArm.z;
            float sweep = Mth.sin(attackTime * Mth.PI);
            float recovery = Mth.sin((1 - (1 - attackTime) * (1 - attackTime)) * Mth.PI);
            rightArm.yRot = -(0.1F - sweep * 0.6F);
            leftArm.yRot = -rightArm.yRot;
            rightArm.xRot = leftArm.xRot = -Mth.HALF_PI - (sweep * 1.2F - recovery * 0.4F);
            rightArm.zRot = leftArm.zRot = 0;
            AnimationUtils.bobArms(rightArm, leftArm, age);
        }
    }
}
