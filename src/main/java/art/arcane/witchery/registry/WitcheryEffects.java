package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.ChilledEffect;
import art.arcane.witchery.vampirism.ParalysisEffect;
import art.arcane.witchery.brewing.GrotesqueEffect;
import art.arcane.witchery.brewing.MortalCoilEffect;
import art.arcane.witchery.brewing.EnvironmentalCurseEffect;
import art.arcane.witchery.brewing.FoodBrewEffect;
import art.arcane.witchery.brewing.IllFittingEffect;
import art.arcane.witchery.brewing.LightAllergyEffect;
import art.arcane.witchery.brewing.MovementBrewEffect;
import art.arcane.witchery.brewing.ProjectileBrewEffect;
import art.arcane.witchery.brewing.SnowTrailEffect;
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
    public static final RegistryObject<MobEffect> CHILLED = MOB_EFFECTS.register("potion.chilled", ChilledEffect::new);
    public static final RegistryObject<MobEffect> WOLFSBANE = simpleEffect("potion.wolfsbane", MobEffectCategory.NEUTRAL);
    public static final RegistryObject<MobEffect> ENDER_INHIBITION = simpleEffect("potion.enderinhibition", MobEffectCategory.HARMFUL);
    public static final RegistryObject<MobEffect> BREWING_EXPERTISE = simpleEffect("potion.brewingexpertise", MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> WORSHIP = simpleEffect("potion.worship", MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> GAS_MASK = MOB_EFFECTS.register("potion.gasmask",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, legacyColor("potion.gasmask")) {
                @Override
                public java.util.List<net.minecraft.world.item.ItemStack> getCurativeItems() {
                    return java.util.List.of();
                }
            });
    public static final RegistryObject<MobEffect> SINKING = environmentalCurse("potion.sinking", EnvironmentalCurseEffect.Kind.SINKING);
    public static final RegistryObject<MobEffect> OVERHEATING = environmentalCurse("potion.overheating", EnvironmentalCurseEffect.Kind.OVERHEATING);
    public static final RegistryObject<MobEffect> INSANITY = environmentalCurse("potion.insane", EnvironmentalCurseEffect.Kind.INSANITY);
    public static final RegistryObject<MobEffect> RESIZING = simpleEffect("potion.resizing", MobEffectCategory.NEUTRAL);
    public static final RegistryObject<MobEffect> KEEP_INVENTORY = simpleEffect("potion.keepinventory", MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> KEEP_EFFECTS = simpleEffect("potion.keepeffects", MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> REINCARNATION = simpleEffect("potion.reincarnate", MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> ILL_FITTING = MOB_EFFECTS.register("potion.illfitting",
            () -> new IllFittingEffect(legacyColor("potion.illfitting")));
    public static final RegistryObject<MobEffect> ABSORB_MAGIC = simpleEffect("potion.absorbmagic", MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> REFLECT_DAMAGE = simpleEffect("potion.reflectdamage", MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> REPEL_ATTACKER = simpleEffect("potion.repellattacker", MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> POISON_WEAPONS = simpleEffect("potion.poisonweapons", MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> STOUT_BELLY = MOB_EFFECTS.register("potion.stoutbelly",
            () -> new FoodBrewEffect(FoodBrewEffect.Kind.STOUT_BELLY, legacyColor("potion.stoutbelly")));
    public static final RegistryObject<MobEffect> FEEL_NO_PAIN = MOB_EFFECTS.register("potion.feelnopain",
            () -> new FoodBrewEffect(FoodBrewEffect.Kind.MOONSHINE, legacyColor("potion.feelnopain")));
    public static final RegistryObject<MobEffect> FLOATING = movementBrew("potion.floating", MovementBrewEffect.Kind.FLOATING);
    public static final RegistryObject<MobEffect> FEATHER_FALL = movementBrew("potion.featherfall", MovementBrewEffect.Kind.FEATHER_FALL);
    public static final RegistryObject<MobEffect> SWIMMING = movementBrew("potion.swimming", MovementBrewEffect.Kind.SWIMMING);
    public static final RegistryObject<MobEffect> DOUBLE_JUMP = simpleEffect("potion.doublejump", MobEffectCategory.BENEFICIAL);
    public static final RegistryObject<MobEffect> REFLECT_PROJECTILES = MOB_EFFECTS.register("potion.reflectprojectiles",
            () -> new ProjectileBrewEffect(false, legacyColor("potion.reflectprojectiles")));
    public static final RegistryObject<MobEffect> ATTRACT_PROJECTILES = MOB_EFFECTS.register("potion.attractprojectiles",
            () -> new ProjectileBrewEffect(true, legacyColor("potion.attractprojectiles")));
    public static final RegistryObject<MobEffect> SUN_ALLERGY = MOB_EFFECTS.register("potion.allergysun",
            () -> new LightAllergyEffect(true, legacyColor("potion.allergysun")));
    public static final RegistryObject<MobEffect> DARKNESS_ALLERGY = MOB_EFFECTS.register("potion.allergydark",
            () -> new LightAllergyEffect(false, legacyColor("potion.allergydark")));
    public static final RegistryObject<MobEffect> SNOW_TRAIL = MOB_EFFECTS.register("potion.snowtrail",
            () -> new SnowTrailEffect(legacyColor("potion.snowtrail")));
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
        LEGACY_EFFECTS.put("potion.chilled", CHILLED);
        LEGACY_EFFECTS.put("potion.wolfsbane", WOLFSBANE);
        LEGACY_EFFECTS.put("potion.enderinhibition", ENDER_INHIBITION);
        LEGACY_EFFECTS.put("potion.brewingexpertise", BREWING_EXPERTISE);
        LEGACY_EFFECTS.put("potion.worship", WORSHIP);
        LEGACY_EFFECTS.put("potion.gasmask", GAS_MASK);
        LEGACY_EFFECTS.put("potion.mortalcoil", MORTAL_COIL);
        LEGACY_EFFECTS.put("potion.sinking", SINKING);
        LEGACY_EFFECTS.put("potion.overheating", OVERHEATING);
        LEGACY_EFFECTS.put("potion.insane", INSANITY);
        LEGACY_EFFECTS.put("potion.resizing", RESIZING);
        LEGACY_EFFECTS.put("potion.keepinventory", KEEP_INVENTORY);
        LEGACY_EFFECTS.put("potion.keepeffects", KEEP_EFFECTS);
        LEGACY_EFFECTS.put("potion.reincarnate", REINCARNATION);
        LEGACY_EFFECTS.put("potion.illfitting", ILL_FITTING);
        LEGACY_EFFECTS.put("potion.absorbmagic", ABSORB_MAGIC);
        LEGACY_EFFECTS.put("potion.reflectdamage", REFLECT_DAMAGE);
        LEGACY_EFFECTS.put("potion.repellattacker", REPEL_ATTACKER);
        LEGACY_EFFECTS.put("potion.poisonweapons", POISON_WEAPONS);
        LEGACY_EFFECTS.put("potion.stoutbelly", STOUT_BELLY);
        LEGACY_EFFECTS.put("potion.feelnopain", FEEL_NO_PAIN);
        LEGACY_EFFECTS.put("potion.floating", FLOATING);
        LEGACY_EFFECTS.put("potion.featherfall", FEATHER_FALL);
        LEGACY_EFFECTS.put("potion.swimming", SWIMMING);
        LEGACY_EFFECTS.put("potion.doublejump", DOUBLE_JUMP);
        LEGACY_EFFECTS.put("potion.reflectprojectiles", REFLECT_PROJECTILES);
        LEGACY_EFFECTS.put("potion.attractprojectiles", ATTRACT_PROJECTILES);
        LEGACY_EFFECTS.put("potion.allergysun", SUN_ALLERGY);
        LEGACY_EFFECTS.put("potion.allergydark", DARKNESS_ALLERGY);
        LEGACY_EFFECTS.put("potion.snowtrail", SNOW_TRAIL);
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

    private static RegistryObject<MobEffect> environmentalCurse(String path, EnvironmentalCurseEffect.Kind kind) {
        return MOB_EFFECTS.register(path, () -> new EnvironmentalCurseEffect(kind));
    }

    private static RegistryObject<MobEffect> simpleEffect(String path, MobEffectCategory category) {
        return MOB_EFFECTS.register(path, () -> new MobEffect(category, legacyColor(path)) {});
    }

    private static int legacyColor(String path) {
        return (Witchery.MODID + ":" + path).hashCode() & 0xFFFFFF;
    }

    private static RegistryObject<MobEffect> movementBrew(String path, MovementBrewEffect.Kind kind) {
        return MOB_EFFECTS.register(path, () -> new MovementBrewEffect(kind, legacyColor(path)));
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
