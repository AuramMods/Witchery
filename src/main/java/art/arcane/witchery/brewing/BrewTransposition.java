package art.arcane.witchery.brewing;

import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class BrewTransposition {
    private static final int MOVE_FLAGS = Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
    private static final List<List<Block>> ORES = List.of(
            List.of(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE),
            List.of(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE),
            List.of(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
            List.of(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE));

    private BrewTransposition() {}

    record Site(ServerLevel level, BlockPos pos) {
        Site offset(BlockPos offset) { return new Site(level, pos.offset(offset)); }
    }

    static void transpose(LivingEntity entity, int strength, @Nullable Vec3 impact, @Nullable Site destination) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        var inhibition = entity.getEffect(WitcheryEffects.ENDER_INHIBITION.get());
        if (inhibition != null && inhibition.getAmplifier() >= (destination == null ? strength : 3)) return;
        if (destination != null) {
            Vec3 arrival = Vec3.atBottomCenterOf(destination.pos());
            if (WitcheryTeleport.canArrive(entity, destination.level(), arrival)) {
                WitcheryTeleport.move(entity, destination.level(), arrival);
            }
            return;
        }

        BlockPos column = impact != null ? BlockPos.containing(impact) : entity.blockPosition();
        int range = 10 * (strength + 1);
        for (int attempt = 0; attempt < 3; attempt++) {
            column = column.offset(level.random.nextInt(2 * range) - range, 0, level.random.nextInt(2 * range) - range);
            if (!level.isInWorldBounds(column) || !level.getWorldBorder().isWithinBounds(column)) continue;
            // The original search also checked terrain beyond the currently loaded chunks.
            int ceiling = Math.min(column.getY() + 64, level.getMaxBuildHeight() - 3);
            while (column.getY() > level.getMinBuildHeight() && !level.getBlockState(column).blocksMotion()) {
                column = column.below();
            }
            while (column.getY() < ceiling) {
                BlockState surface = level.getBlockState(column);
                Vec3 arrival = Vec3.atBottomCenterOf(column.above());
                if (surface.blocksMotion() && !surface.is(Blocks.BEDROCK) && level.isEmptyBlock(column.above())
                        && level.isEmptyBlock(column.above(2)) && level.isEmptyBlock(column.above(3))
                        && WitcheryTeleport.canArrive(entity, level, arrival) && WitcheryTeleport.move(entity, level, arrival)) return;
                column = column.above();
            }
        }
    }

    static void moveTerrain(Site source, Site destination) {
        if (source.equals(destination)) return;
        List<BlockPos> offsets = new ArrayList<>();
        for (int y = 0; y < 3; y++) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    // The original shape uses the layer height in the Z term.
                    if (x * x + y * z < 9) offsets.add(new BlockPos(x, y, z));
                }
            }
        }
        boolean sameLevel = source.level() == destination.level();
        if (sameLevel) {
            BlockPos distance = destination.pos().subtract(source.pos());
            // Move the far side first so overlapping destinations cannot overwrite unread source blocks.
            offsets.sort(Comparator.comparingInt((BlockPos offset) -> offset.getX() * distance.getX()
                    + offset.getY() * distance.getY() + offset.getZ() * distance.getZ()).reversed());
        }
        Player sourceActor = FakePlayerFactory.getMinecraft(source.level());
        Player destinationActor = sameLevel ? sourceActor : FakePlayerFactory.getMinecraft(destination.level());
        var blockedSources = new HashSet<BlockPos>();
        var changed = new LinkedHashMap<Site, BlockState>();
        for (BlockPos offset : offsets) {
            Site from = source.offset(offset);
            Site to = destination.offset(offset);
            if (blockedSources.contains(to.pos()) || !moveBlock(from, to, sourceActor, destinationActor, changed)) {
                if (sameLevel) blockedSources.add(from.pos());
            }
        }
        for (var edit : changed.entrySet()) {
            Site site = edit.getKey();
            BlockState current = site.level().getBlockState(site.pos());
            site.level().markAndNotifyBlock(site.pos(), site.level().getChunkAt(site.pos()), edit.getValue(), current,
                    Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS, 512);
        }
    }

    private static boolean moveBlock(Site from, Site to, Player fromActor, Player toActor, Map<Site, BlockState> changed) {
        if (!from.level().isInWorldBounds(from.pos()) || !to.level().isInWorldBounds(to.pos())
                || !from.level().hasChunkAt(from.pos()) || !to.level().hasChunkAt(to.pos())) return false;
        BlockState original = from.level().getBlockState(from.pos());
        BlockState previous = to.level().getBlockState(to.pos());
        if (original.isAir() && previous.isAir()) return true;
        if (BrewTerrain.isProtectedTerrain(original) || BrewTerrain.isProtectedTerrain(previous)
                || !BrewTerrain.canEditIncludingAir(fromActor, from.level(), from.pos(), original)
                || !BrewTerrain.canEditIncludingAir(toActor, to.level(), to.pos(), previous)) return false;
        boolean replace = !original.equals(previous);
        BlockSnapshot snapshot = replace && !original.isAir() ? BlockSnapshot.create(to.level().dimension(), to.level(), to.pos()) : null;
        if (replace && !to.level().setBlock(to.pos(), original, MOVE_FLAGS)) return false;
        if (snapshot != null && ForgeEventFactory.onBlockPlace(toActor, snapshot, Direction.UP)
                || !original.isAir() && !from.level().setBlock(from.pos(), Blocks.AIR.defaultBlockState(), MOVE_FLAGS)) {
            if (replace) to.level().setBlock(to.pos(), previous, MOVE_FLAGS);
            return false;
        }
        if (replace) changed.putIfAbsent(to, previous);
        if (!original.isAir()) changed.putIfAbsent(from, original);
        return true;
    }

    static boolean orePulse(ServerLevel level, BlockPos center, int radius, int strength, int coven, int pulse) {
        BrewRituals.loadArea(level, center, radius + coven);
        extractOre(level, center.below((pulse - 1) * 4), radius + coven, 4, strength, null);
        WitcheryNetwork.sendTransposeOrePulse(level, center);
        return pulse < coven * (1 + strength);
    }

    static void extractOre(ServerLevel level, BlockPos center, int radius, int depth, int strength, @Nullable Player caster) {
        if (strength == 0) return;
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        int bottom = Math.max(level.getMinBuildHeight(), center.getY() - depth);
        int top = Math.min(level.getMaxBuildHeight() - 1, center.getY() + radius);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos column = center.offset(x, 0, z);
                if (!level.hasChunkAt(column) || !level.getWorldBorder().isWithinBounds(column)) continue;
                for (int y = bottom; y <= top; y++) {
                    BlockPos pos = column.atY(y);
                    BlockState state = level.getBlockState(pos);
                    if (!isOre(state.getBlock(), strength) || !BrewTerrain.canChange(actor, level, pos, state)) continue;
                    ItemEntity item = new ItemEntity(level, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                            new ItemStack(state.getBlock()));
                    if (level.addFreshEntity(item) && !level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState())) item.discard();
                }
            }
        }
    }

    private static boolean isOre(Block block, int strength) {
        for (int index = 0; index < Math.min(strength, ORES.size()); index++) {
            if (ORES.get(index).contains(block)) return true;
        }
        return false;
    }
}
