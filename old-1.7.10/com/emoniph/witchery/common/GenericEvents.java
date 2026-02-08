package com.emoniph.witchery.common;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockVoidBramble;
import com.emoniph.witchery.brewing.potions.PotionEnslaved;
import com.emoniph.witchery.brewing.potions.PotionParalysis;
import com.emoniph.witchery.brewing.potions.PotionResizing;
import com.emoniph.witchery.dimension.WorldProviderDreamWorld;
import com.emoniph.witchery.entity.EntityDemon;
import com.emoniph.witchery.entity.EntityFollower;
import com.emoniph.witchery.entity.EntityGoblin;
import com.emoniph.witchery.entity.EntityHornedHuntsman;
import com.emoniph.witchery.entity.EntityItemWaystone;
import com.emoniph.witchery.entity.EntityMindrake;
import com.emoniph.witchery.entity.EntityOwl;
import com.emoniph.witchery.entity.EntitySummonedUndead;
import com.emoniph.witchery.entity.EntityToad;
import com.emoniph.witchery.entity.EntityVillageGuard;
import com.emoniph.witchery.entity.EntityVillagerWere;
import com.emoniph.witchery.entity.EntityWitchHunter;
import com.emoniph.witchery.entity.EntityWolfman;
import com.emoniph.witchery.entity.ai.EntityAIDigBlocks;
import com.emoniph.witchery.entity.ai.EntityAISleep;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.infusion.InfusedBrewEffect;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.item.ItemDeathsClothes;
import com.emoniph.witchery.item.ItemHunterClothes;
import com.emoniph.witchery.item.ItemMoonCharm;
import com.emoniph.witchery.item.ItemPoppet;
import com.emoniph.witchery.item.ItemVampireClothes;
import com.emoniph.witchery.network.PacketExtendedEntityRequestSyncToClient;
import com.emoniph.witchery.network.PacketHowl;
import com.emoniph.witchery.network.PacketParticles;
import com.emoniph.witchery.network.PacketPlayerStyle;
import com.emoniph.witchery.network.PacketSelectPlayerAbility;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.BoltDamageSource;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TameableUtil;
import com.emoniph.witchery.util.TargetPointUtil;
import com.emoniph.witchery.util.TimeUtil;
import com.emoniph.witchery.util.TransformCreature;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

public class GenericEvents {
   @SubscribeEvent
   public void onServerChat(ServerChatEvent event) {
      boolean chatMasqueradeAllowed = Config.instance().allowChatMasquerading;
      ExtendedPlayer playerEx = ExtendedPlayer.get(event.player);
      if (playerEx != null && chatMasqueradeAllowed && playerEx.getCreatureType() == TransformCreature.PLAYER && playerEx.getOtherPlayerSkin() != null && !playerEx.getOtherPlayerSkin().isEmpty()) {
         String disguise = playerEx.getOtherPlayerSkin();
         ChatComponentTranslation comp = new ChatComponentTranslation("chat.type.text", new Object[]{this.getPlayerChatName(event.player, disguise), ForgeHooks.newChatWithLinks(event.message)});
         event.component = comp;
         if (!event.player.field_70170_p.field_72995_K) {
            Iterator i$ = event.player.field_70170_p.field_73010_i.iterator();

            while(i$.hasNext()) {
               Object otherPlayerObj = i$.next();
               EntityPlayer otherPlayer = (EntityPlayer)otherPlayerObj;
               if (otherPlayer.field_71075_bZ.field_75098_d && MinecraftServer.func_71276_C().func_71203_ab().func_152596_g(otherPlayer.func_146103_bH())) {
                  ChatUtil.sendTranslated(EnumChatFormatting.GOLD, otherPlayer, "witchery.rite.mirrormirror.opchatreveal", disguise, event.player.func_70005_c_());
               }
            }
         }
      }

   }

   private IChatComponent getPlayerChatName(EntityPlayerMP player, String otherName) {
      ChatComponentText chatcomponenttext = new ChatComponentText(ScorePlayerTeam.func_96667_a(player.func_96124_cp(), otherName));
      chatcomponenttext.func_150256_b().func_150241_a(new ClickEvent(Action.SUGGEST_COMMAND, "/msg " + player.func_70005_c_() + " "));
      return chatcomponenttext;
   }

   @SubscribeEvent
   public void onEntityConstructing(EntityConstructing event) {
      if (event.entity instanceof EntityPlayer && ExtendedPlayer.get((EntityPlayer)event.entity) == null) {
         ExtendedPlayer.register((EntityPlayer)event.entity);
      } else if (event.entity instanceof EntityVillager && ExtendedVillager.get((EntityVillager)event.entity) == null) {
         ExtendedVillager.register((EntityVillager)event.entity);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onEntityJoinWorld(EntityJoinWorldEvent event) {
      if (event.entity instanceof EntityLivingBase) {
         NBTTagCompound nbtData = event.entity.getEntityData();
         nbtData.func_74776_a("WITCInitialWidth", event.entity.field_70130_N);
         nbtData.func_74776_a("WITCInitialHeight", event.entity.field_70131_O);
      }

      if (!event.entity.field_70170_p.field_72995_K && event.entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)event.entity;
         ExtendedPlayer.loadProxyData(player);
         Shapeshift.INSTANCE.initCurrentShift(player);
         Infusion.syncPlayer(event.world, player);
         Iterator i$ = event.world.field_73010_i.iterator();

         while(i$.hasNext()) {
            Object obj = i$.next();
            EntityPlayer otherPlayer = (EntityPlayer)obj;
            if (otherPlayer != player) {
               Witchery.packetPipeline.sendTo((IMessage)(new PacketPlayerStyle(otherPlayer)), (EntityPlayer)player);
            }
         }

         Witchery.packetPipeline.sendToDimension(new PacketPlayerStyle(player), event.world.field_73011_w.field_76574_g);
         if (player.field_71093_bK != Config.instance().dimensionDreamID && WorldProviderDreamWorld.getPlayerIsSpiritWalking(player) && !WorldProviderDreamWorld.getPlayerIsGhost(player)) {
            WorldProviderDreamWorld.setPlayerMustAwaken(player, true);
         } else if (player.field_71093_bK == Config.instance().dimensionDreamID && !WorldProviderDreamWorld.getPlayerIsSpiritWalking(player)) {
            WorldProviderDreamWorld.changeDimension(player, 0);
            WorldProviderDreamWorld.findTopAndSetPosition(player.field_70170_p, player);
         }
      } else if (event.world.field_73011_w.field_76574_g == Config.instance().dimensionDreamID && isDisallowedEntity(event.entity)) {
         event.setCanceled(true);
      }

      if (event.entity instanceof EntityVillager && !(event.entity instanceof EntityVillagerWere) && !(event.entity instanceof EntityVillageGuard)) {
         EntityVillager villager = (EntityVillager)event.entity;
         villager.field_70714_bg.func_75776_a(1, new EntityAISleep(villager));
      } else if (event.entity instanceof EntityZombie) {
         EntityZombie creature = (EntityZombie)event.entity;
         creature.field_70715_bh.func_75776_a(3, new EntityAINearestAttackableTarget(creature, EntityFollower.class, 0, false, false, new IEntitySelector() {
            public boolean func_82704_a(Entity entity) {
               return entity instanceof EntityFollower && ((EntityFollower)entity).getFollowerType() == 0;
            }
         }));
      } else if (event.entity instanceof EntitySkeleton) {
         EntitySkeleton creature = (EntitySkeleton)event.entity;
         creature.field_70715_bh.func_75776_a(3, new EntityAINearestAttackableTarget(creature, EntityFollower.class, 0, true, false, new IEntitySelector() {
            public boolean func_82704_a(Entity entity) {
               return entity instanceof EntityFollower && ((EntityFollower)entity).getFollowerType() == 0;
            }
         }));
      }

      if (event.entity.field_70170_p.field_72995_K && event.entity instanceof EntityPlayer) {
         Witchery.packetPipeline.sendToServer(new PacketExtendedEntityRequestSyncToClient((EntityLivingBase)event.entity));
      }

   }

   @SubscribeEvent
   public void onPlayerCloneEvent(Clone event) {
      NBTTagCompound oldPlayerNBT = new NBTTagCompound();
      ExtendedPlayer oldPlayerEx = ExtendedPlayer.get(event.original);
      oldPlayerEx.saveNBTData(oldPlayerNBT);
      ExtendedPlayer newPlayerEx = ExtendedPlayer.get(event.entityPlayer);
      newPlayerEx.loadNBTData(oldPlayerNBT);
      newPlayerEx.restorePlayerInventoryFrom(oldPlayerEx);
   }

   private static boolean isDisallowedEntity(Entity entity) {
      if (entity instanceof EntityLiving) {
         Class cls = entity.getClass();
         String packageName = cls.getCanonicalName();
         return !packageName.startsWith("net.minecraft.entity") && !packageName.startsWith("com.emoniph.witchery") ? true : entity instanceof EntityEnderman;
      } else {
         return false;
      }
   }

   @SubscribeEvent
   public void onPlayerWakeUpEvent(PlayerWakeUpEvent event) {
      World world = event.entityPlayer.field_70170_p;
      if (!world.field_72995_K) {
         EntityPlayer player = event.entityPlayer;
         ExtendedPlayer playerEx = ExtendedPlayer.get(player);
         if (playerEx.isVampire() && player.func_71026_bH()) {
            int x = MathHelper.func_76128_c(player.field_70165_t);
            int y = MathHelper.func_76128_c(player.field_70163_u);
            int z = MathHelper.func_76128_c(player.field_70161_v);
            if (world.func_147439_a(x, y, z) == Witchery.Blocks.COFFIN) {
               Iterator iterator = world.field_73010_i.iterator();

               EntityPlayer entityplayer;
               do {
                  if (!iterator.hasNext()) {
                     long currentTime = world.func_72820_D() - 11000L;
                     world.func_72877_b(currentTime);
                     break;
                  }

                  entityplayer = (EntityPlayer)iterator.next();
               } while(entityplayer.func_71026_bH());
            }
         }
      }

   }

   @SubscribeEvent
   public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
      World world = event.entityPlayer.field_70170_p;
      EntityPlayer player = event.entityPlayer;
      if (CreatureUtil.isWerewolf(event.entityPlayer)) {
         if (!world.field_72995_K) {
            ChatUtil.sendTranslated(EnumChatFormatting.RED, event.entityPlayer, "witchery.nosleep.wolf");
            event.result = EnumStatus.OTHER_PROBLEM;
         }
      } else if (event.entityPlayer.func_70644_a(Witchery.Potions.RESIZING)) {
         if (!world.field_72995_K) {
            ChatUtil.sendTranslated(EnumChatFormatting.RED, event.entityPlayer, "witchery.nosleep.resized");
            event.result = EnumStatus.OTHER_PROBLEM;
         }
      } else if (ExtendedPlayer.get(event.entityPlayer).isVampire() && world.func_147439_a(event.x, event.y, event.z) == Witchery.Blocks.COFFIN) {
         if (event.entityPlayer.field_70170_p.func_72935_r()) {
            if (!world.field_72995_K) {
               if (player.func_70608_bn() || !player.func_70089_S()) {
                  return;
               }

               if (!world.field_73011_w.func_76569_d()) {
                  return;
               }

               if (!world.func_72935_r()) {
                  event.result = EnumStatus.OTHER_PROBLEM;
                  return;
               }

               if (Math.abs(player.field_70165_t - (double)event.x) > 3.0D || Math.abs(player.field_70163_u - (double)event.y) > 2.0D || Math.abs(player.field_70161_v - (double)event.z) > 3.0D) {
                  event.result = EnumStatus.TOO_FAR_AWAY;
                  return;
               }

               double d0 = 8.0D;
               double d1 = 5.0D;
               List list = world.func_72872_a(EntityMob.class, AxisAlignedBB.func_72330_a((double)event.x - d0, (double)event.y - d1, (double)event.z - d0, (double)event.x + d0, (double)event.y + d1, (double)event.z + d0));
               if (!list.isEmpty()) {
                  event.result = EnumStatus.NOT_SAFE;
                  return;
               }
            }

            if (player.func_70115_ae()) {
               player.func_70078_a((Entity)null);
            }

            PotionResizing.setEntitySize(player, 0.2F, 0.2F);
            player.field_70129_M = 0.2F;
            if (world.func_72899_e(event.x, event.y, event.z)) {
               int l = world.func_147439_a(event.x, event.y, event.z).getBedDirection(world, event.x, event.y, event.z);
               float f1 = 0.5F;
               float f = 0.5F;
               switch(l) {
               case 0:
                  f = 0.9F;
                  break;
               case 1:
                  f1 = 0.1F;
                  break;
               case 2:
                  f = 0.1F;
                  break;
               case 3:
                  f1 = 0.9F;
               }

               player.field_71079_bU = 0.0F;
               player.field_71089_bV = 0.0F;
               switch(l) {
               case 0:
                  player.field_71089_bV = -1.8F;
                  break;
               case 1:
                  player.field_71079_bU = 1.8F;
                  break;
               case 2:
                  player.field_71089_bV = 1.8F;
                  break;
               case 3:
                  player.field_71079_bU = -1.8F;
               }

               player.func_70107_b((double)((float)event.x + f1), (double)((float)event.y + 0.9375F), (double)((float)event.z + f));
            } else {
               player.func_70107_b((double)((float)event.x + 0.5F), (double)((float)event.y + 0.9375F), (double)((float)event.z + 0.5F));
            }

            player.field_71083_bS = true;
            player.field_71076_b = 0;
            player.field_71081_bT = new ChunkCoordinates(event.x, event.y, event.z);
            player.field_70159_w = player.field_70179_y = player.field_70181_x = 0.0D;
            if (!world.field_72995_K) {
               world.func_72854_c();
            }

            event.result = EnumStatus.OK;
            return;
         }

         if (!world.field_72995_K) {
            ChatUtil.sendTranslated(EnumChatFormatting.RED, event.entityPlayer, "witchery.nosleep.dayonly");
            event.result = EnumStatus.OTHER_PROBLEM;
         }
      }

   }

