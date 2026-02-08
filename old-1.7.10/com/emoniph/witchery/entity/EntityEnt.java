package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.Dye;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

public class EntityEnt extends EntityMob implements IEntitySelector {
   private int attackTimer;
   EntityPlayer fakePlayer = null;

   public EntityEnt(World par1World) {
      super(par1World);
      this.func_70105_a(1.2F, 3.0F);
      this.func_70661_as().func_75491_a(true);
      this.func_70661_as().func_75495_e(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, new EntityAIAttackOnCollide(this, 1.0D, true));
      this.field_70714_bg.func_75776_a(3, new EntityAIMoveTowardsTarget(this, 0.9D, 32.0F));
      this.field_70714_bg.func_75776_a(4, new EntityAIWander(this, 0.6D));
      this.field_70714_bg.func_75776_a(5, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
      this.field_70714_bg.func_75776_a(6, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAIHurtByTarget(this, false));
      this.field_70715_bh.func_75776_a(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false, true, this));
      this.field_70728_aV = 25;
   }

   public boolean func_82704_a(Entity entity) {
      return true;
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(17, "");
      this.field_70180_af.func_75682_a(16, (byte)0);
   }

   public boolean isScreaming() {
      return this.field_70180_af.func_75683_a(16) > 0;
   }

   public void setScreaming(boolean par1) {
      this.field_70180_af.func_75692_b(16, (byte)(par1 ? 1 : 0));
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.ent.name");
   }

   public boolean func_70650_aV() {
      return true;
   }

   protected void func_70629_bd() {
      super.func_70629_bd();
      if (!this.field_70170_p.field_72995_K && this.func_70089_S()) {
         if (this.func_70638_az() != null) {
            this.setScreaming(true);
         } else {
            this.setScreaming(false);
         }
      }

   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(200.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.4D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(4.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111266_c).func_111128_a(1.0D);
   }

   protected int func_70682_h(int par1) {
      return par1;
   }

   protected void func_82167_n(Entity par1Entity) {
      super.func_82167_n(par1Entity);
   }

   public void func_70636_d() {
      super.func_70636_d();
      if (this.attackTimer > 0) {
         --this.attackTimer;
      }

      int i;
      int j;
      int k;
      if (!this.field_70170_p.field_72995_K && this.field_70170_p.field_73012_v.nextInt(300) == 0) {
         try {
            i = MathHelper.func_76128_c(this.field_70165_t);
            j = MathHelper.func_76128_c(this.field_70163_u) - 1;
            k = MathHelper.func_76128_c(this.field_70161_v);
            if ((this.fakePlayer == null || this.fakePlayer.field_70170_p != this.field_70170_p) && this.field_70170_p instanceof WorldServer) {
               this.fakePlayer = new FakePlayer((WorldServer)this.field_70170_p, new GameProfile(UUID.randomUUID(), "[Minecraft]"));
            }

            if (this.fakePlayer != null) {
               ItemDye.applyBonemeal(Dye.BONE_MEAL.createStack(), this.field_70170_p, i, j, k, this.fakePlayer);
            }
         } catch (Throwable var5) {
         }
      }

      if (this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y > 2.500000277905201E-7D && this.field_70146_Z.nextInt(5) == 0) {
         i = MathHelper.func_76128_c(this.field_70165_t);
         j = MathHelper.func_76128_c(this.field_70163_u - 0.20000000298023224D - (double)this.field_70129_M);
         k = MathHelper.func_76128_c(this.field_70161_v);
         Block l = this.field_70170_p.func_147439_a(i, j, k);
         if (l != Blocks.field_150350_a) {
            this.field_70170_p.func_72869_a("tilecrack_" + l + "_" + this.field_70170_p.func_72805_g(i, j, k), this.field_70165_t + ((double)this.field_70146_Z.nextFloat() - 0.5D) * (double)this.field_70130_N, this.field_70121_D.field_72338_b + 0.1D, this.field_70161_v + ((double)this.field_70146_Z.nextFloat() - 0.5D) * (double)this.field_70130_N, 4.0D * ((double)this.field_70146_Z.nextFloat() - 0.5D), 0.5D, ((double)this.field_70146_Z.nextFloat() - 0.5D) * 4.0D);
         }
      }

   }

   public boolean func_70686_a(Class par1Class) {
      return super.func_70686_a(par1Class);
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
      this.func_110163_bv();
      this.field_70180_af.func_75692_b(17, par1Str);
   }

   public EntityPlayer getOwnerEntity() {
      return this.field_70170_p.func_72924_a(this.getOwnerName());
   }

   public boolean func_70652_k(Entity par1Entity) {
      this.attackTimer = 10;
      this.field_70170_p.func_72960_a(this, (byte)4);
      boolean flag = super.func_70652_k(par1Entity);
      if (flag) {
         par1Entity.field_70181_x += 0.4000000059604645D;
      }

      this.func_85030_a("mob.irongolem.throw", 1.0F, 1.0F);
      return flag;
   }

   @SideOnly(Side.CLIENT)
   public void func_70103_a(byte par1) {
      if (par1 == 4) {
         this.attackTimer = 10;
         this.func_85030_a("mob.irongolem.throw", 1.0F, 1.0F);
      } else {
         super.func_70103_a(par1);
      }

   }

   public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
      Entity entity = par1DamageSource.func_76346_g();
      par2 = Math.min(par2, 15.0F);
      if (entity != null && entity instanceof EntityLivingBase && (par1DamageSource.field_76373_n == "mob" || par1DamageSource.field_76373_n == "player") && ((EntityLivingBase)entity).func_70694_bm() != null && ((EntityLivingBase)entity).func_70694_bm().func_77973_b() instanceof ItemAxe) {
         par2 *= 3.0F;
      }

      return super.func_70097_a(par1DamageSource, par2);
   }

   @SideOnly(Side.CLIENT)
   public int getAttackTimer() {
      return this.attackTimer;
   }

   public float func_70013_c(float par1) {
      return 1.0F;
   }

   protected String func_70639_aQ() {
      return null;
   }

   protected String func_70621_aR() {
      return "mob.horse.zombie.hit";
   }

   protected String func_70673_aS() {
      return "mob.horse.zombie.death";
   }

   protected void func_145780_a(int par1, int par2, int par3, Block par4) {
      this.func_85030_a("mob.irongolem.walk", 1.0F, 1.0F);
   }

   protected void func_70628_a(boolean par1, int par2) {
      this.func_70099_a(Witchery.Items.GENERIC.itemBranchEnt.createStack(), 0.0F);
      this.func_70099_a(new ItemStack(Witchery.Blocks.SAPLING, 1, this.field_70170_p.field_73012_v.nextInt(3)), 0.0F);
   }

   public boolean isPlayerCreated() {
      return (this.field_70180_af.func_75683_a(16) & 1) != 0;
   }

   public void setPlayerCreated(boolean par1) {
      this.func_110163_bv();
      byte b0 = this.field_70180_af.func_75683_a(16);
      if (par1) {
         this.field_70180_af.func_75692_b(16, (byte)(b0 | 1));
      } else {
         this.field_70180_af.func_75692_b(16, (byte)(b0 & -2));
      }

   }

   public void func_70645_a(DamageSource par1DamageSource) {
      super.func_70645_a(par1DamageSource);
   }

   protected boolean func_70692_ba() {
      return true;
   }

   public IEntityLivingData func_110161_a(IEntityLivingData par1EntityLivingData) {
      return super.func_110161_a(par1EntityLivingData);
   }
}
