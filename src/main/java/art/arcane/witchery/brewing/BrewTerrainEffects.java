package art.arcane.witchery.brewing;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

final class BrewTerrainEffects {
    private BrewTerrainEffects() {}

    static void extinguish(ServerLevel level, BlockPos center, int radius, boolean ritual, int strength,
                           @Nullable Player caster) {
        if (strength <= 1 && level.dimensionType().ultraWarm()) return;
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        int reach = radius + (ritual ? 5 : 0) - 1;
        int bottom = Math.max(level.getMinBuildHeight(), center.getY() - radius);
        int top = Math.min(level.getMaxBuildHeight() - 1, center.getY() + radius);
        var cursor = new BlockPos.MutableBlockPos();
        int[] rowWidth = new int[reach + 1];
        int circleX = reach;
        int circleZ = 0;
        int error = 1 - reach;
        while (circleX >= circleZ) {
            rowWidth[circleZ] = Math.max(rowWidth[circleZ], circleX);
            rowWidth[circleX] = Math.max(rowWidth[circleX], circleZ);
            circleZ++;
            if (error < 0) {
                error += 2 * circleZ + 1;
            } else {
                circleX--;
                error += 2 * (circleZ - circleX + 1);
            }
        }
        for (int z = -reach; z <= reach; z++) {
            int width = rowWidth[Math.abs(z)];
            for (int x = -width; x <= width; x++) {
                for (int y = bottom; y <= top; y++) {
                    cursor.set(center.getX() + x, y, center.getZ() + z);
                    if (!level.getWorldBorder().isWithinBounds(cursor)) continue;
                    var state = level.getBlockState(cursor);
                    if (state.is(Blocks.FIRE) && !MinecraftForge.EVENT_BUS.post(
                            new BlockEvent.BreakEvent(level, cursor.immutable(), state, actor))) {
                        level.removeBlock(cursor, false);
                        level.playSound(null, cursor, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 2);
                    }
                }
            }
        }
    }
}
