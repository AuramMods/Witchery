package art.arcane.witchery.brewing;

import art.arcane.witchery.event.WitcheryBlockPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

final class BrewLandscape {
    private BrewLandscape() {}

    static void blight(ServerLevel level, BlockPos impact, int radius, @Nullable Player caster) {
        if (!level.isInWorldBounds(impact) || !level.hasChunkAt(impact)) return;
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        BlockPos center = level.getBlockState(impact).canBeReplaced() ? impact : impact.above();
        BrewTerrain.forEachColumn(level, center, radius, pos -> blightSurface(level, pos, actor));
    }

    private static void blightSurface(ServerLevel level, BlockPos pos, Player actor) {
        if (!level.isInWorldBounds(pos) || !level.hasChunkAt(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (!BrewTerrain.canEditIncludingAir(actor, level, pos, state) || state.hasBlockEntity()) return;
        BlockPos soil = pos;
        boolean shortGrass = state.is(Blocks.GRASS) || state.is(Blocks.FERN);
        if (shortGrass) {
            if (!level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState())) return;
            soil = pos.below();
        } else if (withers(state)) {
            if (!WitcheryBlockPlacement.place(level, pos, Blocks.DEAD_BUSH.defaultBlockState(), actor, Direction.UP)) return;
            soil = pos.below();
        } else if (state.is(Blocks.FARMLAND)) {
            WitcheryBlockPlacement.place(level, pos, Blocks.SAND.defaultBlockState(), actor, Direction.UP);
            return;
        } else if (!state.blocksMotion()) {
            soil = pos.below();
        }
        BlockState ground = level.getBlockState(soil);
        if (ground.is(Blocks.DIRT) || ground.is(Blocks.COARSE_DIRT) || ground.is(Blocks.PODZOL)
                || ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.MYCELIUM) || ground.is(Blocks.FARMLAND)) {
            int roll = level.random.nextInt(5);
            if (roll < 2) {
                Block replacement = roll == 0 ? Blocks.SAND : Blocks.DIRT;
                if (soil.equals(pos)) {
                    WitcheryBlockPlacement.place(level, soil, replacement.defaultBlockState(), actor, Direction.UP);
                } else {
                    BrewTerrain.setBlock(actor, level, soil, replacement.defaultBlockState(), Direction.UP);
                }
            }
        }
    }

    private static boolean withers(BlockState state) {
        return state.is(BlockTags.SMALL_FLOWERS) || state.is(Blocks.CARROTS) || state.is(Blocks.WHEAT)
                || state.is(Blocks.POTATOES) || state.is(Blocks.PUMPKIN_STEM) || state.is(Blocks.MELON_STEM)
                || state.is(Blocks.ATTACHED_PUMPKIN_STEM) || state.is(Blocks.ATTACHED_MELON_STEM)
                || state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN)
                || state.is(Blocks.SUNFLOWER) || state.is(Blocks.LILAC) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.LARGE_FERN) || state.is(Blocks.ROSE_BUSH) || state.is(Blocks.PEONY);
    }

    static void raiseArea(ServerLevel level, BlockPos impact, int radius, @Nullable Player caster) {
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz < radius * radius) {
                    raiseColumn(level, impact.offset(dx, 0, dz), radius, actor);
                }
            }
        }
    }

    static void raiseColumn(ServerLevel level, BlockPos base, int height, @Nullable Player caster) {
        if (!level.isInWorldBounds(base) || !level.hasChunkAt(base)) return;
        BlockPos surface = base;
        while (surface.getY() < level.getMaxBuildHeight() - 1 && !level.isEmptyBlock(surface.above())) {
            surface = surface.above();
        }
        BlockPos bottom = surface.below(height - 1);
        BlockPos top = surface.above(height);
        if (!level.isInWorldBounds(bottom) || !level.isInWorldBounds(top)) return;
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        BlockState surfaceState = level.getBlockState(surface);
        if (surfaceState.isAir() && !BrewTerrain.canEditIncludingAir(actor, level, surface, surfaceState)) return;
        boolean empty = true;
        for (BlockPos pos : BlockPos.betweenClosed(bottom, top)) {
            BlockState state = level.getBlockState(pos);
            if (BrewTerrain.isProtectedTerrain(state)) return;
            empty &= state.isAir();
        }
        if (empty || BrewTerrain.placeGroup(level, actor, Direction.UP, () -> shiftColumn(level, bottom, height))) {
            BrewTerrain.lift(level, top.above(), new AABB(surface).expandTowards(0, 1, 0));
        }
    }

    private static boolean shiftColumn(ServerLevel level, BlockPos bottom, int height) {
        BlockState air = Blocks.AIR.defaultBlockState();
        int flags = Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS;
        for (int i = 0; i < height; i++) {
            BlockPos source = bottom.above(i);
            BlockPos destination = source.above(height);
            BlockState state = level.getBlockState(source);
            if (!state.equals(level.getBlockState(destination)) && !level.setBlock(destination, state, flags)) return false;
        }
        for (int i = 0; i < height; i++) {
            BlockPos source = bottom.above(i);
            if (!level.isEmptyBlock(source) && !level.setBlock(source, air, flags)) return false;
        }
        return true;
    }
}
