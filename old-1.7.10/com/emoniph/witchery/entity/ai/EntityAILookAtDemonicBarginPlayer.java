package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.entity.EntityDemon;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;

public class EntityAILookAtDemonicBarginPlayer extends EntityAIWatchClosest {
   private final EntityDemon theMerchant;

   public EntityAILookAtDemonicBarginPlayer(EntityDemon trader) {
      super(trader, EntityPlayer.class, 8.0F);
      this.theMerchant = trader;
   }

   public boolean func_75250_a() {
      if (this.theMerchant.isTrading()) {
         this.field_75334_a = this.theMerchant.func_70931_l_();
         return true;
      } else {
         return false;
      }
   }
}
