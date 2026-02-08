package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.world.World;

public class RiteCookItem extends Rite {
   private final float radius;
   private final double burnChance;

   public RiteCookItem(float radius, double burnChance) {
      this.radius = radius;
      this.burnChance = burnChance;
   }

   public void addSteps(ArrayList<RitualStep> steps, int intialStage) {
      steps.add(new RiteCookItem.StepCookItem(this));
   }

   private static class StepCookItem extends RitualStep {
      private final RiteCookItem rite;

      public StepCookItem(RiteCookItem rite) {
         super(false);
         this.rite = rite;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 20L != 0L) {
            return RitualStep.Result.STARTING;
         } else if (!world.field_72995_K) {
            ArrayList<EntityItem> items = this.rite.getItemsInRadius(world, posX, posY, posZ, this.rite.radius);
            int count = 0;
            Iterator i$ = items.iterator();

            while(true) {
               EntityItem item;
               ItemStack cookedStack;
               do {
                  do {
                     do {
                        if (!i$.hasNext()) {
                           if (count == 0) {
                              return RitualStep.Result.ABORTED_REFUND;
                           }

                           ParticleEffect.FLAME.send(SoundEffect.MOB_GHAST_FIREBALL, world, (double)posX, (double)posY, (double)posZ, 3.0D, 2.0D, 16);
                           return RitualStep.Result.COMPLETED;
                        }

                        item = (EntityItem)i$.next();
                        cookedStack = FurnaceRecipes.func_77602_a().func_151395_a(item.func_92059_d());
                     } while(cookedStack == null);
                  } while(!(cookedStack.func_77973_b() instanceof ItemFood));
               } while(item.func_92059_d().field_77994_a <= 0);

               int size = item.func_92059_d().field_77994_a;
               int burnCount = 0;

               for(int i = 0; i < size; ++i) {
                  if (world.field_73012_v.nextDouble() < this.rite.burnChance) {
                     ++burnCount;
                  }
               }

               item.func_70106_y();
               EntityItem burntEntity;
               if (size - burnCount > 0) {
                  cookedStack.field_77994_a = size - burnCount;
                  burntEntity = new EntityItem(world, (double)posX, (double)posY + 0.05D, (double)posZ, cookedStack);
                  burntEntity.field_70159_w = 0.0D;
                  burntEntity.field_70179_y = 0.0D;
                  world.func_72838_d(burntEntity);
               }

               if (burnCount > 0) {
                  burntEntity = new EntityItem(world, (double)posX, (double)posY + 0.05D, (double)posZ, new ItemStack(Items.field_151044_h, burnCount, 1));
                  burntEntity.field_70159_w = 0.0D;
                  burntEntity.field_70179_y = 0.0D;
                  world.func_72838_d(burntEntity);
               }

               ++count;
            }
         } else {
            return RitualStep.Result.COMPLETED;
         }
      }
   }
}
