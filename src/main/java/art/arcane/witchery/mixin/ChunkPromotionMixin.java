package art.arcane.witchery.mixin;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkMap.class)
abstract class ChunkPromotionMixin {
    @ModifyVariable(method = "lambda$protoChunkToFullChunk$34(Lnet/minecraft/server/level/ChunkHolder;"
            + "Lnet/minecraft/world/level/chunk/ChunkAccess;)Lnet/minecraft/world/level/chunk/ChunkAccess;",
            at = @At("HEAD"), argsOnly = true, index = 2)
    private ChunkAccess witchery$reusePromotedChunk(ChunkAccess captured, ChunkHolder holder) {
        if (!(captured instanceof ProtoChunk) || captured instanceof ImposterProtoChunk) return captured;

        // Earlier promotion updates the holder's futures. Work already queued can still hold the original protochunk.
        ChunkAccess current = holder.getFutureIfPresentUnchecked(ChunkStatus.FULL.getParent())
                .getNow(ChunkHolder.UNLOADED_CHUNK).left().orElse(captured);
        return current instanceof ImposterProtoChunk ? current : captured;
    }
}
