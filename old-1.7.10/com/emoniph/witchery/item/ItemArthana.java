package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.blocks.BlockPlacedItem;
import com.emoniph.witchery.util.ItemUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.world.World;

public class ItemArthana extends ItemSword {
   public ItemArthana() {
      super(ToolMaterial.GOLD);
      this.func_77656_e(ToolMaterial.IRON.func_77997_a());
      this.func_77637_a(WitcheryCreativeTab.INSTANCE);
   }

   public Item func_77655_b(String itemName) {
      ItemUtil.registerItem(this, itemName);
      return super.func_77655_b(itemName);
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack itemstack) {
      return EnumRarity.uncommon;
   }

   public boolean func_77648_a(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
      if (world.func_147439_a(x, y, z) == Witchery.Blocks.ALTAR && side == 1 && world.func_147439_a(x, y + 1, z) == Blocks.field_150350_a) {
         BlockPlacedItem.placeItemInWorld(stack, player, world, x, y + 1, z);
         player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
      }

      return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
   }
}
