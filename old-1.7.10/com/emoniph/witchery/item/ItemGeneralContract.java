package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemGeneralContract extends ItemGeneral.SubItem {
   public ItemGeneralContract(int damageValue, String unlocalizedName) {
      super(damageValue, unlocalizedName);
   }

   public static boolean isBoundContract(ItemStack stack) {
      if (stack.func_77973_b() == Witchery.Items.GENERIC) {
         ItemGeneral.SubItem subItem = (ItemGeneral.SubItem)Witchery.Items.GENERIC.subItems.get(Math.max(stack.func_77960_j(), 0));
         return subItem instanceof ItemGeneralContract ? Witchery.Items.TAGLOCK_KIT.isTaglockPresent(stack, 1) : false;
      } else {
         return false;
      }
   }

   public static EntityLivingBase getBoundEntity(World world, EntityPlayer player, ItemStack stack) {
      EntityLivingBase boundEntity = Witchery.Items.TAGLOCK_KIT.getBoundEntity(world, player, stack, 1);
      return boundEntity;
   }

   public static ItemGeneralContract getContract(ItemStack stack) {
      ItemGeneral.SubItem subItem = (ItemGeneral.SubItem)Witchery.Items.GENERIC.subItems.get(stack.func_77960_j());
      return subItem instanceof ItemGeneralContract ? (ItemGeneralContract)subItem : null;
   }

   public boolean activate(ItemStack stack, EntityLivingBase targetEntity) {
      return false;
   }
}
