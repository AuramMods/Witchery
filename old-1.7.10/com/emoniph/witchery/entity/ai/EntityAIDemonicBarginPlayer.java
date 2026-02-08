package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.entity.EntityDemon;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class EntityAIDemonicBarginPlayer extends EntityAIBase {
   private EntityDemon trader;

   public EntityAIDemonicBarginPlayer(EntityDemon trader) {
      this.trader = trader;
      this.func_75248_a(5);
   }

   public boolean func_75250_a() {
      if (!this.trader.func_70089_S()) {
         return false;
      } else if (this.trader.func_70090_H()) {
         return false;
      } else if (!this.trader.field_70122_E) {
         return false;
      } else if (this.trader.field_70133_I) {
         return false;
      } else {
         EntityPlayer entityplayer = this.trader.func_70931_l_();
         return entityplayer == null ? false : (this.trader.func_70068_e(entityplayer) > 16.0D ? false : entityplayer.field_71070_bA instanceof Container);
      }
   }

   public void func_75249_e() {
      this.trader.func_70661_as().func_75499_g();
   }

   public void func_75251_c() {
      this.trader.func_70932_a_((EntityPlayer)null);
      this.trader.field_70715_bh.func_75774_a();
   }
}
