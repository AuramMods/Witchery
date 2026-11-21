package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.FetishBlock;
import art.arcane.witchery.block.entity.FetishBlockEntity;
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
import net.minecraft.world.item.DyeColor;

public final class FetishRenderer implements BlockEntityRenderer<FetishBlockEntity> {
    public static final ModelLayerLocation SCARECROW = layerId("scarecrow");
    public static final ModelLayerLocation TRENT = layerId("trent");
    public static final ModelLayerLocation LADDER = layerId("witchsladder");

    private final ResourceLocation texture;
    private final ModelPart plain;
    private final ModelPart dyed;

    public FetishRenderer(BlockEntityRendererProvider.Context context, ModelLayerLocation layer) {
        texture = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/" + layer.getModel().getPath() + ".png");
        ModelPart model = context.bakeLayer(layer);
        plain = model.getChild("plain");
        dyed = model.getChild("dyed");
    }

    private static ModelLayerLocation layerId(String path) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, path), "main");
    }

    public static LayerDefinition scarecrowLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition plain = group(mesh, "plain");
        part(plain, "pole", 0, 2, -1, 0, -1, 2, 15, 2, PartPose.offset(0, 9, 0));
        part(plain, "crossbar", 0, 0, -8, 0, -0.5F, 16, 1, 1, PartPose.offset(0, 13, 0));
        part(plain, "face", 29, 25, -2, -4, -1.9F, 4, 5, 0, PartPose.offset(0, 12, 0));
        part(plain, "left_straw", 29, 25, 2.9F, -0.5F, -1.5F, 0, 4, 3, PartPose.offset(3, 13, 0));
        part(plain, "right_straw", 29, 25, -2.9F, -0.5F, -1.5F, 0, 4, 3, PartPose.offset(-3, 13, 0));
        PartDefinition dyed = group(mesh, "dyed");
        part(dyed, "head", 12, 21, -2, -4, -2, 4, 5, 4, PartPose.offset(0, 12, 0));
        part(dyed, "body", 8, 2, -3, 0, -1.5F, 6, 9, 3, PartPose.offset(0, 12.5F, 0));
        part(dyed, "left_arm", 0, 23, 0, -0.5F, -1.5F, 3, 4, 3, PartPose.offset(3, 13, 0));
        part(dyed, "right_arm", 0, 23, -3, -0.5F, -1.5F, 3, 4, 3, PartPose.offset(-3, 13, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition trentLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition plain = group(mesh, "plain");
        part(plain, "body", 0, 14, -3, 0, -3, 6, 9, 6, PartPose.offset(0, 12, 0));
        part(plain, "left_arm", 0, 0, -1, 0, -1, 2, 5, 2, PartPose.offsetAndRotation(2, 13, 0, -0.186F, 0, -0.744F));
        part(plain, "right_arm", 0, 0, -1, 0, -1, 2, 5, 2, PartPose.offsetAndRotation(-2, 13, 0, -0.186F, 0, 0.855F));
        part(plain, "left_thigh", 9, 0, -1, 0, -1, 2, 5, 2, PartPose.offsetAndRotation(2, 18, 0, -0.149F, 0, -0.26F));
        part(plain, "right_thigh", 9, 0, -1, 0, -1, 2, 5, 2, PartPose.offsetAndRotation(-2, 18, 0, 0.186F, 0, 0.335F));
        part(plain, "left_foot", 11, 8, -0.5F, 0, -0.5F, 1, 3, 1, PartPose.offsetAndRotation(3, 21, -0.5F, 0.074F, 0, -0.112F));
        part(plain, "right_foot", 11, 8, -0.5F, 0, -0.5F, 1, 3, 1, PartPose.offsetAndRotation(-3, 21, 0.5F, 0.186F, 0, 0.223F));
        part(plain, "top_branch", 0, 30, -1, 0, -1, 2, 5, 2, PartPose.offsetAndRotation(0, 13, 1, 0.112F, 0, -2.863F));
        part(plain, "left_branch", 0, 30, -1, 0, -1, 2, 5, 2, PartPose.offsetAndRotation(-1, 13, 0, 0.372F, 0, 2.64F));
        part(plain, "right_branch", 0, 30, -1, 0, -1, 2, 5, 2, PartPose.offsetAndRotation(-1, 13, 0, -0.446F, 0, 2.863F));
        part(group(mesh, "dyed"), "face", 18, 1, -3, 1, -2.9F, 6, 7, 0, PartPose.offset(0, 12, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition ladderLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition plain = group(mesh, "plain");
        part(plain, "diagonal", 0, 0, -8, 0, 0, 16, 16, 0, PartPose.offsetAndRotation(0, 8, 0, 0, 0.7853982F, 0));
        part(plain, "other_diagonal", 0, 0, -8, 0, 0, 16, 16, 0, PartPose.offsetAndRotation(0, 8, 0, 0, -0.7853982F, 0));
        group(mesh, "dyed");
        return LayerDefinition.create(mesh, 16, 16);
    }

    private static PartDefinition group(MeshDefinition mesh, String name) {
        return mesh.getRoot().addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
    }

    private static void part(PartDefinition group, String name, int u, int v, float x, float y, float z,
                             int width, int height, int depth, PartPose pose) {
        group.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).mirror().addBox(x, y, z, width, height, depth), pose);
    }

    @Override
    public void render(FetishBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        render(entity.getBlockState().getValue(FetishBlock.FACING), entity.colorIndex(), entity.isSpectral(),
                pose, buffers, light, overlay);
    }

    public void render(Direction facing, int color, boolean spectral, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        var vertices = buffers.getBuffer(spectral ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture));
        float alpha = spectral ? 0.65F : 1;
        float[] tint = DyeColor.byId(color).getTextureDiffuseColors();
        plain.render(pose, vertices, light, overlay, 1, 1, 1, alpha);
        dyed.render(pose, vertices, light, overlay, tint[0], tint[1], tint[2], alpha);
        pose.popPose();
    }
}
