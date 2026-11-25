package art.arcane.witchery.entity;

import art.arcane.witchery.block.SunlightBlock;
import art.arcane.witchery.item.GrenadeItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class GrenadeProjectile extends ThrowableItemProjectile {
    private static final int FUSE_TICKS = 200;
    private static final int MAX_SAVED_FUSE_TICKS = 1200;
    private static final EntityDataAccessor<Boolean> IMPACTED = SynchedEntityData.defineId(GrenadeProjectile.class,
            EntityDataSerializers.BOOLEAN);
    private static final DustParticleOptions SUNLIGHT = new DustParticleOptions(new Vector3f(1F, 0.92F, 0.14F), 0.8F);
    private int remainingTicks = FUSE_TICKS;
    @Nullable private BlockPos litPosition;

    public GrenadeProjectile(EntityType<? extends GrenadeProjectile> type, Level level) {
        super(type, level);
    }

    public GrenadeProjectile(Level level, LivingEntity thrower) {
        super(WitcheryEntities.GRENADE.get(), thrower, level);
        BrewProjectileLaunch.fromPlayer(this, thrower, -20, 0.75F);
    }

    @Override
    public void shoot(double x, double y, double z, float speed, float inaccuracy) {
        BrewProjectileLaunch.shoot(this, random, new Vec3(x, y, z), speed, inaccuracy);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IMPACTED, false);
    }

    @Override
    protected Item getDefaultItem() {
        return WitcheryItems.getRequired("sungrenade").get();
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    public boolean hasImpacted() {
        return entityData.get(IMPACTED);
    }

    @Override
    public void tick() {
        if (hasImpacted()) baseTick();
        else super.tick();
        if (isRemoved()) return;

        boolean duplicate = GrenadeItem.isDuplicate(getItem());
        if (level() instanceof ServerLevel server) {
            if (--remainingTicks <= 0) finish(server);
            else if (!duplicate) {
                if (litPosition != null || tickCount >= 5) moveLight(server, lightPosition());
                if (hasImpacted()) for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class,
                        getBoundingBox().inflate(1), target -> target.isAlive() && target.isPickable() && isUndead(target))) {
                    entity.setSecondsOnFire(3);
                }
            }
        } else if (!duplicate && hasImpacted() && random.nextInt(4) == 0) {
            level().addParticle(SUNLIGHT, getRandomX(0.4), getY() + 0.05, getRandomZ(0.4), 0, 0.04, 0);
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        if (hasImpacted()) return;
        super.onHit(hit);
        if (level() instanceof ServerLevel server) {
            Vec3 point = hit.getLocation();
            if (GrenadeItem.isDuplicate(getItem())) {
                if (hit instanceof BlockHitResult block) {
                    double clearance = switch (block.getDirection()) {
                        case UP -> 0.02;
                        case DOWN -> 1.85;
                        default -> 0.32;
                    };
                    point = point.add(Vec3.atLowerCornerOf(block.getDirection().getNormal()).scale(clearance));
                }
                setPos(point);
                finish(server);
                return;
            }
            if (hit instanceof BlockHitResult block) point = point.add(Vec3.atLowerCornerOf(block.getDirection().getNormal()).scale(0.02));
            setPos(point);
            setDeltaMovement(Vec3.ZERO);
            setNoGravity(true);
            entityData.set(IMPACTED, true);
            hasImpulse = true;
        }
    }

    public BlockPos lightPosition() {
        BlockPos pos = blockPosition();
        var state = level().getBlockState(pos);
        return state.isAir() || state.is(WitcheryBlocks.getRequired("light").get()) ? pos : pos.above();
    }

    public boolean isLighting(BlockPos pos) {
        return !isRemoved() && !GrenadeItem.isDuplicate(getItem()) && pos.equals(litPosition);
    }

    private void moveLight(ServerLevel level, @Nullable BlockPos destination) {
        var light = (SunlightBlock) WitcheryBlocks.getRequired("light").get();
        BlockPos previous = litPosition;
        litPosition = destination;
        if (previous != null && !previous.equals(destination)) light.clearUnusedLight(level, previous);
        if (destination != null) light.illuminate(level, destination);
    }

    private void finish(ServerLevel level) {
        returnQuartzSphere(level);
        if (GrenadeItem.isDuplicate(getItem())) {
            WitcheryFollower.spawnDuplicate(level, position(), GrenadeItem.ownerName(getItem()));
        } else {
            moveLight(level, null);
            burstSunlight(level);
        }
        discard();
    }

    private void returnQuartzSphere(ServerLevel level) {
        var sphere = new ItemEntity(level, getX(), getY() + 0.5, getZ(), new ItemStack(WitcheryIngredient.QUARTZ_SPHERE.item()));
        var bounds = sphere.getBoundingBox();
        if (level.getBlockCollisions(sphere, bounds).iterator().hasNext()) {
            // A landed grenade can hug a wall or ceiling closely enough to bury the returned item.
            level.findFreePosition(sphere, Shapes.create(bounds.inflate(1)), position(),
                            bounds.getXsize() + 0.02, bounds.getYsize() + 0.02, bounds.getZsize() + 0.02)
                    .ifPresent(center -> sphere.setPos(center.x, center.y - bounds.getYsize() / 2, center.z));
        }
        sphere.setDefaultPickUpDelay();
        var captured = captureDrops();
        if (captured != null) captured.add(sphere);
        else level.addFreshEntity(sphere);
    }

    private void burstSunlight(ServerLevel level) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3, 2, 3),
                target -> target.isAlive() && target.isPickable())) {
            if (isUndead(entity)) {
                entity.setSecondsOnFire(5);
                if (entity instanceof ServerPlayer player) Vampirism.onSunBurst(player);
            }
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * (10 + random.nextInt(3)), 0, true, true));
        }
        level.sendParticles(SUNLIGHT, getX(), getY() + 0.1, getZ(), 20, 0.32, 0.18, 0.32, 0.1);
    }

    private static boolean isUndead(LivingEntity entity) {
        return entity instanceof ServerPlayer ? CreatureAlignment.undead(entity) : entity.isInvertedHealAndHarm();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Impacted", hasImpacted());
        tag.putInt("Remaining", remainingTicks);
        tag.putInt("Mode", GrenadeItem.isDuplicate(getItem()) ? 1 : 0);
        if (litPosition != null) {
            tag.putBoolean("BlockPlaced", true);
            tag.putInt("BlockPlacedX", litPosition.getX());
            tag.putInt("BlockPlacedY", litPosition.getY());
            tag.putInt("BlockPlacedZ", litPosition.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getInt("Mode") == 1 && !GrenadeItem.isDuplicate(getItem())) {
            // An original duplicate stored the skin name under Owner. Vanilla now uses Owner for a UUID.
            setItem(GrenadeItem.duplicate(tag.getString("Owner")));
        }
        litPosition = tag.getBoolean("BlockPlaced") && !GrenadeItem.isDuplicate(getItem())
                ? new BlockPos(tag.getInt("BlockPlacedX"), tag.getInt("BlockPlacedY"), tag.getInt("BlockPlacedZ")) : null;
        boolean impacted = tag.getBoolean("Impacted");
        entityData.set(IMPACTED, impacted);
        // Earlier 1.20.1 builds wrote an extended fuse for landed grenades.
        int limit = impacted ? MAX_SAVED_FUSE_TICKS : FUSE_TICKS;
        remainingTicks = tag.contains("Remaining", Tag.TAG_ANY_NUMERIC)
                ? Math.max(1, Math.min(limit, tag.getInt("Remaining"))) : FUSE_TICKS;
        if (impacted) {
            setNoGravity(true);
            setDeltaMovement(Vec3.ZERO);
        }
    }
}
