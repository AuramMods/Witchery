package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class RiteGlyphicTransformation extends Rite {
   public void addSteps(ArrayList<RitualStep> steps, int intialStage) {
      steps.add(new RiteGlyphicTransformation.StepGlyphicTransformation(this));
   }

   private static class StepGlyphicTransformation extends RitualStep {
      private final RiteGlyphicTransformation rite;

      public StepGlyphicTransformation(RiteGlyphicTransformation rite) {
         super(false);
         this.rite = rite;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 30L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            if (!world.field_72995_K) {
               double RADIUS = 4.0D;
               List items = world.func_72872_a(EntityItem.class, AxisAlignedBB.func_72330_a((double)posX - 4.0D, (double)(posY - 2), (double)posZ - 4.0D, (double)posX + 4.0D, (double)(posY + 2), (double)posZ + 4.0D));
               int whiteChalk = 0;
               int purpleChalk = 0;
               int redChalk = 0;
               Iterator i$ = items.iterator();

               label123:
               while(true) {
                  EntityItem item;
                  while(true) {
                     boolean first;
                     if (!i$.hasNext()) {
                        Block blockID = Blocks.field_150350_a;
                        int size = 0;
                        if (whiteChalk == 0 && redChalk == 0 && purpleChalk == 0) {
                           return RitualStep.Result.ABORTED_REFUND;
                        }

                        if (redChalk > 0) {
                           blockID = Witchery.Blocks.GLYPH_INFERNAL;
                           size = Math.min(redChalk, 3);
                        } else if (purpleChalk > 0) {
                           blockID = Witchery.Blocks.GLYPH_OTHERWHERE;
                           size = Math.min(purpleChalk, 3);
                        } else if (whiteChalk > 0) {
                           blockID = Witchery.Blocks.GLYPH_RITUAL;
                           size = Math.min(whiteChalk, 3);
                        }

                        int a = true;
                        int b = true;
                        first = true;
                        int _ = false;
                        int[][] PATTERN = new int[][]{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0}, {0, 0, 0, 3, 0, 0, 2, 2, 2, 2, 2, 0, 0, 3, 0, 0, 0}, {0, 0, 3, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 3, 0, 0}, {0, 3, 0, 0, 2, 0, 0, 1, 1, 1, 0, 0, 2, 0, 0, 3, 0}, {0, 3, 0, 2, 0, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 3, 0}, {0, 3, 0, 2, 0, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 3, 0}, {0, 3, 0, 2, 0, 1, 0, 0, 4, 0, 0, 1, 0, 2, 0, 3, 0}, {0, 3, 0, 2, 0, 1, 0, 0, 0, 0, 0, 1, 0, 2, 0, 3, 0}, {0, 3, 0, 2, 0, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 3, 0}, {0, 3, 0, 0, 2, 0, 0, 1, 1, 1, 0, 0, 2, 0, 0, 3, 0}, {0, 0, 3, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 3, 0, 0}, {0, 0, 0, 3, 0, 0, 2, 2, 2, 2, 2, 0, 0, 3, 0, 0, 0}, {0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
                        int offsetZ = (PATTERN.length - 1) / 2;

                        for(int z = 0; z < PATTERN.length - 1; ++z) {
                           int worldZ = posZ - offsetZ + z;
                           int offsetX = (PATTERN[z].length - 1) / 2;

                           for(int x = 0; x < PATTERN[z].length; ++x) {
                              int worldX = posX - offsetX + x;
                              int item = PATTERN[PATTERN.length - 1 - z][x];
                              if (item == size) {
                                 Block currentBlockID = world.func_147439_a(worldX, posY, worldZ);
                                 if ((currentBlockID == Witchery.Blocks.GLYPH_INFERNAL || currentBlockID == Witchery.Blocks.GLYPH_OTHERWHERE || currentBlockID == Witchery.Blocks.GLYPH_RITUAL) && currentBlockID != blockID) {
                                    int meta = world.func_72805_g(worldX, posY, worldZ);
                                    world.func_147465_d(worldX, posY, worldZ, blockID, meta, 3);
                                    ParticleEffect.SMOKE.send(SoundEffect.NONE, world, (double)worldX, (double)(posY + 1), (double)worldZ, 0.5D, 1.0D, 16);
                                 }
                              }
                           }
                        }
                        break label123;
                     }

                     Object obj = i$.next();
                     item = (EntityItem)obj;
                     ItemStack stack = item.func_92059_d();
                     if (redChalk == 0 && purpleChalk == 0 && stack.func_77969_a(new ItemStack(Witchery.Items.CHALK_RITUAL, 1, 0))) {
                        first = whiteChalk == 0;
                        whiteChalk += stack.field_77994_a;
                        if (first) {
                           --stack.field_77994_a;
                           if (stack.field_77994_a <= 0) {
                              world.func_72900_e(item);
                           }
                        }
                        break;
                     }

                     if (redChalk == 0 && whiteChalk == 0 && stack.func_77969_a(new ItemStack(Witchery.Items.CHALK_OTHERWHERE, 1, 0))) {
                        first = purpleChalk == 0;
                        purpleChalk += stack.field_77994_a;
                        if (first) {
                           --stack.field_77994_a;
                           if (stack.field_77994_a <= 0) {
                              world.func_72900_e(item);
                           }
                        }
                        break;
                     }

                     if (purpleChalk == 0 && whiteChalk == 0 && stack.func_77969_a(new ItemStack(Witchery.Items.CHALK_INFERNAL, 1, 0))) {
                        first = redChalk == 0;
                        redChalk += stack.field_77994_a;
                        if (first) {
                           --stack.field_77994_a;
                           if (stack.field_77994_a <= 0) {
                              world.func_72900_e(item);
                           }
                        }
                        break;
                     }
                  }

                  ParticleEffect.SMOKE.send(SoundEffect.RANDOM_POP, item, 1.0D, 1.0D, 16);
               }
            }

            return RitualStep.Result.COMPLETED;
         }
      }
   }
}
