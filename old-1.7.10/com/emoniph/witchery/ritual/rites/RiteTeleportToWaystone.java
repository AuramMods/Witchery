package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.brewing.potions.PotionEnderInhibition;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.Coord;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class RiteTeleportToWaystone extends RiteTeleportation {
   public RiteTeleportToWaystone(int radius) {
      super(radius);
   }

   protected boolean teleport(World world, int posX, int posY, int posZ, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
      if (!world.field_72995_K) {
         ItemStack waystoneStack = null;
         Iterator i$ = ritual.sacrificedItems.iterator();

         label37: {
            RitualStep.SacrificedItem item;
            do {
               if (!i$.hasNext()) {
                  break label37;
               }

               item = (RitualStep.SacrificedItem)i$.next();
            } while(!Witchery.Items.GENERIC.itemWaystoneBound.isMatch(item.itemstack) && !Witchery.Items.GENERIC.itemWaystonePlayerBound.isMatch(item.itemstack));

            waystoneStack = item.itemstack;
         }

         if (waystoneStack != null) {
            AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)(posX - this.radius), (double)(posY - this.radius), (double)(posZ - this.radius), (double)(posX + this.radius), (double)(posY + this.radius), (double)(posZ + this.radius));
            List list = world.func_72872_a(Entity.class, bounds);
            boolean sent = false;
            Iterator iterator = list.iterator();

            while(iterator.hasNext()) {
               Entity entity = (Entity)iterator.next();
               if (Coord.distance(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, (double)posX, (double)posY, (double)posZ) < (double)this.radius && !PotionEnderInhibition.isActive(entity, 1) && Witchery.Items.GENERIC.teleportToLocation(world, waystoneStack, entity, this.radius, true)) {
                  sent = true;
               }
            }

            return sent;
         }
      }

      return false;
   }
}
