package art.arcane.witchery.mixin;

import art.arcane.witchery.world.migration.LegacyChunkUpgrade;
import art.arcane.witchery.world.migration.LegacyWorldIds;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(ChunkStorage.class)
abstract class LegacyChunkStorageMixin {
    @Unique private LegacyWorldIds witchery$legacyIds;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void witchery$findLegacyIds(Path region, DataFixer fixer, boolean synchronous, CallbackInfo info) {
        witchery$legacyIds = LegacyWorldIds.find(region);
    }

    @Redirect(method = "upgradeChunkTag", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag witchery$upgradeLegacyChunk(DataFixTypes type, DataFixer fixer, CompoundTag chunk, int from, int to) {
        if (witchery$legacyIds != null && from < 100) {
            return LegacyChunkUpgrade.upgrade(type, fixer, chunk, from, to, witchery$legacyIds);
        }
        return type.update(fixer, chunk, from, to);
    }

    @Inject(method = "upgradeChunkTag", at = @At("RETURN"))
    private void witchery$restoreLegacyEntities(ResourceKey<Level> dimension, Supplier<DimensionDataStorage> storage,
            CompoundTag input, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> generator,
            CallbackInfoReturnable<CompoundTag> info) {
        if (witchery$legacyIds != null) LegacyChunkUpgrade.restoreEntities(info.getReturnValue());
    }
}
