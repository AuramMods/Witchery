package art.arcane.witchery.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BlockEntityDisplayItem extends BlockItem {
    private final Supplier<? extends BlockEntityType<?>> type;

    public BlockEntityDisplayItem(Block block, Supplier<? extends BlockEntityType<?>> type) {
        super(block, new Properties());
        this.type = type;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    BlockEntity display = type.get().create(BlockPos.ZERO, getBlock().defaultBlockState());
                    renderer = new DisplayRenderer(Minecraft.getInstance(), display);
                }
                return renderer;
            }
        });
    }

    private static final class DisplayRenderer extends BlockEntityWithoutLevelRenderer {
        private final BlockEntity display;

        private DisplayRenderer(Minecraft minecraft, BlockEntity display) {
            super(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
            this.display = display;
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
            Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(display, pose, buffers, light, overlay);
        }
    }
}
