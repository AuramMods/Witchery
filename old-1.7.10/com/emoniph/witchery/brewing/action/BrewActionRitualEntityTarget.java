package com.emoniph.witchery.brewing.action;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockAreaMarker;
import com.emoniph.witchery.brewing.AltarPower;
import com.emoniph.witchery.brewing.BrewItemKey;
import com.emoniph.witchery.brewing.ModifiersEffect;
import com.emoniph.witchery.brewing.ModifiersImpact;
import com.emoniph.witchery.brewing.ModifiersRitual;
import com.emoniph.witchery.brewing.RitualStatus;
import com.emoniph.witchery.brewing.WitcheryBrewRegistry;
import com.emoniph.witchery.item.ItemHunterClothes;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.util.EntityPosition;
import com.emoniph.witchery.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class BrewActionRitualEntityTarget extends BrewActionRitual {
   public BrewActionRitualEntityTarget(BrewItemKey itemKey, AltarPower powerCost) {
      super(itemKey, powerCost, false);
   }

   public RitualStatus updateRitual(MinecraftServer server, BrewActionList actionList, World world, int x, int y, int z, ModifiersRitual modifiers, ModifiersImpact impactModifiers) {
      NBTTagCompound tag = actionList.getTopItemStack().func_77978_p();
      if (tag == null) {
         return RitualStatus.FAILED;
      } else {
         EntityLivingBase targetEntity = Witchery.Items.TAGLOCK_KIT.getBoundEntity(world, (Entity)null, actionList.getTopItemStack(), 1);
         if (targetEntity != null) {
            if (!isDistanceAllowed(world, x, y, z, targetEntity.field_70165_t, targetEntity.field_70163_u, targetEntity.field_70161_v, targetEntity.field_71093_bK, modifiers.covenSize, modifiers.leonard)) {
               return RitualStatus.FAILED_DISTANCE;
            } else if (!actionList.isTargetLocationValid(server, world, x, y, z, modifiers.getTarget(), modifiers)) {
               return RitualStatus.FAILED_INVALID_CIRCLES;
            } else {
               ModifiersEffect modifiersEffect = new ModifiersEffect(1.0D, 1.0D, false, new EntityPosition(targetEntity), true, modifiers.covenSize, EntityUtil.playerOrFake(world, (EntityLivingBase)((EntityPlayer)null)));
               modifiers.taglockUsed = true;
               boolean isImmune = ItemHunterClothes.isCurseProtectionActive(targetEntity);
               if (!isImmune) {
                  isImmune = BlockAreaMarker.AreaMarkerRegistry.instance().isProtectionActive(targetEntity, (Rite)null);
               }

               if (!isImmune && !Witchery.Items.POPPET.voodooProtectionActivated((EntityPlayer)null, (ItemStack)null, targetEntity, 1)) {
                  WitcheryBrewRegistry.INSTANCE.applyRitualToEntity(targetEntity.field_70170_p, targetEntity, actionList.getTagCompound(), modifiers, modifiersEffect);
               }

               return RitualStatus.COMPLETE;
            }
         } else {
            return RitualStatus.FAILED;
         }
      }
   }
}
