package com.emoniph.witchery.blocks;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;

public abstract class BlockBaseBush extends BlockBush {
   protected boolean registerBlockName;
   protected boolean registerWithCreateTab;
   protected final Class<? extends ItemBlock> clazzItem;

   public BlockBaseBush(Material material) {
      this(material, (Class)null);
   }

   public BlockBaseBush(Material material, Class<? extends ItemBlock> clazzItem) {
      super(material);
      this.registerBlockName = true;
      this.registerWithCreateTab = true;
      this.clazzItem = clazzItem;
   }

   public Block func_149663_c(String blockName) {
      if (this.registerWithCreateTab) {
         this.func_149647_a(WitcheryCreativeTab.INSTANCE);
      } else {
         this.func_149647_a((CreativeTabs)null);
      }

      if (this.registerBlockName) {
         if (this.clazzItem == null) {
            BlockUtil.registerBlock(this, blockName);
         } else {
            BlockUtil.registerBlock(this, this.clazzItem, blockName);
         }
      }

      return super.func_149663_c(blockName);
   }
}
