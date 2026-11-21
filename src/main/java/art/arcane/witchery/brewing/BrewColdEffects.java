package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
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

    private BrewColdEffects() {}

    static void freezeWater(ServerLevel level, BlockPos center, int strength, @Nullable Player caster) {
        Player actor = caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
        if (!BrewTerrain.canEdit(actor, level, center, level.getBlockState(center))) return;
        int radius = 2 + 2 * strength;
        freezeConnectedWater(level, center, actor, pos -> pos.distSqr(center) < radius * radius);
    }

    static void freezeConnectedWater(ServerLevel level, BlockPos center, @Nullable Entity actor,
                                     Predicate<BlockPos> canSpreadFrom) {
        var queue = new ArrayDeque<BlockPos>();
        var visited = new HashSet<BlockPos>();
        queue.add(center);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            if (!canSpreadFrom.test(current)) continue;
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (visited.add(next) && level.getBlockState(next).is(Blocks.WATER)
                        && BrewTerrain.setBlock(actor, level, next, Blocks.ICE.defaultBlockState(), direction)) {
                    queue.addLast(next);
                }
            }
        }
    }

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
        int radius = strength + (strength > 3 ? 1 : 0);
        BlockState ice = WitcheryBlocks.getRequired("perpetualice").get().defaultBlockState();
        for (BlockPos offset : IceShell.outline(radius)) {
            BlockPos pos = center.offset(offset);
            if (level.getBlockState(pos).canBeReplaced()) BrewTerrain.setBlock(actor, level, pos, ice, Direction.UP);
        }
        var gas = WitcheryBlocks.getRequired("brewgas").get();
        var liquid = WitcheryBlocks.getRequired("brewliquid").get();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (pos.distSqr(center) >= radius * radius) continue;
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.WATER) || state.is(gas) || state.is(liquid)) {
                BrewTerrain.setBlock(actor, level, pos, Blocks.AIR.defaultBlockState(), Direction.UP);
            }
        }
    }

    static void wetResistantTarget(ServerLevel level, BlockPos pos, @Nullable Player caster) {
        Entity actor = caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
        wetTarget(level, pos, actor);
    }

    static void wetTarget(ServerLevel level, BlockPos pos, @Nullable Entity actor) {
        if (!level.dimensionType().ultraWarm() && level.getBlockState(pos).canBeReplaced()) {
            BrewTerrain.setBlock(actor, level, pos, Blocks.WATER.defaultBlockState(), Direction.UP);
        }
    }
}
