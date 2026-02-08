package com.emoniph.witchery.brewing.potions;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderLivingEvent.Post;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import org.lwjgl.opengl.GL11;

public class PotionSpiked extends PotionBase implements IHandleLivingUpdate, IHandleRenderLiving {
   @SideOnly(Side.CLIENT)
   private static ResourceLocation TEXTURE;

   public PotionSpiked(int id, int color) {
      super(id, color);
   }

   public void onLivingUpdate(World world, EntityLivingBase entity, LivingUpdateEvent event, int amplifier, int duration) {
      if (!world.field_72995_K && world.func_82737_E() % 5L == 3L) {
         List<EntityLivingBase> entities = world.func_72872_a(EntityLivingBase.class, entity.field_70121_D.func_72314_b(0.2D + 0.1D * (double)amplifier, 0.0D, 0.2D + 0.1D * (double)amplifier));
         Iterator i$ = entities.iterator();

         while(i$.hasNext()) {
            EntityLivingBase otherEntity = (EntityLivingBase)i$.next();
            if (otherEntity != entity) {
               otherEntity.func_70097_a(DamageSource.field_76367_g, (float)(1 + amplifier));
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void onLivingRender(World world, EntityLivingBase entity, Post event, int amplifier) {
      if (TEXTURE == null) {
         TEXTURE = new ResourceLocation("witchery", "textures/entities/cactus_overlay.png");
      }

      GL11.glPushMatrix();
      Minecraft.func_71410_x().func_110434_K().func_110577_a(TEXTURE);
      ModelOverlayRenderer.render(entity, event.x, event.y, event.z, event.renderer);
      GL11.glPopMatrix();
   }
}
