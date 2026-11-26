package art.arcane.witchery.entity;

import art.arcane.witchery.block.entity.BloodCrucibleBlockEntity;
import art.arcane.witchery.item.VampireClothingItem;
import art.arcane.witchery.item.VampireClothingItem.Style;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.vampirism.BloodFeeding;
import art.arcane.witchery.vampirism.VampireVillages;
import art.arcane.witchery.vampirism.Vampirism;
import art.arcane.witchery.world.WitcheryTeleport;
import art.arcane.witchery.world.migration.LegacyVillages;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public final class Vampire extends PathfinderMob {
    private static final int VILLAGE_RANGE = 128;
    private static final int BITES_PER_NIGHT = 5;
    private static final int TRAVEL_HEIGHT = 8;
    private BlockPos home;
    private boolean hunting;
    private int bites;
    @Nullable private VampireVillages.Territory huntVillage;

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
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(WitcheryDamageTypes.SPECTRAL_TOUCH) || super.isInvulnerableTo(source);
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
        boolean fitted = random.nextBoolean();
        boolean reinforced = random.nextInt(3) == 0;
        Style coat = fitted ? (reinforced ? Style.FITTED_CHAIN_COAT : Style.FITTED_COAT)
                : (reinforced ? Style.CHAIN_COAT : Style.COAT);
        Style legs = (fitted ? random.nextInt(4) != 0 : random.nextInt(3) == 0) ? Style.KILT : Style.TROUSERS;
        setItemSlot(EquipmentSlot.CHEST, VampireClothingItem.make(coat));
        setItemSlot(EquipmentSlot.LEGS, VampireClothingItem.make(legs));
        setItemSlot(EquipmentSlot.FEET, VampireClothingItem.make(Style.BOOTS));
        return super.finalizeSpawn(world, difficulty, reason, data, tag);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel world)) return;
        if (home == null) home = blockPosition();
        if (!world.isNight()) {
            if (getLastHurtByMob() == null) setTarget(null);
            if (tickCount % 100 == 2) {
                finishHunt();
                bites = 0;
                if (distanceToSqr(Vec3.atLowerCornerOf(home)) > 16) returnHome(world);
            }
            if (tickCount % 20 == 2 && Vampirism.isInSunlight(this)) setSecondsOnFire(2);
        } else if (bites >= BITES_PER_NIGHT) {
            if (hunting) {
                finishHunt();
                setTarget(null);
                setLastHurtByMob(null);
                returnHome(world);
                fillNearbyCrucible(world);
            }
        } else if (!hunting && tickCount % 500 == 2) {
            beginHunt(world);
        }
    }

    private void finishHunt() {
        hunting = false;
        if (huntVillage != null) {
            huntVillage = null;
            clearRestriction();
        }
    }

    private void fillNearbyCrucible(ServerLevel world) {
        var pos = new BlockPos.MutableBlockPos();
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                pos.setWithOffset(home, x, 0, z);
                for (int y = -6; y <= 6; y++) {
                    pos.setY(home.getY() + y);
                    if (world.getBlockEntity(pos) instanceof BloodCrucibleBlockEntity crucible) {
                        crucible.depositBlood();
                        return;
                    }
                }
            }
        }
    }

    private void beginHunt(ServerLevel world) {
        var legacy = LegacyVillages.nearest(world, blockPosition(), VILLAGE_RANGE).orElse(null);
        var nearby = world.getEntitiesOfClass(Villager.class, getBoundingBox().inflate(VILLAGE_RANGE),
                villager -> villager.isAlive() && distanceToSqr(villager) <= VILLAGE_RANGE * VILLAGE_RANGE
                        && world.isVillage(villager.blockPosition())
                        && (legacy == null || LegacyVillages.nearest(world, villager.blockPosition(), 0).isEmpty()));
        var victim = nearby.stream().min(Comparator.comparingDouble(this::distanceToSqr));
        if (legacy != null && (victim.isEmpty()
                || distanceToSqr(Vec3.atCenterOf(legacy.center())) <= distanceToSqr(victim.get()))) {
            relocate(world, legacy.center());
            huntVillage = new VampireVillages.Territory(legacy.center(), legacy.radius());
            restrictTo(huntVillage.center(), huntVillage.radius());
            hunting = true;
            setTarget(null);
            return;
        }
        if (victim.isEmpty()) return;
        BlockPos village = victim.get().blockPosition();
        relocate(world, village);
        clearRestriction();
        hunting = true;
        setTarget(victim.get());
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return (!(target instanceof Villager) || inHuntingGround(target)) && super.canAttack(target);
    }

    private boolean inHuntingGround(LivingEntity target) {
        return huntVillage == null || isWithinRestriction(target.blockPosition());
    }

    private void returnHome(ServerLevel world) {
        relocate(world, home);
        restrictTo(home, 4);
    }

    boolean relocate(ServerLevel world, BlockPos destination) {
        if (world.getWorldBorder().isWithinBounds(destination)) {
            for (int step = 0; step <= TRAVEL_HEIGHT * 2; step++) {
                int offset = (step & 1) == 0 ? -step / 2 : (step + 1) / 2;
                BlockPos ground = destination.above(offset);
                if (world.isOutsideBuildHeight(ground) || world.isOutsideBuildHeight(ground.above(2))) break;
                if (!world.getBlockState(ground).isSolidRender(world, ground)
                        || !world.isEmptyBlock(ground.above()) || !world.isEmptyBlock(ground.above(2))) continue;
                Vec3 landing = Vec3.atLowerCornerOf(ground).add(0.5, 1.05, 0.5);
                if (WitcheryTeleport.move(this, world, landing, WitcheryNetwork::sendVampireHuntTravel)) {
                    getNavigation().stop();
                    return true;
                }
                break;
            }
        }
        WitcheryNetwork.sendVampireHuntTravel(world, position());
        WitcheryNetwork.sendVampireHuntTravel(world, position());
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof Villager villager)) return super.doHurtTarget(target);
        if (level() instanceof ServerLevel world && hunting && bites < BITES_PER_NIGHT
                && inHuntingGround(villager) && random.nextInt(10) == 0) {
            bites++;
            if (BloodFeeding.takeFromVillager(villager, 30) > 0) {
                heal(4);
                WitcheryNetwork.sendVampireBite(world, villager, false);
            }
        }
        return true;
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
        if (huntVillage != null) {
            var village = new CompoundTag();
            village.putLong("Center", huntVillage.center().asLong());
            village.putInt("Radius", huntVillage.radius());
            tag.put("HuntVillage", village);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BaseY", Tag.TAG_ANY_NUMERIC)) {
            home = new BlockPos(tag.getInt("BaseX"), tag.getInt("BaseY"), tag.getInt("BaseZ"));
        }
        finishHunt();
        hunting = tag.getBoolean("Hunting");
        bites = Math.max(0, Math.min(BITES_PER_NIGHT, tag.getInt("HuntBites")));
        var village = tag.getCompound("HuntVillage");
        int radius = village.getInt("Radius");
        if (hunting && radius > 0 && village.contains("Center", Tag.TAG_LONG)) {
            huntVillage = new VampireVillages.Territory(BlockPos.of(village.getLong("Center")), radius);
            restrictTo(huntVillage.center(), radius);
        }
    }
}
