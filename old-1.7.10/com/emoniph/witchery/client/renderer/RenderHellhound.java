package com.emoniph.witchery.client.renderer;

import com.emoniph.witchery.entity.EntityHellhound;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderHellhound extends RenderLiving {
   private static final ResourceLocation anrgyWolfTextures = new ResourceLocation("witchery", "textures/entities/hellhound.png");

   public RenderHellhound(ModelBase model, float shadow) {
      super(model, shadow);
   }

   protected float handleRotationFloat(EntityHellhound entity, float p_77044_2_) {
      return entity.getTailRotation();
   }

   protected int shouldRenderPass(EntityHellhound entity, int p_77032_2_, float p_77032_3_) {
      return -1;
   }

   protected ResourceLocation getEntityTexture(EntityHellhound entity) {
      return anrgyWolfTextures;
   }

   protected int func_77032_a(EntityLivingBase entity, int p_77032_2_, float p_77032_3_) {
      return this.shouldRenderPass((EntityHellhound)entity, p_77032_2_, p_77032_3_);
   }

   protected float func_77044_a(EntityLivingBase entity, float p_77044_2_) {
      return this.handleRotationFloat((EntityHellhound)entity, p_77044_2_);
   }

   protected ResourceLocation func_110775_a(Entity entity) {
      return this.getEntityTexture((EntityHellhound)entity);
   }
}
