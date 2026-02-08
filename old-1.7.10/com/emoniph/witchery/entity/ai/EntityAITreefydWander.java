package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.entity.EntityTreefyd;
import net.minecraft.entity.ai.EntityAIWander;

public class EntityAITreefydWander extends EntityAIWander {
   private final EntityTreefyd treefyd;

   public EntityAITreefydWander(EntityTreefyd treefyd, double speed) {
      super(treefyd, speed);
      this.treefyd = treefyd;
   }

   public boolean func_75250_a() {
      return !this.treefyd.isSentinal() && super.func_75250_a();
   }

   public boolean func_75253_b() {
      return !this.treefyd.isSentinal() && super.func_75253_b();
   }
}
