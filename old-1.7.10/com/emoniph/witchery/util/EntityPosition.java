package com.emoniph.witchery.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class EntityPosition {
   public final double x;
   public final double y;
   public final double z;

   public EntityPosition(int x, int y, int z) {
      this(0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z);
   }

   public EntityPosition(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public EntityPosition(BlockPosition position) {
      this.x = (double)position.x;
      this.y = (double)position.y;
      this.z = (double)position.z;
   }

   public EntityPosition(Entity entity) {
      this(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v);
   }

   public EntityPosition(MovingObjectPosition mop) {
      if (mop.field_72313_a == MovingObjectType.ENTITY) {
         this.x = mop.field_72308_g.field_70165_t;
         this.y = mop.field_72308_g.field_70163_u;
         this.z = mop.field_72308_g.field_70161_v;
      } else if (mop.field_72313_a == MovingObjectType.BLOCK) {
         this.x = (double)mop.field_72311_b + 0.5D;
         this.y = (double)mop.field_72312_c + 0.5D;
         this.z = (double)mop.field_72309_d + 0.5D;
      } else {
         this.x = 0.0D;
         this.y = 0.0D;
         this.z = 0.0D;
      }

   }

   public double getDistanceSqToEntity(Entity entity) {
      double d0 = this.x - entity.field_70165_t;
      double d1 = this.y - entity.field_70163_u;
      double d2 = this.z - entity.field_70161_v;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public AxisAlignedBB getBounds(double radius) {
      AxisAlignedBB aabb = AxisAlignedBB.func_72330_a(this.x - radius, this.y - radius, this.z - radius, this.x + radius, this.y + radius, this.z + radius);
      return aabb;
   }

   public boolean occupiedBy(Entity entity) {
      return entity != null && entity.field_70165_t == this.x && entity.field_70163_u == this.y && entity.field_70161_v == this.z;
   }
}
