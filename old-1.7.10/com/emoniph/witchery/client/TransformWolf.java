package com.emoniph.witchery.client;

import com.emoniph.witchery.entity.EntityBroom;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderWolf;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TransformWolf {
   private EntityWolf proxyEntity;
   private RenderWolf proxyRenderer = new RenderWolf(new ModelWolf(), new ModelWolf(), 0.5F);

   public EntityLivingBase getModel() {
      return this.proxyEntity;
   }

   public void syncModelWith(EntityLivingBase entity, boolean frontface) {
      if (this.proxyEntity == null) {
         this.proxyEntity = new EntityWolf(entity.field_70170_p);
      } else if (this.proxyEntity.field_70170_p != entity.field_70170_p) {
         this.proxyEntity.func_70029_a(entity.field_70170_p);
      }

      this.proxyEntity.func_70107_b(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v);
      this.proxyEntity.field_70142_S = entity.field_70142_S;
      this.proxyEntity.field_70137_T = entity.field_70137_T;
      this.proxyEntity.field_70136_U = entity.field_70136_U;
      this.proxyEntity.field_70159_w = entity.field_70159_w;
      this.proxyEntity.field_70181_x = entity.field_70181_x;
      this.proxyEntity.field_70179_y = entity.field_70179_y;
      this.proxyEntity.field_70701_bs = entity.field_70701_bs;
      this.proxyEntity.field_70702_br = entity.field_70702_br;
      this.proxyEntity.field_70122_E = entity.field_70122_E;
      this.proxyEntity.field_70169_q = entity.field_70169_q;
      this.proxyEntity.field_70167_r = entity.field_70167_r;
      this.proxyEntity.field_70166_s = entity.field_70166_s;
      this.proxyEntity.field_70125_A = entity.field_70125_A;
      this.proxyEntity.field_70177_z = entity.field_70177_z;
      this.proxyEntity.field_70759_as = entity.field_70759_as;
      this.proxyEntity.field_70127_C = entity.field_70127_C;
      this.proxyEntity.field_70126_B = entity.field_70126_B;
      this.proxyEntity.field_70758_at = entity.field_70758_at;
      this.proxyEntity.field_70754_ba = entity.field_70754_ba;
      this.proxyEntity.field_70721_aZ = entity.field_70721_aZ;
      this.proxyEntity.field_70722_aY = entity.field_70722_aY;
      this.proxyEntity.field_82175_bq = entity.field_82175_bq;
      this.proxyEntity.field_70733_aJ = entity.field_70733_aJ;
      this.proxyEntity.field_70732_aI = entity.field_70732_aI;
      this.proxyEntity.field_70761_aq = frontface ? 0.0F : entity.field_70761_aq;
      this.proxyEntity.field_70760_ar = frontface ? 0.0F : entity.field_70760_ar;
      this.proxyEntity.field_70173_aa = entity.field_70173_aa;
      this.proxyEntity.field_70128_L = false;
      this.proxyEntity.field_70160_al = entity.field_70160_al;
      this.proxyEntity.field_70129_M = 0.0F;
      this.proxyEntity.func_70095_a(entity.func_70093_af());
      this.proxyEntity.func_70031_b(entity.func_70051_ag());
      this.proxyEntity.func_82142_c(entity.func_82150_aj());
      this.proxyEntity.func_70904_g(entity.func_70115_ae());
   }

   public void render(World worldObj, EntityLivingBase entity, double x, double y, double z, RendererLivingEntity renderer, float partialTicks, boolean frontface) {
      this.syncModelWith(entity, frontface);
      this.proxyRenderer.func_76976_a(RenderManager.field_78727_a);
      float f1 = this.proxyEntity.field_70126_B + (this.proxyEntity.field_70177_z - this.proxyEntity.field_70126_B) * partialTicks;
      double d3 = -((double)this.proxyEntity.field_70129_M);
      if (this.proxyEntity.func_70093_af() && !(entity instanceof EntityPlayerSP)) {
         d3 -= 0.125D;
      }

      if (entity.func_70115_ae()) {
         Entity ridden = entity.field_70154_o;
         d3 += ridden.func_70042_X() + (entity.field_70154_o instanceof EntityBroom ? (double)ridden.field_70131_O - 0.2D : 0.0D);
      }

      float f2 = 1.0F;
      GL11.glColor3f(f2, f2, f2);
      this.proxyRenderer.func_76986_a(this.proxyEntity, x, y + d3, z, frontface ? 0.0F : f1, partialTicks);
   }

   protected void renderEquippedItems(ItemRenderer itemRenderer, EntityLivingBase p_77029_1_, float p_77029_2_) {
      GL11.glColor3f(1.0F, 1.0F, 1.0F);
      ItemStack itemstack = p_77029_1_.func_70694_bm();
      if (itemstack != null && itemstack.func_77973_b() != null) {
         Item item = itemstack.func_77973_b();
         GL11.glPushMatrix();
         GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);
         IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ItemRenderType.EQUIPPED);
         boolean is3D = customRenderer != null && customRenderer.shouldUseRenderHelper(ItemRenderType.EQUIPPED, itemstack, ItemRendererHelper.BLOCK_3D);
         float f1;
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
         int i;
         float f5;
         if (itemstack.func_77973_b().func_77623_v()) {
            for(i = 0; i < itemstack.func_77973_b().getRenderPasses(itemstack.func_77960_j()); ++i) {
               int j = itemstack.func_77973_b().func_82790_a(itemstack, i);
               f5 = (float)(j >> 16 & 255) / 255.0F;
               f2 = (float)(j >> 8 & 255) / 255.0F;
               float f3 = (float)(j & 255) / 255.0F;
               GL11.glColor4f(f5, f2, f3, 1.0F);
               itemRenderer.func_78443_a(p_77029_1_, itemstack, i);
            }
         } else {
            i = itemstack.func_77973_b().func_82790_a(itemstack, 0);
            float f4 = (float)(i >> 16 & 255) / 255.0F;
            f5 = (float)(i >> 8 & 255) / 255.0F;
            f2 = (float)(i & 255) / 255.0F;
            GL11.glColor4f(f4, f5, f2, 1.0F);
            itemRenderer.func_78443_a(p_77029_1_, itemstack, 0);
         }

         GL11.glPopMatrix();
      }

   }
}
