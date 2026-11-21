package art.arcane.witchery.brewing;

import art.arcane.witchery.mixin.MobGoalSelectorAccessor;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

public final class LoveEffect extends MobEffect {
    public LoveEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (!(target.level() instanceof ServerLevel level) || level.getGameTime() % 20 != 7) return;
        if (target instanceof Animal animal && animal.getAge() >= 0 && !animal.isInLove()) {
            animal.setAge(0);
            animal.setInLove(null);
        } else if (target instanceof Zombie zombie && !zombie.isBaby()) {
            addCourtship(zombie, level, Zombie.class, 600, 1.4);
        } else if (target instanceof Villager villager && canCourt(villager)) {
            addCourtship(villager, level, Villager.class, 300, 0.25);
        } else if (target instanceof Player && target.getRandom().nextBoolean()) {
            level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + 1, target.getZ(),
                    5, 0.6, 0.6, 0.6, 0);
        }
    }

    private static <T extends Mob> void addCourtship(T parent, ServerLevel level, Class<T> kind, int time, double speed) {
        GoalSelector goals = goals(parent);
        if (goals.getAvailableGoals().stream().noneMatch(goal -> goal.getGoal() instanceof Courtship)) {
            goals.addGoal(1, new Courtship<>(parent, level, kind, time, speed));
        }
    }

    private static GoalSelector goals(Mob mob) {
        return ((MobGoalSelectorAccessor) mob).witchery$goals();
    }

    private static boolean canCourt(Mob mob) {
        return mob.isAlive() && (mob instanceof Villager villager
                ? villager.getAge() == 0 && !villager.isSleeping() : !mob.isBaby());
    }

    private static final class Courtship<T extends Mob> extends Goal {
        private final T parent;
        private final ServerLevel level;
        private final Class<T> kind;
        private final int courtshipTicks;
        private final double speed;
        @Nullable private T mate;
        private int remaining;

        Courtship(T parent, ServerLevel level, Class<T> kind, int courtshipTicks, double speed) {
            this.parent = parent;
            this.level = level;
            this.kind = kind;
            this.courtshipTicks = courtshipTicks;
            this.speed = speed;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!parent.hasEffect(WitcheryEffects.LOVE.get()) || !canCourt(parent)
                    || parent instanceof Villager && !level.isVillage(parent.blockPosition())) return false;
            mate = level.getEntitiesOfClass(kind, parent.getBoundingBox().inflate(8, 3, 8),
                            candidate -> candidate != parent && canCourt(candidate))
                    .stream().min(Comparator.comparingDouble(parent::distanceToSqr)).orElse(null);
            // A single zombie with Love starts the courtship. Only one goal needs to lead the pair.
            return mate != null && (!(parent instanceof Zombie) || goals(mate).getRunningGoals()
                    .noneMatch(goal -> goal.getGoal() instanceof Courtship<?> other && other.mate == parent));
        }

        @Override
        public void start() {
            remaining = courtshipTicks;
        }

        @Override
        public boolean canContinueToUse() {
            return remaining > 0 && mate != null && canCourt(parent) && canCourt(mate);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (mate == null) return;
            remaining--;
            parent.getLookControl().setLookAt(mate, 10, 30);
            if (parent instanceof Villager && parent.getRandom().nextInt(35) == 0) {
                level.broadcastEntityEvent(parent, (byte) 12);
            }
            if (parent.distanceToSqr(mate) > 2.25) {
                parent.getNavigation().moveTo(mate, speed);
            } else if (remaining == 0) {
                if (parent instanceof Zombie zombie && mate instanceof Zombie partner) {
                    BrewImpacts.breedZombies(level, zombie, partner, null);
                } else if (parent instanceof Villager villager && mate instanceof Villager partner && isCourting(partner)) {
                    BrewImpacts.breedVillagers(level, villager, partner, 500);
                }
            }
        }

        private boolean isCourting(Villager candidate) {
            return candidate.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET)
                    || goals(candidate).getRunningGoals().anyMatch(goal -> goal.getGoal() instanceof Courtship<?>);
        }

        @Override
        public void stop() {
            mate = null;
            parent.getNavigation().stop();
        }
    }
}
