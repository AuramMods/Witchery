package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

final class BrewPlantEffects {
    private BrewPlantEffects() {}

    static void webs(ServerLevel level, BlockPos impact, @Nullable Direction face, int strength, @Nullable Player caster) {
        BlockPos center = plantSite(level, impact, face, true);
        if (center == null) return;
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        BlockState web = WitcheryBlocks.getRequired("web").get().defaultBlockState();
        BlockState existing = level.getBlockState(center);
        // The original cast checks the site even when only the surrounding cells will change.
        boolean allowed = existing.canBeReplaced()
                ? BrewTerrain.setBlock(actor, level, center, web, Direction.UP)
                : BrewTerrain.canEdit(actor, level, center, existing);
        if (!allowed || strength == 0) return;
        for (Direction direction : Direction.values()) place(level, center.relative(direction), web, direction, actor);
        if (strength == 1) return;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) == 2) {
                        place(level, center.offset(x, y, z), web, Direction.UP, actor);
                    }
                }
            }
        }
    }

    static void vines(ServerLevel level, BlockPos impact, Direction face, int strength, @Nullable Player caster) {
        if (!face.getAxis().isHorizontal()) return;
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        BlockState vine = WitcheryBlocks.getRequired("vine").get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, face);
        for (int y = impact.getY(); y >= level.getMinBuildHeight(); y--) {
            BlockPos support = impact.atY(y);
            if (strength == 0 && !level.getBlockState(support).blocksMotion()) break;
            if (!place(level, support.relative(face), vine, face, actor)) break;
        }
        for (int y = impact.getY() + 1; y < level.getMaxBuildHeight(); y++) {
            BlockPos support = impact.atY(y);
            BlockPos pos = support.relative(face);
            if (!level.getBlockState(support).blocksMotion() || !available(level, pos)) break;
            BlockState found = level.getBlockState(pos);
            if ((!found.canBeReplaced() && !found.is(BlockTags.LEAVES)) || !BrewTerrain.setBlock(actor, level, pos, vine, face)) break;
        }
    }

    static void thorns(ServerLevel level, BlockPos impact, Direction face, int strength, @Nullable Player caster) {
        if (!available(level, impact)) return;
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        BlockState cactus = WitcheryBlocks.getRequired("cactus").get().defaultBlockState();
        BlockPos pos;
        if (level.getBlockState(impact).is(cactus.getBlock())) {
            pos = impact.above();
            while (available(level, pos) && level.getBlockState(pos).is(cactus.getBlock())) pos = pos.above();
        } else {
            pos = plantSite(level, impact, face, false);
        }
        if (pos == null) return;
        for (int height = 0; height < 3 + strength; height++, pos = pos.above()) {
            if (!available(level, pos) || !cactus.canSurvive(level, pos) || !place(level, pos, cactus, Direction.UP, actor)) break;
        }
    }

    @Nullable
    static BlockPos plantSite(ServerLevel level, BlockPos impact, @Nullable Direction face, boolean allowAir) {
        if (!available(level, impact)) return null;
        BlockPos base = impact;
        BlockState state = level.getBlockState(base);
        if (state.canBeReplaced() && (!allowAir || !state.isAir())) {
            do {
                base = base.below();
                if (!available(level, base)) return null;
            } while (level.getBlockState(base).canBeReplaced());
        } else if (face != null && face != Direction.UP) {
            if (face == Direction.DOWN) return null;
            BlockPos neighbor = base.relative(face);
            if (!available(level, neighbor) || !level.getBlockState(neighbor).canBeReplaced()) return null;
            base = neighbor.below();
            if (!available(level, base) || level.getBlockState(base).canBeReplaced()) return null;
        }
        return base.above();
    }

    private static boolean place(ServerLevel level, BlockPos pos, BlockState state, Direction face, Player actor) {
        return available(level, pos) && level.getBlockState(pos).canBeReplaced()
                && BrewTerrain.setBlock(actor, level, pos, state, face);
    }

    private static boolean available(ServerLevel level, BlockPos pos) {
        return level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos) && level.hasChunkAt(pos);
    }
}
