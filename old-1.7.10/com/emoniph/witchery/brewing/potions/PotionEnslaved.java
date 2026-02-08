package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.EntityDemon;
import com.emoniph.witchery.entity.EntityEnt;
import com.emoniph.witchery.entity.EntityImp;
import com.emoniph.witchery.util.EntityUtil;
import java.util.Iterator;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PotionEnslaved extends PotionBase implements IHandleLivingSetAttackTarget, IHandleLivingUpdate {
   private static final String ENSLAVER_KEY = "WITCEnslaverName";

   public PotionEnslaved(int id, int color) {
      super(id, true, color);
   }

   public void onLivingSetAttackTarget(World world, EntityLiving entity, LivingSetAttackTargetEvent event, int amplifier) {
      if (event.target != null && event.target instanceof EntityPlayer && entity instanceof EntityLiving) {
         String enslaverName = getMobEnslaverName(entity);
         if (enslaverName.equals(event.target.func_70005_c_())) {
            entity.func_70624_b((EntityLivingBase)null);
         }
      }

   }

   public static boolean setEnslaverForMob(EntityLiving entity, EntityPlayer player) {
      if (entity != null && player != null) {
         String enslaverName = entity.getEntityData().func_74779_i("WITCEnslaverName");
         boolean isEnslaved = enslaverName != null && !enslaverName.isEmpty();
         if (isEnslaved && player.func_70005_c_().equals(enslaverName)) {
            return false;
         } else {
            entity.getEntityData().func_74778_a("WITCEnslaverName", player.func_70005_c_());
            entity.func_70690_d(new PotionEffect(Witchery.Potions.ENSLAVED.field_76415_H, Integer.MAX_VALUE));
            EntityUtil.dropAttackTarget(entity);
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean isMobEnslavedBy(EntityLiving entity, EntityPlayer player) {
      return player != null && entity != null && entity.getEntityData() != null && player.func_70005_c_().equals(entity.getEntityData().func_74779_i("WITCEnslaverName"));
   }

   public static boolean canCreatureBeEnslaved(EntityLivingBase entityLiving) {
      if (entityLiving instanceof EntityLiving) {
         return !(entityLiving instanceof IBossDisplayData) && !(entityLiving instanceof EntityGolem) && !(entityLiving instanceof EntityDemon) && !(entityLiving instanceof EntityWitch) && !(entityLiving instanceof EntityImp) && !(entityLiving instanceof EntityEnt);
      } else {
         return false;
      }
   }

   public static boolean isMobEnslaved(EntityLiving entity) {
      if (entity == null) {
         return false;
      } else {
         String enslaverName = entity.getEntityData().func_74779_i("WITCEnslaverName");
         return enslaverName != null && !enslaverName.isEmpty();
      }
   }

   public static String getMobEnslaverName(EntityLiving entity) {
      if (entity == null) {
         return "";
      } else {
         String enslaverName = entity.getEntityData().func_74779_i("WITCEnslaverName");
         return enslaverName;
      }
   }

   public void onLivingUpdate(World world, EntityLivingBase entity, LivingUpdateEvent event, int amplifier, int duration) {
      if (!world.field_72995_K && world.func_82737_E() % 20L == 3L && entity instanceof EntityCreature) {
         EntityCreature creature = (EntityCreature)entity;
         Iterator i$ = creature.field_70715_bh.field_75782_a.iterator();

         while(i$.hasNext()) {
            Object obj = i$.next();
            EntityAITaskEntry task = (EntityAITaskEntry)obj;
            if (task.field_75733_a instanceof EntityAIEnslaverHurtByTarget) {
               return;
            }
         }

         creature.field_70715_bh.func_75776_a(1, new EntityAIEnslaverHurtByTarget(creature));
      }

   }
}
