package art.arcane.witchery.world;

import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.common.IPlantable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class WitchwoodTreeFeature extends Feature<WitchwoodTreeFeature.Configuration> {
    public record Configuration(WitchWoodType wood) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = StringRepresentable.fromEnum(WitchWoodType::values)
                .fieldOf("wood").xmap(Configuration::new, Configuration::wood).codec();
    }

    private record LeafNode(BlockPos start, int branchY) {}

    public WitchwoodTreeFeature() {
        super(Configuration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        WitchWoodType wood = context.config().wood();
        BlockState log = WitcheryBlocks.getRequired("witchlog").get().defaultBlockState()
                .setValue(WitchWoodType.PROPERTY, wood);
        BlockState leaves = WitcheryBlocks.getRequired("witchleaves").get().defaultBlockState()
                .setValue(WitchWoodType.PROPERTY, wood);
        // The original builds the canopy first. Publish only the final state of cells also used by a branch.
        var blocks = new LinkedHashMap<BlockPos, BlockState>();
        boolean grown = switch (wood) {
            case ROWAN -> growRowan(level, origin, log, leaves, context.random(), blocks);
            case ALDER -> growBranched(level, origin, log, leaves, context.random(), blocks, 7, 0.5, 0.5, 0.5);
            case HAWTHORN -> growBranched(level, origin, log, leaves, context.random(), blocks, 9, 1.2, 1.0, 0.381);
        };
        if (grown) {
            blocks.forEach((pos, state) -> {
                if (!level.isOutsideBuildHeight(pos) && level.ensureCanWrite(pos)) {
                    level.setBlock(pos, state, Block.UPDATE_ALL);
                }
            });
        }
        return grown;
    }

    private static boolean growRowan(WorldGenLevel level, BlockPos origin, BlockState log, BlockState leaves,
                                     RandomSource random, Map<BlockPos, BlockState> blocks) {
        int height = 5 + random.nextInt(3);
        if (!supportsSapling(level, origin) || level.isOutsideBuildHeight(origin.above(height + 1))) return false;
        for (int y = 0; y <= height + 1; y++) {
            int reach = y == 0 ? 0 : y >= height - 1 ? 2 : 1;
            for (int x = -reach; x <= reach; x++) {
                for (int z = -reach; z <= reach; z++) {
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    if (!state.isAir() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS)
                            && !state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.DIRT)) return false;
                }
            }
        }

        BlockPos soil = origin.below();
        BlockState ground = level.getBlockState(soil);
        if (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.FARMLAND)) {
            blocks.put(soil, Blocks.DIRT.defaultBlockState());
        }
        for (int layer = -3; layer <= 0; layer++) {
            int reach = 2 - layer / 2;
            for (int x = -reach; x <= reach; x++) {
                for (int z = -reach; z <= reach; z++) {
                    if (Math.abs(x) == reach && Math.abs(z) == reach && (random.nextInt(2) == 0 || layer == 0)) continue;
                    BlockPos pos = origin.offset(x, height + layer, z);
                    if (level.getBlockState(pos).isAir() || level.getBlockState(pos).is(BlockTags.LEAVES)) {
                        blocks.put(pos, leaves);
                    }
                }
            }
        }
        for (int y = 0; y < height; y++) {
            BlockPos pos = origin.above(y);
            if (level.getBlockState(pos).isAir() || level.getBlockState(pos).is(BlockTags.LEAVES)) {
                blocks.put(pos, log);
            }
        }
        return true;
    }

    private static boolean growBranched(WorldGenLevel level, BlockPos origin, BlockState log, BlockState leaves,
                                         RandomSource worldRandom, Map<BlockPos, BlockState> blocks,
                                         int heightRange, double width, double density, double slope) {
        Random random = new Random(worldRandom.nextLong());
        int height = 5 + random.nextInt(heightRange);
        if (!supportsSapling(level, origin) || level.isOutsideBuildHeight(origin)) return false;
        int obstacle = firstObstruction(level, origin, origin.above(height - 1), leaves.getBlock());
        if (obstacle >= 0) {
            if (obstacle < 6) return false;
            height = obstacle;
        }

        int trunkHeight = (int) (height * 0.618);
        List<LeafNode> nodes = leafNodes(level, origin, leaves.getBlock(), random, height, trunkHeight, width, density, slope);
        for (LeafNode node : nodes) {
            for (int y = 0; y < 4; y++) {
                int radius = y == 0 || y == 3 ? 2 : 3;
                for (int z = -radius; z <= radius; z++) {
                    for (int x = -radius; x <= radius; x++) {
                        double dx = Math.abs(x) + 0.5;
                        double dz = Math.abs(z) + 0.5;
                        BlockPos pos = node.start().offset(x, y, z);
                        if (dx * dx + dz * dz <= radius * radius && canFill(level, pos, leaves.getBlock())) {
                            blocks.put(pos, leaves);
                        }
                    }
                }
            }
        }
        placeLogLine(origin, origin.above(trunkHeight), log, blocks);
        for (LeafNode node : nodes) {
            if (node.branchY() - origin.getY() >= height * 0.2) {
                placeLogLine(origin.atY(node.branchY()), node.start(), log, blocks);
            }
        }
        return true;
    }

    private static List<LeafNode> leafNodes(WorldGenLevel level, BlockPos origin, Block leafBlock, Random random,
                                             int height, int trunkHeight, double width, double density, double slope) {
        int trunkTop = origin.getY() + trunkHeight;
        int samples = Math.max(1, (int) (1.382 + Math.pow(density * height / 13.0, 2)));
        var nodes = new ArrayList<LeafNode>();
        nodes.add(new LeafNode(origin.above(height - 4), trunkTop));
        for (int layer = height - 4; layer >= height * 0.3; layer--) {
            float middle = height / 2.0F;
            float relative = middle - layer;
            float crown = Math.abs(relative) >= middle ? 0 : (float) Math.sqrt(middle * middle - relative * relative);
            for (int sample = 0; sample < samples; sample++) {
                double distance = width * (crown * 0.5F) * (random.nextFloat() + 0.328);
                double angle = random.nextFloat() * 2.0 * Math.PI;
                int x = Mth.floor(distance * Math.sin(angle) + origin.getX() + 0.5);
                int z = Mth.floor(distance * Math.cos(angle) + origin.getZ() + 0.5);
                BlockPos start = new BlockPos(x, origin.getY() + layer - 1, z);
                if (firstObstruction(level, start, start.above(4), leafBlock) >= 0) continue;
                int dx = x - origin.getX();
                int dz = z - origin.getZ();
                int branchY = Math.min(trunkTop, Mth.floor(start.getY() - Math.sqrt(dx * dx + dz * dz) * slope));
                if (firstObstruction(level, origin.atY(branchY), start, leafBlock) < 0) {
                    nodes.add(new LeafNode(start, branchY));
                }
            }
        }
        return nodes;
    }

    private static int firstObstruction(WorldGenLevel level, BlockPos start, BlockPos end, Block leafBlock) {
        BlockPos delta = end.subtract(start);
        int steps = span(delta);
        for (int step = 0; steps > 0 && step <= steps; step++) {
            if (!canFill(level, linePoint(start, delta, steps, step, 0), leafBlock)) return step;
        }
        return -1;
    }

    private static void placeLogLine(BlockPos start, BlockPos end, BlockState log, Map<BlockPos, BlockState> blocks) {
        BlockPos delta = end.subtract(start);
        int steps = span(delta);
        for (int step = 0; steps > 0 && step <= steps; step++) {
            blocks.put(linePoint(start, delta, steps, step, 0.5), log);
        }
    }

    private static int span(BlockPos delta) {
        return Math.max(Math.abs(delta.getX()), Math.max(Math.abs(delta.getY()), Math.abs(delta.getZ())));
    }

    private static BlockPos linePoint(BlockPos start, BlockPos delta, int span, int step, double rounding) {
        return BlockPos.containing(start.getX() + step * ((double) delta.getX() / span) + rounding,
                start.getY() + step * ((double) delta.getY() / span) + rounding,
                start.getZ() + step * ((double) delta.getZ() / span) + rounding);
    }

    private static boolean supportsSapling(WorldGenLevel level, BlockPos origin) {
        BlockPos soil = origin.below();
        return !level.isOutsideBuildHeight(soil) && level.getBlockState(soil).canSustainPlant(level, soil, Direction.UP,
                (IPlantable) WitcheryBlocks.getRequired("witchsapling").get());
    }

    private static boolean canFill(WorldGenLevel level, BlockPos pos, Block leafBlock) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(leafBlock);
    }
}
