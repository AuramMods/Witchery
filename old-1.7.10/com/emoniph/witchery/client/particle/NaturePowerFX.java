package com.emoniph.witchery.client.particle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class NaturePowerFX extends EntityFX {
   public static final ResourceLocation particles = new ResourceLocation("witchery:textures/particle/power.png");
   private boolean canMove = false;
   private boolean circling = false;

   public NaturePowerFX(World world, double x, double y, double z) {
      super(world, x, y, z);
      this.field_70145_X = true;
   }

   public void func_70539_a(Tessellator tess, float partialTicks, float par3, float par4, float par5, float par6, float par7) {
      Minecraft.func_71410_x().field_71446_o.func_110577_a(particles);
      GL11.glDepthMask(false);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glAlphaFunc(516, 0.003921569F);
      tess.func_78382_b();
      tess.func_78380_c(this.func_70070_b(partialTicks));
      int typeIndex = 0;
      int par1 = typeIndex + this.field_70546_d * 8 / 20 % 16;
      par1 = par1 > 7 ? 15 - par1 : par1;
      int particleTextureIndexX = par1 % 16;
      int particleTextureIndexY = par1 / 16;
      float f6 = (float)particleTextureIndexX / 16.0F;
      float f7 = f6 + 0.0624375F;
      float f8 = (float)particleTextureIndexY / 16.0F;
      float f9 = f8 + 0.0624375F;
      float scale = 0.1F * this.field_70544_f;
      float x = (float)(this.field_70169_q + (this.field_70165_t - this.field_70169_q) * (double)partialTicks - field_70556_an);
      float y = (float)(this.field_70167_r + (this.field_70163_u - this.field_70167_r) * (double)partialTicks - field_70554_ao);
      float z = (float)(this.field_70166_s + (this.field_70161_v - this.field_70166_s) * (double)partialTicks - field_70555_ap);
      tess.func_78369_a(this.field_70552_h, this.field_70553_i, this.field_70551_j, 1.0F);
      tess.func_78374_a((double)(x - par3 * scale - par6 * scale), (double)(y - par4 * scale), (double)(z - par5 * scale - par7 * scale), (double)f7, (double)f9);
      tess.func_78374_a((double)(x - par3 * scale + par6 * scale), (double)(y + par4 * scale), (double)(z - par5 * scale + par7 * scale), (double)f7, (double)f8);
      tess.func_78374_a((double)(x + par3 * scale + par6 * scale), (double)(y + par4 * scale), (double)(z + par5 * scale + par7 * scale), (double)f6, (double)f8);
      tess.func_78374_a((double)(x + par3 * scale - par6 * scale), (double)(y - par4 * scale), (double)(z + par5 * scale - par7 * scale), (double)f6, (double)f9);
      tess.func_78381_a();
      GL11.glDisable(3042);
      GL11.glDepthMask(true);
      GL11.glAlphaFunc(516, 0.1F);
   }

   public void func_70071_h_() {
      if (!this.field_70170_p.field_72995_K) {
         this.func_70106_y();
      }

      this.field_70169_q = this.field_70165_t;
      this.field_70167_r = this.field_70163_u;
      this.field_70166_s = this.field_70161_v;
      if (this.field_70546_d++ < Math.min(this.field_70547_e, 600) && this.field_70546_d >= 0) {
         if ((double)this.field_70546_d > (double)this.field_70547_e * 0.9D) {
            this.field_70145_X = false;
         }
      } else {
         this.func_70106_y();
      }

      if (!this.field_70128_L && this.canMove) {
         if (this.circling) {
            Vec3 motion = Vec3.func_72443_a(this.field_70159_w, this.field_70181_x, this.field_70179_y);
            motion.func_72442_b(0.5F);
            this.field_70159_w = motion.field_72450_a *= 1.08D;
            this.field_70181_x = motion.field_72448_b *= 0.85D;
            this.field_70179_y = motion.field_72449_c *= 1.08D;
         } else {
            this.field_70181_x -= 0.04D * (double)this.field_70545_g;
         }

         this.func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
         if (this.field_70122_E) {
            this.field_70159_w *= 0.699999988079071D;
            this.field_70179_y *= 0.699999988079071D;
         }
      }

   }

   public int func_70537_b() {
      return 3;
   }

   public NaturePowerFX setMaxAge(int maxAge) {
      this.field_70547_e = maxAge;
      return this;
   }

   public NaturePowerFX setGravity(float gravity) {
      this.field_70545_g = gravity;
      return this;
   }

   public NaturePowerFX setCanMove(boolean canMove) {
      this.canMove = canMove;
      return this;
   }

   public NaturePowerFX setScale(float scale) {
      this.field_70544_f = scale;
      return this;
   }

   public NaturePowerFX setCircling(boolean circling) {
      this.circling = circling;
      return this;
   }
}
