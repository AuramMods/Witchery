package com.emoniph.witchery.item;

import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemWolfToken extends ItemBase {
   public ItemWolfToken() {
      this.autoGenerateTooltip = true;
      this.func_77625_d(1);
      this.func_77656_e(0);
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack itemstack) {
      return EnumRarity.epic;
   }

   public EnumAction func_77661_b(ItemStack itemstack) {
      return EnumAction.bow;
   }

   public int func_77626_a(ItemStack itemstack) {
      return TimeUtil.secsToTicks(1);
   }

   public void onUsingTick(ItemStack stack, EntityPlayer player, int countdown) {
      if (!player.field_70170_p.field_72995_K && countdown == 1) {
         ExtendedPlayer playerEx;
         int vampLevel;
         if (player.func_70093_af()) {
            playerEx = ExtendedPlayer.get(player);
            vampLevel = playerEx.getVampireLevel() + 1;
            if (vampLevel > 10) {
               vampLevel = 0;
            }

            playerEx.setVampireLevel(vampLevel);
            ChatUtil.sendTranslated(EnumChatFormatting.GREEN, player, "witchery.vampire.setlevel", Integer.valueOf(vampLevel).toString());
            ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_FIZZ, player, 1.5D, 1.5D, 16);
         } else {
            playerEx = ExtendedPlayer.get(player);
            vampLevel = playerEx.getWerewolfLevel() + 1;
            if (vampLevel > 10) {
               vampLevel = 0;
            }

            playerEx.setWerewolfLevel(vampLevel);
            ChatUtil.sendTranslated(EnumChatFormatting.GREEN, player, "witchery.werewolf.setlevel", Integer.valueOf(vampLevel).toString());
            ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_FIZZ, player, 1.5D, 1.5D, 16);
         }
      }

   }

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      player.func_71008_a(stack, this.func_77626_a(stack));
      return stack;
   }
}
