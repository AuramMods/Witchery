package art.arcane.witchery.entity;

import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.infusion.SpellKnowledge;
import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.infusion.SymbolTerrain;
import art.arcane.witchery.item.MysticBranchItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.vampirism.Vampirism;
import art.arcane.witchery.world.TormentTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.UUID;
import java.util.function.Consumer;

public final class SymbolProjectile extends Projectile {
    private static final int MAX_FLIGHT_TICKS = 200;
    private static final int MOB_SHOOTER_GRACE_TICKS = 25;
    private static final EntityDataAccessor<Integer> SPELL = SynchedEntityData.defineId(SymbolProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> ACCELERATION = SynchedEntityData.defineId(SymbolProjectile.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> EFFECT_LEVEL = SynchedEntityData.defineId(SymbolProjectile.class, EntityDataSerializers.INT);
    private int age;
    private int ticksUntilImpact = -1;
    @Nullable
    private UUID initialMobShooter;

    public SymbolProjectile(EntityType<? extends SymbolProjectile> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public SymbolProjectile(ServerLevel level, ServerPlayer owner, SymbolSpell spell) {
        this(level, owner, spell, 1);
    }

    public SymbolProjectile(ServerLevel level, ServerPlayer owner, SymbolSpell spell, int effectLevel) {
        this(level, owner, spell, owner.getEyePosition().add(0, -0.1, 0).add(owner.getLookAngle().scale(1.5)),
                owner.getLookAngle());
        entityData.set(EFFECT_LEVEL, effectLevel);
    }

    public SymbolProjectile(ServerLevel level, LivingEntity owner, SymbolSpell spell, Vec3 origin, Vec3 direction) {
        this(level, owner, spell, origin);
        entityData.set(ACCELERATION, direction.normalize().scale(0.3).toVector3f());
    }

    private SymbolProjectile(ServerLevel level, LivingEntity owner, SymbolSpell spell, Vec3 origin) {
        this(WitcheryEntities.SYMBOL_PROJECTILE.get(), level);
        setOwner(owner);
        moveTo(origin.x, origin.y, origin.z, owner.getYRot(), owner.getXRot());
        entityData.set(SPELL, spell.id());
        if (spell == SymbolSpell.MORSMORDRE) ticksUntilImpact = spell.lifetime();
    }

    static SymbolProjectile mobShot(ServerLevel level, LivingEntity caster, LivingEntity target, SymbolSpell spell,
                                    float attackStrength) {
        Vec3 origin = new Vec3(caster.getX(), caster.getY(0.5), caster.getZ());
        var projectile = new SymbolProjectile(level, caster, spell, origin);
        Vec3 aim = MobProjectile.aim(caster, target, attackStrength);
        projectile.entityData.set(ACCELERATION, MobProjectile.acceleration(aim, projectile.random).toVector3f());
        projectile.initialMobShooter = caster.getUUID();
        return projectile;
    }

    static void castMobSymbol(ServerLevel level, LivingEntity caster, LivingEntity target, SymbolSpell spell, float strength) {
        MobProjectile.playLaunchCue(level, caster);
        // The original single-symbol casts advance the caster's random source before aiming.
        caster.getRandom().nextInt(10);
        level.addFreshEntity(mobShot(level, caster, target, spell, strength));
    }

    static SymbolSpell commonMobSpell(RandomSource random) {
        return switch ((int) Math.ceil(random.nextDouble() * 21)) {
            case 15, 16 -> SymbolSpell.EXPELLIARMUS;
            case 17, 18 -> SymbolSpell.FLIPENDO;
            case 19, 20 -> SymbolSpell.IMPEDIMENTA;
            case 21 -> SymbolSpell.CONFUNDUS;
            default -> SymbolSpell.IGNIANIMA;
        };
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SPELL, 0);
        entityData.define(ACCELERATION, new Vector3f());
        entityData.define(EFFECT_LEVEL, 1);
    }

    @Nullable
    public SymbolSpell spell() {
        return SymbolSpell.byId(entityData.get(SPELL));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double size = getBoundingBox().getSize() * 4;
        double range = (Double.isNaN(size) ? 4 : size) * 64;
        return distance < range * range;
    }

    @Override
    public void tick() {
        SymbolSpell spell = spell();
        if (spell == null) {
            if (!level().isClientSide) discard();
            return;
        }
        age++;
        Vec3 start = position();
        Vec3 velocity = getDeltaMovement();
        boolean inWater = isInWater();
        super.tick();

        Vec3 next = start.add(velocity);
        HitResult hit = level().clip(new ClipContext(start, next, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
        Vec3 entityEnd = hit.getType() == HitResult.Type.MISS ? next : hit.getLocation();
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(level(), this, start, entityEnd,
                getBoundingBox().expandTowards(velocity).inflate(1), this::canHitEntity);
        if (entity != null) hit = entity;
        if (hit.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hit)) onHit(hit);
        if (isRemoved()) return;

        if (level() instanceof ServerLevel server) {
            if (ticksUntilImpact > 0 && --ticksUntilImpact == 0) {
                impact(server, spell, null, start);
                discard();
                return;
            }
            if (age >= MAX_FLIGHT_TICKS || spell.lifetime() == 0) {
                discard();
                return;
            }
        }

        checkInsideBlocks();
        updateRotation();
        setPos(next);
        if (inWater) {
            Vec3 bubbles = next.subtract(velocity.scale(0.25));
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.BUBBLE, bubbles.x, bubbles.y, bubbles.z, velocity.x, velocity.y, velocity.z);
            }
        }

        Vec3 acceleration = new Vec3(entityData.get(ACCELERATION));
        if (spell.falls() && effectLevel() == 1) {
            Vec3 decayed = acceleration.scale(0.8);
            if (!level().isClientSide) entityData.set(ACCELERATION, decayed.toVector3f());
            setDeltaMovement(velocity.add(decayed).add(0, -0.05, 0));
        } else {
            setDeltaMovement(velocity.add(acceleration).scale(inWater ? 0.8 : 0.95));
        }
        if (level().isClientSide) {
            boolean curse = spell.isCurse();
            level().addParticle(curse ? ParticleTypes.ENTITY_EFFECT : ParticleTypes.ITEM_SLIME,
                    getX(), getY() + 0.5, getZ(), 0, 0, 0);
            if (curse) level().addParticle(ParticleTypes.FLAME, getX(), getY() + 0.5, getZ(), 0, 0, 0);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return target.canBeHitByProjectile() && (age >= MOB_SHOOTER_GRACE_TICKS
                || target instanceof Player || !target.getUUID().equals(initialMobShooter));
    }

    @Override
    protected void onHit(HitResult hit) {
        SymbolSpell spell = spell();
        if (level() instanceof ServerLevel server && spell != null) {
            // Vanilla's collision callback runs before the projectile advances.
            impact(server, spell, hit, position());
            discard();
        }
    }

    private void impact(ServerLevel level, SymbolSpell spell, @Nullable HitResult hit, Vec3 center) {
        int effectLevel = effectLevel();
        LivingEntity direct = hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living
                ? living : null;
        WitcheryNetwork.sendSymbolProjectileImpact(level, center, spell.isCurse());
        switch (spell) {
            case AVADA_KEDAVRA, CRUCIO -> damageCurse(spell, direct);
            case ENNERVATE -> applyToTargets(level, center, direct, 2, 4, target -> {
                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                target.removeEffect(MobEffects.DIG_SLOWDOWN);
                target.removeEffect(MobEffects.CONFUSION);
            });
            case IMPERIO -> {
                if (direct instanceof Mob mob && getOwner() instanceof ServerPlayer player) InfernalInfusion.castImperio(player, mob);
            }
            case STUPEFY -> {
                if (direct != null && !direct.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                    direct.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6000, 9));
                }
            }
            case IGNIANIMA -> Soulfire.ignite(level, this, getOwner(), center);
            case MORSMORDRE -> DarkMark.summon(level, center);
            case TORMENTUM -> sendToTorment(level, center);
            case CONFUNDUS -> applyToTargets(level, center, direct, 2, 4, target -> {
                if (!target.hasEffect(MobEffects.CONFUSION)) target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 600));
            });
            case IMPEDIMENTA -> applyToTargets(level, center, direct, 3, 6, target -> {
                if (target != getOwner() && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 1));
                }
            });
            case EXPELLIARMUS -> applyToTargets(level, center, direct, 3, 5, target -> {
                if (target != getOwner()) disarm(target);
            });
            case FLIPENDO -> applyToTargets(level, center, direct, 3, 6, target -> {
                if (effectLevel == 2 || target != getOwner()) {
                    Vec3 motion = getDeltaMovement();
                    double scale = target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) ? 2.5 : 2;
                    applyMotion(target, new Vec3(motion.x * scale, 0.3, motion.z * scale));
                }
            });
            case ATTRAHO -> pullToCaster(level, center);
            case ACCIO -> retrieveItems(level, center);
            case ALOHOMORA -> SymbolTerrain.toggleDoor(level, getOwner(), hit);
            case CAVE_INIMICUM -> SymbolTerrain.reinforce(level, getOwner(), hit, effectLevel);
            case COLLOPORTUS -> SymbolTerrain.sealDoor(level, getOwner(), hit);
            case DEFODIO -> SymbolTerrain.excavate(level, getOwner(), hit, effectLevel);
            case AGUAMENTI -> SymbolTerrain.pourWater(level, getOwner(), hit, effectLevel);
            case INCENDIO -> SymbolTerrain.ignite(level, this, hit, effectLevel);
            case LUMOS -> SymbolTerrain.placeLight(level, getOwner(), hit);
            case CARNOSA_DIEM, LEONARD_1, LEONARD_2, LEONARD_3, LEONARD_4, METEOLOJINX_RECANTO, NOX,
                    EPISKEY, FLAGRATE, PROTEGO -> { }
        }
    }

    private void damageCurse(SymbolSpell spell, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || !(getOwner() instanceof LivingEntity caster)) return;
        if (target instanceof Player player && caster instanceof Player attacker && !attacker.canHarmPlayer(player)) return;
        DamageSource thrown = damageSources().thrown(this, caster);
        if (spell == SymbolSpell.CRUCIO) {
            if (target instanceof Player) target.hurt(thrown, 4 * effectLevel());
            else if (target instanceof Mob) target.hurt(thrown, 4);
        } else if (target instanceof Player player) {
            if (!player.isCreative() && !player.isSpectator()) instantDeath(player, caster);
        } else if (target instanceof Mob mob) {
            if (caster instanceof Player player && player.isCreative()) {
                instantDeath(mob, caster);
            } else {
                boolean vulnerable = mob.getMaxHealth() <= 200 && (InfernalInfusion.canBind(mob)
                        || mob instanceof Witch || mob instanceof Ent || mob instanceof AbstractGolem);
                mob.hurt(thrown, vulnerable ? 200 : 25);
            }
        }
    }

    private static void instantDeath(LivingEntity target, LivingEntity caster) {
        DamageSource kind = target instanceof Player && Vampirism.isVampire(target)
                ? WitcheryDamageTypes.sunlight(target.level()) : target.damageSources().genericKill();
        WitcheryDamageTypes.killDirectly(target, caster, kind);
    }

    private void retrieveItems(ServerLevel level, Vec3 impact) {
        var owner = getOwner();
        if (owner == null) return;
        int effectLevel = effectLevel();
        double radius = effectLevel == 1 ? 0.8 : effectLevel == 2 ? 3 : 9;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(impact, impact).inflate(radius),
                target -> target.distanceToSqr(impact) <= radius * radius)) {
            item.setPos(owner.getX(), owner.getY() + 1, owner.getZ());
        }
    }

    private void applyToTargets(ServerLevel level, Vec3 impact, @Nullable LivingEntity direct, double tierTwo,
                                double tierThree, Consumer<LivingEntity> effect) {
        int effectLevel = effectLevel();
        if (effectLevel == 1) {
            if (direct != null) effect.accept(direct);
        } else {
            double radius = effectLevel == 2 ? tierTwo : tierThree;
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(impact, impact).inflate(radius),
                    living -> living.distanceToSqr(impact) <= radius * radius)) effect.accept(target);
        }
    }

    private static void disarm(LivingEntity target) {
        if (target instanceof Reflection || target instanceof WitcheryFollower) return;
        var held = target.getMainHandItem();
        if (held.isEmpty()) return;
        if (target instanceof net.minecraft.world.entity.player.Player player) {
            if (player.containerMenu != player.inventoryMenu || player.isCreative()) return;
            if (player.drop(held, true, false) == null) return;
        } else if (target.spawnAtLocation(held, 0.5F) == null) {
            return;
        }
        target.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
    }

    private void pullToCaster(ServerLevel level, Vec3 center) {
        var owner = getOwner();
        if (owner == null) return;
        int rank = effectLevel();
        double radius = rank == 1 ? 2 : rank == 2 ? 3 : 9;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius),
                living -> living != owner && !(living instanceof EnderDragon || living instanceof HornedHuntsman)
                        && living.distanceToSqr(center) <= radius * radius)) {
            Vec3 difference = owner.position().subtract(target.position());
            if (difference.lengthSqr() < 0.0001) continue;
            target.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 1));
            applyMotion(target, new Vec3(difference.x * 0.14, difference.y * 0.2 + 0.3, difference.z * 0.14));
        }
    }

    private static void applyMotion(LivingEntity target, Vec3 motion) {
        target.setDeltaMovement(motion);
        if (target instanceof ServerPlayer player) WitcheryNetwork.sendPushTarget(player, player.getId(), motion.x, motion.y, motion.z);
        else target.hurtMarked = true;
    }

    private void sendToTorment(ServerLevel level, Vec3 location) {
        if (level.dimension().equals(WitcheryDimensions.TORMENT)) return;
        boolean affected = false;
        AABB bounds = new AABB(location, location).inflate(2);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (victim instanceof ServerPlayer player) {
                affected |= TormentTravel.requestSpellEncounter(player);
            } else if (victim instanceof Mob && !CreatureAlignment.boss(victim)) {
                victim.discard();
                affected = true;
            }
        }
        if (affected && getOwner() instanceof ServerPlayer caster) {
            SpellKnowledge.TORMENTUM.startCooldown(caster);
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1;
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (isInvulnerableTo(source) || spell() == SymbolSpell.CAVE_INIMICUM || !(source.getEntity() instanceof ServerPlayer player)
                || !(player.getMainHandItem().getItem() instanceof MysticBranchItem)) return false;
        Vec3 look = player.getLookAngle();
        setOwner(player);
        setDeltaMovement(look);
        entityData.set(ACCELERATION, look.scale(0.1).toVector3f());
        markHurt();
        return true;
    }

    private int effectLevel() {
        return entityData.get(EFFECT_LEVEL);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("EffectID", entityData.get(SPELL));
        tag.putInt("Age", age);
        if (initialMobShooter != null) tag.putUUID("InitialMobShooter", initialMobShooter);
        tag.putInt("lifetime", ticksUntilImpact);
        tag.putInt("effectLevel", effectLevel());
        tag.putBoolean("CurrentAcceleration", true);
        Vector3f acceleration = entityData.get(ACCELERATION);
        tag.putFloat("AccelerationX", acceleration.x);
        tag.putFloat("AccelerationY", acceleration.y);
        tag.putFloat("AccelerationZ", acceleration.z);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SPELL, tag.getInt("EffectID"));
        age = Math.max(0, tag.getInt("Age"));
        // Earlier port saves have only the current owner, which still identifies an undeflected mob spell.
        initialMobShooter = tag.hasUUID("InitialMobShooter") ? tag.getUUID("InitialMobShooter")
                : tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        entityData.set(EFFECT_LEVEL, Mth.clamp(tag.getInt("effectLevel"), 1, 3));
        SymbolSpell spell = spell();
        Vec3 acceleration = new Vec3(tag.getFloat("AccelerationX"), tag.getFloat("AccelerationY"), tag.getFloat("AccelerationZ"));
        // Earlier 1.20.1 saves wrote the launch acceleration and computed decay from total age.
        if (!tag.getBoolean("CurrentAcceleration") && spell != null && spell.falls() && effectLevel() == 1) {
            acceleration = acceleration.scale(Math.pow(0.8, age));
        }
        entityData.set(ACCELERATION, acceleration.toVector3f());

        ListTag direction = tag.getList("direction", Tag.TAG_DOUBLE);
        if (direction.size() == 3) {
            setDeltaMovement(direction.getDouble(0), direction.getDouble(1), direction.getDouble(2));
            setNoGravity(true);
        }
        ticksUntilImpact = -1;
        if (tag.contains("lifetime", Tag.TAG_ANY_NUMERIC)) {
            int remaining = tag.getInt("lifetime");
            if (remaining != -1) ticksUntilImpact = Math.max(1, remaining);
        } else if (direction.isEmpty() && spell == SymbolSpell.MORSMORDRE) {
            ticksUntilImpact = Math.max(1, spell.lifetime() - age);
        }
    }
}
