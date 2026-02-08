package com.emoniph.witchery.common;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.brewing.potions.PotionBase;
import com.emoniph.witchery.client.renderer.RenderReflection;
import com.emoniph.witchery.entity.EntityAttackBat;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.network.PacketExtendedPlayerSync;
import com.emoniph.witchery.network.PacketPartialExtendedPlayerSync;
import com.emoniph.witchery.network.PacketPlayerStyle;
import com.emoniph.witchery.network.PacketSelectPlayerAbility;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import com.emoniph.witchery.util.TransformCreature;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.village.Village;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.IExtendedEntityProperties;

public class ExtendedPlayer implements IExtendedEntityProperties {
   private static final String EXT_PROP_NAME = "WitcheryExtendedPlayer";
   private final EntityPlayer player;
   private Hashtable<Integer, PotionEffect> incurablePotionEffectCache = new Hashtable();
   private static final int MAX_SKILL_LEVEL_POTION_BOTTLING = 100;
   private int skillLevelPotionBottling;
   private static final int MAX_SKILL_LEVEL_POTION_THROWING = 100;
   private int skillLevelPotionThrowing;
   public static final int MAX_HUMAN_BLOOD = 500;
   private int creatureType;
   private int werewolfLevel;
   private int vampireLevel;
   private int bloodPower;
   private int bloodReserve;
   private int vampireUltimate;
   private int vampireUltimateCharges;
   private int humanBlood;
   private int wolfmanQuestState;
   private int wolfmanQuestCounter;
   private long lastBoneFind;
   private long lastHowl;
   private ExtendedPlayer.VampirePower selectedVampirePower;
   private int vampireCooldown;
   private int vampireQuestCounter;
   private boolean vampVisionActive;
   private String lastPlayerSkin;
   @SideOnly(Side.CLIENT)
   private ThreadDownloadImageData downloadImageSkin;
   private ResourceLocation locationSkin;
   private NBTTagList cachedInventory;
   private boolean inventoryCanBeRestored;
   private int vampireLevelCap;
   private static final int DEFAULT_ULTIMATE_CHARGES = 5;
   public int highlightTicks;
   public int cachedWorship;
   private final List<Long> visitedChunks;
   private final List<Long> visitedVampireChunks;
   boolean getPlayerData;
   boolean resetSleep;
   int cachedSky;
   private Coord mirrorWorldEntryPoint;
   static final long COOLDOWN_ESCAPE_1_TICKS = (long)TimeUtil.minsToTicks(5);
   static final long COOLDOWN_ESCAPE_2_TICKS = (long)TimeUtil.minsToTicks(60);
   long mirrorWorldEscapeCooldown1;
   long mirrorWorldEscapeCooldown2;

   public static final void register(EntityPlayer player) {
      player.registerExtendedProperties("WitcheryExtendedPlayer", new ExtendedPlayer(player));
   }

   public static final ExtendedPlayer get(EntityPlayer player) {
      return (ExtendedPlayer)player.getExtendedProperties("WitcheryExtendedPlayer");
   }

   public ExtendedPlayer(EntityPlayer player) {
      this.selectedVampirePower = ExtendedPlayer.VampirePower.NONE;
      this.cachedWorship = -1;
      this.visitedChunks = new ArrayList();
      this.visitedVampireChunks = new ArrayList();
      this.mirrorWorldEscapeCooldown1 = Long.MIN_VALUE;
      this.mirrorWorldEscapeCooldown2 = Long.MIN_VALUE;
      this.player = player;
   }

   public void init(Entity entity, World world) {
   }

