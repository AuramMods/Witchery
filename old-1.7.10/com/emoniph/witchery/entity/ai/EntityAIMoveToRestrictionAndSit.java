package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.entity.EntityCovenWitch;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;

public class EntityAIMoveToRestrictionAndSit extends EntityAIBase {
   private EntityCovenWitch theEntity;
   private double movePosX;
   private double movePosY;
   private double movePosZ;
   private double movementSpeed;

   public EntityAIMoveToRestrictionAndSit(EntityCovenWitch p_i2347_1_, double p_i2347_2_) {
      this.theEntity = p_i2347_1_;
      this.movementSpeed = p_i2347_2_;
      this.func_75248_a(1);
   }

   public boolean func_75250_a() {
      if (this.theEntity.func_110173_bK()) {
         return false;
      } else {
         ChunkCoordinates chunkcoordinates = this.theEntity.func_110172_bL();
         Vec3 vec3 = RandomPositionGenerator.func_75464_a(this.theEntity, 16, 7, Vec3.func_72443_a((double)chunkcoordinates.field_71574_a, (double)chunkcoordinates.field_71572_b, (double)chunkcoordinates.field_71573_c));
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

   public boolean func_75253_b() {
      return !this.theEntity.func_70661_as().func_75500_f();
   }

   public void func_75249_e() {
      this.theEntity.func_70661_as().func_75492_a(this.movePosX, this.movePosY, this.movePosZ, this.movementSpeed);
   }

   public void func_75251_c() {
      super.func_75251_c();
      if (this.theEntity.func_110173_bK()) {
         this.theEntity.standStill();
      }

   }
}
