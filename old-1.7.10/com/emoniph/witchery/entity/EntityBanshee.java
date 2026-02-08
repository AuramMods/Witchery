package com.emoniph.witchery.entity;

import com.emoniph.witchery.item.ItemEarmuffs;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.TimeUtil;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityBanshee extends EntitySummonedUndead {
   public EntityBanshee(World par1World) {
      super(par1World);
      this.func_70661_as().func_75491_a(true);
      this.func_70661_as().func_75504_d(true);
      this.func_70661_as().func_75498_b(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, new EntityAIAttackOnCollide(this, EntityLivingBase.class, 0.3D, false));
      this.field_70714_bg.func_75776_a(3, new EntityAIWander(this, 1.0D));
      this.field_70714_bg.func_75776_a(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(5, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAIHurtByTarget(this, true));
      this.field_70715_bh.func_75776_a(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111265_b).func_111128_a(40.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.2D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(0.0D);
   }

   protected void func_70088_a() {
      super.func_70088_a();
   }

   protected boolean func_70650_aV() {
      return true;
   }

   public void func_70636_d() {
      super.func_70636_d();
      boolean startedScreaming = false;
      if (!this.field_70170_p.field_72995_K && (TimeUtil.secondsElapsed(5, (long)this.field_70173_aa) || this.isScreaming() && TimeUtil.ticksElapsed(20, (long)this.field_70173_aa))) {
         double RADIUS = 6.0D;
         double RADIUS_SQ = 36.0D;
         AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(this.field_70165_t - 6.0D, this.field_70163_u - 6.0D, this.field_70161_v - 6.0D, this.field_70165_t + 6.0D, this.field_70163_u + 6.0D, this.field_70161_v + 6.0D);
         List players = this.field_70170_p.func_72872_a(EntityLivingBase.class, bounds);
         boolean playersFound = false;
         Iterator i$ = players.iterator();

         label60:
         while(true) {
            EntityLivingBase player;
            do {
               do {
                  do {
                     if (!i$.hasNext()) {
                        if (!playersFound && this.isScreaming()) {
                           this.setScreaming(false);
                        }
                        break label60;
                     }

                     Object obj = i$.next();
                     player = (EntityLivingBase)obj;
                  } while(!(this.func_70068_e(player) <= 36.0D));
               } while(player != this.func_70638_az() && player != this.field_70789_a && !(player instanceof EntityPlayer));

               playersFound = true;
               if (!this.isScreaming()) {
                  this.setScreaming(true);
                  startedScreaming = true;
               }
            } while(player instanceof EntityPlayer && ItemEarmuffs.isHelmWorn((EntityPlayer)player));

            float maxHealth = player.func_110138_aP();
            EntityUtil.touchOfDeath(player, this, Math.max(0.1F * maxHealth, 1.0F));
         }
      }

      if ((startedScreaming || TimeUtil.secondsElapsed(3, (long)this.field_70173_aa)) && this.isScreaming()) {
         this.func_85030_a("witchery:mob.banshee.banshee_scream", 1.0F, this.field_70170_p.field_73012_v.nextFloat() * 0.3F + 0.7F);
      }

   }

   public void func_70071_h_() {
      super.func_70071_h_();
   }

   public boolean func_70652_k(Entity par1Entity) {
      boolean flag = super.func_70652_k(par1Entity);
      return flag;
   }

   protected String func_70639_aQ() {
      return null;
   }

   protected String func_70621_aR() {
      return "witchery:mob.spectre.spectre_hit";
   }

   protected String func_70673_aS() {
      return "witchery:mob.spectre.spectre_hit";
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.banshee.name");
   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      super.func_70014_b(par1NBTTagCompound);
   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      super.func_70037_a(par1NBTTagCompound);
   }

   public IEntityLivingData func_110161_a(IEntityLivingData par1EntityLivingData) {
      Object par1EntityLivingData1 = super.func_110161_a(par1EntityLivingData);
      return (IEntityLivingData)par1EntityLivingData1;
   }
}
