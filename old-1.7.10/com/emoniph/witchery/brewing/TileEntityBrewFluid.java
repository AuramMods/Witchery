package com.emoniph.witchery.brewing;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityBrewFluid extends TileEntity {
   NBTTagCompound nbtEffect;
   int color;
   int duration;
   int expansion;
   int updateCount;
   String thrower;
   private int runTicks = 0;

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

      this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
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
   }

   public boolean canUpdate() {
      return false;
   }

   public int incRunTicks() {
      return ++this.runTicks;
   }
}
