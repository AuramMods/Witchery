package art.arcane.witchery.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import org.jetbrains.annotations.Nullable;

public final class LostSoul extends Monster {
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(LostSoul.class, EntityDataSerializers.INT);
    private static final int[] COLORS = {0xFF0000, 0x00FF00, 0x0000FF};

    private int soulType;
    private int remaining = -1;

    public LostSoul(EntityType<? extends LostSoul> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 20, true);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.MOVEMENT_SPEED, 0.4).add(Attributes.FLYING_SPEED, 0.4).add(Attributes.FOLLOW_RANGE, 20);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 0.8));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COLOR, COLORS[0]);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData group, @Nullable CompoundTag data) {
        configure(random.nextInt(COLORS.length), -1);
        return super.finalizeSpawn(level, difficulty, reason, group, data);
    }

    void configure(int kind, int ticks) {
        soulType = Mth.clamp(kind, 0, COLORS.length - 1);
        entityData.set(COLOR, COLORS[soulType]);
        remaining = ticks;
    }

    public int color() {
        return entityData.get(COLOR);
    }

    void setLifetime(int ticks) { remaining = ticks; }

    public boolean hasTimedLife() { return remaining >= 0; }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel && hasTimedLife() && --remaining <= 0) discard();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (soulType != 1 && random.nextInt(4) == 0) {
            DamageSource source = soulType == 0 ? damageSources().inFire() : damageSources().indirectMagic(this, this);
            return target.hurt(source, (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
        }
        return super.doHurtTarget(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return super.hurt(source, amount);
        }
        boolean heat = source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION);
        boolean magic = source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(art.arcane.witchery.registry.WitcheryDamageTypes.DEMONIC);
        boolean vulnerable = switch (soulType) {
            case 0 -> heat;
            case 1 -> !heat && !magic && !source.is(DamageTypeTags.BYPASSES_ARMOR)
                    && !source.is(DamageTypeTags.IS_PROJECTILE) && !source.is(DamageTypeTags.IS_FALL)
                    && !source.is(DamageTypeTags.IS_DROWNING) && !source.is(DamageTypes.IN_WALL);
            case 2 -> magic;
            default -> false;
        };
        return vulnerable && super.hurt(source, Math.min(amount, 15));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("SoulType", (byte) soulType);
        tag.putInt("FeatherColor", color());
        tag.putInt("SuicideIn", remaining);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        configure(tag.contains("SoulType", Tag.TAG_ANY_NUMERIC) ? tag.getInt("SoulType") : random.nextInt(COLORS.length),
                tag.contains("SuicideIn", Tag.TAG_ANY_NUMERIC) ? tag.getInt("SuicideIn") : -1);
        if (tag.contains("FeatherColor", Tag.TAG_ANY_NUMERIC)) entityData.set(COLOR, tag.getInt("FeatherColor"));
    }
}
