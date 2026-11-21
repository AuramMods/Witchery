package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.familiar.Familiar;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.MirrorDisguise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class BrewCreatureEffects {
    private static final TagKey<Block> WASTING_PLANTS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "wasting_plants"));

    private BrewCreatureEffects() {}

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
        Vec3 impact = hit.getLocation();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, BrewImpacts.splashBounds(brew, impact, 4),
                entity -> entity != thrower && entity.isAlive() && entity.distanceToSqr(impact) < 16
                        && entity.getType() != EntityType.ENDER_DRAGON
                        && entity.getType() != WitcheryEntities.all().get("hornedhuntsman").get())) {
            Vec3 difference = thrower.position().subtract(victim.position());
            move(victim, new Vec3(difference.x * 0.15, 0.4, difference.z * 0.15));
            victim.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 1), brew.getEffectSource());
        }
    }

    public static void cursedLeaping(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        Vec3 impact = hit.getLocation();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, BrewImpacts.splashBounds(brew, impact, 5),
                entity -> entity.isAlive() && entity.distanceToSqr(impact) < 25)) {
            double lift = 1.6 * (direct(hit, victim) ? 1 : 1 - Math.sqrt(victim.distanceToSqr(impact)) / 20);
            victim.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, 3), brew.getEffectSource());
            move(victim, victim.getDeltaMovement().add(0, lift, 0));
        }
    }

    public static void hitchcock(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        if (!(hit instanceof EntityHitResult entity) || !(entity.getEntity() instanceof LivingEntity victim)) {
            BrewImpacts.recoverBrew(brew, level, hit);
            return;
        }
        int count = 3 + level.random.nextInt(2);
        for (int i = 0; i < count; i++) {
            var owl = WitcheryEntities.OWL.get().create(level);
            if (owl == null) continue;
            owl.moveTo(victim.getX() + level.random.nextInt(5) - 2,
                    victim.getY() + victim.getBbHeight() + 1 + level.random.nextInt(2),
                    victim.getZ() + level.random.nextInt(5) - 2, 0, 0);
            owl.attackTemporarily(victim, 400);
            if (level.addFreshEntity(owl)) {
                level.sendParticles(ParticleTypes.PORTAL, owl.getX(), owl.getY(), owl.getZ(), 16, 0.5, 0.5, 0.5, 0.1);
            }
        }
        level.playSound(null, victim.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1, 1);
    }

    public static void bats(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        boolean enhanced = BrewImpacts.enhanced(brew);
        BlockPos release = BrewTerrain.splashPosition(level, hit);
        for (int i = 0; i < (enhanced ? 14 : 10); i++) {
            var bat = EntityType.BAT.create(level);
            if (bat == null) continue;
            Vec3 position = Vec3.atCenterOf(release);
            bat.moveTo(position.x, position.y, position.z, 0, 0);
            bat.setResting(false);
            bat.getPersistentData().putBoolean("WITCNoDrops", true);
            level.addFreshEntity(bat);
        }
        level.playSound(null, release, SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1, 1);
        Vec3 impact = hit.getLocation();
        double radius = enhanced ? 4 : 3;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, BrewImpacts.splashBounds(brew, impact, radius))) {
            int ticks = splashTicks(hit, victim, impact, radius, 100);
            if (ticks > 0) {
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 5), brew.getEffectSource());
                BrewImpacts.clearTarget(victim);
            }
        }
    }

    public static void wasting(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        boolean enhanced = BrewImpacts.enhanced(brew);
        Vec3 impact = hit instanceof EntityHitResult entity ? entity.getEntity().position()
                : hit instanceof BlockHitResult block ? Vec3.atLowerCornerOf(block.getBlockPos()) : hit.getLocation();
        double radius = enhanced ? 5 : 4;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                BrewImpacts.splashBounds(brew, hit.getLocation(), radius))) {
            int ticks = splashTicks(hit, victim, impact, radius, 400);
            if (ticks <= 0) continue;
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
            if (plant.is(WASTING_PLANTS) && BrewTerrain.canChange(brew, level, pos, plant)) {
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

    private static void move(LivingEntity victim, Vec3 speed) {
        victim.setDeltaMovement(speed);
        victim.hurtMarked = true;
    }
}
