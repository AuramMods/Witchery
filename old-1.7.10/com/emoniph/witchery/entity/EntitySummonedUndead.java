package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntitySummonedUndead extends EntityMob {
   private int timeToLive = -1;

   public EntitySummonedUndead(World world) {
      super(world);
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(17, "");
      this.field_70180_af.func_75682_a(18, Integer.valueOf(0));
      this.field_70180_af.func_75682_a(19, Integer.valueOf(0));
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      if (this.getSummonerName() == null) {
         nbtRoot.func_74778_a("Summoner", "");
      } else {
         nbtRoot.func_74778_a("Summoner", this.getSummonerName());
      }

      nbtRoot.func_74757_a("Obscured", this.isObscured());
      nbtRoot.func_74768_a("SuicideIn", this.timeToLive);
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      String s = nbtRoot.func_74779_i("Summoner");
      if (s.length() > 0) {
         this.setSummoner(s);
      }

      this.setObscured(nbtRoot.func_74767_n("Obscured"));
      if (nbtRoot.func_74764_b("SuicideIn")) {
         this.timeToLive = nbtRoot.func_74762_e("SuicideIn");
      } else {
         this.timeToLive = -1;
      }

   }

   public void setTimeToLive(int i) {
      this.timeToLive = i;
   }

   public boolean isTemp() {
      return this.timeToLive != -1;
   }

   protected int func_70682_h(int par1) {
      return par1;
   }

   public String getSummonerName() {
      return this.field_70180_af.func_75681_e(17);
   }

   public void setSummoner(String par1Str) {
      this.func_110163_bv();
      this.field_70180_af.func_75692_b(17, par1Str);
   }

   public EntityPlayer getSummoner() {
      return this.field_70170_p.func_72924_a(this.getSummonerName());
   }

   public EnumCreatureAttribute func_70668_bt() {
      return EnumCreatureAttribute.UNDEAD;
   }

   protected void func_70628_a(boolean par1, int par2) {
      if (!this.isTemp()) {
         int chance = this.field_70146_Z.nextInt(Math.max(4 - par2, 2));
         int quantity = chance == 0 ? 1 : 0;
         if (quantity > 0) {
            this.func_70099_a(Witchery.Items.GENERIC.itemSpectralDust.createStack(quantity), 0.0F);
         }
      }

   }

   protected void func_70629_bd() {
      super.func_70629_bd();
      if (this.field_70170_p != null && !this.field_70128_L && !this.field_70170_p.field_72995_K && this.timeToLive != -1 && (--this.timeToLive == 0 || this.func_70638_az() == null || this.func_70638_az().field_70128_L)) {
         ParticleEffect.EXPLODE.send(SoundEffect.NONE, this, 1.0D, 1.0D, 16);
         this.func_70106_y();
      }

   }

   public int func_70627_aG() {
      return super.func_70627_aG() * 3;
   }

   public boolean isScreaming() {
      return this.field_70180_af.func_75679_c(18) == 1;
   }

   protected void setScreaming(boolean screaming) {
      this.field_70180_af.func_75692_b(18, Integer.valueOf(screaming ? 1 : 0));
   }

   public boolean isObscured() {
      return this.field_70180_af.func_75679_c(19) == 1;
   }

   public void setObscured(boolean obscured) {
      this.field_70180_af.func_75692_b(19, Integer.valueOf(obscured ? 1 : 0));
   }

   public boolean func_70097_a(DamageSource damageSource, float damage) {
      return super.func_70097_a(damageSource, Math.min(damage, 15.0F));
   }
}
