package com.emoniph.witchery.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAvoidEntity;

public class EntityAIAvoidEntityConditionally extends EntityAIAvoidEntity {
   private final EntityAIAvoidEntityConditionally.IAvoidEntities condition;

   public EntityAIAvoidEntityConditionally(EntityCreature entity, Class targetClass, float distanceFromEntity, double farSpeed, double nearSpeed, EntityAIAvoidEntityConditionally.IAvoidEntities condition) {
      super(entity, targetClass, distanceFromEntity, farSpeed, nearSpeed);
      this.condition = condition;
   }

   public boolean func_75250_a() {
      return super.func_75250_a() && !this.condition.shouldAvoid();
   }

   public interface IAvoidEntities {
      boolean shouldAvoid();
   }
}
