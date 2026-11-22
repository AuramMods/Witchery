package art.arcane.witchery.mixin;

import art.arcane.witchery.block.WitchSaplingBlock;
import art.arcane.witchery.block.WitchWoodType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Pseudo
@Mixin(targets = "com.buuz135.industrial.block.agriculturehusbandry.tile.PlantSowerTile", remap = false)
abstract class IndustrialPlantSowerMixin {
    @Unique
    private static final String witchery$getPlant = "Lnet/minecraftforge/common/IPlantable;getPlant"
            + "(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;";

    @Unique
    private WitchWoodType witchery$selectedWood;

    // IPlantable receives no ItemStack. Capture the stack immediately before the redirected call.
    @Inject(method = "work", at = @At(value = "INVOKE", target = witchery$getPlant),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void witchery$readSelectedWood(CallbackInfoReturnable<?> result,
                                          BlockPos position, int filterSlot, ItemStack selectedStack) {
        witchery$selectedWood = WitchWoodType.fromStack(selectedStack);
    }

    @Redirect(method = "work", at = @At(value = "INVOKE", target = witchery$getPlant))
    private BlockState witchery$preserveSelectedWood(IPlantable plant, BlockGetter level, BlockPos position) {
        WitchWoodType wood = witchery$selectedWood;
        witchery$selectedWood = null;
        BlockState state = plant.getPlant(level, position);
        return plant instanceof WitchSaplingBlock && wood != null ? state.setValue(WitchWoodType.PROPERTY, wood) : state;
    }
}
