package art.arcane.witchery.mixin;

import art.arcane.witchery.world.migration.LegacyChunkUpgrade;
import art.arcane.witchery.world.migration.LegacyWorldIds;
import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

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
}
