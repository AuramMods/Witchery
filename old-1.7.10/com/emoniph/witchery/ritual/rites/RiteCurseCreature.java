package com.emoniph.witchery.ritual.rites;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockAreaMarker;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.entity.EntityWitchHunter;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.item.ItemHunterClothes;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class RiteCurseCreature extends Rite {
   private final boolean curse;
   private final int level;
   private final String curseType;

   public RiteCurseCreature(boolean curse, String curseType, int level) {
      this.curse = curse;
      this.level = level;
      this.curseType = curseType;
   }

   public void addSteps(ArrayList<RitualStep> steps, int intialStage) {
      steps.add(new RiteCurseCreature.StepCurseCreature(this));
   }

   private static class StepCurseCreature extends RitualStep {
      private final RiteCurseCreature rite;
      private static final int CURSE_MASTER_BONUS_LEVELS = 1;

      public StepCurseCreature(RiteCurseCreature rite) {
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
               int levelBuff = curseMasterPlayer != null && Familiar.hasActiveCurseMasteryFamiliar(curseMasterPlayer) ? 1 : 0;
               if (ritual.covenSize == 6) {
                  levelBuff += 2;
               } else if (ritual.covenSize >= 3) {
                  ++levelBuff;
               }

               Iterator i$ = ritual.sacrificedItems.iterator();

               while(i$.hasNext()) {
                  RitualStep.SacrificedItem item = (RitualStep.SacrificedItem)i$.next();
                  if (item.itemstack.func_77973_b() == Witchery.Items.TAGLOCK_KIT && item.itemstack.func_77960_j() == 1) {
                     EntityLivingBase entity = Witchery.Items.TAGLOCK_KIT.getBoundEntity(world, (Entity)null, item.itemstack, 1);
                     if (entity == null) {
                        break;
                     }

                     NBTTagCompound nbtTag = entity instanceof EntityPlayer ? Infusion.getNBT(entity) : entity.getEntityData();
                     if (nbtTag == null) {
                        break;
                     }

                     int currentLevel = nbtTag.func_74764_b(this.rite.curseType) ? nbtTag.func_74762_e(this.rite.curseType) : 0;
                     if (!this.rite.curse) {
                        int newLevel = 0;
                        if (currentLevel > 0) {
                           if (this.rite.level + levelBuff > currentLevel) {
                              newLevel = world.field_73012_v.nextInt(20) == 0 ? currentLevel + 1 : 0;
                           } else if (this.rite.level + levelBuff < currentLevel) {
                              newLevel = world.field_73012_v.nextInt(4) == 0 ? 0 : currentLevel + 1;
                           } else {
                              newLevel = world.field_73012_v.nextInt(4) == 0 ? currentLevel + 1 : 0;
                           }
                        }

                        if (newLevel == 0) {
                           if (nbtTag.func_74764_b(this.rite.curseType)) {
                              nbtTag.func_82580_o(this.rite.curseType);
                           }

                           if (entity.func_70644_a(Potion.field_76436_u)) {
                              entity.func_82170_o(Potion.field_76436_u.field_76415_H);
                           }

                           if (entity.func_70644_a(Potion.field_76437_t)) {
                              entity.func_82170_o(Potion.field_76437_t.field_76415_H);
                           }

                           if (entity.func_70644_a(Potion.field_76440_q)) {
                              entity.func_82170_o(Potion.field_76440_q.field_76415_H);
                           }

                           if (entity.func_70644_a(Potion.field_76419_f)) {
                              entity.func_82170_o(Potion.field_76419_f.field_76415_H);
                           }

                           if (entity.func_70644_a(Potion.field_76421_d)) {
                              entity.func_82170_o(Potion.field_76421_d.field_76415_H);
                           }
                        } else {
                           nbtTag.func_74768_a(this.rite.curseType, newLevel);
                           cursed = true;
                        }

                        if (entity instanceof EntityPlayer) {
                           Infusion.syncPlayer(entity.field_70170_p, (EntityPlayer)entity);
                        }

                        complete = true;
                        break;
                     }

                     EntityWitchHunter.blackMagicPerformed(curseMasterPlayer);
                     boolean isImmune = ItemHunterClothes.isCurseProtectionActive(entity) && (this.rite.curseType.equals("witcheryCursed") || this.rite.curseType.equals("witcheryWakingNightmare"));
                     if (!isImmune) {
                        isImmune = BlockAreaMarker.AreaMarkerRegistry.instance().isProtectionActive(entity, this.rite);
                     }

                     if (!isImmune && !Witchery.Items.POPPET.voodooProtectionActivated(curseMasterPlayer, (ItemStack)null, entity, levelBuff > 0 ? 3 : 1)) {
                        nbtTag.func_74768_a(this.rite.curseType, Math.max(this.rite.level + levelBuff, currentLevel));
                        cursed = true;
                        if (entity instanceof EntityPlayer) {
                           Infusion.syncPlayer(entity.field_70170_p, (EntityPlayer)entity);
                        }
                     }

                     if (isImmune) {
                        if (curseMasterPlayer != null) {
                           ChatUtil.sendTranslated(EnumChatFormatting.RED, curseMasterPlayer, "witchery.rite.blackmagicdampening");
                        }
                     } else {
                        complete = true;
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
