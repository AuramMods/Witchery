package art.arcane.witchery.brewing;

import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.entity.ThrownBrew;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
            BrewImpacts.recoverBrew(brew, level, Vec3.atCenterOf(block.getBlockPos().relative(block.getDirection())));
        } else if (hit instanceof EntityHitResult entity) {
            for (Direction side : SIDES) {
                if (!cactus(brew.getOwner(), level, entity.getEntity().blockPosition().relative(side), enhanced ? 2 : 1)) {
                    BrewImpacts.recoverBrew(brew, level, entity.getEntity().position());
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
            BrewImpacts.recoverBrew(brew, level, hit);
            return;
        }
        Direction side = block.getDirection();
        boolean enhanced = BrewImpacts.enhanced(brew);
        BlockPos impact = block.getBlockPos();
        var face = VineBlock.getPropertyForFace(side.getOpposite());
        for (Direction vertical : List.of(Direction.DOWN, Direction.UP)) {
            BlockPos support = vertical == Direction.DOWN ? impact : impact.above();
            while (level.isInWorldBounds(support)) {
                if (!level.getBlockState(support).isFaceSturdy(level, support, side) && enhanced) {
                    BlockPos step = support.relative(vertical == Direction.UP ? side.getOpposite() : side);
                    if (level.getBlockState(step).isFaceSturdy(level, step, side)) support = step;
                }
                if (!level.getBlockState(support).isFaceSturdy(level, support, side)) break;
                BlockPos target = support.relative(side);
                BlockState existing = level.getBlockState(target);
                BlockState vine = existing.is(Blocks.VINE) ? existing : Blocks.VINE.defaultBlockState();
                if (!place(brew.getOwner(), level, target, vine.setValue(face, true), side)) break;
                support = support.relative(vertical);
            }
        }
    }

    public static void sprouting(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        if (!(hit instanceof BlockHitResult block)) {
            BrewImpacts.recoverBrew(brew, level, hit);
            return;
        }
        Vec3 center = hit.getLocation();
        AABB bounds = brew.getBoundingBox().move(center.subtract(brew.position())).inflate(0, 2, 0);
        BlockPos end = growBranch(brew.getOwner(), level, block.getBlockPos(), splashPosition(level, hit), block.getDirection(),
                BrewImpacts.enhanced(brew) ? 20 : 15);
        if (end != null && block.getDirection() == Direction.UP) lift(level, end.above(), bounds);
    }

    public static void mineSprouting(ServerLevel level, BlockPos pos) {
        BlockPos end = growBranch(null, level, pos, pos, Direction.UP, 10);
        if (end != null) lift(level, end.above(2), new AABB(pos).inflate(0, 2, 0));
    }

    @Nullable
    private static BlockPos growBranch(@Nullable Entity owner, ServerLevel level, BlockPos impact, BlockPos start,
                                       Direction direction, int limit) {
        TargetBranch branch = branch(level.getBlockState(impact), level.getRandom());
        BlockState log = branch.log().setValue(RotatedPillarBlock.AXIS, direction.getAxis());
        BlockPos next = start;
        BlockPos top = null;
        for (int i = next.equals(impact) ? 0 : 1; i < limit; i++) {
            if (!place(owner, level, next, log, direction)) break;
            top = next;
            if (level.getRandom().nextInt(4) == 0) {
                Direction twig = Direction.getRandom(level.getRandom());
                if (twig.getAxis() != direction.getAxis()) place(owner, level, next.relative(twig), branch.leaves(), twig);
            }
            next = next.relative(direction);
        }
        return top;
    }

    private static void lift(ServerLevel level, BlockPos destination, AABB bounds) {
        Vec3 landing = Vec3.atBottomCenterOf(destination);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (WitcheryTeleport.canArrive(entity, level, landing)) WitcheryTeleport.move(entity, level, landing);
        }
    }

    private record TargetBranch(BlockState log, BlockState leaves) {}

    private static TargetBranch branch(BlockState hit, RandomSource random) {
        Block witchLog = WitcheryBlocks.getRequired("witchlog").get();
        Block witchLeaves = WitcheryBlocks.getRequired("witchleaves").get();
        if (hit.hasProperty(WitchWoodType.PROPERTY)) {
            var species = hit.getValue(WitchWoodType.PROPERTY);
            return new TargetBranch(witchLog.defaultBlockState().setValue(WitchWoodType.PROPERTY, species),
                    witchLeaves.defaultBlockState().setValue(WitchWoodType.PROPERTY, species));
        }
        Block source = hit.getBlock();
        for (int species = 0; species < LOGS.size(); species++) {
            if (source == LOGS.get(species) || source == LEAVES.get(species)
                    || source == PLANKS.get(species) || source == SAPLINGS.get(species)) return vanillaBranch(species);
        }
        if (random.nextBoolean()) return vanillaBranch(random.nextInt(LOGS.size()));
        var species = WitchWoodType.values()[random.nextInt(WitchWoodType.values().length)];
        return new TargetBranch(witchLog.defaultBlockState().setValue(WitchWoodType.PROPERTY, species),
                witchLeaves.defaultBlockState().setValue(WitchWoodType.PROPERTY, species));
    }

    private static TargetBranch vanillaBranch(int species) {
        return new TargetBranch(LOGS.get(species).defaultBlockState(),
                LEAVES.get(species).defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
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
        BlockState previous = level.getBlockState(pos);
        boolean replaceable = !previous.blocksMotion() || state.is(Blocks.VINE) && previous.is(BlockTags.LEAVES);
        return replaceable && replace(owner, level, pos, state, face);
    }

    private static boolean replace(@Nullable Entity owner, ServerLevel level, BlockPos pos, BlockState state, Direction face) {
        return setBlock(owner, level, pos, state, face, true);
    }

    static boolean setBlock(@Nullable Entity owner, ServerLevel level, BlockPos pos, BlockState state, Direction face) {
        return setBlock(owner, level, pos, state, face, false);
    }

    private static boolean setBlock(@Nullable Entity owner, ServerLevel level, BlockPos pos, BlockState state,
                                    Direction face, boolean particles) {
        BlockState old = level.getBlockState(pos);
        if (old.equals(state)) return true;
        boolean replacingDispersal = old.getBlock() instanceof BrewDispersalBlock && (state.isAir()
                || state.is(WitcheryBlocks.getRequired("perpetualice").get()));
        if ((old.hasBlockEntity() && !replacingDispersal) || !canEdit(owner, level, pos, old)) return false;
        if (!state.isAir() && owner != null && ForgeEventFactory.onBlockPlace(owner,
                BlockSnapshot.create(level.dimension(), level, pos), face)) return false;
        if (!level.setBlockAndUpdate(pos, state)) return false;
        if (particles) {
            level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    6, 0.25, 0.25, 0.25, 0.02);
        }
        return true;
    }

    static boolean canChange(@Nullable Entity owner, ServerLevel level, BlockPos pos, BlockState old) {
        return !old.hasBlockEntity() && canEdit(owner, level, pos, old);
    }

    static boolean isProtectedTerrain(BlockState state) {
        return state.hasBlockEntity() || state.is(Blocks.BEDROCK) || state.is(Blocks.DRAGON_EGG)
                || state.is(WitcheryBlocks.getRequired("force").get())
                || state.is(WitcheryBlocks.getRequired("barrier").get());
    }

    static boolean canEdit(@Nullable Entity owner, ServerLevel level, BlockPos pos, BlockState old) {
        if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)
                || old.getDestroySpeed(level, pos) < 0) return false;
        return !(owner instanceof Player player) || level.mayInteract(player, pos)
                && (old.isAir() || !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, old, player)));
    }
}
