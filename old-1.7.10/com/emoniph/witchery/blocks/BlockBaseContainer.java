package com.emoniph.witchery.blocks;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class BlockBaseContainer extends BlockContainer {
   protected boolean registerBlockName;
   protected boolean registerTileEntity;
   protected boolean registerWithCreateTab;
   protected final Class<? extends TileEntity> clazzTile;
   protected final Class<? extends ItemBlock> clazzItem;

   public BlockBaseContainer(Material material, Class<? extends TileEntity> clazzTile) {
      this(material, clazzTile, (Class)null);
   }

   public BlockBaseContainer(Material material, Class<? extends TileEntity> clazzTile, Class<? extends ItemBlock> clazzItem) {
      super(material);
      this.registerBlockName = true;
      this.registerTileEntity = true;
      this.registerWithCreateTab = true;
      this.clazzTile = clazzTile;
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

      if (this.registerTileEntity) {
         GameRegistry.registerTileEntity(this.clazzTile, blockName);
      }

      return super.func_149663_c(blockName);
   }

   public TileEntity func_149915_a(World world, int metadata) {
      try {
         return (TileEntity)this.clazzTile.newInstance();
      } catch (Throwable var4) {
         return null;
      }
   }
}
