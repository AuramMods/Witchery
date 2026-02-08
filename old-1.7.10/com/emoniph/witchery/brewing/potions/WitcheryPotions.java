package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockBeartrap;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.EntitySizeInfo;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.TransformCreature;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderLivingEvent.Post;
import net.minecraftforge.client.event.RenderLivingEvent.Pre;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

public class WitcheryPotions {
   private final List<PotionBase> allEffects = new ArrayList();
   private final List<IHandleLivingUpdate> livingUpdateEventHandlers = new ArrayList();
   private final List<IHandleLivingJump> livingJumpEventHandlers = new ArrayList();
   private final List<IHandleLivingHurt> livingHurtEventHandlers = new ArrayList();
   private final List<IHandleLivingDeath> livingDeathEventHandlers = new ArrayList();
   private final List<IHandlePlayerDrops> playerDropsEventHandlers = new ArrayList();
   private final List<IHandleRenderLiving> renderLivingEventHandlers = new ArrayList();
   private final List<IHandlePreRenderLiving> renderLivingPreEventHandlers = new ArrayList();
   private final List<IHandleEnderTeleport> enderTeleportEventHandlers = new ArrayList();
   private final List<IHandleHarvestDrops> harvestDropsEventHandlers = new ArrayList();
   private final List<IHandleLivingSetAttackTarget> livingSetAttackTargetEventHandlers = new ArrayList();
   private final List<IHandleLivingAttack> livingAttackEventHandlers = new ArrayList();
   public final Potion PARALYSED = this.register("witchery:potion.paralysed", PotionParalysis.class);
   public final Potion WRAPPED_IN_VINE = this.register("witchery:potion.wrappedinvine", PotionWrappedInVine.class);
   public final Potion SPIKED = this.register("witchery:potion.spiked", PotionSpiked.class);
   public final Potion SPROUTING = this.register("witchery:potion.sprouting", PotionSprouting.class);
   public final Potion GROTESQUE = this.register("witchery:potion.grotesque", PotionGrotesque.class);
   public final Potion LOVE = this.register("witchery:potion.love", PotionLove.class);
   public final Potion SUN_ALLERGY = this.register("witchery:potion.allergysun", PotionSunAllergy.class);
   public final Potion CHILLED = this.register("witchery:potion.chilled", PotionChilled.class);
   public final Potion SNOW_TRAIL = this.register("witchery:potion.snowtrail", PotionSnowTrail.class);
   public final Potion FLOATING = this.register("witchery:potion.floating", PotionFloating.class);
   public final Potion NETHER_BOUND = this.register("witchery:potion.hellishaura", PotionHellishAura.class);
   public final Potion BREWING_EXPERT = this.register("witchery:potion.brewingexpertise", PotionBrewingExpertise.class);
   public final Potion DOUBLE_JUMP = this.register("witchery:potion.doublejump", PotionBase.class);
   public final Potion FEATHER_FALL = this.register("witchery:potion.featherfall", PotionFeatherFall.class);
   public final Potion DARKNESS_ALLERGY = this.register("witchery:potion.allergydark", PotionDarknessAllergy.class);
   public final Potion REINCARNATE = this.register("witchery:potion.reincarnate", PotionReincarnate.class);
   public final Potion INSANITY = this.register("witchery:potion.insane", PotionInsanity.class);
   public final Potion KEEP_INVENTORY = this.register("witchery:potion.keepinventory", PotionKeepInventory.class);
   public final Potion SINKING = this.register("witchery:potion.sinking", PotionSinking.class);
   public final Potion OVERHEATING = this.register("witchery:potion.overheating", PotionOverheating.class);
   public final Potion WAKING_NIGHTMARE = this.register("witchery:potion.wakingnightmare", PotionWakingNightmare.class);
   public final Potion QUEASY = this.register("witchery:potion.queasy", PotionQueasy.class);
   public final Potion SWIMMING = this.register("witchery:potion.swimming", PotionSwimming.class);
   public final Potion RESIZING = this.register("witchery:potion.resizing", PotionResizing.class);
   public final Potion COLORFUL = this.register("witchery:potion.colorful", PotionColorful.class);
   public final Potion ENDER_INHIBITION = this.register("witchery:potion.enderinhibition", PotionEnderInhibition.class);
   public final Potion ILL_FITTING = this.register("witchery:potion.illfitting", PotionIllFitting.class);
   public final Potion VOLATILITY = this.register("witchery:potion.volatility", PotionVolatility.class);
   public final Potion ENSLAVED = this.register("witchery:potion.enslaved", PotionEnslaved.class);
   public final Potion MORTAL_COIL = this.register("witchery:potion.mortalcoil", PotionMortalCoil.class);
   public final Potion ABSORB_MAGIC = this.register("witchery:potion.absorbmagic", PotionAbsorbMagic.class);
   public final Potion POISON_WEAPONS = this.register("witchery:potion.poisonweapons", PotionPoisonWeapons.class);
   public final Potion REFLECT_PROJECTILES = this.register("witchery:potion.reflectprojectiles", PotionReflectProjectiles.class);
   public final Potion REFLECT_DAMAGE = this.register("witchery:potion.reflectdamage", PotionReflectDamage.class);
   public final Potion ATTRACT_PROJECTILES = this.register("witchery:potion.attractprojectiles", PotionAttractProjectiles.class);
   public final Potion REPELL_ATTACKER = this.register("witchery:potion.repellattacker", PotionRepellAttacker.class);
   public final Potion STOUT_BELLY = this.register("witchery:potion.stoutbelly", PotionStoutBelly.class);
   public final Potion FEEL_NO_PAIN = this.register("witchery:potion.feelnopain", PotionFeelNoPain.class);
   public final Potion GAS_MASK = this.register("witchery:potion.gasmask", PotionGasMask.class);
   public final Potion DISEASED = this.register("witchery:potion.diseased", PotionDiseased.class);
   public final Potion FORTUNE = this.register("witchery:potion.fortune", PotionFortune.class);
   public final Potion WORSHIP = this.register("witchery:potion.worship", PotionWorship.class);
   public final Potion KEEP_EFFECTS = this.register("witchery:potion.keepeffects", PotionKeepEffectsOnDeath.class);
   public final Potion WOLFSBANE = this.register("witchery:potion.wolfsbane", PotionBase.class);

