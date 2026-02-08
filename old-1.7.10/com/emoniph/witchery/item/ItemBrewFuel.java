package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemBrewFuel extends ItemBase implements IFuelHandler {
   @SideOnly(Side.CLIENT)
   protected IIcon itemIconOverlay;
   private static final int[] COLORS = new int[]{16754270, 16748088, 16740620, 14702848};
   private static final int[] BURN_TIMES = new int[]{2400, 5000, 10000, 50000};

   public ItemBrewFuel() {
      this.func_77625_d(64);
      this.func_77656_e(0);
      this.func_77627_a(true);
   }

   public Item func_77655_b(String itemName) {
      GameRegistry.registerFuelHandler(this);
      return super.func_77655_b(itemName);
   }

   @SideOnly(Side.CLIENT)
   public boolean hasEffect(ItemStack stack, int pass) {
      return pass == 0;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_77623_v() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public IIcon getIcon(ItemStack stack, int pass) {
      return pass == 0 ? this.itemIconOverlay : this.field_77791_bV;
   }

   @SideOnly(Side.CLIENT)
   public void func_94581_a(IIconRegister iconRegister) {
      super.func_94581_a(iconRegister);
      this.itemIconOverlay = iconRegister.func_94245_a("witchery:brew_overlay");
   }

   @SideOnly(Side.CLIENT)
   public int func_82790_a(ItemStack stack, int pass) {
      if (pass == 0) {
         int color = Math.min(stack.func_77960_j(), COLORS.length);
         return COLORS[color];
      } else {
         return super.func_82790_a(stack, pass);
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean expanded) {
      String localText = Witchery.resource("item.witchery:brew.fuel." + Math.min(stack.func_77960_j(), BURN_TIMES.length));
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

   public int getBurnTime(ItemStack fuel) {
      if (fuel.func_77973_b() == this) {
         int burnTime = BURN_TIMES[Math.min(fuel.func_77960_j(), BURN_TIMES.length)];
         return burnTime;
      } else {
         return 0;
      }
   }
}
