package art.arcane.witchery.client.packet;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public final class WitcheryClientPacketHandlers {
    private static final String KEY_LEGACY_GROTESQUE = "witcheryGrotesque";
    private static final String KEY_CLIENT_NIGHTMARE_LEVEL = "WitcheryNightmareLevel";
    private static final String KEY_CLIENT_GHOST = "WitcheryGhostState";

    private WitcheryClientPacketHandlers() {
    }

    public static void handleParticles(WitcheryNetwork.ParticlesPacket message) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        ParticleOptions particleType = resolveParticleType(message.particleEffectId());
        RandomSource random = level.getRandom();
        double spreadX = Math.max(0.01D, message.width());
        double spreadY = Math.max(0.01D, message.height());
        double spreadZ = Math.max(0.01D, message.width());

        for (int i = 0; i < 8; i++) {
            double px = message.x() + (random.nextDouble() - 0.5D) * spreadX;
            double py = message.y() + random.nextDouble() * spreadY;
            double pz = message.z() + (random.nextDouble() - 0.5D) * spreadZ;
            double vx = (random.nextDouble() - 0.5D) * 0.1D;
            double vy = random.nextDouble() * 0.08D;
            double vz = (random.nextDouble() - 0.5D) * 0.1D;
            level.addParticle(particleType, px, py, pz, vx, vy, vz);
        }

        if (message.soundEffectId() > 0) {
            SoundEvent sound = resolveScaffoldSound(message.soundEffectId());
            level.playLocalSound(
                    message.x(),
                    message.y(),
                    message.z(),
                    sound,
                    SoundSource.AMBIENT,
                    0.35F,
                    0.95F + random.nextFloat() * 0.2F,
                    false
            );
        }
    }

    public static void handlePlayerStyle(WitcheryNetwork.PlayerStylePacket message) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Player target = findPlayerByUsername(level, message.username());
        if (target == null) {
            Witchery.LOGGER.debug("Client style packet target '{}' not found in current level", message.username());
            return;
        }

        if (message.grotesqueTicks() > 0) {
            target.getPersistentData().putInt(KEY_LEGACY_GROTESQUE, message.grotesqueTicks());
        } else {
            target.getPersistentData().remove(KEY_LEGACY_GROTESQUE);
        }
        target.getPersistentData().putInt(KEY_CLIENT_NIGHTMARE_LEVEL, message.nightmare());
        target.getPersistentData().putBoolean(KEY_CLIENT_GHOST, message.ghost());

        WitcheryPlayerDataProvider.get(target).ifPresent(data -> data.applyPlayerStyle(
                message.playerSkin(),
                message.grotesqueTicks(),
                message.nightmare(),
                message.ghost(),
                message.creatureType(),
                message.blood()
        ));
    }

    private static Player findPlayerByUsername(ClientLevel level, String username) {
        for (Player player : level.players()) {
            if (player.getGameProfile().getName().equals(username)) {
                return player;
            }
        }
        return null;
    }

    private static ParticleOptions resolveParticleType(int particleEffectId) {
        return switch (Math.floorMod(particleEffectId, 6)) {
            case 0 -> ParticleTypes.SMOKE;
            case 1 -> ParticleTypes.FLAME;
            case 2 -> ParticleTypes.DRIPPING_WATER;
            case 3 -> ParticleTypes.PORTAL;
            case 4 -> ParticleTypes.HAPPY_VILLAGER;
            default -> ParticleTypes.ENCHANT;
        };
    }

    private static SoundEvent resolveScaffoldSound(int soundEffectId) {
        return switch (Math.floorMod(soundEffectId, 4)) {
            case 0 -> SoundEvents.BREWING_STAND_BREW;
            case 1 -> SoundEvents.FIRECHARGE_USE;
            case 2 -> SoundEvents.ENDERMAN_AMBIENT;
            default -> SoundEvents.AMETHYST_BLOCK_CHIME;
        };
    }
}
