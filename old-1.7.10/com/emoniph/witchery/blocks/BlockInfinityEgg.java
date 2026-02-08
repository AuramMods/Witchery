package com.emoniph.witchery.blocks;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDragonEgg;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class BlockInfinityEgg extends BlockDragonEgg {
   public BlockInfinityEgg() {
      this.func_149711_c(3.0F);
      this.func_149752_b(15.0F);
      this.func_149672_a(field_149780_i);
      this.func_149715_a(0.125F);
   }

   public Block func_149663_c(String blockName) {
      this.func_149647_a(WitcheryCreativeTab.INSTANCE);
      BlockUtil.registerBlock(this, blockName);
      return super.func_149663_c(blockName);
   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
      return true;
   }

   public void func_149699_a(World world, int x, int y, int z, EntityPlayer player) {
   }
}
