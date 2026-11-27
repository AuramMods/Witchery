package art.arcane.witchery.mixin;

import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.client.renderer.BrewLiquidRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlockRenderer.class)
abstract class BrewLiquidRenderMixin {
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    private void witchery$drawBrewLiquid(BlockAndTintGetter level, BlockPos pos, VertexConsumer vertices,
                                         BlockState state, FluidState fluid, CallbackInfo callback) {
        if (state.getBlock() instanceof BrewDispersalBlock block && !block.isGas()) {
            BrewLiquidRenderer.render(level, pos, vertices, state, fluid, block);
            callback.cancel();
        }
    }
}
