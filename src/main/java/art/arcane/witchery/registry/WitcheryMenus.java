package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.menu.AltarMenu;
import art.arcane.witchery.menu.DistilleryMenu;
import art.arcane.witchery.menu.LegacyPlaceholderMenu;
import art.arcane.witchery.menu.SpinningWheelMenu;
import art.arcane.witchery.menu.WitchesOvenMenu;
import art.arcane.witchery.menu.LeonardsUrnMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WitcheryMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Witchery.MODID);
    public static final Map<String, RegistryObject<? extends MenuType<?>>> LEGACY_MENUS = new LinkedHashMap<>();
    public static final Map<Integer, RegistryObject<? extends MenuType<?>>> LEGACY_MENUS_BY_GUI_ID = new LinkedHashMap<>();
    private static final Map<String, RegistryObject<MenuType<LegacyPlaceholderMenu>>> PLACEHOLDER_MENUS = new LinkedHashMap<>();
    public static final RegistryObject<MenuType<WitchesOvenMenu>> WITCHES_OVEN = MENU_TYPES.register("witches_oven",
            () -> new MenuType<>(WitchesOvenMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<AltarMenu>> ALTAR = MENU_TYPES.register("altar",
            () -> new MenuType<>(AltarMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<DistilleryMenu>> DISTILLERY = MENU_TYPES.register("distillery",
            () -> new MenuType<>(DistilleryMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<LeonardsUrnMenu>> LEONARDS_URN = MENU_TYPES.register("leonards_urn",
            () -> IForgeMenuType.create(LeonardsUrnMenu::new));
    public static final RegistryObject<MenuType<SpinningWheelMenu>> SPINNING_WHEEL = MENU_TYPES.register("spinning_wheel",
            () -> new MenuType<>(SpinningWheelMenu::new, FeatureFlags.DEFAULT_FLAGS));

    static {
        for (LegacyRegistryData.LegacyMenuIntent menuIntent : LegacyRegistryData.MENUS) {
            registerLegacyMenu(menuIntent);
        }
    }

    private WitcheryMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

    public static Map<String, RegistryObject<? extends MenuType<?>>> all() {
        return Collections.unmodifiableMap(LEGACY_MENUS);
    }

    public static Map<String, RegistryObject<MenuType<LegacyPlaceholderMenu>>> placeholders() {
        return Collections.unmodifiableMap(PLACEHOLDER_MENUS);
    }

    public static RegistryObject<? extends MenuType<?>> getRequired(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        RegistryObject<? extends MenuType<?>> menu = LEGACY_MENUS.get(path);
        if (menu == null) {
            throw new IllegalStateException("Missing legacy menu placeholder for path: " + path);
        }
        return menu;
    }

    public static RegistryObject<? extends MenuType<?>> getRequiredByGuiId(int legacyGuiId) {
        RegistryObject<? extends MenuType<?>> menu = LEGACY_MENUS_BY_GUI_ID.get(legacyGuiId);
        if (menu == null) {
            throw new IllegalStateException("Missing legacy menu placeholder for GUI id: " + legacyGuiId);
        }
        return menu;
    }

    private static void registerLegacyMenu(LegacyRegistryData.LegacyMenuIntent menuIntent) {
        String path = RegistryNameUtil.normalizePath(menuIntent.key());
        if (LEGACY_MENUS.containsKey(path)) {
            return;
        }

        if ("witches_oven".equals(path)) {
            LEGACY_MENUS.put(path, WITCHES_OVEN);
            LEGACY_MENUS_BY_GUI_ID.put(menuIntent.legacyGuiId(), WITCHES_OVEN);
            return;
        }
        if ("altar".equals(path)) {
            LEGACY_MENUS.put(path, ALTAR);
            LEGACY_MENUS_BY_GUI_ID.put(menuIntent.legacyGuiId(), ALTAR);
            return;
        }
        if ("spinning_wheel".equals(path)) {
            LEGACY_MENUS.put(path, SPINNING_WHEEL);
            LEGACY_MENUS_BY_GUI_ID.put(menuIntent.legacyGuiId(), SPINNING_WHEEL);
            return;
        }
        if ("distillery".equals(path)) {
            LEGACY_MENUS.put(path, DISTILLERY);
            LEGACY_MENUS_BY_GUI_ID.put(menuIntent.legacyGuiId(), DISTILLERY);
            return;
        }

        if ("leonards_urn".equals(path)) {
            LEGACY_MENUS.put(path, LEONARDS_URN);
            LEGACY_MENUS_BY_GUI_ID.put(menuIntent.legacyGuiId(), LEONARDS_URN);
            return;
        }
        RegistryObject<MenuType<LegacyPlaceholderMenu>> menu = MENU_TYPES.register(path,
                () -> IForgeMenuType.create((windowId, inventory, buffer) -> new LegacyPlaceholderMenu(windowId, inventory, path)));

        PLACEHOLDER_MENUS.put(path, menu);
        LEGACY_MENUS.put(path, menu);
        LEGACY_MENUS_BY_GUI_ID.put(menuIntent.legacyGuiId(), menu);
    }
}
