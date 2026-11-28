package art.arcane.witchery.entity;

import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

final class FlyingCompanionGoals {
    private FlyingCompanionGoals() {}

    static void register(GoalSelector selector, TamableAnimal companion) {
        selector.addGoal(3, new Defend(companion));
        selector.addGoal(4, new Tempt(companion));
        selector.addGoal(5, new Follow(companion));
        selector.addGoal(8, new Mate(companion));
        selector.addGoal(9, new Land(companion));
        selector.addGoal(10, new Wander(companion));
    }

    static final class Tempt extends Goal {
        private final Mob companion;
        private final Predicate<ItemStack> food;
        @Nullable private Player player;
        private Vec3 trackedPosition = Vec3.ZERO;
        private int cooldown;

        Tempt(TamableAnimal companion) {
            this(companion, companion::isFood);
        }

        Tempt(Mob companion, Predicate<ItemStack> food) {
            this.companion = companion;
            this.food = food;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (isTamed(companion)) return false;
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            player = companion.level().getNearestPlayer(companion, 10);
            return player != null && food.test(player.getMainHandItem());
        }

        @Override
        public boolean canContinueToUse() {
            if (player == null || !player.isAlive()) return false;
            if (companion.distanceToSqr(player) < 36) {
                if (player.distanceToSqr(trackedPosition) > 0.010000000000000002D) return false;
            } else {
                trackedPosition = player.position();
            }
            return canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            trackedPosition = player.position();
            companion.getNavigation().stop();
        }

        @Override
        public void stop() {
            player = null;
            cooldown = 100;
        }

        @Override
        public void tick() {
            if (player == null || companion.distanceToSqr(player) < 3) return;
            Vec3 course = trackedPosition.subtract(companion.position());
            if (clearCourse(companion, course)) {
                steer(companion, course, 0.05, companion.getY() < trackedPosition.y + 1 ? 0.025 : 0);
            }
            faceVelocity(companion);
        }
    }

    static final class Mate extends Goal {
        private final TamableAnimal companion;
        @Nullable private TamableAnimal partner;
        private int courtshipTicks;
        private int courseDelay;

        Mate(TamableAnimal companion) {
            this.companion = companion;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!companion.isInLove() || companion.isOrderedToSit()) return false;
            double nearest = Double.POSITIVE_INFINITY;
            partner = null;
            for (TamableAnimal candidate : companion.level().getEntitiesOfClass(TamableAnimal.class,
                    companion.getBoundingBox().inflate(8), companion::canMate)) {
                double distance = companion.distanceToSqr(candidate);
                if (distance < nearest) {
                    nearest = distance;
                    partner = candidate;
                }
            }
            return partner != null;
        }

        @Override
        public boolean canContinueToUse() {
            return partner != null && partner.isAlive() && companion.canMate(partner)
                    && !companion.isOrderedToSit() && courtshipTicks < 60;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            companion.getNavigation().stop();
        }

        @Override
        public void stop() {
            partner = null;
            courtshipTicks = 0;
            courseDelay = 0;
        }