   public void saveNBTData(NBTTagCompound compound) {
      NBTTagCompound props = new NBTTagCompound();
      props.func_74768_a("PotionBottling", this.skillLevelPotionBottling);
      props.func_74768_a("PotionThrowing", this.skillLevelPotionThrowing);
      props.func_74768_a("CreatureType", this.creatureType);
      props.func_74768_a("WerewolfLevel", this.werewolfLevel);
      props.func_74768_a("WolfmanQuestState", this.wolfmanQuestState);
      props.func_74768_a("WolfmanQuestCounter", this.wolfmanQuestCounter);
      props.func_74772_a("LastBoneFind", this.lastBoneFind);
      props.func_74772_a("LastHowl", this.lastHowl);
      NBTTagList nbtChunks = new NBTTagList();
      Iterator i$ = this.visitedChunks.iterator();

      while(i$.hasNext()) {
         long l = (Long)i$.next();
         NBTTagCompound tag = new NBTTagCompound();
         tag.func_74772_a("Location", l);
         nbtChunks.func_74742_a(tag);
      }

      props.func_74782_a("WolfmanQuestChunks", nbtChunks);
      props.func_74768_a("VampireLevel", this.vampireLevel);
      props.func_74768_a("BloodPower", this.bloodPower);
      props.func_74768_a("HumanBlood", this.humanBlood);
      props.func_74768_a("VampireUltimate", this.vampireUltimate);
      props.func_74768_a("VampireUltimateCharges", this.vampireUltimateCharges);
      props.func_74768_a("VampireLevelCap", this.vampireLevelCap);
      props.func_74768_a("VampireQuestCounter", this.vampireQuestCounter);
      NBTTagList nbtVampireChunks = new NBTTagList();
      Iterator i$ = this.visitedVampireChunks.iterator();

      while(i$.hasNext()) {
         long l = (Long)i$.next();
         NBTTagCompound tag = new NBTTagCompound();
         tag.func_74772_a("Location", l);
         nbtVampireChunks.func_74742_a(tag);
      }

      props.func_74782_a("VampireQuestChunks", nbtVampireChunks);
      props.func_74768_a("BloodReserve", this.bloodReserve);
      props.func_74757_a("VampireVision", this.vampVisionActive);
      if (this.cachedInventory != null) {
         props.func_74782_a("CachedInventory2", this.cachedInventory.func_74737_b());
         props.func_74757_a("CanRestoreInventory", this.inventoryCanBeRestored);
      }

      if (this.mirrorWorldEntryPoint != null) {
         props.func_74782_a("MirrorWorldEntryPoint", this.mirrorWorldEntryPoint.toTagNBT());
      }

      if (this.lastPlayerSkin != null) {
         props.func_74778_a("LastPlayerSkin", this.lastPlayerSkin);
      }

      props.func_74772_a("MirrorEscape1", this.mirrorWorldEscapeCooldown1);
      props.func_74772_a("MirrorEscape2", this.mirrorWorldEscapeCooldown2);
      compound.func_74782_a("WitcheryExtendedPlayer", props);
   }

   public void loadNBTData(NBTTagCompound compound) {
      if (compound.func_74764_b("WitcheryExtendedPlayer")) {
         NBTTagCompound props = (NBTTagCompound)compound.func_74781_a("WitcheryExtendedPlayer");
         this.skillLevelPotionBottling = MathHelper.func_76125_a(props.func_74762_e("PotionBottling"), 0, 100);
         this.skillLevelPotionThrowing = MathHelper.func_76125_a(props.func_74762_e("PotionThrowing"), 0, 100);
         this.creatureType = MathHelper.func_76125_a(props.func_74762_e("CreatureType"), 0, 5);
         this.werewolfLevel = MathHelper.func_76125_a(props.func_74762_e("WerewolfLevel"), 0, 10);
         this.wolfmanQuestState = MathHelper.func_76125_a(props.func_74762_e("WolfmanQuestState"), 0, ExtendedPlayer.QuestState.values().length - 1);
         this.wolfmanQuestCounter = MathHelper.func_76125_a(props.func_74762_e("WolfmanQuestCounter"), 0, 100);
         this.visitedChunks.clear();
         NBTTagList nbtChunks = props.func_150295_c("WolfmanQuestChunks", 10);

         for(int i = 0; i < nbtChunks.func_74745_c(); ++i) {
            this.visitedChunks.add(nbtChunks.func_150305_b(i).func_74763_f("Location"));
         }

         this.lastBoneFind = props.func_74763_f("LastBoneFind");
         this.lastHowl = props.func_74763_f("LastHowl");
         this.vampireLevel = MathHelper.func_76125_a(props.func_74762_e("VampireLevel"), 0, 10);
         this.bloodPower = MathHelper.func_76125_a(props.func_74762_e("BloodPower"), 0, this.getMaxBloodPower());
         this.humanBlood = MathHelper.func_76125_a(props.func_74762_e("HumanBlood"), 0, 500);
         this.vampireUltimate = props.func_74762_e("VampireUltimate");
         this.vampireUltimateCharges = props.func_74762_e("VampireUltimateCharges");
         this.vampireLevelCap = props.func_74762_e("VampireLevelCap");
         this.vampireQuestCounter = props.func_74762_e("VampireQuestCounter");
         NBTTagList nbtVampireChunks = props.func_150295_c("VampireQuestChunks", 10);

         for(int i = 0; i < nbtVampireChunks.func_74745_c(); ++i) {
            this.visitedVampireChunks.add(nbtVampireChunks.func_150305_b(i).func_74763_f("Location"));
         }

         this.bloodReserve = props.func_74762_e("BloodReserve");
         this.vampVisionActive = props.func_74767_n("VampireVision");
         if (props.func_74764_b("CachedInventory2")) {
            this.cachedInventory = props.func_150295_c("CachedInventory2", 10);
            this.inventoryCanBeRestored = props.func_74767_n("CanRestoreInventory");
         }

         if (props.func_74764_b("MirrorWorldEntryPoint")) {
            this.mirrorWorldEntryPoint = Coord.fromTagNBT(props.func_74775_l("MirrorWorldEntryPoint"));
         }

         if (props.func_74764_b("LastPlayerSkin")) {
            this.lastPlayerSkin = props.func_74779_i("LastPlayerSkin");
         }

         this.mirrorWorldEscapeCooldown1 = props.func_74763_f("MirrorEscape1");
         this.mirrorWorldEscapeCooldown2 = props.func_74763_f("MirrorEscape2");
      }

   }

