package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemVanillaPotion extends ItemBase {
   public final ArrayList<ItemVanillaPotion.SubItem> subItems = new ArrayList();
   public final ItemVanillaPotion.SubItem potionAntidote;
   @SideOnly(Side.CLIENT)
   private IIcon field_94591_c;
   @SideOnly(Side.CLIENT)
   private IIcon field_94590_d;
   @SideOnly(Side.CLIENT)
   private IIcon field_94592_ct;

   public ItemVanillaPotion() {
      this.potionAntidote = ItemVanillaPotion.SubItem.register(new ItemVanillaPotion.SubItem(0, "antidote", 65280) {
         public void onDrunk(World world, EntityPlayer player, ItemStack stack) {
            if (player != null && world != null && !world.field_72995_K) {
               if (player.func_70644_a(Potion.field_76436_u)) {
                  player.func_82170_o(Potion.field_76436_u.field_76415_H);
               }

               if (player.func_70644_a(Potion.field_82731_v)) {
                  player.func_82170_o(Potion.field_82731_v.field_76415_H);
               }
            }

         }
      }, this.subItems);
      this.func_77625_d(4);
      this.func_77627_a(true);
      this.func_77656_e(0);
   }

   public ItemStack func_77654_b(ItemStack stack, World world, EntityPlayer player) {
      if (!player.field_71075_bZ.field_75098_d) {
         --stack.field_77994_a;
      }

      if (!world.field_72995_K) {
         int damage = stack.func_77960_j();
         if (damage >= 0 && damage < this.subItems.size()) {
            ((ItemVanillaPotion.SubItem)this.subItems.get(damage)).onDrunk(world, player, stack);
         }
      }

      if (!player.field_71075_bZ.field_75098_d) {
         if (stack.field_77994_a <= 0) {
            return new ItemStack(Items.field_151069_bo);
         }

         player.field_71071_by.func_70441_a(new ItemStack(Items.field_151069_bo));
      }

      return stack;
   }

   public int func_77626_a(ItemStack par1ItemStack) {
      return 32;
   }

   public EnumAction func_77661_b(ItemStack par1ItemStack) {
      return EnumAction.drink;
   }

   public ItemStack func_77659_a(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
      par3EntityPlayer.func_71008_a(par1ItemStack, this.func_77626_a(par1ItemStack));
      return par1ItemStack;
   }

   public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_77617_a(int par1) {
      return this.field_94590_d;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_77618_c(int par1, int par2) {
      return par2 == 0 ? this.field_94592_ct : super.func_77618_c(par1, par2);
   }

   public String func_77667_c(ItemStack itemStack) {
      int damage = itemStack.func_77960_j();

      assert damage >= 0 && damage < this.subItems.size() : "damage value is too large";

      return super.func_77658_a() + "." + ((ItemVanillaPotion.SubItem)this.subItems.get(Math.max(damage, 0))).unlocalizedName;
   }

   @SideOnly(Side.CLIENT)
   public int getColorFromDamage(int damage) {
      return damage >= 0 && damage < this.subItems.size() ? ((ItemVanillaPotion.SubItem)this.subItems.get(damage)).color : 0;
   }

   @SideOnly(Side.CLIENT)
   public int func_82790_a(ItemStack par1ItemStack, int par2) {
      return par2 > 0 ? 16777215 : this.getColorFromDamage(par1ItemStack.func_77960_j());
   }

   @SideOnly(Side.CLIENT)
   public boolean func_77623_v() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_77636_d(ItemStack par1ItemStack) {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public void func_150895_a(Item item, CreativeTabs creativeTabs, List itemList) {
      Iterator i$ = this.subItems.iterator();

      while(i$.hasNext()) {
         ItemVanillaPotion.SubItem subItem = (ItemVanillaPotion.SubItem)i$.next();
         if (subItem.showInCreativeTab) {
            itemList.add(subItem.createStack());
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_94581_a(IIconRegister par1IconRegister) {
      this.field_94590_d = par1IconRegister.func_94245_a(this.func_111208_A() + "_" + "bottle_drinkable");
      this.field_94591_c = par1IconRegister.func_94245_a(this.func_111208_A() + "_" + "bottle_splash");
      this.field_94592_ct = par1IconRegister.func_94245_a(this.func_111208_A() + "_" + "overlay");
   }

   public static class SubItem {
      public final int damageValue;
      private final String unlocalizedName;
      private final int rarity;
      private final boolean showInCreativeTab;
      private final int color;
      @SideOnly(Side.CLIENT)
      private IIcon icon;

      private static <T extends ItemVanillaPotion.SubItem> T register(T subItem, ArrayList<ItemVanillaPotion.SubItem> subItems) {
         assert subItems.size() == subItem.damageValue : "Misalignement with subItem registration";

         subItems.add(subItem);
         return subItem;
      }

      private SubItem(int damageValue, String unlocalizedName, int color) {
         this(damageValue, unlocalizedName, color, 0, true);
      }

      private SubItem(int damageValue, String unlocalizedName, int color, int rarity) {
         this(damageValue, unlocalizedName, color, rarity, true);
      }

      private SubItem(int damageValue, String unlocalizedName, int color, int rarity, boolean showInCreativeTab) {
         this.damageValue = damageValue;
         this.unlocalizedName = unlocalizedName;
         this.rarity = rarity;
         this.showInCreativeTab = showInCreativeTab;
         this.color = color;
      }

      @SideOnly(Side.CLIENT)
      private void registerIcon(IIconRegister iconRegister, ItemVanillaPotion itemIngredient) {
         this.icon = iconRegister.func_94245_a(itemIngredient.func_111208_A() + "." + this.unlocalizedName);
      }

      public boolean isMatch(ItemStack itemstack) {
         return itemstack != null && Witchery.Items.POTIONS == itemstack.func_77973_b() && itemstack.func_77960_j() == this.damageValue;
      }

      public ItemStack createStack(int stackSize) {
         return new ItemStack(Witchery.Items.POTIONS, stackSize, this.damageValue);
      }

      public ItemStack createStack() {
         return this.createStack(1);
      }

      public boolean isItemInInventory(InventoryPlayer inventory) {
         return this.getItemSlotFromInventory(inventory) != -1;
      }

      public int getItemSlotFromInventory(InventoryPlayer inventory) {
         for(int k = 0; k < inventory.field_70462_a.length; ++k) {
            if (inventory.field_70462_a[k] != null && inventory.field_70462_a[k].func_77973_b() == Witchery.Items.POTIONS && inventory.field_70462_a[k].func_77960_j() == this.damageValue) {
               return k;
            }
         }

         return -1;
      }

      public boolean consumeItemFromInventory(InventoryPlayer inventory) {
         int j = this.getItemSlotFromInventory(inventory);
         if (j < 0) {
            return false;
         } else {
            if (--inventory.field_70462_a[j].field_77994_a <= 0) {
               inventory.field_70462_a[j] = null;
            }

            return true;
         }
      }

      public void onDrunk(World world, EntityPlayer player, ItemStack stack) {
      }

      // $FF: synthetic method
      SubItem(int x0, String x1, int x2, Object x3) {
         this(x0, x1, x2);
      }
   }
}
