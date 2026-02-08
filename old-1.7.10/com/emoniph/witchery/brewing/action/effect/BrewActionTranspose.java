package com.emoniph.witchery.brewing.action.effect;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.brewing.AltarPower;
import com.emoniph.witchery.brewing.BrewItemKey;
import com.emoniph.witchery.brewing.BrewNamePart;
import com.emoniph.witchery.brewing.EffectLevel;
import com.emoniph.witchery.brewing.ModifiersEffect;
import com.emoniph.witchery.brewing.ModifiersRitual;
import com.emoniph.witchery.brewing.Probability;
import com.emoniph.witchery.brewing.action.BrewActionEffect;
import com.emoniph.witchery.brewing.action.BrewActionRitual;
import com.emoniph.witchery.brewing.potions.PotionEnderInhibition;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.util.BlockPosition;
import com.emoniph.witchery.util.BlockProtect;
import com.emoniph.witchery.util.CircleUtil;
import com.emoniph.witchery.util.EntityPosition;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BrewActionTranspose extends BrewActionEffect {
   public BrewActionTranspose(BrewItemKey itemKey, BrewNamePart namePart, AltarPower powerCost, Probability baseProbability, EffectLevel effectLevel) {
      super(itemKey, namePart, powerCost, baseProbability, effectLevel);
   }

   public boolean isRitualTargetLocationValid(MinecraftServer server, World world, int x, int y, int z, BlockPosition target, ModifiersRitual modifiers) {
      return BrewActionRitual.isDistanceAllowed(world, x, y, z, target, modifiers.covenSize, modifiers.leonard) && CircleUtil.isMediumCircle(target.getWorld(server), target.x, target.y, target.z, Witchery.Blocks.GLYPH_OTHERWHERE);
   }

   protected void doApplyRitualToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersRitual ritual, ModifiersEffect modifiers, ItemStack stack) {
      int height = true;
      BlockPosition midSource = ritual.getTarget();
      BlockPosition midTarget = ritual.getTarget(1);
      World worldSource = midSource.getWorld(MinecraftServer.func_71276_C());
      World worldTarget = midTarget.getWorld(MinecraftServer.func_71276_C());

      for(int dy = 0; dy < 3; ++dy) {
         for(int dx = -3; dx <= 3; ++dx) {
            for(int dz = -3; dz <= 3; ++dz) {
               if (dx * dx + dy * dz < 9) {
                  int sx = midSource.x + dx;
                  int sy = midSource.y + dy;
                  int sz = midSource.z + dz;
                  int tx = midTarget.x + dx;
                  int ty = midTarget.y + dy;
                  int tz = midTarget.z + dz;
                  Block block = world.func_147439_a(sx, sy, sz);
                  int meta = world.func_72805_g(sx, sy, sz);
                  if (BlockProtect.checkModsForBreakOK(worldSource, sx, sy, sz, block, meta, modifiers.caster) && BlockProtect.canBreak(block, worldSource) && BlockProtect.canBreak(tx, ty, tz, worldTarget) && BlockProtect.checkModsForBreakOK(worldTarget, tx, ty, tz, modifiers.caster)) {
                     world.func_147465_d(tx, ty, tz, block, meta, 3);
                     world.func_147468_f(sx, sy, sz);
                  }
               }
            }
         }
      }

   }

   protected void doApplyRitualToEntity(World world, EntityLivingBase targetEntity, ModifiersRitual ritualModifiers, ModifiersEffect modifiers, ItemStack stack) {
      if (!PotionEnderInhibition.isActive(targetEntity, 3)) {
         BlockPosition target = ritualModifiers.getTarget();
         ItemGeneral var10000 = Witchery.Items.GENERIC;
         ItemGeneral.teleportToLocation(targetEntity.field_70170_p, (double)target.x, (double)target.y, (double)target.z, target.dimension, targetEntity, true);
      }

   }

   protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
   }

   protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
      if (!PotionEnderInhibition.isActive(targetEntity, modifiers.getStrength())) {
         this.teleportAway(world, new BlockPosition(world, modifiers.impactLocation != null ? modifiers.impactLocation : new EntityPosition(targetEntity)), targetEntity, 10 * (modifiers.getStrength() + 1));
      }

   }

   private void teleportAway(World world, BlockPosition position, EntityLivingBase entity, int range) {
      if (!world.field_72995_K) {
         int distance = range;
         int doubleDistance = range * 2;
         int posX = position.x;
         int posY = position.y;
         int posZ = position.z;

         for(int attempt = 0; attempt < 3; ++attempt) {
            posX += world.field_73012_v.nextInt(doubleDistance) - distance;
            posZ += world.field_73012_v.nextInt(doubleDistance) - distance;

            int maxY;
            for(maxY = Math.min(posY + 64, 250); !world.func_147439_a(posX, posY, posZ).func_149688_o().func_76220_a() && posY >= 0; --posY) {
            }

            while((!world.func_147439_a(posX, posY, posZ).func_149688_o().func_76220_a() || world.func_147439_a(posX, posY, posZ) == Blocks.field_150357_h || !world.func_147437_c(posX, posY + 1, posZ) || !world.func_147437_c(posX, posY + 2, posZ) || !world.func_147437_c(posX, posY + 3, posZ)) && posY < maxY) {
               ++posY;
            }

            if (posY > 0 && posY < maxY) {
               ItemGeneral.teleportToLocation(world, 0.5D + (double)posX, 1.0D + (double)posY, 0.5D + (double)posZ, world.field_73011_w.field_76574_g, entity, true);
               break;
            }
         }
      }

   }
}
