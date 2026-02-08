package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Timer;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelOverlayRenderer {
   private static Field fieldMainModel;
   private static Field fieldTimer;
   private static Method methodRotateCorpse;
   private static Method methodHandleRotationFloat;
   private static Method methodPreRenderCallback;
   private static Timer timer;

   public static float getRenderPartialTicks() {
      if (fieldTimer == null) {
         fieldTimer = ReflectionHelper.findField(Minecraft.class, new String[]{"timer", "field_71428_T", "Q"});

         try {
            Minecraft mc = Minecraft.func_71410_x();
            if (timer == null) {
               timer = (Timer)fieldTimer.get(mc);
            }
         } catch (IllegalAccessException var1) {
         }
      }

      return timer != null ? timer.field_74281_c : 0.0F;
   }

   private static void ensureInitialized(RendererLivingEntity originalRenderer) {
      if (fieldTimer == null) {
         fieldTimer = ReflectionHelper.findField(Minecraft.class, new String[]{"timer", "field_71428_T", "Q"});

         try {
            Minecraft mc = Minecraft.func_71410_x();
            if (timer == null) {
               timer = (Timer)fieldTimer.get(mc);
            }
         } catch (IllegalAccessException var2) {
         }
      }

      if (fieldMainModel == null) {
         fieldMainModel = ReflectionHelper.findField(RendererLivingEntity.class, new String[]{"mainModel", "field_77045_g", "i"});
      }

      if (methodRotateCorpse == null) {
         methodRotateCorpse = ReflectionHelper.findMethod(RendererLivingEntity.class, originalRenderer, new String[]{"rotateCorpse", "func_77043_a", "a"}, new Class[]{EntityLivingBase.class, Float.TYPE, Float.TYPE, Float.TYPE});
      }

      if (methodHandleRotationFloat == null) {
         methodHandleRotationFloat = ReflectionHelper.findMethod(RendererLivingEntity.class, originalRenderer, new String[]{"handleRotationFloat", "func_77044_a", "b"}, new Class[]{EntityLivingBase.class, Float.TYPE});
      }

      if (methodPreRenderCallback == null) {
         methodPreRenderCallback = ReflectionHelper.findMethod(RendererLivingEntity.class, originalRenderer, new String[]{"preRenderCallback", "func_77041_b", "a"}, new Class[]{EntityLivingBase.class, Float.TYPE});
      }

   }

   public static void render(EntityLivingBase entity, double x, double y, double z, RendererLivingEntity originalRenderer) {
      try {
         ensureInitialized(originalRenderer);
         ModelBase mainModel = (ModelBase)fieldMainModel.get(originalRenderer);
         renderModel(entity, x, y, z, originalRenderer, mainModel);
      } catch (IllegalAccessException var9) {
      }

   }

   public static void renderModel(EntityLivingBase entity, double x, double y, double z, RendererLivingEntity originalRenderer, ModelBase model) {
      ensureInitialized(originalRenderer);
      if (timer != null) {
         renderModelAsOverlay(entity, model, x, y, z, timer.field_74281_c, originalRenderer);
      }

   }

   private static void renderModelAsOverlay(EntityLivingBase entity, ModelBase mainModel, double x, double y, double z, float partialRenderTicks, RendererLivingEntity originalRenderer) {
      try {
         GL11.glPushMatrix();
         mainModel.field_78095_p = renderSwingProgress(entity, partialRenderTicks);
         mainModel.field_78093_q = entity.func_70115_ae();
         mainModel.field_78091_s = entity.func_70631_g_();
         float f2 = interpolateRotation(entity.field_70760_ar, entity.field_70761_aq, partialRenderTicks);
         float f3 = interpolateRotation(entity.field_70758_at, entity.field_70759_as, partialRenderTicks);
         float f4;
         if (entity.func_70115_ae() && entity.field_70154_o instanceof EntityLivingBase) {
            EntityLivingBase entitylivingbase1 = (EntityLivingBase)entity.field_70154_o;
            f2 = interpolateRotation(entitylivingbase1.field_70760_ar, entitylivingbase1.field_70761_aq, partialRenderTicks);
            f4 = MathHelper.func_76142_g(f3 - f2);
            if (f4 < -85.0F) {
               f4 = -85.0F;
            }

            if (f4 >= 85.0F) {
               f4 = 85.0F;
            }

            f2 = f3 - f4;
            if (f4 * f4 > 2500.0F) {
               f2 += f4 * 0.2F;
            }
         }

         float f13 = entity.field_70127_C + (entity.field_70125_A - entity.field_70127_C) * partialRenderTicks;
         renderLivingAt(entity, x, y, z);
         f4 = (Float)methodHandleRotationFloat.invoke(originalRenderer, entity, partialRenderTicks);
         methodRotateCorpse.invoke(originalRenderer, entity, f4, f2, partialRenderTicks);
         float f5 = 0.0625F;
         GL11.glEnable(32826);
         GL11.glScalef(-1.0F, -1.0F, 1.0F);
         methodPreRenderCallback.invoke(originalRenderer, entity, partialRenderTicks);
         GL11.glTranslatef(0.0F, -24.0F * f5 - 0.0078125F, 0.0F);
         float f6 = entity.field_70722_aY + (entity.field_70721_aZ - entity.field_70722_aY) * partialRenderTicks;
         float f7 = entity.field_70754_ba - entity.field_70721_aZ * (1.0F - partialRenderTicks);
         if (entity.func_70631_g_()) {
            f7 *= 3.0F;
         }

         if (f6 > 1.0F) {
            f6 = 1.0F;
         }

         GL11.glEnable(3008);
         mainModel.func_78086_a(entity, f7, f6, partialRenderTicks);
         float SCALE = 1.01F;
         GL11.glScalef(1.01F, 1.01F, 1.01F);
         renderModel(entity, f7, f6, f4, f3 - f2, f13, f5, mainModel);
      } catch (IllegalAccessException var22) {
      } catch (InvocationTargetException var23) {
      } finally {
         GL11.glPopMatrix();
      }

   }

   private static float interpolateRotation(float par1, float par2, float partialRenderTicks) {
      float f3;
      for(f3 = par2 - par1; f3 < -180.0F; f3 += 360.0F) {
      }

      while(f3 >= 180.0F) {
         f3 -= 360.0F;
      }

      return par1 + partialRenderTicks * f3;
   }

   private static void renderLivingAt(EntityLivingBase entity, double p_77039_2_, double p_77039_4_, double p_77039_6_) {
      GL11.glTranslatef((float)p_77039_2_, (float)p_77039_4_, (float)p_77039_6_);
      if (entity != null && entity.func_70644_a(Witchery.Potions.RESIZING)) {
         PotionEffect resizing = entity.func_70660_b(Witchery.Potions.RESIZING);
         if (resizing != null) {
            float scale = PotionResizing.getModifiedScaleFactor(entity, resizing.func_76458_c());
            GL11.glScalef(scale, scale, scale);
         }
      }

   }

   private static float renderSwingProgress(EntityLivingBase entity, float partialRenderTicks) {
      return entity.func_70678_g(partialRenderTicks);
   }

   private static void renderModel(EntityLivingBase entity, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_, ModelBase mainModel) {
      if (!entity.func_82150_aj()) {
         mainModel.func_78088_a(entity, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
      } else if (!entity.func_98034_c(Minecraft.func_71410_x().field_71439_g)) {
         GL11.glPushMatrix();
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.15F);
         GL11.glDepthMask(false);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glAlphaFunc(516, 0.003921569F);
         mainModel.func_78088_a(entity, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
         GL11.glDisable(3042);
         GL11.glAlphaFunc(516, 0.1F);
         GL11.glPopMatrix();
         GL11.glDepthMask(true);
      } else {
         mainModel.func_78087_a(p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_, entity);
      }

   }
}
