package art.arcane.witchery.event;

import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.brewing.GrotesqueEffect;
import art.arcane.witchery.brewing.WakingNightmareEffect;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.infusion.OverworldInfusion;
import art.arcane.witchery.item.DeathProtectionPoppetItem;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.MirrorTravel;
import art.arcane.witchery.world.TormentTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import art.arcane.witchery.world.WitcheryDimensionTravelHooks;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WitcheryEventHooks {
    private WitcheryEventHooks() {
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        DreamCatcherBlock.finishSleep((ServerLevel) event.getLevel());
    }

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        WitcheryPlayerDataProvider.attach(event);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GrotesqueEffect.restoreLegacyBrew(serverPlayer);
            InfusionEnergy.sync(serverPlayer);
            WitcheryPlayerDataProvider.get(serverPlayer).ifPresent(data -> {
                if (data.getOtherPlayerSkin().isEmpty()) {
                    data.applyPlayerStyle(
                            serverPlayer.getGameProfile().getName(),
                            data.getGrotesqueTicks(),
                            data.getNightmareLevel(),
                            data.isGhost(),
                            data.getCreatureTypeOrdinal(),
                            data.getHumanBlood()
                    );
                }
                data.setInitialized(true);
                data.bumpSyncRevision();
                WitcheryNetwork.sendExtendedPlayerSync(serverPlayer, data);
                WitcheryNetwork.sendPlayerSync(serverPlayer, data);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) InfusionEnergy.sync(serverPlayer);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer target) {
            WitcheryPlayerDataProvider.get(target).ifPresent(data -> {
                if (data.isGhost() || data.getNightmareLevel() > 0) {
                    WitcheryNetwork.sendPlayerStyle(viewer, target.getGameProfile().getName(), data.getGrotesqueTicks(),
                            data.getNightmareLevel(), data.isGhost(), data.getCreatureTypeOrdinal(),
                            data.getHumanBlood(), data.getOtherPlayerSkin());
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        WitcheryPlayerDataProvider.get(event.getOriginal()).ifPresent(oldData ->
                WitcheryPlayerDataProvider.get(event.getEntity()).ifPresent(newData -> newData.copyFrom(oldData)));
        event.getOriginal().invalidateCaps();

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            WitcheryPlayerDataProvider.get(serverPlayer).ifPresent(data ->
                    WitcheryNetwork.sendPartialExtendedPlayerSync(serverPlayer, data));
        }
    }

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        WitcheryDimensionTravelHooks.onEntityTravelToDimension(event.getEntity(), event.getDimension());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            WitcheryDimensionTravelHooks.onPlayerChangedDimension(serverPlayer, event.getFrom(), event.getTo());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (event.player.level().isClientSide()) {
            return;
        }

        if (!(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (DreamTravel.processRequests(serverPlayer) || MirrorTravel.processPlayerCrossing(serverPlayer)
                || TormentTravel.process(serverPlayer)) {
            return;
        }
        DreamTravel.spawnNightmare(serverPlayer);
        WakingNightmareEffect.tickCurse(serverPlayer);

        if (serverPlayer.level().getGameTime() % 20 == 0 && serverPlayer.hasEffect(WitcheryEffects.DEPTHS.get())) {
            if (serverPlayer.isEyeInFluid(FluidTags.WATER)) {
                serverPlayer.setAirSupply(serverPlayer.getMaxAirSupply());
                serverPlayer.removeEffect(MobEffects.WITHER);
            } else if (!serverPlayer.hasEffect(MobEffects.WITHER)) {
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            }
        }
    }

    @SubscribeEvent
    public static void onServerLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level) {
            TormentTravel.tickEncounters(level);
            OverworldInfusion.tick(level);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && InfusionEnergy.type(player) == OverworldInfusion.ID) {
            OverworldInfusion.fall(player, event);
        }
    }

    @SubscribeEvent
    public static void onDreamAtmosphereTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)
                || !level.dimension().equals(WitcheryDimensions.DREAM) || level.getGameTime() % 20 != 0
                || level.players().isEmpty()) {
            return;
        }
        int mode = DreamTravel.isDemonicNightmare(level) ? 2 : DreamTravel.isNightmare(level) ? 1 : 0;
        for (ServerPlayer player : level.players()) {
            WitcheryNetwork.sendDreamAtmosphere(player, mode);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.getAbilities().instabuild
                && (player.level().dimension().equals(WitcheryDimensions.DREAM) || DreamTravel.isGhost(player))
                && DreamTravel.isSpiritWalking(player)) {
            event.setCanceled(true);
            player.setHealth(1);
            DreamTravel.requestWake(player);
            return;
        }
        FamiliarBinding.handleDeath(event);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFamiliarDamage(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer server) FamiliarBinding.shareDamage(server, event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void protectFromLethalDamage(LivingDamageEvent event) {
        DeathProtectionPoppetItem.protect(event);
    }

    @SubscribeEvent
    public static void onPlayerInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().getItem() instanceof TaglockItem kit && event.getTarget() instanceof LivingEntity target) {
            InteractionResult result = kit.collectFromEntity(event.getEntity(), event.getHand(), target);
            if (result.consumesAction()) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
        }
    }

    @SubscribeEvent
    public static void onMirrorEscapePhrase(ServerChatEvent event) {
        if (MirrorTravel.queueEscapePhrase(event.getPlayer(), event.getRawText())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        // Placeholder hook for world/runtime registry bootstrap migration.
    }
}
