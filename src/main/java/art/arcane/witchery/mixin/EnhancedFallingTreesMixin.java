package art.arcane.witchery.mixin;

import art.arcane.witchery.block.WitchLeavesBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.config.WitcheryCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Queue;
import java.util.Set;

@Pseudo
@Mixin(targets = "me.adda.enhanced_falling_trees.trees.DefaultTree", remap = false)
abstract class EnhancedFallingTreesMixin {
    @Shadow
    public abstract boolean extraRequiredBlockCheck(BlockState state);

    @Inject(method = "isMatchingLog", at = @At("HEAD"), cancellable = true)
    private void witchery$keepTrunkSpeciesSeparate(BlockState candidate, BlockState trunk,
                                                   CallbackInfoReturnable<Boolean> result) {
        if (WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get() && witchery$differentWood(candidate, trunk)) {
            result.setReturnValue(false);
        }
    }

    @Redirect(method = "initializeLeafSearch", at = @At(value = "INVOKE",
            target = "Lme/adda/enhanced_falling_trees/trees/DefaultTree;extraRequiredBlockCheck(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean witchery$seedCanopyForTrunk(@Coerce Object tree, BlockState candidate,
                                              Set<BlockPos> logs, LevelAccessor level, Queue<?> leafQueue) {
        if (!extraRequiredBlockCheck(candidate)) {
            return false;
        }
        if (!WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get()) return true;
        BlockState trunk = level.getBlockState(logs.iterator().next());
        boolean witchwood = trunk.hasProperty(WitchWoodType.PROPERTY);
        // The first accepted seed sets the leaf species for the rest of the canopy search.
        return witchwood == (candidate.getBlock() instanceof WitchLeavesBlock)
                && !witchery$differentWood(candidate, trunk);
    }

    @Inject(method = "isValidLeaf", at = @At("HEAD"), cancellable = true)
    private void witchery$stayInSameCanopy(BlockState candidate, int distance, BlockState seed,
                                          CallbackInfoReturnable<Boolean> result) {
        if (WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get() && witchery$differentWood(candidate, seed)) {
            result.setReturnValue(false);
        }
    }

    @Unique
    private static boolean witchery$differentWood(BlockState first, BlockState second) {
        return first.hasProperty(WitchWoodType.PROPERTY) && second.hasProperty(WitchWoodType.PROPERTY)
                && first.getValue(WitchWoodType.PROPERTY) != second.getValue(WitchWoodType.PROPERTY);
    }
}
