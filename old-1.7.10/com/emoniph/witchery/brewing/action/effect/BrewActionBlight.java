package com.emoniph.witchery.brewing.action.effect;

import com.emoniph.witchery.brewing.AltarPower;
import com.emoniph.witchery.brewing.BrewItemKey;
import com.emoniph.witchery.brewing.BrewNamePart;
import com.emoniph.witchery.brewing.EffectLevel;
import com.emoniph.witchery.brewing.ModifiersEffect;
import com.emoniph.witchery.brewing.Probability;
import com.emoniph.witchery.brewing.action.BrewActionEffect;
import com.emoniph.witchery.util.BlockActionCircle;
import com.emoniph.witchery.util.BlockProtect;
import com.emoniph.witchery.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BrewActionBlight extends BrewActionEffect {
   public BrewActionBlight(BrewItemKey itemKey, BrewNamePart namePart, AltarPower powerCost, Probability baseProbability, EffectLevel effectLevel) {
      super(itemKey, namePart, powerCost, baseProbability, effectLevel);
   }

   protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, final ModifiersEffect modifiers, ItemStack stack) {
      if (BlockUtil.isReplaceableBlock(world, x, y, z)) {
         --y;
      }

      (new BlockActionCircle() {
         public void onBlock(World world, int x, int y, int z) {
            if (BlockProtect.checkModsForBreakOK(world, x, y, z, modifiers.caster)) {
               Block blockID = world.func_147439_a(x, y, z);
               Block blockBelowID = world.func_147439_a(x, y - 1, z);
               if (blockID == Blocks.field_150329_H) {
                  world.func_147468_f(x, y, z);
                  BrewActionBlight.this.blightGround(world, x, y - 1, z, blockBelowID);
               } else if (blockID != Blocks.field_150328_O && blockID != Blocks.field_150327_N && blockID != Blocks.field_150459_bM && blockID != Blocks.field_150464_aj && blockID != Blocks.field_150469_bN && blockID != Blocks.field_150393_bb && blockID != Blocks.field_150394_bc && blockID != Blocks.field_150440_ba && blockID != Blocks.field_150423_aK && blockID != Blocks.field_150398_cm) {
                  if (blockID == Blocks.field_150458_ak) {
                     world.func_147449_b(x, y, z, Blocks.field_150354_m);
                  } else if (blockID.func_149688_o().func_76220_a()) {
                     BrewActionBlight.this.blightGround(world, x, y, z, blockID);
                  } else if (blockBelowID.func_149688_o().func_76220_a()) {
                     BrewActionBlight.this.blightGround(world, x, y - 1, z, blockBelowID);
                  }
               } else {
                  world.func_147449_b(x, y, z, Blocks.field_150330_I);
                  BrewActionBlight.this.blightGround(world, x, y - 1, z, blockBelowID);
               }
            }

         }
      }).processFilledCircle(world, x, y + 1, z, radius);
   }

   protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
      if (targetEntity instanceof EntityVillager && world.field_73012_v.nextInt(10 - modifiers.getStrength() * 2) == 0) {
         EntityZombie entityzombie = new EntityZombie(world);
         entityzombie.func_82149_j(targetEntity);
         world.func_72900_e(targetEntity);
         entityzombie.func_110161_a((IEntityLivingData)null);
         entityzombie.func_82229_g(true);
         if (targetEntity.func_70631_g_()) {
            entityzombie.func_82227_f(true);
         }

         world.func_72838_d(entityzombie);
         world.func_72889_a((EntityPlayer)null, 1016, (int)entityzombie.field_70165_t, (int)entityzombie.field_70163_u, (int)entityzombie.field_70161_v, 0);
      } else if (targetEntity instanceof EntityCow && world.field_73012_v.nextInt(20 - modifiers.getStrength() * 3) == 0) {
         EntityMooshroom entityzombie = new EntityMooshroom(world);
         entityzombie.func_82149_j(targetEntity);
         world.func_72900_e(targetEntity);
         entityzombie.func_110161_a((IEntityLivingData)null);
         world.func_72838_d(entityzombie);
         world.func_72889_a((EntityPlayer)null, 1016, (int)entityzombie.field_70165_t, (int)entityzombie.field_70163_u, (int)entityzombie.field_70161_v, 0);
      } else if (targetEntity instanceof EntityAnimal && world.field_73012_v.nextInt(modifiers.getStrength() > 1 ? 2 : 3) == 0) {
         targetEntity.func_70097_a(DamageSource.field_76376_m, 20.0F);
      }

   }

   public void blightGround(World world, int posX, int posY, int posZ, Block blockBelowID) {
      if (blockBelowID == Blocks.field_150346_d || blockBelowID == Blocks.field_150349_c || blockBelowID == Blocks.field_150391_bh || blockBelowID == Blocks.field_150458_ak) {
         int rand = world.field_73012_v.nextInt(5);
         if (rand == 0) {
            world.func_147449_b(posX, posY, posZ, Blocks.field_150354_m);
         } else if (rand == 1) {
            world.func_147449_b(posX, posY, posZ, Blocks.field_150346_d);
         }
      }

   }
}
