package art.arcane.witchery.integration.jei;

import art.arcane.witchery.brewing.LousePotion;
import art.arcane.witchery.crafting.AssembleVampireBookRecipe;
import art.arcane.witchery.crafting.ClothingRepairRecipe;
import art.arcane.witchery.crafting.CopyBiomeNoteRecipe;
import art.arcane.witchery.crafting.DoorKeyringRecipe;
import art.arcane.witchery.crafting.DyeBrewBagRecipe;
import art.arcane.witchery.crafting.PotionChargeRecipe;
import art.arcane.witchery.crafting.TaglockBindingRecipe;
import art.arcane.witchery.crafting.UrnUpgradeRecipe;
import art.arcane.witchery.item.BiomeBookIndex;
import art.arcane.witchery.item.BiomeNoteItem;
import art.arcane.witchery.item.BrewBagItem;
import art.arcane.witchery.item.DoorKeyItem;
import art.arcane.witchery.item.LeonardsUrnItem;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.VampireBookItem;
import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter.NONE;

final class DynamicCraftingLayout implements ICraftingCategoryExtension {
    private static final AbstractContainerMenu PREVIEW_MENU = new AbstractContainerMenu(MenuType.CRAFTING, 0) {
        @Override
        public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

        @Override
        public boolean stillValid(Player player) { return false; }
    };

    private final ResourceLocation id;
    private final List<Example> examples;

    private DynamicCraftingLayout(ResourceLocation id, List<Example> examples) {
        this.id = id;
        this.examples = examples;
    }

    static void register(IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> category) {
        category.addCategoryExtension(TaglockBindingRecipe.class, recipe -> assemble(recipe, taglocks(recipe)));
        category.addCategoryExtension(UrnUpgradeRecipe.class, recipe -> assemble(recipe, urns()));
        category.addCategoryExtension(AssembleVampireBookRecipe.class, recipe -> assemble(recipe, vampireBooks()));
        category.addCategoryExtension(PotionChargeRecipe.class, recipe -> assemble(recipe, charges(recipe)));
        category.addCategoryExtension(ClothingRepairRecipe.class, recipe -> assemble(recipe, clothing()));
        category.addCategoryExtension(DyeBrewBagRecipe.class, recipe -> assemble(recipe, bags()));
        category.addCategoryExtension(CopyBiomeNoteRecipe.class, DynamicCraftingLayout::biomeNotes);
        category.addCategoryExtension(DoorKeyringRecipe.class, recipe -> assemble(recipe, keyrings()));
    }

    static void registerSubtypes(ISubtypeRegistration registration) {
        registerSubtype(registration, item("taglockkit"), DynamicCraftingLayout::binding);
        for (TaglockBindingRecipe.Binding binding : TaglockBindingRecipe.Binding.values()) {
            for (Item item : binding.items()) registerSubtype(registration, item, DynamicCraftingLayout::binding);
        }
        registerSubtype(registration, item("leonardsurn"), stack -> number(LeonardsUrnItem.tier(stack)));
        registerSubtype(registration, item("vampirebook"), stack -> number(VampireBookItem.tornPages(stack)));
        int defaultColor = BrewBagItem.color(new ItemStack(item("brewbag")));
        registerSubtype(registration, item("brewbag"), stack -> {
            int color = BrewBagItem.color(stack);
            return color == defaultColor ? NONE : number(color);
        });
        registerSubtype(registration, item("biomenote"), stack -> {
            var biome = BiomeNoteItem.biomeId(stack);
            return biome == null ? NONE : biome.toString();
        });
        registerSubtype(registration, atlasItem(), stack -> {
            var biome = BiomeNoteItem.biomeId(stack);
            Level level = Minecraft.getInstance().level;
            if (biome == null && level != null) biome = BiomeBookIndex.selectedBiome(stack, level.registryAccess());
            return biome == null ? NONE : biome.toString();
        });
    }

    private static void registerSubtype(ISubtypeRegistration registration, Item item, Function<ItemStack, String> subtype) {
        registration.registerSubtypeInterpreter(item, (stack, context) -> subtype.apply(stack));
    }

    private static String binding(ItemStack stack) {
        if (TaglockItem.isBound(stack, 2)) return "bound_pair";
        return TaglockItem.isBound(stack) ? "bound" : NONE;
    }

    private static String number(int value) {
        return value == 0 ? NONE : Integer.toString(value);
    }

    @Override
    public ResourceLocation getRegistryName() { return id; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper grid, IFocusGroup focuses) {
        int size = examples.stream().mapToInt(example -> example.inputs().size()).max().orElse(0);
        if (size == 0) return;
        var inputs = new ArrayList<List<ItemStack>>();
        for (int i = 0; i < size; i++) {
            final int index = i;
            inputs.add(examples.stream().map(example -> example.input(index)).toList());
        }
        builder.setShapeless();
        var inputSlots = grid.createAndSetInputs(builder, inputs, 3, 3);
        var output = grid.createAndSetOutputs(builder, examples.stream().map(Example::output).toList());
        var linked = new ArrayList<IIngredientAcceptor<?>>(inputSlots.subList(0, size));
        linked.add(output);
        builder.createFocusLink(linked.toArray(IIngredientAcceptor<?>[]::new));
    }

