package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WolfHeadBlock;
import art.arcane.witchery.block.entity.WolfHeadBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.RotationSegment;

public final class WolfHeadRenderer implements BlockEntityRenderer<WolfHeadBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "wolfhead"), "main");
    private static final ResourceLocation WOLF_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf.png");
    private final SkullModelBase model;

    public WolfHeadRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart head = context.bakeLayer(LAYER).getChild("head");
        model = new SkullModelBase() {
            @Override
            public void setupAnim(float animation, float yaw, float pitch) {
                head.yRot = yaw * Mth.DEG_TO_RAD;
            }

            @Override
            public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                                       float red, float green, float blue, float alpha) {
                head.render(pose, vertices, light, overlay, red, green, blue, alpha);
            }
        };
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3, -6, 0, 6, 6, 4)
                .texOffs(16, 14).addBox(-3, -8, 3, 2, 2, 1).addBox(1, -8, 3, 2, 2, 1)
                .texOffs(0, 10).addBox(-1.5F, -3.1F, -3, 3, 3, 4), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(WolfHeadBlockEntity skull, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        var state = skull.getBlockState();
        Direction facing = state.getValue(WolfHeadBlock.FACING);
        boolean onFloor = facing == Direction.UP;
        int angle = onFloor ? state.getValue(WolfHeadBlock.ROTATION) : RotationSegment.convertToSegment(facing.getOpposite());
        ResourceLocation texture = state.getValue(WolfHeadBlock.HELLHOUND) ? HellhoundRenderer.TEXTURE : WOLF_TEXTURE;
        SkullBlockRenderer.renderSkull(onFloor ? null : facing, RotationSegment.convertToDegrees(angle), 0,
                pose, buffers, light, model, RenderType.entityCutoutNoCullZOffset(texture));
    }
}
