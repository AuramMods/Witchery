package art.arcane.witchery.brewing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class BrewLevelling {
    private static final List<Block> FILL_ORDER = List.of(
            Blocks.END_STONE, Blocks.NETHERRACK, Blocks.SANDSTONE, Blocks.SAND, Blocks.STONE, Blocks.DIRT);

    private BrewLevelling() {}

    static void levelTerrain(ServerLevel level, BlockPos impact, int radius, int strength, boolean ritual,
                             @Nullable Player caster) {
        BlockPos center = ritual ? impact.below() : impact;
        if (!level.isInWorldBounds(center) || !level.hasChunkAt(center)) return;
        int reach = ritual ? radius + (strength + 1) * 3 : radius;
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        Map<Block, Integer> materials = new HashMap<>();
        Block seed = fillMaterial(level.getBlockState(center));
        if (seed != null) materials.put(seed, ritual ? 64 + 32 * strength : 16);

        int top = Math.min(level.getMaxBuildHeight() - 1, center.getY() + 3 + strength);
        BrewTerrain.forEachColumn(level, center, reach, column -> {
            for (int y = center.getY() + 1; y <= top; y++) {
                BlockPos pos = column.atY(y);
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || BrewTerrain.isProtectedTerrain(state)
                        || !BrewTerrain.setBlock(actor, level, pos, Blocks.AIR.defaultBlockState(), Direction.UP)) continue;
                Block fill = fillMaterial(state);
                materials.merge(fill != null ? fill : Blocks.DIRT, 1, Integer::sum);
                smoke(level, pos);
            }
        });

        int bottom = Math.max(level.getMinBuildHeight(), center.getY() - 4 - strength);
        for (int y = center.getY(); y >= bottom && !materials.isEmpty(); y--) {
            BrewTerrain.forEachColumn(level, center.atY(y), reach, pos -> fill(level, pos, actor, materials));
        }
        level.playSound(null, center, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 2);
    }

    private static void fill(ServerLevel level, BlockPos pos, Player actor, Map<Block, Integer> materials) {
        if (materials.isEmpty() || !level.getBlockState(pos).canBeReplaced()) return;
        for (Block block : FILL_ORDER) {
            Integer count = materials.get(block);
            if (count == null) continue;
            if (BrewTerrain.setBlock(actor, level, pos, block.defaultBlockState(), Direction.UP)) {
                if (count == 1) materials.remove(block);
                else materials.put(block, count - 1);
                smoke(level, pos);
            }
            return;
        }
    }

    @Nullable
    private static Block fillMaterial(BlockState state) {
        if (state.is(Blocks.RED_SAND)) return Blocks.SAND;
        if (state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)) return Blocks.DIRT;
        if (state.is(Blocks.CUT_SANDSTONE) || state.is(Blocks.CHISELED_SANDSTONE)) return Blocks.SANDSTONE;
        Block block = state.getBlock();
        return FILL_ORDER.contains(block) ? block : null;
    }

    private static void smoke(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                16, 0.5, 0.5, 0.5, 0);
    }
}
