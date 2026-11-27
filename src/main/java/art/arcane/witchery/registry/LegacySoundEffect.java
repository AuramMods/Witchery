package art.arcane.witchery.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/** Packet ordinals and mixer settings from Witchery 0.24.1 and the Minecraft 1.7.10 sound definitions. */
public enum LegacySoundEffect {
    NONE(null),
    RANDOM_ORB("random.orb", "minecraft:entity.experience_orb.pickup", SoundSource.PLAYERS),
    RANDOM_FIZZ("random.fizz", "minecraft:block.fire.extinguish", SoundSource.BLOCKS),
    NOTE_SNARE("note.snare", "minecraft:block.note_block.snare", SoundSource.RECORDS),
    WATER_SPLASH("game.player.swim.splash", "minecraft:entity.player.splash", SoundSource.BLOCKS),
    DAMAGE_HIT("damage.hit"),
    FIREWORKS_BLAST1("fireworks.blast", "minecraft:entity.firework_rocket.blast", SoundSource.AMBIENT),
    WATER_SWIM("game.player.swim", "minecraft:entity.generic.swim", SoundSource.PLAYERS),
    NOTE_HARP("note.harp", "witchery:note.harp", SoundSource.RECORDS),
    NOTE_PLING("note.pling", "minecraft:block.note_block.pling", SoundSource.RECORDS),
    RANDOM_EXPLODE("random.explode", "minecraft:entity.generic.explode", SoundSource.BLOCKS),
    RANDOM_POP("random.pop", "minecraft:entity.item.pickup", SoundSource.PLAYERS),
    DIG_CLOTH("dig.cloth", "minecraft:block.wool.break", SoundSource.BLOCKS),
    MOB_SLIME_BIG("mob.slime.big", "minecraft:entity.slime.jump", SoundSource.HOSTILE),
    MOB_SLIME_SMALL("mob.slime.small", "minecraft:entity.slime.squish_small", SoundSource.HOSTILE),
    MOB_ZOMBIE_DEATH("mob.zombie.death", "minecraft:entity.zombie.death", SoundSource.HOSTILE),
    MOB_ENDERMEN_PORTAL("mob.endermen.portal", "minecraft:entity.enderman.teleport", SoundSource.HOSTILE),
    FIRE_FIRE("fire.fire", "minecraft:block.fire.ambient", SoundSource.BLOCKS),
    FIRE_IGNITE("fire.ignite", "minecraft:item.flintandsteel.use", SoundSource.BLOCKS),
    MOB_GHAST_FIREBALL("mob.ghast.fireball", "minecraft:entity.ghast.shoot", SoundSource.HOSTILE),
    MOB_WITHER_SPAWN("mob.wither.spawn", "minecraft:entity.wither.spawn", SoundSource.HOSTILE),
    MOB_HORSE_SKELETON_DEATH("mob.horse.skeleton.death", "minecraft:entity.skeleton_horse.death", SoundSource.NEUTRAL),
    RANDOM_SPLASH("witchery:random.splash", SoundSource.NEUTRAL),
    MOB_SILVERFISH_KILL("mob.silverfish.kill", "minecraft:entity.silverfish.death", SoundSource.HOSTILE),
    MOB_ZOMBIE_INFECT("mob.zombie.infect", "minecraft:entity.zombie.infect", SoundSource.HOSTILE),
    MOB_WOLF_DEATH("mob.wolf.death", "minecraft:entity.wolf.death", SoundSource.NEUTRAL),
    MOB_OCELOT_DEATH("mob.ocelot.death"),
    MOB_ENDERDRAGON_GROWL("mob.enderdragon.growl", "minecraft:entity.ender_dragon.growl", SoundSource.HOSTILE),
    MOB_HORSE_SKELETON_HIT("mob.horse.skeleton.hit", "minecraft:entity.skeleton_horse.hurt", SoundSource.NEUTRAL),
    RANDOM_LEVELUP("random.levelup", "minecraft:entity.player.levelup", SoundSource.PLAYERS),
    MOB_SPIDER_SAY("mob.spider.say", "minecraft:entity.spider.ambient", SoundSource.HOSTILE),
    MOB_ZOMBIE_SAY("mob.zombie.say", "minecraft:entity.zombie.ambient", SoundSource.HOSTILE),
    WITCHERY_RANDOM_THEYCOME("witchery:random.theycome", SoundSource.PLAYERS),
    MOB_ENDERDRAGON_HIT("mob.enderdragon.hit", "minecraft:entity.ender_dragon.hurt", SoundSource.HOSTILE),
    WITCHERY_MOB_BABA_DEATH("witchery:mob.baba.baba_death", SoundSource.NEUTRAL),
    WITCHERY_MOB_BABA_LIVING("witchery:mob.baba.baba_living", SoundSource.NEUTRAL),
    WITCHERY_RANDOM_CLICK("witchery:random.click", SoundSource.PLAYERS),
    WITCHERY_RANDOM_WINDUP("witchery:random.wind_up", SoundSource.PLAYERS),
    WITCHERY_RANDOM_LOVED("witchery:random.loved", SoundSource.NEUTRAL),
    MOB_ENDERMAN_IDLE("mob.enderman.idle"),
    MOB_WITHER_DEATH("mob.wither.death", "minecraft:entity.wither.death", SoundSource.HOSTILE),
    RANDOM_BREATH("random.breath"),
    WITCHERY_MOB_SPECTRE_SPECTRE_HIT("witchery:mob.spectre.spectre_hit", SoundSource.HOSTILE),
    WITCHERY_MOB_SPECTRE_SPECTRE_SAY("witchery:mob.spectre.spectre_say", SoundSource.HOSTILE),
    MOB_BLAZE_DEATH("mob.blaze.death", "minecraft:entity.blaze.death", SoundSource.HOSTILE),
    WITCHERY_MOB_IMP_LAUGH("witchery:mob.imp.laugh", SoundSource.NEUTRAL),
    MOB_GHAST_DEATH("mob.ghast.death", "minecraft:entity.ghast.death", SoundSource.HOSTILE),
    MOB_CREEPER_DEATH("mob.creeper.death", "minecraft:entity.creeper.death", SoundSource.HOSTILE),
    WITCHERY_RANDOM_CHALK("witchery:random.chalk", SoundSource.BLOCKS),
    WITCHERY_MOB_WOLFMAN_HOWL("witchery:mob.wolfman.howl", SoundSource.HOSTILE),
    WITCHERY_MOB_WOLFMAN_EAT("witchery:mob.wolfman.eat", SoundSource.HOSTILE),
    WITCHERY_MOB_WOLFMAN_LORD("witchery:mob.wolfman.lord", SoundSource.HOSTILE),
    WITCHERY_RANDOM_HORN("witchery:random.horn", SoundSource.NEUTRAL),
    WITCHERY_RANDOM_MANTRAP("witchery:random.mantrap", SoundSource.BLOCKS),
    WITCHERY_MOB_WOLFMAN_TALK("witchery:mob.wolfman.say", SoundSource.HOSTILE),
    WITCHERY_RANDOM_HYPNOSIS("witchery:random.hypnosis", SoundSource.PLAYERS),
    WITCHERY_RANDOM_DRINK("witchery:random.drink", SoundSource.PLAYERS),
    WITCHERY_RANDOM_POOF("witchery:random.poof", SoundSource.NEUTRAL),
    WITCHERY_MOB_LILITH_TALK("witchery:mob.lilith.say", SoundSource.HOSTILE),
    WITCHERY_RANDOM_SWORD_DRAW("witchery:random.sworddraw", SoundSource.PLAYERS),
    WITCHERY_RANDOM_SWORD_SHEATHE("witchery:random.swordsheathe", SoundSource.PLAYERS),
    WITCHERY_MOB_REFLECTION_SPEECH("witchery:mob.reflection.speech", SoundSource.NEUTRAL);

