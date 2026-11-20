package art.arcane.witchery.client.renderer;

import art.arcane.witchery.entity.Reflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public final class ReflectionRenderer extends HumanoidMobRenderer<Reflection, PlayerModel<Reflection>> {
    public ReflectionRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(Reflection reflection) {
        UUID owner = reflection.ownerId().orElseGet(reflection::getUUID);
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo player = connection == null ? null : connection.getPlayerInfo(owner);
        return player == null ? DefaultPlayerSkin.getDefaultSkin(owner) : player.getSkinLocation();
    }
}
