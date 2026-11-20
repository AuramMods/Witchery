package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.CoffinBlock;
import art.arcane.witchery.block.entity.CoffinBlockEntity;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BedPart;

public final class CoffinRenderer implements BlockEntityRenderer<CoffinBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "coffin"), "main");
    private static final RenderType TEXTURE = RenderType.entityCutout(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/blocks/coffin.png"));
    private final ModelPart head;
    private final ModelPart foot;

    public CoffinRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(LAYER);
        head = root.getChild("head");
        foot = root.getChild("foot");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        addHalf(mesh.getRoot(), "head", false);
        addHalf(mesh.getRoot(), "foot", true);
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static void addHalf(PartDefinition root, String name, boolean foot) {
        float seam = foot ? 1 : 0;
        PartDefinition half = root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
        var lid = half.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(60, 0).addBox(-1, 0, -8, 16, 1, 16),
                PartPose.offset(-7, -5, 0));
        lid.addOrReplaceChild("top", CubeListBuilder.create().texOffs(67, 35).addBox(1, -2, -8, 12, 1, 14),
                PartPose.offset(0, 0, seam * 2));
        lid.addOrReplaceChild("middle", CubeListBuilder.create().texOffs(64, 18).addBox(0, -1, -8, 14, 1, 15),
                PartPose.offset(0, 0, seam));
        half.addOrReplaceChild("end", CubeListBuilder.create().texOffs(33, 51).addBox(-6, 0, 6, 12, 7, 1),
                PartPose.offsetAndRotation(0, -4, 0, 0, foot ? (float) Math.PI : 0, 0));
        half.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 37).addBox(-7, 0, -8, 1, 7, 15),
                PartPose.offset(0, -4, seam));
        half.addOrReplaceChild("left", CubeListBuilder.create().mirror().texOffs(0, 37).addBox(6, 0, -8, 1, 7, 15),
                PartPose.offset(0, -4, seam));
        half.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 20).addBox(-8, 0, -8, 16, 1, 16),
                PartPose.offset(0, 8, 0));
        half.addOrReplaceChild("lining", CubeListBuilder.create().texOffs(0, 0).addBox(-7, 0, -8, 14, 5, 15),
                PartPose.offset(0, 3, seam));
    }

    @Override
    public void render(CoffinBlockEntity coffin, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var state = coffin.getBlockState();
        ModelPart half = state.getValue(CoffinBlock.PART) == BedPart.HEAD ? head : foot;
        float closed = 1 - coffin.openness(partialTick);
        half.getChild("lid").zRot = -(1 - closed * closed * closed) * (float) (Math.PI / 2);
        pose.pushPose();
        pose.translate(0.5, 0.6, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(state.getValue(CoffinBlock.FACING).toYRot()));
        half.render(pose, buffers.getBuffer(TEXTURE), light, overlay);
        pose.popPose();
    }
}
