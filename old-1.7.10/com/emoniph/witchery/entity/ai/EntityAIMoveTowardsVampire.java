package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.common.ExtendedPlayer;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

public class EntityAIMoveTowardsVampire extends EntityAIBase {
   private EntityCreature theEntity;
   private EntityLivingBase targetEntity;
   private double movePosX;
   private double movePosY;
   private double movePosZ;
   private double speed;
   private float maxTargetDistance;
   private float minTargetDistance;

   public EntityAIMoveTowardsVampire(EntityCreature par1EntityCreature, double par2, float min, float max) {
      this.theEntity = par1EntityCreature;
      this.speed = par2;
      this.minTargetDistance = min;
      this.maxTargetDistance = max;
      this.func_75248_a(1);
   }

   private EntityLivingBase getDistanceSqToPartner() {
      double R = (double)this.maxTargetDistance;
      AxisAlignedBB bb = AxisAlignedBB.func_72330_a(this.theEntity.field_70165_t - R, this.theEntity.field_70163_u - R, this.theEntity.field_70161_v - R, this.theEntity.field_70165_t + R, this.theEntity.field_70163_u + R, this.theEntity.field_70161_v + R);
      List<EntityPlayer> mogs = this.theEntity.field_70170_p.func_72872_a(EntityPlayer.class, bb);
      double minDistance = Double.MAX_VALUE;
      EntityLivingBase target = null;
      Iterator i$ = mogs.iterator();

      while(i$.hasNext()) {
         EntityPlayer player = (EntityPlayer)i$.next();
         if (ExtendedPlayer.get(player).getVampireLevel() >= 8) {
            double distance = this.theEntity.func_70068_e(player);
            if (distance < minDistance) {
               minDistance = distance;
               target = player;
            }
         }
      }

      return target;
   }

   public boolean func_75250_a() {
      this.targetEntity = this.getDistanceSqToPartner();
      if (this.targetEntity == null) {
         return false;
      } else {
         double dist = this.targetEntity.func_70068_e(this.theEntity);
         if (dist > (double)(this.maxTargetDistance * this.maxTargetDistance)) {
            return false;
         } else {
            return !(dist < (double)(this.minTargetDistance * this.minTargetDistance));
         }
      }
   }

   public boolean func_75253_b() {
      if (this.theEntity.field_70173_aa % 20 == 0) {
         this.theEntity.func_70661_as().func_75492_a(this.targetEntity.field_70165_t, this.targetEntity.field_70163_u, this.targetEntity.field_70161_v, this.speed);
      }

      return true;
   }

   public void func_75251_c() {
      this.targetEntity = null;
   }

   public void func_75249_e() {
      this.theEntity.func_70661_as().func_75492_a(this.targetEntity.field_70165_t, this.targetEntity.field_70163_u, this.targetEntity.field_70161_v, this.speed);
   }
}
