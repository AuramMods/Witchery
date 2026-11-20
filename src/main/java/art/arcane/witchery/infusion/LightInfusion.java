package art.arcane.witchery.infusion;

import art.arcane.witchery.block.BarrierBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public final class LightInfusion {
    public static final int ID = 1;
    private static final int CLOAK_TICKS = 30;
    private static final int BARRIER_TICKS = 200;
    private static final Map<ServerPlayer, MobEffectInstance> CLOAKS = new WeakHashMap<>();

    private LightInfusion() {}

    public static void channel(ServerPlayer player, int elapsed) {
        if (elapsed == 0 || elapsed % CLOAK_TICKS != 0) return;
        if (!InfusionEnergy.spend(player, 1)) {
            endCloak(player);
            return;
        }

        MobEffectInstance invisibility = player.getEffect(MobEffects.INVISIBILITY);
        if (invisibility == null || isCloak(player, invisibility)) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, CLOAK_TICKS, 0, true, false, false));
            CLOAKS.put(player, player.getEffect(MobEffects.INVISIBILITY));
        }

        AABB area = new AABB(player.getX() - 20, player.getY(), player.getZ() - 20,
                player.getX() + 20, player.getY() + 2, player.getZ() + 20);
        for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, area,
                target -> target.getTarget() == player && target.distanceToSqr(player) <= 400)) {
            mob.setTarget(null);
            if (mob.getLastHurtByMob() == player) mob.setLastHurtByMob(null);
        }
    }

    public static void endCloak(ServerPlayer player) {
        MobEffectInstance invisibility = player.getEffect(MobEffects.INVISIBILITY);
        if (invisibility != null && isCloak(player, invisibility)) player.removeEffect(MobEffects.INVISIBILITY);
        CLOAKS.remove(player);
    }

    private static boolean isCloak(ServerPlayer player, MobEffectInstance invisibility) {
        return invisibility == CLOAKS.get(player) && invisibility.getAmplifier() == 0
                && invisibility.getDuration() <= CLOAK_TICKS && !invisibility.isInfiniteDuration()
                && invisibility.isAmbient() && !invisibility.isVisible();
    }

    public static void release(ServerPlayer player, int elapsed) {
        if (elapsed >= 20 || !player.isShiftKeyDown()) return;
        HitResult hit = OtherwhereInfusion.rayTrace(player, 16);
        ServerLevel level = player.serverLevel();
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity target) {
            if (target.isAlive() && InfusionEnergy.spend(player, 3)) cage(level, target.blockPosition(), 1, player);
        } else if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK
                && InfusionEnergy.spend(player, 3)) {
            if (blockHit.getDirection() == Direction.UP) shield(level, blockHit.getBlockPos(), player);
            else extend(level, blockHit.getBlockPos(), blockHit.getDirection());
        }
    }

    public static void attack(ServerPlayer player, LivingEntity target) {
        if (!target.isAlive() || target.isSpectator()) return;
        ServerLevel level = player.serverLevel();
        BlockPos raised = target.blockPosition().above(4);
        if (level.isOutsideBuildHeight(raised.above(3)) || !clearForCage(level, raised)) return;
        if (!InfusionEnergy.spend(player, 5)) return;
        cage(level, raised, 2, null);
        target.teleportTo(raised.getX() + 0.5, raised.getY(), raised.getZ() + 0.5);
        target.resetFallDistance();
    }

    private static boolean clearForCage(ServerLevel level, BlockPos base) {
        for (int y = 0; y < 3; y++) {
            BlockPos middle = base.above(y);
            if (!level.isEmptyBlock(middle)) return false;
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (!level.isEmptyBlock(middle.relative(side))) return false;
            }
        }
        return true;
    }

    private static void cage(ServerLevel level, BlockPos center, int floorRadius, @Nullable Player owner) {
        circle(level, center.below(), floorRadius, true, owner);
        for (int y = 0; y < 3; y++) circle(level, center.above(y), 2, false, owner);
        circle(level, center.above(3), 2, true, owner);
    }

    private static void circle(ServerLevel level, BlockPos center, int radius, boolean filled, @Nullable Player owner) {
        for (int z = -radius; z <= radius; z++) {
            boolean outerRow = Math.abs(z) == radius;
            int extent = outerRow ? radius - 1 : radius;
            for (int x = -extent; x <= extent; x++) {
                if (filled || outerRow || Math.abs(x) == extent) place(level, center.offset(x, 0, z), owner);
            }
        }
    }

    private static void shield(ServerLevel level, BlockPos hit, ServerPlayer owner) {
        BlockPos base = level.getBlockState(hit).blocksMotion() ? hit.above() : hit;
        Vec3 across = Vec3.directionFromRotation(0, owner.getYRot()).yRot(Mth.HALF_PI);
        int dx = Mth.floor(0.5 + across.x);
        int dz = Mth.floor(0.5 + across.z);
        for (int column = -1; column <= 1; column++) {
            for (int y = 0; y < 3; y++) place(level, base.offset(column * dx, y, column * dz), owner);
        }
    }

    private static void extend(ServerLevel level, BlockPos hit, Direction direction) {
        for (int i = 1; i <= 15; i++) {
            if (!place(level, hit.relative(direction, i), null)) break;
        }
    }

    private static boolean place(ServerLevel level, BlockPos pos, @Nullable Player owner) {
        return BarrierBlock.place(level, pos, owner, true, BARRIER_TICKS);
    }
}
