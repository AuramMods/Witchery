package com.emoniph.witchery.client.model;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockDreamCatcher;
import com.emoniph.witchery.item.ItemGeneral;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelDreamCatcher extends ModelBase {
   final ModelRenderer frameLeft;
   final ModelRenderer frameRight;
   final ModelRenderer frameTop;
   final ModelRenderer frameBottom;
   final ModelRenderer[] nets;
   final ModelRenderer decoration;

   public ModelDreamCatcher() {
      this.field_78090_t = 32;
      this.field_78089_u = 32;
      this.frameLeft = new ModelRenderer(this, 0, 2);
      this.frameLeft.func_78789_a(0.0F, 0.0F, 0.0F, 1, 8, 1);
      this.frameLeft.func_78793_a(-4.0F, 10.0F, 7.0F);
      this.frameLeft.func_78787_b(32, 32);
      this.frameLeft.field_78809_i = true;
      this.setRotation(this.frameLeft, 0.0F, 0.0F, 0.0F);
      this.frameRight = new ModelRenderer(this, 0, 2);
      this.frameRight.func_78789_a(0.0F, 0.0F, 0.0F, 1, 8, 1);
      this.frameRight.func_78793_a(3.0F, 10.0F, 7.0F);
      this.frameRight.func_78787_b(32, 32);
      this.frameRight.field_78809_i = true;
      this.setRotation(this.frameRight, 0.0F, 0.0F, 0.0F);
      this.frameTop = new ModelRenderer(this, 0, 0);
      this.frameTop.func_78789_a(0.0F, 0.0F, 0.0F, 6, 1, 1);
      this.frameTop.func_78793_a(-3.0F, 10.0F, 7.0F);
      this.frameTop.func_78787_b(32, 32);
      this.frameTop.field_78809_i = true;
      this.setRotation(this.frameTop, 0.0F, 0.0F, 0.0F);
      this.frameBottom = new ModelRenderer(this, 0, 0);
      this.frameBottom.func_78789_a(0.0F, 0.0F, 0.0F, 6, 1, 1);
      this.frameBottom.func_78793_a(-3.0F, 17.0F, 7.0F);
      this.frameBottom.func_78787_b(32, 32);
      this.frameBottom.field_78809_i = true;
      this.setRotation(this.frameBottom, 0.0F, 0.0F, 0.0F);
      this.nets = new ModelRenderer[Witchery.Items.GENERIC.weaves.size()];

      for(int i = 0; i < Witchery.Items.GENERIC.weaves.size(); ++i) {
         ItemGeneral.DreamWeave weave = (ItemGeneral.DreamWeave)Witchery.Items.GENERIC.weaves.get(i);
         this.nets[i] = new ModelRenderer(this, weave.textureOffsetX, weave.textureOffsetY);
         this.nets[i].func_78789_a(0.0F, 0.0F, 0.0F, 6, 6, 0);
         this.nets[i].func_78793_a(-3.0F, 11.0F, 8.0F);
         this.nets[i].func_78787_b(32, 32);
         this.nets[i].field_78809_i = true;
         this.setRotation(this.nets[i], 0.0F, 0.0F, 0.0F);
      }

      this.decoration = new ModelRenderer(this, 0, 12);
      this.decoration.func_78789_a(0.0F, 0.0F, 0.0F, 8, 6, 0);
      this.decoration.func_78793_a(-4.0F, 18.0F, 7.0F);
      this.decoration.func_78787_b(32, 32);
      this.decoration.field_78809_i = true;
      this.setRotation(this.decoration, 0.0F, 0.0F, 0.0F);
   }

   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, BlockDreamCatcher.TileEntityDreamCatcher tileEntity) {
      super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
      this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
      this.frameLeft.func_78785_a(f5);
      this.frameRight.func_78785_a(f5);
      this.frameTop.func_78785_a(f5);
      this.frameBottom.func_78785_a(f5);
      ItemGeneral.DreamWeave weave = tileEntity.getWeave();
      if (weave != null) {
         this.nets[weave.weaveID].func_78785_a(f5);
      }

      this.decoration.func_78785_a(f5);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.field_78795_f = x;
      model.field_78796_g = y;
      model.field_78808_h = z;
   }

   public void func_78087_a(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
      super.func_78087_a(f, f1, f2, f3, f4, f5, entity);
   }
}