   private Potion register(String unlocalisedName, Class<? extends PotionBase> clazz) {
      int potionID = -1;
      WitcheryPotions.PotionArrayExtender.extendPotionArray();
      if (potionID < 1) {
         for(int i = Config.instance().potionStartID; i < Potion.field_76425_a.length; ++i) {
            if (Potion.field_76425_a[i] == null) {
               potionID = Config.instance().configuration.get("potions", unlocalisedName, i).getInt();
               break;
            }
         }
      }

      if (potionID > 31 && potionID < Potion.field_76425_a.length) {
         try {
            if (Potion.field_76425_a[potionID] != null) {
               Log.instance().warning(String.format("Potion slot %d already occupided by %s is being overwriting with %s, you may want to change potion ids in the config file!", potionID, Potion.field_76425_a[potionID].func_76393_a(), unlocalisedName));
            }

            if (potionID > 127) {
               Log.instance().warning(String.format("Using potion slot %d (for potion %s), can lead to problems, since there is a client/server syncing restriction of max 128 potion IDs. Use the PotionStartID configuration setting to lower the range witchery uses.", potionID, unlocalisedName));
            }

            Constructor<? extends PotionBase> ctor = clazz.getConstructor(Integer.TYPE, Integer.TYPE);
            PotionBase potion = (PotionBase)ctor.newInstance(potionID, unlocalisedName.hashCode());
            potion.func_76390_b(unlocalisedName);
            this.allEffects.add(potion);
            if (potion instanceof IHandleLivingHurt) {
               this.livingHurtEventHandlers.add((IHandleLivingHurt)potion);
            }

            if (potion instanceof IHandleLivingDeath) {
               this.livingDeathEventHandlers.add((IHandleLivingDeath)potion);
            }

            if (potion instanceof IHandleLivingUpdate) {
               this.livingUpdateEventHandlers.add((IHandleLivingUpdate)potion);
            }

            if (potion instanceof IHandleRenderLiving) {
               this.renderLivingEventHandlers.add((IHandleRenderLiving)potion);
            }

            if (potion instanceof IHandlePreRenderLiving) {
               this.renderLivingPreEventHandlers.add((IHandlePreRenderLiving)potion);
            }

            if (potion instanceof IHandlePlayerDrops) {
               this.playerDropsEventHandlers.add((IHandlePlayerDrops)potion);
            }

            if (potion instanceof IHandleLivingJump) {
               this.livingJumpEventHandlers.add((IHandleLivingJump)potion);
            }

            if (potion instanceof IHandleEnderTeleport) {
               this.enderTeleportEventHandlers.add((IHandleEnderTeleport)potion);
            }

            if (potion instanceof IHandleLivingSetAttackTarget) {
               this.livingSetAttackTargetEventHandlers.add((IHandleLivingSetAttackTarget)potion);
            }

            if (potion instanceof IHandleHarvestDrops) {
               this.harvestDropsEventHandlers.add((IHandleHarvestDrops)potion);
            }

            if (potion instanceof IHandleLivingAttack) {
               this.livingAttackEventHandlers.add((IHandleLivingAttack)potion);
            }

            potion.postContructInitialize();
            return potion;
         } catch (NoSuchMethodException var6) {
            return null;
         } catch (InvocationTargetException var7) {
            return null;
         } catch (InstantiationException var8) {
            return null;
         } catch (IllegalAccessException var9) {
            return null;
         }
      } else {
         Log.instance().warning(String.format("Failed to assign potion %s to slot %d, max slot id is %d, you may want to change the potion ids in the config file!", unlocalisedName, potionID, Potion.field_76425_a.length - 1));
         return null;
      }
   }

