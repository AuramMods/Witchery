package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.dimension.WorldProviderTorment;
import com.emoniph.witchery.entity.ai.EntityAIFlyerArrowAttack;
import com.emoniph.witchery.entity.ai.EntityAIFlyerLand;
import com.emoniph.witchery.entity.ai.EntityAIFlyerWander;
import com.emoniph.witchery.infusion.infusions.symbols.EffectRegistry;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.DemonicDamageSource;
import com.emoniph.witchery.util.IHandleDT;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityLordOfTorment extends EntityFlyingMob implements IRangedAttackMob, IBossDisplayData, IHandleDT {
   private int attackTimer;
   private final HashSet<String> attackers = new HashSet();

   public EntityLordOfTorment(World world) {
      super(world);
      this.func_70105_a(0.6F, 1.9F);
      this.field_70178_ae = true;
      this.field_70728_aV = 50;
      this.func_70661_as().func_75495_e(true);
      this.func_70661_as().func_75491_a(true);
      this.field_70728_aV = 80;
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, new EntityAIFlyerArrowAttack(this, 1.0D, 20, 60, 12.0F));
      this.field_70714_bg.func_75776_a(5, new EntityAIFlyerLand(this, 0.8D, false));
      this.field_70714_bg.func_75776_a(6, new EntityAIFlyerWander(this, 0.2D, 10.0D));
      this.field_70714_bg.func_75776_a(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(8, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAIHurtByTarget(this, false));
      this.field_70715_bh.func_75776_a(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(500.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.2D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(8.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111265_b).func_111128_a(50.0D);
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(16, (byte)0);
   }

   public void func_70071_h_() {
      super.func_70071_h_();
      if (this.field_70170_p.field_72995_K) {
         this.field_70170_p.func_72869_a(ParticleEffect.FLAME.toString(), this.field_70165_t - (double)this.field_70130_N * 0.5D + this.field_70170_p.field_73012_v.nextDouble() * (double)this.field_70130_N, 0.1D + this.field_70163_u + this.field_70170_p.field_73012_v.nextDouble() * 2.0D, this.field_70161_v - (double)this.field_70130_N * 0.5D + this.field_70170_p.field_73012_v.nextDouble() * (double)this.field_70130_N, 0.0D, 0.0D, 0.0D);
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean func_110182_bF() {
      return this.field_70180_af.func_75683_a(16) != 0;
   }

   public boolean func_70650_aV() {
      return true;
   }

   protected int func_70682_h(int air) {
      return air;
   }

   protected boolean func_70692_ba() {
      return false;
   }

   public void func_70636_d() {
      super.func_70636_d();
      if (this.attackTimer > 0) {
         --this.attackTimer;
      }

      if (this.field_70170_p.field_73012_v.nextDouble() < 0.05D && this.func_70638_az() != null && this.func_70638_az() instanceof EntityPlayer && ((EntityPlayer)this.func_70638_az()).field_71075_bZ.field_75100_b && !this.func_70638_az().func_70644_a(Potion.field_76421_d)) {
         this.func_70638_az().func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, 200, 5));
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_70103_a(byte state) {
      if (state == 4) {
         this.attackTimer = 10;
         this.func_85030_a("mob.irongolem.throw", 1.0F, 1.0F);
      } else {
         super.func_70103_a(state);
      }

   }

   public boolean func_70652_k(Entity target) {
      this.attackTimer = 10;
      this.field_70170_p.func_72960_a(this, (byte)4);
      boolean flag = target.func_70097_a(DamageSource.func_76358_a(this), (float)(7 + this.field_70146_Z.nextInt(15)));
      if (flag) {
         target.field_70181_x += 0.4000000059604645D;
      }

      this.func_85030_a("mob.irongolem.throw", 1.0F, 1.0F);
      return flag;
   }

   @SideOnly(Side.CLIENT)
   public int getAttackTimer() {
      return this.attackTimer;
   }

   public float func_70013_c(float par1) {
      return 1.0F;
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.lordoftorment.name");
   }

   public void func_70020_e(NBTTagCompound nbtRoot) {
      super.func_70020_e(nbtRoot);
      if (nbtRoot.func_74764_b("WITCAttackers")) {
         NBTTagList nbtAttackers = nbtRoot.func_150295_c("WITCAttackers", 8);

         for(int i = 0; i < nbtAttackers.func_74745_c(); ++i) {
            String attacker = nbtAttackers.func_150307_f(i);
            if (!this.attackers.contains(attacker)) {
               this.attackers.add(attacker);
            }
         }
      }

   }

   public void func_70109_d(NBTTagCompound nbtRoot) {
      super.func_70109_d(nbtRoot);
      NBTTagList nbtAttackers = new NBTTagList();
      int i = false;
      Iterator i$ = this.attackers.iterator();

      while(i$.hasNext()) {
         String attacker = (String)i$.next();
         nbtAttackers.func_74742_a(new NBTTagString(attacker));
      }

      nbtRoot.func_74782_a("WITCAttackers", nbtAttackers);
   }

   public boolean func_70097_a(DamageSource source, float damage) {
      if (source.func_94541_c()) {
         return false;
      } else {
         if (source.func_76364_f() != null && source.func_76364_f() instanceof EntityPlayer) {
            EntityPlayer attacker = (EntityPlayer)source.func_76364_f();
            if (!this.attackers.contains(attacker.func_70005_c_())) {
               this.attackers.add(attacker.func_70005_c_());
            }
         }

         float damageCap = source instanceof DemonicDamageSource ? 8.0F : 5.0F;
         boolean damaged = super.func_70097_a(source, Math.min(damage, damageCap));
         if (!this.field_70170_p.field_72995_K && this.field_71093_bK != Config.instance().dimensionTormentID && this.func_110143_aJ() <= this.func_110138_aP() * 0.5F) {
            int tormentlevel = WorldProviderTorment.getRandomTormentLevel(this.field_70170_p);
            double R = 16.0D;
            double Ry = 32.0D;
            AxisAlignedBB bb = AxisAlignedBB.func_72330_a(this.field_70165_t - 16.0D, this.field_70163_u - 32.0D, this.field_70161_v - 16.0D, this.field_70165_t + 16.0D, this.field_70163_u + 32.0D, this.field_70161_v + 16.0D);
            List players = this.field_70170_p.func_72872_a(EntityPlayer.class, bb);
            Iterator i$ = players.iterator();

            EntityPlayer otherPlayer;
            while(i$.hasNext()) {
               Object obj = i$.next();
               otherPlayer = (EntityPlayer)obj;
               WorldProviderTorment.setPlayerMustTorment((EntityPlayer)otherPlayer, 2, tormentlevel);
            }

            i$ = this.attackers.iterator();

            while(i$.hasNext()) {
               String playerName = (String)i$.next();
               otherPlayer = this.field_70170_p.func_72924_a(playerName);
               if (otherPlayer != null && otherPlayer.field_71093_bK == this.field_71093_bK) {
                  WorldProviderTorment.setPlayerMustTorment((EntityPlayer)otherPlayer, 2, tormentlevel);
               }
            }

            ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, this, 1.0D, 2.0D, 16);
            this.func_70106_y();
         }

         return damaged;
      }
   }

   public float getCapDT(DamageSource source, float damage) {
      return 5.0F;
   }

   protected String func_70639_aQ() {
      return "witchery:mob.torment.laugh";
   }

   protected String func_70621_aR() {
      return "witchery:mob.torment.hit";
   }

   protected String func_70673_aS() {
      return "witchery:mob.torment.death";
   }

   public int func_70627_aG() {
      return TimeUtil.secsToTicks(10);
   }

   protected Item func_146068_u() {
      return null;
   }

   protected void func_70628_a(boolean par1, int par2) {
      Enchantment enchantment = Enchantment.field_92090_c[this.field_70146_Z.nextInt(Enchantment.field_92090_c.length)];
      int k = MathHelper.func_76136_a(this.field_70146_Z, Math.min(enchantment.func_77319_d() + 3, enchantment.func_77325_b()), enchantment.func_77325_b());
      ItemStack itemstack = Items.field_151134_bR.func_92111_a(new EnchantmentData(enchantment, k));
      this.func_70099_a(itemstack, 0.0F);
      Enchantment enchantment2 = Enchantment.field_92090_c[this.field_70146_Z.nextInt(Enchantment.field_92090_c.length)];
      int k2 = MathHelper.func_76136_a(this.field_70146_Z, Math.min(enchantment2.func_77319_d() + 1, enchantment.func_77325_b()), enchantment.func_77325_b());
      ItemStack itemstack2 = Items.field_151134_bR.func_92111_a(new EnchantmentData(enchantment, k2));
      this.func_70099_a(itemstack, 0.0F);
      this.func_70099_a(Witchery.Items.GENERIC.itemDemonHeart.createStack(), 0.0F);
      this.func_70099_a(Witchery.Items.GENERIC.itemBrewSoulTorment.createStack(), 0.0F);
   }

   protected float func_70599_aP() {
      return 2.0F;
   }

   public boolean func_70601_bi() {
      return true;
   }

   public int func_70641_bl() {
      return 1;
   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      super.func_70014_b(par1NBTTagCompound);
   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      super.func_70037_a(par1NBTTagCompound);
   }

   public void func_82196_d(EntityLivingBase targetEntity, float par2) {
      this.attackTimer = 10;
      this.field_70170_p.func_72960_a(this, (byte)4);
      double d0 = targetEntity.field_70165_t - this.field_70165_t;
      double d1 = targetEntity.field_70121_D.field_72338_b + (double)(targetEntity.field_70131_O / 2.0F) - (this.field_70163_u + (double)(this.field_70131_O / 2.0F));
      double d2 = targetEntity.field_70161_v - this.field_70161_v;
      float f1 = MathHelper.func_76129_c(par2) * 0.5F;
      if (!this.field_70170_p.field_72995_K) {
         this.field_70170_p.func_72889_a((EntityPlayer)null, 1009, (int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v, 0);
         int count = this.field_70146_Z.nextInt(10) == 0 ? 9 : 3;
         EntitySpellEffect effect = new EntitySpellEffect(this.field_70170_p, this, d0 + this.field_70146_Z.nextGaussian() * (double)f1, d1, d2 + this.field_70146_Z.nextGaussian() * (double)f1, EffectRegistry.instance().getEffect(39), 1);
         double d8 = 1.0D;
         effect.field_70165_t = this.field_70165_t;
         effect.field_70163_u = this.field_70163_u + (double)(this.field_70131_O / 2.0F);
         effect.field_70161_v = this.field_70161_v;
         this.field_70170_p.func_72838_d(effect);
         effect.setShooter(this);

         for(int i = 0; i < count; ++i) {
            EntitySoulfire fireballEntity = new EntitySoulfire(this.field_70170_p, this, d0 + this.field_70146_Z.nextGaussian() * (double)f1, d1, d2 + this.field_70146_Z.nextGaussian() * (double)f1);
            d8 = 1.0D;
            fireballEntity.field_70165_t = this.field_70165_t;
            fireballEntity.field_70163_u = this.field_70163_u + (double)(this.field_70131_O / 2.0F) + 0.5D;
            fireballEntity.field_70161_v = this.field_70161_v;
            this.field_70170_p.func_72838_d(fireballEntity);
         }
      }

   }
}
