package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.ThrownBrew;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

import java.util.ArrayList;
import java.util.List;

public final class BrewImpacts {
    private static final double SPLASH_RADIUS = 4;

    private BrewImpacts() {
    }

    public static void splashInk(ThrownBrew brew, HitResult hit) {
        Vec3 impact = hit.getLocation();
        for (LivingEntity entity : brew.level().getEntitiesOfClass(LivingEntity.class, splashBounds(brew, impact))) {
            boolean direct = hit instanceof EntityHitResult entityHit && entityHit.getEntity() == entity;
            double distance = entity.distanceToSqr(impact);
            if (direct || distance < SPLASH_RADIUS * SPLASH_RADIUS) {
                int duration = direct ? 400 : (int) Math.round((1 - Math.sqrt(distance) / SPLASH_RADIUS) * 400);
                if (duration > 0) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration), brew.getEffectSource());
                }
                if (entity instanceof Mob mob) {
                    mob.setTarget(null);
                    mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                }
            }
        }
    }

    public static void splashLove(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 impact = hit.getLocation();
        List<Villager> villagers = new ArrayList<>();
        for (AgeableMob mob : level.getEntitiesOfClass(AgeableMob.class, splashBounds(brew, impact),
                candidate -> candidate.getAge() >= 0 && candidate.distanceToSqr(impact) < SPLASH_RADIUS * SPLASH_RADIUS)) {
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
    }

    private static AABB splashBounds(ThrownBrew brew, Vec3 impact) {
        return brew.getBoundingBox().move(impact.subtract(brew.position())).inflate(SPLASH_RADIUS, 2, SPLASH_RADIUS);
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
}
