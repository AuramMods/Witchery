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
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.familiar.IFamiliar;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TameableUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
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

public class EntityOwl extends EntityFlyingTameable implements IFamiliar {
   public EntityAIFlyingTempt aiTempt;
   private int timeToLive = -1;
   private static final ItemStack[] TEMPTATIONS;

   public EntityOwl(World world) {
      super(world);
      this.func_70105_a(0.6F, 0.8F);
      this.func_70661_as().func_75495_e(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISitAndStay(this));
      this.field_70714_bg.func_75776_a(2, new EntityAIFlyerFlyToWaypoint(this, EntityAIFlyerFlyToWaypoint.CarryRequirement.HELD_ITEM));
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
      return super.func_70658_aO() + (this.isFamiliar() ? 5 : 1);
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74774_a("FeatherColor", (byte)this.getFeatherColor());
      nbtRoot.func_74774_a("Familiar", (byte)(this.isFamiliar() ? 1 : 0));
      nbtRoot.func_74768_a("SuicideIn", this.timeToLive);
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      if (nbtRoot.func_74764_b("FeatherColor")) {
         this.setFeatherColor(nbtRoot.func_74771_c("FeatherColor"));
      }

      if (nbtRoot.func_74764_b("Familiar")) {
         this.setFamiliar(nbtRoot.func_74771_c("Familiar") > 0);
      }

      if (nbtRoot.func_74764_b("SuicideIn")) {
         this.timeToLive = nbtRoot.func_74762_e("SuicideIn");
      } else {
         this.timeToLive = -1;
      }

   }

   public void func_70071_h_() {
      this.field_70178_ae = this.isFamiliar();
      super.func_70071_h_();
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(18, (byte)0);
      this.field_70180_af.func_75682_a(21, (byte)(this.field_70170_p.field_73012_v.nextInt(100) == 0 ? 0 : this.field_70170_p.field_73012_v.nextInt(15) + 1));
      this.field_70180_af.func_75682_a(26, (byte)0);
   }

   public boolean isFamiliar() {
      return this.field_70180_af.func_75683_a(26) > 0;
   }

   public void setFamiliar(boolean familiar) {
      this.field_70180_af.func_75692_b(26, (byte)(familiar ? 1 : 0));
   }

   protected int func_70682_h(int par1) {
      return par1;
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(10.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.30000001192092896D);
      this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(4.0D);
   }

