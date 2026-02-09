package art.arcane.witchery.client.packet;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

public final class WitcheryClientPacketHandlers {
    private static final String KEY_LEGACY_GROTESQUE = "witcheryGrotesque";
    private static final String KEY_CLIENT_NIGHTMARE_LEVEL = "WitcheryNightmareLevel";
    private static final String KEY_CLIENT_GHOST = "WitcheryGhostState";
    private static final String KEY_CLIENT_LAST_CAM_X = "WitcheryCameraX";
    private static final String KEY_CLIENT_LAST_CAM_Y = "WitcheryCameraY";
    private static final String KEY_CLIENT_LAST_CAM_Z = "WitcheryCameraZ";
    private static final String KEY_CLIENT_LAST_CAM_YAW = "WitcheryCameraYaw";
    private static final String KEY_CLIENT_LAST_CAM_PITCH = "WitcheryCameraPitch";
    private static final String KEY_CLIENT_SYNCED_WIDTH = "WitcherySyncedWidth";
    private static final String KEY_CLIENT_SYNCED_HEIGHT = "WitcherySyncedHeight";
    private static final String KEY_CLIENT_SYNCED_TARGET_ID = "WitcherySyncedTargetId";

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
        double width = Math.max(0.01D, message.width());
        double height = Math.max(0.01D, message.height());
        int effectCount = Math.min(Mth.ceil(Math.max(width, 1.0D) * 20.0D), 300);

        SoundEvent sound = resolveLegacySound(message.soundEffectId());
        if (sound != null) {
            level.playLocalSound(
                    message.x(),
                    message.y(),
                    message.z(),
                    sound,
                    SoundSource.AMBIENT,
                    0.5F,
                    randomPitch(random),
                    false
            );
        }

        for (int i = 0; i < effectCount; i++) {
            double px = message.x() + random.nextDouble() * width * 2.0D - width;
            double py = message.y() + random.nextDouble() * height;
            double pz = message.z() + random.nextDouble() * width * 2.0D - width;

            if (effect == LegacyParticleEffect.SPELL_COLORED) {
                Vector3f rgb = colorToVector(message.color());
                level.addParticle(ParticleTypes.ENTITY_EFFECT, px, py, pz, rgb.x(), rgb.y(), rgb.z());
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

        WitcheryPlayerDataProvider.get(target).ifPresent(data -> data.applyPlayerStyle(
                message.playerSkin(),
                message.grotesqueTicks(),
                message.nightmare(),
                message.ghost(),
                message.creatureType(),
                message.blood()
        ));
    }

    public static void handleCamPos(WitcheryNetwork.CamPosPacket message) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity cameraEntity = minecraft.getCameraEntity();
        if (cameraEntity != null) {
            cameraEntity.setPos(message.x(), message.y(), message.z());
            cameraEntity.setYRot(message.yaw());
            cameraEntity.setXRot(message.pitch());
        }

        Player player = minecraft.player;
        if (player != null) {
            player.getPersistentData().putDouble(KEY_CLIENT_LAST_CAM_X, message.x());
            player.getPersistentData().putDouble(KEY_CLIENT_LAST_CAM_Y, message.y());
            player.getPersistentData().putDouble(KEY_CLIENT_LAST_CAM_Z, message.z());
            player.getPersistentData().putFloat(KEY_CLIENT_LAST_CAM_YAW, message.yaw());
            player.getPersistentData().putFloat(KEY_CLIENT_LAST_CAM_PITCH, message.pitch());
        }
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
        if (sound == null) {
            Witchery.LOGGER.debug("No mapped sound for client packet id '{}'", soundId);
            sound = SoundEvents.AMETHYST_BLOCK_CHIME;
        }

        RandomSource random = level.getRandom();
        float volume = message.volume() < 0.0F ? 0.5F : message.volume();
        float pitch = message.pitch() < 0.0F ? randomPitch(random) : message.pitch();
        level.playLocalSound(
                message.x(),
                message.y(),
                message.z(),
                sound,
                SoundSource.AMBIENT,
                volume,
                pitch,
                false
        );
    }

    public static void handleSyncEntitySize(WitcheryNetwork.SyncEntitySizePacket message) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Entity target = level.getEntity(message.entityId());
        if (target == null) {
            return;
        }

        target.getPersistentData().putFloat(KEY_CLIENT_SYNCED_WIDTH, message.width());
        target.getPersistentData().putFloat(KEY_CLIENT_SYNCED_HEIGHT, message.height());
        target.getPersistentData().putInt(KEY_CLIENT_SYNCED_TARGET_ID, message.entityId());
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

