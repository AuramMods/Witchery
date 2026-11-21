package art.arcane.witchery.mixin;

import art.arcane.witchery.world.migration.LegacyWorldIds;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
abstract class LegacyLevelStorageMixin {
    @Inject(method = "readAdditionalLevelSaveData", at = @At("HEAD"), remap = false)
    private void witchery$captureLegacyRegistry(CallbackInfo info) {
        LevelStorageSource.LevelStorageAccess access = (LevelStorageSource.LevelStorageAccess) (Object) this;
        LegacyWorldIds.prepare(access.getLevelPath(LevelResource.ROOT));
    }
}
