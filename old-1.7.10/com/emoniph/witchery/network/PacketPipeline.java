package com.emoniph.witchery.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class PacketPipeline {
   private SimpleNetworkWrapper CHANNEL;

   public void preInit() {
      this.CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("witchery".toLowerCase());
   }

   public void init() {
      this.CHANNEL.registerMessage(PacketBrewPrepared.Handler.class, PacketBrewPrepared.class, 1, Side.SERVER);
      this.CHANNEL.registerMessage(PacketParticles.Handler.class, PacketParticles.class, 2, Side.CLIENT);
      this.CHANNEL.registerMessage(PacketCamPos.Handler.class, PacketCamPos.class, 3, Side.CLIENT);
      this.CHANNEL.registerMessage(PacketItemUpdate.Handler.class, PacketItemUpdate.class, 4, Side.SERVER);
      this.CHANNEL.registerMessage(PacketPlayerStyle.Handler.class, PacketPlayerStyle.class, 5, Side.CLIENT);
      this.CHANNEL.registerMessage(PacketPlayerSync.Handler.class, PacketPlayerSync.class, 6, Side.CLIENT);
      this.CHANNEL.registerMessage(PacketPushTarget.Handler.class, PacketPushTarget.class, 7, Side.CLIENT);
      this.CHANNEL.registerMessage(PacketSound.Handler.class, PacketSound.class, 8, Side.CLIENT);
      this.CHANNEL.registerMessage(PacketSpellPrepared.Handler.class, PacketSpellPrepared.class, 9, Side.SERVER);
      this.CHANNEL.registerMessage(PacketClearFallDamage.Handler.class, PacketClearFallDamage.class, 10, Side.SERVER);
      this.CHANNEL.registerMessage(PacketSyncEntitySize.Handler.class, PacketSyncEntitySize.class, 11, Side.CLIENT);
      this.CHANNEL.registerMessage(PacketSyncMarkupBook.Handler.class, PacketSyncMarkupBook.class, 12, Side.SERVER);
      this.CHANNEL.registerMessage(PacketExtendedPlayerSync.Handler.class, PacketExtendedPlayerSync.class, 13, Side.CLIENT);
      this.CHANNEL.registerMessage(PacketHowl.Handler.class, PacketHowl.class, 14, Side.SERVER);
      this.CHANNEL.registerMessage(PacketExtendedVillagerSync.Handler.class, PacketExtendedVillagerSync.class, 15, Side.CLIENT);
      this.CHANNEL.registerMessage(PacketSelectPlayerAbility.Handler.class, PacketSelectPlayerAbility.class, 16, Side.SERVER);
      this.CHANNEL.registerMessage(PacketExtendedEntityRequestSyncToClient.Handler.class, PacketExtendedEntityRequestSyncToClient.class, 17, Side.SERVER);
      this.CHANNEL.registerMessage(PacketPartialExtendedPlayerSync.Handler.class, PacketPartialExtendedPlayerSync.class, 18, Side.CLIENT);
      this.CHANNEL.registerMessage(PacketSetClientPlayerFacing.Handler.class, PacketSetClientPlayerFacing.class, 19, Side.CLIENT);
   }

   public void sendTo(IMessage message, EntityPlayer player) {
      if (player instanceof EntityPlayerMP) {
         this.CHANNEL.sendTo(message, (EntityPlayerMP)player);
      }

   }

   public void sendTo(IMessage message, EntityPlayerMP player) {
      this.CHANNEL.sendTo(message, player);
   }

   public void sendToServer(IMessage message) {
      this.CHANNEL.sendToServer(message);
   }

   public void sendToAllAround(IMessage message, TargetPoint targetPoint) {
      this.CHANNEL.sendToAllAround(message, targetPoint);
   }

   public void sendToAll(IMessage message) {
      this.CHANNEL.sendToAll(message);
   }

   public void sendToDimension(IMessage message, int dimensionId) {
      this.CHANNEL.sendToDimension(message, dimensionId);
   }

   public void sendTo(Packet packet, EntityPlayer player) {
      if (player instanceof EntityPlayerMP) {
         EntityPlayerMP mp = (EntityPlayerMP)player;
         mp.field_71135_a.func_147359_a(packet);
      }

   }

   public void sendToDimension(Packet packet, World world) {
      Iterator i$ = world.field_73010_i.iterator();

      while(i$.hasNext()) {
         Object obj = i$.next();
         if (obj instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP)obj;
            mp.field_71135_a.func_147359_a(packet);
         }
      }

   }

   public void sendToAll(Packet packet) {
      WorldServer[] arr$ = MinecraftServer.func_71276_C().field_71305_c;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         WorldServer world = arr$[i$];
         this.sendToDimension(packet, world);
      }

   }

   public void sendToAllAround(Packet packet, World world, TargetPoint point) {
      double RANGE_SQ = point.range * point.range;
      Iterator i$ = world.field_73010_i.iterator();

      while(i$.hasNext()) {
         Object obj = i$.next();
         if (obj instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP)obj;
            if (mp.func_70092_e(point.x, point.y, point.z) <= RANGE_SQ) {
               mp.field_71135_a.func_147359_a(packet);
            }
         }
      }

   }
}
