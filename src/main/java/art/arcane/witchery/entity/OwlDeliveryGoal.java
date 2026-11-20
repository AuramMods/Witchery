package art.arcane.witchery.entity;

import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

final class OwlDeliveryGoal extends Goal {
    private static final TicketType<UUID> FLIGHT_TICKET = TicketType.create("witchery_owl_delivery", UUID::compareTo, 40);
    private static final double SPEED = 1.15;
    private final WitchOwl owl;
    private ItemStack waypoint = ItemStack.EMPTY;
    private Vec3 home = Vec3.ZERO;
    private boolean returning;

    OwlDeliveryGoal(WitchOwl owl) {
        this.owl = owl;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    boolean assign(ItemStack stone) {
        if (destination((ServerLevel) owl.level(), stone) == null) return false;
        waypoint = stone.copyWithCount(1);
        home = owl.position();
        returning = false;
        owl.setDelivering(true);
        owl.getNavigation().stop();
        owl.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7F, 1.1F);
        ServerLevel level = (ServerLevel) owl.level();
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, owl.getX(), owl.getY(0.5), owl.getZ(),
                16, 0.25, 0.25, 0.25, 0.05);
        return true;
    }

    @Override
    public boolean canUse() {
        return !waypoint.isEmpty() && owl.isTame() && !owl.isTemporary() && !owl.isOrderedToSit();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        holdChunk((ServerLevel) owl.level(), owl.chunkPosition());
    }

    @Override
    public void stop() {
        owl.getNavigation().stop();
    }

    @Override
    public void tick() {
        ServerLevel level = (ServerLevel) owl.level();
        if (owl.tickCount % 20 == 0) holdChunk(level, owl.chunkPosition());
        Vec3 target = returning || owl.getMainHandItem().isEmpty() ? null : destination(level, waypoint);
        if (target == null) {
            returning = true;
            target = home;
        }
        double distance = owl.distanceToSqr(target);
        if (distance <= 1) {
            owl.getNavigation().stop();
            if (returning) {
                waypoint = ItemStack.EMPTY;
                returning = false;
                owl.setDelivering(false);
                owl.setOrderedToSit(true);
            } else if (deliver(level)) {
                returning = true;
            }
            return;
        }
        if (distance >= 128 * 128 && owl.tickCount % 10 == 0) {
            Vec3 arrival = target.add(0, 2, 0);
            BlockPos pos = BlockPos.containing(arrival);
            if (level.getWorldBorder().isWithinBounds(pos) && !level.isOutsideBuildHeight(pos)) {
                holdChunk(level, new ChunkPos(pos));
                level.getChunkAt(pos);
                if (WitcheryTeleport.canArrive(owl, level, arrival) && WitcheryTeleport.move(owl, level, arrival)) {
                    owl.getNavigation().stop();
                    return;
                }
            }
        }
        flyToward(level, target);
    }

    private void flyToward(ServerLevel level, Vec3 target) {
        if (owl.tickCount % 10 == 0) owl.getNavigation().moveTo(target.x, target.y, target.z, SPEED);
        if (!owl.getNavigation().isDone()) return;
        Vec3 direction = target.subtract(owl.position());
        Vec3 ahead = owl.position().add(direction.normalize().scale(Math.min(direction.length(), 2)));
        var hit = level.clip(new ClipContext(owl.getEyePosition(), ahead.add(0, owl.getEyeHeight(), 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owl));
        if (hit.getType() != HitResult.Type.MISS) {
            owl.getMoveControl().setWantedPosition(owl.getX(), owl.getY() + 2, owl.getZ(), SPEED);
        } else {
            owl.getMoveControl().setWantedPosition(target.x, target.y, target.z, SPEED);
        }
    }

    private void holdChunk(ServerLevel level, ChunkPos chunk) {
        level.getChunkSource().addRegionTicket(FLIGHT_TICKET, chunk, 3, owl.getUUID());
    }

    @Nullable
    private static Vec3 destination(ServerLevel level, ItemStack stone) {
        var binding = WaystoneItem.location(stone);
        if (binding != null) {
            if (!level.dimension().equals(binding.dimension())) return null;
            return Vec3.atBottomCenterOf(binding.position()).add(0, 1.5, 0);
        }
        LivingEntity target = TaglockItem.resolveTarget(level, stone);
        return target != null && target.isAlive() && target.level() == level ? target.position().add(0, 1, 0) : null;
    }

    private boolean deliver(ServerLevel level) {
        ItemStack held = owl.getMainHandItem();
        if (held.isEmpty()) return true;
        if (held.getItem() instanceof ThrowableBrewItem) {
            var projectile = new ThrownBrew(owl, level);
            projectile.setItem(held);
            projectile.setDeltaMovement(0, -0.1, 0);
            if (!level.addFreshEntity(projectile)) return false;
            held.shrink(1);
        } else if (held.is(Items.SPLASH_POTION) || held.is(Items.LINGERING_POTION)) {
            var projectile = new ThrownPotion(level, owl);
            projectile.setItem(held);
            projectile.setDeltaMovement(0, -0.1, 0);
            if (!level.addFreshEntity(projectile)) return false;
            held.shrink(1);
        } else {
            ItemEntity parcel = owl.spawnAtLocation(held.copy());
            if (parcel == null) return false;
            if (held.is(WitcheryItems.getRequired("seedsmandrake").get())) parcel.lifespan = 60;
            held = ItemStack.EMPTY;
        }
        owl.setItemSlot(EquipmentSlot.MAINHAND, held.isEmpty() ? ItemStack.EMPTY : held);
        return true;
    }

    void save(CompoundTag tag) {
        if (waypoint.isEmpty()) return;
        tag.put("WITCWaypoint", waypoint.save(new CompoundTag()));
        tag.putDouble("HomeX", home.x);
        tag.putDouble("HomeY", home.y);
        tag.putDouble("HomeZ", home.z);
        tag.putBoolean("ReturningFromDelivery", returning);
    }

    void load(CompoundTag tag) {
        waypoint = tag.contains("WITCWaypoint", Tag.TAG_COMPOUND)
                ? WitcheryIngredient.loadInventoryStack(tag.getCompound("WITCWaypoint")) : ItemStack.EMPTY;
        home = new Vec3(tag.getDouble("HomeX"), tag.getDouble("HomeY"), tag.getDouble("HomeZ"));
        if (!WaystoneItem.isBoundWaystone(waypoint) || !Double.isFinite(home.x) || !Double.isFinite(home.y)
                || !Double.isFinite(home.z)) waypoint = ItemStack.EMPTY;
        returning = tag.getBoolean("ReturningFromDelivery");
        owl.setDelivering(!waypoint.isEmpty());
    }
}
