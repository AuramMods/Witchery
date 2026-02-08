package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.predictions.PredictionManager;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class RiteSummonItem extends Rite {
   private final ItemStack itemToSummon;
   private final RiteSummonItem.Binding binding;

   public RiteSummonItem(ItemStack itemToSummon, RiteSummonItem.Binding binding) {
      this.itemToSummon = itemToSummon;
      this.binding = binding;
   }

   public void addSteps(ArrayList<RitualStep> steps, int intialStage) {
      steps.add(new RiteSummonItem.StepSummonItem(this));
   }

   private static class StepSummonItem extends RitualStep {
      private final RiteSummonItem rite;

      public StepSummonItem(RiteSummonItem rite) {
         super(false);
         this.rite = rite;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 20L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            if (!world.field_72995_K) {
               ItemStack itemstack = ItemStack.func_77944_b(this.rite.itemToSummon);
               if (this.rite.binding == RiteSummonItem.Binding.LOCATION) {
                  Witchery.Items.GENERIC.bindToLocation(world, posX, posY, posZ, world.field_73011_w.field_76574_g, world.field_73011_w.func_80007_l(), itemstack);
               } else {
                  boolean r;
                  AxisAlignedBB bounds;
                  Iterator i$;
                  Object obj;
                  EntityPlayer player;
                  if (this.rite.binding == RiteSummonItem.Binding.ENTITY) {
                     r = true;
                     EntityLivingBase target = null;
                     bounds = AxisAlignedBB.func_72330_a((double)(posX - 4), (double)posY, (double)(posZ - 4), (double)(posX + 4), (double)(posY + 1), (double)(posZ + 4));
                     i$ = world.func_72872_a(EntityPlayer.class, bounds).iterator();

                     while(i$.hasNext()) {
                        obj = i$.next();
                        player = (EntityPlayer)obj;
                        if (Coord.distance(player.field_70165_t, player.field_70163_u, player.field_70161_v, (double)posX, (double)posY, (double)posZ) <= 4.0D) {
                           target = player;
                        }
                     }

                     if (target != null) {
                        bounds = AxisAlignedBB.func_72330_a((double)(posX - 4), (double)posY, (double)(posZ - 4), (double)(posX + 4), (double)(posY + 1), (double)(posZ + 4));
                        i$ = world.func_72872_a(EntityLiving.class, bounds).iterator();

                        while(i$.hasNext()) {
                           obj = i$.next();
                           EntityLiving entity = (EntityLiving)obj;
                           if (Coord.distance(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, (double)posX, (double)posY, (double)posZ) <= 4.0D) {
                              target = entity;
                           }
                        }
                     }

                     if (target == null) {
                        return RitualStep.Result.ABORTED_REFUND;
                     }

                     Witchery.Items.TAGLOCK_KIT.setTaglockForEntity(itemstack, (EntityPlayer)null, (Entity)target, false, 1);
                  } else if (this.rite.binding != RiteSummonItem.Binding.PLAYER) {
                     if (this.rite.binding == RiteSummonItem.Binding.COPY_LOCATION) {
                        Iterator i$ = ritual.sacrificedItems.iterator();

                        while(i$.hasNext()) {
                           RitualStep.SacrificedItem item = (RitualStep.SacrificedItem)i$.next();
                           if (Witchery.Items.GENERIC.hasLocationBinding(item.itemstack)) {
                              Witchery.Items.GENERIC.copyLocationBinding(item.itemstack, itemstack);
                              break;
                           }
                        }
                     }
                  } else {
                     r = true;
                     EntityLivingBase target = null;
                     bounds = AxisAlignedBB.func_72330_a((double)(posX - 4), (double)posY, (double)(posZ - 4), (double)(posX + 4), (double)(posY + 1), (double)(posZ + 4));
                     i$ = world.func_72872_a(EntityPlayer.class, bounds).iterator();

                     while(i$.hasNext()) {
                        obj = i$.next();
                        player = (EntityPlayer)obj;
                        if (Coord.distance(player.field_70165_t, player.field_70163_u, player.field_70161_v, (double)posX, (double)posY, (double)posZ) <= 4.0D) {
                           target = player;
                        }
                     }

                     if (target == null) {
                        return RitualStep.Result.ABORTED_REFUND;
                     }

                     NBTTagCompound nbtRoot = new NBTTagCompound();
                     nbtRoot.func_74778_a("WITCBoundPlayer", target.func_70005_c_());
                     itemstack.func_77982_d(nbtRoot);
                  }
               }

               if (itemstack.func_77973_b() == Item.func_150898_a(Witchery.Blocks.CRYSTAL_BALL)) {
                  EntityPlayer player = ritual.getInitiatingPlayer(world);
                  if (player != null) {
                     PredictionManager.instance().setFortuneTeller(player, true);
                  }
               }

               EntityItem entity = new EntityItem(world, 0.5D + (double)posX, (double)posY + 1.5D, 0.5D + (double)posZ, itemstack);
               entity.field_70159_w = 0.0D;
               entity.field_70181_x = 0.3D;
               entity.field_70179_y = 0.0D;
               world.func_72838_d(entity);
               ParticleEffect.SPELL.send(SoundEffect.RANDOM_FIZZ, entity, 0.5D, 0.5D, 16);
            }

            return RitualStep.Result.COMPLETED;
         }
      }
   }

   public static enum Binding {
      NONE,
      LOCATION,
      ENTITY,
      COPY_LOCATION,
      PLAYER;
   }
}
