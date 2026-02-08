package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.entity.EntityDemon;
import com.emoniph.witchery.entity.EntityImp;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RiteRegistry;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TameableUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class RiteSummonCreature extends Rite {
   private final Class<? extends EntityCreature> creatureToSummon;
   private boolean bindTameable;

   public RiteSummonCreature(Class<? extends EntityCreature> creatureToSummon, boolean bindTameable) {
      this.creatureToSummon = creatureToSummon;
      this.bindTameable = bindTameable;
   }

   public void addSteps(ArrayList<RitualStep> steps, int intialStage) {
      steps.add(new RiteSummonCreature.StepSummonCreature(this));
   }

   private static class StepSummonCreature extends RitualStep {
      private final RiteSummonCreature rite;

      public StepSummonCreature(RiteSummonCreature rite) {
         super(false);
         this.rite = rite;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 20L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            if (!world.field_72995_K) {
               int[][] PATTERN = new int[][]{{0, 0, 1, 1, 1, 0, 0}, {0, 1, 1, 1, 1, 1, 0}, {1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 2, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}, {0, 1, 1, 1, 1, 1, 0}, {0, 0, 1, 1, 1, 0, 0}};
               int obstructions = 0;

               for(int y = posY + 1; y <= posY + 3; ++y) {
                  int offsetZ = (PATTERN.length - 1) / 2;

                  for(int z = 0; z < PATTERN.length - 1; ++z) {
                     int worldZ = posZ - offsetZ + z;
                     int offsetX = (PATTERN[z].length - 1) / 2;

                     for(int x = 0; x < PATTERN[z].length; ++x) {
                        int worldX = posX - offsetX + x;
                        int val = PATTERN[PATTERN.length - 1 - z][x];
                        Material material;
                        if (val == 1) {
                           material = world.func_147439_a(worldX, y, worldZ).func_149688_o();
                           if (material != null && material.func_76220_a()) {
                              ++obstructions;
                           }
                        } else if (val == 2) {
                           material = world.func_147439_a(worldX, y, worldZ).func_149688_o();
                           if (material != null && material.func_76220_a()) {
                              obstructions += 100;
                           }
                        }
                     }
                  }
               }

               int MAX_OBSTRUCTIONS = true;
               if (obstructions > 1) {
                  ParticleEffect.LARGE_SMOKE.send(SoundEffect.NOTE_SNARE, world, (double)posX, (double)posY, (double)posZ, 0.5D, 2.0D, 16);
                  RiteRegistry.RiteError("witchery.rite.obstructedcircle", ritual.getInitiatingPlayerName(), world);
                  return RitualStep.Result.ABORTED_REFUND;
               }

               try {
                  Constructor ctor = this.rite.creatureToSummon.getConstructor(World.class);
                  EntityLiving entity = (EntityLiving)ctor.newInstance(world);
                  EntityPlayer player;
                  if (entity instanceof EntityDemon) {
                     ((EntityDemon)entity).setPlayerCreated(true);
                  } else {
                     if (entity instanceof EntityImp && ritual.covenSize == 0) {
                        player = ritual.getInitiatingPlayer(world);
                        SoundEffect.NOTE_SNARE.playAt(world, (double)posX, (double)posY, (double)posZ);
                        if (player != null) {
                           ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.coventoosmall");
                        }

                        return RitualStep.Result.ABORTED_REFUND;
                     }

                     if (this.rite.bindTameable && entity instanceof EntityTameable) {
                        ((EntityTameable)entity).func_70903_f(true);
                        TameableUtil.setOwner((EntityTameable)entity, ritual.getInitiatingPlayer(world));
                     }
                  }

                  entity.func_70012_b(0.5D + (double)posX, 1.0D + (double)posY, 0.5D + (double)posZ, 1.0F, 0.0F);
                  world.func_72838_d(entity);
                  player = null;
                  entity.func_110161_a(player);
                  ParticleEffect.PORTAL.send(SoundEffect.RANDOM_FIZZ, entity, 0.5D, 1.0D, 16);
               } catch (NoSuchMethodException var19) {
               } catch (InvocationTargetException var20) {
               } catch (InstantiationException var21) {
               } catch (IllegalAccessException var22) {
               }
            }

            return RitualStep.Result.COMPLETED;
         }
      }
   }
}
