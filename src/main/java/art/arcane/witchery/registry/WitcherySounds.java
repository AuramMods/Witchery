package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class WitcherySounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Witchery.MODID);
    public static final RegistryObject<SoundEvent> CHALK = sound("random.chalk");
    public static final RegistryObject<SoundEvent> HEARTBEAT = sound("random.heartbeat");
    public static final RegistryObject<SoundEvent> NIGHTMARE_LIVE = sound("mob.nightmare.nightmare_live");
    public static final RegistryObject<SoundEvent> NIGHTMARE_HIT = sound("mob.nightmare.nightmare_hit");
    public static final RegistryObject<SoundEvent> NIGHTMARE_DEAD = sound("mob.nightmare.nightmare_dead");
    public static final RegistryObject<SoundEvent> TORMENT_LAUGH = sound("mob.torment.laugh");
    public static final RegistryObject<SoundEvent> TORMENT_HIT = sound("mob.torment.hit");
    public static final RegistryObject<SoundEvent> TORMENT_DEATH = sound("mob.torment.death");
    public static final RegistryObject<SoundEvent> COVEN_TALK = sound("mob.baba.baba_living");
    public static final RegistryObject<SoundEvent> COVEN_CACKLE = sound("mob.baba.baba_death");
    public static final RegistryObject<SoundEvent> IMP_LAUGH = sound("mob.imp.laugh");
    public static final RegistryObject<SoundEvent> IMP_HURT = sound("mob.imp.hit");
    public static final RegistryObject<SoundEvent> IMP_DEATH = sound("mob.imp.death");
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
    public static final RegistryObject<SoundEvent> WOLFMAN_HOWL = sound("mob.wolfman.howl");
    public static final RegistryObject<SoundEvent> OWL_HOOT = sound("mob.owl.owl_hoot");
    public static final RegistryObject<SoundEvent> OWL_HURT = sound("mob.owl.owl_hurt");
    public static final RegistryObject<SoundEvent> TOAD_CROAK = sound("mob.toad.toad_croak");
    public static final RegistryObject<SoundEvent> TOAD_HURT = sound("mob.toad.toad_hurt");

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