   public void setOtherPlayerSkin(String username) {
      this.lastPlayerSkin = username;
      this.locationSkin = null;
      this.sync();
   }

   public String getOtherPlayerSkin() {
      return this.lastPlayerSkin != null ? this.lastPlayerSkin : "";
   }

   @SideOnly(Side.CLIENT)
   public ResourceLocation getLocationSkin() {
      if (this.locationSkin == null) {
         this.setupCustomSkin();
      }

      return this.locationSkin != null ? this.locationSkin : null;
   }

   @SideOnly(Side.CLIENT)
   private void setupCustomSkin() {
      String ownerName = this.getOtherPlayerSkin();
      if (ownerName != null && !ownerName.isEmpty()) {
         this.locationSkin = AbstractClientPlayer.func_110311_f(ownerName);
         this.downloadImageSkin = getDownloadImageSkin(this.locationSkin, ownerName);
      } else {
         this.locationSkin = null;
         this.downloadImageSkin = null;
      }

   }

   @SideOnly(Side.CLIENT)
   private static ThreadDownloadImageData getDownloadImageSkin(ResourceLocation location, String name) {
      TextureManager texturemanager = Minecraft.func_71410_x().func_110434_K();
      Object object = texturemanager.func_110581_b(location);
      if (object == null) {
         object = new ThreadDownloadImageData((File)null, String.format("http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtils.func_76338_a(name)), RenderReflection.SKIN, new ImageBufferDownload());
         texturemanager.func_110579_a(location, (ITextureObject)object);
      }

      return (ThreadDownloadImageData)object;
   }

   public ResourceLocation getOtherPlayerSkinLocation() {
      return this.getLocationSkin();
   }

   public void cachePlayerInventory() {
      this.inventoryCanBeRestored = true;
   }

   public void backupPlayerInventory() {
      NBTTagList nbtInventory = new NBTTagList();
      this.player.field_71071_by.func_70442_a(nbtInventory);
      this.cachedInventory = nbtInventory;
   }

   public void restorePlayerInventoryFrom(ExtendedPlayer original) {
      if (original != null && this.cachedInventory != null && this.inventoryCanBeRestored) {
         this.player.field_71071_by.func_70443_b(original.cachedInventory);
         this.inventoryCanBeRestored = false;
         this.cachedInventory = null;
      }

   }

   public int getSkillPotionBottling() {
      return this.skillLevelPotionBottling;
   }

   public int increaseSkillPotionBottling() {
      this.skillLevelPotionBottling = Math.min(this.skillLevelPotionBottling + 1, 100);
      if (this.skillLevelPotionBottling == 30 || this.skillLevelPotionBottling == 60 || this.skillLevelPotionBottling == 90) {
         ChatUtil.sendTranslated(this.player, "witchery:brew.skillincrease");
      }

      return this.getSkillPotionBottling();
   }

   public int getSkillPotionThrowing() {
      return this.skillLevelPotionThrowing;
   }

   public int increaseSkillPotionThrowing() {
      this.skillLevelPotionThrowing = Math.min(this.skillLevelPotionThrowing + 1, 100);
      return this.getSkillPotionBottling();
   }

   public int getWerewolfLevel() {
      return this.werewolfLevel;
   }

   public void setWerewolfLevel(int level) {
      if (this.werewolfLevel != level && level >= 0 && level <= 10) {
         this.werewolfLevel = level;
         this.wolfmanQuestState = 0;
         this.wolfmanQuestCounter = 0;
         this.visitedChunks.clear();
         if (this.werewolfLevel == 0 && !this.player.field_70170_p.field_72995_K && (this.creatureType == 1 || this.creatureType == 2)) {
            Shapeshift.INSTANCE.shiftTo(this.player, TransformCreature.NONE);
         }

         this.sync();
      }

   }

   public void increaseWerewolfLevel() {
      if (this.werewolfLevel < 10) {
         this.setWerewolfLevel(this.werewolfLevel + 1);
         Shapeshift.INSTANCE.initCurrentShift(this.player);
      }

   }

   public int getHumanBlood() {
      return this.humanBlood;
   }

   public void setHumanBlood(int blood) {
      if (this.humanBlood != blood) {
         this.humanBlood = MathHelper.func_76125_a(blood, 0, 500);
         if (!this.player.field_70170_p.field_72995_K) {
            Witchery.packetPipeline.sendToAll((IMessage)(new PacketPartialExtendedPlayerSync(this, this.player)));
         }
      }

   }

   public int takeHumanBlood(int quantity, EntityLivingBase attacker) {
      if (!this.player.func_70608_bn()) {
         quantity = (int)Math.ceil((double)(0.66F * (float)quantity));
      }

      int remainder = Math.max(this.humanBlood - quantity, 0);
      int taken = this.humanBlood - remainder;
      this.setHumanBlood(remainder);
      if (this.humanBlood < (int)Math.ceil(250.0D)) {
         this.player.func_70097_a(new EntityDamageSource(DamageSource.field_76376_m.func_76355_l(), attacker), 1.0F);
      } else if (!this.player.func_70608_bn()) {
         this.player.func_70097_a(new EntityDamageSource(DamageSource.field_76376_m.func_76355_l(), attacker), 0.1F);
      }

      return taken;
   }

   public void giveHumanBlood(int quantity) {
      if (this.humanBlood < 500) {
         this.setHumanBlood(this.humanBlood + quantity);
      }

   }

   public int getVampireLevel() {
      return this.vampireLevel;
   }

   public boolean isVampire() {
      return this.getVampireLevel() > 0;
   }

   public void setVampireLevel(int level) {
      if (this.vampireLevel != level && level >= 0 && level <= 10) {
         this.vampireLevel = level;
         this.vampireQuestCounter = 0;
         this.visitedVampireChunks.clear();
         if (this.vampireLevel == 0 && !this.player.field_70170_p.field_72995_K) {
            if (this.creatureType == 3) {
               Shapeshift.INSTANCE.shiftTo(this.player, TransformCreature.NONE);
            } else {
               Shapeshift.INSTANCE.initCurrentShift(this.player);
            }

            this.bloodPower = 0;
            this.humanBlood = 50;
            this.vampireUltimate = 0;
            this.vampireUltimateCharges = 0;
         } else {
            Shapeshift.INSTANCE.initCurrentShift(this.player);
         }

         this.selectedVampirePower = ExtendedPlayer.VampirePower.NONE;
         if (this.vampireLevel == 1) {
            this.bloodPower = 125;
         }

         if (this.vampireLevel > 0) {
            this.humanBlood = 0;
         }

         this.sync();
      }

   }

   public int getMaxBloodPower() {
      return 500 + (this.getWerewolfLevel() >= 2 ? (int)Math.floor((double)this.getVampireLevel() * 0.5D) : this.getVampireLevel()) * 250;
   }

   public int getBloodPower() {
      return this.bloodPower;
   }

   public boolean decreaseBloodPower(int quantity, boolean exact) {
      if (this.player.field_71075_bZ.field_75098_d) {
         return true;
      } else if (this.bloodPower >= (exact ? quantity : 1)) {
         this.setBloodPower(this.bloodPower - quantity);
         return true;
      } else {
         return false;
      }
   }

   public void increaseBloodPower(int quantity) {
      if (this.bloodPower < this.getMaxBloodPower()) {
         this.setBloodPower(this.bloodPower + quantity);
         if (Config.instance().allowVampireQuests && this.getVampireLevel() == 1 && this.getBloodPower() == this.getMaxBloodPower()) {
            this.increaseVampireLevel();
         }
      }

   }

   public void increaseVampireLevel() {
      if (this.vampireLevel < 10) {
         this.setVampireLevel(this.vampireLevel + 1);
         if (!this.player.field_70170_p.field_72995_K) {
            ChatUtil.sendTranslated(EnumChatFormatting.GOLD, this.player, "Your thirst grows stronger!");
            SoundEffect.RANDOM_LEVELUP.playOnlyTo(this.player);
         }
      }

   }

   public void increaseVampireLevelCap(int levelCap) {
      if (levelCap > this.vampireLevelCap) {
         this.vampireLevelCap = Math.max(levelCap, 3);
      }

   }

   public boolean canIncreaseVampireLevel() {
      return Config.instance().allowVampireQuests && this.vampireLevel < this.vampireLevelCap;
   }

   public void increaseBloodPower(int quantity, int maxIncrease) {
      if (this.bloodPower < this.getMaxBloodPower() && this.bloodPower < maxIncrease) {
         this.setBloodPower(Math.min(this.bloodPower + quantity, maxIncrease));
      }

   }

   public void setBloodPower(int bloodLevel) {
      if (this.bloodPower != bloodLevel) {
         this.bloodPower = MathHelper.func_76125_a(bloodLevel, 0, this.getMaxBloodPower());
         this.sync();
      }

   }

   public ExtendedPlayer.VampireUltimate getVampireUltimate() {
      return ExtendedPlayer.VampireUltimate.values()[this.vampireUltimate];
   }

   public void setVampireUltimate(ExtendedPlayer.VampireUltimate skill) {
      this.setVampireUltimate(skill, 5);
   }

   public void setVampireUltimate(ExtendedPlayer.VampireUltimate skill, int charges) {
      this.vampireUltimate = skill.ordinal();
      this.vampireUltimateCharges = charges;
      this.sync();
   }

   public int getVampireUltimateCharges() {
      return this.vampireUltimateCharges;
   }

   public ExtendedPlayer.VampirePower getSelectedVampirePower() {
      return this.selectedVampirePower;
   }

   public int getMaxAvailablePowerOrdinal() {
      return ExtendedPlayer.VampirePower.levels[this.vampireLevel];
   }

   public void useBloodReserve() {
      int temp = this.bloodReserve;
      if (this.bloodPower < this.getMaxBloodPower()) {
         this.bloodReserve = 0;
         this.increaseBloodPower(temp);
      }

   }

   public boolean isBloodReserveReady() {
      return this.bloodReserve > 0;
   }

   public void fillBloodReserve(int quantity) {
      this.bloodReserve = Math.min(this.bloodReserve + quantity, 250);
      this.sync();
   }

   public int getBloodReserve() {
      return this.isVampire() ? this.bloodReserve : 0;
   }

   public void setBloodReserve(int blood) {
      this.bloodReserve = blood;
   }

   public boolean isVampireVisionActive() {
      return this.vampireLevel >= 2 && this.vampVisionActive;
   }

   public void toggleVampireVision() {
      this.vampVisionActive = !this.vampVisionActive;
      if (!this.player.field_70170_p.field_72995_K) {
         if (!this.vampVisionActive) {
            this.player.func_82170_o(Potion.field_76439_r.field_76415_H);
         } else {
            this.player.func_70690_d(new PotionEffect(Potion.field_76439_r.field_76415_H, 400, 0, true));
         }
      }

   }

   public void setSelectedVampirePower(ExtendedPlayer.VampirePower power, boolean syncToServer) {
      if (this.selectedVampirePower != power) {
         this.selectedVampirePower = power;
         this.highlightTicks = this.selectedVampirePower != ExtendedPlayer.VampirePower.NONE ? 100 : 0;
         if (syncToServer && this.player.field_70170_p.field_72995_K) {
            Witchery.packetPipeline.sendToServer(new PacketSelectPlayerAbility(this, false));
         }
      }

   }

   public void triggerSelectedVampirePower() {
      if (!this.player.field_70170_p.field_72995_K) {
         ExtendedPlayer.VampirePower power = this.getSelectedVampirePower();
         if (this.vampireCooldown <= 0) {
            this.vampireCooldown = 10;
            int dimension;
            int i;
            switch(power) {
            case MESMERIZE:
               if (this.player.func_70093_af()) {
                  this.toggleVampireVision();
               }
               break;
            case SPEED:
               if (this.getCreatureType() == TransformCreature.NONE) {
                  PotionEffect effect = this.player.func_70660_b(Potion.field_76424_c);
                  i = effect == null ? 0 : (int)Math.ceil(Math.log((double)(effect.func_76458_c() + 1)) / Math.log(2.0D));
                  if (this.vampireLevel >= 4 && (double)i <= Math.ceil((double)((float)(this.vampireLevel - 3) / 2.0F))) {
                     if (this.decreaseBloodPower(power.INITIAL_COST, true)) {
                        SoundEffect.RANDOM_FIZZ.playOnlyTo(this.player);
                        int level = effect == null ? 2 : (effect.func_76458_c() + 1) * 2;
                        dimension = effect == null ? TimeUtil.secsToTicks(10) : effect.func_76459_b() + 60;
                        this.player.func_70690_d(new PotionEffect(Potion.field_76424_c.field_76415_H, dimension, level - 1, true));
                        this.player.func_70690_d(new PotionEffect(Potion.field_76430_j.field_76415_H, dimension, i + 1, true));
                     } else {
                        SoundEffect.NOTE_SNARE.playOnlyTo(this.player);
                     }
                  } else {
                     SoundEffect.NOTE_SNARE.playOnlyTo(this.player);
                  }
               } else {
                  SoundEffect.NOTE_SNARE.playOnlyTo(this.player);
               }
               break;
            case BAT:
               if (this.vampireLevel >= 7) {
                  if (this.getCreatureType() == TransformCreature.NONE) {
                     if (this.decreaseBloodPower(power.INITIAL_COST, true)) {
                        SoundEffect.RANDOM_FIZZ.playOnlyTo(this.player);
                        Shapeshift.INSTANCE.shiftTo(this.player, TransformCreature.BAT);
                     } else {
                        SoundEffect.NOTE_SNARE.playOnlyTo(this.player);
                     }
                  } else if (this.getCreatureType() == TransformCreature.BAT) {
                     SoundEffect.RANDOM_FIZZ.playOnlyTo(this.player);
                     Shapeshift.INSTANCE.shiftTo(this.player, TransformCreature.NONE);
                  } else {
                     SoundEffect.NOTE_SNARE.playOnlyTo(this.player);
                  }
               } else {
                  SoundEffect.NOTE_SNARE.playOnlyTo(this.player);
               }
               break;
            case ULTIMATE:
               if (this.vampireLevel >= 10 && this.vampireUltimateCharges > 0 && this.getCreatureType() == TransformCreature.NONE) {
                  switch(this.getVampireUltimate()) {
                  case STORM:
                     WorldInfo worldinfo = ((WorldServer)this.player.field_70170_p).func_72912_H();
                     if (!worldinfo.func_76059_o()) {
                        i = (300 + this.player.field_70170_p.field_73012_v.nextInt(600)) * 20;
                        worldinfo.func_76090_f(i);
                        worldinfo.func_76069_a(true);
                        worldinfo.func_76080_g(i);
                        worldinfo.func_76084_b(true);
                        SoundEffect.RANDOM_FIZZ.playOnlyTo(this.player);
                        if (!this.player.field_71075_bZ.field_75098_d) {
                           --this.vampireUltimateCharges;
                           this.sync();
                        }
                     } else {
                        SoundEffect.NOTE_SNARE.playOnlyTo(this.player);
                     }
                     break;
                  case SWARM:
                     for(i = 0; i < 15; ++i) {
                        EntityLiving creature = spawnCreature(this.player.field_70170_p, EntityAttackBat.class, this.player.field_70165_t, this.player.field_70163_u + 3.0D + this.player.field_70170_p.field_73012_v.nextDouble(), this.player.field_70161_v, 1, 4, ParticleEffect.SMOKE, SoundEffect.WITCHERY_RANDOM_POOF);
                        if (creature != null) {
                           EntityAttackBat bat = (EntityAttackBat)creature;
                           bat.setOwner(this.player);
                           bat.func_82236_f(false);
                           NBTTagCompound nbtBat = bat.getEntityData();
                           nbtBat.func_74757_a("WITCNoDrops", true);
                        }
                     }

                     if (!this.player.field_71075_bZ.field_75098_d) {
                        --this.vampireUltimateCharges;
                        this.sync();
                     }
                     break;
                  case FARM:
                     boolean done = false;
                     if (this.player.field_71093_bK != Config.instance().dimensionDreamID) {
                        ChunkCoordinates coords = this.player.getBedLocation(this.player.field_71093_bK);
                        dimension = this.player.field_71093_bK;
                        World world = this.player.field_70170_p;
                        if (coords == null) {
                           coords = this.player.getBedLocation(0);
                           dimension = 0;
                           world = MinecraftServer.func_71276_C().func_71218_a(0);
                           if (coords == null) {
                              for(coords = ((World)world).func_72861_E(); ((World)world).func_147439_a(coords.field_71574_a, coords.field_71572_b, coords.field_71573_c).func_149721_r() && coords.field_71572_b < 255; ++coords.field_71572_b) {
                              }
                           }
                        }

                        if (coords != null) {
                           double HOME_DIST = 6.0D;
                           double HOME_DIST_SQ = 36.0D;
                           coords = Blocks.field_150324_C.getBedSpawnPosition((IBlockAccess)world, coords.field_71574_a, coords.field_71572_b, coords.field_71573_c, (EntityPlayer)null);
                           if (coords != null) {
                              ItemGeneral var10000;
                              if (dimension == this.player.field_71093_bK && this.player.func_70092_e((double)coords.field_71574_a, this.player.field_70163_u, (double)coords.field_71573_c) <= 36.0D) {
                                 Village village = ((World)world).field_72982_D.func_75550_a(MathHelper.func_76128_c(this.player.field_70165_t), MathHelper.func_76128_c(this.player.field_70163_u), MathHelper.func_76128_c(this.player.field_70161_v), 512);
                                 if (village != null) {
                                    ChunkCoordinates townPos = village.func_75577_a();
                                    var10000 = Witchery.Items.GENERIC;
                                    if (ItemGeneral.teleportToLocationSafely(this.player.field_70170_p, (double)townPos.field_71574_a + 0.5D, (double)(townPos.field_71572_b + 1), (double)townPos.field_71573_c + 0.5D, dimension, this.player, true)) {
                                       done = true;
                                    }
                                 }
                              } else {
                                 var10000 = Witchery.Items.GENERIC;
                                 if (ItemGeneral.teleportToLocationSafely(this.player.field_70170_p, (double)coords.field_71574_a + 0.5D, (double)(coords.field_71572_b + 1), (double)coords.field_71573_c + 0.5D, dimension, this.player, true)) {
                                    done = true;
                                 }
                              }
                           }
                        }
                     }

                     if (!done) {
                        SoundEffect.NOTE_SNARE.playOnlyTo(this.player);
                     } else if (!this.player.field_71075_bZ.field_75098_d) {
                        --this.vampireUltimateCharges;
                        this.sync();
                     }
                     break;
                  default:
                     SoundEffect.NOTE_SNARE.playOnlyTo(this.player);
                  }
               } else {
                  SoundEffect.NOTE_SNARE.playOnlyTo(this.player);
               }
            }
         } else {
            SoundEffect.NOTE_SNARE.playOnlyTo(this.player);
         }
      }

   }

   public static EntityLiving spawnCreature(World world, Class<? extends EntityLiving> creatureType, double posX, double posY, double posZ, int minRange, int maxRange, ParticleEffect effect, SoundEffect effectSound) {
      if (!world.field_72995_K) {
         int x = MathHelper.func_76128_c(posX);
         int y = MathHelper.func_76128_c(posY);
         int z = MathHelper.func_76128_c(posZ);
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
               EntityLiving creature = (EntityLiving)ctor.newInstance(world);
               creature.func_70012_b(0.5D + (double)nx, 0.05D + (double)ny + 1.0D, 0.5D + (double)nz, 0.0F, 0.0F);
               world.func_72838_d(creature);
               if (effect != null) {
                  effect.send(effectSound, world, 0.5D + (double)nx, 0.05D + (double)ny + 1.0D, 0.5D + (double)nz, 1.0D, (double)creature.field_70131_O, 16);
               }

               return creature;
            } catch (NoSuchMethodException var24) {
            } catch (InvocationTargetException var25) {
            } catch (InstantiationException var26) {
            } catch (IllegalAccessException var27) {
            }
         }
      }

      return null;
   }

