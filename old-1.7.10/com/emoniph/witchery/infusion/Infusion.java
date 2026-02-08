package com.emoniph.witchery.infusion;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.dimension.WorldProviderDreamWorld;
import com.emoniph.witchery.dimension.WorldProviderTorment;
import com.emoniph.witchery.entity.EntityCovenWitch;
import com.emoniph.witchery.entity.EntityDemon;
import com.emoniph.witchery.entity.EntityIllusion;
import com.emoniph.witchery.entity.EntityIllusionCreeper;
import com.emoniph.witchery.entity.EntityIllusionSpider;
import com.emoniph.witchery.entity.EntityIllusionZombie;
import com.emoniph.witchery.entity.EntityNightmare;
import com.emoniph.witchery.entity.EntityVillageGuard;
import com.emoniph.witchery.entity.EntityWitchHunter;
import com.emoniph.witchery.entity.ai.EntityAIDigBlocks;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePower;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.item.ItemHunterClothes;
import com.emoniph.witchery.network.PacketPlayerStyle;
import com.emoniph.witchery.network.PacketPlayerSync;
import com.emoniph.witchery.predictions.PredictionManager;
import com.emoniph.witchery.ritual.rites.RiteProtectionCircleRepulsive;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.Dye;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

public class Infusion {
   public static final Infusion DEFUSED = new Infusion(0);
   public static final String INFUSION_CHARGES_KEY = "witcheryInfusionCharges";
   public static final String INFUSION_ID_KEY = "witcheryInfusionID";
   public static final String INFUSION_MAX_CHARGES_KEY = "witcheryInfusionChargesMax";
   public static final String INFUSION_NEXTSYNC = "WITCResyncLook";
   public static final String INFUSION_GROTESQUE = "witcheryGrotesque";
   public static final String INFUSION_DEPTHS = "witcheryDepths";
   public static final String INFUSION_CURSED = "witcheryCursed";
   public static final String INFUSION_INSANITY = "witcheryInsanity";
   public static final String INFUSION_SINKING = "witcherySinking";
   public static final String INFUSION_OVERHEAT = "witcheryOverheating";
   public static final String INFUSION_NIGHTMARE = "witcheryWakingNightmare";
   public final int infusionID;
   protected static final int DEFAULT_CHARGE_COST = 1;

   public static EntityItem dropEntityItemWithRandomChoice(EntityLivingBase entity, ItemStack par1ItemStack, boolean par2) {
      if (par1ItemStack != null && entity != null) {
         if (par1ItemStack.field_77994_a == 0) {
            return null;
         } else {
            EntityItem entityitem = new EntityItem(entity.field_70170_p, entity.field_70165_t, entity.field_70163_u - 0.30000001192092896D + (double)entity.func_70047_e(), entity.field_70161_v, par1ItemStack);
            entityitem.field_145804_b = 40;
            float f = 0.1F;
            float f1;
            if (par2) {
               f1 = entity.field_70170_p.field_73012_v.nextFloat() * 0.5F;
               float f2 = entity.field_70170_p.field_73012_v.nextFloat() * 3.1415927F * 2.0F;
               entityitem.field_70159_w = (double)(-MathHelper.func_76126_a(f2) * f1);
               entityitem.field_70179_y = (double)(MathHelper.func_76134_b(f2) * f1);
               entityitem.field_70181_x = 0.20000000298023224D;
            } else {
               f = 0.3F;
               entityitem.field_70159_w = (double)(-MathHelper.func_76126_a(entity.field_70177_z / 180.0F * 3.1415927F) * MathHelper.func_76134_b(entity.field_70125_A / 180.0F * 3.1415927F) * f);
               entityitem.field_70179_y = (double)(MathHelper.func_76134_b(entity.field_70177_z / 180.0F * 3.1415927F) * MathHelper.func_76134_b(entity.field_70125_A / 180.0F * 3.1415927F) * f);
               entityitem.field_70181_x = (double)(-MathHelper.func_76126_a(entity.field_70125_A / 180.0F * 3.1415927F) * f + 0.1F);
               f = 0.02F;
               f1 = entity.field_70170_p.field_73012_v.nextFloat() * 3.1415927F * 2.0F;
               f *= entity.field_70170_p.field_73012_v.nextFloat();
               entityitem.field_70159_w += Math.cos((double)f1) * (double)f;
               entityitem.field_70181_x += (double)((entity.field_70170_p.field_73012_v.nextFloat() - entity.field_70170_p.field_73012_v.nextFloat()) * 0.1F);
               entityitem.field_70179_y += Math.sin((double)f1) * (double)f;
            }

            entity.field_70170_p.func_72838_d(entityitem);
            return entityitem;
         }
      } else {
         return null;
      }
   }

   public static EntityCreature spawnCreature(World world, Class<? extends EntityCreature> creatureType, EntityLivingBase victim, int minRange, int maxRange, ParticleEffect effect, SoundEffect effectSound) {
      int x = MathHelper.func_76128_c(victim.field_70165_t);
      int y = MathHelper.func_76128_c(victim.field_70163_u);
      int z = MathHelper.func_76128_c(victim.field_70161_v);
      return spawnCreature(world, creatureType, x, y, z, victim, minRange, maxRange, effect, effectSound);
   }

   public static EntityCreature spawnCreature(World world, Class<? extends EntityCreature> creatureType, int x, int y, int z, EntityPlayer victim, int minRange, int maxRange) {
      return spawnCreature(world, creatureType, x, y, z, victim, minRange, maxRange, (ParticleEffect)null, SoundEffect.NONE);
   }

