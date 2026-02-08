package com.emoniph.witchery.blocks;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.item.ItemLeapingLily;
import com.emoniph.witchery.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class BlockLeapingLily extends BlockLilyPad {
   public BlockLeapingLily() {
      this.func_149711_c(0.0F);
      this.func_149715_a(0.4F);
      this.func_149672_a(Block.field_149779_h);
      this.func_149647_a(WitcheryCreativeTab.INSTANCE);
   }

   public Block func_149663_c(String blockName) {
      BlockUtil.registerBlock(this, ItemLeapingLily.class, blockName);
      return super.func_149663_c(blockName);
   }

   protected boolean func_149854_a(Block block) {
      return block != null && block.func_149688_o() != null && (block.func_149688_o().func_76220_a() || block.func_149688_o() == Material.field_151586_h);
   }

   public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
      Material material = par1World.func_147439_a(par2, par3, par4).func_149688_o();
      return super.func_149742_c(par1World, par2, par3, par4) && material != null && !material.func_76224_d();
   }

   public boolean func_149718_j(World world, int posX, int posY, int posZ) {
      Material material = world.func_147439_a(posX, posY - 1, posZ).func_149688_o();
      return material != null && (material.func_76220_a() || material.func_76224_d()) && world.func_147437_c(posX, posY + 1, posZ);
   }

   public void func_149670_a(World world, int posX, int posY, int posZ, Entity entity) {
      if (!world.field_72995_K && entity instanceof EntityLivingBase) {
         EntityLivingBase livingEntity = (EntityLivingBase)entity;
         if (!livingEntity.func_70644_a(Potion.field_76424_c)) {
            livingEntity.func_70690_d(new PotionEffect(Potion.field_76424_c.field_76415_H, 10, 0));
         }

         if (!livingEntity.func_70644_a(Potion.field_76430_j)) {
            livingEntity.func_70690_d(new PotionEffect(Potion.field_76430_j.field_76415_H, 10, 4));
         }
      }

   }
}
