package com.emoniph.witchery.infusion.infusions.symbols;

import com.emoniph.witchery.entity.EntitySpellEffect;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class SymbolEffectProjectile extends SymbolEffect {
   private float size = 1.0F;
   private int color = 16711680;
   private int timetolive = -1;

   public SymbolEffectProjectile(int effectID, String unlocalisedName) {
      super(effectID, unlocalisedName);
   }

   public SymbolEffectProjectile(int effectID, String unlocalisedName, int spellCost, boolean curse, boolean fallsToEarth, String knowledgeKey, int cooldown) {
      super(effectID, unlocalisedName, spellCost, curse, fallsToEarth, knowledgeKey, cooldown);
   }

   public SymbolEffectProjectile(int effectID, String unlocalisedName, int spellCost, boolean curse, boolean fallsToEarth, String knowledgeKey, int cooldown, boolean isVisible) {
      super(effectID, unlocalisedName, spellCost, curse, fallsToEarth, knowledgeKey, cooldown, isVisible);
   }

   public SymbolEffectProjectile setColor(int color) {
      this.color = color;
      return this;
   }

   public SymbolEffectProjectile setSize(float size) {
      this.size = size;
      return this;
   }

   public SymbolEffectProjectile setTimeToLive(int ticks) {
      this.timetolive = ticks;
      return this;
   }

   public int getColor() {
      return this.color;
   }

   public float getSize() {
      return this.size;
   }

   public void perform(World world, EntityPlayer player, int effectLevel) {
      world.func_72889_a((EntityPlayer)null, 1008, (int)player.field_70165_t, (int)player.field_70163_u, (int)player.field_70161_v, 0);
      float f = 1.0F;
      double motionX = (double)(-MathHelper.func_76126_a(player.field_70177_z / 180.0F * 3.1415927F) * MathHelper.func_76134_b(player.field_70125_A / 180.0F * 3.1415927F) * f);
      double motionZ = (double)(MathHelper.func_76134_b(player.field_70177_z / 180.0F * 3.1415927F) * MathHelper.func_76134_b(player.field_70125_A / 180.0F * 3.1415927F) * f);
      double motionY = (double)(-MathHelper.func_76126_a(player.field_70125_A / 180.0F * 3.1415927F) * f);
      EntitySpellEffect entity = new EntitySpellEffect(world, player, motionX, motionY, motionZ, this, effectLevel);
      if (this.timetolive > 0) {
         entity.setLifeTime(this.timetolive);
      }

      entity.func_70012_b(player.field_70165_t, player.field_70163_u + (double)player.func_70047_e(), player.field_70161_v, entity.field_70177_z, entity.field_70125_A);
      entity.func_70107_b(player.field_70165_t, player.field_70163_u + (double)player.func_70047_e(), player.field_70161_v);
      double d6 = (double)MathHelper.func_76133_a(motionX * motionX + motionY * motionY + motionZ * motionZ);
      entity.accelerationX = motionX / d6 * 0.3D;
      entity.accelerationY = motionY / d6 * 0.3D;
      entity.accelerationZ = motionZ / d6 * 0.3D;
      double d8 = 1.5D;
      Vec3 vec3 = player.func_70676_i(1.0F);
      entity.field_70165_t = player.field_70165_t + vec3.field_72450_a * 1.5D;
      entity.field_70163_u = player.field_70163_u + (double)player.eyeHeight - 0.10000000149011612D + vec3.field_72448_b * 1.5D;
      entity.field_70161_v = player.field_70161_v + vec3.field_72449_c * 1.5D;
      world.func_72838_d(entity);
   }

   public abstract void onCollision(World var1, EntityLivingBase var2, MovingObjectPosition var3, EntitySpellEffect var4);
}
