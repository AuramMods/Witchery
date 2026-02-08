package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.BoltDamageSource;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.TargetPointUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityBolt extends Entity {
   private int xTile = -1;
   private int yTile = -1;
   private int zTile = -1;
   private Block inTile;
   private int inData;
   private boolean inGround;
   public int canBePickedUp;
   public int arrowShake;
   public Entity shootingEntity;
   private int ticksInGround;
   private int ticksInAir;
   private double damage = 2.0D;
   private int knockbackStrength;

   public EntityBolt(World par1World) {
      super(par1World);
      this.field_70155_l = 10.0D;
      this.func_70105_a(0.5F, 0.5F);
   }

   public EntityBolt(World par1World, double par2, double par4, double par6) {
      super(par1World);
      this.field_70155_l = 10.0D;
      this.func_70105_a(0.5F, 0.5F);
      this.func_70107_b(par2, par4, par6);
      this.field_70129_M = 0.0F;
   }

   public EntityBolt(World par1World, EntityLivingBase par2EntityLivingBase, EntityLivingBase par3EntityLivingBase, float par4, float par5) {
      super(par1World);
      this.field_70155_l = 10.0D;
      this.shootingEntity = par2EntityLivingBase;
      if (par2EntityLivingBase instanceof EntityPlayer) {
         this.canBePickedUp = 1;
      }

      this.field_70163_u = par2EntityLivingBase.field_70163_u + (double)par2EntityLivingBase.func_70047_e() - 0.10000000149011612D;
      double d0 = par3EntityLivingBase.field_70165_t - par2EntityLivingBase.field_70165_t;
      double d1 = par3EntityLivingBase.field_70121_D.field_72338_b + (double)(par3EntityLivingBase.field_70131_O / 3.0F) - this.field_70163_u;
      double d2 = par3EntityLivingBase.field_70161_v - par2EntityLivingBase.field_70161_v;
      double d3 = (double)MathHelper.func_76133_a(d0 * d0 + d2 * d2);
      if (d3 >= 1.0E-7D) {
         float f2 = (float)(Math.atan2(d2, d0) * 180.0D / 3.141592653589793D) - 90.0F;
         float f3 = (float)(-(Math.atan2(d1, d3) * 180.0D / 3.141592653589793D));
         double d4 = d0 / d3;
         double d5 = d2 / d3;
         this.func_70012_b(par2EntityLivingBase.field_70165_t + d4, this.field_70163_u, par2EntityLivingBase.field_70161_v + d5, f2, f3);
         this.field_70129_M = 0.0F;
         float f4 = (float)d3 * 0.2F;
         this.setThrowableHeading(d0, d1 + (double)f4, d2, par4, par5);
      }

   }

   public EntityBolt(World par1World, EntityLivingBase par2EntityLivingBase, float par3, float arcStart) {
      super(par1World);
      this.field_70155_l = 10.0D;
      this.shootingEntity = par2EntityLivingBase;
      if (par2EntityLivingBase instanceof EntityPlayer) {
         this.canBePickedUp = 1;
      }

      this.func_70105_a(0.5F, 0.5F);
      this.func_70012_b(par2EntityLivingBase.field_70165_t, par2EntityLivingBase.field_70163_u + (double)par2EntityLivingBase.func_70047_e(), par2EntityLivingBase.field_70161_v, par2EntityLivingBase.field_70177_z, par2EntityLivingBase.field_70125_A);
      this.field_70177_z += arcStart;
      this.field_70165_t -= (double)(MathHelper.func_76134_b(this.field_70177_z / 180.0F * 3.1415927F) * 0.16F);
      this.field_70163_u -= 0.30000000149011613D;
      this.field_70161_v -= (double)(MathHelper.func_76126_a(this.field_70177_z / 180.0F * 3.1415927F) * 0.16F);
      this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
      this.field_70129_M = 0.0F;
      this.field_70159_w = (double)(-MathHelper.func_76126_a(this.field_70177_z / 180.0F * 3.1415927F) * MathHelper.func_76134_b(this.field_70125_A / 180.0F * 3.1415927F));
      this.field_70179_y = (double)(MathHelper.func_76134_b(this.field_70177_z / 180.0F * 3.1415927F) * MathHelper.func_76134_b(this.field_70125_A / 180.0F * 3.1415927F));
      this.field_70181_x = (double)(-MathHelper.func_76126_a(this.field_70125_A / 180.0F * 3.1415927F));
      this.setThrowableHeading(this.field_70159_w, this.field_70181_x, this.field_70179_y, par3 * 1.5F, 1.0F);
   }

   protected void func_70088_a() {
      this.field_70180_af.func_75682_a(15, "");
      this.field_70180_af.func_75682_a(16, (byte)0);
      this.field_70180_af.func_75682_a(17, (byte)0);
   }

   public void setShooter(EntityLivingBase entity) {
      if (!this.field_70170_p.field_72995_K && entity instanceof EntityPlayer) {
         this.field_70180_af.func_75692_b(15, ((EntityPlayer)entity).func_70005_c_());
      }

   }

   public String getShooter() {
      String username = this.field_70180_af.func_75681_e(15);
      return username == null ? "" : username;
   }

   public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8) {
      float f2 = MathHelper.func_76133_a(par1 * par1 + par3 * par3 + par5 * par5);
      par1 /= (double)f2;
      par3 /= (double)f2;
      par5 /= (double)f2;
      par1 += this.field_70146_Z.nextGaussian() * (double)(this.field_70146_Z.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)par8;
      par3 += this.field_70146_Z.nextGaussian() * (double)(this.field_70146_Z.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)par8;
      par5 += this.field_70146_Z.nextGaussian() * (double)(this.field_70146_Z.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)par8;
      par1 *= (double)par7;
      par3 *= (double)par7;
      par5 *= (double)par7;
      this.field_70159_w = par1;
      this.field_70181_x = par3;
      this.field_70179_y = par5;
      float f3 = MathHelper.func_76133_a(par1 * par1 + par5 * par5);
      this.field_70126_B = this.field_70177_z = (float)(Math.atan2(par1, par5) * 180.0D / 3.141592653589793D);
      this.field_70127_C = this.field_70125_A = (float)(Math.atan2(par3, (double)f3) * 180.0D / 3.141592653589793D);
      this.ticksInGround = 0;
   }

   @SideOnly(Side.CLIENT)
   public void func_70056_a(double par1, double par3, double par5, float par7, float par8, int par9) {
      this.func_70107_b(par1, par3, par5);
      this.func_70101_b(par7, par8);
   }

   @SideOnly(Side.CLIENT)
   public void func_70016_h(double par1, double par3, double par5) {
      this.field_70159_w = par1;
      this.field_70181_x = par3;
      this.field_70179_y = par5;
      if (this.field_70127_C == 0.0F && this.field_70126_B == 0.0F) {
         float f = MathHelper.func_76133_a(par1 * par1 + par5 * par5);
         this.field_70126_B = this.field_70177_z = (float)(Math.atan2(par1, par5) * 180.0D / 3.141592653589793D);
         this.field_70127_C = this.field_70125_A = (float)(Math.atan2(par3, (double)f) * 180.0D / 3.141592653589793D);
         this.field_70127_C = this.field_70125_A;
         this.field_70126_B = this.field_70177_z;
         this.func_70012_b(this.field_70165_t, this.field_70163_u, this.field_70161_v, this.field_70177_z, this.field_70125_A);
         this.ticksInGround = 0;
      }

   }

   public void func_70071_h_() {
      this.func_70030_z();
      if (this.field_70127_C == 0.0F && this.field_70126_B == 0.0F) {
         float f = MathHelper.func_76133_a(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
         this.field_70126_B = this.field_70177_z = (float)(Math.atan2(this.field_70159_w, this.field_70179_y) * 180.0D / 3.141592653589793D);
         this.field_70127_C = this.field_70125_A = (float)(Math.atan2(this.field_70181_x, (double)f) * 180.0D / 3.141592653589793D);
      }

      Block i = this.field_70170_p.func_147439_a(this.xTile, this.yTile, this.zTile);
      if (i != null) {
         i.func_149719_a(this.field_70170_p, this.xTile, this.yTile, this.zTile);
         AxisAlignedBB axisalignedbb = i.func_149668_a(this.field_70170_p, this.xTile, this.yTile, this.zTile);
         if (axisalignedbb != null && axisalignedbb.func_72318_a(Vec3.func_72443_a(this.field_70165_t, this.field_70163_u, this.field_70161_v))) {
            this.inGround = true;
         }
      }

      if (this.arrowShake > 0) {
         --this.arrowShake;
      }

      if (this.inGround) {
         Block j = this.field_70170_p.func_147439_a(this.xTile, this.yTile, this.zTile);
         int k = this.field_70170_p.func_72805_g(this.xTile, this.yTile, this.zTile);
         if (j == this.inTile && k == this.inData) {
            ++this.ticksInGround;
            if (this.ticksInGround == 1200) {
               this.func_70106_y();
            }
         } else {
            this.inGround = false;
            this.field_70159_w *= (double)(this.field_70146_Z.nextFloat() * 0.2F);
            this.field_70181_x *= (double)(this.field_70146_Z.nextFloat() * 0.2F);
            this.field_70179_y *= (double)(this.field_70146_Z.nextFloat() * 0.2F);
            this.ticksInGround = 0;
            this.ticksInAir = 0;
         }
      } else {
         ++this.ticksInAir;
         Vec3 vec3 = Vec3.func_72443_a(this.field_70165_t, this.field_70163_u, this.field_70161_v);
         Vec3 vec31 = Vec3.func_72443_a(this.field_70165_t + this.field_70159_w, this.field_70163_u + this.field_70181_x, this.field_70161_v + this.field_70179_y);
         MovingObjectPosition movingobjectposition = this.field_70170_p.func_147447_a(vec3, vec31, false, true, false);
         vec3 = Vec3.func_72443_a(this.field_70165_t, this.field_70163_u, this.field_70161_v);
         vec31 = Vec3.func_72443_a(this.field_70165_t + this.field_70159_w, this.field_70163_u + this.field_70181_x, this.field_70161_v + this.field_70179_y);
         if (movingobjectposition != null) {
            vec31 = Vec3.func_72443_a(movingobjectposition.field_72307_f.field_72450_a, movingobjectposition.field_72307_f.field_72448_b, movingobjectposition.field_72307_f.field_72449_c);
         }

         Entity entity = null;
         List list = this.field_70170_p.func_72839_b(this, this.field_70121_D.func_72321_a(this.field_70159_w, this.field_70181_x, this.field_70179_y).func_72314_b(1.0D, 1.0D, 1.0D));
         double d0 = 0.0D;
         String shooterPlayer = this.getShooter();

         int l;
         float f1;
         for(l = 0; l < list.size(); ++l) {
            Entity entity1 = (Entity)list.get(l);
            if (entity1.func_70067_L() && (this.ticksInAir >= 5 || entity1 != this.shootingEntity && (!(entity1 instanceof EntityPlayer) || !((EntityPlayer)entity1).func_70005_c_().equals(shooterPlayer)))) {
               f1 = 0.3F;
               AxisAlignedBB axisalignedbb1 = entity1.field_70121_D.func_72314_b((double)f1, (double)f1, (double)f1);
               MovingObjectPosition movingobjectposition1 = axisalignedbb1.func_72327_a(vec3, vec31);
               if (movingobjectposition1 != null) {
                  double d1 = vec3.func_72438_d(movingobjectposition1.field_72307_f);
                  if (d1 < d0 || d0 == 0.0D) {
                     entity = entity1;
                     d0 = d1;
                  }
               }
            }
         }

         if (entity != null) {
            movingobjectposition = new MovingObjectPosition(entity);
         }

         if (movingobjectposition != null && movingobjectposition.field_72308_g != null && movingobjectposition.field_72308_g instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer)movingobjectposition.field_72308_g;
            if (entityplayer.field_71075_bZ.field_75102_a || this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).func_96122_a(entityplayer)) {
               movingobjectposition = null;
            }
         }

         float f2;
         float f3;
         if (movingobjectposition != null) {
            if (movingobjectposition.field_72308_g == null) {
               this.xTile = movingobjectposition.field_72311_b;
               this.yTile = movingobjectposition.field_72312_c;
               this.zTile = movingobjectposition.field_72309_d;
               this.inTile = this.field_70170_p.func_147439_a(this.xTile, this.yTile, this.zTile);
               this.inData = this.field_70170_p.func_72805_g(this.xTile, this.yTile, this.zTile);
               this.field_70159_w = (double)((float)(movingobjectposition.field_72307_f.field_72450_a - this.field_70165_t));
               this.field_70181_x = (double)((float)(movingobjectposition.field_72307_f.field_72448_b - this.field_70163_u));
               this.field_70179_y = (double)((float)(movingobjectposition.field_72307_f.field_72449_c - this.field_70161_v));
               f2 = MathHelper.func_76133_a(this.field_70159_w * this.field_70159_w + this.field_70181_x * this.field_70181_x + this.field_70179_y * this.field_70179_y);
               this.field_70165_t -= this.field_70159_w / (double)f2 * 0.05000000074505806D;
               this.field_70163_u -= this.field_70181_x / (double)f2 * 0.05000000074505806D;
               this.field_70161_v -= this.field_70179_y / (double)f2 * 0.05000000074505806D;
               this.func_85030_a("random.bowhit", 1.0F, 1.2F / (this.field_70146_Z.nextFloat() * 0.2F + 0.9F));
               this.inGround = true;
               this.arrowShake = 7;
               this.setIsCritical(false);
               if (this.inTile.func_149688_o() != Material.field_151579_a) {
                  this.inTile.func_149670_a(this.field_70170_p, this.xTile, this.yTile, this.zTile, this);
               }
            } else {
               f2 = MathHelper.func_76133_a(this.field_70159_w * this.field_70159_w + this.field_70181_x * this.field_70181_x + this.field_70179_y * this.field_70179_y);
               int i1 = MathHelper.func_76143_f((double)f2 * this.damage);
               if (this.getIsCritical()) {
                  i1 += this.field_70146_Z.nextInt(i1 / 2 + 2);
               }

               BoltDamageSource damagesource = new BoltDamageSource(this, this.shootingEntity != null ? this.shootingEntity : null);
               if (this.func_70027_ad() && !(movingobjectposition.field_72308_g instanceof EntityEnderman)) {
                  movingobjectposition.field_72308_g.func_70015_d(5);
               }

               EntityLivingBase entitylivingbase;
               if (damagesource.isPoweredDraining && movingobjectposition.field_72308_g instanceof EntityLivingBase) {
                  entitylivingbase = (EntityLivingBase)movingobjectposition.field_72308_g;
                  Collection activeEffects = entitylivingbase.func_70651_bq();
                  ArrayList<Integer> removeIDs = new ArrayList();
                  Iterator i$ = activeEffects.iterator();

                  while(i$.hasNext()) {
                     Object obj = i$.next();
                     PotionEffect effect = (PotionEffect)obj;
                     if (effect.func_76456_a() != Potion.field_76436_u.field_76415_H && effect.func_76456_a() != Potion.field_82731_v.field_76415_H && effect.func_76456_a() != Potion.field_76431_k.field_76415_H) {
                        removeIDs.add(effect.func_76456_a());
                     }
                  }

                  i$ = removeIDs.iterator();

                  while(i$.hasNext()) {
                     Integer id = (Integer)i$.next();
                     entitylivingbase.func_82170_o(id);
                  }

                  Witchery.modHooks.reducePowerLevels(entitylivingbase, 0.5F);
               }

               if (damagesource.isHoly && (CreatureUtil.isUndead(entity) || CreatureUtil.isDemonic(entity))) {
                  i1 = (int)((double)i1 * 1.5D);
               }

               if (movingobjectposition.field_72308_g.func_70097_a(damagesource, (float)i1)) {
                  if (movingobjectposition.field_72308_g instanceof EntityLivingBase) {
                     entitylivingbase = (EntityLivingBase)movingobjectposition.field_72308_g;
                     if (!this.field_70170_p.field_72995_K) {
                        entitylivingbase.func_85034_r(entitylivingbase.func_85035_bI() + 1);
                     }

                     if (this.knockbackStrength > 0) {
                        f3 = MathHelper.func_76133_a(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
                        if (f3 > 0.0F) {
                           movingobjectposition.field_72308_g.func_70024_g(this.field_70159_w * (double)this.knockbackStrength * 0.6000000238418579D / (double)f3, 0.1D, this.field_70179_y * (double)this.knockbackStrength * 0.6000000238418579D / (double)f3);
                        }
                     }

                     if (this.shootingEntity != null && this.shootingEntity instanceof EntityLivingBase) {
                        EnchantmentHelper.func_151384_a(entitylivingbase, this.shootingEntity);
                        EnchantmentHelper.func_151385_b((EntityLivingBase)this.shootingEntity, entitylivingbase);
                     }

                     if (this.shootingEntity != null && movingobjectposition.field_72308_g != this.shootingEntity && movingobjectposition.field_72308_g instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP) {
                        Witchery.packetPipeline.sendToAllAround(new S2BPacketChangeGameState(6, 0.0F), this.shootingEntity.field_70170_p, TargetPointUtil.from(this.shootingEntity, 128.0D));
                     }

                     entitylivingbase.field_70172_ad = 0;
                  }

                  this.func_85030_a("random.bowhit", 1.0F, 1.2F / (this.field_70146_Z.nextFloat() * 0.2F + 0.9F));
                  if (!(movingobjectposition.field_72308_g instanceof EntityEnderman)) {
                     this.func_70106_y();
                  }
               } else {
                  this.field_70159_w *= -0.10000000149011612D;
                  this.field_70181_x *= -0.10000000149011612D;
                  this.field_70179_y *= -0.10000000149011612D;
                  this.field_70177_z += 180.0F;
                  this.field_70126_B += 180.0F;
                  this.ticksInAir = 0;
               }
            }
         }

         if (this.getIsCritical()) {
            for(l = 0; l < 4; ++l) {
               this.field_70170_p.func_72869_a("crit", this.field_70165_t + this.field_70159_w * (double)l / 4.0D, this.field_70163_u + this.field_70181_x * (double)l / 4.0D, this.field_70161_v + this.field_70179_y * (double)l / 4.0D, -this.field_70159_w, -this.field_70181_x + 0.2D, -this.field_70179_y);
            }
         }

         this.field_70165_t += this.field_70159_w;
         this.field_70163_u += this.field_70181_x;
         this.field_70161_v += this.field_70179_y;
         f2 = MathHelper.func_76133_a(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
         this.field_70177_z = (float)(Math.atan2(this.field_70159_w, this.field_70179_y) * 180.0D / 3.141592653589793D);

         for(this.field_70125_A = (float)(Math.atan2(this.field_70181_x, (double)f2) * 180.0D / 3.141592653589793D); this.field_70125_A - this.field_70127_C < -180.0F; this.field_70127_C -= 360.0F) {
         }

         while(this.field_70125_A - this.field_70127_C >= 180.0F) {
            this.field_70127_C += 360.0F;
         }

         while(this.field_70177_z - this.field_70126_B < -180.0F) {
            this.field_70126_B -= 360.0F;
         }

         while(this.field_70177_z - this.field_70126_B >= 180.0F) {
            this.field_70126_B += 360.0F;
         }

         this.field_70125_A = this.field_70127_C + (this.field_70125_A - this.field_70127_C) * 0.2F;
         this.field_70177_z = this.field_70126_B + (this.field_70177_z - this.field_70126_B) * 0.2F;
         float f4 = 0.99F;
         f1 = 0.05F;
         if (this.func_70090_H()) {
            for(int j1 = 0; j1 < 4; ++j1) {
               f3 = 0.25F;
               this.field_70170_p.func_72869_a("bubble", this.field_70165_t - this.field_70159_w * (double)f3, this.field_70163_u - this.field_70181_x * (double)f3, this.field_70161_v - this.field_70179_y * (double)f3, this.field_70159_w, this.field_70181_x, this.field_70179_y);
            }

            f4 = 0.8F;
         }

         this.field_70159_w *= (double)f4;
         this.field_70181_x *= (double)f4;
         this.field_70179_y *= (double)f4;
         this.field_70181_x -= (double)f1;
         this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
         this.func_145775_I();
      }

   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      par1NBTTagCompound.func_74777_a("xTile", (short)this.xTile);
      par1NBTTagCompound.func_74777_a("yTile", (short)this.yTile);
      par1NBTTagCompound.func_74777_a("zTile", (short)this.zTile);
      par1NBTTagCompound.func_74774_a("inTile", (byte)Block.func_149682_b(this.inTile));
      par1NBTTagCompound.func_74774_a("inData", (byte)this.inData);
      par1NBTTagCompound.func_74774_a("shake", (byte)this.arrowShake);
      par1NBTTagCompound.func_74774_a("inGround", (byte)(this.inGround ? 1 : 0));
      par1NBTTagCompound.func_74774_a("pickup", (byte)this.canBePickedUp);
      par1NBTTagCompound.func_74780_a("damage", this.damage);
      par1NBTTagCompound.func_74768_a("boltType", this.getBoltType());
   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      this.xTile = par1NBTTagCompound.func_74765_d("xTile");
      this.yTile = par1NBTTagCompound.func_74765_d("yTile");
      this.zTile = par1NBTTagCompound.func_74765_d("zTile");
      this.inTile = Block.func_149729_e(par1NBTTagCompound.func_74771_c("inTile") & 255);
      this.inData = par1NBTTagCompound.func_74771_c("inData") & 255;
      this.arrowShake = par1NBTTagCompound.func_74771_c("shake") & 255;
      this.inGround = par1NBTTagCompound.func_74771_c("inGround") == 1;
      if (par1NBTTagCompound.func_74764_b("damage")) {
         this.damage = par1NBTTagCompound.func_74769_h("damage");
      }

      if (par1NBTTagCompound.func_74764_b("pickup")) {
         this.canBePickedUp = par1NBTTagCompound.func_74771_c("pickup");
      } else if (par1NBTTagCompound.func_74764_b("player")) {
         this.canBePickedUp = par1NBTTagCompound.func_74767_n("player") ? 1 : 0;
      }

      this.setBoltType(par1NBTTagCompound.func_74762_e("boltType"));
   }

   public void func_70100_b_(EntityPlayer par1EntityPlayer) {
      if (!this.field_70170_p.field_72995_K && this.inGround && this.arrowShake <= 0) {
         boolean flag = this.canBePickedUp == 1 || this.canBePickedUp == 2 && par1EntityPlayer.field_71075_bZ.field_75098_d;
         if (this.canBePickedUp == 1) {
            ItemStack stack = null;
            if (this.isDraining()) {
               stack = Witchery.Items.GENERIC.itemBoltAntiMagic.createStack();
            } else if (this.isHolyDamage()) {
               stack = Witchery.Items.GENERIC.itemBoltHoly.createStack();
            } else {
               stack = Witchery.Items.GENERIC.itemBoltStake.createStack();
            }

            if (!par1EntityPlayer.field_71071_by.func_70441_a(stack)) {
               flag = false;
            }
         }

         if (flag) {
            this.func_85030_a("random.pop", 0.2F, ((this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            par1EntityPlayer.func_71001_a(this, 1);
            this.func_70106_y();
         }
      }

   }

   protected boolean func_70041_e_() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public float func_70053_R() {
      return 0.0F;
   }

   public void setDamage(double par1) {
      this.damage = par1;
   }

   public double getDamage() {
      return this.damage;
   }

   public void setKnockbackStrength(int par1) {
      this.knockbackStrength = par1;
   }

   public void setBoltType(int type) {
      if (!this.field_70170_p.field_72995_K) {
         this.field_70180_af.func_75692_b(17, (byte)type);
      }

   }

   public int getBoltType() {
      byte b0 = this.field_70180_af.func_75683_a(17);
      return b0;
   }

   public boolean isDraining() {
      int b0 = this.getBoltType();
      return b0 == 1 || b0 == 2;
   }

   public boolean isPoweredDraining() {
      return this.getBoltType() == 2;
   }

   public boolean isHolyDamage() {
      return this.getBoltType() == 3;
   }

   public boolean isWoodenDamage() {
      int boltType = this.getBoltType();
      return boltType == 0 || boltType == 1 || boltType == 2;
   }

   public boolean isSilverDamage() {
      int boltType = this.getBoltType();
      return boltType == 4;
   }

   public boolean func_70075_an() {
      return false;
   }

   public void setIsCritical(boolean par1) {
      byte b0 = this.field_70180_af.func_75683_a(16);
      if (par1) {
         this.field_70180_af.func_75692_b(16, (byte)(b0 | 1));
      } else {
         this.field_70180_af.func_75692_b(16, (byte)(b0 & -2));
      }

   }

   public boolean getIsCritical() {
      byte b0 = this.field_70180_af.func_75683_a(16);
      return (b0 & 1) != 0;
   }
}
