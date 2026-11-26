package art.arcane.witchery.brewing;

import art.arcane.witchery.block.WitchDoorBlock;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

final class IceWorldBrew {
    private static final Set<Block> SOIL = Set.of(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.GRASS_BLOCK,
            Blocks.MYCELIUM, Blocks.SAND, Blocks.RED_SAND);
    private static final Set<Block> MASONRY = Set.of(Blocks.STONE, Blocks.STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS,
            Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.BRICKS, Blocks.SANDSTONE,
            Blocks.CUT_SANDSTONE, Blocks.CHISELED_SANDSTONE);
    private static final Set<Block> STONE_SLABS = Set.of(Blocks.SMOOTH_STONE_SLAB, Blocks.SANDSTONE_SLAB,
            Blocks.PETRIFIED_OAK_SLAB, Blocks.COBBLESTONE_SLAB, Blocks.BRICK_SLAB, Blocks.STONE_BRICK_SLAB,
            Blocks.NETHER_BRICK_SLAB, Blocks.QUARTZ_SLAB);

    private IceWorldBrew() {}

    static void freeze(ServerLevel level, BlockPos center, int radius, @Nullable Player caster) {
        int reach = Math.max(1, Mth.ceil(radius * 1.5));
        forEachCell(level, center, reach, pos -> {
            var state = level.getBlockState(pos);
            if (state.is(Blocks.OAK_DOOR)) {
                freezeDoor(level, WitchDoorBlock.lowerHalf(pos, state), caster);
            }
        });
        forEachCell(level, center, reach, pos -> {
            BlockState replacement = replacement(level.getBlockState(pos));
            if (replacement == null) return;
            replacement = Block.updateFromNeighbourShapes(replacement, level, pos);
            if (!replacement.isAir() && BrewTerrain.setBlock(caster, level, pos, replacement, Direction.UP)) {
                WitcheryNetwork.sendBrewConversionSmoke(level, pos);
            }
        });
        level.playSound(null, center.getX(), center.getY(), center.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.5F, 2.0F);
    }

    private static void forEachCell(ServerLevel level, BlockPos center, int reach, Consumer<BlockPos> action) {
        int limit = Math.max(1, reach * reach - 1);
        for (int x = -reach; x <= reach; x++) {
            for (int z = -reach; z <= reach; z++) {
                for (int y = -reach; y <= reach; y++) {
                    if (x * x + y * y + z * z >= limit) continue;
                    BlockPos pos = center.offset(x, y, z);
                    if (level.isInWorldBounds(pos) && level.hasChunkAt(pos)) action.accept(pos);
                }
            }
        }
    }

    private static void freezeDoor(ServerLevel level, BlockPos base, @Nullable Player caster) {
        BlockPos top = base.above();
        BlockState bottomState = level.getBlockState(base);
        BlockState topState = level.getBlockState(top);
        if (!bottomState.is(Blocks.OAK_DOOR) || bottomState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER
                || !topState.is(Blocks.OAK_DOOR) || topState.getValue(DoorBlock.HALF) != DoubleBlockHalf.UPPER
                || !BrewTerrain.canChange(caster, level, base, bottomState) || !BrewTerrain.canChange(caster, level, top, topState)) return;
        Block door = block("icedoor");
        Direction facing = bottomState.getValue(DoorBlock.FACING);
        BlockState lower = door.defaultBlockState().setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HINGE, hinge(level, base, facing, door));
        if (WitchDoorBlock.replacePair(level, base, lower, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                caster, Direction.UP)) WitcheryNetwork.sendBrewConversionSmoke(level, base);
    }

    private static DoorHingeSide hinge(ServerLevel level, BlockPos base, Direction facing, Block door) {
        BlockPos left = base.relative(facing.getCounterClockWise());
        BlockPos right = base.relative(facing.getClockWise());
        boolean leftDoor = level.getBlockState(left).is(door) || level.getBlockState(left.above()).is(door);
        boolean rightDoor = level.getBlockState(right).is(door) || level.getBlockState(right.above()).is(door);
        return leftDoor && !rightDoor || solidBlocks(level, right) > solidBlocks(level, left)
                ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
    }

    private static int solidBlocks(ServerLevel level, BlockPos base) {
        BlockPos top = base.above();
        return (level.getBlockState(base).isRedstoneConductor(level, base) ? 1 : 0)
                + (level.getBlockState(top).isRedstoneConductor(level, top) ? 1 : 0);
    }

    @Nullable
    private static BlockState replacement(BlockState state) {
        Block original = state.getBlock();
        if (SOIL.contains(original)) return Blocks.SNOW_BLOCK.defaultBlockState();
        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE) || state.is(BlockTags.LOGS)) {
            return Blocks.PACKED_ICE.defaultBlockState();
        }
        if (MASONRY.contains(original) || state.is(BlockTags.PLANKS) || state.is(BlockTags.LEAVES)) {
            return block("perpetualice").defaultBlockState();
        }
        if (state.is(Blocks.STONE_PRESSURE_PLATE) || state.is(Blocks.OAK_PRESSURE_PLATE)) {
            return block("icepressureplate").defaultBlockState();
        }
        if (state.is(BlockTags.WOODEN_STAIRS) || state.is(Blocks.COBBLESTONE_STAIRS) || state.is(Blocks.BRICK_STAIRS)
                || state.is(Blocks.STONE_BRICK_STAIRS) || state.is(Blocks.SANDSTONE_STAIRS)) {
            return block("icestairs").withPropertiesOf(state);
        }
        if (STONE_SLABS.contains(original) || state.is(BlockTags.WOODEN_SLABS) || state.is(block("witchwoodslab"))) {
            var ice = block("iceslab").withPropertiesOf(state);
            return ice.getValue(SlabBlock.TYPE) == SlabType.TOP ? ice.setValue(SlabBlock.TYPE, SlabType.BOTTOM) : ice;
        }
        if (state.is(block("witchwooddoubleslab"))) {
            return block("iceslab").defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
        }
        if (state.is(Blocks.OAK_FENCE) || state.is(Blocks.COBBLESTONE_WALL) || state.is(Blocks.MOSSY_COBBLESTONE_WALL)) {
            return block("icefence").withPropertiesOf(state);
        }
        if (state.is(Blocks.OAK_FENCE_GATE)) {
            return block("icefencegate").defaultBlockState().setValue(FenceGateBlock.FACING, Direction.SOUTH);
        }
        if (state.is(block("stockade"))) return block("icestockade").withPropertiesOf(state);
        return null;
    }

    private static Block block(String id) {
        return WitcheryBlocks.getRequired(id).get();
    }

}