   public static EntityCreature spawnCreature(World world, Class<? extends EntityCreature> creatureType, int x, int y, int z, EntityLivingBase victim, int minRange, int maxRange, ParticleEffect effect, SoundEffect effectSound) {
      if (!world.field_72995_K) {
         int activeRadius = maxRange - minRange;
         int ax = world.field_73012_v.nextInt(activeRadius * 2 + 1);
         if (ax > activeRadius) {
            ax += minRange * 2;
         }

         int nx = x - maxRange + ax;
         int az = world.field_73012_v.nextInt(activeRadius * 2 + 1);
         if (az > activeRadius) {
            az += minRange * 2;
         }

         int nz = z - maxRange + az;

         int ny;
         for(ny = y; !world.func_147437_c(nx, ny, nz) && ny < y + 8; ++ny) {
         }

         while(world.func_147437_c(nx, ny, nz) && ny > 0) {
            --ny;
         }

         int hy;
         for(hy = 0; world.func_147437_c(nx, ny + hy + 1, nz) && hy < 6; ++hy) {
         }

         Log.instance().debug("Creature: hy: " + hy + " (" + nx + "," + ny + "," + nz + ")");
         if (hy >= 2) {
            try {
               Constructor ctor = creatureType.getConstructor(World.class);
               EntityCreature creature = (EntityCreature)ctor.newInstance(world);
               if (victim instanceof EntityPlayer) {
                  EntityPlayer player = (EntityPlayer)victim;
                  if (creature instanceof EntityIllusion) {
                     ((EntityIllusion)creature).setVictim(player.func_70005_c_());
                  } else if (creature instanceof EntityNightmare) {
                     ((EntityNightmare)creature).setVictim(player.func_70005_c_());
                     creature.func_70624_b(victim);
                  }
               }

               creature.func_70012_b(0.5D + (double)nx, 0.05D + (double)ny + 1.0D, 0.5D + (double)nz, 0.0F, 0.0F);
               world.func_72838_d(creature);
               if (effect != null) {
                  effect.send(effectSound, world, 0.5D + (double)nx, 0.05D + (double)ny + 1.0D, 0.5D + (double)nz, 1.0D, (double)creature.field_70131_O, 16);
               }

               return creature;
            } catch (NoSuchMethodException var20) {
            } catch (InvocationTargetException var21) {
            } catch (InstantiationException var22) {
            } catch (IllegalAccessException var23) {
            }
         }
      }

      return null;
   }

   public static boolean isOnCooldown(World world, ItemStack stack) {
      if (!world.field_72995_K) {
         NBTTagCompound nbtTag = stack.func_77978_p();
         if (nbtTag != null && nbtTag.func_74764_b("WITCCooldown")) {
            long currentTime = MinecraftServer.func_130071_aq();
            if (currentTime < nbtTag.func_74763_f("WITCCooldown")) {
               return true;
            }
         }
      }

      return false;
   }

   public static void setCooldown(World world, ItemStack stack, int milliseconds) {
      if (!world.field_72995_K) {
         if (!stack.func_77942_o()) {
            stack.func_77982_d(new NBTTagCompound());
         }

         NBTTagCompound nbtTag = stack.func_77978_p();
         if (nbtTag != null) {
            long currentTime = MinecraftServer.func_130071_aq();
            nbtTag.func_74772_a("WITCCooldown", currentTime + (long)milliseconds);
         }
      }

   }

   public Infusion(int infusionID) {
      this.infusionID = infusionID;
   }

   public void onHurt(World worldObj, EntityPlayer player, LivingHurtEvent event) {
   }

   public void onFalling(World world, EntityPlayer player, LivingFallEvent event) {
   }

   public IIcon getPowerBarIcon(EntityPlayer player, int index) {
      return Blocks.field_150344_f.func_149691_a(0, 0);
   }

   protected boolean consumeCharges(World world, EntityPlayer player, int cost, boolean playFailSound) {
      if (player.field_71075_bZ.field_75098_d) {
         return true;
      } else {
         int charges = getCurrentEnergy(player);
         if (charges - cost < 0) {
            world.func_72956_a(player, "note.snare", 0.5F, 0.4F / ((float)Math.random() * 0.4F + 0.8F));
            this.clearInfusion(player);
            return false;
         } else {
            setCurrentEnergy(player, charges - cost);
            return true;
         }
      }
   }

   public void onUpdate(ItemStack itemstack, World world, EntityPlayer player, int par4, boolean par5) {
   }

   public void onLeftClickEntity(ItemStack itemstack, World world, EntityPlayer player, Entity otherEntity) {
      if (!world.field_72995_K) {
         world.func_72956_a(player, "note.snare", 0.5F, 0.4F / ((float)Math.random() * 0.4F + 0.8F));
      }

   }

   public int getMaxItemUseDuration(ItemStack itemstack) {
      return 400;
   }

   public void onUsingItemTick(ItemStack itemstack, World world, EntityPlayer player, int countdown) {
   }

