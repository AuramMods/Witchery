package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.GrassperBlock;
import art.arcane.witchery.block.entity.GrassperBlockEntity;
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
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class GrassperRenderer implements BlockEntityRenderer<GrassperBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "grassper"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/grassper.png");
    private final ModelPart plant;
    private final ItemRenderer items;

    public GrassperRenderer(BlockEntityRendererProvider.Context context) {
        plant = context.bakeLayer(LAYER);
        items = context.getItemRenderer();
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        float slope = Mth.PI / 6;
        root.addOrReplaceChild("upper_stalk", CubeListBuilder.create().texOffs(0, 4).addBox(-1, -4, -1, 2, 4, 2),
                PartPose.offsetAndRotation(2, 21, 0, 0, 0, -slope));
        root.addOrReplaceChild("lower_stalk", CubeListBuilder.create().texOffs(0, 10).addBox(-1, -4, -1, 2, 4, 2),
                PartPose.offsetAndRotation(0, 24, 0, 0, 0, slope));
        root.addOrReplaceChild("right_leaf", CubeListBuilder.create().texOffs(0, 8).addBox(0, 0, -4, 8, 0, 8),
                PartPose.offsetAndRotation(0, 24, 0, 0, Mth.PI, slope));
        root.addOrReplaceChild("left_leaf", CubeListBuilder.create().texOffs(0, 8).addBox(0, 0, -4, 8, 0, 8),
                PartPose.offsetAndRotation(0, 24, 0, 0, 0, -slope));
        root.addOrReplaceChild("front_leaf", CubeListBuilder.create().texOffs(0, 0).addBox(-4, 0, -8, 8, 0, 8),
                PartPose.offsetAndRotation(0, 24, 0, -slope, 0, 0));
        root.addOrReplaceChild("back_leaf", CubeListBuilder.create().texOffs(0, 0).addBox(-4, 0, -8, 8, 0, 8),
                PartPose.offsetAndRotation(0, 24, 0, -slope, -Mth.PI, 0));
        root.addOrReplaceChild("back_right_petal", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -2, 0, 1, 2, 1),
                PartPose.offsetAndRotation(0, 18, 0, -slope, 0, -Mth.PI / 4));
        root.addOrReplaceChild("front_right_petal", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -2, -1, 1, 2, 1),
                PartPose.offsetAndRotation(0, 18, 0, slope, 0, -Mth.PI / 4));
        root.addOrReplaceChild("back_left_petal", CubeListBuilder.create().texOffs(0, 0).addBox(0, -2, 0, 1, 2, 1),
                PartPose.offsetAndRotation(0, 18, 0, -20 * Mth.DEG_TO_RAD, 0, 15 * Mth.DEG_TO_RAD));
        root.addOrReplaceChild("front_left_petal", CubeListBuilder.create().texOffs(0, 0).addBox(0, -2, -1, 1, 2, 1),
                PartPose.offsetAndRotation(0, 18, 0, 20 * Mth.DEG_TO_RAD, 0, 15 * Mth.DEG_TO_RAD));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(GrassperBlockEntity grassper, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.scale(-1, -1, 1);
        pose.mulPose(Axis.YP.rotationDegrees(grassper.getBlockState().getValue(GrassperBlock.FACING).toYRot() - 180));
        plant.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, overlay);
        pose.popPose();

        ItemStack displayed = grassper.getItem(0);
        if (!displayed.isEmpty()) {
            pose.pushPose();
            pose.translate(0.5, 0.38, 0.5);
            long time = grassper.getLevel() == null ? 0 : grassper.getLevel().getGameTime();
            pose.mulPose(Axis.YP.rotationDegrees(2 * (time % 180 + partialTick)));
            pose.scale(0.75F, 0.75F, 0.75F);
            items.renderStatic(displayed, ItemDisplayContext.GROUND, light, overlay, pose, buffers,
                    grassper.getLevel(), (int) grassper.getBlockPos().asLong());
            pose.popPose();
        }
    }
}
