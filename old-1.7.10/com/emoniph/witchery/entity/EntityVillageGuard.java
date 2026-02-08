package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.ai.EntityAIDefendVillageGeneric;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class EntityVillageGuard extends EntityCreature implements IRangedAttackMob, EntityAIDefendVillageGeneric.IVillageGuard, IEntitySelector {
   private EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 1.0D, 20, 60, 15.0F);
   private EntityAIAttackOnCollide aiAttackOnCollide = new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.2D, false);
   private int homeCheckTimer;
   Village villageObj;

   public EntityVillageGuard(World world) {
      super(world);
      this.func_70661_as().func_75491_a(true);
      this.func_70661_as().func_75498_b(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(6, new EntityAIMoveThroughVillage(this, 0.6D, true));
      this.field_70714_bg.func_75776_a(7, new EntityAIMoveTowardsRestriction(this, 1.0D));
      this.field_70714_bg.func_75776_a(8, new EntityAIRestrictOpenDoor(this));
      this.field_70714_bg.func_75776_a(9, new EntityAIOpenDoor(this, true));
      this.field_70714_bg.func_75776_a(10, new EntityAIWander(this, 1.0D));
      this.field_70714_bg.func_75776_a(11, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(12, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAIDefendVillageGeneric(this));
      this.field_70715_bh.func_75776_a(2, new EntityAIHurtByTarget(this, true));
      this.field_70715_bh.func_75776_a(3, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 0, false, true, this));
      if (world != null && !world.field_72995_K) {
         this.setCombatTask();
      }

      this.field_70728_aV = 5;
   }

   public boolean func_70686_a(Class p_70686_1_) {
      return EntityCreeper.class != p_70686_1_ && this.getClass() != p_70686_1_;
   }

   public boolean func_82704_a(Entity entity) {
      if ((!(entity instanceof IMob) || entity instanceof EntityWitchHunter) && !(entity instanceof EntityGoblin)) {
         if (this.villageObj != null && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entity;
            EntityLivingBase target = player.func_70643_av();
            if (target instanceof EntityPlayer && this.villageObj.func_82684_a(target.func_70005_c_()) == 10) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public int getBlood() {
      return this.field_70180_af.func_75679_c(14);
   }

   public void setBlood(int blood) {
      this.field_70180_af.func_75692_b(14, MathHelper.func_76125_a(blood, 0, 500));
   }

   public int takeBlood(int quantity, EntityLivingBase player) {
      PotionEffect effect = this.func_70660_b(Witchery.Potions.PARALYSED);
      boolean transfixed = effect != null && effect.func_76458_c() >= 4;
      int blood = this.getBlood();
      quantity = (int)Math.ceil((double)(0.66F * (float)quantity));
      int remainder = Math.max(blood - quantity, 0);
      int taken = blood - remainder;
      this.setBlood(remainder);
      if (blood < (int)Math.ceil(250.0D)) {
         this.func_70097_a(new EntityDamageSource(DamageSource.field_76376_m.func_76355_l(), player), 2.0F);
      } else if (!transfixed) {
         this.func_70097_a(new EntityDamageSource(DamageSource.field_76376_m.func_76355_l(), player), 0.5F);
      }

      return taken;
   }

   public void giveBlood(int quantity) {
      int blood = this.getBlood();
      if (blood < 500) {
         this.setBlood(blood + quantity);
      }

   }

   public Village getVillage() {
      return this.villageObj;
   }

   public EntityCreature getCreature() {
      return this;
   }

   protected void func_70629_bd() {
      if (--this.homeCheckTimer <= 0) {
         this.homeCheckTimer = 70 + this.field_70146_Z.nextInt(50);
         this.villageObj = this.field_70170_p.field_72982_D.func_75550_a(MathHelper.func_76128_c(this.field_70165_t), MathHelper.func_76128_c(this.field_70163_u), MathHelper.func_76128_c(this.field_70161_v), 32);
         if (this.villageObj == null) {
            this.func_110177_bN();
         } else {
            ChunkCoordinates chunkcoordinates = this.villageObj.func_75577_a();
            this.func_110171_b(chunkcoordinates.field_71574_a, chunkcoordinates.field_71572_b, chunkcoordinates.field_71573_c, (int)((float)this.villageObj.func_75568_b() * 1.5F));
            if (this.func_70638_az() == null) {
               this.func_70691_i(1.0F);
               if (this.field_70170_p.field_73012_v.nextInt(4) == 0) {
                  this.giveBlood(1);
               }
            }
         }
      }

      super.func_70629_bd();
   }

   protected String func_145776_H() {
      return "game.hostile.swim";
   }

   protected String func_145777_O() {
      return "game.hostile.swim.splash";
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
      this.func_110148_a(SharedMonsterAttributes.field_111265_b).func_111128_a(40.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.3D);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(40.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(4.0D);
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(13, new Byte((byte)0));
      this.field_70180_af.func_75682_a(14, new Integer(500));
   }

   public boolean func_70650_aV() {
      return true;
   }

   protected String func_70639_aQ() {
      return "mob.villager.idle";
   }

   protected String func_70621_aR() {
      return "mob.villager.hit";
   }

   protected String func_70673_aS() {
      return "mob.villager.death";
   }

   protected float func_70647_i() {
      return 0.8F;
   }

   public void func_70636_d() {
      this.func_82168_bl();
      float f = this.func_70013_c(1.0F);
      if (f > 0.5F) {
         this.field_70708_bq += 2;
      }

      super.func_70636_d();
   }

   public boolean func_70097_a(DamageSource damageSource, float damage) {
      return damageSource.func_76346_g() == null || !(damageSource.func_76346_g() instanceof EntityVillageGuard) && !(damageSource.func_76346_g() instanceof EntityWitchHunter) ? super.func_70097_a(damageSource, damage) : false;
   }

   public boolean func_70652_k(Entity p_70652_1_) {
      float f = (float)this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111126_e();
      int i = 0;
      if (p_70652_1_ instanceof EntityLivingBase) {
         f += EnchantmentHelper.func_77512_a(this, (EntityLivingBase)p_70652_1_);
         i += EnchantmentHelper.func_77507_b(this, (EntityLivingBase)p_70652_1_);
      }

      boolean flag = p_70652_1_.func_70097_a(DamageSource.func_76358_a(this), f);
      if (flag) {
         if (i > 0) {
            p_70652_1_.func_70024_g((double)(-MathHelper.func_76126_a(this.field_70177_z * 3.1415927F / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.func_76134_b(this.field_70177_z * 3.1415927F / 180.0F) * (float)i * 0.5F));
            this.field_70159_w *= 0.6D;
            this.field_70179_y *= 0.6D;
         }

         int j = EnchantmentHelper.func_90036_a(this);
         if (j > 0) {
            p_70652_1_.func_70015_d(j * 4);
         }

         if (p_70652_1_ instanceof EntityLivingBase) {
            EnchantmentHelper.func_151384_a((EntityLivingBase)p_70652_1_, this);
         }

         EnchantmentHelper.func_151385_b(this, p_70652_1_);
      }

      return flag;
   }

   protected void func_70785_a(Entity p_70785_1_, float p_70785_2_) {
      if (this.field_70724_aR <= 0 && p_70785_2_ < 2.0F && p_70785_1_.field_70121_D.field_72337_e > this.field_70121_D.field_72338_b && p_70785_1_.field_70121_D.field_72338_b < this.field_70121_D.field_72337_e) {
         this.field_70724_aR = 20;
         this.func_70652_k(p_70785_1_);
      }

   }

   protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_) {
   }

   public void func_70098_U() {
      super.func_70098_U();
      if (this.field_70154_o instanceof EntityCreature) {
         EntityCreature entitycreature = (EntityCreature)this.field_70154_o;
         this.field_70761_aq = entitycreature.field_70761_aq;
      }

   }

   protected String func_146067_o(int p_146067_1_) {
      return p_146067_1_ > 4 ? "game.hostile.hurt.fall.big" : "game.hostile.hurt.fall.small";
   }

   public void func_70645_a(DamageSource p_70645_1_) {
      if (this.field_70717_bb != null && this.villageObj != null) {
         this.villageObj.func_82688_a(this.field_70717_bb.func_70005_c_(), -5);
      }

      super.func_70645_a(p_70645_1_);
   }

   protected Item func_146068_u() {
      return Items.field_151032_g;
   }

   protected void func_70600_l(int p_70600_1_) {
      this.func_70099_a(new ItemStack(Items.field_151027_R, 1), 0.0F);
   }

   protected void func_82164_bB() {
      this.func_70062_b(0, new ItemStack(Items.field_151031_f));
      this.func_70062_b(1, new ItemStack(Items.field_151021_T));
      this.func_70062_b(2, new ItemStack(Items.field_151026_S));
      this.func_70062_b(3, new ItemStack(this.field_70170_p.field_73012_v.nextInt(5) == 0 ? Items.field_151030_Z : Items.field_151027_R));
      this.func_70062_b(4, new ItemStack(this.field_70170_p.field_73012_v.nextInt(5) == 0 ? Items.field_151028_Y : Items.field_151024_Q));
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.villageguard.name");
   }

   public IEntityLivingData func_110161_a(IEntityLivingData p_110161_1_) {
      p_110161_1_ = super.func_110161_a(p_110161_1_);
      this.func_82164_bB();
      return p_110161_1_;
   }

   public void setCombatTask() {
      this.field_70714_bg.func_85156_a(this.aiAttackOnCollide);
      this.field_70714_bg.func_85156_a(this.aiArrowAttack);
      ItemStack itemstack = this.func_70694_bm();
      if (itemstack != null && itemstack.func_77973_b() == Items.field_151031_f) {
         this.field_70714_bg.func_75776_a(4, this.aiArrowAttack);
      } else {
         this.field_70714_bg.func_75776_a(4, this.aiAttackOnCollide);
      }

   }

   public void func_82196_d(EntityLivingBase target, float p_82196_2_) {
      PotionEffect effect = this.func_70660_b(Witchery.Potions.PARALYSED);
      if (effect == null || effect.func_76458_c() < 4) {
         EntityArrow entityarrow = new EntityArrow(this.field_70170_p, this, target, 1.6F, (float)(16 - this.field_70170_p.field_73013_u.func_151525_a() * 4));
         int i = EnchantmentHelper.func_77506_a(Enchantment.field_77345_t.field_77352_x, this.func_70694_bm());
         int j = EnchantmentHelper.func_77506_a(Enchantment.field_77344_u.field_77352_x, this.func_70694_bm());
         entityarrow.func_70239_b((double)(p_82196_2_ * 2.0F) + this.field_70146_Z.nextGaussian() * 0.25D + (double)((float)this.field_70170_p.field_73013_u.func_151525_a() * 0.11F));
         if (i > 0) {
            entityarrow.func_70239_b(entityarrow.func_70242_d() + (double)i * 0.5D + 0.5D);
         }

         if (j > 0) {
            entityarrow.func_70240_a(j);
         }

         if (EnchantmentHelper.func_77506_a(Enchantment.field_77343_v.field_77352_x, this.func_70694_bm()) > 0 || this.getGuardType() == 1) {
            entityarrow.func_70015_d(100);
         }

         this.func_85030_a("random.bow", 1.0F, 1.0F / (this.func_70681_au().nextFloat() * 0.4F + 0.8F));
         this.field_70170_p.func_72838_d(entityarrow);
      }
   }

   public int getGuardType() {
      return this.field_70180_af.func_75683_a(13);
   }

   public void setGuardType(int p_82201_1_) {
      this.field_70180_af.func_75692_b(13, (byte)p_82201_1_);
      this.field_70178_ae = p_82201_1_ == 1;
      if (p_82201_1_ == 1) {
         this.func_70105_a(0.72F, 2.34F);
      } else {
         this.func_70105_a(0.6F, 1.8F);
      }

   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      if (nbtRoot.func_150297_b("GuardType", 99)) {
         byte b0 = nbtRoot.func_74771_c("GuardType");
         this.setGuardType(b0);
      }

      this.setCombatTask();
      if (nbtRoot.func_74764_b("BloodLevel")) {
         this.setBlood(nbtRoot.func_74762_e("BloodLevel"));
      }

   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74774_a("GuardType", (byte)this.getGuardType());
      nbtRoot.func_74768_a("BloodLevel", this.getBlood());
   }

   public void func_70062_b(int p_70062_1_, ItemStack p_70062_2_) {
      super.func_70062_b(p_70062_1_, p_70062_2_);
      if (!this.field_70170_p.field_72995_K && p_70062_1_ == 0) {
         this.setCombatTask();
      }

   }

   public double func_70033_W() {
      return super.func_70033_W() - 0.5D;
   }

   public static void createFrom(EntityVillager villager) {
      World world = villager.field_70170_p;
      EntityVillageGuard entity = new EntityVillageGuard(world);
      entity.func_110163_bv();
      entity.func_82149_j(villager);
      entity.func_110161_a((IEntityLivingData)null);
      world.func_72900_e(villager);
      world.func_72838_d(entity);
      ParticleEffect.INSTANT_SPELL.send(SoundEffect.NONE, entity, (double)entity.field_70130_N, (double)entity.field_70131_O, 16);
   }
}
