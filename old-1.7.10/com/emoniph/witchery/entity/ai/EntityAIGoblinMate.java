package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.entity.EntityGoblin;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class EntityAIGoblinMate extends EntityAIBase {
   private EntityGoblin goblinObj;
   private EntityGoblin mate;
   private World worldObj;
   private int matingTimeout;
   Village villageObj;

   public EntityAIGoblinMate(EntityGoblin goblin) {
      this.goblinObj = goblin;
      this.worldObj = goblin.field_70170_p;
      this.func_75248_a(3);
   }

   public boolean func_75250_a() {
      if (this.goblinObj.func_70874_b() != 0) {
         return false;
      } else if (this.goblinObj.func_70681_au().nextInt(500) != 0) {
         return false;
      } else {
         this.villageObj = this.worldObj.field_72982_D.func_75550_a(MathHelper.func_76128_c(this.goblinObj.field_70165_t), MathHelper.func_76128_c(this.goblinObj.field_70163_u), MathHelper.func_76128_c(this.goblinObj.field_70161_v), 0);
         if (this.villageObj == null) {
            return false;
         } else if (!this.checkSufficientDoorsPresentForNewVillager()) {
            return false;
         } else {
            Entity entity = this.worldObj.func_72857_a(EntityGoblin.class, this.goblinObj.field_70121_D.func_72314_b(8.0D, 3.0D, 8.0D), this.goblinObj);
            if (entity == null) {
               return false;
            } else {
               this.mate = (EntityGoblin)entity;
               return this.mate.func_70874_b() == 0;
            }
         }
      }
   }

   public void func_75249_e() {
      this.matingTimeout = 300;
      this.goblinObj.setMating(true);
   }

   public void func_75251_c() {
      this.villageObj = null;
      this.mate = null;
      this.goblinObj.setMating(false);
   }

   public boolean func_75253_b() {
      return this.matingTimeout >= 0 && this.checkSufficientDoorsPresentForNewVillager() && this.goblinObj.func_70874_b() == 0;
   }

   public void func_75246_d() {
      --this.matingTimeout;
      this.goblinObj.func_70671_ap().func_75651_a(this.mate, 10.0F, 30.0F);
      if (this.goblinObj.func_70068_e(this.mate) > 2.25D) {
         this.goblinObj.func_70661_as().func_75497_a(this.mate, 0.25D);
      } else if (this.matingTimeout == 0 && this.mate.isMating()) {
         this.giveBirth();
      }

      if (this.goblinObj.func_70681_au().nextInt(35) == 0) {
         this.worldObj.func_72960_a(this.goblinObj, (byte)12);
      }

   }

   private boolean checkSufficientDoorsPresentForNewVillager() {
      if (!this.villageObj.func_82686_i()) {
         return false;
      } else {
         int i = (int)((double)((float)this.villageObj.func_75567_c()) * 0.35D);
         return this.getNumVillagers() < i;
      }
   }

   private int getNumVillagers() {
      if (this.worldObj != null && this.goblinObj != null) {
         List list = this.worldObj.func_72872_a(EntityGoblin.class, this.goblinObj.field_70121_D.func_72314_b(32.0D, 3.0D, 32.0D));
         return list != null ? list.size() : 0;
      } else {
         return 0;
      }
   }

   private void giveBirth() {
      EntityGoblin entityvillager = this.goblinObj.createChild(this.mate);
      this.mate.func_70873_a(6000);
      this.goblinObj.func_70873_a(6000);
      entityvillager.func_70873_a(-24000);
      entityvillager.func_70012_b(this.goblinObj.field_70165_t, this.goblinObj.field_70163_u, this.goblinObj.field_70161_v, 0.0F, 0.0F);
      this.worldObj.func_72838_d(entityvillager);
      this.worldObj.func_72960_a(entityvillager, (byte)12);
   }
}
