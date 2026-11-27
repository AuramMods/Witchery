package art.arcane.witchery.ritual;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class IceShell {
    private static final int[] SCAN_DIRECTIONS = {1, -1};
    private static final Set<Block> LEGACY_FLOWERS = Set.of(Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID,
            Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP, Blocks.OXEYE_DAISY);

    private IceShell() {}

    static int radius(int coven) {
        return coven <= 2 ? 8 : coven <= 5 ? 12 : 16;
    }

    static void pulse(ServerLevel level, BlockPos center, int stage, int coven, @Nullable ServerPlayer caster) {
        if (stage % 2 != 0) return;
        ServerPlayer actor = caster != null && caster.serverLevel() == level ? caster : FakePlayerFactory.getMinecraft(level);
        int radius = stage + 4;
        BlockState ice = WitcheryBlocks.getRequired("perpetualice").get().defaultBlockState();
        draw(level, center, radius, ice, ice, actor);
        draw(level, center, radius - 2, Blocks.AIR.defaultBlockState(), ice, actor);
        if (radius == radius(coven)) {
            forEachDrainCell(level, center, radius, ice, pos -> {
                if (level.getBlockState(pos).is(Blocks.WATER)) {
                    LandscapeRites.replace(level, pos, Blocks.AIR.defaultBlockState(), actor);
                }
            });
        }
    }

    private static void draw(ServerLevel level, BlockPos center, int radius, BlockState output,
                             BlockState shell, ServerPlayer actor) {
        for (BlockPos point : outline(radius)) {
            BlockPos pos = center.offset(point);
            if (replaceable(level.getBlockState(pos), shell)) LandscapeRites.replace(level, pos, output, actor);
        }
    }

    public static Set<BlockPos> outline(int radius) {
        Set<BlockPos> points = new LinkedHashSet<>();
        int error = 1 - radius;
        for (int x = radius, y = 0; x >= y; y++) {
            int circleError = error;
            for (int width = x, z = 0; width >= z; z++) {
                addSigns(points, width, y, z);
                addSigns(points, z, y, width);
                addSigns(points, y, width, z);
                addSigns(points, z, width, y);
                if (circleError < 0) circleError += 2 * (z + 1) + 1;
                else circleError += 2 * (z - --width + 2);
            }
            if (error < 0) error += 2 * (y + 1) + 1;
            else error += 2 * (y - --x + 2);
        }
        return points;
    }

    private static void addSigns(Set<BlockPos> points, int x, int y, int z) {
        for (int signZ : SCAN_DIRECTIONS) {
            for (int signY : SCAN_DIRECTIONS) {
                for (int signX : SCAN_DIRECTIONS) {
                    points.add(new BlockPos(signX * x, signY * y, signZ * z));
                }
            }
        }
    }

    private static boolean replaceable(BlockState state, BlockState shell) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.ICE) || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.GRASS) || state.is(Blocks.FERN) || state.is(Blocks.VINE) || state.is(Blocks.LILY_PAD)
                || LEGACY_FLOWERS.contains(state.getBlock()) || state.is(Blocks.CACTUS) || state.is(Blocks.DEAD_BUSH) || state.is(shell.getBlock());
    }

    public static void forEachDrainCell(ServerLevel level, BlockPos center, int radius, BlockState shell,
                                        Consumer<BlockPos> drain) {
        for (int dy : SCAN_DIRECTIONS) {
            for (int step = 0, y = center.getY() + Math.min(dy, 0); step <= radius; step++, y += dy) {
                if (level.getBlockState(new BlockPos(center.getX(), y, center.getZ())).is(shell.getBlock())) break;
                drainSlice(level, center, radius, shell, drain, y);
            }
        }
    }

    private static void drainSlice(ServerLevel level, BlockPos center, int radius, BlockState shell,
                                   Consumer<BlockPos> drain, int y) {
        for (int dx : SCAN_DIRECTIONS) {
            for (int step = 0, x = center.getX() + Math.min(dx, 0); step <= radius; step++, x += dx) {
                // The original uses the X step as the absolute Y of this stop check.
                if (level.getBlockState(new BlockPos(x, step, center.getZ())).is(shell.getBlock())) break;
                drainRow(level, center.getZ(), radius, shell, drain, x, y);
            }
        }
    }

    private static void drainRow(ServerLevel level, int centerZ, int radius, BlockState shell,
                                 Consumer<BlockPos> drain, int x, int y) {
        for (int dz : SCAN_DIRECTIONS) {
            for (int step = 0, z = centerZ + Math.min(dz, 0); step <= radius; step++, z += dz) {
                BlockPos pos = new BlockPos(x, y, z);
                if (level.getBlockState(pos).is(shell.getBlock())) break;
                drain.accept(pos);
            }
        }
    }
}
