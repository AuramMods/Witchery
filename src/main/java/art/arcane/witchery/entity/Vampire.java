package art.arcane.witchery.entity;

import art.arcane.witchery.item.GlassGobletItem;
import art.arcane.witchery.vampirism.BloodFeeding;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public final class Vampire extends PathfinderMob {
    private static final int VILLAGE_RANGE = 128;
    private static final int BITES_PER_NIGHT = 5;
    private BlockPos home;
    private boolean hunting;
    private int bites;

    public Vampire(EntityType<? extends Vampire> type, Level level) {
        super(type, level);
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        xpReward = 20;
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.MOVEMENT_SPEED, 0.4).add(Attributes.ATTACK_DAMAGE, 5)
                .add(Attributes.FOLLOW_RANGE, 40);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new RestrictSunGoal(this));
        goalSelector.addGoal(3, new FleeSunGoal(this, 1));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, false));
        goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, 10, false, true,
                villager -> hunting && level().isNight()));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, true,
                target -> level().isNight() && !hunting && target instanceof Player player && Vampirism.level(player) == 0));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        home = blockPosition();
        return super.finalizeSpawn(world, difficulty, reason, data, tag);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel world)) return;
        if (home == null) home = blockPosition();
        if (!world.isNight()) {
            if (getLastHurtByMob() == null) setTarget(null);
            hunting = false;
            bites = 0;
            if (tickCount % 100 == 2 && returnHome(world)) restrictTo(home, 4);
            if (tickCount % 20 == 2 && Vampirism.isInSunlight(this)) setSecondsOnFire(2);
        } else if (hunting && bites >= BITES_PER_NIGHT) {
            if (tickCount % 20 == 2 && returnHome(world)) {
                hunting = false;
                setTarget(null);
                setLastHurtByMob(null);
                restrictTo(home, 4);
            }
        } else if (bites == 0 && !hunting && tickCount % 500 == 2) {
            beginHunt(world);
        }
    }

    private void beginHunt(ServerLevel world) {
        var nearby = world.getEntitiesOfClass(Villager.class, getBoundingBox().inflate(VILLAGE_RANGE),
                villager -> villager.isAlive() && distanceToSqr(villager) <= VILLAGE_RANGE * VILLAGE_RANGE
                        && world.isVillage(villager.blockPosition()));
        var victim = nearby.stream().min(Comparator.comparingDouble(this::distanceToSqr));
        if (victim.isEmpty()) return;
        BlockPos village = victim.get().blockPosition();
        if (village.distSqr(blockPosition()) <= 16 || relocate(world, village)) {
            clearRestriction();
            hunting = true;
            setTarget(victim.get());
        }
    }

    private boolean returnHome(ServerLevel world) {
        return home.distSqr(blockPosition()) <= 16 || relocate(world, home);
    }

    private boolean relocate(ServerLevel world, BlockPos destination) {
        double oldX = getX();
        double oldY = getY();
        double oldZ = getZ();
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos landing = destination.offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
            if (!world.hasChunkAt(landing) || !world.getBlockState(landing.below()).isSolidRender(world, landing.below())) continue;
            if (randomTeleport(landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5, false)) {
                getNavigation().stop();
                world.sendParticles(ParticleTypes.SMOKE, oldX, oldY + 0.9, oldZ, 16, 0.4, 0.5, 0.4, 0.01);
                world.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.9, getZ(), 16, 0.4, 0.5, 0.4, 0.01);
                playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof Villager villager)) return super.doHurtTarget(target);
        if (level() instanceof ServerLevel world && hunting && bites < BITES_PER_NIGHT && random.nextInt(10) == 0) {
            bites++;
            if (BloodFeeding.takeFromVillager(villager, 30) > 0) {
                heal(4);
                GlassGobletItem.bloodParticles(world, villager);
            }
        }
        return true;
    }

    @Override
    public void die(DamageSource source) {
        Entity attacker = source.getEntity();
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(DamageTypes.GENERIC_KILL) || attacker instanceof Vampire
                || attacker instanceof Player player && Vampirism.level(player) > 0
                || attacker instanceof LivingEntity living && Vampirism.isBoss(living)) {
            super.die(source);
        } else {
            setHealth(1);
        }
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        BlockPos base = home == null ? blockPosition() : home;
        tag.putInt("BaseX", base.getX());
        tag.putInt("BaseY", base.getY());
        tag.putInt("BaseZ", base.getZ());
        tag.putBoolean("Hunting", hunting);
        tag.putInt("HuntBites", bites);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BaseY", Tag.TAG_ANY_NUMERIC)) {
            home = new BlockPos(tag.getInt("BaseX"), tag.getInt("BaseY"), tag.getInt("BaseZ"));
        }
        hunting = tag.getBoolean("Hunting");
        bites = Math.max(0, Math.min(BITES_PER_NIGHT, tag.getInt("HuntBites")));
    }
}
