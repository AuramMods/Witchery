package art.arcane.witchery.block.entity;

import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.world.DreamTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum FetishEffect {
    ENHANCED_POPPETS(1, "enhanced_poppets", 0, 0, 3, 1, 1, 1),
    SENTINEL(2, "sentinel", 8, 600, 3, 3, 0, 0),
    SCREAMER(3, "screamer", 16, 0, 3, 0, 2, 0),
    TWISTER(4, "twister", 8, 10, 3, 0, 0, 2),
    GHOST_WALKER(5, "ghost_walker", 8, 0, 3, 1, 1, 0);

    public final int id;
    public final String key;
    public final int radius;
    public final int cooldown;
    public final int spirits;
    public final int spectres;
    public final int banshees;
    public final int poltergeists;

    FetishEffect(int id, String key, int radius, int cooldown, int spirits, int spectres, int banshees, int poltergeists) {
        this.id = id;
        this.key = key;
        this.radius = radius;
        this.cooldown = cooldown;
        this.spirits = spirits;
        this.spectres = spectres;
        this.banshees = banshees;
        this.poltergeists = poltergeists;
    }

    @Nullable
    public static FetishEffect byId(int id) {
        for (FetishEffect effect : values()) {
            if (effect.id == id) return effect;
        }
        return null;
    }

    void activate(ServerLevel level, BlockPos pos, boolean quiet, List<LivingEntity> intruders) {
        switch (this) {
            case ENHANCED_POPPETS -> { }
            case SENTINEL -> {
                int count = intruders.size() == 1 ? 2 : 1;
                for (LivingEntity intruder : intruders) {
                    for (int i = 0; i < count; i++) SpectralUndead.summonSentinel(level, intruder);
                }
            }
            case SCREAMER -> {
                level.sendParticles(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 0.3,
                        pos.getZ() + 0.5, 16, 0.2, 0.5, 0.2, 0);
                if (!quiet) level.playSound(null, pos, WitcherySounds.SPECTRE_HIT.get(), SoundSource.BLOCKS, 1, 1);
            }
            case TWISTER -> {
                Vec3 center = Vec3.atCenterOf(pos);
                for (LivingEntity intruder : intruders) {
                    if (intruder instanceof ServerPlayer player) turnPlayer(player, center);
                    else if (intruder instanceof Mob mob && mob.getMaxHealth() < 50) divertMob(level, mob, intruders);
                }
            }
            case GHOST_WALKER -> {
                for (LivingEntity intruder : intruders) {
                    if (intruder instanceof ServerPlayer player) DreamTravel.deferManifestationCountdown(player);
                }
            }
        }
    }

    private static void turnPlayer(ServerPlayer player, Vec3 center) {
        if (player.getMainHandItem().isEmpty() && player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                && player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() && player.getItemBySlot(EquipmentSlot.LEGS).isEmpty()
                && player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) return;
        float facing = (float) (Math.toDegrees(Mth.atan2(center.z - player.getZ(), center.x - player.getX())) - 90);
        if (Math.abs(Mth.wrapDegrees(facing - player.getYRot())) < 45 && player.connection != null) {
            player.connection.teleport(player.getX(), player.getY(), player.getZ(), Mth.wrapDegrees(facing + 180), player.getXRot());
        }
    }

    private static void divertMob(ServerLevel level, Mob mob, List<LivingEntity> intruders) {
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        mob.getNavigation().stop();
        List<LivingEntity> targets = intruders.stream().filter(candidate -> candidate != mob && mob.canAttack(candidate)).toList();
        if (!targets.isEmpty()) mob.setTarget(targets.get(level.random.nextInt(targets.size())));
    }
}
