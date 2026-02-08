package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.IHandleDT;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityDeath extends EntityMob implements IBossDisplayData, IHandleDT {
   private static final UUID attackingSpeedBoostModifierUUID = UUID.fromString("e942c510-c256-11e3-8a33-0800200c9a66");
   private static final AttributeModifier attackingSpeedBoostModifier;
   private int teleportDelay;
   private int stareTimer;
   private Entity lastEntityToAttack;
   private boolean isAggressive;

   public EntityDeath(World par1World) {
      super(par1World);
      this.func_70105_a(0.6F, 1.8F);
      this.field_70138_W = 1.0F;
      this.field_70178_ae = true;
      this.field_70728_aV = 80;
   }

   protected int func_70682_h(int par1) {
      return par1;
   }

   protected boolean func_70692_ba() {
      return false;
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.death.name");
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(1000.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.30000001192092896D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(7.0D);
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(16, new Byte((byte)0));
      this.field_70180_af.func_75682_a(17, new Byte((byte)0));
      this.field_70180_af.func_75682_a(18, new Byte((byte)0));
   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      super.func_70014_b(par1NBTTagCompound);
   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      super.func_70037_a(par1NBTTagCompound);
   }

   protected Entity func_70782_k() {
      EntityPlayer entityplayer = this.field_70170_p.func_72856_b(this, 64.0D);
      if (entityplayer != null) {
         if (this.shouldAttackPlayer(entityplayer)) {
            this.isAggressive = true;
            if (this.stareTimer == 0) {
               this.field_70170_p.func_72956_a(entityplayer, "mob.wither.spawn", 1.0F, 1.0F);
            }

            if (this.stareTimer++ == 5) {
               this.stareTimer = 0;
               this.setScreaming(true);
               return entityplayer;
            }
         } else {
            this.stareTimer = 0;
         }
      }

      return null;
   }

   private boolean shouldAttackPlayer(EntityPlayer par1EntityPlayer) {
      ItemStack itemstack = par1EntityPlayer.field_71071_by.field_70460_b[3];
      Vec3 vec3 = par1EntityPlayer.func_70676_i(1.0F).func_72432_b();
      Vec3 vec31 = Vec3.func_72443_a(this.field_70165_t - par1EntityPlayer.field_70165_t, this.field_70121_D.field_72338_b + (double)(this.field_70131_O / 2.0F) - (par1EntityPlayer.field_70163_u + (double)par1EntityPlayer.func_70047_e()), this.field_70161_v - par1EntityPlayer.field_70161_v);
      double d0 = vec31.func_72433_c();
      vec31 = vec31.func_72432_b();
      double d1 = vec3.func_72430_b(vec31);
      return d1 > 1.0D - 0.025D / d0;
   }

   public void func_70636_d() {
      if (this.field_70173_aa % 20 == 0) {
         this.func_70691_i(1.0F);
      }

      if (this.lastEntityToAttack != this.field_70789_a) {
         IAttributeInstance attributeinstance = this.func_110148_a(SharedMonsterAttributes.field_111263_d);
         attributeinstance.func_111124_b(attackingSpeedBoostModifier);
         if (this.field_70789_a != null) {
            attributeinstance.func_111121_a(attackingSpeedBoostModifier);
         }
      }

      this.lastEntityToAttack = this.field_70789_a;

      for(int i = 0; i < 2; ++i) {
         this.field_70170_p.func_72869_a("portal", this.field_70165_t + (this.field_70146_Z.nextDouble() - 0.5D) * (double)this.field_70130_N, this.field_70163_u + this.field_70146_Z.nextDouble() * (double)this.field_70131_O - 0.25D, this.field_70161_v + (this.field_70146_Z.nextDouble() - 0.5D) * (double)this.field_70130_N, (this.field_70146_Z.nextDouble() - 0.5D) * 2.0D, -this.field_70146_Z.nextDouble(), (this.field_70146_Z.nextDouble() - 0.5D) * 2.0D);
      }

      if (this.isScreaming() && !this.isAggressive) {
         this.setScreaming(false);
      }

      this.field_70703_bu = false;
      if (this.field_70789_a != null) {
         this.func_70625_a(this.field_70789_a, 100.0F, 100.0F);
      }

      if (!this.field_70170_p.field_72995_K && this.func_70089_S()) {
         if (this.field_70789_a != null) {
            if ((!(this.field_70789_a instanceof EntityPlayer) || !this.shouldAttackPlayer((EntityPlayer)this.field_70789_a)) && this.field_70170_p.field_73012_v.nextInt(100) != 0) {
               if (this.field_70789_a.func_70068_e(this) > 256.0D && this.teleportDelay++ >= 30 && this.teleportToEntity(this.field_70789_a)) {
                  this.teleportDelay = 0;
               }
            } else {
               if (this.field_70789_a.func_70068_e(this) < 16.0D) {
                  this.teleportRandomly();
               }

               this.teleportDelay = 0;
            }
         } else {
            this.setScreaming(false);
            this.teleportDelay = 0;
         }
      }

      if (this.field_70170_p.field_73012_v.nextDouble() < 0.05D && this.func_70638_az() != null && (this.func_70638_az().field_70160_al || this.func_70638_az() instanceof EntityPlayer && ((EntityPlayer)this.func_70638_az()).field_71075_bZ.field_75100_b) && !this.func_70638_az().func_70644_a(Potion.field_76421_d)) {
         this.func_70638_az().func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, 200, 5));
      }

      super.func_70636_d();
   }

   protected boolean teleportRandomly() {
      double d0 = this.field_70165_t + (this.field_70146_Z.nextDouble() - 0.5D) * 32.0D;
      double d1 = this.field_70163_u + (double)(this.field_70146_Z.nextInt(64) - 32);
      double d2 = this.field_70161_v + (this.field_70146_Z.nextDouble() - 0.5D) * 32.0D;
      return this.teleportTo(d0, d1, d2);
   }

   protected boolean teleportToEntity(Entity par1Entity) {
      Vec3 vec3 = Vec3.func_72443_a(this.field_70165_t - par1Entity.field_70165_t, this.field_70121_D.field_72338_b + (double)(this.field_70131_O / 2.0F) - par1Entity.field_70163_u + (double)par1Entity.func_70047_e(), this.field_70161_v - par1Entity.field_70161_v);
      vec3 = vec3.func_72432_b();
      double d0 = 16.0D;
      double d1 = this.field_70165_t + (this.field_70146_Z.nextDouble() - 0.5D) * 8.0D - vec3.field_72450_a * d0;
      double d2 = this.field_70163_u + (double)(this.field_70146_Z.nextInt(16) - 8) - vec3.field_72448_b * d0;
      double d3 = this.field_70161_v + (this.field_70146_Z.nextDouble() - 0.5D) * 8.0D - vec3.field_72449_c * d0;
      return this.teleportTo(d1, d2, d3);
   }

   protected boolean teleportTo(double par1, double par3, double par5) {
      double d3 = this.field_70165_t;
      double d4 = this.field_70163_u;
      double d5 = this.field_70161_v;
      this.field_70165_t = par1;
      this.field_70163_u = par3;
      this.field_70161_v = par5;
      boolean flag = false;
      int i = MathHelper.func_76128_c(this.field_70165_t);
      int j = MathHelper.func_76128_c(this.field_70163_u);
      int k = MathHelper.func_76128_c(this.field_70161_v);
      if (this.field_70170_p.func_72899_e(i, j, k)) {
         boolean flag1 = false;

         while(!flag1 && j > 0) {
            Block block = this.field_70170_p.func_147439_a(i, j - 1, k);
            if (block.func_149688_o().func_76230_c()) {
               flag1 = true;
            } else {
               --this.field_70163_u;
               --j;
            }
         }

         if (flag1) {
            this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
            if (this.field_70170_p.func_72945_a(this, this.field_70121_D).isEmpty() && !this.field_70170_p.func_72953_d(this.field_70121_D)) {
               flag = true;
            }
         }
      }

      if (!flag) {
         this.func_70107_b(d3, d4, d5);
         return false;
      } else {
         short short1 = 128;

         for(int l = 0; l < short1; ++l) {
            double d6 = (double)l / ((double)short1 - 1.0D);
            float f = (this.field_70146_Z.nextFloat() - 0.5F) * 0.2F;
            float f1 = (this.field_70146_Z.nextFloat() - 0.5F) * 0.2F;
            float f2 = (this.field_70146_Z.nextFloat() - 0.5F) * 0.2F;
            double d7 = d3 + (this.field_70165_t - d3) * d6 + (this.field_70146_Z.nextDouble() - 0.5D) * (double)this.field_70130_N * 2.0D;
            double d8 = d4 + (this.field_70163_u - d4) * d6 + this.field_70146_Z.nextDouble() * (double)this.field_70131_O;
            double d9 = d5 + (this.field_70161_v - d5) * d6 + (this.field_70146_Z.nextDouble() - 0.5D) * (double)this.field_70130_N * 2.0D;
            this.field_70170_p.func_72869_a("portal", d7, d8, d9, (double)f, (double)f1, (double)f2);
         }

         this.field_70170_p.func_72908_a(d3, d4, d5, "mob.endermen.portal", 1.0F, 1.0F);
         this.func_85030_a("mob.endermen.portal", 1.0F, 1.0F);
         return true;
      }
   }

   protected String func_70639_aQ() {
      return null;
   }

   protected String func_70621_aR() {
      return "mob.skeleton.hurt";
   }

   protected String func_70673_aS() {
      return "mob.skeleton.death";
   }

   protected Item func_146068_u() {
      return Items.field_151103_aS;
   }

   protected void func_70628_a(boolean par1, int par2) {
      this.func_70099_a(new ItemStack(Items.field_151144_bL, 1, 0), 0.0F);
      this.func_70099_a(new ItemStack(Items.field_151103_aS, 5, 0), 0.0F);
      Enchantment enchantment = Enchantment.field_92090_c[this.field_70146_Z.nextInt(Enchantment.field_92090_c.length)];
      int k = MathHelper.func_76136_a(this.field_70146_Z, Math.min(enchantment.func_77319_d() + 2, enchantment.func_77325_b()), enchantment.func_77325_b());
      ItemStack itemstack = Items.field_151134_bR.func_92111_a(new EnchantmentData(enchantment, k));
      this.func_70099_a(itemstack, 0.0F);
      if (this.field_70170_p.field_73012_v.nextInt(4) == 0) {
         ItemStack sword = new ItemStack(Items.field_151048_u);
         EnchantmentHelper.func_77504_a(this.field_70170_p.field_73012_v, sword, 40);
         sword.func_151001_c(Witchery.resource("item.witchery.swordofdeath.customname"));
         this.func_70099_a(sword, 0.0F);
      }

      switch(this.field_70170_p.field_73012_v.nextInt(5)) {
      case 0:
         this.func_70099_a(new ItemStack(Items.field_151141_av), 0.0F);
         this.func_70099_a(Witchery.Items.GENERIC.itemBinkyHead.createStack(), 0.0F);
         break;
      case 1:
         this.func_70099_a(new ItemStack(Witchery.Items.DEATH_HOOD), 0.0F);
         break;
      case 2:
         this.func_70099_a(new ItemStack(Witchery.Items.DEATH_ROBE), 0.0F);
         break;
      case 3:
         this.func_70099_a(new ItemStack(Witchery.Items.DEATH_FEET), 0.0F);
         break;
      case 4:
         this.func_70099_a(new ItemStack(Witchery.Items.DEATH_HAND), 0.0F);
      }

   }

   public float getCapDT(DamageSource source, float damage) {
      return 15.0F;
   }

   public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
      if (this.func_85032_ar()) {
         return false;
      } else {
         this.setScreaming(true);
         if (par1DamageSource instanceof EntityDamageSource && par1DamageSource.func_76346_g() instanceof EntityPlayer) {
            this.isAggressive = true;
         }

         if (par1DamageSource instanceof EntityDamageSourceIndirect) {
            this.isAggressive = false;

            for(int i = 0; i < 64; ++i) {
               if (this.teleportRandomly()) {
                  return true;
               }
            }

            return super.func_70097_a(par1DamageSource, Math.min(par2, 15.0F));
         } else {
            return super.func_70097_a(par1DamageSource, Math.min(par2, 15.0F));
         }
      }
   }

   public boolean func_70652_k(Entity par1Entity) {
      float f = (float)this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111126_e();
      int i = 0;
      if (par1Entity instanceof EntityLivingBase) {
         EntityLivingBase living = (EntityLivingBase)par1Entity;
         float maxHealth = living.func_110138_aP();
         f = Math.max(maxHealth * 0.15F, 1.0F);
      }

      if (par1Entity instanceof EntityLivingBase) {
         f += EnchantmentHelper.func_77512_a(this, (EntityLivingBase)par1Entity);
         i += EnchantmentHelper.func_77507_b(this, (EntityLivingBase)par1Entity);
      }

      boolean flag = EntityUtil.touchOfDeath(par1Entity, this, f);
      if (flag) {
         if (i > 0) {
            par1Entity.func_70024_g((double)(-MathHelper.func_76126_a(this.field_70177_z * 3.1415927F / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.func_76134_b(this.field_70177_z * 3.1415927F / 180.0F) * (float)i * 0.5F));
            this.field_70159_w *= 0.6D;
            this.field_70179_y *= 0.6D;
         }

         int j = EnchantmentHelper.func_90036_a(this);
         if (j > 0) {
            par1Entity.func_70015_d(j * 4);
         }
      }

      return flag;
   }

   public void func_70645_a(DamageSource par1DamageSource) {
      super.func_70645_a(par1DamageSource);
      if (!this.field_70170_p.field_72995_K) {
         ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, this, 1.0D, 2.0D, 16);
      }

      this.func_70106_y();
   }

   public boolean isScreaming() {
      return this.field_70180_af.func_75683_a(18) > 0;
   }

   public void setScreaming(boolean par1) {
      this.field_70180_af.func_75692_b(18, (byte)(par1 ? 1 : 0));
   }

   static {
      attackingSpeedBoostModifier = (new AttributeModifier(attackingSpeedBoostModifierUUID, "Attacking speed boost", 6.199999809265137D, 0)).func_111168_a(false);
   }
}
