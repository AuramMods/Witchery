package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class ItemBook extends ItemBase {
   static final String CURRENT_PAGE_KEY = "CurrentPage";
   public static final Type[] BIOME_TYPES;

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      int posX = MathHelper.func_76128_c(player.field_70165_t);
      int posY = MathHelper.func_76128_c(player.field_70163_u);
      int posZ = MathHelper.func_76128_c(player.field_70161_v);
      player.openGui(Witchery.instance, 6, world, posX, posY, posZ);
      return stack;
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean expandedTooltip) {
      list.add(String.format(Witchery.resource("witchery.biomebook.currentpage"), getSelectedBiome(getSelectedBiome(stack, 1000)).field_76791_y));
      list.add("");
      String[] arr$ = Witchery.resource("item.witchery:biomebook2.tip").split("\n");
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String s = arr$[i$];
         if (!s.isEmpty()) {
            list.add(s);
         }
      }

   }

   public static int getSelectedBiome(ItemStack stack, int maxPages) {
      NBTTagCompound stackCompound = stack.func_77978_p();
      return stackCompound != null && stackCompound.func_74764_b("CurrentPage") ? Math.min(Math.max(stackCompound.func_74762_e("CurrentPage"), 0), Math.max(maxPages, 1) - 1) : 0;
   }

   public static BiomeGenBase getSelectedBiome(int page) {
      new ArrayList();
      int i = 0;
      Type[] arr$ = BIOME_TYPES;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Type biomeType = arr$[i$];
         BiomeGenBase[] biomesInType = BiomeDictionary.getBiomesForType(biomeType);

         for(int j = 0; j < biomesInType.length; ++j) {
            if (i++ == page) {
               return biomesInType[j];
            }
         }
      }

      return BiomeGenBase.field_76772_c;
   }

   public ItemStack getContainerItem(ItemStack stack) {
      return !this.hasContainerItem(stack) ? null : stack.func_77946_l();
   }

   public static void setSelectedBiome(ItemStack itemstack, int pageIndex) {
      if (itemstack.func_77978_p() == null) {
         itemstack.func_77982_d(new NBTTagCompound());
      }

      itemstack.func_77978_p().func_74768_a("CurrentPage", pageIndex);
   }

   static {
      BIOME_TYPES = new Type[]{Type.BEACH, Type.FOREST, Type.HILLS, Type.MESA, Type.MOUNTAIN, Type.PLAINS, Type.SANDY, Type.SNOWY, Type.SWAMP, Type.WASTELAND, Type.RIVER, Type.OCEAN, Type.SPOOKY, Type.MAGICAL};
   }
}
