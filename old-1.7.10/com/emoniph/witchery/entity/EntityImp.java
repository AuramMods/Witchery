package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.ai.EntityAIWanderWithRestriction;
import com.emoniph.witchery.item.ItemGeneralContract;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TameableUtil;
import com.emoniph.witchery.util.TimeUtil;
import java.util.HashMap;
import java.util.Random;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityImp extends EntityTameable implements IMob, IEntitySelector, EntityAIWanderWithRestriction.IHomeLocationProvider {
   private float field_70926_e;
   private float field_70924_f;
   private boolean field_70928_h;
   private static final int MAX_WANDER_RANGE = 16;
   private int secretsShared;
   private int homeX;
   private int homeY;
   private int homeZ;
   private long lastGiftTime;
   private long powerUpExpiry;
   private static final HashMap<Item, Integer> shinies = new HashMap();
   private static final int REWARD_AFFECTION_LEVEL = 20;
   private static final long GIFT_DELAY_TICKS;
   private static final ItemStack[] EXTRA_DROPS;
   private static final String[] DEMON_NAMES;

   public EntityImp(World par1World) {
      super(par1World);
      this.field_70178_ae = true;
      this.func_70105_a(0.4F, 1.3F);
      this.func_70661_as().func_75491_a(true);
      this.func_70661_as().func_75495_e(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, new EntityAILeapAtTarget(this, 0.4F));
      this.field_70714_bg.func_75776_a(3, new EntityAIAttackOnCollide(this, 1.0D, true));
      this.field_70714_bg.func_75776_a(4, new EntityAIWanderWithRestriction(this, 1.0D, this));
      this.field_70714_bg.func_75776_a(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(6, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAIOwnerHurtByTarget(this));
      this.field_70715_bh.func_75776_a(2, new EntityAIOwnerHurtTarget(this));
      this.field_70715_bh.func_75776_a(3, new EntityAIHurtByTarget(this, true));
      this.field_70715_bh.func_75776_a(3, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 0, false, true, this));
      this.func_70903_f(false);
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.30000001192092896D);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(50.0D);
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(18, 0);
      this.field_70180_af.func_75682_a(19, 0);
   }

   private void setAffection(int affection) {
      this.field_70180_af.func_75692_b(18, affection);
   }

   private int getAffection() {
      return this.field_70180_af.func_75679_c(18);
   }

   private void setPowered(boolean powered) {
      if (!this.field_70170_p.field_72995_K) {
         this.field_70180_af.func_75692_b(19, powered ? 1 : 0);
      }

   }

   public boolean isPowered() {
      return this.field_70180_af.func_75679_c(19) == 1;
   }

   public boolean func_82704_a(Entity target) {
      if (!this.func_70909_n()) {
         return target instanceof EntityPlayer;
      } else {
         return target == this.func_70638_az();
      }
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.imp.name");
   }

   public boolean func_70650_aV() {
      return true;
   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      super.func_70014_b(par1NBTTagCompound);
      par1NBTTagCompound.func_74768_a("Affection", this.getAffection());
      par1NBTTagCompound.func_74768_a("SecretsShared", this.secretsShared);
      par1NBTTagCompound.func_74772_a("LastGiftTime", this.lastGiftTime);
      par1NBTTagCompound.func_74772_a("PowerUpUntil2", this.powerUpExpiry);
      par1NBTTagCompound.func_74768_a("HomeLocX", this.homeX);
      par1NBTTagCompound.func_74768_a("HomeLocY", this.homeY);
      par1NBTTagCompound.func_74768_a("HomeLocZ", this.homeZ);
   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      super.func_70037_a(par1NBTTagCompound);
      this.setAffection(par1NBTTagCompound.func_74762_e("Affection"));
      this.secretsShared = par1NBTTagCompound.func_74762_e("SecretsShared");
      this.lastGiftTime = par1NBTTagCompound.func_74763_f("LastGiftTime");
      long time = TimeUtil.getServerTimeInTicks();
      if (par1NBTTagCompound.func_74764_b("PowerUpUntil2")) {
         this.powerUpExpiry = par1NBTTagCompound.func_74763_f("PowerUpUntil2");
      } else if (par1NBTTagCompound.func_74764_b("PowerUpUntil")) {
         this.powerUpExpiry = par1NBTTagCompound.func_74763_f("PowerUpUntil");
         if (this.powerUpExpiry > 0L) {
            this.powerUpExpiry = time + (long)TimeUtil.minsToTicks(60);
         }
      }

      if (time < this.powerUpExpiry) {
         this.setPowered(true);
      }

      this.homeX = par1NBTTagCompound.func_74762_e("HomeLocX");
      this.homeY = par1NBTTagCompound.func_74762_e("HomeLocY");
      this.homeZ = par1NBTTagCompound.func_74762_e("HomeLocZ");
   }

   protected String func_70639_aQ() {
      return "witchery:mob.imp.laugh";
   }

   protected float func_70647_i() {
      return this.isPowered() ? (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.2F + 0.7F : (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.2F + 1.1F;
   }

   protected String func_70621_aR() {
      return "witchery:mob.imp.hit";
   }

   protected String func_70673_aS() {
      return "witchery:mob.imp.death";
   }

   protected float func_70599_aP() {
      return 0.5F;
   }

   public int func_70627_aG() {
      return TimeUtil.secsToTicks(40);
   }

   public void func_70636_d() {
      super.func_70636_d();
      if (!this.field_70170_p.field_72995_K && TimeUtil.secondsElapsed(300, (long)this.field_70173_aa) && TameableUtil.hasOwner(this)) {
         EntityLivingBase owner = this.func_70902_q();
         if (owner instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)owner;
            this.setAffection(Math.max(0, this.getAffection() - 1));
            if (this.getAffection() == 0 && this.field_70173_aa > TimeUtil.minsToTicks(60) && this.field_70170_p.field_73012_v.nextDouble() < 0.01D) {
               ParticleEffect.FLAME.send(SoundEffect.WITCHERY_MOB_IMP_LAUGH, this, 1.0D, 1.0D, 16);
               ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.goodbye", this.func_70005_c_());
               this.func_70106_y();
            }
         }
      }

      if (!this.field_70170_p.field_72995_K && this.powerUpExpiry > 0L && this.isPowerupExpired()) {
         this.setPowered(false);
         this.powerUpExpiry = 0L;
      }

      if (this.field_70173_aa % 20 == 0) {
         if (this.isPowered()) {
            if ((double)this.field_70130_N != 0.6D) {
               this.func_70105_a(0.6F, 1.3F);
            }

            if (!this.field_70170_p.field_72995_K) {
               this.func_70691_i(1.0F);
            }
         } else if ((double)this.field_70130_N != 0.4D) {
            this.func_70105_a(0.4F, 1.3F);
         }
      }

      if (this.field_70173_aa % 400 == 0) {
         this.func_70691_i(1.0F);
      }

   }

   private boolean isPowerupExpired() {
      return TimeUtil.getServerTimeInTicks() >= this.powerUpExpiry;
   }

   public void func_70071_h_() {
      super.func_70071_h_();
      if (this.field_70170_p.field_72995_K && this.isPowered()) {
         this.field_70170_p.func_72869_a(ParticleEffect.FLAME.toString(), this.field_70165_t - (double)this.field_70130_N * 0.5D + this.field_70170_p.field_73012_v.nextDouble() * (double)this.field_70130_N, 0.1D + this.field_70163_u + this.field_70170_p.field_73012_v.nextDouble() * 2.0D, this.field_70161_v - (double)this.field_70130_N * 0.5D + this.field_70170_p.field_73012_v.nextDouble() * (double)this.field_70130_N, 0.0D, 0.0D, 0.0D);
      }

   }

   public boolean func_70097_a(DamageSource source, float damage) {
      return super.func_70097_a(source, Math.min(damage, this.isPowered() ? 5.0F : 15.0F));
   }

   public boolean func_70652_k(Entity par1Entity) {
      return par1Entity.func_70097_a(DamageSource.func_76358_a(this), this.isPowered() ? 8.0F : 4.0F);
   }

   public boolean func_70085_c(EntityPlayer player) {
      ItemStack stack = player.field_71071_by.func_70448_g();
      if (stack == null) {
         return true;
      } else if (this.field_70170_p.field_72995_K) {
         return false;
      } else {
         if (this.func_70909_n()) {
            if (Witchery.Items.GENERIC.itemDemonHeart.isMatch(stack)) {
               if (!player.field_71075_bZ.field_75098_d) {
                  --stack.field_77994_a;
                  if (stack.field_77994_a <= 0) {
                     player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                  }
               }

               if (!this.field_70170_p.field_72995_K) {
                  this.powerUpExpiry = TimeUtil.getServerTimeInTicks() + (long)TimeUtil.minsToTicks(60);
                  this.setPowered(true);
                  SoundEffect.WITCHERY_MOB_IMP_LAUGH.playAtPlayer(this.field_70170_p, player, 0.5F, this.func_70647_i());
                  ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.gift.power", this.func_70005_c_());
               }
            } else if (Witchery.Items.GENERIC.itemIcyNeedle.isMatch(stack)) {
               if (!player.field_71075_bZ.field_75098_d) {
                  --stack.field_77994_a;
                  if (stack.field_77994_a <= 0) {
                     player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                  }
               }

               if (!this.field_70170_p.field_72995_K) {
                  this.powerUpExpiry = 0L;
                  this.setPowered(false);
                  SoundEffect.WITCHERY_MOB_IMP_LAUGH.playAtPlayer(this.field_70170_p, player, 0.5F, this.func_70647_i());
                  ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.gift.powerloss", this.func_70005_c_());
               }
            } else {
               String name;
               if (!ItemGeneralContract.isBoundContract(stack)) {
                  if (!this.field_70170_p.field_72995_K) {
                     Integer affectionBoost = (Integer)shinies.get(stack.func_77973_b());
                     if (affectionBoost != null && stack.func_77960_j() == 0) {
                        long timeNow = TimeUtil.getServerTimeInTicks();
                        if (!player.field_71075_bZ.field_75098_d) {
                           --stack.field_77994_a;
                           if (stack.field_77994_a <= 0) {
                              player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                           }
                        }

                        int affection = this.getAffection() + affectionBoost;
                        SoundEffect.WITCHERY_MOB_IMP_LAUGH.playAtPlayer(this.field_70170_p, player, 0.5F, this.func_70647_i());
                        if (affection >= 20 && (timeNow > this.lastGiftTime + GIFT_DELAY_TICKS || player.field_71075_bZ.field_75098_d) && this.field_70146_Z.nextInt(Math.max(1, 10 - Math.max(affection - 20, 0))) == 0) {
                           this.lastGiftTime = timeNow;
                           affection = 0;
                           ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.gift.reciprocate", this.func_70005_c_());
                           name = null;
                           ItemStack stackForPlayer;
                           switch(this.secretsShared) {
                           case 0:
                              stackForPlayer = Witchery.Items.GENERIC.itemBrewSoulHunger.createStack();
                              ++this.secretsShared;
                              break;
                           case 1:
                              stackForPlayer = Witchery.Items.GENERIC.itemBrewSoulFear.createStack();
                              ++this.secretsShared;
                              break;
                           case 2:
                              stackForPlayer = Witchery.Items.GENERIC.itemBrewSoulAnguish.createStack();
                              ++this.secretsShared;
                              break;
                           case 3:
                              stackForPlayer = Witchery.Items.GENERIC.itemContractTorment.createStack();
                              ++this.secretsShared;
                              break;
                           default:
                              stackForPlayer = EXTRA_DROPS[this.field_70146_Z.nextInt(EXTRA_DROPS.length)].func_77946_l();
                           }

                           if (stackForPlayer != null) {
                              ParticleEffect.INSTANT_SPELL.send(SoundEffect.NOTE_HARP, this, 1.0D, 1.0D, 16);
                              this.field_70170_p.func_72838_d(new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, stackForPlayer));
                           }
                        } else if (timeNow < this.lastGiftTime + GIFT_DELAY_TICKS) {
                           ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.gift.toomany", this.func_70005_c_());
                        } else {
                           ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.gift.like", this.func_70005_c_());
                        }

                        this.setAffection(affection);
                     } else {
                        SoundEffect.WITCHERY_MOB_IMP_LAUGH.playAtPlayer(this.field_70170_p, player, 0.5F, this.func_70647_i());
                        ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.gift.hate", this.func_70005_c_());
                     }
                  }

                  return true;
               }

               if (!this.field_70170_p.field_72995_K) {
                  SoundEffect.WITCHERY_MOB_IMP_LAUGH.playAtPlayer(this.field_70170_p, player, 0.5F, this.func_70647_i());
                  if (!this.isPowered()) {
                     if (this.getAffection() >= 20) {
                        long timeNow = TimeUtil.getServerTimeInTicks();
                        if (timeNow <= this.lastGiftTime + GIFT_DELAY_TICKS && !player.field_71075_bZ.field_75098_d) {
                           ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.spell.toooften", this.func_70005_c_());
                        } else {
                           ItemGeneralContract contract = ItemGeneralContract.getContract(stack);
                           EntityLivingBase targetEntity = Witchery.Items.TAGLOCK_KIT.getBoundEntity(this.field_70170_p, player, stack, 1);
                           if (targetEntity != null) {
                              if (contract.activate(stack, targetEntity)) {
                                 this.lastGiftTime = timeNow;
                                 ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.spell.feelthefire", this.func_70005_c_(), targetEntity.func_70005_c_());
                                 if (!player.field_71075_bZ.field_75098_d) {
                                    --stack.field_77994_a;
                                    if (stack.field_77994_a <= 0) {
                                       player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                                    }
                                 }
                              } else {
                                 ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.spell.failed", this.func_70005_c_(), targetEntity.func_70005_c_());
                              }
                           } else {
                              name = Witchery.Items.TAGLOCK_KIT.getBoundEntityDisplayName(stack, 1);
                              ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.spell.cannotfind", this.func_70005_c_(), name);
                           }
                        }
                     } else {
                        ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.spell.notliked", this.func_70005_c_());
                     }
                  } else {
                     ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.spell.toomuchpower", this.func_70005_c_());
                  }
               }
            }
         } else if (Witchery.Items.GENERIC.itemContractOwnership.isMatch(stack)) {
            if (!this.field_70170_p.field_72995_K) {
               EntityLivingBase boundEntity = ItemGeneralContract.getBoundEntity(this.field_70170_p, player, stack);
               if (boundEntity == player) {
                  int EXPERIENCE_NEEDED = true;
                  if (player.field_71068_ca < 25 && !player.field_71075_bZ.field_75098_d) {
                     SoundEffect.WITCHERY_MOB_IMP_LAUGH.playAtPlayer(this.field_70170_p, player, 0.5F, this.func_70647_i());
                     ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.contract.noxp", this.func_70005_c_());
                  } else {
                     if (!player.field_71075_bZ.field_75098_d) {
                        --stack.field_77994_a;
                        if (stack.field_77994_a <= 0) {
                           player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                        }
                     }

                     player.func_82242_a(-25);
                     this.func_70903_f(true);
                     TameableUtil.setOwner(this, player);
                     this.func_70624_b((EntityLivingBase)null);
                     this.func_70778_a((PathEntity)null);
                     this.homeX = (int)this.field_70165_t;
                     this.homeY = (int)this.field_70163_u;
                     this.homeZ = (int)this.field_70161_v;
                     this.func_110163_bv();
                     SoundEffect.WITCHERY_MOB_IMP_LAUGH.playAtPlayer(this.field_70170_p, player, 0.5F, this.func_70647_i());
                     ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.contract.deal", this.func_70005_c_());
                     this.func_94058_c(getDemonName(this.field_70146_Z));
                  }
               } else if (boundEntity != null) {
                  SoundEffect.WITCHERY_MOB_IMP_LAUGH.playAtPlayer(this.field_70170_p, player, 0.5F, this.func_70647_i());
                  ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.contract.notowners", this.func_70005_c_());
               } else {
                  SoundEffect.WITCHERY_MOB_IMP_LAUGH.playAtPlayer(this.field_70170_p, player, 0.5F, this.func_70647_i());
                  ChatUtil.sendTranslated(EnumChatFormatting.DARK_RED, player, "entity.witchery.imp.contract.unsigned", this.func_70005_c_());
               }
            }

            return true;
         }

         return super.func_70085_c(player);
      }
   }

   public EntityImp createChild(EntityAgeable par1EntityAgeable) {
      return null;
   }

   public boolean func_70878_b(EntityAnimal par1EntityAnimal) {
      return false;
   }

   protected boolean func_70692_ba() {
      return true;
   }

   private static String getDemonName(Random rand) {
      return rand.nextInt(5) == 0 ? DEMON_NAMES[rand.nextInt(DEMON_NAMES.length)] : DEMON_NAMES[rand.nextInt(DEMON_NAMES.length)] + " " + DEMON_NAMES[rand.nextInt(DEMON_NAMES.length)];
   }

   public double getHomeX() {
      return (double)this.homeX;
   }

   public double getHomeY() {
      return (double)this.homeY;
   }

   public double getHomeZ() {
      return (double)this.homeZ;
   }

   public double getHomeRange() {
      return 16.0D;
   }

   static {
      shinies.put((new ItemStack(Items.field_151045_i)).func_77973_b(), 8);
      shinies.put((new ItemStack(Items.field_151056_x)).func_77973_b(), 24);
      shinies.put((new ItemStack(Items.field_151012_L)).func_77973_b(), 16);
      shinies.put((new ItemStack(Items.field_151048_u)).func_77973_b(), 16);
      shinies.put((new ItemStack(Items.field_151047_v)).func_77973_b(), 8);
      shinies.put((new ItemStack(Items.field_151046_w)).func_77973_b(), 24);
      shinies.put((new ItemStack(Items.field_151166_bC)).func_77973_b(), 3);
      shinies.put((new ItemStack(Items.field_151043_k)).func_77973_b(), 1);
      shinies.put((new ItemStack(Items.field_151156_bN)).func_77973_b(), 16);
      shinies.put((new ItemStack(Items.field_151072_bj)).func_77973_b(), 1);
      shinies.put((new ItemStack(Items.field_151073_bk)).func_77973_b(), 4);
      shinies.put((new ItemStack(Items.field_151006_E)).func_77973_b(), 3);
      shinies.put((new ItemStack(Items.field_151010_B)).func_77973_b(), 2);
      shinies.put((new ItemStack(Items.field_151013_M)).func_77973_b(), 2);
      shinies.put((new ItemStack(Items.field_151011_C)).func_77973_b(), 1);
      shinies.put((new ItemStack(Items.field_151005_D)).func_77973_b(), 3);
      shinies.put((new ItemStack(Blocks.field_150340_R)).func_77973_b(), 9);
      shinies.put((new ItemStack(Blocks.field_150475_bE)).func_77973_b(), 27);
      shinies.put((new ItemStack(Blocks.field_150484_ah)).func_77973_b(), 72);
      shinies.put((new ItemStack(Blocks.field_150368_y)).func_77973_b(), 7);
      shinies.put((new ItemStack(Blocks.field_150451_bX)).func_77973_b(), 5);
      GIFT_DELAY_TICKS = (long)TimeUtil.minsToTicks(3);
      EXTRA_DROPS = new ItemStack[]{Witchery.Items.GENERIC.itemBatWool.createStack(5), Witchery.Items.GENERIC.itemDogTongue.createStack(5), Witchery.Items.GENERIC.itemToeOfFrog.createStack(2), Witchery.Items.GENERIC.itemOwletsWing.createStack(2), Witchery.Items.GENERIC.itemBranchEnt.createStack(1), Witchery.Items.GENERIC.itemInfernalBlood.createStack(2), Witchery.Items.GENERIC.itemCreeperHeart.createStack(2)};
      DEMON_NAMES = new String[]{"Ppaironael", "Aethon", "Tyrnak", "Beelzebuth", "Botis", "Moloch", "Taet", "Epnanaet", "Unonom", "Hexpemsazon", "Thayax", "Ethahoat", "Pruslas", "Ahtuxies", "Laripael", "Elxar", "Tarihimal", "Sapanolr", "Sahaminapiel", "Honed", "Oghmus", "Zedeson", "Halmaneop", "Nopoz", "Ekarnahox", "Sacuhatakael", "Ticos", "Arametheus", "Azmodaeus", "Larhepeis", "Topriraiz", "Rarahaimzah", "Tedrahamael", "Osaselael", "Phlegon", "Nelokhiel", "Haristum", "Zul", "Larhepeis", "Aamon", "Tramater", "Ehhbes", "Kra`an", "Quarax", "Hotesiatrem", "Surgat", "Nu`uhn", "Litedabh", "Unonom", "Bolenoz", "Hilopael", "Haristum", "Uhn", "Hiepacth", "Pemcapso", "Ankou", "Pundohien", "Koit", "Montobulus", "Amsaset", "Aropet", "Isnal", "Solael", "Exroh", "Sidragrosam", "Pnecamob", "Malashim", "Beelzebuth", "Ehohit", "Izatap", "Olon", "Assoaz", "Agalierept", "Krakus", "Umlaboor", "Aknrar", "Damaz", "Rhysus", "Pundohien", "Ba`al", "Rasuniolpas", "Anhoor", "Nyarlathotep", "Krakus", "Larhepeis", "Itakup", "Erdok", "Umlaboor", "Ezon", "Krakus", "Glassyalabolas", "Kra`an", "Ehnnat", "Terxor", "Asramel", "Tadal", "Arpzih", "Azmodaeus", "Henbolaron", "Rhysus"};
   }
}
