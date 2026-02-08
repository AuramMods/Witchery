package com.emoniph.witchery.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockLight extends BlockBase {
   public BlockLight() {
      super(Material.field_151579_a);
      this.func_149715_a(1.0F);
      this.registerWithCreateTab = false;
   }

   public int func_149645_b() {
      return -1;
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149678_a(int p_149678_1_, boolean p_149678_2_) {
      return false;
   }

   public void func_149690_a(World world, int x, int y, int z, int side, float p_149690_6_, int p_149690_7_) {
   }
}
