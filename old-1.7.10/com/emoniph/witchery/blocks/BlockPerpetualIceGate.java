package com.emoniph.witchery.blocks;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

public class BlockPerpetualIceGate extends BlockFenceGate {
   public BlockPerpetualIceGate() {
      this.func_149647_a(WitcheryCreativeTab.INSTANCE);
      this.func_149713_g(3);
      this.func_149711_c(2.0F);
      this.func_149752_b(5.0F);
   }

   public Block func_149663_c(String blockName) {
      BlockUtil.registerBlock(this, blockName);
      return super.func_149663_c(blockName);
   }

   @SideOnly(Side.CLIENT)
   public int func_149701_w() {
      return 1;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int p_149691_1_, int p_149691_2_) {
      return Blocks.field_150432_aD.func_149733_h(p_149691_1_);
   }
}
