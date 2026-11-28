package art.arcane.witchery.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

final class ReachableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    ReachableTargetGoal(Mob mob, Class<T> type, boolean mustSee, @Nullable Predicate<LivingEntity> filter) {
        super(mob, type, 0, mustSee, false, filter);
        if (!mustSee) targetConditions.ignoreLineOfSight();
    }

    @Override
    protected void findTarget() {
        double range = getFollowDistance();
        var search = getTargetSearchArea(range);
        targetConditions.range(range);
        Stream<? extends LivingEntity> candidates = targetType == Player.class
                ? mob.level().players().stream().filter(player -> search.intersects(player.getBoundingBox()))
                : mob.level().getEntitiesOfClass(targetType, search).stream();
        // The original acquisition only considers candidates whose navigation can reach them.
        target = candidates.filter(candidate -> mob.isWithinRestriction(candidate.blockPosition())
                        && targetConditions.test(mob, candidate))
                .sorted(Comparator.comparingDouble(mob::distanceToSqr))
                .filter(this::canReach)
                .findFirst().orElse(null);
    }

    private boolean canReach(LivingEntity candidate) {
        var path = mob.getNavigation().createPath(candidate, 0);
        var end = path == null ? null : path.getEndNode();
        if (end == null) return false;
        int dx = end.x - candidate.getBlockX();
        int dz = end.z - candidate.getBlockZ();
        return dx * dx + dz * dz <= 2.25;
    }
}
