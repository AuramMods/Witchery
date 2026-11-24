package art.arcane.witchery.item;

import art.arcane.witchery.block.entity.BrazierBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class BrazierItem extends BlockItem {
    public BrazierItem(Block block) {
        super(block, new Properties());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return DisplayRenderer.INSTANCE;
            }
        });
    }

    private static final class DisplayRenderer extends BlockEntityWithoutLevelRenderer {
        private static final DisplayRenderer INSTANCE = new DisplayRenderer(Minecraft.getInstance());
        private final BrazierBlockEntity display = new BrazierBlockEntity(BlockPos.ZERO,
                WitcheryBlocks.getRequired("brazier").get().defaultBlockState());

        private DisplayRenderer(Minecraft minecraft) {
            super(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
            Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(display, pose, buffers, light, overlay);
        }
    }
}
