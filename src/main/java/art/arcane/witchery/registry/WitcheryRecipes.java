package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.CauldronRitualRecipe;
import art.arcane.witchery.brewing.KettleRecipe;
import art.arcane.witchery.crafting.DistillingRecipe;
import art.arcane.witchery.crafting.SpinningRecipe;
import art.arcane.witchery.ritual.ItemRiteRecipe;
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
    public static final RegistryObject<RecipeType<DistillingRecipe>> DISTILLING =
            TYPES.register("distilling", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Witchery.MODID + ":distilling";
                }
            });
    public static final RegistryObject<RecipeSerializer<DistillingRecipe>> DISTILLING_SERIALIZER =
            SERIALIZERS.register("distilling", DistillingRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<KettleRecipe>> KETTLE =
            TYPES.register("kettle", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Witchery.MODID + ":kettle";
                }
            });
    public static final RegistryObject<RecipeSerializer<KettleRecipe>> KETTLE_SERIALIZER =
            SERIALIZERS.register("kettle", KettleRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<SpinningRecipe>> SPINNING =
            TYPES.register("spinning", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Witchery.MODID + ":spinning";
                }
            });
    public static final RegistryObject<RecipeSerializer<SpinningRecipe>> SPINNING_SERIALIZER =
            SERIALIZERS.register("spinning", SpinningRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<ItemRiteRecipe>> ITEM_RITE =
            TYPES.register("item_rite", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Witchery.MODID + ":item_rite";
                }
            });
    public static final RegistryObject<RecipeSerializer<ItemRiteRecipe>> ITEM_RITE_SERIALIZER =
            SERIALIZERS.register("item_rite", ItemRiteRecipe.Serializer::new);

    private WitcheryRecipes() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        SERIALIZERS.register(bus);
    }
}
