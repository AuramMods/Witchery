package art.arcane.witchery.world;

import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public final class WaystoneTravel {
    private static final int[] LANDING_HEIGHTS = {0, 1, -1, 2, -2};

    private WaystoneTravel() {}

    @Nullable
    public static LivingEntity findBindingTarget(ServerLevel level, BlockPos circle, boolean droppedStone) {
        Vec3 center = Vec3.atBottomCenterOf(circle);
        int radius = droppedStone ? 2 : 4;
        AABB area = new AABB(circle).inflate(radius, droppedStone ? 2 : 0, radius);
        Comparator<LivingEntity> preference = Comparator.<LivingEntity>comparingInt(entity ->
                (entity instanceof ServerPlayer) == droppedStone ? 0 : 1)
                .thenComparingDouble(entity -> distanceSqr(entity, center, droppedStone));
        return level.getEntitiesOfClass(LivingEntity.class, area,
                candidate -> (candidate instanceof ServerPlayer || candidate instanceof Mob)
                        && candidate.isAlive() && !candidate.isSpectator()
                        && distanceSqr(candidate, center, droppedStone) <= radius * radius)
                .stream().min(preference).orElse(null);
    }

    public static boolean teleportCircle(ServerLevel origin, BlockPos circle, ItemStack stone, int radius) {
        return teleport(origin, circle, stone, radius, radius, false);
    }

    public static boolean teleportDropped(ServerLevel origin, BlockPos circle, ItemStack stone) {
        return teleport(origin, circle, stone, 4, 0, true);
    }

    private static boolean teleport(ServerLevel origin, BlockPos circle, ItemStack stone, int radius, int spread,
                                    boolean droppedStone) {
        WaystoneItem.Location location = WaystoneItem.location(stone);
        LivingEntity target = null;
        ServerLevel destination;
        if (location != null) {
            destination = origin.getServer().getLevel(location.dimension());
        } else {
            target = TaglockItem.resolveTarget(origin, stone);
            if (target == null || !target.isAlive() || !(target.level() instanceof ServerLevel world)) return false;
            destination = world;
        }
        if (destination == null || destination != origin && (protectedWorld(origin.dimension()) || protectedWorld(destination.dimension()))) {
            return false;
        }

        Vec3 center = Vec3.atBottomCenterOf(circle);
        boolean sent = false;
        for (Entity actor : origin.getEntities((Entity) null, new AABB(center, center).inflate(radius),
                candidate -> inRange(candidate, center, radius, droppedStone))) {
            if (!actor.isAlive() || actor.level() != origin || inhibited(actor)) continue;
            Vec3 arrival;
            if (location != null) {
                arrival = findLanding(actor, destination, location.position(), spread);
            } else {
                arrival = target.position();
                if (!WitcheryTeleport.canArrive(actor, destination, arrival)) continue;
            }
            if (arrival != null) sent |= WitcheryTeleport.move(actor, destination, arrival);
        }
        return sent;
    }

    private static boolean inRange(Entity entity, Vec3 center, int radius, boolean droppedStone) {
        if (!entity.isAlive() || entity.isSpectator()) return false;
        if (!droppedStone) return entity.distanceToSqr(center) < radius * radius;
        return (entity instanceof LivingEntity || entity instanceof ItemEntity)
                && distanceSqr(entity, center, true) <= radius * radius;
    }

    private static double distanceSqr(Entity entity, Vec3 center, boolean horizontal) {
        return entity.distanceToSqr(center.x, horizontal ? entity.getY() : center.y, center.z);
    }

    @Nullable
    static Vec3 findLanding(Entity actor, ServerLevel level, BlockPos center, int radius) {
        BlockPos preferred = center.offset(level.random.nextIntBetweenInclusive(-radius, radius), 0,
                level.random.nextIntBetweenInclusive(-radius, radius));
        Vec3 arrival = landingInColumn(actor, level, preferred);
        if (arrival != null) return arrival;
        for (BlockPos feet : BlockPos.spiralAround(center, radius, Direction.EAST, Direction.SOUTH)) {
            arrival = landingInColumn(actor, level, feet);
            if (arrival != null) return arrival;
        }
        return null;
    }

    @Nullable
    private static Vec3 landingInColumn(Entity actor, ServerLevel level, BlockPos column) {
        if (!level.getWorldBorder().isWithinBounds(column) || level.isOutsideBuildHeight(column)) return null;
        for (int offset : LANDING_HEIGHTS) {
            BlockPos feet = column.above(offset);
            BlockPos floor = feet.below();
            if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) continue;
            Vec3 arrival = Vec3.atBottomCenterOf(feet);
            if (WitcheryTeleport.canArrive(actor, level, arrival)) return arrival;
        }
        return null;
    }

    static boolean protectedWorld(ResourceKey<Level> dimension) {
        return dimension.equals(WitcheryDimensions.DREAM) || dimension.equals(WitcheryDimensions.MIRROR);
    }

    private static boolean inhibited(Entity actor) {
        if (!(actor instanceof LivingEntity living)) return false;
        MobEffectInstance effect = living.getEffect(WitcheryEffects.all().get("potion.enderinhibition").get());
        return effect != null && effect.getAmplifier() >= 1;
    }
}
