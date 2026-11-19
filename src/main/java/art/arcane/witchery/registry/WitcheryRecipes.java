package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.CauldronRitualRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class WitcheryRecipes {
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Witchery.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Witchery.MODID);
    public static final RegistryObject<RecipeType<CauldronRitualRecipe>> CAULDRON_RITUAL =
            TYPES.register("cauldron_ritual", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Witchery.MODID + ":cauldron_ritual";
                }
            });
    public static final RegistryObject<RecipeSerializer<CauldronRitualRecipe>> CAULDRON_RITUAL_SERIALIZER =
            SERIALIZERS.register("cauldron_ritual", CauldronRitualRecipe.Serializer::new);

    private WitcheryRecipes() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        SERIALIZERS.register(bus);
    }
}
