package com.emoniph.witchery.brewing;

import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.EntityPosition;
import com.emoniph.witchery.util.EntityUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;

public class EntitySplatter extends Entity {
   private int field_145788_c = -1;
   private int field_145786_d = -1;
   private int field_145787_e = -1;
   private Block field_145785_f;
   protected boolean inGround;
   public int throwableShake;
   private int ticksInGround;
   private int ticksInAir;
   private int effectID;
   private int color;
   private int level;

   public EntitySplatter(World world) {
      super(world);
      this.func_70105_a(0.25F, 0.25F);
   }

   public EntitySplatter(World world, double x, double y, double z, int effectID, int color, int level) {
      super(world);
      this.ticksInGround = 0;
      this.func_70105_a(0.25F, 0.25F);
      this.func_70107_b(x, y, z);
      this.field_70129_M = 0.0F;
      this.effectID = effectID;
      this.level = level;
      this.setColor(color);
      if (effectID == 1) {
         this.func_70015_d(1000);
      }

   }

   protected void func_70088_a() {
      this.field_70180_af.func_75682_a(6, 0);
   }

   protected void setColor(int color) {
      this.func_70096_w().func_75692_b(6, color);
   }

   public int getColor() {
      return this.func_70096_w().func_75679_c(6);
   }

   @SideOnly(Side.CLIENT)
   public boolean func_70112_a(double distSq) {
      double d1 = this.field_70121_D.func_72320_b() * 4.0D;
      d1 *= 64.0D;
      return distSq < d1 * d1;
   }

   public void setThrowableHeading(double p_70186_1_, double p_70186_3_, double p_70186_5_, float p_70186_7_, float p_70186_8_) {
      float f2 = MathHelper.func_76133_a(p_70186_1_ * p_70186_1_ + p_70186_3_ * p_70186_3_ + p_70186_5_ * p_70186_5_);
      p_70186_1_ /= (double)f2;
      p_70186_3_ /= (double)f2;
      p_70186_5_ /= (double)f2;
      p_70186_1_ += this.field_70146_Z.nextGaussian() * 0.007499999832361937D * (double)p_70186_8_;
      p_70186_3_ += this.field_70146_Z.nextGaussian() * 0.007499999832361937D * (double)p_70186_8_;
      p_70186_5_ += this.field_70146_Z.nextGaussian() * 0.007499999832361937D * (double)p_70186_8_;
      p_70186_1_ *= (double)p_70186_7_;
      p_70186_3_ *= (double)p_70186_7_;
      p_70186_5_ *= (double)p_70186_7_;
      this.field_70159_w = p_70186_1_;
      this.field_70181_x = p_70186_3_;
      this.field_70179_y = p_70186_5_;
      float f3 = MathHelper.func_76133_a(p_70186_1_ * p_70186_1_ + p_70186_5_ * p_70186_5_);
      this.field_70126_B = this.field_70177_z = (float)(Math.atan2(p_70186_1_, p_70186_5_) * 180.0D / 3.141592653589793D);
      this.field_70127_C = this.field_70125_A = (float)(Math.atan2(p_70186_3_, (double)f3) * 180.0D / 3.141592653589793D);
      this.ticksInGround = 0;
   }

   public void func_70016_h(double motionX, double motionY, double motionZ) {
      this.field_70159_w = motionX;
      this.field_70181_x = motionY;
      this.field_70179_y = motionZ;
      if (this.field_70127_C == 0.0F && this.field_70126_B == 0.0F) {
         float f = MathHelper.func_76133_a(motionX * motionX + motionZ * motionZ);
         this.field_70126_B = this.field_70177_z = (float)(Math.atan2(motionX, motionZ) * 180.0D / 3.141592653589793D);
         this.field_70127_C = this.field_70125_A = (float)(Math.atan2(motionY, (double)f) * 180.0D / 3.141592653589793D);
      }

   }