   public void preInit() {
      Config.instance().saveIfChanged();
   }

   public void init() {
      Iterator i$ = this.allEffects.iterator();

      while(true) {
         while(i$.hasNext()) {
            Potion potion = (Potion)i$.next();
            if (potion.field_76415_H > 0 && potion.field_76415_H < Potion.field_76425_a.length) {
               if (Potion.field_76425_a[potion.field_76415_H] != potion) {
                  Log.instance().warning(String.format("Another mod has overwritten Witchery potion %s in slot %d! offender: %s.", potion.func_76393_a(), potion.field_76415_H, Potion.field_76425_a[potion.field_76415_H].func_76393_a()));
               }
            } else {
               Log.instance().warning(String.format("Witchery potion has not been registered: %s!", potion.func_76393_a()));
            }
         }

         return;
      }
   }

   public static class ClientEventHooks {
      @SubscribeEvent(
         priority = EventPriority.NORMAL
      )
      public void onRenderLiving(Pre event) {
         if (event.entity != null && event.entity.field_70170_p != null && event.entity.field_70170_p.field_72995_K) {
            Iterator i$ = Witchery.Potions.renderLivingPreEventHandlers.iterator();

            while(i$.hasNext()) {
               IHandlePreRenderLiving handler = (IHandlePreRenderLiving)i$.next();
               if (event.isCanceled()) {
                  break;
               }

               if (event.entity.func_82165_m(handler.getPotion().field_76415_H)) {
                  PotionEffect effect = event.entity.func_70660_b(handler.getPotion());
                  handler.onLivingRender(event.entity.field_70170_p, event.entity, event, effect.func_76458_c());
               }
            }
         }

      }

      @SubscribeEvent(
         priority = EventPriority.NORMAL
      )
      public void onRenderLiving(Post event) {
         if (event.entity != null && event.entity.field_70170_p != null && event.entity.field_70170_p.field_72995_K) {
            Iterator i$ = Witchery.Potions.renderLivingEventHandlers.iterator();

            while(i$.hasNext()) {
               IHandleRenderLiving handler = (IHandleRenderLiving)i$.next();
               if (event.isCanceled()) {
                  break;
               }

               if (event.entity.func_82165_m(handler.getPotion().field_76415_H)) {
                  PotionEffect effect = event.entity.func_70660_b(handler.getPotion());
                  handler.onLivingRender(event.entity.field_70170_p, event.entity, event, effect.func_76458_c());
               }
            }
         }

      }

