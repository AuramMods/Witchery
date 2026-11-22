package art.arcane.witchery.mixin;

import art.arcane.witchery.entity.Ent;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.Set;

@Pseudo
@Mixin(targets = "me.adda.enhanced_falling_trees.entity.TreeEntity", remap = false)
abstract class EnhancedFallingTreesHarvestMixin {
    // Felling removes the collected blocks directly. Roll just before each log is removed.
    @Redirect(method = "destroyTree", at = @At(value = "INVOKE",
            target = "Ljava/util/Iterator;next()Ljava/lang/Object;", ordinal = 0))
    private static Object witchery$checkEntAwakening(Iterator<BlockPos> blocks,
                                                    Set<BlockPos> collected, BlockPos origin, LevelAccessor level,
                                                    @Coerce Object treeType, Player player) {
        BlockPos pos = blocks.next();
        if (level instanceof ServerLevel server && !player.isCreative()) {
            var log = WitcheryBlocks.getRequired("witchlog").get();
            if (server.getBlockState(pos).is(log)) {
                Ent.tryAwaken(server, pos, log);
            }
        }
        return pos;
    }
}
