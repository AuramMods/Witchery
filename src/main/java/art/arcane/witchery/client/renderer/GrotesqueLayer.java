package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.GrotesqueEffect;
import art.arcane.witchery.client.model.DemonModel;
import art.arcane.witchery.registry.WitcheryEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class GrotesqueLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "grotesque"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID,
            "textures/entities/demon.png");
    private final ModelPart face;

    public GrotesqueLayer(RenderLayerParent<T, M> parent, ModelPart root) {
        super(parent);
        this.face = root.getChild("face");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("face", DemonModel.headGeometry(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 32);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float age, float yaw, float pitch) {
        boolean fear = entity.hasEffect(WitcheryEffects.GROTESQUE.get());
        if (!fear && !(entity instanceof Player && entity.getPersistentData().getInt(GrotesqueEffect.BREW_TIMER) > 0)) return;
        boolean invisible = entity.isInvisible();
        var viewer = Minecraft.getInstance().player;
        if (invisible && (!fear || viewer == null || entity.isInvisibleTo(viewer))) return;

        pose.pushPose();
        if (getParentModel() instanceof HeadedModel headed) {
            headed.getHead().translateAndRotate(pose);
        } else {
            // The legacy overlay uses the living model's origin for creatures without a head transform.
            pose.mulPose(Axis.YP.rotationDegrees(yaw));
            pose.mulPose(Axis.XP.rotationDegrees(pitch));
        }
        pose.scale(1.3F, 1.3F, 1.3F);
        var type = invisible ? RenderType.entityTranslucent(TEXTURE) : RenderType.entityCutoutNoCull(TEXTURE);
        face.render(pose, buffers.getBuffer(type), light, OverlayTexture.NO_OVERLAY, 1, 1, 1, invisible ? 0.15F : 1);
        pose.popPose();
    }
}
