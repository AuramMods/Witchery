package com.emoniph.witchery.client.renderer;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockGrassper;
import com.emoniph.witchery.client.model.ModelGrassper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderGrassper extends TileEntitySpecialRenderer {
   final ModelGrassper model = new ModelGrassper();
   private RenderItem renderItems = new RenderItem() {
      public byte getMiniItemCountForItemStack(ItemStack stack) {
         return 1;
      }

      public byte getMiniBlockCountForItemStack(ItemStack stack) {
         return 1;
      }

      public boolean shouldBob() {
         return false;
      }

      public boolean shouldSpreadItems() {
         return false;
      }
   };
   private static final ResourceLocation TEXTURE_URL = new ResourceLocation("witchery", "textures/blocks/grassper.png");

   public RenderGrassper() {
      this.renderItems.func_76976_a(RenderManager.field_78727_a);
   }

   public void func_147500_a(TileEntity tileEntity, double d, double d1, double d2, float f) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)d, (float)d1, (float)d2);
      BlockGrassper.TileEntityGrassper tileEntityYour = (BlockGrassper.TileEntityGrassper)tileEntity;
      this.renderGrassper(tileEntityYour, tileEntity.func_145831_w(), tileEntity.field_145851_c, tileEntity.field_145848_d, tileEntity.field_145849_e, Witchery.Blocks.GRASSPER);
      GL11.glPopMatrix();
   }

   public void renderGrassper(BlockGrassper.TileEntityGrassper te, World world, int x, int y, int z, Block block) {
      GL11.glPushMatrix();
      GL11.glTranslatef(0.5F, 0.5F, 0.5F);
      this.func_147499_a(TEXTURE_URL);
      GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
      GL11.glTranslatef(0.0F, -1.0F, 0.0F);
      float rotational;
      if (world != null) {
         int meta = world.func_72805_g(x, y, z);
         rotational = 0.0F;
         switch(meta) {
         case 2:
            rotational = 0.0F;
            break;
         case 3:
            rotational = 180.0F;
            break;
         case 4:
            rotational = 270.0F;
            break;
         case 5:
            rotational = 90.0F;
         }

         GL11.glRotatef(rotational, 0.0F, 1.0F, 0.0F);
      }

      this.model.func_78088_a((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
      GL11.glPopMatrix();
      if (world != null) {
         ItemStack newStack = null;
         rotational = (float)Minecraft.func_71386_F() / 3000.0F * 300.0F;
         EntityItem ei = new EntityItem(world);
         ei.field_70290_d = 0.0F;
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glPushMatrix();
         GL11.glEnable(32826);
         GL11.glTranslatef(0.0F, 0.0F, 0.0F);
         OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, 170.0F, 170.0F);
         GL11.glTranslatef(0.0F, 0.6F, 0.0F);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glScalef(0.75F, 0.75F, 0.75F);
         float yShift = -0.15F;
         float zShift = 0.65F;
         float xShift = 0.65F;
         boolean fancy = Witchery.proxy.getGraphicsLevel();

         for(int i = 0; i < te.func_70302_i_() && i <= 46; ++i) {
            if (te.func_70301_a(i) != null) {
               newStack = te.func_70301_a(i).func_77946_l();
               newStack.field_77994_a = 1;
               ei.func_92058_a(newStack);
               GL11.glPushMatrix();
               GL11.glTranslatef(xShift, yShift, zShift);
               if (fancy) {
                  GL11.glRotatef(rotational / 2.0F, 0.0F, 1.0F, 0.0F);
               }

               GL11.glPushMatrix();
               this.renderItems.func_76986_a(ei, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
               GL11.glPopMatrix();
               GL11.glPopMatrix();
            }
         }

         GL11.glDisable(32826);
         GL11.glPopMatrix();
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      }

   }
}
