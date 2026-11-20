package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.CauldronRitualRecipe;
import art.arcane.witchery.brewing.KettleRecipe;
import art.arcane.witchery.crafting.BoundTaglockIngredient;
import art.arcane.witchery.crafting.ClothingRepairRecipe;
import art.arcane.witchery.crafting.DistillingRecipe;
import art.arcane.witchery.crafting.SpinningRecipe;
import art.arcane.witchery.crafting.TaglockBindingRecipe;
import art.arcane.witchery.ritual.ItemRiteRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegisterEvent;
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
    public static final RegistryObject<RecipeSerializer<ClothingRepairRecipe>> CLOTHING_REPAIR =
            SERIALIZERS.register("repair_witch_clothing", () -> new SimpleCraftingRecipeSerializer<>(ClothingRepairRecipe::new));
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> SIGN_OWNERSHIP_CONTRACT =
            SERIALIZERS.register("sign_ownership_contract", () -> new SimpleCraftingRecipeSerializer<>(
                    (id, category) -> new TaglockBindingRecipe(id, category, TaglockBindingRecipe.Binding.OWNERSHIP)));
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> BIND_FIRE_POPPET =
            SERIALIZERS.register("bind_fire_protection_poppet", () -> new SimpleCraftingRecipeSerializer<>(
                    (id, category) -> new TaglockBindingRecipe(id, category, TaglockBindingRecipe.Binding.FIRE_POPPET)));
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> BIND_EARTH_POPPET =
            registerWardBinding("earth", TaglockBindingRecipe.Binding.EARTH_POPPET);
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> BIND_WATER_POPPET =
            registerWardBinding("water", TaglockBindingRecipe.Binding.WATER_POPPET);
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> BIND_HUNGER_POPPET =
            registerWardBinding("hunger", TaglockBindingRecipe.Binding.HUNGER_POPPET);
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> BIND_TOOL_POPPET =
            registerWardBinding("tool", TaglockBindingRecipe.Binding.TOOL_POPPET);
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> BIND_ARMOR_POPPET =
            registerWardBinding("armor", TaglockBindingRecipe.Binding.ARMOR_POPPET);
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> BIND_DEATH_POPPET =
            SERIALIZERS.register("bind_death_protection_poppet", () -> new SimpleCraftingRecipeSerializer<>(
                    (id, category) -> new TaglockBindingRecipe(id, category, TaglockBindingRecipe.Binding.DEATH_POPPET)));
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> BIND_VOODOO_POPPET =
            SERIALIZERS.register("bind_voodoo_poppet", () -> new SimpleCraftingRecipeSerializer<>(
                    (id, category) -> new TaglockBindingRecipe(id, category, TaglockBindingRecipe.Binding.VOODOO_POPPET)));
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> BIND_VOODOO_PROTECTION =
            SERIALIZERS.register("bind_voodoo_protection_poppet", () -> new SimpleCraftingRecipeSerializer<>(
                    (id, category) -> new TaglockBindingRecipe(id, category, TaglockBindingRecipe.Binding.VOODOO_PROTECTION)));
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> BIND_VAMPIRIC_POPPET =
            SERIALIZERS.register("bind_vampiric_poppet", () -> new SimpleCraftingRecipeSerializer<>(
                    (id, category) -> new TaglockBindingRecipe(id, category, TaglockBindingRecipe.Binding.VAMPIRIC_POPPET)));
    public static final RegistryObject<RecipeSerializer<TaglockBindingRecipe>> BIND_POPPET_PROTECTION =
            SERIALIZERS.register("bind_poppet_protection", () -> new SimpleCraftingRecipeSerializer<>(
                    (id, category) -> new TaglockBindingRecipe(id, category, TaglockBindingRecipe.Binding.POPPET_PROTECTION)));

    private WitcheryRecipes() {
    }

    private static RegistryObject<RecipeSerializer<TaglockBindingRecipe>> registerWardBinding(String name, TaglockBindingRecipe.Binding binding) {
        return SERIALIZERS.register("bind_" + name + "_protection_poppet", () -> new SimpleCraftingRecipeSerializer<>(
                (id, category) -> new TaglockBindingRecipe(id, category, binding)));
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        SERIALIZERS.register(bus);
        bus.addListener(WitcheryRecipes::registerIngredients);
    }

    private static void registerIngredients(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
            CraftingHelper.register(BoundTaglockIngredient.ID, BoundTaglockIngredient.SERIALIZER);
        }
    }
}
