package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.infusion.InfusedBrewEffect;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.BlockColored;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingFallEvent;

public class EntityBroom extends Entity {
   private boolean field_70279_a;
   private double speedMultiplier;
   private int broomPosRotationIncrements;
   private double broomX;
   private double broomY;
   private double broomZ;
   private double broomYaw;
   private double broomPitch;
   @SideOnly(Side.CLIENT)
   private double velocityX;
   @SideOnly(Side.CLIENT)
   private double velocityY;
   @SideOnly(Side.CLIENT)
   private double velocityZ;
   boolean riderHasOwlFamiliar;
   boolean riderHasSoaringBrew;

   public EntityBroom(World world) {
      super(world);
      this.riderHasOwlFamiliar = false;
      this.riderHasSoaringBrew = false;
      this.field_70279_a = true;
      this.speedMultiplier = 0.07D;
      this.field_70156_m = true;
      this.func_70105_a(1.2F, 0.5F);
      this.field_70129_M = this.field_70131_O / 2.0F;
   }

   public EntityBroom(World world, double x, double y, double z) {
      this(world);
      this.func_70107_b(x, y + (double)this.field_70129_M, z);
      this.field_70159_w = 0.0D;
      this.field_70181_x = 0.0D;
      this.field_70179_y = 0.0D;
      this.field_70169_q = x;
      this.field_70167_r = y;
      this.field_70166_s = z;
   }

   protected boolean func_70041_e_() {
      return false;
   }

   protected void func_70088_a() {
      this.field_70180_af.func_75682_a(10, "");
      this.field_70180_af.func_75682_a(16, -1);
      this.field_70180_af.func_75682_a(17, new Integer(0));
      this.field_70180_af.func_75682_a(18, new Integer(1));
      this.field_70180_af.func_75682_a(19, new Float(0.0F));
   }

   protected void func_70081_e(int par1) {
   }

   public void setBrushColor(int color) {
      this.field_70180_af.func_75692_b(16, (byte)color);
   }

   public int getBrushColor() {
      return this.field_70180_af.func_75683_a(16);
   }

   public void setCustomNameTag(String par1Str) {
      this.field_70180_af.func_75692_b(10, par1Str);
   }

   public String getCustomNameTag() {
      return this.field_70180_af.func_75681_e(10);
   }

   public boolean hasCustomNameTag() {
      return this.field_70180_af.func_75681_e(10).length() > 0;
   }

   public AxisAlignedBB func_70114_g(Entity par1Entity) {
      return par1Entity.field_70121_D;
   }

   public AxisAlignedBB func_70046_E() {
      return this.field_70121_D;
   }

   public boolean func_70104_M() {
      return true;
   }

   public double func_70042_X() {
      return (double)this.field_70131_O * 0.55D;
   }