   public void tick() {
      if (this.vampireCooldown > 0) {
         --this.vampireCooldown;
      }

   }

   public void updateWorship() {
      if (this.cachedWorship >= 0) {
         this.player.func_70690_d(new PotionEffect(Witchery.Potions.WORSHIP.field_76415_H, TimeUtil.secsToTicks(60), this.cachedWorship, true));
         this.cachedWorship = -1;
      }

      this.processSync();
   }

   public boolean cacheIncurablePotionEffect(Collection<PotionEffect> activePotionEffects) {
      boolean cached = false;
      Iterator i$ = activePotionEffects.iterator();

      while(i$.hasNext()) {
         PotionEffect activeEffect = (PotionEffect)i$.next();
         int potionID = activeEffect.func_76456_a();
         if (potionID >= 0 && potionID < Potion.field_76425_a.length && Potion.field_76425_a[potionID] != null && Potion.field_76425_a[potionID] instanceof PotionBase && activeEffect.func_76459_b() > 5) {
            PotionBase potion = (PotionBase)Potion.field_76425_a[potionID];
            if (!potion.isCurable()) {
               this.incurablePotionEffectCache.put(activeEffect.func_76456_a(), activeEffect);
               cached = true;
            }
         }
      }

      return cached;
   }

   public void clearCachedIncurablePotionEffect(Potion potion) {
      this.incurablePotionEffectCache.remove(potion.field_76415_H);
   }

