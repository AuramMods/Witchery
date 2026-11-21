package art.arcane.witchery.ritual;

import art.arcane.witchery.block.RitualGlyphBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public enum RitualCircle {
    SMALL(
            ".###.",
            "#...#",
            "#...#",
            "#...#",
            ".###."),
    MEDIUM(
            "..#####..",
            ".#.....#.",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            ".#.....#.",
            "..#####.."),
    SMALL_RITE(
            "..###..",
            ".#...#.",
            "#.....#",
            "#.....#",
            "#.....#",
            ".#...#.",
            "..###.."),
    MEDIUM_RITE(
            "...#####...",
            "..#.....#..",
            ".#.......#.",
            "#.........#",
            "#.........#",
            "#.........#",
            "#.........#",
            "#.........#",
            ".#.......#.",
            "..#.....#..",
            "...#####..."),
    LARGE_RITE(
            "....#######....",
            "...#.......#...",
            "..#.........#..",
            ".#...........#.",
            "#.............#",
            "#.............#",
            "#.............#",
            "#.............#",
            "#.............#",
            "#.............#",
            "#.............#",
            ".#...........#.",
            "..#.........#..",
            "...#.......#...",
            "....#######...."),
    TINY(
            "###",
            "#.#",
            "###");

    private final List<BlockPos> offsets;

    RitualCircle(String... rows) {
        List<BlockPos> outline = new ArrayList<>();
        for (int z = 0; z < rows.length; z++) {
            String row = rows[z];
            for (int x = 0; x < row.length(); x++) {
                if (row.charAt(x) == '#') {
                    outline.add(new BlockPos(x - row.length() / 2, 0, z - rows.length / 2));
                }
            }
        }
        offsets = List.copyOf(outline);
    }

    public List<BlockPos> offsets() {
        return offsets;
    }

    public boolean matches(LevelReader level, BlockPos center, Block glyph) {
        for (BlockPos offset : offsets) {
            BlockPos pos = center.offset(offset);
            if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(glyph)) {
                return false;
            }
        }
        return true;
    }

    public int sacrificeRadius() {
        return (offsets.size() + 2) / 6 + 1;
    }

    public void erase(LevelAccessor level, BlockPos center, Block glyph) {
        for (BlockPos offset : offsets) {
            BlockPos pos = center.offset(offset);
            if (level.getBlockState(pos).is(glyph)) level.removeBlock(pos, false);
        }
    }

    void recolor(ServerLevel level, BlockPos center, Block glyph) {
        for (BlockPos offset : offsets) {
            BlockPos pos = center.offset(offset);
            var state = level.getBlockState(pos);
            if (state.getBlock() instanceof RitualGlyphBlock && !state.is(glyph)) {
                level.setBlockAndUpdate(pos, glyph.defaultBlockState().setValue(RitualGlyphBlock.PATTERN, state.getValue(RitualGlyphBlock.PATTERN)));
                level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
                        6, 0.25, 0.12, 0.25, 0.02);
            }
        }
    }
}
