package art.arcane.witchery.registry;

import net.minecraftforge.eventbus.api.IEventBus;

public final class WitcheryRegistries {
    private WitcheryRegistries() {
    }

    public static void register(IEventBus eventBus) {
        WitcheryBlocks.register(eventBus);
        WitcheryItems.register(eventBus);
        WitcheryFluids.register(eventBus);
        WitcheryBlockEntities.register(eventBus);
        WitcheryEntities.register(eventBus);
        WitcheryMenus.register(eventBus);
        WitcheryEffects.register(eventBus);
        WitcheryCreativeTabs.register(eventBus);
    }
}
