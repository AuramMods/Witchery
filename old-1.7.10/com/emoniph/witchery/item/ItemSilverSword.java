package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.ItemUtil;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.Item.ToolMaterial;

public class ItemSilverSword extends ItemSword {
   public ItemSilverSword() {
      super(ToolMaterial.GOLD);
      this.func_77637_a(WitcheryCreativeTab.INSTANCE);
   }

   public Item func_77655_b(String itemName) {
      ItemUtil.registerItem(this, itemName);
      return super.func_77655_b(itemName);
   }

   public String func_150932_j() {
      return "SILVER";
   }

   public int func_77619_b() {
      return ToolMaterial.IRON.func_77995_e();
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean moreTips) {
      String localText = Witchery.resource(this.func_77658_a() + ".tip");
      if (localText != null) {
         String[] arr$ = localText.split("\n");
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String s = arr$[i$];
            if (!s.isEmpty()) {
               list.add(s);
            }
         }
      }

   }
}
