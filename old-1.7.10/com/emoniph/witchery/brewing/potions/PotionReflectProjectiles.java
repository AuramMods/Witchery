package com.emoniph.witchery.brewing.potions;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class PotionReflectProjectiles extends PotionBase {
   public PotionReflectProjectiles(int id, int color) {
      super(id, color);
   }

   public boolean func_76397_a(int duration, int amplifier) {
      return true;
   }

   public void func_76394_a(EntityLivingBase entity, int amplifier) {
      World world = entity.field_70170_p;
      double RADIUS = 2.0D;
      double RADIUS_SQ = 4.0D;
      AxisAlignedBB bounds = entity.field_70121_D.func_72314_b(2.0D, 2.0D, 2.0D);
      List<IProjectile> projectileList = world.func_72872_a(IProjectile.class, bounds);
      Iterator i$ = projectileList.iterator();

      while(true) {
         IProjectile projectile;
         boolean isArrow;
         label34:
         do {
            while(i$.hasNext()) {
               projectile = (IProjectile)i$.next();
               isArrow = false;
               if (projectile instanceof EntityArrow) {
                  EntityArrow arrow = (EntityArrow)projectile;
                  isArrow = true;
                  if (arrow.field_70250_c == entity) {
                     continue;
                  }
                  continue label34;
               } else {
                  if (!(projectile instanceof EntityThrowable)) {
                     continue label34;
                  }

                  EntityThrowable arrow = (EntityThrowable)projectile;
                  if (arrow.func_85052_h() != entity) {
                     continue label34;
                  }
               }
            }

            return;
         } while(!(projectile instanceof Entity));

         Entity projectileEntity = (Entity)projectile;
         projectileEntity.field_70159_w *= -0.25D * (1.0D + (double)amplifier);
         if (projectileEntity.field_70159_w > 0.0D || projectileEntity.field_70179_y > 0.0D) {
            projectileEntity.field_70181_x *= -0.25D * (1.0D + (double)amplifier);
         }

         projectileEntity.field_70179_y *= -0.25D * (1.0D + (double)amplifier);
         if (isArrow) {
         }
      }
   }
}
