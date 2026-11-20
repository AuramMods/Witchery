package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public final class BrewGasRenderer implements BlockEntityRenderer<BrewFluidBlockEntity> {
    private static final ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "block/brewgas");
    private static final RenderType SURFACE = RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);
    private static final float FACE_HEIGHT = 0.499F;

    public BrewGasRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(BrewFluidBlockEntity fluid, float partialTick, PoseStack matrices,
                       MultiBufferSource buffers, int light, int overlay) {
        if (!(fluid.getBlockState().getBlock() instanceof BrewDispersalBlock block) || !block.isGas()) return;
        var level = fluid.getLevel();
        if (level == null) return;
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(SPRITE);
        VertexConsumer vertices = buffers.getBuffer(SURFACE);
        int color = fluid.color();
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        for (Direction face : Direction.values()) {
            int alpha = level.getBlockState(fluid.getBlockPos().relative(face)).is(block) ? 51 : 102;
            matrices.pushPose();
            matrices.translate(0.5, 0.5, 0.5);
            matrices.mulPose(face.getRotation());
            PoseStack.Pose pose = matrices.last();
            vertex(pose, vertices, -0.5F, 0.5F, sprite.getU0(), sprite.getV1(), red, green, blue, alpha, light, overlay);
            vertex(pose, vertices, 0.5F, 0.5F, sprite.getU1(), sprite.getV1(), red, green, blue, alpha, light, overlay);
            vertex(pose, vertices, 0.5F, -0.5F, sprite.getU1(), sprite.getV0(), red, green, blue, alpha, light, overlay);
            vertex(pose, vertices, -0.5F, -0.5F, sprite.getU0(), sprite.getV0(), red, green, blue, alpha, light, overlay);
            matrices.popPose();
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, float x, float z, float u, float v,
                               int red, int green, int blue, int alpha, int light, int overlay) {
        vertices.vertex(pose.pose(), x, FACE_HEIGHT, z).color(red, green, blue, alpha)
                .uv(u, v).overlayCoords(overlay).uv2(light).normal(pose.normal(), 0, 1, 0).endVertex();
    }
}
