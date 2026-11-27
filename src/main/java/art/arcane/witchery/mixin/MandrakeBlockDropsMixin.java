package art.arcane.witchery.mixin;

import art.arcane.witchery.block.WitchCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
abstract class MandrakeBlockDropsMixin {
    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD"), cancellable = true)
    private static void witchery$awakenOnDrop(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (WitchCropBlock.awakenMandrake(level, pos, state)) ci.cancel();
    }

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
            at = @At("HEAD"), cancellable = true)
    private static void witchery$awakenOnDrop(BlockState state, LevelAccessor level, BlockPos pos,
                                             @Nullable BlockEntity tile, CallbackInfo ci) {
        if (WitchCropBlock.awakenMandrake(level, pos, state)) ci.cancel();
    }

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Z)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void witchery$awakenOnHarvest(BlockState state, Level level, BlockPos pos, @Nullable BlockEntity tile,
                                                @Nullable Entity breaker, ItemStack tool, boolean dropXp, CallbackInfo ci) {
        if (WitchCropBlock.awakenMandrake(level, pos, state)) ci.cancel();
    }
}