   public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int countdown) {
      if (!world.field_72995_K) {
         world.func_72956_a(player, "note.snare", 0.5F, 0.4F / ((float)Math.random() * 0.4F + 0.8F));
      }

   }

   public void playSound(World world, EntityPlayer player, String sound) {
      world.func_72956_a(player, sound, 0.5F, 0.4F / ((float)world.field_73012_v.nextDouble() * 0.4F + 0.8F));
   }

   public void playFailSound(World world, EntityPlayer player) {
      this.playSound(world, player, "note.snare");
   }

   public static NBTTagCompound getNBT(Entity player) {
      NBTTagCompound entityData = player.getEntityData();
      if (player.field_70170_p.field_72995_K) {
         return entityData;
      } else {
         NBTTagCompound persistedData = entityData.func_74775_l("PlayerPersisted");
         if (!entityData.func_74764_b("PlayerPersisted")) {
            entityData.func_74782_a("PlayerPersisted", persistedData);
         }

         return persistedData;
      }
   }

   public void infuse(EntityPlayer player, int charges) {
      if (!player.field_70170_p.field_72995_K) {
         NBTTagCompound nbt = getNBT(player);
         nbt.func_74768_a("witcheryInfusionID", this.infusionID);
         nbt.func_74768_a("witcheryInfusionCharges", charges);
         nbt.func_74768_a("witcheryInfusionChargesMax", charges);
         CreaturePower.setCreaturePowerID(player, 0, 0);
         syncPlayer(player.field_70170_p, player);
      }

   }

   private void clearInfusion(EntityPlayer player) {
      if (!player.field_70170_p.field_72995_K) {
         NBTTagCompound nbt = getNBT(player);
         nbt.func_82580_o("witcheryInfusionCharges");
         syncPlayer(player.field_70170_p, player);
      }

   }

   public static void setCurrentEnergy(EntityPlayer player, int currentEnergy) {
      if (!player.field_70170_p.field_72995_K) {
         NBTTagCompound nbt = getNBT(player);
         nbt.func_74768_a("witcheryInfusionCharges", currentEnergy);
         syncPlayer(player.field_70170_p, player);
      }

   }

   public static void syncPlayer(World world, EntityPlayer player) {
      if (!world.field_72995_K) {
         Witchery.packetPipeline.sendTo((IMessage)(new PacketPlayerSync(player)), (EntityPlayer)player);
      }

   }

   public static int getInfusionID(EntityPlayer player) {
      NBTTagCompound nbt = getNBT(player);
      return nbt.func_74764_b("witcheryInfusionID") ? nbt.func_74762_e("witcheryInfusionID") : 0;
   }

   public static int getCurrentEnergy(EntityPlayer player) {
      NBTTagCompound nbt = getNBT(player);
      return nbt.func_74764_b("witcheryInfusionCharges") ? nbt.func_74762_e("witcheryInfusionCharges") : 0;
   }

   public static int getMaxEnergy(EntityPlayer player) {
      NBTTagCompound nbt = getNBT(player);
      return nbt.func_74764_b("witcheryInfusionChargesMax") ? nbt.func_74762_e("witcheryInfusionChargesMax") : 0;
   }

   public static void setEnergy(EntityPlayer player, int infusionID, int currentEnergy, int maxEnergy) {
      if (player.field_70170_p.field_72995_K) {
         NBTTagCompound nbt = getNBT(player);
         nbt.func_74768_a("witcheryInfusionID", infusionID);
         nbt.func_74768_a("witcheryInfusionCharges", currentEnergy);
         nbt.func_74768_a("witcheryInfusionChargesMax", maxEnergy);
      }

   }

   public static void setSinkingCurseLevel(EntityPlayer playerEntity, int sinkingLevel) {
      if (playerEntity.field_70170_p.field_72995_K) {
         NBTTagCompound nbt = getNBT(playerEntity);
         if (nbt.func_74764_b("witcherySinking") && sinkingLevel <= 0) {
            nbt.func_82580_o("witcherySinking");
         }

         nbt.func_74768_a("witcherySinking", sinkingLevel);
      }

   }

   public static int getSinkingCurseLevel(EntityPlayer player) {
      NBTTagCompound nbtTag = getNBT(player);
      return nbtTag.func_74764_b("witcherySinking") ? nbtTag.func_74762_e("witcherySinking") : 0;
   }

   public static boolean aquireEnergy(World world, EntityPlayer player, int cost, boolean showMessages) {
      NBTTagCompound nbtPlayer = getNBT(player);
      return nbtPlayer != null ? aquireEnergy(world, player, nbtPlayer, cost, showMessages) : false;
   }

   public static boolean aquireEnergy(World world, EntityPlayer player, NBTTagCompound nbtPlayer, int cost, boolean showMessages) {
      if (nbtPlayer != null && nbtPlayer.func_74764_b("witcheryInfusionID") && nbtPlayer.func_74764_b("witcheryInfusionCharges")) {
         if (!player.field_71075_bZ.field_75098_d && nbtPlayer.func_74762_e("witcheryInfusionCharges") < cost) {
            if (showMessages) {
               ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.infuse.nocharges");
               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }

            return false;
         } else {
            if (!player.field_71075_bZ.field_75098_d) {
               setCurrentEnergy(player, nbtPlayer.func_74762_e("witcheryInfusionCharges") - cost);
            }

            return true;
         }
      } else {
         if (showMessages) {
            ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.infuse.infusionrequired");
            SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
         }

         return false;
      }
   }

   public static class Registry {
      private static final Infusion.Registry INSTANCE = new Infusion.Registry();
      private final ArrayList<Infusion> registry = new ArrayList();

      public static Infusion.Registry instance() {
         return INSTANCE;
      }

      private Registry() {
      }

      public void add(Infusion infusion) {
         if (infusion.infusionID == this.registry.size() + 1) {
            this.registry.add(infusion);
         } else if (infusion.infusionID > this.registry.size() + 1) {
            for(int i = this.registry.size(); i < infusion.infusionID; ++i) {
               this.registry.add((Object)null);
            }

            this.registry.add(infusion);
         } else {
            Infusion existingInfusion = (Infusion)this.registry.get(infusion.infusionID);
            if (existingInfusion != null) {
               Log.instance().warning(String.format("Creature power %s at id %d is being overwritten by another creature power %s.", existingInfusion, infusion.infusionID, infusion));
            }

            this.registry.set(infusion.infusionID, infusion);
         }

      }

      public Infusion get(EntityPlayer player) {
         int infusionID = Infusion.getInfusionID(player);
         return infusionID > 0 ? (Infusion)this.registry.get(infusionID - 1) : Infusion.DEFUSED;
      }

      public Infusion get(int infusionID) {
         return infusionID > 0 ? (Infusion)this.registry.get(infusionID - 1) : Infusion.DEFUSED;
      }
   }

   public static class EventHooks {
      private boolean isBannedSpiritObject(ItemStack stack) {
         if (stack == null) {
            return false;
         } else {
            Item item = stack.func_77973_b();
            return item == Items.field_151079_bi || item == Items.field_151065_br;
         }
      }

      @SubscribeEvent(
         priority = EventPriority.NORMAL
      )
      public void onEnderTeleport(EnderTeleportEvent event) {
         if (!event.isCanceled() && event.entityLiving != null && !event.entityLiving.field_70170_p.field_72995_K && event.entityLiving instanceof EntityPlayer && ItemHunterClothes.isFullSetWorn(event.entityLiving, false)) {
            event.setCanceled(true);
         }

      }

      @SubscribeEvent(
         priority = EventPriority.NORMAL
      )
      public void FillBucket(FillBucketEvent event) {
         ItemStack result = this.attemptFill(event.world, event.target);
         if (result != null) {
            event.result = result;
            event.setResult(Result.ALLOW);
         }

      }

      private ItemStack attemptFill(World world, MovingObjectPosition p) {
         Block id = world.func_147439_a(p.field_72311_b, p.field_72312_c, p.field_72309_d);
         if (id == Witchery.Blocks.FLOWING_SPIRIT) {
            if (world.func_72805_g(p.field_72311_b, p.field_72312_c, p.field_72309_d) == 0) {
               world.func_147449_b(p.field_72311_b, p.field_72312_c, p.field_72309_d, Blocks.field_150350_a);
               return new ItemStack(Witchery.Items.BUCKET_FLOWINGSPIRIT);
            }
         } else if (id == Witchery.Blocks.HOLLOW_TEARS && world.func_72805_g(p.field_72311_b, p.field_72312_c, p.field_72309_d) == 0) {
            world.func_147449_b(p.field_72311_b, p.field_72312_c, p.field_72309_d, Blocks.field_150350_a);
            return new ItemStack(Witchery.Items.BUCKET_HOLLOWTEARS);
         }

         return null;
      }

      @SubscribeEvent
      public void onLivingDamage(LivingHurtEvent event) {
         if (event.entityLiving != null && event.entityLiving.field_70170_p != null && !event.entityLiving.field_70170_p.field_72995_K && event.entityLiving instanceof EntityPlayer && !event.isCanceled()) {
            EntityPlayer player = (EntityPlayer)event.entityLiving;
            PredictionManager.instance().checkIfFulfilled(player, event);
         }

      }

      @SubscribeEvent
      public void onServerChat(ServerChatEvent event) {
         if (event.player != null && !event.isCanceled() && !event.player.field_70170_p.field_72995_K && event.message != null) {
            Witchery.Items.RUBY_SLIPPERS.trySayTheresNoPlaceLikeHome(event.player, event.message);
         }

      }

      @SubscribeEvent
      public void onHarvestDrops(HarvestDropsEvent event) {
         if (event.harvester != null && event.harvester.field_70170_p != null && !event.harvester.field_70170_p.field_72995_K) {
            PredictionManager.instance().checkIfFulfilled(event.harvester, event);
            PlayerEffects.onHarvestDrops(event.harvester, event);
            EntityAIDigBlocks.onHarvestDrops(event.harvester, event);
         }

         if (!event.world.field_72995_K && event.world.field_73011_w.field_76574_g == Config.instance().dimensionDreamID && !event.isCanceled()) {
            Iterator iterator = event.drops.iterator();

            while(iterator.hasNext()) {
               ItemStack stack = (ItemStack)iterator.next();
               if (stack != null && this.isBannedSpiritObject(stack)) {
                  iterator.remove();
               }
            }
         }

      }

      @SubscribeEvent
      public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.entityLiving != null && event.entityLiving.field_70170_p != null && !event.entityLiving.field_70170_p.field_72995_K && event.entityLiving instanceof EntityPlayer && !event.isCanceled()) {
            EntityPlayer player = (EntityPlayer)event.entityLiving;
            PredictionManager.instance().checkIfFulfilled(player, event);
            PlayerEffects.onInteract(player, event);
         }

      }

      @SubscribeEvent
      public void onLivingUpdate(LivingUpdateEvent event) {
         long counter = event.entityLiving.field_70170_p.func_82737_E();
         if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.entityLiving;
            if (!event.entityLiving.field_70170_p.field_72995_K) {
               long time = TimeUtil.getServerTimeInTicks();
               if (counter % 4L == 0L) {
                  NBTTagCompound nbtPlayer = Infusion.getNBT(player);
                  this.handleBrewGrotesqueEffect(player, nbtPlayer);
                  WorldProviderDreamWorld.updatePlayerEffects(player.field_70170_p, player, nbtPlayer, time, counter);
                  WorldProviderTorment.updatePlayerEffects(player.field_70170_p, player, nbtPlayer, time, counter);
                  if (counter % 20L == 0L) {
                     this.handleSyncEffects(player, nbtPlayer);
                     this.handleBrewDepthsEffect(player, nbtPlayer);
                     this.handleCurseEffects(player, nbtPlayer);
                     this.handleSeepingShoesEffect(player, nbtPlayer);
                     InfusedBrewEffect.checkActiveEffects(player.field_70170_p, player, nbtPlayer, counter % 1200L == 0L, time);
                  }

                  if (counter % 100L == 0L && !event.isCanceled()) {
                     PredictionManager.instance().checkIfFulfilled(player, event);
                     if (Config.instance().allowCovenWitchVisits && nbtPlayer.func_74764_b("WITCCoven") && player.field_70170_p.field_73012_v.nextInt(20) == 0) {
                        ChunkCoordinates coords = player.getBedLocation(player.field_71093_bK);
                        if (coords != null && coords.func_71569_e((int)player.field_70165_t, (int)player.field_70163_u, (int)player.field_70161_v) < 256.0F) {
                           NBTTagList nbtCovenList = nbtPlayer.func_150295_c("WITCCoven", 10);
                           if (nbtCovenList.func_74745_c() > 0) {
                              EntityCovenWitch.summonCovenMember(player.field_70170_p, player, 90);
                           }
                        }
                     }
                  }
               }

               PlayerEffects.onUpdate(player, time);
               if (counter % 100L == 1L) {
                  EntityWitchHunter.handleWitchHunterEffects(player, time);
               }
            }

            this.handleIcySlippersEffect(player);
            this.handleFamiliarFollowerSync(player);
         } else if (!event.entityLiving.field_70170_p.field_72995_K && counter % 20L == 0L) {
            this.handleCurseEffects(event.entityLiving, event.entityLiving.getEntityData());
         }

         if (counter % 100L == 0L) {
            ItemStack belt = event.entityLiving.func_71124_b(2);
            if (belt != null && belt.func_77973_b() == Witchery.Items.BARK_BELT) {
               Block blockID = event.entityLiving.field_70170_p.func_147439_a(MathHelper.func_76128_c(event.entityLiving.field_70165_t), MathHelper.func_76128_c(event.entityLiving.field_70163_u) - 1, MathHelper.func_76128_c(event.entityLiving.field_70161_v));
               if (blockID == Blocks.field_150349_c || blockID == Blocks.field_150391_bh) {
                  int maxChargeLevel = Witchery.Items.BARK_BELT.getMaxChargeLevel(event.entityLiving);
                  int currentChargeLevel = Witchery.Items.BARK_BELT.getChargeLevel(belt);
                  if (currentChargeLevel < maxChargeLevel) {
                     Witchery.Items.BARK_BELT.setChargeLevel(belt, Math.min(currentChargeLevel + 1, maxChargeLevel));
                     event.entityLiving.field_70170_p.func_72956_a(event.entityLiving, "witchery:random.wood_creak", 0.5F, (float)(0.8D + 2.0D * event.entityLiving.field_70170_p.field_73012_v.nextGaussian()));
                  }
               }
            }
         }

      }

      private void handleSeepingShoesEffect(EntityPlayer player, NBTTagCompound nbtTag) {
         if (player.field_70122_E) {
            if (player.func_70644_a(Potion.field_76436_u) || player.func_70644_a(Potion.field_82731_v)) {
               ItemStack shoes = player.func_71124_b(1);
               if (shoes != null && shoes.func_77973_b() == Witchery.Items.SEEPING_SHOES) {
                  boolean poisonRemoved = false;
                  if (player.func_70644_a(Potion.field_76436_u)) {
                     player.func_82170_o(Potion.field_76436_u.field_76415_H);
                     poisonRemoved = true;
                  }

                  if (player.func_70644_a(Potion.field_82731_v)) {
                     player.func_82170_o(Potion.field_82731_v.field_76415_H);
                     poisonRemoved = true;
                  }

                  if (poisonRemoved) {
                     int x = MathHelper.func_76128_c(player.field_70165_t);
                     int z = MathHelper.func_76128_c(player.field_70161_v);
                     int y = MathHelper.func_76128_c(player.field_70163_u);
                     int RADIUS = true;
                     int RADIUS_SQ = true;

                     for(int dx = x - 3; dx <= x + 3; ++dx) {
                        for(int dz = z - 3; dz <= z + 3; ++dz) {
                           for(int dy = y - 1; dy <= y + 1; ++dy) {
                              if (Coord.distanceSq((double)dx, 1.0D, (double)dy, (double)x, 1.0D, (double)dy) <= 9.0D && player.field_70170_p.func_147437_c(dx, dy + 1, dz) && !player.field_70170_p.func_147437_c(dx, dy, dz)) {
                                 ItemDye.applyBonemeal(Dye.BONE_MEAL.createStack(), player.field_70170_p, dx, dy, dz, player);
                              }
                           }
                        }
                     }
                  }

               }
            }
         }
      }

      private void handleSyncEffects(EntityPlayer player, NBTTagCompound nbtPlayer) {
         if (!player.field_70170_p.field_72995_K && nbtPlayer.func_74764_b("WITCResyncLook")) {
            long nextSync = nbtPlayer.func_74763_f("WITCResyncLook");
            if (nextSync <= MinecraftServer.func_130071_aq()) {
               nbtPlayer.func_82580_o("WITCResyncLook");
               Witchery.packetPipeline.sendToDimension(new PacketPlayerStyle(player), player.field_71093_bK);
            }
         }

      }

      private void handleFamiliarFollowerSync(EntityPlayer player) {
         if (!player.field_70170_p.field_72995_K) {
            NBTTagCompound compound = player.getEntityData();
            NBTTagCompound pos;
            if (compound.func_74764_b("WITC_LASTPOS")) {
               pos = compound.func_74775_l("WITC_LASTPOS");
               int lastDimension = pos.func_74762_e("D");
               if (lastDimension != player.field_71093_bK || Math.abs(pos.func_74769_h("X") - player.field_70165_t) > 32.0D || Math.abs(pos.func_74769_h("Z") - player.field_70161_v) > 32.0D) {
                  if (lastDimension != player.field_71093_bK && player.field_71093_bK == -1 || lastDimension == -1) {
                     NBTTagCompound nbtPlayer = Infusion.getNBT(player);
                     nbtPlayer.func_74757_a("WITCVisitedNether", true);
                  }

                  if (Familiar.hasActiveFamiliar(player)) {
                     EntityTameable familiar = Familiar.getFamiliarEntity(player);
                     if (familiar != null && !familiar.func_70906_o()) {
                        int ipx = MathHelper.func_76128_c(player.field_70165_t) - 2;
                        int j = MathHelper.func_76128_c(player.field_70161_v) - 2;
                        int k = MathHelper.func_76128_c(player.field_70121_D.field_72338_b) - 2;
                        boolean done = false;

                        for(int l = 0; l <= 4 && !done; ++l) {
                           for(int i1 = 0; i1 <= 4 && !done; ++i1) {
                              for(int dy = 0; dy <= 4 && !done; ++dy) {
                                 if (player.field_70170_p.func_147439_a(ipx + l, k + dy - 1, j + i1).isSideSolid(player.field_70170_p, ipx + l, k + dy - 1, j + i1, ForgeDirection.UP) && !player.field_70170_p.func_147439_a(ipx + l, k + dy, j + i1).func_149721_r() && !player.field_70170_p.func_147439_a(ipx + l, k + dy + 1, j + i1).func_149721_r()) {
                                    ItemGeneral var10000 = Witchery.Items.GENERIC;
                                    ItemGeneral.teleportToLocation(player.field_70170_p, 0.5D + (double)ipx + (double)l, (double)(k + dy), 0.5D + (double)j + (double)i1, player.field_71093_bK, familiar, true);
                                    done = true;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               pos.func_74780_a("X", player.field_70165_t);
               pos.func_74780_a("Z", player.field_70161_v);
               pos.func_74768_a("D", player.field_71093_bK);
            } else {
               pos = new NBTTagCompound();
               pos.func_74780_a("X", player.field_70165_t);
               pos.func_74780_a("Z", player.field_70161_v);
               pos.func_74768_a("D", player.field_71093_bK);
               pos.func_74757_a("visitedNether", player.field_71093_bK == -1);
            }
         }

      }

      private void handleIcySlippersEffect(EntityPlayer player) {
         ItemStack shoes = player.func_82169_q(0);
         if (shoes != null && shoes.func_77973_b() == Witchery.Items.ICY_SLIPPERS) {
            int k = MathHelper.func_76128_c(player.field_70163_u - 1.0D);

            for(int i = 0; i < 4; ++i) {
               int j = MathHelper.func_76128_c(player.field_70165_t + (double)((float)(i % 2 * 2 - 1) * 0.5F));
               int l = MathHelper.func_76128_c(player.field_70161_v + (double)((float)(i / 2 % 2 * 2 - 1) * 0.5F));
               Block blockID = player.field_70170_p.func_147439_a(j, k, l);
               if (blockID != Blocks.field_150358_i && blockID != Blocks.field_150355_j) {
                  if (blockID == Blocks.field_150356_k || blockID == Blocks.field_150353_l) {
                     player.field_70170_p.func_147449_b(j, k, l, Blocks.field_150343_Z);
                     if (player.field_70170_p.field_73012_v.nextInt(10) == 0) {
                        shoes.func_77972_a(1, player);
                     }
                  }
               } else {
                  player.field_70170_p.func_147449_b(j, k, l, Blocks.field_150432_aD);
               }
            }
         }

      }

      private void handleBrewDepthsEffect(EntityPlayer player, NBTTagCompound nbtTag) {
         if (nbtTag.func_74764_b("witcheryDepths")) {
            int timeLeft = nbtTag.func_74762_e("witcheryDepths");
            if (timeLeft > 0) {
               if (!player.func_70644_a(Potion.field_76427_o)) {
                  player.func_70690_d(new PotionEffect(Potion.field_76427_o.field_76415_H, 6000));
               }

               if (!player.func_70055_a(Material.field_151586_h)) {
                  if (!player.func_70644_a(Potion.field_82731_v)) {
                     player.func_70690_d(new PotionEffect(Potion.field_82731_v.field_76415_H, 100, 1));
                  }
               } else if (player.func_70644_a(Potion.field_82731_v)) {
                  player.func_82170_o(Potion.field_82731_v.field_76415_H);
               }
            }

            --timeLeft;
            if (timeLeft <= 0) {
               nbtTag.func_82580_o("witcheryDepths");
               if (player.func_70644_a(Potion.field_76427_o)) {
                  player.func_82170_o(Potion.field_76427_o.field_76415_H);
               }

               if (player.func_70644_a(Potion.field_76436_u)) {
                  player.func_82170_o(Potion.field_76436_u.field_76415_H);
               }
            } else {
               nbtTag.func_74768_a("witcheryDepths", timeLeft);
            }
         }

      }

      private void handleBrewGrotesqueEffect(EntityPlayer player, NBTTagCompound nbtTag) {
         if (nbtTag.func_74764_b("witcheryGrotesque")) {
            int timeLeft = nbtTag.func_74762_e("witcheryGrotesque");
            if (timeLeft > 0) {
               float radius = 4.0F;
               AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(player.field_70165_t - 4.0D, player.field_70163_u - 4.0D, player.field_70161_v - 4.0D, player.field_70165_t + 4.0D, player.field_70163_u + 4.0D, player.field_70161_v + 4.0D);
               List list = player.field_70170_p.func_72872_a(EntityLiving.class, bounds);
               Iterator iterator = list.iterator();

               while(iterator.hasNext()) {
                  EntityLiving entity = (EntityLiving)iterator.next();
                  boolean victim = !(entity instanceof EntityDemon) && !(entity instanceof IBossDisplayData) && !(entity instanceof EntityGolem) && !(entity instanceof EntityWitch);
                  if (victim && Coord.distance(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, player.field_70165_t, player.field_70163_u, player.field_70161_v) < 4.0D) {
                     RiteProtectionCircleRepulsive.push(player.field_70170_p, entity, player.field_70165_t, player.field_70163_u, player.field_70161_v);
                  }
               }
            }

            --timeLeft;
            if (timeLeft <= 0) {
               nbtTag.func_82580_o("witcheryGrotesque");
               Witchery.packetPipeline.sendToDimension(new PacketPlayerStyle(player), player.field_71093_bK);
            } else {
               nbtTag.func_74768_a("witcheryGrotesque", timeLeft);
            }
         }

      }

      private void handleCurseEffects(EntityLivingBase entity, NBTTagCompound nbtTag) {
         if (entity != null && nbtTag != null) {
            int level;
            if (!(entity instanceof EntityPlayer) && nbtTag.func_74764_b("witcherySinking")) {
               level = nbtTag.func_74762_e("witcherySinking");
               if (level > 0) {
                  if (entity.func_70090_H() || entity instanceof EntityPlayer && !entity.field_70122_E) {
                     if (entity.field_70181_x < 0.0D) {
                        entity.field_70181_x *= 1.0D + Math.min(0.1D * (double)level, 0.4D);
                     } else if (entity.field_70181_x > 0.0D) {
                        entity.field_70181_x *= 1.0D - Math.min(0.1D * (double)level, 0.4D);
                     }
                  }
               } else {
                  nbtTag.func_82580_o("witcherySinking");
               }
            }

            int x;
            if (nbtTag.func_74764_b("witcheryCursed")) {
               level = nbtTag.func_74762_e("witcheryCursed");
               if (level > 0) {
                  if (!entity.func_82165_m(Potion.field_76440_q.field_76415_H) && !entity.func_82165_m(Potion.field_76437_t.field_76415_H) && !entity.func_82165_m(Potion.field_76419_f.field_76415_H) && !entity.func_82165_m(Potion.field_76421_d.field_76415_H) && !entity.func_82165_m(Potion.field_76436_u.field_76415_H) && entity.field_70170_p.field_73012_v.nextInt(20) == 0) {
                     switch(entity.field_70170_p.field_73012_v.nextInt(level >= 5 ? 6 : (level >= 4 ? 5 : (level >= 3 ? 4 : (level >= 2 ? 3 : 2))))) {
                     case 0:
                        entity.func_70690_d(new PotionEffect(Potion.field_76419_f.field_76415_H, 600, Math.min(level - 1, 4)));
                        break;
                     case 1:
                        entity.func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, 600, Math.min(level - 1, 4)));
                        break;
                     case 2:
                        entity.func_70690_d(new PotionEffect(Potion.field_76437_t.field_76415_H, (13 + 2 * level) * 20, Math.min(level - 2, 4)));
                        break;
                     case 3:
                        entity.func_70690_d(new PotionEffect(Potion.field_76440_q.field_76415_H, 5 * level * 20));
                        if (level > 5) {
                           entity.func_70690_d(new PotionEffect(Potion.field_76439_r.field_76415_H, 5 * level * 20));
                        }
                     case 4:
                     default:
                        break;
                     case 5:
                        if (entity instanceof EntityPlayer) {
                           EntityPlayer player = (EntityPlayer)entity;
                           x = player.field_71071_by.field_70461_c;
                           if (player.field_71071_by.field_70462_a[x] != null) {
                              player.func_71019_a(player.field_71071_by.field_70462_a[x], true);
                              player.field_71071_by.field_70462_a[x] = null;
                           }
                        } else {
                           ItemStack heldItem = entity.func_70694_bm();
                           if (heldItem != null) {
                              Infusion.dropEntityItemWithRandomChoice(entity, heldItem, true);
                              entity.func_70062_b(0, (ItemStack)null);
                           }
                        }
                     }
                  }
               } else {
                  nbtTag.func_82580_o("witcheryCursed");
               }
            }

            int y;
            World world;
            if (nbtTag.func_74764_b("witcheryOverheating")) {
               level = nbtTag.func_74762_e("witcheryOverheating");
               if (level > 0) {
                  world = entity.field_70170_p;
                  if (!entity.func_70027_ad() && world.field_73012_v.nextInt(level > 2 ? 20 : (level > 1 ? 25 : 30)) == 0) {
                     x = MathHelper.func_76128_c(entity.field_70165_t);
                     y = MathHelper.func_76128_c(entity.field_70161_v);
                     BiomeGenBase biome = world.func_72807_a(x, y);
                     if ((double)biome.field_76750_F >= 1.5D && (!biome.func_76738_d() || !world.func_72896_J()) && !entity.func_70090_H()) {
                        entity.func_70015_d(Math.min(world.field_73012_v.nextInt(level < 4 ? 2 : level - 1) + 1, 4));
                     }
                  }
               } else {
                  nbtTag.func_82580_o("witcheryOverheating");
               }
            }

            if (nbtTag.func_74764_b("witcheryWakingNightmare") && entity instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)entity;
               int level = nbtTag.func_74762_e("witcheryWakingNightmare");
               if (level > 0 && player.field_71093_bK != Config.instance().dimensionDreamID) {
                  World world = player.field_70170_p;
                  if (world.field_73012_v.nextInt(level > 4 ? 30 : (level > 2 ? 60 : 180)) == 0) {
                     double R = 16.0D;
                     double H = 8.0D;
                     AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(entity.field_70165_t - 16.0D, entity.field_70163_u - 8.0D, entity.field_70161_v - 16.0D, entity.field_70165_t + 16.0D, entity.field_70163_u + 8.0D, entity.field_70161_v + 16.0D);
                     List entities = world.func_72872_a(EntityNightmare.class, bounds);
                     boolean doNothing = false;
                     Iterator i$ = entities.iterator();

                     while(i$.hasNext()) {
                        Object obj = i$.next();
                        EntityNightmare nightmare = (EntityNightmare)obj;
                        if (nightmare.getVictimName().equalsIgnoreCase(player.func_70005_c_())) {
                           doNothing = true;
                           break;
                        }
                     }

                     if (!doNothing) {
                        Infusion.spawnCreature(world, EntityNightmare.class, MathHelper.func_76128_c(player.field_70165_t), MathHelper.func_76128_c(player.field_70163_u), MathHelper.func_76128_c(player.field_70161_v), player, 2, 6);
                     }
                  }
               } else {
                  nbtTag.func_82580_o("witcheryWakingNightmare");
               }
            }

            if (entity instanceof EntityPlayer && nbtTag.func_74764_b("witcheryInsanity")) {
               level = nbtTag.func_74762_e("witcheryInsanity");
               if (level > 0) {
                  world = entity.field_70170_p;
                  x = MathHelper.func_76128_c(entity.field_70165_t);
                  y = MathHelper.func_76128_c(entity.field_70163_u);
                  int z = MathHelper.func_76128_c(entity.field_70161_v);
                  if (world.field_73012_v.nextInt(level > 2 ? 25 : (level > 1 ? 30 : 35)) == 0) {
                     Class<? extends EntityCreature> creatureType = null;
                     switch(world.field_73012_v.nextInt(3)) {
                     case 0:
                     default:
                        creatureType = EntityIllusionCreeper.class;
                        break;
                     case 1:
                        creatureType = EntityIllusionSpider.class;
                        break;
                     case 2:
                        creatureType = EntityIllusionZombie.class;
                     }

                     int MAX_DISTANCE = true;
                     int MIN_DISTANCE = true;
                     Infusion.spawnCreature(world, creatureType, x, y, z, (EntityPlayer)entity, 4, 9);
                  } else if (level >= 4 && world.field_73012_v.nextInt(20) == 0) {
                     SoundEffect sound = SoundEffect.NONE;
                     switch(world.field_73012_v.nextInt(3)) {
                     case 0:
                     case 2:
                     case 3:
                     default:
                        sound = SoundEffect.RANDOM_EXPLODE;
                        break;
                     case 1:
                        sound = SoundEffect.MOB_ENDERMAN_IDLE;
                     }

                     sound.playOnlyTo((EntityPlayer)entity, 1.0F, 1.0F);
                  }
               } else {
                  nbtTag.func_82580_o("witcheryInsanity");
               }
            }
         }

      }

      @SubscribeEvent(
         priority = EventPriority.HIGH
      )
      public void onLivingDeath(LivingDeathEvent event) {
         if (!event.entityLiving.field_70170_p.field_72995_K && !event.isCanceled()) {
            if (event.entityLiving instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)event.entity;
               World world = player.field_70170_p;
               NBTTagCompound nbtTag = Infusion.getNBT(player);
               if (nbtTag.func_74764_b("witcheryDepths")) {
                  nbtTag.func_82580_o("witcheryDepths");
               }

               PlayerEffects.onDeath(player);
            }

            Familiar.handleLivingDeath(event);
         }

      }

      @SubscribeEvent
      public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
         if (event.target != null && event.entityLiving instanceof EntityLiving) {
            EntityLiving aggressorEntity = (EntityLiving)event.entityLiving;
            if (event.target instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)event.target;
               if (player.func_82150_aj()) {
                  if (aggressorEntity.field_70170_p.func_72846_b(aggressorEntity.field_70165_t, aggressorEntity.field_70163_u, aggressorEntity.field_70161_v, 16.0D) != event.target) {
                     aggressorEntity.func_70624_b((EntityLivingBase)null);
                  }
               } else if (aggressorEntity.func_70644_a(Potion.field_76440_q)) {
                  aggressorEntity.func_70624_b((EntityLivingBase)null);
               } else {
                  ItemStack stack;
                  if (aggressorEntity instanceof EntityCreeper) {
                     stack = player.field_71071_by.func_70440_f(2);
                     if (stack != null && stack.func_77973_b() == Witchery.Items.WITCH_ROBES) {
                        aggressorEntity.func_70624_b((EntityLivingBase)null);
                     }
                  } else if (aggressorEntity.func_70662_br()) {
                     if (aggressorEntity instanceof EntityZombie && ExtendedPlayer.get(player).getVampireLevel() >= 10) {
                        aggressorEntity.func_70624_b((EntityLivingBase)null);
                     } else {
                        stack = player.field_71071_by.func_70440_f(2);
                        if (stack != null && stack.func_77973_b() == Witchery.Items.NECROMANCERS_ROBES) {
                           aggressorEntity.func_70624_b((EntityLivingBase)null);
                        }
                     }
                  }
               }
            }

            if (event.target instanceof EntityVillageGuard && event.entityLiving instanceof EntityGolem) {
               aggressorEntity.func_70624_b((EntityLivingBase)null);
            } else if (Config.instance().isZombeIgnoreVillagerActive() && event.target instanceof EntityVillager && event.entityLiving instanceof EntityZombie) {
               aggressorEntity.func_70624_b((EntityLivingBase)null);
            }
         }

      }

      @SubscribeEvent
      public void onLivingFall(LivingFallEvent event) {
         if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.entityLiving;
            Infusion.Registry.instance().get(player).onFalling(player.field_70170_p, player, event);
         }

      }

      @SubscribeEvent
      public void onLivingHurt(LivingHurtEvent event) {
         if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.entityLiving;
            if (event.source.func_76347_k() && event.isCancelable() && !event.isCanceled() && player.func_82169_q(2) != null && player.func_82169_q(2).func_77973_b() == Witchery.Items.DEATH_ROBE) {
               if (!player.func_82165_m(Potion.field_76426_n.field_76415_H)) {
                  player.func_70690_d(new PotionEffect(Potion.field_76426_n.field_76415_H, 100, 0));
               }

               event.setCanceled(true);
            }

            if (!event.isCanceled()) {
               Infusion.Registry.instance().get(player).onHurt(player.field_70170_p, player, event);
            }
         }

      }
   }
}
