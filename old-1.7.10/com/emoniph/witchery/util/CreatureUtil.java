package com.emoniph.witchery.util;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.entity.EntityBabaYaga;
import com.emoniph.witchery.entity.EntityBolt;
import com.emoniph.witchery.entity.EntityDemon;
import com.emoniph.witchery.entity.EntityHornedHuntsman;
import com.emoniph.witchery.entity.EntityImp;
import com.emoniph.witchery.entity.EntityLeonard;
import com.emoniph.witchery.entity.EntityLilith;
import com.emoniph.witchery.entity.EntityLordOfTorment;
import com.emoniph.witchery.entity.EntityMandrake;
import com.emoniph.witchery.entity.EntityNightmare;
import com.emoniph.witchery.entity.EntityReflection;
import com.emoniph.witchery.entity.EntitySpirit;
import com.emoniph.witchery.entity.EntityTreefyd;
import com.emoniph.witchery.entity.EntityVampire;
import com.emoniph.witchery.entity.EntityVillagerWere;
import com.emoniph.witchery.entity.EntityWolfman;
import com.emoniph.witchery.infusion.InfusedBrewEffect;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.item.ItemVampireClothes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class CreatureUtil {
   private static Class classBloodMagicDemon;

   private CreatureUtil() {
   }

   public static boolean isDemonic(Entity entity) {
      if (entity != null) {
         if (entity instanceof EntityDemon || entity instanceof EntityGhast || entity instanceof EntityBlaze || entity instanceof EntityMagmaCube || entity instanceof EntityLeonard || entity instanceof EntityLordOfTorment || entity instanceof EntityImp || entity instanceof EntityLilith || entity instanceof EntityWither) {
            EntityLiving living = (EntityLiving)entity;
            return true;
         }

         if (entity instanceof EntityPlayer) {
            return false;
         }

         if (isModDemon(entity)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isModDemon(Entity entity) {
      if (classBloodMagicDemon == null) {
         try {
            classBloodMagicDemon = Class.forName("WayofTime.alchemicalWizardry.common.entity.mob.EntityDemon");
         } catch (ClassNotFoundException var2) {
         }
      }

      return classBloodMagicDemon != null ? classBloodMagicDemon.isAssignableFrom(entity.getClass()) : false;
   }

   public static boolean isUndead(Entity entity) {
      if (entity != null) {
         if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving)entity;
            return living.func_70662_br();
         }

         if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entity;
            ExtendedPlayer playerEx = ExtendedPlayer.get(player);
            return playerEx.isVampire() || InfusedBrewEffect.getActiveBrew(player) == InfusedBrewEffect.Grave;
         }
      }

      return false;
   }

   public static boolean isInsect(EntityLivingBase entity) {
      return entity != null ? entity.func_70668_bt() == EnumCreatureAttribute.ARTHROPOD : false;
   }

   public static boolean isSpirit(EntityLivingBase entity) {
      return entity == null ? false : entity instanceof EntityMandrake || entity instanceof EntityHornedHuntsman || entity instanceof EntityTreefyd || entity instanceof EntityNightmare || entity instanceof EntitySpirit;
   }

   public static EntityLiving spawnWithEgg(EntityLiving entity, boolean requirePersistance) {
      if (entity != null) {
         entity.func_110161_a((IEntityLivingData)null);
         if (requirePersistance) {
            entity.func_110163_bv();
         }
      }

      return entity;
   }

   public static boolean isWitch(Entity entity) {
      if (entity != null) {
         if (entity instanceof EntityWitch || entity instanceof EntityBabaYaga) {
            return true;
         }

         if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entity;
            if (InvUtil.hasItem(player.field_71071_by, Witchery.Items.POPPET, Witchery.Items.POPPET.voodooPoppet.damageValue) || InvUtil.hasItem(player.field_71071_by, Witchery.Items.POPPET, Witchery.Items.POPPET.vampiricPoppet.damageValue) || Infusion.getInfusionID(player) == Witchery.Recipes.infusionBeast.infusionID) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isWoodenDamage(DamageSource source) {
      if (source.func_76364_f() != null && source.func_76364_f() instanceof EntityLivingBase) {
         EntityLivingBase living = (EntityLivingBase)source.func_76364_f();
         if (living instanceof EntityHornedHuntsman && !source.func_76352_a()) {
            return true;
         }

         ItemStack stack = living.func_71124_b(0);
         if (stack != null && stack.func_77973_b() instanceof ItemSword) {
            ItemSword sword = (ItemSword)stack.func_77973_b();
            if (sword.func_150932_j().equalsIgnoreCase(ToolMaterial.WOOD.toString())) {
               return true;
            }
         }
      }

      if (source instanceof BoltDamageSource) {
         BoltDamageSource boltDamage = (BoltDamageSource)source;
         return boltDamage.isWooden;
      } else {
         return false;
      }
   }

   public static boolean isSilverDamage(DamageSource source) {
      if (source instanceof EntityDamageSourceIndirectSilver) {
         return true;
      } else if (source.func_76364_f() != null && source.func_76364_f() instanceof EntityBolt) {
         EntityBolt bolt = (EntityBolt)source.func_76364_f();
         return bolt.isSilverDamage();
      } else {
         if (!source.func_76352_a() && source.func_76346_g() != null && source.func_76346_g() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase)source.func_76346_g();
            ItemStack stack = entity.func_70694_bm();
            if (stack != null && stack.func_77973_b() instanceof ItemSword) {
               ItemSword sword = (ItemSword)stack.func_77973_b();
               String materialName = sword.func_150932_j();
               if (materialName != null) {
                  if (materialName.equals("SILVER")) {
                     return true;
                  }

                  int colonPos = materialName.lastIndexOf(":");
                  if (colonPos >= 0 && colonPos < materialName.length()) {
                     return materialName.substring(colonPos).equals(":SILVER");
                  }
               }
            }
         }

         return false;
      }
   }

   public static boolean isWerewolf(Entity entity) {
      return isWerewolf(entity, false);
   }

   public static boolean isWerewolf(Entity entity, boolean includeUnshifted) {
      if (entity == null) {
         return false;
      } else if (entity instanceof EntityWolfman) {
         return true;
      } else if (entity instanceof EntityReflection) {
         return ((EntityReflection)entity).getModel() == 1;
      } else if (entity instanceof EntityVillagerWere) {
         return includeUnshifted;
      } else if (entity instanceof EntityPlayer) {
         ExtendedPlayer playerEx = ExtendedPlayer.get((EntityPlayer)entity);
         if (includeUnshifted && playerEx.getWerewolfLevel() > 0) {
            return true;
         } else {
            return playerEx.getCreatureType() == TransformCreature.WOLF || playerEx.getCreatureType() == TransformCreature.WOLFMAN;
         }
      } else if (!(entity instanceof EntityLiving)) {
         return false;
      } else {
         String name = entity.getClass().getSimpleName();
         return name != null && name.toUpperCase().contains("WEREWOLF");
      }
   }

   public static boolean isVampire(Entity entity) {
      if (entity == null) {
         return false;
      } else if (entity instanceof EntityVampire) {
         return true;
      } else if (entity instanceof EntityReflection) {
         return ((EntityReflection)entity).isVampire();
      } else if (entity instanceof EntityPlayer) {
         ExtendedPlayer playerEx = ExtendedPlayer.get((EntityPlayer)entity);
         return playerEx.isVampire();
      } else if (!(entity instanceof EntityLiving)) {
         return false;
      } else {
         String name = entity.getClass().getSimpleName();
         return name != null && name.toUpperCase().contains("VAMPIRE");
      }
   }

   public static boolean isFullMoon(World world) {
      return (double)world.func_130001_d() == 1.0D && !world.func_72935_r();
   }

   public static boolean isImmuneToDisease(EntityLivingBase livingEntity) {
      return isUndead(livingEntity) || isDemonic(livingEntity) || isWerewolf(livingEntity, true) || livingEntity instanceof IBossDisplayData || livingEntity instanceof EntityGolem;
   }

   public static boolean isImmuneToPoison(EntityLivingBase livingEntity) {
      return isWerewolf(livingEntity, false);
   }

   public static boolean checkForVampireDeath(EntityLivingBase creature, DamageSource source) {
      boolean dead = false;
      if (!source.func_76347_k() && !(source instanceof EntityUtil.DamageSourceVampireFire)) {
         if (source instanceof EntityUtil.DamageSourceSunlight) {
            dead = true;
         } else if (creature instanceof EntityPlayer && Witchery.modHooks.canVampireBeKilled((EntityPlayer)creature)) {
            dead = true;
         } else if (source != DamageSource.field_76368_d && source != DamageSource.field_76380_i) {
            if (source.func_76346_g() != null && (isWerewolf(source.func_76346_g()) || isVampire(source.func_76346_g()) || source.func_76346_g() instanceof IBossDisplayData)) {
               dead = true;
            } else if (isWerewolf(creature, true) && isSilverDamage(source)) {
               dead = true;
            }
         } else {
            dead = true;
         }
      } else if (ItemVampireClothes.isExtendedFlameProtectionActive(creature)) {
         dead = creature.field_70170_p.field_73012_v.nextInt(4) == 0;
      } else if (ItemVampireClothes.isFlameProtectionActive(creature)) {
         dead = creature.field_70170_p.field_73012_v.nextInt(4) != 0;
      } else {
         dead = true;
      }

      if (!dead) {
         creature.func_70606_j(1.0F);
         if (creature instanceof EntityPlayer) {
            ((EntityPlayer)creature).func_71024_bL().func_75113_a(5.0F);
         }

         if (source.func_94541_c() && creature.field_70170_p.field_73012_v.nextInt(4) == 0) {
            creature.func_70015_d(2);
         }

         return false;
      } else {
         return true;
      }
   }

   public static boolean isInSunlight(EntityLivingBase entity) {
      World world = entity.field_70170_p;
      if (world.field_73011_w.field_76574_g != Config.instance().dimensionDreamID && world.field_73011_w.field_76574_g != Config.instance().dimensionTormentID && !world.field_73011_w.field_76576_e && world.field_73011_w.func_76569_d() && world.func_72935_r()) {
         int x = MathHelper.func_76128_c(entity.field_70165_t);
         int y = MathHelper.func_76128_c(entity.field_70163_u);
         int z = MathHelper.func_76128_c(entity.field_70161_v);
         BiomeGenBase biome = world.func_72807_a(x, z);
         if (biome.field_76791_y.equals("Ominous Woods")) {
            return false;
         } else if (world.func_72896_J() && biome.func_76738_d()) {
            return false;
         } else {
            return world.func_72937_j(x, y + MathHelper.func_76143_f((double)entity.field_70131_O), z);
         }
      } else {
         return false;
      }
   }
}
