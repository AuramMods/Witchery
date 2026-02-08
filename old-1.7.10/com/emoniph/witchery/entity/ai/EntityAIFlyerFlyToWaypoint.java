package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockVoidBramble;
import com.emoniph.witchery.brewing.EntityBrew;
import com.emoniph.witchery.brewing.WitcheryBrewRegistry;
import com.emoniph.witchery.entity.EntityFlyingTameable;
import com.emoniph.witchery.entity.EntityWitchProjectile;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.util.TimeUtil;
import com.emoniph.witchery.util.Waypoint;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

public class EntityAIFlyerFlyToWaypoint extends EntityAIBase {
   private EntityFlyingTameable flyer;
   private EntityAIFlyerFlyToWaypoint.CarryRequirement carryRequirement;
   private static final double HIT_RADIUS = 1.0D;
   private static final double HIT_RADIUS_SQ = 1.0D;
   int courseTimer = 0;

   public EntityAIFlyerFlyToWaypoint(EntityFlyingTameable flyer, EntityAIFlyerFlyToWaypoint.CarryRequirement carryRestrictions) {
      this.flyer = flyer;
      this.carryRequirement = carryRestrictions;
      this.func_75248_a(7);
   }

   public boolean func_75250_a() {
      return this.flyer.waypoint != null && (this.flyer.func_70694_bm() != null || this.carryRequirement != EntityAIFlyerFlyToWaypoint.CarryRequirement.HELD_ITEM);
   }

   public boolean func_75253_b() {
      boolean heldItem = this.flyer.func_70694_bm() != null;
      boolean awayFromHome = this.flyer.func_70092_e(this.flyer.homeX, this.flyer.field_70163_u, this.flyer.homeZ) > 1.0D || Math.abs(this.flyer.field_70163_u - this.flyer.homeY) > 1.0D;
      return heldItem && this.carryRequirement == EntityAIFlyerFlyToWaypoint.CarryRequirement.HELD_ITEM || this.flyer.waypoint != null || awayFromHome;
   }

   public void func_75249_e() {
   }

   public void func_75251_c() {
      this.flyer.waypoint = null;
      this.flyer.func_70904_g(true);
      if (this.flyer.field_70153_n != null) {
         this.flyer.field_70153_n.func_70078_a((Entity)null);
      }

      this.courseTimer = 0;
   }

