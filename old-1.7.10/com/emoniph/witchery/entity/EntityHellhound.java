package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.ParticleEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityHellhound extends EntityMob implements IEntitySelector {
   private float field_70926_e;
   private float field_70924_f;
   private boolean isShaking;
   private boolean field_70928_h;
   private float timeWolfIsShaking;
   private float prevTimeWolfIsShaking;
   private int conversionTime;

   public EntityHellhound(World world) {
      super(world);
      this.field_70178_ae = true;
      this.func_70105_a(0.9F, 0.9F);
      this.func_70661_as().func_75491_a(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, new EntityAILeapAtTarget(this, 0.4F));
      this.field_70714_bg.func_75776_a(3, new EntityAIAttackOnCollide(this, 1.0D, true));
      this.field_70714_bg.func_75776_a(4, new EntityAIWander(this, 1.0D));
      this.field_70714_bg.func_75776_a(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(5, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAIHurtByTarget(this, true));
      this.field_70715_bh.func_75776_a(2, new EntityAINearestAttackableTarget(this, EntitySheep.class, 0, true, true));
      this.field_70715_bh.func_75776_a(3, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true, true, this));
      this.field_70715_bh.func_75776_a(4, new EntityAINearestAttackableTarget(this, EntityFollower.class, 0, true, true, this));
   }

   public boolean func_82704_a(Entity entity) {
      double AGGRO_RANGE = 5.0D;
      return entity != null && entity.func_70068_e(this) < 25.0D;
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.30000001192092896D);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(20.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(4.0D);
   }

   public int func_70658_aO() {
      int i = super.func_70658_aO() + 2;
      if (i > 20) {
         i = 20;
      }

      return i;
   }

   public boolean func_70650_aV() {
      return true;
   }

   protected void func_70629_bd() {
      this.field_70180_af.func_75692_b(18, this.func_110143_aJ());
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(18, new Float(this.func_110143_aJ()));
      this.field_70180_af.func_75682_a(19, new Byte((byte)0));
      this.func_70096_w().func_75682_a(14, (byte)0);
   }

   protected void convertToWolf() {
      EntityWolf entityvillager = new EntityWolf(this.field_70170_p);
      entityvillager.func_82149_j(this);
      entityvillager.func_110161_a((IEntityLivingData)null);
      this.field_70170_p.func_72900_e(this);
      this.field_70170_p.func_72838_d(entityvillager);
      entityvillager.func_70690_d(new PotionEffect(Potion.field_76431_k.field_76415_H, 200, 0));
      this.field_70170_p.func_72889_a((EntityPlayer)null, 1017, (int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v, 0);
   }

   public boolean func_70085_c(EntityPlayer p_70085_1_) {
      ItemStack itemstack = p_70085_1_.func_71045_bC();
      if (itemstack != null && itemstack.func_77973_b() == Items.field_151153_ao && itemstack.func_77960_j() == 0 && this.func_70644_a(Potion.field_76437_t)) {
         if (!p_70085_1_.field_71075_bZ.field_75098_d) {
            --itemstack.field_77994_a;
         }

         if (itemstack.field_77994_a <= 0) {
            p_70085_1_.field_71071_by.func_70299_a(p_70085_1_.field_71071_by.field_70461_c, (ItemStack)null);
         }

         if (!this.field_70170_p.field_72995_K) {
            this.startConversion(this.field_70146_Z.nextInt(1000) + 3600);
         }

         return true;
      } else {
         return false;
      }
   }

   protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_) {
      this.func_85030_a("mob.wolf.step", 0.15F, 1.0F);
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74768_a("ConversionTime", this.isConverting() ? this.conversionTime : -1);
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      if (nbtRoot.func_150297_b("ConversionTime", 99) && nbtRoot.func_74762_e("ConversionTime") > -1) {
         this.startConversion(nbtRoot.func_74762_e("ConversionTime"));
      }

   }

   protected void startConversion(int p_82228_1_) {
      this.conversionTime = p_82228_1_;
      this.func_70096_w().func_75692_b(14, (byte)1);
      this.func_82170_o(Potion.field_76437_t.field_76415_H);
      this.func_70690_d(new PotionEffect(Potion.field_76420_g.field_76415_H, p_82228_1_, Math.min(this.field_70170_p.field_73013_u.func_151525_a() - 1, 0)));
      this.field_70170_p.func_72960_a(this, (byte)16);
   }

   protected int getConversionTimeBoost() {
      int i = 1;
      if (this.field_70146_Z.nextFloat() < 0.01F) {
         int j = 0;

         for(int k = (int)this.field_70165_t - 4; k < (int)this.field_70165_t + 4 && j < 14; ++k) {
            for(int l = (int)this.field_70163_u - 4; l < (int)this.field_70163_u + 4 && j < 14; ++l) {
               for(int i1 = (int)this.field_70161_v - 4; i1 < (int)this.field_70161_v + 4 && j < 14; ++i1) {
                  Block block = this.field_70170_p.func_147439_a(k, l, i1);
                  if (block == Blocks.field_150411_aY || block == Blocks.field_150324_C) {
                     if (this.field_70146_Z.nextFloat() < 0.3F) {
                        ++i;
                     }

                     ++j;
                  }
               }
            }
         }
      }

      return i;
   }

   protected String func_70639_aQ() {
      return "mob.wolf.growl";
   }

   protected String func_70621_aR() {
      return "mob.wolf.hurt";
   }

   protected String func_70673_aS() {
      return "mob.wolf.death";
   }

   protected float func_70599_aP() {
      return 0.4F;
   }

   protected Item func_146068_u() {
      return Item.func_150899_d(-1);
   }

   protected void func_70628_a(boolean recentlyHitByPlayer, int looting) {
      this.func_70099_a(Witchery.Items.GENERIC.itemDogTongue.createStack(), 0.0F);
      if (this.field_70170_p.field_73012_v.nextInt(12) <= Math.min(looting, 3)) {
         this.func_70099_a(new ItemStack(Witchery.Blocks.WOLFHEAD, 1, 1), 0.0F);
      }

   }

   public void func_70636_d() {
      super.func_70636_d();
      if (!this.field_70170_p.field_72995_K && !this.field_70128_L && this.isShaking && !this.field_70928_h && !this.func_70781_l() && this.field_70122_E) {
         this.field_70928_h = true;
         this.timeWolfIsShaking = 0.0F;
         this.prevTimeWolfIsShaking = 0.0F;
         this.field_70170_p.func_72960_a(this, (byte)8);
      }

   }

   public void func_70071_h_() {
      if (!this.field_70170_p.field_72995_K && this.isConverting()) {
         int i = this.getConversionTimeBoost();
         this.conversionTime -= i;
         if (this.conversionTime <= 0) {
            this.convertToWolf();
         }
      }

      super.func_70071_h_();
      if (!this.field_70128_L) {
         this.field_70924_f = this.field_70926_e;
         if (this.func_70922_bv()) {
            this.field_70926_e += (1.0F - this.field_70926_e) * 0.4F;
         } else {
            this.field_70926_e += (0.0F - this.field_70926_e) * 0.4F;
         }

         if (this.func_70922_bv()) {
            this.field_70700_bx = 10;
         }

         if (this.func_70026_G()) {
            this.isShaking = true;
            this.field_70928_h = false;
            this.timeWolfIsShaking = 0.0F;
            this.prevTimeWolfIsShaking = 0.0F;
         } else if ((this.isShaking || this.field_70928_h) && this.field_70928_h) {
            if (this.timeWolfIsShaking == 0.0F) {
               this.func_85030_a("mob.wolf.shake", this.func_70599_aP(), (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.2F + 1.0F);
            }

            this.prevTimeWolfIsShaking = this.timeWolfIsShaking;
            this.timeWolfIsShaking += 0.05F;
            if (this.prevTimeWolfIsShaking >= 2.0F) {
               this.isShaking = false;
               this.field_70928_h = false;
               this.prevTimeWolfIsShaking = 0.0F;
               this.timeWolfIsShaking = 0.0F;
            }

            if (this.timeWolfIsShaking > 0.4F) {
               float f = (float)this.field_70121_D.field_72338_b;
               int i = (int)(MathHelper.func_76126_a((this.timeWolfIsShaking - 0.4F) * 3.1415927F) * 7.0F);

               for(int j = 0; j < i; ++j) {
                  float f1 = (this.field_70146_Z.nextFloat() * 2.0F - 1.0F) * this.field_70130_N * 0.5F;
                  float f2 = (this.field_70146_Z.nextFloat() * 2.0F - 1.0F) * this.field_70130_N * 0.5F;
                  this.field_70170_p.func_72869_a("splash", this.field_70165_t + (double)f1, (double)(f + 0.8F), this.field_70161_v + (double)f2, this.field_70159_w, this.field_70181_x, this.field_70179_y);
               }
            }
         }

         if (this.field_70170_p.field_72995_K && this.field_70173_aa % 2 == 0) {
            this.field_70170_p.func_72869_a(ParticleEffect.FLAME.toString(), this.field_70165_t - (double)this.field_70130_N * 0.35D + this.field_70170_p.field_73012_v.nextDouble() * (double)this.field_70130_N * 0.7D, 0.5D + this.field_70163_u + this.field_70170_p.field_73012_v.nextDouble() * ((double)this.field_70131_O - 0.1D), this.field_70161_v - (double)this.field_70130_N * 0.35D + this.field_70170_p.field_73012_v.nextDouble() * (double)this.field_70130_N * 0.7D, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean getWolfShaking() {
      return this.isShaking;
   }

   @SideOnly(Side.CLIENT)
   public float getShadingWhileShaking(float p_70915_1_) {
      return 0.75F + (this.prevTimeWolfIsShaking + (this.timeWolfIsShaking - this.prevTimeWolfIsShaking) * p_70915_1_) / 2.0F * 0.25F;
   }

   @SideOnly(Side.CLIENT)
   public float getShakeAngle(float p_70923_1_, float p_70923_2_) {
      float f2 = (this.prevTimeWolfIsShaking + (this.timeWolfIsShaking - this.prevTimeWolfIsShaking) * p_70923_1_ + p_70923_2_) / 1.8F;
      if (f2 < 0.0F) {
         f2 = 0.0F;
      } else if (f2 > 1.0F) {
         f2 = 1.0F;
      }

      return MathHelper.func_76126_a(f2 * 3.1415927F) * MathHelper.func_76126_a(f2 * 3.1415927F * 11.0F) * 0.15F * 3.1415927F;
   }

   public float func_70047_e() {
      return this.field_70131_O * 0.8F;
   }

   @SideOnly(Side.CLIENT)
   public float getInterestedAngle(float p_70917_1_) {
      return (this.field_70924_f + (this.field_70926_e - this.field_70924_f) * p_70917_1_) * 0.15F * 3.1415927F;
   }

   public boolean func_70097_a(DamageSource p_70097_1_, float p_70097_2_) {
      if (this.func_85032_ar()) {
         return false;
      } else {
         Entity entity = p_70097_1_.func_76346_g();
         if (entity != null && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
            p_70097_2_ = (p_70097_2_ + 1.0F) / 2.0F;
         }

         return super.func_70097_a(p_70097_1_, p_70097_2_);
      }
   }

   public boolean func_70652_k(Entity p_70652_1_) {
      boolean flag = super.func_70652_k(p_70652_1_);
      if (flag) {
         int i = this.field_70170_p.field_73013_u.func_151525_a();
         if (this.field_70146_Z.nextFloat() < (float)i * 0.1F) {
            p_70652_1_.func_70015_d(2 * i);
         }
      }

      return flag;
   }

   @SideOnly(Side.CLIENT)
   public void func_70103_a(byte p_70103_1_) {
      if (p_70103_1_ == 8) {
         this.field_70928_h = true;
         this.timeWolfIsShaking = 0.0F;
         this.prevTimeWolfIsShaking = 0.0F;
      } else {
         super.func_70103_a(p_70103_1_);
      }

   }

   @SideOnly(Side.CLIENT)
   public float getTailRotation() {
      return 1.5393804F;
   }

   public int func_70641_bl() {
      return super.func_70641_bl();
   }

   public void func_70918_i(boolean p_70918_1_) {
      if (p_70918_1_) {
         this.field_70180_af.func_75692_b(19, (byte)1);
      } else {
         this.field_70180_af.func_75692_b(19, (byte)0);
      }

   }

   public boolean func_70922_bv() {
      return this.field_70180_af.func_75683_a(19) == 1;
   }

   protected boolean func_70692_ba() {
      return !this.isConverting();
   }

   public boolean isConverting() {
      return this.func_70096_w().func_75683_a(14) == 1;
   }

   public boolean func_70601_bi() {
      return this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL && this.field_70170_p.func_72855_b(this.field_70121_D) && this.field_70170_p.func_72945_a(this, this.field_70121_D).isEmpty() && !this.field_70170_p.func_72953_d(this.field_70121_D);
   }
}
