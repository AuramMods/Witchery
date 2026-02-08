package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockDisease extends BlockFluidClassic {
   @SideOnly(Side.CLIENT)
   protected IIcon[] icons;

   public BlockDisease(Fluid fluid) {
      super(fluid, Material.field_151597_y);
      this.quantaPerBlock = 1;
      this.func_149711_c(100.0F);
      this.func_149713_g(1);
   }

   public int getMaxRenderHeightMeta() {
      return 16;
   }

   public void func_149674_a(World world, int x, int y, int z, Random rand) {
      super.func_149674_a(world, x, y, z, rand);
      int chance = Config.instance().diseaseRemovalChance;
      if (chance > 0) {
         if (world.field_73012_v.nextInt(chance) == 0) {
            world.func_147468_f(x, y, z);
         }

         world.func_147464_a(x, y, z, this, this.tickRate);
      }

   }

   public Block func_149663_c(String blockName) {
      BlockUtil.registerBlock(this, blockName);
      return super.func_149663_c(blockName);
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int side, int meta) {
      return side != 0 && side != 1 ? this.icons[1] : this.icons[0];
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      this.icons = new IIcon[]{iconRegister.func_94245_a(this.func_149641_N() + "_still"), iconRegister.func_94245_a(this.func_149641_N() + "_flow")};
      if (this.stack != null && this.stack.getFluid() != null) {
         this.stack.getFluid().setIcons(this.icons[0], this.icons[1]);
      }

   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      if (!world.field_72995_K && entity != null && entity instanceof EntityLivingBase) {
         EntityLivingBase livingEntity = (EntityLivingBase)entity;
         if (!CreatureUtil.isImmuneToDisease(livingEntity) && !livingEntity.func_70644_a(Witchery.Potions.DISEASED) && world.field_73012_v.nextInt(3) == 0) {
            livingEntity.func_70690_d(new PotionEffect(Witchery.Potions.DISEASED.field_76415_H, TimeUtil.minsToTicks(1 + world.field_73012_v.nextInt(4))));
         }
      }

   }

   public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
      return super.canDisplace(world, x, y, z);
   }

   public boolean displaceIfPossible(World world, int x, int y, int z) {
      return super.displaceIfPossible(world, x, y, z);
   }
}
