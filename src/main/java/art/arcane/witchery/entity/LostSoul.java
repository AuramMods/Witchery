package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public final class LostSoul extends FlyingSpirit {
    private static final int[] COLORS = {0xFF0000, 0x00FF00, 0x0000FF};

    private int soulType;
    private int remaining = -1;
    private boolean dropOnExpiry;

    public LostSoul(EntityType<? extends LostSoul> type, Level level) {
        super(type, level);
        setColor(COLORS[0]);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.MOVEMENT_SPEED, 0.4).add(Attributes.FLYING_SPEED, 0.4).add(Attributes.FOLLOW_RANGE, 16);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(2, new FlyingCompanionGoals.Defend(this));
        goalSelector.addGoal(9, new FlyingCompanionGoals.Land(this));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10, 0.2F));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData group, @Nullable CompoundTag data) {
        configure(level.getLevel().random.nextInt(COLORS.length), -1);
        return super.finalizeSpawn(level, difficulty, reason, group, data);
    }

    void configure(int kind, int ticks) {
        soulType = Mth.clamp(kind, 0, COLORS.length - 1);
        setColor(COLORS[soulType]);
        remaining = ticks;
        dropOnExpiry = false;
    }

    void setLifetime(int ticks) { remaining = ticks; }

    public boolean hasTimedLife() { return remaining >= 0; }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel server && isAlive() && hasTimedLife() && --remaining <= 0) {
            if (dropOnExpiry) {
                WitcheryNetwork.sendSpiritDeparture(server, position());
                spawnAtLocation(WitcheryIngredient.SUBDUED_SPIRIT.item());
            }
            discard();
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        spawnAtLocation(WitcheryIngredient.SUBDUED_SPIRIT.item());
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (level().random.nextInt(4) != 0 || soulType == 1) return super.doHurtTarget(target);

        DamageSource source = soulType == 0 ? WitcheryDamageTypes.directFire(level()) : damageSources().magic();
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        LivingEntity living = target instanceof LivingEntity victim ? victim : null;
        if (living != null) damage += EnchantmentHelper.getDamageBonus(getMainHandItem(), living.getMobType());
        if (!target.hurt(source, damage)) return false;
        int knockback = EnchantmentHelper.getKnockbackBonus(this);
        if (living != null && knockback > 0) {
            float yaw = getYRot() * Mth.DEG_TO_RAD;
            living.knockback(knockback * 0.5, Mth.sin(yaw), -Mth.cos(yaw));
            setDeltaMovement(getDeltaMovement().multiply(0.6, 1, 0.6));
        }
        int fire = EnchantmentHelper.getFireAspect(this);
        if (fire > 0) target.setSecondsOnFire(fire * 4);
        if (living != null) EnchantmentHelper.doPostHurtEffects(living, this);
        EnchantmentHelper.doPostDamageEffects(this, target);
        setLastHurtMob(target);
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.GENERIC_KILL)) {
            return super.hurt(source, amount);
        }
        boolean heat = source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION);
        boolean magic = source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypes.DRAGON_BREATH) || source.is(WitcheryDamageTypes.DEMONIC)
                || source.is(WitcheryDamageTypes.VAMPIRE_FIRE) || source.is(WitcheryDamageTypes.SUNLIGHT);
        boolean vulnerable = switch (soulType) {
            case 0 -> heat;
            case 1 -> !heat && !magic && !source.is(DamageTypeTags.IS_PROJECTILE)
                    && !source.is(DamageTypeTags.IS_DROWNING) && !source.is(DamageTypes.IN_WALL)
                    && !source.is(DamageTypes.CACTUS) && !source.is(DamageTypes.WITHER);
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
        if (tag.contains("FeatherColor", Tag.TAG_ANY_NUMERIC)) setColor(tag.getInt("FeatherColor"));
        // The original read restored the inherited Spirit timer as well as the Lost Soul timer.
        dropOnExpiry = remaining > 0;
    }
}
