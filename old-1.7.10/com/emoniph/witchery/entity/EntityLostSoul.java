package com.emoniph.witchery.entity;

import com.emoniph.witchery.entity.ai.EntityAIFlyerAttackOnCollide;
import com.emoniph.witchery.entity.ai.EntityAIFlyerLand;
import com.emoniph.witchery.entity.ai.EntityAISitAndStay;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityLostSoul extends EntitySpirit {
   private int timeToLive = -1;

   public void setTimeToLive(int i) {
      this.timeToLive = i;
   }

   public boolean isTemp() {
      return this.timeToLive != -1;
   }

   public EntityLostSoul(World world) {
      super(world);
      this.field_70714_bg.field_75782_a.clear();
      this.field_70715_bh.field_75782_a.clear();
      this.field_70714_bg.func_75776_a(1, new EntityAISitAndStay(this));
      this.field_70714_bg.func_75776_a(2, new EntityAIFlyerAttackOnCollide(this, 1.0D, true));
      this.field_70714_bg.func_75776_a(9, new EntityAIFlyerLand(this, 0.8D, true));
      this.field_70714_bg.func_75776_a(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F, 0.2F));
      this.field_70715_bh.func_75776_a(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      super.func_70014_b(par1NBTTagCompound);
      par1NBTTagCompound.func_74768_a("FeatherColor", this.getFeatherColor());
      par1NBTTagCompound.func_74774_a("SoulType", (byte)this.getSoulType());
      par1NBTTagCompound.func_74768_a("SuicideIn", this.timeToLive);
   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      super.func_70037_a(par1NBTTagCompound);
      if (par1NBTTagCompound.func_74764_b("FeatherColor")) {
         this.setFeatherColor(par1NBTTagCompound.func_74762_e("FeatherColor"));
      }

      if (par1NBTTagCompound.func_74764_b("SoulType")) {
         this.setSoulType(par1NBTTagCompound.func_74771_c("SoulType"));
      }

      if (par1NBTTagCompound.func_74764_b("SuicideIn")) {
         this.timeToLive = par1NBTTagCompound.func_74762_e("SuicideIn");
      } else {
         this.timeToLive = -1;
      }

   }

   public void func_70110_aj() {
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(22, this.field_70170_p.field_73012_v.nextInt(3));
      switch(this.getSoulType()) {
      case 0:
         this.setFeatherColor(16711680);
         break;
      case 1:
         this.setFeatherColor(65280);
         break;
      case 2:
         this.setFeatherColor(255);
      }

   }

   public int getSoulType() {
      return this.field_70180_af.func_75679_c(22);
   }

   public void setSoulType(int par1) {
      this.field_70180_af.func_75692_b(22, par1);
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(20.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.4D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(2.0D);
   }

   public boolean func_70652_k(Entity targetEntity) {
      float f = (float)this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111126_e();
      int i = 0;
      if (targetEntity instanceof EntityLivingBase) {
         f += EnchantmentHelper.func_77512_a(this, (EntityLivingBase)targetEntity);
         i += EnchantmentHelper.func_77507_b(this, (EntityLivingBase)targetEntity);
      }

      DamageSource source = null;
      if (this.field_70170_p.field_73012_v.nextInt(4) == 0) {
         switch(this.getSoulType()) {
         case 0:
            source = DamageSource.field_76372_a;
            break;
         case 1:
            source = DamageSource.func_76358_a(this);
            break;
         case 2:
            source = DamageSource.field_76376_m;
         }
      }

      if (source == null) {
         source = DamageSource.func_76358_a(this);
      }

      boolean flag = targetEntity.func_70097_a(source, f);
      if (flag) {
         if (i > 0) {
            targetEntity.func_70024_g((double)(-MathHelper.func_76126_a(this.field_70177_z * 3.1415927F / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.func_76134_b(this.field_70177_z * 3.1415927F / 180.0F) * (float)i * 0.5F));
            this.field_70159_w *= 0.6D;
            this.field_70179_y *= 0.6D;
         }

         int j = EnchantmentHelper.func_90036_a(this);
         if (j > 0) {
            targetEntity.func_70015_d(j * 4);
         }

         if (targetEntity instanceof EntityLivingBase) {
            EnchantmentHelper.func_151384_a((EntityLivingBase)targetEntity, this);
         }

         EnchantmentHelper.func_151385_b(this, targetEntity);
      }

      return flag;
   }

   public boolean func_70097_a(DamageSource source, float damage) {
      float MAX_DAMAGE = 15.0F;
      switch(this.getSoulType()) {
      case 0:
         if (!source.func_76347_k() && !source.func_94541_c()) {
            break;
         }

         return super.func_70097_a(source, Math.min(damage, 15.0F));
      case 1:
         if (!source.func_76352_a() && !source.func_82725_o() && !source.func_76347_k() && !source.func_94541_c() && source != DamageSource.field_76368_d && source != DamageSource.field_76367_g && source != DamageSource.field_76369_e && source != DamageSource.field_82727_n) {
            return super.func_70097_a(source, Math.min(damage, 15.0F));
         }
         break;
      case 2:
         if (source.func_82725_o()) {
            return super.func_70097_a(source, Math.min(damage, 15.0F));
         }
      }

      return false;
   }

   protected void func_70629_bd() {
      super.func_70629_bd();
      if (!this.field_70170_p.field_72995_K && this.timeToLive != -1 && --this.timeToLive <= 0) {
         this.func_70106_y();
      }

   }

   public void func_70071_h_() {
      super.func_70071_h_();
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.lostsoul.name");
   }
}
