package com.emoniph.witchery.infusion.infusions.creature;

import com.emoniph.witchery.infusion.Infusion;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class CreaturePowerCreeper extends CreaturePower {
   private static final float WEB_DAMAGE = 1.0F;
   private final int explosionRadius = 3;

   public CreaturePowerCreeper(int powerID) {
      super(powerID, EntityCreeper.class);
   }

   public int activateCost(World world, EntityPlayer player, int elapsedTicks, MovingObjectPosition mop) {
      int baseCost = super.activateCost(world, player, elapsedTicks, mop);
      return elapsedTicks >= 60 ? baseCost * 2 : baseCost;
   }

   public void onActivate(World world, EntityPlayer player, int elapsedTicks, MovingObjectPosition mop) {
      if (!world.field_72995_K) {
         double var10002 = player.field_70165_t;
         double var10003 = player.field_70163_u;
         double var10004 = player.field_70161_v;
         float var10005;
         if (elapsedTicks >= 60) {
            this.getClass();
            var10005 = 3.0F * 2.0F;
         } else {
            this.getClass();
            var10005 = 3.0F;
         }

         world.func_72876_a(player, var10002, var10003, var10004, var10005, world.func_82736_K().func_82766_b("mobGriefing"));
      }

   }

   public void onDamage(World world, EntityPlayer player, LivingHurtEvent event) {
      if (event.source.func_76347_k()) {
         StackTraceElement[] stack = Thread.currentThread().getStackTrace();
         StackTraceElement[] arr$ = stack;
         int len$ = stack.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            StackTraceElement element = arr$[i$];
            if (element.getMethodName().equals("onStruckByLightning")) {
               int power = Infusion.getNBT(player).func_74762_e("witcheryInfusionCharges");
               Infusion.getNBT(player).func_74768_a("witcheryInfusionCharges", Math.min(power + 25, 200));
               Infusion.syncPlayer(world, player);
               if (event.isCancelable()) {
                  event.setCanceled(true);
               }

               return;
            }
         }
      }

   }
}
