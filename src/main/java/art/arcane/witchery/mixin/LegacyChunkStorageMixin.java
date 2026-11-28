package art.arcane.witchery.mixin;

import art.arcane.witchery.world.WitcheryDimensions;
import art.arcane.witchery.world.migration.LegacyChunkUpgrade;
import art.arcane.witchery.world.migration.LegacyWorldIds;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(ChunkStorage.class)
abstract class LegacyChunkStorageMixin {
    @Shadow @Final protected DataFixer fixerUpper;
    @Unique private LegacyWorldIds witchery$legacyIds;
    @Unique private Path witchery$dimensionData;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void witchery$findLegacyIds(Path region, DataFixer fixer, boolean synchronous, CallbackInfo info) {
        witchery$legacyIds = LegacyWorldIds.find(region);
        witchery$dimensionData = region.resolveSibling("data");
    }

    @Redirect(method = "getLegacyStructureHandler", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/LegacyStructureDataHandler;getLegacyStructureHandler(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/storage/DimensionDataStorage;)Lnet/minecraft/world/level/levelgen/structure/LegacyStructureDataHandler;"))
    private LegacyStructureDataHandler witchery$legacyDimensionStructures(ResourceKey<Level> dimension, DimensionDataStorage overworld) {
        if (witchery$legacyIds != null) {
            if (dimension.equals(WitcheryDimensions.DREAM)) {
                // The old Dream used the Overworld generator, with the Dream's own structure records.
                var data = new DimensionDataStorage(witchery$dimensionData.toFile(), fixerUpper);
                return LegacyStructureDataHandler.getLegacyStructureHandler(Level.OVERWORLD, data);
            }
            if (dimension.equals(WitcheryDimensions.TORMENT) || dimension.equals(WitcheryDimensions.MIRROR)) {
                // These generators have no vanilla structure records to merge into old chunks.
                return new LegacyStructureDataHandler(null, List.of(), List.of());
            }
        }
        return LegacyStructureDataHandler.getLegacyStructureHandler(dimension, overworld);
    }

    @Redirect(method = "upgradeChunkTag", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag witchery$upgradeLegacyChunk(DataFixTypes type, DataFixer fixer, CompoundTag chunk, int from, int to) {
        if (witchery$legacyIds != null && from < 100) {
            return LegacyChunkUpgrade.upgrade(type, fixer, chunk, from, to, witchery$legacyIds);
        }
        return type.update(fixer, chunk, from, to);
    }

    @Redirect(method = "upgradeChunkTag", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/datafix/DataFixTypes;updateToCurrentVersion(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag witchery$finishLegacyChunk(DataFixTypes type, DataFixer fixer, CompoundTag chunk, int version,
            ResourceKey<Level> dimension, Supplier<DimensionDataStorage> storage, CompoundTag input,
            Optional<ResourceKey<Codec<? extends ChunkGenerator>>> generator) {
        boolean legacy = witchery$legacyIds != null && ChunkStorage.getVersion(chunk) < 100;
        if (legacy && (dimension.equals(WitcheryDimensions.DREAM) || dimension.equals(WitcheryDimensions.MIRROR))) {
            // Dream and Mirror expanded to the Overworld's vertical bounds in the port.
            ChunkStorage.injectDatafixingContext(chunk, Level.OVERWORLD, generator);
        }
        CompoundTag updated = type.updateToCurrentVersion(fixer, chunk, version);
        if (legacy) LegacyChunkUpgrade.finish(updated, dimension);
        else {
            LegacyChunkUpgrade.restorePendingLeafChecks(updated);
            LegacyChunkUpgrade.restoreMountedSkullStates(updated);
        }
        var tiles = updated.getList("block_entities", Tag.TAG_COMPOUND);
        LegacyChunkUpgrade.upgradeBlockEntities(tiles, false);
        if (witchery$legacyIds != null) {
            witchery$legacyIds.repairBlockSpawnerEffects(tiles);
            if (!legacy) witchery$legacyIds.repairConvertedTileItems(tiles, fixer, version);
            witchery$legacyIds.repairStoredItems(tiles, fixer);
            if (legacy) witchery$legacyIds.repairStoredItems(updated.getList("entities", Tag.TAG_COMPOUND), fixer);
        } else {
            LegacyWorldIds.repairNamedStoredItems(tiles, fixer, true);
        }
        return updated;
    }
}
