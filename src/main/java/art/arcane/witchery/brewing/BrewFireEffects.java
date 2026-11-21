package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        if (level.isEmptyBlock(site) || level.isOutsideBuildHeight(site.above(3))) return;
        var demon = WitcheryEntities.DEMON.get().create(level);
        if (demon == null) return;
        Vec3 arrival = Vec3.atBottomCenterOf(site.above()).add(0, 0.05, 0);
        demon.moveTo(arrival);
        if (!level.noCollision(demon) || !level.addFreshEntity(demon)) return;
        level.sendParticles(ParticleTypes.FLAME, arrival.x, arrival.y, arrival.z, 16, 1, demon.getBbHeight(), 1, 0);
        level.playSound(null, demon.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1, 1);
    }

    static void blast(ServerLevel level, Vec3 center, int strength, @Nullable Entity caster) {
        level.explode(caster != null && caster.level() == level ? caster : null, center.x, center.y, center.z, strength,
                Level.ExplosionInteraction.TNT);
    }
}
