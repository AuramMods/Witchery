package art.arcane.witchery.data;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

public class WitcheryRecipeProvider extends RecipeProvider {
    private static final TagKey<Item> WOODEN_RODS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("forge", "rods/wooden"));

    public WitcheryRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> recipeOutput) {
        Block oven = WitcheryBlocks.getRequired("witchesovenidle").get();
        Block funnel = WitcheryBlocks.getRequired("fumefunnel").get();
        Block filteredFunnel = WitcheryBlocks.getRequired("filteredfumefunnel").get();
        Block witchLog = WitcheryBlocks.getRequired("witchlog").get();
        Block distillery = WitcheryBlocks.getRequired("distilleryidle").get();
        Block kettle = WitcheryBlocks.getRequired("kettle").get();

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, oven)
                .define('B', Blocks.IRON_BARS)
                .define('I', Items.IRON_INGOT)
                .pattern(" B ")
                .pattern("III")
                .pattern("IBI")
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryIngredient.SOFT_CLAY_JAR.item(), 4)
                .define('C', Items.CLAY_BALL)
                .pattern(" C ")
                .pattern("CCC")
                .unlockedBy("has_clay", has(Items.CLAY_BALL))
                .save(recipeOutput);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(WitcheryIngredient.SOFT_CLAY_JAR.item()),
                        RecipeCategory.MISC, WitcheryIngredient.CLAY_JAR.item(), 0, 200)
                .unlockedBy("has_soft_jar", has(WitcheryIngredient.SOFT_CLAY_JAR.item()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "clay_jar"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.SAPLINGS),
                        RecipeCategory.MISC, WitcheryIngredient.WOOD_ASH.item(), 0, 200)
                .unlockedBy("has_sapling", has(ItemTags.SAPLINGS))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "wood_ash"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(witchLog),
                        RecipeCategory.MISC, Items.CHARCOAL, 0, 200)
                .unlockedBy("has_witch_log", has(witchLog))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "charcoal_from_witch_log"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, funnel)
                .define('E', Items.BUCKET)
                .define('L', Items.LAVA_BUCKET)
                .define('G', Blocks.GLOWSTONE)
                .define('B', Blocks.IRON_BLOCK)
                .define('I', Blocks.IRON_BARS)
                .pattern("ELE")
                .pattern("EGE")
                .pattern("BIB")
                .unlockedBy("has_witches_oven", has(oven))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryIngredient.ATTUNED_STONE.item())
                .define('F', WitcheryIngredient.WHIFF_OF_MAGIC.item())
                .define('D', Items.DIAMOND)
                .define('L', Items.LAVA_BUCKET)
                .pattern("F")
                .pattern("D")
                .pattern("L")
                .unlockedBy("has_whiff_of_magic", has(WitcheryIngredient.WHIFF_OF_MAGIC.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryIngredient.QUARTZ_SPHERE.item())
                .define('Q', Items.QUARTZ)
                .define('B', Blocks.QUARTZ_BLOCK)
                .define('G', Blocks.GLASS)
                .pattern("QBQ")
                .pattern("BGB")
                .pattern("QBQ")
                .unlockedBy("has_quartz", has(Items.QUARTZ))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryIngredient.FUME_FILTER.item())
                .define('G', Blocks.GLASS)
                .define('I', Items.IRON_INGOT)
                .define('A', WitcheryIngredient.CHARGED_ATTUNED_STONE.item())
                .pattern("GGG")
                .pattern("IAI")
                .pattern("GGG")
                .unlockedBy("has_charged_stone", has(WitcheryIngredient.CHARGED_ATTUNED_STONE.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, filteredFunnel)
                .define('B', funnel)
                .define('F', WitcheryIngredient.FUME_FILTER.item())
                .pattern("B")
                .pattern("F")
                .unlockedBy("has_fume_filter", has(WitcheryIngredient.FUME_FILTER.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired("earmuffs").get())
                .define('L', Items.LEATHER)
                .define('W', Blocks.WHITE_WOOL)
                .pattern("LLL")
                .pattern("L L")
                .pattern("W W")
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, WitcheryIngredient.ANOINTING_PASTE.item())
                .requires(WitchCrop.ARTICHOKE.seed())
                .requires(WitchCrop.MANDRAKE.seed())
                .requires(WitchCrop.BELLADONNA.seed())
                .requires(WitchCrop.SNOWBELL.seed())
                .unlockedBy("has_mandrake_seeds", has(WitchCrop.MANDRAKE.seed()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, distillery)
                .define('J', WitcheryIngredient.CLAY_JAR.item())
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GOLD_INGOT)
                .define('A', WitcheryIngredient.ATTUNED_STONE.item())
                .pattern("JIJ")
                .pattern("III")
                .pattern("GAG")
                .unlockedBy("has_attuned_stone", has(WitcheryIngredient.ATTUNED_STONE.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, kettle)
                .define('S', Items.STICK)
                .define('T', Items.STRING)
                .define('A', WitcheryIngredient.ATTUNED_STONE.item())
                .define('C', Items.CAULDRON)
                .pattern("STS")
                .pattern("TAT")
                .pattern(" C ")
                .unlockedBy("has_attuned_stone", has(WitcheryIngredient.ATTUNED_STONE.item()))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, WitcheryIngredient.QUICKLIME.item())
                .requires(WitcheryIngredient.WOOD_ASH.item())
                .unlockedBy("has_wood_ash", has(WitcheryIngredient.WOOD_ASH.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryItems.getRequired("chalkritual").get(), 2)
                .define('A', WitcheryIngredient.WOOD_ASH.item())
                .define('T', WitcheryIngredient.TEAR_OF_THE_GODDESS.item())
                .define('G', WitcheryIngredient.GYPSUM.item())
                .pattern("ATA")
                .pattern("AGA")
                .pattern("AGA")
                .unlockedBy("has_tear_of_the_goddess", has(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired("arthana").get())
                .define('G', Items.GOLD_INGOT)
                .define('N', Items.GOLD_NUGGET)
                .define('E', Items.EMERALD)
                .define('S', Items.STICK)
                .pattern(" G ")
                .pattern("NEN")
                .pattern(" S ")
                .unlockedBy("has_emerald", has(Items.EMERALD))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WitcheryIngredient.CANDELABRA.item())
                .define('T', Items.TORCH)
                .define('I', Items.IRON_INGOT)
                .define('A', WitcheryIngredient.ATTUNED_STONE.item())
                .pattern("TTT")
                .pattern("IAI")
                .pattern(" I ")
                .unlockedBy("has_attuned_stone", has(WitcheryIngredient.ATTUNED_STONE.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WitcheryIngredient.EMPTY_CHALICE.item())
                .define('N', Items.GOLD_NUGGET)
                .define('G', Items.GOLD_INGOT)
                .define('A', WitcheryIngredient.ATTUNED_STONE.item())
                .pattern("NAN")
                .pattern("NGN")
                .pattern(" G ")
                .unlockedBy("has_attuned_stone", has(WitcheryIngredient.ATTUNED_STONE.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WitcheryIngredient.FILLED_CHALICE.item())
                .define('S', WitcheryIngredient.REDSTONE_SOUP.item())
                .define('C', WitcheryIngredient.EMPTY_CHALICE.item())
                .pattern("S")
                .pattern("C")
                .unlockedBy("has_redstone_soup", has(WitcheryIngredient.REDSTONE_SOUP.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WitcheryBlocks.getRequired("spinningwheel").get())
                .define('F', Items.ITEM_FRAME)
                .define('W', Items.WHITE_WOOL)
                .define('S', WOODEN_RODS)
                .define('P', ItemTags.PLANKS)
                .define('A', WitcheryIngredient.ATTUNED_STONE.item())
                .pattern("FFW")
                .pattern("FFS")
                .pattern("PAP")
                .unlockedBy("has_attuned_stone", has(WitcheryIngredient.ATTUNED_STONE.item()))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, WitcheryIngredient.PURIFIED_MILK.item(), 3)
                .requires(Items.MILK_BUCKET)
                .requires(WitcheryIngredient.ODOUR_OF_PURITY.item())
                .requires(WitcheryIngredient.CLAY_JAR.item(), 3)
                .unlockedBy("has_odour_of_purity", has(WitcheryIngredient.ODOUR_OF_PURITY.item()))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, WitcheryIngredient.PURIFIED_MILK.item(), 3)
                .requires(Items.CAKE)
                .requires(WitcheryIngredient.ODOUR_OF_PURITY.item(), 2)
                .requires(WitcheryIngredient.CLAY_JAR.item(), 3)
                .unlockedBy("has_odour_of_purity", has(WitcheryIngredient.ODOUR_OF_PURITY.item()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "purified_milk_from_cake"));
    }
}
