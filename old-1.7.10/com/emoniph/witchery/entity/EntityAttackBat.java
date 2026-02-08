package com.emoniph.witchery.entity;

import com.emoniph.witchery.infusion.infusions.InfusionOtherwhere;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.mojang.authlib.GameProfile;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class EntityAttackBat extends EntityBat {
   private EntityPlayer ownerPlayer;
   private GameProfile owner;

   public EntityAttackBat(World world) {
      super(world);
   }

   protected void func_85033_bc() {
      List list = this.field_70170_p.func_72839_b(this, this.field_70121_D.func_72314_b(0.20000000298023224D, 0.0D, 0.20000000298023224D));
      if (list != null && !list.isEmpty()) {
         for(int i = 0; i < list.size(); ++i) {
            Entity entity = (Entity)list.get(i);
            if (entity.func_70104_M()) {
               this.func_82167_n(entity);
            }
         }
      }

   }

   protected void func_82167_n(Entity entity) {
      if (!this.field_70170_p.field_72995_K && entity instanceof EntityLivingBase) {
         EntityLivingBase target = (EntityLivingBase)entity;
         if (this.ownerPlayer == null) {
            this.ownerPlayer = this.getOwner();
         }

         if (target != this.ownerPlayer && !(target instanceof EntityBat) && !target.field_70128_L) {
            target.func_70097_a(DamageSource.func_76354_b(this, this.ownerPlayer), 4.0F);
            ParticleEffect.REDDUST.send(SoundEffect.WITCHERY_RANDOM_DRINK, entity, (double)entity.field_70130_N, (double)entity.field_70131_O, 16);
            this.func_70106_y();
         }
      }

   }

   protected void func_70619_bc() {
      if (!this.field_70170_p.field_72995_K) {
         boolean done = false;
         if (this.field_70173_aa > 300) {
            ParticleEffect.SMOKE.send(SoundEffect.NONE, this, (double)this.field_70130_N, (double)this.field_70131_O, 16);
            this.func_70106_y();
         } else {
            if (this.ownerPlayer == null) {
               this.ownerPlayer = this.getOwner();
            }

            if (this.ownerPlayer != null && this.ownerPlayer.field_71093_bK == this.field_71093_bK) {
               MovingObjectPosition mop = InfusionOtherwhere.raytraceEntities(this.field_70170_p, this.ownerPlayer, true, 32.0D);
               if (mop != null && mop.field_72313_a == MovingObjectType.ENTITY && mop.field_72308_g instanceof EntityLivingBase && !(mop.field_72308_g instanceof EntityBat)) {
                  double d0 = mop.field_72308_g.field_70165_t - this.field_70165_t;
                  double d1 = mop.field_72308_g.field_70163_u - this.field_70163_u;
                  double d2 = mop.field_72308_g.field_70161_v - this.field_70161_v;
                  double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                  d3 = (double)MathHelper.func_76133_a(d3);
                  if (this.isCourseTraversable(mop.field_72308_g.field_70165_t, mop.field_72308_g.field_70163_u, mop.field_72308_g.field_70161_v, d3)) {
                     this.field_70159_w += d0 / d3 * 0.1D;
                     this.field_70181_x += d1 / d3 * 0.1D;
                     this.field_70179_y += d2 / d3 * 0.1D;
                     float f = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0D / 3.141592653589793D) - 90.0F;
                     float f1 = MathHelper.func_76142_g(f - this.field_70177_z);
                     this.field_70701_bs = 0.5F;
                     this.field_70177_z += f1;
                     done = true;
                  }

                  this.field_70761_aq = this.field_70177_z = -((float)Math.atan2(this.field_70159_w, this.field_70179_y)) * 180.0F / 3.1415927F;
               }
            }
         }

         if (!done) {
            super.func_70619_bc();
         }
      }

   }

   private boolean isCourseTraversable(double par1, double par3, double par5, double par7) {
      double d4 = (par1 - this.field_70165_t) / par7;
      double d5 = (par3 - this.field_70163_u) / par7;
      double d6 = (par5 - this.field_70161_v) / par7;
      AxisAlignedBB axisalignedbb = this.field_70121_D.func_72329_c();

      for(int i = 1; (double)i < par7; ++i) {
         axisalignedbb.func_72317_d(d4, d5, d6);
         if (!this.field_70170_p.func_72945_a(this, axisalignedbb).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public void setOwner(EntityPlayer player) {
      this.owner = player.func_146103_bH();
   }

   public EntityPlayer getOwner() {
      return this.owner != null ? this.field_70170_p.func_152378_a(this.owner.getId()) : null;
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      if (this.owner != null) {
         NBTTagCompound nbttagcompound1 = new NBTTagCompound();
         NBTUtil.func_152460_a(nbttagcompound1, this.owner);
         nbtRoot.func_74782_a("Owner", nbttagcompound1);
      }

   }

   public void func_70020_e(NBTTagCompound nbtRoot) {
      super.func_70020_e(nbtRoot);
      if (nbtRoot.func_150297_b("Owner", 10)) {
         this.owner = NBTUtil.func_152459_a(nbtRoot.func_74775_l("Owner"));
      }

   }
}
