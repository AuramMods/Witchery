package art.arcane.witchery.event;

import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.CoffinBlock;
import art.arcane.witchery.brewing.GrotesqueEffect;
import art.arcane.witchery.brewing.WakingNightmareEffect;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.entity.VillageGuard;
import art.arcane.witchery.entity.VillageGuardRecruitment;
import art.arcane.witchery.entity.WitcheryBolt;
import art.arcane.witchery.entity.WitchHunterPursuit;
import art.arcane.witchery.infusion.CreaturePower;
import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.infusion.InfusedBrews;
import art.arcane.witchery.infusion.OverworldInfusion;
import art.arcane.witchery.item.DeathProtectionPoppetItem;
import art.arcane.witchery.item.CaneSwordItem;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.VampiricPoppetItem;
import art.arcane.witchery.item.GlassGobletItem;
import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.item.HuntsmanSpearItem;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import art.arcane.witchery.vampirism.Vampirism;
import art.arcane.witchery.vampirism.BloodFeeding;
import art.arcane.witchery.vampirism.VampireAbilities;
import art.arcane.witchery.vampirism.VampireFlight;
import art.arcane.witchery.vampirism.VampireMortality;
import art.arcane.witchery.vampirism.VampireVillages;
import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.prediction.PredictedCourtship;
import art.arcane.witchery.prediction.Predictions;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.ritual.DroppedWaystones;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.MirrorTravel;
import art.arcane.witchery.world.ScryingView;
import art.arcane.witchery.world.TormentTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import art.arcane.witchery.world.WitcheryDimensionTravelHooks;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WitcheryEventHooks {
    private WitcheryEventHooks() {
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        ServerLevel level = (ServerLevel) event.getLevel();
        DreamCatcherBlock.finishSleep(level);
        if (level.players().stream().filter(player -> !player.isSpectator()).allMatch(player ->
                player.isSleepingLongEnough() && Vampirism.level(player) > 0
                        && player.getSleepingPos().map(pos -> level.getBlockState(pos).getBlock() instanceof CoffinBlock).orElse(false))) {
            event.setTimeAddition(event.getNewTime() - 11_000);
        }
    }

    @SubscribeEvent
    public static void onCoffinSleepTime(SleepingTimeCheckEvent event) {
        var player = event.getEntity();
        if (Vampirism.level(player) > 0 && event.getSleepingLocation().map(pos ->
                player.level().getBlockState(pos).getBlock() instanceof CoffinBlock).orElse(false)) {
            event.setResult(player.level().isDay() ? Event.Result.ALLOW : Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        WitcheryPlayerDataProvider.attach(event);
    }

    @SubscribeEvent
    public static void onWaystoneToss(ItemTossEvent event) {
        DroppedWaystones.markToss(event.getEntity());
    }

    @SubscribeEvent
    public static void onWaystonePickup(EntityItemPickupEvent event) {
        if (DroppedWaystones.isTossedWaystone(event.getItem()) && event.getEntity().distanceToSqr(event.getItem()) > 0.5625) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.level().dimension().equals(Level.NETHER)) Predictions.onNetherVisit(serverPlayer);
            GrotesqueEffect.restoreLegacyBrew(serverPlayer);
            InfusionEnergy.sync(serverPlayer);
            VampireFlight.restore(serverPlayer);
            Lycanthropy.restore(serverPlayer);
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
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            InfusionEnergy.sync(serverPlayer);
            VampireFlight.restore(serverPlayer);
            Lycanthropy.restore(serverPlayer);
            WitcheryPlayerDataProvider.get(serverPlayer).ifPresent(data ->
                    WitcheryNetwork.sendPartialExtendedPlayerSync(serverPlayer, data));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ScryingView.close(player);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer target) {
            WitcheryPlayerDataProvider.get(target).ifPresent(data -> {
                if (data.isGhost() || data.getNightmareLevel() > 0 || data.getCreatureTypeOrdinal() != 0) {
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
        try {
            WitcheryPlayerDataProvider.get(event.getOriginal()).ifPresent(oldData ->
                    WitcheryPlayerDataProvider.get(event.getEntity()).ifPresent(newData -> newData.copyFrom(oldData)));
        } finally {
            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        WitcheryDimensionTravelHooks.onEntityTravelToDimension(event.getEntity(), event.getDimension());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ScryingView.close(serverPlayer);
            WitcheryDimensionTravelHooks.onPlayerChangedDimension(serverPlayer, event.getFrom(), event.getTo());
            VampireFlight.restore(serverPlayer);
            Lycanthropy.restore(serverPlayer);
            if (event.getTo().equals(Level.NETHER)) Predictions.onNetherVisit(serverPlayer);
            WitcheryPlayerDataProvider.get(serverPlayer).ifPresent(data ->
                    WitcheryNetwork.sendPartialExtendedPlayerSync(serverPlayer, data));
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
        ScryingView.tick(serverPlayer);
        InfusedBrews.tick(serverPlayer);
        Vampirism.tick(serverPlayer);
        Lycanthropy.tick(serverPlayer);
        HunterClothingItem.tick(serverPlayer);
        WitchHunterPursuit.tick(serverPlayer);
        if (InfusionEnergy.type(serverPlayer) == InfernalInfusion.ID) CreaturePower.tickFlight(serverPlayer);
        if (DreamTravel.processRequests(serverPlayer) || MirrorTravel.processPlayerCrossing(serverPlayer)
                || TormentTravel.process(serverPlayer)) {
            return;
        }
        DreamTravel.spawnNightmare(serverPlayer);
        WakingNightmareEffect.tickCurse(serverPlayer);
        Predictions.tick(serverPlayer);

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
    public static void onVillagerTick(LivingTickEvent event) {
        if (event.getEntity() instanceof Villager villager && villager.level() instanceof ServerLevel level) {
            PredictedCourtship.tick(villager, level);
            BloodFeeding.tickVillager(villager, level);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (VampireFlight.isBat(player)) {
                event.setCanceled(true);
                return;
            }
            if (Lycanthropy.isShifted(player)) Lycanthropy.fall(player, event);
            int infusion = InfusionEnergy.type(player);
            if (infusion == OverworldInfusion.ID) OverworldInfusion.fall(player, event);
            if (infusion == InfernalInfusion.ID) {
                CreaturePower power = CreaturePower.forId(CreaturePower.activeId(player));
                if (power != null) power.fall(player, event);
            }
        }
    }

    @SubscribeEvent
    public static void onWolfJump(LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player && Lycanthropy.isShifted(player)) Lycanthropy.jump(player);
    }

    @SubscribeEvent
    public static void restoreMinion(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel && event.getEntity() instanceof Mob mob) InfernalInfusion.restore(mob);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void trackVillagerAlias(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Mob mob) WerewolfVillagers.track(level, mob);
    }

    @SubscribeEvent
    public static void removeVillagerAlias(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Mob mob) WerewolfVillagers.untrack(level, mob);
    }

    @SubscribeEvent
    public static void protectPlayerFromTargeting(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob mob && event.getNewTarget() instanceof ServerPlayer player
                && (WitchClothingItem.deters(mob, player)
                || InfernalInfusion.owns(mob, player) && mob.hasEffect(WitcheryEffects.ENSLAVED.get()))) {
            event.setNewTarget(null);
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onVampireDeath(LivingDeathEvent event) {
        if (VampireMortality.preventDeath(event.getEntity(), event.getSource())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onVampireDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Vampirism.level(player) > 0) {
            int lifespan = WitcheryServerConfig.VAMPIRE_DEATH_ITEM_KEEP_ALIVE_MINS.get() * 60 * 20;
            event.getDrops().forEach(drop -> drop.lifespan = lifespan);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer viewer) ScryingView.close(viewer);
        if (event.getEntity() instanceof net.minecraft.world.entity.animal.Chicken chicken
                && event.getSource().getEntity() instanceof ServerPlayer player) GlassGobletItem.onChickenOffering(chicken, player);
        if (event.getEntity() instanceof ServerPlayer player && !player.getAbilities().instabuild
                && (player.level().dimension().equals(WitcheryDimensions.DREAM) || DreamTravel.isGhost(player))
                && DreamTravel.isSpiritWalking(player)) {
            event.setCanceled(true);
            player.setHealth(1);
            DreamTravel.requestWake(player);
            return;
        }
        FamiliarBinding.handleDeath(event);
        if (!event.isCanceled() && event.getSource().getEntity() instanceof ServerPlayer killer) {
            Vampirism.onKilled(event.getEntity(), killer, event.getSource());
            Lycanthropy.onKilled(event.getEntity(), killer);
        }
    }

    @SubscribeEvent
    public static void blockWithCaneSword(ShieldBlockEvent event) {
        if (CaneSwordItem.isDrawn(event.getEntity().getUseItem())) {
            event.setBlockedDamage(event.getBlockedDamage() * 0.5F);
            event.setShieldTakesDamage(false);
        }
    }

    @SubscribeEvent
    public static void showCaneReserve(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof CaneSwordItem) || event.getEntity() == null) return;
        WitcheryPlayerDataProvider.get(event.getEntity()).ifPresent(data -> event.getToolTip().add(
                Component.translatable("item.witchery.canesword.reserve", data.getBloodReserve())));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO) && HunterClothingItem.resistsMagic(event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().getDirectEntity() instanceof WitcheryBolt bolt) {
            event.setAmount(bolt.modifyDamage(event.getEntity(), event.getAmount()));
        }
        Lycanthropy.adjustDamage(event);
        if (event.isCanceled()) return;
        if (event.getAmount() >= 1 && BloodFeeding.paralysis(event.getEntity(), 4)) {
            event.getEntity().removeEffect(WitcheryEffects.PARALYSIS.get());
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            if (InfusionEnergy.type(player) == InfernalInfusion.ID) {
                CreaturePower power = CreaturePower.forId(CreaturePower.activeId(player));
                if (power != null) power.hurt(player, event);
            }
            boolean ignoreWards = WitcheryBolt.poweredAntiMagic(event.getSource()) || Lycanthropy.isShifted(player);
            if (!event.isCanceled() && !ignoreWards && WitchClothingItem.evadeWithBabaHat(player, event.getSource())) {
                event.setCanceled(true);
                return;
            }
            if (!event.isCanceled() && !ignoreWards) VampiricPoppetItem.transferDamage(player, event);
            if (!event.isCanceled()) FamiliarBinding.shareDamage(player, event);
            if (!event.isCanceled()) {
                HuntsmanSpearItem.defend(player, event.getSource());
                Predictions.onHurt(player, event.getSource());
            }
            DeathProtectionPoppetItem.repairArmor(player);
        }
    }

    @SubscribeEvent
    public static void repairHeldTool(PlayerInteractEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) DeathProtectionPoppetItem.repairHeldTool(player);
    }

    @SubscribeEvent
    public static void preventVillageFriendlyFire(LivingAttackEvent event) {
        if (event.getEntity() instanceof Villager && VillageGuard.isVillageDefender(event.getSource().getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void preventHunterEnderPearlTravel(EntityTeleportEvent.EnderPearl event) {
        if (HunterClothingItem.fullSet(event.getPlayer(), HunterClothingItem.Upgrade.PLAIN)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void protectFromLethalDamage(LivingDamageEvent event) {
        if (VampireMortality.preventLethalDamage(event.getEntity(), event.getSource(), event.getAmount())) {
            event.setCanceled(true);
            return;
        }
        if (!Lycanthropy.isShifted(event.getEntity())) DeathProtectionPoppetItem.protect(event);
        if (event.isCanceled() || event.getAmount() <= 0) return;
        var source = event.getSource();
        if (event.getEntity() instanceof Villager villager && villager.level() instanceof ServerLevel world
                && source.getEntity() instanceof LivingEntity attacker && !source.is(WitcheryDamageTypes.VAMPIRE_BITE)) {
            VampireVillages.recordAggressor(world, villager, attacker);
        }
        if (source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK) && source.getDirectEntity() instanceof ServerPlayer player) {
            Vampirism.knockBack(player, event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        if (VampireAbilities.selected(event.getEntity()) > 0) {
            if (event.getEntity() instanceof ServerPlayer player) VampireAbilities.interact(player, event.getTarget());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            return;
        }
        if (event.getTarget() instanceof Villager villager
                && VillageGuardRecruitment.isOffer(event.getEntity(), villager, event.getHand())) {
            if (event.getEntity() instanceof ServerPlayer player) VillageGuardRecruitment.offer(player, villager);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            event.setCanceled(true);
            return;
        }
        var held = event.getItemStack();
        if (held.getItem() instanceof GlassGobletItem && GlassGobletItem.filled(held)
                && event.getTarget() instanceof Mob target && (target instanceof Villager || target instanceof VillageGuard)) {
            if (event.getEntity() instanceof ServerPlayer player) GlassGobletItem.offerToResident(player, target, held);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            return;
        }
        if (event.getItemStack().getItem() instanceof TaglockItem kit && event.getTarget() instanceof LivingEntity target) {
            InteractionResult result = kit.collectFromEntity(event.getEntity(), event.getHand(), target);
            if (result.consumesAction()) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onVampireInteraction(PlayerInteractEvent event) {
        if (!event.isCancelable()) return;
        var player = event.getEntity();
        if (BloodFeeding.paralysis(player, 4) || (event instanceof PlayerInteractEvent.RightClickBlock
                || event instanceof PlayerInteractEvent.RightClickItem) && VampireAbilities.selected(player) > 0) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onParalysedAttack(AttackEntityEvent event) {
        if (BloodFeeding.paralysis(event.getEntity(), 4) || VampireFlight.isBat(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    @SuppressWarnings("removal")
    public static void resizeCreatureForm(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (VampireFlight.isBat(player)) {
            event.setNewSize(VampireFlight.BAT_DIMENSIONS);
            event.setNewEyeHeight(VampireFlight.BAT_EYE_HEIGHT);
        } else if (Lycanthropy.isWolf(player)) {
            event.setNewSize(Lycanthropy.WOLF_DIMENSIONS);
            event.setNewEyeHeight(Lycanthropy.WOLF_EYE_HEIGHT);
        }
    }

    @SubscribeEvent
    public static void shatterStoneAsVampire(PlayerEvent.BreakSpeed event) {
        if (Vampirism.canShatter(event.getEntity(), event.getState())) event.setNewSpeed(Math.max(event.getNewSpeed(), 1000));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void harvestStoneAsVampire(PlayerEvent.HarvestCheck event) {
        if (!event.canHarvest() && Vampirism.canShatter(event.getEntity(), event.getTargetBlock())) event.setCanHarvest(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void chargeForShatteringStone(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)
                || !Vampirism.canShatter(player, event.getState())) return;
        var server = level.getServer();
        server.tell(new TickTask(server.getTickCount(), () -> {
            if (!event.isCanceled() && level.getBlockState(event.getPos()) != event.getState()) player.causeFoodExhaustion(10F);
        }));
    }

    @SubscribeEvent
    public static void onVampireStartEating(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getItem().isEdible() && Vampirism.level(player) > 0) {
            WitcheryPlayerDataProvider.get(player).ifPresent(data -> data.beginEating(player.getFoodData().getFoodLevel()));
        }
    }

    @SubscribeEvent
    public static void onVampireFinishEating(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player && Vampirism.level(player) > 0) {
            WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
                var food = player.getFoodData();
                if (data.gainedFood(food.getFoodLevel())) {
                    food.setFoodLevel(0);
                    food.setSaturation(0);
                }
            });
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
