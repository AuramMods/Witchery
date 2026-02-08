package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.brewing.EntityThrowableBase;
import com.emoniph.witchery.client.particle.NaturePowerFX;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.item.ItemSunGrenade;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityGrenade extends EntityThrowableBase {
   String owner;
   boolean blockPlaced;
   int blockX;
   int blockY;
   int blockZ;

   public EntityGrenade(World world) {
      super(world);
      this.func_70105_a(0.25F, 0.25F);
      this.field_70145_X = false;
   }

   public EntityGrenade(World world, EntityLivingBase thrower, ItemStack stack) {
      super(world, thrower, -20.0F);
      this.func_70105_a(0.25F, 0.25F);
      this.field_70145_X = false;
      if (stack != null && stack.func_77973_b() == Witchery.Items.DUP_GRENADE) {
         this.setMode(1);
         this.setOwner(ItemSunGrenade.getOwnerName(stack));
      } else {
         this.setMode(0);
      }

   }

   public EntityGrenade(World world, double x, double y, double z, ItemStack stack) {
      super(world, x, y, z, -20.0F);
      this.func_70105_a(0.25F, 0.25F);
      this.field_70145_X = false;
      if (stack != null && stack.func_77973_b() == Witchery.Items.DUP_GRENADE) {
         this.setMode(1);
         this.setOwner(ItemSunGrenade.getOwnerName(stack));
      } else {
         this.setMode(0);
      }

   }

   protected float getGravityVelocity() {
      return this.getImpact() ? 0.0F : 0.05F;
   }

   protected float func_70182_d() {
      return 0.75F;
   }

   protected float func_70183_g() {
      return -20.0F;
   }

   public void setOwner(String owner) {
      this.owner = owner;
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(6, (byte)0);
      this.field_70180_af.func_75682_a(16, (byte)0);
   }

   public int getMode() {
      return this.field_70180_af.func_75683_a(16);
   }

   public void setMode(int mode) {
      this.field_70180_af.func_75692_b(16, (byte)mode);
   }

   protected void setImpact(boolean impact) {
      this.func_70096_w().func_75692_b(6, Byte.valueOf((byte)(impact ? 1 : 0)));
   }

   public boolean getImpact() {
      return this.func_70096_w().func_75683_a(6) == 1;
   }

   protected int getMaxGroundTicks() {
      return super.getMaxGroundTicks();
   }

   protected int getMaxAirTicks() {
      return super.getMaxAirTicks();
   }

   protected void onImpact(MovingObjectPosition mop) {
      if (!this.field_70170_p.field_72995_K) {
         if (this.getMode() == 0) {
            this.setImpact(true);
         } else {
            if (!this.field_70170_p.field_72995_K) {
               this.onSetDead();
            } else {
               this.onClientSetDead();
            }

            this.func_70106_y();
         }
      }

      this.field_70159_w = 0.0D;
      this.field_70181_x = 0.0D;
      this.field_70179_y = 0.0D;
   }

   public void func_70030_z() {
      super.func_70030_z();
   }

   public void func_70071_h_() {
      super.func_70071_h_();
      if (this.getMode() == 0) {
         if (this.field_70170_p.field_72995_K && this.getImpact() && this.field_70170_p.field_73012_v.nextInt(4) == 0) {
            float red = 1.0F;
            float green = 1.0F;
            float blue = 0.0F;
            Witchery.proxy.generateParticle(this.field_70170_p, this.field_70165_t - 0.1D + this.field_70170_p.field_73012_v.nextDouble() * 0.2D, this.field_70163_u + 0.3D * (double)this.field_70131_O - 0.1D + this.field_70170_p.field_73012_v.nextDouble() * 0.2D, this.field_70161_v - 0.1D + this.field_70170_p.field_73012_v.nextDouble() * 0.2D, red, green, blue, 10, -0.3F);
         } else if (!this.field_70170_p.field_72995_K && !this.field_70128_L) {
            if (!this.blockPlaced && this.field_70173_aa % 5 == 4) {
               this.blockPlaced = true;
               this.blockX = MathHelper.func_76128_c(this.field_70165_t);
               this.blockY = MathHelper.func_76128_c(this.field_70163_u);
               this.blockZ = MathHelper.func_76128_c(this.field_70161_v);
               if (this.field_70170_p.func_147437_c(this.blockX, this.blockY, this.blockZ)) {
                  this.field_70170_p.func_147449_b(this.blockX, this.blockY, this.blockZ, Witchery.Blocks.LIGHT);
               } else {
                  ++this.blockY;
                  if (this.field_70170_p.func_147437_c(this.blockX, this.blockY, this.blockZ)) {
                     this.field_70170_p.func_147449_b(this.blockX, this.blockY, this.blockZ, Witchery.Blocks.LIGHT);
                  }
               }
            } else if (this.blockPlaced && (this.field_70173_aa % 5 == 2 || this.getImpact())) {
               int x = MathHelper.func_76128_c(this.field_70165_t);
               int y = MathHelper.func_76128_c(this.field_70163_u);
               int z = MathHelper.func_76128_c(this.field_70161_v);
               if (this.blockX != x || this.blockY != y || this.blockZ != z || this.field_70173_aa % 30 == 4 && this.field_70170_p.func_147437_c(x, y, z)) {
                  if (this.field_70170_p.func_147439_a(this.blockX, this.blockY, this.blockZ) == Witchery.Blocks.LIGHT) {
                     this.field_70170_p.func_147468_f(this.blockX, this.blockY, this.blockZ);
                  }

                  this.blockX = x;
                  this.blockY = y;
                  this.blockZ = z;
                  if (this.field_70170_p.func_147437_c(this.blockX, this.blockY, this.blockZ)) {
                     this.field_70170_p.func_147449_b(this.blockX, this.blockY, this.blockZ, Witchery.Blocks.LIGHT);
                  } else {
                     ++this.blockY;
                     if (this.field_70170_p.func_147437_c(this.blockX, this.blockY, this.blockZ)) {
                        this.field_70170_p.func_147449_b(this.blockX, this.blockY, this.blockZ, Witchery.Blocks.LIGHT);
                     }
                  }
               }
            }

            if (this.getImpact()) {
               Entity entity = null;
               List<EntityLivingBase> list = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72321_a(this.field_70159_w, this.field_70181_x, this.field_70179_y).func_72314_b(1.0D, 1.0D, 1.0D));
               double d0 = 0.0D;

               for(int j = 0; j < list.size(); ++j) {
                  EntityLivingBase entity1 = (EntityLivingBase)list.get(j);
                  if (entity1.func_70067_L() && CreatureUtil.isUndead(entity1)) {
                     entity1.func_70015_d(3);
                  }
               }
            }
         }
      }

   }

   protected void onSetDead() {
      if (!this.field_70170_p.field_72995_K) {
         this.func_70099_a(Witchery.Items.GENERIC.itemQuartzSphere.createStack(), 0.5F);
         int mode = this.getMode();
         EntityFollower entity;
         if (mode == 0) {
            if (this.blockPlaced) {
               this.blockPlaced = false;
               if (this.field_70170_p.func_147439_a(this.blockX, this.blockY, this.blockZ) == Witchery.Blocks.LIGHT) {
                  this.field_70170_p.func_147468_f(this.blockX, this.blockY, this.blockZ);
               }
            }

            entity = null;
            List<EntityLivingBase> list = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72321_a(this.field_70159_w, this.field_70181_x, this.field_70179_y).func_72314_b(3.0D, 2.0D, 3.0D));
            double d0 = 0.0D;

            for(int j = 0; j < list.size(); ++j) {
               EntityLivingBase entity1 = (EntityLivingBase)list.get(j);
               if (entity1.func_70067_L()) {
                  if (CreatureUtil.isUndead(entity1)) {
                     entity1.func_70015_d(5);
                     if (entity1 instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer)entity1;
                        ExtendedPlayer playerEx = ExtendedPlayer.get(player);
                        if (playerEx.getVampireLevel() == 4 && playerEx.canIncreaseVampireLevel()) {
                           if (playerEx.getVampireQuestCounter() >= 9) {
                              playerEx.increaseVampireLevel();
                           } else {
                              playerEx.increaseVampireQuestCounter();
                           }
                        }
                     }
                  }

                  entity1.func_70690_d(new PotionEffect(Potion.field_76440_q.field_76415_H, TimeUtil.secsToTicks(this.field_70170_p.field_73012_v.nextInt(3) + 10), 0, true));
               }
            }
         } else if (mode == 1) {
            entity = new EntityFollower(this.field_70170_p);
            entity.setFollowerType(5);
            entity.setSkin(this.owner != null ? this.owner : "");
            entity.func_94058_c(this.owner != null ? this.owner : "Steve");
            entity.func_70012_b(this.field_70165_t, this.field_70163_u, this.field_70161_v, 0.0F, 0.0F);
            entity.setTTL(TimeUtil.secsToTicks(10));
            this.field_70170_p.func_72838_d(entity);
            entity.attractAttention();
         }
      }

   }

   @SideOnly(Side.CLIENT)
   protected void onClientSetDead() {
      if (this.getMode() == 0) {
         for(int i = 0; i < 20; ++i) {
            double width = 0.4D;
            double xPos = 0.3D + this.field_70146_Z.nextDouble() * 0.4D;
            double zPos = 0.3D + this.field_70146_Z.nextDouble() * 0.4D;
            double d0 = this.field_70165_t;
            double d1 = this.field_70163_u;
            double d2 = this.field_70161_v;
            NaturePowerFX sparkle = new NaturePowerFX(this.field_70170_p, d0, d1, d2);
            sparkle.setScale(1.0F);
            sparkle.setGravity(0.2F);
            sparkle.setCanMove(true);
            sparkle.field_70145_X = true;
            double maxSpeed = 0.08D;
            double doubleSpeed = 0.16D;
            sparkle.func_70016_h(this.field_70146_Z.nextDouble() * 0.16D - 0.08D, this.field_70146_Z.nextDouble() * 0.05D + 0.12D, this.field_70146_Z.nextDouble() * 0.16D - 0.08D);
            sparkle.setMaxAge(25 + this.field_70146_Z.nextInt(10));
            float red = 1.0F;
            float green = 1.0F;
            float blue = 0.0F;
            float maxColorShift = 0.2F;
            float doubleColorShift = maxColorShift * 2.0F;
            float colorshiftR = this.field_70146_Z.nextFloat() * doubleColorShift - maxColorShift;
            float colorshiftG = this.field_70146_Z.nextFloat() * doubleColorShift - maxColorShift;
            float colorshiftB = this.field_70146_Z.nextFloat() * doubleColorShift - maxColorShift;
            sparkle.func_70538_b(red + colorshiftR, green + colorshiftG, blue + colorshiftB);
            sparkle.func_82338_g(0.1F);
            Minecraft.func_71410_x().field_71452_i.func_78873_a(sparkle);
         }
      }

   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      this.setImpact(nbtRoot.func_74767_n("Impacted"));
      this.blockPlaced = nbtRoot.func_74767_n("BlockPlaced");
      if (this.blockPlaced) {
         this.blockX = nbtRoot.func_74762_e("BlockPlacedX");
         this.blockY = nbtRoot.func_74762_e("BlockPlacedY");
         this.blockZ = nbtRoot.func_74762_e("BlockPlacedZ");
      }

      if (nbtRoot.func_74764_b("Mode")) {
         this.setMode(nbtRoot.func_74762_e("Mode"));
      }

      if (nbtRoot.func_74764_b("Owner")) {
         this.owner = nbtRoot.func_74779_i("Owner");
      } else {
         this.owner = null;
      }

   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74757_a("Impacted", this.getImpact());
      if (this.blockPlaced) {
         nbtRoot.func_74757_a("BlockPlaced", this.blockPlaced);
         nbtRoot.func_74768_a("BlockPlacedX", this.blockX);
         nbtRoot.func_74768_a("BlockPlacedY", this.blockY);
         nbtRoot.func_74768_a("BlockPlacedZ", this.blockZ);
      }

      nbtRoot.func_74768_a("Mode", this.getMode());
      if (this.owner != null) {
         nbtRoot.func_74778_a("Owner", this.owner);
      }

   }
}
