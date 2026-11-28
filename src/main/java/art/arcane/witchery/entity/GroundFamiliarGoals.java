package art.arcane.witchery.entity;

import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

final class GroundFamiliarGoals {
    private GroundFamiliarGoals() {}

    static final class Sit extends SitWhenOrderedToGoal {
        private final TamableAnimal pet;

        Sit(TamableAnimal pet) {
            super(pet);
            this.pet = pet;
        }

        @Override
        public boolean canUse() {
            return pet.isTame() && pet.isOrderedToSit();
        }
    }

    static final class Follow extends Goal {
        private final TamableAnimal pet;
        private final double stopSquared;
        @Nullable private LivingEntity owner;
        private int pathDelay;
        private float waterMalus;

        Follow(TamableAnimal pet, double stopDistance) {
            this.pet = pet;
            stopSquared = stopDistance * stopDistance;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity candidate = pet.getOwner();
            if (!canFollow(candidate) || pet.distanceToSqr(candidate) < 100) return false;
            owner = candidate;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return canFollow(owner) && !pet.getNavigation().isDone() && pet.distanceToSqr(owner) > stopSquared;
        }

        private boolean canFollow(@Nullable LivingEntity candidate) {
            return candidate != null && candidate.isAlive() && !candidate.isSpectator()
                    && candidate.level() == pet.level() && !pet.isOrderedToSit() && !pet.isPassenger();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            pathDelay = 0;
            waterMalus = pet.getPathfindingMalus(BlockPathTypes.WATER);
            pet.setPathfindingMalus(BlockPathTypes.WATER, 0);
        }

        @Override
        public void stop() {
            owner = null;
            pet.getNavigation().stop();
            pet.setPathfindingMalus(BlockPathTypes.WATER, waterMalus);
        }

        @Override
        public void tick() {
            if (!canFollow(owner)) return;
            pet.getLookControl().setLookAt(owner, 10, pet.getMaxHeadXRot());
            if (--pathDelay > 0) return;
            pathDelay = 10;
            if (pet.getNavigation().moveTo(owner, 1) || pet.isLeashed() || pet.distanceToSqr(owner) < 144
                    || !(pet.level() instanceof ServerLevel level)) return;

            BlockPos center = owner.blockPosition();
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) != 2 && Math.abs(z) != 2) continue;
                    BlockPos pos = center.offset(x, 0, z);
                    BlockPos ground = pos.below();
                    Vec3 arrival = Vec3.atBottomCenterOf(pos);
                    if (level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)
                            && !level.getBlockState(pos).isSolidRender(level, pos)
                            && !level.getBlockState(pos.above()).isSolidRender(level, pos.above())
                            && WitcheryTeleport.canArrive(pet, level, arrival)) {
                        pet.moveTo(arrival.x, arrival.y, arrival.z, pet.getYRot(), pet.getXRot());
                        pet.getNavigation().stop();
                        return;
                    }
                }
            }
        }
    }
}