        @Override
        public void tick() {
            if (partner == null || !companion.canMate(partner)) return;
            if (--courseDelay <= 0) {
                Vec3 course = partner.position().subtract(companion.position());
                if (clearCourse(companion, course)) steer(companion, course, 0.25, 0);
                courseDelay = 10;
            }
            faceVelocity(companion);
            if (++courtshipTicks == 60 && companion.distanceToSqr(partner) < 9
                    && companion.level() instanceof ServerLevel level) {
                companion.spawnChildFromBreeding(level, partner);
            }
        }
    }

    static void travel(Mob companion, Vec3 input) {
        if (companion.isControlledByLocalInstance()) {
            Level level = companion.level();
            float drag = 0.91F;
            float acceleration = 0.02F;
            if (companion.isInWater()) {
                drag = 0.8F;
            } else if (companion.isInLava()) {
                drag = 0.5F;
            } else if (companion.onGround()) {
                BlockPos ground = companion.blockPosition().below();
                drag *= level.getBlockState(ground).getFriction(level, ground, companion);
                acceleration = 0.1F * 0.16277136F / (drag * drag * drag);
            }
            companion.moveRelative(acceleration, input);
            companion.move(MoverType.SELF, companion.getDeltaMovement());
            companion.setDeltaMovement(companion.getDeltaMovement().scale(drag));
        }
        companion.calculateEntityAnimation(false);
    }

    static boolean clearCourse(Mob companion, Vec3 displacement) {
        double distance = (float) displacement.length();
        if (distance < 1) return true;
        Vec3 direction = displacement.scale(1 / distance);
        var bounds = companion.getBoundingBox();
        for (int step = 1; step < distance; step++) {
            if (!companion.level().noCollision(companion, bounds.move(direction.scale(step)))) return false;
        }
        return true;
    }

    static void steer(Mob companion, Vec3 displacement, double impulse, double climb) {
        double distance = (float) displacement.length();
        Vec3 acceleration = distance > 0 ? displacement.scale(impulse / distance) : Vec3.ZERO;
        companion.setDeltaMovement(companion.getDeltaMovement().add(acceleration).add(0, climb, 0));
    }

    static void faceVelocity(Mob companion) {
        Vec3 velocity = companion.getDeltaMovement();
        float heading = (float) (-Math.atan2(velocity.x, velocity.z) * Mth.RAD_TO_DEG);
        companion.setYRot(heading);
        companion.yBodyRot = heading;
    }

    private static Vec3 randomDirection(Level level, float radius) {
        return new Vec3((level.random.nextFloat() * 8 - 4) * radius,
                (level.random.nextFloat() * 2 - 1) * radius, (level.random.nextFloat() * 8 - 4) * radius);
    }

    private static boolean isTamed(Mob companion) {
        return companion instanceof TamableAnimal pet && pet.isTame();
    }

    private static boolean isSitting(Mob companion) {
        return companion instanceof TamableAnimal pet && pet.isOrderedToSit();
    }

    static final class Defend extends Goal {
        private final Mob companion;
        private int pursuitDelay;
        private int blockedPenalty;
        private int strikeDelay;

        Defend(Mob companion) {
            this.companion = companion;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = companion.getTarget();
            return !isSitting(companion) && target != null && target.isAlive() && target.level() == companion.level();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() && companion.isWithinRestriction(companion.getTarget().blockPosition());
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            companion.getNavigation().stop();
            pursuitDelay = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = companion.getTarget();
            if (target == null || !target.isAlive()) return;
            companion.getLookControl().setLookAt(target, 30, 30);
            if (--pursuitDelay <= 0) {
                pursuitDelay = blockedPenalty + 4 + companion.getRandom().nextInt(7);
                Vec3 displacement = target.position().subtract(companion.position());
                if (clearCourse(companion, displacement)) {
                    steer(companion, displacement, 0.15, 0);
                    blockedPenalty = 0;
                } else {
                    blockedPenalty += 10;
                }
                faceVelocity(companion);
            }
            strikeDelay = Math.max(0, strikeDelay - 1);
            float width = 2 * companion.getBbWidth();
            double reachSquared = width * width + target.getBbWidth();
            if (strikeDelay == 0 && companion.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ())
                    <= reachSquared) {
                strikeDelay = 20;
                if (!companion.getMainHandItem().isEmpty()) companion.swing(InteractionHand.MAIN_HAND);
                companion.doHurtTarget(target);
            }
        }
    }

    static final class Follow extends Goal {
        private final TamableAnimal companion;
        @Nullable private LivingEntity owner;
        private int followDelay;

        Follow(TamableAnimal companion) {
            this.companion = companion;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity candidate = companion.getOwner();
            if (companion.isOrderedToSit() || candidate == null || !candidate.isAlive()
                    || candidate.level() != companion.level() || companion.distanceToSqr(candidate) < 14 * 14) return false;
            owner = candidate;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return owner != null && owner.isAlive() && !companion.isOrderedToSit()
                    && companion.distanceToSqr(owner) > 5 * 5;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            followDelay = 0;
            companion.getNavigation().stop();
        }

        @Override
        public void stop() {
            owner = null;
        }

        @Override
        public void tick() {
            if (owner == null || companion.isOrderedToSit() || --followDelay > 0) return;
            followDelay = 10;
            if (owner.level() != companion.level() || companion.distanceToSqr(owner) >= 16 * 16) {
                rejoinOwner();
                return;
            }
            Vec3 course = owner.position().subtract(companion.position());
            if (clearCourse(companion, course)) {
                steer(companion, course, 0.1, companion.getY() < owner.getY() + 2 ? 0.1 : 0);
            } else {
                steer(companion, randomDirection(companion.level(), 6), 0.1, 0.1);
            }
            faceVelocity(companion);
        }

        private void rejoinOwner() {
            if (!(owner.level() instanceof ServerLevel destination)) return;
            BlockPos center = owner.blockPosition();
            // The original search advances x and z together and checks the five heights in each column.
            for (int offset = -2; offset <= 2; offset++) {
                for (int y = -2; y <= 2; y++) {
                    BlockPos pos = center.offset(offset, y, offset);
                    BlockPos ground = pos.below();
                    Vec3 arrival = Vec3.atBottomCenterOf(pos).add(0, 0.01, 0);
                    if (destination.getBlockState(ground).isFaceSturdy(destination, ground, Direction.UP)
                            && !destination.getBlockState(pos).isSolidRender(destination, pos)
                            && !destination.getBlockState(pos.above()).isSolidRender(destination, pos.above())
                            && WitcheryTeleport.canArrive(companion, destination, arrival)
                            && WitcheryTeleport.moveWithPortalCues(companion, destination, arrival)) return;
                }
            }
        }
    }

    static final class Land extends Goal {
        private final Mob companion;
        @Nullable private BlockPos perch;
        private int travelTicks;

        Land(Mob companion) {
            this.companion = companion;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return canContinueToUse() && companion.level().random.nextInt(20) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos pos = companion.blockPosition();
            return !isSitting(companion)
                    && !solidOrLeaves(BlockPos.containing(companion.getX(), companion.getY() - 0.01, companion.getZ()))
                    && !liquid(pos) && !liquid(pos.below());
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            companion.getNavigation().stop();
            travelTicks = 100;
            BlockPos origin = companion.blockPosition();
            perch = findTree(origin);
            if (perch == null) perch = findGround(origin);
        }

        @Override
        public void stop() {
            perch = null;
        }

        @Override
        public void tick() {
            if (!canContinueToUse()) return;
            Vec3 motion = companion.getDeltaMovement();
            if (perch != null && companion.distanceToSqr(perch.getX(), companion.getY(), perch.getZ()) > 1
                    && travelTicks-- > 0) {
                Vec3 course = Vec3.atLowerCornerOf(perch).subtract(companion.position());
                if (clearCourse(companion, course)) steer(companion, course, 0.05, 0);
            } else {
                companion.setDeltaMovement(motion.x, -0.1, motion.z);
            }
            faceVelocity(companion);
        }

        @Nullable
        private BlockPos findTree(BlockPos origin) {
            Level level = companion.level();
            BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
            for (int y = -3; y <= 3; y++) {
                for (int x = -16; x <= 16; x++) {
                    for (int z = -16; z <= 16; z++) {
                        probe.setWithOffset(origin, x, y, z);
                        if (level.isOutsideBuildHeight(probe) || !level.hasChunkAt(probe)
                                || !level.getBlockState(probe).is(BlockTags.LEAVES)) continue;
                        for (int above = y; above < 10; above++) {
                            probe.setWithOffset(origin, x, above, z);
                            if (level.isEmptyBlock(probe)
                                    && clearCourse(companion, Vec3.atLowerCornerOf(probe).subtract(companion.position()))) {
                                return probe.above(2);
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Nullable
        private BlockPos findGround(BlockPos origin) {
            Level level = companion.level();
            for (BlockPos pos = origin; !level.isOutsideBuildHeight(pos); pos = pos.below()) {
                if (level.isEmptyBlock(pos)) continue;
                if (!liquid(pos)) return pos.above();
                for (int attempt = 0; attempt < 10; attempt++) {
                    BlockPos edge = origin.offset(level.random.nextInt(20) - 10,
                            level.random.nextInt(6) - 3, level.random.nextInt(20) - 10);
                    if (solidOrLeaves(edge) && level.isEmptyBlock(edge.above())
                            && clearCourse(companion, Vec3.atLowerCornerOf(edge).subtract(companion.position()))) {
                        return edge.above();
                    }
                }
            }
            return null;
        }

        private boolean liquid(BlockPos pos) {
            return companion.level().getBlockState(pos).liquid();
        }

        private boolean solidOrLeaves(BlockPos pos) {
            var state = companion.level().getBlockState(pos);
            return state.blocksMotion() || state.is(BlockTags.LEAVES);
        }
    }

    static final class Wander extends Goal {
        private final Mob companion;
        private Vec3 waypoint = Vec3.ZERO;
        private int courseDelay;

        Wander(Mob companion) {
            this.companion = companion;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Level level = companion.level();
            if (isSitting(companion)) return false;
            if (!isTamed(companion) && level.getNearestPlayer(companion, 10) != null) return true;
            return companion.getNoActionTime() < 100 && companion.getRandom().nextInt(level.isDay() ? 300 : 100) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return !isSitting(companion);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Vec3 course = waypoint.subtract(companion.position());
            double distance = course.lengthSqr();
            if (distance < 1 || distance > 3600) {
                course = randomDirection(companion.level(), isTamed(companion) ? 2 : 6);
                waypoint = companion.position().add(course);
            }
            if (courseDelay-- <= 0) {
                courseDelay += companion.level().random.nextInt(2) + 2;
                if (clearCourse(companion, course)) steer(companion, course, 0.1, 0);
                else waypoint = companion.position();
            }
            faceVelocity(companion);
        }
    }
}
