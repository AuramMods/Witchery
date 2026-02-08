package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.common.Shapeshift;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import com.emoniph.witchery.util.TransformCreature;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class ItemMoonCharm extends ItemBase {
   public ItemMoonCharm() {
      this.autoGenerateTooltip = true;
      this.func_77625_d(1);
      this.func_77656_e(49);
   }

   public boolean func_82789_a(ItemStack item, ItemStack otherMaterial) {
      return otherMaterial.func_77969_a(new ItemStack(Items.field_151043_k));
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack itemstack) {
      return EnumRarity.rare;
   }

   public EnumAction func_77661_b(ItemStack itemstack) {
      return EnumAction.bow;
   }

   public int func_77626_a(ItemStack itemstack) {
      return TimeUtil.secsToTicks(3);
   }

   public void onUsingTick(ItemStack stack, EntityPlayer player, int countdown) {
      if (!player.field_70170_p.field_72995_K) {
         ExtendedPlayer playerEx = ExtendedPlayer.get(player);
         int level = playerEx.getWerewolfLevel();
         if (countdown == Math.max((level - 1) * 4, 1)) {
            if (!isWolfsbaneActive(player, playerEx) && Shapeshift.INSTANCE.canControlTransform(playerEx)) {
               switch(playerEx.getCreatureType()) {
               case NONE:
                  if (player.func_70093_af() && Shapeshift.INSTANCE.isWolfmanAllowed(playerEx)) {
                     Shapeshift.INSTANCE.shiftTo(player, TransformCreature.WOLFMAN);
                  } else {
                     Shapeshift.INSTANCE.shiftTo(player, TransformCreature.WOLF);
                  }

                  ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_FIZZ, player, 1.5D, 1.5D, 16);
                  break;
               case WOLF:
                  if (player.func_70093_af() && Shapeshift.INSTANCE.isWolfmanAllowed(playerEx)) {
                     Shapeshift.INSTANCE.shiftTo(player, TransformCreature.WOLFMAN);
                  } else {
                     Shapeshift.INSTANCE.shiftTo(player, TransformCreature.NONE);
                  }

                  ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_FIZZ, player, 1.5D, 1.5D, 16);
                  break;
               case WOLFMAN:
                  if (player.func_70093_af()) {
                     Shapeshift.INSTANCE.shiftTo(player, TransformCreature.NONE);
                  } else {
                     Shapeshift.INSTANCE.shiftTo(player, TransformCreature.WOLF);
                  }

                  ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_FIZZ, player, 1.5D, 1.5D, 16);
                  break;
               default:
                  ParticleEffect.SMOKE.send(SoundEffect.NOTE_SNARE, player, 0.5D, 0.5D, 8);
               }
            } else {
               ParticleEffect.SMOKE.send(SoundEffect.NOTE_PLING, player, 0.5D, 0.5D, 8);
            }

            stack.func_77972_a(1, player);
         }
      }

   }

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      player.func_71008_a(stack, this.func_77626_a(stack));
      return stack;
   }

   public static boolean isWolfsbaneActive(EntityPlayer player, ExtendedPlayer playerEx) {
      PotionEffect potion = player.func_70660_b(Witchery.Potions.WOLFSBANE);
      if (potion == null) {
         return false;
      } else {
         int amplifier = 1 + Math.max(0, potion.func_76458_c() * 3 - 1);
         return amplifier >= playerEx.getWerewolfLevel();
      }
   }
}
