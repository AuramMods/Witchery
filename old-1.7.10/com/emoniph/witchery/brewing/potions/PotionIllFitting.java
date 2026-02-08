package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.entity.EntityFollower;
import com.emoniph.witchery.entity.EntityReflection;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class PotionIllFitting extends PotionBase {
   public PotionIllFitting(int id, int color) {
      super(id, true, color);
   }

   public void postContructInitialize() {
      this.setPermenant();
      this.setIncurable();
   }

   public boolean func_76397_a(int duration, int amplifier) {
      if (duration % 15 == 0) {
         switch(amplifier) {
         case 1:
            return duration <= 30;
         case 2:
            return duration <= 45;
         case 3:
            return duration <= 60;
         default:
            return duration <= 15;
         }
      } else {
         return false;
      }
   }

   public void func_76394_a(EntityLivingBase entity, int amplifier) {
      World world = entity.field_70170_p;
      if (!world.field_72995_K && !isTargetBanned(entity)) {
         int slot = world.field_73012_v.nextInt(4) + 1;
         ItemStack armorPiece = entity.func_71124_b(slot);
         if (armorPiece != null) {
            entity.func_70062_b(slot, (ItemStack)null);
            EntityItem droppedItem = entity.func_70099_a(armorPiece, 0.0F);
            droppedItem.field_145804_b = 5 + 5 * amplifier;
         }
      }

   }

   public static boolean isTargetBanned(EntityLivingBase entity) {
      return entity instanceof EntityReflection || entity instanceof EntityFollower;
   }
}
