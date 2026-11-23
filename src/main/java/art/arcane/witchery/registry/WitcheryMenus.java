package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.menu.AltarMenu;
import art.arcane.witchery.menu.BrewBagMenu;
import art.arcane.witchery.menu.DistilleryMenu;
import art.arcane.witchery.menu.LeonardsUrnMenu;
import art.arcane.witchery.menu.SpinningWheelMenu;
import art.arcane.witchery.menu.WitchesOvenMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class WitcheryMenus {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Witchery.MODID);
    public static final RegistryObject<MenuType<WitchesOvenMenu>> WITCHES_OVEN = MENU_TYPES.register("witches_oven",
            () -> new MenuType<>(WitchesOvenMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<AltarMenu>> ALTAR = MENU_TYPES.register("altar",
            () -> new MenuType<>(AltarMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<DistilleryMenu>> DISTILLERY = MENU_TYPES.register("distillery",
            () -> new MenuType<>(DistilleryMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<LeonardsUrnMenu>> LEONARDS_URN = MENU_TYPES.register("leonards_urn",
            () -> IForgeMenuType.create(LeonardsUrnMenu::new));
    public static final RegistryObject<MenuType<BrewBagMenu>> BREW_BAG = MENU_TYPES.register("brew_bag",
            () -> IForgeMenuType.create(BrewBagMenu::new));
    public static final RegistryObject<MenuType<SpinningWheelMenu>> SPINNING_WHEEL = MENU_TYPES.register("spinning_wheel",
            () -> new MenuType<>(SpinningWheelMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private WitcheryMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
