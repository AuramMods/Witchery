package art.arcane.witchery.item;

import art.arcane.witchery.block.FetishBlock;
import art.arcane.witchery.block.entity.FetishBlockEntity;
import art.arcane.witchery.block.entity.FetishEffect;
import art.arcane.witchery.client.renderer.FetishRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class FetishItem extends BlockItem {
    public FetishItem(Block block) {
        super(block, new Properties());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new DisplayRenderer(Minecraft.getInstance(), getBlock());
                return renderer;
            }
        });
    }

    @Override
    public Component getName(ItemStack stack) {
        FetishEffect effect = FetishEffect.byId(FetishBlockEntity.itemEffect(stack));
        if (effect == null) return super.getName(stack);
        return Component.translatable("item.witchery.fetish.bound", super.getName(stack),
                Component.translatable("effect.witchery.fetish." + effect.key));
    }

    private static final class DisplayRenderer extends BlockEntityWithoutLevelRenderer {
        private final FetishBlockEntity display;

        private DisplayRenderer(Minecraft minecraft, Block block) {
            super(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
            display = ((FetishBlock) block).newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
            if (Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(display) instanceof FetishRenderer renderer) {
                renderer.render(Direction.NORTH, FetishBlockEntity.itemColor(stack), false, pose, buffers, light, overlay);
            }
        }
    }
}
