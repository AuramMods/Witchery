package art.arcane.witchery.brewing;

import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

final class BrewFireEffects {
    private BrewFireEffects() {}

    static void openHellGate(ServerLevel level, BlockPos center) {
        BlockPos site = center.offset(level.random.nextInt(5) - 2, 0, level.random.nextInt(5) - 2);
        if (!level.isInWorldBounds(site) || !level.getWorldBorder().isWithinBounds(site) || !level.hasChunkAt(site)) return;
        int ceiling = Math.min(center.getY() + 8, level.getMaxBuildHeight() - 1);
        while (site.getY() < ceiling && !level.isEmptyBlock(site)) site = site.above();
        while (site.getY() > level.getMinBuildHeight() && level.isEmptyBlock(site)) site = site.below();
        // Hell Gate accepts two blocks of air, including the open sky above the build limit.
        if (!level.isEmptyBlock(site.above()) || !level.isEmptyBlock(site.above(2))) return;
        var demon = WitcheryEntities.DEMON.get().create(level);
        if (demon == null) return;
        Vec3 arrival = Vec3.atBottomCenterOf(site.above()).add(0, 0.05, 0);
        demon.moveTo(arrival);
        level.addFreshEntity(demon);
        WitcheryNetwork.sendHellGateArrival(level, arrival, demon.getBbHeight());
    }

    static void blast(ServerLevel level, Vec3 center, int strength, @Nullable Entity caster) {
        level.explode(caster != null && caster.level() == level ? caster : null, center.x, center.y, center.z, strength,
                Level.ExplosionInteraction.TNT);
    }
}
