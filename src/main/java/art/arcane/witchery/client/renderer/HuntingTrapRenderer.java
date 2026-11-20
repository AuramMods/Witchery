package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.HuntingTrapBlock;
import art.arcane.witchery.block.entity.HuntingTrapBlockEntity;
import art.arcane.witchery.config.WitcheryClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
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

public final class HuntingTrapRenderer implements BlockEntityRenderer<HuntingTrapBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "beartrap"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/blocks/beartrap.png");
    private final ModelPart root;
    private final ModelPart plate;
    private final ModelPart front;
    private final ModelPart back;

    public HuntingTrapRenderer(BlockEntityRendererProvider.Context context) {
        root = context.bakeLayer(LAYER);
        plate = root.getChild("plate");
        front = root.getChild("front");
        back = root.getChild("back");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 20)
                        .addBox(-5, -1, -0.5F, 10, 1, 1), PartPose.offset(0, 23.99F, 0));
        root.addOrReplaceChild("hinges", CubeListBuilder.create().texOffs(19, 3)
                        .addBox(3.7F, -2, -1, 1, 2, 2).addBox(-4.7F, -2, -1, 1, 2, 2), PartPose.offset(0, 24, 0));
        root.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(1, 0)
                        .addBox(-2, -1.5F, -2, 4, 1, 4), PartPose.offset(0, 23.2F, 0));
        addJaw(root, "front", -7, -6);
        addJaw(root, "back", 6, 0);
        return LayerDefinition.create(mesh, 32, 32);
    }

    private static void addJaw(PartDefinition root, String name, int crossbar, int side) {
        var jaw = CubeListBuilder.create().texOffs(0, 9).addBox(-4.5F, -1, crossbar, 9, 1, 1)
                .texOffs(0, 12).addBox(-4.5F, -1, side, 1, 1, 6).addBox(3.5F, -1, side, 1, 1, 6)
                .texOffs(0, 0);
        for (int tooth = 0; tooth < 5; tooth++) jaw.addBox(-4.5F + 2 * tooth, -2, crossbar, 1, 1, 1);
        root.addOrReplaceChild(name, jaw, PartPose.offset(0, 23.99F, 0));
    }

    @Override
    public void render(HuntingTrapBlockEntity trap, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        boolean inWorld = trap.hasLevel();
        boolean sprung = inWorld && trap.sprung();
        boolean visible = !inWorld || trap.visibleTo(Minecraft.getInstance().player);
        float alpha = visible ? 1 : WitcheryClientConfig.MANTRAP_OPACITY.get().floatValue();
        plate.y = sprung ? 23.8F : 23.2F;
        front.xRot = sprung ? -1.2F : 0;
        back.xRot = sprung ? 1.2F : 0;
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        if (inWorld) pose.mulPose(Axis.YP.rotationDegrees(trap.getBlockState().getValue(HuntingTrapBlock.FACING).toYRot() - 180));
        var renderType = visible ? RenderType.entityCutoutNoCull(TEXTURE) : RenderType.entityTranslucent(TEXTURE);
        root.render(pose, buffers.getBuffer(renderType), light, overlay, 1, 1, 1, alpha);
        pose.popPose();
    }
}
