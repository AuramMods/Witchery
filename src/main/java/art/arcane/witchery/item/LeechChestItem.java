package art.arcane.witchery.item;

import art.arcane.witchery.block.entity.LeechChestBlockEntity;
import art.arcane.witchery.block.entity.LeechSamples;
import art.arcane.witchery.client.renderer.LeechChestRenderer;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class LeechChestItem extends BlockItem {
    public LeechChestItem(Block block) {
        super(block, new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (String name : LeechSamples.names(stack.getTag())) tooltip.add(Component.literal(name).withStyle(ChatFormatting.GRAY));
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
        private final LeechChestBlockEntity display = new LeechChestBlockEntity(BlockPos.ZERO,
                WitcheryBlocks.getRequired("leechchest").get().defaultBlockState());

        private DisplayRenderer(Minecraft minecraft) {
            super(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
            var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(display);
            if (renderer instanceof LeechChestRenderer chest) {
                chest.renderModel(Direction.SOUTH, 0, LeechSamples.names(stack.getTag()).size(), pose, buffers, light, overlay);
            }
        }
    }
}
