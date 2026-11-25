package art.arcane.witchery.infusion;

import art.arcane.witchery.entity.CreatureAlignment;
import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.entity.Imp;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public final class InfernalInfusion {
    public static final int ID = 4;
    private static final String OWNER_NAME = "WITCEnslaverName";
    private static final String OWNER_UUID = "WITCEnslaverUUID";

    private InfernalInfusion() {}

    public static void attack(ServerPlayer player, LivingEntity target) {
        if (!target.isAlive() || target.isSpectator()) return;
        if (player.isShiftKeyDown()) {
            if (!(target instanceof Mob mob) || !canBind(mob)) {
                fail(player);
            } else if (owns(mob, player)) {
                if (InfusionEnergy.spend(player, 1)) sacrifice(player, mob);
            } else if (InfusionEnergy.spend(player, 5)) {
                bind(mob, player);
                particles(mob, ParticleTypes.ENTITY_EFFECT);
                mob.level().playSound(null, mob.blockPosition(), SoundEvents.ZOMBIE_INFECT, mob.getSoundSource(), 0.8F, 1);
            }
            return;
        }
        if (!InfusionEnergy.spend(player, 1)) return;
        orderAttack(minions(player), target);
    }

    private static boolean orderAttack(List<Mob> minions, LivingEntity target) {
        boolean commanded = false;
        for (Mob mob : minions) {
            if (mob == target || owns(mob, target)) continue;
            clearMove(mob);
            mob.setTarget(target);
            mob.getLookControl().setLookAt(target, 30, 30);
            mob.getNavigation().moveTo(target, 1);
            commanded = true;
        }
        if (commanded) particles(target, ParticleTypes.CRIT);
        return commanded;
    }

    public static void release(ServerPlayer player, int elapsed) {
        HitResult hit = OtherwhereInfusion.rayTrace(player, 15);
        if (player.isShiftKeyDown()) {
            if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK
                    || block.getDirection() != Direction.UP) {
                fail(player);
                return;
            }
            orderTo(player, minions(player), block.getBlockPos().above());
            return;
        }
        CreaturePower power = CreaturePower.forId(CreaturePower.activeId(player));
        if (power != null) power.use(player, elapsed, hit);
        else fail(player);
    }

    public static void commandUndead(ServerPlayer player) {
        HitResult hit = OtherwhereInfusion.rayTrace(player, 15);
        if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK
                && player.level().getBlockState(block.getBlockPos()).is(WitcheryBlocks.getRequired("alluringskull").get())) return;
        if (player.isShiftKeyDown() && hit instanceof EntityHitResult entity && entity.getEntity() instanceof Mob mob) {
            if (InfusedBrews.useGrave(player, mob)) {
                particles(mob, ParticleTypes.ENTITY_EFFECT);
                player.serverLevel().playSound(null, mob.blockPosition(), SoundEvents.ZOMBIE_INFECT, player.getSoundSource(), 1, 1);
            } else {
                particles(mob, ParticleTypes.SMOKE);
                fail(player);
            }
            return;
        }
        List<Mob> undead = minions(player).stream().filter(mob -> mob.getMobType() == MobType.UNDEAD).toList();
        if (!player.isShiftKeyDown() && hit instanceof EntityHitResult entity
                && entity.getEntity() instanceof LivingEntity target && target.isAlive() && !target.isSpectator()
                && orderAttack(undead, target)) {
            player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.ZOMBIE_DEATH, player.getSoundSource(), 0.5F, 1);
        } else if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK
                && block.getDirection() == Direction.UP && !undead.isEmpty()) {
            orderTo(player, undead, block.getBlockPos().above());
        } else {
            fail(player);
        }
    }

    private static void orderTo(ServerPlayer player, List<Mob> minions, BlockPos destination) {
        for (Mob minion : minions) {
            clearAggro(minion);
            MoveOrder move = order(minion);
            if (move != null) {
                move.destination = destination.immutable();
                move.ticks = 0;
            }
            if (minion instanceof Spider || move == null || !minion.getNavigation().moveTo(
                    destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, 1)) {
                minion.restrictTo(destination, 10);
            }
        }
        if (!minions.isEmpty()) {
            player.serverLevel().sendParticles(ParticleTypes.INSTANT_EFFECT, destination.getX() + 0.5,
                    destination.getY() + 0.5, destination.getZ() + 0.5, 16, 0.5, 0.7, 0.5, 0.03);
            player.serverLevel().playSound(null, destination, SoundEvents.CHICKEN_EGG, player.getSoundSource(), 0.5F, 1);
        }
    }

    private static void sacrifice(ServerPlayer player, Mob creature) {
        CreaturePower power = CreaturePower.forCreature(creature.getType());
        if (power == null) {
            fail(player);
            return;
        }
        creature.hurt(player.serverLevel().damageSources().indirectMagic(player, player), creature.getHealth() + 1);
        if (!creature.isAlive()) power.absorb(player);
    }

    private static List<Mob> minions(ServerPlayer player) {
        AABB area = new AABB(player.getX() - 50, player.getY() - 15, player.getZ() - 50,
                player.getX() + 50, player.getY() + 15, player.getZ() + 50);
        return player.serverLevel().getEntitiesOfClass(Mob.class, area, mob -> mob.isAlive() && owns(mob, player));
    }

    public static boolean canBind(Mob mob) {
        return !CreatureAlignment.boss(mob) && !(mob instanceof AbstractGolem || mob instanceof ElderGuardian
                || mob instanceof Demon || mob instanceof Witch || mob instanceof Imp)
                && mob.getType() != WitcheryEntities.ENT.get();
    }

    public static void castImperio(ServerPlayer player, Mob mob) {
        if (!canBind(mob) || owns(mob, player)) return;
        bind(mob, player);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.ENTITY_EFFECT, mob.getX(), mob.getY(0.5), mob.getZ(), 8, 0.45, 0.65, 0.45, 0.04);
        level.playSound(null, mob.blockPosition(), SoundEvents.ZOMBIE_INFECT, SoundSource.PLAYERS, 1, 1);
    }

    public static boolean owns(Mob mob, @Nullable LivingEntity owner) {
        if (!(owner instanceof ServerPlayer player)) return false;
        CompoundTag data = mob.getPersistentData();
        return data.hasUUID(OWNER_UUID) ? data.getUUID(OWNER_UUID).equals(player.getUUID())
                : data.getString(OWNER_NAME).equals(player.getGameProfile().getName());
    }

    public static void bind(Mob mob, ServerPlayer player) {
        CompoundTag data = mob.getPersistentData();
        data.putString(OWNER_NAME, player.getGameProfile().getName());
        data.putUUID(OWNER_UUID, player.getUUID());
        restore(mob);
        clearMove(mob);
        clearAggro(mob);
    }

    public static void restore(Mob mob) {
        if (!(mob.level() instanceof ServerLevel)) return;
        CompoundTag data = mob.getPersistentData();
        if (!data.hasUUID(OWNER_UUID) && data.getString(OWNER_NAME).isEmpty()) return;
        if (!mob.hasEffect(WitcheryEffects.ENSLAVED.get())) {
            mob.addEffect(new MobEffectInstance(WitcheryEffects.ENSLAVED.get(), MobEffectInstance.INFINITE_DURATION));
        }
        if (order(mob) == null) {
            mob.goalSelector.addGoal(1, new MoveOrder(mob));
            mob.targetSelector.addGoal(1, new DefendOwner(mob));
        }
    }

    @Nullable
    private static MoveOrder order(Mob mob) {
        return mob.goalSelector.getAvailableGoals().stream().map(goal -> goal.getGoal())
                .filter(MoveOrder.class::isInstance).map(MoveOrder.class::cast).findFirst().orElse(null);
    }

    private static void clearMove(Mob mob) {
        MoveOrder order = order(mob);
        if (order != null) order.destination = null;
        mob.getNavigation().stop();
    }

    private static void clearAggro(Mob mob) {
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        mob.getNavigation().stop();
    }

    private static void particles(LivingEntity living, ParticleOptions particle) {
        if (living.level() instanceof ServerLevel level) {
            level.sendParticles(particle, living.getX(), living.getY(0.5), living.getZ(), 16, 0.45, 0.65, 0.45, 0.04);
        }
    }

    static void fail(ServerPlayer player) {
        WitcherySounds.playLegacyPlayerCue(player, SoundEvents.NOTE_BLOCK_SNARE.value(), 0.5F);
    }

    private static final class MoveOrder extends Goal {
        private final Mob mob;
        @Nullable private BlockPos destination;
        private int ticks;

        private MoveOrder(Mob mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return destination != null && mob.hasEffect(WitcheryEffects.ENSLAVED.get());
        }

        @Override
        public void start() {
            ticks = 0;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() && ticks < 400 && mob.distanceToSqr(destination.getX() + 0.5,
                    destination.getY(), destination.getZ() + 0.5) > 2.25;
        }

        @Override
        public void tick() {
            if (destination == null || ++ticks % 10 != 1) return;
            mob.getNavigation().moveTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, 1);
        }

        @Override
        public void stop() {
            destination = null;
            mob.getNavigation().stop();
        }
    }

    private static final class DefendOwner extends TargetGoal {
        private int lastHurt;
        private int observedHurt;
        @Nullable private LivingEntity attacker;

        private DefendOwner(Mob mob) {
            super(mob, false);
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!mob.hasEffect(WitcheryEffects.ENSLAVED.get()) || !(mob.level() instanceof ServerLevel level)) return false;
            CompoundTag data = mob.getPersistentData();
            var players = level.getServer().getPlayerList();
            ServerPlayer owner = data.hasUUID(OWNER_UUID) ? players.getPlayer(data.getUUID(OWNER_UUID))
                    : players.getPlayerByName(data.getString(OWNER_NAME));
            if (owner == null || owner.level() != level) return false;
            attacker = owner.getLastHurtByMob();
            observedHurt = owner.getLastHurtByMobTimestamp();
            return observedHurt != lastHurt && canAttack(attacker, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            lastHurt = observedHurt;
            mob.setTarget(attacker);
            super.start();
        }
    }
}
