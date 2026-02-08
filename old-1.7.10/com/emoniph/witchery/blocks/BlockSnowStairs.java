package com.emoniph.witchery.blocks;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;

public class BlockSnowStairs extends BlockStairs {
   public BlockSnowStairs(Block baseBlock, int baseMeta) {
      super(baseBlock, baseMeta);
      this.func_149711_c(0.2F);
      this.func_149672_a(field_149773_n);
      this.func_149647_a(WitcheryCreativeTab.INSTANCE);
   }

   public Block func_149663_c(String blockName) {
      BlockUtil.registerBlock(this, blockName);
      super.func_149663_c(blockName);
      return this;
   }
}
