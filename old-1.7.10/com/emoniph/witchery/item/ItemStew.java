package com.emoniph.witchery.item;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.ItemUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemStew extends ItemFood {
   public ItemStew(int hunger, float saturation) {
      super(hunger, saturation, false);
   }

   public Item func_77655_b(String itemName) {
      ItemUtil.registerItem(this, itemName);
      this.func_77637_a(WitcheryCreativeTab.INSTANCE);
      return super.func_77655_b(itemName);
   }

   public ItemStack func_77654_b(ItemStack stack, World world, EntityPlayer player) {
      super.func_77654_b(stack, world, player);
      ItemStack bowlStack = new ItemStack(Items.field_151054_z);
      if (!player.field_71071_by.func_70441_a(bowlStack)) {
         world.func_72838_d(new EntityItem(world, player.field_70165_t + 0.5D, player.field_70163_u + 1.5D, player.field_70161_v + 0.5D, bowlStack));
      } else if (player instanceof EntityPlayerMP) {
         ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
      }

      return stack;
   }
}
