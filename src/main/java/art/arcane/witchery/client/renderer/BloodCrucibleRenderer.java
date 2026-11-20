package art.arcane.witchery.client.renderer;

import art.arcane.witchery.block.entity.BloodCrucibleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public final class BloodCrucibleRenderer implements BlockEntityRenderer<BloodCrucibleBlockEntity> {
    private static final ResourceLocation LIQUID = ResourceLocation.withDefaultNamespace("block/lava_still");
    private static final RenderType SURFACE = RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);
    private static final float NEAR_EDGE = 5 / 16F;
    private static final float FAR_EDGE = 11 / 16F;

    public BloodCrucibleRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(BloodCrucibleBlockEntity crucible, float partialTick, PoseStack stack,
                       MultiBufferSource buffers, int light, int overlay) {
        float fullness = crucible.fullness();
        if (fullness == 0) return;
        float surfaceY = 0.10F + 0.20F * fullness;
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(LIQUID);
        var pose = stack.last();
        var vertices = buffers.getBuffer(SURFACE);
        vertex(pose, vertices, NEAR_EDGE, surfaceY, FAR_EDGE, sprite.getU0(), sprite.getV1(), light, overlay);
        vertex(pose, vertices, FAR_EDGE, surfaceY, FAR_EDGE, sprite.getU1(), sprite.getV1(), light, overlay);
        vertex(pose, vertices, FAR_EDGE, surfaceY, NEAR_EDGE, sprite.getU1(), sprite.getV0(), light, overlay);
        vertex(pose, vertices, NEAR_EDGE, surfaceY, NEAR_EDGE, sprite.getU0(), sprite.getV0(), light, overlay);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, float x, float y, float z,
                               float u, float v, int light, int overlay) {
        vertices.vertex(pose.pose(), x, y, z).color(255, 0, 0, 230)
                .uv(u, v).overlayCoords(overlay).uv2(light).normal(pose.normal(), 0, 1, 0).endVertex();
    }
}
