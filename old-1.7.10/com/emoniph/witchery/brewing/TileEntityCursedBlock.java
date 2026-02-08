package com.emoniph.witchery.brewing;

import com.emoniph.witchery.blocks.TileEntityBase;
import com.emoniph.witchery.util.EntityPosition;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public class TileEntityCursedBlock extends TileEntityBase {
   NBTTagCompound nbtEffect;
   int color;
   int duration;
   int expansion;
   int count;
   String thrower;

   public boolean canUpdate() {
      return false;
   }

   public void initalise(ModifiersImpact impactModifiers, NBTTagCompound nbtBrew) {
      if (nbtBrew != null) {
         this.nbtEffect = (NBTTagCompound)nbtBrew.func_74737_b();
      }

      this.color = WitcheryBrewRegistry.INSTANCE.getBrewColor(this.nbtEffect);
      this.duration = impactModifiers.lifetime >= 0 ? 5 + impactModifiers.lifetime * impactModifiers.lifetime * 5 : 100;
      this.expansion = Math.min(4 + impactModifiers.extent, 10);
      if (impactModifiers.thrower != null) {
         this.thrower = impactModifiers.thrower.func_70005_c_();
      }

      this.count = 1;
      this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
   }

   public void updateCurse(ModifiersImpact impactModifiers, NBTTagCompound nbtBrew) {
      if (nbtBrew != null) {
         if (this.nbtEffect != null && this.nbtEffect.func_150295_c("Items", 10).equals(nbtBrew.func_150295_c("Items", 10))) {
            ++this.count;
         } else {
            this.nbtEffect = nbtBrew;
            this.count = 1;
            this.color = WitcheryBrewRegistry.INSTANCE.getBrewColor(this.nbtEffect);
            this.duration = impactModifiers.lifetime >= 0 ? 5 + impactModifiers.lifetime * impactModifiers.lifetime * 5 : 100;
            this.expansion = Math.min(4 + impactModifiers.extent, 10);
            if (impactModifiers.thrower != null) {
               this.thrower = impactModifiers.thrower.func_70005_c_();
            }
         }
      }

   }

   public Packet func_145844_m() {
      NBTTagCompound nbtTag = new NBTTagCompound();
      this.func_145841_b(nbtTag);
      return new S35PacketUpdateTileEntity(this.field_145851_c, this.field_145848_d, this.field_145849_e, 1, nbtTag);
   }

   public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
      super.onDataPacket(net, packet);
      this.func_145839_a(packet.func_148857_g());
      this.field_145850_b.func_147479_m(this.field_145851_c, this.field_145848_d, this.field_145849_e);
   }

   public void func_145841_b(NBTTagCompound nbtRoot) {
      super.func_145841_b(nbtRoot);
      if (this.nbtEffect != null) {
         nbtRoot.func_74782_a("Effect", this.nbtEffect);
      }

      nbtRoot.func_74768_a("Color", this.color);
      nbtRoot.func_74768_a("Duration", this.duration);
      nbtRoot.func_74768_a("Expansion", this.expansion);
      nbtRoot.func_74768_a("Count", this.count);
      if (this.thrower != null) {
         nbtRoot.func_74778_a("Thrower", this.thrower);
      }

   }

   public void func_145839_a(NBTTagCompound nbtRoot) {
      super.func_145839_a(nbtRoot);
      if (nbtRoot.func_74764_b("Effect")) {
         this.nbtEffect = nbtRoot.func_74775_l("Effect");
      }

      this.color = nbtRoot.func_74762_e("Color");
      this.duration = nbtRoot.func_74762_e("Duration");
      this.expansion = nbtRoot.func_74762_e("Expansion");
      this.thrower = nbtRoot.func_74779_i("Thrower");
      this.count = nbtRoot.func_74762_e("Count");
   }

   public boolean applyToEntityAndDestroy(Entity entity) {
      if (this.nbtEffect != null && entity != null && entity instanceof EntityLivingBase) {
         EntityLivingBase living = (EntityLivingBase)entity;
         WitcheryBrewRegistry.INSTANCE.applyToEntity(entity.field_70170_p, living, this.nbtEffect, new ModifiersEffect(1.0D, 1.0D, false, new EntityPosition(living), false, 0, EntityUtil.playerOrFake(entity.field_70170_p, this.thrower)));
         ParticleEffect.SPELL_COLORED.send(SoundEffect.RANDOM_POP, living, 1.0D, 1.0D, 16);
      }

      return --this.count > 0;
   }
}
