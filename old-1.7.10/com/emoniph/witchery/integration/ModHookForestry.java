package com.emoniph.witchery.integration;

import net.minecraft.entity.EntityLivingBase;

public class ModHookForestry extends ModHook {
   public String getModID() {
      return "Forestry";
   }

   protected void doInit() {
   }

   protected void doPostInit() {
   }

   protected void doReduceMagicPower(EntityLivingBase entity, float factor) {
   }
}
