package art.arcane.witchery.data;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.CritterSnareBlock;
import art.arcane.witchery.block.ShadedGlassBlock;
import art.arcane.witchery.block.StockadeBlock;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.crafting.AllSaplingsSmeltCondition;
import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.item.HunterClothingItem.Piece;
import art.arcane.witchery.item.HunterClothingItem.Upgrade;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.item.VampireClothingItem.Style;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRecipes;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.DifferenceIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.common.crafting.conditions.TrueCondition;

import java.util.function.Consumer;

public class WitcheryRecipeProvider extends RecipeProvider {
    private static final TagKey<Item> WOODEN_RODS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("forge", "rods/wooden"));
    private static final TagKey<Item> GARLIC_CROPS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("forge", "crops/garlic"));
    private static final TagKey<Item> SILVER_INGOTS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("forge", "ingots/silver"));

    public WitcheryRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> recipeOutput) {
        Block oven = WitcheryBlocks.getRequired("witchesovenidle").get();
        Block funnel = WitcheryBlocks.getRequired("fumefunnel").get();
        Block filteredFunnel = WitcheryBlocks.getRequired("filteredfumefunnel").get();
        Block witchLog = WitcheryBlocks.getRequired("witchlog").get();
        Block witchPlanks = WitcheryBlocks.getRequired("witchwood").get();
        Block distillery = WitcheryBlocks.getRequired("distilleryidle").get();
        Block kettle = WitcheryBlocks.getRequired("kettle").get();

        Ingredient alderPlanks = PartialNBTIngredient.of(witchPlanks.asItem(), WitchWoodType.ALDER.stack(witchPlanks).getTag());
        Ingredient hawthornPlanks = PartialNBTIngredient.of(witchPlanks.asItem(), WitchWoodType.HAWTHORN.stack(witchPlanks).getTag());
        Ingredient rowanPlanks = DifferenceIngredient.of(Ingredient.of(witchPlanks), CompoundIngredient.of(alderPlanks, hawthornPlanks));
        door(recipeOutput, WitcheryIngredient.ROWAN_DOOR, witchPlanks, rowanPlanks);
        door(recipeOutput, WitcheryIngredient.ALDER_DOOR, witchPlanks, alderPlanks);
        witchWoodStairs(recipeOutput, WitchWoodType.ROWAN, witchPlanks, rowanPlanks);
        witchWoodStairs(recipeOutput, WitchWoodType.ALDER, witchPlanks, alderPlanks);
        witchWoodStairs(recipeOutput, WitchWoodType.HAWTHORN, witchPlanks, hawthornPlanks);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WitcheryIngredient.TREEFYD_SEEDS.item(), 2)
                .define('X', WitcheryIngredient.MUTANDIS_EXTREMIS.item())
                .define('A', WitcheryIngredient.REEK_OF_MISFORTUNE.item())
                .define('C', WitcheryBlocks.getRequired("embermoss").get())
                .define('Y', WitcheryIngredient.GLOBE_ARTICHOKE.item())
                .define('D', WitcheryIngredient.MANDRAKE_ROOT.item())
                .define('B', WitcheryIngredient.TEAR_OF_THE_GODDESS.item())
                .pattern("XAX")
                .pattern("CYD")
                .pattern("XBX")
                .unlockedBy("has_mutandis_extremis", has(WitcheryIngredient.MUTANDIS_EXTREMIS.item()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WitcheryBlocks.getRequired("brazier").get())
                .define('I', Items.IRON_INGOT)
                .define('N', WitcheryIngredient.NECROMANTIC_STONE.item())
                .define('S', Items.STICK)
                .pattern("INI")
                .pattern(" S ")
                .pattern("SSS")
                .unlockedBy("has_necromantic_stone", has(WitcheryIngredient.NECROMANTIC_STONE.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WitcheryBlocks.getRequired("alluringskull").get())
                .define('N', WitcheryIngredient.NECROMANTIC_STONE.item())
                .define('G', Items.GLOWSTONE_DUST)
                .define('S', Ingredient.of(Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL))
                .define('P', WitcheryItems.getRequired("voodoo_poppet").get())
                .pattern(" N ")
                .pattern("GSG")
                .pattern(" P ")
                .unlockedBy("has_necromantic_stone", has(WitcheryIngredient.NECROMANTIC_STONE.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WitcheryBlocks.getRequired("scarecrow").get())
                .define('W', Blocks.WHITE_WOOL)
                .define('P', Blocks.JACK_O_LANTERN)
                .define('S', WOODEN_RODS)
                .define('T', WitcheryIngredient.TORMENTED_TWINE.item())
                .pattern("WPW")
                .pattern("STS")
                .pattern("WSW")
                .unlockedBy("has_tormented_twine", has(WitcheryIngredient.TORMENTED_TWINE.item()))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WitcheryBlocks.getRequired("witchsladder").get())
                .define('F', Items.FEATHER)
                .define('S', Items.STRING)
                .define('T', WitcheryIngredient.FANCIFUL_THREAD.item())
                .pattern("FSF")
                .pattern("FTF")
                .pattern("FSF")
                .unlockedBy("has_fanciful_thread", has(WitcheryIngredient.FANCIFUL_THREAD.item()))
                .save(recipeOutput);
        Ingredient rowan = DifferenceIngredient.of(Ingredient.of(witchLog), CompoundIngredient.of(
                PartialNBTIngredient.of(witchLog.asItem(), WitchWoodType.ALDER.stack(witchLog).getTag()),
                PartialNBTIngredient.of(witchLog.asItem(), WitchWoodType.HAWTHORN.stack(witchLog).getTag())));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WitcheryBlocks.getRequired("trent").get())
                .define('O', Blocks.OAK_LOG)
                .define('P', Blocks.JACK_O_LANTERN)
                .define('T', WitcheryIngredient.TORMENTED_TWINE.item())
                .define('R', rowan)
                .pattern("OPO")
                .pattern("TRT")
                .pattern("O O")
                .unlockedBy("has_tormented_twine", has(WitcheryIngredient.TORMENTED_TWINE.item()))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, WitcheryIngredient.WORMY_APPLE.item())
                .requires(Items.APPLE)
                .requires(Items.ROTTEN_FLESH)
                .requires(Items.SUGAR)
                .unlockedBy("has_apple", has(Items.APPLE))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, WitcheryIngredient.GRAVEYARD_DUST.item())
                .requires(WitcheryIngredient.SPECTRAL_DUST.item())
                .requires(Items.BONE_MEAL)
                .requires(WitcheryIngredient.MUTANDIS.item())
                .unlockedBy("has_spectral_dust", has(WitcheryIngredient.SPECTRAL_DUST.item()))
                .save(recipeOutput);

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
        woodAshRecipe(recipeOutput);
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
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, WitcheryItems.getRequired("brewbag").get())
                .define('L', impregnatedLeather)
                .define('T', WitcheryIngredient.GOLDEN_THREAD.item())
                .pattern("LLL").pattern("LTL").pattern("LLL")
                .unlockedBy("has_golden_thread", has(WitcheryIngredient.GOLDEN_THREAD.item()))
                .save(recipeOutput);
        SpecialRecipeBuilder.special(WitcheryRecipes.DYE_BREW_BAG.get()).save(recipeOutput, "witchery:dye_brew_bag");
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
        Item bitingBelt = WitcheryItems.getRequired("bitingbelt").get();
        Item louse = WitcheryItems.getRequired("louse").get();
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, bitingBelt)
                .define('L', impregnatedLeather)
                .define('T', goldenThread)
                .define('P', louse)
                .define('H', WitcheryIngredient.MELLIFLUOUS_HUNGER.item())
                .pattern("PLH").pattern("LTL").pattern("L L")
                .unlockedBy("has_louse", has(louse))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired("barkbelt").get())
                .define('S', WitcheryIngredient.BREW_OF_FLOWING_SPIRIT.item())
                .define('E', Items.EMERALD)
                .define('G', WitcheryIngredient.ENT_BRANCH.item())
                .define('B', bitingBelt)
                .define('H', WitcheryIngredient.CREEPER_HEART.item())
                .pattern("SES").pattern("GBG").pattern("SHS")
                .unlockedBy("has_biting_belt", has(bitingBelt))
                .save(recipeOutput);
        SpecialRecipeBuilder.special(WitcheryRecipes.CHARGE_BITING_BELT.get())
                .save(recipeOutput, "witchery:charge_biting_belt");
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired("iceslippers").get())
                .define('L', impregnatedLeather)
                .define('T', goldenThread)
                .define('H', WitcheryIngredient.FROZEN_HEART.item())
                .define('D', WitcheryIngredient.DIAMOND_VAPOUR.item())
                .define('O', WitcheryIngredient.ODOUR_OF_PURITY.item())
                .pattern("LTL").pattern("LHL").pattern("DOD")
                .unlockedBy("has_frozen_heart", has(WitcheryIngredient.FROZEN_HEART.item()))
                .save(recipeOutput);
        Item seepingShoes = WitcheryItems.getRequired("seepingshoes").get();
        Item witchesHand = WitcheryItems.getRequired("witchhand").get();
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, seepingShoes)
                .define('L', impregnatedLeather)
                .define('T', goldenThread)
                .define('H', witchesHand)
                .define('R', WitcheryIngredient.REDSTONE_SOUP.item())
                .define('M', Items.MILK_BUCKET)
                .pattern("LTL").pattern("HRH").pattern("MMM")
                .unlockedBy("has_witches_hand", has(witchesHand))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired("rubyslippers").get())
                .define('A', WitcheryIngredient.ATTUNED_STONE.item())
                .define('B', WitcheryIngredient.INFERNAL_BLOOD.item())
                .define('T', goldenThread)
                .define('S', seepingShoes)
                .pattern("ABA").pattern("TST").pattern("ABA")
                .unlockedBy("has_seeping_shoes", has(seepingShoes))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, WitcheryBlocks.getRequired("snowpressureplate").get())
                .define('S', Items.SNOW_BLOCK).pattern("SS")
                .unlockedBy("has_snow_block", has(Items.SNOW_BLOCK)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, WitcheryBlocks.getRequired("snowstairs").get(), 4)
                .define('S', Items.SNOW_BLOCK).pattern("S  ").pattern("SS ").pattern("SSS")
                .unlockedBy("has_snow_block", has(Items.SNOW_BLOCK)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, WitcheryBlocks.getRequired("snowslab").get(), 6)
                .define('S', Items.SNOW).pattern("SSS").pattern("SSS")
                .unlockedBy("has_snow", has(Items.SNOW)).save(recipeOutput);
        stockadeRecipes(recipeOutput);
        shadedGlassRecipes(recipeOutput);
        Item breath = WitcheryIngredient.BREATH_OF_THE_GODDESS.item();
        ItemStack swiftness = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.SWIFTNESS);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WitcheryBlocks.getRequired("leapinglily").get(), 5)
                .define('#', Items.LILY_PAD).define('P', PartialNBTIngredient.of(Items.POTION, swiftness.getTag()))
                .define('G', Items.GLOWSTONE_DUST).define('B', breath)
                .pattern("#P#").pattern("G#G").pattern("#B#")
                .unlockedBy("has_breath_of_the_goddess", has(breath)).save(recipeOutput);
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
        handBowRecipes(recipeOutput);
        hunterClothingRecipes(recipeOutput);
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
        impContract(recipeOutput, WitcheryIngredient.CONTRACT_LIVING_FLAME, Items.BLAZE_ROD,
                WitcheryIngredient.HINT_OF_REBIRTH.item());
        impContract(recipeOutput, WitcheryIngredient.CONTRACT_FIERY_TOLERANCE, Items.BLAZE_POWDER);
        impContract(recipeOutput, WitcheryIngredient.CONTRACT_EVAPORATION, Items.MAGMA_CREAM, Items.BLAZE_ROD);
        impContract(recipeOutput, WitcheryIngredient.CONTRACT_FIERY_TOUCH,
                WitcheryBlocks.getRequired("embermoss").get(), Items.BLAZE_ROD);
        impContract(recipeOutput, WitcheryIngredient.CONTRACT_MELTING_TOUCH, Items.LAVA_BUCKET);
        SpecialRecipeBuilder.special(WitcheryRecipes.SIGN_IMP_SPELL_CONTRACT.get())
                .save(recipeOutput, "witchery:sign_imp_spell_contract");
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

    private static void impContract(Consumer<FinishedRecipe> output, WitcheryIngredient contract, ItemLike... ingredients) {
        Item ownership = WitcheryIngredient.CONTRACT_OF_OWNERSHIP.item();
        var recipe = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, contract.item()).requires(ownership);
        for (ItemLike ingredient : ingredients) recipe.requires(ingredient);
        recipe.unlockedBy("has_ownership_contract", has(ownership)).save(output);
    }

    private static void stockadeRecipes(Consumer<FinishedRecipe> output) {
        Block stockade = WitcheryBlocks.getRequired("stockade").get();
        Item exhale = WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item();
        for (var wood : StockadeBlock.Wood.values()) {
            Block log = wood.log();
            Ingredient logs = Ingredient.of(log);
            var type = wood.witchWood();
            if (type == WitchWoodType.ROWAN) {
                var alder = PartialNBTIngredient.of(log.asItem(), WitchWoodType.ALDER.stack(log).getTag());
                var hawthorn = PartialNBTIngredient.of(log.asItem(), WitchWoodType.HAWTHORN.stack(log).getTag());
                logs = DifferenceIngredient.of(logs, CompoundIngredient.of(alder, hawthorn));
            } else if (type != null) {
                logs = PartialNBTIngredient.of(log.asItem(), type.stack(log).getTag());
            }
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, stockade, 9)
                    .define('L', logs).define('E', exhale).pattern(" L ").pattern("LEL").pattern("LLL")
                    .unlockedBy("has_exhale", has(exhale)).save(recipe -> output.accept(wood == StockadeBlock.Wood.OAK ? recipe
                                    : new BlockStateRecipe(recipe, StockadeBlock.WOOD.getName(), wood.getSerializedName())),
                            "witchery:stockade/" + wood.getSerializedName());
        }
    }

    private static void shadedGlassRecipes(Consumer<FinishedRecipe> output) {
        Block shadedGlass = WitcheryBlocks.getRequired("shadedglass").get();
        for (var color : ShadedGlassBlock.Color.values()) {
            Item stainedGlass = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(color.dye().getName() + "_stained_glass"));
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, shadedGlass, 8).group("witchery:shaded_glass")
                    .define('G', stainedGlass).define('R', Items.REDSTONE).pattern("GGG").pattern("GRG").pattern("GGG")
                    .unlockedBy("has_stained_glass", has(stainedGlass)).save(recipe -> output.accept(color == ShadedGlassBlock.Color.WHITE
                                    ? recipe : new BlockStateRecipe(recipe, ShadedGlassBlock.COLOR.getName(), color.getSerializedName())),
                            "witchery:shaded_glass/" + color.getSerializedName());
        }
    }

    private record BlockStateRecipe(FinishedRecipe recipe, String property, String value) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            recipe.serializeRecipeData(json);
            var state = new JsonObject();
            state.addProperty(property, value);
            var nbt = new JsonObject();
            nbt.add(BlockItem.BLOCK_STATE_TAG, state);
            json.getAsJsonObject("result").add("nbt", nbt);
        }

        @Override public ResourceLocation getId() { return recipe.getId(); }
        @Override public RecipeSerializer<?> getType() { return recipe.getType(); }
        @Override public JsonObject serializeAdvancement() { return recipe.serializeAdvancement(); }
        @Override public ResourceLocation getAdvancementId() { return recipe.getAdvancementId(); }
    }

    private static void door(Consumer<FinishedRecipe> output, WitcheryIngredient door, Block planks, Ingredient ingredient) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, door.item())
                .define('#', ingredient).pattern("##").pattern("##").pattern("##")
                .unlockedBy("has_witchwood", has(planks)).save(output);
    }

    private static void witchWoodStairs(Consumer<FinishedRecipe> output, WitchWoodType wood, Block planks, Ingredient ingredient) {
        Block stairs = WitcheryBlocks.getRequired("stairswood" + wood.getSerializedName()).get();
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, stairs, 4)
                .define('#', ingredient).pattern("#  ").pattern("## ").pattern("###")
                .unlockedBy("has_witchwood", has(planks)).save(output);
    }

    private static void handBowRecipes(Consumer<FinishedRecipe> output) {
        Item stake = WitcheryIngredient.STAKE_BOLT.item();
        Item holy = WitcheryIngredient.HOLY_BOLT.item();
        Item catalyst = WitcheryIngredient.NULL_CATALYST.item();
        Item silver = WitcheryIngredient.SILVER_DUST.item();
        Item needle = WitcheryIngredient.BONE_NEEDLE.item();
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryItems.getRequired("handbow").get())
                .define('I', Items.IRON_INGOT).define('B', Items.BOW).define('S', Items.STRING)
                .define('W', WOODEN_RODS).define('N', needle)
                .pattern("IBI").pattern("SWN").pattern(" I ")
                .unlockedBy("has_bone_needle", has(needle)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, stake, 9)
                .define('S', Items.STRING).define('W', WOODEN_RODS).define('F', Items.FEATHER)
                .pattern(" S ").pattern("WWW").pattern("FFF")
                .unlockedBy("has_feather", has(Items.FEATHER)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryIngredient.SPLITTING_BOLT.item())
                .define('S', Items.STRING).define('B', stake).define('F', Items.FEATHER)
                .pattern(" S ").pattern("BBB").pattern(" F ")
                .unlockedBy("has_stake_bolt", has(stake)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, holy, 12)
                .define('A', stake).define('B', Items.BONE).define('T', Items.GHAST_TEAR)
                .pattern("ABA").pattern("ATA").pattern("ABA")
                .unlockedBy("has_stake_bolt", has(stake)).save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, catalyst, 2)
                .requires(Items.NETHER_STAR).requires(Items.DIAMOND).requires(Items.FLINT).requires(Items.MAGMA_CREAM, 6)
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR)).save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, catalyst, 2)
                .requires(catalyst).requires(Items.MAGMA_CREAM).requires(Items.BLAZE_POWDER)
                .unlockedBy("has_null_catalyst", has(catalyst)).save(output, "witchery:null_catalyst_amplification");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, WitcheryIngredient.ANTI_MAGIC_BOLT.item(), 3)
                .requires(catalyst).requires(WitcheryIngredient.BELLADONNA_FLOWER.item(), 2).requires(holy, 3)
                .unlockedBy("has_null_catalyst", has(catalyst)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, WitcheryIngredient.SILVER_BOLT.item(), 3)
                .define('S', silver).define('B', stake).pattern(" S ").pattern("BBB")
                .unlockedBy("has_silver_dust", has(silver)).save(output);
    }

    private static void woodAshRecipe(Consumer<FinishedRecipe> output) {
        Item witchSapling = WitcheryBlocks.getRequired("witchsapling").get().asItem();
        Ingredient nonRowan = CompoundIngredient.of(
                PartialNBTIngredient.of(witchSapling, WitchWoodType.ALDER.stack(witchSapling).getTag()),
                PartialNBTIngredient.of(witchSapling, WitchWoodType.HAWTHORN.stack(witchSapling).getTag()));
        Ingredient originalSaplings = DifferenceIngredient.of(Ingredient.of(witchSapling, Items.OAK_SAPLING,
                Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING, Items.JUNGLE_SAPLING, Items.ACACIA_SAPLING,
                Items.DARK_OAK_SAPLING), nonRowan);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "wood_ash");
        ConditionalRecipe.builder()
                .addCondition(AllSaplingsSmeltCondition.INSTANCE)
                .addRecipe(conditional -> SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemTags.SAPLINGS),
                                RecipeCategory.MISC, WitcheryIngredient.WOOD_ASH.item(), 0, 200)
                        .unlockedBy("has_sapling", has(ItemTags.SAPLINGS)).save(conditional, id))
                .addCondition(TrueCondition.INSTANCE)
                .addRecipe(conditional -> SimpleCookingRecipeBuilder.smelting(originalSaplings,
                                RecipeCategory.MISC, WitcheryIngredient.WOOD_ASH.item(), 0, 200)
                        .unlockedBy("has_sapling", has(ItemTags.SAPLINGS)).save(conditional, id))
                .generateAdvancement(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "recipes/misc/wood_ash"))
                .build(output, id);
    }

    private static void hunterClothingRecipes(Consumer<FinishedRecipe> output) {
        Item leather = WitcheryIngredient.NULLIFIED_LEATHER.item();
        Item catalyst = WitcheryIngredient.NULL_CATALYST.item();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, leather, 3)
                .define('L', Items.LEATHER).define('N', catalyst)
                .pattern("LLL").pattern("LNL").pattern("LLL")
                .unlockedBy("has_null_catalyst", has(catalyst)).save(output);
        for (Piece piece : Piece.values()) {
            Item plain = HunterClothingItem.make(piece, Upgrade.PLAIN).getItem();
            Item silvered = HunterClothingItem.make(piece, Upgrade.SILVERED).getItem();
            Item garlicked = HunterClothingItem.make(piece, Upgrade.GARLICKED).getItem();
            ShapedRecipeBuilder clothing = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, plain)
                    .define('L', leather).unlockedBy("has_nullified_leather", has(leather));
            switch (piece) {
                case HAT -> clothing.pattern("LLL").pattern("L L");
                case COAT -> clothing.pattern("L L").pattern("LLL").pattern("LLL");
                case LEGS -> clothing.pattern("LLL").pattern("L L").pattern("L L");
                case BOOTS -> clothing.pattern("L L").pattern("L L");
            }
            clothing.save(output);
            silverHunterClothing(plain, silvered).define('D', WitcheryIngredient.SILVER_DUST.item())
                    .save(recipe -> output.accept(new HunterUpgradeResult(recipe)));
            ResourceLocation ingotRecipe = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, piece.path(Upgrade.SILVERED) + "_from_ingots");
            ConditionalRecipe.builder().addCondition(new NotCondition(new TagEmptyCondition(SILVER_INGOTS.location())))
                    .addRecipe(conditional -> silverHunterClothing(plain, silvered).define('D', SILVER_INGOTS)
                            .save(recipe -> conditional.accept(new HunterUpgradeResult(recipe)), ingotRecipe))
                    .generateAdvancement().build(output, ingotRecipe);
            ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, garlicked)
                    .define('G', WitchCrop.GARLIC.seed()).define('A', silvered).define('S', Items.STRING)
                    .pattern(" G ").pattern("GAG").pattern(" S ")
                    .unlockedBy("has_silvered_clothing", has(silvered))
                    .save(recipe -> output.accept(new HunterUpgradeResult(recipe)));
        }
    }

    private static ShapedRecipeBuilder silverHunterClothing(Item plain, Item silvered) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, silvered)
                .define('W', WitcheryIngredient.WOLFSBANE.item()).define('A', plain).define('S', Items.STRING)
                .pattern("DWD").pattern("WAW").pattern("DSD").unlockedBy("has_hunter_clothing", has(plain));
    }

    private record HunterUpgradeResult(FinishedRecipe recipe) implements FinishedRecipe {
        @Override public ResourceLocation getId() { return recipe.getId(); }
        @Override public RecipeSerializer<?> getType() { return WitcheryRecipes.HUNTER_CLOTHING_UPGRADE.get(); }
        @Override public void serializeRecipeData(JsonObject json) { recipe.serializeRecipeData(json); }
        @Override public JsonObject serializeAdvancement() { return recipe.serializeAdvancement(); }
        @Override public ResourceLocation getAdvancementId() { return recipe.getAdvancementId(); }
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
