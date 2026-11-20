package art.arcane.witchery.vampirism;

import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
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

    boolean activate(ServerPlayer player) {
        return switch (this) {
            case STORM -> startStorm(player);
            case SWARM -> startSwarm(player);
            default -> false;
        };
    }

    private static boolean startStorm(ServerPlayer player) {
        var world = player.serverLevel();
        if (!world.dimension().equals(Level.OVERWORLD) || world.getLevelData().isRaining()) return false;
        int duration = 20 * (300 + world.random.nextInt(600));
        world.setWeatherParameters(0, duration, true, true);
        return true;
    }

    private static boolean startSwarm(ServerPlayer player) {
        var world = player.serverLevel();
        int spawned = 0;
        for (int i = 0; i < 15; i++) {
            int x = world.random.nextIntBetweenInclusive(1, 4) * (world.random.nextBoolean() ? 1 : -1);
            int z = world.random.nextIntBetweenInclusive(1, 4) * (world.random.nextBoolean() ? 1 : -1);
            Vec3 release = swarmRelease(world, player.blockPosition().offset(x, 3, z));
            if (release == null) continue;
            var bat = WitcheryEntities.ATTACK_BAT.get().create(world);
            if (bat == null) continue;
            bat.moveTo(release);
            bat.joinSwarm(player);
            if (world.noCollision(bat) && world.addFreshEntity(bat)) {
                spawned++;
                world.sendParticles(ParticleTypes.SMOKE, release.x, release.y, release.z, 12, 0.35, 0.25, 0.35, 0.02);
            }
        }
        if (spawned == 0) return false;
        world.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1);
        return true;
    }

    @Nullable
    private static Vec3 swarmRelease(ServerLevel world, BlockPos start) {
        BlockPos.MutableBlockPos point = start.mutable();
        int ceiling = Math.min(world.getMaxBuildHeight() - 2, start.getY() + 8);
        if (point.getY() > ceiling || !world.hasChunkAt(point)) return null;
        while (point.getY() < ceiling && !world.isEmptyBlock(point)) point.move(Direction.UP);
        if (!world.isEmptyBlock(point)) return null;
        while (point.getY() > world.getMinBuildHeight() && world.isEmptyBlock(point.below())) point.move(Direction.DOWN);
        return world.isEmptyBlock(point.above()) ? Vec3.atBottomCenterOf(point).add(0, 0.05, 0) : null;
    }
}
