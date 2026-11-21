package art.arcane.witchery.mixin;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.world.VillageWallPlan;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(JigsawStructure.class)
abstract class VillageWallMixin {
    @Inject(method = "findGenerationPoint", at = @At("RETURN"), cancellable = true)
    private void witchery$assembleVillageWall(Structure.GenerationContext context,
            CallbackInfoReturnable<Optional<Structure.GenerationStub>> callback) {
        if (WitcheryServerConfig.TOWN_WALL_MODE.get() == 0 || WitcheryServerConfig.TOWN_WALL_WEIGHT.get() == 0) return;
        var stub = callback.getReturnValue();
        if (stub.isEmpty()) return;
        var generation = stub.get();
        callback.setReturnValue(Optional.of(new Structure.GenerationStub(generation.position(), builder ->
                VillageWallPlan.assemble(context, generation.getPiecesBuilder().build().pieces(), builder))));
    }
}
