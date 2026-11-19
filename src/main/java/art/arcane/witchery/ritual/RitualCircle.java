package art.arcane.witchery.ritual;

import net.minecraft.core.BlockPos;
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
    LARGE(
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
            "....#######....");

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

    public boolean matches(LevelReader level, BlockPos center, Block glyph) {
        for (BlockPos offset : offsets) {
            BlockPos pos = center.offset(offset);
            if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(glyph)) {
                return false;
            }
        }
        return true;
    }
}
