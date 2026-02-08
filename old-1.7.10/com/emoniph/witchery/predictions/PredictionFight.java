package com.emoniph.witchery.predictions;

import com.emoniph.witchery.entity.EntityDemon;
import com.emoniph.witchery.entity.IOwnable;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TameableUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class PredictionFight extends Prediction {
   private final Class<? extends EntityLiving> entityClass;
   private final boolean bindTameable;

   public PredictionFight(int id, int itemWeight, double selfFulfillmentProbabilityPerSec, String translationKey, Class<? extends EntityLiving> entityClass, boolean bindTameable) {
      super(id, itemWeight, selfFulfillmentProbabilityPerSec, translationKey);
      this.entityClass = entityClass;
      this.bindTameable = bindTameable;
   }

   public boolean doSelfFulfillment(World world, EntityPlayer player) {
      try {
         int x = MathHelper.func_76128_c(player.field_70165_t);
         int y = MathHelper.func_76128_c(player.field_70163_u);
         int z = MathHelper.func_76128_c(player.field_70161_v);
         if (!world.field_72995_K) {
            int MAX_DISTANCE = true;
            int MIN_DISTANCE = true;
            int activeRadius = 2;
            int ax = world.field_73012_v.nextInt(activeRadius * 2 + 1);
            if (ax > activeRadius) {
               ax += 4;
            }

            int nx = x - 4 + ax;
            int az = world.field_73012_v.nextInt(activeRadius * 2 + 1);
            if (az > activeRadius) {
               az += 4;
            }

            int nz = z - 4 + az;

            int ny;
            for(ny = y; !world.func_147437_c(nx, ny, nz) && ny < y + 8; ++ny) {
            }

            while(world.func_147437_c(nx, ny, nz) && ny > 0) {
               --ny;
            }

            int hy;
            for(hy = 0; world.func_147437_c(nx, ny + hy + 1, nz) && hy < 6; ++hy) {
            }

            Constructor ctor = this.entityClass.getConstructor(World.class);
            EntityLiving entity = (EntityLiving)ctor.newInstance(world);
            if (!((float)hy >= entity.field_70131_O)) {
               return false;
            }

            boolean bound = false;
            if (entity instanceof EntityDemon) {
               ((EntityDemon)entity).setPlayerCreated(true);
            } else if (this.bindTameable && entity instanceof EntityTameable) {
               ((EntityTameable)entity).func_70903_f(true);
               TameableUtil.setOwner((EntityTameable)entity, player);
               bound = true;
            } else if (this.bindTameable && entity instanceof IOwnable) {
               ((IOwnable)entity).setOwner(player.func_70005_c_());
               bound = true;
            }

            entity.func_70012_b(0.5D + (double)nx, 0.05D + (double)ny + 1.0D, 0.5D + (double)nz, 0.0F, 0.0F);
            world.func_72838_d(entity);
            Log.instance().debug(String.format("Forcing prediction for attack by %s", entity.toString()));
            IEntityLivingData entitylivingData = null;
            entity.func_110161_a((IEntityLivingData)entitylivingData);
            if (!bound) {
               entity.func_70624_b(player);
            }

            ParticleEffect.SMOKE.send(SoundEffect.NONE, entity, 0.5D, 2.0D, 16);
         }
      } catch (NoSuchMethodException var19) {
      } catch (InvocationTargetException var20) {
      } catch (InstantiationException var21) {
      } catch (IllegalAccessException var22) {
      }

      return true;
   }

   public boolean checkIfFulfilled(World world, EntityPlayer player, LivingHurtEvent event, boolean isPastDue, boolean veryOld) {
      if (!event.isCanceled()) {
         Entity attackingEntity = event.source.func_76346_g();
         if (attackingEntity != null) {
            boolean attackedByCreature = this.entityClass.isAssignableFrom(attackingEntity.getClass());
            if (attackedByCreature) {
               Log.instance().debug(String.format("Prediction for attack by %s fulfilled as predicted", attackingEntity.toString()));
            }

            return attackedByCreature;
         }
      }

      return false;
   }
}
