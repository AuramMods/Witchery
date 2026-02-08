package com.emoniph.witchery.entity;

import com.emoniph.witchery.ritual.rites.RiteProtectionCircleRepulsive;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.TimeUtil;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityDarkMark extends EntityLiving {
   private long ticksAlive = 0L;

   public EntityDarkMark(World world) {
      super(world);
      this.field_70178_ae = true;
      this.func_70105_a(2.0F, 2.0F);
      this.field_70714_bg.func_75776_a(1, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
   }

   protected void func_70088_a() {
      super.func_70088_a();
   }

   protected int func_70682_h(int air) {
      return air;
   }

   protected float func_70599_aP() {
      return 0.8F;
   }

   protected float func_70647_i() {
      return 1.0F;
   }

   public int func_70627_aG() {
      return 80;
   }

   protected String func_70639_aQ() {
      return "witchery:mob.torment.laugh";
   }

   protected String func_70621_aR() {
      return null;
   }

   protected String func_70673_aS() {
      return null;
   }

   public boolean func_70104_M() {
      return false;
   }

   protected void func_82167_n(Entity par1Entity) {
   }

   protected void func_85033_bc() {
   }

   protected boolean func_70650_aV() {
      return true;
   }

   public void func_70636_d() {
      super.func_70636_d();
      this.ticksAlive = Math.max((long)this.field_70173_aa, ++this.ticksAlive);
      if (this.ticksAlive > (long)TimeUtil.minsToTicks(5)) {
         if (!this.field_70170_p.field_72995_K) {
            this.func_70106_y();
         }
      } else if (this.field_70170_p.field_72995_K && TimeUtil.ticksElapsed(4, this.ticksAlive)) {
         double radius = 10.0D;
         AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(this.field_70165_t - 10.0D, 1.0D, this.field_70161_v - 10.0D, this.field_70165_t + 10.0D, 255.0D, this.field_70161_v + 10.0D);
         List list = this.field_70170_p.func_72872_a(EntityCreature.class, bounds);
         Iterator iterator = list.iterator();

         while(iterator.hasNext()) {
            Entity entity = (Entity)iterator.next();
            if (Coord.distance(entity.field_70165_t, 1.0D, entity.field_70161_v, this.field_70165_t, 1.0D, this.field_70161_v) <= 10.0D) {
               RiteProtectionCircleRepulsive.push(this.field_70170_p, entity, this.field_70165_t, entity.field_70163_u, this.field_70161_v);
            }
         }
      }

   }

   public void func_70071_h_() {
      super.func_70071_h_();
      this.field_70181_x = 0.0D;
      if (this.field_70170_p.field_72995_K) {
         for(int i = 0; i < 5; ++i) {
            this.field_70170_p.func_72869_a(ParticleEffect.LARGE_SMOKE.toString(), this.field_70165_t - 1.4D + this.field_70170_p.field_73012_v.nextDouble() * 2.8D, this.field_70163_u + this.field_70170_p.field_73012_v.nextDouble() * 2.0D, this.field_70161_v - 1.4D + this.field_70170_p.field_73012_v.nextDouble() * 2.8D, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   protected void func_70628_a(boolean par1, int par2) {
   }

   protected boolean func_70041_e_() {
      return false;
   }

   protected void func_70069_a(float par1) {
   }

   protected void func_70064_a(double par1, boolean par3) {
   }

   public boolean func_145773_az() {
      return true;
   }

   public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
      return false;
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      this.ticksAlive = nbtRoot.func_74763_f("WITCTicksAlive");
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74772_a("WITCTicksAlive", this.ticksAlive);
   }
}
