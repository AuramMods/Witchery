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
    private double acceleration = INITIAL_ACCELERATION;
    private int interpolationTicks;
    private Vec3 interpolationPos;
    private float interpolationYaw;

    public WitchBroom(EntityType<? extends WitchBroom> type, Level level) {
        super(type, level);
        blocksBuilding = true;
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
            super.lerpTo(x, y, z, yaw, pitch, ticks, true);
            return;
        }
        interpolationPos = new Vec3(x, y, z);
        interpolationYaw = yaw;
        interpolationTicks = Math.max(1, ticks);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (interpolationTicks > 0) {
                double step = 1D / interpolationTicks--;
                setPos(position().lerp(interpolationPos, step));
                setYRot(Mth.rotLerp((float) step, getYRot(), interpolationYaw));
            }
            return;
        }

        int hitTicks = entityData.get(HIT_TICKS);
        if (hitTicks > 0) entityData.set(HIT_TICKS, hitTicks - 1);
        float damage = entityData.get(DAMAGE);
        if (damage > 0) entityData.set(DAMAGE, Math.max(0, damage - 1));
        fly();
    }

    private void fly() {
        Vec3 velocity = getDeltaMovement();
        double previousSpeed = velocity.horizontalDistance();
        double speedLimit = 0.9;
        if (getFirstPassenger() instanceof ServerPlayer rider) {
            boolean owl = FamiliarBinding.hasBroomMastery(rider);
            boolean soaring = InfusedBrews.hasSoaring(rider);
            speedLimit += (owl ? 0.3 : 0) + (soaring ? 0.3 : 0);
            if (rider.zza > 0) {
                double yaw = rider.getYRot() * Mth.DEG_TO_RAD;
                double thrust = acceleration * (0.1 + (owl ? 0.2 : 0) + (soaring ? 0.1 : 0));
                double climb = -Math.sin(rider.getXRot() * Mth.DEG_TO_RAD);
                if (climb > -0.5 && climb < 0.2) climb = 0;
                else if (climb < 0) climb *= 0.5;
                velocity = new Vec3(velocity.x - Math.sin(yaw) * thrust, climb * acceleration * 2,
                        velocity.z + Math.cos(yaw) * thrust);
            } else if (rider.zza == 0 && (owl || soaring)) {
                velocity = velocity.multiply(0.9, 1, 0.9);
            }
        } else {
            velocity = new Vec3(settle(velocity.x), onGround() ? 0 : -0.2, settle(velocity.z));
        }
        double speed = velocity.horizontalDistance();
        if (speed > speedLimit) {
            velocity = velocity.scale(speedLimit / speed);
            speed = speedLimit;
        }
        double targetAcceleration = speed > previousSpeed ? MAX_ACCELERATION : INITIAL_ACCELERATION;
        acceleration += (targetAcceleration - acceleration) / (MAX_ACCELERATION * 100);
        Vec3 origin = position();
        move(MoverType.SELF, velocity);
        Vec3 traveled = position().subtract(origin);
        if (traveled.horizontalDistanceSqr() > 0.001) {
            setYRot((float) (Math.atan2(-traveled.x, traveled.z) * Mth.RAD_TO_DEG));
        }
        setDeltaMovement(velocity.multiply(
                traveled.x == 0 ? 0 : 0.99, traveled.y == 0 ? 0 : 0.99, traveled.z == 0 ? 0 : 0.99));
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
