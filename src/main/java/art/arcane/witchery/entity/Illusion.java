package art.arcane.witchery.entity;

import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.WitcheryCreatureSpawns;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class Illusion extends Monster {
    public enum Kind {
        CREEPER(null), SPIDER("mob.spider.say"), ZOMBIE("mob.zombie.say");

        @Nullable
        private final String sound;

        Kind(@Nullable String sound) {
            this.sound = sound;
        }
    }

    private static final EntityDataAccessor<String> VICTIM =
            SynchedEntityData.defineId(Illusion.class, EntityDataSerializers.STRING);
    private final Kind kind;
    private byte illusionType;

    public Illusion(EntityType<? extends Illusion> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 0);
    }

    public static boolean spawnNear(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        var random = level.getRandom();
        var type = switch (random.nextInt(3)) {
            case 0 -> WitcheryEntities.ILLUSION_CREEPER;
            case 1 -> WitcheryEntities.ILLUSION_SPIDER;
            default -> WitcheryEntities.ILLUSION_ZOMBIE;
        };
        Illusion illusion = type.get().create(level);
        if (illusion == null) return false;
        illusion.entityData.set(VICTIM, player.getGameProfile().getName());
        illusion.setTarget(player);
        return WitcheryCreatureSpawns.spawnNear(level, player.blockPosition(), illusion, 4, 9, random);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1, false));
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VICTIM, "");
    }

    public String victimName() { return entityData.get(VICTIM); }

    @Override
    public void aiStep() {
        if (level() instanceof ServerLevel level && isAlive()) {
            var victim = loadedVictim(level);
            if (getTarget() != victim) setTarget(victim);
        }
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) return;
        var random = level.getRandom();
        if (random.nextInt(15) == 0) {
            float remaining = getHealth() - 1;
            if (remaining <= 0.5F) discard();
            else setHealth(remaining);
        }
        if (random.nextInt(40) == 0 && kind.sound != null) {
            var victim = loadedVictim(level);
            if (victim != null && distanceToSqr(victim) < 64) {
                WitcheryNetwork.sendSound(victim, kind.sound, getX(), getY(), getZ(), 1, 1);
            }
        }
    }

    @Nullable
    private ServerPlayer loadedVictim(ServerLevel level) {
        var victim = level.getServer().getPlayerList().getPlayerByName(victimName());
        return victim != null && victim.isAlive() && victim.level() == level ? victim : null;
    }

    @Override
    public int getMaxFallDistance() {
        return getTarget() == null ? 3 : 3 + (int) (getHealth() - 1);
    }

    @Override public boolean doHurtTarget(Entity entity) { return true; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return (source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD))
                && super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Victim", victimName());
        tag.putInt("IllusionType", illusionType);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(VICTIM, tag.getString("Victim"));
        illusionType = (byte) tag.getInt("IllusionType");
    }
}
