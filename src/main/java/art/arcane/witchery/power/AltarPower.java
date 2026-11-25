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

    public static final class WorkstationSource {
        private static final int IDLE_CHECK_INTERVAL = 40;
        private static final int FORCE_LOOKUP_INTERVAL = 100;

        @Nullable
        private BlockPos cachedPos;
        private long ticks;

        public boolean idleTick(ServerLevel level, BlockPos pos, boolean powered) {
            if (++ticks % IDLE_CHECK_INTERVAL != 0) return powered;
            AltarBlockEntity source = find(level, pos);
            if (source != null) cachedPos = source.getBlockPos();
            return source != null;
        }

        public boolean activeTick(ServerLevel level, BlockPos pos, float cost) {
            ticks++;
            AltarBlockEntity source = find(level, pos);
            // A burning machine keeps a newly selected altar for one more tick.
            cachedPos = source != null && !source.getBlockPos().equals(cachedPos) ? source.getBlockPos() : null;
            return cost == 0 || source != null && source.consumePower(cost);
        }

        @Nullable
        private AltarBlockEntity find(ServerLevel level, BlockPos pos) {
            if (cachedPos != null && ticks % FORCE_LOOKUP_INTERVAL != 0) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(
                        SectionPos.blockToSectionCoord(cachedPos.getX()), SectionPos.blockToSectionCoord(cachedPos.getZ()));
                if (chunk != null && chunk.getBlockEntity(cachedPos) instanceof AltarBlockEntity altar
                        && !altar.isRemoved() && altar.isInRange(pos) && altar.isCore(level)) {
                    return altar;
                }
            }
            return nearest(level, pos);
        }
    }
}
