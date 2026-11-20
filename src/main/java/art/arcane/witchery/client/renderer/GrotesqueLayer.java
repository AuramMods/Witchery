package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.DemonModel;
import art.arcane.witchery.registry.WitcheryEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class GrotesqueLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "grotesque"), "main");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/demon.png"));
    private final ModelPart face;

    public GrotesqueLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, ModelPart root) {
        super(parent);
        this.face = root.getChild("face");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("face", DemonModel.headGeometry(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 32);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float age, float yaw, float pitch) {
        if (!player.isInvisible() && player.hasEffect(WitcheryEffects.GROTESQUE.get())) {
            pose.pushPose();
            getParentModel().head.translateAndRotate(pose);
            pose.scale(1.3F, 1.3F, 1.3F);
            face.render(pose, buffers.getBuffer(RENDER_TYPE), light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
    }
}
