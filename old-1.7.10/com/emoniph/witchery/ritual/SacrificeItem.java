package com.emoniph.witchery.ritual;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.blocks.BlockGrassper;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Const;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class SacrificeItem extends Sacrifice {
   final ItemStack[] itemstacks;

   public SacrificeItem(ItemStack... itemstacks) {
      this.itemstacks = itemstacks;
   }

   public void addDescription(StringBuffer sb) {
      ItemStack[] arr$ = this.itemstacks;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ItemStack itemstack = arr$[i$];
         sb.append("§8>§0 ");
         if (itemstack.func_77973_b() == Items.field_151068_bn) {
            List list = Items.field_151068_bn.func_77832_l(itemstack);
            if (list != null && !list.isEmpty()) {
               PotionEffect effect = (PotionEffect)list.get(0);
               String s = itemstack.func_82833_r();
               if (effect.func_76458_c() > 0) {
                  s = s + " " + StatCollector.func_74838_a("potion.potency." + effect.func_76458_c()).trim();
               }

               if (effect.func_76459_b() > 20) {
                  s = s + " (" + Potion.func_76389_a(effect) + ")";
               }

               sb.append(s);
            } else {
               sb.append(itemstack.func_82833_r());
            }
         } else {
            sb.append(itemstack.func_82833_r());
         }

         sb.append(Const.BOOK_NEWLINE);
      }

   }

   public boolean isMatch(World world, int posX, int posY, int posZ, int maxDistance, ArrayList<Entity> entities, ArrayList<ItemStack> grassperStacks) {
      ArrayList<Entity> itemsToRemove = new ArrayList();
      ArrayList<ItemStack> otherItemsToRemove = new ArrayList();

      for(int j = 0; j < this.itemstacks.length; ++j) {
         boolean found = false;

         int i;
         for(i = 0; i < entities.size(); ++i) {
            if (entities.get(i) instanceof EntityItem) {
               EntityItem entity = (EntityItem)entities.get(i);
               if (isItemEqual(this.itemstacks[j], entity.func_92059_d()) && !itemsToRemove.contains(entity) && distance(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, (double)posX, (double)posY, (double)posZ) <= (double)maxDistance) {
                  itemsToRemove.add(entity);
                  found = true;
                  break;
               }
            }
         }

         for(i = 0; i < grassperStacks.size(); ++i) {
            if (isItemEqual(this.itemstacks[j], (ItemStack)grassperStacks.get(i)) && !otherItemsToRemove.contains(grassperStacks.get(i))) {
               otherItemsToRemove.add(grassperStacks.get(i));
               found = true;
               break;
            }
         }

         if (!found) {
            return false;
         }
      }

      Iterator i$ = itemsToRemove.iterator();

      while(i$.hasNext()) {
         Entity itemToRemove = (Entity)i$.next();
         entities.remove(itemToRemove);
      }

      i$ = otherItemsToRemove.iterator();

      while(i$.hasNext()) {
         ItemStack itemToRemove = (ItemStack)i$.next();
         grassperStacks.remove(itemToRemove);
      }

      return true;
   }

   public static boolean isItemEqual(ItemStack itemstackToFind, ItemStack itemstackFound) {
      return (itemstackFound.func_77973_b() == Witchery.Items.ARTHANA || itemstackFound.func_77973_b() == Witchery.Items.BOLINE) && itemstackFound.func_77973_b() == itemstackToFind.func_77973_b() || itemstackFound.func_77969_a(itemstackToFind);
   }

   public void addSteps(ArrayList<RitualStep> steps, AxisAlignedBB bounds, int maxDistance) {
      ItemStack[] arr$ = this.itemstacks;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ItemStack itemstack = arr$[i$];
         steps.add(new SacrificeItem.StepSacrificeItem(itemstack, bounds, maxDistance));
      }

   }

   protected static class StepSacrificeItem extends RitualStep {
      protected final ItemStack itemstack;
      protected final AxisAlignedBB bounds;
      protected final int maxDistance;
      protected boolean showMessages;

      public StepSacrificeItem(ItemStack itemstack, AxisAlignedBB bounds, int maxDistance) {
         super(false);
         this.itemstack = itemstack;
         this.bounds = bounds;
         this.maxDistance = maxDistance;
         this.showMessages = false;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 20L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            if (!world.field_72995_K) {
               List itemEntities = world.func_72872_a(EntityItem.class, this.bounds);
               Iterator i$;
               Object obj;
               EntityItem entity;
               ItemStack foundItemstack;
               if (Config.instance().traceRites()) {
                  i$ = itemEntities.iterator();

                  while(i$.hasNext()) {
                     obj = i$.next();
                     entity = (EntityItem)obj;
                     foundItemstack = entity.func_92059_d();
                     Log.instance().traceRite(String.format(" * found: %s, distance: %f", foundItemstack.toString(), Sacrifice.distance(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, (double)posX, (double)posY, (double)posZ)));
                  }
               }

               i$ = itemEntities.iterator();

               while(i$.hasNext()) {
                  obj = i$.next();
                  entity = (EntityItem)obj;
                  foundItemstack = entity.func_92059_d();
                  if (SacrificeItem.isItemEqual(this.itemstack, foundItemstack) && Sacrifice.distance(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, (double)posX, (double)posY, (double)posZ) <= (double)this.maxDistance) {
                     ItemStack sacrificedItemstack = ItemStack.func_77944_b(foundItemstack);
                     sacrificedItemstack.field_77994_a = 1;
                     ritual.sacrificedItems.add(new RitualStep.SacrificedItem(sacrificedItemstack, new Coord(entity)));
                     if (foundItemstack.func_77985_e() && foundItemstack.field_77994_a > 1) {
                        --foundItemstack.field_77994_a;
                     } else {
                        world.func_72900_e(entity);
                     }

                     ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_POP, entity, 0.5D, 1.0D, 16);
                     return RitualStep.Result.COMPLETED;
                  }
               }

               int radius = true;

               for(int x = posX - 5; x <= posX + 5; ++x) {
                  for(int z = posZ - 5; z <= posZ + 5; ++z) {
                     Block blockID = world.func_147439_a(x, posY, z);
                     if (blockID == Witchery.Blocks.GRASSPER) {
                        TileEntity tile = world.func_147438_o(x, posY, z);
                        if (tile != null && tile instanceof BlockGrassper.TileEntityGrassper) {
                           BlockGrassper.TileEntityGrassper grassper = (BlockGrassper.TileEntityGrassper)tile;
                           ItemStack stack = grassper.func_70301_a(0);
                           if (stack != null && SacrificeItem.isItemEqual(this.itemstack, stack)) {
                              ItemStack sacrificedItemstack = ItemStack.func_77944_b(stack);
                              sacrificedItemstack.field_77994_a = 1;
                              ritual.sacrificedItems.add(new RitualStep.SacrificedItem(sacrificedItemstack, new Coord(tile)));
                              if (stack.func_77985_e() && stack.field_77994_a > 1) {
                                 --stack.field_77994_a;
                              } else {
                                 grassper.func_70299_a(0, (ItemStack)null);
                              }

                              ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_POP, world, 0.5D + (double)x, 0.8D + (double)posY, 0.5D + (double)z, 0.5D, 1.0D, 16);
                              return RitualStep.Result.COMPLETED;
                           }
                        }
                     }
                  }
               }
            }

            if (this.showMessages) {
               RiteRegistry.RiteError("witchery.rite.missingitem", ritual.getInitiatingPlayerName(), world);
            }

            return RitualStep.Result.ABORTED_REFUND;
         }
      }

      public String toString() {
         return String.format("StepSacrificeItem: %s, maxDistance: %d", this.itemstack.toString(), this.maxDistance);
      }
   }
}