   public void restoreIncurablePotionEffects() {
      if (this.incurablePotionEffectCache.size() > 0) {
         Collection<PotionEffect> activeEffectList = this.player.func_70651_bq();
         Iterator i$ = activeEffectList.iterator();

         PotionEffect restoredEffect;
         while(i$.hasNext()) {
            restoredEffect = (PotionEffect)i$.next();
            this.incurablePotionEffectCache.remove(restoredEffect.func_76456_a());
         }

         i$ = this.incurablePotionEffectCache.values().iterator();

         while(i$.hasNext()) {
            restoredEffect = (PotionEffect)i$.next();
            this.player.func_70690_d(new PotionEffect(restoredEffect));
         }

         this.incurablePotionEffectCache.clear();
      }

   }

   public void addWorship(int level) {
      this.cachedWorship = level;
   }

   public void sync() {
      if (!this.player.field_70170_p.field_72995_K) {
         Witchery.packetPipeline.sendTo((IMessage)(new PacketExtendedPlayerSync(this)), (EntityPlayer)this.player);
      }

   }

   public static void loadProxyData(EntityPlayer player) {
      if (player != null) {
         ExtendedPlayer playerEx = get(player);
         playerEx.sync();
      }

   }

   public int getCreatureTypeOrdinal() {
      return this.creatureType;
   }

