package art.arcane.witchery.mixin;

import art.arcane.witchery.world.migration.LegacyChunkEntities;
import art.arcane.witchery.world.migration.LegacyWorldIds;
import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.concurrent.Executor;

@Mixin(EntityStorage.class)
abstract class LegacyEntityStorageMixin {
    @Shadow @Final protected DataFixer fixerUpper;
    @Unique private LegacyWorldIds witchery$legacyIds;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void witchery$findLegacyIds(ServerLevel level, Path storage, DataFixer fixer, boolean synchronous,
                                       Executor executor, CallbackInfo info) {
        witchery$legacyIds = LegacyWorldIds.find(storage);
    }

    @Inject(method = "upgradeChunkTag", at = @At("RETURN"))
    private void witchery$repairStoredEntities(CompoundTag input, CallbackInfoReturnable<CompoundTag> info) {
        var entities = info.getReturnValue().getList("Entities", Tag.TAG_COMPOUND);
        LegacyChunkEntities.restore(entities);
        if (witchery$legacyIds != null) {
            witchery$legacyIds.repairEntityEffects(entities);
            witchery$legacyIds.repairStoredItems(entities, fixerUpper);
        } else {
            LegacyWorldIds.repairNamedStoredItems(entities, fixerUpper, true);
        }
    }
}
