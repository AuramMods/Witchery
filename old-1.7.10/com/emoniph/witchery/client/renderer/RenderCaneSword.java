package com.emoniph.witchery.client.renderer;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.client.model.ModelCaneSword;
import com.emoniph.witchery.util.Config;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderCaneSword implements IItemRenderer {
   private final ModelCaneSword model = new ModelCaneSword();
   private static final ResourceLocation TEXTURE_URL = new ResourceLocation("witchery", "textures/entities/canesword.png");
   double rx = 100.0D;
   double ry = -51.0D;
   double rz = -81.0D;
   double tx = 0.125D;
   double ty = 0.12D;
   double tz = -0.85D;
   double scale = 1.0D;
   private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

   public boolean handleRenderType(ItemStack item, ItemRenderType type) {
      switch(type) {
      case EQUIPPED:
      case EQUIPPED_FIRST_PERSON:
         return true;
      default:
         return false;
      }
   }

   public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
      return false;
   }

   public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
      switch(type) {
      case EQUIPPED:
      case EQUIPPED_FIRST_PERSON:
         GL11.glPushMatrix();
         Minecraft.func_71410_x().func_110434_K().func_110577_a(TEXTURE_URL);
         GL11.glRotatef((float)this.rx, 1.0F, 0.0F, 0.0F);
         GL11.glRotatef((float)this.ry + 70.0F, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef((float)this.rz - 5.0F, 0.0F, 0.0F, 1.0F);
         GL11.glTranslatef((float)this.tx + 0.35F, (float)this.ty + 0.0F, (float)this.tz + 0.85F);
         float SCALE = 1.0F;
         GL11.glScalef(1.0F, 1.0F, 1.0F);
         if (data.length > 1 && data[1] != null) {
            boolean deployed = data[1] instanceof EntityLivingBase ? Witchery.Items.CANE_SWORD.isDrawn((EntityLivingBase)data[1]) : false;
            if (!(data[1] instanceof EntityPlayer)) {
               if (deployed) {
                  GL11.glRotatef(80.0F, 1.0F, 0.0F, 0.0F);
                  GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
                  GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);
                  GL11.glTranslatef(0.0F, -0.3F, -0.5F);
               }

               this.renderModel((Entity)data[1], false, deployed, item);
            } else {
               EntityPlayer player = (EntityPlayer)data[1];
               if ((EntityPlayer)data[1] != Minecraft.func_71410_x().field_71451_h || Minecraft.func_71410_x().field_71474_y.field_74320_O != 0 || (Minecraft.func_71410_x().field_71462_r instanceof GuiInventory || Minecraft.func_71410_x().field_71462_r instanceof GuiContainerCreative) && RenderManager.field_78727_a.field_78735_i == 180.0F) {
                  if (deployed) {
                     GL11.glRotatef(80.0F, 1.0F, 0.0F, 0.0F);
                     GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
                     GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);
                     GL11.glTranslatef(0.0F, -0.3F, -0.5F);
                  }

                  this.renderModel(player, false, deployed, item);
               } else {
                  if (deployed) {
                     GL11.glTranslatef(0.4F, 0.3F, -0.2F);
                     GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                     GL11.glRotatef(1.0F, 0.0F, 0.0F, 1.0F);
                     GL11.glRotatef(20.0F, 0.0F, 1.0F, 0.0F);
                  } else {
                     GL11.glRotatef(70.0F, 1.0F, 0.0F, 0.0F);
                     GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
                     GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);
                     GL11.glTranslatef(0.0F, -0.5F, -0.9F);
                  }

                  this.renderModel(player, true, deployed, item);
               }
            }
         }

         GL11.glPopMatrix();
      default:
      }
   }

   private void renderModel(Entity player, boolean firstPerson, boolean deployed, ItemStack item) {
      this.model.render(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, firstPerson, deployed);
      Minecraft mc = Minecraft.func_71410_x();
      if (mc.field_71474_y.field_74347_j && Config.instance().render3dGlintEffect && item != null && item.func_77948_v()) {
         float f9 = (float)player.field_70173_aa;
         mc.field_71446_o.func_110577_a(RES_ITEM_GLINT);
         GL11.glEnable(3042);
         float f10 = 0.5F;
         GL11.glColor4f(f10, f10, f10, 1.0F);
         GL11.glDepthFunc(514);
         GL11.glDepthMask(false);

         for(int k = 0; k < 2; ++k) {
            GL11.glDisable(2896);
            float f11 = 0.76F;
            GL11.glColor4f(0.5F * f11, 0.25F * f11, 0.8F * f11, 1.0F);
            GL11.glBlendFunc(768, 1);
            GL11.glMatrixMode(5890);
            GL11.glLoadIdentity();
            float f12 = f9 * (0.001F + (float)k * 0.003F) * 20.0F;
            float f13 = 0.33333334F;
            GL11.glScalef(f13, f13, f13);
            GL11.glRotatef(30.0F - (float)k * 60.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(0.0F, f12, 0.0F);
            GL11.glMatrixMode(5888);
            this.model.render(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, firstPerson, deployed);
         }

         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glMatrixMode(5890);
         GL11.glDepthMask(true);
         GL11.glLoadIdentity();
         GL11.glMatrixMode(5888);
         GL11.glEnable(2896);
         GL11.glDisable(3042);
         GL11.glDepthFunc(515);
      }

   }
}
