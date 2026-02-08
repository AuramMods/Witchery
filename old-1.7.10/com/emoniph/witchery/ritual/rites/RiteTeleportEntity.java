package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockAreaMarker;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.brewing.potions.PotionEnderInhibition;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.item.ItemHunterClothes;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class RiteTeleportEntity extends RiteTeleportation {
   public RiteTeleportEntity(int radius) {
      super(radius);
   }

   protected boolean teleport(World world, int posX, int posY, int posZ, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
      if (!world.field_72995_K) {
         ItemStack taglockStack = null;
         Iterator i$ = ritual.sacrificedItems.iterator();

         while(i$.hasNext()) {
            RitualStep.SacrificedItem item = (RitualStep.SacrificedItem)i$.next();
            if (Witchery.Items.TAGLOCK_KIT == item.itemstack.func_77973_b() && item.itemstack.func_77960_j() == 1) {
               taglockStack = item.itemstack;
               break;
            }
         }

         if (taglockStack != null) {
            EntityLivingBase entity = Witchery.Items.TAGLOCK_KIT.getBoundEntity(world, (Entity)null, taglockStack, 1);
            if (entity != null && entity.field_71093_bK != Config.instance().dimensionDreamID && world.field_73011_w.field_76574_g != Config.instance().dimensionDreamID) {
               EntityPlayer player = ritual.getInitiatingPlayer(world);
               boolean isImmune = ItemHunterClothes.isCurseProtectionActive(entity);
               if (!isImmune) {
                  isImmune = BlockAreaMarker.AreaMarkerRegistry.instance().isProtectionActive(entity, this);
               }

               if (!isImmune && !Witchery.Items.POPPET.voodooProtectionActivated(player, (ItemStack)null, entity, true, true) && !PotionEnderInhibition.isActive(entity, 3)) {
                  ItemGeneral var10000 = Witchery.Items.GENERIC;
                  ItemGeneral.teleportToLocation(world, (double)posX, (double)posY, (double)posZ, world.field_73011_w.field_76574_g, entity, true);
                  return true;
               }

               if (player != null) {
                  if (isImmune) {
                     ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.blackmagicdampening");
                  } else {
                     ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.voodooprotectionactivated");
                  }
               }

               return false;
            }
         }
      }

      return false;
   }
}
