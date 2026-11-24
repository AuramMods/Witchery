package art.arcane.witchery.integration.jei;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.block.entity.WitchesOvenBlockEntity;
import art.arcane.witchery.integration.ForestrySaplings;
import art.arcane.witchery.config.WitcheryClientConfig;
import art.arcane.witchery.config.WitcheryCommonConfig;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static art.arcane.witchery.integration.jei.MachineRecipeCategory.Entry;
import static art.arcane.witchery.integration.jei.MachineRecipeCategory.Input;

@JeiPlugin
public final class WitcheryJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "recipes");
    private static final String TEXT = "jei.witchery.";
    private static final Machine OVEN = new Machine("oven", "witchesovenidle", 1);
    private static final Machine DISTILLERY = new Machine("distilling", "distilleryidle", 2);
    private static final Machine KETTLE = new Machine("kettle", "kettle", 2);
    private static final Machine CAULDRON = new Machine("cauldron", "cauldron", 5);
    private static final Machine WHEEL = new Machine("spinning", "spinningwheel", 1);
    private static final List<Machine> MACHINES = List.of(OVEN, DISTILLERY, KETTLE, CAULDRON, WHEEL);

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        if (!enabled()) return;
        for (String block : List.of("witchsapling", "witchleaves", "witchlog", "witchwood", "witchwoodslab", "witchwooddoubleslab")) {
            registration.registerSubtypeInterpreter(WitcheryBlocks.getRequired(block).get().asItem(),
                    (stack, context) -> WitchWoodType.fromStack(stack).getSerializedName());
        }
        DynamicCraftingLayout.registerSubtypes(registration);
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        if (!enabled()) return;
        DynamicCraftingLayout.register(registration.getCraftingCategory());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (!enabled()) return;
        for (Machine machine : MACHINES) {
            registration.addRecipeCategories(new MachineRecipeCategory(
                    machine.type(), machine.stack(), machine.rows(), registration.getJeiHelpers().getGuiHelper()));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (!enabled()) return;
        for (Machine machine : MACHINES) registration.addRecipeCatalyst(machine.stack(), machine.type());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (!enabled()) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        var manager = level.getRecipeManager();
        var registries = level.registryAccess();

        registration.addRecipes(OVEN.type(), manager.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMELTING)
                .stream().flatMap(recipe -> ovenRecipes(recipe, level).stream()).toList());
        registration.addRecipes(DISTILLERY.type(), manager.getAllRecipesFor(WitcheryRecipes.DISTILLING.get())
                .stream().map(recipe -> {
                    var inputs = inputs(recipe.getIngredients());
                    if (recipe.jars() > 0) inputs.add(input(new ItemStack(WitcheryIngredient.CLAY_JAR.item(), recipe.jars())));
                    return new Entry(recipe.id(), inputs, recipe.outputs(), List.of());
                }).toList());
        registration.addRecipes(WHEEL.type(), manager.getAllRecipesFor(WitcheryRecipes.SPINNING.get())
                .stream().map(recipe -> {
                    var inputs = inputs(recipe.modifiers());
                    inputs.add(0, new Input(Arrays.stream(recipe.fibre().getItems())
                            .map(stack -> stack.copyWithCount(recipe.fibreCount())).toList(), null));
                    return new Entry(recipe.id(), inputs, List.of(recipe.result()), List.of());
                }).toList());
        registration.addRecipes(KETTLE.type(), manager.getAllRecipesFor(WitcheryRecipes.KETTLE.get())
                .stream().map(recipe -> {
                    var inputs = inputs(recipe.ingredients());
                    inputs.add(input(new ItemStack(Items.GLASS_BOTTLE)));
                    var details = new ArrayList<Component>();
                    details.add(Component.translatable(TEXT + "heat_kettle"));
                    if (recipe.power() > 0) details.add(Component.translatable(TEXT + "power", recipe.power()));
                    if (recipe.dimension() != null) details.add(Component.translatable(TEXT + "dimension",
                            Component.translatableWithFallback(recipe.dimension().toLanguageKey("dimension"),
                                    recipe.dimension().toString())));
                    if (recipe.bottlingFamiliar() != null) details.add(Component.translatable(TEXT + "familiar",
                            recipe.bottlingFamiliar().entityType().getDescription()));
                    return new Entry(recipe.id(), inputs, List.of(recipe.getResultItem(registries)), details);
                }).toList());
        registration.addRecipes(CAULDRON.type(), manager.getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get())
                .stream().map(recipe -> {
                    var inputs = inputs(recipe.ingredients());
                    inputs.add(input(recipe.trigger(), Component.translatable(TEXT + "final_offering")));
                    var details = new ArrayList<Component>();
                    details.add(Component.translatable(TEXT + "heat_cauldron"));
                    if (recipe.power() > 0) details.add(Component.translatable(TEXT + "base_power", recipe.power()));
                    if (recipe.summon() != null) details.add(Component.translatable(TEXT + "summons", recipe.summon().getDescription()));
                    return new Entry(recipe.id(), inputs, List.of(recipe.result()), details);
                }).toList());
    }

    private static List<Entry> ovenRecipes(SmeltingRecipe recipe, ClientLevel level) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        if (!WitchesOvenBlockEntity.isOvenResult(result)) return List.of();
        Map<WitcheryIngredient, List<ItemStack>> groups = new LinkedHashMap<>();
        var sapling = WitcheryBlocks.getRequired("witchsapling").get();
        for (Ingredient ingredient : recipe.getIngredients()) {
            for (ItemStack stack : ingredient.getItems()) {
                if (stack.is(sapling.asItem())) {
                    for (WitchWoodType wood : WitchWoodType.values()) {
                        ItemStack variant = wood.stack(sapling);
                        if (ingredient.test(variant)) addOvenInput(groups, variant);
                    }
                } else if (ForestrySaplings.isSapling(stack)) {
                    for (ItemStack variant : ForestrySaplings.variants()) {
                        if (ingredient.test(variant)) addOvenInput(groups, variant);
                    }
                } else {
                    addOvenInput(groups, stack);
                }
            }
        }
        var entries = new ArrayList<Entry>();
        groups.forEach((fume, stacks) -> {
            var id = recipe.getId();
            if (fume == null) {
                entries.add(new Entry(id, List.of(new Input(stacks, null)), List.of(result), List.of()));
            } else {
                var jar = new Input(List.of(new ItemStack(WitcheryIngredient.CLAY_JAR.item())),
                        Component.translatable(TEXT + "jar"));
                var chance = Component.translatable(TEXT + "fume_chance",
                        (int) (100 * WitchesOvenBlockEntity.BASE_FUME_CHANCE));
                var fumeId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                        id.getPath() + "/" + fume.name().toLowerCase(Locale.ROOT));
                entries.add(new Entry(fumeId, List.of(new Input(stacks, null), jar),
                        List.of(result, new ItemStack(fume.item())), List.of(chance)));
            }
        });
        return entries;
    }

    private static boolean enabled() {
        return WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get() && WitcheryClientConfig.ALLOW_MOD_JEI.get();
    }

    private static void addOvenInput(Map<WitcheryIngredient, List<ItemStack>> groups, ItemStack stack) {
        var group = groups.computeIfAbsent(WitchesOvenBlockEntity.fumeFor(stack), unused -> new ArrayList<>());
        if (group.stream().noneMatch(existing -> ItemStack.isSameItemSameTags(existing, stack))) group.add(stack);
    }

    private static ArrayList<Input> inputs(List<Ingredient> ingredients) {
        var inputs = new ArrayList<Input>();
        ingredients.forEach(ingredient -> {
            if (!ingredient.isEmpty()) inputs.add(input(ingredient, null));
        });
        return inputs;
    }

    private static Input input(Ingredient ingredient, @Nullable Component tooltip) {
        return new Input(List.of(ingredient.getItems()), tooltip);
    }

    private static Input input(ItemStack stack) {
        return new Input(List.of(stack), null);
    }

    private record Machine(RecipeType<Entry> type, String block, int rows) {
        private Machine(String name, String block, int rows) {
            this(RecipeType.create(Witchery.MODID, name, Entry.class), block, rows);
        }

        private ItemStack stack() {
            return new ItemStack(WitcheryBlocks.getRequired(block).get());
        }
    }
}
