package com.emoniph.witchery.brewing.potions;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

public class PotionFortune extends PotionBase implements IHandleHarvestDrops {
   public PotionFortune(int id, int color) {
      super(id, color);
   }

   public void onHarvestDrops(World world, EntityPlayer player, HarvestDropsEvent event, int amplifier) {
      if (!event.world.field_72995_K && !event.isSilkTouching && event.block != null && !event.block.hasTileEntity(event.blockMetadata) && event.drops.size() > 0) {
         ArrayList<ItemStack> drops = event.block.getDrops(event.world, event.x, event.y, event.z, event.blockMetadata, event.fortuneLevel + (amplifier > 2 ? 2 : 1));
         event.drops.clear();
         event.drops.addAll(drops);
      }

   }
}
