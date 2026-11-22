package art.arcane.witchery.entity;

import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

final class MonkeyFlightGoal extends Goal {
    private static final TicketType<UUID> FLIGHT_TICKET = TicketType.create("witchery_monkey_flight", UUID::compareTo, 40);
    private final WingedMonkey monkey;
    private ItemStack waypoint = ItemStack.EMPTY;
    private Vec3 home = Vec3.ZERO;
    private boolean returning;

    MonkeyFlightGoal(WingedMonkey monkey) {
        this.monkey = monkey;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    boolean active() {
        return !waypoint.isEmpty();
    }

    boolean assign(Player owner, ItemStack stone, boolean capture) {
        if (!(monkey.level() instanceof ServerLevel level) || active() || monkey.isVehicle()
                || destination(level, stone) == null || !capture && !owner.startRiding(monkey)) return false;
        waypoint = stone.copyWithCount(1);
        home = monkey.position();
        returning = false;
        if (!capture) {
            monkey.setOrderedToSit(false);
            monkey.setInSittingPose(false);
        }
        monkey.setTarget(null);
        monkey.getNavigation().stop();
        monkey.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7F, 1.1F);
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, monkey.getX(), monkey.getY(0.5), monkey.getZ(),
                16, 0.25, 0.25, 0.25, 0.05);
        return true;
    }

    @Override
    public boolean canUse() {
        return active() && monkey.isTame() && !monkey.isTemporary() && !monkey.isOrderedToSit();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        holdChunk((ServerLevel) monkey.level());
    }

    @Override
    public void stop() {
        monkey.getNavigation().stop();
    }

    @Override
    public void tick() {
        var level = (ServerLevel) monkey.level();
        if (monkey.tickCount % 20 == 0) holdChunk(level);
        Vec3 target = returning ? home : destination(level, waypoint);
        if (target == null) {
            returning = true;
            target = home;
        }
        if (monkey.distanceToSqr(target) <= 1) {
            if (returning || WaystoneItem.isBoundWaystone(waypoint)) {
                finish();
            } else {
                LivingEntity passenger = recipient(level, waypoint);
                if (passenger != null && !passenger.isPassenger() && !passenger.isVehicle()) passenger.startRiding(monkey, true);
                returning = true;
                monkey.getNavigation().stop();
            }
            return;
        }
        FlyingCompanionNavigation.flyToward(monkey, level, target, 1.15);
    }

    void finish() {
        waypoint = ItemStack.EMPTY;
        returning = false;
        monkey.getNavigation().stop();
        monkey.ejectPassengers();
        monkey.setOrderedToSit(true);
    }

    private void holdChunk(ServerLevel level) {
        level.getChunkSource().addRegionTicket(FLIGHT_TICKET, monkey.chunkPosition(), 3, monkey.getUUID());
    }

    @Nullable
    private Vec3 destination(ServerLevel level, ItemStack stone) {
        Vec3 target;
        WaystoneItem.Location location = WaystoneItem.isBoundWaystone(stone) ? WaystoneItem.location(stone) : null;
        if (location != null) {
            if (!level.dimension().equals(location.dimension())) return null;
            target = Vec3.atBottomCenterOf(location.position()).add(0, 1.5, 0);
        } else {
            LivingEntity entity = recipient(level, stone);
            if (entity == null) return null;
            target = entity.position().add(0, 1, 0);
        }
        BlockPos pos = BlockPos.containing(target);
        return level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos) ? null : target;
    }

    @Nullable
    private LivingEntity recipient(ServerLevel level, ItemStack stone) {
        LivingEntity entity = TaglockItem.resolveTarget(level, stone);
        return entity != null && entity != monkey && entity.isAlive() && !entity.isSpectator() && entity.level() == level ? entity : null;
    }

    void save(CompoundTag tag) {
        if (!active()) return;
        tag.put("WITCWaypoint", waypoint.save(new CompoundTag()));
        tag.putDouble("HomeX", home.x);
        tag.putDouble("HomeY", home.y);
        tag.putDouble("HomeZ", home.z);
        tag.putBoolean("MonkeyFlightReturning", returning);
    }

    void load(CompoundTag tag) {
        waypoint = tag.contains("WITCWaypoint", Tag.TAG_COMPOUND)
                ? WitcheryIngredient.loadInventoryStack(tag.getCompound("WITCWaypoint")) : ItemStack.EMPTY;
        home = new Vec3(tag.getDouble("HomeX"), tag.getDouble("HomeY"), tag.getDouble("HomeZ"));
        if (!(WaystoneItem.isBoundWaystone(waypoint) || waypoint.getItem() instanceof TaglockItem)
                || !Double.isFinite(home.x) || !Double.isFinite(home.y) || !Double.isFinite(home.z)) waypoint = ItemStack.EMPTY;
        // The old passenger flight saved its destination in Home and the departure point in the copied Waystone.
        returning = tag.contains("MonkeyFlightReturning") ? tag.getBoolean("MonkeyFlightReturning") : WaystoneItem.isBoundWaystone(waypoint);
    }
}
