package com.emoniph.witchery.item;

import com.emoniph.witchery.entity.EntityParasyticLouse;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class ItemParasyticLouse extends ItemBase {
   public ItemParasyticLouse() {
      this.func_77625_d(1);
      this.func_77627_a(true);
   }

   public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
      if (par3World.field_72995_K) {
         return true;
      } else {
         Block i1 = par3World.func_147439_a(par4, par5, par6);
         par4 += Facing.field_71586_b[par7];
         par5 += Facing.field_71587_c[par7];
         par6 += Facing.field_71585_d[par7];
         double d0 = 0.0D;
         if (par7 == 1 && i1.func_149645_b() == 11) {
            d0 = 0.5D;
         }

         Entity entity = this.spawnCreature(par1ItemStack, par3World, (double)par4 + 0.5D, (double)par5 + d0, (double)par6 + 0.5D);
         if (entity != null) {
            if (entity instanceof EntityLivingBase && par1ItemStack.func_82837_s()) {
               ((EntityLiving)entity).func_94058_c(par1ItemStack.func_82833_r());
            }

            if (!par2EntityPlayer.field_71075_bZ.field_75098_d) {
               --par1ItemStack.field_77994_a;
            }
         }

         return true;
      }
   }

   public String func_77653_i(ItemStack stack) {
      return super.func_77653_i(stack);
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean advancedTooltips) {
      List effects = Items.field_151068_bn.func_77834_f(stack.func_77960_j());
      if (effects != null && !effects.isEmpty()) {
         PotionEffect effect = (PotionEffect)effects.get(0);
         String s1 = effect.func_76453_d();
         s1 = s1 + ".postfix";
         String s2 = "§6" + StatCollector.func_74838_a(s1).trim() + "§r";
         if (effect.func_76458_c() > 0) {
            s2 = s2 + " " + StatCollector.func_74838_a("potion.potency." + effect.func_76458_c()).trim();
         }

         if (effect.func_76459_b() > 20) {
            s2 = s2 + " [" + Potion.func_76389_a(effect) + "]";
         }

         list.add(s2);
      }

   }

   public ItemStack func_77659_a(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
      if (par2World.field_72995_K) {
         return par1ItemStack;
      } else {
         MovingObjectPosition movingobjectposition = this.func_77621_a(par2World, par3EntityPlayer, true);
         if (movingobjectposition == null) {
            return par1ItemStack;
         } else {
            if (movingobjectposition.field_72313_a == MovingObjectType.BLOCK) {
               int i = movingobjectposition.field_72311_b;
               int j = movingobjectposition.field_72312_c;
               int k = movingobjectposition.field_72309_d;
               if (!par2World.func_72962_a(par3EntityPlayer, i, j, k)) {
                  return par1ItemStack;
               }

               if (!par3EntityPlayer.func_82247_a(i, j, k, movingobjectposition.field_72310_e, par1ItemStack)) {
                  return par1ItemStack;
               }

               if (par2World.func_147439_a(i, j, k).func_149688_o() == Material.field_151586_h) {
                  Entity entity = this.spawnCreature(par1ItemStack, par2World, (double)i, (double)j, (double)k);
                  if (entity != null) {
                     if (entity instanceof EntityLivingBase && par1ItemStack.func_82837_s()) {
                        ((EntityLiving)entity).func_94058_c(par1ItemStack.func_82833_r());
                     }

                     if (!par3EntityPlayer.field_71075_bZ.field_75098_d) {
                        --par1ItemStack.field_77994_a;
                     }
                  }
               }
            }

            return par1ItemStack;
         }
      }
   }

   private Entity spawnCreature(ItemStack stack, World par0World, double par2, double par4, double par6) {
      EntityParasyticLouse entity = new EntityParasyticLouse(par0World);
      int damage = stack.func_77960_j();
      if (damage > 0) {
         entity.setBitePotionEffect(damage);
      }

      if (entity != null && entity instanceof EntityLivingBase) {
         entity.func_70012_b(par2, par4, par6, MathHelper.func_76142_g(par0World.field_73012_v.nextFloat() * 360.0F), 0.0F);
         entity.func_110163_bv();
         entity.field_70759_as = entity.field_70177_z;
         entity.field_70761_aq = entity.field_70177_z;
         entity.func_110161_a((IEntityLivingData)null);
         par0World.func_72838_d(entity);
         entity.func_70642_aH();
      }

      return entity;
   }
}
