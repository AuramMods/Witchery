package art.arcane.witchery.vampirism;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.WitcheryDimensions;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public enum VampireUltimate {
    NONE,
    STORM,
    SWARM,
    FARM;

    public static final int MAX_CHARGES = 5;
    private static final VampireUltimate[] BY_ID = values();

    public static VampireUltimate fromId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : NONE;
    }

    public static VampireUltimate fromOffering(ItemStack held) {
        if (held.is(WitcheryIngredient.GLOBE_ARTICHOKE.item())) return STORM;
        if (held.is(WitcheryIngredient.BAT_WOOL.item())) return SWARM;
        return held.is(Items.BONE) ? FARM : NONE;
    }

    boolean activate(ServerPlayer player) {
        return switch (this) {
            case STORM -> startStorm(player);
            case SWARM -> {
                startSwarm(player);
                yield true;
            }
            case FARM -> travelToFeedingGrounds(player);
            default -> false;
        };
    }

    private static boolean startStorm(ServerPlayer player) {
        var world = player.serverLevel();
        if (world.getLevelData().isRaining()) return false;
        int duration = 20 * (300 + world.random.nextInt(600));
        // Secondary vanilla dimensions read the shared flags, but their derived weather setters are inert.
        world.setWeatherParameters(0, duration, true, true);
        return true;
    }

    private static void startSwarm(ServerPlayer player) {
        var world = player.serverLevel();
        for (int i = 0; i < 15; i++) {
            BlockPos start = BlockPos.containing(player.getX(), player.getY() + 3 + world.random.nextDouble(), player.getZ())
                    .offset(swarmOffset(world.random), 0, swarmOffset(world.random));
            Vec3 release = swarmRelease(world, start);
            if (release == null) continue;
            var bat = WitcheryEntities.ATTACK_BAT.get().create(world);
            if (bat == null) continue;
            bat.moveTo(release);
            bat.joinSwarm(player);
            if (world.addFreshEntity(bat)) WitcheryNetwork.sendAttackBatArrival(world, bat);
        }
    }

    private static int swarmOffset(RandomSource random) {
        // The original draw picks -4 through -1 or +2 through +4 independently on each axis.
        int draw = random.nextInt(7);
        return draw - 4 + (draw > 3 ? 2 : 0);
    }

    private static boolean travelToFeedingGrounds(ServerPlayer player) {
        ServerLevel here = player.serverLevel();
        if (here.dimension().equals(WitcheryDimensions.DREAM)) return false;
        ServerLevel home = player.getServer().getLevel(player.getRespawnDimension());
        BlockPos respawn = player.getRespawnPosition();
        Vec3 arrival = home != null && respawn != null ? Player.findRespawnPositionAndUseSpawnBlock(home, respawn,
                player.getRespawnAngle(), player.isRespawnForced(), true).orElse(null) : null;
        if (arrival == null) {
            home = player.getServer().overworld();
            arrival = safeFooting(player, home, home.getSharedSpawnPos());
        }
        if (arrival == null) return false;
        if (home == here && player.distanceToSqr(arrival.x, player.getY(), arrival.z) <= 36) {
            var village = VampireVillages.forFeedingGrounds(here, player.blockPosition());
            if (village.isEmpty()) return false;
            var target = village.get();
            arrival = target.legacy() ? legacyVillageFooting(player, here, target.position())
                    : safeFooting(player, here, target.position());
            if (arrival == null) return false;
        }
        if (!WitcheryTeleport.canArrive(player, home, arrival)) return false;
        return WitcheryTeleport.moveWithLegacyCues(player, home, arrival);
    }

    @Nullable
    static Vec3 legacyVillageFooting(ServerPlayer player, ServerLevel world, BlockPos village) {
        BlockPos start = village.above();
        for (int distance = 0; distance < 16; distance++) {
            Vec3 landing = villageLanding(player, world, start.above(distance));
            if (landing != null) return landing;
            if (distance > 0) {
                landing = villageLanding(player, world, start.below(distance));
                if (landing != null) return landing;
            }
        }
        return null;
    }

    @Nullable
    private static Vec3 villageLanding(ServerPlayer player, ServerLevel world, BlockPos ground) {
        if (world.isOutsideBuildHeight(ground) || world.isOutsideBuildHeight(ground.above(2))
                || world.getBlockState(ground).canBeReplaced()
                || !world.getBlockState(ground.above()).canBeReplaced()
                || !world.getBlockState(ground.above(2)).canBeReplaced()) return null;
        Vec3 landing = Vec3.atLowerCornerOf(ground.above());
        return WitcheryTeleport.canArrive(player, world, landing) ? landing : null;
    }

    @Nullable
    private static Vec3 safeFooting(ServerPlayer player, ServerLevel world, BlockPos destination) {
        for (BlockPos spot : BlockPos.withinManhattan(destination, 2, 16, 2)) {
            BlockPos floor = spot.below();
            if (!world.getBlockState(floor).isFaceSturdy(world, floor, Direction.UP)
                    || !world.isEmptyBlock(spot) || !world.isEmptyBlock(spot.above())) continue;
            Vec3 arrival = Vec3.atBottomCenterOf(spot);
            if (WitcheryTeleport.canArrive(player, world, arrival)) return arrival;
        }
        return null;
    }

    @Nullable
    private static Vec3 swarmRelease(ServerLevel world, BlockPos start) {
        BlockPos.MutableBlockPos point = start.mutable();
        int min = world.getMinBuildHeight();
        int max = world.getMaxBuildHeight();
        point.setY(Mth.clamp(point.getY(), min, max - 1));
        int ceiling = Math.min(max - 1, point.getY() + 8);
        while (point.getY() < ceiling && !world.isEmptyBlock(point)) point.move(Direction.UP);
        while (point.getY() > min && world.isEmptyBlock(point)) point.move(Direction.DOWN);
        if (point.getY() + 2 >= max || !world.isEmptyBlock(point.above()) || !world.isEmptyBlock(point.above(2))) return null;
        return Vec3.atBottomCenterOf(point.above()).add(0, 0.05, 0);
    }
}
