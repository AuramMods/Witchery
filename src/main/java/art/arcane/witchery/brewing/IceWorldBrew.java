package art.arcane.witchery.brewing;

import art.arcane.witchery.block.WitchDoorBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

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
        int limit = reach * reach - 1;
        var area = BlockPos.betweenClosed(center.offset(-reach, -reach, -reach), center.offset(reach, reach, reach));
        for (BlockPos pos : area) {
            if (inRange(center, pos, limit) && level.hasChunkAt(pos) && level.getBlockState(pos).is(Blocks.OAK_DOOR)) {
                var state = level.getBlockState(pos);
                BlockPos base = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
                freezeDoor(level, base, caster);
            }
        }
        for (BlockPos pos : area) {
            if (!inRange(center, pos, limit) || !level.hasChunkAt(pos)) continue;
            BlockState replacement = replacement(level.getBlockState(pos));
            if (replacement == null) continue;
            replacement = Block.updateFromNeighbourShapes(replacement, level, pos);
            if (!replacement.isAir() && BrewTerrain.setBlock(caster, level, pos, replacement, Direction.UP)) particles(level, pos);
        }
        level.playSound(null, center, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.5F, 2);
    }

    private static boolean inRange(BlockPos center, BlockPos pos, int limit) {
        return limit == 0 ? pos.equals(center) : pos.distSqr(center) < limit;
    }

    private static void freezeDoor(ServerLevel level, BlockPos base, @Nullable Player caster) {
        BlockPos top = base.above();
        BlockState bottomState = level.getBlockState(base);
        BlockState topState = level.getBlockState(top);
        if (!bottomState.is(Blocks.OAK_DOOR) || bottomState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER
                || !topState.is(Blocks.OAK_DOOR) || topState.getValue(DoorBlock.HALF) != DoubleBlockHalf.UPPER
                || !BrewTerrain.canChange(caster, level, base, bottomState) || !BrewTerrain.canChange(caster, level, top, topState)) return;
        Block door = block("icedoor");
        if (WitchDoorBlock.replacePair(level, base, door.withPropertiesOf(bottomState),
                door.withPropertiesOf(topState), caster, Direction.UP)) particles(level, base);
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
            return block("iceslab").withPropertiesOf(state);
        }
        if (state.is(block("witchwooddoubleslab"))) {
            return block("iceslab").defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
        }
        if (state.is(Blocks.OAK_FENCE) || state.is(Blocks.COBBLESTONE_WALL) || state.is(Blocks.MOSSY_COBBLESTONE_WALL)) {
            return block("icefence").withPropertiesOf(state);
        }
        if (state.is(Blocks.OAK_FENCE_GATE)) return block("icefencegate").withPropertiesOf(state);
        if (state.is(block("stockade"))) return block("icestockade").withPropertiesOf(state);
        return null;
    }

    private static Block block(String id) {
        return WitcheryBlocks.getRequired(id).get();
    }

    private static void particles(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                16, 0.25, 0.25, 0.25, 0);
    }
}
