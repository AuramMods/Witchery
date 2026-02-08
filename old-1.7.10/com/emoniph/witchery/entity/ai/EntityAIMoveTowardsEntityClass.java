package com.emoniph.witchery.entity.ai;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class EntityAIMoveTowardsEntityClass extends EntityAIBase {
   private EntityCreature theEntity;
   private EntityLivingBase targetEntity;
   private Class<? extends EntityLiving> targetType;
   private double movePosX;
   private double movePosY;
   private double movePosZ;
   private double speed;
   private float maxTargetDistance;
   private float minTargetDistance;

   public EntityAIMoveTowardsEntityClass(EntityCreature par1EntityCreature, Class<? extends EntityLiving> targetType, double par2, float par4, float par5) {
      this.theEntity = par1EntityCreature;
      this.targetType = targetType;
      this.speed = par2;
      this.minTargetDistance = par4;
      this.maxTargetDistance = par5;
      this.func_75248_a(1);
   }

   private EntityLiving getDistanceSqToPartner() {
      double R = (double)this.maxTargetDistance;
      AxisAlignedBB bb = AxisAlignedBB.func_72330_a(this.theEntity.field_70165_t - R, this.theEntity.field_70163_u - R, this.theEntity.field_70161_v - R, this.theEntity.field_70165_t + R, this.theEntity.field_70163_u + R, this.theEntity.field_70161_v + R);
      List mogs = this.theEntity.field_70170_p.func_72872_a(this.targetType, bb);
      double minDistance = Double.MAX_VALUE;
      EntityLiving target = null;
      Iterator i$ = mogs.iterator();

      while(i$.hasNext()) {
         Object obj = i$.next();
         EntityLiving mog = (EntityLiving)obj;
         double distance = this.theEntity.func_70068_e(mog);
         if (distance < minDistance) {
            minDistance = distance;
            target = mog;
         }
      }

      return target;
   }

   public boolean func_75250_a() {
      if (this.theEntity.field_70170_p.field_73012_v.nextInt(20) != 0) {
         return false;
      } else {
         this.targetEntity = this.getDistanceSqToPartner();
         if (this.targetEntity == null) {
            return false;
         } else {
            double dist = this.targetEntity.func_70068_e(this.theEntity);
            if (dist > (double)(this.maxTargetDistance * this.maxTargetDistance)) {
               return false;
            } else if (dist < (double)(this.minTargetDistance * this.minTargetDistance)) {
               return false;
            } else {
               Vec3 vec3 = RandomPositionGenerator.func_75464_a(this.theEntity, 16, 7, Vec3.func_72443_a(this.targetEntity.field_70165_t, this.targetEntity.field_70163_u, this.targetEntity.field_70161_v));
               if (vec3 == null) {
                  return false;
               } else {
                  this.movePosX = vec3.field_72450_a;
                  this.movePosY = vec3.field_72448_b;
                  this.movePosZ = vec3.field_72449_c;
                  return true;
               }
            }
         }
      }
   }

   public boolean func_75253_b() {
      return !this.theEntity.func_70661_as().func_75500_f() && this.targetEntity.func_70089_S() && this.targetEntity.func_70068_e(this.theEntity) < (double)(this.maxTargetDistance * this.maxTargetDistance);
   }

   public void func_75251_c() {
      this.targetEntity = null;
   }

   public void func_75249_e() {
      this.theEntity.func_70661_as().func_75492_a(this.movePosX, this.movePosY, this.movePosZ, this.speed);
   }
}
