package art.arcane.witchery.entity;

import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;

public final class HobgoblinGodEncounter {
    private HobgoblinGodEncounter() {}

    public static boolean summon(ServerLevel level, BlockPos statue, ServerPlayer patron, int detection, int radius) {
        if (patron.level() != level || !patron.isAlive()
                || !level.getEntitiesOfClass(HobgoblinGod.class, new AABB(statue).inflate(detection), HobgoblinGod::isAlive).isEmpty()) {
            return false;
        }
        var random = level.getRandom();
        var spawn = statue.offset(random.nextInt(radius * 2 + 1) - radius, 0,
                random.nextInt(radius * 2 + 1) - radius).mutable();
        if (!level.hasChunkAt(spawn) || !level.getWorldBorder().isWithinBounds(spawn)) return false;
        int limit = Math.min(statue.getY() + 8, level.getMaxBuildHeight() - 2);
        while (!level.isEmptyBlock(spawn) && spawn.getY() < limit) spawn.move(Direction.UP);
        while (spawn.getY() > level.getMinBuildHeight() && level.isEmptyBlock(spawn.below())) spawn.move(Direction.DOWN);
        BlockPos floor = spawn.below();
        if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) return false;

        HobgoblinGod mog = WitcheryEntities.MOG.get().create(level);
        HobgoblinGod gulg = WitcheryEntities.GULG.get().create(level);
        if (mog == null || gulg == null) return false;
        place(level, spawn, patron, mog);
        place(level, spawn, patron, gulg);
        if (!level.noCollision(mog) || level.containsAnyLiquid(mog.getBoundingBox())) return false;
        if (!level.addFreshEntity(mog)) return false;
        if (!level.addFreshEntity(gulg)) {
            mog.discard();
            return false;
        }
        level.sendParticles(ParticleTypes.FLAME, mog.getX(), mog.getY(0.5), mog.getZ(), 16, 1, 0.9, 1, 0.02);
        level.playSound(null, spawn, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1, 1);
        return true;
    }

    private static void place(ServerLevel level, BlockPos spawn, ServerPlayer patron, HobgoblinGod god) {
        god.moveTo(spawn.getX() + 0.5, spawn.getY() + 0.05, spawn.getZ() + 0.5, 0, 0);
        ForgeEventFactory.onFinalizeSpawn(god, level, level.getCurrentDifficultyAt(spawn), MobSpawnType.TRIGGERED, null, null);
        god.assignPatron(patron);
    }
}
