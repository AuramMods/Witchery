package com.emoniph.witchery.blocks;

import com.emoniph.witchery.util.MultiItemBlock;
import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockWitchWood extends BlockBase implements IFuelHandler {
   private static final String[] WOOD_TYPES = new String[]{"rowan", "alder", "hawthorn"};
   @SideOnly(Side.CLIENT)
   private IIcon[] iconArray;

   public BlockWitchWood() {
      super(Material.field_151575_d, BlockWitchWood.ClassItemBlock.class);
      this.func_149711_c(2.0F);
      this.func_149672_a(Block.field_149766_f);
      GameRegistry.registerFuelHandler(this);
   }

   public Block func_149663_c(String blockName) {
      super.func_149663_c(blockName);
      Blocks.field_150480_ab.setFireInfo(this, 5, 20);
      return this;
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
   public void func_149666_a(Item item, CreativeTabs creativeTabs, List list) {
      for(int i = 0; i < WOOD_TYPES.length; ++i) {
         list.add(new ItemStack(item, 1, i));
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister par1IconRegister) {
      this.iconArray = new IIcon[WOOD_TYPES.length];

      for(int i = 0; i < this.iconArray.length; ++i) {
         this.iconArray[i] = par1IconRegister.func_94245_a(this.func_149641_N() + "_" + WOOD_TYPES[i]);
      }

   }

   public int getBurnTime(ItemStack fuel) {
      return Item.func_150898_a(this) == fuel.func_77973_b() ? 300 : 0;
   }

   public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
      return world.func_72805_g(x, y, z) == 2 ? 1 : super.getFlammability(world, x, y, z, face);
   }

   public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
      return world.func_72805_g(x, y, z) == 2 ? 1 : super.getFireSpreadSpeed(world, x, y, z, face);
   }

   public static class ClassItemBlock extends MultiItemBlock {
      public ClassItemBlock(Block block) {
         super(block);
      }

      protected String[] getNames() {
         return BlockWitchWood.WOOD_TYPES;
      }
   }
}
