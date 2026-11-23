package art.arcane.witchery.client.renderer;

import art.arcane.witchery.client.ClientPlayerAppearance;
import art.arcane.witchery.entity.SleepingBody;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public final class SleepingBodyRenderer extends MobRenderer<SleepingBody, PlayerModel<SleepingBody>> {
    public SleepingBodyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0);
    }

    @Override
    protected void setupRotations(SleepingBody body, PoseStack pose, float age, float yaw, float partialTick) {
        pose.translate(0.9, 0.25, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(90));
        pose.mulPose(Axis.YP.rotationDegrees(270 - yaw));
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
