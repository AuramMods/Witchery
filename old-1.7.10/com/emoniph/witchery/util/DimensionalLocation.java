package com.emoniph.witchery.util;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public class DimensionalLocation {
   public final int dimension;
   public final double posX;
   public final double posY;
   public final double posZ;
   public final boolean isValid;

   public DimensionalLocation(Entity entity) {
      this(entity.field_71093_bK, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, true);
   }

   public DimensionalLocation(NBTTagCompound nbtTag, String prefix) {
      this(nbtTag.func_74762_e(prefix + "D"), nbtTag.func_74769_h(prefix + "X"), nbtTag.func_74769_h(prefix + "Y"), nbtTag.func_74769_h(prefix + "Z"), nbtTag.func_74764_b(prefix + "D") && nbtTag.func_74764_b(prefix + "X") && nbtTag.func_74764_b(prefix + "Y") && nbtTag.func_74764_b(prefix + "Z"));
   }

   public DimensionalLocation(int dimension, double posX, double posY, double posZ) {
      this(dimension, posX, posY, posZ, true);
   }

   protected DimensionalLocation(int dimension, double posX, double posY, double posZ, boolean isValid) {
      this.dimension = dimension;
      this.posX = posX;
      this.posY = posY;
      this.posZ = posZ;
      this.isValid = isValid;
   }

   public void saveToNBT(NBTTagCompound nbtTag, String prefix) {
      nbtTag.func_74768_a(prefix + "D", this.dimension);
      nbtTag.func_74780_a(prefix + "X", this.posX);
      nbtTag.func_74780_a(prefix + "Y", this.posY);
      nbtTag.func_74780_a(prefix + "Z", this.posZ);
   }
}
