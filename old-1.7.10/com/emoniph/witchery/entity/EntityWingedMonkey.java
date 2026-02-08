package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.ai.EntityAIFlyerAttackOnCollide;
import com.emoniph.witchery.entity.ai.EntityAIFlyerFlyToWaypoint;
import com.emoniph.witchery.entity.ai.EntityAIFlyerFollowOwner;
import com.emoniph.witchery.entity.ai.EntityAIFlyerLand;
import com.emoniph.witchery.entity.ai.EntityAIFlyerMate;
import com.emoniph.witchery.entity.ai.EntityAIFlyerWander;
import com.emoniph.witchery.entity.ai.EntityAIFlyingTempt;
import com.emoniph.witchery.entity.ai.EntityAISitAndStay;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TameableUtil;
import com.emoniph.witchery.util.Waypoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityWingedMonkey extends EntityFlyingTameable {
   private int attackTimer;
   public EntityAIFlyingTempt aiTempt;
   private int timeToLive = -1;
   private static final ItemStack[] TEMPTATIONS;

   public EntityWingedMonkey(World world) {
      super(world);
      this.func_70105_a(0.6F, 0.8F);
      this.func_70661_as().func_75495_e(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISitAndStay(this));
      this.field_70714_bg.func_75776_a(2, new EntityAIFlyerFlyToWaypoint(this, EntityAIFlyerFlyToWaypoint.CarryRequirement.ENTITY_LIVING));
      this.field_70714_bg.func_75776_a(3, new EntityAIFlyerAttackOnCollide(this, 1.0D, true));
      this.field_70714_bg.func_75776_a(4, this.aiTempt = new EntityAIFlyingTempt(this, 0.6D, TEMPTATIONS, true));
      this.field_70714_bg.func_75776_a(5, new EntityAIFlyerFollowOwner(this, 1.0D, 14.0F, 5.0F));
      this.field_70714_bg.func_75776_a(8, new EntityAIFlyerMate(this, 0.8D));
      this.field_70714_bg.func_75776_a(9, new EntityAIFlyerLand(this, 0.8D, true));
      this.field_70714_bg.func_75776_a(10, new EntityAIFlyerWander(this, 0.8D, 10.0D));
      this.field_70714_bg.func_75776_a(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F, 0.2F));
      this.field_70715_bh.func_75776_a(1, new EntityAIOwnerHurtByTarget(this));
      this.field_70715_bh.func_75776_a(2, new EntityAIOwnerHurtTarget(this));
      this.field_70715_bh.func_75776_a(3, new EntityAIHurtByTarget(this, true));
   }

   public int func_70658_aO() {
      return super.func_70658_aO() + 5;
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74774_a("FeatherColor", (byte)this.getFeatherColor());
      nbtRoot.func_74768_a("SuicideIn", this.timeToLive);
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      if (nbtRoot.func_74764_b("FeatherColor")) {
         this.setFeatherColor(nbtRoot.func_74771_c("FeatherColor"));
      }

      if (nbtRoot.func_74764_b("SuicideIn")) {
         this.timeToLive = nbtRoot.func_74762_e("SuicideIn");
      } else {
         this.timeToLive = -1;
      }

   }

   public void func_70071_h_() {
      super.func_70071_h_();
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(18, (byte)0);
      this.field_70180_af.func_75682_a(21, (byte)(this.field_70170_p.field_73012_v.nextInt(100) == 0 ? 0 : this.field_70170_p.field_73012_v.nextInt(15) + 1));
   }

   protected int func_70682_h(int par1) {
      return par1;
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(20.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.30000001192092896D);
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(4.0D);
   }

   protected boolean func_70692_ba() {
      return false;
   }

   public boolean func_70650_aV() {
      return true;
   }

   public void func_70629_bd() {
      this.func_70661_as().func_75499_g();
      super.func_70629_bd();
      if (this.field_70170_p != null && !this.field_70128_L && !this.field_70170_p.field_72995_K && this.timeToLive != -1 && (--this.timeToLive == 0 || this.func_70638_az() == null || this.func_70638_az().field_70128_L)) {
         ParticleEffect.EXPLODE.send(SoundEffect.NONE, this, 1.0D, 1.0D, 16);
         this.func_70106_y();
      }

   }

   public void func_70636_d() {
      super.func_70636_d();
      if (this.attackTimer > 0) {
         --this.attackTimer;
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_70103_a(byte par1) {
      if (par1 == 4) {
         this.attackTimer = 10;
      } else {
         super.func_70103_a(par1);
      }

   }

   @SideOnly(Side.CLIENT)
   public int getAttackTimer() {
      return this.attackTimer;
   }

   protected void func_70628_a(boolean par1, int par2) {
      if (!this.isTemp()) {
         int var3 = this.field_70146_Z.nextInt(3) + this.field_70146_Z.nextInt(1 + par2);

         for(int var4 = 0; var4 < var3; ++var4) {
            this.func_70099_a(new ItemStack(Items.field_151008_G), 0.0F);
         }
      }

   }

   public int func_70627_aG() {
      return super.func_70627_aG() * 2;
   }

   protected String func_70639_aQ() {
      return "witchery:mob.monkey.say";
   }

   protected String func_70621_aR() {
      return "witchery:mob.monkey.hit";
   }

   protected String func_70673_aS() {
      return "witchery:mob.monkey.death";
   }

   public boolean func_70652_k(Entity par1Entity) {
      this.attackTimer = 10;
      this.field_70170_p.func_72960_a(this, (byte)4);
      return par1Entity.func_70097_a(DamageSource.func_76358_a(this), 2.0F);
   }

   public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
      if (this.func_85032_ar()) {
         return false;
      } else {
         this.func_70904_g(false);
         return super.func_70097_a(par1DamageSource, par2);
      }
   }

   public double func_70042_X() {
      return this.field_70153_n != null ? (double)(-this.field_70153_n.field_70131_O) * 0.6D : 0.0D;
   }

   public boolean shouldRiderSit() {
      return false;
   }

   public boolean func_70085_c(EntityPlayer player) {
      if (this.isTemp()) {
         return true;
      } else {
         ItemStack itemstack = player.field_71071_by.func_70448_g();
         if (this.func_70909_n()) {
            if (TameableUtil.isOwner(this, player) && !this.func_70877_b(itemstack)) {
               if (itemstack != null && itemstack.func_77973_b() == Items.field_151100_aR) {
                  if (!this.field_70170_p.field_72995_K) {
                     int i = BlockColored.func_150032_b(itemstack.func_77960_j());
                     this.setFeatherColor(i);
                     if (!player.field_71075_bZ.field_75098_d) {
                        --itemstack.field_77994_a;
                     }

                     if (itemstack.field_77994_a <= 0) {
                        player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                     }
                  }
               } else {
                  if (itemstack != null && (itemstack.func_77973_b() == Items.field_151057_cb || itemstack.func_77973_b() == Witchery.Items.POLYNESIA_CHARM || itemstack.func_77973_b() == Witchery.Items.DEVILS_TONGUE_CHARM)) {
                     return false;
                  }

                  if (!Witchery.Items.GENERIC.itemWaystonePlayerBound.isMatch(itemstack) && !Witchery.Items.GENERIC.itemWaystoneBound.isMatch(itemstack)) {
                     if (itemstack != null && Witchery.Items.TAGLOCK_KIT.isTaglockPresent(itemstack, 1)) {
                        this.waypoint = itemstack.func_77946_l();
                        this.homeX = this.field_70165_t;
                        this.homeY = this.field_70163_u;
                        this.homeZ = this.field_70161_v;
                        ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_ORB, this, 1.0D, 1.0D, 16);
                        if (!player.field_71075_bZ.field_75098_d) {
                           --itemstack.field_77994_a;
                        }
                     } else if (this.func_70068_e(player) < 9.0D && !this.field_70170_p.field_72995_K) {
                        this.func_70904_g(!this.func_70906_o());
                     }
                  } else {
                     this.waypoint = itemstack.func_77946_l();
                     this.homeX = 0.0D;
                     this.homeY = 0.0D;
                     this.homeZ = 0.0D;
                     Waypoint wp = this.getWaypoint();
                     this.homeX = wp.X;
                     this.homeY = wp.Y;
                     this.homeZ = wp.Z;
                     Witchery.Items.GENERIC.bindToLocation(this.field_70170_p, (int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v, this.field_70170_p.field_73011_w.field_76574_g, this.field_70170_p.field_73011_w.func_80007_l(), this.waypoint);
                     if (!this.field_70170_p.field_72995_K && this.func_70906_o()) {
                        this.func_70904_g(false);
                     }

                     player.func_70078_a(this);
                     ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_ORB, this, 1.0D, 1.0D, 16);
                  }
               }

               return true;
            } else if (TameableUtil.isOwner(this, player) && this.func_70877_b(itemstack) && this.func_110143_aJ() < this.func_110138_aP()) {
               if (!this.field_70170_p.field_72995_K) {
                  this.func_70691_i(10.0F);
                  if (!player.field_71075_bZ.field_75098_d) {
                     --itemstack.field_77994_a;
                  }

                  if (itemstack.field_77994_a <= 0) {
                     player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                  }
               }

               return true;
            } else {
               return super.func_70085_c(player);
            }
         } else if (itemstack != null && (itemstack.func_77973_b() == Items.field_151147_al || itemstack.func_77973_b() == Items.field_151082_bd) && player.func_70068_e(this) < 9.0D) {
            if (!player.field_71075_bZ.field_75098_d) {
               --itemstack.field_77994_a;
            }

            if (itemstack.field_77994_a <= 0) {
               player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
            }

            if (!this.field_70170_p.field_72995_K) {
               if (this.field_70146_Z.nextInt(3) == 0) {
                  this.func_70903_f(true);
                  this.setTameSkin(1 + this.field_70170_p.field_73012_v.nextInt(3));
                  TameableUtil.setOwner(this, player);
                  this.func_110163_bv();
                  this.func_70908_e(true);
                  this.func_70904_g(true);
                  this.field_70170_p.func_72960_a(this, (byte)7);
               } else {
                  this.func_70908_e(false);
                  this.field_70170_p.func_72960_a(this, (byte)6);
               }
            }

            return true;
         } else {
            return !this.func_70877_b(itemstack) ? super.func_70085_c(player) : false;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_70620_b(ItemStack stack, int pass) {
      return stack.func_77973_b().func_77623_v() ? stack.func_77973_b().getIcon(stack, pass) : stack.func_77954_c();
   }

   public EntityWingedMonkey spawnBabyAnimal(EntityAgeable par1EntityAgeable) {
      EntityWingedMonkey entityocelot = new EntityWingedMonkey(this.field_70170_p);
      if (this.func_70909_n()) {
         entityocelot.func_110163_bv();
         entityocelot.setTameSkin(this.getTameSkin());
         entityocelot.setFeatherColor(this.getFeatherColor());
      }

      return entityocelot;
   }

   public boolean func_70877_b(ItemStack itemstack) {
      return itemstack != null && (itemstack.func_77973_b() == Items.field_151147_al || itemstack.func_77973_b() == Items.field_151082_bd);
   }

   public boolean func_70878_b(EntityAnimal par1EntityAnimal) {
      if (par1EntityAnimal == this) {
         return false;
      } else if (!this.func_70909_n()) {
         return false;
      } else if (!(par1EntityAnimal instanceof EntityWingedMonkey)) {
         return false;
      } else {
         EntityWingedMonkey entityocelot = (EntityWingedMonkey)par1EntityAnimal;
         return !entityocelot.func_70909_n() ? false : this.func_70880_s() && entityocelot.func_70880_s();
      }
   }

   public int getFeatherColor() {
      return this.field_70180_af.func_75683_a(21) & 15;
   }

   public void setFeatherColor(int par1) {
      byte b0 = this.field_70180_af.func_75683_a(16);
      this.field_70180_af.func_75692_b(21, (byte)(b0 & 240 | par1 & 15));
   }

   public int getTameSkin() {
      return this.field_70180_af.func_75683_a(18);
   }

   public void setTameSkin(int par1) {
      this.field_70180_af.func_75692_b(18, (byte)par1);
   }

   public boolean func_70601_bi() {
      if (this.field_70170_p.field_73012_v.nextInt(3) == 0) {
         return false;
      } else {
         if (this.field_70170_p.func_72855_b(this.field_70121_D) && this.field_70170_p.func_72945_a(this, this.field_70121_D).isEmpty() && !this.field_70170_p.func_72953_d(this.field_70121_D)) {
            int i = MathHelper.func_76128_c(this.field_70165_t);
            int j = MathHelper.func_76128_c(this.field_70121_D.field_72338_b);
            int k = MathHelper.func_76128_c(this.field_70161_v);
            if (j < 63) {
               return false;
            }

            Block block = this.field_70170_p.func_147439_a(i, j - 1, k);
            if (block == Blocks.field_150349_c || block != null && block.isLeaves(this.field_70170_p, i, j - 1, k)) {
               return true;
            }
         }

         return false;
      }
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.wingedmonkey.name");
   }

   public EntityAgeable func_90011_a(EntityAgeable par1EntityAgeable) {
      return this.spawnBabyAnimal(par1EntityAgeable);
   }

   public void setTimeToLive(int i) {
      this.timeToLive = i;
   }

   public boolean isTemp() {
      return this.timeToLive != -1;
   }

   static {
      TEMPTATIONS = new ItemStack[]{new ItemStack(Items.field_151147_al), new ItemStack(Items.field_151082_bd)};
   }
}
