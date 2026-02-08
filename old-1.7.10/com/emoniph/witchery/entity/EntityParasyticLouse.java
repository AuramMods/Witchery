package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityParasyticLouse extends EntityMob {
   public EntityParasyticLouse(World par1World) {
      super(par1World);
      this.func_70105_a(0.3F, 0.7F);
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(4.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.6000000238418579D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(0.0D);
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(20, new Integer(0));
   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      super.func_70014_b(par1NBTTagCompound);
      par1NBTTagCompound.func_74768_a("BitePotionEffect", this.getBitePotionEffect());
   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      super.func_70037_a(par1NBTTagCompound);
      if (par1NBTTagCompound.func_74764_b("BitePotionEffect")) {
         this.setBitePotionEffect(par1NBTTagCompound.func_74762_e("BitePotionEffect"));
      }

   }

   public int getBitePotionEffect() {
      return this.field_70180_af.func_75679_c(20);
   }

   public void setBitePotionEffect(int par1) {
      this.field_70180_af.func_75692_b(20, par1);
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.louse.name");
   }

   protected boolean func_70041_e_() {
      return false;
   }

   protected Entity func_70782_k() {
      double d0 = 8.0D;
      return this.field_70170_p.func_72856_b(this, d0);
   }

   protected String func_70639_aQ() {
      return "mob.silverfish.say";
   }

   protected String func_70621_aR() {
      return "mob.silverfish.hit";
   }

   protected String func_70673_aS() {
      return "mob.silverfish.kill";
   }

   public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
      return this.func_85032_ar() ? false : super.func_70097_a(par1DamageSource, par2);
   }

   protected void func_70785_a(Entity par1Entity, float par2) {
      if (this.field_70724_aR <= 0 && par2 < 1.2F && par1Entity.field_70121_D.field_72337_e > this.field_70121_D.field_72338_b && par1Entity.field_70121_D.field_72338_b < this.field_70121_D.field_72337_e) {
         this.field_70724_aR = 20;
         this.func_70652_k(par1Entity);
         if (par1Entity instanceof EntityLivingBase && !this.field_70170_p.field_72995_K) {
            EntityLivingBase living = (EntityLivingBase)par1Entity;
            int potionEffect = this.getBitePotionEffect();
            if (potionEffect > 0) {
               List list = Items.field_151068_bn.func_77834_f(potionEffect);
               if (list != null && !list.isEmpty()) {
                  PotionEffect effect = new PotionEffect((PotionEffect)list.get(0));
                  living.func_70690_d(effect);
               }

               this.setBitePotionEffect(0);
            }
         }
      }

   }

   protected void func_145780_a(int par1, int par2, int par3, Block par4) {
      this.func_85030_a("mob.silverfish.step", 0.15F, 1.0F);
   }

   protected Item func_146068_u() {
      return null;
   }

   public void func_70071_h_() {
      this.field_70761_aq = this.field_70177_z;
      super.func_70071_h_();
   }

   protected void func_70626_be() {
      super.func_70626_be();
      if (!this.field_70170_p.field_72995_K && this.field_70789_a != null && !this.func_70781_l()) {
         this.field_70789_a = null;
      }

   }

   public float func_70783_a(int par1, int par2, int par3) {
      return this.field_70170_p.func_147439_a(par1, par2 - 1, par3) == Blocks.field_150348_b ? 10.0F : super.func_70783_a(par1, par2, par3);
   }

   protected boolean func_70814_o() {
      return true;
   }

   public boolean func_70601_bi() {
      if (super.func_70601_bi()) {
         EntityPlayer entityplayer = this.field_70170_p.func_72890_a(this, 5.0D);
         return entityplayer == null;
      } else {
         return false;
      }
   }

   public EnumCreatureAttribute func_70668_bt() {
      return EnumCreatureAttribute.ARTHROPOD;
   }

   protected boolean func_70085_c(EntityPlayer player) {
      this.func_70106_y();
      if (!this.field_70170_p.field_72995_K) {
         ItemStack stack = new ItemStack(Witchery.Items.PARASYTIC_LOUSE);
         EntityItem item = new EntityItem(this.field_70170_p, this.field_70165_t, 0.4D + this.field_70163_u, this.field_70161_v, stack);
         this.field_70170_p.func_72838_d(item);
         return true;
      } else {
         return super.func_70085_c(player);
      }
   }
}
