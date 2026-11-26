package art.arcane.witchery.mixin;

import art.arcane.witchery.world.migration.LegacyWorldIds;
import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerDataStorage.class)
abstract class LegacyPlayerDataMixin {
    @Unique private LegacyWorldIds witchery$legacyIds;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void witchery$findLegacyIds(LevelStorageSource.LevelStorageAccess access, DataFixer fixer, CallbackInfo info) {
        witchery$legacyIds = LegacyWorldIds.find(access.getLevelPath(LevelResource.ROOT));
    }

    @Redirect(method = "load", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/datafix/DataFixTypes;updateToCurrentVersion(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag witchery$upgradeLegacyPlayer(DataFixTypes type, DataFixer fixer, CompoundTag player, int from) {
        if (witchery$legacyIds != null) return witchery$legacyIds.upgradePlayer(player, fixer, from);
        LegacyWorldIds.repairPlayerWithoutLegacyRegistry(player, fixer, from);
        return type.updateToCurrentVersion(fixer, player, from);
    }
}
