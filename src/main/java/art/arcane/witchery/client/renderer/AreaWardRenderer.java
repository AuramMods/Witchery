package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.AreaWardBlock;
import art.arcane.witchery.block.entity.AreaWardBlockEntity;
import art.arcane.witchery.client.model.MandrakeModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class AreaWardRenderer implements BlockEntityRenderer<AreaWardBlockEntity> {
    public static final ModelLayerLocation WOLF_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "statuewolf"), "main");
    private static final ResourceLocation WOLF_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/block/statuewolf.png");
    private static final ResourceLocation MANDRAKE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/block/statuemandrake.png");
    private final ModelPart model;
    private final RenderType renderType;
    private final double footOffset;

    public AreaWardRenderer(BlockEntityRendererProvider.Context context, AreaWardBlockEntity.Kind kind) {
        boolean wolf = kind == AreaWardBlockEntity.Kind.DIRECTED;
        model = context.bakeLayer(wolf ? WOLF_LAYER : MandrakeModel.LAYER);
        renderType = RenderType.entityCutoutNoCull(wolf ? WOLF_TEXTURE : MANDRAKE_TEXTURE);
        footOffset = wolf ? 0.01 : 0.005;
    }

    public static LayerDefinition wolfLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3, -3, -2, 6, 6, 4)
                .texOffs(16, 14).addBox(-3, -5, 0, 2, 2, 1)
                .texOffs(16, 14).addBox(1, -5, 0, 2, 2, 1)
                .texOffs(0, 10).addBox(-1.5F, 0, -5, 3, 3, 4), PartPose.offset(-0.5F, 13.5F, -7));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 14)
                .addBox(-4, -2, -3, 6, 9, 6), PartPose.offsetAndRotation(0.5F, 14, 2, Mth.HALF_PI, 0, 0));
        root.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(21, 0)
                .addBox(-4, -3, -3, 8, 6, 7), PartPose.offsetAndRotation(-0.5F, 14, -3, Mth.HALF_PI, 0, 0));
        var leg = CubeListBuilder.create().texOffs(0, 18).addBox(-1, 0, -1, 2, 7, 2);
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-2, 16, 7));
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(1, 16, 7));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-2, 16, -4));
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(1, 16, -4));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(9, 18)
                .addBox(-1, 0, -1, 2, 8, 2), PartPose.offsetAndRotation(-0.5F, 12, 8, 1.130069F, 0, 0));
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(22, 18)
                .addBox(0, 0, 0, 8, 1, 13), PartPose.offset(-4.5F, 23, -5));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(AreaWardBlockEntity ward, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        draw(ward.getBlockState().getValue(AreaWardBlock.FACING), 0.5F, pose, buffers, light, overlay);
    }

    public void renderItem(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        draw(Direction.SOUTH, 0.3F, pose, buffers, light, overlay);
    }

    private void draw(Direction facing, float scale, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 1.5 * scale + footOffset, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(facing.toYRot() - 180));
        pose.scale(scale, scale, scale);
        model.render(pose, buffers.getBuffer(renderType), light, overlay);
        pose.popPose();
    }
}
