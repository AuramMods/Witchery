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
