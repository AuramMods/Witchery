package art.arcane.witchery.ritual;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public final class IceShell {
    private IceShell() {}

    static int radius(int coven) {
        return coven <= 2 ? 8 : coven <= 5 ? 12 : 16;
    }

    static void pulse(ServerLevel level, BlockPos center, int stage, int coven, @Nullable ServerPlayer caster) {
        if (stage % 2 != 0) return;
        int radius = stage + 4;
        BlockState ice = WitcheryBlocks.getRequired("perpetualice").get().defaultBlockState();
        draw(level, center, radius, ice, ice, caster);
        draw(level, center, radius - 2, Blocks.AIR.defaultBlockState(), ice, caster);
        if (radius == radius(coven)) drain(level, center, radius, caster);
    }

    private static void draw(ServerLevel level, BlockPos center, int radius, BlockState output,
                             BlockState shell, @Nullable ServerPlayer caster) {
        for (BlockPos point : outline(radius)) {
            BlockPos pos = center.offset(point);
            if (replaceable(level.getBlockState(pos), shell)) LandscapeRites.replace(level, pos, output, caster);
        }
    }

    public static Set<BlockPos> outline(int radius) {
        Set<BlockPos> points = new HashSet<>();
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
        for (int signX : new int[]{-1, 1}) {
            for (int signY : new int[]{-1, 1}) {
                for (int signZ : new int[]{-1, 1}) {
                    points.add(new BlockPos(signX * x, signY * y, signZ * z));
                }
            }
        }
    }

    private static boolean replaceable(BlockState state, BlockState shell) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.ICE) || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.GRASS) || state.is(Blocks.FERN) || state.is(Blocks.VINE) || state.is(Blocks.LILY_PAD)
                || state.is(BlockTags.SMALL_FLOWERS) || state.is(Blocks.CACTUS) || state.is(Blocks.DEAD_BUSH) || state.is(shell.getBlock());
    }

    private static void drain(ServerLevel level, BlockPos center, int radius, @Nullable ServerPlayer caster) {
        BlockPos offset = new BlockPos(radius, radius, radius);
        for (BlockPos point : BlockPos.betweenClosed(center.subtract(offset), center.offset(offset))) {
            if (point.distSqr(center) < radius * radius && level.getBlockState(point).is(Blocks.WATER)) {
                LandscapeRites.replace(level, point, Blocks.AIR.defaultBlockState(), caster);
            }
        }
    }
}
