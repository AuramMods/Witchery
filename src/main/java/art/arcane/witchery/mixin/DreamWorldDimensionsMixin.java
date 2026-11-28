package art.arcane.witchery.mixin;

import art.arcane.witchery.world.DreamChunkGenerator;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldDimensions.class)
abstract class DreamWorldDimensionsMixin {
    @Inject(method = "bake", at = @At("RETURN"), cancellable = true)
    private void witchery$resolveDreamGenerator(Registry<LevelStem> datapackDimensions,
                                                CallbackInfoReturnable<WorldDimensions.Complete> info) {
        var baked = info.getReturnValue();
        var dimensions = baked.dimensions();
        var dream = dimensions.get(WitcheryDimensions.DREAM_STEM);
        if (dream == null || !(dream.generator() instanceof DreamChunkGenerator)) return;

        var original = (WorldDimensions) (Object) this;
        var generator = original.get(WitcheryDimensions.DREAM_STEM)
                .map(LevelStem::generator)
                .filter(saved -> !(saved instanceof DreamChunkGenerator))
                .orElseGet(() -> dimensions.getOrThrow(LevelStem.OVERWORLD).generator());
        var resolved = new LevelStem(dream.type(), generator);

        // The selected terrain is written to level.dat with the rest of the baked dimensions.
        var replacement = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, dimensions.registryLifecycle());
        for (var stem : dimensions) {
            var key = dimensions.getResourceKey(stem).orElseThrow();
            replacement.register(key, key.equals(WitcheryDimensions.DREAM_STEM) ? resolved : stem,
                    dimensions.lifecycle(stem));
        }
        info.setReturnValue(new WorldDimensions.Complete(replacement.freeze(), baked.specialWorldProperty()));
    }
}