    private static final LegacySoundEffect[] VALUES = values();
    private static final Map<ResourceLocation, LegacySoundEffect> BY_NAME = indexNames();
    private final ResourceLocation legacyId;
    private final ResourceLocation eventId;
    private final SoundSource source;

    LegacySoundEffect(@Nullable String legacyId) {
        this(legacyId, null, SoundSource.AMBIENT);
    }

    LegacySoundEffect(String legacyId, SoundSource source) {
        this(legacyId, legacyId, source);
    }

    LegacySoundEffect(@Nullable String legacyId, @Nullable String eventId, SoundSource source) {
        this.legacyId = legacyId == null ? null : ResourceLocation.parse(legacyId);
        this.eventId = eventId == null ? null : ResourceLocation.parse(eventId);
        this.source = source;
    }

    private static Map<ResourceLocation, LegacySoundEffect> indexNames() {
        var names = new HashMap<ResourceLocation, LegacySoundEffect>();
        for (LegacySoundEffect cue : VALUES) {
            if (cue.legacyId != null) names.put(cue.legacyId, cue);
            if (cue.eventId != null) names.put(cue.eventId, cue);
        }
        return Map.copyOf(names);
    }

    public static LegacySoundEffect byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : NONE;
    }

    @Nullable
    public static LegacySoundEffect byName(ResourceLocation id) {
        return BY_NAME.get(id);
    }

    public static SoundSource sourceFor(SoundEvent sound, SoundSource fallback) {
        LegacySoundEffect cue = byName(sound.getLocation());
        return cue == null ? fallback : cue.source;
    }

    @Nullable
    public SoundEvent sound() {
        return eventId == null ? null : BuiltInRegistries.SOUND_EVENT.getOptional(eventId).orElse(null);
    }

    public SoundSource source() {
        return source;
    }
}
