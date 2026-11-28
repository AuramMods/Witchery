package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.block.entity.MirrorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class MirrorBlockRenderer implements BlockEntityRenderer<MirrorBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "mirror"), "main");
    private static final RenderType FRAME = RenderType.entityCutoutNoCull(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/mirror.png"));
    private static final RenderType GLASS = RenderType.entityCutoutNoCull(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/mirror2.png"));
    private final ModelPart model;
    private final ModelPart glass;

    public MirrorBlockRenderer(BlockEntityRendererProvider.Context context) {
        model = context.bakeLayer(LAYER);
        glass = model.getChild("glass");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(7, 16).addBox(-5, -7, -1, 10, 15, 1),
                PartPose.offset(0, 0, 1));
        root.addOrReplaceChild("side_glass", CubeListBuilder.create().texOffs(0, 17)
                .addBox(-7, -6, -1, 2, 14, 1).mirror().addBox(5, -6, -1, 2, 14, 1), PartPose.offset(0, 0, 1));
        root.addOrReplaceChild("frame", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7.5F, -3, -1, 1, 11, 1).mirror().addBox(6.5F, -3, -1, 1, 11, 1).mirror(false)
                .texOffs(5, 0).addBox(-8, -5, -1, 1, 2, 1).mirror().addBox(7, -5, -1, 1, 2, 1).mirror(false)
                .texOffs(10, 0).addBox(-7.5F, -6, -1, 2, 1, 1).mirror().addBox(5.5F, -6, -1, 2, 1, 1).mirror(false)
                .texOffs(17, 0).addBox(-6, -7, -1, 1, 1, 1).mirror().addBox(5, -7, -1, 1, 1, 1).mirror(false)
                .texOffs(4, 3).addBox(-5, -7.5F, -1, 10, 1, 1)
                .texOffs(5, 5).addBox(-6, -2, -1, 1, 10, 1).mirror().addBox(5, -2, -1, 1, 10, 1).mirror(false)
                .texOffs(10, 6).addBox(-5, -3, -1, 1, 2, 1).mirror().addBox(4, -3, -1, 1, 2, 1).mirror(false)
                .texOffs(15, 6).addBox(-4, -4, -1, 1, 2, 1).mirror().addBox(3, -4, -1, 1, 2, 1).mirror(false)
                .texOffs(10, 10).addBox(-3, -4, -1, 6, 1, 1), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void render(MirrorBlockEntity mirror, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        BlockState state = mirror.getBlockState();
        if (!(state.getBlock() instanceof MirrorBlock)) return;
        Direction front = state.getValue(MirrorBlock.FACING);
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(front.toYRot() - 180));
        if (state.getValue(MirrorBlock.HALF) == DoubleBlockHalf.LOWER) pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.translate(0, 0, 0.44);
        model.render(pose, buffers.getBuffer(FRAME), light, overlay);
        if (mirror.hasGlassVisitor()) {
            pose.translate(0, 0, -0.005);
            glass.render(pose, buffers.getBuffer(GLASS), light, overlay);
        }
        pose.popPose();
    }
}
