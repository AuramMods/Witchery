package art.arcane.witchery.mixin;

import art.arcane.witchery.world.migration.LegacyChunkUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockEntity.class)
abstract class LegacyBlockEntityTypeMixin {
    @Redirect(method = "loadStatic", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompoundTag;getString(Ljava/lang/String;)Ljava/lang/String;"))
    private static String witchery$restoreSharedType(CompoundTag tag, String key,
                                                    BlockPos pos, BlockState state, CompoundTag data) {
        // A converted chunk can still carry the old shared ID. The placed block identifies the intended tile.
        return LegacyChunkUpgrade.restoreSharedTileType(tag.getString(key), BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }
}
