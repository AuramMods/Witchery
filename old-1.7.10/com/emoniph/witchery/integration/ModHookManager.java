package com.emoniph.witchery.integration;

import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ModHookManager {
   private ArrayList<ModHook> hooks = new ArrayList();
   public boolean isTinkersPresent;
   public boolean isAM2Present;
   public boolean isMorphPresent;

   public void register(Class<? extends ModHook> clazz) {
      try {
         ModHook hook = (ModHook)clazz.newInstance();
         this.hooks.add(hook);
      } catch (Throwable var3) {
         Log.instance().warning(var3, "unhandled exception loading ModHook");
      }

   }

   public void init() {
      Iterator i$ = this.hooks.iterator();

      while(i$.hasNext()) {
         ModHook hook = (ModHook)i$.next();

         try {
            hook.init();
         } catch (Throwable var4) {
            Log.instance().warning(var4, String.format("unhandled exception init for hook %s", hook.getModID()));
         }
      }

   }

   public void postInit() {
      Iterator i$ = this.hooks.iterator();

      while(i$.hasNext()) {
         ModHook hook = (ModHook)i$.next();

         try {
            hook.postInit();
         } catch (Throwable var4) {
            Log.instance().warning(var4, String.format("unhandled exception post init for hook %s", hook.getModID()));
         }
      }

   }

   public void reducePowerLevels(EntityLivingBase entity, float reduction) {
      if (entity != null && entity.field_70170_p != null && !entity.field_70170_p.field_72995_K) {
         if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entity;
            int maxEnergy = Infusion.getMaxEnergy(player);
            int currentEnergy = Infusion.getCurrentEnergy(player);
            if (maxEnergy > 0 && currentEnergy > 0) {
               int reduceBy = Math.max((int)((float)maxEnergy * reduction), 1);
               int newMana = Math.max(currentEnergy - reduceBy, 0);
               Infusion.setCurrentEnergy(player, newMana);
            }
         }

         Iterator i$ = this.hooks.iterator();

         while(i$.hasNext()) {
            ModHook hook = (ModHook)i$.next();

            try {
               hook.reduceMagicPower(entity, reduction);
            } catch (Throwable var8) {
               Log.instance().warning(var8, String.format("unhandled exception post init for hook %s", hook.getModID()));
            }
         }

      }
   }

   public void boostBloodPowers(EntityPlayer player, float health) {
      Iterator i$ = this.hooks.iterator();

      while(i$.hasNext()) {
         ModHook hook = (ModHook)i$.next();

         try {
            hook.boostBloodPowers(player, health);
         } catch (Throwable var6) {
            Log.instance().warning(var6, String.format("unhandled exception post init for hook %s", hook.getModID()));
         }
      }

   }

   public boolean canVampireBeKilled(EntityPlayer player) {
      Iterator i$ = this.hooks.iterator();

      while(i$.hasNext()) {
         ModHook hook = (ModHook)i$.next();

         try {
            if (hook.canVampireBeKilled(player)) {
               return true;
            }
         } catch (Throwable var5) {
            Log.instance().warning(var5, String.format("unhandled exception post init for hook %s", hook.getModID()));
         }
      }

      return false;
   }

   public void makeItemModProof(ItemStack stack) {
      Iterator i$ = this.hooks.iterator();

      while(i$.hasNext()) {
         ModHook hook = (ModHook)i$.next();

         try {
            hook.tryMakeItemModProof(stack);
         } catch (Throwable var5) {
            Log.instance().warning(var5, String.format("unhandled exception post init for hook %s", hook.getModID()));
         }
      }

   }
}
