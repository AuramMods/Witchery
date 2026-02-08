package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.EntityUtil;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;

public class EntityAIEnslaverHurtByTarget extends EntityAITarget {
   EntityCreature enslavedEntity;
   EntityLivingBase enslaversAttacker;
   private int enslaversRevengeTimer;

   public EntityAIEnslaverHurtByTarget(EntityCreature enslavedCreature) {
      super(enslavedCreature, false);
      this.enslavedEntity = enslavedCreature;
      this.func_75248_a(1);
   }

   public boolean func_75250_a() {
      if (!this.enslavedEntity.func_70644_a(Witchery.Potions.ENSLAVED)) {
         return false;
      } else {
         String ownerName = PotionEnslaved.getMobEnslaverName(this.enslavedEntity);
         if (ownerName != null && !ownerName.isEmpty()) {
            EntityLivingBase enslaver = this.enslavedEntity.field_70170_p.func_72924_a(ownerName);
            if (enslaver == null) {
               return false;
            } else {
               this.enslaversAttacker = enslaver.func_70643_av();
               int revengeTimer = enslaver.func_142015_aE();
               if (revengeTimer == this.enslaversRevengeTimer) {
                  return false;
               } else {
                  return this.enslaversAttacker == null ? false : this.func_75296_a(this.enslaversAttacker, false);
               }
            }
         } else {
            return false;
         }
      }
   }

   public void func_75249_e() {
      EntityUtil.setTarget(this.field_75299_d, this.enslaversAttacker);
      String enslaverName = PotionEnslaved.getMobEnslaverName(this.enslavedEntity);
      EntityLivingBase enslaver = this.enslavedEntity.field_70170_p.func_72924_a(enslaverName);
      if (enslaver != null) {
         this.enslaversRevengeTimer = enslaver.func_142015_aE();
      }

      super.func_75249_e();
   }
}
