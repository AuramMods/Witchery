package art.arcane.witchery.item;

import art.arcane.witchery.block.AreaWardBlock;
import art.arcane.witchery.block.entity.AreaWardBlockEntity;
import art.arcane.witchery.client.renderer.AreaWardRenderer;
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

public final class AreaWardItem extends BlockItem {
    public AreaWardItem(Block block) {
        super(block, new Properties());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() { return DisplayRenderer.INSTANCE; }
        });
    }

    private static final class DisplayRenderer extends BlockEntityWithoutLevelRenderer {
        private static final DisplayRenderer INSTANCE = new DisplayRenderer(Minecraft.getInstance());
        private final AreaWardBlockEntity wolf = display("decursedirected");
        private final AreaWardBlockEntity mandrake = display("decurseteleport");

        private DisplayRenderer(Minecraft minecraft) {
            super(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        }

        private static AreaWardBlockEntity display(String id) {
            var block = (AreaWardBlock) WitcheryBlocks.getRequired(id).get();
            return block.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
            var display = stack.is(wolf.getBlockState().getBlock().asItem()) ? wolf : mandrake;
            var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(display);
            if (renderer instanceof AreaWardRenderer statue) statue.renderItem(pose, buffers, light, overlay);
        }
    }
}
