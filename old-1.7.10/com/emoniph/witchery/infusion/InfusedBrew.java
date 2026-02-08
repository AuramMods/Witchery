package com.emoniph.witchery.infusion;

import com.emoniph.witchery.item.ItemGeneral;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class InfusedBrew extends ItemGeneral.Drinkable {
   private final InfusedBrewEffect effect;

   public InfusedBrew(int damageValue, String unlocalizedName, InfusedBrewEffect effect) {
      super(damageValue, unlocalizedName, 2);
      this.effect = effect;
      this.potion = true;
   }

   public void onDrunk(World world, EntityPlayer player, ItemStack itemstack) {
      this.effect.drunk(world, player, itemstack);
   }

   public boolean isInfused() {
      return true;
   }
}
