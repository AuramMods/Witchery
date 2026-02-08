package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.util.Dye;
import com.emoniph.witchery.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemDye;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class RiteFertility extends RiteExpandingEffect {
   public RiteFertility(int radius, int height) {
      super(radius, height, false);
   }

   public void doBlockAction(World world, int posX, int posY, int posZ, int currentRadius, EntityPlayer player, boolean enhanced) {
      Block blockID = world.func_147439_a(posX, posY, posZ);
      if ((blockID != Blocks.field_150346_d || blockID != Blocks.field_150349_c || blockID != Blocks.field_150391_bh || blockID != Blocks.field_150458_ak || world.field_73012_v.nextInt(5) == 0) && player != null) {
         ItemDye.applyBonemeal(Dye.BONE_MEAL.createStack(), world, posX, posY, posZ, player);
      }

   }

   public boolean doRadiusAction(World world, int posX, int posY, int posZ, int radius, EntityPlayer player, boolean enhanced) {
      double radiusSq = (double)(radius * radius);
      double minSq = (double)Math.max(0, (radius - 1) * (radius - 1));
      Iterator i$ = world.field_73010_i.iterator();

      while(i$.hasNext()) {
         Object obj = i$.next();
         EntityPlayer victim = (EntityPlayer)obj;
         double distanceSq = victim.func_70092_e(0.5D + (double)posX, 0.5D + (double)posY, 0.5D + (double)posZ);
         if (distanceSq > minSq && distanceSq <= radiusSq) {
            if (victim.func_70644_a(Potion.field_76431_k)) {
               victim.func_82170_o(Potion.field_76431_k.field_76415_H);
            }

            if (victim.func_70644_a(Potion.field_76440_q)) {
               victim.func_82170_o(Potion.field_76440_q.field_76415_H);
            }

            if (victim.func_70644_a(Potion.field_76436_u)) {
               victim.func_82170_o(Potion.field_76436_u.field_76415_H);
            }

            if (enhanced) {
               victim.func_70690_d(new PotionEffect(Potion.field_76428_l.field_76415_H, 300, 1));
               victim.func_70690_d(new PotionEffect(Potion.field_76443_y.field_76415_H, 2400));
            }
         }
      }

      ArrayList<EntityZombie> villagersToZombify = new ArrayList();
      Iterator i$ = world.field_72996_f.iterator();

      while(i$.hasNext()) {
         Object obj = i$.next();
         if (obj instanceof EntityZombie) {
            EntityZombie victim = (EntityZombie)obj;
            if (victim.func_82231_m()) {
               double distanceSq = victim.func_70092_e(0.5D + (double)posX, 0.5D + (double)posY, 0.5D + (double)posZ);
               if (distanceSq > minSq && distanceSq <= radiusSq) {
                  Log.instance().debug(String.format("Try curing zombie %f %f %f", distanceSq, minSq, radiusSq));
                  villagersToZombify.add(victim);
               }
            }
         }
      }

      i$ = villagersToZombify.iterator();

      while(i$.hasNext()) {
         EntityZombie victim = (EntityZombie)i$.next();
         EntityVillager entityvillager = new EntityVillager(world);
         entityvillager.func_82149_j(victim);
         entityvillager.func_110161_a((IEntityLivingData)null);
         entityvillager.func_82187_q();
         if (victim.func_70631_g_()) {
            entityvillager.func_70873_a(-24000);
         }

         world.func_72900_e(victim);
         world.func_72838_d(entityvillager);
         entityvillager.func_70690_d(new PotionEffect(Potion.field_76431_k.field_76415_H, 200, 0));
         world.func_72889_a((EntityPlayer)null, 1017, (int)entityvillager.field_70165_t, (int)entityvillager.field_70163_u, (int)entityvillager.field_70161_v, 0);
      }

      return true;
   }
}
