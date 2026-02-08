package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.IHandleDT;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityBabaYaga extends EntityMob implements IBossDisplayData, IRangedAttackMob, IEntitySelector, IOwnable, IHandleDT {
   private static final UUID field_110184_bp = UUID.fromString("ab0df555-0786-4951-a8df-ca61749f164e");
   private static final AttributeModifier field_110185_bq;
   private static final int[] witchDrops;
   private int witchAttackTimer;
   private static final double MAX_HEALTH = 500.0D;
   long ticksSinceTeleport = 0L;

   public EntityBabaYaga(World par1World) {
      super(par1World);
      this.func_70661_as().func_75491_a(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, new EntityAIArrowAttack(this, 1.0D, 60, 10.0F));
      this.field_70714_bg.func_75776_a(2, new EntityAIWander(this, 1.0D));
      this.field_70714_bg.func_75776_a(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(3, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAIHurtByTarget(this, false));
      this.field_70715_bh.func_75776_a(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false, true, this));
      this.field_70728_aV = 70;
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.func_70096_w().func_75682_a(21, (byte)0);
      this.field_70180_af.func_75682_a(17, "");
   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      super.func_70014_b(par1NBTTagCompound);
      if (this.getOwnerName() == null) {
         par1NBTTagCompound.func_74778_a("Owner", "");
      } else {
         par1NBTTagCompound.func_74778_a("Owner", this.getOwnerName());
      }

   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      super.func_70037_a(par1NBTTagCompound);
      String s = par1NBTTagCompound.func_74779_i("Owner");
      if (s.length() > 0) {
         this.setOwner(s);
      }

   }

   public String getOwnerName() {
      return this.field_70180_af.func_75681_e(17);
   }

   public void setOwner(String par1Str) {
      this.field_70180_af.func_75692_b(17, par1Str);
   }

   public EntityPlayer getOwnerEntity() {
      return this.field_70170_p.func_72924_a(this.getOwnerName());
   }

   public boolean func_82704_a(Entity entity) {
      if (entity != null && entity instanceof EntityPlayer) {
         String ownerName = this.getOwnerName();
         boolean isOwned = ((EntityPlayer)entity).func_70005_c_().equalsIgnoreCase(ownerName);
         return !isOwned;
      } else {
         return true;
      }
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.babayaga.name");
   }

   public int func_70658_aO() {
      return 4;
   }

   protected String func_70639_aQ() {
      return "witchery:mob.baba.baba_living";
   }

   protected String func_70621_aR() {
      return "mob.witch.hurt";
   }

   protected String func_70673_aS() {
      return "witchery:mob.baba.baba_death";
   }

   public void setAggressive(boolean par1) {
      this.func_70096_w().func_75692_b(21, (byte)(par1 ? 1 : 0));
   }

   public boolean getAggressive() {
      return this.func_70096_w().func_75683_a(21) == 1;
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(500.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.3D);
   }

   protected int func_70682_h(int par1) {
      return par1;
   }

   protected void func_70069_a(float par1) {
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
            } else if (this.field_70146_Z.nextFloat() < 0.01F && this.func_110143_aJ() < this.func_110138_aP()) {
               short1 = 16341;
            } else if (this.field_70146_Z.nextFloat() < 0.25F && this.func_70638_az() != null && !this.func_70644_a(Potion.field_76424_c) && this.func_70638_az().func_70068_e(this) > 121.0D) {
               short1 = 16274;
            } else if (this.field_70146_Z.nextFloat() < 0.25F && this.func_70638_az() != null && !this.func_70644_a(Potion.field_76424_c) && this.func_70638_az().func_70068_e(this) > 121.0D) {
               short1 = 16274;
            }

            if (short1 > -1) {
               this.func_70062_b(0, new ItemStack(Items.field_151068_bn, 1, short1));
               this.witchAttackTimer = this.func_70694_bm().func_77988_m() - 20;
               this.setAggressive(true);
               IAttributeInstance attributeinstance = this.func_110148_a(SharedMonsterAttributes.field_111263_d);
               attributeinstance.func_111124_b(field_110185_bq);
               attributeinstance.func_111121_a(field_110185_bq);
            }
         }

         if (this.field_70146_Z.nextFloat() < 7.5E-4F) {
            this.field_70170_p.func_72960_a(this, (byte)15);
         }

         if ((this.func_70661_as().func_75500_f() || this.field_70170_p.field_73012_v.nextDouble() < 0.02D) && this.func_70638_az() != null && (long)this.field_70173_aa - this.ticksSinceTeleport > 100L) {
            this.ticksSinceTeleport = (long)this.field_70173_aa;
            this.teleportToEntity(this.func_70638_az());
         }

         if (this.field_70170_p.field_73012_v.nextDouble() < 0.05D && this.func_70638_az() != null && (this.func_70638_az().field_70160_al || this.func_70638_az() instanceof EntityPlayer && ((EntityPlayer)this.func_70638_az()).field_71075_bZ.field_75100_b) && !this.func_70638_az().func_70644_a(Potion.field_76421_d)) {
            this.func_70638_az().func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, 200, 5));
         }

         EntityPlayer owner = this.getOwnerEntity();
         if (owner != null) {
            double distance = this.func_70068_e(owner);
            if (distance < 64.0D && this.field_70173_aa % 100 == 0) {
               int l = this.field_70146_Z.nextInt(3);
               int i1 = witchDrops[this.field_70146_Z.nextInt(witchDrops.length - 3)];

               for(int j1 = 0; j1 < l; ++j1) {
                  this.func_70099_a(new ItemStack(Witchery.Items.GENERIC, 1, i1), 0.0F);
               }
            }

            if (this.field_70173_aa > 600) {
               this.func_70106_y();
               ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, this, 1.0D, 2.0D, 16);
            }
         }
      }

      super.func_70636_d();
   }

   protected boolean teleportToEntity(Entity par1Entity) {
      Vec3 vec3 = Vec3.func_72443_a(this.field_70165_t - par1Entity.field_70165_t, this.field_70121_D.field_72338_b + (double)(this.field_70131_O / 2.0F) - par1Entity.field_70163_u + (double)par1Entity.func_70047_e(), this.field_70161_v - par1Entity.field_70161_v);
      vec3 = vec3.func_72432_b();
      double d0 = 8.0D;
      double d1 = this.field_70165_t + (this.field_70146_Z.nextDouble() - 0.5D) * 8.0D - vec3.field_72450_a * d0;
      double d2 = this.field_70163_u + (double)(this.field_70146_Z.nextInt(16) - 8) - vec3.field_72448_b * d0;
      double d3 = this.field_70161_v + (this.field_70146_Z.nextDouble() - 0.5D) * 8.0D - vec3.field_72449_c * d0;
      return this.teleportTo(d1, d2, d3);
   }

   protected boolean teleportTo(double par1, double par3, double par5) {
      double d3 = this.field_70165_t;
      double d4 = this.field_70163_u;
      double d5 = this.field_70161_v;
      this.field_70165_t = par1;
      this.field_70163_u = par3;
      this.field_70161_v = par5;
      boolean flag = false;
      int i = MathHelper.func_76128_c(this.field_70165_t);
      int j = MathHelper.func_76128_c(this.field_70163_u);
      int k = MathHelper.func_76128_c(this.field_70161_v);
      if (this.field_70170_p.func_72899_e(i, j, k)) {
         boolean flag1 = false;

         while(!flag1 && j > 0) {
            Block block = this.field_70170_p.func_147439_a(i, j - 1, k);
            if (block.func_149688_o().func_76230_c()) {
               flag1 = true;
            } else {
               --this.field_70163_u;
               --j;
            }
         }

         if (flag1) {
            this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
            if (this.field_70170_p.func_72945_a(this, this.field_70121_D).isEmpty() && !this.field_70170_p.func_72953_d(this.field_70121_D)) {
               flag = true;
            }
         }
      }

      if (!flag) {
         this.func_70107_b(d3, d4, d5);
         return false;
      } else {
         short short1 = 128;

         for(int l = 0; l < short1; ++l) {
            double d6 = (double)l / ((double)short1 - 1.0D);
            float f = (this.field_70146_Z.nextFloat() - 0.5F) * 0.2F;
            float f1 = (this.field_70146_Z.nextFloat() - 0.5F) * 0.2F;
            float f2 = (this.field_70146_Z.nextFloat() - 0.5F) * 0.2F;
            double d7 = d3 + (this.field_70165_t - d3) * d6 + (this.field_70146_Z.nextDouble() - 0.5D) * (double)this.field_70130_N * 2.0D;
            double d8 = d4 + (this.field_70163_u - d4) * d6 + this.field_70146_Z.nextDouble() * (double)this.field_70131_O;
            double d9 = d5 + (this.field_70161_v - d5) * d6 + (this.field_70146_Z.nextDouble() - 0.5D) * (double)this.field_70130_N * 2.0D;
            this.field_70170_p.func_72869_a("portal", d7, d8, d9, (double)f, (double)f1, (double)f2);
         }

         this.field_70170_p.func_72908_a(d3, d4, d5, "mob.endermen.portal", 1.0F, 1.0F);
         this.func_85030_a("mob.endermen.portal", 1.0F, 1.0F);
         return true;
      }
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

   public boolean func_70097_a(DamageSource source, float damage) {
      boolean result = super.func_70097_a(source, Math.min(damage, 15.0F));
      if (!this.field_70170_p.field_72995_K && source.func_76346_g() != null && source.func_76346_g() instanceof EntityLiving) {
         EntityLiving attacker = (EntityLiving)source.func_76346_g();
         if (attacker.func_70668_bt() == EnumCreatureAttribute.UNDEAD || attacker instanceof EntityTameable && !((EntityTameable)attacker).func_70909_n()) {
            EntityCaveSpider spider = new EntityCaveSpider(this.field_70170_p);
            spider.func_70012_b(attacker.field_70165_t, attacker.field_70163_u, attacker.field_70161_v, attacker.field_70125_A, attacker.field_70177_z);
            EntityLivingBase target = this.func_70638_az();
            spider.func_70624_b(target);
            spider.func_70604_c(target);
            spider.func_70784_b(target);
            this.field_70170_p.func_72838_d(spider);
            ParticleEffect.MOB_SPELL.send(SoundEffect.RANDOM_POP, spider, 2.0D, 2.0D, 16);
            attacker.func_70106_y();
         }
      }

      if (!this.field_70170_p.field_72995_K && source.func_76346_g() != null && source.func_76346_g() instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)source.func_76346_g();
         if (!CreatureUtil.isWoodenDamage(source)) {
            player.func_70097_a(DamageSource.func_76354_b(this, player), damage * 0.25F);
         }
      }

      return result;
   }

   public float getCapDT(DamageSource source, float damage) {
      return 15.0F;
   }

   public void func_70645_a(DamageSource par1DamageSource) {
      super.func_70645_a(par1DamageSource);
      if (!this.field_70170_p.field_72995_K) {
         ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, this, 1.0D, 2.0D, 16);
      }

      this.func_70106_y();
   }

   protected void func_70628_a(boolean par1, int par2) {
      int j = this.field_70146_Z.nextInt(3) + 2;

      int l;
      for(int k = 0; k < j; ++k) {
         l = this.field_70146_Z.nextInt(3) + 1;
         int i1 = witchDrops[this.field_70146_Z.nextInt(witchDrops.length)];
         if (par2 > 0) {
            l += this.field_70146_Z.nextInt(par2 + 1);
         }

         for(int j1 = 0; j1 < l; ++j1) {
            this.func_70099_a(new ItemStack(Witchery.Items.GENERIC, 1, i1), 0.0F);
         }
      }

      Enchantment enchantment = Enchantment.field_92090_c[this.field_70146_Z.nextInt(Enchantment.field_92090_c.length)];
      l = MathHelper.func_76136_a(this.field_70146_Z, Math.min(enchantment.func_77319_d() + 2, enchantment.func_77325_b()), enchantment.func_77325_b());
      ItemStack itemstack = Items.field_151134_bR.func_92111_a(new EnchantmentData(enchantment, l));
      this.func_70099_a(itemstack, 0.0F);
      this.func_70099_a(new ItemStack(Witchery.Items.BABAS_HAT), 0.0F);
   }

   public void func_82196_d(EntityLivingBase par1EntityLivingBase, float par2) {
      if (!this.getAggressive()) {
         if (this.field_70170_p.field_73012_v.nextInt(3) == 0) {
            ItemGeneral.SubItem brew = null;
            switch(this.field_70170_p.field_73012_v.nextInt(12)) {
            case 0:
            case 1:
               brew = Witchery.Items.GENERIC.itemBrewOfWebs;
               break;
            case 2:
            case 3:
               brew = Witchery.Items.GENERIC.itemBrewOfThorns;
               break;
            case 4:
            case 5:
               brew = Witchery.Items.GENERIC.itemBrewOfFrogsTongue;
               break;
            case 6:
            case 7:
               brew = Witchery.Items.GENERIC.itemBrewOfInk;
               break;
            case 8:
            case 9:
               brew = Witchery.Items.GENERIC.itemBrewOfHitchcock;
               break;
            case 10:
               brew = Witchery.Items.GENERIC.itemBrewOfBats;
               break;
            case 11:
               brew = Witchery.Items.GENERIC.itemBrewOfWasting;
               break;
            default:
               return;
            }

            EntityWitchProjectile entitypotion = new EntityWitchProjectile(this.field_70170_p, this, brew);
            entitypotion.field_70125_A -= -20.0F;
            double d0 = par1EntityLivingBase.field_70165_t + par1EntityLivingBase.field_70159_w - this.field_70165_t;
            double d1 = par1EntityLivingBase.field_70163_u + (double)par1EntityLivingBase.func_70047_e() - 1.100000023841858D - this.field_70163_u;
            double d2 = par1EntityLivingBase.field_70161_v + par1EntityLivingBase.field_70179_y - this.field_70161_v;
            float f1 = MathHelper.func_76133_a(d0 * d0 + d2 * d2);
            entitypotion.func_70186_c(d0, d1 + (double)(f1 * 0.2F), d2, 0.75F, 8.0F);
            this.field_70170_p.func_72838_d(entitypotion);
         } else {
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

   }

   static {
      field_110185_bq = (new AttributeModifier(field_110184_bp, "Drinking speed penalty", -0.25D, 0)).func_111168_a(false);
      witchDrops = new int[]{Witchery.Items.GENERIC.itemSpectralDust.damageValue, Witchery.Items.GENERIC.itemBatWool.damageValue, Witchery.Items.GENERIC.itemToeOfFrog.damageValue, Witchery.Items.GENERIC.itemOwletsWing.damageValue, Witchery.Items.GENERIC.itemDogTongue.damageValue, Witchery.Items.GENERIC.itemBrewOfVines.damageValue, Witchery.Items.GENERIC.itemBrewOfSprouting.damageValue, Witchery.Items.GENERIC.itemBrewOfHitchcock.damageValue, Witchery.Items.GENERIC.itemBrewOfCursedLeaping.damageValue, Witchery.Items.GENERIC.itemBrewOfFrogsTongue.damageValue};
   }
}
