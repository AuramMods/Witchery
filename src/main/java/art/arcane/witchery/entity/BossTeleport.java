package art.arcane.witchery.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

final class BossTeleport {
    private BossTeleport() {}

    static void toward(ServerLevel level, Mob boss, LivingEntity target, double distance) {
        Vec3 origin = boss.position();
        Vec3 toward = new Vec3(target.getX() - origin.x, target.getY() - target.getEyeHeight() - boss.getY(0.5),
                target.getZ() - origin.z).normalize().scale(distance);
        var random = boss.getRandom();
        Vec3 destination = origin.add(toward).add(random.nextDouble() * 8 - 4, random.nextInt(16) - 8,
                random.nextDouble() * 8 - 4);
        if (boss.randomTeleport(destination.x, destination.y, destination.z, true)) {
            level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1, 1);
            boss.playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
        }
    }
}
