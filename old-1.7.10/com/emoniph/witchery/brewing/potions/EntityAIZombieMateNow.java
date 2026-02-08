package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;

public class EntityAIZombieMateNow extends EntityAIBase {
   private EntityZombie zombieObj;
   private EntityZombie mate;
   private World worldObj;
   private int matingTimeout;
   private boolean mating;
   private boolean begin;

   public EntityAIZombieMateNow(EntityZombie zombie) {
      this.zombieObj = zombie;
      this.worldObj = zombie.field_70170_p;
      this.func_75248_a(3);
   }

   public void beginMating() {
      this.begin = true;
   }

   public boolean func_75250_a() {
      if (!this.begin) {
         return false;
      } else {
         EntityZombie zombie = (EntityZombie)EntityUtil.findNearestEntityWithinAABB(this.worldObj, EntityZombie.class, this.zombieObj.field_70121_D.func_72314_b(8.0D, 3.0D, 8.0D), this.zombieObj);
         if (zombie != null && !zombie.func_70631_g_()) {
            this.mate = zombie;
            return true;
         } else {
            return false;
         }
      }
   }

   public void func_75249_e() {
      this.matingTimeout = 600;
      this.mating = true;
      this.begin = false;
   }

   public void func_75251_c() {
      this.mate = null;
      this.mating = false;
      this.begin = false;
   }

   public boolean func_75253_b() {
      boolean keepGoing = this.matingTimeout >= 0;
      return keepGoing;
   }

   public void func_75246_d() {
      --this.matingTimeout;
      this.zombieObj.func_70671_ap().func_75651_a(this.mate, 10.0F, 30.0F);
      if (this.zombieObj.func_70068_e(this.mate) > 2.25D) {
         this.zombieObj.func_70661_as().func_75497_a(this.mate, 1.4D);
      } else if (this.matingTimeout == 0 && this.mating) {
         this.giveBirth();
      }

   }

   private void giveBirth() {
      ParticleEffect.HEART.send(SoundEffect.NONE, this.mate, 1.0D, 2.0D, 8);
      this.zombieObj.func_82229_g(true);
      this.mate.func_82229_g(true);
      EntityZombie baby = new EntityZombie(this.worldObj);
      baby.func_70012_b(this.mate.field_70165_t, this.mate.field_70163_u, this.mate.field_70161_v, 0.0F, 0.0F);
      baby.func_82227_f(true);
      this.worldObj.func_72838_d(baby);
   }
}
