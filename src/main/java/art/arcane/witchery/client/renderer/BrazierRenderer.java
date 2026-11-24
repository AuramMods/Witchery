package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.BrazierBlockEntity;
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
import net.minecraft.resources.ResourceLocation;

public final class BrazierRenderer implements BlockEntityRenderer<BrazierBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "brazier"), "main");
    private static final RenderType TEXTURE = RenderType.entityCutout(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/brazier.png"));
    private static final float FOOT_TILT = (float) (Math.PI / 4);

    private final ModelPart stand;
    private final ModelPart ash;

    public BrazierRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart model = context.bakeLayer(LAYER);
        stand = model.getChild("stand");
        ash = model.getChild("ash");
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        var stand = root.addOrReplaceChild("stand", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.2F, 10, -1.24F, 1, 11, 1)
                .texOffs(0, 0).addBox(-1.2F, 10, -1.2F, 1, 11, 1)
                .texOffs(0, 0).addBox(-1.2F, 10, 0.2F, 1, 11, 1)
                .texOffs(0, 0).addBox(0.2F, 10, 0.2F, 1, 11, 1)
                .texOffs(5, 12).addBox(2.5F, 9, -3, 1, 1, 6)
                .texOffs(5, 12).addBox(-3.5F, 9, -3, 1, 1, 6)
                .texOffs(4, 26).addBox(-3, 9, 2.5F, 6, 1, 1)
                .texOffs(4, 26).addBox(-3, 9, -3.5F, 6, 1, 1)
                .texOffs(6, 0).addBox(-1.5F, 20.5F, -1.5F, 3, 1, 3)
                .texOffs(6, 5).addBox(-3, 9.95F, -3, 6, 1, 6), PartPose.ZERO);
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) {
                stand.addOrReplaceChild("foot_" + x + "_" + z,
                        CubeListBuilder.create().texOffs(0, 13).addBox(-0.5F, 0, -0.5F, 1, 5, 1),
                        PartPose.offsetAndRotation(x * 0.7F, 21, z * 0.7F, z * FOOT_TILT, 0, -x * FOOT_TILT));
            }
        }
        root.addOrReplaceChild("ash", CubeListBuilder.create().texOffs(0, 20)
                .addBox(-2.5F, 0, -2.5F, 5, 0, 5), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(BrazierBlockEntity brazier, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        var vertices = buffers.getBuffer(TEXTURE);
        stand.render(pose, vertices, light, overlay);
        int offerings = brazier.displayedOfferings();
        if (offerings > 0) {
            pose.translate(0, (9.7F - (offerings - 1) * 0.1F) / 16, 0);
            ash.render(pose, vertices, light, overlay);
        }
        pose.popPose();
    }
}
