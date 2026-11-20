package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.player.Player;
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

    public static void depositSpirit(ThrownBrew brew, HitResult hit) {
        depositLiquid(brew, hit, "spiritflowing");
    }

    public static void depositHollowTears(ThrownBrew brew, HitResult hit) {
        depositLiquid(brew, hit, "hollowtears");
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
