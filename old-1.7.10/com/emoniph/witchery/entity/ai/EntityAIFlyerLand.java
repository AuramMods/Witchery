package com.emoniph.witchery.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAIFlyerLand extends EntityAIBase {
   private double speed;
   int[] target;
   World worldObj;
   public int courseChangeCooldown;
   public double waypointX;
   public double waypointY;
   public double waypointZ;
   public boolean findTrees;
   EntityLiving living;

   public EntityAIFlyerLand(EntityLiving par1EntityCreature, double par2, boolean findTrees) {
      this.living = par1EntityCreature;
      this.worldObj = this.living.field_70170_p;
      this.speed = par2;
      this.findTrees = findTrees;
      this.func_75248_a(1);
   }

   public boolean func_75250_a() {
      return !this.isLanded() && !this.liquidBelow((int)this.living.field_70163_u - 1) && !this.liquidBelow((int)this.living.field_70163_u) && this.worldObj.field_73012_v.nextInt(20) == 0;
   }

   private boolean liquidBelow(int y) {
      return this.worldObj.func_147439_a(MathHelper.func_76128_c(this.living.field_70165_t), y, MathHelper.func_76128_c(this.living.field_70161_v)).func_149688_o().func_76224_d();
   }

   public boolean func_75253_b() {
      boolean cont = !this.isLanded() && !this.liquidBelow((int)this.living.field_70163_u - 1) && !this.liquidBelow((int)this.living.field_70163_u);
      return cont;
   }

   public void func_75249_e() {
      this.courseChangeCooldown = 100;
      int x0 = MathHelper.func_76128_c(this.living.field_70165_t);
      int y0 = MathHelper.func_76128_c(this.living.field_70163_u);
      int z0 = MathHelper.func_76128_c(this.living.field_70161_v);
      this.target = this.findTrees ? this.findTreeTop(x0, y0, z0) : null;
      if (this.target == null) {
         this.target = this.findGround(x0, y0, z0);
      }

      if (this.target != null) {
      }

   }

   public void func_75251_c() {
      this.target = null;
      super.func_75251_c();
   }

   private int[] findTreeTop(int x0, int y0, int z0) {
      int RADIUS = true;
      int Y_RADIUS = true;

      for(int y = Math.max(y0 - 3, 1); y <= y0 + 3; ++y) {
         for(int x = x0 - 16; x <= x0 + 16; ++x) {
            for(int z = z0 - 16; z <= z0 + 16; ++z) {
               Block blockID = this.worldObj.func_147439_a(x, y, z);
               if (blockID.func_149688_o() == Material.field_151584_j) {
                  for(int y2 = y; y2 < y0 + 10; ++y2) {
                     if (this.worldObj.func_147437_c(x, y2, z)) {
                        double d0 = (double)x - this.living.field_70165_t;
                        double d1 = (double)y2 - this.living.field_70163_u;
                        double d2 = (double)z - this.living.field_70161_v;
                        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                        d3 = (double)MathHelper.func_76133_a(d3);
                        if (this.isCourseTraversable((double)x, (double)y2, (double)z, d3)) {
                           return new int[]{x, y2 + 2, z};
                        }
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private int[] findGround(int x0, int y0, int z0) {
      for(int y = y0; y > 1; --y) {
         Material material = this.worldObj.func_147439_a(x0, y, z0).func_149688_o();
         if (material != Material.field_151579_a) {
            if (!material.func_76224_d()) {
               return new int[]{x0, y + 1, z0};
            }

            for(int i = 0; i < 10; ++i) {
               int j = MathHelper.func_76128_c(this.living.field_70165_t + (double)this.worldObj.field_73012_v.nextInt(20) - 10.0D);
               int k = MathHelper.func_76128_c(this.living.field_70121_D.field_72338_b + (double)this.worldObj.field_73012_v.nextInt(6) - 3.0D);
               int l = MathHelper.func_76128_c(this.living.field_70161_v + (double)this.worldObj.field_73012_v.nextInt(20) - 10.0D);
               Block blockID = this.worldObj.func_147439_a(j, k, l);
               double d0 = (double)j - this.living.field_70165_t;
               double d1 = (double)k - this.living.field_70163_u;
               double d2 = (double)l - this.living.field_70161_v;
               double d3 = d0 * d0 + d1 * d1 + d2 * d2;
               d3 = (double)MathHelper.func_76133_a(d3);
               if ((blockID.func_149688_o() == Material.field_151584_j || blockID.func_149688_o().func_76220_a()) && this.worldObj.func_147437_c(j, k + 1, l) && this.isCourseTraversable((double)j, (double)k, (double)l, d3)) {
                  return new int[]{j, k + 1, l};
               }
            }
         }
      }

      return null;
   }

   public void func_75246_d() {
      if (!this.isLanded()) {
         if (this.target != null && this.living.func_70092_e((double)this.target[0], this.living.field_70163_u, (double)this.target[2]) > 1.0D && this.courseChangeCooldown-- > 0) {
            double d0 = (double)this.target[0] - this.living.field_70165_t;
            double d1 = (double)this.target[1] - this.living.field_70163_u;
            double d2 = (double)this.target[2] - this.living.field_70161_v;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            d3 = (double)MathHelper.func_76133_a(d3);
            if (this.isCourseTraversable((double)this.target[0], (double)this.target[1], (double)this.target[2], d3)) {
               EntityLiving var10000 = this.living;
               var10000.field_70159_w += d0 / d3 * 0.05D;
               var10000 = this.living;
               var10000.field_70181_x += d1 / d3 * 0.05D;
               var10000 = this.living;
               var10000.field_70179_y += d2 / d3 * 0.05D;
            }
         } else if (!this.liquidBelow((int)(this.living.field_70163_u - 1.0D))) {
            this.living.field_70181_x = -0.1D;
         }

         this.living.field_70761_aq = this.living.field_70177_z = -((float)Math.atan2(this.living.field_70159_w, this.living.field_70179_y)) * 180.0F / 3.1415927F;
      }

      this.living.field_70761_aq = this.living.field_70177_z = -((float)Math.atan2(this.living.field_70159_w, this.living.field_70179_y)) * 180.0F / 3.1415927F;
   }

   private boolean isLanded() {
      Block blockID = this.worldObj.func_147439_a(MathHelper.func_76128_c(this.living.field_70165_t), (int)(this.living.field_70163_u - 0.01D), MathHelper.func_76128_c(this.living.field_70161_v));
      Material material = blockID.func_149688_o();
      return material == Material.field_151584_j || material.func_76220_a();
   }

   private boolean isCourseTraversable(double par1, double par3, double par5, double par7) {
      double d4 = (par1 - this.living.field_70165_t) / par7;
      double d5 = (par3 - this.living.field_70163_u) / par7;
      double d6 = (par5 - this.living.field_70161_v) / par7;
      AxisAlignedBB axisalignedbb = this.living.field_70121_D.func_72329_c();

      for(int i = 1; (double)i < par7; ++i) {
         axisalignedbb.func_72317_d(d4, d5, d6);
         if (!this.worldObj.func_72945_a(this.living, axisalignedbb).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}
