package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockPlacedItem;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.infusion.infusions.symbols.EffectRegistry;
import com.emoniph.witchery.infusion.infusions.symbols.SymbolEffect;
import com.emoniph.witchery.network.PacketSpellPrepared;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemMysticBranch extends ItemBase {
   private static final float THRESHOLD_ORTHOGONAL = 7.0F;
   private static final int MAX_STROKES = 15;

   public ItemMysticBranch() {
      this.func_77625_d(1);
      this.func_77664_n();
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack stack) {
      return EnumRarity.rare;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_77662_d() {
      return true;
   }

   public EnumAction func_77661_b(ItemStack stack) {
      return EnumAction.block;
   }

   public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
      return super.onDroppedByPlayer(item, player);
   }

   public int func_77626_a(ItemStack stack) {
      return 36000;
   }

   public boolean hasEffect(ItemStack par1ItemStack, int pass) {
      return true;
   }

   public void func_77663_a(ItemStack stack, World world, Entity entity, int invSlot, boolean isHeld) {
   }

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      NBTTagCompound nbtTag = player.getEntityData();
      if (!player.field_70170_p.field_72995_K) {
         nbtTag.func_82580_o("WITCSpellEffectID");
         nbtTag.func_82580_o("WITCSpellEffectEnhanced");
      }

      nbtTag.func_74773_a("Strokes", new byte[0]);
      nbtTag.func_74776_a("startPitch", player.field_70125_A);
      nbtTag.func_74776_a("startYaw", player.field_70759_as);
      player.func_71008_a(stack, this.func_77626_a(stack));
      return stack;
   }

   public boolean func_77648_a(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
      if (world.func_147439_a(x, y, z) == Witchery.Blocks.ALTAR && side == 1 && world.func_147439_a(x, y + 1, z) == Blocks.field_150350_a) {
         BlockPlacedItem.placeItemInWorld(stack, player, world, x, y + 1, z);
         player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
         return !world.field_72995_K;
      } else {
         return super.func_77648_a(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
      }
   }

   public void onUsingTick(ItemStack stack, EntityPlayer player, int countdown) {
      if (player.field_70170_p.field_72995_K) {
         NBTTagCompound nbtTag = player.getEntityData();
         if (nbtTag == null) {
            return;
         }

         float yawDiff = nbtTag.func_74760_g("startYaw") - player.field_70759_as;
         float pitchDiff = nbtTag.func_74760_g("startPitch") - player.field_70125_A;
         byte[] strokes = nbtTag.func_74770_j("Strokes");
         int strokesStart = strokes.length;
         if (!EffectRegistry.instance().contains(strokes) && strokesStart <= 15) {
            if (pitchDiff >= 7.0F) {
               strokes = this.addNewStroke(nbtTag, strokes, (byte)0);
            } else if (pitchDiff <= -7.0F) {
               strokes = this.addNewStroke(nbtTag, strokes, (byte)1);
            } else if (yawDiff <= -7.0F) {
               strokes = this.addNewStroke(nbtTag, strokes, (byte)2);
            } else if (yawDiff >= 7.0F) {
               strokes = this.addNewStroke(nbtTag, strokes, (byte)3);
            }

            if (strokes.length > strokesStart) {
               nbtTag.func_74776_a("startPitch", player.field_70125_A);
               nbtTag.func_74776_a("startYaw", player.field_70759_as);
            }

            SymbolEffect effect = EffectRegistry.instance().getEffect(strokes);
            if (effect != null) {
               int level = EffectRegistry.instance().getLevel(strokes);
               Witchery.packetPipeline.sendToServer(new PacketSpellPrepared(effect, level));
            }
         }
      }

   }

   public byte[] addNewStroke(NBTTagCompound nbtTag, byte[] strokes, byte stroke) {
      byte[] newStrokes = new byte[strokes.length + 1];
      System.arraycopy(strokes, 0, newStrokes, 0, strokes.length);
      newStrokes[newStrokes.length - 1] = stroke;
      nbtTag.func_74773_a("Strokes", newStrokes);
      return newStrokes;
   }

   public void func_77615_a(ItemStack stack, World world, EntityPlayer player, int countdown) {
      NBTTagCompound nbtTag = player.getEntityData();
      if (nbtTag != null) {
         if (!world.field_72995_K) {
            int effectID = nbtTag.func_74762_e("WITCSpellEffectID");
            int level = 1;
            if (nbtTag.func_74764_b("WITCSpellEffectEnhanced")) {
               level = nbtTag.func_74762_e("WITCSpellEffectEnhanced");
               nbtTag.func_82580_o("WITCSpellEffectEnhanced");
            }

            nbtTag.func_82580_o("WITCSpellEffectID");
            SymbolEffect effect = EffectRegistry.instance().getEffect(effectID);
            NBTTagCompound nbtPerm = Infusion.getNBT(player);
            if (effect != null) {
               if (!player.field_71075_bZ.field_75098_d && (nbtPerm == null || !nbtPerm.func_74764_b("witcheryInfusionID") || !nbtPerm.func_74764_b("witcheryInfusionCharges"))) {
                  ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.infuse.branch.infusionrequired");
                  SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
               } else if (effect.hasValidInfusion(player, nbtPerm.func_74762_e("witcheryInfusionID"))) {
                  if (effect.hasValidKnowledge(player, nbtPerm)) {
                     long ticksRemaining = effect.cooldownRemaining(player, nbtPerm);
                     if (ticksRemaining > 0L && !player.field_71075_bZ.field_75098_d) {
                        ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.infuse.branch.effectoncooldown", Long.valueOf(TimeUtil.ticksToSecs(ticksRemaining)).toString());
                        SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
                     } else {
                        if (level > 1) {
                           int newLevel = 1;
                           if (player.func_70644_a(Witchery.Potions.WORSHIP)) {
                              PotionEffect potion = player.func_70660_b(Witchery.Potions.WORSHIP);
                              if (level <= potion.func_76458_c() + 2) {
                                 newLevel = level;
                              }
                           }

                           level = newLevel;
                        }

                        if (!player.field_71075_bZ.field_75098_d && nbtPerm.func_74762_e("witcheryInfusionCharges") < effect.getChargeCost(world, player, level)) {
                           ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.infuse.branch.nocharges");
                           SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
                        } else {
                           effect.perform(world, player, level);
                           if (!player.field_71075_bZ.field_75098_d) {
                              Infusion.setCurrentEnergy(player, nbtPerm.func_74762_e("witcheryInfusionCharges") - effect.getChargeCost(world, player, level));
                           }
                        }
                     }
                  } else {
                     ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.infuse.branch.unknowneffect");
                     SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
                  }
               } else {
                  ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.infuse.branch.infernalrequired");
                  SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
               }
            } else {
               ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.infuse.branch.unknownsymbol");
               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }
         } else {
            nbtTag.func_82580_o("Strokes");
            nbtTag.func_82580_o("startYaw");
            nbtTag.func_82580_o("startPitch");
         }
      }

   }
}
