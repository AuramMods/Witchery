package com.emoniph.witchery.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemCloth;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderItem3d extends Render {
   private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
   private RenderBlocks renderBlocksRi = new RenderBlocks();
   private Random random = new Random();
   public boolean renderWithColor = true;
   public float zLevel;
   public static boolean renderInFrame;
   private static final String __OBFID = "CL_00001003";
   protected final boolean alwaysFancy;

   public RenderItem3d(boolean alwaysFancy) {
      this.field_76989_e = 0.15F;
      this.field_76987_f = 0.75F;
      this.alwaysFancy = alwaysFancy;
   }

   public void doRender(EntityItem par1EntityItem, double par2, double par4, double par6, float par8, float par9) {
      ItemStack itemstack = par1EntityItem.func_92059_d();
      if (itemstack.func_77973_b() != null) {
         this.func_110777_b(par1EntityItem);
         this.random.setSeed(187L);
         GL11.glPushMatrix();
         float f2 = this.shouldBob() ? MathHelper.func_76126_a(((float)par1EntityItem.field_70292_b + par9) / 10.0F + par1EntityItem.field_70290_d) * 0.1F + 0.1F : 0.0F;
         float f3 = (((float)par1EntityItem.field_70292_b + par9) / 20.0F + par1EntityItem.field_70290_d) * 57.295776F;
         byte b0 = 1;
         if (par1EntityItem.func_92059_d().field_77994_a > 1) {
            b0 = 2;
         }

         if (par1EntityItem.func_92059_d().field_77994_a > 5) {
            b0 = 3;
         }

         if (par1EntityItem.func_92059_d().field_77994_a > 20) {
            b0 = 4;
         }

         if (par1EntityItem.func_92059_d().field_77994_a > 40) {
            b0 = 5;
         }

         byte b0 = this.getMiniBlockCount(itemstack, b0);
         GL11.glTranslatef((float)par2, (float)par4 + f2, (float)par6);
         GL11.glEnable(32826);
         if (!ForgeHooksClient.renderEntityItem(par1EntityItem, itemstack, f2, f3, this.random, this.field_76990_c.field_78724_e, this.field_147909_c, b0)) {
            float f6;
            float f7;
            int k;
            int l;
            float f8;
            if (itemstack.func_94608_d() == 0 && itemstack.func_77973_b() instanceof ItemBlock && RenderBlocks.func_147739_a(Block.func_149634_a(itemstack.func_77973_b()).func_149645_b())) {
               Block block = Block.func_149634_a(itemstack.func_77973_b());
               GL11.glRotatef(f3, 0.0F, 1.0F, 0.0F);
               if (renderInFrame) {
                  GL11.glScalef(1.25F, 1.25F, 1.25F);
                  GL11.glTranslatef(0.0F, 0.05F, 0.0F);
                  GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
               }

               float f9 = 0.25F;
               k = block.func_149645_b();
               if (k == 1 || k == 19 || k == 12 || k == 2) {
                  f9 = 0.5F;
               }

               if (block.func_149701_w() > 0) {
                  GL11.glAlphaFunc(516, 0.1F);
                  GL11.glEnable(3042);
                  OpenGlHelper.func_148821_a(770, 771, 1, 0);
               }

               GL11.glScalef(f9, f9, f9);

               for(l = 0; l < b0; ++l) {
                  GL11.glPushMatrix();
                  if (l > 0) {
                     f6 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / f9;
                     f7 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / f9;
                     f8 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / f9;
                     GL11.glTranslatef(f6, f7, f8);
                  }

                  this.renderBlocksRi.func_147800_a(block, itemstack.func_77960_j(), 1.0F);
                  GL11.glPopMatrix();
               }

               if (block.func_149701_w() > 0) {
                  GL11.glDisable(3042);
               }
            } else {
               float f5;
               if (itemstack.func_77973_b().func_77623_v()) {
                  if (renderInFrame) {
                     GL11.glScalef(0.5128205F, 0.5128205F, 0.5128205F);
                     GL11.glTranslatef(0.0F, -0.05F, 0.0F);
                  } else {
                     GL11.glScalef(0.5F, 0.5F, 0.5F);
                  }

                  for(int j = 0; j < itemstack.func_77973_b().getRenderPasses(itemstack.func_77960_j()); ++j) {
                     this.random.setSeed(187L);
                     IIcon iicon1 = itemstack.func_77973_b().getIcon(itemstack, j);
                     if (this.renderWithColor) {
                        k = itemstack.func_77973_b().func_82790_a(itemstack, j);
                        f5 = (float)(k >> 16 & 255) / 255.0F;
                        f6 = (float)(k >> 8 & 255) / 255.0F;
                        f7 = (float)(k & 255) / 255.0F;
                        GL11.glColor4f(f5, f6, f7, 1.0F);
                        this.renderDroppedItem(par1EntityItem, iicon1, b0, par9, f5, f6, f7, j);
                     } else {
                        this.renderDroppedItem(par1EntityItem, iicon1, b0, par9, 1.0F, 1.0F, 1.0F, j);
                     }
                  }
               } else {
                  if (itemstack != null && itemstack.func_77973_b() instanceof ItemCloth) {
                     GL11.glAlphaFunc(516, 0.1F);
                     GL11.glEnable(3042);
                     OpenGlHelper.func_148821_a(770, 771, 1, 0);
                  }

                  if (renderInFrame) {
                     GL11.glScalef(0.5128205F, 0.5128205F, 0.5128205F);
                     GL11.glTranslatef(0.0F, -0.05F, 0.0F);
                  } else {
                     GL11.glScalef(0.5F, 0.5F, 0.5F);
                  }

                  IIcon iicon = itemstack.func_77954_c();
                  if (this.renderWithColor) {
                     l = itemstack.func_77973_b().func_82790_a(itemstack, 0);
                     f8 = (float)(l >> 16 & 255) / 255.0F;
                     f5 = (float)(l >> 8 & 255) / 255.0F;
                     f6 = (float)(l & 255) / 255.0F;
                     this.renderDroppedItem(par1EntityItem, iicon, b0, par9, f8, f5, f6);
                  } else {
                     this.renderDroppedItem(par1EntityItem, iicon, b0, par9, 1.0F, 1.0F, 1.0F);
                  }

                  if (itemstack != null && itemstack.func_77973_b() instanceof ItemCloth) {
                     GL11.glDisable(3042);
                  }
               }
            }
         }

         GL11.glDisable(32826);
         GL11.glPopMatrix();
      }

   }

   protected ResourceLocation getEntityTexture(EntityItem par1EntityItem) {
      return this.field_76990_c.field_78724_e.func_130087_a(par1EntityItem.func_92059_d().func_94608_d());
   }

   private void renderDroppedItem(EntityItem par1EntityItem, IIcon par2Icon, int par3, float par4, float par5, float par6, float par7) {
      this.renderDroppedItem(par1EntityItem, par2Icon, par3, par4, par5, par6, par7, 0);
   }

   private void renderDroppedItem(EntityItem par1EntityItem, IIcon par2Icon, int par3, float par4, float par5, float par6, float par7, int pass) {
      Tessellator tessellator = Tessellator.field_78398_a;
      if (par2Icon == null) {
         TextureManager texturemanager = Minecraft.func_71410_x().func_110434_K();
         ResourceLocation resourcelocation = texturemanager.func_130087_a(par1EntityItem.func_92059_d().func_94608_d());
         par2Icon = ((TextureMap)texturemanager.func_110581_b(resourcelocation)).func_110572_b("missingno");
      }

      float f14 = ((IIcon)par2Icon).func_94209_e();
      float f15 = ((IIcon)par2Icon).func_94212_f();
      float f4 = ((IIcon)par2Icon).func_94206_g();
      float f5 = ((IIcon)par2Icon).func_94210_h();
      float f6 = 1.0F;
      float f7 = 0.5F;
      float f8 = 0.25F;
      float f10;
      if (!this.alwaysFancy && !this.field_76990_c.field_78733_k.field_74347_j) {
         for(int l = 0; l < par3; ++l) {
            GL11.glPushMatrix();
            if (l > 0) {
               f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
               float f16 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
               float f17 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
               GL11.glTranslatef(f10, f16, f17);
            }

            if (!renderInFrame) {
               GL11.glRotatef(180.0F - this.field_76990_c.field_78735_i, 0.0F, 1.0F, 0.0F);
            }

            GL11.glColor4f(par5, par6, par7, 1.0F);
            tessellator.func_78382_b();
            tessellator.func_78375_b(0.0F, 1.0F, 0.0F);
            tessellator.func_78374_a((double)(0.0F - f7), (double)(0.0F - f8), 0.0D, (double)f14, (double)f5);
            tessellator.func_78374_a((double)(f6 - f7), (double)(0.0F - f8), 0.0D, (double)f15, (double)f5);
            tessellator.func_78374_a((double)(f6 - f7), (double)(1.0F - f8), 0.0D, (double)f15, (double)f4);
            tessellator.func_78374_a((double)(0.0F - f7), (double)(1.0F - f8), 0.0D, (double)f14, (double)f4);
            tessellator.func_78381_a();
            GL11.glPopMatrix();
         }
      } else {
         GL11.glPushMatrix();
         if (renderInFrame) {
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
         } else {
            GL11.glRotatef((((float)par1EntityItem.field_70292_b + par4) / 20.0F + par1EntityItem.field_70290_d) * 57.295776F, 0.0F, 1.0F, 0.0F);
         }

         float f9 = 0.0625F;
         f10 = 0.021875F;
         ItemStack itemstack = par1EntityItem.func_92059_d();
         int j = itemstack.field_77994_a;
         byte b0;
         if (j < 2) {
            b0 = 1;
         } else if (j < 16) {
            b0 = 2;
         } else if (j < 32) {
            b0 = 3;
         } else {
            b0 = 4;
         }

         byte b0 = this.getMiniItemCount(itemstack, b0);
         GL11.glTranslatef(-f7, -f8, -((f9 + f10) * (float)b0 / 2.0F));

         for(int k = 0; k < b0; ++k) {
            float f11;
            float f12;
            float f13;
            if (k > 0 && this.shouldSpreadItems()) {
               f11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
               f12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
               f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
               GL11.glTranslatef(f11, f12, f9 + f10);
            } else {
               GL11.glTranslatef(0.0F, 0.0F, f9 + f10);
            }

            if (itemstack.func_94608_d() == 0) {
               this.func_110776_a(TextureMap.field_110575_b);
            } else {
               this.func_110776_a(TextureMap.field_110576_c);
            }

            GL11.glColor4f(par5, par6, par7, 1.0F);
            ItemRenderer.func_78439_a(tessellator, f15, f4, f14, f5, ((IIcon)par2Icon).func_94211_a(), ((IIcon)par2Icon).func_94216_b(), f9);
            if (itemstack.hasEffect(pass)) {
               GL11.glDepthFunc(514);
               GL11.glDisable(2896);
               this.field_76990_c.field_78724_e.func_110577_a(RES_ITEM_GLINT);
               GL11.glEnable(3042);
               GL11.glBlendFunc(768, 1);
               f11 = 0.76F;
               GL11.glColor4f(0.5F * f11, 0.25F * f11, 0.8F * f11, 1.0F);
               GL11.glMatrixMode(5890);
               GL11.glPushMatrix();
               f12 = 0.125F;
               GL11.glScalef(f12, f12, f12);
               f13 = (float)(Minecraft.func_71386_F() % 3000L) / 3000.0F * 8.0F;
               GL11.glTranslatef(f13, 0.0F, 0.0F);
               GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
               ItemRenderer.func_78439_a(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 255, 255, f9);
               GL11.glPopMatrix();
               GL11.glPushMatrix();
               GL11.glScalef(f12, f12, f12);
               f13 = (float)(Minecraft.func_71386_F() % 4873L) / 4873.0F * 8.0F;
               GL11.glTranslatef(-f13, 0.0F, 0.0F);
               GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
               ItemRenderer.func_78439_a(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 255, 255, f9);
               GL11.glPopMatrix();
               GL11.glMatrixMode(5888);
               GL11.glDisable(3042);
               GL11.glEnable(2896);
               GL11.glDepthFunc(515);
            }
         }

         GL11.glPopMatrix();
      }

   }

   public void renderItemIntoGUI(FontRenderer par1FontRenderer, TextureManager par2TextureManager, ItemStack par3ItemStack, int par4, int par5) {
      this.renderItemIntoGUI(par1FontRenderer, par2TextureManager, par3ItemStack, par4, par5, false);
   }

   public void renderItemIntoGUI(FontRenderer par1FontRenderer, TextureManager par2TextureManager, ItemStack par3ItemStack, int par4, int par5, boolean renderEffect) {
      int k = par3ItemStack.func_77960_j();
      Object object = par3ItemStack.func_77954_c();
      GL11.glEnable(3042);
      OpenGlHelper.func_148821_a(770, 771, 1, 0);
      int l;
      float f;
      float f3;
      float f4;
      if (par3ItemStack.func_94608_d() == 0 && RenderBlocks.func_147739_a(Block.func_149634_a(par3ItemStack.func_77973_b()).func_149645_b())) {
         par2TextureManager.func_110577_a(TextureMap.field_110575_b);
         Block block = Block.func_149634_a(par3ItemStack.func_77973_b());
         GL11.glPushMatrix();
         GL11.glTranslatef((float)(par4 - 2), (float)(par5 + 3), -3.0F + this.zLevel);
         GL11.glScalef(10.0F, 10.0F, 10.0F);
         GL11.glTranslatef(1.0F, 0.5F, 1.0F);
         GL11.glScalef(1.0F, 1.0F, -1.0F);
         GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
         GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
         l = par3ItemStack.func_77973_b().func_82790_a(par3ItemStack, 0);
         f3 = (float)(l >> 16 & 255) / 255.0F;
         f4 = (float)(l >> 8 & 255) / 255.0F;
         f = (float)(l & 255) / 255.0F;
         if (this.renderWithColor) {
            GL11.glColor4f(f3, f4, f, 1.0F);
         }

         GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
         this.renderBlocksRi.field_147844_c = this.renderWithColor;
         this.renderBlocksRi.func_147800_a(block, k, 1.0F);
         this.renderBlocksRi.field_147844_c = true;
         GL11.glPopMatrix();
      } else if (par3ItemStack.func_77973_b().func_77623_v()) {
         GL11.glDisable(2896);
         GL11.glEnable(3008);
         par2TextureManager.func_110577_a(TextureMap.field_110576_c);
         GL11.glDisable(3008);
         GL11.glDisable(3553);
         GL11.glEnable(3042);
         OpenGlHelper.func_148821_a(0, 0, 0, 0);
         GL11.glColorMask(false, false, false, true);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         Tessellator tessellator = Tessellator.field_78398_a;
         tessellator.func_78382_b();
         tessellator.func_78378_d(-1);
         tessellator.func_78377_a((double)(par4 - 2), (double)(par5 + 18), (double)this.zLevel);
         tessellator.func_78377_a((double)(par4 + 18), (double)(par5 + 18), (double)this.zLevel);
         tessellator.func_78377_a((double)(par4 + 18), (double)(par5 - 2), (double)this.zLevel);
         tessellator.func_78377_a((double)(par4 - 2), (double)(par5 - 2), (double)this.zLevel);
         tessellator.func_78381_a();
         GL11.glColorMask(true, true, true, true);
         GL11.glEnable(3553);
         GL11.glEnable(3008);
         Item item = par3ItemStack.func_77973_b();

         for(l = 0; l < item.getRenderPasses(k); ++l) {
            OpenGlHelper.func_148821_a(770, 771, 1, 0);
            par2TextureManager.func_110577_a(item.func_94901_k() == 0 ? TextureMap.field_110575_b : TextureMap.field_110576_c);
            IIcon iicon = item.getIcon(par3ItemStack, l);
            int i1 = par3ItemStack.func_77973_b().func_82790_a(par3ItemStack, l);
            f = (float)(i1 >> 16 & 255) / 255.0F;
            float f1 = (float)(i1 >> 8 & 255) / 255.0F;
            float f2 = (float)(i1 & 255) / 255.0F;
            if (this.renderWithColor) {
               GL11.glColor4f(f, f1, f2, 1.0F);
            }

            GL11.glDisable(2896);
            GL11.glEnable(3008);
            this.renderIcon(par4, par5, iicon, 16, 16);
            GL11.glDisable(3008);
            GL11.glEnable(2896);
            if (renderEffect && par3ItemStack.hasEffect(l)) {
               this.renderEffect(par2TextureManager, par4, par5);
            }
         }

         GL11.glDisable(3008);
         GL11.glEnable(2896);
      } else {
         GL11.glDisable(2896);
         ResourceLocation resourcelocation = par2TextureManager.func_130087_a(par3ItemStack.func_94608_d());
         par2TextureManager.func_110577_a(resourcelocation);
         if (object == null) {
            object = ((TextureMap)Minecraft.func_71410_x().func_110434_K().func_110581_b(resourcelocation)).func_110572_b("missingno");
         }

         l = par3ItemStack.func_77973_b().func_82790_a(par3ItemStack, 0);
         f3 = (float)(l >> 16 & 255) / 255.0F;
         f4 = (float)(l >> 8 & 255) / 255.0F;
         f = (float)(l & 255) / 255.0F;
         if (this.renderWithColor) {
            GL11.glColor4f(f3, f4, f, 1.0F);
         }

         GL11.glDisable(2896);
         GL11.glEnable(3008);
         this.renderIcon(par4, par5, (IIcon)object, 16, 16);
         GL11.glDisable(3008);
         GL11.glEnable(2896);
         if (renderEffect && par3ItemStack.hasEffect(0)) {
            this.renderEffect(par2TextureManager, par4, par5);
         }

         GL11.glEnable(2896);
      }

      GL11.glEnable(2884);
   }

   public void renderItemAndEffectIntoGUI(FontRenderer par1FontRenderer, TextureManager par2TextureManager, final ItemStack par3ItemStack, int par4, int par5) {
      if (par3ItemStack != null) {
         this.zLevel += 50.0F;

         try {
            if (!ForgeHooksClient.renderInventoryItem(this.field_147909_c, par2TextureManager, par3ItemStack, this.renderWithColor, this.zLevel, (float)par4, (float)par5)) {
               this.renderItemIntoGUI(par1FontRenderer, par2TextureManager, par3ItemStack, par4, par5, true);
            }
         } catch (Throwable var9) {
            CrashReport crashreport = CrashReport.func_85055_a(var9, "Rendering item");
            CrashReportCategory crashreportcategory = crashreport.func_85058_a("Item being rendered");
            crashreportcategory.func_71500_a("Item Type", new Callable() {
               private static final String __OBFID = "CL_00001004";

               public String call() {
                  return String.valueOf(par3ItemStack.func_77973_b());
               }
            });
            crashreportcategory.func_71500_a("Item Aux", new Callable() {
               private static final String __OBFID = "CL_00001005";

               public String call() {
                  return String.valueOf(par3ItemStack.func_77960_j());
               }
            });
            crashreportcategory.func_71500_a("Item NBT", new Callable() {
               private static final String __OBFID = "CL_00001006";

               public String call() {
                  return String.valueOf(par3ItemStack.func_77978_p());
               }
            });
            crashreportcategory.func_71500_a("Item Foil", new Callable() {
               private static final String __OBFID = "CL_00001007";

               public String call() {
                  return String.valueOf(par3ItemStack.func_77962_s());
               }
            });
            throw new ReportedException(crashreport);
         }

         this.zLevel -= 50.0F;
      }

   }

   public void renderEffect(TextureManager manager, int x, int y) {
      GL11.glDepthFunc(514);
      GL11.glDisable(2896);
      GL11.glDepthMask(false);
      manager.func_110577_a(RES_ITEM_GLINT);
      GL11.glEnable(3008);
      GL11.glEnable(3042);
      GL11.glColor4f(0.5F, 0.25F, 0.8F, 1.0F);
      this.renderGlint(x * 431278612 + y * 32178161, x - 2, y - 2, 20, 20);
      GL11.glDepthMask(true);
      GL11.glDisable(3042);
      GL11.glDisable(3008);
      GL11.glEnable(2896);
      GL11.glDepthFunc(515);
   }

   private void renderGlint(int par1, int par2, int par3, int par4, int par5) {
      for(int j1 = 0; j1 < 2; ++j1) {
         OpenGlHelper.func_148821_a(772, 1, 0, 0);
         float f = 0.00390625F;
         float f1 = 0.00390625F;
         float f2 = (float)(Minecraft.func_71386_F() % (long)(3000 + j1 * 1873)) / (3000.0F + (float)(j1 * 1873)) * 256.0F;
         float f3 = 0.0F;
         Tessellator tessellator = Tessellator.field_78398_a;
         float f4 = 4.0F;
         if (j1 == 1) {
            f4 = -1.0F;
         }

         tessellator.func_78382_b();
         tessellator.func_78374_a((double)(par2 + 0), (double)(par3 + par5), (double)this.zLevel, (double)((f2 + (float)par5 * f4) * f), (double)((f3 + (float)par5) * f1));
         tessellator.func_78374_a((double)(par2 + par4), (double)(par3 + par5), (double)this.zLevel, (double)((f2 + (float)par4 + (float)par5 * f4) * f), (double)((f3 + (float)par5) * f1));
         tessellator.func_78374_a((double)(par2 + par4), (double)(par3 + 0), (double)this.zLevel, (double)((f2 + (float)par4) * f), (double)((f3 + 0.0F) * f1));
         tessellator.func_78374_a((double)(par2 + 0), (double)(par3 + 0), (double)this.zLevel, (double)((f2 + 0.0F) * f), (double)((f3 + 0.0F) * f1));
         tessellator.func_78381_a();
      }

   }

   public void renderItemOverlayIntoGUI(FontRenderer par1FontRenderer, TextureManager par2TextureManager, ItemStack par3ItemStack, int par4, int par5) {
      this.renderItemOverlayIntoGUI(par1FontRenderer, par2TextureManager, par3ItemStack, par4, par5, (String)null);
   }

   public void renderItemOverlayIntoGUI(FontRenderer par1FontRenderer, TextureManager par2TextureManager, ItemStack par3ItemStack, int par4, int par5, String par6Str) {
      if (par3ItemStack != null) {
         if (par3ItemStack.field_77994_a > 1 || par6Str != null) {
            String s1 = par6Str == null ? String.valueOf(par3ItemStack.field_77994_a) : par6Str;
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            GL11.glDisable(3042);
            par1FontRenderer.func_78261_a(s1, par4 + 19 - 2 - par1FontRenderer.func_78256_a(s1), par5 + 6 + 3, 16777215);
            GL11.glEnable(2896);
            GL11.glEnable(2929);
         }

         if (par3ItemStack.func_77973_b().showDurabilityBar(par3ItemStack)) {
            double health = par3ItemStack.func_77973_b().getDurabilityForDisplay(par3ItemStack);
            int j1 = (int)Math.round(13.0D - health * 13.0D);
            int k = (int)Math.round(255.0D - health * 255.0D);
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            GL11.glDisable(3553);
            GL11.glDisable(3008);
            GL11.glDisable(3042);
            Tessellator tessellator = Tessellator.field_78398_a;
            int l = 255 - k << 16 | k << 8;
            int i1 = (255 - k) / 4 << 16 | 16128;
            this.renderQuad(tessellator, par4 + 2, par5 + 13, 13, 2, 0);
            this.renderQuad(tessellator, par4 + 2, par5 + 13, 12, 1, i1);
            this.renderQuad(tessellator, par4 + 2, par5 + 13, j1, 1, l);
            GL11.glEnable(3553);
            GL11.glEnable(2896);
            GL11.glEnable(2929);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         }
      }

   }

   private void renderQuad(Tessellator par1Tessellator, int par2, int par3, int par4, int par5, int par6) {
      par1Tessellator.func_78382_b();
      par1Tessellator.func_78378_d(par6);
      par1Tessellator.func_78377_a((double)(par2 + 0), (double)(par3 + 0), 0.0D);
      par1Tessellator.func_78377_a((double)(par2 + 0), (double)(par3 + par5), 0.0D);
      par1Tessellator.func_78377_a((double)(par2 + par4), (double)(par3 + par5), 0.0D);
      par1Tessellator.func_78377_a((double)(par2 + par4), (double)(par3 + 0), 0.0D);
      par1Tessellator.func_78381_a();
   }

   public void renderIcon(int par1, int par2, IIcon par3Icon, int par4, int par5) {
      Tessellator tessellator = Tessellator.field_78398_a;
      tessellator.func_78382_b();
      tessellator.func_78374_a((double)(par1 + 0), (double)(par2 + par5), (double)this.zLevel, (double)par3Icon.func_94209_e(), (double)par3Icon.func_94210_h());
      tessellator.func_78374_a((double)(par1 + par4), (double)(par2 + par5), (double)this.zLevel, (double)par3Icon.func_94212_f(), (double)par3Icon.func_94210_h());
      tessellator.func_78374_a((double)(par1 + par4), (double)(par2 + 0), (double)this.zLevel, (double)par3Icon.func_94212_f(), (double)par3Icon.func_94206_g());
      tessellator.func_78374_a((double)(par1 + 0), (double)(par2 + 0), (double)this.zLevel, (double)par3Icon.func_94209_e(), (double)par3Icon.func_94206_g());
      tessellator.func_78381_a();
   }

   protected ResourceLocation func_110775_a(Entity par1Entity) {
      return this.getEntityTexture((EntityItem)par1Entity);
   }

   public void func_76986_a(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRender((EntityItem)par1Entity, par2, par4, par6, par8, par9);
   }

   public boolean shouldSpreadItems() {
      return true;
   }

   public boolean shouldBob() {
      return true;
   }

   public byte getMiniBlockCount(ItemStack stack, byte original) {
      return original;
   }

   public byte getMiniItemCount(ItemStack stack, byte original) {
      return original;
   }
}
