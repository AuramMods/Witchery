package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.Shapeshift;
import com.emoniph.witchery.util.CreatureUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

public class EntityWolfman extends EntityMob implements IEntitySelector {
   private int formerProfession = -1;
   private int attackTimer;
   private boolean infectious;
   boolean isSitting;
   private MerchantRecipeList buyingList;
   private int wealth;
   public ItemStack itemInUse;
   public int itemInUseCount;
   @SideOnly(Side.CLIENT)
   private ResourceLocation skinOverride;

   public EntityWolfman(World world) {
      super(world);
      this.func_70661_as().func_75498_b(true);
      this.func_70661_as().func_75491_a(true);
      this.field_70714_bg.func_75776_a(0, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(1, new EntityAIBreakDoor(this));
      this.field_70714_bg.func_75776_a(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
      this.field_70714_bg.func_75776_a(3, new EntityAIAttackOnCollide(this, EntityVillager.class, 1.0D, true));
      this.field_70714_bg.func_75776_a(4, new EntityAIAttackOnCollide(this, EntityWitchHunter.class, 1.0D, true));
      this.field_70714_bg.func_75776_a(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
      this.field_70714_bg.func_75776_a(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
      this.field_70714_bg.func_75776_a(7, new EntityAIWander(this, 1.0D));
      this.field_70714_bg.func_75776_a(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(8, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAIHurtByTarget(this, true));
      this.field_70715_bh.func_75776_a(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true, false, this));
      this.field_70715_bh.func_75776_a(2, new EntityAINearestAttackableTarget(this, EntityVillager.class, 0, false));
      this.func_70105_a(0.6F, 1.8F);
      this.field_70728_aV = 20;
   }

   public boolean func_82704_a(Entity target) {
      if (target instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)target;
         return !Shapeshift.INSTANCE.isAnimalForm(player);
      } else {
         return false;
      }
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111265_b).func_111128_a(40.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.4D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(5.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(80.0D);
   }

   protected void func_70088_a() {
      super.func_70088_a();
   }

   public boolean isSitting() {
      return this.isSitting;
   }

   public void setSitting(boolean p_70904_1_) {
      this.isSitting = p_70904_1_;
   }

   public int func_70658_aO() {
      int i = super.func_70658_aO() + 10;
      if (i > 20) {
         i = 20;
      }

      return i;
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.wolfman.name");
   }

   protected boolean func_70650_aV() {
      return true;
   }

   public void func_70636_d() {
      if (this.attackTimer > 0) {
         --this.attackTimer;
      }

      if (!this.field_70170_p.field_72995_K) {
         if (this.formerProfession != -1 && this.field_70173_aa % 100 == 3 && !CreatureUtil.isFullMoon(this.field_70170_p) && !this.func_70644_a(Witchery.Potions.WOLFSBANE)) {
            convertToVillager(this, this.formerProfession, this.infectious, this.wealth, this.buyingList);
         } else if (this.field_70173_aa % 40 == 4 && this.func_70644_a(Potion.field_76436_u)) {
            this.func_82170_o(Potion.field_76436_u.field_76415_H);
         }
      }

      super.func_70636_d();
   }

   public boolean func_70097_a(DamageSource source, float damage) {
      return CreatureUtil.isSilverDamage(source) ? super.func_70097_a(source, Math.min(damage * 1.5F, 15.0F)) : super.func_70097_a(source, Math.min(damage, 1.0F));
   }

   public boolean func_70652_k(Entity targetEntity) {
      this.attackTimer = 10;
      this.field_70170_p.func_72960_a(this, (byte)4);
      boolean flag = super.func_70652_k(targetEntity);
      if (flag) {
      }

      return flag;
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

   public int func_70627_aG() {
      return super.func_70627_aG() * 4;
   }

   protected String func_70639_aQ() {
      return this.field_70170_p.field_73012_v.nextInt(20) == 0 ? "witchery:mob.wolfman.howl" : "witchery:mob.wolfman.say";
   }

   protected String func_70621_aR() {
      return "witchery:mob.wolfman.hit";
   }

   protected String func_70673_aS() {
      return "witchery:mob.wolfman.death";
   }

   protected void func_70628_a(boolean p_70628_1_, int fortune) {
      super.func_70628_a(p_70628_1_, fortune);
   }

   protected Item func_146068_u() {
      return Items.field_151103_aS;
   }

   protected void func_70600_l(int p_70600_1_) {
      switch(this.field_70146_Z.nextInt(3)) {
      case 0:
         this.func_70099_a(Witchery.Items.GENERIC.itemSilverDust.createStack(this.field_70170_p.field_73012_v.nextInt(3) + 1), 0.0F);
         break;
      case 1:
         this.func_145779_a(Items.field_151103_aS, 1);
         break;
      case 2:
         this.func_145779_a(Items.field_151116_aA, 1);
      }

   }

   public void setFormerProfession(int profession, int wealth, MerchantRecipeList buyingList) {
      this.formerProfession = profession;
      this.buyingList = buyingList;
      this.wealth = wealth;
   }

   public int getFormerProfession() {
      return this.formerProfession;
   }

   public int getWealth() {
      return this.wealth;
   }

   public MerchantRecipeList getBuyingList() {
      return this.buyingList;
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74768_a("FormerProfession", this.formerProfession);
      nbtRoot.func_74757_a("Infectious", this.infectious);
      nbtRoot.func_74768_a("Riches", this.wealth);
      if (this.buyingList != null) {
         nbtRoot.func_74782_a("Offers", this.buyingList.func_77202_a());
      }

   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      this.formerProfession = nbtRoot.func_74762_e("FormerProfession");
      this.infectious = nbtRoot.func_74767_n("Infectious");
      this.wealth = nbtRoot.func_74762_e("Riches");
      if (nbtRoot.func_150297_b("Offers", 10)) {
         NBTTagCompound nbttagcompound1 = nbtRoot.func_74775_l("Offers");
         this.buyingList = new MerchantRecipeList(nbttagcompound1);
      }

   }

   public void setInfectious() {
      this.infectious = true;
   }

   public boolean isInfectious() {
      return this.infectious;
   }

   public void func_70074_a(EntityLivingBase targetEntity) {
      super.func_70074_a(targetEntity);
   }

   protected boolean func_70692_ba() {
      return false;
   }

   public static void convertToVillager(EntityLiving target, int profession, boolean infectious, int wealth, MerchantRecipeList buyingList) {
      if (target != null && !target.field_70170_p.field_72995_K) {
         EntityVillager entity = new EntityVillagerWere(target.field_70170_p, profession, infectious);
         entity.func_82149_j(target);
         entity.func_82187_q();
         entity.func_110163_bv();
         target.field_70170_p.func_72900_e(target);
         target.field_70170_p.func_72838_d(entity);
         target.field_70170_p.func_72889_a((EntityPlayer)null, 1017, (int)target.field_70165_t, (int)target.field_70163_u, (int)target.field_70161_v, 0);
      }

   }

   public static void convertToCuredVillager(EntityLiving target, int profession, int wealth, MerchantRecipeList buyingList) {
      if (target != null && !target.field_70170_p.field_72995_K) {
         EntityVillager entity = new EntityVillager(target.field_70170_p, profession);
         entity.func_82149_j(target);
         entity.func_82187_q();
         entity.func_110163_bv();
         target.field_70170_p.func_72900_e(target);
         target.field_70170_p.func_72838_d(entity);
         target.field_70170_p.func_72889_a((EntityPlayer)null, 1017, (int)target.field_70165_t, (int)target.field_70163_u, (int)target.field_70161_v, 0);
      }

   }

   public void setItemInUse(ItemStack stack, int itemInUseCount) {
      this.itemInUse = stack;
      this.itemInUseCount = itemInUseCount;
   }

   @SideOnly(Side.CLIENT)
   public void setSkinResource(ResourceLocation skinOverride) {
      this.skinOverride = skinOverride;
   }

   @SideOnly(Side.CLIENT)
   public ResourceLocation getSkinResource() {
      return this.skinOverride;
   }
}
