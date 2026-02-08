package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.entity.EntityBanshee;
import com.emoniph.witchery.entity.EntityDeath;
import com.emoniph.witchery.entity.EntityPoltergeist;
import com.emoniph.witchery.entity.EntitySpectre;
import com.emoniph.witchery.entity.EntitySpirit;
import com.emoniph.witchery.infusion.infusions.spirit.InfusedSpiritEffect;
import com.emoniph.witchery.item.ItemDeathsClothes;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class RiteBindSpiritsToFetish extends Rite {
   private final int radius;

   public RiteBindSpiritsToFetish(int radius) {
      this.radius = radius;
   }

   public void addSteps(ArrayList<RitualStep> steps, int intialStage) {
      steps.add(new RiteBindSpiritsToFetish.StepSpiritsToFetish(this));
   }

   private static class StepSpiritsToFetish extends RitualStep {
      private final RiteBindSpiritsToFetish rite;

      public StepSpiritsToFetish(RiteBindSpiritsToFetish rite) {
         super(false);
         this.rite = rite;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 20L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            if (!world.field_72995_K) {
               int r = this.rite.radius;
               int r2 = r * r;
               AxisAlignedBB bb = AxisAlignedBB.func_72330_a((double)(posX - r), (double)(posY - r), (double)(posZ - r), (double)(posX + r), (double)(posY + r), (double)(posZ + r));
               List entities = world.func_72872_a(EntityCreature.class, bb);
               ArrayList<EntitySpectre> spectreList = new ArrayList();
               ArrayList<EntitySpirit> spiritList = new ArrayList();
               ArrayList<EntityBanshee> bansheeList = new ArrayList();
               ArrayList<EntityPoltergeist> poltergeistList = new ArrayList();
               Iterator i$ = entities.iterator();

               while(i$.hasNext()) {
                  Object obj = i$.next();
                  if (obj instanceof EntitySpectre) {
                     spectreList.add((EntitySpectre)obj);
                  } else if (obj instanceof EntityPoltergeist) {
                     poltergeistList.add((EntityPoltergeist)obj);
                  } else if (obj instanceof EntityBanshee) {
                     bansheeList.add((EntityBanshee)obj);
                  } else if (obj instanceof EntitySpirit) {
                     spiritList.add((EntitySpirit)obj);
                  }
               }

               ItemStack stack = null;
               Iterator i$ = ritual.sacrificedItems.iterator();

               while(i$.hasNext()) {
                  RitualStep.SacrificedItem item = (RitualStep.SacrificedItem)i$.next();
                  if (item.itemstack.func_77969_a(new ItemStack(Witchery.Blocks.FETISH_SCARECROW))) {
                     stack = item.itemstack;
                     break;
                  }

                  if (item.itemstack.func_77969_a(new ItemStack(Witchery.Blocks.FETISH_TREANT_IDOL))) {
                     stack = item.itemstack;
                     break;
                  }

                  if (item.itemstack.func_77969_a(new ItemStack(Witchery.Blocks.FETISH_WITCHS_LADDER))) {
                     stack = item.itemstack;
                     break;
                  }
               }

               if (stack == null) {
                  return RitualStep.Result.ABORTED_REFUND;
               }

               int result = InfusedSpiritEffect.tryBindFetish(world, stack, spiritList, spectreList, bansheeList, poltergeistList);
               if (result == 0) {
                  return RitualStep.Result.ABORTED_REFUND;
               }

               if (result == 2) {
                  EntityPlayer deathPlayer = this.findDeathPlayer(world);
                  if (deathPlayer != null) {
                     ItemGeneral var10000 = Witchery.Items.GENERIC;
                     ItemGeneral.teleportToLocation(world, (double)posX, (double)posY, (double)posZ, world.field_73011_w.field_76574_g, deathPlayer, true);
                     ParticleEffect.INSTANT_SPELL.send(SoundEffect.MOB_WITHER_SPAWN, deathPlayer, 0.5D, 1.5D, 16);
                  } else {
                     EntityDeath death = new EntityDeath(world);
                     death.func_70012_b(0.5D + (double)posX, (double)posY + 0.1D, 0.5D + (double)posZ, 0.0F, 0.0F);
                     death.func_110163_bv();
                     world.func_72838_d(death);
                     ParticleEffect.INSTANT_SPELL.send(SoundEffect.MOB_WITHER_SPAWN, death, 0.5D, 1.5D, 16);
                  }
               } else {
                  EntityItem entity = new EntityItem(world, 0.5D + (double)posX, (double)posY + 1.5D, 0.5D + (double)posZ, stack);
                  entity.field_70159_w = 0.0D;
                  entity.field_70181_x = 0.3D;
                  entity.field_70179_y = 0.0D;
                  world.func_72838_d(entity);
                  ParticleEffect.SPELL.send(SoundEffect.RANDOM_FIZZ, entity, 0.5D, 1.5D, 16);
               }
            }

            return RitualStep.Result.COMPLETED;
         }
      }

      private EntityPlayer findDeathPlayer(World world) {
         Iterator i$ = world.field_73010_i.iterator();

         EntityPlayer player;
         do {
            if (!i$.hasNext()) {
               return null;
            }

            Object obj = i$.next();
            player = (EntityPlayer)obj;
         } while(!ItemDeathsClothes.isFullSetWorn(player) || player.func_71045_bC() == null || player.func_71045_bC().func_77973_b() != Witchery.Items.DEATH_HAND);

         return player;
      }
   }
}
