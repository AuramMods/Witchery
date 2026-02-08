package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class ItemLeapingLily extends ItemColored {
   public ItemLeapingLily(Block par1) {
      super(par1, false);
   }

   public ItemStack func_77659_a(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
      MovingObjectPosition movingobjectposition = this.func_77621_a(par2World, par3EntityPlayer, true);
      if (movingobjectposition == null) {
         return par1ItemStack;
      } else {
         if (movingobjectposition.field_72313_a == MovingObjectType.BLOCK) {
            int i = movingobjectposition.field_72311_b;
            int j = movingobjectposition.field_72312_c;
            int k = movingobjectposition.field_72309_d;
            if (!par2World.func_72962_a(par3EntityPlayer, i, j, k)) {
               return par1ItemStack;
            }

            if (!par3EntityPlayer.func_82247_a(i, j, k, movingobjectposition.field_72310_e, par1ItemStack)) {
               return par1ItemStack;
            }

            if (par2World.func_147439_a(i, j, k).func_149688_o() == Material.field_151586_h && par2World.func_72805_g(i, j, k) == 0 && par2World.func_147437_c(i, j + 1, k)) {
               par2World.func_147449_b(i, j + 1, k, Witchery.Blocks.LEAPING_LILY);
               if (!par3EntityPlayer.field_71075_bZ.field_75098_d) {
                  --par1ItemStack.field_77994_a;
               }
            }
         }

         return par1ItemStack;
      }
   }

   @SideOnly(Side.CLIENT)
   public int func_82790_a(ItemStack par1ItemStack, int par2) {
      return Blocks.field_150392_bi.func_149741_i(par1ItemStack.func_77960_j());
   }
}