    private static ParticleOptions resolveParticleType(LegacyParticleEffect effect) {
        return switch (effect) {
            case HUGE_EXPLOSION -> ParticleTypes.EXPLOSION_EMITTER;
            case LARGE_EXPLODE, EXPLODE -> ParticleTypes.EXPLOSION;
            case WATER_BUBBLE -> ParticleTypes.BUBBLE;
            case SUSPENDED -> ParticleTypes.ASH;
            case DEPTH_SUSPEND -> ParticleTypes.WHITE_ASH;
            case TOWN_AURA -> ParticleTypes.HAPPY_VILLAGER;
            case CRIT -> ParticleTypes.CRIT;
            case MAGIC_CRIT -> ParticleTypes.ENCHANTED_HIT;
            case SMOKE -> ParticleTypes.SMOKE;
            case MOB_SPELL, SPELL, INSTANT_SPELL -> ParticleTypes.ENTITY_EFFECT;
            case NOTE -> ParticleTypes.NOTE;
            case PORTAL -> ParticleTypes.PORTAL;
            case ENCHANTMENT_TABLE -> ParticleTypes.ENCHANT;
            case FLAME -> ParticleTypes.FLAME;
            case LAVA -> ParticleTypes.LAVA;
            case FOOTSTEP -> ParticleTypes.CLOUD;
            case SPLASH -> ParticleTypes.SPLASH;
            case LARGE_SMOKE -> ParticleTypes.LARGE_SMOKE;
            case CLOUD -> ParticleTypes.CLOUD;
            case REDDUST -> DustParticleOptions.REDSTONE;
            case SNOWBALL_POOF -> ParticleTypes.ITEM_SNOWBALL;
            case DRIP_WATER -> ParticleTypes.DRIPPING_WATER;
            case DRIP_LAVA -> ParticleTypes.DRIPPING_LAVA;
            case SNOW_SHOVEL -> ParticleTypes.SNOWFLAKE;
            case SLIME -> ParticleTypes.ITEM_SLIME;
            case HEART -> ParticleTypes.HEART;
            case ICON_CRACK, TILE_CRACK -> ParticleTypes.ITEM_SLIME;
            case SPELL_COLORED -> ParticleTypes.ENTITY_EFFECT;
        };
    }

    private static SoundEvent resolveLegacySound(int soundEffectId) {
        LegacySoundEffect legacySound = LegacySoundEffect.byId(soundEffectId);
        if (legacySound == LegacySoundEffect.NONE) {
            return null;
        }
        SoundEvent mapped = resolveSoundEvent(legacySound.soundId());
        return mapped != null ? mapped : SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    private static SoundEvent resolveSoundEvent(String soundId) {
        ResourceLocation direct = ResourceLocation.tryParse(soundId);
        if (direct != null) {
            SoundEvent event = BuiltInRegistries.SOUND_EVENT.getOptional(direct).orElse(null);
            if (event != null) {
                return event;
            }
        }

        if (!soundId.contains(":")) {
            ResourceLocation defaultNamespace = ResourceLocation.tryParse("minecraft:" + soundId);
            if (defaultNamespace != null) {
                SoundEvent event = BuiltInRegistries.SOUND_EVENT.getOptional(defaultNamespace).orElse(null);
                if (event != null) {
                    return event;
                }
            }
        }

        return switch (soundId) {
            case "random.orb" -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            case "random.fizz" -> SoundEvents.FIRE_EXTINGUISH;
            case "note.snare" -> SoundEvents.NOTE_BLOCK_SNARE.value();
            case "game.player.swim.splash" -> SoundEvents.PLAYER_SPLASH;
            case "damage.hit" -> SoundEvents.PLAYER_HURT;
            case "fireworks.blast" -> SoundEvents.FIREWORK_ROCKET_BLAST;
            case "game.player.swim" -> SoundEvents.PLAYER_SWIM;
            case "note.harp" -> SoundEvents.NOTE_BLOCK_HARP.value();
            case "note.pling" -> SoundEvents.NOTE_BLOCK_PLING.value();
            case "random.explode" -> SoundEvents.GENERIC_EXPLODE;
            case "random.pop" -> SoundEvents.BUBBLE_COLUMN_BUBBLE_POP;
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
            case "mob.ocelot.death" -> SoundEvents.CAT_DEATH;
            case "mob.enderdragon.growl" -> SoundEvents.ENDER_DRAGON_GROWL;
            case "mob.horse.skeleton.hit" -> SoundEvents.SKELETON_HORSE_HURT;
            case "random.levelup" -> SoundEvents.PLAYER_LEVELUP;
            case "mob.spider.say" -> SoundEvents.SPIDER_AMBIENT;
            case "mob.zombie.say" -> SoundEvents.ZOMBIE_AMBIENT;
            case "mob.enderdragon.hit" -> SoundEvents.ENDER_DRAGON_HURT;
            case "mob.enderman.idle" -> SoundEvents.ENDERMAN_AMBIENT;
            case "mob.wither.death" -> SoundEvents.WITHER_DEATH;
            case "random.breath" -> SoundEvents.PLAYER_BREATH;
            case "mob.blaze.death" -> SoundEvents.BLAZE_DEATH;
            case "mob.ghast.death" -> SoundEvents.GHAST_DEATH;
            case "mob.creeper.death" -> SoundEvents.CREEPER_DEATH;
            default -> null;
        };
    }

    private static float randomPitch(RandomSource random) {
        return 0.4F / (random.nextFloat() * 0.4F + 0.8F);
    }

    private static Vector3f colorToVector(int color) {
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        return new Vector3f(red, green, blue);
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
