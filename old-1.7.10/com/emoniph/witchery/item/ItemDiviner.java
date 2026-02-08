package com.emoniph.witchery.item;

import com.emoniph.witchery.infusion.infusions.InfusionOtherwhere;
import com.emoniph.witchery.util.BlockSide;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class ItemDiviner extends ItemBase {
   private static final int MAX_DAMAGE = 50;
   private static final int DAMAGE_PER_USE = 1;
   private final Block blockToDetect;

   public ItemDiviner(Block blockToDetect) {
      this.blockToDetect = blockToDetect;
      this.func_77625_d(1);
      this.func_77656_e(50);
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack itemstack) {
      return EnumRarity.uncommon;
   }

   public EnumAction func_77661_b(ItemStack itemstack) {
      return EnumAction.bow;
   }

   public int func_77626_a(ItemStack itemstack) {
      return 400;
   }

   public void onUsingTick(ItemStack itemstack, EntityPlayer player, int countdown) {
      World world = player.field_70170_p;
      if (!world.field_72995_K) {
         int elapsedTicks = this.func_77626_a(itemstack) - countdown;
         MovingObjectPosition mop = InfusionOtherwhere.doCustomRayTrace(world, player, true, 6.0D);
         if (mop == null || mop.field_72313_a != MovingObjectType.BLOCK || !BlockSide.TOP.isEqual(mop.field_72310_e)) {
            SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            player.func_71041_bz();
            return;
         }

         int posX = MathHelper.func_76128_c((double)mop.field_72311_b);
         int posY = MathHelper.func_76128_c((double)mop.field_72312_c) - elapsedTicks;
         int posZ = MathHelper.func_76128_c((double)mop.field_72309_d);
         Block block = world.func_147439_a(posX, posY, posZ);
         boolean foundBlock = false;
         boolean foundSomething = false;
         if (block == this.blockToDetect) {
            foundBlock = true;
            foundSomething = true;
         } else if (block == Blocks.field_150357_h) {
            foundBlock = false;
            foundSomething = true;
         }

         if (foundSomething || posY <= 1) {
            if (foundBlock) {
               ParticleEffect.MAGIC_CRIT.send(SoundEffect.RANDOM_ORB, world, 0.5D + (double)mop.field_72311_b, (double)(mop.field_72312_c + 1), 0.5D + (double)mop.field_72309_d, 0.5D, 0.5D, 8);
            } else {
               ParticleEffect.SMOKE.send(SoundEffect.NOTE_SNARE, world, 0.5D + (double)mop.field_72311_b, (double)(mop.field_72312_c + 1), 0.5D + (double)mop.field_72309_d, 0.5D, 0.5D, 8);
            }

            player.func_71041_bz();
            itemstack.func_77972_a(1, player);
         }
      }

   }

   public ItemStack func_77659_a(ItemStack itemstack, World world, EntityPlayer player) {
      player.func_71008_a(itemstack, this.func_77626_a(itemstack));
      return itemstack;
   }
}
