package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockMirror;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class ItemMirror extends ItemBase {
   public ItemMirror() {
      this.func_77625_d(1);
   }

   @SideOnly(Side.CLIENT)
   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean advTooltips) {
      super.func_77624_a(stack, player, list, advTooltips);
      NBTTagCompound nbtRoot = stack.func_77978_p();
      if (nbtRoot != null && nbtRoot.func_74767_n("DemonSlain")) {
         if (!Config.instance().isDebugging()) {
            list.add(Witchery.resource("item.witchery:mirror.tip.bridge"));
         }
      } else {
         list.add(Witchery.resource("item.witchery:mirror.tip.inhabited"));
      }

      if (Config.instance().isDebugging() && nbtRoot != null && nbtRoot.func_74764_b("DimCoords")) {
         int dim = nbtRoot.func_74762_e("Dimension");
         Coord coord = Coord.fromTagNBT(nbtRoot.func_74775_l("DimCoords"));
         WorldProvider prov = WorldProvider.func_76570_a(dim);
         String dimName = prov != null ? prov.func_80007_l() : Integer.valueOf(dim).toString();
         list.add(String.format(Witchery.resource("item.witchery:mirror.tip.bridgeplus"), dimName, coord.x, coord.y, coord.z));
      }

   }

   public boolean func_77648_a(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
      if (world.field_72995_K) {
         return true;
      } else {
         int meta = 0;
         BlockMirror mirror = Witchery.Blocks.MIRROR;
         switch(side) {
         case 0:
         case 1:
         default:
            break;
         case 2:
            meta = 0;
            --z;
            break;
         case 3:
            meta = 1;
            ++z;
            break;
         case 4:
            meta = 2;
            --x;
            break;
         case 5:
            meta = 3;
            ++x;
         }

         if (player.func_82247_a(x, y, z, side, stack) && player.func_82247_a(x, y - 1, z, side, stack)) {
            if (world.func_147437_c(x, y, z) && world.func_147437_c(x, y - 1, z)) {
               world.func_147465_d(x, y, z, mirror, meta | 4, 3);
               if (world.func_147439_a(x, y, z) == mirror) {
                  world.func_147465_d(x, y - 1, z, mirror, meta, 3);
                  Witchery.Blocks.MIRROR.loadFromItem(stack, player, world, x, y, z);
               }

               --stack.field_77994_a;
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }
}
