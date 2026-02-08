package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.EntityVillageGuard;
import com.emoniph.witchery.entity.ai.EntityAIMoveTowardsVampire;
import com.emoniph.witchery.util.TimeUtil;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PotionParalysis extends PotionBase implements IHandleLivingUpdate, IHandleLivingHurt {
   public PotionParalysis(int id, int color) {
      super(id, true, color);
      this.setIncurable();
   }

   public void postContructInitialize() {
      this.func_111184_a(SharedMonsterAttributes.field_111263_d, "E69059D5-CAE6-4695-9BE3-C6F0F22151E8", -40.0D, 2);
   }

   public void func_111185_a(EntityLivingBase entity, BaseAttributeMap attributes, int amplifier) {
      if (this.canApplyToEntity(entity, amplifier)) {
         super.func_111185_a(entity, attributes, amplifier);
      } else if (isVillager(entity)) {
         EntityCreature creature = (EntityCreature)entity;
         creature.func_70624_b((EntityLivingBase)null);
         creature.func_70604_c((EntityLivingBase)null);
         creature.func_70784_b((Entity)null);
         creature.field_70714_bg.func_75776_a(0, new EntityAIMoveTowardsVampire(creature, 0.8D, 1.0F, 16.0F));
      }

   }

   public void func_111187_a(EntityLivingBase entity, BaseAttributeMap attributes, int amplifier) {
      if (this.canApplyToEntity(entity, amplifier)) {
         super.func_111187_a(entity, attributes, amplifier);
      } else if (isVillager(entity) && amplifier >= 5) {
         EntityCreature creature = (EntityCreature)entity;
         Iterator itr = creature.field_70714_bg.field_75782_a.iterator();
         EntityAIBase task = null;

         while(itr.hasNext()) {
            EntityAITaskEntry entityaitaskentry = (EntityAITaskEntry)itr.next();
            EntityAIBase entityaibase1 = entityaitaskentry.field_75733_a;
            if (entityaibase1 instanceof EntityAIMoveTowardsVampire) {
               task = entityaibase1;
               break;
            }
         }

         if (task != null) {
            creature.field_70714_bg.func_85156_a(task);
         }
      }

   }

   public void onLivingUpdate(World world, EntityLivingBase entity, LivingUpdateEvent event, int amplifier, int duration) {
      if (this.canApplyToEntity(entity, amplifier)) {
         if (!world.field_72995_K) {
            if (entity instanceof EntityCreeper) {
               ((EntityCreeper)entity).func_70829_a(-1);
            }

            if (amplifier >= 4 && duration <= 1 && entity instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)entity;
               player.func_70690_d(new PotionEffect(Witchery.Potions.QUEASY.field_76415_H, TimeUtil.secsToTicks(90), 0, true));
            }
         }

         if (entity.field_70173_aa % 20 != 2 || !isVillager(entity) || amplifier < 5) {
            entity.field_70181_x = -0.2D;
         }
      }

   }

   private boolean canApplyToEntity(EntityLivingBase entity, int amplifier) {
      if (entity instanceof IBossDisplayData) {
         return false;
      } else if (amplifier >= 5 && isVillager(entity)) {
         return false;
      } else {
         return !(entity instanceof EntityPlayer) || amplifier >= 2;
      }
   }

   public static boolean isVillager(Entity entity) {
      return entity instanceof EntityVillager || entity instanceof EntityVillageGuard;
   }

   public void onLivingHurt(World world, EntityLivingBase entity, LivingHurtEvent event, int amplifier) {
      if (!world.field_72995_K && amplifier >= 4 && event.ammount >= 1.0F) {
         entity.func_82170_o(this.field_76415_H);
      }

   }

   public boolean handleAllHurtEvents() {
      return false;
   }
}
