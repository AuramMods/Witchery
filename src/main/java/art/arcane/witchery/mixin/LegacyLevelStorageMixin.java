package art.arcane.witchery.mixin;

import art.arcane.witchery.world.migration.LegacyWorldIds;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
abstract class LegacyLevelStorageMixin {
    @Inject(method = "readAdditionalLevelSaveData", at = @At("HEAD"), remap = false)
    private void witchery$captureLegacyRegistry(CallbackInfo info) {
        LevelStorageSource.LevelStorageAccess access = (LevelStorageSource.LevelStorageAccess) (Object) this;
        LegacyWorldIds.prepare(access.getLevelPath(LevelResource.ROOT));
    }

    @Inject(method = "getDataTag", at = @At("RETURN"))
    private void witchery$upgradeIntegratedPlayer(DynamicOps<Tag> ops, WorldDataConfiguration configuration,
            Registry<LevelStem> dimensions, Lifecycle lifecycle, CallbackInfoReturnable<Pair<WorldData, WorldDimensions.Complete>> info) {
        var data = info.getReturnValue();
        if (data == null || !(data.getFirst() instanceof LegacyPrimaryLevelDataAccessor player)) return;
        if (player.witchery$getRawPlayerTag() == null) return;
        var access = (LevelStorageSource.LevelStorageAccess) (Object) this;
        var ids = LegacyWorldIds.find(access.getLevelPath(LevelResource.ROOT));
        if (ids != null) ids.upgradePlayer(player.witchery$getRawPlayerTag(), DataFixers.getDataFixer(), player.witchery$getPlayerDataVersion());
    }
}
