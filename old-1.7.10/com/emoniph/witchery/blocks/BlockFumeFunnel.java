package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.ParticleEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFumeFunnel extends BlockBaseContainer {
   private final boolean filtered;

   public BlockFumeFunnel(boolean filtered) {
      super(Material.field_151573_f, BlockFumeFunnel.TileEntityFumeFunnel.class);
      this.registerTileEntity = !filtered;
      this.filtered = filtered;
      this.func_149711_c(3.5F);
      this.func_149672_a(field_149777_j);
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
   }

   public boolean isFiltered() {
      return this.filtered;
   }

   public boolean func_149727_a(World world, int posX, int posY, int posZ, EntityPlayer player, int par6, float par7, float par8, float par9) {
      if (world.field_72995_K) {
         return true;
      } else {
         int meta = world.func_72805_g(posX, posY, posZ);
         switch(meta) {
         case 2:
         case 3:
            if (BlockWitchesOven.isOven(world.func_147439_a(posX + 1, posY, posZ))) {
               ++posX;
            } else if (BlockWitchesOven.isOven(world.func_147439_a(posX - 1, posY, posZ))) {
               --posX;
            } else if (BlockWitchesOven.isOven(world.func_147439_a(posX, posY - 1, posZ))) {
               --posY;
            }
            break;
         case 4:
         case 5:
            if (BlockWitchesOven.isOven(world.func_147439_a(posX, posY, posZ + 1))) {
               ++posZ;
            } else if (BlockWitchesOven.isOven(world.func_147439_a(posX, posY, posZ - 1))) {
               --posZ;
            } else if (BlockWitchesOven.isOven(world.func_147439_a(posX, posY - 1, posZ))) {
               --posY;
            }
         }

         if (world.func_147438_o(posX, posY, posZ) instanceof BlockWitchesOven.TileEntityWitchesOven) {
            player.openGui(Witchery.instance, 2, world, posX, posY, posZ);
         }

         return true;
      }
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public int func_149745_a(Random rand) {
      return 1;
   }

   public void func_149689_a(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
      int l = MathHelper.func_76128_c((double)(par5EntityLivingBase.field_70177_z * 4.0F / 360.0F) + 0.5D) & 3;
      if (l == 0) {
         par1World.func_72921_c(par2, par3, par4, 2, 2);
      }

      if (l == 1) {
         par1World.func_72921_c(par2, par3, par4, 5, 2);
      }

      if (l == 2) {
         par1World.func_72921_c(par2, par3, par4, 3, 2);
      }

      if (l == 3) {
         par1World.func_72921_c(par2, par3, par4, 4, 2);
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      int metadata = world.func_72805_g(x, y, z);
      if (metadata == 1) {
         double d0 = (double)((float)x + 0.45F);
         double d1 = (double)((float)y + 0.4F);
         double d2 = (double)((float)z + 0.5F);
         world.func_72869_a(ParticleEffect.SMOKE.toString(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }

   }

   public static class TileEntityFumeFunnel extends TileEntity {
      public boolean canUpdate() {
         return false;
      }
   }
}