    private static DynamicCraftingLayout assemble(CraftingRecipe recipe, Stream<List<ItemStack>> combinations) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return new DynamicCraftingLayout(recipe.getId(), List.of());
        var examples = combinations.map(inputs -> {
            var inventory = NonNullList.withSize(9, ItemStack.EMPTY);
            for (int i = 0; i < inputs.size(); i++) inventory.set(i, inputs.get(i).copy());
            var grid = new TransientCraftingContainer(PREVIEW_MENU, 3, 3, inventory);
            return new Example(inputs, recipe.assemble(grid, level.registryAccess()));
        }).filter(example -> !example.output().isEmpty()).toList();
        return new DynamicCraftingLayout(recipe.getId(), examples);
    }

    private static Stream<List<ItemStack>> taglocks(TaglockBindingRecipe recipe) {
        ItemStack first = sampleTaglock("first", 1L);
        ItemStack second = sampleTaglock("second", 2L);
        return recipe.binding().items().stream().map(item -> recipe.needsSecondTaglock()
                ? List.of(new ItemStack(item), first, second) : List.of(new ItemStack(item), first));
    }

    private static ItemStack sampleTaglock(String sample, long id) {
        var stack = new ItemStack(item("taglockkit"));
        TaglockItem.bindPlayer(stack, Component.translatable("jei.witchery.taglock_sample_" + sample).getString(), new UUID(0, id));
        return stack;
    }

    private static Stream<List<ItemStack>> urns() {
        return IntStream.range(0, 3).mapToObj(tier -> List.of(
                variant(item("leonardsurn"), tier, LeonardsUrnItem::setTier), new ItemStack(item("leonardsurn"))));
    }

    private static Stream<List<ItemStack>> vampireBooks() {
        return IntStream.range(0, VampireBookItem.MAX_TORN_PAGES).mapToObj(pages -> List.of(
                variant(item("vampirebook"), pages, VampireBookItem::setTornPages),
                new ItemStack(WitcheryIngredient.TORN_VAMPIRE_PAGE.item())));
    }

    private static ItemStack variant(Item item, int value, ObjIntConsumer<ItemStack> setter) {
        var stack = new ItemStack(item);
        if (value != 0) setter.accept(stack, value);
        return stack;
    }

    private static Stream<List<ItemStack>> charges(PotionChargeRecipe recipe) {
        return Arrays.stream(LousePotion.values()).map(potion -> List.of(new ItemStack(recipe.chargeableItem()), potion.bottle()));
    }

    private static Stream<List<ItemStack>> clothing() {
        return WitcheryItems.all().stream().map(RegistryObject::get)
                .filter(WitchClothingItem.class::isInstance).map(WitchClothingItem.class::cast).map(item -> {
                    var worn = new ItemStack(item);
                    worn.setDamageValue(1);
                    var items = new ArrayList<ItemStack>();
                    items.add(worn);
                    for (int i = 0; i < item.repairLeather(); i++) {
                        items.add(new ItemStack(WitcheryIngredient.IMPREGNATED_LEATHER.item()));
                    }
                    return items;
                });
    }

    private static Stream<List<ItemStack>> bags() {
        return Arrays.stream(DyeColor.values()).flatMap(current -> {
            var bag = new ItemStack(item("brewbag"));
            BrewBagItem.dye(bag, current);
            return Arrays.stream(DyeColor.values()).map(dye -> List.of(bag, new ItemStack(DyeItem.byColor(dye))));
        });
    }

    private static DynamicCraftingLayout biomeNotes(CopyBiomeNoteRecipe recipe) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return new DynamicCraftingLayout(recipe.getId(), List.of());
        var examples = BiomeBookIndex.pages(level.registryAccess()).stream().map(BiomeBookIndex.Entry::id).distinct().map(biome -> {
            var atlas = new ItemStack(atlasItem());
            atlas.getOrCreateTag().putString(BiomeNoteItem.BIOME, biome.toString());
            return new Example(List.of(atlas, new ItemStack(Items.PAPER)), CopyBiomeNoteRecipe.noteForBiome(biome));
        }).toList();
        return new DynamicCraftingLayout(recipe.getId(), examples);
    }

    private static Stream<List<ItemStack>> keyrings() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return Stream.empty();
        var key = DoorKeyItem.forDoor(level, BlockPos.ZERO);
        var second = DoorKeyItem.forDoor(level, BlockPos.ZERO.east());
        var ring = new ItemStack(WitcheryIngredient.DOOR_KEYRING.item());
        DoorKeyItem.addToRing(ring, second);
        return Stream.of(List.of(key, second), List.of(key, ring));
    }

    private static Item atlasItem() {
        return item("bookbiomes2");
    }

    private static Item item(String id) {
        return WitcheryItems.getRequired(id).get();
    }

    private record Example(List<ItemStack> inputs, ItemStack output) {
        ItemStack input(int index) {
            return index < inputs.size() ? inputs.get(index) : ItemStack.EMPTY;
        }
    }
}
