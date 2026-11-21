package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.StatueOfWorshipBlock;
import art.arcane.witchery.block.entity.StatueOfWorshipBlockEntity;
import art.arcane.witchery.client.ClientPlayerAppearance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class StatueOfWorshipRenderer implements BlockEntityRenderer<StatueOfWorshipBlockEntity> {
    public static final ModelLayerLocation STONE_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "statueofworship"), "stone");
    private static final ResourceLocation STONE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/block/statueofworship.png");

    private final HumanoidModel<LivingEntity> normal;
    private final HumanoidModel<LivingEntity> slim;
    private final HumanoidModel<LivingEntity> stone;

    public StatueOfWorshipRenderer(BlockEntityRendererProvider.Context context) {
        normal = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER));
        slim = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM));
        stone = new HumanoidModel<>(context.bakeLayer(STONE_LAYER));
        pose(normal);
        pose(slim);
        pose(stone);
    }

    public static LayerDefinition layer() {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.02F), 0), 64, 32);
    }

    private static void pose(HumanoidModel<?> model) {
        model.body.xRot = 0.5F;
        model.rightArm.xRot = model.leftArm.xRot = 0.086F;
        model.rightLeg.y = model.leftLeg.y = 9;
        model.rightLeg.z = model.leftLeg.z = 4;
        model.head.y = 1;
        model.hat.copyFrom(model.head);
    }

    @Override
    public void render(StatueOfWorshipBlockEntity statue, float partialTick, PoseStack pose, MultiBufferSource buffer,
                       int light, int overlay) {
        var appearance = ClientPlayerAppearance.of(statue.owner());
        var skin = "slim".equals(appearance.model()) ? slim : normal;
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(statue.getBlockState().getValue(StatueOfWorshipBlock.FACING).toYRot() + 180));
        skin.renderToBuffer(pose, buffer.getBuffer(RenderType.entityCutoutNoCull(appearance.texture())),
                light, overlay, 0.7F, 0.7F, 0.7F, 1);
        stone.renderToBuffer(pose, buffer.getBuffer(RenderType.entityTranslucent(STONE)),
                light, overlay, 0.8F, 0.8F, 0.8F, 1);
        pose.popPose();
    }
}