   public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
      if (this.func_85032_ar()) {
         return false;
      } else if (!this.field_70170_p.field_72995_K && !this.field_70128_L) {
         this.setForwardDirection(-this.getForwardDirection());
         this.setTimeSinceHit(10);
         this.setDamageTaken(this.getDamageTaken() + par2 * 10.0F);
         this.func_70018_K();
         boolean flag = par1DamageSource.func_76346_g() instanceof EntityPlayer && ((EntityPlayer)par1DamageSource.func_76346_g()).field_71075_bZ.field_75098_d;
         if (flag || this.getDamageTaken() > 40.0F) {
            if (this.field_70153_n != null) {
               this.field_70153_n.func_70078_a(this);
            }

            if (!flag) {
               ItemStack broomStack = Witchery.Items.GENERIC.itemBroomEnchanted.createStack();
               if (this.hasCustomNameTag()) {
                  broomStack.func_151001_c(this.getCustomNameTag());
               }

               int brushColor = this.getBrushColor();
               if (brushColor >= 0 && brushColor <= 15) {
                  Witchery.Items.GENERIC.setBroomItemColor(broomStack, brushColor);
               }

               this.func_70099_a(broomStack, 0.0F);
            }

            this.func_70106_y();
         }

         return true;
      } else {
         return true;
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_70057_ab() {
      this.setForwardDirection(-this.getForwardDirection());
      this.setTimeSinceHit(10);
      this.setDamageTaken(this.getDamageTaken() * 11.0F);
   }

   public boolean func_70067_L() {
      return !this.field_70128_L && this.field_70153_n == null;
   }

   @SideOnly(Side.CLIENT)
   public void func_70056_a(double x, double y, double z, float yaw, float pitch, int par9) {
      if (this.field_70279_a) {
         this.broomPosRotationIncrements = par9 + 5;
      } else {
         double d3 = x - this.field_70165_t;
         double d4 = y - this.field_70163_u;
         double d5 = z - this.field_70161_v;
         double d6 = d3 * d3 + d4 * d4 + d5 * d5;
         if (d6 <= 1.0D) {
            return;
         }

         this.broomPosRotationIncrements = 3;
      }

      this.broomX = x;
      this.broomY = y;
      this.broomZ = z;
      this.broomYaw = (double)yaw;
      this.broomPitch = (double)pitch;
      this.field_70159_w = this.velocityX;
      this.field_70181_x = this.velocityY;
      this.field_70179_y = this.velocityZ;
   }

   @SideOnly(Side.CLIENT)
   public void func_70016_h(double x, double y, double z) {
      this.velocityX = this.field_70159_w = x;
      this.velocityY = this.field_70181_x = y;
      this.velocityZ = this.field_70179_y = z;
   }

   public void func_70071_h_() {
      super.func_70071_h_();
      if (this.field_70173_aa % 100 == 0 && this.field_70153_n != null && this.field_70153_n instanceof EntityPlayer) {
         this.riderHasSoaringBrew = InfusedBrewEffect.Soaring.isActive((EntityPlayer)this.field_70153_n);
      }

      if (this.getTimeSinceHit() > 0) {
         this.setTimeSinceHit(this.getTimeSinceHit() - 1);
      }

      if (this.getDamageTaken() > 0.0F) {
         this.setDamageTaken(this.getDamageTaken() - 1.0F);
      }

      this.field_70169_q = this.field_70165_t;
      this.field_70167_r = this.field_70163_u;
      this.field_70166_s = this.field_70161_v;
      byte b0 = true;
      double d0 = 0.0D;
      double initialHorzVelocity = Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
      double newHorzVelocity;
      double d5;
      if (initialHorzVelocity > 0.26249999999999996D) {
         newHorzVelocity = Math.cos((double)this.field_70177_z * 3.141592653589793D / 180.0D);
         d5 = Math.sin((double)this.field_70177_z * 3.141592653589793D / 180.0D);
      }

      double d10;
      double d11;
      if (this.field_70170_p.field_72995_K && this.field_70279_a) {
         if (this.broomPosRotationIncrements > 0) {
            newHorzVelocity = this.field_70165_t + (this.broomX - this.field_70165_t) / (double)this.broomPosRotationIncrements;
            d5 = this.field_70163_u + (this.broomY - this.field_70163_u) / (double)this.broomPosRotationIncrements;
            d11 = this.field_70161_v + (this.broomZ - this.field_70161_v) / (double)this.broomPosRotationIncrements;
            d10 = MathHelper.func_76138_g(this.broomYaw - (double)this.field_70177_z);
            this.field_70177_z = (float)((double)this.field_70177_z + d10 / (double)this.broomPosRotationIncrements);
            this.field_70125_A = (float)((double)this.field_70125_A + (this.broomPitch - (double)this.field_70125_A) / (double)this.broomPosRotationIncrements);
            --this.broomPosRotationIncrements;
            this.func_70107_b(newHorzVelocity, d5, d11);
            this.func_70101_b(this.field_70177_z, this.field_70125_A);
         } else {
            newHorzVelocity = this.field_70165_t + this.field_70159_w;
            d5 = this.field_70163_u + this.field_70181_x;
            d11 = this.field_70161_v + this.field_70179_y;
            this.func_70101_b((float)((double)this.field_70177_z + (this.broomYaw - (double)this.field_70177_z)), (float)((double)this.field_70125_A + (this.broomPitch - (double)this.field_70125_A)));
            this.func_70107_b(newHorzVelocity, d5, d11);
            this.field_70159_w *= 0.9900000095367432D;
            this.field_70179_y *= 0.9900000095367432D;
         }
      } else {
         double pitch;
         double MAX_ACCELERATION;
         if (this.field_70153_n != null && this.field_70153_n instanceof EntityLivingBase) {
            newHorzVelocity = (double)((EntityLivingBase)this.field_70153_n).field_70701_bs;
            if (newHorzVelocity > 0.0D) {
               d5 = -Math.sin((double)(this.field_70153_n.field_70177_z * 3.1415927F / 180.0F));
               d11 = Math.cos((double)(this.field_70153_n.field_70177_z * 3.1415927F / 180.0F));
               this.field_70159_w += d5 * this.speedMultiplier * (0.1D + (this.riderHasSoaringBrew ? 0.1D : 0.0D) + (this.riderHasOwlFamiliar ? 0.2D : 0.0D));
               this.field_70179_y += d11 * this.speedMultiplier * (0.1D + (this.riderHasSoaringBrew ? 0.1D : 0.0D) + (this.riderHasOwlFamiliar ? 0.2D : 0.0D));
               pitch = -Math.sin((double)(this.field_70153_n.field_70125_A * 3.1415927F / 180.0F));
               if (pitch > -0.5D && pitch < 0.2D) {
                  pitch = 0.0D;
               } else if (pitch < 0.0D) {
                  pitch *= 0.5D;
               }

               this.field_70181_x = pitch * this.speedMultiplier * 2.0D;
            } else if (newHorzVelocity == 0.0D && (this.riderHasOwlFamiliar || this.riderHasSoaringBrew)) {
               this.field_70159_w *= 0.9D;
               this.field_70179_y *= 0.9D;
            }
         } else if (this.field_70153_n == null) {
            this.riderHasOwlFamiliar = false;
            pitch = this.field_70159_w * 0.9D;
            MAX_ACCELERATION = this.field_70179_y * 0.9D;
            this.field_70159_w = Math.abs(pitch) < 0.01D ? 0.0D : pitch;
            this.field_70179_y = Math.abs(MAX_ACCELERATION) < 0.01D ? 0.0D : MAX_ACCELERATION;
            if (!this.field_70122_E) {
               this.field_70181_x = -0.2D;
            }
         }

         newHorzVelocity = Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
         pitch = 0.9D + (this.riderHasOwlFamiliar ? 0.3D : 0.0D) + (this.riderHasSoaringBrew ? 0.3D : 0.0D);
         if (newHorzVelocity > pitch) {
            d5 = pitch / newHorzVelocity;
            this.field_70159_w *= d5;
            this.field_70179_y *= d5;
            this.field_70181_x *= d5;
            newHorzVelocity = pitch;
         }

         MAX_ACCELERATION = !this.riderHasSoaringBrew && !this.riderHasOwlFamiliar ? 0.35D : 0.35D;
         double MAX_ACCELERATION_FACTOR = MAX_ACCELERATION * 100.0D;
         if (newHorzVelocity > initialHorzVelocity && this.speedMultiplier < MAX_ACCELERATION) {
            this.speedMultiplier += (MAX_ACCELERATION - this.speedMultiplier) / MAX_ACCELERATION_FACTOR;
            if (this.speedMultiplier > MAX_ACCELERATION) {
               this.speedMultiplier = MAX_ACCELERATION;
            }
         } else {
            this.speedMultiplier -= (this.speedMultiplier - 0.07D) / MAX_ACCELERATION_FACTOR;
            if (this.speedMultiplier < 0.07D) {
               this.speedMultiplier = 0.07D;
            }
         }

         this.func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
         this.field_70159_w *= 0.9900000095367432D;
         this.field_70181_x *= 0.9900000095367432D;
         this.field_70179_y *= 0.9900000095367432D;
         this.field_70125_A = 0.0F;
         d5 = (double)this.field_70177_z;
         d11 = this.field_70169_q - this.field_70165_t;
         d10 = this.field_70166_s - this.field_70161_v;
         if (d11 * d11 + d10 * d10 > 0.001D) {
            d5 = (double)((float)(Math.atan2(d10, d11) * 180.0D / 3.141592653589793D));
         }

         double d12 = MathHelper.func_76138_g(d5 - (double)this.field_70177_z);
         this.field_70177_z = (float)((double)this.field_70177_z + d12);
         this.func_70101_b(this.field_70177_z, this.field_70125_A);
         if (!this.field_70170_p.field_72995_K) {
            List list = this.field_70170_p.func_72839_b(this, this.field_70121_D.func_72314_b(0.20000000298023224D, 0.0D, 0.20000000298023224D));
            if (list != null && !list.isEmpty()) {
               for(int l = 0; l < list.size(); ++l) {
                  Entity entity = (Entity)list.get(l);
                  if (entity != this.field_70153_n && entity.func_70104_M() && entity instanceof EntityBroom) {
                     entity.func_70108_f(this);
                  }
               }
            }

            if (this.field_70153_n != null && this.field_70153_n.field_70128_L) {
               this.field_70153_n = null;
            }
         }
      }

   }

   public void func_70043_V() {
      super.func_70043_V();
   }

   protected void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      par1NBTTagCompound.func_74778_a("CustomName", this.getCustomNameTag());
      int brushColor = this.getBrushColor();
      if (brushColor >= 0) {
         par1NBTTagCompound.func_74774_a("BrushColor", Byte.valueOf((byte)brushColor));
      }

   }

