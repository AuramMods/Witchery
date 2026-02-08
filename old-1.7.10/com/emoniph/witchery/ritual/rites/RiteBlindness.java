package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import java.util.Iterator;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class RiteBlindness extends RiteExpandingEffect {
   public RiteBlindness(int radius, int height) {
      super(radius, height, true);
   }

   public boolean doRadiusAction(World world, int posX, int posY, int posZ, int radius, EntityPlayer player, boolean enhanced) {
      double radiusSq = (double)(radius * radius);
      double minSq = (double)Math.max(0, (radius - 1) * (radius - 1));
      Iterator i$ = world.field_73010_i.iterator();

      Object obj;
      double distanceSq;
      while(i$.hasNext()) {
         obj = i$.next();
         EntityPlayer victim = (EntityPlayer)obj;
         distanceSq = victim.func_70092_e(0.5D + (double)posX, 0.5D + (double)posY, 0.5D + (double)posZ);
         if (distanceSq > minSq && distanceSq <= radiusSq) {
            if (Witchery.Items.POPPET.voodooProtectionActivated(player, (ItemStack)null, victim, 6)) {
               return false;
            }

            if (!victim.func_70644_a(Potion.field_76440_q)) {
               victim.func_70690_d(new PotionEffect(Potion.field_76440_q.field_76415_H, (enhanced ? 5 : 2) * 1200, 0));
            }
         }
      }

      i$ = world.field_72996_f.iterator();

      while(i$.hasNext()) {
         obj = i$.next();
         if (obj instanceof EntityLiving) {
            EntityLiving victim = (EntityLiving)obj;
            distanceSq = victim.func_70092_e(0.5D + (double)posX, 0.5D + (double)posY, 0.5D + (double)posZ);
            if (distanceSq > minSq && distanceSq <= radiusSq && !victim.func_70644_a(Potion.field_76440_q)) {
               victim.func_70690_d(new PotionEffect(Potion.field_76440_q.field_76415_H, (enhanced ? 5 : 2) * 1200, 0));
            }
         }
      }

      return true;
   }

   public void doBlockAction(World world, int posX, int posY, int posZ, int currentRadius, EntityPlayer player, boolean enhanced) {
   }
}
