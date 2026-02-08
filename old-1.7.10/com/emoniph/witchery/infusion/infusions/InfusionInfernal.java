package com.emoniph.witchery.infusion.infusions;

import com.emoniph.witchery.brewing.potions.PotionEnslaved;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePower;
import com.emoniph.witchery.util.BlockSide;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.lang.reflect.Field;
import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class InfusionInfernal extends Infusion {
   private static final int MAX_CHARGES = 20;

   public InfusionInfernal(int infusionID) {
      super(infusionID);
   }

   public IIcon getPowerBarIcon(EntityPlayer player, int index) {
      return Blocks.field_150424_aL.func_149691_a(0, 0);
   }

   public void onLeftClickEntity(ItemStack itemstack, World world, EntityPlayer player, Entity otherEntity) {
      if (!world.field_72995_K && otherEntity instanceof EntityLivingBase) {
         EntityLivingBase entityLivingBase = (EntityLivingBase)otherEntity;
         if (player.func_70093_af()) {
            if (PotionEnslaved.canCreatureBeEnslaved(entityLivingBase)) {
               EntityLiving entityLiving = (EntityLiving)entityLivingBase;
               if (PotionEnslaved.isMobEnslavedBy(entityLiving, player)) {
                  if (this.consumeCharges(world, player, 1, true)) {
                     this.trySacrificeCreature(world, player, entityLiving);
                  }
               } else if (this.consumeCharges(world, player, 5, true)) {
                  PotionEnslaved.setEnslaverForMob(entityLiving, player);
                  EntityUtil.dropAttackTarget((EntityLiving)otherEntity);
                  ParticleEffect.SPELL.send(SoundEffect.MOB_ZOMBIE_INFECT, entityLiving, 1.0D, 2.0D, 16);
               }
            } else {
               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }
         } else {
            int r = true;
            if (this.consumeCharges(world, player, 1, true)) {
               int minionCount = 0;
               AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(player.field_70165_t - 50.0D, player.field_70163_u - 15.0D, player.field_70161_v - 50.0D, player.field_70165_t + 50.0D, player.field_70163_u + 15.0D, player.field_70161_v + 50.0D);
               Iterator i$ = world.func_72872_a(EntityLiving.class, bounds).iterator();

               while(true) {
                  EntityCreature nearbyCreatureEntity;
                  do {
                     Object obj;
                     EntityLiving nearbyLivingEntity;
                     do {
                        do {
                           if (!i$.hasNext()) {
                              if (minionCount > 0) {
                                 ParticleEffect.CRIT.send(SoundEffect.RANDOM_BREATH, entityLivingBase, 0.5D, 2.0D, 16);
                              }

                              return;
                           }

                           obj = i$.next();
                           nearbyLivingEntity = (EntityLiving)obj;
                        } while(!PotionEnslaved.isMobEnslavedBy(nearbyLivingEntity, player));

                        ++minionCount;
                        nearbyLivingEntity.func_70624_b(entityLivingBase);
                        if (nearbyLivingEntity instanceof EntityGhast) {
                           try {
                              EntityGhast ghastEntity = (EntityGhast)nearbyLivingEntity;
                              Field[] fields = EntityGhast.class.getDeclaredFields();
                              Field fieldTargetedEntity = fields[4];
                              fieldTargetedEntity.setAccessible(true);
                              fieldTargetedEntity.set(ghastEntity, entityLivingBase);
                              Field fieldAggroCooldown = fields[5];
                              fieldAggroCooldown.setAccessible(true);
                              fieldAggroCooldown.set(ghastEntity, 20000);
                           } catch (IllegalAccessException var16) {
                              Log.instance().warning(var16, "Exception occurred setting ghast target.");
                           } catch (Exception var17) {
                              Log.instance().debug(String.format("Exception occurred setting ghast target. %s", var17.toString()));
                           }
                        }
                     } while(!(nearbyLivingEntity instanceof EntityCreature));

                     nearbyCreatureEntity = (EntityCreature)obj;
                     nearbyCreatureEntity.func_70784_b(entityLivingBase);
                     nearbyCreatureEntity.func_70604_c(entityLivingBase);
                  } while(!(nearbyCreatureEntity instanceof EntityZombie) && !(nearbyCreatureEntity instanceof EntityCreeper));

                  nearbyCreatureEntity.field_70714_bg.func_75776_a(2, new EntityAIAttackOnCollide(nearbyCreatureEntity, entityLivingBase.getClass(), 1.0D, false));
               }
            }
         }
      }

   }

   private void trySacrificeCreature(World world, EntityPlayer player, EntityLiving creature) {
      CreaturePower power = CreaturePower.Registry.instance().get(creature);
      if (power != null) {
         int currentCreaturePowerID = CreaturePower.getCreaturePowerID(player);
         if (currentCreaturePowerID == power.getCreaturePowerID()) {
            int currentCharges = CreaturePower.getCreaturePowerCharges(player);
            CreaturePower.setCreaturePowerCharges(player, MathHelper.func_76128_c((double)Math.min(currentCharges + power.getChargesPerSacrifice(), 20)));
         } else {
            CreaturePower.setCreaturePowerID(player, power.getCreaturePowerID(), power.getChargesPerSacrifice());
         }

         syncPlayer(world, player);
         creature.func_70097_a(DamageSource.func_76354_b(player, (Entity)null), creature.func_110143_aJ() + 1.0F);
      } else {
         this.playFailSound(world, player);
      }

   }

   public void onHurt(World worldObj, EntityPlayer player, LivingHurtEvent event) {
      int creaturePowerID = CreaturePower.getCreaturePowerID(player);
      if (creaturePowerID > 0) {
         CreaturePower.Registry.instance().get(creaturePowerID).onDamage(player.field_70170_p, player, event);
      }

   }

   public void onFalling(World world, EntityPlayer player, LivingFallEvent event) {
      int creaturePowerID = CreaturePower.getCreaturePowerID(player);
      if (creaturePowerID > 0) {
         CreaturePower.Registry.instance().get(creaturePowerID).onFalling(world, player, event);
      }

   }

   public void onUsingItemTick(ItemStack itemstack, World world, EntityPlayer player, int countdown) {
      int elapsedTicks = this.getMaxItemUseDuration(itemstack) - countdown;
   }

   public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int countdown) {
      if (!world.field_72995_K) {
         int elapsedTicks = this.getMaxItemUseDuration(itemstack) - countdown;
         double MAX_TARGET_RANGE = 15.0D;
         MovingObjectPosition mop = InfusionOtherwhere.doCustomRayTrace(world, player, true, 15.0D);
         int minionCount;
         if (player.func_70093_af()) {
            if (mop != null) {
               switch(mop.field_72313_a) {
               case ENTITY:
                  this.playFailSound(world, player);
                  break;
               case BLOCK:
                  if (BlockSide.TOP.isEqual(mop.field_72310_e)) {
                     minionCount = 0;
                     int r = true;
                     AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(player.field_70165_t - 50.0D, player.field_70163_u - 15.0D, player.field_70161_v - 50.0D, player.field_70165_t + 50.0D, player.field_70163_u + 15.0D, player.field_70161_v + 50.0D);
                     Iterator i$ = world.func_72872_a(EntityLiving.class, bounds).iterator();

                     while(true) {
                        EntityLiving creature;
                        EntityCreature creature2;
                        do {
                           do {
                              if (!i$.hasNext()) {
                                 if (minionCount > 0) {
                                    ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_POP, world, (double)mop.field_72311_b, (double)(mop.field_72312_c + 1), (double)mop.field_72309_d, 0.5D, 2.0D, 16);
                                 }

                                 return;
                              }

                              Object obj = i$.next();
                              creature = (EntityLiving)obj;
                              creature2 = creature instanceof EntityCreature ? (EntityCreature)creature : null;
                           } while(!PotionEnslaved.isMobEnslavedBy(creature, player));

                           ++minionCount;
                           creature.func_70624_b((EntityLivingBase)null);
                           creature.func_70604_c((EntityLivingBase)null);
                           if (creature2 != null) {
                              creature2.func_70784_b((Entity)null);
                           }
                        } while(!(creature instanceof EntitySpider) && creature.func_70661_as().func_75492_a((double)mop.field_72311_b, (double)(mop.field_72312_c + 1), (double)mop.field_72309_d, 1.0D));

                        if (creature2 != null) {
                           creature2.func_70778_a(world.func_72844_a(creature, mop.field_72311_b, mop.field_72312_c + 1, mop.field_72309_d, 10.0F, true, false, false, true));
                        }
                     }
                  }
               case MISS:
               }
            } else {
               this.playFailSound(world, player);
            }
         } else {
            minionCount = CreaturePower.getCreaturePowerID(player);
            if (minionCount > 0) {
               CreaturePower power = CreaturePower.Registry.instance().get(minionCount);
               int chargesRequired = power.activateCost(world, player, elapsedTicks, mop);
               int currentCharges = CreaturePower.getCreaturePowerCharges(player);
               if (currentCharges - chargesRequired >= 0 && this.consumeCharges(world, player, 1, true)) {
                  power.onActivate(world, player, elapsedTicks, mop);
                  if (!player.field_71075_bZ.field_75098_d) {
                     CreaturePower.setCreaturePowerCharges(player, currentCharges - chargesRequired);
                     syncPlayer(world, player);
                  }
               } else {
                  this.playFailSound(world, player);
               }
            } else {
               this.playFailSound(world, player);
            }
         }
      }

   }
}
