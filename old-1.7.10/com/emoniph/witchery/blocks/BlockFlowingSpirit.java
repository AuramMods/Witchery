package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.EntityNightmare;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.CreatureUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockFlowingSpirit extends BlockFluidClassic {
   protected final boolean nightmareBane;
   protected final boolean igniteSpiritPortals;
   protected final PotionEffect goodyEffect;
   protected final PotionEffect baddyEffect;
   @SideOnly(Side.CLIENT)
   protected IIcon[] icons;

   public BlockFlowingSpirit(Fluid fluid, PotionEffect goodyEffect, PotionEffect baddyEffect, boolean nightmareBane, boolean igniteSpiritPortals) {
      super(fluid, Material.field_151586_h);
      this.quantaPerBlock = 5;
      this.func_149711_c(100.0F);
      this.func_149713_g(3);
      this.goodyEffect = goodyEffect;
      this.baddyEffect = baddyEffect;
      this.nightmareBane = nightmareBane;
      this.igniteSpiritPortals = igniteSpiritPortals;
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

   public void func_149726_b(World world, int x, int y, int z) {
      if (!this.igniteSpiritPortals || world.field_73011_w.field_76574_g != Config.instance().dimensionDreamID || world.func_147439_a(x, y - 1, z) != Blocks.field_150433_aE || world.func_72805_g(x, y, z) != 0 || !Witchery.Blocks.SPIRIT_PORTAL.tryToCreatePortal(world, x, y, z)) {
         super.func_149726_b(world, x, y, z);
      }

   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      if (!world.field_72995_K && entity != null) {
         if (entity instanceof EntityLivingBase) {
            EntityLivingBase livingEntity = (EntityLivingBase)entity;
            if (CreatureUtil.isUndead(livingEntity) || CreatureUtil.isDemonic(livingEntity) || this.nightmareBane && livingEntity instanceof EntityNightmare) {
               if (!livingEntity.func_82165_m(this.baddyEffect.func_76456_a())) {
                  livingEntity.func_70690_d(new PotionEffect(this.baddyEffect));
               }
            } else if (!livingEntity.func_82165_m(this.goodyEffect.func_76456_a())) {
               livingEntity.func_70690_d(new PotionEffect(this.goodyEffect));
            }
         } else if (this.nightmareBane && entity instanceof EntityItem) {
            EntityItem item = (EntityItem)entity;
            ItemStack stack = item.func_92059_d();
            if (Witchery.Items.GENERIC.itemDisturbedCotton.isMatch(stack)) {
               ItemStack newStack = new ItemStack(Witchery.Blocks.WISPY_COTTON, stack.field_77994_a);
               item.func_92058_a(newStack);
            }
         }
      }

   }

   public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
      return world.func_147439_a(x, y, z).func_149688_o().func_76224_d() ? false : super.canDisplace(world, x, y, z);
   }

   public boolean displaceIfPossible(World world, int x, int y, int z) {
      return world.func_147439_a(x, y, z).func_149688_o().func_76224_d() ? false : super.displaceIfPossible(world, x, y, z);
   }
}
