package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.client.particle.NaturePowerFX;
import com.emoniph.witchery.util.ParticleEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCircleGlyph extends BlockBase {
   private int color;
   private boolean charged;
   @SideOnly(Side.CLIENT)
   private IIcon[] icons;

   public BlockCircleGlyph(int color, boolean charged) {
      super(Material.field_151582_l);
      super.registerWithCreateTab = false;
      this.color = color;
      this.charged = charged;
      this.func_149752_b(1000.0F);
      this.func_149711_c(2.0F);
      float f = 0.5F;
      float f1 = 0.015625F;
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 0.015625F, 1.0F);
   }

   public void func_149699_a(World world, int posX, int posY, int posZ, EntityPlayer player) {
      if (!world.field_72995_K) {
         ItemStack itemstack = player.func_70694_bm();
         if (itemstack != null && (Witchery.Items.GENERIC.itemBroom.isMatch(itemstack) || Witchery.Items.GENERIC.itemBroomEnchanted.isMatch(itemstack))) {
            world.func_147480_a(posX, posY, posZ, false);
         }
      }

   }

   public void func_149636_a(World world, EntityPlayer player, int posX, int posY, int posZ, int meta) {
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegistrar) {
      this.icons = new IIcon[12];

      for(int glyph = 0; glyph < this.icons.length; ++glyph) {
         this.icons[glyph] = iconRegistrar.func_94245_a(this.func_149641_N() + String.format("%d.%d", this.color + 1, glyph + 1));
      }

   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int face, int metadata) {
      return this.icons[MathHelper.func_76125_a(metadata, 0, 12)];
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public int func_149745_a(Random rand) {
      return 0;
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      double d0;
      double d1;
      double d2;
      if (this.color > 0) {
         d0 = (double)((float)x + 0.4F + rand.nextFloat() * 0.2F);
         d1 = (double)((float)y + 0.1F + rand.nextFloat() * 0.3F);
         d2 = (double)((float)z + 0.4F + rand.nextFloat() * 0.2F);
         world.func_72869_a(this.color == 2 ? ParticleEffect.FLAME.toString() : ParticleEffect.PORTAL.toString(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
      } else if (this.charged) {
         d0 = (double)((float)x + 0.3F + rand.nextFloat() * 0.4F);
         d1 = (double)((float)y + 0.1F + rand.nextFloat() * 0.3F);
         d2 = (double)((float)z + 0.3F + rand.nextFloat() * 0.4F);
         NaturePowerFX sparkle = new NaturePowerFX(world, d0, d1, d2);
         sparkle.setScale(0.6F);
         sparkle.setGravity(0.01F);
         sparkle.setCanMove(true);
         double maxSpeed = 0.01D;
         double doubleSpeed = 0.02D;
         sparkle.func_70016_h(rand.nextDouble() * 0.02D - 0.01D, rand.nextDouble() * 0.02D + 0.01D, rand.nextDouble() * 0.02D - 0.01D);
         sparkle.setMaxAge(10 + rand.nextInt(5));
         float maxColorShift = 0.2F;
         float doubleColorShift = maxColorShift * 2.0F;
         float colorshiftR = rand.nextFloat() * doubleColorShift - maxColorShift;
         float colorshiftG = rand.nextFloat() * doubleColorShift - maxColorShift;
         float colorshiftB = rand.nextFloat() * doubleColorShift - maxColorShift;
         float red = 1.0F;
         float green = 0.8F;
         float blue = 0.2F;
         sparkle.func_70538_b(red + colorshiftR, green + colorshiftG, blue + colorshiftB);
         sparkle.func_82338_g(0.1F);
         Minecraft.func_71410_x().field_71452_i.func_78873_a(sparkle);
      }

   }

   public void func_149695_a(World world, int x, int y, int z, Block block) {
      this.func_111046_k(world, x, y, z);
   }

   private boolean func_111046_k(World world, int x, int y, int z) {
      if (!this.func_149718_j(world, x, y, z)) {
         world.func_147468_f(x, y, z);
         return false;
      } else {
         return true;
      }
   }

   public boolean func_149718_j(World world, int x, int y, int z) {
      Material materialBelow = world.func_147439_a(x, y - 1, z).func_149688_o();
      return !world.func_147437_c(x, y - 1, z) && materialBelow != null && materialBelow.func_76218_k() && materialBelow.func_76220_a();
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess blockAccess, int x, int y, int z, int side) {
      return side == 1;
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      Block block = world.func_147439_a(x, y, z);
      if (block == Witchery.Blocks.GLYPH_INFERNAL) {
         return new ItemStack(Witchery.Items.CHALK_INFERNAL);
      } else {
         return block == Witchery.Blocks.GLYPH_OTHERWHERE ? new ItemStack(Witchery.Items.CHALK_OTHERWHERE) : new ItemStack(Witchery.Items.CHALK_RITUAL);
      }
   }
}
