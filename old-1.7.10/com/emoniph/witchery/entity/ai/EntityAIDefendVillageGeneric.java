package com.emoniph.witchery.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.village.Village;

public class EntityAIDefendVillageGeneric extends EntityAITarget {
   EntityAIDefendVillageGeneric.IVillageGuard defender;
   EntityLivingBase villageAgressorTarget;

   public EntityAIDefendVillageGeneric(EntityAIDefendVillageGeneric.IVillageGuard guard) {
      super(guard.getCreature(), false, true);
      this.defender = guard;
      this.func_75248_a(1);
   }

   public boolean func_75250_a() {
      Village village = this.defender.getVillage();
      if (village == null) {
         return false;
      } else {
         this.villageAgressorTarget = village.func_75571_b(this.defender.getCreature());
         if (!this.func_75296_a(this.villageAgressorTarget, false)) {
            if (this.field_75299_d.func_70681_au().nextInt(20) == 0) {
               this.villageAgressorTarget = village.func_82685_c(this.defender.getCreature());
               return this.func_75296_a(this.villageAgressorTarget, false);
            } else {
               return false;
            }
         } else {
            return true;
         }
      }
   }

   public void func_75249_e() {
      this.defender.getCreature().func_70624_b(this.villageAgressorTarget);
      super.func_75249_e();
   }

   public interface IVillageGuard {
      Village getVillage();

      EntityCreature getCreature();
   }
}
