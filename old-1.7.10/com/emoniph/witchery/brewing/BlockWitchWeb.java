package com.emoniph.witchery.brewing;

import com.emoniph.witchery.blocks.BlockBase;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BlockWitchWeb extends BlockBase {
   public BlockWitchWeb() {
      super(Material.field_151580_n);
      this.func_149713_g(1);
      this.func_149711_c(8.0F);
      this.registerWithCreateTab = false;
   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      entity.func_70110_aj();
   }

   public boolean func_149662_c() {
      return false;
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public int func_149645_b() {
      return 1;
   }

   public boolean func_149686_d() {
      return false;
   }

   public int func_149745_a(Random rand) {
      return 0;
   }

   public Item func_149650_a(int metadata, Random rand, int fortune) {
      return null;
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return null;
   }

   protected boolean func_149700_E() {
      return false;
   }
}
