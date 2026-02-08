package com.emoniph.witchery;

import com.emoniph.witchery.crafting.DistilleryRecipes;
import com.emoniph.witchery.crafting.KettleRecipes;
import com.emoniph.witchery.crafting.RecipeAttachTaglock;
import com.emoniph.witchery.crafting.RecipeShapelessAddColor;
import com.emoniph.witchery.crafting.RecipeShapelessAddKeys;
import com.emoniph.witchery.crafting.RecipeShapelessAddPotion;
import com.emoniph.witchery.crafting.RecipeShapelessBiomeCopy;
import com.emoniph.witchery.crafting.RecipeShapelessPoppet;
import com.emoniph.witchery.crafting.RecipeShapelessRepair;
import com.emoniph.witchery.crafting.SpinningRecipes;
import com.emoniph.witchery.entity.EntityBabaYaga;
import com.emoniph.witchery.entity.EntityDemon;
import com.emoniph.witchery.entity.EntityEnt;
import com.emoniph.witchery.entity.EntityFamiliar;
import com.emoniph.witchery.entity.EntityImp;
import com.emoniph.witchery.entity.EntityOwl;
import com.emoniph.witchery.entity.EntityReflection;
import com.emoniph.witchery.entity.EntityToad;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.infusion.infusions.InfusionInfernal;
import com.emoniph.witchery.infusion.infusions.InfusionLight;
import com.emoniph.witchery.infusion.infusions.InfusionOtherwhere;
import com.emoniph.witchery.infusion.infusions.InfusionOverworld;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePower;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerBat;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerBlaze;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerCreeper;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerEnderman;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerGhast;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerHeal;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerJump;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerPigMan;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerSkeleton;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerSpeed;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerSpider;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerSquid;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerZombie;
import com.emoniph.witchery.predictions.PredictionArrow;
import com.emoniph.witchery.predictions.PredictionBuriedTreasure;
import com.emoniph.witchery.predictions.PredictionFall;
import com.emoniph.witchery.predictions.PredictionFallInLove;
import com.emoniph.witchery.predictions.PredictionFight;
import com.emoniph.witchery.predictions.PredictionManager;
import com.emoniph.witchery.predictions.PredictionMultiMine;
import com.emoniph.witchery.predictions.PredictionNetherTrip;
import com.emoniph.witchery.predictions.PredictionRescue;
import com.emoniph.witchery.predictions.PredictionWet;
import com.emoniph.witchery.ritual.Circle;
import com.emoniph.witchery.ritual.RiteRegistry;
import com.emoniph.witchery.ritual.RitualTraits;
import com.emoniph.witchery.ritual.Sacrifice;
import com.emoniph.witchery.ritual.SacrificeItem;
import com.emoniph.witchery.ritual.SacrificeLiving;
import com.emoniph.witchery.ritual.SacrificeMultiple;
import com.emoniph.witchery.ritual.SacrificeOptionalItem;
import com.emoniph.witchery.ritual.SacrificePower;
import com.emoniph.witchery.ritual.rites.RiteBanishDemon;
import com.emoniph.witchery.ritual.rites.RiteBindCircleToTalisman;
import com.emoniph.witchery.ritual.rites.RiteBindFamiliar;
import com.emoniph.witchery.ritual.rites.RiteBindSpiritsToFetish;
import com.emoniph.witchery.ritual.rites.RiteBlight;
import com.emoniph.witchery.ritual.rites.RiteBlindness;
import com.emoniph.witchery.ritual.rites.RiteCallCreatures;
import com.emoniph.witchery.ritual.rites.RiteCallFamiliar;
import com.emoniph.witchery.ritual.rites.RiteClimateChange;
import com.emoniph.witchery.ritual.rites.RiteCookItem;
import com.emoniph.witchery.ritual.rites.RiteCurseCreature;
import com.emoniph.witchery.ritual.rites.RiteCurseOfTheWolf;
import com.emoniph.witchery.ritual.rites.RiteCursePoppets;
import com.emoniph.witchery.ritual.rites.RiteEclipse;
import com.emoniph.witchery.ritual.rites.RiteFertility;
import com.emoniph.witchery.ritual.rites.RiteForestation;
import com.emoniph.witchery.ritual.rites.RiteGlyphicTransformation;
import com.emoniph.witchery.ritual.rites.RiteHellOnEarth;
import com.emoniph.witchery.ritual.rites.RiteInfusePlayers;
import com.emoniph.witchery.ritual.rites.RiteInfusionRecharge;
import com.emoniph.witchery.ritual.rites.RiteNaturesPower;
import com.emoniph.witchery.ritual.rites.RitePartEarth;
import com.emoniph.witchery.ritual.rites.RitePriorIncarnation;
import com.emoniph.witchery.ritual.rites.RiteProtectionCircleAttractive;
import com.emoniph.witchery.ritual.rites.RiteProtectionCircleBarrier;
import com.emoniph.witchery.ritual.rites.RiteProtectionCircleRepulsive;
import com.emoniph.witchery.ritual.rites.RiteRainOfToads;
import com.emoniph.witchery.ritual.rites.RiteRaiseColumn;
import com.emoniph.witchery.ritual.rites.RiteRaiseVolcano;
import com.emoniph.witchery.ritual.rites.RiteRemoveVampirism;
import com.emoniph.witchery.ritual.rites.RiteSetNBT;
import com.emoniph.witchery.ritual.rites.RiteSphereEffect;
import com.emoniph.witchery.ritual.rites.RiteSummonCreature;
import com.emoniph.witchery.ritual.rites.RiteSummonItem;
import com.emoniph.witchery.ritual.rites.RiteSummonSpectralStone;
import com.emoniph.witchery.ritual.rites.RiteTeleportEntity;
import com.emoniph.witchery.ritual.rites.RiteTeleportToWaystone;
import com.emoniph.witchery.ritual.rites.RiteTransposeOres;
import com.emoniph.witchery.ritual.rites.RiteWeatherCallStorm;
import com.emoniph.witchery.util.ClothColor;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Dye;
import cpw.mods.fml.common.registry.GameRegistry;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.oredict.RecipeSorter.Category;

public class WitcheryRecipes {
   public Infusion infusionEnder;
   public Infusion infusionLight;
   public Infusion infusionWorld;
   public Infusion infusionBeast;

