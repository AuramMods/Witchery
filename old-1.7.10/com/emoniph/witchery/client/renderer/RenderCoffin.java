package com.emoniph.witchery.client.renderer;

import com.emoniph.witchery.blocks.BlockCoffin;
import com.emoniph.witchery.client.model.ModelCoffin;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderCoffin extends TileEntitySpecialRenderer {
   final ModelCoffin model = new ModelCoffin();
   private static final ResourceLocation TEXTURE_URL = new ResourceLocation("witchery", "textures/blocks/coffin.png");

   public void func_147500_a(TileEntity tileEntity, double d, double d1, double d2, float f) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)d, (float)d1, (float)d2);
      BlockCoffin.TileEntityCoffin tileEntityGoddess = (BlockCoffin.TileEntityCoffin)tileEntity;
      this.renderGoddess(tileEntityGoddess, tileEntity.func_145831_w(), tileEntity.field_145851_c, tileEntity.field_145848_d, tileEntity.field_145849_e, f);
      GL11.glPopMatrix();
   }

   public void renderGoddess(BlockCoffin.TileEntityCoffin tile, World world, int x, int y, int z, float f) {
      GL11.glPushMatrix();
      GL11.glTranslatef(0.5F, 0.5F, 0.5F);
      this.func_147499_a(TEXTURE_URL);
      GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
      GL11.glTranslatef(0.0F, -0.1F, 0.0F);
      if (world != null) {
         int meta = world.func_72805_g(x, y, z);
         int direction = BlockCoffin.getDirection(meta);
         float rotation = 0.0F;
         switch(direction) {
         case 0:
            rotation = 0.0F;
            break;
         case 1:
            rotation = 90.0F;
            break;
         case 2:
            rotation = 180.0F;
            break;
         case 3:
            rotation = 270.0F;
         }

         GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
         if (!BlockCoffin.isBlockHeadOfBed(meta)) {
            this.model.sideLeft.field_78798_e = 1.0F;
            this.model.sideRight.field_78798_e = 1.0F;
            this.model.base.field_78798_e = 1.0F;
            this.model.sideEnd.field_78796_g = 3.1415927F;
            this.model.lidTop.field_78798_e = 2.0F;
            this.model.lidMid.field_78798_e = 1.0F;
            this.model.lid.field_78796_g = 0.0F;
         } else {
            this.model.sideLeft.field_78798_e = 0.0F;
            this.model.sideRight.field_78798_e = 0.0F;
            this.model.sideEnd.field_78798_e = 0.0F;
            this.model.sideEnd.field_78796_g = 0.0F;
            this.model.base.field_78798_e = 0.0F;
            this.model.lidTop.field_78798_e = 0.0F;
            this.model.lidMid.field_78798_e = 0.0F;
         }

         float f1 = tile.prevLidAngle + (tile.lidAngle - tile.prevLidAngle) * f;
         f1 = 1.0F - f1;
         f1 = 1.0F - f1 * f1 * f1;
         this.model.lid.field_78808_h = -(f1 * 3.1415927F / 2.0F);
      }

      this.model.func_78088_a((Entity)null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
      GL11.glPopMatrix();
   }
}
