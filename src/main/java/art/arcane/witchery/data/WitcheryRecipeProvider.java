package art.arcane.witchery.data;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.CritterSnareBlock;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.item.VampireClothingItem.Style;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.PartialNBTIngredient;

import java.util.function.Consumer;

public class WitcheryRecipeProvider extends RecipeProvider {
    private static final TagKey<Item> WOODEN_RODS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("forge", "rods/wooden"));
    private static final TagKey<Item> GARLIC_CROPS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("forge", "crops/garlic"));

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
        Block bloodedWool = WitcheryBlocks.getRequired("bloodedwool").get();
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(bloodedWool), RecipeCategory.MISC,
                        WitcheryIngredient.DARK_CLOTH.item(), 0, 200)
                .unlockedBy("has_blooded_wool", has(bloodedWool))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "dark_cloth"));

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
        Item impregnatedLeather = WitcheryIngredient.IMPREGNATED_LEATHER.item();
        Item goldenThread = WitcheryIngredient.GOLDEN_THREAD.item();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, impregnatedLeather, 4)
                .define('M', WitcheryIngredient.WHIFF_OF_MAGIC.item())
                .define('L', Items.LEATHER)
                .define('D', WitcheryIngredient.DIAMOND_VAPOUR.item())
                .pattern("MLM")
                .pattern("LDL")
                .pattern("MLM")
                .unlockedBy("has_diamond_vapour", has(WitcheryIngredient.DIAMOND_VAPOUR.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired("witchhat").get())
                .define('L', impregnatedLeather)
                .define('S', goldenThread)
                .define('G', Items.GLOWSTONE_DUST)
                .pattern(" L ")
                .pattern("SLS")
                .pattern("LGL")
                .unlockedBy("has_impregnated_leather", has(impregnatedLeather))
                .save(recipeOutput);
        robe(recipeOutput, "witchrobe", impregnatedLeather, goldenThread, WitcheryIngredient.CREEPER_HEART.item());
        robe(recipeOutput, "necromancerrobe", impregnatedLeather, goldenThread, WitcheryIngredient.NECROMANTIC_STONE.item());
        SpecialRecipeBuilder.special(WitcheryRecipes.CLOTHING_REPAIR.get()).save(recipeOutput, "witchery:repair_witch_clothing");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryItems.getRequired("vampirebook").get())
                .define('W', Items.NETHER_WART)
                .define('S', Items.NETHER_STAR)
                .define('B', Items.BOOK)
                .define('G', WitcheryItems.getRequired("garlic").get())
                .pattern("WSW")
                .pattern("WBW")
                .pattern("WGW")
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                .save(recipeOutput);
        SpecialRecipeBuilder.special(WitcheryRecipes.ASSEMBLE_VAMPIRE_BOOK.get())
                .save(recipeOutput, "witchery:assemble_vampire_book");
        vampireClothing(Style.HAT).pattern("CCC").pattern("C C").save(recipeOutput);
        vampireClothing(Style.COAT).pattern("C C").pattern("CCC").pattern("CCC").save(recipeOutput);
        vampireClothing(Style.FITTED_COAT).define('L', Items.LEATHER)
                .pattern("C C").pattern("CLC").pattern("CCC").save(recipeOutput);
        vampireClothing(Style.TROUSERS).pattern("CCC").pattern("C C").pattern("C C").save(recipeOutput);
        vampireClothing(Style.KILT).pattern("CCC").pattern("CCC").pattern("C C").save(recipeOutput);
        vampireClothing(Style.BOOTS).pattern("C C").pattern("C C").save(recipeOutput);
        reinforceClothing(recipeOutput, Style.HAT, Style.HELMET);
        reinforceClothing(recipeOutput, Style.COAT, Style.CHAIN_COAT);
        reinforceClothing(recipeOutput, Style.FITTED_COAT, Style.FITTED_CHAIN_COAT);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired("canesword").get())
                .define('C', WitcheryIngredient.DARK_CLOTH.item())
                .define('G', Items.GOLD_INGOT)
                .define('D', Items.DIAMOND_SWORD)
                .pattern(" CG").pattern("CDC").pattern("CC ")
                .unlockedBy("has_dark_cloth", has(WitcheryIngredient.DARK_CLOTH.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WitcheryBlocks.getRequired("garlicgarland").get())
                .define('G', GARLIC_CROPS).define('S', Items.STRING)
                .pattern("S S").pattern("GSG").pattern("GGG")
                .unlockedBy("has_garlic", has(WitchCrop.GARLIC.seed()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryBlocks.getRequired("silvervat").get())
                .define('I', Items.IRON_INGOT)
                .define('B', Items.WATER_BUCKET)
                .define('F', Blocks.FURNACE)
                .pattern("IBI").pattern("IFI")
                .unlockedBy("has_furnace", has(Blocks.FURNACE))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired("silversword").get())
                .define('D', WitcheryIngredient.SILVER_DUST.item())
                .define('S', Items.GOLDEN_SWORD)
                .pattern("DDD").pattern("DSD").pattern("DDD")
                .unlockedBy("has_silver_dust", has(WitcheryIngredient.SILVER_DUST.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, WitcheryBlocks.getRequired("daylightcollector").get())
                .define('G', Items.GOLD_INGOT)
                .define('R', Items.REPEATER)
                .define('I', Items.IRON_INGOT)
                .define('C', Blocks.DAYLIGHT_DETECTOR)
                .pattern("G G")
                .pattern(" R ")
                .pattern("ICI")
                .unlockedBy("has_daylight_detector", has(Blocks.DAYLIGHT_DETECTOR))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryBlocks.getRequired("bloodcrucible").get())
                .define('S', Blocks.STONE_BRICK_STAIRS)
                .define('B', Blocks.STONE_BRICKS)
                .define('L', Blocks.STONE_BRICK_SLAB)
                .pattern("S S")
                .pattern("BLB")
                .unlockedBy("has_stone_bricks", has(Blocks.STONE_BRICKS))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryIngredient.WEB.item())
                .define('S', Items.STRING)
                .define('W', Blocks.COBWEB)
                .pattern(" S ")
                .pattern("SWS")
                .pattern(" S ")
                .unlockedBy("has_cobweb", has(Blocks.COBWEB))
                .save(recipeOutput);
        Block critterSnare = WitcheryBlocks.getRequired("crittersnare").get();
        var batState = new CompoundTag();
        batState.putString(CritterSnareBlock.CRITTER.getName(), CritterSnareBlock.Critter.BAT.getSerializedName());
        var batSnareTag = new CompoundTag();
        batSnareTag.put(BlockItem.BLOCK_STATE_TAG, batState);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryIngredient.BAT_BALL.item())
                .define('S', Items.SLIME_BALL)
                .define('B', PartialNBTIngredient.of(critterSnare, batSnareTag))
                .pattern("SBS")
                .pattern("B B")
                .pattern("SBS")
                .unlockedBy("has_critter_snare", has(critterSnare))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, WitcheryIngredient.ODD_PORKCHOP_RAW.item())
                .requires(WitcheryIngredient.MUTANDIS.item())
                .requires(Items.ROTTEN_FLESH)
                .unlockedBy("has_mutandis", has(WitcheryIngredient.MUTANDIS.item()))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, WitcheryIngredient.ODD_PORKCHOP_RAW.item())
                .requires(WitcheryIngredient.MUTANDIS_EXTREMIS.item())
                .requires(Items.ROTTEN_FLESH)
                .unlockedBy("has_mutandis_extremis", has(WitcheryIngredient.MUTANDIS_EXTREMIS.item()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "odd_porkchop_raw_from_extremis"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryIngredient.CONTRACT_OF_OWNERSHIP.item())
                .define('P', Items.PAPER)
                .define('F', WitcheryIngredient.ODD_PORKCHOP_RAW.item())
                .define('S', Items.STRING)
                .pattern("PPP")
                .pattern("PFP")
                .pattern("PPS")
                .unlockedBy("has_odd_porkchop", has(WitcheryIngredient.ODD_PORKCHOP_RAW.item()))
                .save(recipeOutput);
        Item poppet = WitcheryItems.getRequired("poppet").get();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, poppet)
                .define('W', Blocks.WHITE_WOOL)
                .define('M', WitcheryBlocks.getRequired("spanishmoss").get())
                .define('N', WitcheryIngredient.BONE_NEEDLE.item())
                .define('S', Items.STRING)
                .pattern("WMW")
                .pattern("NMS")
                .pattern("W W")
                .unlockedBy("has_needle", has(WitcheryIngredient.BONE_NEEDLE.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryItems.getRequired("death_protection_poppet").get())
                .define('L', WitcheryIngredient.DROP_OF_LUCK.item())
                .define('G', Items.GOLD_NUGGET)
                .define('D', WitcheryIngredient.DIAMOND_VAPOUR.item())
                .define('P', poppet)
                .pattern("LGD")
                .pattern("GPG")
                .pattern(" G ")
                .unlockedBy("has_poppet", has(poppet))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryItems.getRequired("voodoo_poppet").get())
                .define('P', poppet)
                .define('F', Items.FERMENTED_SPIDER_EYE)
                .define('B', WitcheryIngredient.BELLADONNA_FLOWER.item())
                .define('M', WitcheryIngredient.MANDRAKE_ROOT.item())
                .define('H', WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item())
                .pattern(" F ")
                .pattern("BPM")
                .pattern(" H ")
                .unlockedBy("has_poppet", has(poppet))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryItems.getRequired("voodoo_protection_poppet").get())
                .define('P', poppet)
                .define('B', WitcheryIngredient.BELLADONNA_FLOWER.item())
                .define('M', WitcheryIngredient.MANDRAKE_ROOT.item())
                .define('D', Blocks.DANDELION)
                .define('R', Blocks.RED_MUSHROOM)
                .define('F', Blocks.POPPY)
                .define('U', Blocks.BROWN_MUSHROOM)
                .pattern("DRF")
                .pattern("BPM")
                .pattern("FUD")
                .unlockedBy("has_poppet", has(poppet))
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

    private static ShapedRecipeBuilder vampireClothing(Style style) {
        Item cloth = WitcheryIngredient.DARK_CLOTH.item();
        return ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired(style.path()).get())
                .define('C', cloth).unlockedBy("has_dark_cloth", has(cloth));
    }

    private static void reinforceClothing(Consumer<FinishedRecipe> output, Style base, Style reinforced) {
        Item clothing = WitcheryItems.getRequired(base.path()).get();
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired(reinforced.path()).get())
                .define('I', Items.IRON_INGOT).define('C', clothing).pattern(" I ").pattern("ICI").pattern(" I ")
                .unlockedBy("has_vampire_clothing", has(clothing)).save(output);
    }

    private static void robe(Consumer<FinishedRecipe> recipeOutput, String path, Item leather, Item thread, Item center) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired(path).get())
                .define('L', leather)
                .define('S', thread)
                .define('C', center)
                .pattern("LSL")
                .pattern("LCL")
                .pattern("LLL")
                .unlockedBy("has_impregnated_leather", has(leather))
                .save(recipeOutput);
    }
}
