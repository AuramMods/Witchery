package art.arcane.witchery.client.renderer;

import art.arcane.witchery.client.ClientPlayerAppearance;
import art.arcane.witchery.entity.SleepingBody;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.UUID;

public final class SleepingBodyRenderer extends MobRenderer<SleepingBody, HumanoidModel<SleepingBody>> {
    private static final LayerDefinition BODY_LAYER = LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0), 64, 64);

    public SleepingBodyRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(BODY_LAYER.bakeRoot()) {
            @Override
            public void setupAnim(SleepingBody body, float walk, float stride, float age, float headYaw, float headPitch) {
                // The original sleeping body holds the neutral biped pose.
            }
        }, 0);
    }

    @Override
    protected void setupRotations(SleepingBody body, PoseStack pose, float age, float yaw, float partialTick) {
        float roll = getFlipDegrees(body);
        pose.translate(0.9F, 0.25F, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(roll));
        pose.mulPose(Axis.YP.rotationDegrees(roll));
        pose.mulPose(Axis.YP.rotationDegrees(180 - yaw));
        if (body.deathTime > 0) {
            float progress = Mth.sqrt((body.deathTime + partialTick - 1) / 20 * 1.6F);
            pose.mulPose(Axis.YP.rotationDegrees(Math.min(progress, 1) * roll));
        } else if (isEntityUpsideDown(body)) {
            pose.translate(0, body.getBbHeight() + 0.1F, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(180));
        }
    }

    @Override
    protected void scale(SleepingBody body, PoseStack pose, float partialTick) {
        // Convert LivingEntityRenderer's model offset to the original biped renderer's offset.
        pose.translate(0, 1.501F - 1.5078125F, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(SleepingBody body) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo owner = connection == null ? null : body.ownerId().map(connection::getPlayerInfo).orElse(null);
        if (owner != null) return owner.getSkinLocation();
        if (!body.ownerName().isEmpty()) return ClientPlayerAppearance.of(body.ownerName()).texture();
        UUID fallback = body.ownerId().orElseGet(body::getUUID);
        return DefaultPlayerSkin.getDefaultSkin(fallback);
    }
}
