package art.arcane.witchery.mixin;

import art.arcane.witchery.world.migration.LegacyVillagePieces;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
abstract class LegacyVillageDecorationMixin {
    @Inject(method = "applyBiomeDecoration", at = @At("TAIL"))
    private void witchery$resumeLegacyVillagePieces(WorldGenLevel level, ChunkAccess chunk, StructureManager structures,
                                                    CallbackInfo info) {
        if (level instanceof WorldGenRegion region) {
            LegacyVillagePieces.place(region, chunk, (ChunkGenerator) (Object) this);
        }
    }
}
