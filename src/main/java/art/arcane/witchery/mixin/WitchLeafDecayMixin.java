package art.arcane.witchery.mixin;

import art.arcane.witchery.block.WitchLeavesBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
abstract class WitchLeafDecayMixin {
    // Forge calls this entry point after a live write or a committed placement snapshot.
    @Inject(method = "markAndNotifyBlock", at = @At("RETURN"), remap = false)
    private void witchery$checkLeavesAfterRemoval(BlockPos pos, LevelChunk chunk, BlockState previous, BlockState replacement,
                                                  int flags, int recursionLeft, CallbackInfo info) {
        if ((Object) this instanceof ServerLevel level && !level.restoringBlockSnapshots) {
            WitchLeavesBlock.checkAfterRemoval(level, pos, previous, replacement);
        }
    }
}
