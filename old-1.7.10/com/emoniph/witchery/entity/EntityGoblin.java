package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.ai.EntityAIAvoidEntityConditionally;
import com.emoniph.witchery.entity.ai.EntityAIDigBlocks;
import com.emoniph.witchery.entity.ai.EntityAIDropOffBlocks;
import com.emoniph.witchery.entity.ai.EntityAIGoblinMate;
import com.emoniph.witchery.entity.ai.EntityAILookAtTradePlayerGeneric;
import com.emoniph.witchery.entity.ai.EntityAIMoveIndoorsLeashAware;
import com.emoniph.witchery.entity.ai.EntityAIPickUpBlocks;
import com.emoniph.witchery.entity.ai.EntityAITradePlayerGeneric;
import com.emoniph.witchery.entity.ai.EntityAIWorship;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.command.IEntitySelector;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Tuple;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class EntityGoblin extends EntityAgeable implements IMerchant, INpc, IEntitySelector, EntityAIAvoidEntityConditionally.IAvoidEntities {
   private int randomTickDivider;
   private boolean isMating;
   Village villageObj;
   private EntityPlayer buyingPlayer;
   private MerchantRecipeList buyingList;
   private int timeUntilReset;
   private boolean needsInitilization;
   private int wealth;
   private String lastBuyingPlayer;
   private boolean isLookingForHome;
   private float field_82191_bN;
   public static final Map villagersSellingList = new HashMap();
   public static final Map blacksmithSellingList = new HashMap();
   private EntityAIWorship aiWorship;
   private boolean preventDespawn;
   private static final double KOBOLDITE_HARVEST_CHANCE = 0.02D;
   private boolean testingLeashRange;

   public EntityGoblin(World par1World) {
      this(par1World, 0);
   }

   public EntityGoblin(World par1World, int par2) {
      super(par1World);
      this.setProfession(this.field_70146_Z.nextInt(4));
      this.func_70105_a(0.6F, 0.95F);
      this.func_70661_as().func_75498_b(true);
      this.func_70661_as().func_75491_a(true);
      this.field_70714_bg.func_75776_a(0, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(1, this.aiWorship = new EntityAIWorship(this, (double)(TimeUtil.secsToTicks(30) + this.field_70146_Z.nextInt(10))));
      this.field_70714_bg.func_75776_a(2, new EntityAIPickUpBlocks(this, 24.0D));
      this.field_70714_bg.func_75776_a(2, new EntityAIDropOffBlocks(this, 24.0D));
      this.field_70714_bg.func_75776_a(2, new EntityAIDigBlocks(this, 16.0D, 0.02D));
      this.field_70714_bg.func_75776_a(3, new EntityAIAvoidEntity(this, EntityPlayer.class, 8.0F, 0.6D, 0.6D));
      this.field_70714_bg.func_75776_a(3, new EntityAIAvoidEntityConditionally(this, EntityVillageGuard.class, 12.0F, 0.8D, 0.8D, this));
      this.field_70714_bg.func_75776_a(4, new EntityAIAttackOnCollide(this, 1.0D, true));
      this.field_70714_bg.func_75776_a(4, new EntityAITradePlayerGeneric(this, this));
      this.field_70714_bg.func_75776_a(4, new EntityAILookAtTradePlayerGeneric(this, this));
      this.field_70714_bg.func_75776_a(5, new EntityAIMoveIndoorsLeashAware(this));
      this.field_70714_bg.func_75776_a(5, new EntityAIRestrictOpenDoor(this));
      this.field_70714_bg.func_75776_a(6, new EntityAIOpenDoor(this, true));
      this.field_70714_bg.func_75776_a(7, new EntityAIMoveTowardsRestriction(this, 0.6D));
      this.field_70714_bg.func_75776_a(8, new EntityAIGoblinMate(this));
      this.field_70714_bg.func_75776_a(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
      this.field_70714_bg.func_75776_a(9, new EntityAIWatchClosest2(this, EntityGoblin.class, 5.0F, 0.02F));
      this.field_70714_bg.func_75776_a(9, new EntityAIWander(this, 0.6D));
      this.field_70714_bg.func_75776_a(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
      this.field_70715_bh.func_75776_a(1, new EntityAIHurtByTarget(this, true));
      this.field_70715_bh.func_75776_a(2, new EntityAINearestAttackableTarget(this, EntityVillager.class, 0, true, true, this));
   }

   public boolean func_82704_a(Entity entity) {
      double R = 8.0D;
      if (entity instanceof EntityVillager) {
         return this.field_70170_p.func_72872_a(EntityGoblin.class, AxisAlignedBB.func_72330_a(this.field_70165_t - 8.0D, this.field_70163_u - 8.0D, this.field_70161_v - 8.0D, this.field_70165_t + 8.0D, this.field_70163_u + 8.0D, this.field_70161_v + 8.0D)).size() >= 3;
      } else {
         return true;
      }
   }

   public boolean shouldAvoid() {
      double R = 8.0D;
      return this.field_70170_p.func_72872_a(EntityGoblin.class, AxisAlignedBB.func_72330_a(this.field_70165_t - 8.0D, this.field_70163_u - 8.0D, this.field_70161_v - 8.0D, this.field_70165_t + 8.0D, this.field_70163_u + 8.0D, this.field_70161_v + 8.0D)).size() >= 3;
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.goblin.name");
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.4D);
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e).func_111128_a(2.0D);
   }

   public boolean func_70652_k(Entity targetEntity) {
      float f = (float)this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111126_e();
      int i = 0;
      if (targetEntity instanceof EntityLivingBase) {
         f += EnchantmentHelper.func_77512_a(this, (EntityLivingBase)targetEntity);
         i += EnchantmentHelper.func_77507_b(this, (EntityLivingBase)targetEntity);
      }

      boolean flag = targetEntity.func_70097_a(DamageSource.func_76358_a(this), f);
      if (flag) {
         if (i > 0) {
            targetEntity.func_70024_g((double)(-MathHelper.func_76126_a(this.field_70177_z * 3.1415927F / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.func_76134_b(this.field_70177_z * 3.1415927F / 180.0F) * (float)i * 0.5F));
            this.field_70159_w *= 0.6D;
            this.field_70179_y *= 0.6D;
         }

         int j = EnchantmentHelper.func_90036_a(this);
         if (j > 0) {
            targetEntity.func_70015_d(j * 4);
         }

         if (targetEntity instanceof EntityLivingBase) {
            EnchantmentHelper.func_151384_a((EntityLivingBase)targetEntity, this);
         }

         EnchantmentHelper.func_151385_b(this, targetEntity);
      }

      return flag;
   }

   public boolean func_70650_aV() {
      return true;
   }

   protected void func_70629_bd() {
      if (--this.randomTickDivider <= 0) {
         this.field_70170_p.field_72982_D.func_75551_a(MathHelper.func_76128_c(this.field_70165_t), MathHelper.func_76128_c(this.field_70163_u), MathHelper.func_76128_c(this.field_70161_v));
         this.randomTickDivider = 70 + this.field_70146_Z.nextInt(50);
         this.villageObj = this.field_70170_p.field_72982_D.func_75550_a(MathHelper.func_76128_c(this.field_70165_t), MathHelper.func_76128_c(this.field_70163_u), MathHelper.func_76128_c(this.field_70161_v), 32);
         if (this.villageObj == null) {
            this.func_110177_bN();
         } else {
            this.preventDespawn = true;
            ChunkCoordinates chunkcoordinates = this.villageObj.func_75577_a();
            this.func_110171_b(chunkcoordinates.field_71574_a, chunkcoordinates.field_71572_b, chunkcoordinates.field_71573_c, (int)((float)this.villageObj.func_75568_b() * 0.6F));
            if (this.isLookingForHome) {
               this.isLookingForHome = false;
               this.villageObj.func_82683_b(5);
            }
         }
      }

      if (!this.isTrading() && this.timeUntilReset > 0) {
         --this.timeUntilReset;
         if (this.timeUntilReset <= 0) {
            if (this.needsInitilization) {
               if (this.buyingList.size() > 1) {
                  Iterator iterator = this.buyingList.iterator();

                  while(iterator.hasNext()) {
                     MerchantRecipe merchantrecipe = (MerchantRecipe)iterator.next();
                     if (merchantrecipe.func_82784_g()) {
                        merchantrecipe.func_82783_a(this.field_70146_Z.nextInt(6) + this.field_70146_Z.nextInt(6) + 2);
                     }
                  }
               }

               this.addDefaultEquipmentAndRecipies(1);
               this.needsInitilization = false;
               if (this.villageObj != null && this.lastBuyingPlayer != null) {
                  this.field_70170_p.func_72960_a(this, (byte)14);
                  this.villageObj.func_82688_a(this.lastBuyingPlayer, 1);
               }
            }

            this.func_70690_d(new PotionEffect(Potion.field_76428_l.field_76415_H, 200, 0));
         }
      }

      super.func_70629_bd();
   }

   protected void func_110159_bB() {
      try {
         this.testingLeashRange = true;
         if (this.func_110167_bD()) {
            this.preventDespawn = true;
         }

         super.func_110159_bB();
      } finally {
         this.testingLeashRange = false;
      }

   }

   public float func_70032_d(Entity par1Entity) {
      float distance = super.func_70032_d(par1Entity);
      if (this.testingLeashRange && distance < 9.0F) {
         distance *= 0.5F;
      }

      return distance;
   }

   public void func_70071_h_() {
      super.func_70071_h_();
      if (!this.field_70170_p.field_72995_K) {
         this.setBesideClimbableBlock(this.field_70123_F);
      }

   }

   public boolean func_70617_f_() {
      return super.func_70617_f_();
   }

   public boolean isWorking() {
      return this.field_70180_af.func_75683_a(18) == 1;
   }

   public void setWorking(boolean par1) {
      byte b0 = this.field_70180_af.func_75683_a(18);
      if (par1 && b0 != 1 || !par1 && b0 == 1) {
         this.field_70180_af.func_75692_b(18, (byte)(par1 ? 1 : 0));
      }

   }

   public boolean isWorshipping() {
      return this.field_70180_af.func_75683_a(18) == 2;
   }

   public void setWorshipping(boolean worshiping) {
      byte b0 = this.field_70180_af.func_75683_a(18);
      if (worshiping && b0 != 2 || !worshiping && b0 == 2) {
         this.field_70180_af.func_75692_b(18, (byte)(worshiping ? 2 : 0));
      }

   }

   public void beginWorship(TileEntity tile) {
      this.aiWorship.begin(tile);
   }

   public boolean isBesideClimbableBlock() {
      return (this.field_70180_af.func_75683_a(17) & 1) != 0;
   }

   public void setBesideClimbableBlock(boolean par1) {
      byte b0 = this.field_70180_af.func_75683_a(17);
      if (par1) {
         b0 = (byte)(b0 | 1);
      } else {
         b0 &= -2;
      }

      this.field_70180_af.func_75692_b(17, b0);
   }

   public boolean func_70085_c(EntityPlayer player) {
      ItemStack stack = player.field_71071_by.func_70448_g();
      boolean heldSpawnEgg = stack != null && stack.func_77973_b() == Items.field_151063_bx;
      if (!heldSpawnEgg && this.func_70089_S() && !this.isTrading() && !this.func_70631_g_() && !player.func_70093_af()) {
         if (this.func_110167_bD()) {
            if (this.func_70694_bm() == null) {
               if (stack != null && stack.func_77973_b() instanceof ItemPickaxe) {
                  this.func_70062_b(0, stack);
                  player.func_70062_b(0, (ItemStack)null);
               }
            } else {
               if (!this.field_70170_p.field_72995_K) {
                  ItemStack goblinItem = this.func_70694_bm();
                  if (!player.field_71071_by.func_70441_a(goblinItem)) {
                     this.func_70099_a(goblinItem, 1.0F);
                  } else if (player instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                  }
               }

               this.func_70062_b(0, (ItemStack)null);
            }
         } else if (!this.isWorking() && !this.isWorshipping() && this.villageObj != null && !this.field_70170_p.field_72995_K) {
            this.func_70932_a_(player);
            player.func_71030_a(this, this.func_70005_c_());
         }

         return true;
      } else {
         return super.func_70085_c(player);
      }
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(16, 0);
      this.field_70180_af.func_75682_a(17, (byte)0);
      this.field_70180_af.func_75682_a(18, (byte)0);
      this.field_70180_af.func_75682_a(19, (byte)0);
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74768_a("Profession", this.getProfession());
      nbtRoot.func_74768_a("Riches", this.wealth);
      nbtRoot.func_74757_a("Worshipping", this.isWorshipping());
      if (this.buyingList != null) {
         nbtRoot.func_74782_a("Offers", this.buyingList.func_77202_a());
      }

      nbtRoot.func_74757_a("PreventDespawn", this.preventDespawn);
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      this.setProfession(nbtRoot.func_74762_e("Profession"));
      this.wealth = nbtRoot.func_74762_e("Riches");
      if (nbtRoot.func_74767_n("Worshipping") && !this.field_70170_p.field_72995_K) {
         this.setWorshipping(true);
      }

      if (nbtRoot.func_150297_b("Offers", 10)) {
         NBTTagCompound nbttagcompound1 = nbtRoot.func_74775_l("Offers");
         this.buyingList = new MerchantRecipeList(nbttagcompound1);
      }

      this.preventDespawn = nbtRoot.func_74767_n("PreventDespawn");
   }

   protected float func_70647_i() {
      return 1.2F;
   }

   protected boolean func_70692_ba() {
      return !Config.instance().goblinDespawnBlock && this.villageObj == null && !this.preventDespawn && !this.isWorshipping();
   }

   protected String func_70639_aQ() {
      return this.isTrading() ? "witchery:mob.goblin.haggle" : "witchery:mob.goblin.idle";
   }

   protected String func_70621_aR() {
      return "witchery:mob.goblin.hit";
   }

   protected String func_70673_aS() {
      return "witchery:mob.goblin.death";
   }

   public void setProfession(int par1) {
      this.field_70180_af.func_75692_b(16, par1);
   }

   public int getProfession() {
      return this.field_70180_af.func_75679_c(16);
   }

   public boolean isMating() {
      return this.isMating;
   }

   public void setMating(boolean par1) {
      this.isMating = par1;
   }

   public void func_70604_c(EntityLivingBase par1EntityLivingBase) {
      super.func_70604_c(par1EntityLivingBase);
      if (this.villageObj != null && par1EntityLivingBase != null) {
         this.villageObj.func_75575_a(par1EntityLivingBase);
         if (par1EntityLivingBase instanceof EntityPlayer) {
            if (this.func_70631_g_()) {
               this.villageObj.func_82688_a(par1EntityLivingBase.func_70005_c_(), -3);
            }

            if (this.func_70089_S()) {
               this.field_70170_p.func_72960_a(this, (byte)13);
            }
         }
      }

   }

   public void func_70645_a(DamageSource par1DamageSource) {
      if (this.villageObj != null) {
         Entity entity = par1DamageSource.func_76346_g();
         if (entity != null) {
            if (!(entity instanceof EntityPlayer) && entity instanceof IMob) {
               this.villageObj.func_82692_h();
            }
         } else if (entity == null) {
            EntityPlayer entityplayer = this.field_70170_p.func_72890_a(this, 16.0D);
            if (entityplayer != null) {
               this.villageObj.func_82692_h();
            }
         }
      }

      super.func_70645_a(par1DamageSource);
   }

   public boolean func_70601_bi() {
      int i = MathHelper.func_76128_c(this.field_70165_t);
      int j = MathHelper.func_76128_c(this.field_70121_D.field_72338_b);
      int k = MathHelper.func_76128_c(this.field_70161_v);
      return this.field_70170_p.func_147439_a(i, j - 1, k) == Blocks.field_150349_c && this.field_70170_p.func_72883_k(i, j, k) > 8 && super.func_70601_bi();
   }

   public void func_70932_a_(EntityPlayer par1EntityPlayer) {
      this.buyingPlayer = par1EntityPlayer;
   }

   public EntityPlayer func_70931_l_() {
      return this.buyingPlayer;
   }

   public boolean isTrading() {
      return this.buyingPlayer != null;
   }

   public void func_70933_a(MerchantRecipe par1MerchantRecipe) {
      par1MerchantRecipe.func_77399_f();
      this.field_70757_a = -this.func_70627_aG();
      this.func_85030_a("witchery:mob.goblin.yes", this.func_70599_aP(), this.func_70647_i());
      if (par1MerchantRecipe.func_77393_a((MerchantRecipe)this.buyingList.get(this.buyingList.size() - 1))) {
         this.timeUntilReset = 40;
         this.needsInitilization = true;
         if (this.buyingPlayer != null) {
            this.lastBuyingPlayer = this.buyingPlayer.func_70005_c_();
         } else {
            this.lastBuyingPlayer = null;
         }
      }

      if (par1MerchantRecipe.func_77394_a().func_77973_b() == Items.field_151166_bC) {
         this.wealth += par1MerchantRecipe.func_77394_a().field_77994_a;
      }

   }

   public void func_110297_a_(ItemStack par1ItemStack) {
      if (!this.field_70170_p.field_72995_K && this.field_70757_a > -this.func_70627_aG() + 20) {
         this.field_70757_a = -this.func_70627_aG();
         if (par1ItemStack != null) {
            this.func_85030_a("witchery:mob.goblin.yes", this.func_70599_aP(), this.func_70647_i());
         } else {
            this.func_85030_a("witchery:mob.goblin.no", this.func_70599_aP(), this.func_70647_i());
         }
      }

   }

   public MerchantRecipeList func_70934_b(EntityPlayer par1EntityPlayer) {
      if (this.buyingList == null) {
         this.addDefaultEquipmentAndRecipies(1);
      }

      return this.buyingList;
   }

   private float adjustProbability(float par1) {
      float f1 = par1 + this.field_82191_bN;
      return f1 > 0.9F ? 0.9F - (f1 - 0.9F) : f1;
   }

   private void addDefaultEquipmentAndRecipies(int par1) {
      if (this.buyingList != null) {
         this.field_82191_bN = MathHelper.func_76129_c((float)this.buyingList.size()) * 0.2F;
      } else {
         this.field_82191_bN = 0.0F;
      }

      MerchantRecipeList merchantrecipelist = new MerchantRecipeList();
      boolean shuffle = true;
      switch(this.getProfession()) {
      case 0:
         addItemToSwapForAnEmerald(merchantrecipelist, Items.field_151015_O, this.field_70146_Z, this.adjustProbability(0.9F));
         addItemToSwapForAnEmerald(merchantrecipelist, Item.func_150898_a(Blocks.field_150325_L), this.field_70146_Z, this.adjustProbability(0.5F));
         addItemToSwapForAnEmerald(merchantrecipelist, Items.field_151076_bf, this.field_70146_Z, this.adjustProbability(0.5F));
         addItemToSwapForAnEmerald(merchantrecipelist, Items.field_151101_aQ, this.field_70146_Z, this.adjustProbability(0.4F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151025_P, this.field_70146_Z, this.adjustProbability(0.9F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151127_ba, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151034_e, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151106_aX, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151097_aZ, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151033_d, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151077_bg, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151032_g, this.field_70146_Z, this.adjustProbability(0.5F));
         if (this.field_70146_Z.nextFloat() < this.adjustProbability(0.5F)) {
            merchantrecipelist.add(new MerchantRecipe(new ItemStack(Blocks.field_150351_n, 10), new ItemStack(Items.field_151166_bC), new ItemStack(Items.field_151145_ak, 4 + this.field_70146_Z.nextInt(2), 0)));
         }
         break;
      case 1:
      case 2:
         shuffle = false;
         if (this.buyingList == null) {
            merchantrecipelist.add(new MerchantRecipe(Witchery.Items.GENERIC.itemKobolditeDust.createStack(9), new ItemStack(Items.field_151074_bl, 5), Witchery.Items.GENERIC.itemKobolditeNugget.createStack()));
         } else if (this.buyingList.size() == 1) {
            merchantrecipelist.add(new MerchantRecipe(Witchery.Items.GENERIC.itemKobolditeDust.createStack(16), new ItemStack(Items.field_151043_k), Witchery.Items.GENERIC.itemKobolditeNugget.createStack(2)));
         } else if (this.buyingList.size() == 2) {
            merchantrecipelist.add(new MerchantRecipe(Witchery.Items.GENERIC.itemKobolditeNugget.createStack(9), new ItemStack(Items.field_151166_bC), Witchery.Items.GENERIC.itemKobolditeIngot.createStack()));
         }
         break;
      case 3:
         addItemToSwapForAnEmerald(merchantrecipelist, Items.field_151044_h, this.field_70146_Z, this.adjustProbability(0.7F));
         addItemToSwapForAnEmerald(merchantrecipelist, Items.field_151042_j, this.field_70146_Z, this.adjustProbability(0.5F));
         addItemToSwapForAnEmerald(merchantrecipelist, Items.field_151043_k, this.field_70146_Z, this.adjustProbability(0.5F));
         addItemToSwapForAnEmerald(merchantrecipelist, Items.field_151045_i, this.field_70146_Z, this.adjustProbability(0.5F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151040_l, this.field_70146_Z, this.adjustProbability(0.5F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151048_u, this.field_70146_Z, this.adjustProbability(0.5F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151036_c, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151056_x, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151035_b, this.field_70146_Z, this.adjustProbability(0.5F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151046_w, this.field_70146_Z, this.adjustProbability(0.5F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151037_a, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151047_v, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151019_K, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151012_L, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151167_ab, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151175_af, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151028_Y, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151161_ac, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151030_Z, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151163_ad, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151165_aa, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151173_ae, this.field_70146_Z, this.adjustProbability(0.2F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151029_X, this.field_70146_Z, this.adjustProbability(0.1F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151020_U, this.field_70146_Z, this.adjustProbability(0.1F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151023_V, this.field_70146_Z, this.adjustProbability(0.1F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151022_W, this.field_70146_Z, this.adjustProbability(0.1F));
         break;
      case 4:
         addItemToSwapForAnEmerald(merchantrecipelist, Items.field_151044_h, this.field_70146_Z, this.adjustProbability(0.7F));
         addItemToSwapForAnEmerald(merchantrecipelist, Items.field_151147_al, this.field_70146_Z, this.adjustProbability(0.5F));
         addItemToSwapForAnEmerald(merchantrecipelist, Items.field_151082_bd, this.field_70146_Z, this.adjustProbability(0.5F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151141_av, this.field_70146_Z, this.adjustProbability(0.1F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151027_R, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151021_T, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151024_Q, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151026_S, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151157_am, this.field_70146_Z, this.adjustProbability(0.3F));
         addItemToBuyOrSell(merchantrecipelist, Items.field_151083_be, this.field_70146_Z, this.adjustProbability(0.3F));
      }

      if (merchantrecipelist.isEmpty()) {
         merchantrecipelist.add(new MerchantRecipe(getItemStackToSwapForAnEmerald(Items.field_151043_k, this.field_70146_Z), Items.field_151166_bC));
      }

      if (shuffle) {
         Collections.shuffle(merchantrecipelist);
      }

      if (this.buyingList == null) {
         this.buyingList = new MerchantRecipeList();
      }

      for(int l = 0; l < par1 && l < merchantrecipelist.size(); ++l) {
         this.buyingList.func_77205_a((MerchantRecipe)merchantrecipelist.get(l));
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_70930_a(MerchantRecipeList par1MerchantRecipeList) {
   }

   public static void addItemToSwapForAnEmerald(MerchantRecipeList buyList, Item item, Random rand, float probability) {
      if (rand.nextFloat() < probability) {
         if (rand.nextInt(3) == 0) {
            buyList.add(new MerchantRecipe(getItemStackToSwapForAnEmerald(item, rand), Witchery.Items.GENERIC.itemKobolditeDust.createStack()));
         } else {
            buyList.add(new MerchantRecipe(getItemStackToSwapForAnEmerald(item, rand), Items.field_151166_bC));
         }
      }

   }

   private static ItemStack getItemStackToSwapForAnEmerald(Item item, Random rand) {
      return new ItemStack(item, getQuantityToSwapForAnEmerald(item, rand), 0);
   }

   private static int getQuantityToSwapForAnEmerald(Item item, Random rand) {
      Tuple tuple = (Tuple)villagersSellingList.get(item);
      return tuple == null ? 1 : ((Integer)tuple.func_76341_a() >= (Integer)tuple.func_76340_b() ? (Integer)tuple.func_76341_a() : (Integer)tuple.func_76341_a() + rand.nextInt((Integer)tuple.func_76340_b() - (Integer)tuple.func_76341_a()));
   }

   public static void addItemToBuyOrSell(MerchantRecipeList list, Item item, Random rand, float probability) {
      if (rand.nextFloat() < probability) {
         int i = quantityToBuyOrSell(item, rand);
         ItemStack itemstack;
         ItemStack itemstack1;
         if (i < 0) {
            itemstack = Witchery.Items.GENERIC.itemKobolditeNugget.createStack(1);
            itemstack1 = new ItemStack(item, -i, 0);
         } else {
            itemstack = Witchery.Items.GENERIC.itemKobolditeNugget.createStack(i);
            itemstack1 = new ItemStack(item, 1, 0);
         }

         list.add(new MerchantRecipe(itemstack, itemstack1));
      }

   }

   private static int quantityToBuyOrSell(Item item, Random rand) {
      Tuple tuple = (Tuple)blacksmithSellingList.get(item);
      return tuple == null ? 1 : ((Integer)tuple.func_76341_a() >= (Integer)tuple.func_76340_b() ? (Integer)tuple.func_76341_a() : (Integer)tuple.func_76341_a() + rand.nextInt((Integer)tuple.func_76340_b() - (Integer)tuple.func_76341_a()));
   }

   @SideOnly(Side.CLIENT)
   public void func_70103_a(byte par1) {
      if (par1 == 12) {
         this.generateRandomParticles("heart");
      } else if (par1 == 13) {
         this.generateRandomParticles("angryVillager");
      } else if (par1 == 14) {
         this.generateRandomParticles("happyVillager");
      } else {
         super.func_70103_a(par1);
      }

   }

   public IEntityLivingData func_110161_a(IEntityLivingData par1EntityLivingData) {
      par1EntityLivingData = super.func_110161_a(par1EntityLivingData);
      int trade = this.field_70146_Z.nextInt(5);
      this.setProfession(trade);
      return par1EntityLivingData;
   }

   @SideOnly(Side.CLIENT)
   private void generateRandomParticles(String par1Str) {
      for(int i = 0; i < 5; ++i) {
         double d0 = this.field_70146_Z.nextGaussian() * 0.02D;
         double d1 = this.field_70146_Z.nextGaussian() * 0.02D;
         double d2 = this.field_70146_Z.nextGaussian() * 0.02D;
         this.field_70170_p.func_72869_a(par1Str, this.field_70165_t + (double)(this.field_70146_Z.nextFloat() * this.field_70130_N * 2.0F) - (double)this.field_70130_N, this.field_70163_u + 1.0D + (double)(this.field_70146_Z.nextFloat() * this.field_70131_O), this.field_70161_v + (double)(this.field_70146_Z.nextFloat() * this.field_70130_N * 2.0F) - (double)this.field_70130_N, d0, d1, d2);
      }

   }

   public void setLookingForHome() {
      this.isLookingForHome = true;
   }

   public EntityGoblin createChild(EntityAgeable par1EntityAgeable) {
      EntityGoblin entityvillager = new EntityGoblin(this.field_70170_p);
      entityvillager.func_110161_a((IEntityLivingData)null);
      return entityvillager;
   }

   static {
      villagersSellingList.put(Items.field_151044_h, new Tuple(16, 24));
      villagersSellingList.put(Items.field_151042_j, new Tuple(8, 10));
      villagersSellingList.put(Items.field_151043_k, new Tuple(8, 10));
      villagersSellingList.put(Items.field_151045_i, new Tuple(4, 6));
      villagersSellingList.put(Items.field_151121_aF, new Tuple(24, 36));
      villagersSellingList.put(Items.field_151122_aG, new Tuple(11, 13));
      villagersSellingList.put(Items.field_151164_bB, new Tuple(1, 1));
      villagersSellingList.put(Items.field_151079_bi, new Tuple(3, 4));
      villagersSellingList.put(Items.field_151061_bv, new Tuple(2, 3));
      villagersSellingList.put(Items.field_151147_al, new Tuple(14, 18));
      villagersSellingList.put(Items.field_151082_bd, new Tuple(14, 18));
      villagersSellingList.put(Items.field_151076_bf, new Tuple(14, 18));
      villagersSellingList.put(Items.field_151101_aQ, new Tuple(9, 13));
      villagersSellingList.put(Items.field_151014_N, new Tuple(34, 48));
      villagersSellingList.put(Items.field_151081_bc, new Tuple(30, 38));
      villagersSellingList.put(Items.field_151080_bb, new Tuple(30, 38));
      villagersSellingList.put(Items.field_151015_O, new Tuple(18, 22));
      villagersSellingList.put(Item.func_150898_a(Blocks.field_150325_L), new Tuple(14, 22));
      villagersSellingList.put(Items.field_151078_bh, new Tuple(36, 64));
      blacksmithSellingList.put(Items.field_151033_d, new Tuple(3, 4));
      blacksmithSellingList.put(Items.field_151097_aZ, new Tuple(3, 4));
      blacksmithSellingList.put(Items.field_151040_l, new Tuple(7, 11));
      blacksmithSellingList.put(Items.field_151048_u, new Tuple(12, 14));
      blacksmithSellingList.put(Items.field_151036_c, new Tuple(6, 8));
      blacksmithSellingList.put(Items.field_151056_x, new Tuple(9, 12));
      blacksmithSellingList.put(Items.field_151035_b, new Tuple(7, 9));
      blacksmithSellingList.put(Items.field_151046_w, new Tuple(10, 12));
      blacksmithSellingList.put(Items.field_151037_a, new Tuple(4, 6));
      blacksmithSellingList.put(Items.field_151047_v, new Tuple(7, 8));
      blacksmithSellingList.put(Items.field_151019_K, new Tuple(4, 6));
      blacksmithSellingList.put(Items.field_151012_L, new Tuple(7, 8));
      blacksmithSellingList.put(Items.field_151167_ab, new Tuple(4, 6));
      blacksmithSellingList.put(Items.field_151175_af, new Tuple(7, 8));
      blacksmithSellingList.put(Items.field_151028_Y, new Tuple(4, 6));
      blacksmithSellingList.put(Items.field_151161_ac, new Tuple(7, 8));
      blacksmithSellingList.put(Items.field_151030_Z, new Tuple(10, 14));
      blacksmithSellingList.put(Items.field_151163_ad, new Tuple(16, 19));
      blacksmithSellingList.put(Items.field_151165_aa, new Tuple(8, 10));
      blacksmithSellingList.put(Items.field_151173_ae, new Tuple(11, 14));
      blacksmithSellingList.put(Items.field_151029_X, new Tuple(5, 7));
      blacksmithSellingList.put(Items.field_151020_U, new Tuple(5, 7));
      blacksmithSellingList.put(Items.field_151023_V, new Tuple(11, 15));
      blacksmithSellingList.put(Items.field_151022_W, new Tuple(9, 11));
      blacksmithSellingList.put(Items.field_151025_P, new Tuple(-4, -2));
      blacksmithSellingList.put(Items.field_151127_ba, new Tuple(-8, -4));
      blacksmithSellingList.put(Items.field_151034_e, new Tuple(-8, -4));
      blacksmithSellingList.put(Items.field_151106_aX, new Tuple(-10, -7));
      blacksmithSellingList.put(Item.func_150898_a(Blocks.field_150359_w), new Tuple(-5, -3));
      blacksmithSellingList.put(Item.func_150898_a(Blocks.field_150342_X), new Tuple(3, 4));
      blacksmithSellingList.put(Items.field_151027_R, new Tuple(4, 5));
      blacksmithSellingList.put(Items.field_151021_T, new Tuple(2, 4));
      blacksmithSellingList.put(Items.field_151024_Q, new Tuple(2, 4));
      blacksmithSellingList.put(Items.field_151026_S, new Tuple(2, 4));
      blacksmithSellingList.put(Items.field_151141_av, new Tuple(6, 8));
      blacksmithSellingList.put(Items.field_151062_by, new Tuple(-4, -1));
      blacksmithSellingList.put(Items.field_151137_ax, new Tuple(-4, -1));
      blacksmithSellingList.put(Items.field_151111_aL, new Tuple(10, 12));
      blacksmithSellingList.put(Items.field_151113_aN, new Tuple(10, 12));
      blacksmithSellingList.put(Item.func_150898_a(Blocks.field_150426_aN), new Tuple(-3, -1));
      blacksmithSellingList.put(Items.field_151157_am, new Tuple(-7, -5));
      blacksmithSellingList.put(Items.field_151083_be, new Tuple(-7, -5));
      blacksmithSellingList.put(Items.field_151077_bg, new Tuple(-8, -6));
      blacksmithSellingList.put(Items.field_151061_bv, new Tuple(7, 11));
      blacksmithSellingList.put(Items.field_151032_g, new Tuple(-12, -8));
   }
}