      @SubscribeEvent
      public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
         if (event != null && !event.isCanceled() && event.player != null) {
            if (!event.player.func_70644_a(Witchery.Potions.RESIZING) && (new EntitySizeInfo(event.player)).isDefault) {
               if (BlockBeartrap.checkForHiddenTrap(event.player, event.target)) {
                  event.setCanceled(true);
               }
            } else {
               double reach = (double)Minecraft.func_71410_x().field_71442_b.func_78757_d();
               MovingObjectPosition mop = event.player.func_70614_a(reach, event.partialTicks);
               if (mop != null && !BlockBeartrap.checkForHiddenTrap(event.player, mop)) {
                  event.context.func_72731_b(event.player, mop, 0, event.partialTicks);
               }

               event.setCanceled(true);
            }
         }

      }
   }

   public static class EventHooks {
      @SubscribeEvent(
         priority = EventPriority.HIGHEST
      )
      public void onPlayerDrops(PlayerDropsEvent event) {
         Iterator i$ = Witchery.Potions.playerDropsEventHandlers.iterator();

         while(i$.hasNext()) {
            IHandlePlayerDrops handler = (IHandlePlayerDrops)i$.next();
            if (event.isCanceled()) {
               break;
            }

            if (event.entityLiving.func_70644_a(handler.getPotion())) {
               PotionEffect effect = event.entityLiving.func_70660_b(handler.getPotion());
               handler.onPlayerDrops(event.entityPlayer.func_130014_f_(), event.entityPlayer, event, effect.func_76458_c());
            }
         }

      }

      @SubscribeEvent(
         priority = EventPriority.HIGH
      )
      public void onBlockHarvest(HarvestDropsEvent event) {
         Iterator i$ = Witchery.Potions.harvestDropsEventHandlers.iterator();

         while(i$.hasNext()) {
            IHandleHarvestDrops handler = (IHandleHarvestDrops)i$.next();
            if (event.isCanceled()) {
               break;
            }

            if (event.harvester != null && event.harvester.func_70644_a(handler.getPotion())) {
               PotionEffect effect = event.harvester.func_70660_b(handler.getPotion());
               handler.onHarvestDrops(event.world, event.harvester, event, effect.func_76458_c());
            }
         }

      }

      @SubscribeEvent
      public void onLivingHurt(LivingHurtEvent event) {
         Iterator i$ = Witchery.Potions.livingHurtEventHandlers.iterator();

         while(i$.hasNext()) {
            IHandleLivingHurt handler = (IHandleLivingHurt)i$.next();
            if (event.isCanceled()) {
               break;
            }

            if (handler.handleAllHurtEvents() || event.entityLiving.func_70644_a(handler.getPotion())) {
               PotionEffect effect = event.entityLiving.func_70660_b(handler.getPotion());
               handler.onLivingHurt(event.entityLiving.field_70170_p, event.entityLiving, event, effect != null ? effect.func_76458_c() : -1);
            }
         }

      }

      @SubscribeEvent
      public void onLivingUpdate(LivingUpdateEvent event) {
         Iterator i$ = Witchery.Potions.livingUpdateEventHandlers.iterator();

         while(i$.hasNext()) {
            IHandleLivingUpdate handler = (IHandleLivingUpdate)i$.next();
            if (event.isCanceled()) {
               break;
            }

            if (event.entityLiving.func_70644_a(handler.getPotion())) {
               PotionEffect effect = event.entityLiving.func_70660_b(handler.getPotion());
               handler.onLivingUpdate(event.entityLiving.field_70170_p, event.entityLiving, event, effect.func_76458_c(), effect.func_76459_b());
            }
         }

      }

      @SubscribeEvent
      public void onLivingAttack(LivingAttackEvent event) {
         Iterator i$ = Witchery.Potions.livingAttackEventHandlers.iterator();

         while(i$.hasNext()) {
            IHandleLivingAttack handler = (IHandleLivingAttack)i$.next();
            if (event.isCanceled()) {
               break;
            }

            if (event.entityLiving.func_70644_a(handler.getPotion())) {
               PotionEffect effect = event.entityLiving.func_70660_b(handler.getPotion());
               handler.onLivingAttack(event.entityLiving.field_70170_p, event.entityLiving, event, effect.func_76458_c());
            }
         }

         if (Witchery.modHooks.isAM2Present && !event.isCanceled() && !event.entity.field_70170_p.field_72995_K && event.source == DamageSource.field_76368_d && event.entity instanceof EntityPlayer && (ExtendedPlayer.get((EntityPlayer)event.entity).getCreatureType() == TransformCreature.WOLF || ExtendedPlayer.get((EntityPlayer)event.entity).getCreatureType() == TransformCreature.BAT) && !event.entity.field_70170_p.func_147439_a(MathHelper.func_76128_c(event.entity.field_70165_t), MathHelper.func_76128_c(event.entity.field_70163_u), MathHelper.func_76128_c(event.entity.field_70161_v)).func_149721_r()) {
            event.setCanceled(true);
         }

      }

      @SubscribeEvent
      public void onLivingJump(LivingJumpEvent event) {
         Iterator i$ = Witchery.Potions.livingJumpEventHandlers.iterator();

         while(i$.hasNext()) {
            IHandleLivingJump handler = (IHandleLivingJump)i$.next();
            if (event.isCanceled()) {
               break;
            }

            if (event.entityLiving.func_70644_a(handler.getPotion())) {
               PotionEffect effect = event.entityLiving.func_70660_b(handler.getPotion());
               handler.onLivingJump(event.entityLiving.field_70170_p, event.entityLiving, event, effect.func_76458_c());
            }
         }

      }

      @SubscribeEvent
      public void onEnderTeleport(EnderTeleportEvent event) {
         if (event.entityLiving == null || event.entityLiving.field_70170_p.field_73011_w.field_76574_g != Config.instance().dimensionTormentID && event.entityLiving.field_70170_p.field_73011_w.field_76574_g != Config.instance().dimensionMirrorID) {
            Iterator i$ = Witchery.Potions.enderTeleportEventHandlers.iterator();

            while(i$.hasNext()) {
               IHandleEnderTeleport handler = (IHandleEnderTeleport)i$.next();
               if (event.isCanceled()) {
                  break;
               }

               if (event.entityLiving.func_70644_a(handler.getPotion())) {
                  PotionEffect effect = event.entityLiving.func_70660_b(handler.getPotion());
                  handler.onEnderTeleport(event.entityLiving.field_70170_p, event.entityLiving, event, effect.func_76458_c());
               }
            }

         } else {
            event.setCanceled(true);
         }
      }

      @SubscribeEvent
      public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
         if (event.entityLiving instanceof EntityLiving) {
            EntityLiving livingEntity = (EntityLiving)event.entityLiving;
            if (livingEntity != null && Witchery.Potions.ENSLAVED != null && event.target != null && event.target instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)event.target;
               if (!livingEntity.func_70644_a(Witchery.Potions.ENSLAVED) && PotionEnslaved.isMobEnslavedBy(livingEntity, player)) {
                  livingEntity.func_70624_b((EntityLivingBase)null);
               }
            }

            Iterator i$ = Witchery.Potions.livingSetAttackTargetEventHandlers.iterator();

            while(i$.hasNext()) {
               IHandleLivingSetAttackTarget handler = (IHandleLivingSetAttackTarget)i$.next();
               if (event.isCanceled()) {
                  break;
               }

               if (event.entityLiving.func_70644_a(handler.getPotion())) {
                  PotionEffect effect = event.entityLiving.func_70660_b(handler.getPotion());
                  handler.onLivingSetAttackTarget(event.entityLiving.field_70170_p, livingEntity, event, effect.func_76458_c());
               }
            }
         }

      }

      @SubscribeEvent(
         priority = EventPriority.LOW
      )
      public void onLivingDeath(LivingDeathEvent event) {
         Iterator i$ = Witchery.Potions.livingDeathEventHandlers.iterator();

         while(i$.hasNext()) {
            IHandleLivingDeath handler = (IHandleLivingDeath)i$.next();
            if (event.isCanceled()) {
               break;
            }

            if (event.entityLiving.func_70644_a(handler.getPotion())) {
               PotionEffect effect = event.entityLiving.func_70660_b(handler.getPotion());
               handler.onLivingDeath(event.entityLiving.field_70170_p, event.entityLiving, event, effect.func_76458_c());
            }
         }

         if (!event.entityLiving.field_70170_p.field_72995_K && event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.entityLiving;
            Collection<PotionEffect> activeEffects = player.func_70651_bq();
            if (activeEffects.size() > 0) {
               ArrayList<PotionEffect> permenantEffectList = new ArrayList();
               int allPermentantLevel = -1;
               if (player.func_70644_a(Witchery.Potions.KEEP_EFFECTS)) {
                  PotionEffect permAll = player.func_70660_b(Witchery.Potions.KEEP_EFFECTS);
                  allPermentantLevel = permAll.func_76458_c();
               }

               Iterator i$ = activeEffects.iterator();

               while(true) {
                  while(true) {
                     PotionEffect effect;
                     int potionID;
                     do {
                        do {
                           do {
                              if (!i$.hasNext()) {
                                 if (permenantEffectList.size() > 0) {
                                    NBTTagList nbtEffectList = new NBTTagList();
                                    Iterator i$ = permenantEffectList.iterator();

                                    while(i$.hasNext()) {
                                       PotionEffect permenantEffect = (PotionEffect)i$.next();
                                       NBTTagCompound nbtEffect = new NBTTagCompound();
                                       permenantEffect.func_82719_a(nbtEffect);
                                       nbtEffectList.func_74742_a(nbtEffect);
                                    }

                                    NBTTagCompound nbtPlayer = Infusion.getNBT(player);
                                    nbtPlayer.func_74782_a("WITCPoSpawn", nbtEffectList);
                                 }

                                 return;
                              }

                              effect = (PotionEffect)i$.next();
                              potionID = effect.func_76456_a();
                           } while(potionID < 0);
                        } while(potionID >= Potion.field_76425_a.length);
                     } while(Potion.field_76425_a[potionID] == null);

                     if (Potion.field_76425_a[potionID] instanceof PotionBase) {
                        PotionBase potion = (PotionBase)Potion.field_76425_a[potionID];
                        if (potion.isPermenant()) {
                           permenantEffectList.add(effect);
                           continue;
                        }
                     }

                     Potion potion = Potion.field_76425_a[potionID];
                     if (!PotionBase.isDebuff(potion) && allPermentantLevel >= effect.func_76458_c()) {
                        permenantEffectList.add(effect);
                     }
                  }
               }
            }
         }

      }
   }

   private static class PotionArrayExtender {
      private static boolean potionArrayExtended;

      private static void extendPotionArray() {
         if (!potionArrayExtended) {
            int RESERVED = true;
            int MAX_EXTRA = Math.min(Config.instance().potionStartID - 32, 0) + 96;
            Log.instance().debug("Extending the vanilla potions array");
            int existingArrayLength = Potion.field_76425_a.length;
            Potion[] newPotionArray = new Potion[existingArrayLength + MAX_EXTRA];
            System.arraycopy(Potion.field_76425_a, 0, newPotionArray, 0, existingArrayLength);
            setPrivateFinalValue(Potion.class, (Object)null, newPotionArray, "potionTypes", "field_76425_a");
            potionArrayExtended = true;
         }

      }

      private static <T, E> void setPrivateFinalValue(Class<? super T> classToAccess, T instance, E value, String... fieldNames) {
         Field field = ReflectionHelper.findField(classToAccess, ObfuscationReflectionHelper.remapFieldNames(classToAccess.getName(), fieldNames));

         try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & -17);
            field.set(instance, value);
         } catch (Throwable var6) {
            var6.printStackTrace();
         }

      }
   }
}
