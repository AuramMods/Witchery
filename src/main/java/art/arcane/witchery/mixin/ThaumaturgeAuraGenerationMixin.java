package art.arcane.witchery.mixin;

import art.arcane.witchery.integration.ThaumaturgeCompat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.leclowndu93150.thaumaturge.content.aura.AuraGenHandler", remap = false)
abstract class ThaumaturgeAuraGenerationMixin {
    @Inject(method = "initializeIfNeeded", at = @At("HEAD"), cancellable = true)
    private static void witchery$excludeNaturalAura(ServerLevel level, LevelChunk chunk, @Coerce Object aura,
            CallbackInfo callback) {
        if (ThaumaturgeCompat.excludesGeneration(level)) callback.cancel();
    }
}
