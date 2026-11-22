package art.arcane.witchery.mixin;

import art.arcane.witchery.integration.ThaumaturgeCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlacedFeature.class)
abstract class ThaumaturgePlacedFeatureMixin {
    @Inject(method = "placeWithBiomeCheck", at = @At("HEAD"), cancellable = true)
    private void witchery$excludeThaumaturgeFeatures(WorldGenLevel level, ChunkGenerator generator,
            RandomSource random, BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        if (!ThaumaturgeCompat.excludesGeneration(level)) return;
        var features = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        if (ThaumaturgeCompat.isThaumaturgeContent(features.getKey((PlacedFeature) (Object) this))) {
            callback.setReturnValue(false);
        }
    }
}