   public void func_75246_d() {
      if (!this.flyer.func_70906_o()) {
         Waypoint waypoint = this.flyer.getWaypoint();
         if (this.carryRequirement == EntityAIFlyerFlyToWaypoint.CarryRequirement.ENTITY_LIVING) {
            if (this.flyer.func_70092_e(waypoint.X, waypoint.Y, waypoint.Z) <= 1.0D) {
               List<EntityLivingBase> entities = this.flyer.field_70170_p.func_72872_a(EntityLivingBase.class, this.flyer.field_70121_D.func_72314_b(1.0D, 1.0D, 1.0D));
               if (entities != null && entities.size() > 1) {
                  if (!this.flyer.field_70170_p.field_72995_K) {
                     Iterator i$ = entities.iterator();

                     while(i$.hasNext()) {
                        EntityLivingBase entity = (EntityLivingBase)i$.next();
                        if (entity != this.flyer) {
                           entity.func_70078_a(this.flyer);
                        }
                     }
                  }

                  this.flyer.waypoint = null;
                  waypoint = this.flyer.getWaypoint();
               }
            }
         } else if (this.flyer.func_70694_bm() != null && this.flyer.func_70092_e(waypoint.X, waypoint.Y, waypoint.Z) <= 1.0D) {
            if (!this.flyer.field_70170_p.field_72995_K) {
               ItemStack stack = this.flyer.func_70694_bm();
               this.flyer.func_70062_b(0, (ItemStack)null);
               if (Witchery.Items.GENERIC.isBrew(stack)) {
                  this.flyer.field_70170_p.func_72956_a(this.flyer, "random.bow", 0.5F, 0.4F / (this.flyer.field_70170_p.field_73012_v.nextFloat() * 0.4F + 0.8F));
                  EntityWitchProjectile projectile = new EntityWitchProjectile(this.flyer.field_70170_p, this.flyer, (ItemGeneral.SubItem)Witchery.Items.GENERIC.subItems.get(stack.func_77960_j()));
                  projectile.field_70159_w = 0.0D;
                  projectile.field_70179_y = 0.0D;
                  this.flyer.field_70170_p.func_72838_d(projectile);
               } else if (Witchery.Items.BREW == stack.func_77973_b() && WitcheryBrewRegistry.INSTANCE.isSplash(stack.func_77978_p())) {
                  this.flyer.field_70170_p.func_72956_a(this.flyer, "random.bow", 0.5F, 0.4F / (this.flyer.field_70170_p.field_73012_v.nextFloat() * 0.4F + 0.8F));
                  EntityBrew projectile = new EntityBrew(this.flyer.field_70170_p, this.flyer, stack, false);
                  projectile.field_70159_w = 0.0D;
                  projectile.field_70179_y = 0.0D;
                  this.flyer.field_70170_p.func_72838_d(projectile);
               } else if (stack.func_77973_b() == Items.field_151068_bn && ItemPotion.func_77831_g(stack.func_77960_j())) {
                  this.flyer.field_70170_p.func_72956_a(this.flyer, "random.bow", 0.5F, 0.4F / (this.flyer.field_70170_p.field_73012_v.nextFloat() * 0.4F + 0.8F));
                  EntityPotion projectile = new EntityPotion(this.flyer.field_70170_p, this.flyer, stack);
                  projectile.field_70159_w = 0.0D;
                  projectile.field_70179_y = 0.0D;
                  this.flyer.field_70170_p.func_72838_d(projectile);
               } else {
                  EntityItem item = new EntityItem(this.flyer.field_70170_p, this.flyer.field_70165_t, this.flyer.field_70163_u, this.flyer.field_70161_v, stack);
                  if (stack.func_77973_b() == Witchery.Items.SEEDS_MINDRAKE) {
                     item.lifespan = TimeUtil.secsToTicks(3);
                  }

                  this.flyer.field_70170_p.func_72838_d(item);
               }
            }

            this.flyer.waypoint = null;
            waypoint = this.flyer.getWaypoint();
         }

         double dX = waypoint.X - this.flyer.field_70165_t;
         double dY = waypoint.Y - this.flyer.field_70163_u;
         double dZ = waypoint.Z - this.flyer.field_70161_v;
         double trajectory = dX * dX + dY * dY + dZ * dZ;
         trajectory = (double)MathHelper.func_76133_a(trajectory);
         if (trajectory >= 128.0D && this.carryRequirement == EntityAIFlyerFlyToWaypoint.CarryRequirement.HELD_ITEM) {
            BlockVoidBramble.teleportRandomly(this.flyer.field_70170_p, (int)waypoint.X, (int)waypoint.Y, (int)waypoint.Z, this.flyer, 16);
         }

         if (--this.courseTimer < 0) {
            this.courseTimer = 0;
         }

         if (this.courseTimer == 0) {
            double newX;
            if (!this.isCourseTraversable(waypoint.X, waypoint.Y, waypoint.Z, trajectory)) {
               newX = this.flyer.field_70165_t + (this.flyer.field_70170_p.field_73012_v.nextDouble() * 4.0D - 2.0D) * 6.0D;
               double newY = this.flyer.field_70163_u + (this.flyer.field_70170_p.field_73012_v.nextDouble() * 2.0D - 1.0D) * 4.0D;
               double newZ = this.flyer.field_70161_v + (this.flyer.field_70170_p.field_73012_v.nextDouble() * 4.0D - 2.0D) * 6.0D;
               if (this.flyer.field_70170_p.field_73012_v.nextInt(2) != 0) {
                  dX = newX - this.flyer.field_70165_t;
                  dZ = newZ - this.flyer.field_70161_v;
               }

               if (!(this.flyer.func_70092_e(waypoint.X, waypoint.Y, waypoint.Z) <= 1.0D)) {
                  dY = newY - this.flyer.field_70163_u;
               } else {
                  dY = (this.flyer.field_70163_u > waypoint.Y && newY > 0.0D ? -newY : newY) - this.flyer.field_70163_u;
               }

               trajectory = dX * dX + dY * dY + dZ * dZ;
               trajectory = (double)MathHelper.func_76133_a(trajectory);
            }

            newX = 0.2D;
            EntityFlyingTameable var10000 = this.flyer;
            var10000.field_70159_w += dX / trajectory * 0.2D;
            var10000 = this.flyer;
            var10000.field_70179_y += dZ / trajectory * 0.2D;
            var10000 = this.flyer;
            var10000.field_70181_x += dY / trajectory * 0.2D + (this.flyer.field_70163_u < Math.min(waypoint.Y + (double)(this.carryRequirement == EntityAIFlyerFlyToWaypoint.CarryRequirement.HELD_ITEM ? 32 : 32), 255.0D) ? 0.1D : 0.0D);
            this.courseTimer = 10;
         }

         this.flyer.field_70761_aq = this.flyer.field_70177_z = -((float)Math.atan2(this.flyer.field_70159_w, this.flyer.field_70179_y)) * 180.0F / 3.1415927F;
      }

   }

   private boolean isCourseTraversable(double par1, double par3, double par5, double par7) {
      double d4 = (par1 - this.flyer.field_70165_t) / par7;
      double d5 = (par3 - this.flyer.field_70163_u) / par7;
      double d6 = (par5 - this.flyer.field_70161_v) / par7;
      AxisAlignedBB axisalignedbb = this.flyer.field_70121_D.func_72329_c();

      for(int i = 1; (double)i < par7; ++i) {
         axisalignedbb.func_72317_d(d4, d5, d6);
         if (!this.flyer.field_70170_p.func_72945_a(this.flyer, axisalignedbb).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public static enum CarryRequirement {
      NONE,
      HELD_ITEM,
      ENTITY_LIVING;
   }
}