   public TransformCreature getCreatureType() {
      return TransformCreature.values()[this.creatureType];
   }

   public void setCreatureType(TransformCreature type) {
      int ordinalType = type.ordinal();
      this.setCreatureTypeOrdinal(ordinalType);
   }

   public void setCreatureTypeOrdinal(int type) {
      if (type != this.creatureType) {
         this.creatureType = type;
         if (!this.player.field_70170_p.field_72995_K) {
            Witchery.packetPipeline.sendToAll((IMessage)(new PacketPlayerStyle(this.player)));
         }
      }

   }

   public long getLastBoneFind() {
      return this.lastBoneFind;
   }

   public void setLastBoneFind(long serverTime) {
      this.lastBoneFind = serverTime;
   }

   public long getLastHowl() {
      return this.lastHowl;
   }

   public void setLastHowl(long serverTime) {
      this.lastHowl = serverTime;
   }

   public ExtendedPlayer.QuestState getWolfmanQuestState() {
      return ExtendedPlayer.QuestState.values()[this.wolfmanQuestState];
   }

   public void setWolfmanQuestState(ExtendedPlayer.QuestState state) {
      this.wolfmanQuestState = state.ordinal();
   }

   public int getWolfmanQuestCounter() {
      return this.wolfmanQuestCounter;
   }

