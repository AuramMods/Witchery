package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockAlluringSkull;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemAlluringSkull extends ItemBlock {
   @SideOnly(Side.CLIENT)
   private IIcon[] field_94586_c;
   public static final String[] field_94587_a = new String[]{"witchery:alluringSkull_off", "witchery:alluringSkull_on"};

   public ItemAlluringSkull(Block par1) {
      super(par1);
      this.func_77656_e(0);
      this.func_77625_d(1);
   }

   public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
      if (par7 == 0) {
         return false;
      } else if (!par3World.func_147439_a(par4, par5, par6).func_149688_o().func_76220_a()) {
         return false;
      } else {
         if (par7 == 1) {
            ++par5;
         }

         if (par7 == 2) {
            --par6;
         }

         if (par7 == 3) {
            ++par6;
         }

         if (par7 == 4) {
            --par4;
         }

         if (par7 == 5) {
            ++par4;
         }

         if (!par2EntityPlayer.func_82247_a(par4, par5, par6, par7, par1ItemStack)) {
            return false;
         } else if (!Witchery.Blocks.ALLURING_SKULL.func_149742_c(par3World, par4, par5, par6)) {
            return false;
         } else {
            if (!par3World.field_72995_K) {
               par3World.func_147465_d(par4, par5, par6, Witchery.Blocks.ALLURING_SKULL, par7, 2);
               int i1 = 0;
               if (par7 == 1) {
                  i1 = MathHelper.func_76128_c((double)(par2EntityPlayer.field_70177_z * 16.0F / 360.0F) + 0.5D) & 15;
               }

               TileEntity tileentity = par3World.func_147438_o(par4, par5, par6);
               if (tileentity != null && tileentity instanceof BlockAlluringSkull.TileEntityAlluringSkull) {
                  ((BlockAlluringSkull.TileEntityAlluringSkull)tileentity).setSkullType(par1ItemStack.func_77960_j());
                  ((BlockAlluringSkull.TileEntityAlluringSkull)tileentity).setSkullRotation(i1);
               }
            }

            --par1ItemStack.field_77994_a;
            return true;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_94581_a(IIconRegister par1IconRegister) {
      this.field_94586_c = new IIcon[field_94587_a.length];

      for(int i = 0; i < field_94587_a.length; ++i) {
         this.field_94586_c[i] = par1IconRegister.func_94245_a(field_94587_a[i]);
      }

   }

   public IIcon func_77617_a(int par1) {
      if (par1 < 0 || par1 >= field_94587_a.length) {
         par1 = 0;
      }

      return this.field_94586_c[par1];
   }

   public String func_77667_c(ItemStack par1ItemStack) {
      return super.func_77658_a();
   }
}
