package art.arcane.witchery.item;

import art.arcane.witchery.block.entity.HuntingTrapBlockEntity;
import art.arcane.witchery.client.renderer.HuntingTrapRenderer;
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

public final class HuntingTrapItem extends BlockItem {
    public HuntingTrapItem(Block block) { super(block, new Properties()); }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() { return DisplayRenderer.INSTANCE; }
        });
    }

    private static final class DisplayRenderer extends BlockEntityWithoutLevelRenderer {
        private static final DisplayRenderer INSTANCE = new DisplayRenderer(Minecraft.getInstance());
        private final HuntingTrapBlockEntity display = new HuntingTrapBlockEntity(BlockPos.ZERO,
                WitcheryBlocks.getRequired("beartrap").get().defaultBlockState());

        private DisplayRenderer(Minecraft minecraft) {
            super(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
            var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(display);
            if (renderer instanceof HuntingTrapRenderer trap) trap.render(display, 0, pose, buffers, light, overlay);
        }
    }
}
