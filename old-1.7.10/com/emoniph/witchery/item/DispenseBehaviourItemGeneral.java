package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.EntityWitchProjectile;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class DispenseBehaviourItemGeneral implements IBehaviorDispenseItem {
   private final BehaviorDefaultDispenseItem defaultDispenserItemBehavior = new BehaviorDefaultDispenseItem();

   public ItemStack func_82482_a(IBlockSource block, ItemStack stack) {
      return Witchery.Items.GENERIC.isBrew(stack.func_77960_j()) ? (new DispenseBehaviourItemGeneral.DispenserBehaviorBrew(this, stack)).func_82482_a(block, stack) : this.defaultDispenserItemBehavior.func_82482_a(block, stack);
   }

   static class DispenserBehaviorBrew extends BehaviorProjectileDispense {
      final ItemStack potionItemStack;
      final DispenseBehaviourItemGeneral dispenserPotionBehavior;

      DispenserBehaviorBrew(DispenseBehaviourItemGeneral par1DispenserBehaviorPotion, ItemStack par2ItemStack) {
         this.dispenserPotionBehavior = par1DispenserBehaviorPotion;
         this.potionItemStack = par2ItemStack;
      }

      protected IProjectile func_82499_a(World par1World, IPosition par2IPosition) {
         return new EntityWitchProjectile(par1World, par2IPosition.func_82615_a(), par2IPosition.func_82617_b(), par2IPosition.func_82616_c(), (ItemGeneral.SubItem)Witchery.Items.GENERIC.subItems.get(this.potionItemStack.func_77960_j()));
      }

      protected float func_82498_a() {
         return super.func_82498_a() * 0.5F;
      }

      protected float func_82500_b() {
         return super.func_82500_b() * 1.25F;
      }
   }
}
