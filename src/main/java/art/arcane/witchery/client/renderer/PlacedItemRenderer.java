package art.arcane.witchery.client.renderer;

import art.arcane.witchery.block.PlacedItemBlock;
import art.arcane.witchery.block.entity.PlacedItemBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;

public final class PlacedItemRenderer implements BlockEntityRenderer<PlacedItemBlockEntity> {
    private final ItemRenderer renderer;

    public PlacedItemRenderer(BlockEntityRendererProvider.Context context) {
        renderer = context.getItemRenderer();
    }

    @Override
    public void render(PlacedItemBlockEntity placed, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        if (placed.stack().isEmpty()) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5, 0.05, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(180 - placed.getBlockState().getValue(PlacedItemBlock.FACING).toYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(90));
        // The original laid the centered, half-size dropped sprite just off the center of the mount.
        pose.translate(0, 0.025, 0.00546875);
        pose.scale(0.5F, 0.5F, 0.5F);
        renderer.renderStatic(placed.stack(), ItemDisplayContext.NONE, light, overlay, pose, buffers,
                placed.getLevel(), (int) placed.getBlockPos().asLong());
        pose.popPose();
    }
}