   public void preInit() {
      RecipeSorter.register("witchery:bindpoppet", RecipeShapelessPoppet.class, Category.SHAPELESS, "after:minecraft:shapeless");
      RecipeSorter.register("witchery:addpotion", RecipeShapelessAddPotion.class, Category.SHAPELESS, "after:minecraft:shapeless");
      RecipeSorter.register("witchery:repair", RecipeShapelessRepair.class, Category.SHAPELESS, "after:minecraft:shapeless");
      RecipeSorter.register("witchery:addcolor", RecipeShapelessAddColor.class, Category.SHAPELESS, "after:minecraft:shapeless");
      RecipeSorter.register("witchery:addkeys", RecipeShapelessAddKeys.class, Category.SHAPELESS, "after:minecraft:shapeless");
      RecipeSorter.register("witchery:attachtaglock", RecipeAttachTaglock.class, Category.SHAPELESS, "after:minecraft:shapeless");
      RecipeSorter.register("witchery:biomecopy", RecipeShapelessBiomeCopy.class, Category.SHAPELESS, "after:minecraft:shapeless");
      if (Config.instance().allowStatueGoddessRecipe) {
         GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.STATUE_GODDESS), new Object[]{"s#s", "shs", "###", 'h', Witchery.Items.GENERIC.itemDemonHeart.createStack(), '#', new ItemStack(Blocks.field_150348_b), 's', new ItemStack(Items.field_151156_bN)});
      }

      ItemStack ash = Witchery.Items.GENERIC.itemAshWood.createStack();
      ItemStack bone = new ItemStack(Items.field_151103_aS);
      GameRegistry.addShapelessRecipe(Dye.BONE_MEAL.createStack(4), new Object[]{bone, ash, ash});
      GameRegistry.addShapelessRecipe(Dye.BONE_MEAL.createStack(5), new Object[]{bone, ash, ash, ash, ash});
      GameRegistry.addShapelessRecipe(Dye.BONE_MEAL.createStack(6), new Object[]{bone, ash, ash, ash, ash, ash, ash});
      GameRegistry.addShapelessRecipe(Dye.BONE_MEAL.createStack(7), new Object[]{bone, ash, ash, ash, ash, ash, ash, ash, ash});
      GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Witchery.Blocks.WICKER_BUNDLE, 1, 0), new Object[]{"###", "###", "###", '#', "treeSapling"}));
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.WICKER_BUNDLE, 5, 1), new Object[]{"#b#", "###", '#', new ItemStack(Witchery.Blocks.WICKER_BUNDLE), 'b', Witchery.Items.GENERIC.itemInfernalBlood.createStack()});
      this.addPlantMineRecipe(0, new ItemStack(Blocks.field_150328_O), Witchery.Items.GENERIC.itemBrewOfWebs.createStack());
      this.addPlantMineRecipe(1, new ItemStack(Blocks.field_150328_O), Witchery.Items.GENERIC.itemBrewOfInk.createStack());
      this.addPlantMineRecipe(2, new ItemStack(Blocks.field_150328_O), Witchery.Items.GENERIC.itemBrewOfThorns.createStack());
      this.addPlantMineRecipe(3, new ItemStack(Blocks.field_150328_O), Witchery.Items.GENERIC.itemBrewOfSprouting.createStack());
      this.addPlantMineRecipe(4, new ItemStack(Blocks.field_150327_N), Witchery.Items.GENERIC.itemBrewOfWebs.createStack());
      this.addPlantMineRecipe(5, new ItemStack(Blocks.field_150327_N), Witchery.Items.GENERIC.itemBrewOfInk.createStack());
      this.addPlantMineRecipe(6, new ItemStack(Blocks.field_150327_N), Witchery.Items.GENERIC.itemBrewOfThorns.createStack());
      this.addPlantMineRecipe(7, new ItemStack(Blocks.field_150327_N), Witchery.Items.GENERIC.itemBrewOfSprouting.createStack());
      this.addPlantMineRecipe(8, new ItemStack(Blocks.field_150330_I), Witchery.Items.GENERIC.itemBrewOfWebs.createStack());
      this.addPlantMineRecipe(9, new ItemStack(Blocks.field_150330_I), Witchery.Items.GENERIC.itemBrewOfInk.createStack());
      this.addPlantMineRecipe(10, new ItemStack(Blocks.field_150330_I), Witchery.Items.GENERIC.itemBrewOfThorns.createStack());
      this.addPlantMineRecipe(11, new ItemStack(Blocks.field_150330_I), Witchery.Items.GENERIC.itemBrewOfSprouting.createStack());
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151170_bI, 2), new Object[]{new ItemStack(Items.field_151170_bI), new ItemStack(Items.field_151174_bG), new ItemStack(Items.field_151070_bp)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.LEAPING_LILY, 5), new Object[]{"#p#", "c#c", "#b#", '#', new ItemStack(Blocks.field_150392_bi), 'p', new ItemStack(Items.field_151068_bn, 1, 8194), 'b', Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(), 'c', new ItemStack(Items.field_151114_aO)});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemBoneNeedle.createStack(8), new Object[]{"ab", 'a', new ItemStack(Items.field_151103_aS), 'b', new ItemStack(Items.field_151145_ak)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.TAGLOCK_KIT), new Object[]{"ab", 'b', Witchery.Items.GENERIC.itemBoneNeedle.createStack(), 'a', new ItemStack(Items.field_151069_bo)});
      ItemStack taglocks = new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1);
      ItemStack unboundPoppet = Witchery.Items.POPPET.unboundPoppet.createStack();
      GameRegistry.addRecipe(unboundPoppet, new Object[]{"xyx", "ayb", "x x", 'x', new ItemStack(Blocks.field_150325_L), 'y', new ItemStack(Witchery.Blocks.SPANISH_MOSS), 'a', Witchery.Items.GENERIC.itemBoneNeedle.createStack(), 'b', new ItemStack(Items.field_151007_F)});
      ItemStack earthPoppet = Witchery.Items.POPPET.earthPoppet.createStack();
      GameRegistry.addRecipe(Witchery.Items.POPPET.earthPoppet.createStack(), new Object[]{" a ", "b#b", " c ", '#', Witchery.Items.POPPET.unboundPoppet.createStack(), 'b', new ItemStack(Items.field_151008_G), 'a', new ItemStack(Items.field_151119_aD), 'c', new ItemStack(Blocks.field_150346_d)});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessPoppet(earthPoppet, new ItemStack[]{taglocks, earthPoppet}));
      ItemStack waterPoppet = Witchery.Items.POPPET.waterPoppet.createStack();
      GameRegistry.addRecipe(waterPoppet, new Object[]{" a ", "b#b", " a ", '#', Witchery.Items.POPPET.unboundPoppet.createStack(), 'a', Witchery.Items.GENERIC.itemArtichoke.createStack(), 'b', Dye.INK_SAC.createStack()});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessPoppet(waterPoppet, new ItemStack[]{taglocks, waterPoppet}));
      ItemStack foodPoppet = Witchery.Items.POPPET.foodPoppet.createStack();
      GameRegistry.addRecipe(foodPoppet, new Object[]{" a ", "b#b", " a ", '#', unboundPoppet, 'b', new ItemStack(Items.field_151060_bw), 'a', new ItemStack(Items.field_151078_bh)});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessPoppet(foodPoppet, new ItemStack[]{taglocks, foodPoppet}));
      ItemStack firePoppet = Witchery.Items.POPPET.firePoppet.createStack();
      GameRegistry.addRecipe(firePoppet, new Object[]{" a ", "b#b", " a ", '#', unboundPoppet, 'b', Witchery.Items.GENERIC.itemBatWool.createStack(), 'a', new ItemStack(Witchery.Blocks.EMBER_MOSS)});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessPoppet(firePoppet, new ItemStack[]{taglocks, firePoppet}));
      ItemStack antiVoodooPoppet = Witchery.Items.POPPET.antiVoodooPoppet.createStack();
      GameRegistry.addRecipe(antiVoodooPoppet, new Object[]{"ced", "a#b", "dfc", '#', unboundPoppet, 'a', Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), 'b', Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), 'c', new ItemStack(Blocks.field_150327_N), 'd', new ItemStack(Blocks.field_150328_O), 'e', new ItemStack(Blocks.field_150337_Q), 'f', new ItemStack(Blocks.field_150338_P)});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessPoppet(antiVoodooPoppet, new ItemStack[]{taglocks, antiVoodooPoppet}));
      ItemStack poppetProectionPoppet = Witchery.Items.POPPET.poppetProtectionPoppet.createStack();
      GameRegistry.addRecipe(poppetProectionPoppet, new Object[]{"gfg", "e#e", "glg", '#', antiVoodooPoppet, 'l', Witchery.Items.GENERIC.itemDropOfLuck.createStack(), 'e', Witchery.Items.GENERIC.itemEnderDew.createStack(), 'g', new ItemStack(Items.field_151074_bl), 'f', Witchery.Items.GENERIC.itemToeOfFrog.createStack()});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessPoppet(poppetProectionPoppet, new ItemStack[]{taglocks, poppetProectionPoppet}));
      ItemStack voodooPoppet = Witchery.Items.POPPET.voodooPoppet.createStack();
      GameRegistry.addRecipe(voodooPoppet, new Object[]{" d ", "a#b", " c ", '#', unboundPoppet, 'a', Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), 'b', Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), 'c', Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), 'd', new ItemStack(Items.field_151071_bq)});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessPoppet(voodooPoppet, new ItemStack[]{taglocks, voodooPoppet}));
      ItemStack toolPoppet = Witchery.Items.POPPET.toolPoppet.createStack();
      GameRegistry.addRecipe(toolPoppet, new Object[]{" a ", "b#b", " a ", '#', unboundPoppet, 'a', Witchery.Items.GENERIC.itemReekOfMisfortune.createStack(), 'b', Witchery.Items.GENERIC.itemDropOfLuck.createStack()});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessPoppet(toolPoppet, new ItemStack[]{taglocks, toolPoppet}));
      ItemStack armorPoppet = Witchery.Items.POPPET.armorPoppet.createStack();
      GameRegistry.addRecipe(armorPoppet, new Object[]{" a ", "b#b", " d ", '#', unboundPoppet, 'a', Witchery.Items.GENERIC.itemReekOfMisfortune.createStack(), 'b', Witchery.Items.GENERIC.itemDropOfLuck.createStack(), 'd', Witchery.Items.GENERIC.itemDiamondVapour.createStack()});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessPoppet(armorPoppet, new ItemStack[]{taglocks, armorPoppet}));
      ItemStack avoidDeathPoppet = Witchery.Items.POPPET.deathPoppet.createStack();
      GameRegistry.addRecipe(avoidDeathPoppet, new Object[]{"axb", "x#x", " x ", '#', unboundPoppet, 'a', Witchery.Items.GENERIC.itemDropOfLuck.createStack(), 'b', Witchery.Items.GENERIC.itemDiamondVapour.createStack(), 'x', new ItemStack(Items.field_151074_bl)});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessPoppet(avoidDeathPoppet, new ItemStack[]{taglocks, avoidDeathPoppet}));
      ItemStack vampiricPoppet = Witchery.Items.POPPET.vampiricPoppet.createStack();
      GameRegistry.addRecipe(vampiricPoppet, new Object[]{" b ", "c#c", " a ", '#', unboundPoppet, 'a', Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), 'b', Witchery.Items.GENERIC.itemDiamondVapour.createStack(), 'c', Witchery.Items.GENERIC.itemBatWool.createStack()});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessPoppet(vampiricPoppet, new ItemStack[]{taglocks, taglocks, vampiricPoppet}));
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.POPPET_SHELF), new Object[]{"yzy", "zxz", "yzy", 'x', ClothColor.GREEN.createStack(), 'y', Witchery.Items.GENERIC.itemAttunedStone.createStack(), 'z', new ItemStack(Blocks.field_150385_bj)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.OVEN_IDLE), new Object[]{" z ", "xxx", "xzx", 'x', new ItemStack(Items.field_151042_j), 'z', new ItemStack(Blocks.field_150411_aY)});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemSoftClayJar.createStack(4), new Object[]{" # ", "###", '#', new ItemStack(Items.field_151119_aD)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.PLANKS, 4, 0), new Object[]{"#", '#', new ItemStack(Witchery.Blocks.LOG, 1, 0)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.PLANKS, 4, 1), new Object[]{"#", '#', new ItemStack(Witchery.Blocks.LOG, 1, 1)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.PLANKS, 4, 2), new Object[]{"#", '#', new ItemStack(Witchery.Blocks.LOG, 1, 2)});
      CraftingManager.func_77594_a().func_77592_b().add(0, getShapedRecipe(Witchery.Items.GENERIC.itemDoorRowan.createStack(), "##", "##", "##", '#', new ItemStack(Witchery.Blocks.PLANKS, 1, 0)));
      CraftingManager.func_77594_a().func_77592_b().add(0, getShapedRecipe(Witchery.Items.GENERIC.itemDoorAlder.createStack(), "##", "##", "##", '#', new ItemStack(Witchery.Blocks.PLANKS, 1, 1)));
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.STAIRS_ALDER, 4, 0), new Object[]{"#  ", "## ", "###", '#', new ItemStack(Witchery.Blocks.PLANKS, 1, 1)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.STAIRS_HAWTHORN, 4, 0), new Object[]{"#  ", "## ", "###", '#', new ItemStack(Witchery.Blocks.PLANKS, 1, 2)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.STAIRS_ROWAN, 4, 0), new Object[]{"#  ", "## ", "###", '#', new ItemStack(Witchery.Blocks.PLANKS, 1, 0)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.SNOW_STAIRS, 4, 0), new Object[]{"#  ", "## ", "###", '#', new ItemStack(Blocks.field_150433_aE, 1, 0)});
      CraftingManager.func_77594_a().func_77592_b().add(0, getShapedRecipe(new ItemStack(Witchery.Blocks.SNOW_SLAB_SINGLE, 6, 0), "###", "###", '#', new ItemStack(Blocks.field_150431_aC, 1, 0)));
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.SNOW_PRESSURE_PLATE, 1, 0), new Object[]{"##", '#', new ItemStack(Blocks.field_150433_aE, 1, 0)});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemQuicklime.createStack(), new Object[]{"#", '#', Witchery.Items.GENERIC.itemAshWood.createStack()});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.ALTAR, 3), new Object[]{"abc", "xyx", "xyx", 'a', Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(), 'b', new ItemStack(Items.field_151068_bn), 'c', Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), 'x', new ItemStack(Blocks.field_150417_aV, 1, 0), 'y', new ItemStack(Witchery.Blocks.LOG, 1, 0)});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemAttunedStone.createStack(), new Object[]{"a", "b", "c", 'a', Witchery.Items.GENERIC.itemWhiffOfMagic.createStack(), 'b', new ItemStack(Items.field_151045_i), 'c', new ItemStack(Items.field_151129_at)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.DISTILLERY_IDLE), new Object[]{"bxb", "xxx", "yay", 'a', Witchery.Items.GENERIC.itemAttunedStone.createStack(), 'b', Witchery.Items.GENERIC.itemEmptyClayJar.createStack(), 'y', new ItemStack(Items.field_151043_k), 'x', new ItemStack(Items.field_151042_j)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.KETTLE), new Object[]{"bxb", "xax", " y ", 'a', Witchery.Items.GENERIC.itemAttunedStone.createStack(), 'b', new ItemStack(Items.field_151055_y), 'x', new ItemStack(Items.field_151007_F), 'y', new ItemStack(Items.field_151066_bu)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.BRAZIER), new Object[]{"#a#", " w ", "www", 'a', Witchery.Items.GENERIC.itemNecroStone.createStack(), 'w', new ItemStack(Items.field_151055_y), '#', new ItemStack(Items.field_151042_j)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.CHALK_RITUAL, 2, 0), new Object[]{"xax", "xyx", "xyx", 'a', Witchery.Items.GENERIC.itemTearOfTheGoddess.createStack(), 'x', Witchery.Items.GENERIC.itemAshWood.createStack(), 'y', Witchery.Items.GENERIC.itemGypsum.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemWaystone.createStack(), new Object[]{"ab", 'a', new ItemStack(Items.field_151145_ak), 'b', Witchery.Items.GENERIC.itemBoneNeedle.createStack()});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.ARTHANA), new Object[]{" y ", "xbx", " a ", 'a', new ItemStack(Items.field_151055_y), 'b', new ItemStack(Items.field_151166_bC), 'y', new ItemStack(Items.field_151043_k), 'x', new ItemStack(Items.field_151074_bl)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.BOLINE), new Object[]{"y", "a", "b", 'a', new ItemStack(Items.field_151103_aS), 'b', new ItemStack(Items.field_151166_bC), 'y', new ItemStack(Items.field_151042_j)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.CIRCLE_TALISMAN), new Object[]{"yxy", "xax", "yxy", 'a', new ItemStack(Items.field_151045_i), 'x', new ItemStack(Items.field_151043_k), 'y', new ItemStack(Items.field_151074_bl)});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemBroom.createStack(), new Object[]{" x ", " x ", "yyy", 'x', new ItemStack(Items.field_151055_y), 'y', new ItemStack(Witchery.Blocks.SAPLING, 1, 2)});
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemOddPorkRaw.createStack(), new Object[]{Witchery.Items.GENERIC.itemMutandis.createStack(), new ItemStack(Items.field_151078_bh)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151147_al), new Object[]{Witchery.Items.GENERIC.itemMutandis.createStack(), Witchery.Items.GENERIC.itemOddPorkRaw.createStack()});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151147_al), new Object[]{Witchery.Items.GENERIC.itemMutandis.createStack(), new ItemStack(Items.field_151076_bf)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151076_bf), new Object[]{Witchery.Items.GENERIC.itemMutandis.createStack(), new ItemStack(Items.field_151082_bd)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151082_bd), new Object[]{Witchery.Items.GENERIC.itemMutandis.createStack(), new ItemStack(Items.field_151147_al)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151157_am), new Object[]{Witchery.Items.GENERIC.itemMutandis.createStack(), Witchery.Items.GENERIC.itemOddPorkCooked.createStack()});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151157_am), new Object[]{Witchery.Items.GENERIC.itemMutandis.createStack(), new ItemStack(Items.field_151077_bg)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151077_bg), new Object[]{Witchery.Items.GENERIC.itemMutandis.createStack(), new ItemStack(Items.field_151083_be)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151083_be), new Object[]{Witchery.Items.GENERIC.itemMutandis.createStack(), new ItemStack(Items.field_151157_am)});
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemOddPorkCooked.createStack(), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151078_bh)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151157_am), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), Witchery.Items.GENERIC.itemOddPorkRaw.createStack()});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151157_am), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151076_bf)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151077_bg), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151082_bd)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151083_be), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151147_al)});
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemOddPorkRaw.createStack(), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151078_bh)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151147_al), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), Witchery.Items.GENERIC.itemOddPorkRaw.createStack()});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151147_al), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151076_bf)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151076_bf), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151082_bd)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151082_bd), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151147_al)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151157_am), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), Witchery.Items.GENERIC.itemOddPorkCooked.createStack()});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151157_am), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151077_bg)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151077_bg), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151083_be)});
      GameRegistry.addShapelessRecipe(new ItemStack(Items.field_151083_be), new Object[]{Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151157_am)});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemCandelabra.createStack(), new Object[]{"xxx", "yay", " y ", 'x', new ItemStack(Blocks.field_150478_aa), 'y', new ItemStack(Items.field_151042_j), 'a', Witchery.Items.GENERIC.itemAttunedStone.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemChaliceEmpty.createStack(), new Object[]{"yay", "yxy", " x ", 'x', new ItemStack(Items.field_151043_k), 'y', new ItemStack(Items.field_151074_bl), 'a', Witchery.Items.GENERIC.itemAttunedStone.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemChaliceFull.createStack(), new Object[]{"b", "a", 'a', Witchery.Items.GENERIC.itemChaliceEmpty.createStack(), 'b', Witchery.Items.GENERIC.itemRedstoneSoup.createStack()});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.DIVINER_WATER), new Object[]{"yay", "yay", "axa", 'a', new ItemStack(Items.field_151055_y), 'y', new ItemStack(Items.field_151068_bn), 'x', Witchery.Items.GENERIC.itemTearOfTheGoddess.createStack()});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.DIVINER_LAVA), new Object[]{" a ", " x ", "a a", 'x', new ItemStack(Witchery.Items.DIVINER_WATER), 'a', new ItemStack(Items.field_151072_bj)});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemDreamMove.createStack(), new Object[]{"dxe", "bab", "cfc", 'a', new ItemStack(Items.field_151160_bD), 'b', Witchery.Items.GENERIC.itemFancifulThread.createStack(), 'f', Witchery.Items.GENERIC.itemTormentedTwine.createStack(), 'c', new ItemStack(Items.field_151008_G), 'd', new ItemStack(Items.field_151068_bn, 1, 16450), 'e', new ItemStack(Items.field_151068_bn, 1, 16458), 'x', Witchery.Items.GENERIC.itemDiamondVapour.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemDreamMove.createStack(), new Object[]{"dxe", "bab", "cfc", 'a', new ItemStack(Items.field_151160_bD), 'b', Witchery.Items.GENERIC.itemFancifulThread.createStack(), 'f', Witchery.Items.GENERIC.itemTormentedTwine.createStack(), 'c', new ItemStack(Items.field_151008_G), 'd', new ItemStack(Items.field_151068_bn, 1, 16450), 'e', new ItemStack(Items.field_151068_bn, 1, 24650), 'x', Witchery.Items.GENERIC.itemDiamondVapour.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemDreamDig.createStack(), new Object[]{"dxe", "bab", "cfc", 'a', new ItemStack(Items.field_151160_bD), 'b', Witchery.Items.GENERIC.itemFancifulThread.createStack(), 'f', Witchery.Items.GENERIC.itemTormentedTwine.createStack(), 'c', new ItemStack(Items.field_151008_G), 'd', new ItemStack(Items.field_151068_bn, 1, 16457), 'e', new ItemStack(Items.field_151068_bn, 1, 16456), 'x', Witchery.Items.GENERIC.itemDiamondVapour.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemDreamDig.createStack(), new Object[]{"dxe", "bab", "cfc", 'a', new ItemStack(Items.field_151160_bD), 'b', Witchery.Items.GENERIC.itemFancifulThread.createStack(), 'f', Witchery.Items.GENERIC.itemTormentedTwine.createStack(), 'c', new ItemStack(Items.field_151008_G), 'd', new ItemStack(Items.field_151068_bn, 1, 16457), 'e', new ItemStack(Items.field_151068_bn, 1, 24648), 'x', Witchery.Items.GENERIC.itemDiamondVapour.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemDreamEat.createStack(), new Object[]{"dxe", "bab", "cfc", 'a', new ItemStack(Items.field_151160_bD), 'b', Witchery.Items.GENERIC.itemFancifulThread.createStack(), 'f', Witchery.Items.GENERIC.itemTormentedTwine.createStack(), 'c', new ItemStack(Items.field_151008_G), 'd', new ItemStack(Items.field_151068_bn, 1, 16421), 'e', Witchery.Items.GENERIC.itemMellifluousHunger.createStack(), 'x', Witchery.Items.GENERIC.itemDiamondVapour.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemDreamEat.createStack(), new Object[]{"dxe", "bab", "cfc", 'a', new ItemStack(Items.field_151160_bD), 'b', Witchery.Items.GENERIC.itemFancifulThread.createStack(), 'f', Witchery.Items.GENERIC.itemTormentedTwine.createStack(), 'c', new ItemStack(Items.field_151008_G), 'd', new ItemStack(Items.field_151068_bn, 1, 16421), 'e', Witchery.Items.GENERIC.itemMellifluousHunger.createStack(), 'x', Witchery.Items.GENERIC.itemDiamondVapour.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemDreamNightmare.createStack(), new Object[]{"dxe", "bab", "cbc", 'a', new ItemStack(Items.field_151160_bD), 'b', Witchery.Items.GENERIC.itemTormentedTwine.createStack(), 'c', new ItemStack(Items.field_151008_G), 'd', new ItemStack(Items.field_151068_bn, 1, 16452), 'e', new ItemStack(Items.field_151068_bn, 1, 16454), 'x', Witchery.Items.GENERIC.itemDiamondVapour.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemDreamIntensity.createStack(), new Object[]{"dxe", "bab", "cfc", 'a', new ItemStack(Items.field_151160_bD), 'b', Witchery.Items.GENERIC.itemFancifulThread.createStack(), 'f', Witchery.Items.GENERIC.itemTormentedTwine.createStack(), 'c', new ItemStack(Items.field_151008_G), 'd', Witchery.Items.GENERIC.itemBrewOfFlowingSpirit.createStack(), 'e', Witchery.Items.GENERIC.itemBrewOfSleeping.createStack(), 'x', Witchery.Items.GENERIC.itemDiamondVapour.createStack()});
      GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Witchery.Items.CAULDRON_BOOK), new Object[]{" c ", "a#b", '#', new ItemStack(Items.field_151122_aG), 'a', "dyeBlack", 'b', new ItemStack(Items.field_151008_G), 'c', new ItemStack(Blocks.field_150346_d)}));
      GameRegistry.addRecipe(new ShapedOreRecipe(Witchery.Items.GENERIC.itemBookHerbology.createStack(), new Object[]{" c ", "a#b", " d ", '#', new ItemStack(Items.field_151122_aG), 'a', "dyeBlack", 'b', new ItemStack(Items.field_151008_G), 'c', new ItemStack(Blocks.field_150328_O), 'd', new ItemStack(Blocks.field_150327_N)}));
      GameRegistry.addRecipe(new ShapedOreRecipe(Witchery.Items.GENERIC.itemBookWands.createStack(), new Object[]{" c ", "a#b", "   ", '#', new ItemStack(Items.field_151122_aG), 'a', "dyeBlack", 'b', new ItemStack(Items.field_151008_G), 'c', Witchery.Items.GENERIC.itemBranchEnt.createStack()}));
      GameRegistry.addRecipe(new ShapedOreRecipe(Witchery.Items.GENERIC.itemBookBiomes.createStack(), new Object[]{" c ", "a#b", " d ", '#', new ItemStack(Items.field_151122_aG), 'a', "dyeBlack", 'b', new ItemStack(Items.field_151008_G), 'c', new ItemStack(Blocks.field_150345_g), 'd', new ItemStack(Blocks.field_150348_b)}));
      GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Witchery.Items.BIOME_BOOK), new Object[]{" d ", "d#d", " d ", '#', Witchery.Items.GENERIC.itemBookBiomes.createStack(), 'd', new ItemStack(Blocks.field_150348_b)}));
      GameRegistry.addRecipe(new ShapedOreRecipe(Witchery.Items.GENERIC.itemBookBurning.createStack(), new Object[]{" c ", "a#b", " d ", '#', new ItemStack(Items.field_151122_aG), 'a', "dyeBlack", 'b', new ItemStack(Items.field_151008_G), 'c', Witchery.Items.GENERIC.itemAshWood.createStack(), 'd', new ItemStack(Items.field_151033_d)}));
      GameRegistry.addRecipe(new ShapedOreRecipe(Witchery.Items.GENERIC.itemBookOven.createStack(), new Object[]{" c ", "a#b", " d ", '#', new ItemStack(Items.field_151122_aG), 'a', "dyeBlack", 'b', new ItemStack(Items.field_151008_G), 'c', Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), 'd', new ItemStack(Items.field_151044_h, 1, 1)}));
      GameRegistry.addRecipe(new ShapedOreRecipe(Witchery.Items.GENERIC.itemBookDistilling.createStack(), new Object[]{" c ", "a#b", " d ", '#', new ItemStack(Items.field_151122_aG), 'a', "dyeBlack", 'b', new ItemStack(Items.field_151008_G), 'c', Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), 'd', Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack()}));
      GameRegistry.addRecipe(new ShapedOreRecipe(Witchery.Items.GENERIC.itemBookCircleMagic.createStack(), new Object[]{" c ", "a#b", " d ", '#', new ItemStack(Items.field_151122_aG), 'a', "dyeBlack", 'b', new ItemStack(Items.field_151008_G), 'c', Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), 'd', Witchery.Items.GENERIC.itemWhiffOfMagic.createStack()}));
      GameRegistry.addRecipe(new ShapedOreRecipe(Witchery.Items.GENERIC.itemBookInfusions.createStack(), new Object[]{" c ", "a#b", " d ", '#', new ItemStack(Items.field_151122_aG), 'a', "dyeBlack", 'b', new ItemStack(Items.field_151008_G), 'c', Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), 'd', Witchery.Items.GENERIC.itemOdourOfPurity.createStack()}));
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemWeb.createStack(), new Object[]{" s ", "sws", " s ", 's', new ItemStack(Items.field_151007_F), 'w', new ItemStack(Blocks.field_150321_G)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.ALLURING_SKULL), new Object[]{" a ", "bcb", " d ", 'a', Witchery.Items.GENERIC.itemNecroStone.createStack(), 'd', Witchery.Items.POPPET.voodooPoppet.createStack(), 'c', new ItemStack(Items.field_151144_bL), 'b', new ItemStack(Items.field_151114_aO)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.ALLURING_SKULL), new Object[]{" a ", "bcb", " d ", 'a', Witchery.Items.GENERIC.itemNecroStone.createStack(), 'd', Witchery.Items.POPPET.voodooPoppet.createStack(), 'c', new ItemStack(Items.field_151144_bL, 1, 1), 'b', new ItemStack(Items.field_151114_aO)});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemSeedsTreefyd.createStack(2), new Object[]{"xax", "cyd", "xbx", 'x', Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), 'y', Witchery.Items.GENERIC.itemArtichoke.createStack(), 'c', new ItemStack(Witchery.Blocks.EMBER_MOSS), 'd', Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), 'a', Witchery.Items.GENERIC.itemReekOfMisfortune.createStack(), 'b', Witchery.Items.GENERIC.itemTearOfTheGoddess.createStack()});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.POLYNESIA_CHARM, 1), new Object[]{"nin", "p#p", "nwn", '#', new ItemStack(Items.field_151115_aP), 'i', new ItemStack(Items.field_151042_j), 'p', Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), 'w', Witchery.Items.GENERIC.itemWhiffOfMagic.createStack(), 'n', new ItemStack(Items.field_151075_bm)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.DEVILS_TONGUE_CHARM, 1), new Object[]{"b#b", "dse", "btb", '#', new ItemStack(Witchery.Items.POLYNESIA_CHARM), 'd', Witchery.Items.GENERIC.itemDemonHeart.createStack(), 't', Witchery.Items.GENERIC.itemDogTongue.createStack(), 'e', Witchery.Items.GENERIC.itemRefinedEvil.createStack(), 's', new ItemStack(Items.field_151144_bL), 'b', new ItemStack(Items.field_151065_br)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.OVEN_FUMEFUNNEL), new Object[]{"ele", "ege", "bib", 'e', new ItemStack(Items.field_151133_ar), 'l', new ItemStack(Items.field_151129_at), 'b', new ItemStack(Blocks.field_150339_S), 'g', new ItemStack(Blocks.field_150426_aN), 'i', new ItemStack(Blocks.field_150411_aY)});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemFumeFilter.createStack(), new Object[]{"ggg", "sas", "ggg", 'g', new ItemStack(Blocks.field_150359_w), 's', new ItemStack(Items.field_151042_j), 'a', Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()});
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.OVEN_FUMEFUNNEL_FILTERED), new Object[]{"b", "f", 'b', new ItemStack(Witchery.Blocks.OVEN_FUMEFUNNEL), 'f', Witchery.Items.GENERIC.itemFumeFilter.createStack()});
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemPurifiedMilk.createStack(3), new Object[]{new ItemStack(Items.field_151117_aB), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), Witchery.Items.GENERIC.itemEmptyClayJar.createStack(), Witchery.Items.GENERIC.itemEmptyClayJar.createStack(), Witchery.Items.GENERIC.itemEmptyClayJar.createStack()});
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemPurifiedMilk.createStack(3), new Object[]{new ItemStack(Items.field_151105_aU), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), Witchery.Items.GENERIC.itemEmptyClayJar.createStack(), Witchery.Items.GENERIC.itemEmptyClayJar.createStack(), Witchery.Items.GENERIC.itemEmptyClayJar.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemImpregnatedLeather.createStack(4), new Object[]{"mlm", "ldl", "mlm", 'l', new ItemStack(Items.field_151116_aA), 'd', Witchery.Items.GENERIC.itemDiamondVapour.createStack(), 'm', Witchery.Items.GENERIC.itemWhiffOfMagic.createStack()});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.WITCH_HAT), new Object[]{" l ", "sls", "lgl", 's', Witchery.Items.GENERIC.itemGoldenThread.createStack(), 'l', Witchery.Items.GENERIC.itemImpregnatedLeather.createStack(), 'g', new ItemStack(Items.field_151114_aO)});
      if (Config.instance().allowVoidBrambleRecipe) {
         GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.VOID_BRAMBLE, 4), new Object[]{"lml", "r#r", "lml", '#', new ItemStack(Witchery.Blocks.BRAMBLE), 'r', new ItemStack(Items.field_151156_bN), 'l', Witchery.Items.GENERIC.itemDropOfLuck.createStack(), 'm', Witchery.Items.GENERIC.itemMutandisExtremis.createStack()});
      }

      GameRegistry.addRecipe(new ItemStack(Items.field_151016_H, 5), new Object[]{"#", '#', Witchery.Items.GENERIC.itemCreeperHeart.createStack()});
      GameRegistry.addShapelessRecipe(new ItemStack(Blocks.field_150424_aL), new Object[]{Witchery.Items.GENERIC.itemInfernalBlood.createStack(), new ItemStack(Blocks.field_150351_n)});
      ItemStack impregLeather = Witchery.Items.GENERIC.itemImpregnatedLeather.createStack();
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.WITCH_ROBES), new Object[]{"lsl", "l#l", "lll", 's', Witchery.Items.GENERIC.itemGoldenThread.createStack(), 'l', impregLeather, '#', Witchery.Items.GENERIC.itemCreeperHeart.createStack()});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.NECROMANCERS_ROBES), new Object[]{"lsl", "l#l", "lll", 's', Witchery.Items.GENERIC.itemGoldenThread.createStack(), 'l', impregLeather, '#', Witchery.Items.GENERIC.itemNecroStone.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemFrozenHeart.createStack(), new Object[]{"n", "h", "t", 'h', Witchery.Items.GENERIC.itemCreeperHeart.createStack(), 'n', Witchery.Items.GENERIC.itemIcyNeedle.createStack(), 't', new ItemStack(Items.field_151073_bk)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.ICY_SLIPPERS), new Object[]{"lsl", "l#l", "dod", 'l', impregLeather, 's', Witchery.Items.GENERIC.itemGoldenThread.createStack(), '#', Witchery.Items.GENERIC.itemFrozenHeart.createStack(), 'd', Witchery.Items.GENERIC.itemDiamondVapour.createStack(), 'o', Witchery.Items.GENERIC.itemOdourOfPurity.createStack()});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.BITING_BELT), new Object[]{"#lh", "lsl", "l l", 'l', impregLeather, 's', Witchery.Items.GENERIC.itemGoldenThread.createStack(), 'h', Witchery.Items.GENERIC.itemMellifluousHunger.createStack(), '#', new ItemStack(Witchery.Items.PARASYTIC_LOUSE)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.SEEPING_SHOES), new Object[]{"lsl", "hrh", "mmm", 'l', impregLeather, 's', Witchery.Items.GENERIC.itemGoldenThread.createStack(), 'h', new ItemStack(Witchery.Items.WITCH_HAND), 'r', Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), 'm', new ItemStack(Items.field_151117_aB)});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.RUBY_SLIPPERS), new Object[]{"aba", "tst", "aba", 's', new ItemStack(Witchery.Items.SEEPING_SHOES), 't', Witchery.Items.GENERIC.itemGoldenThread.createStack(), 'a', Witchery.Items.GENERIC.itemAttunedStone.createStack(), 'b', Witchery.Items.GENERIC.itemInfernalBlood.createStack()});
      GameRegistry.addRecipe(new ItemStack(Witchery.Items.BARK_BELT), new Object[]{"ses", "gbg", "shs", 'b', new ItemStack(Witchery.Items.BITING_BELT), 's', Witchery.Items.GENERIC.itemBrewOfFlowingSpirit.createStack(), 'g', Witchery.Items.GENERIC.itemBranchEnt.createStack(), 'h', Witchery.Items.GENERIC.itemCreeperHeart.createStack(), 'e', new ItemStack(Items.field_151166_bC)});
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemWormyApple.createStack(), new Object[]{new ItemStack(Items.field_151034_e), new ItemStack(Items.field_151078_bh), new ItemStack(Items.field_151102_aT)});
      ItemStack louse = new ItemStack(Witchery.Items.PARASYTIC_LOUSE, 1, 32767);
      ItemStack belt = new ItemStack(Witchery.Items.BITING_BELT, 1, 32767);
      int[] lousePotions = new int[]{8200, 8202, 8264, 8266, 8193, 8194, 8196, 8225, 8226, 8227, 8228, 8229, 8230, 8232, 8233, 8234, 8236, 8238, 8257, 8258, 8259, 8260, 8261, 8262, 8264, 8265, 8266, 8268, 8270, 8201, 8206};
      int[] arr$ = lousePotions;
      int i = lousePotions.length;

      int i$;
      int i;
      for(i$ = 0; i$ < i; ++i$) {
         i = arr$[i$];
         GameRegistry.addShapelessRecipe(new ItemStack(Witchery.Items.PARASYTIC_LOUSE, 1, i), new Object[]{louse, new ItemStack(Items.field_151068_bn, 1, i)});
         CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessAddPotion(new ItemStack(Witchery.Items.BITING_BELT, 1, i), new ItemStack[]{belt, new ItemStack(Items.field_151068_bn, 1, i)}));
      }

      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessRepair(new ItemStack(Witchery.Items.WITCH_ROBES), new ItemStack[]{new ItemStack(Witchery.Items.WITCH_ROBES), impregLeather, impregLeather, impregLeather, impregLeather}));
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessRepair(new ItemStack(Witchery.Items.NECROMANCERS_ROBES), new ItemStack[]{new ItemStack(Witchery.Items.NECROMANCERS_ROBES), impregLeather, impregLeather, impregLeather, impregLeather}));
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessRepair(new ItemStack(Witchery.Items.WITCH_HAT), new ItemStack[]{new ItemStack(Witchery.Items.WITCH_HAT), impregLeather, impregLeather, impregLeather}));
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessRepair(new ItemStack(Witchery.Items.ICY_SLIPPERS), new ItemStack[]{new ItemStack(Witchery.Items.ICY_SLIPPERS), impregLeather, impregLeather, impregLeather}));
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessRepair(new ItemStack(Witchery.Items.RUBY_SLIPPERS), new ItemStack[]{new ItemStack(Witchery.Items.RUBY_SLIPPERS), impregLeather, impregLeather, impregLeather}));
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessRepair(new ItemStack(Witchery.Items.SEEPING_SHOES), new ItemStack[]{new ItemStack(Witchery.Items.SEEPING_SHOES), impregLeather, impregLeather, impregLeather}));
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessRepair(new ItemStack(Witchery.Items.BITING_BELT), new ItemStack[]{new ItemStack(Witchery.Items.BITING_BELT), impregLeather, impregLeather, impregLeather, impregLeather}));
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessRepair(new ItemStack(Witchery.Items.BARK_BELT), new ItemStack[]{new ItemStack(Witchery.Items.BARK_BELT), impregLeather, impregLeather, impregLeather, impregLeather}));
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessRepair(new ItemStack(Witchery.Items.BABAS_HAT), new ItemStack[]{new ItemStack(Witchery.Items.BABAS_HAT), impregLeather, impregLeather, impregLeather}));
      Dye[] arr$ = Dye.DYES;
      i = arr$.length;

      for(i$ = 0; i$ < i; ++i$) {
         Dye dye = arr$[i$];
         CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessAddColor(new ItemStack(Witchery.Items.BREW_BAG), new ItemStack[]{new ItemStack(Witchery.Items.BREW_BAG), dye.createStack()}));
      }

      GameRegistry.addRecipe(new ItemStack(Witchery.Items.BREW_BAG), new Object[]{"lll", "lsl", "lll", 'l', impregLeather, 's', Witchery.Items.GENERIC.itemGoldenThread.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemCharmOfDisruptedDreams.createStack(), new Object[]{"lll", "lsl", "lll", 'l', new ItemStack(Items.field_151055_y), 's', Witchery.Items.GENERIC.itemFancifulThread.createStack()});
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessAddKeys(Witchery.Items.GENERIC.itemDoorKeyring.createStack(), new ItemStack[]{Witchery.Items.GENERIC.itemDoorKey.createStack(), Witchery.Items.GENERIC.itemDoorKey.createStack()}));
      CraftingManager.func_77594_a().func_77592_b().add(new RecipeShapelessAddKeys(Witchery.Items.GENERIC.itemDoorKeyring.createStack(), new ItemStack[]{Witchery.Items.GENERIC.itemDoorKeyring.createStack(), Witchery.Items.GENERIC.itemDoorKey.createStack()}));
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemQuartzSphere.createStack(), new Object[]{"qbq", "bgb", "qbq", 'q', new ItemStack(Items.field_151128_bU), 'b', new ItemStack(Blocks.field_150371_ca), 'g', new ItemStack(Blocks.field_150359_w)});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemSleepingApple.createStack(), new Object[]{" g ", "mam", "gsg", 'a', Witchery.Items.GENERIC.itemWormyApple.createStack(), 'g', Witchery.Items.GENERIC.itemMutandis.createStack(), 'm', Witchery.Items.GENERIC.itemReekOfMisfortune.createStack(), 's', Witchery.Items.GENERIC.itemBrewOfSleeping.createStack()});
      GameRegistry.addRecipe(Witchery.Items.GENERIC.itemBatBall.createStack(), new Object[]{"sbs", "b b", "sbs", 's', new ItemStack(Items.field_151123_aH), 'b', new ItemStack(Witchery.Blocks.CRITTER_SNARE, 1, 1)});
      GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Witchery.Blocks.SPINNING_WHEEL), new Object[]{"aab", "aac", "wsw", 'a', new ItemStack(Items.field_151160_bD), 'b', new ItemStack(Blocks.field_150325_L), 'c', "stickWood", 'w', "plankWood", 's', Witchery.Items.GENERIC.itemAttunedStone.createStack()}));
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemGraveyardDust.createStack(), new Object[]{Witchery.Items.GENERIC.itemSpectralDust.createStack(), Dye.BONE_MEAL.createStack(), Witchery.Items.GENERIC.itemMutandis.createStack()});
      GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Witchery.Blocks.FETISH_SCARECROW), new Object[]{"w#w", "sls", "wsw", '#', new ItemStack(Blocks.field_150428_aP), 'w', new ItemStack(Blocks.field_150325_L), 's', "stickWood", 'l', Witchery.Items.GENERIC.itemTormentedTwine.createStack()}));
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Blocks.FETISH_WITCHS_LADDER), new Object[]{"fsf", "ftf", "fsf", 'f', new ItemStack(Items.field_151008_G), 's', new ItemStack(Items.field_151007_F), 't', Witchery.Items.GENERIC.itemFancifulThread.createStack()});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Blocks.FETISH_TREANT_IDOL), new Object[]{"o#o", "srs", "o o", '#', new ItemStack(Blocks.field_150428_aP), 'o', new ItemStack(Blocks.field_150364_r, 1, 0), 'r', new ItemStack(Witchery.Blocks.LOG, 1, 0), 's', Witchery.Items.GENERIC.itemTormentedTwine.createStack()});
      SpinningRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemFancifulThread.createStack(), new ItemStack(Witchery.Blocks.WISPY_COTTON, 4), new ItemStack(Items.field_151007_F), Witchery.Items.GENERIC.itemOdourOfPurity.createStack());
      SpinningRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemTormentedTwine.createStack(), Witchery.Items.GENERIC.itemDisturbedCotton.createStack(4), new ItemStack(Items.field_151007_F), Witchery.Items.GENERIC.itemReekOfMisfortune.createStack());
      SpinningRecipes.instance().addRecipe(new ItemStack(Blocks.field_150321_G), new ItemStack(Items.field_151007_F, 8));
      SpinningRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemGoldenThread.createStack(3), new ItemStack(Blocks.field_150407_cf), Witchery.Items.GENERIC.itemWhiffOfMagic.createStack());
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemNullCatalyst.createStack(2), new Object[]{new ItemStack(Items.field_151156_bN), new ItemStack(Items.field_151045_i), new ItemStack(Items.field_151145_ak), new ItemStack(Items.field_151064_bs), new ItemStack(Items.field_151064_bs), new ItemStack(Items.field_151064_bs), new ItemStack(Items.field_151064_bs), new ItemStack(Items.field_151064_bs), new ItemStack(Items.field_151064_bs)});
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemNullCatalyst.createStack(2), new Object[]{Witchery.Items.GENERIC.itemNullCatalyst.createStack(), new ItemStack(Items.field_151064_bs), new ItemStack(Items.field_151065_br)});
      GameRegistry.addShapedRecipe(Witchery.Items.GENERIC.itemNullifiedLeather.createStack(3), new Object[]{"lll", "lcl", "lll", 'l', new ItemStack(Items.field_151116_aA), 'c', Witchery.Items.GENERIC.itemNullCatalyst.createStack()});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.HUNTER_HAT), new Object[]{"lll", "l l", 'l', Witchery.Items.GENERIC.itemNullifiedLeather.createStack()});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.HUNTER_COAT), new Object[]{"l l", "lll", "lll", 'l', Witchery.Items.GENERIC.itemNullifiedLeather.createStack()});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.HUNTER_LEGS), new Object[]{"lll", "l l", "l l", 'l', Witchery.Items.GENERIC.itemNullifiedLeather.createStack()});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.HUNTER_BOOTS), new Object[]{"l l", "l l", 'l', Witchery.Items.GENERIC.itemNullifiedLeather.createStack()});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.SHELF_COMPASS), new Object[]{"gdg", "d#d", "gcg", 'g', new ItemStack(Items.field_151043_k), 'd', new ItemStack(Items.field_151045_i), '#', new ItemStack(Items.field_151113_aN), 'c', Witchery.Items.GENERIC.itemNullCatalyst.createStack()});
      GameRegistry.addRecipe(new ShapedOreRecipe(Witchery.Items.GENERIC.itemBoltStake.createStack(9), new Object[]{" s ", "www", "fff", 'f', new ItemStack(Items.field_151008_G), 's', new ItemStack(Items.field_151007_F), 'w', "stickWood"}));
      GameRegistry.addShapedRecipe(Witchery.Items.GENERIC.itemBoltSplitting.createStack(), new Object[]{" s ", "bbb", " f ", 'f', new ItemStack(Items.field_151008_G), 's', new ItemStack(Items.field_151007_F), 'b', Witchery.Items.GENERIC.itemBoltStake.createStack()});
      GameRegistry.addShapedRecipe(Witchery.Items.GENERIC.itemBoltHoly.createStack(12), new Object[]{"aba", "ata", "aba", 't', new ItemStack(Items.field_151073_bk), 'a', Witchery.Items.GENERIC.itemBoltStake.createStack(), 'b', new ItemStack(Items.field_151103_aS)});
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemBoltAntiMagic.createStack(3), new Object[]{Witchery.Items.GENERIC.itemNullCatalyst.createStack(), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), Witchery.Items.GENERIC.itemBoltHoly.createStack(), Witchery.Items.GENERIC.itemBoltHoly.createStack(), Witchery.Items.GENERIC.itemBoltHoly.createStack()});
      GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Witchery.Items.CROSSBOW_PISTOL), new Object[]{"mbm", "swn", " m ", 'm', new ItemStack(Items.field_151042_j), 'b', new ItemStack(Items.field_151031_f), 'n', Witchery.Items.GENERIC.itemBoneNeedle.createStack(), 's', new ItemStack(Items.field_151007_F), 'w', "stickWood"}));
      GameRegistry.addShapelessRecipe(Witchery.Items.POTIONS.potionAntidote.createStack(2), new Object[]{Witchery.Items.GENERIC.itemNullCatalyst.createStack(), new ItemStack(Items.field_151068_bn, 1, 8196), new ItemStack(Items.field_151068_bn, 1, 8196)});
      GameRegistry.addShapedRecipe(Witchery.Items.GENERIC.itemContractOwnership.createStack(), new Object[]{"ppp", "pfp", "pps", 'f', Witchery.Items.GENERIC.itemOddPorkRaw.createStack(), 'p', new ItemStack(Items.field_151121_aF), 's', new ItemStack(Items.field_151007_F)});
      GameRegistry.addRecipe(new RecipeAttachTaglock(Witchery.Items.GENERIC.itemContractOwnership.createStack(), new ItemStack[]{Witchery.Items.GENERIC.itemContractOwnership.createStack(), new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1)}));
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemContractBlaze.createStack(), new Object[]{Witchery.Items.GENERIC.itemContractOwnership.createStack(), new ItemStack(Items.field_151072_bj), Witchery.Items.GENERIC.itemHintOfRebirth.createStack()});
      GameRegistry.addRecipe(new RecipeAttachTaglock(Witchery.Items.GENERIC.itemContractBlaze.createStack(), new ItemStack[]{Witchery.Items.GENERIC.itemContractBlaze.createStack(), new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1)}));
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemContractResistFire.createStack(), new Object[]{Witchery.Items.GENERIC.itemContractOwnership.createStack(), new ItemStack(Items.field_151065_br)});
      GameRegistry.addRecipe(new RecipeAttachTaglock(Witchery.Items.GENERIC.itemContractResistFire.createStack(), new ItemStack[]{Witchery.Items.GENERIC.itemContractResistFire.createStack(), new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1)}));
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemContractEvaporate.createStack(), new Object[]{Witchery.Items.GENERIC.itemContractOwnership.createStack(), new ItemStack(Items.field_151064_bs), new ItemStack(Items.field_151072_bj)});
      GameRegistry.addRecipe(new RecipeAttachTaglock(Witchery.Items.GENERIC.itemContractEvaporate.createStack(), new ItemStack[]{Witchery.Items.GENERIC.itemContractEvaporate.createStack(), new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1)}));
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemContractFieryTouch.createStack(), new Object[]{Witchery.Items.GENERIC.itemContractOwnership.createStack(), new ItemStack(Witchery.Blocks.EMBER_MOSS), new ItemStack(Items.field_151072_bj)});
      GameRegistry.addRecipe(new RecipeAttachTaglock(Witchery.Items.GENERIC.itemContractFieryTouch.createStack(), new ItemStack[]{Witchery.Items.GENERIC.itemContractFieryTouch.createStack(), new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1)}));
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemContractSmelting.createStack(), new Object[]{Witchery.Items.GENERIC.itemContractOwnership.createStack(), new ItemStack(Items.field_151129_at)});
      GameRegistry.addRecipe(new RecipeAttachTaglock(Witchery.Items.GENERIC.itemContractSmelting.createStack(), new ItemStack[]{Witchery.Items.GENERIC.itemContractSmelting.createStack(), new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1)}));
      GameRegistry.addShapelessRecipe(new ItemStack(Witchery.Items.LEONARDS_URN, 1, 1), new Object[]{new ItemStack(Witchery.Items.LEONARDS_URN, 1, 0), new ItemStack(Witchery.Items.LEONARDS_URN, 1, 0)});
      GameRegistry.addShapelessRecipe(new ItemStack(Witchery.Items.LEONARDS_URN, 1, 2), new Object[]{new ItemStack(Witchery.Items.LEONARDS_URN, 1, 1), new ItemStack(Witchery.Items.LEONARDS_URN, 1, 0)});
      GameRegistry.addShapelessRecipe(new ItemStack(Witchery.Items.LEONARDS_URN, 1, 3), new Object[]{new ItemStack(Witchery.Items.LEONARDS_URN, 1, 2), new ItemStack(Witchery.Items.LEONARDS_URN, 1, 0)});
      GameRegistry.addRecipe(new RecipeAttachTaglock(new ItemStack(Witchery.Items.PLAYER_COMPASS), new ItemStack[]{new ItemStack(Witchery.Items.PLAYER_COMPASS, 1, 32767), new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1)}));
      ItemStack[] logs = new ItemStack[]{new ItemStack(Blocks.field_150364_r, 1, 0), new ItemStack(Blocks.field_150364_r, 1, 1), new ItemStack(Blocks.field_150364_r, 1, 2), new ItemStack(Blocks.field_150364_r, 1, 3), new ItemStack(Witchery.Blocks.LOG, 1, 0), new ItemStack(Witchery.Blocks.LOG, 1, 1), new ItemStack(Witchery.Blocks.LOG, 1, 2), new ItemStack(Blocks.field_150363_s, 1, 0), new ItemStack(Blocks.field_150363_s, 1, 1)};

      for(i = 0; i < logs.length; ++i) {
         GameRegistry.addShapedRecipe(new ItemStack(Witchery.Blocks.STOCKADE, 9, i), new Object[]{" w ", "wfw", "www", 'f', Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), 'w', logs[i]});
      }

      ItemStack kobolditeIngot = Witchery.Items.GENERIC.itemKobolditeIngot.createStack();
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.KOBOLDITE_PICKAXE), new Object[]{"bab", "iii", " s ", 'i', kobolditeIngot, 'a', Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack(), 'b', new ItemStack(Items.field_151129_at), 's', new ItemStack(Items.field_151055_y)});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Blocks.STATUE_OF_WORSHIP), new Object[]{"sks", " s ", "s s", 'k', kobolditeIngot, 's', new ItemStack(Blocks.field_150348_b)});
      GameRegistry.addShapedRecipe(Witchery.Items.GENERIC.itemKobolditePentacle.createStack(), new Object[]{"sks", "kdk", "sks", 'k', kobolditeIngot, 's', Witchery.Items.GENERIC.itemKobolditeNugget.createStack(), 'd', new ItemStack(Items.field_151045_i)});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.KOBOLDITE_HELM), new Object[]{"iii", "iai", 'i', kobolditeIngot, 'a', Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.EARMUFFS), new Object[]{"iii", "i i", "w w", 'i', new ItemStack(Items.field_151116_aA), 'w', new ItemStack(Blocks.field_150325_L)});
      GameRegistry.addRecipe(new RecipeShapelessBiomeCopy(new ItemStack(Witchery.Items.BIOME_NOTE), new ItemStack[]{new ItemStack(Witchery.Items.BIOME_BOOK.func_77642_a(Witchery.Items.BIOME_BOOK)), new ItemStack(Items.field_151121_aF)}));
      GameRegistry.addShapelessRecipe(Witchery.Items.GENERIC.itemAnnointingPaste.createStack(), new Object[]{new ItemStack(Witchery.Items.SEEDS_ARTICHOKE), new ItemStack(Witchery.Items.SEEDS_MANDRAKE), new ItemStack(Witchery.Items.SEEDS_BELLADONNA), new ItemStack(Witchery.Items.SEEDS_SNOWBELL)});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.SILVER_SWORD), new Object[]{"ddd", "dsd", "ddd", 's', new ItemStack(Items.field_151010_B), 'd', Witchery.Items.GENERIC.itemSilverDust.createStack()});
      Item[][] hunterItems = new Item[][]{{Witchery.Items.HUNTER_BOOTS, Witchery.Items.HUNTER_BOOTS_SILVERED}, {Witchery.Items.HUNTER_LEGS, Witchery.Items.HUNTER_LEGS_SILVERED}, {Witchery.Items.HUNTER_COAT, Witchery.Items.HUNTER_COAT_SILVERED}, {Witchery.Items.HUNTER_HAT, Witchery.Items.HUNTER_HAT_SILVERED}};

      for(i = 0; i < hunterItems.length; ++i) {
         CraftingManager.func_77594_a().func_92103_a(new ItemStack(hunterItems[i][1]), new Object[]{"dwd", "w#w", "dsd", '#', new ItemStack(hunterItems[i][0]), 's', new ItemStack(Items.field_151007_F), 'w', Witchery.Items.GENERIC.itemWolfsbane.createStack(), 'd', Witchery.Items.GENERIC.itemSilverDust.createStack()}).func_92100_c();
      }

      GameRegistry.addShapedRecipe(Witchery.Items.GENERIC.itemBoltSilver.createStack(3), new Object[]{" s ", "bbb", 'b', Witchery.Items.GENERIC.itemBoltStake.createStack(), 's', Witchery.Items.GENERIC.itemSilverDust.createStack()});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Blocks.WOLF_ALTAR), new Object[]{" w ", "w#w", "#d#", 'w', new ItemStack(Witchery.Blocks.WOLFHEAD, 1, 32767), '#', new ItemStack(Blocks.field_150348_b), 'd', Witchery.Items.GENERIC.itemWolfsbane.createStack()});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Blocks.SILVER_VAT), new Object[]{"ibi", "ifi", 'i', new ItemStack(Items.field_151042_j), 'b', new ItemStack(Items.field_151131_as), 'f', new ItemStack(Blocks.field_150460_al)});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Blocks.BEARTRAP), new Object[]{"iii", "bpb", "iii", 'p', new ItemStack(Blocks.field_150443_bT), 'i', new ItemStack(Items.field_151042_j), 'b', new ItemStack(Items.field_151097_aZ)});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Blocks.WOLFTRAP), new Object[]{"sns", "w#w", "sns", '#', new ItemStack(Witchery.Blocks.BEARTRAP), 's', Witchery.Items.GENERIC.itemSilverDust.createStack(), 'n', Witchery.Items.GENERIC.itemNullCatalyst.createStack(), 'w', Witchery.Items.GENERIC.itemWolfsbane.createStack()});
      GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Witchery.Blocks.GARLIC_GARLAND), new Object[]{"s s", "GsG", "GGG", 'G', "cropGarlic", 's', new ItemStack(Items.field_151007_F)}));
      ItemStack[] meats = new ItemStack[]{new ItemStack(Items.field_151082_bd), new ItemStack(Items.field_151076_bf), new ItemStack(Items.field_151147_al), new ItemStack(Items.field_151115_aP), new ItemStack(Items.field_151115_aP, 1), Witchery.Items.GENERIC.itemMuttonRaw.createStack()};
      ItemStack[] arr$ = meats;
      int i = meats.length;

      int i;
      for(i = 0; i < i; ++i) {
         ItemStack meat = arr$[i];
         GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Witchery.Items.STEW_RAW), new Object[]{"cropGarlic", meat, new ItemStack(Items.field_151174_bG), new ItemStack(Items.field_151172_bF), new ItemStack(Items.field_151054_z), new ItemStack(Blocks.field_150338_P)}));
      }

      Item[][] hunterItemsSilvered = new Item[][]{{Witchery.Items.HUNTER_BOOTS_SILVERED, Witchery.Items.HUNTER_BOOTS_GARLICKED}, {Witchery.Items.HUNTER_LEGS_SILVERED, Witchery.Items.HUNTER_LEGS_GARLICKED}, {Witchery.Items.HUNTER_COAT_SILVERED, Witchery.Items.HUNTER_COAT_GARLICKED}, {Witchery.Items.HUNTER_HAT_SILVERED, Witchery.Items.HUNTER_HAT_GARLICKED}};

      for(i = 0; i < hunterItemsSilvered.length; ++i) {
         CraftingManager.func_77594_a().func_92103_a(new ItemStack(hunterItemsSilvered[i][1]), new Object[]{" g ", "g#g", " s ", '#', new ItemStack(hunterItemsSilvered[i][0]), 's', new ItemStack(Items.field_151007_F), 'g', new ItemStack(Witchery.Items.SEEDS_GARLIC)}).func_92100_c();
      }

      for(i = 0; i < 9; ++i) {
         GameRegistry.addShapelessRecipe(new ItemStack(Witchery.Items.VAMPIRE_BOOK, 1, i + 1), new Object[]{new ItemStack(Witchery.Items.VAMPIRE_BOOK, 1, i), Witchery.Items.GENERIC.itemVampireBookPage.createStack()});
      }

      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.BLOOD_GOBLET), new Object[]{"b b", " b ", " g ", 'g', new ItemStack(Blocks.field_150359_w), 'b', new ItemStack(Items.field_151069_bo)});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Blocks.BLOOD_CRUCIBLE), new Object[]{"s s", "blb", 's', new ItemStack(Blocks.field_150390_bg), 'b', new ItemStack(Blocks.field_150417_aV), 'l', new ItemStack(Blocks.field_150333_U, 1, 5)});
      GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Witchery.Items.COFFIN), new Object[]{"ppp", "lbl", "lll", 'b', new ItemStack(Items.field_151104_aV), 'p', "plankWood", 'l', "logWood"}));
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Blocks.DAYLIGHT_COLLECTOR), new Object[]{"g g", " r ", "ici", 'g', new ItemStack(Items.field_151043_k), 'r', new ItemStack(Items.field_151107_aW), 'i', new ItemStack(Items.field_151042_j), 'c', new ItemStack(Blocks.field_150453_bW)});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.VAMPIRE_HELMET), new Object[]{" i ", "i#i", " i ", 'i', new ItemStack(Items.field_151042_j), '#', new ItemStack(Witchery.Items.VAMPIRE_HAT)});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.VAMPIRE_COAT_CHAIN), new Object[]{" i ", "i#i", " i ", 'i', new ItemStack(Items.field_151042_j), '#', new ItemStack(Witchery.Items.VAMPIRE_COAT)});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.VAMPIRE_COAT_FEMALE_CHAIN), new Object[]{" i ", "i#i", " i ", 'i', new ItemStack(Items.field_151042_j), '#', new ItemStack(Witchery.Items.VAMPIRE_COAT_FEMALE)});
      ItemStack cloth = Witchery.Items.GENERIC.itemDarkCloth.createStack();
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.VAMPIRE_HAT), new Object[]{"###", "# #", '#', cloth});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.VAMPIRE_COAT), new Object[]{"# #", "###", "###", '#', cloth});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.VAMPIRE_COAT_FEMALE), new Object[]{"# #", "#l#", "###", 'l', new ItemStack(Items.field_151116_aA), '#', cloth});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.VAMPIRE_LEGS), new Object[]{"###", "# #", "# #", '#', cloth});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.VAMPIRE_LEGS_KILT), new Object[]{"###", "###", "# #", '#', cloth});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.VAMPIRE_BOOTS), new Object[]{"# #", "# #", '#', cloth});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.CANE_SWORD), new Object[]{" #g", "#d#", "## ", 'g', new ItemStack(Items.field_151043_k), 'd', new ItemStack(Items.field_151048_u), '#', cloth});
      GameRegistry.addShapedRecipe(new ItemStack(Witchery.Items.VAMPIRE_BOOK), new Object[]{"#s#", "#b#", "#g#", 's', new ItemStack(Items.field_151156_bN), 'b', new ItemStack(Items.field_151122_aG), 'g', new ItemStack(Witchery.Items.SEEDS_GARLIC), '#', new ItemStack(Items.field_151075_bm)});

      for(i = 0; i < 16; ++i) {
         GameRegistry.addShapedRecipe(new ItemStack(Witchery.Blocks.SHADED_GLASS, 8, i), new Object[]{"###", "#r#", "###", 'r', new ItemStack(Items.field_151137_ax), '#', new ItemStack(Blocks.field_150399_cn, 1, i)});
      }

      GameRegistry.addRecipe(new ShapedOreRecipe(Witchery.Items.GENERIC.itemWoodenStake.createStack(), new Object[]{"GGG", "GsG", "GGG", 'G', "cropGarlic", 's', new ItemStack(Items.field_151055_y)}));
      OreDictionary.registerOre("plankWood", new ItemStack(Witchery.Blocks.PLANKS, 1, 32767));
      OreDictionary.registerOre("treeSapling", new ItemStack(Witchery.Blocks.SAPLING, 1, 32767));
      OreDictionary.registerOre("logWood", new ItemStack(Witchery.Blocks.LOG, 1, 32767));
      OreDictionary.registerOre("treeLeaves", new ItemStack(Witchery.Blocks.LEAVES, 1, 32767));
      OreDictionary.registerOre("stairWood", new ItemStack(Witchery.Blocks.STAIRS_ALDER, 1, 32767));
      OreDictionary.registerOre("stairWood", new ItemStack(Witchery.Blocks.STAIRS_HAWTHORN, 1, 32767));
      OreDictionary.registerOre("stairWood", new ItemStack(Witchery.Blocks.STAIRS_ROWAN, 1, 32767));
      OreDictionary.registerOre("cropGarlic", new ItemStack(Witchery.Items.SEEDS_GARLIC, 1, 32767));
      GameRegistry.addSmelting(Witchery.Items.GENERIC.itemSoftClayJar.createStack(), Witchery.Items.GENERIC.itemEmptyClayJar.createStack(), 0.0F);
      GameRegistry.addSmelting(Witchery.Items.GENERIC.itemOddPorkRaw.createStack(), Witchery.Items.GENERIC.itemOddPorkCooked.createStack(), 0.0F);
      GameRegistry.addSmelting(Witchery.Items.GENERIC.itemGoldenThread.createStack(), new ItemStack(Items.field_151074_bl), 0.0F);
      GameRegistry.addSmelting(Witchery.Items.GENERIC.itemMuttonRaw.createStack(), Witchery.Items.GENERIC.itemMuttonCooked.createStack(), 0.0F);
      GameRegistry.addSmelting(new ItemStack(Witchery.Blocks.BLOODED_WOOL), Witchery.Items.GENERIC.itemDarkCloth.createStack(), 0.0F);
      GameRegistry.addSmelting(new ItemStack(Witchery.Items.STEW_RAW), new ItemStack(Witchery.Items.STEW), 1.0F);
      if (!Config.instance().smeltAllSaplingsToWoodAsh) {
         GameRegistry.addSmelting(Blocks.field_150345_g, Witchery.Items.GENERIC.itemAshWood.createStack(), 0.0F);
         GameRegistry.addSmelting(new ItemStack(Witchery.Blocks.SAPLING), Witchery.Items.GENERIC.itemAshWood.createStack(), 0.0F);
      }

      GameRegistry.addSmelting(new ItemStack(Witchery.Blocks.LOG, 1, 0), new ItemStack(Items.field_151044_h, 1, 1), 0.0F);
      GameRegistry.addSmelting(new ItemStack(Witchery.Blocks.LOG, 1, 1), new ItemStack(Items.field_151044_h, 1, 1), 0.0F);
      GameRegistry.addSmelting(new ItemStack(Witchery.Blocks.LOG, 1, 2), new ItemStack(Items.field_151044_h, 1, 1), 0.0F);
      DistilleryRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemFoulFume.createStack(), Witchery.Items.GENERIC.itemQuicklime.createStack(), 1, Witchery.Items.GENERIC.itemGypsum.createStack(), Witchery.Items.GENERIC.itemOilOfVitriol.createStack(), new ItemStack(Items.field_151123_aH), (ItemStack)null);
      DistilleryRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(), Dye.LAPIS_LAZULI.createStack(), 3, Witchery.Items.GENERIC.itemTearOfTheGoddess.createStack(), Witchery.Items.GENERIC.itemWhiffOfMagic.createStack(), new ItemStack(Items.field_151123_aH), Witchery.Items.GENERIC.itemFoulFume.createStack());
      DistilleryRecipes.instance().addRecipe(new ItemStack(Items.field_151045_i), Witchery.Items.GENERIC.itemOilOfVitriol.createStack(), 3, Witchery.Items.GENERIC.itemDiamondVapour.createStack(), Witchery.Items.GENERIC.itemDiamondVapour.createStack(), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), (ItemStack)null);
      DistilleryRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemDiamondVapour.createStack(), new ItemStack(Items.field_151073_bk), 3, Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), Witchery.Items.GENERIC.itemReekOfMisfortune.createStack(), Witchery.Items.GENERIC.itemFoulFume.createStack(), Witchery.Items.GENERIC.itemRefinedEvil.createStack());
      DistilleryRecipes.instance().addRecipe(new ItemStack(Items.field_151079_bi), (ItemStack)null, 6, Witchery.Items.GENERIC.itemEnderDew.createStack(2), Witchery.Items.GENERIC.itemEnderDew.createStack(2), Witchery.Items.GENERIC.itemEnderDew.createStack(), Witchery.Items.GENERIC.itemWhiffOfMagic.createStack());
      DistilleryRecipes.instance().addRecipe(new ItemStack(Items.field_151065_br), new ItemStack(Items.field_151016_H), 1, new ItemStack(Items.field_151114_aO), new ItemStack(Items.field_151114_aO), Witchery.Items.GENERIC.itemReekOfMisfortune.createStack(), (ItemStack)null);
      DistilleryRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemDemonHeart.createStack(), Witchery.Items.GENERIC.itemDiamondVapour.createStack(), 4, Witchery.Items.GENERIC.itemInfernalBlood.createStack(2), Witchery.Items.GENERIC.itemInfernalBlood.createStack(2), Witchery.Items.GENERIC.itemRefinedEvil.createStack(), (ItemStack)null);
      DistilleryRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemDemonHeart.createStack(), new ItemStack(Blocks.field_150424_aL), 2, new ItemStack(Blocks.field_150425_aM), Witchery.Items.GENERIC.itemInfernalBlood.createStack(), Witchery.Items.GENERIC.itemInfernalBlood.createStack(), (ItemStack)null);
      DistilleryRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfFlowingSpirit.createStack(), Witchery.Items.GENERIC.itemOilOfVitriol.createStack(), 2, Witchery.Items.GENERIC.itemFocusedWill.createStack(), Witchery.Items.GENERIC.itemCondensedFear.createStack(), Witchery.Items.GENERIC.itemBrewOfHollowTears.createStack(4), Witchery.Items.GENERIC.itemBrewOfHollowTears.createStack(4));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfVines.createStack(3), 1, 0, 0.0F, -16753913, 0, new ItemStack(Blocks.field_150395_bd), new ItemStack(Blocks.field_150337_Q), new ItemStack(Blocks.field_150338_P), Witchery.Items.GENERIC.itemDogTongue.createStack(), new ItemStack(Items.field_151015_O), Witchery.Items.GENERIC.itemReekOfMisfortune.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfWebs.createStack(3), 1, 0, 0.0F, -1, 0, Witchery.Items.GENERIC.itemWeb.createStack(), new ItemStack(Blocks.field_150337_Q), Witchery.Items.GENERIC.itemBatWool.createStack(), new ItemStack(Blocks.field_150327_N), Witchery.Items.GENERIC.itemWhiffOfMagic.createStack(), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfThorns.createStack(3), 1, 0, 0.0F, -10027232, 0, Dye.CACTUS_GREEN.createStack(), new ItemStack(Blocks.field_150338_P), Witchery.Items.GENERIC.itemOilOfVitriol.createStack(), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), new ItemStack(Blocks.field_150328_O), Witchery.Items.GENERIC.itemMandrakeRoot.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfInk.createStack(3), 1, 0, 0.0F, -13421773, 0, Dye.INK_SAC.createStack(), Witchery.Items.GENERIC.itemQuicklime.createStack(), Witchery.Items.GENERIC.itemOilOfVitriol.createStack(), new ItemStack(Items.field_151123_aH), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), Witchery.Items.GENERIC.itemRowanBerries.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(3), 1, 0, 0.0F, -11258073, 0, new ItemStack(Witchery.Blocks.SAPLING, 1, 0), new ItemStack(Witchery.Blocks.SAPLING, 1, 1), new ItemStack(Witchery.Blocks.SAPLING, 1, 2), Witchery.Items.GENERIC.itemDogTongue.createStack(), Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), new ItemStack(Blocks.field_150328_O));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfErosion.createStack(3), 1, 0, 0.0F, -4456656, 0, Witchery.Items.GENERIC.itemOilOfVitriol.createStack(), Witchery.Items.GENERIC.itemOilOfVitriol.createStack(), Witchery.Items.GENERIC.itemQuicklime.createStack(), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), new ItemStack(Blocks.field_150327_N), new ItemStack(Items.field_151064_bs));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfRaising.createStack(3), 1, 0, 500.0F, -12120505, 0, Witchery.Items.GENERIC.itemBatWool.createStack(), Witchery.Items.GENERIC.itemMutandis.createStack(), new ItemStack(Items.field_151137_ax), Witchery.Items.GENERIC.itemOilOfVitriol.createStack(), new ItemStack(Items.field_151103_aS), new ItemStack(Items.field_151078_bh));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewGrotesque.createStack(3), 1, 0, 500.0F, -13491946, 0, Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), Witchery.Items.GENERIC.itemArtichoke.createStack(), Witchery.Items.GENERIC.itemDogTongue.createStack(), new ItemStack(Items.field_151153_ao), new ItemStack(Items.field_151170_bI));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfLove.createStack(3), 1, 0, 0.0F, -23044, 0, new ItemStack(Blocks.field_150328_O), Witchery.Items.GENERIC.itemWhiffOfMagic.createStack(), Witchery.Items.GENERIC.itemArtichoke.createStack(), new ItemStack(Items.field_151150_bK), new ItemStack(Blocks.field_150392_bi), Dye.COCOA_BEANS.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfIce.createStack(3), 1, 0, 1000.0F, -13565953, 0, Witchery.Items.GENERIC.itemIcyNeedle.createStack(), new ItemStack(Items.field_151126_ay), Witchery.Items.GENERIC.itemArtichoke.createStack(), new ItemStack(Items.field_151060_bw), new ItemStack(Blocks.field_150337_Q), Witchery.Items.GENERIC.itemOdourOfPurity.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfTheDepths.createStack(3), 1, 0, 0.0F, -15260093, 0, Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), Witchery.Items.GENERIC.itemArtichoke.createStack(), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), Witchery.Items.GENERIC.itemTearOfTheGoddess.createStack(), new ItemStack(Blocks.field_150392_bi), Dye.INK_SAC.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfInfection.createStack(3), 0, 0, 0.0F, -11112850, 0, Witchery.Items.GENERIC.itemToeOfFrog.createStack(), Witchery.Items.GENERIC.itemCreeperHeart.createStack(), Witchery.Items.GENERIC.itemWormyApple.createStack(), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), new ItemStack(Items.field_151078_bh), Witchery.Items.GENERIC.itemMutandis.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfSleeping.createStack(3), 1, 0, 0.0F, -7710856, 0, Witchery.Items.GENERIC.itemPurifiedMilk.createStack(), new ItemStack(Items.field_151106_aX), Witchery.Items.GENERIC.itemBrewOfLove.createStack(), Witchery.Items.GENERIC.itemWhiffOfMagic.createStack(), Witchery.Items.GENERIC.itemIcyNeedle.createStack(), Witchery.Items.GENERIC.itemArtichoke.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfFlowingSpirit.createStack(3), 0, 0, 0.0F, -16711834, Config.instance().dimensionDreamID, Witchery.Items.GENERIC.itemFancifulThread.createStack(), Witchery.Items.GENERIC.itemArtichoke.createStack(), Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), new ItemStack(Witchery.Blocks.SPANISH_MOSS), new ItemStack(Witchery.Blocks.GLINT_WEED), Witchery.Items.GENERIC.itemBatWool.createStack()).setUnlocalizedName("witchery.brew.flowingspirit");
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfWasting.createStack(3), 1, 0, 0.0F, -12440546, 0, Witchery.Items.GENERIC.itemMellifluousHunger.createStack(), new ItemStack(Items.field_151078_bh), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), new ItemStack(Witchery.Blocks.EMBER_MOSS), new ItemStack(Items.field_151170_bI), new ItemStack(Items.field_151070_bp));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfBats.createStack(3), 1, 0, 0.0F, -9809858, 0, Witchery.Items.GENERIC.itemBatBall.createStack(), Witchery.Items.GENERIC.itemBatWool.createStack(), new ItemStack(Items.field_151034_e), new ItemStack(Items.field_151102_aT), new ItemStack(Items.field_151071_bq), new ItemStack(Items.field_151016_H));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewSubstitution.createStack(3), 1, 0, 0.0F, -7010720, 0, Witchery.Items.GENERIC.itemEnderDew.createStack(), Witchery.Items.GENERIC.itemEnderDew.createStack(), Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack(Items.field_151110_aK), new ItemStack(Items.field_151064_bs), Witchery.Items.GENERIC.itemBranchEnt.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewRevealing.createStack(3), 1, 0, 0.0F, -4079167, 0, new ItemStack(Items.field_151172_bF), new ItemStack(Items.field_151070_bp), new ItemStack(Items.field_151070_bp), new ItemStack(Items.field_151068_bn, 1, 8198), new ItemStack(Blocks.field_150338_P), Witchery.Items.GENERIC.itemOdourOfPurity.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfSolidDirt.createStack(3), 1, 0, 2000.0F, -11720688, 0, true, new ItemStack(Blocks.field_150346_d), Witchery.Items.GENERIC.itemFoulFume.createStack(), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), Witchery.Items.GENERIC.itemMutandis.createStack(), Witchery.Items.GENERIC.itemAshWood.createStack(), new ItemStack(Witchery.Blocks.SPANISH_MOSS)).setUnlocalizedName("witchery.brew.solidification");
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfSolidRock.createStack(3), 1, 0, 2000.0F, -8355712, 0, false, new ItemStack(Blocks.field_150348_b), Witchery.Items.GENERIC.itemFoulFume.createStack(), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), Witchery.Items.GENERIC.itemMutandis.createStack(), Witchery.Items.GENERIC.itemAshWood.createStack(), new ItemStack(Witchery.Blocks.SPANISH_MOSS));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfSolidSand.createStack(3), 1, 0, 2000.0F, -3495323, 0, false, new ItemStack(Blocks.field_150354_m), Witchery.Items.GENERIC.itemFoulFume.createStack(), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), Witchery.Items.GENERIC.itemMutandis.createStack(), Witchery.Items.GENERIC.itemAshWood.createStack(), new ItemStack(Witchery.Blocks.SPANISH_MOSS));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfSolidSandstone.createStack(3), 1, 0, 2000.0F, -8427008, 0, false, new ItemStack(Blocks.field_150322_A), Witchery.Items.GENERIC.itemFoulFume.createStack(), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), Witchery.Items.GENERIC.itemMutandis.createStack(), Witchery.Items.GENERIC.itemAshWood.createStack(), new ItemStack(Witchery.Blocks.SPANISH_MOSS));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfSolidErosion.createStack(3), 1, 0, 2000.0F, -3300, 0, false, Witchery.Items.GENERIC.itemBrewOfErosion.createStack(), Witchery.Items.GENERIC.itemFoulFume.createStack(), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), Witchery.Items.GENERIC.itemMutandis.createStack(), Witchery.Items.GENERIC.itemAshWood.createStack(), new ItemStack(Witchery.Blocks.SPANISH_MOSS));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfCursedLeaping.createStack(3), 1, 1, 0.0F, -16758145, 0, new ItemStack(Items.field_151103_aS), new ItemStack(Items.field_151034_e), Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), new ItemStack(Items.field_151008_G), new ItemStack(Items.field_151115_aP));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfFrogsTongue.createStack(3), 1, 2, 0.0F, -12938226, 0, new ItemStack(Blocks.field_150337_Q), new ItemStack(Items.field_151015_O), Witchery.Items.GENERIC.itemBrewOfWebs.createStack(), Witchery.Items.GENERIC.itemArtichoke.createStack(), new ItemStack(Items.field_151123_aH), Witchery.Items.GENERIC.itemToeOfFrog.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemBrewOfHitchcock.createStack(3), 1, 3, 0.0F, -3908582, 0, new ItemStack(Blocks.field_150338_P), new ItemStack(Items.field_151014_N), Witchery.Items.GENERIC.itemBrewOfThorns.createStack(), Witchery.Items.GENERIC.itemBatWool.createStack(), new ItemStack(Items.field_151008_G), Witchery.Items.GENERIC.itemOwletsWing.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemCongealedSpirit.createStack(), 0, 0, 2000.0F, -3096310, 0, Witchery.Items.GENERIC.itemBrewOfHollowTears.createStack(), Witchery.Items.GENERIC.itemSubduedSpirit.createStack(), Witchery.Items.GENERIC.itemSubduedSpirit.createStack(), Witchery.Items.GENERIC.itemSubduedSpirit.createStack(), Witchery.Items.GENERIC.itemSubduedSpirit.createStack(), Witchery.Items.GENERIC.itemSubduedSpirit.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), 0, 0, 1000.0F, -59882, 0, new ItemStack(Items.field_151137_ax), Witchery.Items.GENERIC.itemDropOfLuck.createStack(), Witchery.Items.GENERIC.itemBatWool.createStack(), Witchery.Items.GENERIC.itemDogTongue.createStack(), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), Witchery.Items.GENERIC.itemMandrakeRoot.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemFlyingOintment.createStack(), 0, 0, 3000.0F, -17620, 0, Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), new ItemStack(Items.field_151068_bn, 1, 8258), new ItemStack(Items.field_151045_i), new ItemStack(Items.field_151008_G), Witchery.Items.GENERIC.itemBatWool.createStack(), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemMysticUnguent.createStack(), 0, 0, 3000.0F, -14333109, 0, Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), new ItemStack(Items.field_151068_bn, 1, 8265), new ItemStack(Items.field_151045_i), new ItemStack(Witchery.Blocks.SAPLING, 1, 0), Witchery.Items.GENERIC.itemCreeperHeart.createStack(), Witchery.Items.GENERIC.itemInfernalBlood.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemHappenstanceOil.createStack(), 0, 0, 2000.0F, 8534058, 0, Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), new ItemStack(Items.field_151068_bn, 1, 8262), new ItemStack(Items.field_151061_bv), new ItemStack(Items.field_151150_bK), new ItemStack(Items.field_151070_bp), Witchery.Items.GENERIC.itemMandrakeRoot.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemGhostOfTheLight.createStack(2), 0, 0, 4000.0F, -5584658, 0, Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), new ItemStack(Items.field_151068_bn, 1, 8270), new ItemStack(Items.field_151068_bn, 1, 8259), Witchery.Items.POPPET.firePoppet.createStack(), new ItemStack(Blocks.field_150478_aa), Witchery.Items.GENERIC.itemDogTongue.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemSoulOfTheWorld.createStack(2), 0, 0, 4000.0F, -16003328, 0, Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), new ItemStack(Items.field_151068_bn, 1, 8257), new ItemStack(Items.field_151153_ao, 1, 1), Witchery.Items.GENERIC.itemAttunedStone.createStack(), Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), new ItemStack(Witchery.Blocks.SAPLING, 1, 0));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemSpiritOfOtherwhere.createStack(2), 0, 0, 4000.0F, -7128833, 0, Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), new ItemStack(Items.field_151068_bn, 1, 8258), new ItemStack(Items.field_151061_bv), new ItemStack(Items.field_151061_bv), Witchery.Items.GENERIC.itemDropOfLuck.createStack(), Witchery.Items.GENERIC.itemBatWool.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemSpiritOfOtherwhere.createStack(2), 0, 0, 4000.0F, -7128833, 0, false, Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), new ItemStack(Items.field_151068_bn, 1, 16210), new ItemStack(Items.field_151061_bv), new ItemStack(Items.field_151061_bv), Witchery.Items.GENERIC.itemDropOfLuck.createStack(), Witchery.Items.GENERIC.itemBatWool.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemInfernalAnimus.createStack(2), 0, 0, 4000.0F, -7598080, 0, Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), new ItemStack(Items.field_151068_bn, 1, 8236), Witchery.Items.POPPET.voodooPoppet.createStack(), Witchery.Items.GENERIC.itemDemonHeart.createStack(), Witchery.Items.GENERIC.itemRefinedEvil.createStack(), new ItemStack(Items.field_151072_bj));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemInfernalAnimus.createStack(2), 0, 0, 4000.0F, -7598080, 0, false, Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), new ItemStack(Items.field_151068_bn, 1, 16172), Witchery.Items.POPPET.voodooPoppet.createStack(), Witchery.Items.GENERIC.itemDemonHeart.createStack(), Witchery.Items.GENERIC.itemRefinedEvil.createStack(), new ItemStack(Items.field_151072_bj));
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemInfusionBase.createStack(), 1, 0, 3000.0F, -10520657, 0, Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), Witchery.Items.GENERIC.itemBrewOfFlowingSpirit.createStack(), Witchery.Items.GENERIC.itemCreeperHeart.createStack(), Witchery.Items.GENERIC.itemToeOfFrog.createStack(), Witchery.Items.GENERIC.itemOwletsWing.createStack(), Witchery.Items.GENERIC.itemDogTongue.createStack());
      KettleRecipes.instance().addRecipe(Witchery.Items.GENERIC.itemInfusionBase.createStack(2), 0, 0, 3000.0F, -10520657, 0, Witchery.Items.GENERIC.itemInfusionBase.createStack(), Witchery.Items.GENERIC.itemBrewOfFlowingSpirit.createStack(), Witchery.Items.GENERIC.itemHintOfRebirth.createStack(), Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), Witchery.Items.GENERIC.itemBatWool.createStack(), new ItemStack(Witchery.Blocks.BRAMBLE, 1, 1));
      CreaturePower.Registry.instance().add(new CreaturePowerSpider(1, EntityCaveSpider.class));
      CreaturePower.Registry.instance().add(new CreaturePowerSpider(2, EntitySpider.class));
      CreaturePower.Registry.instance().add(new CreaturePowerCreeper(3));
      CreaturePower.Registry.instance().add(new CreaturePowerBat(4, EntityBat.class));
      CreaturePower.Registry.instance().add(new CreaturePowerSquid(5));
      CreaturePower.Registry.instance().add(new CreaturePowerGhast(6));
      CreaturePower.Registry.instance().add(new CreaturePowerBlaze(7));
      CreaturePower.Registry.instance().add(new CreaturePowerPigMan(8));
      CreaturePower.Registry.instance().add(new CreaturePowerZombie(9));
      CreaturePower.Registry.instance().add(new CreaturePowerSkeleton(10));
      CreaturePower.Registry.instance().add(new CreaturePowerJump(11, EntityMagmaCube.class));
      CreaturePower.Registry.instance().add(new CreaturePowerJump(12, EntitySlime.class));
      CreaturePower.Registry.instance().add(new CreaturePowerSpeed(13, EntitySilverfish.class));
      CreaturePower.Registry.instance().add(new CreaturePowerSpeed(14, EntityOcelot.class));
      CreaturePower.Registry.instance().add(new CreaturePowerSpeed(15, EntityWolf.class));
      CreaturePower.Registry.instance().add(new CreaturePowerSpeed(16, EntityHorse.class));
      CreaturePower.Registry.instance().add(new CreaturePowerEnderman(17));
      CreaturePower.Registry.instance().add(new CreaturePowerHeal(18, EntitySheep.class, 1));
      CreaturePower.Registry.instance().add(new CreaturePowerHeal(19, EntityCow.class, 1));
      CreaturePower.Registry.instance().add(new CreaturePowerHeal(20, EntityChicken.class, 1));
      CreaturePower.Registry.instance().add(new CreaturePowerHeal(21, EntityPig.class, 1));
      CreaturePower.Registry.instance().add(new CreaturePowerHeal(22, EntityVillager.class, 2));
      CreaturePower.Registry.instance().add(new CreaturePowerHeal(23, EntityMooshroom.class, 2));
      CreaturePower.Registry.instance().add(new CreaturePowerBat(24, EntityOwl.class));
      CreaturePower.Registry.instance().add(new CreaturePowerJump(25, EntityToad.class));
      RiteRegistry.addRecipe(1, 0, new RiteBindCircleToTalisman(), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.CIRCLE_TALISMAN), new ItemStack(Items.field_151137_ax)}), new SacrificePower(1000.0F, 20)}), EnumSet.noneOf(RitualTraits.class)).setUnlocalizedName("witchery.rite.bindcircle");
      RiteRegistry.addRecipe(2, 1, new RiteSummonItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack(), RiteSummonItem.Binding.LOCATION), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemWaystone.createStack(), Witchery.Items.GENERIC.itemEnderDew.createStack(), new ItemStack(Items.field_151114_aO)}), new SacrificePower(500.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.bindwaystone");
      RiteRegistry.addRecipe(3, 3, new RiteSummonItem(Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack(), RiteSummonItem.Binding.NONE), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemAttunedStone.createStack(), new ItemStack(Items.field_151114_aO), new ItemStack(Items.field_151137_ax), Witchery.Items.GENERIC.itemAshWood.createStack(), Witchery.Items.GENERIC.itemQuicklime.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.chargestone");
      RiteRegistry.addRecipe(4, 4, new RiteInfusionRecharge(10, 4, 40.0F, 0), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151068_bn, 1, 8193)}), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.infusionrecharge");
      RiteRegistry.addRecipe(5, 5, new RiteTeleportToWaystone(3), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemWaystoneBound.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 16, 0)).setUnlocalizedName("witchery.rite.teleporttowaystone");
      RiteRegistry.addRecipe(6, 6, new RiteTeleportEntity(3), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemWaystone.createStack(), new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemEnderDew.createStack(), new ItemStack(Items.field_151036_c)}), new SacrificePower(3000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 28, 0)).setUnlocalizedName("witchery.rite.teleportentity");
      RiteRegistry.addRecipe(7, 7, new RiteTransposeOres(8, 30, new Block[]{Blocks.field_150366_p, Blocks.field_150352_o}), new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151079_bi), new ItemStack(Items.field_151042_j), new ItemStack(Items.field_151065_br), Witchery.Items.GENERIC.itemDiamondVapour.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 40, 0)).setUnlocalizedName("witchery.rite.teleportironore");
      RiteRegistry.addRecipe(8, 8, new RiteProtectionCircleRepulsive(4, 0.8F, 0), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151008_G), new ItemStack(Items.field_151137_ax)}), new SacrificePower(500.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.protection");
      RiteRegistry.addRecipe(9, 9, new RiteProtectionCircleAttractive(4, 0.8F, 0), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151123_aH), new ItemStack(Items.field_151137_ax)}), new SacrificePower(500.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.imprisonment");
      RiteRegistry.addRecipe(10, 10, new RiteProtectionCircleBarrier(4, 5, 1.2F, false, 0), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Blocks.field_150343_Z), new ItemStack(Items.field_151137_ax)}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(500.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.barrier");
      RiteRegistry.addRecipe(11, 11, new RiteProtectionCircleBarrier(6, 6, 1.4F, true, 0), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Blocks.field_150343_Z), new ItemStack(Items.field_151114_aO)}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(1000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.barrierlarge");
      RiteRegistry.addRecipe(12, 12, new RiteProtectionCircleBarrier(6, 4, 0.0F, true, 60), new SacrificeItem(new ItemStack[]{new ItemStack(Blocks.field_150343_Z), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.barrierportable");
      RiteRegistry.addRecipe(13, 13, new RiteRaiseVolcano(8, 8), new SacrificeItem(new ItemStack[]{new ItemStack(Blocks.field_150348_b), new ItemStack(Items.field_151064_bs), new ItemStack(Items.field_151010_B), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 16)).setUnlocalizedName("witchery.rite.volcano");
      RiteRegistry.addRecipe(14, 14, new RiteWeatherCallStorm(0, 3, 8), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151041_m), Witchery.Items.GENERIC.itemAshWood.createStack()}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(1000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.storm");
      RiteRegistry.addRecipe(15, 15, new RiteWeatherCallStorm(3, 7, 18), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151052_q), Witchery.Items.GENERIC.itemAshWood.createStack()}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.stormlarge");
      RiteRegistry.addRecipe(16, 16, new RiteWeatherCallStorm(3, 7, 18), new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151040_l), Witchery.Items.GENERIC.itemAshWood.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.stormportable");
      RiteRegistry.addRecipe(17, 17, new RiteEclipse(), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151049_t), Witchery.Items.GENERIC.itemQuicklime.createStack()}), new SacrificePower(3000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_AT_DAY), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.eclipse");
      RiteRegistry.addRecipe(18, 18, new RiteEclipse(), new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151036_c), Witchery.Items.GENERIC.itemQuicklime.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.of(RitualTraits.ONLY_AT_DAY), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.eclipseportable");
      RiteRegistry.addRecipe(19, 19, new RitePartEarth(60, 1, 10), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemBrewOfErosion.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.partearth");
      RiteRegistry.addRecipe(20, 20, new RiteRaiseColumn(4, 8), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), new ItemStack(Blocks.field_150434_aF), new ItemStack(Items.field_151016_H)}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack())}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.raiseearth");
      RiteRegistry.addRecipe(21, 23, new RiteBanishDemon(9), new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151065_br), Witchery.Items.GENERIC.itemWaystone.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.banishdemonportable");
      RiteRegistry.addRecipe(22, 24, new RiteBanishDemon(9), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151065_br), Witchery.Items.GENERIC.itemWaystone.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.banishdemon");
      RiteRegistry.addRecipe(23, 25, new RiteSummonCreature(EntityDemon.class, false), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemRefinedEvil.createStack(), new ItemStack(Items.field_151065_br), new ItemStack(Items.field_151079_bi)}), new SacrificeLiving(EntityVillager.class), new SacrificePower(3000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 40)).setUnlocalizedName("witchery.rite.summondemon");
      RiteRegistry.addRecipe(24, 26, new RiteSummonCreature(EntityDemon.class, false), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemRefinedEvil.createStack(), new ItemStack(Items.field_151072_bj), new ItemStack(Items.field_151079_bi), Witchery.Items.GENERIC.itemAttunedStone.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), new SacrificePower(3000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 40)).setUnlocalizedName("witchery.rite.summondemonexpensive");
      RiteRegistry.addRecipe(25, 27, new RiteSummonCreature(EntityWither.class, false), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151144_bL, 1, 1), Witchery.Items.GENERIC.itemDiamondVapour.createStack(), new ItemStack(Items.field_151079_bi)}), new SacrificeLiving(EntityVillager.class), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 28), new Circle(0, 0, 40)).setUnlocalizedName("witchery.rite.summonwither");
      RiteRegistry.addRecipe(26, 28, new RiteSummonCreature(EntityWither.class, false), new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151144_bL, 1, 1), new ItemStack(Items.field_151045_i), new ItemStack(Items.field_151079_bi), Witchery.Items.GENERIC.itemAttunedStone.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 28), new Circle(0, 0, 40)).setUnlocalizedName("witchery.rite.summonwitherexpensive");
      this.infusionLight = new InfusionLight(1);
      Infusion.Registry.instance().add(this.infusionLight);
      RiteRegistry.addRecipe(27, 31, new RiteInfusePlayers(this.infusionLight, 200, 4), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemGhostOfTheLight.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.infusionlight");
      this.infusionWorld = new InfusionOverworld(2);
      Infusion.Registry.instance().add(this.infusionWorld);
      RiteRegistry.addRecipe(28, 32, new RiteInfusePlayers(this.infusionWorld, 200, 4), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemSoulOfTheWorld.createStack()}), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.infusionearth");
      this.infusionEnder = new InfusionOtherwhere(3);
      Infusion.Registry.instance().add(this.infusionEnder);
      RiteRegistry.addRecipe(29, 33, new RiteInfusePlayers(this.infusionEnder, 200, 4), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemSpiritOfOtherwhere.createStack()}), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 16, 0), new Circle(0, 28, 0)).setUnlocalizedName("witchery.rite.infusionender");
      this.infusionBeast = new InfusionInfernal(4);
      Infusion.Registry.instance().add(this.infusionBeast);
      RiteRegistry.addRecipe(30, 34, new RiteInfusePlayers(this.infusionBeast, 200, 4), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemInfernalAnimus.createStack()}), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 16), new Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.infusionhell");
      RiteRegistry.addRecipe(31, 35, new RiteSummonItem(Witchery.Items.GENERIC.itemBroomEnchanted.createStack(), RiteSummonItem.Binding.NONE), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemBroom.createStack(), Witchery.Items.GENERIC.itemFlyingOintment.createStack()}), new SacrificePower(3000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_AT_NIGHT), new Circle(16, 0, 0), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.infusionsky");
      RiteRegistry.addRecipe(32, 36, new RiteSummonItem(Witchery.Items.GENERIC.itemNecroStone.createStack(), RiteSummonItem.Binding.NONE), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemAttunedStone.createStack(), new ItemStack(Items.field_151103_aS), new ItemStack(Items.field_151078_bh), Witchery.Items.GENERIC.itemAshWood.createStack(), new ItemStack(Items.field_151040_l), Witchery.Items.GENERIC.itemSpectralDust.createStack()}), new SacrificePower(1000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_AT_NIGHT), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.necrostone");
      RiteRegistry.addRecipe(33, 30, new RiteSummonCreature(EntityFamiliar.class, true), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemDropOfLuck.createStack(), new ItemStack(Items.field_151147_al), new ItemStack(Items.field_151043_k), new ItemStack(Witchery.Items.ARTHANA)}), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.summonfamiliar");
      RiteRegistry.addRecipe(34, 2, new RiteSummonItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack(2), RiteSummonItem.Binding.COPY_LOCATION), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemWaystoneBound.createStack(), Witchery.Items.GENERIC.itemWaystone.createStack(), Witchery.Items.GENERIC.itemEnderDew.createStack(), new ItemStack(Items.field_151137_ax)}), new SacrificePower(500.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.bindwaystonecopy");
      RiteRegistry.addRecipe(35, 21, new RiteFertility(50, 15), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Dye.BONE_MEAL.createStack(), Witchery.Items.GENERIC.itemHintOfRebirth.createStack(), Witchery.Items.GENERIC.itemDiamondVapour.createStack(), Witchery.Items.GENERIC.itemQuicklime.createStack(), Witchery.Items.GENERIC.itemGypsum.createStack(), Witchery.Items.GENERIC.itemMutandis.createStack()}), new SacrificePower(3000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.fertility");
      RiteRegistry.addRecipe(36, 22, new RiteFertility(50, 15), new SacrificeItem(new ItemStack[]{Dye.BONE_MEAL.createStack(), Witchery.Items.GENERIC.itemHintOfRebirth.createStack(), Witchery.Items.GENERIC.itemDiamondVapour.createStack(), Witchery.Items.GENERIC.itemQuicklime.createStack(), Witchery.Items.GENERIC.itemGypsum.createStack(), Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.fertilityportable");
      RiteRegistry.addRecipe(37, 37, new RiteBlight(80, 15), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack(), Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), Witchery.Items.GENERIC.itemReekOfMisfortune.createStack(), new ItemStack(Items.field_151071_bq), new ItemStack(Items.field_151060_bw), new ItemStack(Items.field_151078_bh), new ItemStack(Items.field_151045_i)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.curseblight");
      RiteRegistry.addRecipe(38, 38, new RiteBlindness(80, 15), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack(), Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), Witchery.Items.GENERIC.itemReekOfMisfortune.createStack(), Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), Witchery.Items.GENERIC.itemBrewOfInk.createStack(), new ItemStack(Items.field_151170_bI), new ItemStack(Items.field_151070_bp), new ItemStack(Items.field_151045_i)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 16)).setUnlocalizedName("witchery.rite.curseblindness");
      RiteRegistry.addRecipe(39, 39, new RiteHellOnEarth(20, 15, 200.0F), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), Witchery.Items.GENERIC.itemDemonHeart.createStack(), Witchery.Items.GENERIC.itemWaystone.createStack(), new ItemStack(Items.field_151156_bN)}), new SacrificeLiving(EntityVillager.class), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(5000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_OVERWORLD, RitualTraits.ONLY_AT_NIGHT), new Circle(0, 0, 16), new Circle(0, 28, 0), new Circle(0, 0, 40)).setUnlocalizedName("witchery.rite.hellonearth");
      RiteRegistry.addRecipe(40, 29, new RiteSummonCreature(EntityWitch.class, false), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemDiamondVapour.createStack(), Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), new ItemStack(Items.field_151064_bs), new ItemStack(Witchery.Items.ARTHANA), new ItemStack(Items.field_151071_bq)}), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 16)).setUnlocalizedName("witchery.rite.summonwitch");
      RiteRegistry.addRecipe(41, 1, new RiteSummonItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack(), RiteSummonItem.Binding.LOCATION), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemWaystone.createStack(), Witchery.Items.GENERIC.itemEnderDew.createStack(), Witchery.Items.GENERIC.itemAshWood.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.bindwaystoneportable");
      RiteRegistry.addRecipe(42, 2, new RiteSummonItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack(2), RiteSummonItem.Binding.COPY_LOCATION), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemWaystoneBound.createStack(), Witchery.Items.GENERIC.itemWaystone.createStack(), Witchery.Items.GENERIC.itemEnderDew.createStack(), Witchery.Items.GENERIC.itemQuicklime.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.bindwaystonecopyportable");
      RiteRegistry.addRecipe(43, 22, new RiteNaturesPower(14, 8, 150, 2), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), new ItemStack(Witchery.Blocks.SAPLING, 1, 0), new ItemStack(Witchery.Blocks.SAPLING, 1, 1), new ItemStack(Witchery.Blocks.SAPLING, 1, 2), new ItemStack(Blocks.field_150345_g, 1, 0), new ItemStack(Blocks.field_150345_g, 1, 1), new ItemStack(Blocks.field_150345_g, 1, 2), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.naturespower");
      RiteRegistry.addRecipe(44, 36, new RitePriorIncarnation(5, 16), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemNecroStone.createStack(), Witchery.Items.GENERIC.itemDogTongue.createStack(), new ItemStack(Items.field_151103_aS), Witchery.Items.GENERIC.itemSpectralDust.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 16)).setUnlocalizedName("witchery.rite.priorincarnation");
      RiteRegistry.addRecipe(45, 0, new RiteBindCircleToTalisman(), new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.CIRCLE_TALISMAN), new ItemStack(Items.field_151114_aO), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class)).setUnlocalizedName("witchery.rite.bindcircleportable");
      RiteRegistry.addRecipe(46, 20, new RiteRaiseColumn(6, 8), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), new ItemStack(Blocks.field_150434_aF), new ItemStack(Items.field_151137_ax)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.raiseearth");
      RiteRegistry.addRecipe(47, 20, new RiteRaiseColumn(9, 8), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), new ItemStack(Blocks.field_150434_aF), new ItemStack(Items.field_151114_aO)}), EnumSet.noneOf(RitualTraits.class), new Circle(40, 0, 0)).setUnlocalizedName("witchery.rite.raiseearth");
      RiteRegistry.addRecipe(48, 48, new RiteCurseCreature(true, "witcheryCursed", 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), new ItemStack(Items.field_151071_bq), new ItemStack(Items.field_151016_H), Witchery.Items.GENERIC.itemBrewGrotesque.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_IN_STROM), new Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.cursecreature1");
      RiteRegistry.addRecipe(49, 49, new RiteCurseCreature(false, "witcheryCursed", 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(), new ItemStack(Items.field_151070_bp), new ItemStack(Items.field_151016_H), Witchery.Items.GENERIC.itemBrewOfLove.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.removecurse1");
      RiteRegistry.addRecipe(50, 35, new RiteSummonItem(new ItemStack(Witchery.Items.MYSTIC_BRANCH), RiteSummonItem.Binding.NONE), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemBranchEnt.createStack(), Witchery.Items.GENERIC.itemMysticUnguent.createStack()}), new SacrificePower(3000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_AT_NIGHT), new Circle(16, 0, 0), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.infusiontree");
      RiteRegistry.addRecipe(51, 20, new RiteCookItem(5.0F, 0.08D), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151072_bj), Witchery.Items.GENERIC.itemAshWood.createStack(), new ItemStack(Items.field_151044_h)}), new SacrificePower(1000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 16)).setUnlocalizedName("witchery.rite.cookfood");
      RiteRegistry.addRecipe(52, 48, new RiteCurseCreature(true, "witcheryInsanity", 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), new ItemStack(Items.field_151170_bI), new ItemStack(Items.field_151102_aT), Witchery.Items.GENERIC.itemBrewGrotesque.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_IN_STROM), new Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.curseinsanity1");
      RiteRegistry.addRecipe(53, 49, new RiteCurseCreature(false, "witcheryInsanity", 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(), new ItemStack(Items.field_151174_bG), new ItemStack(Items.field_151102_aT), Witchery.Items.GENERIC.itemBrewOfLove.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.removeinsanity1");
      RiteRegistry.addRecipe(54, 1, new RiteBindFamiliar(7), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemTearOfTheGoddess.createStack(), Witchery.Items.GENERIC.itemOdourOfPurity.createStack(), Witchery.Items.GENERIC.itemWhiffOfMagic.createStack(), new ItemStack(Items.field_151045_i), Witchery.Items.GENERIC.itemInfernalBlood.createStack()}), new SacrificePower(8000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.bindfamiliar");
      RiteRegistry.addRecipe(55, 30, new RiteCallFamiliar(7), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(), Witchery.Items.GENERIC.itemHintOfRebirth.createStack(), Witchery.Items.GENERIC.itemWhiffOfMagic.createStack()}), new SacrificePower(1000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.callfamiliar");
      RiteRegistry.addRecipe(56, 50, new RiteCursePoppets(1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), Witchery.Items.POPPET.antiVoodooPoppet.createStack(), new ItemStack(Items.field_151065_br), Witchery.Items.GENERIC.itemSpectralDust.createStack()}), new SacrificePower(7000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.corruptvoodooprotection");
      RiteRegistry.addRecipe(57, 35, new RiteSummonItem(new ItemStack(Witchery.Blocks.CRYSTAL_BALL), RiteSummonItem.Binding.NONE), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemQuartzSphere.createStack(), new ItemStack(Items.field_151043_k), Witchery.Items.GENERIC.itemHappenstanceOil.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_AT_NIGHT), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.infusionfuture");
      RiteRegistry.addRecipe(58, 20, new RiteCookItem(5.0F, 0.16D), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack(), new ItemStack(Items.field_151072_bj), Witchery.Items.GENERIC.itemAshWood.createStack(), new ItemStack(Items.field_151065_br)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 16)).setUnlocalizedName("witchery.rite.cookfood");
      RiteRegistry.addRecipe(59, 48, new RiteCurseCreature(true, "witcherySinking", 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), Dye.INK_SAC.createStack(), new ItemStack(Items.field_151075_bm), Witchery.Items.GENERIC.itemBrewGrotesque.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_IN_STROM), new Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.cursesinking1");
      RiteRegistry.addRecipe(60, 49, new RiteCurseCreature(false, "witcherySinking", 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(), Dye.BONE_MEAL.createStack(), new ItemStack(Items.field_151075_bm), Witchery.Items.GENERIC.itemBrewOfTheDepths.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.removesinking1");
      RiteRegistry.addRecipe(61, 35, new RiteSummonItem(Witchery.Items.GENERIC.itemSeerStone.createStack(), RiteSummonItem.Binding.NONE), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemQuartzSphere.createStack(), new ItemStack(Blocks.field_150343_Z), Witchery.Items.GENERIC.itemHappenstanceOil.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_AT_NIGHT), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.infusionseerstone");
      RiteRegistry.addRecipe(62, 48, new RiteCurseCreature(true, "witcheryOverheating", 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), Witchery.Items.GENERIC.itemInfernalBlood.createStack(), new ItemStack(Items.field_151072_bj), Witchery.Items.GENERIC.itemBrewGrotesque.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_IN_STROM), new Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.curseoverheating");
      RiteRegistry.addRecipe(63, 49, new RiteCurseCreature(false, "witcheryOverheating", 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(), Witchery.Items.GENERIC.itemIcyNeedle.createStack(), new ItemStack(Items.field_151072_bj), Witchery.Items.GENERIC.itemBrewOfTheDepths.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.cureoverheating");
      RiteRegistry.addRecipe(64, 22, new RiteClimateChange(16), new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151070_bp), Witchery.Items.GENERIC.itemToeOfFrog.createStack(), Witchery.Items.GENERIC.itemBatWool.createStack(), Witchery.Items.GENERIC.itemDogTongue.createStack(), Witchery.Items.GENERIC.itemOwletsWing.createStack(), Witchery.Items.GENERIC.itemAttunedStone.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(40, 0, 0)).setUnlocalizedName("witchery.rite.climatechange");
      RiteRegistry.addRecipe(65, 12, new RiteSphereEffect(8, Witchery.Blocks.PERPETUAL_ICE), new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151048_u), Witchery.Items.GENERIC.itemFrozenHeart.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.iceshell");
      RiteRegistry.addRecipe(66, 38, new RiteRainOfToads(5, 16, 10), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack(), Witchery.Items.GENERIC.itemRedstoneSoup.createStack(), Witchery.Items.GENERIC.itemReekOfMisfortune.createStack(), Witchery.Items.GENERIC.itemToeOfFrog.createStack(), new ItemStack(Items.field_151131_as), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.rainoffrogs");
      RiteRegistry.addRecipe(67, 4, new RiteGlyphicTransformation(), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemGypsum.createStack(), new ItemStack(Witchery.Items.ARTHANA)}), new SacrificePower(1000.0F, 20)}), EnumSet.noneOf(RitualTraits.class)).setUnlocalizedName("witchery.rite.glyphictransform");
      RiteRegistry.addRecipe(68, 7, new RiteCallCreatures(64.0F, new Class[]{EntityPig.class, EntityChicken.class, EntityCow.class, EntitySheep.class, EntityMooshroom.class, EntityWolf.class, EntityOcelot.class}), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Items.field_151117_aB), new ItemStack(Blocks.field_150407_cf), new ItemStack(Items.field_151034_e), new ItemStack(Items.field_151082_bd), new ItemStack(Items.field_151115_aP), new ItemStack(Blocks.field_150337_Q), new ItemStack(Items.field_151172_bF), new ItemStack(Items.field_151014_N)}), new SacrificePower(6000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 40, 0)).setUnlocalizedName("witchery.rite.callbeasts");
      RiteRegistry.addRecipe(69, 7, new RiteSetNBT(5, "WITCManifestDuration", 150, 25), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemSpectralDust.createStack(), Witchery.Items.GENERIC.itemMellifluousHunger.createStack(), Witchery.Items.GENERIC.itemNecroStone.createStack(), new ItemStack(Items.field_151005_D), new ItemStack(Witchery.Items.ARTHANA), new ItemStack(Items.field_151016_H)}), new SacrificePower(5000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 16, 0)).setUnlocalizedName("witchery.rite.manifest");
      RiteRegistry.addRecipe(70, 22, new RiteForestation(20, 8, 60, Blocks.field_150345_g, 0), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Blocks.field_150345_g, 1, 0), new ItemStack(Witchery.Blocks.WICKER_BUNDLE), Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), Witchery.Items.GENERIC.itemBranchEnt.createStack()}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.forestation");
      RiteRegistry.addRecipe(71, 22, new RiteForestation(20, 8, 60, Blocks.field_150345_g, 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Blocks.field_150345_g, 1, 1), new ItemStack(Witchery.Blocks.WICKER_BUNDLE), Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), Witchery.Items.GENERIC.itemBranchEnt.createStack()}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.forestation").setShowInBook(false);
      RiteRegistry.addRecipe(72, 22, new RiteForestation(20, 8, 60, Blocks.field_150345_g, 2), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Blocks.field_150345_g, 1, 2), new ItemStack(Witchery.Blocks.WICKER_BUNDLE), Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), Witchery.Items.GENERIC.itemBranchEnt.createStack()}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.forestation").setShowInBook(false);
      RiteRegistry.addRecipe(73, 22, new RiteForestation(20, 8, 60, Blocks.field_150345_g, 3), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Blocks.field_150345_g, 1, 3), new ItemStack(Witchery.Blocks.WICKER_BUNDLE), Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), Witchery.Items.GENERIC.itemBranchEnt.createStack()}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.forestation").setShowInBook(false);
      RiteRegistry.addRecipe(74, 22, new RiteForestation(20, 8, 60, Witchery.Blocks.SAPLING, 0), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Blocks.SAPLING, 1, 0), new ItemStack(Witchery.Blocks.WICKER_BUNDLE), Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), Witchery.Items.GENERIC.itemBranchEnt.createStack()}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.forestation").setShowInBook(false);
      RiteRegistry.addRecipe(75, 22, new RiteForestation(20, 8, 60, Witchery.Blocks.SAPLING, 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Blocks.SAPLING, 1, 1), new ItemStack(Witchery.Blocks.WICKER_BUNDLE), Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), Witchery.Items.GENERIC.itemBranchEnt.createStack()}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.forestation").setShowInBook(false);
      RiteRegistry.addRecipe(76, 22, new RiteForestation(20, 8, 60, Witchery.Blocks.SAPLING, 2), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Blocks.SAPLING, 1, 2), new ItemStack(Witchery.Blocks.WICKER_BUNDLE), Witchery.Items.GENERIC.itemBrewOfSprouting.createStack(), Witchery.Items.GENERIC.itemBranchEnt.createStack()}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.forestation").setShowInBook(false);
      RiteRegistry.addRecipe(77, 13, new RiteRaiseVolcano(8, 8), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Blocks.field_150347_e), new ItemStack(Items.field_151064_bs), new ItemStack(Items.field_151010_B)}), new SacrificeOptionalItem(Witchery.Items.GENERIC.itemWaystoneBound.createStack()), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 16)).setUnlocalizedName("witchery.rite.volcano");
      RiteRegistry.addRecipe(78, 48, new RiteCurseCreature(true, "witcheryWakingNightmare", 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), Witchery.Items.GENERIC.itemMellifluousHunger.createStack(), Witchery.Items.GENERIC.itemTormentedTwine.createStack(), new ItemStack(Items.field_151045_i), Witchery.Items.GENERIC.itemBrewGrotesque.createStack()}), new SacrificePower(10000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_IN_STROM), new Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.cursenightmare");
      RiteRegistry.addRecipe(79, 49, new RiteCurseCreature(false, "witcheryWakingNightmare", 1), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(), new ItemStack(Items.field_151150_bK), Witchery.Items.GENERIC.itemTormentedTwine.createStack(), Witchery.Items.GENERIC.itemBrewOfLove.createStack()}), new SacrificePower(2000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.curenightmare");
      RiteRegistry.addRecipe(80, 35, new RiteSummonItem(Witchery.Items.GENERIC.itemBrewOfSoaring.createStack(), RiteSummonItem.Binding.NONE), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemInfusionBase.createStack(), Witchery.Items.GENERIC.itemBroom.createStack(), new ItemStack(Items.field_151008_G), new ItemStack(Witchery.Items.ARTHANA)}), new SacrificeLiving(EntityOwl.class), new SacrificePower(3000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.infusebrewsoaring");
      RiteRegistry.addRecipe(81, 35, new RiteSummonItem(Witchery.Items.GENERIC.itemBrewGrave.createStack(), RiteSummonItem.Binding.NONE), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemInfusionBase.createStack(), new ItemStack(Items.field_151103_aS), Witchery.Items.GENERIC.itemWeb.createStack(), Witchery.Items.GENERIC.itemNecroStone.createStack()}), new SacrificeLiving(EntityZombie.class), new SacrificePower(3000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.infusebrewgrave");
      RiteRegistry.addRecipe(82, 36, new RiteSummonItem(new ItemStack(Witchery.Items.SPECTRAL_STONE), RiteSummonItem.Binding.NONE), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemNecroStone.createStack(), Witchery.Items.GENERIC.itemCongealedSpirit.createStack(), Witchery.Items.GENERIC.itemCondensedFear.createStack(), Witchery.Items.GENERIC.itemSpectralDust.createStack(), new ItemStack(Witchery.Items.BOLINE)}), new SacrificePower(6000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_AT_NIGHT), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.spectralstone").setConsumeNecroStone();
      RiteRegistry.addRecipe(83, 1, new RiteSummonSpectralStone(5), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.SPECTRAL_STONE), Witchery.Items.GENERIC.itemSpectralDust.createStack(), new ItemStack(Witchery.Items.BOLINE)}), new SacrificePower(5000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.bindspectral");
      RiteRegistry.addRecipe(84, 1, new RiteBindSpiritsToFetish(5), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Blocks.FETISH_SCARECROW), Witchery.Items.GENERIC.itemAttunedStone.createStack(), Witchery.Items.GENERIC.itemNecroStone.createStack(), new ItemStack(Witchery.Items.BOLINE)}), new SacrificePower(6000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.bindfetish");
      RiteRegistry.addRecipe(85, 1, new RiteBindSpiritsToFetish(5), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Blocks.FETISH_TREANT_IDOL), Witchery.Items.GENERIC.itemAttunedStone.createStack(), Witchery.Items.GENERIC.itemNecroStone.createStack(), new ItemStack(Witchery.Items.BOLINE)}), new SacrificePower(6000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.bindfetish").setShowInBook(false);
      RiteRegistry.addRecipe(86, 1, new RiteBindSpiritsToFetish(5), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Blocks.FETISH_WITCHS_LADDER), Witchery.Items.GENERIC.itemAttunedStone.createStack(), Witchery.Items.GENERIC.itemNecroStone.createStack(), new ItemStack(Witchery.Items.BOLINE)}), new SacrificePower(6000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.bindfetish").setShowInBook(false);
      RiteRegistry.addRecipe(87, 26, new RiteSummonCreature(EntityImp.class, false), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemRefinedEvil.createStack(), Witchery.Items.GENERIC.itemInfernalBlood.createStack(), new ItemStack(Items.field_151079_bi), Witchery.Items.GENERIC.itemAttunedStone.createStack()}), new SacrificePower(5000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.summonimp");
      RiteRegistry.addRecipe(88, 1, new RiteSummonItem(Witchery.Items.GENERIC.itemWaystonePlayerBound.createStack(), RiteSummonItem.Binding.ENTITY), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemWaystone.createStack(), Witchery.Items.GENERIC.itemEnderDew.createStack(), new ItemStack(Items.field_151123_aH), new ItemStack(Items.field_151126_ay)}), new SacrificePower(500.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.bindwaystonetoplayer");
      RiteRegistry.addRecipe(89, 1, new RiteSummonItem(Witchery.Items.GENERIC.itemWaystonePlayerBound.createStack(), RiteSummonItem.Binding.ENTITY), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemWaystone.createStack(), Witchery.Items.GENERIC.itemEnderDew.createStack(), new ItemStack(Items.field_151123_aH), Witchery.Items.GENERIC.itemIcyNeedle.createStack(), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.bindwaystonetoplayer");
      RiteRegistry.addRecipe(90, 1, new RiteSummonItem(new ItemStack(Witchery.Blocks.STATUE_OF_WORSHIP), RiteSummonItem.Binding.PLAYER), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Blocks.STATUE_OF_WORSHIP), Witchery.Items.GENERIC.itemBelladonnaFlower.createStack(), new ItemStack(Blocks.field_150328_O), new ItemStack(Blocks.field_150327_N)}), new SacrificePower(4000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.bindstatuetoplayer");
      RiteRegistry.addRecipe(91, 5, new RiteTeleportToWaystone(3), new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemWaystonePlayerBound.createStack()}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 16, 0)).setUnlocalizedName("witchery.rite.teleporttowaystone");
      RiteRegistry.addRecipe(92, 48, new RiteCurseOfTheWolf(true), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(), new ItemStack(Witchery.Blocks.WOLFHEAD, 1, 1), Witchery.Items.GENERIC.itemWolfsbane.createStack(), new ItemStack(Items.field_151045_i), Witchery.Items.GENERIC.itemBrewGrotesque.createStack()}), new SacrificePower(10000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_AT_NIGHT), new Circle(0, 0, 28)).setUnlocalizedName("witchery.rite.wolfcurse.book");
      RiteRegistry.addRecipe(93, 49, new RiteCurseOfTheWolf(false), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(), new ItemStack(Witchery.Items.SILVER_SWORD), Witchery.Items.GENERIC.itemWolfsbane.createStack(), new ItemStack(Items.field_151045_i), Witchery.Items.GENERIC.itemBrewOfLove.createStack()}), new SacrificePower(10000.0F, 20)}), EnumSet.of(RitualTraits.ONLY_AT_NIGHT), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.wolfcure.book");
      RiteRegistry.addRecipe(94, 49, new RiteRemoveVampirism(), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1), Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(), new ItemStack(Witchery.Items.SILVER_SWORD), new ItemStack(Witchery.Items.SEEDS_GARLIC), new ItemStack(Items.field_151045_i), Witchery.Items.GENERIC.itemBrewOfLove.createStack()}), new SacrificePower(10000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(16, 0, 0)).setUnlocalizedName("witchery.rite.vampirecure.book");
      RiteRegistry.addRecipe(95, 35, new RiteSummonItem(new ItemStack(Witchery.Items.MIRROR), RiteSummonItem.Binding.NONE), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{Witchery.Items.GENERIC.itemBrewOfHollowTears.createStack(), new ItemStack(Items.field_151043_k), new ItemStack(Blocks.field_150410_aZ)}), new SacrificePower(2000.0F, 20), new SacrificeLiving(EntityDemon.class)}), EnumSet.noneOf(RitualTraits.class), new Circle(28, 0, 0)).setUnlocalizedName("witchery.rite.infusionmirror");
      RiteRegistry.addRecipe(96, 28, new RiteSummonCreature(EntityReflection.class, false), new SacrificeMultiple(new Sacrifice[]{new SacrificeItem(new ItemStack[]{new ItemStack(Witchery.Items.MIRROR), Witchery.Items.GENERIC.itemEnderDew.createStack(), new ItemStack(Items.field_151065_br), Witchery.Items.GENERIC.itemQuartzSphere.createStack()}), new SacrificePower(5000.0F, 20)}), EnumSet.noneOf(RitualTraits.class), new Circle(0, 0, 40)).setUnlocalizedName("witchery.rite.summonreflection");
      double DEFAULT_FORCE_CHANCE = 0.05D;
      PredictionManager.instance().addPrediction(new PredictionFight(1, 13, 0.05D, "witchery.prediction.zombie", EntityZombie.class, false));
      PredictionManager.instance().addPrediction(new PredictionArrow(2, 13, 0.05D, "witchery.prediction.arrowhit"));
      PredictionManager.instance().addPrediction(new PredictionFight(3, 3, 0.05D, "witchery.prediction.ent", EntityEnt.class, false));
      PredictionManager.instance().addPrediction(new PredictionFall(4, 13, 0.05D, "witchery.prediction.fall"));
      PredictionManager.instance().addPrediction(new PredictionMultiMine(5, 8, 0.05D, "witchery.prediction.iron", 1212, 0.01D, Blocks.field_150366_p, new ItemStack(Blocks.field_150366_p), 8, 20));
      PredictionManager.instance().addPrediction(new PredictionMultiMine(6, 3, 0.05D, "witchery.prediction.diamond", 1208, 0.01D, Blocks.field_150348_b, new ItemStack(Items.field_151045_i), 1, 1));
      PredictionManager.instance().addPrediction(new PredictionMultiMine(7, 3, 0.05D, "witchery.prediction.emerald", 1208, 0.01D, Blocks.field_150348_b, new ItemStack(Items.field_151166_bC), 1, 1));
      PredictionManager.instance().addPrediction(new PredictionBuriedTreasure(8, 2, 0.05D, "witchery.prediction.treasure", 1210, 0.01D, "mineshaftCorridor"));
      PredictionManager.instance().addPrediction(new PredictionFallInLove(9, 2, 0.05D, "witchery.prediction.love", 1210, 0.01D));
      PredictionManager.instance().addPrediction(new PredictionFight(10, 2, 0.05D, "witchery.prediction.bababad", EntityBabaYaga.class, false));
      PredictionManager.instance().addPrediction(new PredictionFight(11, 2, 0.05D, "witchery.prediction.babagood", EntityBabaYaga.class, true));
      PredictionManager.instance().addPrediction(new PredictionFight(12, 3, 0.05D, "witchery.prediction.friend", EntityWolf.class, true));
      PredictionManager.instance().addPrediction(new PredictionRescue(13, 13, 0.05D, "witchery.prediction.rescued", 1208, 0.01D, EntityOwl.class));
      PredictionManager.instance().addPrediction(new PredictionRescue(14, 13, 0.05D, "witchery.prediction.rescued", 1208, 0.01D, EntityWolf.class));
      PredictionManager.instance().addPrediction(new PredictionWet(15, 13, 0.05D, "witchery.prediction.wet"));
      PredictionManager.instance().addPrediction(new PredictionNetherTrip(16, 3, 0.05D, "witchery.prediction.tothenether"));
      PredictionManager.instance().addPrediction(new PredictionMultiMine(17, 13, 0.05D, "witchery.prediction.coal", 1208, 0.01D, Blocks.field_150365_q, new ItemStack(Items.field_151044_h), 10, 20));
   }

   public void init() {
      ItemStack dust = Witchery.Items.GENERIC.itemSilverDust.createStack();
      List<ItemStack> silverDust = OreDictionary.getOres("dustSilver");
      if (silverDust != null && !silverDust.isEmpty()) {
         GameRegistry.addShapelessRecipe(((ItemStack)silverDust.get(0)).func_77946_l(), new Object[]{dust, dust, dust, dust, dust, dust, dust, dust, dust});
      }

      List<ItemStack> silverIngots = OreDictionary.getOres("ingotSilver");
      if (silverIngots != null && !silverIngots.isEmpty()) {
         GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Witchery.Items.SILVER_SWORD), new Object[]{"s", "s", "b", 's', "ingotSilver", 'b', new ItemStack(Items.field_151010_B)}));
         GameRegistry.addRecipe(new ShapedOreRecipe(Witchery.Items.GENERIC.itemBoltSilver.createStack(6), new Object[]{" s ", "bbb", "bbb", 's', "ingotSilver", 'b', Witchery.Items.GENERIC.itemBoltStake.createStack()}));
         GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Witchery.Blocks.WOLFTRAP), new Object[]{"sns", "w#w", "sns", '#', new ItemStack(Witchery.Blocks.BEARTRAP), 's', "ingotSilver", 'n', Witchery.Items.GENERIC.itemNullCatalyst.createStack(), 'w', Witchery.Items.GENERIC.itemWolfsbane.createStack()}));
         Item[][] hunterItems = new Item[][]{{Witchery.Items.HUNTER_BOOTS, Witchery.Items.HUNTER_BOOTS_SILVERED}, {Witchery.Items.HUNTER_LEGS, Witchery.Items.HUNTER_LEGS_SILVERED}, {Witchery.Items.HUNTER_COAT, Witchery.Items.HUNTER_COAT_SILVERED}, {Witchery.Items.HUNTER_HAT, Witchery.Items.HUNTER_HAT_SILVERED}};
         Item[][] arr$ = hunterItems;
         int len$ = hunterItems.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Item[] hunterItem = arr$[i$];
            ShapedOreRecipe recipe = new ShapedOreRecipe(new ItemStack(hunterItem[1]), new Object[]{"dwd", "w#w", "dsd", '#', new ItemStack(hunterItem[0]), 's', new ItemStack(Items.field_151007_F), 'w', Witchery.Items.GENERIC.itemWolfsbane.createStack(), 'd', "ingotSilver"}) {
               public ItemStack func_77572_b(InventoryCrafting inv) {
                  ItemStack result = this.func_77571_b().func_77946_l();

                  for(int i = 0; i < inv.func_70302_i_(); ++i) {
                     ItemStack material = inv.func_70301_a(i);
                     if (material != null && material.func_77942_o()) {
                        result.func_77982_d((NBTTagCompound)material.field_77990_d.func_74737_b());
                     }
                  }

                  return result;
               }
            };
            GameRegistry.addRecipe(recipe);
         }
      }

   }

   public void postInit() {
      if (Config.instance().smeltAllSaplingsToWoodAsh) {
         ArrayList<ItemStack> saplingTypes = OreDictionary.getOres("treeSapling");
         Iterator i$ = saplingTypes.iterator();

         while(i$.hasNext()) {
            ItemStack stack = (ItemStack)i$.next();
            GameRegistry.addSmelting(stack, Witchery.Items.GENERIC.itemAshWood.createStack(), 0.0F);
         }
      }

   }

   private void addPlantMineRecipe(int damageValue, ItemStack plant, ItemStack brew) {
      GameRegistry.addRecipe(new ItemStack(Witchery.Blocks.TRAPPED_PLANT, 4, damageValue), new Object[]{"ccc", "bab", 'a', plant, 'b', new ItemStack(Blocks.field_150456_au), 'c', brew});
   }

   private static ShapedRecipes getShapedRecipe(ItemStack par1ItemStack, Object... par2ArrayOfObj) {
      String s = "";
      int i = 0;
      int j = 0;
      int k = 0;
      if (par2ArrayOfObj[i] instanceof String[]) {
         String[] astring = (String[])((String[])((String[])par2ArrayOfObj[i++]));

         for(int l = 0; l < astring.length; ++l) {
            String s1 = astring[l];
            ++k;
            j = s1.length();
            s = s + s1;
         }
      } else {
         while(par2ArrayOfObj[i] instanceof String) {
            String s2 = (String)par2ArrayOfObj[i++];
            ++k;
            j = s2.length();
            s = s + s2;
         }
      }

      HashMap hashmap;
      for(hashmap = new HashMap(); i < par2ArrayOfObj.length; i += 2) {
         Character character = (Character)par2ArrayOfObj[i];
         ItemStack itemstack1 = null;
         if (par2ArrayOfObj[i + 1] instanceof Item) {
            itemstack1 = new ItemStack((Item)par2ArrayOfObj[i + 1]);
         } else if (par2ArrayOfObj[i + 1] instanceof Block) {
            itemstack1 = new ItemStack((Block)par2ArrayOfObj[i + 1], 1, 32767);
         } else if (par2ArrayOfObj[i + 1] instanceof ItemStack) {
            itemstack1 = (ItemStack)par2ArrayOfObj[i + 1];
         }

         hashmap.put(character, itemstack1);
      }

      ItemStack[] aitemstack = new ItemStack[j * k];

      for(int i1 = 0; i1 < j * k; ++i1) {
         char c0 = s.charAt(i1);
         if (hashmap.containsKey(c0)) {
            aitemstack[i1] = ((ItemStack)hashmap.get(c0)).func_77946_l();
         } else {
            aitemstack[i1] = null;
         }
      }

      ShapedRecipes shapedrecipes = new ShapedRecipes(j, k, aitemstack, par1ItemStack);
      return shapedrecipes;
   }
}
