package art.arcane.witchery.client.packet;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.ResizingEffect;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.client.ClientWitcherySounds;
import art.arcane.witchery.client.event.WitcheryClientCameraHooks;
import art.arcane.witchery.client.particle.LegacySmokeParticle;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryRegistries;
import art.arcane.witchery.registry.WitcherySounds;
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
            SoundEvent sound = resolveSoundEvent(legacySound.soundId());
            if (sound != null) {
                ClientWitcherySounds.playLocal(level, message.x(), message.y(), message.z(),
                        sound, SoundSource.AMBIENT, 0.5F, pitch);
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

        SoundEvent sound = resolveSoundEvent(soundId);
        RandomSource random = level.getRandom();
        float volume = message.volume() == -1.0F ? 0.5F : message.volume();
        float pitch = message.pitch();
        boolean ghastFireball = sound == SoundEvents.GHAST_SHOOT;
        if (pitch == -1.0F) {
            pitch = ghastFireball ? 1 + (random.nextFloat() - random.nextFloat()) * 0.2F
                    : randomPitch(random);
        }
        if (sound == null) return;
        SoundSource source = sound == SoundEvents.SLIME_JUMP || ghastFireball ? SoundSource.HOSTILE : SoundSource.AMBIENT;
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

    private static SoundEvent resolveSoundEvent(String soundId) {
        ResourceLocation direct = ResourceLocation.tryParse(soundId);
        if (direct != null) {
            SoundEvent event = BuiltInRegistries.SOUND_EVENT.getOptional(direct).orElse(null);
            if (event != null) {
                return event;
            }
        }

        return switch (soundId) {
            case "random.orb" -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            case "random.fizz" -> SoundEvents.FIRE_EXTINGUISH;
            case "note.snare" -> SoundEvents.NOTE_BLOCK_SNARE.value();
            case "game.player.swim.splash" -> SoundEvents.PLAYER_SPLASH;
            case "fireworks.blast" -> SoundEvents.FIREWORK_ROCKET_BLAST;
            case "game.player.swim" -> SoundEvents.GENERIC_SWIM;
            case "note.harp" -> WitcherySounds.LEGACY_HARP.get();
            case "note.pling" -> SoundEvents.NOTE_BLOCK_PLING.value();
            case "random.explode" -> SoundEvents.GENERIC_EXPLODE;
            case "random.pop" -> SoundEvents.ITEM_PICKUP;
            case "dig.cloth" -> SoundEvents.WOOL_BREAK;
            case "mob.slime.big" -> SoundEvents.SLIME_JUMP;
            case "mob.slime.small" -> SoundEvents.SLIME_SQUISH_SMALL;
            case "mob.zombie.death" -> SoundEvents.ZOMBIE_DEATH;
            case "mob.endermen.portal" -> SoundEvents.ENDERMAN_TELEPORT;
            case "fire.fire" -> SoundEvents.FIRE_AMBIENT;
            case "fire.ignite" -> SoundEvents.FLINTANDSTEEL_USE;
            case "mob.ghast.fireball" -> SoundEvents.GHAST_SHOOT;
            case "mob.wither.spawn" -> SoundEvents.WITHER_SPAWN;
            case "mob.horse.skeleton.death" -> SoundEvents.SKELETON_HORSE_DEATH;
            case "mob.silverfish.kill" -> SoundEvents.SILVERFISH_DEATH;
            case "mob.zombie.infect" -> SoundEvents.ZOMBIE_INFECT;
            case "mob.wolf.death" -> SoundEvents.WOLF_DEATH;
            case "mob.enderdragon.growl" -> SoundEvents.ENDER_DRAGON_GROWL;
            case "mob.horse.skeleton.hit" -> SoundEvents.SKELETON_HORSE_HURT;
            case "random.levelup" -> SoundEvents.PLAYER_LEVELUP;
            case "mob.spider.say" -> SoundEvents.SPIDER_AMBIENT;
            case "mob.zombie.say" -> SoundEvents.ZOMBIE_AMBIENT;
            case "mob.enderdragon.hit" -> SoundEvents.ENDER_DRAGON_HURT;
            case "mob.wither.death" -> SoundEvents.WITHER_DEATH;
            case "mob.blaze.death" -> SoundEvents.BLAZE_DEATH;
            case "mob.ghast.death" -> SoundEvents.GHAST_DEATH;
            case "mob.creeper.death" -> SoundEvents.CREEPER_DEATH;
            default -> null;
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

    private enum LegacySoundEffect {
        NONE(""),
        RANDOM_ORB("random.orb"),
        RANDOM_FIZZ("random.fizz"),
        NOTE_SNARE("note.snare"),
        WATER_SPLASH("game.player.swim.splash"),
        DAMAGE_HIT("damage.hit"),
        FIREWORKS_BLAST1("fireworks.blast"),
        WATER_SWIM("game.player.swim"),
        NOTE_HARP("note.harp"),
        NOTE_PLING("note.pling"),
        RANDOM_EXPLODE("random.explode"),
        RANDOM_POP("random.pop"),
        DIG_CLOTH("dig.cloth"),
        MOB_SLIME_BIG("mob.slime.big"),
        MOB_SLIME_SMALL("mob.slime.small"),
        MOB_ZOMBIE_DEATH("mob.zombie.death"),
        MOB_ENDERMEN_PORTAL("mob.endermen.portal"),
        FIRE_FIRE("fire.fire"),
        FIRE_IGNITE("fire.ignite"),
        MOB_GHAST_FIREBALL("mob.ghast.fireball"),
        MOB_WITHER_SPAWN("mob.wither.spawn"),
        MOB_HORSE_SKELETON_DEATH("mob.horse.skeleton.death"),
        RANDOM_SPLASH("witchery:random.splash"),
        MOB_SILVERFISH_KILL("mob.silverfish.kill"),
        MOB_ZOMBIE_INFECT("mob.zombie.infect"),
        MOB_WOLF_DEATH("mob.wolf.death"),
        MOB_OCELOT_DEATH("mob.ocelot.death"),
        MOB_ENDERDRAGON_GROWL("mob.enderdragon.growl"),
        MOB_HORSE_SKELETON_HIT("mob.horse.skeleton.hit"),
        RANDOM_LEVELUP("random.levelup"),
        MOB_SPIDER_SAY("mob.spider.say"),
        MOB_ZOMBIE_SAY("mob.zombie.say"),
        WITCHERY_RANDOM_THEYCOME("witchery:random.theycome"),
        MOB_ENDERDRAGON_HIT("mob.enderdragon.hit"),
        WITCHERY_MOB_BABA_DEATH("witchery:mob.baba.baba_death"),
        WITCHERY_MOB_BABA_LIVING("witchery:mob.baba.baba_living"),
        WITCHERY_RANDOM_CLICK("witchery:random.click"),
        WITCHERY_RANDOM_WINDUP("witchery:random.wind_up"),
        WITCHERY_RANDOM_LOVED("witchery:random.loved"),
        MOB_ENDERMAN_IDLE("mob.enderman.idle"),
        MOB_WITHER_DEATH("mob.wither.death"),
        RANDOM_BREATH("random.breath"),
        WITCHERY_MOB_SPECTRE_SPECTRE_HIT("witchery:mob.spectre.spectre_hit"),
        WITCHERY_MOB_SPECTRE_SPECTRE_SAY("witchery:mob.spectre.spectre_say"),
        MOB_BLAZE_DEATH("mob.blaze.death"),
        WITCHERY_MOB_IMP_LAUGH("witchery:mob.imp.laugh"),
        MOB_GHAST_DEATH("mob.ghast.death"),
        MOB_CREEPER_DEATH("mob.creeper.death"),
        WITCHERY_RANDOM_CHALK("witchery:random.chalk"),
        WITCHERY_MOB_WOLFMAN_HOWL("witchery:mob.wolfman.howl"),
        WITCHERY_MOB_WOLFMAN_EAT("witchery:mob.wolfman.eat"),
        WITCHERY_MOB_WOLFMAN_LORD("witchery:mob.wolfman.lord"),
        WITCHERY_RANDOM_HORN("witchery:random.horn"),
        WITCHERY_RANDOM_MANTRAP("witchery:random.mantrap"),
        WITCHERY_MOB_WOLFMAN_TALK("witchery:mob.wolfman.say"),
        WITCHERY_RANDOM_HYPNOSIS("witchery:random.hypnosis"),
        WITCHERY_RANDOM_DRINK("witchery:random.drink"),
        WITCHERY_RANDOM_POOF("witchery:random.poof"),
        WITCHERY_MOB_LILITH_TALK("witchery:mob.lilith.say"),
        WITCHERY_RANDOM_SWORD_DRAW("witchery:random.sworddraw"),
        WITCHERY_RANDOM_SWORD_SHEATHE("witchery:random.swordsheathe"),
        WITCHERY_MOB_REFLECTION_SPEECH("witchery:mob.reflection.speech");

        private static final LegacySoundEffect[] VALUES = values();
        private final String soundId;

        LegacySoundEffect(String soundId) {
            this.soundId = soundId;
        }

        String soundId() {
            return soundId;
        }

        static LegacySoundEffect byId(int id) {
            if (id < 0 || id >= VALUES.length) {
                return NONE;
            }
            return VALUES[id];
        }
    }
}
