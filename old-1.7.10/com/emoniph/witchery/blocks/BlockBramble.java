package com.emoniph.witchery.blocks;

import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.util.MultiItemBlock;
import com.emoniph.witchery.util.ParticleEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockBramble extends BlockBase {
   private static final String[] BRAMBLE_TYPES = new String[]{"ender", "wild"};
   @SideOnly(Side.CLIENT)
   private IIcon[] iconArray;

   public BlockBramble() {
      super(Material.field_151585_k, BlockBramble.ClassItemBlock.class);
      this.func_149711_c(20.0F);
      this.func_149752_b(1000.0F);
      float f = 0.45F;
      this.func_149676_a(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
   }

   public int func_149645_b() {
      return 6;
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public AxisAlignedBB func_149668_a(World par1World, int par2, int par3, int par4) {
      return null;
   }

   public void func_149670_a(World world, int posX, int posY, int posZ, Entity entity) {
      int meta = world.func_72805_g(posX, posY, posZ);
      switch(meta) {
      case 0:
         if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)entity;
            this.teleportAway(world, posX, posY, posZ, living);
         }
         break;
      case 1:
         entity.func_70097_a(DamageSource.field_76367_g, 1.0F);
      }

   }

   public void teleportAway(World world, int posX, int posY, int posZ, EntityLivingBase entity) {
      if (!world.field_72995_K) {
         int distance = true;
         int doubleDistance = true;
         posX += world.field_73012_v.nextInt(1000) - 500;
         posZ += world.field_73012_v.nextInt(1000) - 500;

         int maxY;
         for(maxY = Math.min(posY + 64, 250); !world.func_147439_a(posX, posY, posZ).func_149688_o().func_76220_a() && posY >= 0; --posY) {
         }

         while((!world.func_147439_a(posX, posY, posZ).func_149688_o().func_76220_a() || world.func_147439_a(posX, posY, posZ) == Blocks.field_150357_h || !world.func_147437_c(posX, posY + 1, posZ) || !world.func_147437_c(posX, posY + 2, posZ) || !world.func_147437_c(posX, posY + 3, posZ)) && posY < maxY) {
            ++posY;
         }

         if (posY > 0 && posY < maxY) {
            ItemGeneral.teleportToLocation(world, 0.5D + (double)posX, 1.0D + (double)posY, 0.5D + (double)posZ, world.field_73011_w.field_76574_g, entity, true);
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int par1, int par2) {
      if (par2 < 0 || par2 >= this.iconArray.length) {
         par2 = 0;
      }

      return this.iconArray[par2];
   }

   public int func_149692_a(int par1) {
      return par1;
   }

   @SideOnly(Side.CLIENT)
   public void func_149666_a(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
      for(int j = 0; j < BRAMBLE_TYPES.length; ++j) {
         par3List.add(new ItemStack(par1, 1, j));
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister par1IconRegister) {
      this.iconArray = new IIcon[BRAMBLE_TYPES.length];

      for(int i = 0; i < this.iconArray.length; ++i) {
         this.iconArray[i] = par1IconRegister.func_94245_a(this.func_149641_N() + "_" + BRAMBLE_TYPES[i]);
      }

   }

   public void func_149636_a(World world, EntityPlayer player, int x, int y, int z, int meta) {
      super.func_149636_a(world, player, x, y, z, meta);
      if (!world.field_72995_K && meta == 1 && (player.func_70694_bm() == null || player.func_70694_bm().func_77973_b() != Items.field_151006_E)) {
         spreadToIfEmpty(world, x + 1, y, z, this, meta);
         spreadToIfEmpty(world, x, y, z + 1, this, meta);
         spreadToIfEmpty(world, x - 1, y, z, this, meta);
         spreadToIfEmpty(world, x, y, z - 1, this, meta);
         spreadToIfEmpty(world, x + 1, y, z + 1, this, meta);
         spreadToIfEmpty(world, x - 1, y, z - 1, this, meta);
         spreadToIfEmpty(world, x - 1, y, z + 1, this, meta);
         spreadToIfEmpty(world, x + 1, y, z - 1, this, meta);
      }

   }

   private static void spreadToIfEmpty(World world, int x, int y0, int z, Block newBlock, int newBlockMeta) {
      if (!world.field_72995_K) {
         for(int y = y0 - 1; y <= y0 + 1; ++y) {
            Block block = world.func_147439_a(x, y, z);
            if (block == Blocks.field_150433_aE || block == Blocks.field_150329_H || block == Blocks.field_150350_a) {
               Block belowBlock = world.func_147439_a(x, y - 1, z);
               if (belowBlock != Blocks.field_150350_a && world.field_73012_v.nextInt(2) == 0) {
                  world.func_147465_d(x, y, z, newBlock, newBlockMeta, 3);
                  if (world.field_73012_v.nextInt(3) != 0) {
                     break;
                  }
               }
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      int meta = world.func_72805_g(x, y, z);
      if (meta == 0 && rand.nextInt(2) == 0) {
         double d0 = (double)((float)x + rand.nextFloat());
         double d1 = (double)((float)y + 0.15F + rand.nextFloat() * 0.3F) + 0.5D;
         double d2 = (double)((float)z + rand.nextFloat());
         world.func_72869_a(ParticleEffect.PORTAL.toString(), d0, d1, d2, 0.0D, -1.2D, 0.0D);
      }

   }

   public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
      return !par1World.func_147437_c(par2, par3 - 1, par4);
   }

   public void func_149695_a(World par1World, int par2, int par3, int par4, Block par5) {
      this.checkBlockCoordValid(par1World, par2, par3, par4);
   }

   protected final void checkBlockCoordValid(World par1World, int par2, int par3, int par4) {
      if (!this.func_149718_j(par1World, par2, par3, par4)) {
         this.func_149697_b(par1World, par2, par3, par4, par1World.func_72805_g(par2, par3, par4), 0);
         par1World.func_147468_f(par2, par3, par4);
      }

   }

   public boolean func_149718_j(World par1World, int par2, int par3, int par4) {
      return this.func_149742_c(par1World, par2, par3, par4);
   }

   public static class ClassItemBlock extends MultiItemBlock {
      public ClassItemBlock(Block block) {
         super(block);
      }

      protected String[] getNames() {
         return BlockBramble.BRAMBLE_TYPES;
      }

      @SideOnly(Side.CLIENT)
      public IIcon func_77617_a(int meta) {
         return this.field_150939_a.func_149691_a(0, meta);
      }
   }
}
