package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.brewing.potions.PotionEnslaved;
import com.emoniph.witchery.entity.EntityBanshee;
import com.emoniph.witchery.entity.EntityPoltergeist;
import com.emoniph.witchery.entity.EntitySpectre;
import com.emoniph.witchery.entity.EntitySummonedUndead;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.infusion.infusions.InfusionOtherwhere;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ItemSpectralStone extends ItemBase {
   @SideOnly(Side.CLIENT)
   IIcon item0;
   @SideOnly(Side.CLIENT)
   IIcon item1;
   @SideOnly(Side.CLIENT)
   IIcon item2;
   @SideOnly(Side.CLIENT)
   IIcon item3;
   private static final int SPECTRE = 1;
   private static final int BANSHEE = 2;
   private static final int POLTERGEIST = 3;
   private static final int TICKS_TO_ACTIVATE = 40;

   public ItemSpectralStone() {
      this.func_77625_d(16);
      this.func_77627_a(true);
   }

   @SideOnly(Side.CLIENT)
   public void func_150895_a(Item item, CreativeTabs tab, List itemList) {
      itemList.add(new ItemStack(item, 1, 0));
      itemList.add(new ItemStack(item, 1, 17));
      itemList.add(new ItemStack(item, 1, 18));
      itemList.add(new ItemStack(item, 1, 19));
   }

   @SideOnly(Side.CLIENT)
   public void func_94581_a(IIconRegister iconRegister) {
      super.func_94581_a(iconRegister);
      this.item0 = this.field_77791_bV;
      this.item1 = iconRegister.func_94245_a(this.func_111208_A() + ".spectre");
      this.item2 = iconRegister.func_94245_a(this.func_111208_A() + ".banshee");
      this.item3 = iconRegister.func_94245_a(this.func_111208_A() + ".poltergeist");
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_77617_a(int damage) {
      switch(this.getBeingFromMeta(damage)) {
      case 0:
      default:
         return this.item0;
      case 1:
         return this.item1;
      case 2:
         return this.item2;
      case 3:
         return this.item3;
      }
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean extraTip) {
      int creature = this.getBeingFromMeta(stack.func_77960_j());
      int quantity = Math.min(this.getQuantityFromMeta(stack.func_77960_j()), 4);
      switch(creature) {
      case 1:
         list.add(String.format("%s: %d", Witchery.resource("entity.witchery.spectre.name"), quantity));
         break;
      case 2:
         list.add(String.format("%s: %d", Witchery.resource("entity.witchery.banshee.name"), quantity));
         break;
      case 3:
         list.add(String.format("%s: %d", Witchery.resource("entity.witchery.poltergeist.name"), quantity));
      }

   }

   public boolean func_77636_d(ItemStack stack) {
      return true;
   }

   public static int metaFromCreature(Class<? extends EntitySummonedUndead> creatureType, int quantity) {
      if (creatureType == EntitySpectre.class) {
         return 1 | quantity << 4;
      } else if (creatureType == EntityBanshee.class) {
         return 2 | quantity << 4;
      } else {
         return creatureType == EntityPoltergeist.class ? 3 | quantity << 4 : 0;
      }
   }

   private int getBeingFromMeta(int meta) {
      int critter = meta & 15;
      if (critter < 0 || critter > 15) {
         critter = 0;
      }

      return critter;
   }

   private int getQuantityFromMeta(int meta) {
      int quantity = meta >>> 4 & 7;
      if (quantity < 0 || quantity >= 8) {
         quantity = 0;
      }

      return quantity;
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack stack) {
      return EnumRarity.rare;
   }

   public EnumAction func_77661_b(ItemStack stack) {
      return EnumAction.bow;
   }

   public int func_77626_a(ItemStack stack) {
      return TimeUtil.secsToTicks(20);
   }

   public void onUsingTick(ItemStack stack, EntityPlayer player, int countdown) {
      World world = player.field_70170_p;
      int elapsedTicks = this.func_77626_a(stack) - countdown;
      if (elapsedTicks == 40) {
         SoundEffect.NOTE_PLING.playOnlyTo(player);
      }

   }

   public void func_77615_a(ItemStack stack, World world, EntityPlayer player, int countdown) {
      int elapsedTicks = this.func_77626_a(stack) - countdown;
      int creature = this.getBeingFromMeta(stack.func_77960_j());
      int quantity = Math.min(this.getQuantityFromMeta(stack.func_77960_j()), 3);
      if (elapsedTicks >= 40 && creature > 0 && quantity > 0) {
         MovingObjectPosition mop = InfusionOtherwhere.doCustomRayTrace(world, player, true, 16.0D);
         int[] coords = BlockUtil.getBlockCoords(world, mop, true);
         Class<? extends EntitySummonedUndead> theClass = null;
         if (coords != null) {
            switch(creature) {
            case 1:
               theClass = EntitySpectre.class;
               break;
            case 2:
               theClass = EntityBanshee.class;
               break;
            case 3:
               theClass = EntityPoltergeist.class;
               break;
            default:
               SoundEffect.NOTE_SNARE.playOnlyTo(player);
               return;
            }

            for(int i = 0; i < quantity; ++i) {
               EntitySummonedUndead entity = (EntitySummonedUndead)Infusion.spawnCreature(world, theClass, coords[0], coords[1], coords[2], (EntityLivingBase)null, 0, 1, ParticleEffect.INSTANT_SPELL, (SoundEffect)null);
               if (entity != null) {
                  CreatureUtil.spawnWithEgg(entity, true);
                  entity.setSummoner(player.func_70005_c_());
                  PotionEnslaved.setEnslaverForMob(entity, player);
               }
            }

            if (!player.field_71075_bZ.field_75098_d) {
               if (stack.field_77994_a > 1) {
                  ItemStack newStack = stack.func_77979_a(1);
                  newStack.func_77964_b(0);
                  if (!player.field_71071_by.func_70441_a(newStack)) {
                     if (!world.field_72995_K) {
                        world.func_72838_d(new EntityItem(world, player.field_70165_t + 0.5D, player.field_70163_u + 1.5D, player.field_70161_v + 0.5D, newStack));
                     }
                  } else if (player instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                  }
               } else {
                  stack.func_77964_b(0);
               }
            }
         } else {
            SoundEffect.NOTE_SNARE.playOnlyTo(player, 1.0F, 1.0F);
         }
      } else {
         SoundEffect.NOTE_SNARE.playOnlyTo(player, 1.0F, 1.0F);
      }

   }

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      player.func_71008_a(stack, this.func_77626_a(stack));
      return stack;
   }
}
