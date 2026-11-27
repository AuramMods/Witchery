package art.arcane.witchery.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

final class TormentFlightGoals {
    private TormentFlightGoals() {}

    static final class RangedAttack extends Goal {
        private static final float RANGE = 12;
        private final LordOfTorment lord;
        private LivingEntity target;
        private int nextShot = -1;
        private int nextPursuit;
        private int blockedPenalty;

        RangedAttack(LordOfTorment lord) {
            this.lord = lord;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            var candidate = lord.getTarget();
            if (candidate == null || !candidate.isAlive()) return false;
            target = candidate;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() || target != null && target.isAlive() && !lord.getNavigation().isDone();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            target = null;
            nextShot = -1;
            nextPursuit = 0;
        }

        @Override
        public void tick() {
            Vec3 delta = target.position().subtract(lord.position());
            double distanceSquared = delta.lengthSqr();
            boolean visible = lord.getSensing().hasLineOfSight(target);
            if (distanceSquared > RANGE * RANGE && --nextPursuit <= 0) {
                nextPursuit = blockedPenalty + 4 + lord.getRandom().nextInt(7);
                double distance = Math.sqrt(distanceSquared);
                if (clearCourse(target, delta.reverse())) {
                    lord.addDeltaMovement(delta.scale(0.15 / distance));
                    blockedPenalty = 0;
                } else {
                    blockedPenalty += 10;
                }
                faceMotion(lord);
                lord.getNavigation().moveTo(target, 1);
            }

            lord.getLookControl().setLookAt(target, 30, 30);
            float rangeRatio = (float) Math.sqrt(distanceSquared) / RANGE;
            if (--nextShot == 0) {
                if (distanceSquared > RANGE * RANGE || !visible) return;
                lord.performRangedAttack(target, Mth.clamp(rangeRatio, 0.1F, 1));
            }
            if (nextShot <= 0) nextShot = Mth.floor(20 + rangeRatio * 40);
        }
    }

    static final class Land extends Goal {
        private final LordOfTorment lord;
        private int remaining;
        private Vec3 target;

        Land(LordOfTorment lord) {
            this.lord = lord;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return canLand() && lord.level().getRandom().nextInt(20) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return canLand();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            remaining = 100;
            target = findGround();
        }

        @Override
        public void stop() {
            target = null;
        }

        @Override
        public void tick() {
            if (!isLanded()) {
                Vec3 delta = target == null ? Vec3.ZERO : target.subtract(lord.position());
                if (delta.horizontalDistanceSqr() > 1 && remaining-- > 0) {
                    double distance = delta.length();
                    if (clearCourse(lord, delta)) lord.addDeltaMovement(delta.scale(0.05 / distance));
                } else if (!isLiquidAt(lord.getY() - 1)) {
                    lord.setDeltaMovement(lord.getDeltaMovement().with(Direction.Axis.Y, -0.1));
                }
            }
            faceMotion(lord);
        }

        private boolean canLand() {
            return !isLanded() && !isLiquidAt(lord.getY() - 1) && !isLiquidAt(lord.getY());
        }

        private boolean isLanded() {
            BlockPos below = BlockPos.containing(lord.getX(), lord.getY() - 0.01, lord.getZ());
            return supportsLanding(lord.level().getBlockState(below));
        }

        private boolean isLiquidAt(double y) {
            BlockPos pos = BlockPos.containing(lord.getX(), y, lord.getZ());
            return !lord.level().getFluidState(pos).isEmpty();
        }

        private Vec3 findGround() {
            var level = lord.level();
            var column = lord.blockPosition().mutable();
            for (int y = column.getY(); y > level.getMinBuildHeight() + 1; y--) {
                column.setY(y);
                BlockState state = level.getBlockState(column);
                if (state.isAir()) continue;
                if (state.getFluidState().isEmpty()) return Vec3.atLowerCornerOf(column.above());
                Vec3 shore = findShore();
                if (shore != null) return shore;
            }
            return null;
        }

        private Vec3 findShore() {
            var level = lord.level();
            var random = level.getRandom();
            for (int attempt = 0; attempt < 10; attempt++) {
                BlockPos column = BlockPos.containing(lord.getX() + random.nextInt(20) - 10,
                        lord.getY() + random.nextInt(6) - 3, lord.getZ() + random.nextInt(20) - 10);
                Vec3 delta = Vec3.atLowerCornerOf(column).subtract(lord.position());
                if (supportsLanding(level.getBlockState(column)) && level.isEmptyBlock(column.above())
                        && clearCourse(lord, delta)) return Vec3.atLowerCornerOf(column.above());
            }
            return null;
        }

        private static boolean supportsLanding(BlockState state) {
            return state.is(BlockTags.LEAVES) || state.blocksMotion();
        }
    }

    static final class Wander extends Goal {
        private final LordOfTorment lord;
        private Vec3 waypoint = Vec3.ZERO;
        private int nextCourse;

        Wander(LordOfTorment lord) {
            this.lord = lord;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            var level = lord.level();
            return level.getNearestPlayer(lord, 10) != null
                    || lord.getNoActionTime() < 100 && lord.getRandom().nextInt(level.isDay() ? 300 : 100) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return lord.getRandom().nextInt(40) != 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Vec3 offset = waypoint.subtract(lord.position());
            double distanceSquared = offset.lengthSqr();
            var random = lord.level().getRandom();
            if (distanceSquared < 1 || distanceSquared > 3600) {
                waypoint = lord.position().add((random.nextFloat() * 8 - 4) * 6,
                        (random.nextFloat() * 2 - 1) * 6, (random.nextFloat() * 8 - 4) * 6);
            }
            if (nextCourse-- <= 0) {
                nextCourse += random.nextInt(2) + 2;
                double distance = Math.sqrt(distanceSquared);
                // The original keeps the previous heading for the update that picks a new waypoint.
                if (distance > 0 && clearCourse(lord, waypoint.subtract(lord.position()))) {
                    lord.addDeltaMovement(offset.scale(0.1 / distance));
                } else {
                    waypoint = lord.position();
                }
            }
            faceMotion(lord);
        }
    }

    private static boolean clearCourse(LivingEntity origin, Vec3 delta) {
        double distance = delta.length();
        if (distance == 0) return true;
        Vec3 step = delta.scale(1 / distance);
        var bounds = origin.getBoundingBox();
        for (int block = 1; block < distance; block++) {
            if (!origin.level().noCollision(origin, bounds.move(step.scale(block)))) return false;
        }
        return true;
    }

    private static void faceMotion(LordOfTorment lord) {
        Vec3 motion = lord.getDeltaMovement();
        float yaw = (float) (-Math.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG);
        lord.setYRot(yaw);
        lord.yBodyRot = yaw;
    }
}
