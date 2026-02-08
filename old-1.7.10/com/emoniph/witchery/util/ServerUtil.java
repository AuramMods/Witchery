package com.emoniph.witchery.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class ServerUtil {
   public static WorldServer getWorld(int dimension) {
      WorldServer[] arr$ = MinecraftServer.func_71276_C().field_71305_c;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         WorldServer world = arr$[i$];
         if (world.field_73011_w.field_76574_g == dimension) {
            return world;
         }
      }

      return null;
   }
}
