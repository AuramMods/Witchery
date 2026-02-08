package com.emoniph.witchery.blocks;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.InvUtil;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;

public class BlockRefillingChest extends BlockChest {
   public BlockRefillingChest() {
      super(0);
      this.func_149752_b(9999.0F);
      this.func_149722_s();
   }

   public Block func_149663_c(String blockName) {
      this.func_149647_a(WitcheryCreativeTab.INSTANCE);
      BlockUtil.registerBlock(this, blockName);
      GameRegistry.registerTileEntity(BlockRefillingChest.TileEntityRefillingChest.class, blockName);
      return super.func_149663_c(blockName);
   }

   public TileEntity func_149915_a(World world, int metadata) {
      return new BlockRefillingChest.TileEntityRefillingChest();
   }

   public static class TileEntityRefillingChest extends TileEntityChest {
      protected long ticks = 0L;
      private static final int MAX_ITEMS_FOR_REFILL = 0;

      public void func_145845_h() {
         super.func_145845_h();
         if (this.ticks == 0L) {
            this.initiate();
         } else if (this.ticks >= Long.MAX_VALUE) {
            this.ticks = 1L;
         }

         ++this.ticks;
         this.doUpdate();
      }

      protected void initiate() {
         this.doUpdate();
      }

      protected void doUpdate() {
         if (!this.field_145850_b.field_72995_K && this.field_145850_b.field_73011_w.field_76574_g == Config.instance().dimensionTormentID && TimeUtil.secondsElapsed(3600, this.ticks) && InvUtil.getItemStackCount(this) <= 0) {
            int numItems = 2 + this.field_145850_b.field_73012_v.nextInt(4);
            ChestGenHooks gen = ChestGenHooks.getInfo("dungeonChest");
            WeightedRandomChestContent.func_76293_a(this.field_145850_b.field_73012_v, gen.getItems(this.field_145850_b.field_73012_v), this, numItems);
         }

      }

      public void func_145841_b(NBTTagCompound nbtChest) {
         super.func_145841_b(nbtChest);
         nbtChest.func_74772_a("WITCLifeTicks", this.ticks);
      }

      public void func_145839_a(NBTTagCompound nbtChest) {
         super.func_145839_a(nbtChest);
         this.ticks = nbtChest.func_74763_f("WITCLifeTicks");
      }
   }
}
