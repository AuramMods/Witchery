package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.network.PacketSound;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import java.util.List;
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
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.village.Village;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class EntityWitchHunter extends EntityCreature implements IRangedAttackMob, IEntitySelector {
   private EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 1.0D, 20, 60, 15.0F);
   private EntityAIAttackOnCollide aiAttackOnCollide = new EntityAIAttackOnCollide(this, EntityLivingBase.class, 1.2D, false);
   private String targetPlayerName;
   private static final double HUNTER_NOTICE_CHANCE = 0.1D;
   private static final long HUNTER_DELAY = (long)TimeUtil.minsToTicks(2);
   private static final double HUNTER_TRIGGER_CHANCE = 0.01D;

   public EntityWitchHunter(World par1World) {
      super(par1World);
      this.func_70661_as().func_75491_a(true);
      this.func_70661_as().func_75495_e(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(5, new EntityAIWander(this, 1.0D));
      this.field_70714_bg.func_75776_a(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(6, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAIHurtByTarget(this, false));
      this.field_70715_bh.func_75776_a(2, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 0, false, true, this));
      this.field_70728_aV = 5;
      this.targetPlayerName = "";
      if (par1World != null && !par1World.field_72995_K) {
         this.setCombatTask();
      }

   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.witchhunter.name");
   }

   public boolean func_82704_a(Entity entity) {
      if (!CreatureUtil.isUndead(entity) && !CreatureUtil.isDemonic(entity) && !(entity instanceof EntityWitch) && !CreatureUtil.isWerewolf(entity)) {
         if (!(entity instanceof EntityPlayer)) {
            return false;
         } else {
            EntityPlayer player = (EntityPlayer)entity;
            return CreatureUtil.isWitch(entity) || CreatureUtil.isWerewolf(entity) || CreatureUtil.isVampire(entity) || this.targetPlayerName != null && !this.targetPlayerName.isEmpty() && player.func_70005_c_().equals(this.targetPlayerName);
         }
      } else {
         return true;
      }
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.25D);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(30.0D);
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(13, new Byte((byte)0));
   }

   public boolean func_70650_aV() {
      return true;
   }

   protected String func_70639_aQ() {
      return null;
   }

   protected String func_70621_aR() {
      return "mob.villager.hit";
   }

   protected String func_70673_aS() {
      return "mob.villager.death";
   }

   protected void func_145780_a(int par1, int par2, int par3, Block par4) {
      this.func_85030_a("step.grass", 0.15F, 1.0F);
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

   public void func_70636_d() {
      this.func_82168_bl();
      float f = this.func_70013_c(1.0F);
      if (f > 0.5F) {
         this.field_70708_bq += 2;
      }

      if (!this.field_70170_p.field_72995_K && this.field_70173_aa % 20 == 2 && this.func_70644_a(Potion.field_76436_u)) {
         this.func_82170_o(Potion.field_76436_u.field_76415_H);
      }

      super.func_70636_d();
   }

   protected String func_145776_H() {
      return "game.hostile.swim";
   }

   protected String func_145777_O() {
      return "game.hostile.swim.splash";
   }

   public void func_70098_U() {
      super.func_70098_U();
      if (this.field_70154_o instanceof EntityCreature) {
         EntityCreature entitycreature = (EntityCreature)this.field_70154_o;
         this.field_70761_aq = entitycreature.field_70761_aq;
      }

   }

   public boolean func_70097_a(DamageSource damageSource, float damage) {
      if (damageSource.func_76346_g() == null || !(damageSource.func_76346_g() instanceof EntityVillageGuard) && !(damageSource.func_76346_g() instanceof EntityWitchHunter)) {
         if (this.func_85032_ar()) {
            return false;
         } else if (super.func_70097_a(damageSource, Math.min(damage, 9.0F))) {
            Entity entity = damageSource.func_76346_g();
            if (this.field_70153_n != entity && this.field_70154_o != entity) {
               if (entity != this) {
                  this.field_70789_a = entity;
               }

               return true;
            } else {
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected String func_146067_o(int distance) {
      return distance > 4 ? "game.hostile.hurt.fall.big" : "game.hostile.hurt.fall.small";
   }

   protected void func_70628_a(boolean par1, int par2) {
      int j = this.field_70146_Z.nextInt(3 + par2);

      for(int k = 0; k < j; ++k) {
         this.func_70099_a(Witchery.Items.GENERIC.itemBoltStake.createStack(), 0.0F);
      }

   }

   protected void func_70600_l(int par1) {
      this.func_70099_a(Witchery.Items.GENERIC.itemBoltAntiMagic.createStack(2), 0.0F);
   }

   public IEntityLivingData func_110161_a(IEntityLivingData par1EntityLivingData) {
      par1EntityLivingData = super.func_110161_a(par1EntityLivingData);
      this.setHunterType(this.field_70170_p.field_73012_v.nextInt(3));
      this.func_70062_b(0, new ItemStack(Witchery.Items.CROSSBOW_PISTOL));
      this.func_82162_bC();
      return par1EntityLivingData;
   }

   public void setCombatTask() {
      this.field_70714_bg.func_85156_a(this.aiAttackOnCollide);
      this.field_70714_bg.func_85156_a(this.aiArrowAttack);
      ItemStack itemstack = this.func_70694_bm();
      if (itemstack != null && itemstack.func_77973_b() == Witchery.Items.CROSSBOW_PISTOL) {
         this.field_70714_bg.func_75776_a(4, this.aiArrowAttack);
      } else {
         this.field_70714_bg.func_75776_a(4, this.aiAttackOnCollide);
      }

   }

   public void func_82196_d(EntityLivingBase par1EntityLivingBase, float par2) {
      EntityBolt entityarrow = new EntityBolt(this.field_70170_p, this, par1EntityLivingBase, 1.6F, (float)(14 - this.field_70170_p.field_73013_u.func_151525_a() * 4));
      int i = EnchantmentHelper.func_77506_a(Enchantment.field_77345_t.field_77352_x, this.func_70694_bm());
      int j = EnchantmentHelper.func_77506_a(Enchantment.field_77344_u.field_77352_x, this.func_70694_bm());
      entityarrow.setDamage((double)(par2 * 2.0F) + this.field_70146_Z.nextGaussian() * 0.25D + (double)((float)this.field_70170_p.field_73013_u.func_151525_a() * 0.11F));
      if (i > 0) {
         entityarrow.setDamage(entityarrow.getDamage() + (double)i * 0.5D + 0.5D);
      }

      if (j > 0) {
         entityarrow.setKnockbackStrength(j);
      }

      if (EnchantmentHelper.func_77506_a(Enchantment.field_77343_v.field_77352_x, this.func_70694_bm()) > 0 || CreatureUtil.isVampire(this.func_70638_az()) && this.field_70170_p.field_73012_v.nextInt(3) == 0) {
         entityarrow.func_70015_d(100);
      }

      if (this.func_70638_az() != null) {
         if (CreatureUtil.isWerewolf(this.func_70638_az())) {
            entityarrow.setBoltType(4);
         } else if (CreatureUtil.isUndead(this.func_70638_az())) {
            entityarrow.setBoltType(3);
         } else if (this.field_70170_p.field_73012_v.nextInt(4) == 0) {
            entityarrow.setBoltType(2);
         }
      }

      this.func_85030_a("random.bow", 1.0F, 1.0F / (this.func_70681_au().nextFloat() * 0.4F + 0.8F));
      this.field_70170_p.func_72838_d(entityarrow);
   }

   public int getHunterType() {
      return this.field_70180_af.func_75683_a(13);
   }

   public void setHunterType(int par1) {
      this.field_70180_af.func_75692_b(13, (byte)par1);
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      if (nbtRoot.func_74764_b("HunterType")) {
         byte b0 = nbtRoot.func_74771_c("HunterType");
         this.setHunterType(b0);
      }

      this.targetPlayerName = nbtRoot.func_74779_i("HunterTarget");
      this.setCombatTask();
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74774_a("HunterType", (byte)this.getHunterType());
      nbtRoot.func_74778_a("HunterTarget", this.targetPlayerName);
   }

   public void func_70062_b(int slot, ItemStack stack) {
      super.func_70062_b(slot, stack);
      if (!this.field_70170_p.field_72995_K && slot == 0) {
         this.setCombatTask();
      }

   }

   public double func_70033_W() {
      return super.func_70033_W() - 0.5D;
   }

   public static void blackMagicPerformed(EntityPlayer player) {
      if (player != null && player.field_70170_p != null && !player.field_70170_p.field_72995_K) {
         NBTTagCompound nbtPlayer = Infusion.getNBT(player);
         if (nbtPlayer != null && nbtPlayer.func_74763_f("WITCHunterTrigger") <= 0L && player.field_70170_p.field_73012_v.nextDouble() < 0.1D) {
            long totalWorldTicks = TimeUtil.getServerTimeInTicks();
            nbtPlayer.func_74772_a("WITCHunterTrigger", totalWorldTicks);
         }
      }

   }

   public static void handleWitchHunterEffects(EntityPlayer player, long totalWorldTicks) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      if (nbtPlayer != null) {
         long triggerTimeTicks = nbtPlayer.func_74763_f("WITCHunterTrigger");
         if (triggerTimeTicks > 0L && totalWorldTicks >= triggerTimeTicks + HUNTER_DELAY && player.field_70170_p.field_73012_v.nextDouble() < 0.01D || isVampireActive(player, totalWorldTicks)) {
            nbtPlayer.func_82580_o("WITCHunterTrigger");
            int MAX_SPAWNS = true;
            int tries = true;
            int spawned = 0;

            for(int i = 0; i < 3 && spawned < 2; ++i) {
               EntityWitchHunter creature = (EntityWitchHunter)Infusion.spawnCreature(player.field_70170_p, EntityWitchHunter.class, MathHelper.func_76128_c(player.field_70165_t), MathHelper.func_76128_c(player.field_70163_u), MathHelper.func_76128_c(player.field_70161_v), player, 3, 8, ParticleEffect.SMOKE, (SoundEffect)null);
               if (creature != null) {
                  ++spawned;
                  creature.targetPlayerName = player.func_70005_c_();
                  creature.func_110161_a((IEntityLivingData)null);
                  EntityUtil.setTarget(creature, player);
               }
            }

            if (spawned > 0) {
               Witchery.packetPipeline.sendTo((IMessage)(new PacketSound(SoundEffect.WITCHERY_RANDOM_THEYCOME, player, 1.0F, 1.0F)), (EntityPlayer)player);
            }
         }
      }

   }

   private static boolean isVampireActive(EntityPlayer player, long totalWorldTicks) {
      if (!(Config.instance().vampireHunterSpawnChance <= 0.0D) && !player.field_71075_bZ.field_75098_d) {
         ExtendedPlayer playerEx = ExtendedPlayer.get(player);
         if (playerEx.getVampireLevel() < 10) {
            return false;
         } else {
            if (player.field_70170_p.field_73012_v.nextDouble() < Config.instance().vampireHunterSpawnChance) {
               Village village = player.field_70170_p.field_72982_D.func_75550_a(MathHelper.func_76128_c(player.field_70165_t), MathHelper.func_76128_c(player.field_70163_u), MathHelper.func_76128_c(player.field_70161_v), 128);
               if (village != null && village.func_82684_a(player.func_70005_c_()) < -1) {
                  List hunters = player.field_70170_p.func_72872_a(EntityWitchHunter.class, player.field_70121_D.func_72314_b(64.0D, 16.0D, 64.0D));
                  return hunters == null || hunters.size() == 0;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   protected void func_70785_a(Entity p_70785_1_, float p_70785_2_) {
      if (this.field_70724_aR <= 0 && p_70785_2_ < 2.0F && p_70785_1_.field_70121_D.field_72337_e > this.field_70121_D.field_72338_b && p_70785_1_.field_70121_D.field_72338_b < this.field_70121_D.field_72337_e) {
         this.field_70724_aR = 20;
         this.func_70652_k(p_70785_1_);
      }

   }

   public float func_70783_a(int p_70783_1_, int p_70783_2_, int p_70783_3_) {
      return 0.5F - this.field_70170_p.func_72801_o(p_70783_1_, p_70783_2_, p_70783_3_);
   }

   protected boolean isValidLightLevel() {
      int i = MathHelper.func_76128_c(this.field_70165_t);
      int j = MathHelper.func_76128_c(this.field_70121_D.field_72338_b);
      int k = MathHelper.func_76128_c(this.field_70161_v);
      if (this.field_70170_p.func_72972_b(EnumSkyBlock.Sky, i, j, k) > this.field_70146_Z.nextInt(32)) {
         return false;
      } else {
         int l = this.field_70170_p.func_72957_l(i, j, k);
         if (this.field_70170_p.func_72911_I()) {
            int i1 = this.field_70170_p.field_73008_k;
            this.field_70170_p.field_73008_k = 10;
            l = this.field_70170_p.func_72957_l(i, j, k);
            this.field_70170_p.field_73008_k = i1;
         }

         return l <= this.field_70146_Z.nextInt(8);
      }
   }

   public boolean func_70601_bi() {
      return this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL && this.isValidLightLevel() && super.func_70601_bi();
   }

   protected boolean func_146066_aG() {
      return true;
   }
}
