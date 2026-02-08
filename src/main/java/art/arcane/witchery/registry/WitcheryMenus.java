package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.world.inventory.ChestMenu;
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
    public static final Map<String, RegistryObject<MenuType<ChestMenu>>> LEGACY_MENUS = new LinkedHashMap<>();

    static {
        for (String legacyName : LegacyRegistryData.MENUS) {
            registerLegacyMenu(legacyName);
        }
    }

    private WitcheryMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

    public static Map<String, RegistryObject<MenuType<ChestMenu>>> all() {
        return Collections.unmodifiableMap(LEGACY_MENUS);
    }

    private static void registerLegacyMenu(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        if (LEGACY_MENUS.containsKey(path)) {
            return;
        }

        RegistryObject<MenuType<ChestMenu>> menu = MENU_TYPES.register(path,
                () -> IForgeMenuType.create((windowId, inventory, buffer) -> ChestMenu.threeRows(windowId, inventory)));

        LEGACY_MENUS.put(path, menu);
    }
}
