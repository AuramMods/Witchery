package art.arcane.witchery.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

final class HobgoblinMateGoal extends Goal {
    private final Hobgoblin goblin;
    @Nullable
    private Pair pair;

    HobgoblinMateGoal(Hobgoblin goblin) {
        this.goblin = goblin;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!eligible(goblin)) return false;
        if (pair != null) return canContinueToUse();
        if (!(goblin.level() instanceof ServerLevel level) || goblin.getRandom().nextInt(500) != 0
                || !hasRoom(level, goblin)) return false;
        var mate = level.getEntitiesOfClass(Hobgoblin.class, goblin.getBoundingBox().inflate(8, 3, 8),
                        candidate -> candidate != goblin && eligible(candidate) && candidate.mateGoal().pair == null)
                .stream().min(Comparator.comparingDouble(goblin::distanceToSqr)).orElse(null);
        if (mate == null) return false;
        pair = new Pair(goblin, mate);
        mate.mateGoal().pair = pair;
        return true;
    }

    private static boolean eligible(Hobgoblin candidate) {
        return candidate.isAlive() && candidate.getAge() == 0 && !candidate.isNoAi() && !candidate.isLeashed()
                && !candidate.isTrading() && !candidate.isWorshipping() && candidate.getTarget() == null;
    }

    private static boolean hasRoom(ServerLevel level, Hobgoblin parent) {
        if (!level.isVillage(parent.blockPosition())) return false;
        long beds = level.getPoiManager().getCountInRange(holder -> holder.is(PoiTypes.HOME), parent.blockPosition(),
                32, PoiManager.Occupancy.ANY);
        int count = level.getEntitiesOfClass(Hobgoblin.class, parent.getBoundingBox().inflate(32, 3, 32), Hobgoblin::isAlive).size();
        return count < beds;
    }

    @Override
    public boolean canContinueToUse() {
        return pair != null && pair.remaining > 0 && eligible(pair.first) && eligible(pair.second)
                && pair.first.distanceToSqr(pair.second) <= 100;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (pair == null || !(goblin.level() instanceof ServerLevel level)) return;
        Hobgoblin mate = pair.first == goblin ? pair.second : pair.first;
        goblin.getLookControl().setLookAt(mate, 10, 30);
        if (goblin.distanceToSqr(mate) > 2.25) goblin.getNavigation().moveTo(mate, 0.25);
        if (goblin.getRandom().nextInt(35) == 0) level.broadcastEntityEvent(goblin, (byte) 12);
        if (goblin == pair.first && --pair.remaining == 0 && goblin.distanceToSqr(mate) <= 2.25 && hasRoom(level, goblin)) {
            birth(level, mate);
        }
    }

    private void birth(ServerLevel level, Hobgoblin mate) {
        Hobgoblin child = goblin.getBreedOffspring(level, mate);
        if (child == null) return;
        child.setAge(-24000);
        child.moveTo(goblin.position());
        if (level.addFreshEntity(child)) {
            goblin.setAge(6000);
            mate.setAge(6000);
            level.broadcastEntityEvent(child, (byte) 12);
        }
    }

    @Override
    public void stop() {
        if (pair != null) {
            pair.first.mateGoal().pair = null;
            pair.second.mateGoal().pair = null;
        }
        goblin.getNavigation().stop();
    }

    private static final class Pair {
        private final Hobgoblin first;
        private final Hobgoblin second;
        private int remaining = 300;

        private Pair(Hobgoblin first, Hobgoblin second) {
            this.first = first;
            this.second = second;
        }
    }
}
