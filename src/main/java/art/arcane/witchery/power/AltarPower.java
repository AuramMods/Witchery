package art.arcane.witchery.power;

import art.arcane.witchery.block.entity.AltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public final class AltarPower {
    private static final int RANGE = 16;

    private AltarPower() {
    }

    @Nullable
    public static AltarBlockEntity nearest(ServerLevel level, BlockPos pos) {
        AltarBlockEntity closest = null;
        double nearestDistance = RANGE * RANGE;
        for (int x = SectionPos.blockToSectionCoord(pos.getX() - RANGE); x <= SectionPos.blockToSectionCoord(pos.getX() + RANGE); x++) {
            for (int z = SectionPos.blockToSectionCoord(pos.getZ() - RANGE); z <= SectionPos.blockToSectionCoord(pos.getZ() + RANGE); z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof AltarBlockEntity altar) {
                        double distance = altar.getBlockPos().distSqr(pos);
                        if (distance <= nearestDistance && altar.isCore(level)) {
                            nearestDistance = distance;
                            closest = altar;
                        }
                    }
                }
            }
        }
        return closest;
    }
}
