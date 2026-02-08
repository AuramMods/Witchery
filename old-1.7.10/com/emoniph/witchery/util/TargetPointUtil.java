package com.emoniph.witchery.util;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public final class TargetPointUtil {
   public static TargetPoint from(Entity entity, double range) {
      return entity != null ? new TargetPoint(entity.field_71093_bK, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, range) : new TargetPoint(0, 0.0D, 0.0D, 0.0D, range);
   }

   public static TargetPoint from(World world, double x, double y, double z, double range) {
      return new TargetPoint(world.field_73011_w.field_76574_g, x, y, z, range);
   }
}
