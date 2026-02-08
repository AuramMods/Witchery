package com.emoniph.witchery.blocks;

import com.emoniph.witchery.entity.EntityWitchProjectile;
import com.emoniph.witchery.util.MultiItemBlock;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockPlantMine extends BlockBase implements IPlantable {
   private static final String[] woodType = new String[]{"rose_webs", "rose_ink", "rose_thorns", "rose_sprouting", "dandelion_webs", "dandelion_ink", "dandelion_thorns", "dandelion_sprouting", "grass_webs", "grass_ink", "grass_thorns", "grass_sprouting"};
   private final float RADIUS = 0.2F;
   public static final int MINE_ROSE_WEBS = 0;
   public static final int MINE_ROSE_INK = 1;
   public static final int MINE_ROSE_THORNS = 2;
   public static final int MINE_ROSE_SPROUTING = 3;
   public static final int MINE_DANDELION_WEBS = 4;
   public static final int MINE_DANDELION_INK = 5;
   public static final int MINE_DANDELION_THORNS = 6;
   public static final int MINE_DANDELION_SPROUTING = 7;
   public static final int MINE_SHRUB_WEBS = 8;
   public static final int MINE_SHRUB_INK = 9;
   public static final int MINE_SHRUB_THORNS = 10;
   public static final int MINE_SHRUB_SPROUTING = 11;

   public BlockPlantMine() {
      super(Material.field_151585_k, BlockPlantMine.ClassItemBlock.class);
      this.func_149675_a(true);
      this.func_149711_c(6.0F);
      this.func_149752_b(1000.0F);
      this.func_149672_a(field_149779_h);
      this.func_149676_a(0.3F, 0.0F, 0.3F, 0.7F, 0.6F, 0.7F);
   }

   public void func_149670_a(World world, int posX, int posY, int posZ, Entity entity) {
      if (!world.field_72995_K) {
         int metadata = world.func_72805_g(posX, posY, posZ);
         int effect = metadata & 3;
         world.func_147468_f(posX, posY, posZ);
         ParticleEffect.MAGIC_CRIT.send(SoundEffect.RANDOM_EXPLODE, world, 0.5D + (double)posX, 0.05D + (double)posY, 0.5D + (double)posZ, 0.5D, 1.0D, 16);
         switch(effect) {
         case 0:
            EntityWitchProjectile.explodeWeb(world, posX, posY, posZ, 1, false);
            break;
         case 1:
            EntityWitchProjectile.explodeInk(world, (double)posX, (double)posY, (double)posZ, entity, AxisAlignedBB.func_72330_a((double)posX, (double)posY, (double)posZ, (double)(posX + 1), (double)(posY + 1), (double)(posZ + 1)), false);
            break;
         case 2:
            EntityWitchProjectile.plantCactus(world, posX, posY, posZ, 4);
            break;
         case 3:
            EntityWitchProjectile.growBranch(posX, posY, posZ, world, 1, 10, AxisAlignedBB.func_72330_a((double)posX, (double)posY, (double)posZ, (double)(posX + 1), (double)(posY + 1), (double)(posZ + 1)));
         }
      }

   }

   public Item func_149650_a(int par1, Random rand, int fortune) {
      return null;
   }

   public int func_149692_a(int metadata) {
      return 0;
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return new ItemStack(this, 1, world.func_72805_g(x, y, z));
   }

   @SideOnly(Side.CLIENT)
   public void func_149666_a(Item item, CreativeTabs creativeTabs, List list) {
      list.add(new ItemStack(item, 1, 0));
      list.add(new ItemStack(item, 1, 1));
      list.add(new ItemStack(item, 1, 2));
      list.add(new ItemStack(item, 1, 3));
      list.add(new ItemStack(item, 1, 4));
      list.add(new ItemStack(item, 1, 5));
      list.add(new ItemStack(item, 1, 6));
      list.add(new ItemStack(item, 1, 7));
      list.add(new ItemStack(item, 1, 8));
      list.add(new ItemStack(item, 1, 9));
      list.add(new ItemStack(item, 1, 10));
      list.add(new ItemStack(item, 1, 11));
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int par1, int metadata) {
      metadata = metadata >>> 2 & 3;
      if (metadata < 0 || metadata >= 4) {
         metadata = 0;
      }

      switch(metadata) {
      case 0:
      default:
         return Blocks.field_150328_O.func_149691_a(0, 0);
      case 1:
         return Blocks.field_150327_N.func_149691_a(0, 0);
      case 2:
         return Blocks.field_150330_I.func_149691_a(0, 0);
      }
   }

   public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
      return super.func_149742_c(par1World, par2, par3, par4) && this.func_149718_j(par1World, par2, par3, par4);
   }

   protected boolean canPlaceBlockOn(Block block) {
      return block == Blocks.field_150349_c || block == Blocks.field_150346_d || block == Blocks.field_150458_ak || block == Blocks.field_150354_m || block == Blocks.field_150391_bh;
   }

   public void func_149695_a(World par1World, int par2, int par3, int par4, Block par5) {
      super.func_149695_a(par1World, par2, par3, par4, par5);
      this.checkFlowerChange(par1World, par2, par3, par4);
   }

   public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
      this.checkFlowerChange(par1World, par2, par3, par4);
   }

   protected final void checkFlowerChange(World par1World, int par2, int par3, int par4) {
      if (!this.func_149718_j(par1World, par2, par3, par4)) {
         this.func_149697_b(par1World, par2, par3, par4, par1World.func_72805_g(par2, par3, par4), 0);
         par1World.func_147465_d(par2, par3, par4, Blocks.field_150350_a, 0, 2);
      }

   }

   public boolean func_149718_j(World par1World, int par2, int par3, int par4) {
      Block soil = par1World.func_147439_a(par2, par3 - 1, par4);
      return (par1World.func_72883_k(par2, par3, par4) >= 8 || par1World.func_72937_j(par2, par3, par4)) && soil != null && (soil.canSustainPlant(par1World, par2, par3 - 1, par4, ForgeDirection.UP, this) || soil == Blocks.field_150354_m || soil == Blocks.field_150391_bh);
   }

   public AxisAlignedBB func_149668_a(World par1World, int par2, int par3, int par4) {
      float f = 0.0625F;
      return AxisAlignedBB.func_72330_a((double)((float)par2 + 0.5F - 0.2F + f), (double)par3, (double)((float)par4 + 0.5F - 0.2F + f), (double)((float)par2 + 0.5F + 0.2F - f), (double)((float)par3 + 0.6F - f), (double)((float)par4 + 0.5F + 0.2F - f));
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB func_149633_g(World par1World, int par2, int par3, int par4) {
      float f = 0.0625F;
      return AxisAlignedBB.func_72330_a((double)((float)par2 + 0.5F - 0.2F + f), (double)par3, (double)((float)par4 + 0.5F - 0.2F + f), (double)((float)par2 + 0.5F + 0.2F - f), (double)((float)par3 + 0.6F - f), (double)((float)par4 + 0.5F + 0.2F - f));
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public int func_149645_b() {
      return 1;
   }

   public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
      return EnumPlantType.Plains;
   }

   public Block getPlant(IBlockAccess world, int x, int y, int z) {
      return this;
   }

   public int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
      return world.func_72805_g(x, y, z);
   }

   public static class ClassItemBlock extends MultiItemBlock {
      public ClassItemBlock(Block block) {
         super(block);
      }

      protected String[] getNames() {
         return BlockPlantMine.woodType;
      }

      @SideOnly(Side.CLIENT)
      public IIcon func_77617_a(int par1) {
         return this.field_150939_a.func_149691_a(0, par1);
      }
   }
}
