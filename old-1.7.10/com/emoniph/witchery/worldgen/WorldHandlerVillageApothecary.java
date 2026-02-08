package com.emoniph.witchery.worldgen;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;

public class WorldHandlerVillageApothecary implements IVillageCreationHandler, IVillageTradeHandler {
   public PieceWeight getVillagePieceWeight(Random random, int size) {
      return new PieceWeight(ComponentVillageApothecary.class, 15, 1 + (size > 2 ? random.nextInt(2) : 0));
   }

   public Class getComponentClass() {
      return ComponentVillageApothecary.class;
   }

   public Object buildComponent(PieceWeight villagePiece, Start startPiece, List pieces, Random random, int p1, int p2, int p3, int p4, int p5) {
      return ComponentVillageApothecary.buildComponent(startPiece, pieces, random, p1, p2, p3, p4, p5);
   }

   public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
      recipeList.func_77205_a(new MerchantRecipe(new ItemStack(Items.field_151166_bC, 2), Witchery.Items.GENERIC.itemDogTongue.createStack(2)));
      recipeList.func_77205_a(new MerchantRecipe(new ItemStack(Items.field_151166_bC, 1), Witchery.Items.GENERIC.itemBatWool.createStack(3)));
      recipeList.func_77205_a(new MerchantRecipe(new ItemStack(Items.field_151166_bC, 4), Witchery.Items.GENERIC.itemSpectralDust.createStack()));
      recipeList.func_77205_a(new MerchantRecipe(new ItemStack(Items.field_151166_bC, 5), new ItemStack(Witchery.Items.SEEDS_GARLIC)));
      recipeList.func_77205_a(new MerchantRecipe(new ItemStack(Items.field_151166_bC, 6), Witchery.Items.GENERIC.itemArtichoke.createStack()));
      recipeList.func_77205_a(new MerchantRecipe(new ItemStack(Items.field_151166_bC, 7), new ItemStack(Blocks.field_150328_O, 5)));
      recipeList.func_77205_a(new MerchantRecipe(new ItemStack(Items.field_151166_bC, 8), new ItemStack(Items.field_151123_aH)));
      recipeList.func_77205_a(new MerchantRecipe(new ItemStack(Items.field_151166_bC, 3), new ItemStack(Items.field_151119_aD, 5)));
   }
}