   protected void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      if (par1NBTTagCompound.func_74764_b("CustomName") && par1NBTTagCompound.func_74779_i("CustomName").length() > 0) {
         this.setCustomNameTag(par1NBTTagCompound.func_74779_i("CustomName"));
      }

      if (par1NBTTagCompound.func_74764_b("BrushColor") && par1NBTTagCompound.func_74771_c("BrushColor") >= 0) {
         this.setBrushColor(par1NBTTagCompound.func_74771_c("BrushColor"));
      }

   }

   @SideOnly(Side.CLIENT)
   public float func_70053_R() {
      return 0.0F;
   }

   public boolean func_130002_c(EntityPlayer player) {
      if (this.field_70153_n != null && this.field_70153_n instanceof EntityPlayer && this.field_70153_n != player) {
         return true;
      } else if (!this.field_70170_p.field_72995_K && player.func_70694_bm() != null && player.func_70694_bm().func_77973_b() == Items.field_151100_aR) {
         ItemStack itemstack = player.func_70694_bm();
         int i = BlockColored.func_150032_b(itemstack.func_77960_j());
         this.setBrushColor(i);
         if (!player.field_71075_bZ.field_75098_d) {
            --itemstack.field_77994_a;
         }

         if (itemstack.field_77994_a <= 0) {
            player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
         }

         return true;
      } else {
         if (!this.field_70170_p.field_72995_K) {
            this.riderHasOwlFamiliar = Familiar.hasActiveBroomMasteryFamiliar(player);
            this.riderHasSoaringBrew = InfusedBrewEffect.Soaring.isActive(player);
            player.func_70078_a(this);
         }

         return true;
      }
   }

   public void setDamageTaken(float par1) {
      this.field_70180_af.func_75692_b(19, par1);
   }

   public float getDamageTaken() {
      return this.field_70180_af.func_111145_d(19);
   }

   public void setTimeSinceHit(int par1) {
      this.field_70180_af.func_75692_b(17, par1);
   }

   public int getTimeSinceHit() {
      return this.field_70180_af.func_75679_c(17);
   }

   public void setForwardDirection(int par1) {
      this.field_70180_af.func_75692_b(18, par1);
   }

   public int getForwardDirection() {
      return this.field_70180_af.func_75679_c(18);
   }

   @SideOnly(Side.CLIENT)
   public void func_70270_d(boolean par1) {
      this.field_70279_a = par1;
   }

   public static class EventHooks {
      @SubscribeEvent
      public void onLivingFall(LivingFallEvent event) {
         if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.entityLiving;
            if (player.func_70115_ae() && player.field_70154_o instanceof EntityBroom) {
               event.distance = 0.0F;
            }
         }

      }
   }
}
