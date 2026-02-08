package com.emoniph.witchery.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAISitAndStay extends EntityAIBase {
   private EntityTameable theEntity;

   public EntityAISitAndStay(EntityTameable par1EntityTameable) {
      this.theEntity = par1EntityTameable;
      this.func_75248_a(5);
   }

   public boolean func_75250_a() {
      return this.theEntity.func_70906_o();
   }

   public void func_75249_e() {
      this.theEntity.func_70661_as().func_75499_g();
   }

   public void func_75251_c() {
   }
}
