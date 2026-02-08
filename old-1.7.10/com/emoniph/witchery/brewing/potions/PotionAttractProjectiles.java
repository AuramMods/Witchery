package com.emoniph.witchery.brewing.potions;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class PotionAttractProjectiles extends PotionBase {
   public PotionAttractProjectiles(int id, int color) {
      super(id, true, color);
   }

   public boolean func_76397_a(int duration, int amplifier) {
      return true;
   }

   public void func_76394_a(EntityLivingBase target, int amplifier) {
      World world = target.field_70170_p;
      double RADIUS = (1.0D + (double)amplifier) * 3.0D;
      double RADIUS_SQ = RADIUS * RADIUS;
      AxisAlignedBB bounds = target.field_70121_D.func_72314_b(RADIUS, RADIUS, RADIUS);
      List<IProjectile> projectileList = world.func_72872_a(IProjectile.class, bounds);
      Iterator i$ = projectileList.iterator();

      while(i$.hasNext()) {
         IProjectile projectile = (IProjectile)i$.next();
         if (projectile instanceof Entity) {
            Entity arrow = (Entity)projectile;
            double velocitySq = arrow.field_70159_w * arrow.field_70159_w + arrow.field_70181_x * arrow.field_70181_x + arrow.field_70179_y * arrow.field_70179_y;
            double FAST_SQ = 0.25D;
            if (arrow.field_70173_aa >= (velocitySq > 0.25D ? 1 : 10)) {
               double d0 = target.field_70165_t - arrow.field_70165_t;
               double d1 = target.field_70121_D.field_72338_b + (double)target.field_70131_O * 0.75D - arrow.field_70163_u;
               double d2 = target.field_70161_v - arrow.field_70161_v;
               double d3 = (double)MathHelper.func_76133_a(d0 * d0 + d2 * d2);
               if (d3 >= 1.0E-7D) {
                  projectile.func_70186_c(d0, d1, d2, 1.0F, 1.0F);
               }
            }
         }
      }

   }
}
