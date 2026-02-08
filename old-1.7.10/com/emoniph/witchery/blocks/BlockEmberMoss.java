package com.emoniph.witchery.blocks;

import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

public class BlockEmberMoss extends BlockBaseBush implements IShearable {
   public BlockEmberMoss() {
      super(Material.field_151585_k);
      this.func_149675_a(true);
      this.func_149711_c(0.0F);
      this.func_149715_a(0.4F);
      this.func_149672_a(field_149779_h);
      float f = 0.4F;
      this.func_149676_a(0.099999994F, 0.0F, 0.099999994F, 0.9F, 0.4F, 0.9F);
   }

   public void func_149670_a(World world, int posX, int posY, int posZ, Entity entity) {
      if (!world.field_72995_K && entity instanceof EntityLivingBase && !entity.func_70027_ad() && !entity.func_70045_F() && (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).field_71075_bZ.field_75098_d)) {
         entity.func_70015_d(3);
         ParticleEffect.FLAME.send(SoundEffect.MOB_GHAST_FIREBALL, world, 0.5D + (double)posX, 0.05D + (double)posY, 0.5D + (double)posZ, 0.5D, 1.0D, 16);
      }

   }

   public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
      if (!par1World.field_72995_K && par1World.field_73012_v.nextInt(6) == 0) {
         byte b0 = 4;
         int l = 5;

         int i1;
         int j1;
         int k1;
         for(i1 = par2 - b0; i1 <= par2 + b0; ++i1) {
            for(j1 = par4 - b0; j1 <= par4 + b0; ++j1) {
               for(k1 = par3 - 1; k1 <= par3 + 1; ++k1) {
                  if (par1World.func_147439_a(i1, k1, j1) == this) {
                     --l;
                     if (l <= 0) {
                        return;
                     }
                  }
               }
            }
         }

         i1 = par2 + par5Random.nextInt(3) - 1;
         j1 = par3 + par5Random.nextInt(2) - par5Random.nextInt(2);
         k1 = par4 + par5Random.nextInt(3) - 1;

         for(int l1 = 0; l1 < 4; ++l1) {
            if (par1World.func_147437_c(i1, j1, k1) && this.canBlockSpread(par1World, i1, j1, k1)) {
               par2 = i1;
               par3 = j1;
               par4 = k1;
            }

            i1 = par2 + par5Random.nextInt(3) - 1;
            j1 = par3 + par5Random.nextInt(2) - par5Random.nextInt(2);
            k1 = par4 + par5Random.nextInt(3) - 1;
         }

         if (par1World.func_147437_c(i1, j1, k1) && this.canBlockSpread(par1World, i1, j1, k1)) {
            par1World.func_147465_d(i1, j1, k1, this, 0, 2);
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      if (rand.nextInt(100) == 0) {
         double d0 = (double)((float)x + 0.2F + rand.nextFloat() * 0.8F);
         double d1 = (double)((float)y + 0.15F + rand.nextFloat() * 0.3F);
         double d2 = (double)((float)z + 0.2F + rand.nextFloat() * 0.8F);
         world.func_72869_a(ParticleEffect.FLAME.toString(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }

   }

   public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
      return super.func_149742_c(par1World, par2, par3, par4) && this.func_149718_j(par1World, par2, par3, par4);
   }

   protected boolean func_149854_a(Block block) {
      return block != null && block.func_149662_c();
   }

   public boolean canBlockSpread(World world, int posX, int posY, int posZ) {
      Block block = world.func_147439_a(posX, posY - 1, posZ);
      return this.func_149718_j(world, posX, posY, posZ) && (block == Blocks.field_150346_d || block == Blocks.field_150349_c || block == Blocks.field_150391_bh || block == Blocks.field_150354_m || block == Blocks.field_150458_ak);
   }

   public boolean func_149718_j(World world, int posX, int posY, int posZ) {
      Material material = world.func_147439_a(posX, posY - 1, posZ).func_149688_o();
      return material != null && material.func_76220_a();
   }

   public Item func_149650_a(int par1, Random rand, int fortune) {
      return null;
   }

   public int func_149745_a(Random par1Random) {
      return 0;
   }

   public void func_149636_a(World par3World, EntityPlayer player, int par4, int par5, int par6, int damageValue) {
      super.func_149636_a(par3World, player, par4, par5, par6, damageValue);
   }

   public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z) {
      return true;
   }

   public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
      ArrayList<ItemStack> ret = new ArrayList();
      ret.add(new ItemStack(this, 1, 0));
      return ret;
   }
}