   public void increaseWolfmanQuestCounter() {
      ++this.wolfmanQuestCounter;
      if (this.wolfmanQuestCounter > 100) {
         this.wolfmanQuestCounter = 100;
      }

   }

   public boolean storeWolfmanQuestChunk(int x, int z) {
      long location = (long)x << 32 | (long)z & 4294967295L;
      if (this.visitedChunks.contains(location)) {
         return false;
      } else {
         this.visitedChunks.add(location);
         return true;
      }
   }

   public boolean storeVampireQuestChunk(int x, int z) {
      long location = (long)x << 32 | (long)z & 4294967295L;
      if (this.visitedVampireChunks.contains(location)) {
         return false;
      } else {
         this.visitedVampireChunks.add(location);
         return true;
      }
   }

   public int getVampireQuestCounter() {
      return this.vampireQuestCounter;
   }

   public void increaseVampireQuestCounter() {
      ++this.vampireQuestCounter;
      if (this.vampireQuestCounter > 10000) {
         this.vampireQuestCounter = 10000;
      }

   }

   public void resetVampireQuestCounter() {
      this.vampireQuestCounter = 0;
   }

   public void scheduleSync() {
      this.getPlayerData = true;
   }

   public void processSync() {
      if (this.getPlayerData) {
         this.getPlayerData = false;
         Iterator i$ = this.player.field_70170_p.field_73010_i.iterator();

         while(i$.hasNext()) {
            Object obj = i$.next();
            EntityPlayer otherPlayer = (EntityPlayer)obj;
            if (otherPlayer != this.player) {
               Witchery.packetPipeline.sendTo((IMessage)(new PacketPlayerStyle(otherPlayer)), (EntityPlayer)this.player);
            }
         }
      }

   }

