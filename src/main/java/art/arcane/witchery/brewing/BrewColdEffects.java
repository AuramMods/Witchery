package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.SpiritFluidBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.ritual.IceShell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.function.Predicate;

final class BrewColdEffects {
    static final TagKey<EntityType<?>> SHELL_RESISTANT = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "ice_shell_resistant"));
    private static final Direction[] FREEZE_ORDER = {
            Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH, Direction.UP, Direction.DOWN
    };

    private BrewColdEffects() {}

    static void freezeWater(ServerLevel level, BlockPos center, int strength, @Nullable Player caster) {
        Player actor = caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
        if (!BrewTerrain.canEdit(actor, level, center, level.getBlockState(center))) return;
        int radius = 2 + 2 * strength;
        freezeConnectedWater(level, center, actor, pos -> pos.distSqr(center) < radius * radius);
    }

    static void freezeConnectedWater(ServerLevel level, BlockPos center, @Nullable Entity actor,
                                     Predicate<BlockPos> canSpreadFrom) {
        var pending = new ArrayDeque<FreezeStep>();
        var visited = new HashSet<BlockPos>();
        if (canSpreadFrom.test(center)) visitNeighbors(pending, center);
        while (!pending.isEmpty()) {
            FreezeStep step = pending.pop();
            BlockPos pos = step.pos();
            if (visited.add(pos) && isLegacyWater(level.getBlockState(pos))
                    && BrewTerrain.setBlock(actor, level, pos, Blocks.ICE.defaultBlockState(), step.face())
                    && canSpreadFrom.test(pos)) {
                visitNeighbors(pending, pos);
            }
        }
    }

    private static void visitNeighbors(ArrayDeque<FreezeStep> pending, BlockPos pos) {
        // Reverse the pushes so the stack visits the same depth-first path as the original recursion.
        for (int i = FREEZE_ORDER.length - 1; i >= 0; i--) {
            Direction face = FREEZE_ORDER[i];
            pending.push(new FreezeStep(pos.relative(face), face));
        }
    }

    static boolean isLegacyWater(BlockState state) {
        return state.is(Blocks.WATER) || state.getBlock() instanceof SpiritFluidBlock
                || state.getBlock() instanceof BrewDispersalBlock brew && !brew.isGas();
    }

    private record FreezeStep(BlockPos pos, Direction face) {}

    static void shellOnImpact(ServerLevel level, BlockPos impact, Direction face, int strength, @Nullable Player caster) {
        BlockPos base = impact;
        if (level.getBlockState(base).canBeReplaced()) {
            do {
                base = base.below();
            } while (base.getY() >= level.getMinBuildHeight() && level.getBlockState(base).canBeReplaced());
            if (base.getY() < level.getMinBuildHeight()) return;
        } else if (face.getAxis().isHorizontal()) {
            BlockPos adjacent = base.relative(face);
            if (!level.getBlockState(adjacent).canBeReplaced() || level.getBlockState(adjacent.below()).canBeReplaced()) return;
            base = adjacent.below();
        } else if (face == Direction.DOWN) {
            return;
        }
        BlockPos center = base.above();
        Player actor = caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
        if (BrewTerrain.canEdit(actor, level, center, level.getBlockState(center))) {
            shell(level, center, strength, actor);
        }
    }

    static void shell(ServerLevel level, BlockPos center, int strength, @Nullable Player caster) {
        Player actor = caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
        int radius = strength + (strength > 3 ? 2 : 1);
        BlockState ice = WitcheryBlocks.getRequired("perpetualice").get().defaultBlockState();
        for (BlockPos offset : IceShell.outline(radius - 1)) {
            BlockPos pos = center.offset(offset);
            if (level.getBlockState(pos).canBeReplaced()) BrewTerrain.setBlock(actor, level, pos, ice, Direction.UP);
        }
        IceShell.forEachDrainCell(level, center, radius, ice, pos -> {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.WATER) || state.getBlock() instanceof BrewDispersalBlock) {
                BrewTerrain.setBlock(actor, level, pos, Blocks.AIR.defaultBlockState(), Direction.UP);
            }
        });
    }

    static void wetResistantTarget(ServerLevel level, BlockPos pos, @Nullable Player caster) {
        Entity actor = caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
        if (!level.dimensionType().ultraWarm() && level.getBlockState(pos).canBeReplaced()) {
            BrewTerrain.setBlock(actor, level, pos, Blocks.WATER.defaultBlockState(), Direction.UP);
        }
    }
}
