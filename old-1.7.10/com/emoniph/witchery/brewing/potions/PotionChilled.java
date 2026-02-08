package com.emoniph.witchery.brewing.potions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class PotionChilled extends PotionBase implements IHandleLivingHurt {
   public PotionChilled(int id, int color) {
      super(id, color);
   }

   public void postContructInitialize() {
      this.func_111184_a(SharedMonsterAttributes.field_111263_d, "7A20B8CD-7A97-4800-A7DC-5B464E31A11A", -0.1D, 2);
   }

   public boolean handleAllHurtEvents() {
      return false;
   }

   public void onLivingHurt(World world, EntityLivingBase entity, LivingHurtEvent event, int amplifier) {
      if (!world.field_72995_K && event.source.func_76347_k()) {
         event.ammount = Math.max(event.ammount - (float)(1 + amplifier), Math.min(event.ammount, amplifier >= 2 ? 0.0F : 1.0F));
      }

   }

   public boolean func_76397_a(int duration, int amplifier) {
      int k = 25 >> amplifier;
      return k > 0 ? duration % k == 0 : true;
   }

   public void func_76394_a(EntityLivingBase entity, int amplifier) {
      if (entity instanceof EntityBlaze || amplifier >= 2) {
         entity.func_70097_a(DamageSource.field_76376_m, 1.0F);
      }

   }
}