   @SubscribeEvent
   public void onLivingDrops(LivingDropsEvent event) {
      if (!event.isCanceled() && event.entityLiving != null && !event.entityLiving.field_70170_p.field_72995_K) {
         if (event.entityLiving instanceof EntityLiving && EntityUtil.isNoDrops((EntityLiving)event.entityLiving)) {
            event.setCanceled(true);
            return;
         }

         if (event.entityLiving instanceof EntityHorse) {
            EntityHorse horse = (EntityHorse)event.entityLiving;
            NBTTagCompound nbtHorse = horse.getEntityData();
            if (nbtHorse != null && nbtHorse.func_74767_n("WITCIsBinky")) {
               event.drops.clear();
               event.drops.add(new EntityItem(horse.field_70170_p, horse.field_70165_t, horse.field_70163_u, horse.field_70161_v, Witchery.Items.GENERIC.itemBinkyHead.createStack()));
            }
         }
      }

   }

   @SubscribeEvent
   public void onItemToss(ItemTossEvent event) {
      if (!event.isCanceled() && !event.player.field_70170_p.field_72995_K && event.entityItem != null && event.entityItem.func_92059_d() != null) {
         if (event.entityItem.func_92059_d().func_77973_b() == Witchery.Items.SEEDS_MINDRAKE) {
            event.entityItem.lifespan = TimeUtil.secsToTicks(3);
            NBTTagCompound nbtItem = event.entityItem.getEntityData();
            nbtItem.func_74778_a("WITCThrower", event.player.func_70005_c_());
         } else if (Witchery.Items.GENERIC.itemWaystone.isMatch(event.entityItem.func_92059_d()) || Witchery.Items.GENERIC.itemWaystoneBound.isMatch(event.entityItem.func_92059_d()) || Witchery.Items.GENERIC.itemAttunedStone.isMatch(event.entityItem.func_92059_d()) || Witchery.Items.GENERIC.itemSubduedSpirit.isMatch(event.entityItem.func_92059_d()) || Witchery.Items.GENERIC.itemWaystonePlayerBound.isMatch(event.entityItem.func_92059_d())) {
            EntityUtil.spawnEntityInWorld(event.entity.field_70170_p, new EntityItemWaystone(event.entityItem));
            event.setCanceled(true);
         }
      }

   }

