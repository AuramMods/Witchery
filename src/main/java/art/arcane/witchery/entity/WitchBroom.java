package art.arcane.witchery.entity;

import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.infusion.InfusedBrews;
import art.arcane.witchery.item.BroomItem;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class WitchBroom extends Entity {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(WitchBroom.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HIT_TICKS = SynchedEntityData.defineId(WitchBroom.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HIT_DIRECTION = SynchedEntityData.defineId(WitchBroom.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(WitchBroom.class, EntityDataSerializers.FLOAT);
    private static final double INITIAL_ACCELERATION = 0.07;
    private static final double MAX_ACCELERATION = 0.35;
    private static final double DRAG = 0.99F;
    private double acceleration = INITIAL_ACCELERATION;
    private boolean owlRider;
    private boolean soaringRider;
    private int interpolationTicks;
    private Vec3 interpolationPos;
    private float interpolationYaw;
    private float interpolationPitch;
    private Vec3 receivedMotion = Vec3.ZERO;

    public WitchBroom(EntityType<? extends WitchBroom> type, Level level) {
        super(type, level);
        blocksBuilding = true;
        // The modern renderer uses a heading 90 degrees above the legacy entity yaw.
        setYRot(90);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(COLOR, -1);
        entityData.define(HIT_TICKS, 0);
        entityData.define(HIT_DIRECTION, 1);
        entityData.define(DAMAGE, 0F);
    }

    public void setBrushColor(int color) {
        entityData.set(COLOR, color >= 0 && color < 16 ? color : -1);
    }

    public int brushColor() {
        return entityData.get(COLOR);
    }

    public float wobble(float partialTick) {
        float ticks = entityData.get(HIT_TICKS) - partialTick;
        float damage = Math.max(0, entityData.get(DAMAGE) - partialTick);
        return ticks > 0 ? Mth.sin(ticks) * ticks * damage / 10 * entityData.get(HIT_DIRECTION) : 0;
    }

    @Override
    public boolean isPickable() {
        return isAlive() && !isVehicle();
    }

    @Override
    public boolean canBeCollidedWith() {
        return isAlive();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public double getPassengersRidingOffset() {
        return getBbHeight() * 0.55;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (passenger instanceof ServerPlayer rider) {
            owlRider = FamiliarBinding.hasBroomMastery(rider);
            soaringRider = InfusedBrews.hasSoaring(rider);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (isVehicle()) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof DyeItem dye) {
            if (!level().isClientSide) {
                setBrushColor(dye.getDyeColor().getId());
                if (!player.getAbilities().instabuild) stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        return level().isClientSide ? InteractionResult.SUCCESS
                : player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || isRemoved()) return false;
        if (!level().isClientSide && amount > 0) {
            entityData.set(HIT_DIRECTION, -entityData.get(HIT_DIRECTION));
            entityData.set(HIT_TICKS, 10);
            float damage = entityData.get(DAMAGE) + amount * 10;
            entityData.set(DAMAGE, damage);
            boolean creative = source.getEntity() instanceof Player player && player.getAbilities().instabuild;
            if (creative || damage > 40) {
                ejectPassengers();
                if (!creative && level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    spawnAtLocation(getPickResult());
                }
                discard();
            }
        }
        return true;
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack stack = new ItemStack(WitcheryIngredient.ENCHANTED_BROOM.item());
        if (hasCustomName()) stack.setHoverName(getCustomName());
        if (brushColor() >= 0) stack.getOrCreateTag().putByte(BroomItem.BRUSH_COLOR, (byte) brushColor());
        return stack;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int ticks, boolean teleport) {
        if (teleport) {
            interpolationTicks = 0;
            interpolationPos = null;
            super.lerpTo(x, y, z, yaw, pitch, ticks, true);
            return;
        }
        interpolationPos = new Vec3(x, y, z);
        interpolationYaw = yaw;
        interpolationPitch = pitch;
        interpolationTicks = Math.max(1, ticks) + 5;
        setDeltaMovement(receivedMotion);
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        receivedMotion = new Vec3(x, y, z);
        setDeltaMovement(receivedMotion);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (interpolationTicks > 0) {
                double step = 1D / interpolationTicks--;
                setPos(position().lerp(interpolationPos, step));
                setYRot(Mth.rotLerp((float) step, getYRot(), interpolationYaw));
                setXRot(Mth.lerp((float) step, getXRot(), interpolationPitch));
            } else if (interpolationPos != null) {
                var motion = getDeltaMovement();
                setPos(position().add(motion));
                setYRot(interpolationYaw);
                setXRot(interpolationPitch);
                setDeltaMovement(motion.multiply(DRAG, 1, DRAG));
            }
            return;
        }

        int hitTicks = entityData.get(HIT_TICKS);
        if (hitTicks > 0) entityData.set(HIT_TICKS, hitTicks - 1);
        float damage = entityData.get(DAMAGE);
        if (damage > 0) entityData.set(DAMAGE, Math.max(0, damage - 1));
        var passenger = getFirstPassenger();
        if (tickCount % 100 == 0 && passenger instanceof ServerPlayer rider) soaringRider = InfusedBrews.hasSoaring(rider);
        fly(passenger);
        pushNearbyBrooms(passenger);
    }

    private void fly(Entity passenger) {
        Vec3 velocity = getDeltaMovement();
        double previousSpeed = velocity.horizontalDistance();
        if (passenger instanceof LivingEntity rider) {
            if (rider.zza > 0) {
                double yaw = rider.getYRot() * Mth.PI / 180F;
                double thrust = acceleration * (0.1 + (soaringRider ? 0.1 : 0) + (owlRider ? 0.2 : 0));
                double climb = -Math.sin(rider.getXRot() * Mth.PI / 180F);
                if (climb > -0.5 && climb < 0.2) climb = 0;
                else if (climb < 0) climb *= 0.5;
                velocity = new Vec3(velocity.x - Math.sin(yaw) * thrust, climb * acceleration * 2,
                        velocity.z + Math.cos(yaw) * thrust);
            } else if (rider.zza == 0 && (owlRider || soaringRider)) {
                velocity = velocity.multiply(0.9, 1, 0.9);
            }
        } else if (passenger == null) {
            owlRider = false;
            velocity = new Vec3(settle(velocity.x), onGround() ? velocity.y : -0.2, settle(velocity.z));
        }
        double speedLimit = 0.9 + (owlRider ? 0.3 : 0) + (soaringRider ? 0.3 : 0);
        double speed = velocity.horizontalDistance();
        if (speed > speedLimit) {
            velocity = velocity.scale(speedLimit / speed);
            speed = speedLimit;
        }
        if (speed > previousSpeed && acceleration < MAX_ACCELERATION) {
            acceleration = Math.min(MAX_ACCELERATION, acceleration + (MAX_ACCELERATION - acceleration) / (MAX_ACCELERATION * 100));
        } else {
            acceleration = Math.max(INITIAL_ACCELERATION, acceleration - (acceleration - INITIAL_ACCELERATION) / (MAX_ACCELERATION * 100));
        }
        Vec3 origin = position();
        setDeltaMovement(velocity);
        move(MoverType.SELF, velocity);
        Vec3 traveled = position().subtract(origin);
        if (traveled.horizontalDistanceSqr() > 0.001) {
            float heading = (float) (Math.atan2(-traveled.z, -traveled.x) * 180D / Math.PI) + 90;
            setYRot(getYRot() + Mth.wrapDegrees(heading - getYRot()));
        }
        setXRot(0);
        setDeltaMovement(getDeltaMovement().scale(DRAG));
    }

    private void pushNearbyBrooms(Entity passenger) {
        if (noPhysics) return;
        var bounds = getBoundingBox().inflate(0.2F, 0, 0.2F);
        for (var other : level().getEntitiesOfClass(WitchBroom.class, bounds,
                broom -> broom != this && broom != passenger && broom.isAlive() && !broom.noPhysics
                        && !isPassengerOfSameVehicle(broom))) {
            double x = getX() - other.getX();
            double z = getZ() - other.getZ();
            double separation = Mth.absMax(x, z);
            if (separation < 0.01F) continue;
            double distance = (float) Math.sqrt(separation);
            double weight = Math.min(1 / distance, 1);
            double pushX = x / distance * weight * 0.05F;
            double pushZ = z / distance * weight * 0.05F;
            // In 0.24.1 a collision also pushed occupied brooms.
            other.push(-pushX, 0, -pushZ);
            push(pushX, 0, pushZ);
        }
    }

    private static double settle(double speed) {
        return Math.abs(speed * 0.9) < 0.01 ? 0 : speed * 0.9;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (brushColor() >= 0) tag.putByte(BroomItem.BRUSH_COLOR, (byte) brushColor());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(BroomItem.BRUSH_COLOR, Tag.TAG_ANY_NUMERIC)) setBrushColor(tag.getByte(BroomItem.BRUSH_COLOR));
        if (!hasCustomName() && !tag.getString("CustomName").isEmpty()) {
            setCustomName(Component.literal(tag.getString("CustomName")));
        }
    }
}
