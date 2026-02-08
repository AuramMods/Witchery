package com.emoniph.witchery.client.renderer;

import com.emoniph.witchery.client.model.ModelGoblinMog;
import com.emoniph.witchery.entity.EntityGoblinMog;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderGoblinMog extends RenderLiving {
   public ModelGoblinMog modelBipedMain;
   protected float field_77070_b;
   protected ModelGoblinMog field_82423_g;
   protected ModelGoblinMog field_82425_h;
   private static final Map field_110859_k = Maps.newHashMap();
   public static String[] bipedArmorFilenamePrefix = new String[]{"leather", "chainmail", "iron", "diamond", "gold"};
   private static final ResourceLocation TEXTURE_URL = new ResourceLocation("witchery", "textures/entities/mog.png");

   public RenderGoblinMog(ModelGoblinMog par1ModelBiped, float par2) {
      this(par1ModelBiped, par2, 1.0F);
   }

   public RenderGoblinMog(ModelGoblinMog par1ModelBiped, float par2, float par3) {
      super(par1ModelBiped, par2);
      this.modelBipedMain = par1ModelBiped;
      this.field_77070_b = par3;
      this.func_82421_b();
   }

   protected void func_82421_b() {
      this.field_82423_g = new ModelGoblinMog(1.0F);
      this.field_82425_h = new ModelGoblinMog(0.5F);
   }

   protected int shouldRenderPass(EntityLiving par1EntityLiving, int par2, float par3) {
      ItemStack itemstack = par1EntityLiving.func_130225_q(3 - par2);
      if (itemstack != null) {
         Item item = itemstack.func_77973_b();
         if (item instanceof ItemArmor) {
            ItemArmor itemarmor = (ItemArmor)item;
            this.func_110776_a(getArmorResource(par1EntityLiving, itemstack, par2, (String)null));
            ModelGoblinMog modelbiped = par2 == 2 ? this.field_82425_h : this.field_82423_g;
            modelbiped.bipedHead.field_78806_j = par2 == 0;
            modelbiped.bipedBody.field_78806_j = par2 == 1 || par2 == 2;
            modelbiped.bipedRightArm.field_78806_j = par2 == 1;
            modelbiped.bipedLeftArm.field_78806_j = par2 == 1;
            modelbiped.bipedRightLeg.field_78806_j = par2 == 2 || par2 == 3;
            modelbiped.bipedLeftLeg.field_78806_j = par2 == 2 || par2 == 3;
            this.func_77042_a(modelbiped);
            modelbiped.field_78095_p = this.field_77045_g.field_78095_p;
            modelbiped.field_78093_q = this.field_77045_g.field_78093_q;
            modelbiped.field_78091_s = this.field_77045_g.field_78091_s;
            int j = itemarmor.func_82814_b(itemstack);
            if (j != -1) {
               float f1 = (float)(j >> 16 & 255) / 255.0F;
               float f2 = (float)(j >> 8 & 255) / 255.0F;
               float f3 = (float)(j & 255) / 255.0F;
               GL11.glColor3f(f1, f2, f3);
               if (itemstack.func_77948_v()) {
                  return 31;
               }

               return 16;
            }

            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            if (itemstack.func_77948_v()) {
               return 15;
            }

            return 1;
         }
      }

      return -1;
   }

   protected void func_82408_c(EntityLiving par1EntityLivingBase, int par2, float par3) {
      ItemStack itemstack = par1EntityLivingBase.func_130225_q(3 - par2);
      if (itemstack != null) {
         Item item = itemstack.func_77973_b();
         if (item instanceof ItemArmor) {
            this.func_110776_a(getArmorResource(par1EntityLivingBase, itemstack, par2, "overlay"));
            float f1 = 1.0F;
            GL11.glColor3f(1.0F, 1.0F, 1.0F);
         }
      }

   }

   public void func_76986_a(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9) {
      GL11.glColor3f(1.0F, 1.0F, 1.0F);
      ItemStack itemstack = par1EntityLiving.func_70694_bm();
      this.func_82420_a(par1EntityLiving, itemstack);
      double d3 = par4 - (double)par1EntityLiving.field_70129_M;
      BossStatus.func_82824_a((EntityGoblinMog)par1EntityLiving, true);
      if (par1EntityLiving.func_70093_af()) {
         d3 -= 0.125D;
      }

      super.func_76986_a(par1EntityLiving, par2, d3, par6, par8, par9);
      this.field_82423_g.aimedBow = this.field_82425_h.aimedBow = this.modelBipedMain.aimedBow = false;
      this.field_82423_g.isSneak = this.field_82425_h.isSneak = this.modelBipedMain.isSneak = false;
      this.field_82423_g.heldItemRight = this.field_82425_h.heldItemRight = this.modelBipedMain.heldItemRight = 0;
   }

   protected ResourceLocation getEntityTexture(EntityLiving entity) {
      return TEXTURE_URL;
   }

   protected void func_82420_a(EntityLiving par1EntityLiving, ItemStack par2ItemStack) {
      this.field_82423_g.heldItemRight = this.field_82425_h.heldItemRight = this.modelBipedMain.heldItemRight = par2ItemStack != null ? 1 : 0;
      this.field_82423_g.isSneak = this.field_82425_h.isSneak = this.modelBipedMain.isSneak = par1EntityLiving.func_70093_af();
   }

   protected void renderEquippedItems(EntityLiving par1EntityLiving, float par2) {
      GL11.glColor3f(1.0F, 1.0F, 1.0F);
      super.func_77029_c(par1EntityLiving, par2);
      ItemStack itemstack = par1EntityLiving.func_70694_bm();
      ItemStack itemstack1 = par1EntityLiving.func_130225_q(3);
      Item item;
      float f1;
      IItemRenderer customRenderer;
      boolean is3D;
      if (itemstack1 != null) {
         GL11.glPushMatrix();
         this.modelBipedMain.bipedHead.func_78794_c(0.0625F);
         item = itemstack1.func_77973_b();
         customRenderer = MinecraftForgeClient.getItemRenderer(itemstack1, ItemRenderType.EQUIPPED);
         is3D = customRenderer != null && customRenderer.shouldUseRenderHelper(ItemRenderType.EQUIPPED, itemstack1, ItemRendererHelper.BLOCK_3D);
         if (item instanceof ItemBlock) {
            if (is3D || RenderBlocks.func_147739_a(Block.func_149634_a(item).func_149645_b())) {
               f1 = 0.625F;
               GL11.glTranslatef(0.0F, -0.25F, 0.0F);
               GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
               GL11.glScalef(f1, -f1, -f1);
            }

            this.field_76990_c.field_78721_f.func_78443_a(par1EntityLiving, itemstack1, 0);
         }

         GL11.glPopMatrix();
      }

      if (itemstack != null && itemstack.func_77973_b() != null) {
         item = itemstack.func_77973_b();
         GL11.glPushMatrix();
         if (this.field_77045_g.field_78091_s) {
            f1 = 0.5F;
            GL11.glTranslatef(0.0F, 0.625F, 0.0F);
            GL11.glRotatef(-20.0F, -1.0F, 0.0F, 0.0F);
            GL11.glScalef(f1, f1, f1);
         }

         this.modelBipedMain.bipedRightArm.func_78794_c(0.0625F);
         GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);
         customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ItemRenderType.EQUIPPED);
         is3D = customRenderer != null && customRenderer.shouldUseRenderHelper(ItemRenderType.EQUIPPED, itemstack, ItemRendererHelper.BLOCK_3D);
         if (!(item instanceof ItemBlock) || !is3D && !RenderBlocks.func_147739_a(Block.func_149634_a(item).func_149645_b())) {
            if (item == Items.field_151031_f) {
               f1 = 0.625F;
               GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
               GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
               GL11.glScalef(f1, -f1, f1);
               GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
               GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            } else if (item.func_77662_d()) {
               f1 = 0.625F;
               if (item.func_77629_n_()) {
                  GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                  GL11.glTranslatef(0.0F, -0.125F, 0.0F);
               }

               this.func_82422_c();
               GL11.glScalef(f1, -f1, f1);
               GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
               GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            } else {
               f1 = 0.375F;
               GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
               GL11.glScalef(f1, f1, f1);
               GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
               GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
               GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
            }
         } else {
            f1 = 0.5F;
            GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
            f1 *= 0.75F;
            GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(-f1, -f1, f1);
         }

         float f2;
         float f3;
         int i;
         if (itemstack.func_77973_b().func_77623_v()) {
            for(i = 0; i < itemstack.func_77973_b().getRenderPasses(itemstack.func_77960_j()); ++i) {
               int j = itemstack.func_77973_b().func_82790_a(itemstack, i);
               f2 = (float)(j >> 16 & 255) / 255.0F;
               f3 = (float)(j >> 8 & 255) / 255.0F;
               float f4 = (float)(j & 255) / 255.0F;
               GL11.glColor4f(f2, f3, f4, 1.0F);
               this.field_76990_c.field_78721_f.func_78443_a(par1EntityLiving, itemstack, i);
            }
         } else {
            i = itemstack.func_77973_b().func_82790_a(itemstack, 0);
            float f5 = (float)(i >> 16 & 255) / 255.0F;
            f2 = (float)(i >> 8 & 255) / 255.0F;
            f3 = (float)(i & 255) / 255.0F;
            GL11.glColor4f(f5, f2, f3, 1.0F);
            this.field_76990_c.field_78721_f.func_78443_a(par1EntityLiving, itemstack, 0);
         }

         GL11.glPopMatrix();
      }

   }

   protected void func_82422_c() {
      GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
   }

   protected void func_82408_c(EntityLivingBase par1EntityLivingBase, int par2, float par3) {
      this.func_82408_c((EntityLiving)par1EntityLivingBase, par2, par3);
   }

   protected int func_77032_a(EntityLivingBase par1EntityLivingBase, int par2, float par3) {
      return this.shouldRenderPass((EntityLiving)par1EntityLivingBase, par2, par3);
   }

   protected void func_77029_c(EntityLivingBase par1EntityLivingBase, float par2) {
      this.renderEquippedItems((EntityLiving)par1EntityLivingBase, par2);
   }

   public void func_76986_a(EntityLivingBase par1Entity, double par2, double par4, double par6, float par8, float par9) {
      this.func_76986_a((EntityLiving)par1Entity, par2, par4, par6, par8, par9);
   }

   protected ResourceLocation func_110775_a(Entity par1Entity) {
      return this.getEntityTexture((EntityLiving)par1Entity);
   }

   public void func_76986_a(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
      this.func_76986_a((EntityLiving)par1Entity, par2, par4, par6, par8, par9);
   }

   public static ResourceLocation getArmorResource(Entity entity, ItemStack stack, int slot, String type) {
      ItemArmor item = (ItemArmor)stack.func_77973_b();
      String s1 = String.format("textures/models/armor/%s_layer_%d%s.png", bipedArmorFilenamePrefix[item.field_77880_c], slot == 2 ? 2 : 1, type == null ? "" : String.format("_%s", type));
      s1 = ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
      ResourceLocation resourcelocation = (ResourceLocation)field_110859_k.get(s1);
      if (resourcelocation == null) {
         resourcelocation = new ResourceLocation(s1);
         field_110859_k.put(s1, resourcelocation);
      }

      return resourcelocation;
   }
}
