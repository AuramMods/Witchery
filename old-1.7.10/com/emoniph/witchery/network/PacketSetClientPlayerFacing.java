package com.emoniph.witchery.network;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class PacketSetClientPlayerFacing implements IMessage {
   private float pitch;
   private float yaw;

   public PacketSetClientPlayerFacing() {
   }

   public PacketSetClientPlayerFacing(EntityPlayer player) {
      this.pitch = player.field_70125_A;
      this.yaw = player.field_70177_z;
   }

   public void toBytes(ByteBuf buffer) {
      buffer.writeFloat(this.pitch);
      buffer.writeFloat(this.yaw);
   }

   public void fromBytes(ByteBuf buffer) {
      this.pitch = buffer.readFloat();
      this.yaw = buffer.readFloat();
   }

   public static class Handler implements IMessageHandler<PacketSetClientPlayerFacing, IMessage> {
      public IMessage onMessage(PacketSetClientPlayerFacing message, MessageContext ctx) {
         EntityPlayer player = Witchery.proxy.getPlayer(ctx);
         player.func_70080_a(player.field_70165_t, player.field_70163_u, player.field_70161_v, message.yaw, message.pitch);
         return null;
      }
   }
}