   public void func_70071_h_() {
      this.field_70142_S = this.field_70165_t;
      this.field_70137_T = this.field_70163_u;
      this.field_70136_U = this.field_70161_v;
      super.func_70071_h_();
      if (this.throwableShake > 0) {
         --this.throwableShake;
      }

      if (this.inGround) {
         if (this.field_70170_p.func_147439_a(this.field_145788_c, this.field_145786_d, this.field_145787_e) == this.field_145785_f) {
            ++this.ticksInGround;
            if (this.ticksInGround == 1200) {
               this.func_70106_y();
            }

            return;
         }

         this.inGround = false;
         this.field_70159_w *= (double)(this.field_70146_Z.nextFloat() * 0.2F);
         this.field_70181_x *= (double)(this.field_70146_Z.nextFloat() * 0.2F);
         this.field_70179_y *= (double)(this.field_70146_Z.nextFloat() * 0.2F);
         this.ticksInGround = 0;
         this.ticksInAir = 0;
      } else {
         ++this.ticksInAir;
      }

      Vec3 vec3 = Vec3.func_72443_a(this.field_70165_t, this.field_70163_u, this.field_70161_v);
      Vec3 vec31 = Vec3.func_72443_a(this.field_70165_t + this.field_70159_w, this.field_70163_u + this.field_70181_x, this.field_70161_v + this.field_70179_y);
      MovingObjectPosition movingobjectposition = this.field_70170_p.func_72933_a(vec3, vec31);
      vec3 = Vec3.func_72443_a(this.field_70165_t, this.field_70163_u, this.field_70161_v);
      vec31 = Vec3.func_72443_a(this.field_70165_t + this.field_70159_w, this.field_70163_u + this.field_70181_x, this.field_70161_v + this.field_70179_y);
      if (movingobjectposition != null) {
         vec31 = Vec3.func_72443_a(movingobjectposition.field_72307_f.field_72450_a, movingobjectposition.field_72307_f.field_72448_b, movingobjectposition.field_72307_f.field_72449_c);
      }

      if (!this.field_70170_p.field_72995_K) {
         Entity entity = null;
         List list = this.field_70170_p.func_72839_b(this, this.field_70121_D.func_72321_a(this.field_70159_w, this.field_70181_x, this.field_70179_y).func_72314_b(1.0D, 1.0D, 1.0D));
         double d0 = 0.0D;

         for(int j = 0; j < list.size(); ++j) {
            Entity entity1 = (Entity)list.get(j);
            if (entity1.func_70067_L() && this.ticksInAir >= 5 && !(entity instanceof EntitySplatter)) {
               float f = 0.3F;
               AxisAlignedBB axisalignedbb = entity1.field_70121_D.func_72314_b((double)f, (double)f, (double)f);
               MovingObjectPosition movingobjectposition1 = axisalignedbb.func_72327_a(vec3, vec31);
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
      }

      if (movingobjectposition != null) {
         if (movingobjectposition.field_72313_a == MovingObjectType.BLOCK && this.field_70170_p.func_147439_a(movingobjectposition.field_72311_b, movingobjectposition.field_72312_c, movingobjectposition.field_72309_d) == Blocks.field_150427_aO) {
            this.func_70063_aa();
         } else {
            this.onImpact(movingobjectposition);
         }
      }

      this.field_70165_t += this.field_70159_w;
      this.field_70163_u += this.field_70181_x;
      this.field_70161_v += this.field_70179_y;
      float f1 = MathHelper.func_76133_a(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
      this.field_70177_z = (float)(Math.atan2(this.field_70159_w, this.field_70179_y) * 180.0D / 3.141592653589793D);

      for(this.field_70125_A = (float)(Math.atan2(this.field_70181_x, (double)f1) * 180.0D / 3.141592653589793D); this.field_70125_A - this.field_70127_C < -180.0F; this.field_70127_C -= 360.0F) {
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
      float f2 = 0.99F;
      float f3 = this.getGravityVelocity();
      if (this.func_70090_H()) {
         for(int i = 0; i < 4; ++i) {
            float f4 = 0.25F;
            this.field_70170_p.func_72869_a("bubble", this.field_70165_t - this.field_70159_w * (double)f4, this.field_70163_u - this.field_70181_x * (double)f4, this.field_70161_v - this.field_70179_y * (double)f4, this.field_70159_w, this.field_70181_x, this.field_70179_y);
         }

         f2 = 0.8F;
      }

      this.field_70159_w *= (double)f2;
      this.field_70181_x *= (double)f2;
      this.field_70179_y *= (double)f2;
      this.field_70181_x -= (double)f3;
      this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
   }

   protected float getGravityVelocity() {
      return 0.03F;
   }

   protected void onImpact(MovingObjectPosition mop) {
      if (!this.field_70170_p.field_72995_K && mop != null) {
         Coord coord = new Coord(mop, new EntityPosition(this), true);
         switch(mop.field_72313_a) {
         case BLOCK:
            ForgeDirection side = ForgeDirection.getOrientation(mop.field_72310_e);
            int x = mop.field_72311_b + side.offsetX;
            int y = mop.field_72312_c + side.offsetY;
            int z = mop.field_72309_d + side.offsetZ;
            if (BlockUtil.isReplaceableBlock(this.field_70170_p, x, y, z, FakePlayerFactory.getMinecraft((WorldServer)this.field_70170_p))) {
               this.field_70170_p.func_147449_b(x, y, z, Blocks.field_150480_ab);
            }

            if (this.level - 1 > 0) {
               splatter(this.field_70170_p, coord, this.level - 1);
            }

            this.func_70106_y();
            break;
         case ENTITY:
            if (mop.field_72308_g instanceof EntityLivingBase) {
               mop.field_72308_g.func_70015_d(5);
            }
            break;
         case MISS:
            this.func_70106_y();
         }
      }

   }

   public static void splatter(World world, Coord coord, int level) {
      if (!world.field_72995_K) {
         for(int i = 0; i < 3 + level; ++i) {
            EntitySplatter splatter = new EntitySplatter(world, 0.5D + (double)coord.x, 0.5D + (double)coord.y, 0.5D + (double)coord.z, 1, 10027008, level);
            double maxSpeed = 0.1D;
            double doubleSpeed = 0.2D;
            splatter.func_70016_h(world.field_73012_v.nextDouble() * 0.2D - 0.1D, world.field_73012_v.nextDouble() * 0.05D + 0.3D, world.field_73012_v.nextDouble() * 0.2D - 0.1D);
            EntityUtil.spawnEntityInWorld(world, splatter);
         }
      }

   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      nbtRoot.func_74777_a("xTile", (short)this.field_145788_c);
      nbtRoot.func_74777_a("yTile", (short)this.field_145786_d);
      nbtRoot.func_74777_a("zTile", (short)this.field_145787_e);
      nbtRoot.func_74774_a("inTile", (byte)Block.func_149682_b(this.field_145785_f));
      nbtRoot.func_74774_a("shake", (byte)this.throwableShake);
      nbtRoot.func_74774_a("inGround", (byte)(this.inGround ? 1 : 0));
      nbtRoot.func_74768_a("Color", this.color);
      nbtRoot.func_74768_a("Level", this.level);
      nbtRoot.func_74768_a("Effect", this.effectID);
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      this.field_145788_c = nbtRoot.func_74765_d("xTile");
      this.field_145786_d = nbtRoot.func_74765_d("yTile");
      this.field_145787_e = nbtRoot.func_74765_d("zTile");
      this.field_145785_f = Block.func_149729_e(nbtRoot.func_74771_c("inTile") & 255);
      this.throwableShake = nbtRoot.func_74771_c("shake") & 255;
      this.inGround = nbtRoot.func_74771_c("inGround") == 1;
      this.effectID = nbtRoot.func_74762_e("Effect");
      this.level = nbtRoot.func_74762_e("Level");
      this.setColor(nbtRoot.func_74762_e("Color"));
   }

   @SideOnly(Side.CLIENT)
   public float func_70053_R() {
      return 0.0F;
   }
}
