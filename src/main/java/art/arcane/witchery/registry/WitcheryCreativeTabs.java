package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class WitcheryCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Witchery.MODID);

    public static final RegistryObject<CreativeModeTab> WITCHERY_TAB = CREATIVE_MODE_TABS.register("witchery_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Witchery"))
                    .icon(() -> {
                        RegistryObject<net.minecraft.world.item.Item> iconItem = WitcheryItems.LEGACY_ITEMS.values().iterator().next();
                        return new ItemStack(iconItem.get());
                    })
                    .displayItems((parameters, output) -> WitcheryItems.allForCreativeTab().forEach(item -> output.accept(item.get())))
                    .build());

    private WitcheryCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
