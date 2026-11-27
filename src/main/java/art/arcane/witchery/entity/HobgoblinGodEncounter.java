package art.arcane.witchery.entity;

import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public final class HobgoblinGodEncounter {
    private HobgoblinGodEncounter() {}

    public static boolean summon(ServerLevel level, BlockPos statue, ServerPlayer patron, int detection, int radius) {
        if (patron.level() != level || !patron.isAlive()
                || !level.getEntitiesOfClass(HobgoblinGod.class,
                AABB.ofSize(Vec3.atCenterOf(statue), detection * 2, detection * 2, detection * 2), god -> !god.isRemoved()).isEmpty()) {
            return false;
        }
        var random = level.getRandom();
        var spawn = statue.offset(random.nextInt(radius * 2 + 1) - radius, 0,
                random.nextInt(radius * 2 + 1) - radius).mutable();
        if (!level.hasChunkAt(spawn) || !level.getWorldBorder().isWithinBounds(spawn)) return false;
        int limit = Math.min(statue.getY() + 8, level.getMaxBuildHeight() - 2);
        while (!level.isEmptyBlock(spawn) && spawn.getY() < limit) spawn.move(Direction.UP);
        while (spawn.getY() > level.getMinBuildHeight() && level.isEmptyBlock(spawn)) spawn.move(Direction.DOWN);
        spawn.move(Direction.UP);
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
        WitcheryNetwork.sendPatronGodArrival(level, mog.position(), mog.getBbHeight());
        return true;
    }

    private static void place(ServerLevel level, BlockPos spawn, ServerPlayer patron, HobgoblinGod god) {
        god.moveTo(spawn.getX() + 0.5, spawn.getY() + 0.05, spawn.getZ() + 0.5, 0, 0);
        ForgeEventFactory.onFinalizeSpawn(god, level, level.getCurrentDifficultyAt(spawn), MobSpawnType.TRIGGERED, null, null);
        god.assignPatron(patron);
    }
}
