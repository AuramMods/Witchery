package com.emoniph.witchery.blocks;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.IIcon;

public abstract class BlockBaseRotatedPillar extends BlockRotatedPillar {
   protected boolean registerBlockName = true;
   protected boolean registerWithCreateTab = true;
   protected final Class<? extends ItemBlock> clazzItem;

   public BlockBaseRotatedPillar(Material material, Class<? extends ItemBlock> clazzItem) {
      super(material);
      this.clazzItem = clazzItem;
   }

   public Block func_149663_c(String blockName) {
      if (this.registerWithCreateTab) {
         this.func_149647_a(WitcheryCreativeTab.INSTANCE);
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

   protected abstract IIcon func_150163_b(int var1);
}
