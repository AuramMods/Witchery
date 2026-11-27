package art.arcane.witchery.mixin;

import art.arcane.witchery.block.WitchCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(Explosion.class)
abstract class MandrakeExplosionMixin {
    @Redirect(method = "finalizeExplosion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getDrops(Lnet/minecraft/world/level/storage/loot/LootParams$Builder;)Ljava/util/List;"))
    private List<ItemStack> witchery$awakenDuringBlast(BlockState state, LootParams.Builder loot) {
        if (!(state.getBlock() instanceof WitchCropBlock)) return state.getDrops(loot);
        var pos = BlockPos.containing(loot.getParameter(LootContextParams.ORIGIN));
        return WitchCropBlock.awakenMandrake(loot.getLevel(), pos, state) ? List.of() : state.getDrops(loot);
    }
}
