package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.menu.LegacyPlaceholderMenu;
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
    public static final Map<String, RegistryObject<MenuType<LegacyPlaceholderMenu>>> LEGACY_MENUS = new LinkedHashMap<>();
    public static final Map<Integer, RegistryObject<MenuType<LegacyPlaceholderMenu>>> LEGACY_MENUS_BY_GUI_ID = new LinkedHashMap<>();

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

    public static Map<String, RegistryObject<MenuType<LegacyPlaceholderMenu>>> all() {
        return Collections.unmodifiableMap(LEGACY_MENUS);
    }

    public static RegistryObject<MenuType<LegacyPlaceholderMenu>> getRequired(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        RegistryObject<MenuType<LegacyPlaceholderMenu>> menu = LEGACY_MENUS.get(path);
        if (menu == null) {
            throw new IllegalStateException("Missing legacy menu placeholder for path: " + path);
        }
        return menu;
    }

    public static RegistryObject<MenuType<LegacyPlaceholderMenu>> getRequiredByGuiId(int legacyGuiId) {
        RegistryObject<MenuType<LegacyPlaceholderMenu>> menu = LEGACY_MENUS_BY_GUI_ID.get(legacyGuiId);
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

        RegistryObject<MenuType<LegacyPlaceholderMenu>> menu = MENU_TYPES.register(path,
                () -> IForgeMenuType.create((windowId, inventory, buffer) -> new LegacyPlaceholderMenu(windowId, inventory, path)));

        LEGACY_MENUS.put(path, menu);
        LEGACY_MENUS_BY_GUI_ID.put(menuIntent.legacyGuiId(), menu);
    }
}