   public void setMaxHealth(float maxHealth) {
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)maxHealth);
      this.func_70606_j(maxHealth);
      this.setFamiliar(true);
   }

   protected boolean func_70692_ba() {
      return false;
   }

   public EntityLivingBase func_70902_q() {
      return this.isFamiliar() && !this.field_70170_p.field_72995_K ? TameableUtil.getOwnerAccrossDimensions(this) : super.func_70902_q();
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
      return "witchery:mob.owl.owl_hoot";
   }

   protected String func_70621_aR() {
      return "witchery:mob.owl.owl_hurt";
   }

   protected String func_70673_aS() {
      return "witchery:mob.owl.owl_hurt";
   }

   public boolean func_70652_k(Entity par1Entity) {
      return par1Entity.func_70097_a(DamageSource.func_76358_a(this), 2.0F);
   }

   public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
      if (this.func_85032_ar()) {
         return false;
      } else {
         if (!this.isFamiliar()) {
            this.func_70904_g(false);
         }

         return super.func_70097_a(par1DamageSource, par2);
      }
   }

   public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
      if (this.isTemp()) {
         return true;
      } else {
         ItemStack itemstack = par1EntityPlayer.field_71071_by.func_70448_g();
         if (this.func_70909_n()) {
            if (TameableUtil.isOwner(this, par1EntityPlayer) && this.isFamiliar() && par1EntityPlayer.func_70093_af() && this.func_70906_o()) {
               if (!this.field_70170_p.field_72995_K) {
                  Familiar.dismissFamiliar(par1EntityPlayer, this);
               }

               return true;
            } else if (TameableUtil.isOwner(this, par1EntityPlayer) && !this.func_70877_b(itemstack)) {
               if (itemstack != null && itemstack.func_77973_b() == Items.field_151100_aR) {
                  if (!this.field_70170_p.field_72995_K) {
                     int i = BlockColored.func_150032_b(itemstack.func_77960_j());
                     this.setFeatherColor(i);
                     if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                        --itemstack.field_77994_a;
                     }

                     if (itemstack.field_77994_a <= 0) {
                        par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                     }
                  }
               } else {
                  if (itemstack != null && (itemstack.func_77973_b() == Items.field_151057_cb || itemstack.func_77973_b() == Witchery.Items.POLYNESIA_CHARM || itemstack.func_77973_b() == Witchery.Items.DEVILS_TONGUE_CHARM)) {
                     return false;
                  }

                  if (itemstack != null && this.func_70694_bm() == null) {
                     if (itemstack.field_77994_a == 1) {
                        this.func_70062_b(0, itemstack);
                        par1EntityPlayer.func_70062_b(0, (ItemStack)null);
                     } else {
                        this.func_70062_b(0, itemstack.func_77979_a(1));
                     }
                  } else if (!Witchery.Items.GENERIC.itemWaystonePlayerBound.isMatch(itemstack) && !Witchery.Items.GENERIC.itemWaystoneBound.isMatch(itemstack)) {
                     if (this.func_70694_bm() != null && this.waypoint == null) {
                        ItemStack heldStack = this.func_70694_bm();
                        this.func_70062_b(0, (ItemStack)null);
                        if (!this.field_70170_p.field_72995_K) {
                           this.field_70170_p.func_72838_d(new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, heldStack));
                        }
                     } else if (this.func_70068_e(par1EntityPlayer) < 9.0D && !this.field_70170_p.field_72995_K) {
                        this.func_70904_g(!this.func_70906_o());
                     }
                  } else {
                     this.waypoint = itemstack.func_77946_l();
                     this.homeX = this.field_70165_t;
                     this.homeY = this.field_70163_u;
                     this.homeZ = this.field_70161_v;
                     ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_ORB, this, 1.0D, 1.0D, 16);
                  }
               }

               return true;
            } else if (TameableUtil.isOwner(this, par1EntityPlayer) && this.func_70877_b(itemstack) && this.func_110143_aJ() < this.func_110138_aP()) {
               if (!this.field_70170_p.field_72995_K) {
                  this.func_70691_i(10.0F);
                  if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                     --itemstack.field_77994_a;
                  }

                  if (itemstack.field_77994_a <= 0) {
                     par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                  }
               }

               return true;
            } else {
               return super.func_70085_c(par1EntityPlayer);
            }
         } else if (itemstack != null && (itemstack.func_77973_b() == Items.field_151147_al || itemstack.func_77973_b() == Items.field_151082_bd) && par1EntityPlayer.func_70068_e(this) < 9.0D) {
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
               --itemstack.field_77994_a;
            }

            if (itemstack.field_77994_a <= 0) {
               par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            }

            if (!this.field_70170_p.field_72995_K) {
               if (this.field_70146_Z.nextInt(3) == 0) {
                  this.func_70903_f(true);
                  this.setTameSkin(1 + this.field_70170_p.field_73012_v.nextInt(3));
                  TameableUtil.setOwner(this, par1EntityPlayer);
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
            return !this.func_70877_b(itemstack) ? super.func_70085_c(par1EntityPlayer) : false;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_70620_b(ItemStack stack, int pass) {
      return stack.func_77973_b().func_77623_v() ? stack.func_77973_b().getIcon(stack, pass) : stack.func_77954_c();
   }

   public EntityOwl spawnBabyAnimal(EntityAgeable par1EntityAgeable) {
      EntityOwl entityocelot = new EntityOwl(this.field_70170_p);
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
      } else if (!(par1EntityAnimal instanceof EntityOwl)) {
         return false;
      } else {
         EntityOwl entityocelot = (EntityOwl)par1EntityAnimal;
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
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.owl.name");
   }

   public IEntityLivingData func_110161_a(IEntityLivingData par1EntityLivingData) {
      par1EntityLivingData = super.func_110161_a(par1EntityLivingData);
      return par1EntityLivingData;
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

   public void clearFamiliar() {
      this.setFamiliar(false);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(10.0D);
      this.func_70606_j(15.0F);
   }

   static {
      TEMPTATIONS = new ItemStack[]{new ItemStack(Items.field_151147_al), new ItemStack(Items.field_151082_bd)};
   }
}
