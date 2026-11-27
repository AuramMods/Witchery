package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.concurrent.ThreadLocalRandom;

public final class WitcherySounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Witchery.MODID);
    public static final RegistryObject<SoundEvent> CHALK = sound("random.chalk");
    public static final RegistryObject<SoundEvent> CAULDRON_BUBBLE = sound("random.blop");
    public static final RegistryObject<SoundEvent> WOOD_CREAK = sound("random.wood_creak");
    public static final RegistryObject<SoundEvent> HEARTBEAT = sound("random.heartbeat");
    public static final RegistryObject<SoundEvent> NIGHTMARE_LIVE = sound("mob.nightmare.nightmare_live");
    public static final RegistryObject<SoundEvent> NIGHTMARE_HIT = sound("mob.nightmare.nightmare_hit");
    public static final RegistryObject<SoundEvent> NIGHTMARE_DEAD = sound("mob.nightmare.nightmare_dead");
    public static final RegistryObject<SoundEvent> DARK_MARK_IDLE = sound("mob.darkmark.idle");
    public static final RegistryObject<SoundEvent> TORMENT_LAUGH = sound("mob.torment.laugh");
    public static final RegistryObject<SoundEvent> TORMENT_HIT = sound("mob.torment.hit");
    public static final RegistryObject<SoundEvent> TORMENT_DEATH = sound("mob.torment.death");
    public static final RegistryObject<SoundEvent> COVEN_TALK = sound("mob.baba.baba_living");
    public static final RegistryObject<SoundEvent> COVEN_CACKLE = sound("mob.baba.baba_death");
    public static final RegistryObject<SoundEvent> GOBLIN_IDLE = sound("mob.goblin.idle");
    public static final RegistryObject<SoundEvent> GOBLIN_HAGGLE = sound("mob.goblin.haggle");
    public static final RegistryObject<SoundEvent> GOBLIN_HIT = sound("mob.goblin.hit");
    public static final RegistryObject<SoundEvent> GOBLIN_DEATH = sound("mob.goblin.death");
    public static final RegistryObject<SoundEvent> GOBLIN_YES = sound("mob.goblin.yes");
    public static final RegistryObject<SoundEvent> GOBLIN_NO = sound("mob.goblin.no");
    public static final RegistryObject<SoundEvent> MOG_IDLE = sound("mob.goblin.mog_idle");
    public static final RegistryObject<SoundEvent> GULG_IDLE = sound("mob.goblin.gulg_idle");
    public static final RegistryObject<SoundEvent> IMP_LAUGH = sound("mob.imp.laugh");
    public static final RegistryObject<SoundEvent> IMP_HURT = sound("mob.imp.hit");
    public static final RegistryObject<SoundEvent> IMP_DEATH = sound("mob.imp.death");
    public static final RegistryObject<SoundEvent> LEONARD_AMBIENT = sound("mob.leonard.say");
    public static final RegistryObject<SoundEvent> LEONARD_HIT = sound("mob.leonard.hit");
    public static final RegistryObject<SoundEvent> LEONARD_DEATH = sound("mob.leonard.death");
    public static final RegistryObject<SoundEvent> MIRROR_SPEECH = sound("mob.reflection.speech");
    public static final RegistryObject<SoundEvent> REFLECTION_AMBIENT = sound("mob.reflection.say");
    public static final RegistryObject<SoundEvent> REFLECTION_HIT = sound("mob.reflection.hit");
    public static final RegistryObject<SoundEvent> REFLECTION_DEATH = sound("mob.reflection.death");
    public static final RegistryObject<SoundEvent> MIRROR_POOF = sound("random.poof");
    public static final RegistryObject<SoundEvent> MIRROR_SPLASH = sound("random.splash");
    public static final RegistryObject<SoundEvent> LILITH_TALK = sound("mob.lilith.say");
    public static final RegistryObject<SoundEvent> LILITH_HIT = sound("mob.lilith.hit");
    public static final RegistryObject<SoundEvent> LILITH_DEATH = sound("mob.lilith.death");
    public static final RegistryObject<SoundEvent> BLOOD_DRINK = sound("random.drink");
    public static final RegistryObject<SoundEvent> SWORD_DRAW = sound("random.sworddraw");
    public static final RegistryObject<SoundEvent> SWORD_SHEATHE = sound("random.swordsheathe");
    public static final RegistryObject<SoundEvent> HYPNOSIS = sound("random.hypnosis");
    public static final RegistryObject<SoundEvent> BOW_CLICK = sound("random.click");
    public static final RegistryObject<SoundEvent> BOW_WIND_UP = sound("random.wind_up");
    public static final RegistryObject<SoundEvent> WITCH_HUNTERS_ARRIVE = sound("random.theycome");
    public static final RegistryObject<SoundEvent> FORECAST_LOVE = sound("random.loved");
    public static final RegistryObject<SoundEvent> WOLFMAN_HOWL = sound("mob.wolfman.howl");
    public static final RegistryObject<SoundEvent> WOLFMAN_EAT = sound("mob.wolfman.eat");
    public static final RegistryObject<SoundEvent> WOLFMAN_SAY = sound("mob.wolfman.say");
    public static final RegistryObject<SoundEvent> WOLFMAN_HIT = sound("mob.wolfman.hit");
    public static final RegistryObject<SoundEvent> WOLFMAN_DEATH = sound("mob.wolfman.death");
    public static final RegistryObject<SoundEvent> WOLFMAN_LORD = sound("mob.wolfman.lord");
    public static final RegistryObject<SoundEvent> HUNT_HORN = sound("random.horn");
    public static final RegistryObject<SoundEvent> MANTRAP = sound("random.mantrap");
    public static final RegistryObject<SoundEvent> OWL_HOOT = sound("mob.owl.owl_hoot");
    public static final RegistryObject<SoundEvent> OWL_HURT = sound("mob.owl.owl_hurt");
    public static final RegistryObject<SoundEvent> MONKEY_AMBIENT = sound("mob.monkey.say");
    public static final RegistryObject<SoundEvent> MONKEY_HIT = sound("mob.monkey.hit");
    public static final RegistryObject<SoundEvent> MONKEY_DEATH = sound("mob.monkey.death");
    public static final RegistryObject<SoundEvent> TREEFYD_AMBIENT = sound("mob.treefyd.treefyd_say");
    public static final RegistryObject<SoundEvent> TOAD_CROAK = sound("mob.toad.toad_croak");
    public static final RegistryObject<SoundEvent> TOAD_HURT = sound("mob.toad.toad_hurt");

    public static final RegistryObject<SoundEvent> SPECTRE_AMBIENT = sound("mob.spectre.spectre_say");
    public static final RegistryObject<SoundEvent> SPECTRE_HIT = sound("mob.spectre.spectre_hit");
    public static final RegistryObject<SoundEvent> SPECTRE_DEATH = sound("mob.spectre.spectre_die");
    public static final RegistryObject<SoundEvent> BANSHEE_SCREAM = sound("mob.banshee.banshee_scream");

    public static void playLegacyPlayerCue(ServerPlayer player, SoundEvent sound, float volume) {
        playLegacyPlayerCue(player, sound, SoundSource.PLAYERS, volume);
    }

    public static void playLegacyPlayerCue(ServerPlayer player, SoundEvent sound, SoundSource source, float volume) {
        var level = player.serverLevel();
        // Modern playback needs a sample seed in addition to the original pitch roll.
        level.playSeededSound(null, player, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                source, volume, legacyPitch(level), ThreadLocalRandom.current().nextLong());
    }

    public static void playLegacyBlockCue(ServerLevel level, BlockPos pos, SoundEvent sound, float volume) {
        playLegacyBlockCue(level, pos, sound, SoundSource.PLAYERS, volume);
    }

    public static void playLegacyBlockCue(ServerLevel level, BlockPos pos, SoundEvent sound, SoundSource source, float volume) {
        // The original world random source only selected the pitch.
        level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), sound, source, volume,
                legacyPitch(level), ThreadLocalRandom.current().nextLong());
    }

    private static float legacyPitch(ServerLevel level) {
        return 0.4F / ((float) level.random.nextDouble() * 0.4F + 0.8F);
    }

    private WitcherySounds() {
    }

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
