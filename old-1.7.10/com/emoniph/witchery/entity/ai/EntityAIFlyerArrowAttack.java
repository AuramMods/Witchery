package com.emoniph.witchery.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

public class EntityAIFlyerArrowAttack extends EntityAIBase {
   private final EntityLiving entityHost;
   private final IRangedAttackMob rangedAttackEntityHost;
   private EntityLivingBase attackTarget;
   private int rangedAttackTime;
   private double entityMoveSpeed;
   private int field_75318_f;
   private int field_96561_g;
   private int maxRangedAttackTime;
   private float field_96562_i;
   private float field_82642_h;
   private static final String __OBFID = "CL_00001609";
   private int field_75445_i;
   private int failedPathFindingPenalty;

   public EntityAIFlyerArrowAttack(IRangedAttackMob par1IRangedAttackMob, double par2, int par4, float par5) {
      this(par1IRangedAttackMob, par2, par4, par4, par5);
   }

   public EntityAIFlyerArrowAttack(IRangedAttackMob par1IRangedAttackMob, double par2, int par4, int par5, float par6) {
      this.rangedAttackTime = -1;
      if (!(par1IRangedAttackMob instanceof EntityLivingBase)) {
         throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
      } else {
         this.rangedAttackEntityHost = par1IRangedAttackMob;
         this.entityHost = (EntityLiving)par1IRangedAttackMob;
         this.entityMoveSpeed = par2;
         this.field_96561_g = par4;
         this.maxRangedAttackTime = par5;
         this.field_96562_i = par6;
         this.field_82642_h = par6 * par6;
         this.func_75248_a(3);
      }
   }

   public boolean func_75250_a() {
      EntityLivingBase entitylivingbase = this.entityHost.func_70638_az();
      if (entitylivingbase == null) {
         return false;
      } else {
         this.attackTarget = entitylivingbase;
         return true;
      }
   }

   public boolean func_75253_b() {
      return this.func_75250_a() || !this.entityHost.func_70661_as().func_75500_f();
   }

   public void func_75251_c() {
      this.attackTarget = null;
      this.field_75318_f = 0;
      this.rangedAttackTime = -1;
      this.field_75445_i = 0;
   }

   public void func_75246_d() {
      double d0 = this.entityHost.func_70092_e(this.attackTarget.field_70165_t, this.attackTarget.field_70121_D.field_72338_b, this.attackTarget.field_70161_v);
      boolean flag = this.entityHost.func_70635_at().func_75522_a(this.attackTarget);
      if (flag) {
         ++this.field_75318_f;
      } else {
         this.field_75318_f = 0;
      }

      if (!(d0 <= (double)this.field_82642_h) && --this.field_75445_i <= 0) {
         this.field_75445_i = this.failedPathFindingPenalty + 4 + this.entityHost.func_70681_au().nextInt(7);
         double d = this.attackTarget.field_70165_t - this.entityHost.field_70165_t;
         double d1 = this.attackTarget.field_70163_u - this.entityHost.field_70163_u;
         double d2 = this.attackTarget.field_70161_v - this.entityHost.field_70161_v;
         double d3 = d * d + d1 * d1 + d2 * d2;
         d3 = (double)MathHelper.func_76133_a(d3);
         if (this.isCourseTraversable(this.attackTarget.field_70165_t, this.attackTarget.field_70163_u, this.attackTarget.field_70161_v, d3)) {
            EntityLiving var10000 = this.entityHost;
            var10000.field_70159_w += d / d3 * 0.15D;
            var10000 = this.entityHost;
            var10000.field_70181_x += d1 / d3 * 0.15D;
            var10000 = this.entityHost;
            var10000.field_70179_y += d2 / d3 * 0.15D;
            this.failedPathFindingPenalty = 0;
         } else {
            this.failedPathFindingPenalty += 10;
         }

         this.entityHost.field_70761_aq = this.entityHost.field_70177_z = -((float)Math.atan2(this.entityHost.field_70159_w, this.entityHost.field_70179_y)) * 180.0F / 3.1415927F;
         this.entityHost.func_70661_as().func_75497_a(this.attackTarget, this.entityMoveSpeed);
      }

      this.entityHost.func_70671_ap().func_75651_a(this.attackTarget, 30.0F, 30.0F);
      float f;
      if (--this.rangedAttackTime == 0) {
         if (d0 > (double)this.field_82642_h || !flag) {
            return;
         }

         f = MathHelper.func_76133_a(d0) / this.field_96562_i;
         float f1 = f;
         if (f < 0.1F) {
            f1 = 0.1F;
         }

         if (f1 > 1.0F) {
            f1 = 1.0F;
         }

         this.rangedAttackEntityHost.func_82196_d(this.attackTarget, f1);
         this.rangedAttackTime = MathHelper.func_76141_d(f * (float)(this.maxRangedAttackTime - this.field_96561_g) + (float)this.field_96561_g);
      } else if (this.rangedAttackTime < 0) {
         f = MathHelper.func_76133_a(d0) / this.field_96562_i;
         this.rangedAttackTime = MathHelper.func_76141_d(f * (float)(this.maxRangedAttackTime - this.field_96561_g) + (float)this.field_96561_g);
      }

   }

   private boolean isCourseTraversable(double par1, double par3, double par5, double par7) {
      double d4 = (par1 - this.attackTarget.field_70165_t) / par7;
      double d5 = (par3 - this.attackTarget.field_70163_u) / par7;
      double d6 = (par5 - this.attackTarget.field_70161_v) / par7;
      AxisAlignedBB axisalignedbb = this.attackTarget.field_70121_D.func_72329_c();

      for(int i = 1; (double)i < par7; ++i) {
         axisalignedbb.func_72317_d(d4, d5, d6);
         if (!this.attackTarget.field_70170_p.func_72945_a(this.attackTarget, axisalignedbb).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}
