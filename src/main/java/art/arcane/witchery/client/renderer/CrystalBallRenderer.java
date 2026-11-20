package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.CrystalBallBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
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

public final class CrystalBallRenderer implements BlockEntityRenderer<CrystalBallBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "crystalball"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/crystalball.png");
    private final ModelPart base;
    private final ModelPart inner;
    private final ModelPart middle;
    private final ModelPart outer;

    public CrystalBallRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(LAYER);
        base = root.getChild("base");
        inner = root.getChild("inner");
        middle = root.getChild("middle");
        outer = root.getChild("outer");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("base", CubeListBuilder.create().mirror()
                .texOffs(0, 25).addBox(-3, 23, -3, 6, 1, 6)
                .texOffs(0, 20).addBox(-2, 22, -2, 4, 1, 4)
                .texOffs(0, 17).addBox(-1, 21, -1, 2, 1, 2), PartPose.ZERO);
        root.addOrReplaceChild("inner", CubeListBuilder.create().mirror().texOffs(4, 0)
                .addBox(-1, 17, -1, 2, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("middle", CubeListBuilder.create().mirror().texOffs(12, 0)
                .addBox(-2, 16, -2, 4, 4, 4), PartPose.ZERO);
        root.addOrReplaceChild("outer", CubeListBuilder.create().mirror().texOffs(8, 8)
                .addBox(-3, 15, -3, 6, 6, 6), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void render(CrystalBallBlockEntity ball, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.scale(-1, -1, 1);
        base.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, overlay);
        var crystal = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
        long time = ball.getLevel() == null ? 80 : ball.getLevel().getDayTime();
        float brightness = (100 - Math.abs(Math.floorMod(time, 160) - 80)) / 100F;
        inner.render(pose, crystal, light, overlay, brightness, brightness, brightness, 1);
        middle.render(pose, crystal, light, overlay, 0.8F, 0.8F, 1, 1);
        outer.render(pose, crystal, light, overlay, 0.8F, 0.8F, 1, 1);
        pose.popPose();
    }
}
