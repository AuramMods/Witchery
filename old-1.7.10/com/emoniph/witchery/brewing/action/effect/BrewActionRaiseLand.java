package com.emoniph.witchery.brewing.action.effect;

import com.emoniph.witchery.brewing.AltarPower;
import com.emoniph.witchery.brewing.BrewItemKey;
import com.emoniph.witchery.brewing.BrewNamePart;
import com.emoniph.witchery.brewing.EffectLevel;
import com.emoniph.witchery.brewing.ModifiersEffect;
import com.emoniph.witchery.brewing.ModifiersRitual;
import com.emoniph.witchery.brewing.Probability;
import com.emoniph.witchery.brewing.action.BrewActionEffect;
import com.emoniph.witchery.util.BlockProtect;
import com.emoniph.witchery.util.Coord;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BrewActionRaiseLand extends BrewActionEffect {
   public BrewActionRaiseLand(BrewItemKey itemKey, BrewNamePart namePart, AltarPower powerCost, EffectLevel effectLevel) {
      super(itemKey, namePart, powerCost, new Probability(1.0D), effectLevel);
   }

   protected void doApplyRitualToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersRitual ritualModifiers, ModifiersEffect modifiers, ItemStack stack) {
      int r = (modifiers.getStrength() + 1) * 2;
      int rsq = r * r;

      for(int dx = -r; dx <= r; ++dx) {
         for(int dz = -r; dz <= r; ++dz) {
            if (dx * dx + dz * dz < rsq) {
               int nx = x + dx;
               int nz = z + dz;
               this.doApplyToBlock(world, nx, y, nz, ForgeDirection.UP, 1, modifiers, stack);
            }
         }
      }

   }

   protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
      Coord coord = new Coord(targetEntity);
      this.doApplyToBlock(world, coord.x, coord.y - 1, coord.z, ForgeDirection.UP, 1, modifiers, stack);
   }

   protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
      while(!world.func_147437_c(x, y + 1, z) && y < 255) {
         ++y;
      }

      if (BlockProtect.canBreak(x, y, z, world) && BlockProtect.checkModsForBreakOK(world, x, y, z, modifiers.caster)) {
         int height = (modifiers.getStrength() + 1) * (modifiers.ritualised ? 2 : 3);
         if (!world.field_72995_K) {
            for(int i = 0; i < height; ++i) {
               int dy = y - i;
               Block block = world.func_147439_a(x, dy, z);
               int meta = world.func_72805_g(x, dy, z);
               world.func_147468_f(x, dy, z);
               world.func_147465_d(x, dy + height, z, block, meta, 3);
            }
         }

         AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)x, (double)y, (double)z, (double)(x + 1), (double)(y + 2), (double)(z + 1));
         List<EntityLivingBase> list = world.func_72872_a(EntityLivingBase.class, bounds);
         Iterator i$ = list.iterator();

         while(i$.hasNext()) {
            EntityLivingBase entity = (EntityLivingBase)i$.next();
            if (entity instanceof EntityPlayer) {
               entity.func_70634_a(0.5D + (double)x, (double)(y + height + 1), 0.5D + (double)z);
            } else {
               entity.func_70634_a(0.5D + (double)x, (double)(y + height + 1), 0.5D + (double)z);
            }
         }
      }

   }
}
