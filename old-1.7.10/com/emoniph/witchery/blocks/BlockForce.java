package com.emoniph.witchery.blocks;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BlockForce extends BlockBase {
   private final boolean transparent;

   public BlockForce(boolean transparent) {
      super(Material.field_151576_e);
      this.transparent = transparent;
      super.registerWithCreateTab = false;
      this.func_149722_s();
      this.func_149752_b(9999.0F);
      this.func_149713_g(0);
      this.func_149672_a(transparent ? field_149778_k : field_149769_e);
   }

   public int func_149645_b() {
      return this.transparent ? -1 : super.func_149645_b();
   }

   protected boolean func_149700_E() {
      return false;
   }

   public int func_149745_a(Random rand) {
      return 0;
   }

   public int func_149701_w() {
      return 0;
   }

   public boolean func_149662_c() {
      return this.transparent ? false : super.func_149662_c();
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return null;
   }
}
