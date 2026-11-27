package art.arcane.witchery.client.packet;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.ResizingEffect;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.client.ClientWitcherySounds;
import art.arcane.witchery.client.event.WitcheryClientCameraHooks;
import art.arcane.witchery.client.particle.LegacySmokeParticle;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryRegistries;
import art.arcane.witchery.registry.LegacySoundEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class WitcheryClientPacketHandlers {
    private static final String KEY_LEGACY_GROTESQUE = "witcheryGrotesque";
    private static final String KEY_CLIENT_NIGHTMARE_LEVEL = "WitcheryNightmareLevel";
    private static final String KEY_CLIENT_GHOST = "WitcheryGhostState";
    private static final String KEY_CLIENT_SYNC_REVISION = "WitcherySyncRevision";
    private static final String KEY_CLIENT_SYNC_INITIALIZED = "WitcherySyncInitialized";

    private WitcheryClientPacketHandlers() {
    }

    public static void handleParticles(WitcheryNetwork.ParticlesPacket message) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        LegacyParticleEffect effect = LegacyParticleEffect.byId(message.particleEffectId());
        ParticleOptions particleType = resolveParticleType(effect);
        RandomSource random = level.getRandom();
        double width = message.width();
        double height = message.height();
        int effectCount = Math.min(Mth.ceil(Math.max(width, 1.0D) * 20.0D), 300);

        LegacySoundEffect legacySound = LegacySoundEffect.byId(message.soundEffectId());
        if (legacySound != LegacySoundEffect.NONE) {
            float pitch = randomPitch(random);
            SoundEvent sound = legacySound.sound();
            if (sound != null) {
                ClientWitcherySounds.playLocal(level, message.x(), message.y(), message.z(),
                        sound, legacySound.source(), 0.5F, pitch);
            }
        }

        for (int i = 0; i < effectCount; i++) {
            // The original consumed these draws even though it spawned each particle with zero velocity.
            random.nextGaussian();
            random.nextGaussian();
            random.nextGaussian();
            double px = message.x() + random.nextDouble() * width * 2.0D - width;
            double py = message.y() + random.nextDouble() * height;
            double pz = message.z() + random.nextFloat() * width * 2.0D - width;

            if (effect == LegacyParticleEffect.SPELL_COLORED) {
                if (minecraft.particleEngine.createParticle(particleType, px, py, pz, 0, 0, 0) instanceof LegacySmokeParticle smoke) {
                    smoke.applyTint(message.color());
                }
            } else {
                level.addParticle(particleType, px, py, pz, 0.0D, 0.0D, 0.0D);
            }
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

        WitcheryPlayerDataProvider.get(target).ifPresent(data -> {
            int oldForm = data.getCreatureTypeOrdinal();
            data.applyPlayerStyle(message.playerSkin(), message.grotesqueTicks(), message.nightmare(), message.ghost(),
                    message.creatureType(), message.blood());
            if (oldForm != data.getCreatureTypeOrdinal()) target.refreshDimensions();
        });
    }

    public static void handleCamPos(WitcheryNetwork.CamPosPacket message) {
        WitcheryClientCameraHooks.updateView(message.active(), message.updatePosition(), message.entityId());
    }

    public static void handlePlayerSync(WitcheryNetwork.PlayerSyncPacket message) {
        applyClientSyncData(message.playerId(), message.syncRevision(), null, message.playerData());
    }

    public static void handleExtendedPlayerSync(WitcheryNetwork.ExtendedPlayerSyncPacket message) {
        applyClientSyncData(message.playerId(), message.syncRevision(), message.initialized(), message.playerData());
    }

    public static void handlePartialExtendedPlayerSync(WitcheryNetwork.PartialExtendedPlayerSyncPacket message) {
        applyClientSyncData(message.playerId(), message.syncRevision(), null, message.playerData());
    }

    public static void handlePushTarget(WitcheryNetwork.PushTargetPacket message) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Entity target = message.entityId() != 0 ? level.getEntity(message.entityId()) : minecraft.player;
        if (target == null) {
            return;
        }

        target.setDeltaMovement(message.velocityX(), message.velocityY(), message.velocityZ());
        target.hasImpulse = true;
    }

    public static void handleSound(WitcheryNetwork.SoundPacket message) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        String soundId = message.soundId() == null ? "" : message.soundId().trim();
        if (soundId.isEmpty()) {
            return;
        }

        ResourceLocation id = ResourceLocation.tryParse(soundId);
        if (id == null) return;
        LegacySoundEffect legacySound = LegacySoundEffect.byName(id);
        SoundEvent sound = legacySound == null ? BuiltInRegistries.SOUND_EVENT.getOptional(id).orElse(null) : legacySound.sound();
        RandomSource random = level.getRandom();
        float volume = message.volume() == -1.0F ? 0.5F : message.volume();
        float pitch = message.pitch();
        boolean ghastFireball = sound == SoundEvents.GHAST_SHOOT;
        if (pitch == -1.0F) {
            pitch = ghastFireball ? 1 + (random.nextFloat() - random.nextFloat()) * 0.2F
                    : randomPitch(random);
        }
        if (sound == null) return;
        SoundSource source = legacySound == null ? SoundSource.AMBIENT : legacySound.source();
        ClientWitcherySounds.playLocal(level, message.x(), message.y(), message.z(), sound, source, volume, pitch);
    }

    public static void handleSyncEntitySize(WitcheryNetwork.SyncEntitySizePacket message) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        if (level.getEntity(message.entityId()) instanceof LivingEntity target) {
            ResizingEffect.setScale(target, message.scale());
        }
    }

    public static void handleSetClientPlayerFacing(WitcheryNetwork.SetClientPlayerFacingPacket message) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        player.setYRot(message.yaw());
        player.setXRot(message.pitch());
        player.setYHeadRot(message.yaw());
        player.setYBodyRot(message.yaw());
    }

    private static Player findPlayerByUsername(ClientLevel level, String username) {
        for (Player player : level.players()) {
            if (player.getGameProfile().getName().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }

    private static void applyClientSyncData(UUID playerId, int syncRevision, Boolean initialized, CompoundTag playerData) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player target = null;

        if (level != null) {
            target = level.getPlayerByUUID(playerId);
        }

        if (target == null && minecraft.player != null && minecraft.player.getUUID().equals(playerId)) {
            target = minecraft.player;
        }

        if (target == null) {
            Witchery.LOGGER.debug("Client sync packet target '{}' not found in current level", playerId);
            return;
        }
        final Player resolvedTarget = target;

        int currentRevision = resolvedTarget.getPersistentData().getInt(KEY_CLIENT_SYNC_REVISION);
        if (syncRevision < currentRevision) {
            Witchery.LOGGER.debug(
                    "Ignoring stale client sync packet for '{}' revision={} current={}",
                    playerId,
                    syncRevision,
                    currentRevision
            );
            return;
        }

        resolvedTarget.getPersistentData().putInt(KEY_CLIENT_SYNC_REVISION, syncRevision);
        if (initialized != null) {
            resolvedTarget.getPersistentData().putBoolean(KEY_CLIENT_SYNC_INITIALIZED, initialized);
        }

        WitcheryPlayerDataProvider.get(resolvedTarget).ifPresent(data -> {
            if (syncRevision < data.getSyncRevision()) {
                return;
            }

            if (playerData != null && !playerData.isEmpty()) {
                int oldForm = data.getCreatureTypeOrdinal();
                data.deserializeNBT(playerData.copy());
                if (oldForm != data.getCreatureTypeOrdinal()) resolvedTarget.refreshDimensions();
            }
            data.mergeSyncRevision(syncRevision);
            if (initialized != null) {
                data.setInitialized(initialized);
            }

            if (data.getGrotesqueTicks() > 0) {
                resolvedTarget.getPersistentData().putInt(KEY_LEGACY_GROTESQUE, data.getGrotesqueTicks());
            } else {
                resolvedTarget.getPersistentData().remove(KEY_LEGACY_GROTESQUE);
            }
            resolvedTarget.getPersistentData().putInt(KEY_CLIENT_NIGHTMARE_LEVEL, data.getNightmareLevel());
            resolvedTarget.getPersistentData().putBoolean(KEY_CLIENT_GHOST, data.isGhost());
            resolvedTarget.getPersistentData().putBoolean(KEY_CLIENT_SYNC_INITIALIZED, data.isInitialized());
        });
    }

    private static ParticleOptions resolveParticleType(LegacyParticleEffect effect) {
        return switch (effect) {
            case HUGE_EXPLOSION -> ParticleTypes.EXPLOSION_EMITTER;
            case LARGE_EXPLODE -> ParticleTypes.EXPLOSION;
            case EXPLODE -> ParticleTypes.POOF;
            case WATER_BUBBLE -> ParticleTypes.BUBBLE;
            case SUSPENDED -> ParticleTypes.ASH;
            case DEPTH_SUSPEND -> ParticleTypes.WHITE_ASH;
            case TOWN_AURA -> ParticleTypes.HAPPY_VILLAGER;
            case CRIT -> ParticleTypes.CRIT;
            case MAGIC_CRIT -> ParticleTypes.ENCHANTED_HIT;
            case SMOKE, SPELL_COLORED -> WitcheryRegistries.LEGACY_SMOKE.get();
            case MOB_SPELL -> ParticleTypes.ENTITY_EFFECT;
            case SPELL -> ParticleTypes.EFFECT;
            case INSTANT_SPELL -> ParticleTypes.INSTANT_EFFECT;
            case NOTE -> ParticleTypes.NOTE;
            case PORTAL -> ParticleTypes.PORTAL;
            case ENCHANTMENT_TABLE -> ParticleTypes.ENCHANT;
            case FLAME -> ParticleTypes.FLAME;
            case LAVA -> ParticleTypes.LAVA;
            case FOOTSTEP -> ParticleTypes.CLOUD;
            case SPLASH -> ParticleTypes.SPLASH;
            case LARGE_SMOKE -> WitcheryRegistries.LEGACY_LARGE_SMOKE.get();
            case CLOUD -> ParticleTypes.CLOUD;
            case REDDUST -> DustParticleOptions.REDSTONE;
            case SNOWBALL_POOF -> ParticleTypes.ITEM_SNOWBALL;
            case DRIP_WATER -> ParticleTypes.DRIPPING_WATER;
            case DRIP_LAVA -> ParticleTypes.DRIPPING_LAVA;
            case SNOW_SHOVEL -> ParticleTypes.SNOWFLAKE;
            case SLIME -> ParticleTypes.ITEM_SLIME;
            case HEART -> ParticleTypes.HEART;
            case ICON_CRACK, TILE_CRACK -> ParticleTypes.ITEM_SLIME;
        };
    }

    private static float randomPitch(RandomSource random) {
        return 0.4F / ((float) random.nextDouble() * 0.4F + 0.8F);
    }

    private enum LegacyParticleEffect {
        HUGE_EXPLOSION,
        LARGE_EXPLODE,
        WATER_BUBBLE,
        SUSPENDED,
        DEPTH_SUSPEND,
        TOWN_AURA,
        CRIT,
        MAGIC_CRIT,
        SMOKE,
        MOB_SPELL,
        SPELL,
        INSTANT_SPELL,
        NOTE,
        PORTAL,
        ENCHANTMENT_TABLE,
        EXPLODE,
        FLAME,
        LAVA,
        FOOTSTEP,
        SPLASH,
        LARGE_SMOKE,
        CLOUD,
        REDDUST,
        SNOWBALL_POOF,
        DRIP_WATER,
        DRIP_LAVA,
        SNOW_SHOVEL,
        SLIME,
        HEART,
        ICON_CRACK,
        TILE_CRACK,
        SPELL_COLORED;

        private static final LegacyParticleEffect[] VALUES = values();

        static LegacyParticleEffect byId(int id) {
            if (id < 0 || id >= VALUES.length) {
                return SMOKE;
            }
            return VALUES[id];
        }
    }
}
