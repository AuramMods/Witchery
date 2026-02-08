package com.emoniph.witchery.client.renderer;

import com.emoniph.witchery.blocks.BlockStatueWerewolf;
import com.emoniph.witchery.client.model.ModelWolfAltar;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderStatueWerewolf extends TileEntitySpecialRenderer {
   final ModelWolfAltar model = new ModelWolfAltar();
   private static final ResourceLocation TEXTURE_URL = new ResourceLocation("witchery", "textures/blocks/wolfaltar.png");

   public void func_147500_a(TileEntity tileEntity, double d, double d1, double d2, float f) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)d, (float)d1, (float)d2);
      BlockStatueWerewolf.TileEntityStatueWerewolf tileEntityGoddess = (BlockStatueWerewolf.TileEntityStatueWerewolf)tileEntity;
      this.renderStatue(tileEntityGoddess, tileEntity.func_145831_w(), tileEntity.field_145851_c, tileEntity.field_145848_d, tileEntity.field_145849_e);
      GL11.glPopMatrix();
   }

   public void renderStatue(BlockStatueWerewolf.TileEntityStatueWerewolf tileEntity, World world, int x, int y, int z) {
      GL11.glPushMatrix();
      GL11.glTranslatef(0.5F, 0.5F, 0.5F);
      this.func_147499_a(TEXTURE_URL);
      GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
      GL11.glTranslatef(0.0F, -1.0F, 0.0F);
      if (world != null) {
         int meta = world.func_72805_g(x, y, z);
         float rotation = 0.0F;
         switch(meta) {
         case 2:
            rotation = 0.0F;
            break;
         case 3:
            rotation = 180.0F;
            break;
         case 4:
            rotation = 270.0F;
            break;
         case 5:
            rotation = 90.0F;
         }

         GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
      } else {
         GL11.glScalef(0.6F, 0.6F, 0.6F);
         GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
         GL11.glTranslatef(0.0F, 1.5F, 0.0F);
      }

      this.model.func_78088_a((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
      GL11.glPopMatrix();
   }
}
