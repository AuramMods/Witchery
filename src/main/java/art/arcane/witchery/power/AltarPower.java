package art.arcane.witchery.power;

import art.arcane.witchery.block.entity.AltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public final class AltarPower {
    private AltarPower() {
    }

    @Nullable
    public static AltarBlockEntity upkeepSource(ServerLevel level, BlockPos pos, @Nullable AltarBlockEntity current) {
        if (current != null && !current.isRemoved() && current.isInRange(pos)
                && level.random.nextInt(5) != 0 && current.isCore(level)) return current;
        return nearest(level, pos);
    }

    @Nullable
    public static AltarBlockEntity nearest(ServerLevel level, BlockPos pos) {
        int range = AltarBlockEntity.MAX_RANGE;
        AltarBlockEntity closest = null;
        double nearestDistance = range * range;
        for (int x = SectionPos.blockToSectionCoord(pos.getX() - range); x <= SectionPos.blockToSectionCoord(pos.getX() + range); x++) {
            for (int z = SectionPos.blockToSectionCoord(pos.getZ() - range); z <= SectionPos.blockToSectionCoord(pos.getZ() + range); z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof AltarBlockEntity altar) {
                        double distance = altar.getBlockPos().distSqr(pos);
                        if (distance <= nearestDistance && altar.isInRange(pos) && altar.isCore(level)) {
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
