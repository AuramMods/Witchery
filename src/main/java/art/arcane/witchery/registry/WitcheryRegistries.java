package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.data.GrassSeedLootModifier;
import art.arcane.witchery.world.DreamCottonFeature;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class WitcheryRegistries {
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Witchery.MODID);
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Witchery.MODID);

    static {
        LOOT_MODIFIERS.register("grass_seeds", () -> GrassSeedLootModifier.CODEC);
        FEATURES.register("dream_cotton", DreamCottonFeature::new);
    }

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
        WitcheryRecipes.register(eventBus);
        WitcherySounds.register(eventBus);
        LOOT_MODIFIERS.register(eventBus);
        FEATURES.register(eventBus);
    }
}
