package com.emoniph.witchery.ritual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public abstract class Rite {
   protected boolean canRelocate = false;

   public abstract void addSteps(ArrayList<RitualStep> var1, int var2);

   public ArrayList<EntityItem> getItemsInRadius(World world, int x, int y, int z, float radius) {
      float RADIUS_SQ = radius * radius;
      double midX = 0.5D + (double)x;
      double midZ = 0.5D + (double)z;
      ArrayList<EntityItem> resultList = new ArrayList();
      AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(midX - (double)radius, (double)y, midZ - (double)radius, midX + (double)radius, 1.0D + (double)y, midZ + (double)radius);
      List items = world.func_72872_a(EntityItem.class, bounds);
      Iterator i$ = items.iterator();

      while(i$.hasNext()) {
         Object obj = i$.next();
         EntityItem entity = (EntityItem)obj;
         if (entity.func_70092_e(midX, (double)y, midZ) <= (double)RADIUS_SQ) {
            resultList.add(entity);
         }
      }

      return resultList;
   }

   public boolean relocatable() {
      return this.canRelocate;
   }
}
