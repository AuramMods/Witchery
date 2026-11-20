package art.arcane.witchery.client.renderer;

import art.arcane.witchery.block.entity.PoppetShelfBlockEntity;
import art.arcane.witchery.item.BoundPoppetItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class PoppetShelfRenderer implements BlockEntityRenderer<PoppetShelfBlockEntity> {
    private final ItemRenderer renderer;

    public PoppetShelfRenderer(BlockEntityRendererProvider.Context context) {
        renderer = context.getItemRenderer();
    }

    @Override
    public void render(PoppetShelfBlockEntity shelf, float partialTick, PoseStack pose, MultiBufferSource buffer,
                       int light, int overlay) {
        Level level = shelf.getLevel();
        if (level == null) return;
        boolean rotate = Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST;
        float angle = ((level.getGameTime() % 144L) + partialTick) * 2.5F;
        for (int slot = 0; slot < shelf.getContainerSize(); slot++) {
            ItemStack stack = shelf.getItem(slot);
            if (stack.isEmpty() || !BoundPoppetItem.isPoppet(stack)) continue;
            pose.pushPose();
            pose.translate(0.25 + slot % 3 * 0.25, 0.57, 0.25 + slot / 3 * 0.25);
            if (rotate) pose.mulPose(Axis.YP.rotationDegrees(angle));
            pose.scale(0.55F, 0.55F, 0.55F);
            renderer.renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, pose, buffer, level, slot);
            pose.popPose();
        }
    }
}
