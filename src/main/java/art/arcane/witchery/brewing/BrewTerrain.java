package art.arcane.witchery.brewing;

import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.event.WitcheryBlockBreak;
import art.arcane.witchery.event.WitcheryBlockPlacement;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class BrewTerrain {
    private static final List<Direction> SIDES = List.of(Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH);
    private static final List<Block> LOGS = List.of(Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG);
    private static final List<Block> LEAVES = List.of(Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES);
    private static final List<Block> PLANKS = List.of(Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS, Blocks.JUNGLE_PLANKS);
    private static final List<Block> SAPLINGS = List.of(Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING);

    private BrewTerrain() {}

    public static void webs(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        webs(brew.getOwner(), level, splashPosition(level, hit), BrewImpacts.enhanced(brew));
    }

    public static void mineWebs(ServerLevel level, BlockPos pos) {
        webs(null, level, pos, false);
    }

    private static void webs(@Nullable Entity owner, ServerLevel level, BlockPos center, boolean enhanced) {
        BlockState web = Blocks.COBWEB.defaultBlockState();
        place(owner, level, center, web, Direction.UP);
        for (Direction direction : Direction.values()) place(owner, level, center.relative(direction), web, direction);
        if (enhanced) {
            for (int x : new int[]{-1, 1}) {
                for (int z : new int[]{-1, 1}) place(owner, level, center.offset(x, 0, z), web, Direction.UP);
            }
        }
    }

    public static void thorns(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        boolean enhanced = BrewImpacts.enhanced(brew);
        if (hit instanceof BlockHitResult block) {
            if ((block.getDirection() == Direction.UP || level.getBlockState(block.getBlockPos()).is(Blocks.CACTUS))
                    && cactus(brew.getOwner(), level, block.getBlockPos(), enhanced ? 4 : 3)) return;
            brew.recoverAt(Vec3.atCenterOf(block.getBlockPos().relative(block.getDirection())));
        } else if (hit instanceof EntityHitResult entity) {
            for (Direction side : SIDES) {
                if (!cactus(brew.getOwner(), level, entity.getEntity().blockPosition().relative(side), enhanced ? 2 : 1)) {
                    brew.recoverAt(entity.getEntity().position());
                    break;
                }
            }
        }
    }

    public static void mineThorns(ServerLevel level, BlockPos pos) {
        cactus(null, level, pos, 4);
    }

    private static boolean cactus(@Nullable Entity owner, ServerLevel level, BlockPos hit, int height) {
        BlockPos base = hit;
        BlockState ground = level.getBlockState(base);
        if (!ground.blocksMotion() && !ground.is(Blocks.CACTUS)) {
            base = base.below();
            ground = level.getBlockState(base);
        }
        if (ground.is(Blocks.CACTUS)) {
            while (level.getBlockState(base.above()).is(Blocks.CACTUS)) base = base.above();
        } else if (!cactusSoil(ground) || !replace(owner, level, base, Blocks.SAND.defaultBlockState(), Direction.UP)) {
            return false;
        }
        for (int i = 1; i <= height; i++) {
            if (!place(owner, level, base.above(i), Blocks.CACTUS.defaultBlockState(), Direction.UP)) break;
        }
        return true;
    }

    private static boolean cactusSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.SNOW)
                || state.getSoundType() == SoundType.STONE || state.is(Blocks.CLAY)
                || state.is(Blocks.SPONGE) || state.is(Blocks.WET_SPONGE);
    }

    public static void vines(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        if (!(hit instanceof BlockHitResult block) || !block.getDirection().getAxis().isHorizontal()) {
            BrewImpacts.recoverBrew(brew, hit);
            return;
        }
        Direction side = block.getDirection();
        int ledgeWidth = BrewImpacts.enhanced(brew) ? 2 : 1;
        BlockPos impact = block.getBlockPos();
        growVines(brew, level, impact, side, Direction.DOWN, ledgeWidth);
        growVines(brew, level, impact.above(), side, Direction.UP, ledgeWidth);
    }

    private static void growVines(ThrownBrew brew, ServerLevel level, BlockPos support, Direction side,
                                  Direction vertical, int ledgeWidth) {
        boolean descending = vertical == Direction.DOWN;
        Direction across = descending ? side : side.getOpposite();
        int remaining = descending ? 1 : ledgeWidth;
        var face = VineBlock.getPropertyForFace(side.getOpposite());
        while (level.isInWorldBounds(support)) {
            BlockPos target = support.relative(side);
            BlockState existing = level.getBlockState(target);
            BlockState vine = existing.is(Blocks.VINE) ? existing : Blocks.VINE.defaultBlockState();
            if (!level.getBlockState(support).isFaceSturdy(level, support, side)
                    || descending && !canReplaceForPlacement(existing, vine)) {
                if (remaining == 0) break;
                support = support.relative(across);
                remaining--;
            } else {
                if (!place(brew.getOwner(), level, target, vine.setValue(face, true), side)) break;
                support = support.relative(vertical);
                remaining = ledgeWidth;
            }
        }
    }

    public static void sprouting(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        if (!(hit instanceof BlockHitResult block)) {
            BrewImpacts.recoverBrew(brew, hit);
            return;
        }
        Direction face = block.getDirection();
        BlockPos tip = growBranch(level, brew.getOwner(), block.getBlockPos(), face,
                BrewImpacts.enhanced(brew) ? 20 : 15, false);
        liftSprouting(level, face, tip, 1, brew.getBoundingBox());
    }

    public static void mineSprouting(ServerLevel level, BlockPos pos) {
        BlockPos tip = growBranch(level, null, pos, Direction.UP, 10, false);
        liftSprouting(level, Direction.UP, tip, 1, new AABB(pos));
    }

    static void sprout(ServerLevel level, BlockPos impact, Direction face, int extent, AABB bounds, @Nullable Player caster) {
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        BlockPos tip = growBranch(level, actor, impact, face, extent + 1, true);
        liftSprouting(level, face, tip, 2, bounds);
    }

    @Nullable
    private static BlockPos growBranch(ServerLevel level, @Nullable Entity owner, BlockPos impact, Direction face,
                                       int limit, boolean replaceableOnly) {
        if (!level.isInWorldBounds(impact) || replaceableOnly && !level.hasChunkAt(impact)) return null;
        BlockState source = level.getBlockState(impact);
        RandomSource random = level.getRandom();
        TargetBranch branch = branch(source, random);
        Direction.Axis axis = face.getAxis();
        BlockState log = branch.log().setValue(RotatedPillarBlock.AXIS, axis);
        BlockState leaves = branch.leaves().setValue(LeavesBlock.DISTANCE, 1)
                .setValue(LeavesBlock.PERSISTENT, axis == Direction.Axis.X);
        int start = face == Direction.UP && !source.blocksMotion() ? 0 : 1;
        BlockPos next = impact.relative(face, start);
        for (int offset = start; offset < limit; offset++, next = next.relative(face)) {
            if (!canGrowAt(level, next, replaceableOnly)) break;
            if (!placeBranchBlock(level, owner, next, log, face, replaceableOnly)) return null;

            int x = axis == Direction.Axis.X ? 0 : twigOffset(random);
            int y = axis == Direction.Axis.Y || x != 0 ? 0 : twigOffset(random);
            int z = axis == Direction.Axis.Z || x != 0 || y != 0 ? 0 : twigOffset(random);
            if (x != 0 || y != 0 || z != 0) {
                BlockPos twig = next.offset(x, y, z);
                if (canGrowAt(level, twig, replaceableOnly)) {
                    placeBranchBlock(level, owner, twig, leaves, face, replaceableOnly);
                }
            }
        }
        return next;
    }

    private static int twigOffset(RandomSource random) {
        return random.nextInt(4) == 0 ? random.nextInt(3) - 1 : 0;
    }

    private static boolean canGrowAt(ServerLevel level, BlockPos pos, boolean replaceableOnly) {
        if (!level.isInWorldBounds(pos) || replaceableOnly && !level.hasChunkAt(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return replaceableOnly ? state.canBeReplaced() : !state.blocksMotion();
    }

    private static boolean placeBranchBlock(ServerLevel level, @Nullable Entity owner, BlockPos pos, BlockState state,
                                            Direction face, boolean replaceableOnly) {
        if (!setBlock(owner, level, pos, state, face)) return false;
        if (!replaceableOnly) WitcheryNetwork.sendSproutingGrowth(level, pos);
        return true;
    }

    private static void liftSprouting(ServerLevel level, Direction face, @Nullable BlockPos tip, int landingOffset,
                                      AABB bounds) {
        if (face == Direction.UP && tip != null && !level.getBlockState(tip.above()).blocksMotion()
                && !level.getBlockState(tip.above(2)).blocksMotion()) {
            lift(level, tip.above(landingOffset), bounds.inflate(0, 2, 0));
        }
    }

    static void lift(ServerLevel level, BlockPos destination, AABB bounds) {
        Vec3 landing = Vec3.atBottomCenterOf(destination);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (WitcheryTeleport.canArrive(entity, level, landing)) WitcheryTeleport.move(entity, level, landing);
        }
    }

    private record TargetBranch(BlockState log, BlockState leaves) {}

    private static TargetBranch branch(BlockState hit, RandomSource random) {
        Block witchLog = WitcheryBlocks.getRequired("witchlog").get();
        Block witchLeaves = WitcheryBlocks.getRequired("witchleaves").get();
        if (hit.is(witchLog) || hit.is(witchLeaves) || hit.is(WitcheryBlocks.getRequired("witchwood").get())
                || hit.is(WitcheryBlocks.getRequired("witchsapling").get())) {
            var species = hit.getValue(WitchWoodType.PROPERTY);
            return new TargetBranch(witchLog.defaultBlockState().setValue(WitchWoodType.PROPERTY, species),
                    witchLeaves.defaultBlockState().setValue(WitchWoodType.PROPERTY, species));
        }
        Block source = hit.getBlock();
        for (int species = 0; species < LOGS.size(); species++) {
            if (source == LOGS.get(species) || source == LEAVES.get(species)
                    || source == PLANKS.get(species) || source == SAPLINGS.get(species)) return vanillaBranch(species);
        }
        if (hit.is(Blocks.ACACIA_PLANKS) || hit.is(Blocks.ACACIA_SAPLING)) return vanillaBranch(0);
        if (hit.is(Blocks.DARK_OAK_PLANKS) || hit.is(Blocks.DARK_OAK_SAPLING)) return vanillaBranch(1);
        if (random.nextInt(2) == 0) return vanillaBranch(random.nextInt(LOGS.size()));
        var species = WitchWoodType.values()[random.nextInt(WitchWoodType.values().length)];
        return new TargetBranch(witchLog.defaultBlockState().setValue(WitchWoodType.PROPERTY, species),
                witchLeaves.defaultBlockState().setValue(WitchWoodType.PROPERTY, species));
    }

    private static TargetBranch vanillaBranch(int species) {
        return new TargetBranch(LOGS.get(species).defaultBlockState(),
                LEAVES.get(species).defaultBlockState());
    }

    static BlockPos splashPosition(ServerLevel level, HitResult hit) {
        if (hit instanceof BlockHitResult block) {
            BlockPos pos = block.getBlockPos();
            return block.getDirection() == Direction.UP && !level.getBlockState(pos).blocksMotion()
                    ? pos : pos.relative(block.getDirection());
        }
        return hit instanceof EntityHitResult entity ? entity.getEntity().blockPosition() : BlockPos.containing(hit.getLocation());
    }

    private static boolean place(@Nullable Entity owner, ServerLevel level, BlockPos pos, BlockState state, Direction face) {
        return canReplaceForPlacement(level.getBlockState(pos), state) && replace(owner, level, pos, state, face);
    }

    private static boolean canReplaceForPlacement(BlockState previous, BlockState state) {
        return !previous.blocksMotion() || state.is(Blocks.VINE) && previous.is(BlockTags.LEAVES);
    }

    private static boolean replace(@Nullable Entity owner, ServerLevel level, BlockPos pos, BlockState state, Direction face) {
        return setBlock(owner, level, pos, state, face, true);
    }

    public static boolean setBlock(@Nullable Entity owner, ServerLevel level, BlockPos pos, BlockState state, Direction face) {
        return setBlock(owner, level, pos, state, face, false);
    }

    public static boolean placeGroup(ServerLevel level, Player actor, Direction face, BooleanSupplier writeBlocks) {
        boolean alreadyCapturing = level.captureBlockSnapshots;
        int first = level.capturedBlockSnapshots.size();
        boolean placed = false;
        List<BlockSnapshot> changes;
        level.captureBlockSnapshots = true;
        try {
            if (writeBlocks.getAsBoolean()) {
                changes = List.copyOf(level.capturedBlockSnapshots.subList(first, level.capturedBlockSnapshots.size()));
                placed = !changes.isEmpty() && changes.stream().allMatch(change ->
                        canChange(actor, level, change.getPos(), change.getReplacedBlock()))
                        && !ForgeEventFactory.onMultiBlockPlace(actor, changes, face);
            }
        } finally {
            level.captureBlockSnapshots = alreadyCapturing;
            var captured = level.capturedBlockSnapshots.subList(first, level.capturedBlockSnapshots.size());
            changes = List.copyOf(captured);
            if (!alreadyCapturing || !placed) captured.clear();
            if (!placed) WitcheryBlockPlacement.restore(level, changes);
        }
        if (placed && !alreadyCapturing) {
            WitcheryBlockPlacement.notifyPlacement(level, changes);
        }
        return placed;
    }

    static void forEachColumn(ServerLevel level, BlockPos center, int radius, Consumer<BlockPos> action) {
        int reach = radius - 1;
        if (reach == 0) {
            scanRow(level, center, 0, 0, action);
            return;
        }
        // Preserve the original scan order, including overlapping rows.
        int x = reach;
        int z = 0;
        int error = 1 - reach;
        while (x >= z) {
            scanRow(level, center, z, x, action);
            scanRow(level, center, x, z, action);
            scanRow(level, center, -z, x, action);
            scanRow(level, center, -x, z, action);
            z++;
            if (error < 0) {
                error += 2 * z + 1;
            } else {
                x--;
                error += 2 * (z - x + 1);
            }
        }
    }

    private static void scanRow(ServerLevel level, BlockPos center, int z, int width, Consumer<BlockPos> action) {
        for (int x = -width; x <= width; x++) {
            BlockPos pos = center.offset(x, 0, z);
            if (level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos) && level.hasChunkAt(pos)) {
                action.accept(pos);
            }
        }
    }

    private static boolean setBlock(@Nullable Entity owner, ServerLevel level, BlockPos pos, BlockState state,
                                    Direction face, boolean particles) {
        BlockState old = level.getBlockState(pos);
        if (old.equals(state)) return true;
        boolean replacingDispersal = old.getBlock() instanceof BrewDispersalBlock && (state.isAir()
                || state.is(WitcheryBlocks.getRequired("perpetualice").get()));
        if ((old.hasBlockEntity() && !replacingDispersal) || !canEdit(owner, level, pos, old)) return false;
        boolean changed = state.isAir() ? level.setBlockAndUpdate(pos, state)
                : WitcheryBlockPlacement.place(level, pos, state, owner, face);
        if (!changed) return false;
        if (particles) {
            level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    6, 0.25, 0.25, 0.25, 0.02);
        }
        return true;
    }

    static boolean canChange(@Nullable Entity owner, ServerLevel level, BlockPos pos, BlockState old) {
        return !old.hasBlockEntity() && canEdit(owner, level, pos, old);
    }

    public static boolean isProtectedTerrain(BlockState state) {
        return state.hasBlockEntity() || state.is(Blocks.BEDROCK) || state.is(Blocks.DRAGON_EGG)
                || state.is(WitcheryBlocks.getRequired("force").get())
                || state.is(WitcheryBlocks.getRequired("barrier").get());
    }

    static boolean canEdit(@Nullable Entity owner, ServerLevel level, BlockPos pos, BlockState old) {
        if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)
                || old.getDestroySpeed(level, pos) < 0) return false;
        return !(owner instanceof Player player) || level.mayInteract(player, pos)
                && (old.isAir() || !WitcheryBlockBreak.isCanceled(level, pos, old, player));
    }
}
