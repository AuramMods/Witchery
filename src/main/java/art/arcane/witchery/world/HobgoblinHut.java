package art.arcane.witchery.world;

import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.event.ForgeEventFactory;

final class HobgoblinHut {
    private static final String[][] LAYERS = {
            {"...F...", ".F...F.", ".......", "D.....F", ".......", ".F...F.", "...F..."},
            {"...F...", ".FHHHF.", ".HPPPH.", ".JPUPHF", ".HPPPH.", ".FHHHF.", "...F..."},
            {"..OGO..", ".G.TWG.", "A....QJ", ".K...QG", "A....QJ", ".G...G.", "..RGR.."},
            {"..BFB..", ".B...B.", "B.....B", ".L....F", "B....ZB", ".B...B.", "..BFB.."},
            {"..CSC..", ".CMIXC.", "CIQ.QIC", "EM...XY", "CNQ.QNC", ".CMNXC.", "..CVC.."},
            {".......", ".......", "...S...", "..E.Y..", "...V...", ".......", "......."}
    };

    private HobgoblinHut() {}

    static void build(SurfaceStructureSite terrain) {
        var site = new SurfaceStructureSite(terrain.level(), terrain.center().above(), terrain.facing(),
                terrain.width(), terrain.depth(), terrain.sandy());
        for (int y = 0; y < LAYERS.length; y++) {
            for (int z = 0; z < site.depth(); z++) {
                for (int x = 0; x < site.width(); x++) site.put(x, y, z, state(site, LAYERS[y][z].charAt(x)));
            }
        }
        for (int z = 0; z < site.depth(); z++) {
            for (int x = 0; x < site.width(); x++) {
                if (LAYERS[0][z].charAt(x) != '.') site.support(x, z);
                for (int y = 0; y < LAYERS.length; y++) {
                    if (LAYERS[y][z].charAt(x) == 'F') site.putConnected(x, y, z, Blocks.OAK_FENCE);
                }
            }
        }

        var level = site.level();
        var resident = WitcheryEntities.GOBLIN.get().create(level.getLevel());
        if (resident == null) return;
        var pos = site.at(3, 3, 3);
        resident.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, site.facing().toYRot(), 0);
        ForgeEventFactory.onFinalizeSpawn(resident, level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        resident.setPersistenceRequired();
        level.addFreshEntity(resident);
    }

    private static BlockState state(SurfaceStructureSite site, char symbol) {
        return switch (symbol) {
            case '.' -> Blocks.AIR.defaultBlockState();
            case 'A' -> stairs(site, Blocks.OAK_STAIRS, Direction.EAST, Half.TOP);
            case 'B' -> Blocks.OAK_LOG.defaultBlockState();
            case 'C' -> Blocks.SPRUCE_SLAB.defaultBlockState();
            case 'D' -> stairs(site, Blocks.OAK_STAIRS, Direction.EAST, Half.BOTTOM);
            case 'E' -> stairs(site, Blocks.SPRUCE_STAIRS, Direction.WEST, Half.BOTTOM);
            case 'F' -> Blocks.OAK_FENCE.defaultBlockState();
            case 'G' -> Blocks.OAK_PLANKS.defaultBlockState();
            case 'H' -> slabTop(Blocks.OAK_SLAB);
            case 'I' -> stairs(site, Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.TOP);
            case 'J' -> stairs(site, Blocks.OAK_STAIRS, Direction.WEST, Half.TOP);
            case 'K', 'L' -> Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, site.direction(Direction.EAST))
                    .setValue(DoorBlock.HALF, symbol == 'K' ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
            case 'M' -> stairs(site, Blocks.SPRUCE_STAIRS, Direction.WEST, Half.TOP);
            case 'N' -> stairs(site, Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.TOP);
            case 'O' -> stairs(site, Blocks.OAK_STAIRS, Direction.SOUTH, Half.TOP);
            case 'P' -> Blocks.COBBLESTONE_SLAB.defaultBlockState();
            case 'Q' -> slabTop(Blocks.SPRUCE_SLAB);
            case 'R' -> stairs(site, Blocks.OAK_STAIRS, Direction.NORTH, Half.TOP);
            case 'S' -> stairs(site, Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM);
            case 'T' -> Blocks.BIRCH_SLAB.defaultBlockState();
            case 'U' -> Blocks.CAULDRON.defaultBlockState();
            case 'V' -> stairs(site, Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM);
            case 'W' -> stairs(site, Blocks.BIRCH_STAIRS, Direction.EAST, Half.BOTTOM);
            case 'X' -> stairs(site, Blocks.SPRUCE_STAIRS, Direction.EAST, Half.TOP);
            case 'Y' -> stairs(site, Blocks.SPRUCE_STAIRS, Direction.EAST, Half.BOTTOM);
            case 'Z' -> Blocks.TORCH.defaultBlockState();
            default -> throw new IllegalArgumentException("Unknown hobgoblin hut block " + symbol);
        };
    }

    private static BlockState slabTop(Block slab) {
        return slab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
    }

    private static BlockState stairs(SurfaceStructureSite site, Block stairs, Direction direction, Half half) {
        return stairs.defaultBlockState().setValue(StairBlock.FACING, site.direction(direction)).setValue(StairBlock.HALF, half);
    }
}
