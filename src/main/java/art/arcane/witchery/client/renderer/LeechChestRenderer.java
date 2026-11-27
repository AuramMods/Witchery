package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.LeechChestBlockEntity;
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
import net.minecraft.world.level.block.ChestBlock;

import java.util.List;

public final class LeechChestRenderer implements BlockEntityRenderer<LeechChestBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "leechchest"), "main");
    private static final RenderType TEXTURE = RenderType.entityCutout(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/blocks/leechchest.png"));
    private final ModelPart body;
    private final ModelPart backLeft;
    private final ModelPart backRight;
    private final ModelPart frontLeft;
    private final ModelPart frontRight;
    private final List<ModelPart> sacs;

    public LeechChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(LAYER);
        body = root.getChild("body");
        backLeft = root.getChild("back_left");
        backRight = root.getChild("back_right");
        frontLeft = root.getChild("front_left");
        frontRight = root.getChild("front_right");
        sacs = List.of(root.getChild("sac_one"), root.getChild("sac_two"), root.getChild("sac_three"));
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0, 0, 0, 14, 9, 14),
                PartPose.offset(1, 7, 1));
        root.addOrReplaceChild("back_left", CubeListBuilder.create().mirror().texOffs(28, 24).addBox(-6, -5, -6, 6, 5, 6),
                PartPose.offset(14, 7, 14));
        root.addOrReplaceChild("back_right", CubeListBuilder.create().mirror().texOffs(0, 24).addBox(0, -5, -6, 6, 5, 6),
                PartPose.offset(2, 7, 14));
        root.addOrReplaceChild("front_left", CubeListBuilder.create().mirror().texOffs(28, 36).addBox(-6, -5, 0, 6, 5, 6),
                PartPose.offset(14, 7, 2));
        root.addOrReplaceChild("front_right", CubeListBuilder.create().mirror().texOffs(0, 36).addBox(0, -5, 0, 6, 5, 6),
                PartPose.offset(2, 7, 2));
        root.addOrReplaceChild("sac_one", CubeListBuilder.create().mirror().texOffs(0, 8).addBox(0, 0, 0, 2, 3, 1),
                PartPose.offset(3, 8, 0));
        root.addOrReplaceChild("sac_two", CubeListBuilder.create().mirror().texOffs(0, 3).addBox(0, 0, 0, 3, 2, 1),
                PartPose.offset(9, 13, 0));
        root.addOrReplaceChild("sac_three", CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0, 0, 0, 2, 1, 1),
                PartPose.offset(9, 9, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(LeechChestBlockEntity chest, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var state = chest.getBlockState();
        renderModel(state.getValue(ChestBlock.FACING), chest.getOpenNess(partialTick), chest.renderedCount(), pose, buffers, light, overlay);
    }

    public void renderModel(Direction direction, float openness, int sampleCount, PoseStack pose,
                            MultiBufferSource buffers, int light, int overlay) {
        float closed = 1 - openness;
        float swing = (1 - closed * closed * closed) * (float) (Math.PI / 3);
        backLeft.setRotation(-swing, swing / 2, swing);
        backRight.setRotation(-swing, -swing / 2, -swing);
        frontLeft.setRotation(swing, -swing / 2, swing);
        frontRight.setRotation(swing, swing / 2, -swing);

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
        pose.scale(1, -1, -1);
        pose.translate(-0.5, -0.5, -0.5);
        var vertices = buffers.getBuffer(TEXTURE);
        body.render(pose, vertices, light, overlay);
        backLeft.render(pose, vertices, light, overlay);
        backRight.render(pose, vertices, light, overlay);
        frontLeft.render(pose, vertices, light, overlay);
        frontRight.render(pose, vertices, light, overlay);
        for (int i = 0; i < Math.min(sampleCount, sacs.size()); i++) sacs.get(i).render(pose, vertices, light, overlay);
        pose.popPose();
    }
}
