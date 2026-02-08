package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockAreaMarker;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.entity.EntityVillagerWere;
import com.emoniph.witchery.entity.EntityWitchHunter;
import com.emoniph.witchery.entity.EntityWolfman;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.item.ItemHunterClothes;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class RiteCurseOfTheWolf extends Rite {
   private final boolean curse;

   public RiteCurseOfTheWolf(boolean curse) {
      this.curse = curse;
   }

   public void addSteps(ArrayList<RitualStep> steps, int intialStage) {
      steps.add(new RiteCurseOfTheWolf.StepCurseCreature(this));
   }

   private static class StepCurseCreature extends RitualStep {
      private final RiteCurseOfTheWolf rite;

      public StepCurseCreature(RiteCurseOfTheWolf rite) {
         super(false);
         this.rite = rite;
      }

      public RitualStep.Result process(World world, int posX, int posY, int posZ, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 20L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            if (!world.field_72995_K) {
               boolean complete = false;
               boolean cursed = false;
               EntityPlayer curseMasterPlayer = ritual.getInitiatingPlayer(world);
               if (!CreatureUtil.isFullMoon(world)) {
                  ChatUtil.sendTranslated(EnumChatFormatting.RED, curseMasterPlayer, "witchery.rite.wolfcurse.requiresfullmoon");
                  return RitualStep.Result.ABORTED_REFUND;
               }

               if (!Familiar.hasActiveCurseMasteryFamiliar(curseMasterPlayer)) {
                  ChatUtil.sendTranslated(EnumChatFormatting.RED, curseMasterPlayer, "witchery.rite.wolfcurse.requirescat");
                  return RitualStep.Result.ABORTED_REFUND;
               }

               if (ritual.covenSize < 6) {
                  ChatUtil.sendTranslated(EnumChatFormatting.RED, curseMasterPlayer, "witchery.rite.wolfcurse.requiresfullcoven");
                  return RitualStep.Result.ABORTED_REFUND;
               }

               Iterator i$ = ritual.sacrificedItems.iterator();

               while(i$.hasNext()) {
                  RitualStep.SacrificedItem item = (RitualStep.SacrificedItem)i$.next();
                  if (item.itemstack.func_77973_b() == Witchery.Items.TAGLOCK_KIT && item.itemstack.func_77960_j() == 1) {
                     EntityLivingBase entity = Witchery.Items.TAGLOCK_KIT.getBoundEntity(world, (Entity)null, item.itemstack, 1);
                     if (entity == null) {
                        break;
                     }

                     if (this.rite.curse) {
                        EntityWitchHunter.blackMagicPerformed(curseMasterPlayer);
                        boolean isImmune = ItemHunterClothes.isCurseProtectionActive(entity);
                        if (!isImmune) {
                           isImmune = BlockAreaMarker.AreaMarkerRegistry.instance().isProtectionActive(entity, this.rite);
                        }

                        if (!isImmune && !Witchery.Items.POPPET.voodooProtectionActivated(curseMasterPlayer, (ItemStack)null, entity, 3)) {
                           if (entity instanceof EntityPlayer) {
                              EntityPlayer player = (EntityPlayer)entity;
                              ExtendedPlayer playerEx = ExtendedPlayer.get(player);
                              if (!Config.instance().allowVampireWolfHybrids && playerEx.isVampire()) {
                                 ChatUtil.sendTranslated(EnumChatFormatting.RED, curseMasterPlayer, "witchery.rite.wolfcurse.hybridsnotallow");
                              } else if (playerEx.getWerewolfLevel() == 0) {
                                 playerEx.setWerewolfLevel(1);
                                 ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, player, "witchery.werewolf.infection");
                                 complete = true;
                                 cursed = true;
                              } else {
                                 ChatUtil.sendTranslated(EnumChatFormatting.RED, curseMasterPlayer, "witchery.rite.wolfcurse.alreadyactive");
                              }
                           } else if (entity instanceof EntityVillager && !(entity instanceof EntityVillagerWere)) {
                              EntityVillager villager = (EntityVillager)entity;
                              EntityWolfman.convertToCuredVillager(villager, villager.func_70946_n(), villager.field_70956_bz, villager.field_70963_i);
                              complete = true;
                              cursed = true;
                           } else {
                              ChatUtil.sendTranslated(EnumChatFormatting.RED, curseMasterPlayer, "witchery.rite.wolfcurse.nothuman");
                           }
                        }

                        if (isImmune && curseMasterPlayer != null) {
                           ChatUtil.sendTranslated(EnumChatFormatting.RED, curseMasterPlayer, "witchery.rite.blackmagicdampening");
                        }
                        break;
                     }

                     if (entity instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer)entity;
                        ExtendedPlayer playerEx = ExtendedPlayer.get(player);
                        if (playerEx.getWerewolfLevel() > 0) {
                           double MAX_RANGE_SQ = 64.0D;
                           if (playerEx.getWerewolfLevel() != 1 && !(player.func_70092_e(0.5D + (double)posX, 0.5D + (double)posY, 0.5D + (double)posZ) <= 64.0D)) {
                              ChatUtil.sendTranslated(EnumChatFormatting.RED, curseMasterPlayer, "witchery.rite.wolfcurse.toofar");
                              break;
                           }

                           if (world.field_73012_v.nextInt(4) != 0) {
                              playerEx.setWerewolfLevel(0);
                           } else {
                              cursed = true;
                           }

                           complete = true;
                           break;
                        }

                        ChatUtil.sendTranslated(EnumChatFormatting.RED, curseMasterPlayer, "witchery.rite.wolfcurse.notactive");
                        break;
                     }

                     if (entity instanceof EntityVillagerWere) {
                        EntityVillagerWere villager = (EntityVillagerWere)entity;
                        EntityWolfman.convertToCuredVillager(villager, villager.func_70946_n(), villager.field_70956_bz, villager.field_70963_i);
                        complete = true;
                     } else if (entity instanceof EntityWolfman) {
                        EntityWolfman villager = (EntityWolfman)entity;
                        EntityWolfman.convertToCuredVillager(villager, villager.getFormerProfession(), villager.getWealth(), villager.getBuyingList());
                        complete = true;
                     } else {
                        ChatUtil.sendTranslated(EnumChatFormatting.RED, curseMasterPlayer, "witchery.rite.wolfcurse.notactive");
                     }
                     break;
                  }
               }

               if (!complete) {
                  return RitualStep.Result.ABORTED_REFUND;
               }

               if (cursed) {
                  ParticleEffect.FLAME.send(SoundEffect.MOB_ENDERDRAGON_GROWL, world, 0.5D + (double)posX, 0.1D + (double)posY, 0.5D + (double)posZ, 1.0D, 2.0D, 16);
               } else {
                  ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_LEVELUP, world, 0.5D + (double)posX, 0.1D + (double)posY, 0.5D + (double)posZ, 1.0D, 2.0D, 16);
               }
            }

            return RitualStep.Result.COMPLETED;
         }
      }
   }
}
