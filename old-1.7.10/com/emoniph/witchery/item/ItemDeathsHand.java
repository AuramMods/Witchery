package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class ItemDeathsHand extends ItemBase {
   public ItemDeathsHand() {
      this.func_77625_d(1);
      this.func_77664_n();
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack stack) {
      return EnumRarity.epic;
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean advancedTooltips) {
      String localText = Witchery.resource(this.func_77658_a() + ".tip");
      if (localText != null) {
         String[] arr$ = localText.split("\n");
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String s = arr$[i$];
            if (!s.isEmpty()) {
               list.add(s);
            }
         }
      }

   }

   public void func_77663_a(ItemStack stack, World world, Entity entity, int inventorySlot, boolean isHeldItem) {
      if (entity instanceof EntityPlayer && !world.field_72995_K) {
         EntityPlayer player = (EntityPlayer)entity;
         if (this.isDeployed(stack) && TimeUtil.secondsElapsed(1, world.func_72820_D())) {
            if (!ItemDeathsClothes.isFullSetWorn(player)) {
               this.setDeployed(player, stack, false);
            } else {
               int level = player.func_71024_bL().func_75116_a();
               if (level > 0) {
                  player.func_71024_bL().func_75122_a(level == 1 ? -1 : -2, 0.0F);
               }
            }
         }
      }

   }

   public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity otherEntity) {
      if (!player.field_70170_p.field_72995_K && otherEntity instanceof EntityLivingBase) {
         EntityLivingBase victim = (EntityLivingBase)otherEntity;
         float MAX_DAMAGE = 15.0F;
         float DAMAGE_PERCENTAGE = 0.1F;
         boolean deployed = this.isDeployed(stack);
         float damagePct = 0.1F;
         float minDamage = 2.0F;
         int hungerRestore = 0;
         int healthRestore = 0;
         if (deployed) {
            int hunger = player.func_71024_bL().func_75116_a();
            if (hunger == 0) {
               damagePct = 0.5F;
               minDamage = 4.0F;
               hungerRestore = 10;
               healthRestore = 3;
            } else if (hunger <= 4) {
               damagePct = 0.25F;
               minDamage = 4.0F;
               hungerRestore = 3;
               healthRestore = 2;
            } else if (hunger <= 10) {
               damagePct = 0.2F;
               minDamage = 3.0F;
               hungerRestore = 2;
               healthRestore = 1;
            } else if (hunger <= 20) {
               damagePct = 0.15F;
               minDamage = 3.0F;
               hungerRestore = 1;
            } else {
               damagePct = 0.15F;
               minDamage = 3.0F;
            }
         }

         if (deployed) {
            double r = 1.5D;
            AxisAlignedBB bb = AxisAlignedBB.func_72330_a(victim.field_70165_t - 1.5D, victim.field_70121_D.field_72338_b, victim.field_70161_v - 1.5D, victim.field_70165_t + 1.5D, victim.field_70121_D.field_72337_e, victim.field_70161_v + 1.5D);
            List entities = player.field_70170_p.func_72872_a(EntityLivingBase.class, bb);
            Iterator i$ = entities.iterator();

            while(i$.hasNext()) {
               Object obj = i$.next();
               EntityLivingBase hitEntity = (EntityLivingBase)obj;
               if (hitEntity != player) {
                  float maxHealth = Math.min(hitEntity.func_110138_aP(), 20.0F);
                  float damage = Math.min(Math.max(maxHealth * damagePct, minDamage), 15.0F);
                  boolean flag = EntityUtil.touchOfDeath(hitEntity, player, damage);
                  if (flag) {
                     if (hungerRestore > 0) {
                        player.func_71024_bL().func_75122_a(hungerRestore, 0.0F);
                     }

                     if (healthRestore > 0) {
                        player.func_70691_i((float)healthRestore);
                     }
                  }
               }
            }
         } else {
            float maxHealth = Math.min(victim.func_110138_aP(), 20.0F);
            float damage = Math.min(Math.max(maxHealth * damagePct, minDamage), 15.0F);
            EntityUtil.touchOfDeath(victim, player, damage);
         }
      }

      return true;
   }

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      if (!world.field_72995_K && ItemDeathsClothes.isFullSetWorn(player)) {
         if (!stack.func_77942_o()) {
            stack.func_77982_d(new NBTTagCompound());
         }

         NBTTagCompound nbtItem = stack.func_77978_p();
         boolean deployed = !this.isDeployed(nbtItem);
         this.setDeployed(player, stack, nbtItem, deployed);
         if (deployed) {
            ParticleEffect.MOB_SPELL.send(SoundEffect.MOB_ENDERDRAGON_GROWL, player, 1.0D, 2.0D, 16);
         }
      }

      return stack;
   }

   private void setDeployed(EntityPlayer player, ItemStack stack, boolean deployed) {
      this.setDeployed(player, stack, stack.func_77978_p(), deployed);
   }

   private void setDeployed(EntityPlayer player, ItemStack stack, NBTTagCompound nbtItem, boolean deployed) {
      if (player != null && !player.field_70170_p.field_72995_K && nbtItem != null) {
         nbtItem.func_74757_a("WITCScytheDeployed", deployed);
         if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
         }
      }

   }

   public boolean isDeployed(EntityLivingBase player) {
      ItemStack heldItem = player.func_70694_bm();
      return heldItem != null && heldItem.func_77973_b() == this ? this.isDeployed(heldItem) : false;
   }

   private boolean isDeployed(ItemStack stack) {
      return this.isDeployed(stack.func_77978_p());
   }

   private boolean isDeployed(NBTTagCompound nbtItem) {
      boolean deployed = nbtItem != null && nbtItem.func_74767_n("WITCScytheDeployed");
      return deployed;
   }
}