   @SubscribeEvent
   public void onItemExpireEvent(ItemExpireEvent event) {
      if (!event.isCanceled() && !event.entityItem.field_70170_p.field_72995_K && event.entityItem != null && event.entityItem.func_92059_d() != null && event.entityItem.func_92059_d().func_77973_b() == Witchery.Items.SEEDS_MINDRAKE) {
         for(int i = 0; i < event.entityItem.func_92059_d().field_77994_a; ++i) {
            EntityMindrake mindrake = new EntityMindrake(event.entityItem.field_70170_p);
            mindrake.func_70012_b(event.entityItem.field_70165_t, event.entityItem.field_70163_u, event.entityItem.field_70161_v, 0.0F, 0.0F);
            NBTTagCompound nbtItem = event.entityItem.getEntityData();
            if (nbtItem.func_74764_b("WITCThrower")) {
               String thrower = nbtItem.func_74779_i("WITCThrower");
               if (thrower != null && !thrower.isEmpty()) {
                  mindrake.func_110163_bv();
                  mindrake.func_70903_f(true);
                  TameableUtil.setOwnerByUsername(mindrake, thrower);
               }
            }

            ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_POP, mindrake, 1.0D, 1.0D, 16);
            event.entityItem.field_70170_p.func_72838_d(mindrake);
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onPlayerDrops(PlayerDropsEvent event) {
      if (!event.entityPlayer.field_70170_p.field_72995_K && !event.isCanceled() && ExtendedPlayer.get(event.entityPlayer).isVampire()) {
         int ticks = TimeUtil.minsToTicks(MathHelper.func_76125_a(Config.instance().vampireDeathItemKeepAliveMins, 5, 30));

         EntityItem item;
         for(Iterator i$ = event.drops.iterator(); i$.hasNext(); item.lifespan = ticks) {
            item = (EntityItem)i$.next();
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onEntityInteract(EntityInteractEvent event) {
      PotionEffect effect = event.entityPlayer.func_70660_b(Witchery.Potions.PARALYSED);
      if (effect != null && effect.func_76458_c() >= 4) {
         event.setCanceled(true);
      } else {
         ExtendedPlayer playerEx = ExtendedPlayer.get(event.entityPlayer);
         ExtendedPlayer.VampirePower power = playerEx.getSelectedVampirePower();
         if (power != ExtendedPlayer.VampirePower.NONE) {
            if (power == ExtendedPlayer.VampirePower.DRINK && event.target instanceof EntityLivingBase) {
               if (!event.entityPlayer.field_70170_p.field_72995_K) {
                  float RANGE = ((EntityLivingBase)event.target).func_70644_a(Witchery.Potions.PARALYSED) ? 2.1F : 1.3F;
                  if (event.target.func_70092_e(event.entityPlayer.field_70165_t, event.target.field_70163_u, event.entityPlayer.field_70161_v) <= (double)(RANGE * RANGE)) {
                     int drinkAmount = ItemVampireClothes.isDrinkBoostActive(event.entityPlayer) ? 15 : 10;
                     if (CreatureUtil.isWerewolf(event.target, true)) {
                        event.entityPlayer.func_70097_a(new EntityDamageSource(DamageSource.field_76376_m.func_76355_l(), event.entityPlayer), 4.0F);
                        ParticleEffect.FLAME.send(SoundEffect.WITCHERY_RANDOM_DRINK, event.entityPlayer.field_70170_p, event.target.field_70165_t, event.target.field_70163_u + (double)event.target.field_70131_O * 0.8D, event.target.field_70161_v, 0.5D, 0.2D, 16);
                     } else if (event.target instanceof EntityVillageGuard) {
                        EntityVillageGuard target = (EntityVillageGuard)event.target;
                        playerEx.increaseBloodPower(target.takeBlood(playerEx.getCreatureType() == TransformCreature.NONE ? drinkAmount : 2, event.entityPlayer));
                        ParticleEffect.REDDUST.send(SoundEffect.WITCHERY_RANDOM_DRINK, event.entityPlayer.field_70170_p, target.field_70165_t, target.field_70163_u + (double)target.field_70131_O * 0.8D, target.field_70161_v, 0.5D, 0.2D, 16);
                        this.checkForBloodDrinkingWitnesses(event.entityPlayer, target);
                     } else if (event.target instanceof EntityVillager) {
                        EntityVillager target = (EntityVillager)event.target;
                        ExtendedVillager villagerEx = ExtendedVillager.get(target);
                        playerEx.increaseBloodPower(villagerEx.takeBlood(playerEx.getCreatureType() == TransformCreature.NONE ? drinkAmount : 2, event.entityPlayer));
                        ParticleEffect.REDDUST.send(SoundEffect.WITCHERY_RANDOM_DRINK, event.entityPlayer.field_70170_p, target.field_70165_t, target.field_70163_u + (double)target.field_70131_O * 0.8D, target.field_70161_v, 0.5D, 0.2D, 16);
                        this.checkForBloodDrinkingWitnesses(event.entityPlayer, target);
                        if (playerEx.getVampireLevel() == 2) {
                           if (Config.instance().allowVampireQuests && villagerEx.getBlood() >= 250 && villagerEx.getBlood() <= 280) {
                              if (playerEx.getVampireQuestCounter() >= 5) {
                                 playerEx.increaseVampireLevel();
                              } else {
                                 SoundEffect.NOTE_PLING.playOnlyTo(event.entityPlayer, 1.0F, 1.0F);
                                 playerEx.increaseVampireQuestCounter();
                              }
                           } else if (villagerEx.getBlood() < 240) {
                              playerEx.resetVampireQuestCounter();
                           }
                        } else if (playerEx.getVampireLevel() == 8 && playerEx.canIncreaseVampireLevel() && this.villagerIsInCage(target)) {
                           if (villagerEx.getBlood() >= 250 && villagerEx.getBlood() <= 280) {
                              if (playerEx.getVampireQuestCounter() >= 5) {
                                 playerEx.increaseVampireLevel();
                              } else {
                                 SoundEffect.NOTE_PLING.playOnlyTo(event.entityPlayer, 1.0F, 1.0F);
                                 playerEx.increaseVampireQuestCounter();
                              }
                           } else if (villagerEx.getBlood() < 240) {
                              playerEx.resetVampireQuestCounter();
                           }
                        }
                     } else if (event.target instanceof EntityPlayer) {
                        EntityPlayer target = (EntityPlayer)event.target;
                        playerEx.increaseBloodPower(ExtendedPlayer.get(target).takeHumanBlood(playerEx.getCreatureType() == TransformCreature.NONE ? drinkAmount : 2, event.entityPlayer));
                        ParticleEffect.REDDUST.send(SoundEffect.WITCHERY_RANDOM_DRINK, event.entityPlayer.field_70170_p, target.field_70165_t, target.field_70163_u + (double)target.field_70131_O * 0.8D, target.field_70161_v, 0.5D, 0.2D, 16);
                     } else if (event.target instanceof IAnimals) {
                        EntityLivingBase target = (EntityLivingBase)event.target;
                        target.func_70097_a(new EntityDamageSource(DamageSource.field_76376_m.func_76355_l(), event.entityPlayer), 1.0F);
                        playerEx.increaseBloodPower(2, (int)Math.ceil((double)((float)playerEx.getMaxBloodPower() * 0.25F)));
                        ParticleEffect.REDDUST.send(SoundEffect.WITCHERY_RANDOM_DRINK, event.entityPlayer.field_70170_p, target.field_70165_t, target.field_70163_u + (double)target.field_70131_O * 0.8D, target.field_70161_v, 0.5D, 0.2D, 16);
                     }
                  }

                  event.setCanceled(true);
               }
            } else if (power == ExtendedPlayer.VampirePower.MESMERIZE) {
               if (!event.entityPlayer.field_70170_p.field_72995_K) {
                  if (event.entityPlayer.func_70093_af() && playerEx.getVampireLevel() >= 2) {
                     playerEx.toggleVampireVision();
                  } else if (playerEx.getCreatureType() == TransformCreature.NONE && playerEx.getVampireLevel() >= 2) {
                     if ((!(event.target instanceof EntityVillager) || event.target instanceof EntityVillagerWere) && !(event.target instanceof EntityPlayer) && !(event.target instanceof EntityVillageGuard)) {
                        SoundEffect.NOTE_SNARE.playOnlyTo(event.entityPlayer, 1.0F, 0.5F);
                     } else {
                        EntityLivingBase victim = (EntityLivingBase)event.target;
                        if (!victim.func_70644_a(Witchery.Potions.PARALYSED)) {
                           if (playerEx.decreaseBloodPower(ExtendedPlayer.VampirePower.MESMERIZE.INITIAL_COST, true)) {
                              victim.func_70690_d(new PotionEffect(Witchery.Potions.PARALYSED.field_76415_H, TimeUtil.secsToTicks(5 + playerEx.getVampireLevel() / 2 + Math.max(0, (playerEx.getVampireLevel() - 4) / 2) + (ItemVampireClothes.isMezmeriseBoostActive(event.entityPlayer) ? 3 : 0)), playerEx.getVampireLevel() >= 8 ? 5 : 4));
                              SoundEffect.WITCHERY_RANDOM_HYPNOSIS.playAtPlayer(event.entity.field_70170_p, event.entityPlayer, 0.5F, 1.0F);
                           } else {
                              SoundEffect.NOTE_SNARE.playOnlyTo(event.entityPlayer, 1.0F, 0.5F);
                           }
                        } else {
                           SoundEffect.NOTE_SNARE.playOnlyTo(event.entityPlayer, 1.0F, 0.5F);
                        }
                     }
                  } else {
                     SoundEffect.NOTE_SNARE.playOnlyTo(event.entityPlayer, 1.0F, 0.5F);
                  }

                  event.setCanceled(true);
               }
            } else {
               event.setCanceled(true);
            }
         }

         if (!event.isCanceled()) {
            if (event.target != null && !event.target.field_70170_p.field_72995_K && event.target instanceof EntityLiving && PotionEnslaved.isMobEnslavedBy((EntityLiving)event.target, event.entityPlayer)) {
               EntityPlayer player = event.entityPlayer;
               EntityLiving creature = (EntityLiving)event.target;
               ItemStack heldObject = player.func_70694_bm();
               if (Witchery.Items.GENERIC.itemGraveyardDust.isMatch(heldObject) && creature instanceof EntitySummonedUndead) {
                  float maxHealth = creature.func_110138_aP() + 2.0F;
                  if (maxHealth <= 50.0F) {
                     IAttributeInstance attribute = creature.func_110148_a(SharedMonsterAttributes.field_111267_a);
                     if (attribute != null) {
                        attribute.func_111128_a((double)maxHealth);
                        creature.func_70606_j(maxHealth);
                        creature.func_110163_bv();
                        Witchery.packetPipeline.sendToAllAround(new PacketParticles(ParticleEffect.INSTANT_SPELL, SoundEffect.MOB_SILVERFISH_KILL, creature, 0.5D, 1.0D), TargetPointUtil.from(creature, 16.0D));
                        if (!player.field_71075_bZ.field_75098_d) {
                           --heldObject.field_77994_a;
                           if (player instanceof EntityPlayerMP) {
                              ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                           }
                        }
                     }
                  }
               } else if (heldObject != null && (creature instanceof EntityZombie || creature instanceof EntityPigZombie || creature instanceof EntitySkeleton)) {
                  if (heldObject.func_77973_b() instanceof ItemArmor) {
                     ItemArmor armor = (ItemArmor)heldObject.func_77973_b();
                     if (creature.func_71124_b(4 - armor.field_77881_a) == null) {
                        creature.func_70062_b(4 - armor.field_77881_a, heldObject.func_77979_a(1));
                        creature.func_110163_bv();
                        if (player instanceof EntityPlayerMP) {
                           ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                        }
                     }
                  } else if (heldObject.func_77973_b() instanceof ItemSword && creature.func_71124_b(0) == null) {
                     creature.func_70062_b(0, heldObject.func_77979_a(1));
                     creature.func_110163_bv();
                     if (player instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                     }
                  }
               }
            }

            if (event.target != null && !event.target.field_70170_p.field_72995_K && event.target instanceof EntityVillager) {
               EntityVillager villager = (EntityVillager)event.target;
               ItemStack heldObject = event.entityPlayer.func_70694_bm();
               if (!villager.func_70631_g_() && heldObject != null && heldObject.func_77973_b() == Items.field_151027_R && event.entityPlayer.func_70093_af()) {
                  Village village = villager.field_70954_d;
                  if (village != null) {
                     int rep = village.func_82684_a(event.entityPlayer.func_70005_c_());
                     if (rep >= 10) {
                        if (village.func_75562_e() > 8) {
                           List list = event.entity.field_70170_p.func_72872_a(EntityVillageGuard.class, AxisAlignedBB.func_72330_a((double)(village.func_75577_a().field_71574_a - village.func_75568_b()), (double)(village.func_75577_a().field_71572_b - 4), (double)(village.func_75577_a().field_71573_c - village.func_75568_b()), (double)(village.func_75577_a().field_71574_a + village.func_75568_b()), (double)(village.func_75577_a().field_71572_b + 4), (double)(village.func_75577_a().field_71573_c + village.func_75568_b())));
                           int numGuards = list.size();
                           if (numGuards < MathHelper.func_76128_c((double)village.func_75562_e() * 0.25D)) {
                              int villagerNumTrades = villager.field_70963_i == null ? 1 : villager.field_70963_i.size();
                              if (!CreatureUtil.isWerewolf(event.target, true) && event.target.field_70170_p.field_73012_v.nextInt(villagerNumTrades * 2 + 1) == 0) {
                                 villager.func_85030_a("mob.villager.yew", 1.0F, (villager.field_70170_p.field_73012_v.nextFloat() - villager.field_70170_p.field_73012_v.nextFloat()) * 0.2F + 1.0F);
                                 ChatUtil.sendTranslated(EnumChatFormatting.GREEN, event.entityPlayer, "witchery.village.villageracceptsguardduty");
                                 EntityVillageGuard.createFrom(villager);
                              } else {
                                 ChatUtil.sendTranslated(EnumChatFormatting.RED, event.entityPlayer, "witchery.village.villagerrefusesguardduty");
                                 villager.func_85030_a("mob.villager.no", 1.0F, (villager.field_70170_p.field_73012_v.nextFloat() - villager.field_70170_p.field_73012_v.nextFloat()) * 0.2F + 1.0F);
                              }
                           } else {
                              ChatUtil.sendTranslated(EnumChatFormatting.BLUE, event.entityPlayer, "witchery.village.toomanyguards");
                              SoundEffect.NOTE_SNARE.playOnlyTo(event.entityPlayer);
                           }
                        } else {
                           ChatUtil.sendTranslated(EnumChatFormatting.BLUE, event.entityPlayer, "witchery.village.villagetoosmall");
                           SoundEffect.NOTE_SNARE.playOnlyTo(event.entityPlayer);
                        }
                     } else {
                        ChatUtil.sendTranslated(EnumChatFormatting.BLUE, event.entityPlayer, "witchery.village.reptoolow");
                        SoundEffect.NOTE_SNARE.playOnlyTo(event.entityPlayer);
                     }
                  }
               }
            }

            if (!event.entity.field_70170_p.field_72995_K && event.target != null && event.target instanceof EntityWolf) {
               EntityWolf wolf = (EntityWolf)event.target;
               if (playerEx.getWerewolfLevel() == 7 && playerEx.getWolfmanQuestState() == ExtendedPlayer.QuestState.STARTED && playerEx.getCreatureType() == TransformCreature.WOLF && !wolf.func_70909_n() && !wolf.func_70919_bu()) {
                  if (wolf.field_70170_p.field_73012_v.nextInt(3) == 0) {
                     wolf.func_70903_f(true);
                     wolf.func_70778_a((PathEntity)null);
                     wolf.func_70624_b((EntityLivingBase)null);
                     wolf.func_70907_r().func_75270_a(true);
                     wolf.func_70606_j(20.0F);
                     wolf.func_152115_b(event.entityPlayer.func_110124_au().toString());
                     this.playTameEffect(wolf, true);
                     wolf.field_70170_p.func_72960_a(wolf, (byte)7);
                     playerEx.increaseWolfmanQuestCounter();
                  } else {
                     this.playTameEffect(wolf, false);
                     wolf.field_70170_p.func_72960_a(wolf, (byte)6);
                     if (wolf.field_70170_p.field_73012_v.nextInt(10) == 0) {
                        wolf.func_70916_h(true);
                        wolf.func_70624_b(event.entityPlayer);
                     }
                  }
               }
            }

            ItemStack heldStack = event.entityPlayer.func_70694_bm();
            if (heldStack != null) {
               if (heldStack.func_77973_b() == Witchery.Items.TAGLOCK_KIT) {
                  Witchery.Items.TAGLOCK_KIT.onEntityInteract(event.entityPlayer.field_70170_p, event.entityPlayer, heldStack, event);
                  if (event.isCanceled()) {
                     return;
                  }
               }

               if (heldStack.func_77973_b() == Witchery.Items.BLOOD_GOBLET) {
                  Witchery.Items.BLOOD_GOBLET.onEntityInteract(event.entityPlayer.field_70170_p, event.entityPlayer, heldStack, event);
                  if (event.isCanceled()) {
                     return;
                  }
               }

               if (Witchery.Items.GENERIC.itemWoodenStake.isMatch(heldStack) && Config.instance().allowStakingVampires && event.target instanceof EntityPlayer) {
                  EntityPlayer victim = (EntityPlayer)event.target;
                  if (ExtendedPlayer.get(victim).isVampire() && victim.field_71083_bS) {
                     ParticleEffect.REDDUST.send(SoundEffect.WITCHERY_RANDOM_DRINK, victim.field_70170_p, event.target.field_70165_t, event.target.field_70163_u, event.target.field_70161_v, (double)event.target.field_70130_N, (double)event.target.field_70131_O, 16);
                     EntityUtil.instantDeath(victim, event.entityPlayer);
                     if (!event.entityPlayer.field_71075_bZ.field_75098_d) {
                        --heldStack.field_77994_a;
                     }

                     event.setCanceled(true);
                     return;
                  }
               }
            }

         }
      }
   }

   private boolean villagerIsInCage(EntityVillager target) {
      int ogX = MathHelper.func_76128_c(target.field_70165_t);
      int ogY = MathHelper.func_76128_c(target.field_70163_u);
      int ogZ = MathHelper.func_76128_c(target.field_70161_v);
      if (this.isCaged(target.field_70170_p, ogX, ogY, ogZ)) {
         return true;
      } else if (this.isCaged(target.field_70170_p, ogX + 1, ogY, ogZ)) {
         return true;
      } else if (this.isCaged(target.field_70170_p, ogX, ogY, ogZ + 1)) {
         return true;
      } else if (this.isCaged(target.field_70170_p, ogX - 1, ogY, ogZ)) {
         return true;
      } else if (this.isCaged(target.field_70170_p, ogX, ogY, ogZ - 1)) {
         return true;
      } else if (this.isCaged(target.field_70170_p, ogX + 1, ogY, ogZ + 1)) {
         return true;
      } else if (this.isCaged(target.field_70170_p, ogX + 1, ogY, ogZ - 1)) {
         return true;
      } else if (this.isCaged(target.field_70170_p, ogX - 1, ogY, ogZ + 1)) {
         return true;
      } else {
         return this.isCaged(target.field_70170_p, ogX - 1, ogY, ogZ - 1);
      }
   }

   private boolean isCaged(World world, int x, int y, int z) {
      int count = 0;
      Block bars = Blocks.field_150411_aY;
      int count = count + (world.func_147439_a(x + 1, y, z) == bars ? 1 : 0);
      count += world.func_147439_a(x, y, z + 1) == bars ? 1 : 0;
      count += world.func_147439_a(x - 1, y, z) == bars ? 1 : 0;
      count += world.func_147439_a(x, y, z - 1) == bars ? 1 : 0;
      count += world.func_147439_a(x + 1, y, z + 1) == bars ? 1 : 0;
      count += world.func_147439_a(x - 1, y, z + 1) == bars ? 1 : 0;
      count += world.func_147439_a(x + 1, y, z - 1) == bars ? 1 : 0;
      count += world.func_147439_a(x - 1, y, z - 1) == bars ? 1 : 0;
      ++y;
      count += world.func_147439_a(x + 1, y, z) == bars ? 1 : 0;
      count += world.func_147439_a(x, y, z + 1) == bars ? 1 : 0;
      count += world.func_147439_a(x - 1, y, z) == bars ? 1 : 0;
      count += world.func_147439_a(x, y, z - 1) == bars ? 1 : 0;
      count += world.func_147439_a(x + 1, y, z + 1) == bars ? 1 : 0;
      count += world.func_147439_a(x - 1, y, z + 1) == bars ? 1 : 0;
      count += world.func_147439_a(x + 1, y, z - 1) == bars ? 1 : 0;
      count += world.func_147439_a(x - 1, y, z - 1) == bars ? 1 : 0;
      if (count < 15) {
         return false;
      } else {
         count = 0;
         ++y;
         count = count + (!BlockUtil.isReplaceableBlock(world, x + 1, y, z) ? 1 : 0);
         count += !BlockUtil.isReplaceableBlock(world, x, y, z + 1) ? 1 : 0;
         count += !BlockUtil.isReplaceableBlock(world, x - 1, y, z) ? 1 : 0;
         count += !BlockUtil.isReplaceableBlock(world, x, y, z - 1) ? 1 : 0;
         count += !BlockUtil.isReplaceableBlock(world, x + 1, y, z + 1) ? 1 : 0;
         count += !BlockUtil.isReplaceableBlock(world, x - 1, y, z + 1) ? 1 : 0;
         count += !BlockUtil.isReplaceableBlock(world, x + 1, y, z - 1) ? 1 : 0;
         count += !BlockUtil.isReplaceableBlock(world, x - 1, y, z - 1) ? 1 : 0;
         count += !BlockUtil.isReplaceableBlock(world, x, y, z) ? 1 : 0;
         return count >= 9;
      }
   }

   private void checkForBloodDrinkingWitnesses(EntityPlayer player, EntityLivingBase victim) {
      AxisAlignedBB bounds = victim.field_70121_D.func_72314_b(16.0D, 8.0D, 16.0D);
      List<EntityVillageGuard> guards = victim.field_70170_p.func_72872_a(EntityVillageGuard.class, bounds);
      Iterator i$ = guards.iterator();

      while(i$.hasNext()) {
         EntityVillageGuard guard = (EntityVillageGuard)i$.next();
         if (!guard.func_70644_a(Witchery.Potions.PARALYSED) && guard.func_70635_at().func_75522_a(victim)) {
            guard.func_70624_b(player);
         }
      }

   }

   @SubscribeEvent
   public void onPlayerInteract(PlayerInteractEvent event) {
      PotionEffect effect = event.entityPlayer.func_70660_b(Witchery.Potions.PARALYSED);
      if (effect != null && effect.func_76458_c() >= 4) {
         event.setCanceled(true);
      } else {
         ExtendedPlayer playerEx = ExtendedPlayer.get(event.entityPlayer);
         if (playerEx.getSelectedVampirePower() != ExtendedPlayer.VampirePower.NONE) {
            if (event.action == net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.action == net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
               switch(playerEx.getSelectedVampirePower()) {
               case MESMERIZE:
               case SPEED:
               case BAT:
               case ULTIMATE:
                  Witchery.packetPipeline.sendToServer(new PacketSelectPlayerAbility(playerEx, true));
               default:
                  event.setCanceled(true);
               }
            }
         } else if (event.entityPlayer.field_70170_p.field_72995_K) {
            if ((event.action == net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.action == net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) && event.entityPlayer.field_70125_A == -90.0F && event.entityPlayer.func_70093_af()) {
               Witchery.packetPipeline.sendToServer(new PacketHowl());
            }
         } else if (event.action == net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && !event.entityPlayer.field_71075_bZ.field_75098_d) {
            if (playerEx.isVampire() && event.world.func_147439_a(event.x, event.y, event.z) == Witchery.Blocks.GARLIC_GARLAND) {
               event.entityPlayer.func_70015_d(1);
               event.setCanceled(true);
            } else {
               Block block;
               if (playerEx.getCreatureType() == TransformCreature.WOLF && playerEx.getWerewolfLevel() >= 3 && event.entityPlayer.func_70093_af()) {
                  block = event.world.func_147439_a(event.x, event.y, event.z);
                  if (block == Blocks.field_150349_c || block == Blocks.field_150354_m || block == Blocks.field_150346_d || block == Blocks.field_150391_bh || block == Blocks.field_150351_n) {
                     EntityAIDigBlocks.tryHarvestBlock(event.world, event.x, event.y, event.z, event.entityPlayer, event.entityPlayer);
                     event.setCanceled(true);
                  }
               } else if (playerEx.getVampireLevel() >= 6 && playerEx.getCreatureType() == TransformCreature.NONE && event.entityPlayer.func_70093_af() && (event.entityPlayer.func_70694_bm() == null || !event.entityPlayer.func_70694_bm().func_77973_b().func_150897_b(Blocks.field_150348_b)) && event.entityPlayer.func_71024_bL().func_75116_a() > 0) {
                  block = event.world.func_147439_a(event.x, event.y, event.z);
                  if (block == Blocks.field_150348_b || block == Blocks.field_150424_aL || block == Blocks.field_150347_e) {
                     EntityAIDigBlocks.tryHarvestBlock(event.world, event.x, event.y, event.z, event.entityPlayer, event.entityPlayer);
                     event.entityPlayer.func_71020_j(10.0F);
                     event.setCanceled(true);
                  }
               }
            }
         }

      }
   }

   private void playTameEffect(EntityTameable entity, boolean tamed) {
      String s = "heart";
      if (!tamed) {
         s = "smoke";
      }

      for(int i = 0; i < 7; ++i) {
         double d0 = entity.field_70170_p.field_73012_v.nextGaussian() * 0.02D;
         double d1 = entity.field_70170_p.field_73012_v.nextGaussian() * 0.02D;
         double d2 = entity.field_70170_p.field_73012_v.nextGaussian() * 0.02D;
         entity.field_70170_p.func_72869_a(s, entity.field_70165_t + (double)(entity.field_70170_p.field_73012_v.nextFloat() * entity.field_70130_N * 2.0F) - (double)entity.field_70130_N, entity.field_70163_u + 0.5D + (double)(entity.field_70170_p.field_73012_v.nextFloat() * entity.field_70131_O), entity.field_70161_v + (double)(entity.field_70170_p.field_73012_v.nextFloat() * entity.field_70130_N * 2.0F) - (double)entity.field_70130_N, d0, d1, d2);
      }

   }

   @SubscribeEvent
   public void onLivingUpdate(LivingUpdateEvent event) {
      if (!event.entity.field_70170_p.field_72995_K && event.entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)event.entity;
         ExtendedPlayer playerEx = ExtendedPlayer.get(player);
         Shapeshift.INSTANCE.updatePlayerState(player, playerEx);
         playerEx.tick();
         if (playerEx.isVampire()) {
            int prevHunger = player.func_71024_bL().field_75124_e;
            int hunger = player.func_71024_bL().func_75116_a();
            if (prevHunger < hunger) {
               player.func_71024_bL().func_75122_a(-player.func_71024_bL().func_75116_a(), 0.0F);
            }
         }

         if (event.entity.field_70173_aa % 40 == 1) {
            if (playerEx.getWerewolfLevel() > 0) {
               boolean isFullMoon = CreatureUtil.isFullMoon(player.field_70170_p);
               switch(playerEx.getCreatureType()) {
               case WOLF:
               case WOLFMAN:
                  boolean isWolfman = playerEx.getCreatureType() == TransformCreature.WOLFMAN;
                  if (!isFullMoon && !player.field_71071_by.func_146028_b(Witchery.Items.MOON_CHARM) && !ItemMoonCharm.isWolfsbaneActive(player, playerEx)) {
                     Shapeshift.INSTANCE.shiftTo(player, TransformCreature.NONE);
                     ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_FIZZ, player, 1.5D, 1.5D, 16);
                     break;
                  }

                  updateWerewolfEffects(player, isWolfman);
                  break;
               case NONE:
                  if (isFullMoon && !player.field_71071_by.func_146028_b(Witchery.Items.MOON_CHARM) && !ItemMoonCharm.isWolfsbaneActive(player, playerEx)) {
                     Shapeshift.INSTANCE.shiftTo(player, TransformCreature.WOLF);
                     ParticleEffect.EXPLODE.send(SoundEffect.WITCHERY_MOB_WOLFMAN_HOWL, player, 1.5D, 1.5D, 16);
                     updateWerewolfEffects(player, false);
                  }
               }
            }

            if (playerEx.isVampire()) {
               if (player.func_70090_H()) {
                  player.func_70050_g(300);
               }

               if (playerEx.getCreatureType() == TransformCreature.BAT && !playerEx.decreaseBloodPower(ExtendedPlayer.VampirePower.BAT.UPKEEP_COST, true)) {
                  Shapeshift.INSTANCE.shiftTo(player, TransformCreature.NONE);
               }

               if (playerEx.getVampireLevel() == 3 && !player.field_70170_p.func_72935_r()) {
                  if (playerEx.getVampireQuestCounter() >= 300 || playerEx.getVampireQuestCounter() >= 10 && player.field_71075_bZ.field_75098_d) {
                     if (playerEx.canIncreaseVampireLevel()) {
                        playerEx.increaseVampireLevel();
                     }
                  } else if (Config.instance().allowVampireQuests) {
                     playerEx.increaseVampireQuestCounter();
                  }
               }

               if (playerEx.getVampireLevel() == 7 && playerEx.canIncreaseVampireLevel()) {
                  Village closestVillage = player.field_70170_p.field_72982_D.func_75550_a(MathHelper.func_76128_c(player.field_70165_t), MathHelper.func_76128_c(player.field_70163_u), MathHelper.func_76128_c(player.field_70161_v), 32);
                  if (closestVillage != null && playerEx.storeVampireQuestChunk(closestVillage.func_75577_a().field_71574_a >> 4, closestVillage.func_75577_a().field_71573_c >> 4)) {
                     if (playerEx.getVampireQuestCounter() >= 3) {
                        playerEx.increaseVampireLevel();
                     } else {
                        playerEx.increaseVampireQuestCounter();
                        SoundEffect.NOTE_PLING.playOnlyTo(player, 1.0F, 1.0F);
                     }
                  }
               }

               if (playerEx.isVampireVisionActive()) {
                  player.func_70690_d(new PotionEffect(Potion.field_76439_r.field_76415_H, 400, 0, true));
               }

               if (player.func_70644_a(Potion.field_76436_u)) {
                  player.func_82170_o(Potion.field_76436_u.field_76415_H);
               }

               if (player.func_70027_ad() && player.func_70644_a(Potion.field_76426_n)) {
                  player.func_70097_a(EntityUtil.DamageSourceVampireFire.SOURCE, 2.0F);
               }

               while(player.func_71024_bL().func_75116_a() < 20 && playerEx.decreaseBloodPower(5, true)) {
                  player.func_71024_bL().func_75122_a(1, 4.0F);
               }

               if (playerEx.getBloodPower() == 0 && player.func_71024_bL().func_75116_a() == 0) {
                  player.func_70690_d(new PotionEffect(Potion.field_76437_t.field_76415_H, TimeUtil.secsToTicks(10), 8, true));
                  player.func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, TimeUtil.secsToTicks(10), 1, true));
                  player.func_70690_d(new PotionEffect(Potion.field_76419_f.field_76415_H, TimeUtil.secsToTicks(10), 1, true));
               }

               if (CreatureUtil.isInSunlight(player) && !player.field_71075_bZ.field_75098_d) {
                  if (playerEx.getBloodPower() == 0 && player.field_70173_aa > 400) {
                     EntityUtil.instantDeath(player, (EntityLivingBase)null);
                  }

                  if (playerEx.getVampireLevel() >= 5) {
                     playerEx.decreaseBloodPower(60, false);
                     player.func_70690_d(new PotionEffect(Potion.field_76437_t.field_76415_H, TimeUtil.secsToTicks(10), 3, false));
                     player.func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, TimeUtil.secsToTicks(10), 0, true));
                     player.func_70690_d(new PotionEffect(Potion.field_76419_f.field_76415_H, TimeUtil.secsToTicks(10), 0, true));
                  } else {
                     playerEx.setBloodPower(0);
                  }

                  if (playerEx.getBloodPower() == 0) {
                     player.func_70015_d(5);
                  }
               }
            } else {
               playerEx.giveHumanBlood(2);
            }
         }
      }

   }

   public static void updateWerewolfEffects(EntityPlayer player, boolean isWolfman) {
      player.func_70690_d(new PotionEffect(Potion.field_76439_r.field_76415_H, 400, 0, true));
      if (player.func_70644_a(Potion.field_76436_u)) {
         player.func_82170_o(Potion.field_76436_u.field_76415_H);
      }

      for(int slot = isWolfman ? 0 : 1; slot <= 4; ++slot) {
         ItemStack stack = player.func_71124_b(slot);
         if (stack != null && stack.func_77973_b() != Witchery.Items.MOON_CHARM && (player.field_71070_bA == null || player.field_71070_bA.field_75152_c == 0 || slot != 0)) {
            player.func_70099_a(stack, 1.0F);
            player.func_70062_b(slot, (ItemStack)null);
         }
      }

   }

   @SubscribeEvent
   public void onLivingFall(LivingFallEvent event) {
      if (!event.entity.field_70170_p.field_72995_K && event.entity instanceof EntityPlayer) {
         event.distance = Shapeshift.INSTANCE.updateFallState((EntityPlayer)event.entity, event.distance);
      }

   }

   @SubscribeEvent
   public void onHarvestDrops(HarvestDropsEvent event) {
      if (!event.world.field_72995_K && event.harvester != null && !event.isCanceled()) {
         ExtendedPlayer playerEx = ExtendedPlayer.get(event.harvester);
         Shapeshift.INSTANCE.processDigging(event, event.harvester, playerEx);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onLivingHurt(LivingHurtEvent event) {
      if (!event.entityLiving.field_70170_p.field_72995_K && !event.isCanceled()) {
         this.checkForChargeDamage(event);
         if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.entityLiving;
            float playerHealth = player.func_110143_aJ();
            ExtendedPlayer playerEx = ExtendedPlayer.get(player);
            if (event.source == DamageSource.field_76369_e && playerEx.isVampire()) {
               event.setCanceled(true);
               return;
            }

            boolean wolfForm = playerEx.getWerewolfLevel() > 0 && (playerEx.getCreatureType() == TransformCreature.WOLF || playerEx.getCreatureType() == TransformCreature.WOLFMAN);
            float healthAfterDamage;
            if (wolfForm && event.source != DamageSource.field_76380_i && event.source != DamageSource.field_76368_d && event.source != DamageSource.field_76369_e && event.source != DamageSource.field_76379_h) {
               if (!event.source.func_76347_k()) {
                  healthAfterDamage = Shapeshift.INSTANCE.getResistance(player, playerEx);
                  event.ammount = Math.max(0.0F, event.ammount - healthAfterDamage);
               }

               if (!CreatureUtil.isWerewolf(event.source.func_76364_f())) {
                  if (!CreatureUtil.isSilverDamage(event.source)) {
                     event.ammount = Math.max(Math.min(event.ammount, Shapeshift.INSTANCE.getDamageCap(player, playerEx)), 0.5F);
                  } else {
                     event.ammount += 5.0F;
                  }
               }

               if (event.ammount <= 0.0F) {
                  event.setCanceled(true);
                  return;
               }
            }

            if (ItemDeathsClothes.isFullSetWorn(player) && player.func_70694_bm() != null && player.func_70694_bm().func_77973_b() == Witchery.Items.DEATH_HAND) {
               event.ammount = Math.min(event.ammount, 7.0F);
            }

            healthAfterDamage = EntityUtil.getHealthAfterDamage(event, playerHealth, player);
            if ((player.field_71093_bK == Config.instance().dimensionDreamID || WorldProviderDreamWorld.getPlayerIsGhost(player)) && healthAfterDamage <= 0.0F && !player.field_71075_bZ.field_75098_d) {
               event.setCanceled(true);
               event.setResult(Result.DENY);
               WorldProviderDreamWorld.setPlayerMustAwaken(player, true);
               return;
            }

            dropItemsOnHit(player);
            boolean ignoreProtection = wolfForm || event.source instanceof BoltDamageSource && ((BoltDamageSource)event.source).isPoweredDraining;
            boolean hasHunterSet = ItemHunterClothes.isFullSetWorn(player, false);
            if (hasHunterSet && event.source.func_82725_o() && player.field_70170_p.field_73012_v.nextDouble() < 0.25D) {
               event.setCanceled(true);
               return;
            }

            double MOB_SPAWN_CHANCE;
            boolean louseUsed;
            if ((event.source instanceof EntityDamageSource || event.source.func_94541_c()) && !ignoreProtection) {
               ItemStack hat = player.func_71124_b(4);
               if (hat != null && hat.func_77973_b() == Witchery.Items.BABAS_HAT && player.field_71093_bK != Config.instance().dimensionTormentID) {
                  int TELEPORT_COST = true;
                  MOB_SPAWN_CHANCE = 0.25D;
                  louseUsed = true;
                  if (player.field_70170_p.field_73012_v.nextDouble() < 0.25D && Infusion.aquireEnergy(player.field_70170_p, player, 5, true)) {
                     BlockVoidBramble.teleportRandomly(player.field_70170_p, MathHelper.func_76128_c(player.field_70165_t), MathHelper.func_76128_c(player.field_70163_u), MathHelper.func_76128_c(player.field_70161_v), player, 6);
                     event.setCanceled(true);
                     return;
                  }
               }
            }

            if (event.source instanceof EntityDamageSource) {
               EntityDamageSource entitySource = (EntityDamageSource)event.source;
               ItemStack belt = player.func_71124_b(2);
               int i;
               if (belt != null && belt.func_77973_b() == Witchery.Items.BARK_BELT && !CreatureUtil.isWoodenDamage(event.source)) {
                  int currentLevel = Math.min(Witchery.Items.BARK_BELT.getChargeLevel(belt), Witchery.Items.BARK_BELT.getMaxChargeLevel(player));
                  if (currentLevel > 0) {
                     World world = player.field_70170_p;
                     Random rand = world.field_73012_v;
                     i = currentLevel > 1 && rand.nextDouble() < 0.25D ? 2 : 1;
                     Witchery.Items.BARK_BELT.setChargeLevel(belt, Math.max(currentLevel - i, 0));
                     event.setCanceled(true);

                     for(int i = 0; i < i; ++i) {
                        double dx = 1.0D * (double)(rand.nextInt(2) == 0 ? -1 : 1);
                        double dy = 1.0D * (double)(rand.nextInt(2) == 0 ? -1 : 1);
                        EntityItem item = new EntityItem(world, player.field_70165_t + dx, player.field_70163_u + 1.5D, player.field_70161_v + dy, new ItemStack(Items.field_151055_y));
                        item.field_145804_b = 60;
                        item.lifespan = 60;
                        world.func_72838_d(item);
                     }

                     return;
                  }
               }

               MOB_SPAWN_CHANCE = 0.25D;
               if (player.func_70694_bm() != null && player.func_70694_bm().func_77973_b() == Witchery.Items.HUNTSMANS_SPEAR && player.func_70632_aY() && player.field_70170_p.field_73012_v.nextDouble() < 0.25D && entitySource.func_76346_g() != null && entitySource.func_76346_g() instanceof EntityLivingBase) {
                  EntityLivingBase living = (EntityLivingBase)entitySource.func_76346_g();
                  if (living.func_70089_S()) {
                     EntityWolf wolf = new EntityWolf(player.field_70170_p);
                     wolf.func_70012_b(player.field_70165_t, player.field_70163_u, player.field_70161_v, player.field_70125_A, player.field_70759_as);
                     wolf.func_70624_b(living);
                     wolf.func_70784_b(living);
                     wolf.func_70916_h(true);
                     wolf.func_70690_d(new PotionEffect(Potion.field_82731_v.field_76415_H, 12000, 1));
                     player.field_70170_p.func_72838_d(wolf);
                  }
               }

               louseUsed = false;
               i = 0;

               ItemStack stack;
               List list;
               InventoryPlayer var10001;
               PotionEffect effect;
               while(true) {
                  var10001 = player.field_71071_by;
                  if (i >= InventoryPlayer.func_70451_h()) {
                     break;
                  }

                  label511: {
                     stack = player.field_71071_by.func_70301_a(i);
                     if (stack != null && stack.func_77973_b() == Witchery.Items.PARASYTIC_LOUSE && stack.func_77960_j() > 0) {
                        list = Items.field_151068_bn.func_77834_f(stack.func_77960_j());
                        if (list != null && !list.isEmpty()) {
                           effect = new PotionEffect((PotionEffect)list.get(0));
                           if (isPotionAggressive(effect.func_76456_a()) && event.source.func_76346_g() != null && event.source.func_76346_g() instanceof EntityLivingBase) {
                              ((EntityLivingBase)event.source.func_76346_g()).func_70690_d(effect);
                              break label511;
                           }

                           if (effect.func_76456_a() == Potion.field_76428_l.field_76415_H) {
                              player.func_70690_d(effect);
                              break label511;
                           }
                        } else {
                           stack.func_77964_b(0);
                        }
                     }

                     ++i;
                     continue;
                  }

                  player.func_70097_a(DamageSource.field_76376_m, 1.0F);
                  stack.func_77964_b(0);
                  louseUsed = true;
                  break;
               }

               PotionEffect effect;
               ItemStack stack;
               boolean done;
               int potion;
               List list;
               if (!louseUsed && Witchery.Items.BITING_BELT.isBeltWorn(player)) {
                  stack = player.field_71071_by.func_70440_f(1);
                  if (stack != null && stack.func_77942_o()) {
                     done = false;
                     if (stack.func_77978_p().func_74764_b("WITCPotion")) {
                        potion = stack.func_77978_p().func_74762_e("WITCPotion");
                        list = Items.field_151068_bn.func_77834_f(potion);
                        if (list != null && !list.isEmpty()) {
                           effect = new PotionEffect((PotionEffect)list.get(0));
                           if (!player.func_82165_m(effect.func_76456_a()) && effect.func_76456_a() != Potion.field_76428_l.field_76415_H) {
                              done = true;
                              if (isPotionAggressive(effect.func_76456_a()) && event.source.func_76346_g() != null && event.source.func_76346_g() instanceof EntityLivingBase) {
                                 ((EntityLivingBase)event.source.func_76346_g()).func_70690_d(effect);
                              } else {
                                 player.func_70690_d(effect);
                              }

                              player.func_70097_a(DamageSource.field_76376_m, 1.0F);
                              stack.func_77978_p().func_82580_o("WITCPotion");
                              if (stack.func_77978_p().func_82582_d()) {
                                 stack.func_77982_d((NBTTagCompound)null);
                              }
                           }
                        }
                     }

                     if (!done && stack.func_77978_p().func_74764_b("WITCPotion2")) {
                        potion = stack.func_77978_p().func_74762_e("WITCPotion2");
                        list = Items.field_151068_bn.func_77834_f(potion);
                        if (list != null && !list.isEmpty()) {
                           effect = new PotionEffect((PotionEffect)list.get(0));
                           if (!player.func_82165_m(effect.func_76456_a()) && effect.func_76456_a() != Potion.field_76428_l.field_76415_H) {
                              if (isPotionAggressive(effect.func_76456_a()) && event.source.func_76346_g() != null && event.source.func_76346_g() instanceof EntityLivingBase) {
                                 ((EntityLivingBase)event.source.func_76346_g()).func_70690_d(effect);
                              } else {
                                 player.func_70690_d(effect);
                              }

                              player.func_70097_a(DamageSource.field_76376_m, 1.0F);
                              stack.func_77978_p().func_82580_o("WITCPotion2");
                              if (stack.func_77978_p().func_82582_d()) {
                                 stack.func_77982_d((NBTTagCompound)null);
                              }
                           }
                        }
                     }
                  }
               }

               this.checkForRendArmor(event);
               if (!ignoreProtection && !playerEx.isVampire()) {
                  ItemPoppet var10000 = Witchery.Items.POPPET;
                  stack = ItemPoppet.findBoundPoppetInWorld(Witchery.Items.POPPET.vampiricPoppet, player, 66, true, false);
                  if (stack != null) {
                     EntityWitchHunter.blackMagicPerformed(player);
                     EntityLivingBase targetEntity = Witchery.Items.TAGLOCK_KIT.getBoundEntity(player.field_70170_p, player, stack, 2);
                     if (targetEntity != null && !Witchery.Items.POPPET.voodooProtectionActivated(player, stack, targetEntity, true, false) && !ItemHunterClothes.isFullSetWorn(targetEntity, false)) {
                        if (targetEntity instanceof EntityPlayer) {
                           targetEntity.func_70097_a(event.source, event.ammount);
                           event.setCanceled(true);
                        } else if (targetEntity instanceof EntityLiving && targetEntity.func_70089_S()) {
                           targetEntity.func_70097_a(event.source, Math.min(event.ammount, 15.0F));
                           if (!targetEntity.func_70089_S()) {
                              Witchery.Items.TAGLOCK_KIT.clearTaglock(stack, 2);
                           }

                           event.setCanceled(true);
                        }

                        return;
                     }
                  }
               }

               if (!louseUsed) {
                  i = 0;

                  while(true) {
                     var10001 = player.field_71071_by;
                     if (i >= InventoryPlayer.func_70451_h()) {
                        break;
                     }

                     stack = player.field_71071_by.func_70301_a(i);
                     if (stack != null && stack.func_77973_b() == Witchery.Items.PARASYTIC_LOUSE && stack.func_77960_j() > 0) {
                        list = Items.field_151068_bn.func_77834_f(stack.func_77960_j());
                        if (list != null && !list.isEmpty()) {
                           effect = new PotionEffect((PotionEffect)list.get(0));
                           if (effect.func_76456_a() == Potion.field_76428_l.field_76415_H) {
                              player.func_70690_d(effect);
                           }

                           player.func_70097_a(DamageSource.field_76376_m, 1.0F);
                           stack.func_77964_b(0);
                           louseUsed = true;
                           break;
                        }

                        stack.func_77964_b(0);
                     }

                     ++i;
                  }
               }

               if (!louseUsed && Witchery.Items.BITING_BELT.isBeltWorn(player)) {
                  stack = player.field_71071_by.func_70440_f(1);
                  if (stack != null && stack.func_77942_o()) {
                     done = false;
                     if (stack.func_77978_p().func_74764_b("WITCPotion")) {
                        potion = stack.func_77978_p().func_74762_e("WITCPotion");
                        list = Items.field_151068_bn.func_77834_f(potion);
                        if (list != null && !list.isEmpty()) {
                           effect = new PotionEffect((PotionEffect)list.get(0));
                           if (!player.func_82165_m(effect.func_76456_a()) && effect.func_76456_a() == Potion.field_76428_l.field_76415_H) {
                              done = true;
                              player.func_70690_d(effect);
                              player.func_70097_a(DamageSource.field_76376_m, 1.0F);
                              stack.func_77978_p().func_82580_o("WITCPotion");
                              if (stack.func_77978_p().func_82582_d()) {
                                 stack.func_77982_d((NBTTagCompound)null);
                              }
                           }
                        }
                     }

                     if (!done && stack.func_77978_p().func_74764_b("WITCPotion2")) {
                        potion = stack.func_77978_p().func_74762_e("WITCPotion2");
                        list = Items.field_151068_bn.func_77834_f(potion);
                        if (list != null && !list.isEmpty()) {
                           effect = new PotionEffect((PotionEffect)list.get(0));
                           if (!player.func_82165_m(effect.func_76456_a()) && effect.func_76456_a() == Potion.field_76428_l.field_76415_H) {
                              player.func_70690_d(effect);
                              player.func_70097_a(DamageSource.field_76376_m, 1.0F);
                              stack.func_77978_p().func_82580_o("WITCPotion2");
                              if (stack.func_77978_p().func_82582_d()) {
                                 stack.func_77982_d((NBTTagCompound)null);
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (healthAfterDamage <= 0.0F && !wolfForm && !playerEx.isVampire()) {
               Log.instance().debug(String.format("player terminal damage"));
               if (!event.source.field_76373_n.equals(DamageSource.field_76379_h.field_76373_n) && !event.source.field_76373_n.equals(DamageSource.field_82729_p.field_76373_n)) {
                  if (!event.source.func_76347_k() && !event.source.func_94541_c()) {
                     if (event.source.field_76373_n.equals(DamageSource.field_76369_e.field_76373_n)) {
                        Witchery.Items.POPPET.cancelEventIfPoppetFound(player, Witchery.Items.POPPET.waterPoppet, event, true);
                        if (event.isCanceled()) {
                           player.func_70690_d(new PotionEffect(Potion.field_76427_o.field_76415_H, 60, 0));
                        }
                     } else if (event.source.field_76373_n.equals(DamageSource.field_76366_f.field_76373_n)) {
                        Witchery.Items.POPPET.cancelEventIfPoppetFound(player, Witchery.Items.POPPET.foodPoppet, event, true);
                        if (event.isCanceled()) {
                           player.func_70690_d(new PotionEffect(Potion.field_76443_y.field_76415_H, 60, 0));
                        }
                     }
                  } else {
                     Witchery.Items.POPPET.cancelEventIfPoppetFound(player, Witchery.Items.POPPET.firePoppet, event, true);
                     if (event.isCanceled()) {
                        player.func_70690_d(new PotionEffect(Potion.field_76426_n.field_76415_H, 60, 0));
                     }
                  }
               } else {
                  Witchery.Items.POPPET.cancelEventIfPoppetFound(player, Witchery.Items.POPPET.earthPoppet, event, false);
               }

               if (!event.isCanceled()) {
                  Witchery.Items.POPPET.cancelEventIfPoppetFound(player, Witchery.Items.POPPET.deathPoppet, event, true, ignoreProtection);
                  if (event.isCanceled()) {
                     if (!player.func_70027_ad() && !event.source.func_76347_k() && !event.source.func_94541_c()) {
                        if (event.source.field_76373_n.equals(DamageSource.field_76369_e.field_76373_n)) {
                           player.func_70690_d(new PotionEffect(Potion.field_76427_o.field_76415_H, 120, 0));
                        } else if (event.source.field_76373_n.equals(DamageSource.field_76366_f.field_76373_n)) {
                           player.func_70690_d(new PotionEffect(Potion.field_76443_y.field_76415_H, 120, 0));
                        }
                     } else {
                        player.func_70690_d(new PotionEffect(Potion.field_76426_n.field_76415_H, 120, 0));
                     }
                  }
               }
            }

            if (!event.isCanceled() && healthAfterDamage <= 2.0F && event.source.field_76373_n.equals(DamageSource.field_76366_f.field_76373_n)) {
               Witchery.Items.POPPET.cancelEventIfPoppetFound(player, Witchery.Items.POPPET.foodPoppet, event, true);
               if (event.isCanceled()) {
                  player.func_70690_d(new PotionEffect(Potion.field_76443_y.field_76415_H, 60, 0));
               }
            }

            Familiar.handlePlayerHurt(event, player);
            this.checkForWolfInfection(event, healthAfterDamage);
            Witchery.Items.POPPET.checkForArmorProtection(player);
         } else {
            if (event.entityLiving instanceof EntityGoblin && event.source == DamageSource.field_76379_h) {
               event.setCanceled(true);
               return;
            }

            if (event.entityLiving instanceof EntityVillager && event.source != null && event.source.func_76346_g() != null && (event.source.func_76346_g() instanceof EntityVillageGuard || event.source.func_76346_g() instanceof EntityWitchHunter)) {
               event.setCanceled(true);
               return;
            }

            if (Config.instance().isReduceZombeVillagerDamageActive() && event.entityLiving instanceof EntityVillager && event.source.func_76346_g() != null && event.source.func_76346_g() instanceof EntityZombie) {
               event.ammount = 0.5F;
            }

            this.checkForRendArmor(event);
            this.checkForWolfInfection(event, EntityUtil.getHealthAfterDamage(event, event.entityLiving.func_110143_aJ(), event.entityLiving));
         }
      }

   }

   public void checkForRendArmor(LivingHurtEvent event) {
      if (event.source.field_76373_n.equals("player") && event.source.func_76346_g() != null && event.source.func_76346_g() instanceof EntityPlayer) {
         EntityPlayer attackingPlayer = (EntityPlayer)event.source.func_76346_g();
         ExtendedPlayer playerEx = ExtendedPlayer.get(attackingPlayer);
         Shapeshift.INSTANCE.rendArmor(event.entityLiving, attackingPlayer, playerEx);
      }

   }

   public void checkForWolfInfection(LivingHurtEvent event, float health) {
      if (!event.isCanceled()) {
         if (event.source.field_76373_n.equals("player") && event.source.func_76364_f() != null && event.source.func_76364_f() instanceof EntityPlayer) {
            EntityPlayer attackingPlayer = (EntityPlayer)event.source.func_76346_g();
            ExtendedPlayer playerEx = ExtendedPlayer.get(attackingPlayer);
            Shapeshift.INSTANCE.processWolfInfection(event.entityLiving, attackingPlayer, playerEx, health);
         } else if (event.source.field_76373_n.equals("mob") && event.source.func_76364_f() instanceof EntityWolfman) {
            Shapeshift.INSTANCE.processWolfInfection(event.entityLiving, (EntityWolfman)event.source.func_76364_f(), health);
         }
      }

   }

   public void checkForChargeDamage(LivingHurtEvent event) {
      if (event.source.field_76373_n.equals("player") && event.source.func_76346_g() != null && event.source.func_76346_g() instanceof EntityPlayer) {
         EntityPlayer attackingPlayer = (EntityPlayer)event.source.func_76346_g();
         ExtendedPlayer playerEx = ExtendedPlayer.get(attackingPlayer);
         Shapeshift.INSTANCE.updateChargeDamage(event, attackingPlayer, playerEx);
      }

   }

   private static boolean isPotionAggressive(int potionID) {
      return potionID == Potion.field_76419_f.field_76415_H || potionID == Potion.field_76421_d.field_76415_H || potionID == Potion.field_76436_u.field_76415_H || potionID == Potion.field_82731_v.field_76415_H || potionID == Potion.field_76437_t.field_76415_H || potionID == Potion.field_76438_s.field_76415_H;
   }

   private static void dropItemsOnHit(EntityPlayer player) {
      for(int i = 0; i < player.field_71071_by.field_70462_a.length; ++i) {
         ItemStack stack = player.field_71071_by.field_70462_a[i];
         if (Witchery.Items.GENERIC.itemBatBall.isMatch(stack)) {
            player.func_71019_a(stack, true);
            player.field_71071_by.field_70462_a[i] = null;
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onLivingDeath(LivingDeathEvent event) {
      EntityPlayer player;
      ExtendedPlayer playerEx;
      if (event.entityLiving.field_70170_p.field_72995_K && !event.isCanceled()) {
         if (event.entityLiving instanceof EntityPlayer) {
            player = (EntityPlayer)event.entityLiving;
            playerEx = ExtendedPlayer.get(player);
            if (playerEx.isVampire()) {
               event.setCanceled(true);
               player.func_70606_j(1.0F);
               return;
            }
         }
      } else if (!event.entityLiving.field_70170_p.field_72995_K && (!event.isCancelable() || !event.isCanceled())) {
         if (event.entityLiving instanceof EntityPlayer) {
            player = (EntityPlayer)event.entityLiving;
            playerEx = ExtendedPlayer.get(player);
            if (playerEx.isVampire()) {
               if (player.func_110143_aJ() > 0.0F) {
                  event.setCanceled(true);
                  return;
               }

               if (!CreatureUtil.checkForVampireDeath(player, event.source)) {
                  event.setCanceled(true);
                  return;
               }
            }
         }

         this.dropExtraItemsFromNBT(event);
         Entity attacker = event.source.func_76346_g();
         if (attacker != null && attacker instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)attacker;
            ExtendedPlayer playerEx = ExtendedPlayer.get(player);
            if (event.entity instanceof EntityHornedHuntsman && playerEx.getWolfmanQuestState() == ExtendedPlayer.QuestState.STARTED) {
               playerEx.setWolfmanQuestState(ExtendedPlayer.QuestState.COMPLETE);
            }

            if (playerEx.hasVampireBook()) {
               boolean dropPage = event.entityLiving instanceof IBossDisplayData || (event.entityLiving instanceof EntityPigZombie || event.entityLiving instanceof EntityEnderman) && player.field_70170_p.field_73012_v.nextDouble() < 0.09D || PotionParalysis.isVillager(event.entityLiving) && player.field_70170_p.field_73012_v.nextDouble() < 0.1D || event.entityLiving.func_70662_br() && player.field_70170_p.field_73012_v.nextDouble() < 0.02D;
               if (dropPage) {
                  EntityItem entityItem = new EntityItem(event.entityLiving.field_70170_p, event.entityLiving.field_70165_t, event.entityLiving.field_70163_u + 1.0D, event.entityLiving.field_70161_v, Witchery.Items.GENERIC.itemVampireBookPage.createStack());
                  event.entityLiving.field_70170_p.func_72838_d(entityItem);
               }
            }
         }

         Entity entitySource = event.source.func_76364_f();
         if (entitySource != null && entitySource instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entitySource;
            ExtendedPlayer playerEx = ExtendedPlayer.get(player);
            boolean hasArthana = player.field_71071_by.func_70448_g() != null && player.field_71071_by.func_70448_g().func_77973_b() == Witchery.Items.ARTHANA;
            boolean var10000;
            if (player.field_71071_by.func_70448_g() != null && player.field_71071_by.func_70448_g().func_77973_b() == Witchery.Items.CANE_SWORD && Witchery.Items.CANE_SWORD.isDrawn((EntityLivingBase)player) && playerEx.isVampire()) {
               var10000 = true;
            } else {
               var10000 = false;
            }

            ItemStack itemstack = null;
            Shapeshift.INSTANCE.processCreatureKilled(event, player, playerEx);
            if (playerEx.getWerewolfLevel() == 5 && Shapeshift.INSTANCE.isWolfAnimalForm(playerEx) && playerEx.getWolfmanQuestState() == ExtendedPlayer.QuestState.STARTED) {
               if (event.entity instanceof IMob && !player.field_70122_E) {
                  playerEx.increaseWolfmanQuestCounter();
               }
            } else if (playerEx.getWerewolfLevel() == 8 && playerEx.getCreatureType() == TransformCreature.WOLFMAN && playerEx.getWolfmanQuestState() == ExtendedPlayer.QuestState.STARTED) {
               if (event.entity instanceof EntityPigZombie) {
                  playerEx.increaseWolfmanQuestCounter();
               }
            } else if (playerEx.getWerewolfLevel() == 9 && Shapeshift.INSTANCE.isWolfAnimalForm(playerEx) && playerEx.getWolfmanQuestState() == ExtendedPlayer.QuestState.STARTED && (event.entity instanceof EntityVillager || event.entity instanceof EntityPlayer)) {
               playerEx.increaseWolfmanQuestCounter();
            }

            if (playerEx.getVampireLevel() == 5 && playerEx.canIncreaseVampireLevel() && event.entity instanceof EntityBlaze) {
               if (playerEx.getVampireQuestCounter() >= 19) {
                  playerEx.increaseVampireLevel();
               } else {
                  playerEx.increaseVampireQuestCounter();
               }
            }

            int baseLooting = EnchantmentHelper.func_77519_f(player);
            double lootingFactor = 1.0D + (double)baseLooting;
            double halfLooting = 1.0D + (double)(baseLooting / 2);
            if (InfusedBrewEffect.getActiveBrew(player) == InfusedBrewEffect.Grave) {
               float maxHealth = player.func_110138_aP();
               if (event.entityLiving instanceof EntityPlayer) {
                  player.func_71024_bL().func_75122_a(20, 0.9F);
                  player.func_70691_i(maxHealth * 0.6F);
               } else if (event.entityLiving instanceof EntityVillager) {
                  player.func_71024_bL().func_75122_a(20, 0.9F);
                  player.func_70691_i(maxHealth * 0.4F);
               } else if (event.entityLiving instanceof EntityAnimal) {
                  player.func_71024_bL().func_75122_a(8, 0.8F);
                  player.func_70691_i(maxHealth * 0.1F);
               }
            }

            Witchery.Items.BLOOD_GOBLET.handleCreatureDeath(player.field_70170_p, player, event.entityLiving);
            boolean allowDrops = !EntityUtil.isNoDrops(event.entityLiving);
            if (allowDrops) {
               if (event.entityLiving instanceof EntityVillager) {
                  ExtendedVillager villagerEx = ExtendedVillager.get((EntityVillager)event.entityLiving);
                  playerEx.fillBloodReserve(villagerEx.getBlood());
               } else if (event.entityLiving instanceof EntityVillageGuard) {
                  EntityVillageGuard guard = (EntityVillageGuard)event.entityLiving;
                  playerEx.fillBloodReserve(guard.getBlood());
               } else if (event.entityLiving instanceof EntityPlayer) {
                  ExtendedPlayer targetEx = ExtendedPlayer.get((EntityPlayer)event.entityLiving);
                  playerEx.fillBloodReserve(targetEx.getHumanBlood());
               } else if (event.entityLiving instanceof EntitySkeleton) {
                  EntitySkeleton skeleton = (EntitySkeleton)event.entityLiving;
                  if (hasArthana && skeleton.func_82202_m() == 0 && event.entityLiving.field_70170_p.field_73012_v.nextDouble() <= Math.min(0.05D * lootingFactor, 1.0D)) {
                     itemstack = new ItemStack(Items.field_151144_bL, 1, 0);
                  } else if (hasArthana && event.entityLiving.field_70170_p.field_73012_v.nextDouble() <= Math.min(0.04D * lootingFactor, 1.0D)) {
                     itemstack = Witchery.Items.GENERIC.itemSpectralDust.createStack();
                  }
               } else if (event.entityLiving instanceof EntityZombie) {
                  if (hasArthana && event.entityLiving.field_70170_p.field_73012_v.nextDouble() <= Math.min(0.02D * lootingFactor, 1.0D)) {
                     itemstack = new ItemStack(Items.field_151144_bL, 1, 2);
                  } else if (hasArthana && event.entityLiving.field_70170_p.field_73012_v.nextDouble() <= Math.min(0.03D * lootingFactor, 1.0D)) {
                     itemstack = Witchery.Items.GENERIC.itemSpectralDust.createStack();
                  }
               } else if (event.entityLiving instanceof EntityCreeper) {
                  if (hasArthana && event.entityLiving.field_70170_p.field_73012_v.nextDouble() <= Math.min(0.01D * lootingFactor, 1.0D)) {
                     itemstack = new ItemStack(Items.field_151144_bL, 1, 4);
                  } else if (event.entityLiving.field_70170_p.field_73012_v.nextDouble() <= Math.min((hasArthana ? 0.08D : 0.02D) * lootingFactor, 1.0D)) {
                     itemstack = Witchery.Items.GENERIC.itemCreeperHeart.createStack();
                  }
               } else if (event.entityLiving instanceof EntityDemon) {
                  if (hasArthana && event.entityLiving.field_70170_p.field_73012_v.nextDouble() <= Math.min(0.33D * halfLooting, 1.0D)) {
                     itemstack = Witchery.Items.GENERIC.itemDemonHeart.createStack();
                  }
               } else if (event.entityLiving instanceof EntityPlayer) {
                  if (hasArthana && event.entityLiving.field_70170_p.field_73012_v.nextDouble() <= Math.min(0.1D * halfLooting, 1.0D)) {
                     EntityPlayer victim = (EntityPlayer)event.entityLiving;
                     itemstack = new ItemStack(Items.field_151144_bL, 1, 3);
                     NBTTagCompound tag = itemstack.func_77978_p();
                     if (tag == null) {
                        tag = new NBTTagCompound();
                        itemstack.func_77982_d(tag);
                     }

                     tag.func_74778_a("SkullOwner", victim.func_70005_c_());
                  }
               } else if (event.entityLiving instanceof EntityBat) {
                  if (player.field_70170_p.field_73012_v.nextDouble() <= (hasArthana ? (baseLooting > 0 ? 1.0D : 0.75D) : 0.33D)) {
                     itemstack = Witchery.Items.GENERIC.itemBatWool.createStack();
                  }
               } else if (event.entityLiving instanceof EntityWolf) {
                  if (player.field_70170_p.field_73012_v.nextDouble() <= (hasArthana ? (baseLooting > 0 ? 1.0D : 0.75D) : 0.33D)) {
                     itemstack = Witchery.Items.GENERIC.itemDogTongue.createStack();
                  }

                  if (player.field_70170_p.field_73012_v.nextInt(12) <= Math.min(baseLooting, 3)) {
                     event.entityLiving.func_70099_a(new ItemStack(Witchery.Blocks.WOLFHEAD, 1, 0), 0.0F);
                  }
               } else if (event.entityLiving instanceof EntityOwl) {
                  if (!((EntityOwl)event.entityLiving).isTemp() && player.field_70170_p.field_73012_v.nextDouble() <= (hasArthana ? (baseLooting > 0 ? 1.0D : 0.5D) : 0.2D)) {
                     itemstack = Witchery.Items.GENERIC.itemOwletsWing.createStack();
                  }
               } else if (event.entityLiving instanceof EntitySheep) {
                  if (CreatureUtil.isWerewolf(entitySource, false) && !((EntitySheep)event.entityLiving).func_70631_g_() && event.entityLiving.field_70170_p.field_73012_v.nextInt(4) != 0) {
                     itemstack = Witchery.Items.GENERIC.itemMuttonRaw.createStack();
                  }
               } else if (event.entityLiving instanceof EntityToad) {
                  if (!((EntityToad)event.entityLiving).isTemp() && player.field_70170_p.field_73012_v.nextDouble() <= (hasArthana ? (baseLooting > 0 ? 1.0D : 0.5D) : 0.2D)) {
                     itemstack = Witchery.Items.GENERIC.itemToeOfFrog.createStack();
                  }
               } else {
                  try {
                     Class theClass = event.entityLiving.getClass();
                     if (theClass != null) {
                        String name = theClass.getSimpleName();
                        if (name != null && !name.isEmpty()) {
                           String upperName = name.toUpperCase(Locale.ROOT);
                           if (!upperName.contains("WOLF") && !name.contains("Dog") && !name.contains("Fox")) {
                              if ((upperName.contains("FIREBAT") || name.contains("Bat")) && player.field_70170_p.field_73012_v.nextDouble() <= (hasArthana ? (baseLooting > 0 ? 1.0D : 0.75D) : 0.33D)) {
                                 itemstack = Witchery.Items.GENERIC.itemBatWool.createStack();
                              }
                           } else {
                              if (player.field_70170_p.field_73012_v.nextDouble() <= (hasArthana ? (baseLooting > 0 ? 1.0D : 0.75D) : 0.33D)) {
                                 itemstack = Witchery.Items.GENERIC.itemDogTongue.createStack();
                              }

                              if ((upperName.contains("WOLF") || name.contains("Dog")) && player.field_70170_p.field_73012_v.nextInt(12) <= Math.min(baseLooting, 3)) {
                                 event.entityLiving.func_70099_a(new ItemStack(Witchery.Blocks.WOLFHEAD, 1, 0), 0.0F);
                              }
                           }
                        }
                     }
                  } catch (Exception var18) {
                     Log.instance().debug(String.format("Exception occurred while determining dead creature type: %s", var18.toString()));
                  }
               }
            }

            if (itemstack != null) {
               EntityItem entityItem = new EntityItem(event.entityLiving.field_70170_p, event.entityLiving.field_70165_t, event.entityLiving.field_70163_u + 1.0D, event.entityLiving.field_70161_v, itemstack);
               event.entityLiving.field_70170_p.func_72838_d(entityItem);
            }
         }
      }

   }

   private void dropExtraItemsFromNBT(LivingDeathEvent event) {
      if (!event.entityLiving.field_70170_p.field_72995_K) {
         NBTTagCompound nbtEntityData = event.entityLiving.getEntityData();
         if (nbtEntityData.func_74764_b("WITCExtraDrops")) {
            NBTTagList nbtExtraDrops = nbtEntityData.func_150295_c("WITCExtraDrops", 10);

            for(int i = 0; i < nbtExtraDrops.func_74745_c(); ++i) {
               NBTTagCompound nbtTag = nbtExtraDrops.func_150305_b(i);
               ItemStack extraStack = ItemStack.func_77949_a(nbtTag);
               if (extraStack != null) {
                  EntityItem entityItem = new EntityItem(event.entityLiving.field_70170_p, event.entityLiving.field_70165_t, event.entityLiving.field_70163_u + 1.0D, event.entityLiving.field_70161_v, extraStack);
                  event.entityLiving.field_70170_p.func_72838_d(entityItem);
               }
            }
         }
      }

   }
}
