package com.emoniph.witchery.item;

import com.emoniph.witchery.entity.EntityHornedHuntsman;
import com.emoniph.witchery.infusion.infusions.InfusionInfernal;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemHornOfTheHunt extends ItemBase {
   public ItemHornOfTheHunt() {
      this.autoGenerateTooltip = true;
      this.func_77656_e(1);
      this.func_77625_d(1);
   }

   public EnumAction func_77661_b(ItemStack itemstack) {
      return EnumAction.bow;
   }

   public int func_77626_a(ItemStack itemstack) {
      return TimeUtil.secsToTicks(2);
   }

   public void onUsingTick(ItemStack stack, EntityPlayer player, int countdown) {
      if (!player.field_70170_p.field_72995_K && countdown == 1) {
         SoundEffect.WITCHERY_RANDOM_HORN.playAtPlayer(player.field_70170_p, player, 1.0F, 1.0F);
         EntityCreature creature = InfusionInfernal.spawnCreature(player.field_70170_p, EntityHornedHuntsman.class, (int)player.field_70165_t, (int)player.field_70163_u, (int)player.field_70161_v, player, 2, 8, ParticleEffect.EXPLODE, SoundEffect.MOB_WITHER_SPAWN);
         if (creature != null) {
            EntityHornedHuntsman huntsman = (EntityHornedHuntsman)creature;
            huntsman.causeExplosiveEntrance();
            huntsman.func_110163_bv();
            huntsman.func_82206_m();
            stack.func_77972_a(2, player);
         }
      }

   }

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      player.func_71008_a(stack, this.func_77626_a(stack));
      return stack;
   }
}
