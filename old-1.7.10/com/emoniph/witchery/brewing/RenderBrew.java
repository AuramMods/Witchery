package com.emoniph.witchery.brewing;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBrew extends RenderSnowball {
   public RenderBrew(Item item) {
      this(item, 0);
   }

   public RenderBrew(Item item, int damageValue) {
      super(item, damageValue);
   }

   public void func_76986_a(Entity entity, double par2, double par4, double par6, float par8, float par9) {
      EntityBrew brew = (EntityBrew)entity;
      IIcon icon = Witchery.Items.BREW.getIcon(brew.getBrew(), 1);
      if (icon != null) {
         GL11.glPushMatrix();
         GL11.glTranslatef((float)par2, (float)par4, (float)par6);
         GL11.glEnable(32826);
         GL11.glScalef(0.5F, 0.5F, 0.5F);
         this.func_110777_b(entity);
         Tessellator tessellator = Tessellator.field_78398_a;
         int color = brew.getColor();
         if (color != -1) {
            float red = (float)(color >> 16 & 255) / 255.0F;
            float green = (float)(color >> 8 & 255) / 255.0F;
            float blue = (float)(color & 255) / 255.0F;
            GL11.glColor3f(red, green, blue);
         }

         if (brew.getIsSpell()) {
            this.drawIcon(tessellator, Witchery.Items.GENERIC.func_77617_a(Witchery.Items.GENERIC.itemQuartzSphere.damageValue));
         } else {
            GL11.glPushMatrix();
            this.drawIcon(tessellator, Witchery.Items.BREW.getIcon(brew.getBrew(), 0));
            GL11.glPopMatrix();
            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            this.drawIcon(tessellator, icon);
         }

         GL11.glDisable(32826);
         GL11.glPopMatrix();
      }

   }

   private void drawIcon(Tessellator tessalator, IIcon icon) {
      float f = icon.func_94209_e();
      float f1 = icon.func_94212_f();
      float f2 = icon.func_94206_g();
      float f3 = icon.func_94210_h();
      float f4 = 1.0F;
      float f5 = 0.5F;
      float f6 = 0.25F;
      GL11.glRotatef(180.0F - this.field_76990_c.field_78735_i, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(-this.field_76990_c.field_78732_j, 1.0F, 0.0F, 0.0F);
      tessalator.func_78382_b();
      tessalator.func_78375_b(0.0F, 1.0F, 0.0F);
      tessalator.func_78374_a((double)(0.0F - f5), (double)(0.0F - f6), 0.0D, (double)f, (double)f3);
      tessalator.func_78374_a((double)(f4 - f5), (double)(0.0F - f6), 0.0D, (double)f1, (double)f3);
      tessalator.func_78374_a((double)(f4 - f5), (double)(f4 - f6), 0.0D, (double)f1, (double)f2);
      tessalator.func_78374_a((double)(0.0F - f5), (double)(f4 - f6), 0.0D, (double)f, (double)f2);
      tessalator.func_78381_a();
   }
}
