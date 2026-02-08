package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.network.PacketSyncEntitySize;
import com.emoniph.witchery.util.EntitySizeInfo;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderLivingEvent.Post;
import net.minecraftforge.client.event.RenderLivingEvent.Pre;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import org.lwjgl.opengl.GL11;

public class PotionResizing extends PotionBase implements IHandlePreRenderLiving, IHandleRenderLiving, IHandleLivingUpdate, IHandleLivingHurt, IHandleLivingJump, IHandleLivingAttack {
   private static Method methodEntitySetSize;
   private static Method methodZombieSetSize;
   private static Method methodZombieSetSize2;
   private static Method methodAgeableSetSize;
   private static Method methodAgeableSetSize2;

   public PotionResizing(int id, int color) {
      super(id, color);
   }

   public void func_111187_a(EntityLivingBase entity, BaseAttributeMap attributes, int amplifier) {
      EntitySizeInfo sizeInfo = new EntitySizeInfo(entity);
      setEntitySize(entity, sizeInfo.defaultWidth, sizeInfo.defaultHeight);
      if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         player.eyeHeight = sizeInfo.eyeHeight;
      }

      entity.field_70138_W = sizeInfo.stepSize;
      Witchery.packetPipeline.sendToAll((IMessage)(new PacketSyncEntitySize(entity)));
      super.func_111187_a(entity, attributes, amplifier);
   }

   public static void setEntitySize(Entity entity, float width, float height) {
      try {
         if (entity instanceof EntityZombie) {
            if (methodZombieSetSize == null) {
               methodZombieSetSize = ReflectionHelper.findMethod(EntityZombie.class, (EntityZombie)entity, new String[]{"setSize", "func_70105_a", "a"}, new Class[]{Float.TYPE, Float.TYPE});
            }

            if (methodZombieSetSize2 == null) {
               methodZombieSetSize2 = ReflectionHelper.findMethod(EntityZombie.class, (EntityZombie)entity, new String[]{"func_146069_a", "a"}, new Class[]{Float.TYPE});
            }

            methodZombieSetSize.invoke(entity, width, height);
            methodZombieSetSize2.invoke(entity, 1.0F);
         } else if (entity instanceof EntityAgeable) {
            if (methodAgeableSetSize == null) {
               methodAgeableSetSize = ReflectionHelper.findMethod(EntityAgeable.class, (EntityAgeable)entity, new String[]{"setSize", "func_70105_a", "a"}, new Class[]{Float.TYPE, Float.TYPE});
            }

            if (methodAgeableSetSize2 == null) {
               methodAgeableSetSize2 = ReflectionHelper.findMethod(EntityAgeable.class, (EntityAgeable)entity, new String[]{"setScale", "func_98055_j", "a"}, new Class[]{Float.TYPE});
            }

            methodAgeableSetSize.invoke(entity, width, height);
            methodAgeableSetSize2.invoke(entity, 1.0F);
         } else {
            if (methodEntitySetSize == null) {
               methodEntitySetSize = ReflectionHelper.findMethod(Entity.class, entity, new String[]{"setSize", "func_70105_a", "a"}, new Class[]{Float.TYPE, Float.TYPE});
            }

            methodEntitySetSize.invoke(entity, width, height);
         }
      } catch (IllegalAccessException var4) {
      } catch (IllegalArgumentException var5) {
      } catch (InvocationTargetException var6) {
      }

   }

   public void onLivingRender(World world, EntityLivingBase entity, Pre event, int amplifier) {
      GL11.glPushMatrix();
      GL11.glTranslated(event.x, event.y, event.z);
      float scale = getModifiedScaleFactor(entity, amplifier);
      GL11.glScalef(scale, scale, scale);
      GL11.glTranslated(-event.x, -event.y, -event.z);
   }

   public static float getModifiedScaleFactor(EntityLivingBase entity, int amplifier) {
      float currentHeight = entity.field_70131_O;
      EntitySizeInfo sizeInfo = new EntitySizeInfo(entity);
      float ratio = currentHeight / sizeInfo.defaultHeight;
      float factor = getScaleFactor(amplifier);
      float scale = factor < 1.0F ? Math.max(ratio, factor) : Math.min(ratio, factor);
      return scale;
   }

   public void onLivingRender(World world, EntityLivingBase entity, Post event, int amplifier) {
      GL11.glPopMatrix();
   }

   public void onLivingUpdate(World world, EntityLivingBase entity, LivingUpdateEvent event, int amplifier, int duration) {
      float reductionFactor = 0.03F * (float)(event.entity.field_70170_p.field_72995_K ? 1 : 20);
      if (world.field_72995_K || entity.field_70173_aa % 20 == 0) {
         EntitySizeInfo sizeInfo = new EntitySizeInfo(entity);
         float scale = getScaleFactor(amplifier);
         float requiredHeight = sizeInfo.defaultHeight * scale;
         float requiredWidth = sizeInfo.defaultWidth * scale;
         float currentHeight = event.entityLiving.field_70131_O;
         if (requiredHeight != currentHeight) {
            if (entity instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)entity;
               if (!world.field_72995_K) {
                  player.eyeHeight = currentHeight * 0.92F;
               }
            }

            entity.field_70138_W = scale < 1.0F ? 0.0F : scale - 1.0F;
            if (scale < 1.0F) {
               setEntitySize(entity, Math.max(entity.field_70130_N - reductionFactor, requiredWidth), Math.max(currentHeight - reductionFactor, requiredHeight));
            } else {
               setEntitySize(entity, Math.min(entity.field_70130_N + reductionFactor, requiredWidth), Math.min(currentHeight + reductionFactor, requiredHeight));
            }
         }
      }

   }

   public boolean handleAllHurtEvents() {
      return true;
   }

   public void onLivingHurt(World world, EntityLivingBase entity, LivingHurtEvent event, int amplifier) {
      if (!world.field_72995_K) {
         PotionEffect effectDefender = entity.func_70660_b(this);
         boolean isDefenderShrunken = effectDefender != null;
         DamageSource source = event.source;
         if (source.func_76355_l() != "mob" && source.func_76355_l() != "player") {
            if (source == DamageSource.field_76379_h && isDefenderShrunken && getScaleFactor(effectDefender.func_76458_c()) > event.ammount) {
               event.setCanceled(true);
            }
         } else if (source.func_76346_g() != null && source.func_76346_g() instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase)source.func_76346_g();
            PotionEffect effectAttacker = attacker.func_70660_b(this);
            if (isDefenderShrunken || effectAttacker != null) {
               float scale = getDamageMultiplier(effectAttacker, effectDefender);
               event.ammount *= Math.max(Math.min(scale, 3.0F), 0.5F);
            }
         }
      }

   }

   public void onLivingAttack(World world, EntityLivingBase entity, LivingAttackEvent event, int amplifier) {
      if (Witchery.modHooks.isAM2Present && !world.field_72995_K && event.source == DamageSource.field_76368_d && amplifier <= 1 && entity instanceof EntityPlayer && !event.entity.field_70170_p.func_147439_a(MathHelper.func_76128_c(event.entity.field_70165_t), MathHelper.func_76128_c(event.entity.field_70163_u), MathHelper.func_76128_c(event.entity.field_70161_v)).func_149721_r()) {
         event.setCanceled(true);
      }

   }

   public static float getScaleFactor(int amplifier) {
      switch(amplifier) {
      case 0:
      default:
         return 0.25F;
      case 1:
         return 0.4F;
      case 2:
         return 2.0F;
      case 3:
         return 3.0F;
      }
   }

   private static int getSize(PotionEffect amplifier) {
      if (amplifier == null) {
         return 3;
      } else {
         switch(amplifier.func_76458_c()) {
         case 0:
            return 1;
         case 1:
            return 2;
         case 2:
            return 4;
         case 3:
            return 5;
         default:
            return 3;
         }
      }
   }

   public static float getDamageMultiplier(PotionEffect amplifierA, PotionEffect amplifierB) {
      int sizeA = getSize(amplifierA);
      int sizeB = getSize(amplifierB);
      float sizeDiff = (float)(sizeA / sizeB);
      return sizeDiff;
   }

   public void onLivingJump(World world, EntityLivingBase entity, LivingJumpEvent event, int amplifier) {
      float scale = getScaleFactor(amplifier);
      EntityLivingBase var10000;
      if (scale > 1.0F) {
         var10000 = event.entityLiving;
         var10000.field_70181_x *= (double)scale * 0.5D + 0.5D;
      } else {
         var10000 = event.entityLiving;
         var10000.field_70181_x *= Math.max((double)scale, 0.5D) * 1.5D;
      }

   }
}
