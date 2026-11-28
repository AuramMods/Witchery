package art.arcane.witchery.world;

import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class WitcheryTeleport {
    private static final TicketType<Integer> ARRIVAL = TicketType.create("witchery_travel_arrival", Integer::compareTo, 40);

    private WitcheryTeleport() {}

    public static BlockPos findEncounterGround(ServerLevel level, BlockPos start) {
        var ground = start.mutable();
        while (ground.getY() < start.getY() + 8 && !level.isEmptyBlock(ground)) ground.move(Direction.UP);
        while (ground.getY() > level.getMinBuildHeight() && level.isEmptyBlock(ground)) ground.move(Direction.DOWN);
        return ground.immutable();
    }

    public static boolean canArrive(Entity actor, ServerLevel level, Vec3 position) {
        return isInWorld(level, position)
                && !level.getBlockCollisions(actor, actor.getBoundingBox().move(position.subtract(actor.position())))
                .iterator().hasNext();
    }

    private static boolean isInWorld(ServerLevel level, Vec3 position) {
        if (!Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z)) return false;
        BlockPos pos = BlockPos.containing(position);
        return !level.isOutsideBuildHeight(pos) && level.getWorldBorder().isWithinBounds(pos);
    }

    public static boolean randomSurface(Entity actor, ServerLevel level, BlockPos origin, int radius) {
        Vec3 arrival = findRandomSurface(actor, level, origin, radius);
        return arrival != null && move(actor, level, arrival);
    }

    @Nullable
    public static Vec3 findRandomSurface(Entity actor, ServerLevel level, BlockPos origin, int radius) {
        RandomSource random = level.getRandom();
        BlockPos ground = origin.offset(random.nextInt(radius * 2) - radius, 0, random.nextInt(radius * 2) - radius);
        if (!level.getWorldBorder().isWithinBounds(ground)) return null;
        while (ground.getY() > level.getMinBuildHeight() && !level.getBlockState(ground).blocksMotion()) {
            ground = ground.below();
        }
        int ceiling = Math.min(level.getMaxBuildHeight() - 3, origin.getY() + 64);
        while (ground.getY() < ceiling) {
            var state = level.getBlockState(ground);
            Vec3 landing = Vec3.atBottomCenterOf(ground.above());
            if (state.isFaceSturdy(level, ground, Direction.UP) && !state.is(Blocks.BEDROCK)
                    && level.isEmptyBlock(ground.above()) && level.isEmptyBlock(ground.above(2))
                    && level.isEmptyBlock(ground.above(3)) && canArrive(actor, level, landing)) {
                return landing;
            }
            ground = ground.above();
        }
        return null;
    }

    public static boolean move(Entity actor, ServerLevel destination, Vec3 target) {
        return move(actor, destination, target, Vampirism.isVampire(actor)
                ? WitcheryTeleport::vampireTransitionEffect : TormentTravel::transitionEffect);
    }

    public static boolean moveWithLegacyCues(Entity actor, ServerLevel destination, Vec3 target) {
        return moveWithLegacyCues(actor, destination, target, Vampirism.isVampire(actor));
    }

    public static boolean moveWithPortalCues(Entity actor, ServerLevel destination, Vec3 target) {
        return moveWithLegacyCues(actor, destination, target, false);
    }

    private static boolean moveWithLegacyCues(Entity actor, ServerLevel destination, Vec3 target, boolean vampire) {
        if (!isInWorld(destination, target)) return false;
        return move(actor, destination, target,
                (level, position) -> WitcheryNetwork.sendLegacyTeleport(level, position, vampire), true);
    }

    private static void vampireTransitionEffect(ServerLevel level, Vec3 position) {
        level.sendParticles(ParticleTypes.SMOKE, position.x, position.y + 0.8, position.z, 16, 0.5, 0.9, 0.5, 0.03);
        level.playSound(null, position.x, position.y, position.z, WitcherySounds.MIRROR_POOF.get(), SoundSource.PLAYERS, 1, 1);
    }

    public static boolean move(Entity actor, ServerLevel destination, Vec3 target, BiConsumer<ServerLevel, Vec3> transitionEffect) {
        return move(actor, destination, target, transitionEffect, false);
    }

    private static boolean move(Entity actor, ServerLevel destination, Vec3 target,
                                BiConsumer<ServerLevel, Vec3> transitionEffect, boolean departureBeforeMove) {
        ServerLevel origin = (ServerLevel) actor.level();
        Vec3 start = actor.position();
        ChunkPos startChunk = actor.chunkPosition();
        if (departureBeforeMove) transitionEffect.accept(origin, start);
        Entity moved = actor;
        if (actor instanceof ServerPlayer player) {
            player.teleportTo(destination, target.x, target.y, target.z, player.getYRot(), player.getXRot());
            if (player.connection != null) player.connection.resetPosition();
        } else if (origin == destination) {
            if (startChunk.equals(new ChunkPos(BlockPos.containing(target)))) {
                actor.teleportTo(target.x, target.y, target.z);
            } else {
                actor.teleportToWithTicket(target.x, target.y, target.z);
            }
        } else {
            moved = actor.changeDimension(destination, new ITeleporter() {
                @Override
                public PortalInfo getPortalInfo(Entity entity, ServerLevel level, Function<ServerLevel, PortalInfo> defaultInfo) {
                    return new PortalInfo(target, Vec3.ZERO, entity.getYRot(), entity.getXRot());
                }

                @Override
                public Entity placeEntity(Entity entity, ServerLevel source, ServerLevel level, float yaw,
                                          Function<Boolean, Entity> reposition) {
                    return reposition.apply(false);
                }
            });
        }
        if (moved == null || moved.level() != destination || moved.distanceToSqr(target) > 0.01) return false;
        if (!(moved instanceof ServerPlayer) && (origin != destination || !startChunk.equals(moved.chunkPosition()))) {
            // Make the destination entity-ticking long enough for its entity storage to load.
            destination.getChunkSource().addRegionTicket(ARRIVAL, moved.chunkPosition(), 2, moved.getId());
        }
        moved.resetFallDistance();
        if (!departureBeforeMove) transitionEffect.accept(origin, start);
        transitionEffect.accept(destination, target);
        return true;
    }
}
