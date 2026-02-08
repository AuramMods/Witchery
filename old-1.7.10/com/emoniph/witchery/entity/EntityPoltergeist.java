package com.emoniph.witchery.entity;

import com.emoniph.witchery.blocks.BlockBrazier;
import com.emoniph.witchery.blocks.BlockKettle;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityPoltergeist extends EntitySummonedUndead {
   private int attackTimer;

   public EntityPoltergeist(World par1World) {
      super(par1World);
      this.func_70661_as().func_75491_a(true);
      this.func_70661_as().func_75498_b(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
      this.field_70714_bg.func_75776_a(3, new EntityAIOpenDoor(this, true));
      this.field_70714_bg.func_75776_a(4, new EntityAIWander(this, 1.0D));
      this.field_70714_bg.func_75776_a(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(6, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(1, new EntityAIHurtByTarget(this, true));
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111265_b).func_111128_a(20.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.3D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(3.0D);
   }

   protected void func_70088_a() {
      super.func_70088_a();
   }

   protected boolean func_70650_aV() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public int getAttackTimer() {
      return this.attackTimer;
   }

   @SideOnly(Side.CLIENT)
   public void func_70103_a(byte par1) {
      if (par1 == 4) {
         this.attackTimer = 15;
      } else {
         super.func_70103_a(par1);
      }

   }

   public void func_70636_d() {
      super.func_70636_d();
      if (this.attackTimer > 0) {
         --this.attackTimer;
      }

      if (TimeUtil.secondsElapsed(5, (long)this.field_70173_aa)) {
         double RADIUS = 16.0D;
         double RADIUS_SQ = 256.0D;
         double THROW_RANGE = 3.0D;
         double THROW_RANGE_SQ = 9.0D;
         double EVIL_RANGE = 8.0D;
         double EVIL_RANGE_SQ = 64.0D;
         double MAX_SPEED = 0.6D;
         AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(this.field_70165_t - 16.0D, this.field_70163_u - 16.0D, this.field_70161_v - 16.0D, this.field_70165_t + 16.0D, this.field_70163_u + 16.0D, this.field_70161_v + 16.0D);
         List hangingItems = this.field_70170_p.func_72872_a(EntityHanging.class, bounds);
         Iterator i$ = hangingItems.iterator();

         while(i$.hasNext()) {
            Object obj = i$.next();
            EntityHanging hanging = (EntityHanging)obj;
            if (this.func_70068_e(hanging) <= 256.0D) {
               if (this.func_70068_e(hanging) <= 9.0D) {
                  if (!this.field_70170_p.field_72995_K) {
                     hanging.func_70097_a(DamageSource.func_76358_a(this), 3.0F);
                  }

                  this.attackTimer = 15;
                  this.field_70170_p.func_72960_a(this, (byte)4);
               } else {
                  this.func_70661_as().func_75492_a(hanging.field_70165_t, hanging.field_70163_u, hanging.field_70161_v, 1.0D);
               }

               return;
            }
         }

         EntityPlayer summoner = this.getSummoner();
         if (summoner != null && this.func_70068_e(summoner) <= 64.0D) {
            TileEntity closest = null;
            double closestDist = -1.0D;
            Iterator i$ = this.field_70170_p.field_147482_g.iterator();

            label136:
            while(true) {
               TileEntity tile;
               double distSq;
               ArrayList indices;
               do {
                  do {
                     do {
                        Object obj;
                        do {
                           do {
                              do {
                                 if (!i$.hasNext()) {
                                    if (closest == null) {
                                       break label136;
                                    }

                                    IInventory inventory = (IInventory)closest;
                                    ArrayList<Integer> indices = new ArrayList();

                                    int slot;
                                    for(slot = 0; slot < inventory.func_70302_i_(); ++slot) {
                                       if (inventory.func_70301_a(slot) != null) {
                                          indices.add(slot);
                                       }
                                    }

                                    if (indices.size() > 0) {
                                       if (this.func_70092_e(0.5D + (double)closest.field_145851_c, 0.5D + (double)closest.field_145848_d, 0.5D + (double)closest.field_145849_e) <= 9.0D) {
                                          if (!this.field_70170_p.field_72995_K) {
                                             slot = (Integer)indices.get(this.field_70170_p.field_73012_v.nextInt(indices.size()));
                                             ItemStack stack = inventory.func_70301_a(slot);
                                             if (stack.field_77994_a > 1) {
                                                --stack.field_77994_a;
                                                stack = stack.func_77946_l();
                                                stack.field_77994_a = 1;
                                             } else {
                                                inventory.func_70299_a(slot, (ItemStack)null);
                                             }

                                             EntityItem itemEntity = new EntityItem(this.field_70170_p, 0.5D + (double)closest.field_145851_c, 0.5D + (double)closest.field_145848_d, 0.5D + (double)closest.field_145849_e, stack);
                                             this.field_70170_p.func_72838_d(itemEntity);
                                             itemEntity.lifespan = TimeUtil.minsToTicks(15);
                                             itemEntity.field_70159_w = -0.3D + this.field_70170_p.field_73012_v.nextDouble() * 0.6D;
                                             itemEntity.field_70181_x = 0.1D + this.field_70170_p.field_73012_v.nextDouble() * 0.2D;
                                             itemEntity.field_70179_y = -0.3D + this.field_70170_p.field_73012_v.nextDouble() * 0.6D;
                                          }

                                          this.attackTimer = 15;
                                          this.field_70170_p.func_72960_a(this, (byte)4);
                                       } else {
                                          this.func_70661_as().func_75492_a((double)closest.field_145851_c, (double)closest.field_145848_d, (double)closest.field_145849_e, 1.0D);
                                       }

                                       return;
                                    }
                                    break label136;
                                 }

                                 obj = i$.next();
                              } while(!(obj instanceof IInventory));
                           } while(obj instanceof BlockKettle.TileEntityKettle);
                        } while(obj instanceof BlockBrazier.TileEntityBrazier);

                        tile = (TileEntity)obj;
                        distSq = this.func_70092_e(0.5D + (double)tile.field_145851_c, 0.5D + (double)tile.field_145848_d, 0.5D + (double)tile.field_145849_e);
                     } while(!(distSq <= 256.0D));

                     IInventory inventory = (IInventory)tile;
                     indices = new ArrayList();

                     for(int i = 0; i < inventory.func_70302_i_(); ++i) {
                        if (inventory.func_70301_a(i) != null) {
                           indices.add(i);
                        }
                     }
                  } while(indices.size() <= 0);
               } while(closest != null && !(distSq < closestDist));

               closest = tile;
               closestDist = distSq;
            }
         }

         List droppedItems = this.field_70170_p.func_72872_a(EntityItem.class, bounds);
         Iterator i$ = droppedItems.iterator();

         while(i$.hasNext()) {
            Object obj = i$.next();
            EntityItem dropped = (EntityItem)obj;
            if (this.func_70068_e(dropped) <= 256.0D) {
               if (this.func_70068_e(dropped) <= 9.0D) {
                  if (!this.field_70170_p.field_72995_K) {
                     dropped.field_70159_w = -0.3D + this.field_70170_p.field_73012_v.nextDouble() * 0.6D;
                     dropped.field_70181_x = 0.1D + this.field_70170_p.field_73012_v.nextDouble() * 0.2D;
                     dropped.field_70179_y = -0.3D + this.field_70170_p.field_73012_v.nextDouble() * 0.6D;
                  }

                  this.attackTimer = 15;
                  this.field_70170_p.func_72960_a(this, (byte)4);
               } else {
                  this.func_70661_as().func_75492_a(dropped.field_70165_t, dropped.field_70163_u, dropped.field_70161_v, 1.0D);
               }

               return;
            }
         }
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
      return "witchery:mob.spectre.spectre_die";
   }

   protected String func_70673_aS() {
      return "witchery:mob.spectre.spectre_die";
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.poltergeist.name");
   }

   public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
      super.func_70014_b(par1NBTTagCompound);
   }

   public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
      super.func_70037_a(par1NBTTagCompound);
   }

   public IEntityLivingData func_110161_a(IEntityLivingData par1EntityLivingData) {
      Object par1EntityLivingData1 = super.func_110161_a(par1EntityLivingData);
      this.func_70690_d(new PotionEffect(Potion.field_76441_p.field_76415_H, Integer.MAX_VALUE));
      return (IEntityLivingData)par1EntityLivingData1;
   }
}
