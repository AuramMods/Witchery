package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.brewing.TileEntityCauldron;
import com.emoniph.witchery.entity.ai.EntityAIMoveToRestrictionAndSit;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.network.PacketSound;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TameableUtil;
import com.emoniph.witchery.util.TargetPointUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EntityCovenWitch extends EntityTameable implements IRangedAttackMob {
   private static final UUID field_110184_bp = UUID.fromString("DA2E2747-8768-4F9A-9135-258E93B077A4");
   private static final AttributeModifier field_110185_bq;
   private static final Item[] witchDrops;
   private int witchAttackTimer;
   private String questOfferedTo = "";
   private boolean questAccepted = false;
   private int questType = 0;
   private int questItemsNeeded = 0;
   private int timeToLive = -1;
   public static final String COVEN_KEY = "WITCCoven";
   private static final int MAX_COVEN_SIZE = 6;
   private static final EntityCovenWitch.Quest[] QUESTS;
   private static final String[] FIRST_NAMES;
   private static final String[] SURNAMES;

   public EntityCovenWitch(World world) {
      super(world);
      this.func_70105_a(0.6F, 1.95F);
      this.func_70661_as().func_75491_a(true);
      this.func_70661_as().func_75495_e(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(1, this.field_70911_d);
      this.field_70714_bg.func_75776_a(2, new EntityAIMoveToRestrictionAndSit(this, 0.6D));
      this.field_70714_bg.func_75776_a(2, new EntityAIArrowAttack(this, 1.0D, 60, 10.0F));
      this.field_70714_bg.func_75776_a(4, new EntityAIMoveIndoors(this));
      this.field_70714_bg.func_75776_a(5, new EntityAIRestrictOpenDoor(this));
      this.field_70714_bg.func_75776_a(6, new EntityAIOpenDoor(this, true));
      this.field_70714_bg.func_75776_a(7, new EntityAIMoveTowardsRestriction(this, 0.6D));
      this.field_70714_bg.func_75776_a(8, new EntityAIWander(this, 1.0D));
      this.field_70714_bg.func_75776_a(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(10, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAIHurtByTarget(this, false));
      this.field_70715_bh.func_75776_a(2, new EntityAIOwnerHurtTarget(this));
      this.field_70715_bh.func_75776_a(3, new EntityAIHurtByTarget(this, true));
      this.setTameSkin(this.field_70146_Z.nextInt(5));
      this.func_70903_f(false);
   }

   public boolean func_70601_bi() {
      boolean living = this.field_70170_p.func_72855_b(this.field_70121_D) && this.field_70170_p.func_72945_a(this, this.field_70121_D).isEmpty() && !this.field_70170_p.func_72953_d(this.field_70121_D);
      int i = MathHelper.func_76128_c(this.field_70165_t);
      int j = MathHelper.func_76128_c(this.field_70121_D.field_72338_b);
      int k = MathHelper.func_76128_c(this.field_70161_v);
      boolean creature = living && this.func_70783_a(i, j, k) >= 0.0F && (Config.instance().covenWitchSpawnWeight == 1 || this.field_70170_p.field_73012_v.nextInt(Config.instance().covenWitchSpawnWeight) == 0);
      return creature;
   }

   public void func_94058_c(String par1Str) {
   }

   private void setInnerCustomNameTag(String s) {
      this.field_70180_af.func_75692_b(10, s);
   }

   public IEntityLivingData func_110161_a(IEntityLivingData par1EntityLivingData) {
      this.setTameSkin(this.field_70170_p.field_73012_v.nextInt(5));
      return super.func_110161_a(par1EntityLivingData);
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.func_70096_w().func_75682_a(18, (byte)0);
      this.func_70096_w().func_75682_a(21, (byte)0);
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(30.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.25D);
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74768_a("SkinType", this.getTameSkin());
      nbtRoot.func_74778_a("QuestOffered", this.questOfferedTo);
      nbtRoot.func_74757_a("QuestAccepted", this.questAccepted);
      nbtRoot.func_74768_a("QuestType", this.questType);
      nbtRoot.func_74768_a("QuestItemsNeeded", this.questItemsNeeded);
      nbtRoot.func_74768_a("SuicideIn", this.timeToLive);
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      this.setTameSkin(nbtRoot.func_74762_e("SkinType"));
      this.questOfferedTo = nbtRoot.func_74779_i("QuestOffered");
      this.questAccepted = nbtRoot.func_74767_n("QuestAccepted");
      this.questType = nbtRoot.func_74762_e("QuestType");
      this.questItemsNeeded = nbtRoot.func_74762_e("QuestItemsNeeded");
      if (nbtRoot.func_74764_b("SuicideIn")) {
         this.timeToLive = nbtRoot.func_74762_e("SuicideIn");
      } else {
         this.timeToLive = -1;
      }

      if (nbtRoot.func_74764_b("CustomName") && nbtRoot.func_74779_i("CustomName").length() > 0) {
         this.setInnerCustomNameTag(nbtRoot.func_74779_i("CustomName"));
      }

   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.covenwitch.name");
   }

   public void setTimeToLive(int i) {
      this.timeToLive = i;
   }

   public void func_70629_bd() {
      super.func_70629_bd();
      if (this.field_70170_p != null && !this.field_70128_L && !this.field_70170_p.field_72995_K && this.timeToLive != -1) {
         if (this.timeToLive > 0) {
            --this.timeToLive;
         }

         if (this.func_70638_az() == null && this.timeToLive == 0) {
            ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, this, 1.0D, 1.0D, 16);
            this.func_70106_y();
         }
      }

   }

   public void func_70645_a(DamageSource damageSource) {
      if (!this.field_70170_p.field_72995_K && this.func_70909_n()) {
         Entity owner = this.func_70902_q();
         if (owner != null && owner instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)owner;
            NBTTagCompound nbtPlayer = Infusion.getNBT(player);
            if (nbtPlayer.func_74764_b("WITCCoven")) {
               NBTTagList nbtCovenList = nbtPlayer.func_150295_c("WITCCoven", 10);

               for(int i = 0; i < nbtCovenList.func_74745_c(); ++i) {
                  NBTTagCompound nbtWitch = nbtCovenList.func_150305_b(i);
                  if (nbtWitch.func_74779_i("WitchName").equalsIgnoreCase(this.func_94057_bL())) {
                     nbtCovenList.func_74744_a(i);
                     break;
                  }
               }
            }
         }
      }

      super.func_70645_a(damageSource);
   }

   public int getTameSkin() {
      return this.field_70180_af.func_75683_a(18);
   }

   public void setTameSkin(int par1) {
      this.field_70180_af.func_75692_b(18, (byte)par1);
   }

   protected String func_70639_aQ() {
      return null;
   }

   protected String func_70621_aR() {
      return "mob.witch.hurt";
   }

   protected String func_70673_aS() {
      return "mob.witch.death";
   }

   public void setAggressive(boolean aggressive) {
      byte b0 = this.field_70180_af.func_75683_a(21);
      if (aggressive) {
         b0 = (byte)(b0 | 1);
      } else {
         b0 &= -2;
      }

      this.field_70180_af.func_75692_b(21, b0);
   }

   public boolean getAggressive() {
      return (this.field_70180_af.func_75683_a(21) & 1) != 0;
   }

   public void setQuestOffered(boolean aggressive) {
      byte b0 = this.field_70180_af.func_75683_a(21);
      if (aggressive) {
         b0 = (byte)(b0 | 4);
      } else {
         b0 &= -5;
      }

      this.field_70180_af.func_75692_b(21, b0);
   }

   public boolean isQuestOffered() {
      return (this.field_70180_af.func_75683_a(21) & 2) != 0;
   }

   public boolean func_70650_aV() {
      return true;
   }

   public void func_70636_d() {
      if (!this.field_70170_p.field_72995_K) {
         if (this.getAggressive()) {
            if (this.witchAttackTimer-- <= 0) {
               this.setAggressive(false);
               ItemStack itemstack = this.func_70694_bm();
               this.func_70062_b(0, (ItemStack)null);
               if (itemstack != null && itemstack.func_77973_b() == Items.field_151068_bn) {
                  List list = Items.field_151068_bn.func_77832_l(itemstack);
                  if (list != null) {
                     Iterator iterator = list.iterator();

                     while(iterator.hasNext()) {
                        PotionEffect potioneffect = (PotionEffect)iterator.next();
                        this.func_70690_d(new PotionEffect(potioneffect));
                     }
                  }
               }

               this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111124_b(field_110185_bq);
            }
         } else {
            short short1 = -1;
            if (this.field_70146_Z.nextFloat() < 0.15F && this.func_70027_ad() && !this.func_70644_a(Potion.field_76426_n)) {
               short1 = 16307;
            } else if (this.field_70146_Z.nextFloat() < 0.05F && this.func_110143_aJ() < this.func_110138_aP()) {
               short1 = 16341;
            } else if (this.field_70146_Z.nextFloat() < 0.25F && this.func_70638_az() != null && !this.func_70644_a(Potion.field_76424_c) && this.func_70638_az().func_70068_e(this) > 121.0D) {
               short1 = 16274;
            } else if (this.field_70146_Z.nextFloat() < 0.25F && this.func_70638_az() != null && !this.func_70644_a(Potion.field_76424_c) && this.func_70638_az().func_70068_e(this) > 121.0D) {
               short1 = 16274;
            }

            if (short1 > -1) {
               this.func_70062_b(0, new ItemStack(Items.field_151068_bn, 1, short1));
               this.witchAttackTimer = this.func_70694_bm().func_77988_m();
               this.setAggressive(true);
               IAttributeInstance attributeinstance = this.func_110148_a(SharedMonsterAttributes.field_111263_d);
               attributeinstance.func_111124_b(field_110185_bq);
               attributeinstance.func_111121_a(field_110185_bq);
            }
         }

         if (this.field_70146_Z.nextFloat() < 7.5E-4F) {
            this.field_70170_p.func_72960_a(this, (byte)15);
         }
      }

      super.func_70636_d();
   }

   protected float func_70672_c(DamageSource par1DamageSource, float par2) {
      par2 = super.func_70672_c(par1DamageSource, par2);
      if (par1DamageSource.func_76346_g() == this) {
         par2 = 0.0F;
      }

      if (par1DamageSource.func_82725_o()) {
         par2 = (float)((double)par2 * 0.15D);
      }

      return par2;
   }

   @SideOnly(Side.CLIENT)
   public void func_70103_a(byte par1) {
      if (par1 == 15) {
         for(int i = 0; i < this.field_70146_Z.nextInt(35) + 10; ++i) {
            this.field_70170_p.func_72869_a("witchMagic", this.field_70165_t + this.field_70146_Z.nextGaussian() * 0.12999999523162842D, this.field_70121_D.field_72337_e + 0.5D + this.field_70146_Z.nextGaussian() * 0.12999999523162842D, this.field_70161_v + this.field_70146_Z.nextGaussian() * 0.12999999523162842D, 0.0D, 0.0D, 0.0D);
         }
      } else {
         super.func_70103_a(par1);
      }

   }

   public boolean func_70085_c(EntityPlayer player) {
      if (!this.field_70170_p.field_72995_K && player != null) {
         if (!this.func_70909_n() && !this.getAggressive() && player.field_71093_bK != Config.instance().dimensionDreamID) {
            if (this.questAccepted) {
               if (this.questOfferedTo.equalsIgnoreCase(player.func_70005_c_())) {
                  if (this.isCovenFull(player)) {
                     this.questAccepted = false;
                     this.questType = 0;
                     this.questOfferedTo = "";
                     playWitchTalk(this.field_70170_p, this, 0.4F);
                     ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, String.format("<%s> %s", this.func_94057_bL(), Witchery.resource("witchery.witch.say.covenfull")));
                  } else if (isQuestItemForEntity(player.func_70694_bm(), this)) {
                     --player.field_71071_by.field_70462_a[player.field_71071_by.field_70461_c].field_77994_a;
                     if (player.field_71071_by.field_70462_a[player.field_71071_by.field_70461_c].field_77994_a == 0) {
                        player.field_71071_by.field_70462_a[player.field_71071_by.field_70461_c] = null;
                     }

                     if (--this.questItemsNeeded == 0) {
                        if (this.addToPlayerCoven(player)) {
                           ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, String.format("<%s> %s", this.func_94057_bL(), Witchery.resource("witchery.witch.say.joinedcoven")));
                        } else {
                           ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, String.format("<%s> %s", this.func_94057_bL(), Witchery.resource("witchery.witch.say.tricked")));
                           this.func_70604_c(player);
                           this.func_70624_b(player);
                           this.func_70784_b(player);
                           this.setAggressive(true);
                           this.questAccepted = false;
                           this.questType = 0;
                           this.questOfferedTo = "";
                        }
                     } else {
                        ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, String.format("<%s> %s", this.func_94057_bL(), String.format(Witchery.resource("witchery.witch.say.questitemsremaining"), Integer.valueOf(this.questItemsNeeded).toString())));
                     }

                     playWitchTalk(this.field_70170_p, this, 0.8F);
                  } else {
                     ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, String.format("<%s> %s", this.func_94057_bL(), Witchery.resource("witchery.witch.say.questnotfinished")));
                     playWitchTalk(this.field_70170_p, this, 0.4F);
                  }
               } else {
                  ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, String.format("<%s> %s", this.func_94057_bL(), Witchery.resource("witchery.witch.say.begone")));
                  playWitchTalk(this.field_70170_p, this, 0.4F);
               }
            } else if (!this.questOfferedTo.equalsIgnoreCase(player.func_70005_c_())) {
               this.func_110163_bv();
               if (!this.func_94056_bM()) {
                  this.setInnerCustomNameTag(generateWitchName());
               }

               if (this.isCovenFull(player)) {
                  playWitchTalk(this.field_70170_p, this, 0.4F);
                  ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, String.format("<%s> %s", this.func_94057_bL(), Witchery.resource("witchery.witch.say.covenfull")));
               } else if (!Familiar.hasActiveFamiliar(player)) {
                  String s;
                  switch(this.field_70170_p.field_73012_v.nextInt(3)) {
                  case 0:
                  default:
                     s = Witchery.resource("witchery.witch.say.notinterested1");
                     break;
                  case 1:
                     s = Witchery.resource("witchery.witch.say.notinterested2");
                     break;
                  case 2:
                     s = Witchery.resource("witchery.witch.say.notinterested3");
                  }

                  playWitchTalk(this.field_70170_p, this, 0.4F);
                  ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, String.format("<%s> %s", this.func_94057_bL(), s));
               } else {
                  this.questOfferedTo = player.func_70005_c_();
                  this.questType = this.field_70170_p.field_73012_v.nextInt(QUESTS.length);
                  this.questItemsNeeded = QUESTS[this.questType].getItemsNeeded();
                  ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, String.format("<%s> %s", this.func_94057_bL(), QUESTS[this.questType].getDescriptionText()));
                  playWitchTalk(this.field_70170_p, this, 0.4F);
               }
            } else if (this.isCovenFull(player)) {
               this.questOfferedTo = "";
               playWitchTalk(this.field_70170_p, this, 0.4F);
               ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, String.format("<%s> %s", this.func_94057_bL(), Witchery.resource("witchery.witch.say.covenfull")));
            } else {
               this.questAccepted = true;
               QUESTS[this.questType].activate(this.field_70170_p, this, player);
               if (!QUESTS[this.questType].getStartText().isEmpty()) {
                  ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, String.format("<%s> %s", this.func_94057_bL(), QUESTS[this.questType].getStartText()));
               }

               playWitchTalk(this.field_70170_p, this, 1.0F);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isCovenFull(EntityPlayer player) {
      return getCovenSize(player) >= 6;
   }

   public static int getCovenSize(EntityPlayer player) {
      if (player == null) {
         return 0;
      } else {
         NBTTagCompound nbtPlayer = Infusion.getNBT(player);
         if (!nbtPlayer.func_74764_b("WITCCoven")) {
            return 0;
         } else {
            NBTTagList nbtCovenList = nbtPlayer.func_150295_c("WITCCoven", 10);
            return nbtCovenList.func_74745_c();
         }
      }
   }

   protected boolean func_70692_ba() {
      if (this.func_70909_n()) {
         Entity player = this.func_70902_q();
         if (player == null) {
            return true;
         } else {
            return player.func_70068_e(this) > 4096.0D ? true : super.func_70692_ba();
         }
      } else {
         return super.func_70692_ba();
      }
   }

   private boolean addToPlayerCoven(EntityPlayer player) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      if (!nbtPlayer.func_74764_b("WITCCoven")) {
         nbtPlayer.func_74782_a("WITCCoven", new NBTTagList());
      }

      NBTTagList nbtCovenList = nbtPlayer.func_150295_c("WITCCoven", 10);
      if (this.isWitchInList(nbtCovenList)) {
         return false;
      } else {
         this.func_70903_f(true);
         TameableUtil.setOwner(this, player);
         NBTTagCompound nbtWitch = new NBTTagCompound();
         this.writeToPlayerNBT(nbtWitch);
         nbtCovenList.func_74742_a(nbtWitch);
         ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, this, 1.0D, 2.0D, 16);
         this.func_70106_y();
         return true;
      }
   }

   private boolean isWitchInList(NBTTagList nbtCovenList) {
      for(int i = 0; i < nbtCovenList.func_74745_c(); ++i) {
         NBTTagCompound nbtWitch = nbtCovenList.func_150305_b(i);
         if (nbtWitch != null) {
            String name = nbtWitch.func_74779_i("WitchName");
            if (name != null && name.equalsIgnoreCase(this.func_94057_bL())) {
               return true;
            }
         }
      }

      return false;
   }

   private void writeToPlayerNBT(NBTTagCompound nbtWitch) {
      nbtWitch.func_74778_a("WitchName", this.func_94057_bL());
      nbtWitch.func_74768_a("Skin", this.getTameSkin());
      nbtWitch.func_74768_a("Quest", this.questType);
   }

   private void readFromPlayerNBT(NBTTagCompound nbtWitch) {
      this.setTameSkin(nbtWitch.func_74762_e("Skin"));
      this.questType = nbtWitch.func_74762_e("Quest");
      this.setInnerCustomNameTag(nbtWitch.func_74779_i("WitchName"));
   }

   public static void summonCoven(ArrayList<RitualStep> ritualSteps, World world, EntityPlayer player, int[][] pos) {
      double RADIUS_XZ = 64.0D;
      double RADIUS_Y = 16.0D;
      AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(player.field_70165_t - 64.0D, player.field_70163_u - 16.0D, player.field_70161_v - 64.0D, player.field_70165_t + 64.0D, player.field_70163_u + 16.0D, player.field_70161_v + 64.0D);
      List entities = world.func_72872_a(EntityCovenWitch.class, bounds);

      for(int index = 0; index < entities.size(); ++index) {
         EntityCovenWitch witch = (EntityCovenWitch)entities.get(index);
         if (witch.func_70909_n() && witch.func_70902_q() == player) {
            ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, witch, 1.0D, 2.0D, 16);
            witch.func_70106_y();
         }
      }

      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      if (nbtPlayer.func_74764_b("WITCCoven")) {
         NBTTagList nbtCovenList = nbtPlayer.func_150295_c("WITCCoven", 10);

         for(int index = 0; index < nbtCovenList.func_74745_c() && index < pos.length; ++index) {
            ritualSteps.add(new EntityCovenWitch.StepSummonCovenMemeber(player, index, pos[index]));
         }
      }

   }

   public static void summonCoven(World world, EntityPlayer player, Coord target, int ticks) {
      if (ticks % 20 == 0 && ticks / 20 > 0) {
         NBTTagCompound nbtPlayer = Infusion.getNBT(player);
         if (nbtPlayer.func_74764_b("WITCCoven")) {
            double RADIUS_XZ = 64.0D;
            double RADIUS_Y = 16.0D;
            AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(player.field_70165_t - 64.0D, player.field_70163_u - 16.0D, player.field_70161_v - 64.0D, player.field_70165_t + 64.0D, player.field_70163_u + 16.0D, player.field_70161_v + 64.0D);
            List entities = world.func_72872_a(EntityCovenWitch.class, bounds);
            NBTTagList nbtCovenList = nbtPlayer.func_150295_c("WITCCoven", 10);
            int witchIndex = ticks / 20 - 1;
            if (witchIndex > nbtCovenList.func_74745_c()) {
               return;
            }

            NBTTagCompound nbtWitch = nbtCovenList.func_150305_b(witchIndex);
            EntityCovenWitch witch = null;

            for(int index = 0; index < entities.size(); ++index) {
               EntityCovenWitch witchAround = (EntityCovenWitch)entities.get(index);
               if (witchAround.func_70909_n() && witchAround.func_70902_q() == player && witchAround.func_94057_bL().equalsIgnoreCase(nbtWitch.func_74779_i("WitchName"))) {
                  witch = witchAround;
                  break;
               }
            }

            boolean spawn = false;
            if (witch == null) {
               witch = new EntityCovenWitch(world);
               witch.readFromPlayerNBT(nbtWitch);
               witch.func_70903_f(true);
               TameableUtil.setOwner(witch, player);
               spawn = true;
            }

            TileEntity closest = null;
            double bestDistSq = 0.0D;
            Iterator i$ = world.field_147482_g.iterator();

            while(true) {
               TileEntity tile;
               double dist;
               do {
                  do {
                     if (!i$.hasNext()) {
                        double cauldronRange = 9.0D;
                        double cauldronRangeSq = 81.0D;
                        if (closest != null && bestDistSq <= 81.0D) {
                           witch.func_110171_b(closest.field_145851_c, closest.field_145848_d, closest.field_145849_e, 3);
                           int maxRange = 3;
                           int minRange = 1;
                           int activeRadius = maxRange - minRange;
                           int ax = world.field_73012_v.nextInt(activeRadius * 2 + 1);
                           if (ax > activeRadius) {
                              ax += minRange * 2;
                           }

                           int nx = closest.field_145851_c - maxRange + ax;
                           int az = world.field_73012_v.nextInt(activeRadius * 2 + 1);
                           if (az > activeRadius) {
                              az += minRange * 2;
                           }

                           int nz = closest.field_145849_e - maxRange + az;
                           witch.func_70080_a((double)nx, 0.01D + (double)closest.field_145848_d, (double)nz, 0.0F, 0.0F);
                        } else {
                           witch.func_70080_a(0.5D + (double)target.x + (double)world.field_73012_v.nextInt(3) - 1.5D, 0.01D + (double)target.y, 0.5D + (double)target.z + (double)world.field_73012_v.nextInt(3) - 1.5D, 0.0F, 0.0F);
                        }

                        witch.field_70911_d.func_75251_c();
                        witch.setTimeToLive(1200);
                        if (spawn) {
                           world.func_72838_d(witch);
                        }

                        ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, witch, 1.0D, 2.0D, 16);
                        return;
                     }

                     Object obj = i$.next();
                     tile = (TileEntity)obj;
                  } while(!(tile instanceof TileEntityCauldron));

                  dist = player.func_70092_e((double)tile.field_145851_c, (double)tile.field_145848_d, (double)tile.field_145849_e);
               } while(closest != null && !(dist < bestDistSq));

               closest = tile;
               bestDistSq = dist;
            }
         }
      }

   }

   public static void summonCovenMember(World world, EntityPlayer player, int ttlSecs) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      if (nbtPlayer.func_74764_b("WITCCoven")) {
         NBTTagList nbtCovenList = nbtPlayer.func_150295_c("WITCCoven", 10);
         if (nbtCovenList.func_74745_c() > 0) {
            double RADIUS_XZ = 64.0D;
            double RADIUS_Y = 16.0D;
            AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(player.field_70165_t - 64.0D, player.field_70163_u - 16.0D, player.field_70161_v - 64.0D, player.field_70165_t + 64.0D, player.field_70163_u + 16.0D, player.field_70161_v + 64.0D);
            List entities = world.func_72872_a(EntityCovenWitch.class, bounds);
            Collections.shuffle(entities);

            EntityCovenWitch witch;
            int i;
            int j;
            int k;
            int l;
            int i1;
            for(int index = 0; index < entities.size(); ++index) {
               witch = (EntityCovenWitch)entities.get(index);
               if (witch.func_70909_n() && witch.func_70902_q() == player) {
                  i = MathHelper.func_76128_c(player.field_70165_t) - 2;
                  j = MathHelper.func_76128_c(player.field_70161_v) - 2;
                  k = MathHelper.func_76128_c(player.field_70121_D.field_72338_b);

                  for(l = 0; l <= 4; ++l) {
                     for(i1 = 0; i1 <= 4; ++i1) {
                        if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && world.func_147439_a(i + l, k - 1, j + i1).isSideSolid(world, i + l, k - 1, j + i1, ForgeDirection.UP) && !world.func_147439_a(i + l, k, j + i1).func_149721_r() && !world.func_147439_a(i + l, k + 1, j + i1).func_149721_r()) {
                           ItemGeneral var10000 = Witchery.Items.GENERIC;
                           ItemGeneral.teleportToLocation(world, (double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), player.field_71093_bK, witch, true);
                           witch.func_70661_as().func_75499_g();
                           return;
                        }
                     }
                  }
               }
            }

            NBTTagCompound nbtWitch = nbtCovenList.func_150305_b(world.field_73012_v.nextInt(nbtCovenList.func_74745_c()));
            witch = new EntityCovenWitch(world);
            i = MathHelper.func_76128_c(player.field_70165_t) - 2;
            j = MathHelper.func_76128_c(player.field_70161_v) - 2;
            k = MathHelper.func_76128_c(player.field_70121_D.field_72338_b);

            for(l = 0; l <= 4; ++l) {
               for(i1 = 0; i1 <= 4; ++i1) {
                  if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && world.func_147439_a(i + l, k - 1, j + i1).isSideSolid(world, i + l, k - 1, j + i1, ForgeDirection.UP) && !world.func_147439_a(i + l, k, j + i1).func_149721_r() && !world.func_147439_a(i + l, k + 1, j + i1).func_149721_r()) {
                     witch.func_70012_b((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), 0.0F, 0.0F);
                     break;
                  }
               }
            }

            witch.func_70903_f(true);
            TameableUtil.setOwner(witch, player);
            witch.readFromPlayerNBT(nbtWitch);
            witch.setTimeToLive(ttlSecs * 20);
            world.func_72838_d(witch);
            ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, witch, 1.0D, 2.0D, 16);
         } else {
            SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
         }
      } else {
         SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
      }

   }

   private static void playWitchTalk(World world, Entity where, float volume) {
      Witchery.packetPipeline.sendToAllAround(new PacketSound(world.field_73012_v.nextBoolean() ? SoundEffect.WITCHERY_MOB_BABA_DEATH : SoundEffect.WITCHERY_MOB_BABA_LIVING, where, 1.0F, 1.0F), TargetPointUtil.from(where, 8.0D));
   }

   private static boolean isQuestItemForEntity(ItemStack stack, EntityCovenWitch questGiver) {
      if (questGiver != null && stack != null) {
         if (QUESTS[questGiver.questType].isQuestItem(stack)) {
            return true;
         }

         if (stack.func_77942_o()) {
            NBTTagCompound nbtRoot = stack.func_77978_p();
            if (nbtRoot.func_74764_b("WITCQuestOwnerIDLeast") && nbtRoot.func_74764_b("WITCQuestOwnerIDMost")) {
               UUID questGiverID = new UUID(nbtRoot.func_74763_f("WITCQuestOwnerIDMost"), nbtRoot.func_74763_f("WITCQuestOwnerIDLeast"));
               return questGiverID.equals(questGiver.getPersistentID());
            }
         }
      }

      return false;
   }

   protected void func_70628_a(boolean par1, int par2) {
      int j = this.field_70146_Z.nextInt(3) + 1;

      for(int k = 0; k < j; ++k) {
         int l = this.field_70146_Z.nextInt(3);
         Item i1 = witchDrops[this.field_70146_Z.nextInt(witchDrops.length)];
         if (par2 > 0) {
            l += this.field_70146_Z.nextInt(par2 + 1);
         }

         for(int j1 = 0; j1 < l; ++j1) {
            this.func_145779_a(i1, 1);
         }
      }

   }

   public void func_82196_d(EntityLivingBase par1EntityLivingBase, float par2) {
      if (!this.getAggressive()) {
         EntityPotion entitypotion = new EntityPotion(this.field_70170_p, this, 32732);
         entitypotion.field_70125_A -= -20.0F;
         double d0 = par1EntityLivingBase.field_70165_t + par1EntityLivingBase.field_70159_w - this.field_70165_t;
         double d1 = par1EntityLivingBase.field_70163_u + (double)par1EntityLivingBase.func_70047_e() - 1.100000023841858D - this.field_70163_u;
         double d2 = par1EntityLivingBase.field_70161_v + par1EntityLivingBase.field_70179_y - this.field_70161_v;
         float f1 = MathHelper.func_76133_a(d0 * d0 + d2 * d2);
         if (f1 >= 8.0F && !par1EntityLivingBase.func_70644_a(Potion.field_76421_d)) {
            entitypotion.func_82340_a(32698);
         } else if (par1EntityLivingBase.func_110143_aJ() >= 8.0F && !par1EntityLivingBase.func_70644_a(Potion.field_76436_u)) {
            entitypotion.func_82340_a(32660);
         } else if (f1 <= 3.0F && !par1EntityLivingBase.func_70644_a(Potion.field_76437_t) && this.field_70146_Z.nextFloat() < 0.25F) {
            entitypotion.func_82340_a(32696);
         }

         entitypotion.func_70186_c(d0, d1 + (double)(f1 * 0.2F), d2, 0.75F, 8.0F);
         this.field_70170_p.func_72838_d(entitypotion);
      }

   }

   public EntityAgeable func_90011_a(EntityAgeable entityageable) {
      return null;
   }

   public static String generateWitchName() {
      Random ra = new Random();
      return String.format("%s %s", FIRST_NAMES[ra.nextInt(FIRST_NAMES.length)], SURNAMES[ra.nextInt(SURNAMES.length)]);
   }

   public void standStill() {
      this.field_70911_d.func_75270_a(true);
   }

   static {
      field_110185_bq = (new AttributeModifier(field_110184_bp, "Drinking speed penalty", -0.25D, 0)).func_111168_a(false);
      witchDrops = new Item[]{Items.field_151114_aO, Items.field_151102_aT, Items.field_151137_ax, Items.field_151070_bp, Items.field_151069_bo, Items.field_151016_H, Items.field_151055_y, Items.field_151055_y};
      QUESTS = new EntityCovenWitch.Quest[]{new EntityCovenWitch.FightPetQuest(Witchery.resource("witchery.witch.quest.fightspider"), ""), new EntityCovenWitch.FightZombiePetQuest(Witchery.resource("witchery.witch.quest.fightzombie"), ""), new EntityCovenWitch.FetchQuest(Witchery.resource("witchery.witch.quest.getdemonheart"), Witchery.resource("witchery.witch.quest.go"), Witchery.Items.GENERIC.itemDemonHeart.createStack()), new EntityCovenWitch.FetchQuest(Witchery.resource("witchery.witch.quest.makecrystalball"), Witchery.resource("witchery.witch.quest.go"), new ItemStack(Witchery.Blocks.CRYSTAL_BALL)), new EntityCovenWitch.FetchQuest(Witchery.resource("witchery.witch.quest.getbones"), Witchery.resource("witchery.witch.quest.go"), new ItemStack(Items.field_151103_aS, 30)), new EntityCovenWitch.FetchQuest(Witchery.resource("witchery.witch.quest.makegrotesquebrew"), Witchery.resource("witchery.witch.quest.go"), Witchery.Items.GENERIC.itemBrewGrotesque.createStack(5)), new EntityCovenWitch.FetchQuest(Witchery.resource("witchery.witch.quest.makenecrostone"), Witchery.resource("witchery.witch.quest.go"), Witchery.Items.GENERIC.itemNecroStone.createStack())};
      FIRST_NAMES = new String[]{"Abigail", "Agatha", "Agony", "Alcina", "Alcyone", "Alexandra", "Alexandria", "Alvira", "Amanita", "Amaranth", "Amarantha", "Ambrosia", "Amelia", "Amethyst", "Anastasia", "Andromeda", "Angel", "Angela", "Angelica", "Angelique", "Anna", "Arachne", "Aradia", "Aria", "Arianna", "Ariadne", "Ariel", "Artemis", "Artemisia", "Astrea", "Astrid", "Astoria", "Autumn", "Aurora", "Beatrix", "Bella", "Belladonna", "Belle", "Bernadette", "Beryl", "Bianca", "Blanche", "Bliss", "Calliope", "Callypso", "Calpurnia", "Camilla", "Carlotta", "Carmilla", "Caroline", "Carrie", "Cassandra", "Cassiopeia", "Catherine", "Cathy", "Cecelia", "Celeste", "Celia", "Charlotte", "Christine", "Circe", "Clara", "Claudia", "Cleopatra", "Columbia", "Coraline", "Cordelia", "Cornelia", "Crystal", "Daphne", "Daria", "Darla", "Delia", "Delilah", "Della", "Demetria", "Demonica", "Desdemona", "Desire", "Dolores", "Dora", "Dove", "Drusilla", "Dusk", "Ebony", "Echo", "Eden", "Elanore", "Electra", "Eldora", "Elena", "Eliza", "Eloise", "Elphaba", "Elspeth", "Elsinore", "Elvira", "Ember", "Emilie", "Ephemera", "Eranthe", "Eris", "Esmerelda", "Estrella", "Esther", "Eterna", "Eternity", "Eudora", "Euphemia", "Eva", "Evalina", "Evangeline", "Eve", "Granny", "Gabriella", "Gabrielle", "Garnet", "Genevieve", "Godiva", "Hathor", "Hecate", "Hecuba", "Helena", "Hepzibah", "Hesperia", "Hippolita", "Ianthe", "Icie", "Icy", "Inez", "Infinity", "Ione", "Iris", "Isabeau", "Isabella", "Isabelle", "Isadora", "Isis", "Isolde", "Istar", "Ivy", "Izora", "Jane", "Jeanette", "Jinx", "Jocasta", "Juliet", "Katrina", "Lavinia", "Layla", "Leona", "Lenora", "Lenore", "Leona", "Libitina", "Ligeia", "Lilah", "Lilith", "Lillian", "Lily", "Lolita", "Lorraine", "Lucinda", "Lucretia", "Luna", "Lydia", "Lyra", "Madeline", "Magdalena", "Magenta", "Mara", "Marcella", "Margaret", "Marguerita", "Maria", "Marie", "Marissa", "Martha", "Matilda", "Medea", "Medusa", "Melanie", "Melantha", "Melanthe", "Melinda", "Mercedes", "Merula", "Mildred", "Mina", "Minerva", "Miranda", "Miriam", "Moira", "Mordea", "Morgan", "Morgana", "Morticia", "Nadia", "Nadine", "Nerezza", "Nora", "Nyx", "Obsidia", "Octavia", "Odessa", "Olivia", "Opal", "Ophelia", "Pandora", "Patience", "Pearl", "Penelope", "Perenelle", "Permelia", "Persephone", "Pixie", "Phoenix", "Poppy", "Priscilla", "Prudence", "Rachel", "Rain", "Raven", "Regina", "Rita", "Rosa", "Rose", "Rosemary", "Rowena", "Ruby", "Sabrina", "Salem", "Samantha", "Sangria", "Scarlet", "Selena", "Selene", "Sephora", "Seraphina", "Serena", "Serenity", "Shannon", "Silver", "Simone", "Sophia", "Sybella", "Sybil", "Sylvia", "Tabitha", "Tempest", "Theda", "Theresa", "Thora", "Threnody", "Trinity", "Twilight", "Umbra", "Vaitiare", "Valerie", "Vanessa", "Verna", "Verona", "Veronica", "Vesta", "Victoria", "Violet", "Whisper", "Willow", "Winter", "Xenobia", "Zillah", "Zinnia"};
      SURNAMES = new String[]{"Adams", "Addams", "Argent", "Ashwood", "Balfour", "Barker", "Batby", "Bathory", "Batsford", "Batson", "Batstone", "Batt", "Baudelaire", "Black", "Blackbird", "Blackburn", "Blackcat", "Blacklock", "Blackmoore", "Blackstone", "Blackthorn", "Blackwell", "Blackwing", "Blackwolf", "Blackwood", "Blair", "Blood", "Bloodgood", "Bloodhart", "Bloodmoore", "Bloodsaw", "Bloodstone", "Bloodsworth", "Bloodwine", "Bloodworth", "Boggart", "Boggarty", "Bonebrake", "Bonehart", "Bonehill", "Bonella", "Boneman", "Bones", "Bonesmith", "Bonewits", "Borden", "Broom", "Broomwood", "Burton", "Byron", "Cackler", "Cain", "Calamity", "Castle", "Castleton", "Cemetary", "Chill", "Chillingwood", "Cobweb", "Coffin", "Coffinberry", "Coffins", "Cold", "Coldbridge", "Coldeman", "Coldstone", "Coldwell", "Cole", "Collins", "Constantine", "Corbett", "Corbin", "Corpse", "Corpseley", "Creak", "Creakey", "Creep", "Creeper", "Creeps", "Crepsley", "Crimson", "Cross", "Crossway", "Crosswicks", "Crow", "Crowden", "Crowe", "Crowley", "Darcy", "Dark", "Darke", "Darken", "Darkenwald", "Darkes", "Darkmoore", "Darkwell", "Darkwood", "Deadman", "Deadmond", "Deadmore", "Deadrick", "Deadwood", "DeAngelus", "Dearborn", "Death", "Deathridge", "Deathrow", "December", "Delambre", "DeLioncourt", "Demond", "Demonde", "Demonte", "DeMort", "DeRavin", "Devall", "Devane", "DeVille", "DeWinter", "Dracul", "Drago", "Drake", "Dread", "Drear", "Dreary", "Drelincourt", "DuLac", "Dumaine", "Dunsany", "Eldritch", "Fang", "Fanger", "Fate", "Faust", "February", "Fear", "Fearfield", "Fears", "Frankenstein", "Frost", "Fury", "Gautier", "Ghoul", "Ghoulson", "Ghost", "Ghosten", "Ghostley", "Giger", "Goblin", "Goth", "Gotham", "Gothard", "Gothberg", "Gravedigger", "Gravemaker", "Graves", "Gravesen", "Gravesgard", "Grey", "Greyson", "Greystone", "Grimmauld", "Grimm", "Grimmer", "Grimmes", "Grimmins", "Grimsbro", "Grimsby", "Grimsman", "Grimwood", "Harker", "Hart", "Haunt", "Haunter", "Haunton", "Haunty", "Hawk", "Hawke", "Havelock", "Heart", "Heartstrom", "Hemlock", "Hex", "Hexem", "Hexter", "Hexwood", "Hollow", "Holmes", "Holmwood", "Hugo", "Hunter", "Hyde", "January", "Jekyll", "Kenrick", "Kilgore", "Killar", "Killewich", "Killings", "LaCroix", "Lapidus", "LaRue", "LeFay", "LeStrange", "LeStrange", "Locke", "London", "Loveless", "Lovelock", "Lovett", "Lycan", "MacBeth", "Mandrake", "Marrow", "Masters", "Mist", "Misteri", "Moan", "Moon", "Moones", "Moonie", "Moonly", "Monet", "Monster", "Monstery", "Montague", "Montresor", "Morgan", "Morgue", "Moriarty", "Murdoc", "Murray", "Morrow", "Mort", "Mortella", "Munster", "Mysterios", "Night", "Nightchase", "Nightengale", "Nightingdale", "Nightman", "Nightwalker", "Nightwine", "Nocton", "Nox", "October", "Odd", "Odder", "Oddman", "Oddson", "Owl", "Owley", "Owlford", "Owlsey", "Pale", "Pale", "Paine", "Pains", "Payne", "Plague", "Poe", "Poe", "Poe", "Pyre", "Pyre", "Pyre", "Radcliffe", "Rain", "Raven", "Ravencraft", "Ravendale", "Ravenhorst", "Ravensloft", "Ravenway", "Rayne", "Reaper", "Redbone", "Redcross", "Redd", "Redfern", "Redgrave", "Redmond", "Redwine", "Redwolf", "Renfield", "Riven", "Rookwood", "Roth", "Ripley", "Ripper", "Salvatore", "Scar", "Scare", "Scarebrook", "Scares", "Scarey", "Scarlati", "Setzer", "Seward", "Shade", "Shademoore", "Shadow", "Shadows", "Shadowton", "Shelley", "Skeleton", "Skelinen", "Skellington", "Skelton", "Skull", "Skullman", "Specter", "Spectre", "Spellman", "Spider", "Spinner", "Spirite", "Spook", "Spook", "Spook", "Song", "Snow", "St. Claire", "St. Germaine", "Steele", "Sterling", "Stoker", "Storm", "Storme", "Stormfelt", "Stormwind", "Stormyr", "Stone", "Stonewall", "Strange", "Strangeman", "Strangeway", "Striker", "Swan", "Swann", "Teeth", "Tombs", "Tombstone", "Towers", "Trick", "Valancourt", "Valdemar", "Valentine", "Valentino", "Vamper", "Vamplers", "Vampouille", "Vamprine", "Vampyr", "Van Allen", "Van Gogh", "Van Halen", "Van Helgen", "Van Helsing", "Voorhees", "Webb", "Weird", "Weird", "West", "Westenra", "White", "Whitebone", "Whitemoon", "Whitewing", "Widdowes", "Wild", "Wildblood", "Wilde", "Winchester", "Windgate", "Windholm", "Windward", "Wing", "Wingblade", "Wingfield", "Winter", "Winterford", "Winterrose", "Winterwood", "Winters", "Witche", "Witcher", "Witchery", "Witchey", "Witchman", "Wither", "Wolf", "Wolfen", "Wolfmann", "Wolfram", "Wolfstone", "Wolftooth"};
   }

   private static class FightZombiePetQuest extends EntityCovenWitch.Quest {
      public FightZombiePetQuest(String descriptionText, String startText) {
         super(descriptionText, startText, 1);
      }

      public void activate(World world, EntityCovenWitch witch, EntityPlayer player) {
         EntityZombie spider = new EntityZombie(world);
         spider.func_70012_b(witch.field_70165_t, witch.field_70163_u, witch.field_70161_v, witch.field_70125_A, witch.field_70177_z);
         world.func_72838_d(spider);
         spider.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(100.0D);
         spider.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(5.0D);
         spider.func_70062_b(4, new ItemStack(Items.field_151144_bL));
         spider.func_70606_j((float)spider.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111126_e());
         spider.func_70624_b(player);
         spider.func_70784_b(player);
         spider.func_70604_c(player);
         spider.func_94058_c(String.format(Witchery.resource("witchery.witch.pet"), witch.func_94057_bL()));
         ItemStack stack = new ItemStack(Items.field_151078_bh);
         stack.func_151001_c(String.format(Witchery.resource("witchery.witch.petflesh"), witch.func_94057_bL()));
         if (!stack.func_77942_o()) {
            stack.func_77982_d(new NBTTagCompound());
         }

         NBTTagCompound nbtRoot = stack.func_77978_p();
         nbtRoot.func_74772_a("WITCQuestOwnerIDLeast", witch.func_110124_au().getLeastSignificantBits());
         nbtRoot.func_74772_a("WITCQuestOwnerIDMost", witch.func_110124_au().getMostSignificantBits());
         NBTTagCompound nbtExtraDrop = new NBTTagCompound();
         stack.func_77955_b(nbtExtraDrop);
         NBTTagCompound nbtSpider = spider.getEntityData();
         if (!nbtSpider.func_74764_b("WITCExtraDrops")) {
            nbtSpider.func_74782_a("WITCExtraDrops", new NBTTagList());
         }

         NBTTagList nbtExtraDrops = nbtSpider.func_150295_c("WITCExtraDrops", 10);
         nbtExtraDrops.func_74742_a(nbtExtraDrop);
         ParticleEffect.MOB_SPELL.send(SoundEffect.NONE, spider, 2.0D, 2.0D, 16);
      }
   }

   private static class FightPetQuest extends EntityCovenWitch.Quest {
      public FightPetQuest(String descriptionText, String startText) {
         super(descriptionText, startText, 1);
      }

      public void activate(World world, EntityCovenWitch witch, EntityPlayer player) {
         EntitySpider spider = new EntitySpider(world);
         spider.func_70012_b(witch.field_70165_t, witch.field_70163_u, witch.field_70161_v, witch.field_70125_A, witch.field_70177_z);
         world.func_72838_d(spider);
         spider.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(100.0D);
         spider.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(5.0D);
         spider.func_70606_j((float)spider.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111126_e());
         spider.func_70624_b(player);
         spider.func_70784_b(player);
         spider.func_70604_c(player);
         spider.func_94058_c(String.format(Witchery.resource("witchery.witch.pet"), witch.func_94057_bL()));
         ItemStack stack = new ItemStack(Items.field_151070_bp);
         stack.func_151001_c(String.format(Witchery.resource("witchery.witch.peteye"), witch.func_94057_bL()));
         if (!stack.func_77942_o()) {
            stack.func_77982_d(new NBTTagCompound());
         }

         NBTTagCompound nbtRoot = stack.func_77978_p();
         nbtRoot.func_74772_a("WITCQuestOwnerIDLeast", witch.func_110124_au().getLeastSignificantBits());
         nbtRoot.func_74772_a("WITCQuestOwnerIDMost", witch.func_110124_au().getMostSignificantBits());
         NBTTagCompound nbtExtraDrop = new NBTTagCompound();
         stack.func_77955_b(nbtExtraDrop);
         NBTTagCompound nbtSpider = spider.getEntityData();
         if (!nbtSpider.func_74764_b("WITCExtraDrops")) {
            nbtSpider.func_74782_a("WITCExtraDrops", new NBTTagList());
         }

         NBTTagList nbtExtraDrops = nbtSpider.func_150295_c("WITCExtraDrops", 10);
         nbtExtraDrops.func_74742_a(nbtExtraDrop);
         ParticleEffect.MOB_SPELL.send(SoundEffect.NONE, spider, 2.0D, 2.0D, 16);
      }
   }

   private static class FetchQuest extends EntityCovenWitch.Quest {
      final ItemStack stack;

      public FetchQuest(String descriptionText, String startText, ItemStack stack) {
         super(descriptionText, startText, stack.field_77994_a);
         this.stack = stack;
      }

      public void activate(World world, EntityCovenWitch witch, EntityPlayer player) {
      }

      public boolean isQuestItem(ItemStack stack) {
         return this.stack.func_77969_a(stack);
      }
   }

   private abstract static class Quest {
      private final String questDescriptionText;
      private final String questStartText;
      private final int itemsNeeded;

      public Quest(String descriptionText, String startText, int itemsNeeded) {
         this.questStartText = startText;
         this.questDescriptionText = descriptionText;
         this.itemsNeeded = itemsNeeded;
      }

      public int getItemsNeeded() {
         return this.itemsNeeded;
      }

      public String getDescriptionText() {
         return this.questDescriptionText;
      }

      public String getStartText() {
         return this.questStartText;
      }

      public abstract void activate(World var1, EntityCovenWitch var2, EntityPlayer var3);

      public boolean isQuestItem(ItemStack stack) {
         return false;
      }
   }

   private static class StepSummonCovenMemeber extends RitualStep {
      private final int index;
      private final int[] position;

      public StepSummonCovenMemeber(EntityPlayer player, int index, int[] position) {
         super(false);
         this.index = index;
         this.position = position;
      }

      public RitualStep.Result process(World world, int xCoord, int yCoord, int zCoord, long ticks, BlockCircle.TileEntityCircle.ActivatedRitual ritual) {
         if (ticks % 20L != 0L) {
            return RitualStep.Result.STARTING;
         } else {
            EntityPlayer player = ritual.getInitiatingPlayer(world);
            if (player != null) {
               NBTTagCompound nbtPlayer = Infusion.getNBT(player);
               if (nbtPlayer.func_74764_b("WITCCoven")) {
                  double RADIUS_XZ = 64.0D;
                  double RADIUS_Y = 16.0D;
                  AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(player.field_70165_t - 64.0D, player.field_70163_u - 16.0D, player.field_70161_v - 64.0D, player.field_70165_t + 64.0D, player.field_70163_u + 16.0D, player.field_70161_v + 64.0D);
                  List entities = world.func_72872_a(EntityCovenWitch.class, bounds);
                  NBTTagList nbtCovenList = nbtPlayer.func_150295_c("WITCCoven", 10);
                  NBTTagCompound nbtWitch = nbtCovenList.func_150305_b(this.index);
                  EntityCovenWitch witch = null;

                  for(int index = 0; index < entities.size(); ++index) {
                     EntityCovenWitch witchAround = (EntityCovenWitch)entities.get(index);
                     if (witchAround.func_70909_n() && witchAround.func_70902_q() == player && witchAround.func_94057_bL().equalsIgnoreCase(nbtWitch.func_74779_i("WitchName"))) {
                        witch = witchAround;
                        break;
                     }
                  }

                  boolean spawn = false;
                  if (witch == null) {
                     witch = new EntityCovenWitch(world);
                     witch.readFromPlayerNBT(nbtWitch);
                     witch.func_70903_f(true);
                     TameableUtil.setOwner(witch, player);
                     spawn = true;
                  }

                  float[] FACING = new float[]{-45.0F, 45.0F, -135.0F, 135.0F, 180.0F, 0.0F};
                  witch.func_70080_a(0.5D + (double)this.position[0], 0.01D + (double)this.position[1], 0.5D + (double)this.position[2], FACING[this.index], 0.0F);
                  witch.field_70759_as = witch.field_70177_z;
                  witch.field_70761_aq = witch.field_70177_z;
                  witch.field_70126_B = witch.field_70177_z;
                  witch.field_70911_d.func_75270_a(true);
                  witch.setTimeToLive(300);
                  if (spawn) {
                     world.func_72838_d(witch);
                  }

                  ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, witch, 1.0D, 2.0D, 16);
               }
            }

            return RitualStep.Result.COMPLETED;
         }
      }
   }
}
