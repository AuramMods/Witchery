package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.ai.EntityAIDimensionalFollowOwner;
import com.emoniph.witchery.entity.ai.EntityAISitAndStay;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.familiar.IFamiliar;
import com.emoniph.witchery.util.TameableUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityWitchCat extends EntityOcelot implements IFamiliar {
   public EntityWitchCat(World par1World) {
      super(par1World);
      this.func_70661_as().func_75491_a(true);
      this.func_70661_as().func_75495_e(true);
      this.field_70714_bg.func_85156_a(((EntityAITaskEntry)this.field_70714_bg.field_75782_a.get(4)).field_75733_a);
      this.field_70714_bg.func_85156_a(((EntityAITaskEntry)this.field_70714_bg.field_75782_a.get(1)).field_75733_a);
      this.field_70714_bg.func_75776_a(1, new EntityAISitAndStay(this));
      this.field_70714_bg.func_75776_a(5, new EntityAIDimensionalFollowOwner(this, 1.0D, 10.0F, 5.0F));
   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      super.func_70014_b(par1NBTTagCompound);
      par1NBTTagCompound.func_74774_a("Familiar", (byte)(this.isFamiliar() ? 1 : 0));
   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      super.func_70037_a(par1NBTTagCompound);
      if (par1NBTTagCompound.func_74764_b("Familiar")) {
         this.setFamiliar(par1NBTTagCompound.func_74771_c("Familiar") > 0);
      }

   }

   public void func_70071_h_() {
      this.field_70178_ae = this.isFamiliar();
      super.func_70071_h_();
   }

   public int func_70658_aO() {
      return super.func_70658_aO() + (this.isFamiliar() ? 5 : 0);
   }

   public EntityLivingBase func_70902_q() {
      return this.isFamiliar() && !this.field_70170_p.field_72995_K ? TameableUtil.getOwnerAccrossDimensions(this) : super.func_70902_q();
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(26, (byte)0);
   }

   public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
      boolean sitting = this.func_70906_o();
      boolean result = super.func_70097_a(par1DamageSource, par2);
      if (sitting && this.isFamiliar()) {
         this.func_70904_g(true);
      }

      return result;
   }

   public boolean isFamiliar() {
      return this.field_70180_af.func_75683_a(26) > 0;
   }

   public void setFamiliar(boolean familiar) {
      this.field_70180_af.func_75692_b(26, (byte)(familiar ? 1 : 0));
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.cat.name");
   }

   protected int func_70682_h(int par1) {
      return par1;
   }

   public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
      ItemStack itemstack = par1EntityPlayer.field_71071_by.func_70448_g();
      if (this.func_70909_n()) {
         if (TameableUtil.isOwner(this, par1EntityPlayer) && this.isFamiliar() && par1EntityPlayer.func_70093_af() && this.func_70906_o()) {
            if (!this.field_70170_p.field_72995_K) {
               Familiar.dismissFamiliar(par1EntityPlayer, this);
            }

            return true;
         }

         if (TameableUtil.isOwner(this, par1EntityPlayer)) {
            if (itemstack == null || !this.func_70877_b(itemstack) && itemstack.func_77973_b() != Items.field_151057_cb && itemstack.func_77973_b() != Witchery.Items.POLYNESIA_CHARM && itemstack.func_77973_b() != Witchery.Items.DEVILS_TONGUE_CHARM) {
               if (!this.field_70170_p.field_72995_K) {
                  this.func_70904_g(!this.func_70906_o());
               }

               return true;
            }

            if (itemstack != null && this.func_70877_b(itemstack) && this.func_110143_aJ() < this.func_110138_aP()) {
               if (!this.field_70170_p.field_72995_K) {
                  if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                     --itemstack.field_77994_a;
                  }

                  this.func_70691_i(10.0F);
                  if (itemstack.field_77994_a <= 0) {
                     par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                  }
               }

               return true;
            }
         }
      } else if (itemstack != null && itemstack.func_77973_b() == Items.field_151115_aP && par1EntityPlayer.func_70068_e(this) < 9.0D) {
         if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
            --itemstack.field_77994_a;
         }

         if (itemstack.field_77994_a <= 0) {
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
         }

         if (!this.field_70170_p.field_72995_K) {
            if (this.field_70146_Z.nextInt(3) == 0) {
               this.func_70903_f(true);
               this.func_70912_b(1 + this.field_70170_p.field_73012_v.nextInt(3));
               TameableUtil.setOwner(this, par1EntityPlayer);
               this.func_70908_e(true);
               this.func_110163_bv();
               this.func_70904_g(true);
               this.field_70170_p.func_72960_a(this, (byte)7);
            } else {
               this.func_70908_e(false);
               this.field_70170_p.func_72960_a(this, (byte)6);
            }
         }

         return true;
      }

      return super.func_70085_c(par1EntityPlayer);
   }

   public void setMaxHealth(float maxHealth) {
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)maxHealth);
      this.func_70606_j(maxHealth);
      this.setFamiliar(true);
   }

   public void cloneOcelot(EntityOcelot oldCat) {
      if (oldCat.func_94056_bM()) {
         this.func_94058_c(oldCat.func_94057_bL());
      }

      this.func_70012_b(oldCat.field_70165_t, oldCat.field_70163_u, oldCat.field_70161_v, oldCat.field_70177_z, oldCat.field_70125_A);
      TameableUtil.cloneOwner(this, oldCat);
      this.func_70903_f(true);
      this.func_70904_g(oldCat.func_70906_o());
      double oldMaxHealth = oldCat.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111126_e();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(oldMaxHealth);
      this.func_70606_j(oldCat.func_110143_aJ());
   }

   public void clearFamiliar() {
      this.setFamiliar(false);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(10.0D);
      this.func_70606_j(10.0F);
   }
}
