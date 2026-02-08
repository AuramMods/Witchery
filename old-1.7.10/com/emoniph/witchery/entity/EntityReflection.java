package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.client.renderer.RenderReflection;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.infusion.infusions.symbols.EffectRegistry;
import com.emoniph.witchery.infusion.infusions.symbols.SymbolEffect;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.IHandleDT;
import com.emoniph.witchery.util.RandomCollection;
import com.emoniph.witchery.util.TransformCreature;
import com.google.common.collect.Multimap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
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
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class EntityReflection extends EntityMob implements IBossDisplayData, IRangedAttackMob, IHandleDT {
   private int attackTimer;
   private boolean freeSpawn;
   private boolean isVampire;
   private int livingTicks = -1;
   private EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 1.0D, 20, 60, 15.0F);
   private EntityAIAttackOnCollide aiAttackOnCollide = new EntityAIAttackOnCollide(this, EntityLivingBase.class, 1.2D, false);
   private String owner = "";
   private EntityReflection.Task task;
   private static final RandomCollection<SymbolEffect> SPELLS = createSpells();
   @SideOnly(Side.CLIENT)
   private ThreadDownloadImageData downloadImageSkin;
   @SideOnly(Side.CLIENT)
   private ResourceLocation locationSkin;
   private String lastSkinOwner;

   public EntityReflection(World world) {
      super(world);
      this.task = EntityReflection.Task.NONE;
      this.func_70105_a(0.6F, 1.8F);
      this.field_70178_ae = true;
      this.func_70661_as().func_75491_a(true);
      this.func_70661_as().func_75495_e(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(3, new EntityAIWander(this, 1.0D));
      this.field_70714_bg.func_75776_a(4, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
      this.field_70714_bg.func_75776_a(5, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
      this.field_70715_bh.func_75776_a(2, new EntityAIHurtByTarget(this, false));
      this.field_70728_aV = 50;
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(17, "");
      this.field_70180_af.func_75682_a(18, (byte)0);
   }

   public String getOwnerSkin() {
      return this.field_70180_af.func_75681_e(17);
   }

   public String getOwnerName() {
      return this.owner;
   }

   public void setOwnerSkin(String skinName) {
      this.field_70180_af.func_75692_b(17, skinName);
   }

   public void setOwner(String par1Str) {
      this.func_110163_bv();
      this.owner = par1Str;
   }

   public EntityPlayer getOwnerEntity() {
      return this.field_70170_p.func_72924_a(this.getOwnerName());
   }

   public void setModel(int model) {
      this.field_70180_af.func_75692_b(18, (byte)model);
   }

   public int getModel() {
      return this.field_70180_af.func_75683_a(18);
   }

   public void setLifetime(int ticks) {
      this.livingTicks = ticks;
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(100.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.35D);
      this.func_110148_a(SharedMonsterAttributes.field_111265_b).func_111128_a(50.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111266_c).func_111128_a(1.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(2.0D);
   }

   public void func_70110_aj() {
   }

   public String func_70005_c_() {
      if (this.func_94056_bM()) {
         return this.func_94057_bL();
      } else {
         String owner = this.getOwnerName();
         return owner != null && !owner.isEmpty() ? owner : StatCollector.func_74838_a("entity.witchery.reflection.name");
      }
   }

   public boolean func_70650_aV() {
      return true;
   }

   protected void func_70629_bd() {
      super.func_70629_bd();
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

      if (!this.field_70170_p.field_72995_K && this.field_70173_aa % 30 == 1) {
         if (!this.freeSpawn && this.field_71093_bK != Config.instance().dimensionMirrorID) {
            this.func_70106_y();
            return;
         }

         if (this.livingTicks > -1 && --this.livingTicks == 0) {
            this.func_70106_y();
            return;
         }

         double R = 10.0D;
         double RY = 8.0D;
         AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(this.field_70165_t - R, this.field_70163_u - RY, this.field_70161_v - R, this.field_70165_t + R, this.field_70163_u + RY, this.field_70161_v + R);
         List<EntityPlayer> players = this.field_70170_p.func_72872_a(EntityPlayer.class, bounds);
         EntityPlayer ownerEntity = this.getOwnerEntity();
         boolean ownerFound = false;
         EntityPlayer closest = null;
         double distance = Double.MAX_VALUE;
         Iterator i$ = players.iterator();

         while(i$.hasNext()) {
            EntityPlayer player = (EntityPlayer)i$.next();
            double newDistance = player.func_70068_e(this);
            if (closest == null || newDistance < distance) {
               closest = player;
               distance = newDistance;
            }

            if (ownerEntity == player) {
               ownerFound = true;
            }
         }

         if (ownerEntity == null || !ownerFound) {
            if (closest != null) {
               this.setOwner(closest.func_70005_c_());
            } else {
               this.setOwner("");
            }
         }

         boolean resetGear = true;
         String skinName = this.getOwnerName();
         if (!this.getOwnerName().isEmpty()) {
            EntityPlayer owner = ownerEntity != null && ownerFound ? ownerEntity : this.getOwnerEntity();
            if (owner != null) {
               for(int slot = 1; slot <= 4; ++slot) {
                  ItemStack stack = owner.func_71124_b(slot);
                  if (stack != null) {
                     stack = stack.func_77946_l();
                  }

                  this.func_70062_b(slot, stack);
               }

               ItemStack bestWeapon = null;
               double bestDamage = 0.0D;
               int hot = 0;

               label187:
               while(true) {
                  ItemStack stack;
                  if (hot >= 9) {
                     ExtendedPlayer playerEx = ExtendedPlayer.get(owner);
                     if (playerEx != null) {
                        this.setModel(playerEx.getCreatureType() == TransformCreature.WOLFMAN ? 1 : 0);
                        this.isVampire = playerEx.isVampire();
                        if (playerEx.getCreatureType() == TransformCreature.PLAYER) {
                           skinName = playerEx.getOtherPlayerSkin();
                        }
                     }

                     stack = bestWeapon != null ? bestWeapon : owner.func_71124_b(0);
                     if (stack != null) {
                        stack = stack.func_77946_l();
                        Witchery.modHooks.makeItemModProof(stack);
                     }

                     if (this.getModel() == 1) {
                        stack = null;
                        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(6.0D);
                     } else {
                        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(2.0D);
                     }

                     this.func_70062_b(0, stack);
                     resetGear = false;
                     if (this.field_70173_aa % 60 != 1) {
                        break;
                     }

                     this.func_70674_bp();
                     Iterator effects = owner.func_70651_bq().iterator();

                     while(true) {
                        if (!effects.hasNext()) {
                           break label187;
                        }

                        PotionEffect effect = (PotionEffect)effects.next();
                        this.func_70690_d(new PotionEffect(effect));
                     }
                  }

                  stack = owner.field_71071_by.func_70301_a(hot);
                  if (stack != null) {
                     Multimap modifierMap = stack.func_111283_C();
                     Iterator itr = modifierMap.get(SharedMonsterAttributes.field_111264_e.func_111108_a()).iterator();
                     double damage = 0.0D;

                     while(itr.hasNext()) {
                        AttributeModifier modifier = (AttributeModifier)itr.next();
                        if (modifier.func_111169_c() == 0) {
                           damage += modifier.func_111164_d();
                        }
                     }

                     if (damage > bestDamage) {
                        bestWeapon = stack;
                        bestDamage = damage;
                     }
                  }

                  ++hot;
               }
            }
         }

         if (resetGear) {
            for(int slot = 0; slot <= 4; ++slot) {
               this.func_70062_b(slot, (ItemStack)null);
            }
         }

         this.setOwnerSkin(skinName);
         ItemStack held = this.func_70694_bm();
         if (held != null) {
            if (held.func_77973_b() == Witchery.Items.MYSTIC_BRANCH) {
               if (this.task == EntityReflection.Task.MELEE) {
                  this.field_70714_bg.func_85156_a(this.aiAttackOnCollide);
               }

               this.field_70714_bg.func_75776_a(2, this.aiArrowAttack);
               this.task = EntityReflection.Task.RANGED;
            } else if (held.func_77973_b() != Witchery.Items.CROSSBOW_PISTOL && !(held.func_77973_b() instanceof ItemBow)) {
               if (this.task == EntityReflection.Task.RANGED) {
                  this.field_70714_bg.func_85156_a(this.aiArrowAttack);
               }

               this.field_70714_bg.func_75776_a(2, this.aiAttackOnCollide);
               this.task = EntityReflection.Task.MELEE;
            } else {
               if (this.task == EntityReflection.Task.MELEE) {
                  this.field_70714_bg.func_85156_a(this.aiAttackOnCollide);
               }

               this.field_70714_bg.func_75776_a(2, this.aiArrowAttack);
               this.task = EntityReflection.Task.RANGED;
            }
         } else {
            if (this.task == EntityReflection.Task.RANGED) {
               this.field_70714_bg.func_85156_a(this.aiArrowAttack);
            }

            this.field_70714_bg.func_75776_a(2, this.aiAttackOnCollide);
            this.task = EntityReflection.Task.MELEE;
         }

         if (this.func_70089_S() && this.func_70638_az() != null && this.func_70661_as().func_75500_f() && this.func_70635_at().func_75522_a(this.func_70638_az())) {
            EntityLivingBase var10001 = this.func_70638_az();
            EffectRegistry.instance();
            this.castSpell(var10001, 1.0F, EffectRegistry.Attraho);
         }
      }

      if (!this.field_70170_p.field_72995_K && this.field_70170_p.field_73012_v.nextDouble() < 0.05D && this.func_70638_az() != null && (this.func_70638_az().field_70160_al || this.func_70638_az() instanceof EntityPlayer && ((EntityPlayer)this.func_70638_az()).field_71075_bZ.field_75100_b) && !this.func_70638_az().func_70644_a(Potion.field_76421_d)) {
         this.func_70638_az().func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, 200, 5));
      }

   }

   public void func_70645_a(DamageSource p_70645_1_) {
      super.func_70645_a(p_70645_1_);
      Witchery.Blocks.MIRROR.demonSlain(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v);
   }

   public boolean func_70097_a(DamageSource source, float damage) {
      return super.func_70097_a(source, Math.min(damage, 6.0F));
   }

   public float getCapDT(DamageSource source, float damage) {
      return 2.0F;
   }

   public boolean func_70686_a(Class par1Class) {
      return super.func_70686_a(par1Class);
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74778_a("Owner", this.getOwnerName());
      nbtRoot.func_74778_a("OwnerSkin", this.getOwnerSkin());
      nbtRoot.func_74768_a("Model", this.getModel());
      nbtRoot.func_74757_a("FreeSpawn", this.freeSpawn);
      nbtRoot.func_74757_a("Vampire", this.isVampire);
      nbtRoot.func_74768_a("LivingTicks", this.livingTicks);
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      this.setOwner(nbtRoot.func_74779_i("Owner"));
      this.setOwnerSkin(nbtRoot.func_74779_i("OwnerSkin"));
      this.freeSpawn = nbtRoot.func_74767_n("FreeSpawn");
      this.livingTicks = nbtRoot.func_74762_e("LivingTicks");
      this.isVampire = nbtRoot.func_74767_n("Vampire");
      this.setModel(nbtRoot.func_74762_e("Model"));
   }

   public boolean func_70652_k(Entity par1Entity) {
      this.attackTimer = 10;
      boolean flag = super.func_70652_k(par1Entity);
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

   public float func_70013_c(float par1) {
      return 1.0F;
   }

   protected String func_70639_aQ() {
      return "witchery:mob.reflection.say";
   }

   protected String func_70621_aR() {
      return "witchery:mob.reflection.hit";
   }

   protected String func_70673_aS() {
      return "witchery:mob.reflection.death";
   }

   protected void func_145780_a(int par1, int par2, int par3, Block par4) {
      super.func_145780_a(par1, par2, par3, par4);
   }

   protected void func_70628_a(boolean par1, int par2) {
      this.func_70099_a(Witchery.Items.GENERIC.itemDemonHeart.createStack(), 0.0F);
   }

   protected void func_82160_b(boolean p_82160_1_, int p_82160_2_) {
   }

   protected Item func_146068_u() {
      return null;
   }

   protected boolean func_70692_ba() {
      return false;
   }

   private static RandomCollection<SymbolEffect> createSpells() {
      RandomCollection<SymbolEffect> spells = new RandomCollection();
      EffectRegistry.instance();
      spells.add(14.0D, EffectRegistry.Ignianima);
      EffectRegistry.instance();
      spells.add(2.0D, EffectRegistry.Expelliarmus);
      EffectRegistry.instance();
      spells.add(2.0D, EffectRegistry.Flipendo);
      EffectRegistry.instance();
      spells.add(2.0D, EffectRegistry.Impedimenta);
      EffectRegistry.instance();
      spells.add(1.0D, EffectRegistry.Confundus);
      return spells;
   }

   public void func_82196_d(EntityLivingBase targetEntity, float par2) {
      ItemStack held = this.func_70694_bm();
      if (held != null) {
         this.attackTimer = 10;
         this.field_70170_p.func_72960_a(this, (byte)4);
         if (held.func_77973_b() == Witchery.Items.MYSTIC_BRANCH) {
            if (this.field_70170_p.field_73012_v.nextBoolean()) {
               this.castSpell(targetEntity, par2, (SymbolEffect)SPELLS.next());
            }
         } else {
            int i;
            int j;
            if (held.func_77973_b() == Witchery.Items.CROSSBOW_PISTOL) {
               EntityBolt entityarrow = new EntityBolt(this.field_70170_p, this, targetEntity, 1.6F, (float)(14 - this.field_70170_p.field_73013_u.func_151525_a() * 4));
               i = EnchantmentHelper.func_77506_a(Enchantment.field_77345_t.field_77352_x, this.func_70694_bm());
               j = EnchantmentHelper.func_77506_a(Enchantment.field_77344_u.field_77352_x, this.func_70694_bm());
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
            } else {
               EntityArrow entityarrow = new EntityArrow(this.field_70170_p, this, targetEntity, 1.6F, (float)(14 - this.field_70170_p.field_73013_u.func_151525_a() * 3));
               i = EnchantmentHelper.func_77506_a(Enchantment.field_77345_t.field_77352_x, this.func_70694_bm());
               j = EnchantmentHelper.func_77506_a(Enchantment.field_77344_u.field_77352_x, this.func_70694_bm());
               entityarrow.func_70239_b((double)(par2 * 2.0F) + this.field_70146_Z.nextGaussian() * 0.25D + (double)((float)this.field_70170_p.field_73013_u.func_151525_a() * 0.11F));
               if (i > 0) {
                  entityarrow.func_70239_b(entityarrow.func_70242_d() + (double)i * 0.5D + 0.5D);
               }

               if (j > 0) {
                  entityarrow.func_70240_a(j);
               }

               if (EnchantmentHelper.func_77506_a(Enchantment.field_77343_v.field_77352_x, this.func_70694_bm()) > 0) {
                  entityarrow.func_70015_d(100);
               }

               this.func_85030_a("random.bow", 1.0F, 1.0F / (this.func_70681_au().nextFloat() * 0.4F + 0.8F));
               this.field_70170_p.func_72838_d(entityarrow);
            }
         }

      }
   }

   private void castSpell(EntityLivingBase targetEntity, float par2, SymbolEffect spell) {
      double d0 = targetEntity.field_70165_t - this.field_70165_t;
      double d1 = targetEntity.field_70121_D.field_72338_b + (double)(targetEntity.field_70131_O / 2.0F) - (this.field_70163_u + (double)(this.field_70131_O / 2.0F));
      double d2 = targetEntity.field_70161_v - this.field_70161_v;
      float f1 = MathHelper.func_76129_c(par2) * 0.5F;
      if (!this.field_70170_p.field_72995_K) {
         this.field_70170_p.func_72889_a((EntityPlayer)null, 1009, (int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v, 0);
         int count = this.field_70146_Z.nextInt(10) == 0 ? true : true;
         EntitySpellEffect effect = new EntitySpellEffect(this.field_70170_p, this, d0 + this.field_70146_Z.nextGaussian() * (double)f1, d1, d2 + this.field_70146_Z.nextGaussian() * (double)f1, spell, 1);
         double d8 = 1.0D;
         effect.field_70165_t = this.field_70165_t;
         effect.field_70163_u = this.field_70163_u + (double)(this.field_70131_O / 2.0F);
         effect.field_70161_v = this.field_70161_v;
         this.field_70170_p.func_72838_d(effect);
         effect.setShooter(this);
      }

   }

   @SideOnly(Side.CLIENT)
   public ResourceLocation getLocationSkin() {
      if (this.locationSkin == null || !this.lastSkinOwner.equals(this.getOwnerName())) {
         this.setupCustomSkin();
      }

      return this.locationSkin != null ? this.locationSkin : null;
   }

   @SideOnly(Side.CLIENT)
   private void setupCustomSkin() {
      String ownerName = this.getOwnerSkin();
      if (ownerName != null && !ownerName.isEmpty()) {
         this.locationSkin = AbstractClientPlayer.func_110311_f(ownerName);
         this.downloadImageSkin = getDownloadImageSkin(this.locationSkin, ownerName);
         this.lastSkinOwner = ownerName;
      } else {
         this.locationSkin = null;
         this.downloadImageSkin = null;
         this.lastSkinOwner = "";
      }

   }

   @SideOnly(Side.CLIENT)
   public static ThreadDownloadImageData getDownloadImageSkin(ResourceLocation location, String name) {
      TextureManager texturemanager = Minecraft.func_71410_x().func_110434_K();
      Object object = texturemanager.func_110581_b(location);
      if (object == null) {
         object = new ThreadDownloadImageData((File)null, String.format("http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtils.func_76338_a(name)), RenderReflection.SKIN, new ImageBufferDownload());
         texturemanager.func_110579_a(location, (ITextureObject)object);
      }

      return (ThreadDownloadImageData)object;
   }

   public IEntityLivingData func_110161_a(IEntityLivingData data) {
      this.freeSpawn = true;
      return super.func_110161_a(data);
   }

   public boolean isVampire() {
      return this.isVampire;
   }

   private static enum Task {
      NONE,
      MELEE,
      RANGED;
   }
}
