package com.emoniph.witchery.common;

import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.Log;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PowerSources {
   private static PowerSources INSTANCE_CLIENT;
   private static PowerSources INSTANCE_SERVER;
   private final ArrayList<IPowerSource> powerSources = new ArrayList();
   private final ArrayList<INullSource> nullSources = new ArrayList();

   public static PowerSources instance() {
      return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER ? INSTANCE_SERVER : INSTANCE_CLIENT;
   }

   public static void initiate() {
      INSTANCE_CLIENT = new PowerSources();
      INSTANCE_SERVER = new PowerSources();
   }

   private PowerSources() {
   }

   public String getDebugData() {
      StringBuilder sb = new StringBuilder();

      IPowerSource source;
      for(Iterator i$ = this.powerSources.iterator(); i$.hasNext(); sb.append(String.format("Altar (%s) [dim=%d] power=%f", source.getLocation(), source.getWorld().field_73011_w.field_76574_g, source.getCurrentPower()))) {
         source = (IPowerSource)i$.next();
         if (sb.length() > 0) {
            sb.append('\n');
         }
      }

      return sb.length() > 0 ? sb.insert(0, "** ALTARS **\n").toString() : "No power sources";
   }

   public void registerPowerSource(IPowerSource powerSource) {
      if (!this.powerSources.contains(powerSource)) {
         try {
            Iterator it = this.powerSources.iterator();

            label29:
            while(true) {
               IPowerSource source;
               do {
                  if (!it.hasNext()) {
                     break label29;
                  }

                  source = (IPowerSource)it.next();
               } while(source != null && !source.isPowerInvalid() && !source.getLocation().equals(powerSource.getLocation()));

               it.remove();
            }
         } catch (Throwable var4) {
            Log.instance().warning(var4, "Exception occured validating existing power source entries");
         }

         this.powerSources.add(powerSource);
      }

   }

   public void removePowerSource(IPowerSource powerSource) {
      if (this.powerSources.contains(powerSource)) {
         this.powerSources.remove(powerSource);
      }

      try {
         Iterator it = this.powerSources.iterator();

         while(true) {
            while(it.hasNext()) {
               IPowerSource source = (IPowerSource)it.next();
               if (source != null && !source.isPowerInvalid()) {
                  if (source.getLocation().getBlockTileEntity(source.getWorld()) != source) {
                     it.remove();
                  }
               } else {
                  it.remove();
               }
            }

            return;
         }
      } catch (Throwable var4) {
         Log.instance().warning(var4, "Exception occured removing existing power source entries");
      }
   }

   public ArrayList<PowerSources.RelativePowerSource> get(World world, Coord location, int radius) {
      ArrayList<PowerSources.RelativePowerSource> nearbyPowerSources = new ArrayList();
      double radiusSq = (double)(radius * radius);
      Iterator i$ = this.powerSources.iterator();

      while(i$.hasNext()) {
         IPowerSource registeredSource = (IPowerSource)i$.next();
         PowerSources.RelativePowerSource powerSource = new PowerSources.RelativePowerSource(registeredSource, location);
         if (powerSource.isInWorld(world) && powerSource.isInRange()) {
            nearbyPowerSources.add(powerSource);
         }
      }

      Collections.sort(nearbyPowerSources, new Comparator<PowerSources.RelativePowerSource>() {
         public int compare(PowerSources.RelativePowerSource a, PowerSources.RelativePowerSource b) {
            return Double.compare(a.distanceSq, b.distanceSq);
         }
      });
      return nearbyPowerSources;
   }

   public void registerNullSource(INullSource nullSource) {
      if (!this.nullSources.contains(nullSource)) {
         Coord newLocation = new Coord(nullSource);

         try {
            Iterator it = this.nullSources.iterator();

            label30:
            while(true) {
               INullSource source;
               do {
                  if (!it.hasNext()) {
                     break label30;
                  }

                  source = (INullSource)it.next();
               } while(source != null && !source.isPowerInvalid() && !(new Coord(source)).equals(newLocation));

               it.remove();
            }
         } catch (Throwable var5) {
            Log.instance().warning(var5, "Exception occured validating existing null source entries");
         }

         this.nullSources.add(nullSource);
      }

   }

   public void removeNullSource(INullSource nullSource) {
      if (this.nullSources.contains(nullSource)) {
         this.nullSources.remove(nullSource);
      }

      try {
         Iterator it = this.nullSources.iterator();

         while(true) {
            while(it.hasNext()) {
               INullSource source = (INullSource)it.next();
               if (source != null && !source.isPowerInvalid()) {
                  if ((new Coord(source)).getBlockTileEntity(source.getWorld()) != source) {
                     it.remove();
                  }
               } else {
                  it.remove();
               }
            }

            return;
         }
      } catch (Throwable var4) {
         Log.instance().warning(var4, "Exception occured removing existing null source entries");
      }
   }

   public boolean isAreaNulled(World world, int posX, int posY, int posZ) {
      Iterator i$ = this.nullSources.iterator();

      INullSource source;
      double rangeSq;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         source = (INullSource)i$.next();
         rangeSq = (double)(source.getRange() * source.getRange());
      } while(!(Coord.distanceSq((double)posX, (double)posY, (double)posZ, (double)source.getPosX(), (double)source.getPosY(), (double)source.getPosZ()) < rangeSq));

      return true;
   }

   public static IPowerSource findClosestPowerSource(World world, int posX, int posY, int posZ) {
      List<PowerSources.RelativePowerSource> sources = instance() != null ? instance().get(world, new Coord(posX, posY, posZ), 16) : null;
      return sources != null && sources.size() > 0 ? ((PowerSources.RelativePowerSource)sources.get(0)).source() : null;
   }

   public static IPowerSource findClosestPowerSource(World world, Coord coord) {
      return findClosestPowerSource(world, coord.x, coord.y, coord.z);
   }

   public static IPowerSource findClosestPowerSource(TileEntity tile) {
      return findClosestPowerSource(tile.func_145831_w(), tile.field_145851_c, tile.field_145848_d, tile.field_145849_e);
   }

   public static class RelativePowerSource {
      private final IPowerSource powerSource;
      private final double distanceSq;
      private final double rangeSq;

      public RelativePowerSource(IPowerSource powerSource, Coord relativeLocation) {
         this.powerSource = powerSource;
         this.distanceSq = relativeLocation.distanceSqTo(this.powerSource.getLocation());
         double range = (double)powerSource.getRange();
         this.rangeSq = range * range;
      }

      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && obj.getClass() == this.getClass()) {
            return ((PowerSources.RelativePowerSource)obj).powerSource == this.powerSource;
         } else {
            return false;
         }
      }

      public boolean isInWorld(World world) {
         return this.powerSource.getWorld() == world;
      }

      public IPowerSource source() {
         return this.powerSource;
      }

      public boolean isInRange() {
         return this.distanceSq <= this.rangeSq;
      }
   }
}
