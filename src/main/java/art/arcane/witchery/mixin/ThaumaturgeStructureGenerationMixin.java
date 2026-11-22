package art.arcane.witchery.mixin;

import art.arcane.witchery.integration.ThaumaturgeCompat;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
abstract class ThaumaturgeStructureGenerationMixin {
    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void witchery$excludeThaumaturgeStructures(StructureSet.StructureSelectionEntry entry,
            StructureManager structures, RegistryAccess registries, RandomState random,
            StructureTemplateManager templates, long seed, ChunkAccess chunk, ChunkPos chunkPos, SectionPos section,
            CallbackInfoReturnable<Boolean> callback) {
        if (ThaumaturgeCompat.excludesGeneration(((StructureManagerLevelAccessor) structures).witchery$getLevel())
                && entry.structure().unwrapKey().filter(key -> ThaumaturgeCompat.isThaumaturgeContent(key.location())).isPresent()) {
            callback.setReturnValue(false);
        }
    }
}
