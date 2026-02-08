package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.ai.EntityAIDimensionalFollowOwner;
import com.emoniph.witchery.entity.ai.EntityAISitAndStay;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.familiar.IFamiliar;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TameableUtil;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityToad extends EntityTameable implements IFamiliar {
   private int timeToLive = -1;
   private boolean poisoned = false;

   public EntityToad(World par1World) {
      super(par1World);
      this.func_70105_a(0.8F, 0.8F);
      this.func_70661_as().func_75495_e(true);
      this.func_70661_as().func_75491_a(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, new EntityAISitAndStay(this));
      this.field_70714_bg.func_75776_a(3, new EntityAIDimensionalFollowOwner(this, 1.0D, 10.0F, 2.0F));
      this.field_70714_bg.func_75776_a(4, new EntityAITempt(this, 1.25D, Items.field_151078_bh, false));
      this.field_70714_bg.func_75776_a(6, new EntityAIMate(this, 1.0D));
      this.field_70714_bg.func_75776_a(7, new EntityAIWander(this, 1.0D));
      this.field_70714_bg.func_75776_a(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(9, new EntityAILookIdle(this));
      this.func_70903_f(false);
   }

   public void setTimeToLive(int i, boolean poisoned) {
      this.timeToLive = i;
      this.poisoned = poisoned;
   }

   public boolean isTemp() {
      return this.timeToLive != -1;
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.20000001192092895D);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(10.0D);
   }

   public int func_70658_aO() {
      return super.func_70658_aO() + (this.isFamiliar() ? 5 : 0);
   }

   public int func_70627_aG() {
      return super.func_70627_aG() * 2;
   }

   public void setMaxHealth(float maxHealth) {
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)maxHealth);
      this.func_70606_j(maxHealth);
      this.setFamiliar(true);
   }

   public EntityLivingBase func_70902_q() {
      return this.isFamiliar() && !this.field_70170_p.field_72995_K ? TameableUtil.getOwnerAccrossDimensions(this) : super.func_70902_q();
   }

   protected int func_70682_h(int par1) {
      return par1;
   }

   protected void func_70069_a(float par1) {
   }

   public boolean func_70650_aV() {
      return true;
   }

   protected void func_70629_bd() {
      super.func_70629_bd();
      this.field_70180_af.func_75692_b(18, this.func_110143_aJ());
      if (this.field_70170_p != null && !this.field_70128_L && !this.field_70170_p.field_72995_K && this.timeToLive != -1 && --this.timeToLive == 0) {
         this.func_70106_y();
         if (this.poisoned) {
            AxisAlignedBB axisalignedbb = this.field_70121_D.func_72314_b(3.0D, 2.0D, 3.0D);
            List list1 = this.field_70170_p.func_72872_a(EntityLivingBase.class, axisalignedbb);
            if (list1 != null && !list1.isEmpty()) {
               Iterator iterator = list1.iterator();

               while(iterator.hasNext()) {
                  EntityLivingBase entitylivingbase = (EntityLivingBase)iterator.next();
                  double d0 = this.func_70068_e(entitylivingbase);
                  if (d0 < 9.0D) {
                     double d1 = 1.0D - Math.sqrt(d0) / 3.0D;
                     entitylivingbase.func_70690_d(new PotionEffect(Potion.field_76436_u.field_76415_H, 60, 0));
                  }
               }
            }

            ParticleEffect.SLIME.send(SoundEffect.MOB_SLIME_BIG, this, 1.0D, 1.0D, 16);
         }

         ParticleEffect.MOB_SPELL.send(SoundEffect.NONE, this, 0.5D, 0.5D, 16);
      }

   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(18, new Float(this.func_110143_aJ()));
      this.field_70180_af.func_75682_a(19, new Byte((byte)0));
      this.field_70180_af.func_75682_a(20, new Byte((byte)BlockColored.func_150032_b(this.field_70170_p != null ? this.field_70170_p.field_73012_v.nextInt(16) : (new Random()).nextInt(16))));
      this.field_70180_af.func_75682_a(26, (byte)0);
   }

   public boolean isFamiliar() {
      return this.field_70180_af.func_75683_a(26) > 0;
   }

   public void setFamiliar(boolean familiar) {
      this.field_70180_af.func_75692_b(26, (byte)(familiar ? 1 : 0));
   }

   protected void func_145780_a(int par1, int par2, int par3, Block par4) {
      this.func_85030_a("mob.slime.small", 0.15F, 1.0F);
   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      super.func_70014_b(par1NBTTagCompound);
      par1NBTTagCompound.func_74774_a("SkinColor", (byte)this.getSkinColor());
      par1NBTTagCompound.func_74774_a("Familiar", (byte)(this.isFamiliar() ? 1 : 0));
      par1NBTTagCompound.func_74768_a("SuicideIn", this.timeToLive);
      par1NBTTagCompound.func_74757_a("Poisonous", this.poisoned);
   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      super.func_70037_a(par1NBTTagCompound);
      if (par1NBTTagCompound.func_74764_b("SkinColor")) {
         this.setSkinColor(par1NBTTagCompound.func_74771_c("SkinColor"));
      }

      if (par1NBTTagCompound.func_74764_b("Familiar")) {
         this.setFamiliar(par1NBTTagCompound.func_74771_c("Familiar") > 0);
      }

      if (par1NBTTagCompound.func_74764_b("SuicideIn")) {
         this.timeToLive = par1NBTTagCompound.func_74762_e("SuicideIn");
      } else {
         this.timeToLive = -1;
      }

      if (par1NBTTagCompound.func_74764_b("Poisonous")) {
         this.poisoned = par1NBTTagCompound.func_74767_n("Poisonous");
      } else {
         this.poisoned = false;
      }

   }

   protected String func_70639_aQ() {
      return "witchery:mob.toad.toad_croak";
   }

   protected String func_70621_aR() {
      return "witchery:mob.toad.toad_hurt";
   }

   protected String func_70673_aS() {
      return "witchery:mob.toad.toad_hurt";
   }

   protected float func_70599_aP() {
      return 0.4F;
   }

   protected Item func_146068_u() {
      return !this.isTemp() ? Items.field_151123_aH : super.func_146068_u();
   }

   public void func_70636_d() {
      super.func_70636_d();
   }

   public void func_70071_h_() {
      this.field_70178_ae = this.isFamiliar();
      super.func_70071_h_();
      if (!this.func_70906_o() && !this.field_70170_p.field_72995_K && (this.field_70159_w != 0.0D || this.field_70179_y != 0.0D) && !this.func_70090_H()) {
         this.func_70683_ar().func_75660_a();
      }

   }

   public float func_70047_e() {
      return this.field_70131_O * 0.8F;
   }

   public int func_70646_bf() {
      return this.func_70906_o() ? 20 : super.func_70646_bf();
   }

   public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
      if (this.func_85032_ar()) {
         return false;
      } else {
         Entity entity = par1DamageSource.func_76346_g();
         if (!this.isFamiliar()) {
            this.func_70904_g(false);
         }

         if (entity != null && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
            par2 = (par2 + 1.0F) / 2.0F;
         }

         return super.func_70097_a(par1DamageSource, par2);
      }
   }

   public void func_70903_f(boolean par1) {
      super.func_70903_f(par1);
   }

   public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
      if (this.isTemp()) {
         return true;
      } else {
         ItemStack itemstack = par1EntityPlayer.field_71071_by.func_70448_g();
         if (this.func_70909_n()) {
            if (TameableUtil.isOwner(this, par1EntityPlayer) && this.isFamiliar() && par1EntityPlayer.func_70093_af() && this.func_70906_o()) {
               if (!this.field_70170_p.field_72995_K) {
                  Familiar.dismissFamiliar(par1EntityPlayer, this);
               }

               return true;
            }

            if (itemstack != null) {
               if (itemstack.func_77973_b() == Items.field_151078_bh && this.func_110143_aJ() < this.func_110138_aP()) {
                  if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                     --itemstack.field_77994_a;
                  }

                  this.func_70691_i(10.0F);
                  if (itemstack.field_77994_a <= 0) {
                     par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                  }

                  return true;
               }

               if (itemstack.func_77973_b() == Items.field_151100_aR) {
                  int i = BlockColored.func_150032_b(itemstack.func_77960_j());
                  if (i != this.getSkinColor()) {
                     this.setSkinColor(i);
                     if (!par1EntityPlayer.field_71075_bZ.field_75098_d && --itemstack.field_77994_a <= 0) {
                        par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                     }

                     return true;
                  }
               } else if (itemstack.func_77973_b() == Items.field_151057_cb || itemstack.func_77973_b() == Witchery.Items.POLYNESIA_CHARM || itemstack.func_77973_b() == Witchery.Items.DEVILS_TONGUE_CHARM) {
                  return false;
               }
            }

            if (TameableUtil.isOwner(this, par1EntityPlayer) && !this.func_70877_b(itemstack)) {
               if (!this.field_70170_p.field_72995_K) {
                  this.func_70904_g(!this.func_70906_o());
                  this.func_70683_ar().func_75661_b();
                  this.field_70703_bu = false;
                  this.func_70778_a((PathEntity)null);
                  this.func_70784_b((Entity)null);
                  this.func_70624_b((EntityLivingBase)null);
               }

               return true;
            }
         } else if (itemstack != null && itemstack.func_77973_b() == Items.field_151078_bh) {
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
               --itemstack.field_77994_a;
            }

            if (itemstack.field_77994_a <= 0) {
               par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            }

            if (!this.field_70170_p.field_72995_K) {
               if (this.field_70146_Z.nextInt(3) == 0) {
                  this.func_70903_f(true);
                  this.func_110163_bv();
                  this.func_70778_a((PathEntity)null);
                  this.func_70624_b((EntityLivingBase)null);
                  this.func_70904_g(true);
                  TameableUtil.setOwner(this, par1EntityPlayer);
                  this.func_70908_e(true);
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
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.toad.name");
   }

   public boolean func_70877_b(ItemStack par1ItemStack) {
      return par1ItemStack != null && par1ItemStack.func_77973_b() == Items.field_151078_bh;
   }

   public int getSkinColor() {
      return this.field_70180_af.func_75683_a(20) & 15;
   }

   public void setSkinColor(int par1) {
      this.field_70180_af.func_75692_b(20, (byte)(par1 & 15));
   }

   public EntityToad spawnBabyAnimal(EntityAgeable par1EntityAgeable) {
      EntityToad entity = new EntityToad(this.field_70170_p);
      if (TameableUtil.hasOwner(this)) {
         entity.func_110163_bv();
         entity.setSkinColor(this.getSkinColor());
      }

      return entity;
   }

   public boolean func_70878_b(EntityAnimal par1EntityAnimal) {
      if (par1EntityAnimal == this) {
         return false;
      } else if (!this.func_70909_n()) {
         return false;
      } else if (!(par1EntityAnimal instanceof EntityToad)) {
         return false;
      } else {
         EntityToad entity = (EntityToad)par1EntityAnimal;
         return !entity.func_70909_n() ? false : (entity.func_70906_o() ? false : this.func_70880_s() && entity.func_70880_s());
      }
   }

   public boolean func_70922_bv() {
      return this.field_70180_af.func_75683_a(19) == 1;
   }

   protected boolean func_70692_ba() {
      return false;
   }

   public boolean func_142018_a(EntityLivingBase par1EntityLivingBase, EntityLivingBase par2EntityLivingBase) {
      if (!(par1EntityLivingBase instanceof EntityCreeper) && !(par1EntityLivingBase instanceof EntityGhast)) {
         if (par1EntityLivingBase instanceof EntityToad) {
            EntityToad entity = (EntityToad)par1EntityLivingBase;
            if (entity.func_70909_n() && entity.func_70902_q() == par2EntityLivingBase) {
               return false;
            }
         }

         return par1EntityLivingBase instanceof EntityPlayer && par2EntityLivingBase instanceof EntityPlayer && !((EntityPlayer)par2EntityLivingBase).func_96122_a((EntityPlayer)par1EntityLivingBase) ? false : !(par1EntityLivingBase instanceof EntityHorse) || !((EntityHorse)par1EntityLivingBase).func_110248_bS();
      } else {
         return false;
      }
   }

   public EntityAgeable func_90011_a(EntityAgeable par1EntityAgeable) {
      return this.spawnBabyAnimal(par1EntityAgeable);
   }

   public void clearFamiliar() {
      this.setFamiliar(false);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(10.0D);
      this.func_70606_j(10.0F);
   }
}
