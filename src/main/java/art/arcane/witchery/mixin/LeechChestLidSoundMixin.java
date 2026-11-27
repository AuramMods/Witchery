package art.arcane.witchery.mixin;

import art.arcane.witchery.block.LeechChestBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChestBlockEntity.class)
abstract class LeechChestLidSoundMixin {
    @ModifyVariable(method = "playSound", at = @At("HEAD"), argsOnly = true)
    private static BlockState witchery$playEachLeechChestAtItsOwnPosition(BlockState state) {
        return state.getBlock() instanceof LeechChestBlock ? state.setValue(ChestBlock.TYPE, ChestType.SINGLE) : state;
    }
}
