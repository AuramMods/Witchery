package com.emoniph.witchery.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIMoveIndoors;

public class EntityAIMoveIndoorsLeashAware extends EntityAIMoveIndoors {
   private EntityCreature creature;

   public EntityAIMoveIndoorsLeashAware(EntityCreature creature) {
      super(creature);
      this.creature = creature;
   }

   public boolean func_75250_a() {
      return this.creature != null && !this.creature.func_110167_bD() && super.func_75250_a();
   }
}
