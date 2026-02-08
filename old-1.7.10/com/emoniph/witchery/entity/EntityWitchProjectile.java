package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.blocks.BlockCircleGlyph;
import com.emoniph.witchery.brewing.potions.PotionEnslaved;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.network.PacketPushTarget;
import com.emoniph.witchery.util.BlockProtect;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.EarthItems;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class EntityWitchProjectile extends EntityThrowable {
   private int damageValue;
   private boolean skipFX = false;
   private static final String DAMAGE_VALUE_KEY = "damageValue";

   public EntityWitchProjectile(World world) {
      super(world);
   }

   public EntityWitchProjectile(World world, EntityLivingBase entityLiving, ItemGeneral.SubItem generalSubItem) {
      super(world, entityLiving);
      this.setDamageValue(generalSubItem.damageValue);
   }

   public EntityWitchProjectile(World world, double posX, double posY, double posZ, ItemGeneral.SubItem generalSubItem) {
      super(world, posX, posY, posZ);
      this.setDamageValue(generalSubItem.damageValue);
   }

   protected void func_70088_a() {
      this.field_70180_af.func_75682_a(6, 0);
      super.func_70088_a();
   }

   public void setDamageValue(int damageValue) {
      this.damageValue = damageValue;
      this.func_70096_w().func_75692_b(6, damageValue);
   }

   public int getDamageValue() {
      return this.func_70096_w().func_75679_c(6);
   }

   public boolean isPotion() {
      return Witchery.Items.GENERIC.subItems.get(this.damageValue) instanceof ItemGeneral.Brew || Witchery.Items.GENERIC.itemQuicklime.damageValue == this.damageValue;
   }

   protected float func_70185_h() {
      return this.isPotion() ? 0.05F : super.func_70185_h();
   }

   protected float func_70182_d() {
      return this.isPotion() ? 0.5F : super.func_70182_d();
   }

   protected float func_70183_g() {
      return this.isPotion() ? -20.0F : super.func_70183_g();
   }

   protected void func_70184_a(MovingObjectPosition mop) {
      if (!this.field_70170_p.field_72995_K && mop != null) {
         boolean enhanced = false;
         EntityLivingBase thrower = this.func_85052_h();
         if (thrower != null && thrower instanceof EntityPlayer) {
            enhanced = Familiar.hasActiveBrewMasteryFamiliar((EntityPlayer)thrower);
         }

         this.skipFX = false;
         if (Witchery.Items.GENERIC.itemBrewOfVines.damageValue == this.damageValue) {
            this.impactVines(mop, enhanced);
         } else if (Witchery.Items.GENERIC.itemBrewOfThorns.damageValue == this.damageValue) {
            this.impactThorns(mop, enhanced);
         } else if (Witchery.Items.GENERIC.itemBrewOfWebs.damageValue == this.damageValue) {
            this.impactWebBig(mop, enhanced);
         } else if (Witchery.Items.GENERIC.itemBrewOfInk.damageValue == this.damageValue) {
            this.impactInk(mop, enhanced);
         } else if (Witchery.Items.GENERIC.itemBrewOfWasting.damageValue == this.damageValue) {
            this.impactWasting(mop, enhanced);
         } else if (Witchery.Items.GENERIC.itemBrewOfSprouting.damageValue == this.damageValue) {
            this.impactSprout(mop, enhanced);
         } else if (Witchery.Items.GENERIC.itemBrewOfErosion.damageValue == this.damageValue) {
            this.impactErosion(mop, enhanced);
         } else if (Witchery.Items.GENERIC.itemBrewOfLove.damageValue == this.damageValue) {
            this.impactLove(mop, enhanced);
         } else if (Witchery.Items.GENERIC.itemWeb.damageValue == this.damageValue) {
            this.impactWebSmall(mop);
            this.skipFX = true;
         } else if (Witchery.Items.GENERIC.itemRock.damageValue == this.damageValue) {
            this.impactRock(mop);
            this.skipFX = true;
         } else if (Witchery.Items.GENERIC.itemBrewOfRaising.damageValue == this.damageValue) {
            this.impactRaising(mop);
         } else if (Witchery.Items.GENERIC.itemQuicklime.damageValue == this.damageValue) {
            this.impactQuicklime(mop);
         } else if (Witchery.Items.GENERIC.itemBrewOfIce.damageValue == this.damageValue) {
            this.impactIce(mop);
         } else if (Witchery.Items.GENERIC.itemBrewOfFrogsTongue.damageValue == this.damageValue) {
            this.impactFrogsTongue(mop, false);
         } else if (Witchery.Items.GENERIC.itemBrewOfCursedLeaping.damageValue == this.damageValue) {
            this.impactLeaping(mop, false);
         } else if (Witchery.Items.GENERIC.itemBrewOfHitchcock.damageValue == this.damageValue) {
            this.impactHitchcock(mop);
         } else if (Witchery.Items.GENERIC.itemBrewOfInfection.damageValue == this.damageValue) {
            this.impactInfection(mop, enhanced);
         } else if (Witchery.Items.GENERIC.itemBrewOfBats.damageValue == this.damageValue) {
            this.impactBats(mop, enhanced);
         } else {
            ItemGeneral.SubItem item = (ItemGeneral.SubItem)Witchery.Items.GENERIC.subItems.get(this.damageValue);
            if (item instanceof ItemGeneral.Brew) {
               ItemGeneral.Brew brew = (ItemGeneral.Brew)item;
               ItemGeneral.Brew.BrewResult result = brew.onImpact(this.field_70170_p, thrower, mop, enhanced, this.field_70165_t, this.field_70163_u, this.field_70161_v, this.field_70121_D);
               if (result == ItemGeneral.Brew.BrewResult.DROP_ITEM) {
                  EntityItem itemEntity = null;
                  if (mop != null) {
                     ItemStack newBrewStack = brew.createStack();
                     switch(mop.field_72313_a) {
                     case BLOCK:
                        itemEntity = new EntityItem(this.field_70170_p, (double)mop.field_72311_b + 0.5D, (double)(mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : 1)) + 0.5D, (double)mop.field_72309_d + 0.5D, newBrewStack);
                        break;
                     case ENTITY:
                        itemEntity = new EntityItem(this.field_70170_p, mop.field_72308_g.field_70165_t, mop.field_72308_g.field_70163_u, mop.field_72308_g.field_70161_v, newBrewStack);
                     }
                  }

                  this.skipFX = true;
                  if (itemEntity != null) {
                     this.field_70170_p.func_72838_d(itemEntity);
                  }
               } else {
                  this.skipFX = result == ItemGeneral.Brew.BrewResult.HIDE_EFFECT;
               }
            }
         }

         if (!this.skipFX) {
            this.field_70170_p.func_72926_e(2002, (int)Math.round(this.field_70165_t), (int)Math.round(this.field_70163_u), (int)Math.round(this.field_70161_v), 2);
         }
      }

      this.func_70106_y();
   }

   private void impactBats(MovingObjectPosition mop, boolean enhanced) {
      switch(mop.field_72313_a) {
      case BLOCK:
         this.explodeBats(this.field_70170_p, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, mop.field_72310_e, enhanced);
         break;
      case ENTITY:
         int x = MathHelper.func_76128_c(mop.field_72308_g.field_70165_t);
         int y = MathHelper.func_76128_c(mop.field_72308_g.field_70163_u);
         int z = MathHelper.func_76128_c(mop.field_72308_g.field_70161_v);
         this.explodeBats(this.field_70170_p, x, y, z, -1, enhanced);
      }

      double RADIUS = enhanced ? 4.0D : 3.0D;
      AxisAlignedBB axisalignedbb = this.field_70121_D.func_72314_b(RADIUS, 2.0D, RADIUS);
      List list1 = this.field_70170_p.func_72872_a(EntityLivingBase.class, axisalignedbb);
      if (list1 != null && !list1.isEmpty()) {
         Iterator iterator = list1.iterator();

         while(iterator.hasNext()) {
            EntityLivingBase entitylivingbase = (EntityLivingBase)iterator.next();
            double d0 = entitylivingbase.func_70092_e(this.field_70165_t, this.field_70163_u, this.field_70161_v);
            if (d0 < RADIUS * RADIUS) {
               double d1 = 1.0D - Math.sqrt(d0) / RADIUS;
               if (entitylivingbase == mop.field_72308_g) {
                  d1 = 1.0D;
               }

               int j = (int)(d1 * 100.0D + 0.5D);
               entitylivingbase.func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, j, 5));
               if (entitylivingbase instanceof EntityLiving) {
                  EntityUtil.dropAttackTarget((EntityLiving)entitylivingbase);
               }
            }
         }
      }

   }

   private void explodeBats(World world, int posX, int posY, int posZ, int side, boolean enhanced) {
      int x = posX + (side == 4 ? -1 : (side == 5 ? 1 : 0));
      int z = posZ + (side == 2 ? -1 : (side == 3 ? 1 : 0));
      int y = posY + (side == 0 ? -1 : (side == 1 ? 1 : 0));
      if (side == 1 && !world.func_147439_a(x, posY, z).func_149688_o().func_76220_a()) {
         --y;
      }

      int NUM_BATS = enhanced ? 14 : 10;

      for(int i = 0; i < NUM_BATS; ++i) {
         EntityBat bat = new EntityBat(world);
         NBTTagCompound nbtBat = bat.getEntityData();
         nbtBat.func_74757_a("WITCNoDrops", true);
         bat.func_70012_b((double)x, (double)y, (double)z, 0.0F, 0.0F);
         this.field_70170_p.func_72838_d(bat);
      }

      ParticleEffect.LARGE_EXPLODE.send(SoundEffect.MOB_ENDERMEN_PORTAL, world, 0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z, 3.0D, 3.0D, 16);
   }

   private void impactInfection(MovingObjectPosition mop, boolean enhanced) {
      if (mop.field_72313_a == MovingObjectType.BLOCK) {
         Block blockID = this.field_70170_p.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d);
         int blockMeta = this.field_70170_p.func_72805_g(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d);
         if ((blockID == Blocks.field_150348_b || blockID == Blocks.field_150347_e || blockID == Blocks.field_150417_aV && blockMeta == 0) && BlockProtect.canBreak(mop.field_72311_b, mop.field_72309_d, mop.field_72312_c, this.field_70170_p)) {
            if (blockID == Blocks.field_150348_b) {
               this.field_70170_p.func_147465_d(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, Blocks.field_150418_aU, 0, 3);
            } else if (blockID == Blocks.field_150347_e) {
               this.field_70170_p.func_147465_d(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, Blocks.field_150418_aU, 1, 3);
            } else if (blockID == Blocks.field_150417_aV) {
               this.field_70170_p.func_147465_d(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, Blocks.field_150418_aU, 2, 3);
            }

            return;
         }
      } else if (mop.field_72313_a == MovingObjectType.ENTITY && mop.field_72308_g instanceof EntityLivingBase) {
         EntityLivingBase entity = (EntityLivingBase)mop.field_72308_g;
         if (entity instanceof EntityVillager) {
            EntityZombie entityzombie = new EntityZombie(this.field_70170_p);
            entityzombie.func_82149_j(entity);
            this.field_70170_p.func_72900_e(entity);
            entityzombie.func_110161_a((IEntityLivingData)null);
            entityzombie.func_82229_g(true);
            if (entity.func_70631_g_()) {
               entityzombie.func_82227_f(true);
            }

            this.field_70170_p.func_72838_d(entityzombie);
            this.field_70170_p.func_72889_a((EntityPlayer)null, 1016, (int)entityzombie.field_70165_t, (int)entityzombie.field_70163_u, (int)entityzombie.field_70161_v, 0);
         } else {
            float WORM_DAMAGE = enhanced ? 4.0F : 1.0F;
            entity.func_70097_a(DamageSource.func_76356_a(entity, this.func_85052_h()), WORM_DAMAGE);
            entity.func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, 100, 8));
         }

         return;
      }

      EntityItem itemEntity = null;
      if (mop != null) {
         ItemStack newBrewStack = Witchery.Items.GENERIC.itemBrewOfInfection.createStack();
         switch(mop.field_72313_a) {
         case BLOCK:
            itemEntity = new EntityItem(this.field_70170_p, (double)mop.field_72311_b + 0.5D, (double)(mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : 1)) + 0.5D, (double)mop.field_72309_d + 0.5D, newBrewStack);
            break;
         case ENTITY:
            itemEntity = new EntityItem(this.field_70170_p, mop.field_72308_g.field_70165_t, mop.field_72308_g.field_70163_u, mop.field_72308_g.field_70161_v, newBrewStack);
         }
      }

      this.skipFX = true;
      if (itemEntity != null) {
         this.field_70170_p.func_72838_d(itemEntity);
      }

   }

   private void impactHitchcock(MovingObjectPosition mop) {
      if (mop.field_72313_a == MovingObjectType.ENTITY && mop.field_72308_g instanceof EntityLivingBase) {
         EntityLivingBase victim = (EntityLivingBase)mop.field_72308_g;
         int BIRDS = this.field_70170_p.field_73012_v.nextInt(2) + 3;

         for(int i = 0; i < BIRDS; ++i) {
            EntityOwl owl = new EntityOwl(this.field_70170_p);
            owl.func_70012_b(victim.field_70165_t - 2.0D + (double)this.field_70170_p.field_73012_v.nextInt(5), victim.field_70163_u + (double)victim.field_70131_O + 1.0D + (double)this.field_70170_p.field_73012_v.nextInt(2), victim.field_70161_v - 2.0D + (double)this.field_70170_p.field_73012_v.nextInt(5), 0.0F, 0.0F);
            owl.func_70624_b(victim);
            owl.setTimeToLive(400);
            this.field_70170_p.func_72838_d(owl);
            ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, owl, 1.0D, 1.0D, 16);
         }
      } else {
         EntityItem itemEntity = null;
         if (mop != null) {
            ItemStack newBrewStack = Witchery.Items.GENERIC.itemBrewOfHitchcock.createStack();
            switch(mop.field_72313_a) {
            case BLOCK:
               itemEntity = new EntityItem(this.field_70170_p, (double)mop.field_72311_b + 0.5D, (double)(mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : 1)) + 0.5D, (double)mop.field_72309_d + 0.5D, newBrewStack);
               break;
            case ENTITY:
               itemEntity = new EntityItem(this.field_70170_p, mop.field_72308_g.field_70165_t, mop.field_72308_g.field_70163_u, mop.field_72308_g.field_70161_v, newBrewStack);
            }
         }

         this.skipFX = true;
         if (itemEntity != null) {
            this.field_70170_p.func_72838_d(itemEntity);
         }
      }

   }

   private void impactLeaping(MovingObjectPosition mop, boolean enhanced) {
      Entity livingEntity = mop.field_72308_g;
      double RADIUS = enhanced ? 6.0D : 5.0D;
      AxisAlignedBB axisalignedbb = this.field_70121_D.func_72314_b(RADIUS, 2.0D, RADIUS);
      List list1 = this.field_70170_p.func_72872_a(EntityLivingBase.class, axisalignedbb);
      if (list1 != null && !list1.isEmpty()) {
         Iterator iterator = list1.iterator();

         while(iterator.hasNext()) {
            EntityLivingBase entitylivingbase = (EntityLivingBase)iterator.next();
            double d0 = entitylivingbase.func_70092_e(this.field_70165_t, this.field_70163_u, this.field_70161_v);
            if (d0 < RADIUS * RADIUS) {
               double d1 = 1.0D - 0.5D * (Math.sqrt(d0) / RADIUS / 2.0D);
               if (entitylivingbase == livingEntity) {
                  d1 = 1.0D;
               }

               int j = (int)(d1 * 400.0D + 0.5D);
               double LEAP = d1 * 1.6D;
               entitylivingbase.func_70690_d(new PotionEffect(Potion.field_76430_j.field_76415_H, 200, 3));
               if (entitylivingbase instanceof EntityPlayer) {
                  Witchery.packetPipeline.sendTo((IMessage)(new PacketPushTarget(entitylivingbase.field_70159_w, entitylivingbase.field_70181_x + LEAP, entitylivingbase.field_70179_y)), (EntityPlayer)((EntityPlayer)entitylivingbase));
               } else {
                  entitylivingbase.field_70181_x += LEAP;
               }
            }
         }
      }

   }

   private void impactFrogsTongue(MovingObjectPosition mop, boolean enhanced) {
      if (!this.field_70170_p.field_72995_K && this.func_85052_h() != null) {
         double RADIUS = enhanced ? 5.0D : 4.0D;
         double RADIUS_SQ = RADIUS * RADIUS;
         EntityLivingBase thrower = this.func_85052_h();
         boolean pulled = false;
         AxisAlignedBB axisalignedbb = this.field_70121_D.func_72314_b(RADIUS, 2.0D, RADIUS);
         List entityLivingList = this.field_70170_p.func_72872_a(EntityLivingBase.class, axisalignedbb);
         if (entityLivingList != null && !entityLivingList.isEmpty()) {
            Iterator iterator = entityLivingList.iterator();

            while(iterator.hasNext()) {
               EntityLivingBase livingEntity = (EntityLivingBase)iterator.next();
               double distanceSq = livingEntity.func_70092_e(this.field_70165_t, this.field_70163_u, this.field_70161_v);
               if (distanceSq < RADIUS_SQ && livingEntity != this.func_85052_h()) {
                  this.pull(this.field_70170_p, livingEntity, thrower.field_70165_t, thrower.field_70163_u, thrower.field_70161_v, 0.05D, 0.0D);
               }
            }
         }
      }

   }

   private void pull(World world, Entity entity, double posX, double posY, double posZ, double dy, double yy) {
      if (!(entity instanceof EntityDragon) && !(entity instanceof EntityHornedHuntsman)) {
         double d = posX - entity.field_70165_t;
         double d1 = posY - entity.field_70163_u;
         double d2 = posZ - entity.field_70161_v;
         float distance = MathHelper.func_76133_a(d * d + d1 * d1 + d2 * d2);
         float f2 = 0.1F + (float)dy;
         double mx = d / (double)distance * (double)f2 * (double)distance;
         double my = yy == 0.0D ? 0.4D : d1 / (double)distance * (double)distance * 0.2D + 0.2D + yy;
         double mz = d2 / (double)distance * (double)f2 * (double)distance;
         if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase)entity).func_70690_d(new PotionEffect(Potion.field_76430_j.field_76415_H, 20, 1));
         }

         if (entity instanceof EntityPlayer) {
            Witchery.packetPipeline.sendTo((IMessage)(new PacketPushTarget(mx, my, mz)), (EntityPlayer)((EntityPlayer)entity));
         } else {
            entity.field_70159_w = mx;
            entity.field_70181_x = my;
            entity.field_70179_y = mz;
         }

      }
   }

   private void impactIce(MovingObjectPosition mop) {
      int dx;
      int dy;
      int dz;
      switch(mop.field_72313_a) {
      case BLOCK:
         int FREEZE_RANGE = true;
         if (this.field_70170_p.func_147439_a(mop.field_72311_b + 1, mop.field_72312_c, mop.field_72309_d).func_149688_o() == Material.field_151586_h || this.field_70170_p.func_147439_a(mop.field_72311_b - 1, mop.field_72312_c, mop.field_72309_d).func_149688_o() == Material.field_151586_h || this.field_70170_p.func_147439_a(mop.field_72311_b, mop.field_72312_c + 1, mop.field_72309_d).func_149688_o() == Material.field_151586_h || this.field_70170_p.func_147439_a(mop.field_72311_b, mop.field_72312_c - 1, mop.field_72309_d).func_149688_o() == Material.field_151586_h || this.field_70170_p.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d + 1).func_149688_o() == Material.field_151586_h || this.field_70170_p.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d - 1).func_149688_o() == Material.field_151586_h) {
            this.freezeSurroundingWater(this.field_70170_p, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, 3);
            return;
         }

         int SHIELD_HEIGHT = true;
         if (mop.field_72310_e == 1) {
            explodeIceShield(this.field_70170_p, this.func_85052_h(), mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, 3);
            return;
         }

         if (mop.field_72310_e != 0) {
            int b0 = false;
            switch(mop.field_72310_e) {
            case 0:
            case 1:
               b0 = false;
               break;
            case 2:
            case 3:
               b0 = true;
               break;
            case 4:
            case 5:
               b0 = true;
            }

            dx = mop.field_72310_e == 5 ? 1 : (mop.field_72310_e == 4 ? -1 : 0);
            dy = mop.field_72310_e == 0 ? -1 : (mop.field_72310_e == 1 ? 1 : 0);
            dz = mop.field_72310_e == 3 ? 1 : (mop.field_72310_e == 2 ? -1 : 0);
            explodeIceShield(this.field_70170_p, this.func_85052_h(), mop.field_72311_b + dx, mop.field_72312_c + dy, mop.field_72309_d + dz, 3);
            return;
         }

         EntityItem itemEntity = null;
         if (mop != null) {
            ItemStack newBrewStack = Witchery.Items.GENERIC.itemBrewOfIce.createStack();
            switch(mop.field_72313_a) {
            case BLOCK:
               itemEntity = new EntityItem(this.field_70170_p, (double)mop.field_72311_b + 0.5D, (double)(mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : 1)) + 0.5D, (double)mop.field_72309_d + 0.5D, newBrewStack);
               break;
            case ENTITY:
               itemEntity = new EntityItem(this.field_70170_p, mop.field_72308_g.field_70165_t, mop.field_72308_g.field_70163_u, mop.field_72308_g.field_70161_v, newBrewStack);
            }
         }

         this.skipFX = true;
         if (itemEntity != null) {
            this.field_70170_p.func_72838_d(itemEntity);
         }
         break;
      case ENTITY:
         dx = (int)Math.round(mop.field_72308_g.field_70165_t);
         dy = MathHelper.func_76128_c(mop.field_72308_g.field_70163_u);
         dz = (int)Math.round(mop.field_72308_g.field_70161_v);
         explodeIceBlock(this.field_70170_p, dx, dy, dz, -1, mop.field_72308_g);
      }

   }

   private void freezeSurroundingWater(World world, int x, int y, int z, int x0, int y0, int z0, int range) {
      if (Math.abs(x0 - x) < range && Math.abs(y0 - y) < range && Math.abs(z0 - z) < range) {
         if (this.freezeWater(world, x + 1, y, z)) {
            this.freezeSurroundingWater(world, x + 1, y, z, x0, y0, z0, range);
         }

         if (this.freezeWater(world, x - 1, y, z)) {
            this.freezeSurroundingWater(world, x - 1, y, z, x0, y0, z0, range);
         }

         if (this.freezeWater(world, x, y, z + 1)) {
            this.freezeSurroundingWater(world, x, y, z + 1, x0, y0, z0, range);
         }

         if (this.freezeWater(world, x, y, z - 1)) {
            this.freezeSurroundingWater(world, x, y, z - 1, x0, y0, z0, range);
         }

         if (this.freezeWater(world, x, y + 1, z)) {
            this.freezeSurroundingWater(world, x, y + 1, z, x0, y0, z0, range);
         }

         if (this.freezeWater(world, x, y - 1, z)) {
            this.freezeSurroundingWater(world, x, y - 1, z, x0, y0, z0, range);
         }

      }
   }

   private boolean freezeWater(World world, int x, int y, int z) {
      if (world.func_147439_a(x, y, z).func_149688_o() == Material.field_151586_h) {
         world.func_147449_b(x, y, z, Blocks.field_150432_aD);
         return true;
      } else {
         return false;
      }
   }

   public static void explodeIceBlock(World world, int posX, int posY, int posZ, int side, Entity entity) {
      int x = posX + (side == 4 ? -1 : (side == 5 ? 1 : 0));
      int z = posZ + (side == 2 ? -1 : (side == 3 ? 1 : 0));
      int y = posY + (side == 0 ? -1 : (side == 1 ? 1 : 0)) - 1;
      if (side == 1 && !world.func_147439_a(x, posY, z).func_149688_o().func_76220_a()) {
         --y;
      }

      Block block = Blocks.field_150432_aD;
      boolean resistent = entity instanceof EntityDemon || entity instanceof EntityBlaze || entity instanceof EntityDragon || entity instanceof EntityHornedHuntsman || entity instanceof EntityEnt || entity instanceof EntityWither || entity instanceof EntityIronGolem;
      if (resistent) {
         setBlockIfNotSolid(world, x, y + 1, z, Blocks.field_150358_i);
      } else {
         int HEIGHT = resistent ? 2 : 4;

         for(int i = 0; i < HEIGHT; ++i) {
            setBlockIfNotSolid(world, x - 2, y + i, z - 1, block);
            setBlockIfNotSolid(world, x - 2, y + i, z, block);
            setBlockIfNotSolid(world, x - 1, y + i, z + 1, block);
            setBlockIfNotSolid(world, x, y + i, z + 1, block);
            setBlockIfNotSolid(world, x + 1, y + i, z, block);
            setBlockIfNotSolid(world, x + 1, y + i, z - 1, block);
            setBlockIfNotSolid(world, x, y + i, z - 2, block);
            setBlockIfNotSolid(world, x - 1, y + i, z - 2, block);
            setBlockIfNotSolid(world, x - 2, y + i, z - 2, block);
            setBlockIfNotSolid(world, x - 2, y + i, z + 1, block);
            setBlockIfNotSolid(world, x + 1, y + i, z + 1, block);
            setBlockIfNotSolid(world, x + 1, y + i, z - 2, block);
         }

         setBlockIfNotSolid(world, x, y, z, block);
         if (!resistent) {
            setBlockIfNotSolid(world, x, y + HEIGHT - 1, z, block);
            setBlockIfNotSolid(world, x - 1, y + HEIGHT - 1, z - 1, block);
            setBlockIfNotSolid(world, x - 1, y + HEIGHT - 1, z, block);
            setBlockIfNotSolid(world, x, y + HEIGHT - 1, z - 1, block);
         }

         if (entity instanceof EntityCreeper) {
            EntityCreeper creeper = (EntityCreeper)entity;
            boolean flag = world.func_82736_K().func_82766_b("mobGriefing");
            if (creeper.func_70830_n()) {
               world.func_72876_a(creeper, creeper.field_70165_t, creeper.field_70163_u, creeper.field_70161_v, 6.0F, flag);
            } else {
               world.func_72876_a(creeper, creeper.field_70165_t, creeper.field_70163_u, creeper.field_70161_v, 3.0F, flag);
            }

            creeper.func_70106_y();
         }
      }

   }

   public static void explodeIceShield(World world, EntityLivingBase player, int posX, int posY, int posZ, int height) {
      double f1 = player != null ? (double)MathHelper.func_76134_b(-player.field_70177_z * 0.017453292F - 3.1415927F) : 0.0D;
      double f2 = player != null ? (double)MathHelper.func_76126_a(-player.field_70177_z * 0.017453292F - 3.1415927F) : 0.0D;
      Vec3 loc = Vec3.func_72443_a(f2, 0.0D, f1);
      if (!world.func_147439_a(posX, posY, posZ).func_149688_o().func_76220_a()) {
         --posY;
      }

      explodeIceColumn(world, posX, posY + 1, posZ, height);
      loc.func_72442_b((float)Math.toRadians(90.0D));
      int newX = MathHelper.func_76128_c((double)posX + 0.5D + loc.field_72450_a * 1.0D);
      int newZ = MathHelper.func_76128_c((double)posZ + 0.5D + loc.field_72449_c * 1.0D);
      explodeIceColumn(world, newX, posY + 1, newZ, height);
      loc.func_72442_b((float)Math.toRadians(180.0D));
      newX = MathHelper.func_76128_c((double)posX + 0.5D + loc.field_72450_a * 1.0D);
      newZ = MathHelper.func_76128_c((double)posZ + 0.5D + loc.field_72449_c * 1.0D);
      explodeIceColumn(world, newX, posY + 1, newZ, height);
   }

   public static void explodeIceColumn(World world, int posX, int posY, int posZ, int height) {
      for(int offsetPosY = posY; offsetPosY < posY + height; ++offsetPosY) {
         setBlockIfNotSolid(world, posX, offsetPosY, posZ, Blocks.field_150432_aD);
      }

   }

   private void impactLove(MovingObjectPosition mop, boolean enhanced) {
      double RADIUS = enhanced ? 5.0D : 4.0D;
      AxisAlignedBB axisalignedbb = this.field_70121_D.func_72314_b(RADIUS, 2.0D, RADIUS);
      List list1 = this.field_70170_p.func_72872_a(EntityLiving.class, axisalignedbb);
      if (list1 != null && !list1.isEmpty() && !this.field_70170_p.field_72995_K) {
         EntityLivingBase entityThrower = this.func_85052_h();
         EntityPlayer thrower = entityThrower != null && entityThrower instanceof EntityPlayer ? (EntityPlayer)entityThrower : null;
         Iterator iterator = list1.iterator();
         ArrayList<EntityVillager> villagers = new ArrayList();
         ArrayList zombies = new ArrayList();

         while(iterator.hasNext()) {
            EntityLiving entitylivingbase = (EntityLiving)iterator.next();
            double d0 = entitylivingbase.func_70092_e(this.field_70165_t, this.field_70163_u, this.field_70161_v);
            if (d0 < RADIUS * RADIUS) {
               double d1 = 1.0D - Math.sqrt(d0) / RADIUS;
               if (entitylivingbase == mop.field_72308_g) {
                  d1 = 1.0D;
               }

               int j = (int)(d1 * 400.0D + 0.5D);
               if (entitylivingbase instanceof EntityAnimal) {
                  EntityAnimal animal = (EntityAnimal)entitylivingbase;
                  if (animal.func_70874_b() >= 0) {
                     animal.func_70873_a(0);
                     animal.func_146082_f((EntityPlayer)null);
                  }
               } else if (entitylivingbase instanceof EntityVillager) {
                  EntityVillager villager = (EntityVillager)entitylivingbase;
                  if (villager.func_70874_b() >= 0) {
                     villagers.add(villager);
                  }
               } else if (entitylivingbase instanceof EntityZombie) {
                  EntityZombie zombie = (EntityZombie)entitylivingbase;
                  if (!zombie.func_70631_g_() && thrower != null) {
                     NBTTagCompound nbt = zombie.getEntityData();
                     if (PotionEnslaved.isMobEnslavedBy(zombie, thrower)) {
                        zombies.add(zombie);
                     }
                  }
               }
            }
         }

         int var20 = 10;

         while(villagers.size() > 1 && var20-- > 0) {
            EntityVillager villager = (EntityVillager)villagers.get(0);
            EntityVillager mate = (EntityVillager)villagers.get(1);
            villager.func_70107_b(mate.field_70165_t, mate.field_70163_u, mate.field_70161_v);
            ParticleEffect.HEART.send(SoundEffect.NONE, mate, 1.0D, 2.0D, 8);
            this.giveBirth(villager, mate);
            villagers.remove(0);
            villagers.remove(0);
         }

         var20 = 10;

         while(zombies.size() > 1 && var20-- > 0) {
            EntityZombie zombie = (EntityZombie)zombies.get(0);
            EntityZombie mate = (EntityZombie)zombies.get(1);
            zombie.func_70107_b(mate.field_70165_t, mate.field_70163_u, mate.field_70161_v);
            ParticleEffect.HEART.send(SoundEffect.NONE, mate, 1.0D, 2.0D, 8);
            zombie.func_82229_g(true);
            mate.func_82229_g(true);
            EntityZombie baby = new EntityZombie(this.field_70170_p);
            baby.func_70012_b(mate.field_70165_t, mate.field_70163_u, mate.field_70161_v, 0.0F, 0.0F);
            baby.func_82227_f(true);
            this.field_70170_p.func_72838_d(baby);
            zombies.remove(0);
            zombies.remove(0);
         }
      }

   }

   private void giveBirth(EntityVillager villagerObj, EntityVillager mate) {
      EntityVillager entityvillager = villagerObj.func_90011_a(mate);
      mate.func_70873_a(6000);
      villagerObj.func_70873_a(6000);
      entityvillager.func_70873_a(-24000);
      entityvillager.func_70012_b(villagerObj.field_70165_t, villagerObj.field_70163_u, villagerObj.field_70161_v, 0.0F, 0.0F);
      this.field_70170_p.func_72838_d(entityvillager);
      this.field_70170_p.func_72960_a(entityvillager, (byte)12);
   }

   private void impactQuicklime(MovingObjectPosition mop) {
      if (mop.field_72313_a == MovingObjectType.ENTITY) {
         if (mop.field_72308_g instanceof EntityLivingBase) {
            EntityLivingBase livingEntity = (EntityLivingBase)mop.field_72308_g;
            if (!livingEntity.func_70644_a(Potion.field_76440_q)) {
               livingEntity.func_70690_d(new PotionEffect(Potion.field_76440_q.field_76415_H, 60, 0));
            }

            float DAMAGE = mop.field_72308_g instanceof EntitySlime ? 4.0F : (livingEntity.func_110143_aJ() == livingEntity.func_110138_aP() ? 0.5F : 0.1F);
            livingEntity.func_70097_a(DamageSource.func_76356_a(this, this.func_85052_h()), DAMAGE);
         }

         this.skipFX = true;
      } else {
         EntityItem itemEntity = null;
         if (mop != null) {
            ItemStack newBrewStack = Witchery.Items.GENERIC.itemQuicklime.createStack();
            switch(mop.field_72313_a) {
            case BLOCK:
               itemEntity = new EntityItem(this.field_70170_p, (double)mop.field_72311_b + 0.5D, (double)(mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : 1)) + 0.5D, (double)mop.field_72309_d + 0.5D, newBrewStack);
               break;
            case ENTITY:
               itemEntity = new EntityItem(this.field_70170_p, mop.field_72308_g.field_70165_t, mop.field_72308_g.field_70163_u, mop.field_72308_g.field_70161_v, newBrewStack);
            }
         }

         this.skipFX = true;
         if (itemEntity != null) {
            this.field_70170_p.func_72838_d(itemEntity);
         }

      }
   }

   private void impactRaising(MovingObjectPosition mop) {
      if (mop.field_72313_a == MovingObjectType.BLOCK && mop.field_72310_e == 1) {
         int posX = mop.field_72311_b;
         int posY = mop.field_72312_c;
         int posZ = mop.field_72309_d;
         World world = this.field_70170_p;
         raiseDead(posX, posY, posZ, world, this.func_85052_h());
      } else {
         EntityItem itemEntity = null;
         if (mop != null) {
            ItemStack newBrewStack = Witchery.Items.GENERIC.itemBrewOfRaising.createStack();
            switch(mop.field_72313_a) {
            case BLOCK:
               itemEntity = new EntityItem(this.field_70170_p, (double)mop.field_72311_b + 0.5D, (double)(mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : 1)) + 0.5D, (double)mop.field_72309_d + 0.5D, newBrewStack);
               break;
            case ENTITY:
               itemEntity = new EntityItem(this.field_70170_p, mop.field_72308_g.field_70165_t, mop.field_72308_g.field_70163_u, mop.field_72308_g.field_70161_v, newBrewStack);
            }
         }

         this.skipFX = true;
         if (itemEntity != null) {
            this.field_70170_p.func_72838_d(itemEntity);
         }

      }
   }

   public static void raiseDead(int posX, int posY, int posZ, World world, EntityLivingBase raiser) {
      int y0 = world.func_147439_a(posX, posY, posZ).func_149688_o().func_76220_a() ? posY : posY - 1;
      int MAX_SPAWNS = true;
      int MAX_DISTANCE = true;
      int MAX_DROP = true;
      EntityPlayer playerThrower = (EntityPlayer)((EntityPlayer)(raiser instanceof EntityPlayer ? raiser : null));
      raiseUndead(world, posX, y0, posZ, playerThrower);
      int extraCount = 0;
      double chance = world.field_73012_v.nextDouble();
      if (chance < 0.1D) {
         extraCount = 2;
      } else if (chance < 0.4D) {
         extraCount = 1;
      }

      for(int i = 0; i < extraCount; ++i) {
         int x = posX - 3 + world.field_73012_v.nextInt(6) + 1;
         int z = posZ - 3 + world.field_73012_v.nextInt(6) + 1;
         int y = -1;

         for(int dy = -6; dy < 6; ++dy) {
            if (world.func_147439_a(x, y0 - dy, z).func_149688_o().func_76220_a()) {
               y = y0 - dy;
               break;
            }
         }

         if (y != -1) {
            raiseUndead(world, x, y, z, playerThrower);
         }
      }

   }

   private static void raiseUndead(World world, int posX, int posY, int posZ, EntityPlayer thrower) {
      if (!world.field_72995_K) {
         Block blockID = world.func_147439_a(posX, posY, posZ);
         if (blockID != Blocks.field_150346_d && blockID != Blocks.field_150348_b && blockID != Blocks.field_150349_c && blockID != Blocks.field_150424_aL && blockID != Blocks.field_150391_bh && blockID != Blocks.field_150425_aM && blockID != Blocks.field_150347_e && blockID != Blocks.field_150351_n && blockID != Blocks.field_150354_m) {
            ++posY;
         }

         spawnParticles(world, ParticleEffect.SMOKE, 0.5D + (double)posX, 0.5D + (double)posY, 0.5D + (double)posZ);
         world.func_147468_f(posX, posY, posZ);
         world.func_147468_f(posX, posY + 1, posZ);
         EntityLiving undeadEntity = createUndeadCreature(world);
         undeadEntity.func_70012_b(0.5D + (double)posX, 0.5D + (double)posY, 0.5D + (double)posZ, 1.0F, 0.0F);
         IEntityLivingData entitylivingData = null;
         undeadEntity.func_110161_a((IEntityLivingData)entitylivingData);
         undeadEntity.func_110163_bv();
         if (thrower != null) {
            try {
               PotionEnslaved.setEnslaverForMob(undeadEntity, thrower);
            } catch (Exception var9) {
               Log.instance().warning(var9, "Unhandled exception occurred setting enslaver from raiseUnded potion.");
            }
         }

         world.func_72838_d(undeadEntity);
      }

   }

   private static EntityLiving createUndeadCreature(World world) {
      double value = world.field_73012_v.nextDouble();
      if (value < 0.6D) {
         return new EntityZombie(world);
      } else {
         return (EntityLiving)(value < 0.97D ? new EntitySkeleton(world) : new EntityPigZombie(world));
      }
   }

   private void impactErosion(MovingObjectPosition mop, boolean enhanced) {
      int slot;
      int slot;
      if (mop.field_72313_a == MovingObjectType.BLOCK) {
         if (BlockProtect.checkModsForBreakOK(this.field_70170_p, mop.field_72311_b, mop.field_72309_d, mop.field_72312_c, this.func_85052_h())) {
            int RADIUS = true;
            int obsidianMetled = 0;
            int obsidianMetled = obsidianMetled + this.drawFilledCircle(this.field_70170_p, mop.field_72311_b, mop.field_72309_d, mop.field_72312_c, 2);

            for(slot = 0; slot < 2; ++slot) {
               slot = slot + 1;
               obsidianMetled += this.drawFilledCircle(this.field_70170_p, mop.field_72311_b, mop.field_72309_d, mop.field_72312_c + slot, 2 - slot);
               obsidianMetled += this.drawFilledCircle(this.field_70170_p, mop.field_72311_b, mop.field_72309_d, mop.field_72312_c - slot, 2 - slot);
            }

            if (obsidianMetled > 0) {
               this.field_70170_p.func_72838_d(new EntityItem(this.field_70170_p, 0.5D + (double)mop.field_72311_b, 0.5D + (double)mop.field_72312_c, 0.5D + (double)mop.field_72309_d, new ItemStack(Blocks.field_150343_Z, obsidianMetled, 0)));
            }
         }
      } else if (mop.field_72313_a == MovingObjectType.ENTITY) {
         if (mop.field_72308_g instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase)mop.field_72308_g;
            float ACID_DAMAGE = enhanced ? 10.0F : 8.0F;
            entity.func_70097_a(DamageSource.func_76356_a(entity, this.func_85052_h()), ACID_DAMAGE);
            if (entity instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)entity;
               if (this.causeAcidDamage(entity.func_70694_bm())) {
                  player.func_71028_bD();
               }

               for(slot = 0; slot < player.field_71071_by.field_70460_b.length; ++slot) {
                  if (this.causeAcidDamage(player.field_71071_by.field_70460_b[slot])) {
                     player.field_71071_by.field_70460_b[slot] = null;
                  }
               }
            } else {
               for(slot = 0; slot < 5; ++slot) {
                  if (this.causeAcidDamage(entity.func_71124_b(slot))) {
                     entity.func_70062_b(slot, (ItemStack)null);
                  }
               }
            }
         } else {
            this.skipFX = true;
            this.field_70170_p.func_72838_d(new EntityItem(this.field_70170_p, mop.field_72308_g.field_70165_t, mop.field_72308_g.field_70163_u, mop.field_72308_g.field_70161_v, Witchery.Items.GENERIC.itemBrewOfErosion.createStack()));
         }
      }

   }

   private boolean causeAcidDamage(ItemStack itemstack) {
      int ITEM_ACID_DAMAGE = true;
      if (itemstack != null && itemstack.func_77984_f() && EarthItems.instance().isMatch(itemstack)) {
         itemstack.func_77972_a(100, this.func_85052_h());
         return itemstack.func_77960_j() <= 0;
      } else {
         return false;
      }
   }

   protected int drawFilledCircle(World world, int x0, int z0, int y, int radius) {
      int x = radius;
      int z = 0;
      int radiusError = 1 - radius;
      int obsidianMelted = 0;

      while(x >= z) {
         obsidianMelted += this.drawLine(world, -x + x0, x + x0, z + z0, y);
         obsidianMelted += this.drawLine(world, -z + x0, z + x0, x + z0, y);
         obsidianMelted += this.drawLine(world, -x + x0, x + x0, -z + z0, y);
         obsidianMelted += this.drawLine(world, -z + x0, z + x0, -x + z0, y);
         ++z;
         if (radiusError < 0) {
            radiusError += 2 * z + 1;
         } else {
            --x;
            radiusError += 2 * (z - x + 1);
         }
      }

      return obsidianMelted;
   }

   protected int drawLine(World world, int x1, int x2, int z, int y) {
      int obsidianMelted = 0;

      for(int x = x1; x <= x2; ++x) {
         Block blockID = world.func_147439_a(x, y, z);
         if (blockID == Blocks.field_150343_Z) {
            ++obsidianMelted;
         }

         if (blockID != Blocks.field_150350_a && blockID != Blocks.field_150353_l && blockID != Blocks.field_150356_k && blockID != Blocks.field_150480_ab && blockID != Blocks.field_150358_i && blockID != Blocks.field_150355_j && BlockProtect.canBreak(blockID, world)) {
            world.func_147468_f(x, y, z);
            spawnParticles(this.field_70170_p, ParticleEffect.SPLASH, this.field_70165_t, this.field_70163_u, this.field_70161_v);
         }
      }

      return obsidianMelted;
   }

   private void impactSprout(MovingObjectPosition mop, boolean enhanced) {
      if (mop != null && mop.field_72313_a == MovingObjectType.BLOCK) {
         int posX = mop.field_72311_b;
         int posY = mop.field_72312_c;
         int posZ = mop.field_72309_d;
         World world = this.field_70170_p;
         int sideHit = mop.field_72310_e;
         growBranch(posX, posY, posZ, world, sideHit, enhanced ? 20 : 15, this.field_70121_D);
      } else {
         EntityItem itemEntity = null;
         if (mop != null) {
            ItemStack newBrewStack = Witchery.Items.GENERIC.itemBrewOfSprouting.createStack();
            switch(mop.field_72313_a) {
            case BLOCK:
               itemEntity = new EntityItem(this.field_70170_p, (double)mop.field_72311_b + 0.5D, (double)(mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : 1)) + 0.5D, (double)mop.field_72309_d + 0.5D, newBrewStack);
               break;
            case ENTITY:
               itemEntity = new EntityItem(this.field_70170_p, mop.field_72308_g.field_70165_t, mop.field_72308_g.field_70163_u, mop.field_72308_g.field_70161_v, newBrewStack);
            }
         }

         this.skipFX = true;
         if (itemEntity != null) {
            this.field_70170_p.func_72838_d(itemEntity);
         }
      }

   }

   public static void growBranch(int posX, int posY, int posZ, World world, int sideHit, int extent, AxisAlignedBB boundingBox) {
      Block blockID = world.func_147439_a(posX, posY, posZ);
      int j1 = world.func_72805_g(posX, posY, posZ);
      Block logBlock;
      if (blockID != Blocks.field_150364_r && blockID != Blocks.field_150344_f && blockID != Blocks.field_150345_g && blockID != Blocks.field_150362_t) {
         if (blockID != Witchery.Blocks.LOG && blockID != Witchery.Blocks.PLANKS && blockID != Witchery.Blocks.SAPLING && blockID != Witchery.Blocks.LEAVES) {
            logBlock = world.field_73012_v.nextInt(2) == 0 ? Blocks.field_150364_r : Witchery.Blocks.LOG;
            j1 = world.field_73012_v.nextInt(Blocks.field_150364_r == logBlock ? 4 : 3);
         } else {
            logBlock = Witchery.Blocks.LOG;
         }
      } else {
         logBlock = Blocks.field_150364_r;
      }

      Block leavesBlock = Blocks.field_150364_r == logBlock ? Blocks.field_150362_t : Witchery.Blocks.LEAVES;
      int b0 = 0;
      j1 &= 3;
      switch(sideHit) {
      case 0:
      case 1:
         b0 = 0;
         break;
      case 2:
      case 3:
         b0 = 8;
         break;
      case 4:
      case 5:
         b0 = 4;
      }

      int meta = j1 | b0;
      ParticleEffect particleEffect = ParticleEffect.EXPLODE;
      int dx = sideHit == 5 ? 1 : (sideHit == 4 ? -1 : 0);
      int dy = sideHit == 0 ? -1 : (sideHit == 1 ? 1 : 0);
      int dz = sideHit == 3 ? 1 : (sideHit == 2 ? -1 : 0);
      int sproutExtent = extent;
      boolean isInitialBlockSolid = world.func_147439_a(posX, posY, posZ).func_149688_o().func_76220_a();

      int i;
      int x;
      int y;
      int z;
      for(i = sideHit == 1 && !isInitialBlockSolid ? 0 : 1; i < sproutExtent; ++i) {
         int x = posX + i * dx;
         int y = posY + i * dy;
         int z = posZ + i * dz;
         if (y >= 255 || !setBlockIfNotSolid(world, x, y, z, logBlock, meta)) {
            break;
         }

         x = dx == 0 && world.field_73012_v.nextInt(4) == 0 ? world.field_73012_v.nextInt(3) - 1 : 0;
         y = dy == 0 && x == 0 && world.field_73012_v.nextInt(4) == 0 ? world.field_73012_v.nextInt(3) - 1 : 0;
         z = dz == 0 && x == 0 && y == 0 && world.field_73012_v.nextInt(4) == 0 ? world.field_73012_v.nextInt(3) - 1 : 0;
         if (x != 0 || y != 0 || z != 0) {
            setBlockIfNotSolid(world, x + x, y + y, z + z, (Block)leavesBlock, meta);
         }
      }

      if (sideHit == 1) {
         AxisAlignedBB axisalignedbb = boundingBox.func_72314_b(0.0D, 2.0D, 0.0D);
         List list1 = world.func_72872_a(EntityLivingBase.class, axisalignedbb);
         if (list1 != null && !list1.isEmpty()) {
            Iterator iterator = list1.iterator();
            x = posX + i * dx;
            y = Math.min(posY + i * dy, 255);
            z = posZ + i * dz;

            while(iterator.hasNext()) {
               EntityLivingBase entitylivingbase = (EntityLivingBase)iterator.next();
               if (!world.func_147439_a(x, y + 1, z).func_149688_o().func_76220_a() && !world.func_147439_a(x, y + 2, z).func_149688_o().func_76220_a()) {
                  entitylivingbase.func_70107_b(0.5D + (double)x, (double)(y + 1), 0.5D + (double)z);
               }
            }
         }
      }

   }

   private void impactWasting(MovingObjectPosition mop, boolean enhanced) {
      Entity livingEntity = mop.field_72308_g;
      double x;
      double y;
      double z;
      if (mop.field_72313_a == MovingObjectType.ENTITY) {
         x = livingEntity.field_70165_t;
         y = livingEntity.field_70163_u;
         z = livingEntity.field_70161_v;
      } else {
         x = (double)mop.field_72311_b;
         y = (double)mop.field_72312_c;
         z = (double)mop.field_72309_d;
      }

      explodeWasting(this.field_70170_p, x, y, z, livingEntity, this.field_70121_D, enhanced);
   }

   public static void explodeWasting(World world, double posX, double posY, double posZ, Entity livingEntity, AxisAlignedBB boundingBox, boolean enhanced) {
      double RADIUS = enhanced ? 5.0D : 4.0D;
      AxisAlignedBB axisalignedbb = boundingBox.func_72314_b(RADIUS, 2.0D, RADIUS);
      List list1 = world.func_72872_a(EntityLivingBase.class, axisalignedbb);
      int x;
      if (list1 != null && !list1.isEmpty()) {
         Iterator iterator = list1.iterator();

         while(iterator.hasNext()) {
            EntityLivingBase entitylivingbase = (EntityLivingBase)iterator.next();
            double d0 = entitylivingbase.func_70092_e(posX, posY, posZ);
            if (d0 < RADIUS * RADIUS) {
               double d1 = 1.0D - Math.sqrt(d0) / RADIUS;
               if (entitylivingbase == livingEntity) {
                  d1 = 1.0D;
               }

               x = (int)(d1 * 400.0D + 0.5D);
               if (entitylivingbase instanceof EntityPlayer) {
                  EntityPlayer victim = (EntityPlayer)entitylivingbase;
                  int minLevel = enhanced ? 6 : 10;
                  if (victim.func_71024_bL().func_75116_a() > minLevel) {
                     victim.func_71024_bL().func_75122_a(-minLevel, 0.0F);
                  }

                  victim.func_70690_d(new PotionEffect(Potion.field_76438_s.field_76415_H, x * 2, enhanced ? 2 : 1));
                  victim.func_70690_d(new PotionEffect(Potion.field_76436_u.field_76415_H, Math.max(x / 3, 40), 0));
               } else {
                  entitylivingbase.func_70690_d(new PotionEffect(Potion.field_82731_v.field_76415_H, x * 2, enhanced ? 1 : 0));
                  entitylivingbase.func_70690_d(new PotionEffect(Potion.field_76436_u.field_76415_H, Math.max(x / 3, 40), 0));
               }
            }
         }
      }

      int BLOCK_RADIUS = (int)RADIUS - 1;
      int BLOCK_RADIUS_SQ = BLOCK_RADIUS * BLOCK_RADIUS;
      int blockX = MathHelper.func_76128_c(posX);
      int blockY = MathHelper.func_76128_c(posY);
      int blockZ = MathHelper.func_76128_c(posZ);

      for(int y = blockY - BLOCK_RADIUS; y <= blockY + BLOCK_RADIUS; ++y) {
         for(x = blockX - BLOCK_RADIUS; x <= blockX + BLOCK_RADIUS; ++x) {
            for(int z = blockZ - BLOCK_RADIUS; z <= blockZ + BLOCK_RADIUS; ++z) {
               if (Coord.distanceSq((double)x, (double)y, (double)z, (double)blockX, (double)blockY, (double)blockZ) <= (double)BLOCK_RADIUS_SQ) {
                  Material material = world.func_147439_a(x, y, z).func_149688_o();
                  if (material != null && (material == Material.field_151584_j || (material == Material.field_151585_k || material == Material.field_151582_l) && material.func_76222_j())) {
                     Block blockID = world.func_147439_a(x, y, z);
                     if (!(blockID instanceof BlockCircle) && !(blockID instanceof BlockCircleGlyph)) {
                        blockID.func_149697_b(world, x, y, z, world.func_72805_g(x, y, z), 0);
                        world.func_147468_f(x, y, z);
                     }
                  }
               }
            }
         }
      }

   }

   private void impactInk(MovingObjectPosition mop, boolean enhanced) {
      Entity livingEntity = mop.field_72308_g;
      explodeInk(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, livingEntity, this.field_70121_D, enhanced);
   }

   public static void explodeInk(World world, double posX, double posY, double posZ, Entity livingEntity, AxisAlignedBB boundingBox, boolean enhanced) {
      double RADIUS = enhanced ? 5.0D : 4.0D;
      AxisAlignedBB axisalignedbb = boundingBox.func_72314_b(RADIUS, 2.0D, RADIUS);
      List list1 = world.func_72872_a(EntityLivingBase.class, axisalignedbb);
      if (list1 != null && !list1.isEmpty()) {
         Iterator iterator = list1.iterator();

         while(iterator.hasNext()) {
            EntityLivingBase entitylivingbase = (EntityLivingBase)iterator.next();
            double d0 = entitylivingbase.func_70092_e(posX, posY, posZ);
            if (d0 < RADIUS * RADIUS) {
               double d1 = 1.0D - Math.sqrt(d0) / RADIUS;
               if (entitylivingbase == livingEntity) {
                  d1 = 1.0D;
               }

               int j = (int)(d1 * 400.0D + 0.5D);
               entitylivingbase.func_70690_d(new PotionEffect(Potion.field_76440_q.field_76415_H, j, 0));
               if (entitylivingbase instanceof EntityLiving) {
                  EntityUtil.dropAttackTarget((EntityLiving)entitylivingbase);
               }
            }
         }
      }

   }

   private void impactRock(MovingObjectPosition mop) {
      if (mop.field_72308_g != null) {
         float DAMAGE = 6.0F;
         mop.field_72308_g.func_70097_a(DamageSource.func_76356_a(this, this.func_85052_h()), 6.0F);
      }

      spawnParticles(this.field_70170_p, ParticleEffect.EXPLODE, this.field_70165_t, this.field_70163_u, this.field_70161_v);
   }

   private static void spawnParticles(World world, ParticleEffect effect, double posX, double posY, double posZ) {
      effect.send(SoundEffect.NONE, world, posX, posY, posZ, 1.0D, 1.0D, 8);
   }

   private void impactWebSmall(MovingObjectPosition mop) {
      switch(mop.field_72313_a) {
      case BLOCK:
         if (this.field_70170_p.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d) == Blocks.field_150433_aE) {
            --mop.field_72312_c;
            mop.field_72310_e = 1;
         }

         switch(mop.field_72310_e) {
         case 0:
            setBlockIfNotSolid(this.field_70170_p, mop.field_72311_b, mop.field_72312_c - 1, mop.field_72309_d, Blocks.field_150321_G);
            return;
         case 1:
            setBlockIfNotSolid(this.field_70170_p, mop.field_72311_b, mop.field_72312_c + 1, mop.field_72309_d, Blocks.field_150321_G);
            return;
         case 2:
            setBlockIfNotSolid(this.field_70170_p, mop.field_72311_b - 1, mop.field_72312_c, mop.field_72309_d, Blocks.field_150321_G);
            return;
         case 3:
            setBlockIfNotSolid(this.field_70170_p, mop.field_72311_b + 1, mop.field_72312_c, mop.field_72309_d, Blocks.field_150321_G);
            return;
         case 4:
            setBlockIfNotSolid(this.field_70170_p, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d - 1, Blocks.field_150321_G);
            return;
         case 5:
            setBlockIfNotSolid(this.field_70170_p, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d + 1, Blocks.field_150321_G);
            return;
         default:
            return;
         }
      case ENTITY:
         this.field_70170_p.func_147449_b((int)mop.field_72308_g.field_70165_t, (int)mop.field_72308_g.field_70163_u, (int)mop.field_72308_g.field_70161_v, Blocks.field_150321_G);
      }

   }

   private void impactWebBig(MovingObjectPosition mop, boolean enhanced) {
      switch(mop.field_72313_a) {
      case BLOCK:
         explodeWeb(this.field_70170_p, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, mop.field_72310_e, enhanced);
         break;
      case ENTITY:
         int x = MathHelper.func_76128_c(mop.field_72308_g.field_70165_t);
         int y = MathHelper.func_76128_c(mop.field_72308_g.field_70163_u);
         int z = MathHelper.func_76128_c(mop.field_72308_g.field_70161_v);
         explodeWeb(this.field_70170_p, x, y, z, -1, enhanced);
      }

   }

   public static void explodeWeb(World world, int posX, int posY, int posZ, int side, boolean enhanced) {
      int x = posX + (side == 4 ? -1 : (side == 5 ? 1 : 0));
      int z = posZ + (side == 2 ? -1 : (side == 3 ? 1 : 0));
      int y = posY + (side == 0 ? -1 : (side == 1 ? 1 : 0));
      if (side == 1 && !world.func_147439_a(x, posY, z).func_149688_o().func_76220_a()) {
         --y;
      }

      setBlockIfNotSolid(world, x, y, z, Blocks.field_150321_G);
      setBlockIfNotSolid(world, x + 1, y, z, Blocks.field_150321_G);
      setBlockIfNotSolid(world, x - 1, y, z, Blocks.field_150321_G);
      setBlockIfNotSolid(world, x, y, z + 1, Blocks.field_150321_G);
      setBlockIfNotSolid(world, x, y, z - 1, Blocks.field_150321_G);
      if (enhanced) {
         setBlockIfNotSolid(world, x + 1, y, z + 1, Blocks.field_150321_G);
         setBlockIfNotSolid(world, x - 1, y, z - 1, Blocks.field_150321_G);
         setBlockIfNotSolid(world, x - 1, y, z + 1, Blocks.field_150321_G);
         setBlockIfNotSolid(world, x + 1, y, z - 1, Blocks.field_150321_G);
      }

      setBlockIfNotSolid(world, x, y + 1, z, Blocks.field_150321_G);
      setBlockIfNotSolid(world, x, y - 1, z, Blocks.field_150321_G);
   }

   private void impactThorns(MovingObjectPosition mop, boolean enhanced) {
      int x;
      int y;
      int z;
      switch(mop.field_72313_a) {
      case BLOCK:
         if (mop.field_72310_e == 1 || this.field_70170_p.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d) == Blocks.field_150434_aF) {
            y = mop.field_72312_c;
            x = mop.field_72311_b;
            z = mop.field_72309_d;
            int CACTUS_HEIGHT = enhanced ? 4 : 3;
            if (plantCactus(this.field_70170_p, x, y, z, CACTUS_HEIGHT)) {
               break;
            }
         }

         ItemStack newBrewStack = Witchery.Items.GENERIC.itemBrewOfThorns.createStack();
         x = mop.field_72311_b + (mop.field_72310_e == 4 ? -1 : (mop.field_72310_e == 5 ? 1 : 0));
         z = mop.field_72309_d + (mop.field_72310_e == 2 ? -1 : (mop.field_72310_e == 3 ? 1 : 0));
         y = mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : (mop.field_72310_e == 1 ? 1 : 0));
         this.skipFX = true;
         this.field_70170_p.func_72838_d(new EntityItem(this.field_70170_p, (double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, newBrewStack));
         break;
      case ENTITY:
         int CACTUS_HEIGHT = enhanced ? 2 : 1;
         x = MathHelper.func_76128_c(mop.field_72308_g.field_70165_t);
         y = MathHelper.func_76128_c(mop.field_72308_g.field_70163_u);
         z = MathHelper.func_76128_c(mop.field_72308_g.field_70161_v);
         boolean success = plantCactus(this.field_70170_p, x + 1, y, z, CACTUS_HEIGHT);
         success = success && plantCactus(this.field_70170_p, x - 1, y, z, CACTUS_HEIGHT);
         success = success && plantCactus(this.field_70170_p, x, y, z + 1, CACTUS_HEIGHT);
         success = success && plantCactus(this.field_70170_p, x, y, z - 1, CACTUS_HEIGHT);
         if (!success) {
            this.skipFX = true;
            this.field_70170_p.func_72838_d(new EntityItem(this.field_70170_p, mop.field_72308_g.field_70165_t, mop.field_72308_g.field_70163_u, mop.field_72308_g.field_70161_v, Witchery.Items.GENERIC.itemBrewOfThorns.createStack()));
         }
      }

   }

   public static boolean plantCactus(World world, int x, int y, int z, int CACTUS_HEIGHT) {
      if (!world.func_147439_a(x, y, z).func_149688_o().func_76220_a()) {
         --y;
      }

      Material material = world.func_147439_a(x, y, z).func_149688_o();
      if (material != Material.field_151571_B && material != Material.field_151596_z && material != Material.field_151577_b && material != Material.field_151578_c && material != Material.field_151576_e && material != Material.field_151595_p && material != Material.field_151597_y && material != Material.field_151583_m && material != Material.field_151570_A) {
         return false;
      } else {
         Block blockID = world.func_147439_a(x, y, z);
         if (!BlockProtect.canBreak(blockID, world)) {
            return false;
         } else {
            if (material != Material.field_151570_A) {
               world.func_147449_b(x, y, z, Blocks.field_150354_m);
            } else {
               while(world.func_147439_a(x, y, z) == Blocks.field_150434_aF) {
                  ++y;
               }

               --y;
            }

            for(int i = 1; i <= CACTUS_HEIGHT && y + i < 256 && setBlockIfNotSolid(world, x, y + i, z, Blocks.field_150434_aF); ++i) {
            }

            return true;
         }
      }
   }

   private void impactVines(MovingObjectPosition mop, boolean enhanced) {
      if (mop.field_72313_a == MovingObjectType.BLOCK && mop.field_72310_e != 0 && mop.field_72310_e != 1) {
         int dx = mop.field_72310_e == 4 ? -1 : (mop.field_72310_e == 5 ? 1 : 0);
         int dz = mop.field_72310_e == 2 ? -1 : (mop.field_72310_e == 3 ? 1 : 0);
         int y0 = mop.field_72312_c;
         int meta = 0;
         switch(mop.field_72310_e) {
         case 2:
            meta = 1;
            break;
         case 3:
            meta = 4;
            break;
         case 4:
            meta = 8;
            break;
         case 5:
            meta = 2;
         }

         ParticleEffect EFFECT = ParticleEffect.EXPLODE;
         int y = y0;
         int x = mop.field_72311_b;
         int z = mop.field_72309_d;
         if (!this.isNotSolidOrLeaves(this.field_70170_p.func_147439_a(x + dx, y0, z + dz).func_149688_o()) || !this.field_70170_p.func_147439_a(x, y0, z).func_149688_o().func_76220_a()) {
            x += dx;
            z += dz;
         }

         label112:
         while(true) {
            do {
               do {
                  do {
                     if (!this.isNotSolidOrLeaves(this.field_70170_p.func_147439_a(x + dx, y, z + dz).func_149688_o()) || !this.field_70170_p.func_147439_a(x, y, z).func_149688_o().func_76220_a() || y <= 0) {
                        y = y0 + 1;
                        x = mop.field_72311_b;
                        z = mop.field_72309_d;
                        if (!this.field_70170_p.func_147439_a(x, y, z).func_149688_o().func_76220_a()) {
                           x -= dx;
                           z -= dz;
                           if (enhanced && !this.field_70170_p.func_147439_a(x, y, z).func_149688_o().func_76220_a()) {
                              x -= dx;
                              z -= dz;
                           }
                        }

                        while(this.isNotSolidOrLeaves(this.field_70170_p.func_147439_a(x + dx, y, z + dz).func_149688_o()) && this.field_70170_p.func_147439_a(x, y, z).func_149688_o().func_76220_a() && y < 256) {
                           this.field_70170_p.func_147465_d(x + dx, y, z + dz, Blocks.field_150395_bd, meta, 3);
                           spawnParticles(this.field_70170_p, EFFECT, 0.5D + (double)x + (double)dx, 0.5D + (double)y, 0.5D + (double)z + (double)dz);
                           ++y;
                           if (!this.field_70170_p.func_147439_a(x, y, z).func_149688_o().func_76220_a()) {
                              x -= dx;
                              z -= dz;
                              if (enhanced && !this.field_70170_p.func_147439_a(x, y, z).func_149688_o().func_76220_a()) {
                                 x -= dx;
                                 z -= dz;
                              }
                           }
                        }
                        break label112;
                     }

                     this.field_70170_p.func_147465_d(x + dx, y, z + dz, Blocks.field_150395_bd, meta, 3);
                     spawnParticles(this.field_70170_p, EFFECT, 0.5D + (double)x + (double)dx, 0.5D + (double)y, 0.5D + (double)z + (double)dz);
                     --y;
                  } while(this.isNotSolidOrLeaves(this.field_70170_p.func_147439_a(x + dx, y, z + dz).func_149688_o()) && this.field_70170_p.func_147439_a(x, y, z).func_149688_o().func_76220_a());

                  x += dx;
                  z += dz;
               } while(!enhanced);
            } while(this.isNotSolidOrLeaves(this.field_70170_p.func_147439_a(x + dx, y, z + dz).func_149688_o()) && this.field_70170_p.func_147439_a(x, y, z).func_149688_o().func_76220_a());

            x += dx;
            z += dz;
         }
      } else {
         EntityItem itemEntity = null;
         ItemStack newBrewStack = Witchery.Items.GENERIC.itemBrewOfVines.createStack();
         switch(mop.field_72313_a) {
         case BLOCK:
            itemEntity = new EntityItem(this.field_70170_p, (double)mop.field_72311_b + 0.5D, (double)(mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : 1)) + 0.5D, (double)mop.field_72309_d + 0.5D, newBrewStack);
            break;
         case ENTITY:
            itemEntity = new EntityItem(this.field_70170_p, mop.field_72308_g.field_70165_t, mop.field_72308_g.field_70163_u, mop.field_72308_g.field_70161_v, newBrewStack);
         }

         this.skipFX = true;
         this.field_70170_p.func_72838_d(itemEntity);
      }

   }

   private boolean isNotSolidOrLeaves(Material material) {
      return material == null || !material.func_76220_a() || material == Material.field_151584_j;
   }

   private static boolean setBlockIfNotSolid(World world, int x, int y, int z, Block block) {
      return setBlockIfNotSolid(world, x, y, z, block, 0);
   }

   private static boolean setBlockIfNotSolid(World world, int x, int y, int z, Block block, int metadata) {
      if (world.func_147439_a(x, y, z).func_149688_o().func_76220_a() && (block != Blocks.field_150321_G || world.func_147439_a(x, y, z) != Blocks.field_150433_aE)) {
         return false;
      } else {
         world.func_147465_d(x, y, z, block, metadata, 3);
         spawnParticles(world, ParticleEffect.EXPLODE, 0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z);
         return true;
      }
   }

   public void func_70037_a(NBTTagCompound nbtTag) {
      super.func_70037_a(nbtTag);
      if (nbtTag.func_74764_b("damageValue")) {
         this.damageValue = nbtTag.func_74762_e("damageValue");
         this.setDamageValue(this.damageValue);
      } else {
         this.func_70106_y();
      }

   }

   public void func_70014_b(NBTTagCompound nbtTag) {
      super.func_70014_b(nbtTag);
      nbtTag.func_74768_a("damageValue", this.damageValue);
   }
}
