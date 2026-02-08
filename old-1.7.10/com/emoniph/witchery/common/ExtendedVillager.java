package com.emoniph.witchery.common;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.brewing.potions.PotionResizing;
import com.emoniph.witchery.network.PacketExtendedEntityRequestSyncToClient;
import com.emoniph.witchery.network.PacketExtendedVillagerSync;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class ExtendedVillager implements IExtendedEntityProperties {
   private static final String EXT_PROP_NAME = "WitcheryExtendedVillager";
   private final EntityVillager villager;
   private int blood = 500;
   private boolean sleeping;
   private int sleepingTicks;
   public boolean synced;
   private boolean trySync;

   public static final void register(EntityVillager villager) {
      villager.registerExtendedProperties("WitcheryExtendedVillager", new ExtendedVillager(villager));
   }

   public static final ExtendedVillager get(EntityVillager villager) {
      return (ExtendedVillager)villager.getExtendedProperties("WitcheryExtendedVillager");
   }

   public ExtendedVillager(EntityVillager villager) {
      this.villager = villager;
   }

   public EntityVillager getVillager() {
      return this.villager;
   }

   public void saveNBTData(NBTTagCompound compound) {
      NBTTagCompound props = new NBTTagCompound();
      props.func_74768_a("Blood", this.blood);
      compound.func_74782_a("WitcheryExtendedVillager", props);
   }

   public void loadNBTData(NBTTagCompound compound) {
      if (compound.func_74764_b("WitcheryExtendedVillager")) {
         NBTTagCompound props = (NBTTagCompound)compound.func_74781_a("WitcheryExtendedVillager");
         this.blood = MathHelper.func_76125_a(props.func_74762_e("Blood"), 0, 500);
      }

   }

   public void init(Entity entity, World world) {
   }

   public void setBlood(int blood) {
      if (this.blood != blood) {
         this.blood = Math.max(Math.min(blood, 500), 0);
         this.sync();
      }

   }

   public int takeBlood(int quantity, EntityLivingBase player) {
      PotionEffect potionEffect = this.villager.func_70660_b(Witchery.Potions.PARALYSED);
      boolean isKnockedOut = this.isSleeping() || potionEffect != null && potionEffect.func_76458_c() >= 4;
      if (!isKnockedOut) {
         quantity = (int)Math.ceil((double)(0.66F * (float)quantity));
      }

      int remainder = Math.max(this.blood - quantity, 0);
      int taken = this.blood - remainder;
      this.setBlood(remainder);
      if (player instanceof EntityPlayer) {
         if (this.blood < (int)Math.ceil(250.0D)) {
            this.villager.func_70097_a(new EntityDamageSource(DamageSource.field_76376_m.func_76355_l(), player), 1.3F);
         } else if (!isKnockedOut) {
            this.villager.func_70097_a(new EntityDamageSource(DamageSource.field_76376_m.func_76355_l(), player), 0.1F);
         }
      }

      return taken;
   }

   public void giveBlood(int quantity) {
      if (this.blood < 500) {
         this.setBlood(this.blood + quantity);
      }

   }

   public int getBlood() {
      return this.blood;
   }

   public void setSleeping(boolean sleeping) {
      if (this.sleeping != sleeping) {
         this.sleeping = sleeping;
         if (this.sleeping) {
            PotionResizing.setEntitySize(this.villager, 0.8F, 1.1F);
         } else {
            PotionResizing.setEntitySize(this.villager, 0.6F, 1.8F);
            if (this.sleepingTicks >= TimeUtil.minsToTicks(2)) {
               this.villager.func_70606_j(this.villager.func_110138_aP());
            }

            if (this.sleepingTicks > TimeUtil.minsToTicks(1)) {
               int blops = this.sleepingTicks / TimeUtil.minsToTicks(1);
               this.giveBlood(50 * blops);
            }
         }

         this.sleepingTicks = 0;
         this.sync();
      }

   }

   public boolean isSleeping() {
      return this.sleeping;
   }

   public void incrementSleepingTicks() {
      ++this.sleepingTicks;
   }

   public void sync() {
      if (!this.villager.field_70170_p.field_72995_K && this.villager.func_110143_aJ() > 0.0F && !this.villager.field_70128_L) {
         Witchery.packetPipeline.sendToAll((IMessage)(new PacketExtendedVillagerSync(this)));
      }

   }

   public boolean isClientSynced() {
      if (this.villager.field_70170_p.field_72995_K) {
         if (this.synced) {
            return true;
         }

         if (this.trySync) {
            return false;
         }

         this.trySync = true;
         Witchery.packetPipeline.sendToServer(new PacketExtendedEntityRequestSyncToClient(this.villager));
      }

      return false;
   }
}
