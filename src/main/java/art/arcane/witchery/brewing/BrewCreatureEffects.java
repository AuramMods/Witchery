package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.entity.WitchOwl;
import art.arcane.witchery.entity.WitchToad;
import art.arcane.witchery.familiar.Familiar;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.vampirism.VampireVillages;
import art.arcane.witchery.world.MirrorDisguise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BrewCreatureEffects {
    private static final TagKey<Block> WASTING_PLANTS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "wasting_plants"));
    private static final float FROG_TONGUE_PULL = 0.15F;

    private BrewCreatureEffects() {}

    static void airHike(LivingEntity target, int strength) {
        double lift = 0.6 + 0.2 * strength;
        target.resetFallDistance();
        Vec3 horizontal = target instanceof Player ? Vec3.ZERO : target.getDeltaMovement();
        move(target, new Vec3(horizontal.x, lift, horizontal.z));
    }

    static void frogTongue(Entity target, @Nullable LivingEntity caster, @Nullable Vec3 impact, int strength) {
        Vec3 origin = tongueOrigin(caster, impact, strength);
        if (origin == null || target.getType() == EntityType.ENDER_DRAGON
                || target.getType() == WitcheryEntities.all().get("hornedhuntsman").get()) return;
        Vec3 difference = origin.subtract(target.position());
        if (difference.lengthSqr() < 0.0001) return;
        if (target instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 1));
        move(target, new Vec3(difference.x * FROG_TONGUE_PULL, 0.4, difference.z * FROG_TONGUE_PULL));
    }

    static void frogTongue(ServerLevel level, Vec3 impact, int radius, @Nullable LivingEntity caster, int strength) {
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(impact, impact).inflate(radius),
                candidate -> !(candidate instanceof LivingEntity) && candidate.distanceToSqr(impact) <= radius * radius)) {
            frogTongue(entity, caster, impact, strength);
        }
    }

    @Nullable
    private static Vec3 tongueOrigin(@Nullable LivingEntity caster, @Nullable Vec3 impact, int strength) {
        return strength > 0 && caster instanceof Player && !(caster instanceof FakePlayer) ? caster.position() : impact;
    }

    static void knockback(LivingEntity target, @Nullable Vec3 impact, int strength, double scale) {
        double horizontal = 1 + strength * scale;
        double lift = 0.5 + strength * 0.2;
        if (impact != null) {
            pushFrom(target, impact, horizontal, lift);
            return;
        }
        int radius = 3 + strength;
        Vec3 center = target.position();
        for (Entity neighbor : target.level().getEntities(target, new AABB(center, center).inflate(radius),
                entity -> (entity instanceof LivingEntity || entity instanceof ItemEntity)
                        && entity.distanceToSqr(center) <= radius * radius)) {
            pushFrom(neighbor, center, horizontal, lift);
        }
    }

    private static void pushFrom(Entity target, Vec3 center, double horizontal, double lift) {
        Vec3 offset = target.position().subtract(center).normalize();
        move(target, new Vec3(offset.x * horizontal, Math.max(offset.y, lift), offset.z * horizontal));
    }

    static void batBurst(LivingEntity target, int strength, double scale, boolean glancing) {
        if (!(target.level() instanceof ServerLevel level) || target instanceof Bat || target instanceof WitchOwl) return;
        boolean full = scale == 1 && !glancing;
        if (full || level.random.nextInt(20) == 0) {
            releaseBats(level, target.blockPosition().above(), (full ? 10 : 1) + strength);
        }
    }

    static void bodega(LivingEntity target, @Nullable LivingEntity caster, int strength, double scale, boolean glancing) {
        if (!(target.level() instanceof ServerLevel level) || !(caster instanceof ServerPlayer player)
                || target instanceof Bat || target instanceof WitchOwl || !FamiliarBinding.hasBroomMastery(player)) return;
        boolean full = scale == 1 && !glancing;
        if (full || level.random.nextInt(20) == 0) {
            summonOwls(level, target, (full ? 3 + level.random.nextInt(2) : 1) + strength);
        }
    }

    static void poisonToad(LivingEntity target, int strength, double scale) {
        if (target.level() instanceof ServerLevel level && ((!(target instanceof WitchToad) && scale == 1)
                || level.random.nextDouble() < scale * 0.2)) {
            poisonToad(level, new Vec3(target.getX(), target.getY() + target.getBbHeight() + 1, target.getZ()), strength);
        }
    }

    static void poisonToad(ServerLevel level, Vec3 landing, int strength) {
        var toad = WitcheryEntities.TOAD.get().create(level);
        if (toad == null) return;
        toad.moveTo(landing.x, landing.y, landing.z, 0, 0);
        toad.preparePoisonousToad(60 + 40 * strength);
        level.addFreshEntity(toad);
    }

    static void reveal(LivingEntity target) {
        target.removeEffect(MobEffects.INVISIBILITY);
        if (target instanceof ServerPlayer player) {
            player.setInvisible(false);
            MirrorDisguise.reveal(player);
        } else if (target instanceof SpectralUndead spirit) {
            spirit.reveal();
        }
    }

    static void moveAnimals(LivingEntity target, int strength, boolean repel) {
        if (!(target.level() instanceof ServerLevel level)) return;
        double radius = (strength + 1) * 16.0;
        for (Animal animal : level.getEntitiesOfClass(Animal.class, target.getBoundingBox().inflate(radius),
                candidate -> candidate != target && candidate.isAlive() && candidate.distanceToSqr(target) < radius * radius
                        && !(candidate instanceof Familiar familiar && familiar.isFamiliar()))) {
            if (animal instanceof TamableAnimal pet) changeOwnership(level, target, pet, repel);
            if (repel) driveAway(level, target, animal);
            else animal.getNavigation().moveTo(target, 1);
        }
    }

    private static void changeOwnership(ServerLevel level, LivingEntity target, TamableAnimal pet, boolean repel) {
        if (!repel && !pet.isTame()) {
            pet.setOwnerUUID(target.getUUID());
            pet.setTame(true);
            pet.getNavigation().stop();
            pet.setTarget(null);
            pet.setHealth(20);
            level.broadcastEntityEvent(pet, (byte) 7);
        } else if (repel && pet.isTame() && !target.getUUID().equals(pet.getOwnerUUID())) {
            pet.setOwnerUUID(null);
            pet.setTame(false);
            pet.setOrderedToSit(false);
            pet.setInSittingPose(false);
            pet.getNavigation().stop();
            pet.setTarget(null);
            level.broadcastEntityEvent(pet, (byte) 6);
        }
    }

    private static void driveAway(ServerLevel level, LivingEntity target, Animal animal) {
        int x = target.getBlockX() + (6 + level.random.nextInt(6)) * (level.random.nextBoolean() ? 1 : -1);
        int z = target.getBlockZ() + (6 + level.random.nextInt(6)) * (level.random.nextBoolean() ? 1 : -1);
        BlockPos column = new BlockPos(x, target.getBlockY(), z);
        if (!level.hasChunkAt(column) || !level.getWorldBorder().isWithinBounds(column)) return;
        BlockPos feet = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
        BlockPos ground = feet.below();
        if (!level.isInWorldBounds(feet) || !level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)
                || !level.getFluidState(feet).isEmpty()) return;
        Vec3 destination = Vec3.atBottomCenterOf(feet);
        if (!animal.getNavigation().moveTo(destination.x, destination.y, destination.z, 1)
                && level.noCollision(animal, animal.getBoundingBox().move(destination.subtract(animal.position())))) {
            animal.teleportTo(destination.x, destination.y, destination.z);
        }
    }

    public static void frogsTongue(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level) || !(brew.getOwner() instanceof LivingEntity thrower)) return;
        for (LivingEntity victim : projectileSplashTargets(level, brew, 4)) {
            if (victim == thrower || !victim.isAlive() || victim.getType() == EntityType.ENDER_DRAGON
                    || victim.getType() == WitcheryEntities.all().get("hornedhuntsman").get()) continue;
            Vec3 difference = thrower.position().subtract(victim.position());
            move(victim, new Vec3(difference.x * FROG_TONGUE_PULL, 0.4, difference.z * FROG_TONGUE_PULL));
            victim.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 1), brew.getEffectSource());
        }
    }

    public static void cursedLeaping(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        for (LivingEntity victim : projectileSplashTargets(level, brew, 5)) {
            if (!victim.isAlive()) continue;
            double lift = 1.6 * (direct(hit, victim) ? 1 : 1 - Math.sqrt(victim.distanceToSqr(brew)) / 20);
            victim.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, 3), brew.getEffectSource());
            move(victim, victim.getDeltaMovement().add(0, lift, 0));
        }
    }

    private static List<LivingEntity> projectileSplashTargets(ServerLevel level, ThrownBrew brew, double radius) {
        // These fixed brews measure from the projectile's position before collision.
        return level.getEntitiesOfClass(LivingEntity.class, BrewImpacts.splashBounds(brew, radius),
                candidate -> candidate.distanceToSqr(brew) < radius * radius);
    }

    public static void hitchcock(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        if (!(hit instanceof EntityHitResult entity) || !(entity.getEntity() instanceof LivingEntity victim)) {
            BrewImpacts.recoverBrew(brew, hit);
            return;
        }
        summonOwls(level, victim, 3 + level.random.nextInt(2));
    }

    private static void summonOwls(ServerLevel level, LivingEntity victim, int count) {
        for (int i = 0; i < count; i++) {
            var owl = WitcheryEntities.OWL.get().create(level);
            if (owl == null) continue;
            owl.moveTo(victim.getX() + level.random.nextInt(5) - 2,
                    victim.getY() + victim.getBbHeight() + 1 + level.random.nextInt(2),
                    victim.getZ() + level.random.nextInt(5) - 2, 0, 0);
            owl.attackTemporarily(victim, 400);
            if (level.addFreshEntity(owl)) {
                WitcheryNetwork.sendBrewOwlArrival(level, owl.position());
            }
        }
    }

    public static void bats(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        boolean enhanced = BrewImpacts.enhanced(brew);
        BlockPos release = BrewTerrain.splashPosition(level, hit);
        spawnBats(level, release, enhanced ? 14 : 10);
        WitcheryNetwork.sendBrewBatBurst(level, release, true);
        Vec3 impact = brew.position();
        double radius = enhanced ? 4 : 3;
        for (LivingEntity victim : projectileSplashTargets(level, brew, radius)) {
            int ticks = splashTicks(hit, victim, impact, radius, 100);
            if (ticks > 0) {
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 5), brew.getEffectSource());
            }
            BrewImpacts.clearTarget(victim);
        }
    }

    static void releaseBats(ServerLevel level, BlockPos release, int count) {
        spawnBats(level, release, count);
        WitcheryNetwork.sendBrewBatBurst(level, release, false);
    }

    private static void spawnBats(ServerLevel level, BlockPos release, int count) {
        for (int i = 0; i < count; i++) {
            var bat = EntityType.BAT.create(level);
            if (bat == null) continue;
            bat.moveTo(release.getX(), release.getY(), release.getZ(), 0, 0);
            bat.getPersistentData().putBoolean("WITCNoDrops", true);
            level.addFreshEntity(bat);
        }
    }

    public static void infection(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        if (hit instanceof EntityHitResult entity && entity.getEntity() instanceof LivingEntity target) {
            if (target instanceof Villager villager) {
                if (infectVillager(level, villager)) return;
            } else {
                target.hurt(level.damageSources().indirectMagic(brew, brew.getOwner()), BrewImpacts.enhanced(brew) ? 4 : 1);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 8), brew.getEffectSource());
                return;
            }
        } else if (hit instanceof BlockHitResult block) {
            BlockPos pos = block.getBlockPos();
            var original = level.getBlockState(pos);
            Block infested = null;
            if (original.is(Blocks.STONE)) infested = Blocks.INFESTED_STONE;
            if (original.is(Blocks.COBBLESTONE)) infested = Blocks.INFESTED_COBBLESTONE;
            if (original.is(Blocks.STONE_BRICKS)) infested = Blocks.INFESTED_STONE_BRICKS;
            if (infested != null && BrewTerrain.setBlock(brew.getOwner(), level, pos, infested.defaultBlockState(),
                    block.getDirection())) return;
        }
        BrewImpacts.recoverBrew(brew, hit);
    }

    public static boolean infectVillager(ServerLevel level, Villager villager) {
        if (!villager.isAlive() || !ForgeEventFactory.canLivingConvert(villager, EntityType.ZOMBIE_VILLAGER, ignored -> {})) {
            return false;
        }
        var zombie = EntityType.ZOMBIE_VILLAGER.create(level);
        if (zombie == null) return false;
        zombie.copyPosition(villager);
        zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.CONVERSION,
                new Zombie.ZombieGroupData(false, true), null);
        zombie.setBaby(villager.isBaby());
        zombie.setNoAi(villager.isNoAi());
        zombie.setInvulnerable(villager.isInvulnerable());
        zombie.setCustomName(villager.getCustomName());
        zombie.setCustomNameVisible(villager.isCustomNameVisible());
        if (villager.isPersistenceRequired()) zombie.setPersistenceRequired();
        zombie.setVillagerData(villager.getVillagerData());
        zombie.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
        zombie.setTradeOffers(villager.getOffers().createTag());
        zombie.setVillagerXp(villager.getVillagerXp());
        if (!level.addFreshEntity(zombie)) return false;
        if (villager.isPassenger()) {
            var vehicle = villager.getVehicle();
            villager.stopRiding();
            zombie.startRiding(vehicle, true);
        }
        VampireVillages.releaseResidentClaims(villager);
        ForgeEventFactory.onLivingConvert(villager, zombie);
        villager.discard();
        level.levelEvent(null, 1026, zombie.blockPosition(), 0);
        return true;
    }

    static void blight(LivingEntity target, int strength) {
        if (!(target.level() instanceof ServerLevel level) || !target.isAlive()) return;
        if (target instanceof Villager villager && level.random.nextInt(Math.max(1, 10 - strength * 2)) == 0) {
            infectVillager(level, villager);
        } else if (target instanceof Cow cow && level.random.nextInt(Math.max(1, 20 - strength * 3)) == 0) {
            turnIntoMooshroom(level, cow);
        } else if (target instanceof Animal && level.random.nextInt(strength > 1 ? 2 : 3) == 0) {
            target.hurt(level.damageSources().magic(), 20);
        }
    }

    public static void turnIntoMooshroom(ServerLevel level, Cow cow) {
        if (!ForgeEventFactory.canLivingConvert(cow, EntityType.MOOSHROOM, delay -> {}) || !cow.isAlive()) return;
        var mooshroom = EntityType.MOOSHROOM.create(level);
        if (mooshroom == null) return;
        mooshroom.copyPosition(cow);
        mooshroom.setAge(cow.getAge());
        mooshroom.setNoAi(cow.isNoAi());
        mooshroom.setInvulnerable(cow.isInvulnerable());
        mooshroom.setCustomName(cow.getCustomName());
        mooshroom.setCustomNameVisible(cow.isCustomNameVisible());
        if (cow.isPersistenceRequired()) mooshroom.setPersistenceRequired();
        if (!level.addFreshEntity(mooshroom)) return;
        if (cow.isPassenger()) mooshroom.startRiding(cow.getVehicle(), true);
        ForgeEventFactory.onLivingConvert(cow, mooshroom);
        cow.discard();
        level.levelEvent(null, 1026, mooshroom.blockPosition(), 0);
    }

    public static void quicklime(ThrownBrew shot, HitResult hit) {
        if (!(shot.level() instanceof ServerLevel level)) return;
        if (hit instanceof EntityHitResult entity) {
            if (entity.getEntity() instanceof LivingEntity target) {
                if (!target.hasEffect(MobEffects.BLINDNESS)) {
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60), shot.getEffectSource());
                }
                float damage = target instanceof Slime ? 4 : target.getHealth() == target.getMaxHealth() ? 0.5F : 0.1F;
                target.hurt(level.damageSources().indirectMagic(shot, shot.getOwner()), damage);
            }
        } else {
            BrewImpacts.recoverBrew(shot, hit);
        }
    }

    public static void wasting(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        boolean enhanced = BrewImpacts.enhanced(brew);
        Vec3 impact = hit instanceof EntityHitResult entity ? entity.getEntity().position()
                : hit instanceof BlockHitResult block ? Vec3.atLowerCornerOf(block.getBlockPos()) : hit.getLocation();
        double radius = enhanced ? 5 : 4;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, BrewImpacts.splashBounds(brew, radius),
                candidate -> candidate.distanceToSqr(impact) < radius * radius)) {
            int ticks = splashTicks(hit, victim, impact, radius, 400);
            if (victim instanceof Player player) {
                int foodLoss = enhanced ? 6 : 10;
                if (player.getFoodData().getFoodLevel() > foodLoss) player.getFoodData().eat(-foodLoss, 0);
                victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, ticks * 2, enhanced ? 2 : 1), brew.getEffectSource());
            } else {
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, ticks * 2, enhanced ? 1 : 0), brew.getEffectSource());
            }
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, Math.max(ticks / 3, 40)), brew.getEffectSource());
        }
        BlockPos center = BlockPos.containing(impact);
        int reach = (int) radius - 1;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-reach, -reach, -reach), center.offset(reach, reach, reach))) {
            if (pos.distSqr(center) > reach * reach) continue;
            var plant = level.getBlockState(pos);
            if (plant.is(WASTING_PLANTS) && BrewTerrain.canChange(brew.getOwner(), level, pos, plant)) {
                level.destroyBlock(pos, true, brew.getOwner());
            }
        }
    }

    private static boolean direct(HitResult hit, LivingEntity victim) {
        return hit instanceof EntityHitResult entity && entity.getEntity() == victim;
    }

    private static int splashTicks(HitResult hit, LivingEntity victim, Vec3 impact, double radius, int base) {
        if (direct(hit, victim)) return base;
        return (int) Math.round(base * (1 - Math.sqrt(victim.distanceToSqr(impact)) / radius));
    }

    private static void move(Entity victim, Vec3 speed) {
        victim.setDeltaMovement(speed);
        victim.hurtMarked = true;
    }
}
