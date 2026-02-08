package com.emoniph.witchery.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAIFlyerAttackOnCollide extends EntityAIBase {
   World worldObj;
   EntityCreature attacker;
   int attackTick;
   double speedTowardsTarget;
   boolean longMemory;
   PathEntity entityPathEntity;
   Class classTarget;
   private int field_75445_i;
   private int failedPathFindingPenalty;

   public EntityAIFlyerAttackOnCollide(EntityCreature par1EntityCreature, Class par2Class, double par3, boolean par5) {
      this(par1EntityCreature, par3, par5);
      this.classTarget = par2Class;
   }

   public EntityAIFlyerAttackOnCollide(EntityCreature par1EntityCreature, double par2, boolean par4) {
      this.attacker = par1EntityCreature;
      this.worldObj = par1EntityCreature.field_70170_p;
      this.speedTowardsTarget = par2;
      this.longMemory = par4;
      this.func_75248_a(3);
   }

   public boolean func_75250_a() {
      EntityLivingBase entitylivingbase = this.attacker.func_70638_az();
      if (entitylivingbase == null) {
         return false;
      } else if (!entitylivingbase.func_70089_S()) {
         return false;
      } else {
         return this.classTarget == null || this.classTarget.isAssignableFrom(entitylivingbase.getClass());
      }
   }

   public boolean func_75253_b() {
      EntityLivingBase entitylivingbase = this.attacker.func_70638_az();
      return entitylivingbase == null ? false : (!entitylivingbase.func_70089_S() ? false : (!this.longMemory ? !this.attacker.func_70661_as().func_75500_f() : this.attacker.func_110176_b(MathHelper.func_76128_c(entitylivingbase.field_70165_t), MathHelper.func_76128_c(entitylivingbase.field_70163_u), MathHelper.func_76128_c(entitylivingbase.field_70161_v))));
   }

   public void func_75249_e() {
      this.attacker.func_70661_as().func_75484_a(this.entityPathEntity, this.speedTowardsTarget);
      this.field_75445_i = 0;
   }

   public void func_75251_c() {
      this.attacker.func_70661_as().func_75499_g();
   }

   public void func_75246_d() {
      EntityLivingBase entitylivingbase = this.attacker.func_70638_az();
      this.attacker.func_70671_ap().func_75651_a(entitylivingbase, 30.0F, 30.0F);
      double d0;
      if ((this.longMemory || this.attacker.func_70635_at().func_75522_a(entitylivingbase)) && --this.field_75445_i <= 0) {
         this.field_75445_i = this.failedPathFindingPenalty + 4 + this.attacker.func_70681_au().nextInt(7);
         d0 = entitylivingbase.field_70165_t - this.attacker.field_70165_t;
         double d1 = entitylivingbase.field_70163_u - this.attacker.field_70163_u;
         double d2 = entitylivingbase.field_70161_v - this.attacker.field_70161_v;
         double d3 = d0 * d0 + d1 * d1 + d2 * d2;
         d3 = (double)MathHelper.func_76133_a(d3);
         if (this.isCourseTraversable(entitylivingbase.field_70165_t, entitylivingbase.field_70163_u, entitylivingbase.field_70161_v, d3)) {
            EntityCreature var10000 = this.attacker;
            var10000.field_70159_w += d0 / d3 * 0.15D;
            var10000 = this.attacker;
            var10000.field_70181_x += d1 / d3 * 0.15D;
            var10000 = this.attacker;
            var10000.field_70179_y += d2 / d3 * 0.15D;
            this.failedPathFindingPenalty = 0;
         } else {
            this.failedPathFindingPenalty += 10;
         }

         this.attacker.field_70761_aq = this.attacker.field_70177_z = -((float)Math.atan2(this.attacker.field_70159_w, this.attacker.field_70179_y)) * 180.0F / 3.1415927F;
      }

      this.attackTick = Math.max(this.attackTick - 1, 0);
      d0 = (double)(this.attacker.field_70130_N * 2.0F * this.attacker.field_70130_N * 2.0F + entitylivingbase.field_70130_N);
      if (this.attacker.func_70092_e(entitylivingbase.field_70165_t, entitylivingbase.field_70121_D.field_72338_b, entitylivingbase.field_70161_v) <= d0 && this.attackTick <= 0) {
         this.attackTick = 20;
         if (this.attacker.func_70694_bm() != null) {
            this.attacker.func_71038_i();
         }

         this.attacker.func_70652_k(entitylivingbase);
      }

   }

   private boolean isCourseTraversable(double par1, double par3, double par5, double par7) {
      double d4 = (par1 - this.attacker.field_70165_t) / par7;
      double d5 = (par3 - this.attacker.field_70163_u) / par7;
      double d6 = (par5 - this.attacker.field_70161_v) / par7;
      AxisAlignedBB axisalignedbb = this.attacker.field_70121_D.func_72329_c();

      for(int i = 1; (double)i < par7; ++i) {
         axisalignedbb.func_72317_d(d4, d5, d6);
         if (!this.attacker.field_70170_p.func_72945_a(this.attacker, axisalignedbb).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}
