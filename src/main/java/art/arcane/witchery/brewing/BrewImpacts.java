package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.registry.WitcheryBlocks;
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
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class BrewImpacts {
    private static final double SPLASH_RADIUS = 4;

    private BrewImpacts() {
    }

    public static void splashInk(ThrownBrew brew, HitResult hit) {
        Vec3 impact = hit.getLocation();
        double radius = enhanced(brew) ? 5 : SPLASH_RADIUS;
        Entity directHit = hit instanceof EntityHitResult result ? result.getEntity() : null;
        applyInk(brew.level(), impact, splashBounds(brew, impact, radius), radius, directHit, brew.getEffectSource());
    }

    public static void mineInk(ServerLevel level, BlockPos pos, Entity trigger) {
        applyInk(level, Vec3.atLowerCornerOf(pos), new AABB(pos).inflate(SPLASH_RADIUS, 2, SPLASH_RADIUS),
                SPLASH_RADIUS, trigger, null);
    }

    private static void applyInk(Level level, Vec3 impact, AABB bounds, double radius,
                                 @Nullable Entity directHit, @Nullable Entity source) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bounds)) {
            boolean direct = entity == directHit;
            double distance = entity.distanceToSqr(impact);
            if (direct || distance < radius * radius) {
                int duration = direct ? 400 : (int) Math.round((1 - Math.sqrt(distance) / radius) * 400);
                if (duration > 0) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration), source);
                }
                clearTarget(entity);
            }
        }
    }

    public static void splashLove(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 impact = hit.getLocation();
        double radius = enhanced(brew) ? 5 : SPLASH_RADIUS;
        AABB bounds = splashBounds(brew, impact, radius);
        List<Villager> villagers = new ArrayList<>();
        for (AgeableMob mob : level.getEntitiesOfClass(AgeableMob.class, bounds,
                candidate -> candidate.getAge() >= 0 && candidate.distanceToSqr(impact) < radius * radius)) {
            if (mob instanceof Animal animal) {
                animal.setAge(0);
                animal.setInLove(null);
            } else if (mob instanceof Villager villager) {
                villagers.add(villager);
            }
        }
        for (int pair = 0; pair < Math.min(10, villagers.size() / 2); pair++) {
            breedVillagers(level, villagers.get(pair * 2), villagers.get(pair * 2 + 1));
        }
        if (brew.getOwner() instanceof ServerPlayer player) {
            List<Zombie> zombies = level.getEntitiesOfClass(Zombie.class, bounds, candidate -> candidate.isAlive()
                    && !candidate.isBaby() && candidate.distanceToSqr(impact) < radius * radius
                    && InfernalInfusion.owns(candidate, player));
            for (int pair = 0; pair < Math.min(10, zombies.size() / 2); pair++) {
                breedZombies(level, zombies.get(pair * 2), zombies.get(pair * 2 + 1), player);
            }
        }
    }

    public static void splashRevealing(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        Vec3 impact = hit.getLocation();
        double radius = enhanced(brew) ? 8 : 5;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(impact, impact).inflate(radius),
                living -> living.isAlive() && living.distanceToSqr(impact) <= radius * radius)) {
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

        double roll = level.getRandom().nextDouble();
        EntityType<? extends Mob> type = roll < 0.60 ? EntityType.ZOMBIE
                : roll < 0.97 ? EntityType.SKELETON : EntityType.ZOMBIFIED_PIGLIN;
        Mob undead = type.create(level);
        if (undead == null) return;
        Vec3 landing = Vec3.atBottomCenterOf(feet);
        undead.moveTo(landing.x, landing.y, landing.z, 1, 0);
        ForgeEventFactory.onFinalizeSpawn(undead, level, level.getCurrentDifficultyAt(feet), MobSpawnType.MOB_SUMMONED, null, null);
        undead.setPersistenceRequired();
        if (owner != null) InfernalInfusion.bind(undead, owner);
        if (level.addFreshEntity(undead)) {
            level.removeBlock(feet, false);
            level.removeBlock(feet.above(), false);
            level.sendParticles(ParticleTypes.SMOKE, landing.x, landing.y + 0.5, landing.z, 16, 0.5, 0.5, 0.5, 0.04);
        }
    }

    private static boolean canClearRaisingSpace(ServerLevel level, BlockPos pos, ServerPlayer owner) {
        if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)) return false;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return true;
        if (state.hasBlockEntity() || state.getDestroySpeed(level, pos) < 0) return false;
        return owner == null || level.mayInteract(owner, pos)
                && !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, owner));
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
        if (brew.getOwner() != null && ForgeEventFactory.onBlockPlace(brew.getOwner(), BlockSnapshot.create(level.dimension(), level, pos), face)) {
            return;
        }
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired(block).get().defaultBlockState());
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

    static AABB splashBounds(ThrownBrew brew, Vec3 impact, double radius) {
        return brew.getBoundingBox().move(impact.subtract(brew.position())).inflate(radius, 2, radius);
    }

    private static void breedVillagers(ServerLevel level, Villager first, Villager mate) {
        BabyEntitySpawnEvent birth = new BabyEntitySpawnEvent(first, mate, first.getBreedOffspring(level, mate));
        if (MinecraftForge.EVENT_BUS.post(birth) || birth.getChild() == null) {
            return;
        }
        AgeableMob child = birth.getChild();
        first.setAge(6000);
        mate.setAge(6000);
        first.teleportTo(mate.getX(), mate.getY(), mate.getZ());
        level.sendParticles(ParticleTypes.HEART, mate.getX(), mate.getY() + 1, mate.getZ(), 8, 0.5, 1, 0.5, 0.05);
        child.setAge(-24000);
        child.moveTo(mate.getX(), mate.getY(), mate.getZ(), 0, 0);
        level.addFreshEntityWithPassengers(child);
        level.broadcastEntityEvent(child, (byte) 12);
    }

    private static void breedZombies(ServerLevel level, Zombie first, Zombie mate, ServerPlayer owner) {
        if (!mayBecomeZombieVillager(first) || !mayBecomeZombieVillager(mate)) return;
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

    private static boolean mayBecomeZombieVillager(Zombie zombie) {
        return zombie.getType() == EntityType.ZOMBIE_VILLAGER
                || ForgeEventFactory.canLivingConvert(zombie, EntityType.ZOMBIE_VILLAGER, ignored -> {});
    }

    private static void becomeZombieVillager(Zombie zombie, ServerPlayer owner) {
        if (zombie.getType() == EntityType.ZOMBIE_VILLAGER) return;
        var villager = zombie.convertTo(EntityType.ZOMBIE_VILLAGER, true);
        if (villager == null) return;
        villager.getPersistentData().merge(zombie.getPersistentData());
        villager.setHealth(Math.min(zombie.getHealth(), villager.getMaxHealth()));
        for (MobEffectInstance effect : zombie.getActiveEffects()) villager.addEffect(new MobEffectInstance(effect));
        InfernalInfusion.bind(villager, owner);
        ForgeEventFactory.onLivingConvert(zombie, villager);
    }
}
