package com.emoniph.witchery.common;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.dimension.WorldProviderDreamWorld;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.network.PacketPlayerStyle;
import com.emoniph.witchery.util.Config;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.Side;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class ServerTickEvents {
   public static final ArrayList<ServerTickEvents.ServerTickTask> TASKS = new ArrayList();

   @SubscribeEvent
   public void onServerTick(ServerTickEvent event) {
      if (event.side == Side.SERVER && event.phase == Phase.START && TASKS.size() > 0) {
         ArrayList<ServerTickEvents.ServerTickTask> completedTasks = new ArrayList();
         Iterator i$ = TASKS.iterator();

         ServerTickEvents.ServerTickTask task;
         while(i$.hasNext()) {
            task = (ServerTickEvents.ServerTickTask)i$.next();
            if (task.process()) {
               completedTasks.add(task);
            }
         }

         i$ = completedTasks.iterator();

         while(i$.hasNext()) {
            task = (ServerTickEvents.ServerTickTask)i$.next();
            TASKS.remove(task);
         }
      }

   }

   @SubscribeEvent
   public void onPlayerTick(PlayerTickEvent event) {
      if (event.side == Side.SERVER && !event.player.field_70170_p.field_72995_K) {
         if (event.phase == Phase.START) {
            Collection<PotionEffect> activeEffects = event.player.func_70651_bq();
            ExtendedPlayer playerExt = ExtendedPlayer.get(event.player);
            if (playerExt != null) {
               playerExt.updateWorship();
               if (activeEffects.size() > 0) {
                  playerExt.cacheIncurablePotionEffect(activeEffects);
               }

               playerExt.checkSleep(true);
            }
         } else if (event.phase == Phase.END) {
            ExtendedPlayer playerExt = ExtendedPlayer.get(event.player);
            if (playerExt != null) {
               playerExt.restoreIncurablePotionEffects();
               playerExt.checkSleep(false);
            }
         }
      }

   }

   @SubscribeEvent
   public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      EntityPlayer player = event.player;
      World world = event.player.field_70170_p;
      Shapeshift.INSTANCE.initCurrentShift(player);
      Infusion.syncPlayer(world, player);
      ExtendedPlayer.get(player).scheduleSync();
      Witchery.packetPipeline.sendToDimension(new PacketPlayerStyle(player), world.field_73011_w.field_76574_g);
      if (player.field_71093_bK != Config.instance().dimensionDreamID && WorldProviderDreamWorld.getPlayerIsSpiritWalking(player) && !WorldProviderDreamWorld.getPlayerIsGhost(player)) {
         WorldProviderDreamWorld.setPlayerMustAwaken(player, true);
      } else if (player.field_71093_bK == Config.instance().dimensionDreamID && !WorldProviderDreamWorld.getPlayerIsSpiritWalking(player)) {
         WorldProviderDreamWorld.changeDimension(player, 0);
         WorldProviderDreamWorld.findTopAndSetPosition(player.field_70170_p, player);
      }

   }

   @SubscribeEvent
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      if (!event.player.field_70170_p.field_72995_K) {
         NBTTagCompound nbtPlayer = Infusion.getNBT(event.player);
         if (nbtPlayer.func_74764_b("WITCPoSpawn")) {
            NBTTagList nbtRestoredEffectList = nbtPlayer.func_150295_c("WITCPoSpawn", 10);
            if (nbtRestoredEffectList.func_74745_c() > 0) {
               for(int i = 0; i < nbtRestoredEffectList.func_74745_c(); ++i) {
                  PotionEffect restoredEffect = PotionEffect.func_82722_b(nbtRestoredEffectList.func_150305_b(i));
                  if (!event.player.func_82165_m(restoredEffect.func_76456_a())) {
                     event.player.func_70690_d(restoredEffect);
                  }
               }
            }

            nbtPlayer.func_82580_o("WITCPoSpawn");
         }

         EntityPlayer player = event.player;
         World world = event.player.field_70170_p;
         Shapeshift.INSTANCE.initCurrentShift(player);
         Infusion.syncPlayer(world, player);
         ExtendedPlayer.get(player).scheduleSync();
         Witchery.packetPipeline.sendToDimension(new PacketPlayerStyle(player), world.field_73011_w.field_76574_g);
      }

   }

   @SubscribeEvent
   public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      if (!event.player.field_70170_p.field_72995_K) {
         EntityPlayer player = event.player;
         if (player != null && player.field_70170_p != null && !player.field_70170_p.field_72995_K) {
            long nextUpdate = MinecraftServer.func_130071_aq() + 1500L;
            ExtendedPlayer.get(player).scheduleSync();
            Iterator i$ = player.field_70170_p.field_73010_i.iterator();

            while(i$.hasNext()) {
               Object obj = i$.next();
               EntityPlayer otherPlayer = (EntityPlayer)obj;
               NBTTagCompound nbtOtherPlayer = Infusion.getNBT(otherPlayer);
               if (otherPlayer != player) {
                  nbtOtherPlayer.func_74772_a("WITCResyncLook", nextUpdate);
               }
            }

            NBTTagCompound nbtPlayer = Infusion.getNBT(player);
            Witchery.packetPipeline.sendToDimension(new PacketPlayerStyle(player), player.field_70170_p.field_73011_w.field_76574_g);
            if (player.field_71093_bK != Config.instance().dimensionDreamID && WorldProviderDreamWorld.getPlayerIsSpiritWalking(player) && !WorldProviderDreamWorld.getPlayerIsGhost(player)) {
               WorldProviderDreamWorld.setPlayerMustAwaken(player, true);
            }
         }
      }

   }

   public abstract static class ServerTickTask {
      protected final World world;

      public ServerTickTask(World world) {
         this.world = world;
      }

      public abstract boolean process();
   }
}
