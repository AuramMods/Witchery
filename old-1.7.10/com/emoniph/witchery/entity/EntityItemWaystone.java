package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.brewing.potions.PotionEnderInhibition;
import com.emoniph.witchery.common.IPowerSource;
import com.emoniph.witchery.common.PowerSources;
import com.emoniph.witchery.common.ServerTickEvents;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.CircleUtil;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.EntityPosition;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityItemWaystone extends EntityItem {
   public EntityItemWaystone(World world) {
      super(world);
   }

   public EntityItemWaystone(World world, double x, double y, double z) {
      super(world, x, y, z);
   }

   public EntityItemWaystone(World world, double x, double y, double z, ItemStack stack) {
      super(world, x, y, z, stack);
   }

   public EntityItemWaystone(EntityItem entityItem) {
      super(entityItem.field_70170_p, entityItem.field_70165_t, entityItem.field_70163_u, entityItem.field_70161_v, entityItem.func_92059_d());
      this.field_145804_b = entityItem.field_145804_b;
      this.field_70159_w = entityItem.field_70159_w;
      this.field_70181_x = entityItem.field_70181_x;
      this.field_70179_y = entityItem.field_70179_y;
   }

   public void func_70100_b_(EntityPlayer player) {
      double minPickupRange = 0.75D;
      double minPickupRangeSq = 0.5625D;
      if (this.func_70068_e(player) <= 0.5625D) {
         super.func_70100_b_(player);
      }

   }

   public void func_70071_h_() {
      super.func_70071_h_();
      if (!this.field_70170_p.field_72995_K && this.field_70292_b > TimeUtil.secsToTicks(2) && this.field_70292_b % 40 == 0) {
         Block glyph;
         Coord center;
         int originalStackSize;
         int remainingStackSize;
         double R;
         double RSq;
         EntityPosition centerPoint;
         AxisAlignedBB bounds;
         if (Witchery.Items.GENERIC.itemWaystone.isMatch(this.func_92059_d())) {
            glyph = Witchery.Blocks.GLYPH_OTHERWHERE;
            center = isTinyBlockCircle(this.field_70170_p, new Coord(this), glyph);
            if (center != null) {
               originalStackSize = this.func_92059_d().field_77994_a;
               remainingStackSize = 0;
               R = 2.0D;
               RSq = 4.0D;
               centerPoint = new EntityPosition((double)center.x + 0.5D, (double)center.y + 0.5D, (double)center.z + 0.5D);
               bounds = AxisAlignedBB.func_72330_a(centerPoint.x - 2.0D, centerPoint.y - 2.0D, centerPoint.z - 2.0D, centerPoint.x + 2.0D, centerPoint.y + 2.0D, centerPoint.z + 2.0D);
               ItemStack boundStone = null;
               EntityLivingBase target = null;
               double targetDistSq = -1.0D;
               List<EntityPlayer> nearbyPlayers = this.field_70170_p.func_72872_a(EntityPlayer.class, bounds);
               Iterator i$ = nearbyPlayers.iterator();

               while(true) {
                  EntityPlayer player;
                  double distSq;
                  do {
                     do {
                        if (!i$.hasNext()) {
                           if (target == null) {
                              List<EntityLiving> nearbyCreatures = this.field_70170_p.func_72872_a(EntityLiving.class, bounds);
                              Iterator i$ = nearbyCreatures.iterator();

                              label105:
                              while(true) {
                                 double distSq;
                                 EntityLiving creature;
                                 do {
                                    do {
                                       if (!i$.hasNext()) {
                                          break label105;
                                       }

                                       creature = (EntityLiving)i$.next();
                                       distSq = creature.func_70092_e(centerPoint.x, creature.field_70163_u, centerPoint.z);
                                    } while(!(distSq <= 4.0D));
                                 } while(target != null && !(distSq < targetDistSq));

                                 target = creature;
                                 targetDistSq = distSq;
                              }
                           }

                           if (target != null) {
                              IPowerSource power = PowerSources.findClosestPowerSource(this.field_70170_p, center);
                              if (power != null) {
                                 if (power.consumePower(4000.0F)) {
                                    int convertableStackSize = Math.min(originalStackSize, 1);
                                    remainingStackSize = originalStackSize - convertableStackSize;
                                    boundStone = Witchery.Items.GENERIC.itemWaystonePlayerBound.createStack(convertableStackSize);
                                    Witchery.Items.TAGLOCK_KIT.setTaglockForEntity(boundStone, (EntityPlayer)null, (Entity)target, false, 1);
                                 } else {
                                    ParticleEffect.SMOKE.send(SoundEffect.NOTE_SNARE, this.field_70170_p, center, 1.0D, 1.0D, 16);
                                 }
                              } else {
                                 ParticleEffect.SMOKE.send(SoundEffect.NOTE_SNARE, this.field_70170_p, center, 1.0D, 1.0D, 16);
                              }
                           } else {
                              int convertableStackSize = Math.min(originalStackSize, 8);
                              remainingStackSize = originalStackSize - convertableStackSize;
                              boundStone = Witchery.Items.GENERIC.itemWaystoneBound.createStack(convertableStackSize);
                              Witchery.Items.GENERIC.bindToLocation(this.field_70170_p, center.x, center.y, center.z, this.field_71093_bK, this.field_70170_p.field_73011_w.func_80007_l(), boundStone);
                           }

                           if (boundStone != null) {
                              EntityUtil.spawnEntityInWorld(this.field_70170_p, new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, boundStone));
                              if (remainingStackSize > 0) {
                                 EntityUtil.spawnEntityInWorld(this.field_70170_p, new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, Witchery.Items.GENERIC.itemWaystone.createStack(remainingStackSize)));
                              }

                              ParticleEffect.LARGE_EXPLODE.send(SoundEffect.RANDOM_POP, this, 1.0D, 1.0D, 16);
                              isInnerTinyBlockCircle(this.field_70170_p, center.x, center.y, center.z, glyph, true);
                              this.func_70106_y();
                           }

                           return;
                        }

                        player = (EntityPlayer)i$.next();
                        distSq = player.func_70092_e(centerPoint.x, player.field_70163_u, centerPoint.z);
                     } while(!(distSq <= 4.0D));
                  } while(target != null && !(distSq < targetDistSq));

                  target = player;
                  targetDistSq = distSq;
               }
            }
         } else if (!Witchery.Items.GENERIC.itemWaystoneBound.isMatch(this.func_92059_d()) && !Witchery.Items.GENERIC.itemWaystonePlayerBound.isMatch(this.func_92059_d())) {
            boolean remainingStackSize;
            int convertableStackSize;
            EntityCreature creature;
            EntitySpirit spirit;
            if (Witchery.Items.GENERIC.itemAttunedStone.isMatch(this.func_92059_d())) {
               glyph = Witchery.Blocks.GLYPH_RITUAL;
               center = isTinyBlockCircle(this.field_70170_p, new Coord(this), glyph);
               if (center != null) {
                  originalStackSize = this.func_92059_d().field_77994_a;
                  remainingStackSize = false;
                  R = 2.0D;
                  RSq = 4.0D;
                  centerPoint = new EntityPosition((double)center.x + 0.5D, (double)center.y + 0.5D, (double)center.z + 0.5D);
                  bounds = AxisAlignedBB.func_72330_a(centerPoint.x - 2.0D, centerPoint.y - 2.0D, centerPoint.z - 2.0D, centerPoint.x + 2.0D, centerPoint.y + 2.0D, centerPoint.z + 2.0D);
                  convertableStackSize = Math.min(originalStackSize, 1);
                  remainingStackSize = originalStackSize - convertableStackSize;
                  creature = Infusion.spawnCreature(this.field_70170_p, EntitySpirit.class, (int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v, (EntityLivingBase)null, 0, 0, ParticleEffect.INSTANT_SPELL, (SoundEffect)null);
                  if (creature != null) {
                     spirit = (EntitySpirit)creature;
                     creature.func_110163_bv();
                     spirit.setTarget("Village", 2);
                  }

                  if (remainingStackSize > 0) {
                     EntityUtil.spawnEntityInWorld(this.field_70170_p, new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, Witchery.Items.GENERIC.itemAttunedStone.createStack(remainingStackSize)));
                  }

                  ParticleEffect.LARGE_EXPLODE.send(SoundEffect.RANDOM_POP, this, 1.0D, 1.0D, 16);
                  isInnerTinyBlockCircle(this.field_70170_p, center.x, center.y, center.z, glyph, true);
                  this.func_70106_y();
               }
            } else if (Witchery.Items.GENERIC.itemSubduedSpirit.isMatch(this.func_92059_d())) {
               glyph = Witchery.Blocks.GLYPH_RITUAL;
               center = isTinyBlockCircle(this.field_70170_p, new Coord(this), glyph);
               if (center != null) {
                  originalStackSize = this.func_92059_d().field_77994_a;
                  remainingStackSize = false;
                  R = 2.0D;
                  RSq = 4.0D;
                  centerPoint = new EntityPosition((double)center.x + 0.5D, (double)center.y + 0.5D, (double)center.z + 0.5D);
                  bounds = AxisAlignedBB.func_72330_a(centerPoint.x - 2.0D, centerPoint.y - 2.0D, centerPoint.z - 2.0D, centerPoint.x + 2.0D, centerPoint.y + 2.0D, centerPoint.z + 2.0D);
                  convertableStackSize = Math.min(originalStackSize, 1);
                  remainingStackSize = originalStackSize - convertableStackSize;
                  creature = Infusion.spawnCreature(this.field_70170_p, EntitySpirit.class, (int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v, (EntityLivingBase)null, 0, 0, ParticleEffect.INSTANT_SPELL, (SoundEffect)null);
                  if (creature != null) {
                     spirit = (EntitySpirit)creature;
                     creature.func_110163_bv();
                     spirit.setTarget("Village", 2);
                  }

                  if (remainingStackSize > 0) {
                     EntityUtil.spawnEntityInWorld(this.field_70170_p, new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, Witchery.Items.GENERIC.itemSubduedSpirit.createStack(remainingStackSize)));
                  }

                  ParticleEffect.LARGE_EXPLODE.send(SoundEffect.RANDOM_POP, this, 1.0D, 1.0D, 16);
                  isInnerTinyBlockCircle(this.field_70170_p, center.x, center.y, center.z, glyph, true);
                  this.func_70106_y();
               }
            }
         } else {
            glyph = Witchery.Blocks.GLYPH_OTHERWHERE;
            center = isSmallBlockCircle(this.field_70170_p, new Coord(this), glyph);
            if (center != null) {
               double R = 4.0D;
               R = 16.0D;
               ItemStack usedStone = this.func_92059_d().func_77979_a(1);
               if (this.func_92059_d().field_77994_a > 0) {
                  EntityUtil.spawnEntityInWorld(this.field_70170_p, new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, this.func_92059_d()));
               }

               this.func_70106_y();
               AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)center.x + 0.5D - 4.0D, (double)center.y + 0.5D - 4.0D, (double)center.z + 0.5D - 4.0D, (double)center.x + 0.5D + 4.0D, (double)center.y + 0.5D + 4.0D, (double)center.z + 0.5D + 4.0D);
               List<Entity> list = this.field_70170_p.func_72872_a(Entity.class, bounds);
               Iterator i$ = list.iterator();

               while(true) {
                  Entity entity;
                  do {
                     do {
                        do {
                           if (!i$.hasNext()) {
                              return;
                           }

                           entity = (Entity)i$.next();
                        } while(entity.field_70128_L);
                     } while(!(entity.func_70092_e(0.5D + (double)center.x, entity.field_70163_u, 0.5D + (double)center.z) <= 16.0D));
                  } while(!(entity instanceof EntityLivingBase) && !(entity instanceof EntityItem));

                  if (!PotionEnderInhibition.isActive(entity, 1)) {
                     ServerTickEvents.TASKS.add(new EntityItemWaystone.TeleportTask(this.field_70170_p, usedStone, entity));
                  }
               }
            }
         }
      }

   }

   private static Coord isTinyBlockCircle(World world, Coord coord, Block runeBlock) {
      int x = coord.x;
      int y = coord.y;
      int z = coord.z;
      return isInnerTinyBlockCircle(world, x, y, z, runeBlock, false) ? coord : null;
   }

   private static boolean isInnerTinyBlockCircle(World world, int x, int y, int z, Block runeBlock, boolean explode) {
      int[][] circle = new int[][]{{x, z - 1}, {x + 1, z - 1}, {x + 1, z}, {x + 1, z + 1}, {x, z + 1}, {x - 1, z + 1}, {x - 1, z}, {x - 1, z - 1}};
      int[][] arr$ = circle;
      int len$ = circle.length;

      int i$;
      int[] coord;
      for(i$ = 0; i$ < len$; ++i$) {
         coord = arr$[i$];
         if (world.func_147439_a(coord[0], y, coord[1]) != runeBlock) {
            return false;
         }
      }

      if (explode) {
         arr$ = circle;
         len$ = circle.length;

         for(i$ = 0; i$ < len$; ++i$) {
            coord = arr$[i$];
            world.func_147468_f(coord[0], y, coord[1]);
            ParticleEffect.EXPLODE.send(SoundEffect.NONE, world, 0.5D + (double)coord[0], (double)y, 0.5D + (double)coord[1], 0.5D, 0.5D, 16);
         }
      }

      return true;
   }

   private static Coord isSmallBlockCircle(World world, Coord coord, Block runeBlock) {
      int x = coord.x;
      int z = coord.z;
      int[][] circle = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
      int[][] arr$ = circle;
      int len$ = circle.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         int[] co = arr$[i$];
         if (CircleUtil.isSmallCircle(world, coord.x + co[0], coord.y, coord.z + co[1], runeBlock)) {
            return new Coord(coord.x - co[0], coord.y, coord.z - co[1]);
         }
      }

      return null;
   }

   private static class TeleportTask extends ServerTickEvents.ServerTickTask {
      ItemStack stone;
      Entity entity;

      public TeleportTask(World world, ItemStack stone, Entity entity) {
         super(world);
         this.stone = stone;
         this.entity = entity;
      }

      public boolean process() {
         if (!Witchery.Items.GENERIC.teleportToLocation(this.world, this.stone, this.entity, 0, true)) {
            ParticleEffect.SMOKE.send(SoundEffect.NOTE_SNARE, this.world, this.entity.field_70165_t, this.entity.field_70163_u, this.entity.field_70161_v, 1.0D, 1.0D, 16);
         }

         return true;
      }
   }
}