   public void checkSleep(boolean start) {
      if (start) {
         if (this.isVampire() && this.player.field_71083_bS && this.player.field_70170_p.func_72935_r()) {
            this.resetSleep = true;
            this.cachedSky = this.player.field_70170_p.field_73008_k;
            this.player.field_70170_p.field_73008_k = 4;
         }
      } else if (this.resetSleep) {
         this.resetSleep = false;
         this.player.field_70170_p.field_73008_k = this.cachedSky;
      }

   }

   public boolean hasVampireBook() {
      ItemStack[] arr$ = this.player.field_71071_by.field_70462_a;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ItemStack stack = arr$[i$];
         if (stack != null && stack.func_77973_b() == Witchery.Items.VAMPIRE_BOOK) {
            return stack.func_77960_j() < 9;
         }
      }

      return false;
   }

   public void setMirrorWorldEntryPoint(int x, int y, int z) {
      this.mirrorWorldEntryPoint = new Coord(x, y, z);
   }

   public Coord getMirrorWorldEntryPoint() {
      return this.mirrorWorldEntryPoint;
   }

   public boolean isMirrorWorldEntryPoint(int x, int y, int z) {
      return this.mirrorWorldEntryPoint == null || this.mirrorWorldEntryPoint.isMatch(x, y, z);
   }

   public boolean canEscapeMirrorWorld(int slot) {
      if (slot == 1) {
         return this.player.field_70170_p.func_82737_E() >= this.mirrorWorldEscapeCooldown1 + COOLDOWN_ESCAPE_1_TICKS;
      } else if (slot == 2) {
         return this.player.field_70170_p.func_82737_E() >= this.mirrorWorldEscapeCooldown2 + COOLDOWN_ESCAPE_2_TICKS;
      } else {
         return false;
      }
   }

   public void escapedMirrorWorld(int slot) {
      if (slot == 1) {
         this.mirrorWorldEscapeCooldown1 = this.player.field_70170_p.func_82737_E();
      } else if (slot == 2) {
         this.mirrorWorldEscapeCooldown2 = this.player.field_70170_p.func_82737_E();
      }

   }

   public long getCooldownSecs(int i) {
      if (i == 1) {
         return (this.mirrorWorldEscapeCooldown1 + COOLDOWN_ESCAPE_1_TICKS - this.player.field_70170_p.func_82737_E()) / 20L;
      } else {
         return i == 2 ? (this.mirrorWorldEscapeCooldown2 + COOLDOWN_ESCAPE_2_TICKS - this.player.field_70170_p.func_82737_E()) / 20L : 0L;
      }
   }

   public static enum QuestState {
      NOT_STATED,
      STARTED,
      COMPLETE;
   }

   public static enum VampirePower {
      NONE(0, 0, 0),
      DRINK(0, 0, 1),
      MESMERIZE(50, 0, 2),
      SPEED(10, 0, 4),
      BAT(50, 1, 7),
      ULTIMATE(50, 0, 10);

      public final int INITIAL_COST;
      public final int UPKEEP_COST;
      public final int LEVEL_CAP;
      private static int[] levels = new int[]{0, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5};

      private VampirePower(int initialCost, int upkeepCost, int levelCap) {
         this.INITIAL_COST = initialCost;
         this.UPKEEP_COST = upkeepCost;
         this.LEVEL_CAP = levelCap;
      }
   }

   public static enum VampireUltimate {
      NONE,
      STORM,
      SWARM,
      FARM;
   }
}
