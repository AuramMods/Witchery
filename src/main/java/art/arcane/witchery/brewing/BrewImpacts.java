package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.event.WitcheryBlockBreak;
import art.arcane.witchery.event.WitcheryBlockPlacement;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class BrewImpacts {
    private static final double SPLASH_RADIUS = 4;

    private BrewImpacts() {
    }

    public static void splashInk(ThrownBrew brew, HitResult hit) {
        double radius = enhanced(brew) ? 5 : SPLASH_RADIUS;
        Entity directHit = hit instanceof EntityHitResult result ? result.getEntity() : null;
        applyInk(brew.level(), brew.position(), splashBounds(brew, radius), radius, directHit, brew.getEffectSource());
    }

    public static void mineInk(ServerLevel level, BlockPos pos, Entity trigger) {
        applyInk(level, Vec3.atLowerCornerOf(pos), new AABB(pos).inflate(SPLASH_RADIUS, 2, SPLASH_RADIUS),
                SPLASH_RADIUS, trigger, null);
    }

    private static void applyInk(Level level, Vec3 impact, AABB bounds, double radius,
                                 @Nullable Entity directHit, @Nullable Entity source) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bounds)) {
            double distance = entity.distanceToSqr(impact);
            if (distance >= radius * radius) continue;
            int duration = entity == directHit ? 400 : (int) Math.round((1 - Math.sqrt(distance) / radius) * 400);
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration), source);
            clearTarget(entity);
        }
    }

    public static void splashLove(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) {
            return;
        }
        double radius = enhanced(brew) ? 5 : SPLASH_RADIUS;
        AABB bounds = splashBounds(brew, radius);
        List<Villager> villagers = new ArrayList<>();
        for (AgeableMob mob : level.getEntitiesOfClass(AgeableMob.class, bounds,
                candidate -> candidate.getAge() >= 0 && candidate.distanceToSqr(brew) < radius * radius)) {
            if (mob instanceof Animal animal) {
                animal.setAge(0);
                animal.setInLove(null);
            } else if (mob instanceof Villager villager) {
                villagers.add(villager);
            }
        }
        for (int pair = 0; pair < Math.min(10, villagers.size() / 2); pair++) {
            breedVillagers(level, villagers.get(pair * 2), villagers.get(pair * 2 + 1), 6000);
        }
        if (brew.getOwner() instanceof ServerPlayer player) {
            List<Zombie> zombies = level.getEntitiesOfClass(Zombie.class, bounds, candidate -> candidate.isAlive()
                    && !candidate.isBaby() && candidate.distanceToSqr(brew) < radius * radius
                    && InfernalInfusion.owns(candidate, player));
            for (int pair = 0; pair < Math.min(10, zombies.size() / 2); pair++) {
                breedZombies(level, zombies.get(pair * 2), zombies.get(pair * 2 + 1), player);
            }
        }
    }

    public static void splashRevealing(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        double radius = enhanced(brew) ? 8 : 5;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, brew.getBoundingBox().inflate(radius),
                living -> living.isAlive() && living.distanceToSqr(brew) <= radius * radius)) {
            BrewCreatureEffects.reveal(target);
        }
    }

    public static void depositSpirit(ThrownBrew brew, HitResult hit) {
        depositLiquid(brew, hit, "spiritflowing");
    }

    public static void depositHollowTears(ThrownBrew brew, HitResult hit) {
        depositLiquid(brew, hit, "hollowtears");
    }

    public static void raiseDead(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        if (!(hit instanceof BlockHitResult block) || block.getDirection() != Direction.UP) {
            recoverBrew(brew, level, hit);
            return;
        }

        BlockPos ground = block.getBlockPos();
        if (!level.getBlockState(ground).blocksMotion()) ground = ground.below();
        ServerPlayer owner = brew.getOwner() instanceof ServerPlayer player ? player : null;
        raiseUndead(level, ground, owner);
        RandomSource random = level.getRandom();
        double roll = random.nextDouble();
        int extra = roll < 0.10 ? 2 : roll < 0.40 ? 1 : 0;
        for (int attempt = 0; attempt < extra; attempt++) {
            BlockPos column = ground.offset(random.nextInt(6) - 2, 0, random.nextInt(6) - 2);
            for (int offset = 6; offset > -6; offset--) {
                BlockPos candidate = column.above(offset);
                if (!level.getBlockState(candidate).blocksMotion()) continue;
                raiseUndead(level, candidate, owner);
                break;
            }
        }
    }

    static void raiseFromCauldron(ServerLevel level, BlockPos ground, Direction face, int strength,
                                  boolean ritual, @Nullable ServerPlayer caster) {
        BlockPos center = ground.relative(face);
        raiseCauldronUndead(level, center, caster, ritual ? 200 * (strength + 1) : 0);
        if (ritual) return;
        RandomSource random = level.random;
        int extra = 0;
        if (strength >= 1 && random.nextDouble() < strength * 0.5) extra++;
        if (strength >= 2 && random.nextDouble() < strength * 0.25) extra++;
        if (strength >= 3 && random.nextDouble() < strength * 0.25) extra++;
        for (int attempt = 0; attempt < extra; attempt++) {
            BlockPos column = center.offset(random.nextInt(6) - 2, 0, random.nextInt(6) - 2);
            if (!level.hasChunkAt(column)) continue;
            for (int offset = 6; offset >= -6; offset--) {
                BlockPos feet = column.above(offset);
                if (level.isEmptyBlock(feet) && level.getBlockState(feet.below()).blocksMotion()) {
                    raiseCauldronUndead(level, feet, caster, 0);
                    break;
                }
            }
        }
    }

    private static void raiseCauldronUndead(ServerLevel level, BlockPos feet, @Nullable ServerPlayer caster, int lifespan) {
        if (!level.isInWorldBounds(feet) || !level.getWorldBorder().isWithinBounds(feet) || !level.hasChunkAt(feet)) return;
        Mob undead = createRaisedUndead(level);
        if (undead == null) return;
        Vec3 landing = Vec3.atBottomCenterOf(feet).add(0, 0.1, 0);
        undead.moveTo(landing.x, landing.y, landing.z, 0, 0);
        if (level.getBlockCollisions(undead, undead.getBoundingBox()).iterator().hasNext()) return;
        finishRaisedUndead(level, feet, undead, caster);
        undead.getPersistentData().putBoolean("WITCNoDrops", true);
        if (lifespan > 0) undead.addEffect(new MobEffectInstance(WitcheryEffects.MORTAL_COIL.get(), lifespan));
        if (level.addFreshEntity(undead)) raisingParticles(level, landing);
    }

    static void recoverBrew(ThrownBrew brew, ServerLevel level, HitResult hit) {
        Vec3 drop = hit instanceof BlockHitResult block
                ? Vec3.atCenterOf(block.getBlockPos().relative(block.getDirection() == Direction.DOWN ? Direction.DOWN : Direction.UP))
                : hit instanceof EntityHitResult entity ? entity.getEntity().position() : hit.getLocation();
        recoverBrew(brew, level, drop);
    }

    static void recoverBrew(ThrownBrew brew, ServerLevel level, Vec3 drop) {
        level.addFreshEntity(new ItemEntity(level, drop.x, drop.y, drop.z, brew.getItem().copyWithCount(1)));
    }

    private static void raiseUndead(ServerLevel level, BlockPos ground, ServerPlayer owner) {
        BlockState soil = level.getBlockState(ground);
        boolean emergeThroughGround = soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.STONE)
                || soil.is(Blocks.NETHERRACK) || soil.is(Blocks.MYCELIUM) || soil.is(Blocks.SOUL_SAND)
                || soil.is(Blocks.COBBLESTONE) || soil.is(Blocks.GRAVEL) || soil.is(Blocks.SAND);
        BlockPos feet = emergeThroughGround ? ground : ground.above();
        if (!canClearRaisingSpace(level, feet, owner) || !canClearRaisingSpace(level, feet.above(), owner)) return;

        Mob undead = createRaisedUndead(level);
        if (undead == null) return;
        Vec3 landing = Vec3.atBottomCenterOf(feet);
        undead.moveTo(landing.x, landing.y, landing.z, 1, 0);
        finishRaisedUndead(level, feet, undead, owner);
        if (level.addFreshEntity(undead)) {
            level.removeBlock(feet, false);
            level.removeBlock(feet.above(), false);
            raisingParticles(level, landing);
        }
    }

    @Nullable
    private static Mob createRaisedUndead(ServerLevel level) {
        double roll = level.random.nextDouble();
        EntityType<? extends Mob> type = roll < 0.60 ? EntityType.ZOMBIE
                : roll < 0.97 ? EntityType.SKELETON : EntityType.ZOMBIFIED_PIGLIN;
        return type.create(level);
    }

    private static void finishRaisedUndead(ServerLevel level, BlockPos feet, Mob undead, @Nullable ServerPlayer caster) {
        ForgeEventFactory.onFinalizeSpawn(undead, level, level.getCurrentDifficultyAt(feet), MobSpawnType.MOB_SUMMONED, null, null);
        undead.setPersistenceRequired();
        if (caster != null) InfernalInfusion.bind(undead, caster);
    }

    private static void raisingParticles(ServerLevel level, Vec3 landing) {
        level.sendParticles(ParticleTypes.SMOKE, landing.x, landing.y + 0.5, landing.z, 16, 0.5, 0.5, 0.5, 0.04);
    }

    private static boolean canClearRaisingSpace(ServerLevel level, BlockPos pos, ServerPlayer owner) {
        if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)) return false;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return true;
        if (state.hasBlockEntity() || state.getDestroySpeed(level, pos) < 0) return false;
        return owner == null || level.mayInteract(owner, pos)
                && !WitcheryBlockBreak.isCanceled(level, pos, state, owner);
    }

    private static void depositLiquid(ThrownBrew brew, HitResult hit, String block) {
        if (!(brew.level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos;
        Direction face;
        if (hit instanceof BlockHitResult impact && hit.getType() == HitResult.Type.BLOCK) {
            pos = impact.getBlockPos();
            face = impact.getDirection();
            if (face != Direction.UP || level.getBlockState(pos).blocksMotion()) {
                pos = pos.relative(face);
            }
        } else if (hit instanceof EntityHitResult impact) {
            pos = impact.getEntity().blockPosition();
            face = Direction.UP;
        } else {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!level.isInWorldBounds(pos) || state.blocksMotion() || state.hasBlockEntity()
                || brew.getOwner() instanceof Player player && !level.mayInteract(player, pos)) {
            return;
        }
        WitcheryBlockPlacement.place(level, pos, WitcheryBlocks.getRequired(block).get().defaultBlockState(), brew.getOwner(), face);
    }

    static boolean enhanced(ThrownBrew brew) {
        return brew.getOwner() instanceof ServerPlayer player && FamiliarBinding.hasBrewMastery(player);
    }

    static void clearTarget(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
    }

    static AABB splashBounds(ThrownBrew brew, double radius) {
        return brew.getBoundingBox().inflate(radius, 2, radius);
    }

    static void breedVillagers(ServerLevel level, Villager first, Villager mate, int cooldown) {
        BabyEntitySpawnEvent birth = new BabyEntitySpawnEvent(first, mate, first.getBreedOffspring(level, mate));
        if (MinecraftForge.EVENT_BUS.post(birth) || birth.getChild() == null) {
            return;
        }
        AgeableMob child = birth.getChild();
        first.setAge(cooldown);
        mate.setAge(cooldown);
        first.teleportTo(mate.getX(), mate.getY(), mate.getZ());
        level.sendParticles(ParticleTypes.HEART, mate.getX(), mate.getY() + 1, mate.getZ(), 8, 0.5, 1, 0.5, 0.05);
        child.setAge(-24000);
        child.moveTo(mate.getX(), mate.getY(), mate.getZ(), 0, 0);
        level.addFreshEntityWithPassengers(child);
        level.broadcastEntityEvent(child, (byte) 12);
    }

    static void breedZombies(ServerLevel level, Zombie first, Zombie mate, @Nullable ServerPlayer owner) {
        if (!mayBecomeZombieVillager(first, owner) || !mayBecomeZombieVillager(mate, owner)) return;
        Zombie child = EntityType.ZOMBIE.create(level);
        if (child == null) return;
        child.setBaby(true);
        child.moveTo(mate.getX(), mate.getY(), mate.getZ(), 0, 0);
        if (!level.addFreshEntity(child)) return;
        first.teleportTo(mate.getX(), mate.getY(), mate.getZ());
        becomeZombieVillager(first, owner);
        becomeZombieVillager(mate, owner);
        level.sendParticles(ParticleTypes.HEART, child.getX(), child.getY() + 1, child.getZ(), 8, 0.5, 1, 0.5, 0.05);
    }

    private static boolean mayBecomeZombieVillager(Zombie zombie, @Nullable ServerPlayer owner) {
        return zombie.getType() == EntityType.ZOMBIE_VILLAGER || owner == null && zombie instanceof ZombifiedPiglin
                || ForgeEventFactory.canLivingConvert(zombie, EntityType.ZOMBIE_VILLAGER, ignored -> {});
    }

    private static void becomeZombieVillager(Zombie zombie, @Nullable ServerPlayer owner) {
        if (zombie.getType() == EntityType.ZOMBIE_VILLAGER || owner == null && zombie instanceof ZombifiedPiglin) return;
        var villager = zombie.convertTo(EntityType.ZOMBIE_VILLAGER, true);
        if (villager == null) return;
        villager.getPersistentData().merge(zombie.getPersistentData());
        zombie.getTags().forEach(villager::addTag);
        villager.setHealth(Math.min(zombie.getHealth(), villager.getMaxHealth()));
        for (MobEffectInstance effect : zombie.getActiveEffects()) villager.addEffect(new MobEffectInstance(effect));
        if (owner != null) InfernalInfusion.bind(villager, owner);
        ForgeEventFactory.onLivingConvert(zombie, villager);
    }
}
