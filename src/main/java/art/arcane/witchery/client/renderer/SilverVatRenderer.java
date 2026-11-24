package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.SilverVatBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.inventory.InventoryMenu;

import java.util.EnumMap;
import java.util.Map;

public final class SilverVatRenderer implements BlockEntityRenderer<SilverVatBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "silvervat"), "main");
    private static final RenderType PARTS = RenderType.entityCutout(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/silvervat.png"));
    private static final ResourceLocation LIQUID = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "block/brew_still");
    private static final RenderType SURFACE = RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);
    private static final float NEAR_EDGE = 2 / 16F;
    private static final float FAR_EDGE = 14 / 16F;
    private final ModelPart body;
    private final Map<Direction, ModelPart> spouts = new EnumMap<>(Direction.class);
    private final ModelPart[] silver = new ModelPart[8];

    public SilverVatRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(LAYER);
        body = root.getChild("body");
        for (Direction side : Direction.Plane.HORIZONTAL) {
            spouts.put(side, root.getChild(side.getName()));
        }
        for (int index = 0; index < silver.length; index++) silver[index] = root.getChild("silver_" + index);
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-6, 23, -6, 12, 1, 12)
                .texOffs(38, 10).addBox(-7, 16, -6, 1, 8, 12)
                .texOffs(38, 10).addBox(6, 16, -6, 1, 8, 12)
                .texOffs(34, 0).addBox(-7, 16, -7, 14, 8, 1)
                .texOffs(34, 0).addBox(-7, 16, 6, 14, 8, 1), PartPose.ZERO);
        root.addOrReplaceChild("north", CubeListBuilder.create()
                .texOffs(15, 9).addBox(-1.5F, 14, -8, 3, 2, 4)
                .texOffs(15, 0).addBox(-0.5F, 16, -5.2F, 1, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("south", CubeListBuilder.create()
                .texOffs(15, 9).addBox(-1.5F, 14, 4, 3, 2, 4)
                .texOffs(15, 0).addBox(-0.5F, 16, 4.2F, 1, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("east", CubeListBuilder.create()
                .texOffs(15, 3).addBox(-8, 14, -1.5F, 4, 2, 3)
                .texOffs(15, 0).addBox(-5.2F, 16, -0.5F, 1, 1, 1), PartPose.ZERO);
        root.addOrReplaceChild("west", CubeListBuilder.create()
                .texOffs(15, 3).addBox(4, 14, -1.5F, 4, 2, 3)
                .texOffs(15, 0).addBox(4.2F, 16, -0.5F, 1, 1, 1), PartPose.ZERO);
        float[][] positions = {
                {-2.2F, 19.3F, -3.9F, 1, 1}, {1.6F, 19, -2.1F, 2, 1},
                {-3.8F, 19.1F, 3.1F, 1, 2}, {-3.4F, 18.8F, 0.9F, 1, 1},
                {1.6F, 19, -0.1F, 1, 1}, {-4.6F, 19.1F, -1.6F, 2, 1},
                {-0.5F, 19, 2, 1, 1}, {-1.2F, 19, -0.3F, 1, 1}
        };
        int[] textureRows = {3, 6, 9, 13, 16, 19, 22, 25};
        for (int index = 0; index < positions.length; index++) {
            float[] box = positions[index];
            root.addOrReplaceChild("silver_" + index, CubeListBuilder.create().texOffs(0, textureRows[index])
                    .addBox(box[0], box[1], box[2], box[3], 1, box[4]), PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(SilverVatBlockEntity vat, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
        var level = vat.getLevel();
        var parts = buffers.getBuffer(PARTS);
        stack.pushPose();
        stack.translate(0.5, 1.5, 0.5);
        stack.mulPose(Axis.ZP.rotationDegrees(180));
        body.render(stack, parts, light, overlay);
        for (var spout : spouts.entrySet()) {
            if (level == null || level.getBlockState(vat.getBlockPos().relative(spout.getKey())).hasBlockEntity()) {
                spout.getValue().render(stack, parts, light, overlay);
            }
        }
        int dust = vat.getItem(0).getCount();
        int displayed = dust == 0 ? 0 : Math.max(1, Math.min(silver.length, dust / 8));
        for (int index = 0; index < displayed; index++) silver[index].render(stack, parts, light, overlay);
        stack.popPose();

        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(LIQUID);
        var pose = stack.last();
        var vertices = buffers.getBuffer(SURFACE);
        vertex(pose, vertices, FAR_EDGE, NEAR_EDGE, sprite.getU0(), sprite.getV1(), light, overlay);
        vertex(pose, vertices, NEAR_EDGE, NEAR_EDGE, sprite.getU1(), sprite.getV1(), light, overlay);
        vertex(pose, vertices, NEAR_EDGE, FAR_EDGE, sprite.getU1(), sprite.getV0(), light, overlay);
        vertex(pose, vertices, FAR_EDGE, FAR_EDGE, sprite.getU0(), sprite.getV0(), light, overlay);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, float x, float z, float u, float v, int light, int overlay) {
        vertices.vertex(pose.pose(), x, 0.30F, z).color(52 / 256F, 95 / 256F, 218 / 256F, 1)
                .uv(u, v).overlayCoords(overlay).uv2(light).normal(pose.normal(), 0, 1, 0).endVertex();
    }
}
