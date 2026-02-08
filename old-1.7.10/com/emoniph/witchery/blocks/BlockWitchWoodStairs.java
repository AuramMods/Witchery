package com.emoniph.witchery.blocks;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;

public class BlockWitchWoodStairs extends BlockStairs {
   private final int encouragement;
   private final int flammibility;

   public BlockWitchWoodStairs(Block baseBlock, int baseMeta, int encouragement, int flammibility) {
      super(baseBlock, baseMeta);
      this.flammibility = flammibility;
      this.encouragement = encouragement;
      this.func_149647_a(WitcheryCreativeTab.INSTANCE);
   }

   public Block func_149663_c(String blockName) {
      BlockUtil.registerBlock(this, blockName);
      super.func_149663_c(blockName);
      Blocks.field_150480_ab.setFireInfo(this, this.encouragement, this.flammibility);
      return this;
   }
}
