package art.arcane.witchery.item;

import art.arcane.witchery.block.entity.PoppetShelfBlockEntity;
import net.minecraft.core.SectionPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class PoppetShelfCompassItem extends Item {
    private static final int RANGE = 128;

    public PoppetShelfCompassItem() {
        super(new Properties());
    }

    public static int signal(Level level, double x, double z) {
        var chunks = level.getChunkSource();
        double closestSquared = RANGE * RANGE;
        int minX = SectionPos.blockToSectionCoord(x - RANGE);
        int maxX = SectionPos.blockToSectionCoord(x + RANGE);
        int minZ = SectionPos.blockToSectionCoord(z - RANGE);
        int maxZ = SectionPos.blockToSectionCoord(z + RANGE);
        for (int chunkX = minX; chunkX <= maxX; chunkX++) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                var chunk = chunks.getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;
                for (var entity : chunk.getBlockEntities().values()) {
                    if (!(entity instanceof PoppetShelfBlockEntity shelf) || shelf.isRemoved()) continue;
                    var pos = shelf.getBlockPos();
                    double dx = x - pos.getX();
                    double dz = z - pos.getZ();
                    closestSquared = Math.min(closestSquared, dx * dx + dz * dz);
                    if (closestSquared < 64) return 5;
                }
            }
        }
        if (closestSquared < 256) return 4;
        if (closestSquared < 1024) return 3;
        if (closestSquared < 4096) return 2;
        return closestSquared < RANGE * RANGE ? 1 : 0;
    }
}
