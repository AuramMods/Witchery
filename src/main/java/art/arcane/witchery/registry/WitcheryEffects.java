package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WitcheryEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Witchery.MODID);
    public static final Map<String, RegistryObject<MobEffect>> LEGACY_EFFECTS = new LinkedHashMap<>();

    static {
        for (String legacyName : LegacyRegistryData.EFFECTS) {
            registerLegacyEffect(legacyName);
        }
    }

    private WitcheryEffects() {
    }

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

    public static Map<String, RegistryObject<MobEffect>> all() {
        return Collections.unmodifiableMap(LEGACY_EFFECTS);
    }

    private static void registerLegacyEffect(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        if (LEGACY_EFFECTS.containsKey(path)) {
            return;
        }

        RegistryObject<MobEffect> effect = MOB_EFFECTS.register(path,
                () -> new MobEffect(MobEffectCategory.NEUTRAL, 0x6F6F6F) {
                });

        LEGACY_EFFECTS.put(path, effect);
    }
}
