package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

public class BlockPerpetualIceFence extends BlockFence {
   public BlockPerpetualIceFence(String name) {
      super(name, Material.field_151588_w);
      this.func_149711_c(2.0F);
      this.func_149752_b(5.0F);
      this.func_149647_a(WitcheryCreativeTab.INSTANCE);
   }

   public Block func_149663_c(String blockName) {
      BlockUtil.registerBlock(this, blockName);
      return super.func_149663_c(blockName);
   }

   @SideOnly(Side.CLIENT)
   public int func_149701_w() {
      return 1;
   }

   public boolean func_149826_e(IBlockAccess world, int x, int y, int z) {
      Block block = world.func_147439_a(x, y, z);
      return super.func_149826_e(world, x, y, z) || block == Witchery.Blocks.PERPETUAL_ICE_FENCE_GATE;
   }
}
