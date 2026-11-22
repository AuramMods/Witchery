package art.arcane.witchery.ritual;

import art.arcane.witchery.block.entity.FetishBlockEntity;
import art.arcane.witchery.event.WitcheryBlockBreak;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

public final class EarthRites {
    static final int FISSURE_PULSES = 55;
    static final int RISE_PULSES = 7;
    private static final int[] STEP_X = {0, 1, 1, 1, 0, -1, -1, -1};
    private static final int[] STEP_Z = {-1, -1, 0, 1, 1, 1, 0, -1};
    private static final int MOVE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_MOVE_BY_PISTON;

    private EarthRites() {}

    public record Fissure(long seed, int heading) {
        public Fissure {
            heading = Math.floorMod(heading, 8);
        }

        public static Fissure start(BlockPos heart, BlockPos offering, long seed) {
            double yaw = Math.atan2(heart.getZ() - offering.getZ(), heart.getX() - offering.getX());
            int octant = Mth.ceil(yaw / (Math.PI / 4) - 0.5);
            return new Fissure(seed, octant + 6);
        }

        void cut(ServerLevel level, BlockPos origin, int stage, int coven, @Nullable ServerPlayer caster) {
            RandomSource path = RandomSource.create(seed);
            BlockPos center = origin.below();
            int direction = heading;
            // The first four outbound positions leave the casting circle intact.
            for (int step = 0; step < stage + 4; step++) {
                int turn = path.nextInt(Math.max(20 - step / 2, 6));
                if (turn < 2) direction = Math.floorMod(direction + (turn == 0 ? 1 : -1), 8);
                center = center.offset(STEP_X[direction], 0, STEP_Z[direction]);
            }
            int radius = 1 + (coven > 2 ? 2 : 0) + (path.nextInt(3) == 0 ? 1 : 0);
            int depth = 8 + path.nextInt(5);
            int bottom = Math.max(level.getMinBuildHeight(), center.getY() - depth + 1);
            ServerPlayer actor = caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
            int[] rows = circleRows(radius);
            for (int z = -radius; z <= radius; z++) {
                int width = rows[Math.abs(z)];
                for (int x = -width; x <= width; x++) {
                    BlockPos column = center.offset(x, 0, z);
                    if (!level.getWorldBorder().isWithinBounds(column)) continue;
                    for (int y = center.getY(); y >= bottom; y--) {
                        BlockPos pos = column.atY(y);
                        BlockState state = level.getBlockState(pos);
                        if (!state.isAir() && !state.hasBlockEntity() && !immovable(level, pos, state)
                                && permitsBreak(level, pos, state, actor)) level.removeBlock(pos, false);
                    }
                }
            }
        }
    }

    static void raise(ServerLevel level, BlockPos center, int baseRadius, int stage, int coven,
                      @Nullable ServerPlayer caster) {
        if (stage == 1) LandscapeRites.playRiteStart(level, center);
        ServerPlayer actor = caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
        int radius = baseRadius + 2 * coven;
        int airSpace = 2 * baseRadius;
        int bottom = Math.max(level.getMinBuildHeight(), center.getY() - 8);
        int top = Math.min(level.getMaxBuildHeight() - 2, center.getY() + airSpace);
        int[] rows = circleRows(radius);
        for (int z = -radius; z <= radius; z++) {
            int width = rows[Math.abs(z)];
            for (int x = -width; x <= width; x++) {
                BlockPos column = center.offset(x, 0, z);
                if (!level.getWorldBorder().isWithinBounds(column)) continue;
                boolean edge = Math.abs(z) == radius || Math.abs(x) == width;
                for (int y = top; y >= bottom; y--) {
                    BlockPos from = column.atY(y);
                    BlockState state = level.getBlockState(from);
                    if (state.isAir() || edge && y != center.getY() - 1 && level.random.nextInt(7) == 0) continue;
                    shiftUp(level, from, state, actor);
                }
            }
        }
        liftEntities(level, center, radius, airSpace);
    }

    private static void shiftUp(ServerLevel level, BlockPos from, BlockState state, ServerPlayer actor) {
        BlockPos to = from.above();
        BlockPos below = from.below();
        if (!level.isEmptyBlock(to) || immovable(level, from, state)
                || immovable(level, below, level.getBlockState(below)) || !permitsBreak(level, from, state, actor)
                || !level.mayInteract(actor, to)) return;

        var original = level.getBlockEntity(from);
        if (original instanceof FetishBlockEntity fetish && fetish.isSpectral()) return;
        var destination = BlockSnapshot.create(level.dimension(), level, to);
        if (!level.setBlock(to, state, MOVE_FLAGS)) return;
        if (ForgeEventFactory.onBlockPlace(actor, destination, Direction.UP) || !level.getBlockState(from).equals(state)) {
            level.removeBlockEntity(to);
            level.setBlock(to, destination.getReplacedBlock(), MOVE_FLAGS);
            return;
        }
        if (original != null) {
            var moved = level.getBlockEntity(to);
            if (moved == null || moved.getType() != original.getType()) {
                level.removeBlockEntity(to);
                level.setBlock(to, Blocks.AIR.defaultBlockState(), MOVE_FLAGS);
                return;
            }
            // Remove the old block entity before onRemove can drop its inventory or refund its offerings.
            moved.load(original.saveWithoutMetadata());
            moved.setChanged();
            level.removeBlockEntity(from);
        }
        level.setBlock(from, Blocks.AIR.defaultBlockState(), MOVE_FLAGS);
    }

    private static boolean immovable(ServerLevel level, BlockPos pos, BlockState state) {
        return state.getDestroySpeed(level, pos) < 0 || state.is(WitcheryBlocks.getRequired("altar").get())
                || state.is(WitcheryBlocks.getRequired("voidbramble").get())
                || state.is(WitcheryBlocks.getRequired("barrier").get()) || state.is(WitcheryBlocks.getRequired("force").get());
    }

    private static boolean permitsBreak(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer actor) {
        return level.mayInteract(actor, pos) && !WitcheryBlockBreak.isCanceled(level, pos, state, actor);
    }

    private static void liftEntities(ServerLevel level, BlockPos center, int radius, int airSpace) {
        AABB space = new AABB(center).inflate(radius, 0, radius).expandTowards(0, airSpace - 1, 0);
        for (var entity : level.getEntitiesOfClass(Entity.class, space, entity -> entity.isAlive() && !entity.isPassenger())) {
            double x = entity.getX() - center.getX() - 0.5;
            double z = entity.getZ() - center.getZ() - 0.5;
            if (x * x + z * z > radius * radius || !level.noCollision(entity, entity.getBoundingBox().move(0, 1, 0))) continue;
            entity.teleportTo(entity.getX(), entity.getY() + 1, entity.getZ());
            entity.resetFallDistance();
        }
    }

    private static int[] circleRows(int radius) {
        int[] rows = new int[radius + 1];
        int x = radius;
        int z = 0;
        int error = 1 - radius;
        while (x >= z) {
            rows[z] = Math.max(rows[z], x);
            rows[x] = Math.max(rows[x], z);
            z++;
            if (error < 0) {
                error += 2 * z + 1;
            } else {
                x--;
                error += 2 * (z - x + 1);
            }
        }
        return rows;
    }
}
