package com.emoniph.witchery.item;

import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.blocks.BlockWitchCrop;
import com.emoniph.witchery.util.ItemUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

public class ItemWitchSeeds extends ItemSeeds {
   private final boolean waterPlant;

   public ItemWitchSeeds(BlockWitchCrop plantedBlock, ItemStack cropItemStack, Block soilBlock, boolean waterPlant) {
      super(plantedBlock, soilBlock);
      this.waterPlant = waterPlant;
      this.func_77656_e(0);
      this.func_77625_d(64);
      this.func_77637_a(WitcheryCreativeTab.INSTANCE);
      plantedBlock.setSeedItem(new ItemStack(this));
      plantedBlock.setCropItem(cropItemStack);
   }

   public Item func_77655_b(String itemName) {
      ItemUtil.registerItem(this, itemName);
      return super.func_77655_b(itemName);
   }

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      if (this.waterPlant) {
         MovingObjectPosition mop = this.func_77621_a(world, player, true);
         if (mop != null && mop.field_72313_a == MovingObjectType.BLOCK && mop.field_72310_e == 1) {
            float f = (float)mop.field_72307_f.field_72450_a - (float)mop.field_72311_b;
            float f1 = (float)mop.field_72307_f.field_72448_b - (float)mop.field_72312_c;
            float f2 = (float)mop.field_72307_f.field_72449_c - (float)mop.field_72309_d;
            stack.func_77943_a(player, world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, mop.field_72310_e, f, f1, f2);
         }
      }

      return super.func_77659_a(stack, world, player);
   }

   public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
      return this.waterPlant ? EnumPlantType.Water : super.getPlantType(world, x, y, z);
   }
}
