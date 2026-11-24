package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.KettleBlock;
import art.arcane.witchery.block.entity.KettleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.world.level.Level;

import java.util.Set;

public final class KettleRenderer implements BlockEntityRenderer<KettleBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "kettle"), "main");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/kettle.png"));
    private final ModelPart vessel;
    private final ModelPart suspension;
    private final ModelPart beam;
    private final ModelPart leftBottle;
    private final ModelPart rightBottle;
    private final ModelPart[] liquid = new ModelPart[4];

    public KettleRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(LAYER);
        vessel = root.getChild("vessel");
        suspension = vessel.getChild("suspension");
        beam = suspension.getChild("beam");
        leftBottle = suspension.getChild("bottle_left");
        rightBottle = suspension.getChild("bottle_right");
        for (int frame = 0; frame < liquid.length; frame++) {
            liquid[frame] = root.getChild("liquid_" + frame);
        }
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition vessel = root.addOrReplaceChild("vessel", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5, 18, -5, 9, 6, 1)
                .texOffs(0, 0).addBox(-4, 18, 4, 9, 6, 1)
                .texOffs(13, 0).addBox(-4, 23, -4, 8, 1, 8), PartPose.ZERO);
        vessel.addOrReplaceChild("wall_left", CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 9, 6, 1),
                PartPose.offsetAndRotation(-5, 18, 5, 0, 1.570796F, 0));
        vessel.addOrReplaceChild("wall_right", CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 9, 6, 1),
                PartPose.offsetAndRotation(4, 18, 4, 0, 1.570796F, 0));
        PartDefinition suspension = vessel.addOrReplaceChild("suspension", CubeListBuilder.create(), PartPose.ZERO);
        suspension.addOrReplaceChild("beam", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-12, 8.05F, -1, 24, 2, 2), PartPose.ZERO);
        chain(suspension, "chain_left_front", -2.75F, -1.1F);
        chain(suspension, "chain_left_back", 0.4F, 1.1F);
        chain(suspension, "chain_right_front", 0.4F, 2.05F);
        chain(suspension, "chain_right_back", -0.4F, 1.1F);
        bottle(suspension, "bottle_left", -4);
        bottle(suspension, "bottle_right", 0);
        for (int frame = 0; frame < 4; frame++) {
            root.addOrReplaceChild("liquid_" + frame, CubeListBuilder.create().texOffs(frame * 16 - 8, 16)
                    .addBox(-4, 20, -4, 8, 0, 8, Set.of(Direction.DOWN)), PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void chain(PartDefinition parent, String name, float yaw, float roll) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, 15).addBox(0, -0.5F, 0, 11, 1, 0),
                PartPose.offsetAndRotation(0, 9, 0, 0, yaw, roll));
    }

    private static void bottle(PartDefinition parent, String name, int x) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().mirror()
                .texOffs(52, 5).addBox(0, 2, 0, 3, 3, 3)
                .texOffs(60, 3).addBox(1, 1, 1, 1, 1, 1)
                .texOffs(56, 0).addBox(0.5F, 0, 0.5F, 2, 1, 2), PartPose.offset(x, 13, -6));
    }

    @Override
    public void render(KettleBlockEntity kettle, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Level level = kettle.getLevel();
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(kettle.getBlockState().getValue(KettleBlock.FACING).toYRot() - 180));
        suspension.visible = level != null;
        if (level != null) {
            beam.visible = !level.getBlockState(kettle.getBlockPos().above()).isSolid();
            int bottles = kettle.bottleCount();
            leftBottle.visible = bottles > 0;
            rightBottle.visible = bottles > 1;
        }
        VertexConsumer vertices = buffers.getBuffer(RENDER_TYPE);
        vessel.render(pose, vertices, light, overlay);
        if (level != null && kettle.isFull()) {
            ModelPart frame = liquid[(int) (level.getGameTime() / 5 % liquid.length)];
            int color = kettle.liquidColor();
            frame.render(pose, vertices, light, overlay, (color >> 16 & 255) / 255F,
                    (color >> 8 & 255) / 255F, (color & 255) / 255F, 1);
        }
        pose.popPose();
    }
}
