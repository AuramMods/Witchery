package com.emoniph.witchery.util;

import com.emoniph.witchery.entity.EntityBolt;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSourceIndirect;

public class BoltDamageSource extends EntityDamageSourceIndirect {
   public final boolean isWooden;
   public final boolean isHoly;
   public final boolean isPoweredDraining;

   public BoltDamageSource(EntityBolt bolt, Entity shooter) {
      super("arrow", bolt, shooter);
      this.func_76349_b();
      this.isWooden = bolt.isWoodenDamage();
      this.isPoweredDraining = bolt.isPoweredDraining();
      this.isHoly = bolt.isHolyDamage();
   }

   public String toString() {
      return super.toString() + String.format(" Bolt wood=%s holy=%s drain=%s", this.isWooden, this.isHoly, this.isPoweredDraining);
   }
}
