package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;

public class RitePriorIncarnation extends Rite {
   private static final String PRIOR_INV_KEY = "WITCPriIncInv";
   private static final String PRIOR_USR_KEY = "WITCPriIncUsr";
   private static final String PRIOR_LOC_KEY = "WITCPriIncLoc";
   private final int radius;
   private final int aoe;

   public static boolean isRiteAllowed() {
      return Config.instance().allowDeathItemRecoveryRite && !Witchery.isDeathChestModInstalled;
   }

   public RitePriorIncarnation(int radius, int aoe) {
      this.radius = radius;
      this.aoe = aoe;
   }

   public void addSteps(ArrayList<RitualStep> steps, int initialStage) {
      steps.add(new RitePriorIncarnation.StepPriorIncarnation(this, initialStage));
   }

   private static class StepPriorIncarnation extends RitualStep {
      private final RitePriorIncarnation rite;
      private int stage = 0;

      public StepPriorIncarnation(RitePriorIncarnation rite, int initialStage) {
         super(false);
         this.rite = rite;
         this.stage = initialStage;
      }

      public int getCurrentStage() {
         return this.stage;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (RitePriorIncarnation.isRiteAllowed() && !world.func_82736_K().func_82766_b("keepInventory")) {
            if (this.stage == 0 && ticks % 20L != 0L) {
               return RitualStep.Result.STARTING;
            } else if (!world.field_72995_K) {
               int r = this.rite.radius;
               AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)(posX - r), (double)posY, (double)(posZ - r), (double)(posX + r), (double)(posY + 1), (double)(posZ + r));
               boolean found = false;
               Iterator i$ = world.func_72872_a(EntityPlayer.class, bounds).iterator();

               while(true) {
                  EntityPlayer player;
                  NBTTagCompound nbt;
                  NBTTagList tagList;
                  double dSq;
                  do {
                     do {
                        do {
                           do {
                              do {
                                 do {
                                    do {
                                       if (!i$.hasNext()) {
                                          if (found) {
                                             ParticleEffect.HUGE_EXPLOSION.send(SoundEffect.RANDOM_FIZZ, world, (double)posX, (double)posY, (double)posZ, 3.0D, 3.0D, 16);
                                          } else {
                                             ParticleEffect.SMOKE.send(SoundEffect.NOTE_SNARE, world, (double)posX, (double)posY, (double)posZ, 1.0D, 2.0D, 16);
                                          }

                                          return RitualStep.Result.COMPLETED;
                                       }

                                       Object obj = i$.next();
                                       player = (EntityPlayer)obj;
                                    } while(!(Coord.distance(player.field_70165_t, player.field_70163_u, player.field_70161_v, (double)posX, (double)posY, (double)posZ) <= (double)r));

                                    nbt = Infusion.getNBT(player);
                                    if (Config.instance().traceRites()) {
                                       Log.instance().debug(String.format("Prior invocation for %s", player.func_70005_c_()));
                                    }
                                 } while(!nbt.func_74764_b("WITCPriIncInv"));
                              } while(!nbt.func_74764_b("WITCPriIncLocX"));
                           } while(!nbt.func_74764_b("WITCPriIncLocY"));
                        } while(!nbt.func_74764_b("WITCPriIncLocZ"));

                        tagList = nbt.func_150295_c("WITCPriIncInv", 10);
                        double x = nbt.func_74769_h("WITCPriIncLocX");
                        double y = nbt.func_74769_h("WITCPriIncLocY");
                        double z = nbt.func_74769_h("WITCPriIncLocZ");
                        dSq = Coord.distanceSq((double)posX, (double)posY, (double)posZ, x, y, z);
                        if (Config.instance().traceRites()) {
                           Log.instance().debug(String.format("Distance to death %f items %d", Math.sqrt(dSq), tagList.func_74745_c()));
                        }
                     } while(!(dSq <= (double)(this.rite.aoe * this.rite.aoe)));
                  } while(tagList.func_74745_c() <= 0);

                  if (Config.instance().traceRites()) {
                     Log.instance().debug(String.format("Recovering %d items", tagList.func_74745_c()));
                  }

                  for(int i = 0; i < tagList.func_74745_c(); ++i) {
                     NBTBase baseTag = tagList.func_150305_b(i);
                     if (baseTag != null && baseTag instanceof NBTTagCompound) {
                        NBTTagCompound tag = (NBTTagCompound)baseTag;
                        ItemStack stack = ItemStack.func_77949_a(tag);
                        if (stack != null) {
                           if (Config.instance().traceRites()) {
                              Log.instance().debug(String.format(" - Recovered %s", stack.toString()));
                           }

                           world.func_72838_d(new EntityItem(world, (double)posX, (double)posY, (double)posZ, stack));
                        } else {
                           Log.instance().warning("Prior Incarnation stack is null");
                        }
                     } else {
                        Log.instance().warning("Prior Incarnation item has incorrect NBT type or is null " + baseTag);
                     }
                  }

                  EntitySkeleton skeleton = new EntitySkeleton(world);
                  skeleton.func_70012_b((double)posX, (double)posY, (double)posZ, 0.0F, 0.0F);
                  skeleton.func_94058_c(player.func_70005_c_());
                  world.func_72838_d(skeleton);
                  nbt.func_82580_o("WITCPriIncInv");
                  nbt.func_82580_o("WITCPriIncLocX");
                  nbt.func_82580_o("WITCPriIncLocY");
                  nbt.func_82580_o("WITCPriIncLocZ");
                  found = true;
               }
            } else {
               return RitualStep.Result.COMPLETED;
            }
         } else {
            EntityPlayer player = ritual.getInitiatingPlayer(world);
            if (player != null) {
               ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.disabled");
            }

            return RitualStep.Result.ABORTED_REFUND;
         }
      }
   }

   public static class EventHooks {
      @SubscribeEvent
      public void onItemExpire(ItemExpireEvent event) {
         if (event.entityItem != null && !event.entityItem.field_70170_p.field_72995_K && RitePriorIncarnation.isRiteAllowed() && !event.isCanceled()) {
            ItemStack stack = event.entityItem.func_92059_d();
            NBTTagCompound nbtItem = stack.func_77978_p();
            if (nbtItem != null && nbtItem.func_74764_b("WITCPriIncUsr")) {
               String username = nbtItem.func_74779_i("WITCPriIncUsr");
               if (username != null && !username.isEmpty()) {
                  MinecraftServer server = MinecraftServer.func_71276_C();
                  WorldServer[] arr$ = server.field_71305_c;
                  int len$ = arr$.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     WorldServer world = arr$[i$];
                     EntityPlayer player = world.func_72924_a(username);
                     if (player != null) {
                        if (Config.instance().traceRites()) {
                           Log.instance().debug(String.format("Saving stack %s for player %s", stack.toString(), player.func_70005_c_()));
                        }

                        NBTTagCompound nbt = Infusion.getNBT(player);
                        NBTTagList list;
                        if (!nbt.func_74764_b("WITCPriIncInv")) {
                           list = new NBTTagList();
                           nbt.func_74782_a("WITCPriIncInv", list);
                        }

                        list = nbt.func_150295_c("WITCPriIncInv", 10);
                        NBTTagCompound tagCompound = new NBTTagCompound();
                        nbtItem.func_82580_o("WITCPriIncUsr");
                        if (nbtItem.func_82582_d()) {
                           stack.func_77982_d((NBTTagCompound)null);
                        }

                        stack.func_77955_b(tagCompound);
                        list.func_74742_a(tagCompound);
                        break;
                     }
                  }
               }
            }
         }

      }

      @SubscribeEvent
      public void onEntityItemPickup(EntityItemPickupEvent event) {
         if (!event.item.field_70170_p.field_72995_K && RitePriorIncarnation.isRiteAllowed() && !event.isCanceled()) {
            ItemStack stack = event.item.func_92059_d();
            removePriorUserTag(stack);
         }

      }

      public static void removePriorUserTag(ItemStack stack) {
         if (stack != null) {
            NBTTagCompound nbtItem = stack.func_77978_p();
            if (nbtItem != null && nbtItem.func_74764_b("WITCPriIncUsr")) {
               if (Config.instance().traceRites()) {
                  Log.instance().debug(String.format("removing prio incarnation tag for player %s", nbtItem.func_74779_i("WITCPriIncUsr")));
               }

               nbtItem.func_82580_o("WITCPriIncUsr");
               if (nbtItem.func_82582_d()) {
                  stack.func_77982_d((NBTTagCompound)null);
               }
            }
         }

      }

      @SubscribeEvent
      public void onPlayerDrops(PlayerDropsEvent event) {
         if (event.entityPlayer != null && !event.entityPlayer.field_70170_p.field_72995_K && event.entityPlayer.func_70644_a(Witchery.Potions.KEEP_INVENTORY)) {
            event.setCanceled(true);
         } else {
            if (event.entityPlayer != null && !event.entityPlayer.field_70170_p.field_72995_K && RitePriorIncarnation.isRiteAllowed() && !event.isCanceled()) {
               if (event.entityPlayer.field_70170_p.func_82736_K().func_82766_b("keepInventory")) {
                  return;
               }

               ArrayList<EntityItem> drops = event.drops;
               if (drops != null && drops.size() > 0) {
                  EntityPlayer player = event.entityPlayer;
                  World world = player.field_70170_p;

                  for(int i = 0; i < drops.size(); ++i) {
                     ItemStack stack = ((EntityItem)drops.get(i)).func_92059_d();
                     if (stack != null) {
                        NBTTagCompound nbt = stack.func_77978_p();
                        if (nbt == null) {
                           nbt = new NBTTagCompound();
                           stack.func_77982_d(nbt);
                        }

                        if (Config.instance().traceRites()) {
                           Log.instance().debug(String.format("Tagging stack %s for player %s", stack.toString(), player.func_70005_c_()));
                        }

                        nbt.func_74778_a("WITCPriIncUsr", player.func_70005_c_());
                     }
                  }

                  NBTTagCompound nbt = Infusion.getNBT(player);
                  if (nbt.func_74764_b("WITCPriIncInv")) {
                     nbt.func_82580_o("WITCPriIncInv");
                  }

                  nbt.func_74780_a("WITCPriIncLocX", player.field_70165_t);
                  nbt.func_74780_a("WITCPriIncLocY", player.field_70163_u);
                  nbt.func_74780_a("WITCPriIncLocZ", player.field_70161_v);
               }
            }

         }
      }
   }
}
