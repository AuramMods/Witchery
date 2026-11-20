package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.vampirism.ParalysisEffect;
import art.arcane.witchery.brewing.GrotesqueEffect;
import art.arcane.witchery.brewing.MortalCoilEffect;
import art.arcane.witchery.brewing.WakingNightmareEffect;
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
    public static final RegistryObject<MobEffect> DEPTHS = MOB_EFFECTS.register("depths",
            () -> new MobEffect(MobEffectCategory.NEUTRAL, 0x172643) {});
    public static final RegistryObject<MobEffect> WAKING_NIGHTMARE =
            MOB_EFFECTS.register("potion.wakingnightmare", WakingNightmareEffect::new);
    public static final RegistryObject<MobEffect> GROTESQUE =
            MOB_EFFECTS.register("potion.grotesque", GrotesqueEffect::new);
    public static final RegistryObject<MobEffect> ENSLAVED = MOB_EFFECTS.register("potion.enslaved",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x4F2756) {});
    public static final RegistryObject<MobEffect> PARALYSIS = MOB_EFFECTS.register("potion.paralysed", ParalysisEffect::new);
    public static final RegistryObject<MobEffect> WOLFSBANE = simpleEffect("potion.wolfsbane", MobEffectCategory.NEUTRAL);
    public static final RegistryObject<MobEffect> ENDER_INHIBITION = simpleEffect("potion.enderinhibition", MobEffectCategory.HARMFUL);
    public static final RegistryObject<MobEffect> BREWING_EXPERTISE = simpleEffect("potion.brewingexpertise", MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> MORTAL_COIL = MOB_EFFECTS.register("potion.mortalcoil", MortalCoilEffect::new);
    public static final RegistryObject<MobEffect> QUEASY = MOB_EFFECTS.register("potion.queasy",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x7C9348) {
                @Override
                public java.util.List<net.minecraft.world.item.ItemStack> getCurativeItems() {
                    return java.util.List.of();
                }
            });

    static {
        LEGACY_EFFECTS.put("potion.wakingnightmare", WAKING_NIGHTMARE);
        LEGACY_EFFECTS.put("potion.grotesque", GROTESQUE);
        LEGACY_EFFECTS.put("potion.enslaved", ENSLAVED);
        LEGACY_EFFECTS.put("potion.paralysed", PARALYSIS);
        LEGACY_EFFECTS.put("potion.wolfsbane", WOLFSBANE);
        LEGACY_EFFECTS.put("potion.enderinhibition", ENDER_INHIBITION);
        LEGACY_EFFECTS.put("potion.brewingexpertise", BREWING_EXPERTISE);
        LEGACY_EFFECTS.put("potion.mortalcoil", MORTAL_COIL);
        LEGACY_EFFECTS.put("potion.queasy", QUEASY);
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

    private static RegistryObject<MobEffect> simpleEffect(String path, MobEffectCategory category) {
        int color = (Witchery.MODID + ":" + path).hashCode() & 0xFFFFFF;
        return MOB_EFFECTS.register(path, () -> new MobEffect(category, color) {});
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
