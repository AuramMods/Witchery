package art.arcane.witchery.block.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.RiteHeartBlock;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.entity.CovenWitch;
import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.entity.Reflection;
import art.arcane.witchery.entity.SpectralFamiliar;
import art.arcane.witchery.entity.WereVillager;
import art.arcane.witchery.entity.WitchHunterPursuit;
import art.arcane.witchery.entity.Wolfman;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.item.CircleTalismanItem;
import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.item.PoppetProtectionItem;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.VoodooPoppetItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.item.SpectralStoneItem;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.prediction.Predictions;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.ritual.FetishBinding;
import art.arcane.witchery.ritual.ClimateChange;
import art.arcane.witchery.ritual.CookingRite;
import art.arcane.witchery.ritual.EarthRites;
import art.arcane.witchery.ritual.Banishment;
import art.arcane.witchery.ritual.GlyphicTransformation;
import art.arcane.witchery.ritual.ItemRiteRecipe;
import art.arcane.witchery.ritual.LandscapeRites;
import art.arcane.witchery.ritual.PriorIncarnation;
import art.arcane.witchery.ritual.ProtectionRites;
import art.arcane.witchery.ritual.RainOfToads;
import art.arcane.witchery.ritual.RiteActivation;
import art.arcane.witchery.ritual.RiteOfTransposition;
import art.arcane.witchery.ritual.WeatherRites;
import art.arcane.witchery.vampirism.Vampirism;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WaystoneTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class RiteHeartBlockEntity extends BlockEntity {
    private static final int STEP_TICKS = 20;
    // The least common multiple of the foreground rite intervals.
    private static final int GLYPH_CLOCK_PERIOD = 60;
    private static final int MANIFEST_RANGE = 5;
    private static final int MANIFEST_DURATION = 150;
    private static final int INFUSION_RANGE = 4;
    private static final int INFUSION_CAPACITY = 200;
    private static final int VAMPIRE_CURE_RANGE = 8;
    private static final int WOLF_CURE_RANGE = 8;
    private final List<Sacrifice> sacrificed = new ArrayList<>();
    private final Deque<ResourceLocation> queuedRites = new ArrayDeque<>();
    private final List<RiteUpkeep> upkeep = new ArrayList<>();

    private record Sacrifice(ItemStack stack, BlockPos origin) {
    }

    private record SavedUpkeep(RiteUpkeep rite, int order) {}
    private enum FinalStep { INVALID, LIVING, WAYSTONE, ALTAR, EFFECT }
    @Nullable
    private ResourceLocation activeRite;
    @Nullable
    private UUID initiator;
    private int stepTicks;
    private int glyphPhase;
    private boolean resumeRelativePulse;
    private int offeringStep;
    private boolean livingOffered;
    private boolean altarPowerOffered;
    private int covenSize;
    private int covenSummoned;
    private final List<Runnable> terrainChanges = new ArrayList<>();
    private boolean legacyOfferingReturns;
    private boolean legacyWaystoneMatch;
    @Nullable private BlockPos waystoneTarget;
    @Nullable private WeatherRites.Storm storm;
    private int toadRainStage = -1;

    public RiteHeartBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.CIRCLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RiteHeartBlockEntity heart) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        heart.glyphPhase = (heart.glyphPhase + 1) % GLYPH_CLOCK_PERIOD;
        heart.restoreOfferingReturns(server);
        heart.updateRedstone(server, state);
        heart.updateActiveState(server);
        if (!heart.upkeep.isEmpty()) {
            if (heart.upkeep.removeIf(rite -> !rite.tick(server, pos, heart.terrainChanges::add))) heart.updateActiveState(server);
            level.blockEntityChanged(pos);
        }
        if (heart.activeRite != null) {
            heart.tickCast(server);
            level.blockEntityChanged(pos);
        }
        // Moving Earth can relocate this block entity. Finish bookkeeping before it copies the heart's state.
        heart.terrainChanges.forEach(Runnable::run);
        heart.terrainChanges.clear();
    }

    public void updateRedstone(ServerLevel level, BlockState state) {
        BlockPos pos = getBlockPos();
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == state.getValue(RiteHeartBlock.POWERED)) return;
        level.setBlock(pos, state.setValue(RiteHeartBlock.POWERED, powered), Block.UPDATE_CLIENTS);
        if (powered) toggle(level, null);
    }

    private int relativeInterval() {
        return storm == null && toadRainStage < 0 ? STEP_TICKS : WeatherRites.STORM_INTERVAL;
    }

    private boolean usesGlyphClock() {
        return activeRite != null && RiteActivation.legacyId(activeRite) != Integer.MAX_VALUE;
    }

    private void tickCast(ServerLevel level) {
        boolean clocked = usesGlyphClock();
        ItemRiteRecipe rite = level.getRecipeManager().byKey(activeRite).orElse(null) instanceof ItemRiteRecipe recipe ? recipe : null;
        if (clocked && !resumeRelativePulse) {
            if (glyphPhase % clockInterval(rite) != 0) return;
        } else {
            if (++stepTicks < relativeInterval()) return;
            stepTicks = 0;
            if (resumeRelativePulse) {
                // A prior Forge save knows the delay to its next step, but has no glyph phase.
                glyphPhase = clockInterval(rite) % GLYPH_CLOCK_PERIOD;
                resumeRelativePulse = false;
            }
        }
        if (storm != null) advanceStorm(level);
        else if (toadRainStage >= 0) advanceToadRain(level);
        else advance(level, rite);
    }

    private int clockInterval(@Nullable ItemRiteRecipe rite) {
        if (storm != null || toadRainStage >= 0) return WeatherRites.STORM_INTERVAL;
        if (rite == null || covenSummoned < covenSize || offeringStep < rite.sacrifices().size()
                || finalStep(rite, rite.sacrifices().size()) != FinalStep.EFFECT) return STEP_TICKS;
        return switch (rite.action()) {
            case RAISE_EARTH, RAISE_EARTH_MEDIUM, RAISE_EARTH_LARGE -> 5;
            case TRANSPOSE_ORES -> RiteOfTransposition.ORE_PULSE_TICKS;
            case RAISE_VOLCANO, RAISE_PORTABLE_VOLCANO -> 15;
            case ECLIPSE, CALL_STORM, CALL_GREATER_STORM, CALL_PORTABLE_STORM, RAIN_TOADS,
                    GLYPHIC_TRANSFORMATION -> WeatherRites.STORM_INTERVAL;
            case CALL_BEASTS -> 60;
            default -> STEP_TICKS;
        };
    }

    public void toggle(ServerLevel level, @Nullable Player player) {
        toggle(level, player, false);
    }

    public void toggle(ServerLevel level, @Nullable Player player, boolean callCoven) {
        if (isRiteActive()) {
            upkeep.clear();
            queuedRites.clear();
            abort(level, "message.witchery.rite.cancelled");
            return;
        }
        BlockPos pos = getBlockPos();
        var selection = RiteActivation.select(level, pos);
        if (!selection.rites().isEmpty()) {
            queuedRites.addAll(selection.rites());
            initiator = player == null ? null : player.getUUID();
            covenSize = callCoven && player instanceof ServerPlayer serverPlayer ? CovenWitch.covenSize(serverPlayer) : 0;
            if (covenSize > 0 && player instanceof ServerPlayer serverPlayer) CovenWitch.dismissNearbyForRite(serverPlayer);
            covenSummoned = 0;
            nextCast(level);
            return;
        }
        if (player != null) {
            player.displayClientMessage(Component.translatable(selection.failure()), true);
        }
        failSound(level);
    }

    private void advance(ServerLevel level, @Nullable ItemRiteRecipe rite) {
        if (rite == null) {
            abort(level, "message.witchery.rite.unknown");
            return;
        }
        if (covenSummoned < covenSize) {
            ServerPlayer caller = initiator == null ? null : level.getServer().getPlayerList().getPlayer(initiator);
            if (caller != null && caller.level() == level) CovenWitch.summonToRite(caller, getBlockPos(), covenSummoned);
            covenSummoned++;
            return;
        }
        BlockPos pos = getBlockPos();
        if (legacyWaystoneMatch) {
            legacyWaystoneMatch = false;
            if (offeringStep == 0 && !rite.hasRequiredOfferings(level, pos)
                    && level.getRecipeManager().byKey(RiteActivation.TRAVEL_TO_CREATURE).orElse(null) instanceof ItemRiteRecipe creatureRite
                    && creatureRite.hasRequiredOfferings(level, pos)) {
                rite = creatureRite;
                activeRite = creatureRite.id();
            }
        }
        int required = rite.sacrifices().size();
        if (offeringStep >= required) {
            advanceFinalOfferings(level, rite, pos, required);
            return;
        }
        ItemRiteRecipe.Offering offering = rite.findNextOffering(level, pos, offeringStep);
        if (offering == null) {
            abort(level, "message.witchery.rite.missing_ingredient");
            return;
        }
        ItemStack stack = offering.stack().copyWithCount(1);
        BlockPos origin = BlockPos.containing(offering.position());
        ItemStack returned = rite.returnedOffering(stack);
        if (returned.isEmpty()) sacrificed.add(new Sacrifice(stack, origin));
        else if (!dropOffering(level, origin, returned)) return;
        offering.consume().run();
        offeringStep++;
        itemOfferingEffect(level, offering.position());
    }

    private FinalStep finalStep(ItemRiteRecipe rite, int required) {
        boolean optionalWaystone = rite.action().acceptsOptionalWaystone();
        boolean awaitingWaystone = optionalWaystone && offeringStep == required;
        boolean stagedLiving = rite.stagesLivingOffering();
        // Earlier Hell on Earth saves consumed the villager during the Waystone step.
        boolean needsLiving = stagedLiving && !livingOffered && (!optionalWaystone || awaitingWaystone);
        if (offeringStep > required + (optionalWaystone ? 1 : 0)) return FinalStep.INVALID;
        if (needsLiving && (!rite.chargesBeforeLivingOffering() || altarPowerOffered)) return FinalStep.LIVING;
        if (awaitingWaystone) return FinalStep.WAYSTONE;
        if (!altarPowerOffered && (rite.stagesAltarPower() || stagedLiving)) return FinalStep.ALTAR;
        return FinalStep.EFFECT;
    }

    private void advanceFinalOfferings(ServerLevel level, ItemRiteRecipe rite, BlockPos pos, int required) {
        switch (finalStep(rite, required)) {
            case INVALID -> abort(level, "message.witchery.rite.unknown");
            case LIVING -> livingOffered = offerLiving(level, rite, pos);
            case WAYSTONE -> offerWaystone(level, rite, pos);
            case ALTAR -> {
                AltarBlockEntity altar = rite.power() > 0 ? AltarPower.nearest(level, pos) : null;
                if (rite.power() > 0 && (altar == null || !altar.consumePower(rite.power()))) {
                    abort(level, "message.witchery.rite.missing_power");
                } else {
                    altarPowerOffered = true;
                }
            }
            case EFFECT -> finish(level, rite);
        }
    }

    private void finish(ServerLevel level, ItemRiteRecipe rite) {
        BlockPos pos = getBlockPos();
        var action = rite.action();
        var protectionKind = action.protection();
        boolean hellOnEarth = action == ItemRiteRecipe.Action.HELL_ON_EARTH;
        boolean stagedLiving = rite.stagesLivingOffering();
        boolean sacrificesFirst = !stagedLiving && action == ItemRiteRecipe.Action.SUMMON_WITHER;
        boolean newSummoning = action == ItemRiteRecipe.Action.SUMMON_WITHER
                || action == ItemRiteRecipe.Action.SUMMON_WITCH || action == ItemRiteRecipe.Action.SUMMON_SPECTRAL_FAMILIAR;
        boolean needsPower = !altarPowerOffered && rite.power() > 0;
        AltarBlockEntity altar = needsPower ? AltarPower.nearest(level, pos) : null;
        boolean missingPower = needsPower && (altar == null || altar.power() < rite.power());
        if (missingPower && !sacrificesFirst) {
            abort(level, "message.witchery.rite.missing_power");
            return;
        }
        boolean needsLiving = !stagedLiving && !hellOnEarth && rite.livingOffering() != null;
        Mob living = needsLiving ? rite.findLivingOffering(level, pos) : null;
        if (needsLiving && living == null) {
            abort(level, "message.witchery.rite.missing_living_offering");
            return;
        }
        if (sacrificesFirst) {
            // The Wither rite sacrifices the villager before charging the altar.
            if (living != null) sacrificeLiving(level, living);
            living = null;
            if (missingPower) {
                abort(level, "message.witchery.rite.missing_power");
                return;
            }
        }
        List<ServerPlayer> recipients = List.of();
        Entity result = null;
        LivingEntity curseTarget = null;
        ServerPlayer vampireCureTarget = null;
        LivingEntity wolfRiteTarget = null;
        ServerPlayer corruptionCaster = null;
        int talismanPattern = 0;
        int infusionType = switch (rite.action()) {
            case INFUSE_LIGHT -> 1;
            case INFUSE_OVERWORLD -> 2;
            case INFUSE_OTHERWHERE -> 3;
            case INFUSE_INFERNAL -> 4;
            default -> 0;
        };
        boolean familiarRite = rite.action() == ItemRiteRecipe.Action.BIND_FAMILIAR || rite.action() == ItemRiteRecipe.Action.CALL_FAMILIAR;
        if (rite.action() == ItemRiteRecipe.Action.MANIFESTATION) {
            recipients = nearbyPlayers(level, pos, MANIFEST_RANGE);
            if (recipients.isEmpty()) {
                abort(level, "message.witchery.rite.no_nearby_player");
                return;
            }
        } else if (infusionType > 0) {
            recipients = nearbyPlayers(level, pos, INFUSION_RANGE);
        } else if (action.summonsCreature()) {
            if (rite.action() == ItemRiteRecipe.Action.SUMMON_IMP && covenSize == 0) {
                abort(level, "message.witchery.rite.coven_required");
                return;
            }
            Mob summon = switch (action) {
                case SUMMON_IMP -> WitcheryEntities.IMP.get().create(level);
                case SUMMON_REFLECTION -> WitcheryEntities.REFLECTION.get().create(level);
                case SUMMON_WITHER -> EntityType.WITHER.create(level);
                case SUMMON_WITCH -> EntityType.WITCH.create(level);
                case SUMMON_SPECTRAL_FAMILIAR -> WitcheryEntities.SPECTRAL_FAMILIAR.get().create(level);
                case SUMMON_DEMON -> WitcheryEntities.DEMON.get().create(level);
                default -> throw new IllegalStateException("Unexpected creature rite " + action);
            };
            if (summon == null) {
                if (newSummoning) abort(level, "message.witchery.rite.summon_cancelled");
                return;
            }
            summon.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
            if (hasObstructedSummoningArea(level, pos) || level.getBlockCollisions(summon, summon.getBoundingBox()).iterator().hasNext()) {
                if ((summon instanceof Reflection || newSummoning) && altar != null) altar.consumePower(rite.power());
                abort(level, "message.witchery.rite.obstructed");
                return;
            }
            if (summon instanceof Demon demon) demon.summonedByRite();
            else if (summon instanceof Reflection reflection) reflection.allowOutsideMirror();
            else if (summon instanceof SpectralFamiliar familiar) {
                familiar.setOwnerUUID(initiator);
                familiar.setTame(true);
                familiar.setPersistenceRequired();
            } else if (action == ItemRiteRecipe.Action.SUMMON_IMP) summon.setPersistenceRequired();
            if (newSummoning) ForgeEventFactory.onFinalizeSpawn(summon, level, level.getCurrentDifficultyAt(summon.blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null, null);
            result = summon;
        } else if (rite.action() == ItemRiteRecipe.Action.CORRUPT_VOODOO_PROTECTION) {
            corruptionCaster = initiator == null ? null : level.getServer().getPlayerList().getPlayer(initiator);
            if (corruptionCaster == null || corruptionCaster.level() != level || !FamiliarBinding.hasCurseMastery(corruptionCaster)) {
                abort(level, "message.witchery.rite.requires_curse_mastery");
                return;
            }
            curseTarget = boundOffering(level);
        } else if (rite.action() == ItemRiteRecipe.Action.CURE_VAMPIRISM) {
            ServerPlayer caster = initiator == null ? null : level.getServer().getPlayerList().getPlayer(initiator);
            if (caster == null || caster.level() != level || !FamiliarBinding.hasCurseMastery(caster)) {
                abort(level, "message.witchery.rite.requires_curse_mastery");
                return;
            }
            if (covenSize < CovenWitch.MAX_MEMBERS) {
                abort(level, "message.witchery.rite.requires_full_coven");
                return;
            }
            if (!(boundOffering(level) instanceof ServerPlayer vampire) || Vampirism.level(vampire) == 0) {
                abort(level, "message.witchery.rite.cure_vampirism.no_vampire");
                return;
            }
            if (vampire.level() != level
                    || vampire.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > VAMPIRE_CURE_RANGE * VAMPIRE_CURE_RANGE) {
                abort(level, "message.witchery.rite.cure_vampirism.too_far");
                return;
            }
            vampireCureTarget = vampire;
        } else if (rite.action() == ItemRiteRecipe.Action.CURSE_OF_THE_WOLF || rite.action() == ItemRiteRecipe.Action.CURE_LYCANTHROPY) {
            boolean inflict = rite.action() == ItemRiteRecipe.Action.CURSE_OF_THE_WOLF;
            LivingEntity target = boundOffering(level);
            String failure = wolfRiteFailure(level, pos, target, inflict);
            if (failure != null) {
                abort(level, failure);
                return;
            }
            wolfRiteTarget = target;
        } else if (rite.action() == ItemRiteRecipe.Action.ITEM || rite.action() == ItemRiteRecipe.Action.BIND_WAYSTONE
                || rite.action() == ItemRiteRecipe.Action.BIND_CREATURE_WAYSTONE
                || rite.action() == ItemRiteRecipe.Action.COPY_WAYSTONE
                || rite.action() == ItemRiteRecipe.Action.BIND_CIRCLE_TALISMAN
                || rite.action() == ItemRiteRecipe.Action.BIND_WORSHIP_STATUE) {
            ItemStack output = rite.result().copy();
            if (rite.action() == ItemRiteRecipe.Action.BIND_CIRCLE_TALISMAN) {
                output = new ItemStack(WitcheryItems.getRequired("circletalisman").get());
                talismanPattern = CircleTalismanItem.capturePattern(level, pos);
                CircleTalismanItem.setStoredPattern(output, talismanPattern);
            } else if (rite.action() == ItemRiteRecipe.Action.BIND_WAYSTONE) {
                output = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
                WaystoneItem.bindLocation(output, level, pos);
            } else if (rite.action() == ItemRiteRecipe.Action.COPY_WAYSTONE) {
                output = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item(), 2);
                for (Sacrifice sacrifice : sacrificed) {
                    if (WaystoneItem.copyLocation(sacrifice.stack(), output)) break;
                }
            } else if (rite.action() == ItemRiteRecipe.Action.BIND_CREATURE_WAYSTONE) {
                LivingEntity target = WaystoneTravel.findBindingTarget(level, pos, false);
                if (target == null) {
                    abort(level, "message.witchery.rite.no_waystone_target");
                    return;
                }
                output = new ItemStack(WitcheryIngredient.CREATURE_BOUND_WAYSTONE.item());
                TaglockItem.bindSignature(output, target);
            } else if (rite.action() == ItemRiteRecipe.Action.BIND_WORSHIP_STATUE) {
                Vec3 center = Vec3.atBottomCenterOf(pos);
                var target = level.getNearestPlayer(center.x, center.y, center.z, 4,
                        player -> !player.isSpectator() && player.getY() >= pos.getY() && player.getY() <= pos.getY() + 1);
                if (!(target instanceof ServerPlayer owner)) {
                    abort(level, "message.witchery.rite.no_nearby_player");
                    return;
                }
                output = new ItemStack(WitcheryBlocks.getRequired("statueofworship").get());
                StatueOfWorshipBlockEntity.bindItem(output, owner);
            }
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, output, 0, 0.3, 0);
            drop.setDefaultPickUpDelay();
            result = drop;
        }
        if (result != null && !level.addFreshEntity(result)) {
            if (newSummoning) abort(level, "message.witchery.rite.summon_cancelled");
            return;
        }
        if (altar != null && !altar.consumePower(rite.power())) {
            if (result != null) result.discard();
            abort(level, "message.witchery.rite.missing_power");
            return;
        }
        var directedCurse = action.directedCurse();
        if (directedCurse != null) {
            ServerPlayer caster = initiator == null ? null : level.getServer().getPlayerList().getPlayer(initiator);
            String failure = directedCurse.perform(level, pos, caster, boundOffering(level), covenSize, action.inflictsDirectedCurse());
            if (failure != null) {
                abort(level, failure);
                return;
            }
        }
        if (rite.action().callsStorm()) {
            prepareStorm(level, rite, pos);
            if (storm != null && usesGlyphClock()) advanceStorm(level);
            return;
        }
        if (action == ItemRiteRecipe.Action.RAIN_TOADS) {
            toadRainStage = 0;
            if (usesGlyphClock()) advanceToadRain(level);
            return;
        }
        if (action == ItemRiteRecipe.Action.PRIOR_INCARNATION) {
            if (!PriorIncarnation.isAllowed(level)) {
                abort(level, "message.witchery.rite.prior_incarnation_disabled");
                return;
            }
            PriorIncarnation.recover(level, pos);
            sacrificed.clear();
            nextCast(level);
            return;
        }
        if (rite.action() == ItemRiteRecipe.Action.ECLIPSE && !WeatherRites.eclipse(level)) {
            abort(level, "message.witchery.rite.eclipse_cooldown");
            return;
        }
        if (action == ItemRiteRecipe.Action.BANISH_DEMON) Banishment.perform(level, pos);
        if (action.cooksFood() && !CookingRite.cook(level, pos, action == ItemRiteRecipe.Action.COOK_FOOD_PORTABLE)) {
            abort(level, "message.witchery.rite.no_food_to_cook");
            return;
        }
        if (protectionKind != null && !prepareProtection(level, protectionKind, pos)) return;
        LandscapeRites.Progress landscape = null;
        if (rite.action().usesLandscapeUpkeep()) {
            landscape = prepareLandscape(level, rite);
            if (landscape == null) return;
        }
        if (rite.action() == ItemRiteRecipe.Action.BIND_SPECTRAL_STONE && !SpectralStoneItem.bindFromCircle(level, pos)) {
            abort(level, "message.witchery.rite.no_spectral_creature");
            return;
        }
        if (rite.action() == ItemRiteRecipe.Action.GLYPHIC_TRANSFORMATION && !GlyphicTransformation.transform(level, pos)) {
            abort(level, "message.witchery.rite.missing_chalk");
            return;
        }
        if (rite.action() == ItemRiteRecipe.Action.BIND_FETISH) {
            ItemStack fetish = sacrificed.stream().map(Sacrifice::stack).filter(FetishBinding::isFetish)
                    .findFirst().orElse(ItemStack.EMPTY);
            if (!FetishBinding.bind(level, pos, fetish)) {
                abort(level, "message.witchery.rite.no_fetish_spirits");
                return;
            }
        }
        if (familiarRite && !(rite.action() == ItemRiteRecipe.Action.BIND_FAMILIAR
                ? FamiliarBinding.bindNear(level, pos) : FamiliarBinding.callNear(level, pos))) {
            if (altar != null) altar.refundPower(rite.power());
            abort(level, "message.witchery.rite.no_familiar");
            return;
        }
        if (rite.action() == ItemRiteRecipe.Action.TRAVEL_WAYSTONE) {
            ItemStack stone = sacrificed.stream().map(Sacrifice::stack).filter(WaystoneItem::isBoundWaystone)
                    .findFirst().orElse(ItemStack.EMPTY);
            if (stone.isEmpty() || !WaystoneTravel.teleportCircle(level, pos, stone, 3)) {
                if (altar != null) altar.refundPower(rite.power());
                abort(level, "message.witchery.rite.no_waystone_destination");
                return;
            }
        }
        if (rite.action() == ItemRiteRecipe.Action.TRANSPOSE_CREATURE) {
            ServerPlayer caster = initiator == null ? null : level.getServer().getPlayerList().getPlayer(initiator);
            String failure = RiteOfTransposition.summon(level, pos, boundOffering(level), caster);
            if (failure != null) {
                abort(level, failure);
                return;
            }
        }
        if (initiator != null && result instanceof ItemEntity drop
                && drop.getItem().is(WitcheryBlocks.getRequired("crystalball").get().asItem())) {
            ServerPlayer caster = level.getServer().getPlayerList().getPlayer(initiator);
            if (caster != null) Predictions.attune(caster);
        }
        if (living != null) sacrificeLiving(level, living);
        if (corruptionCaster != null) {
            if (curseTarget != null) PoppetProtectionItem.corrupt(corruptionCaster, curseTarget);
            level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                    16, 0.45, 0.5, 0.45, 0.04);
            level.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 0.7F, 1);
        }
        if (vampireCureTarget != null) finishVampireCure(level, pos, vampireCureTarget);
        if (wolfRiteTarget != null && !finishWolfRite(level, pos, wolfRiteTarget,
                rite.action() == ItemRiteRecipe.Action.CURSE_OF_THE_WOLF)) {
            if (altar != null) altar.refundPower(rite.power());
            abort(level, "message.witchery.rite.wolf.conversion_failed");
            return;
        }
        for (ServerPlayer recipient : recipients) {
            if (rite.action() == ItemRiteRecipe.Action.MANIFESTATION) {
                DreamTravel.grantManifestation(recipient, MANIFEST_DURATION);
            } else {
                recipient.hurt(level.damageSources().magic(), 100);
                if (recipient.isAlive()) InfusionEnergy.grant(recipient, infusionType, INFUSION_CAPACITY);
            }
        }
        sacrificed.clear();
        if (rite.action() == ItemRiteRecipe.Action.BIND_CIRCLE_TALISMAN) {
            CircleTalismanItem.eraseCapturedGlyphs(level, pos);
            if (talismanPattern != 0) level.removeBlock(pos, false);
        }
        if (rite.action() == ItemRiteRecipe.Action.TRANSPOSE_ORES) {
            if (RiteOfTransposition.transposeOreLayer(level, pos, 1, covenSize)) {
                upkeep.add(RiteUpkeep.Timed.transposeOres(rite.id(), covenSize, initiator, 1));
            }
        } else if (landscape != null) {
            ServerPlayer caster = initiator == null ? null : level.getServer().getPlayerList().getPlayer(initiator);
            var progress = LandscapeRites.pulse(level, pos, landscape, covenSize, caster, terrainChanges::add);
            if (progress != null) upkeep.add(RiteUpkeep.Timed.landscape(rite.id(), covenSize, initiator, progress));
        } else if (rite.action() == ItemRiteRecipe.Action.RECHARGE_INFUSION) {
            var recharge = RiteUpkeep.Timed.recharge(rite.id(), covenSize, initiator);
            if (!usesGlyphClock() || recharge.rechargeNearby(level, pos)) upkeep.add(recharge);
        }
        if (talismanPattern == 0) nextCast(level);
        level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                16, 0.4, 0.4, 0.4, 0.05);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.9F, 1.35F);
    }

    private static void sacrificeLiving(ServerLevel level, Mob living) {
        level.sendParticles(ParticleTypes.PORTAL, living.getX(), living.getY(0.5), living.getZ(), 16, 0.4, 0.6, 0.4, 0.05);
        playOfferingPop(level, living.position());
        living.discard();
    }

    private static void itemOfferingEffect(ServerLevel level, Vec3 location) {
        level.sendParticles(ParticleTypes.POOF, location.x, location.y + 0.5, location.z,
                20, 0.25, 0.25, 0.25, 0);
        playOfferingPop(level, location);
    }

    private static void playOfferingPop(ServerLevel level, Vec3 location) {
        float pitch = 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F);
        level.playSound(null, location.x, location.y, location.z, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.5F, pitch);
    }

    private boolean offerLiving(ServerLevel level, ItemRiteRecipe rite, BlockPos heart) {
        Mob living = rite.findLivingOffering(level, heart);
        if (living == null) {
            abort(level, "message.witchery.rite.missing_living_offering");
            return false;
        }
        sacrificeLiving(level, living);
        return true;
    }

    private void prepareStorm(ServerLevel level, ItemRiteRecipe rite, BlockPos heart) {
        BlockPos target = waystoneTarget == null ? heart : waystoneTarget;
        if (!LandscapeRites.canReach(level, heart, target, covenSize)) {
            abort(level, "message.witchery.rite.landscape_too_far");
            return;
        }
        storm = new WeatherRites.Storm(target, rite.action() != ItemRiteRecipe.Action.CALL_STORM, 0);
    }

    private boolean prepareProtection(ServerLevel level, ProtectionRites.Kind kind, BlockPos heart) {
        BlockPos target = waystoneTarget == null ? heart : waystoneTarget;
        if (!LandscapeRites.canReach(level, heart, target, covenSize)) {
            abort(level, "message.witchery.rite.landscape_too_far");
            return false;
        }
        var progress = new ProtectionRites.Upkeep(kind, target);
        if (!usesGlyphClock() || progress.tick(level, heart)) upkeep.add(new RiteUpkeep.Ward(progress));
        return true;
    }

    private void offerWaystone(ServerLevel level, ItemRiteRecipe rite, BlockPos heart) {
        if (!rite.stagesLivingOffering() && rite.action() == ItemRiteRecipe.Action.HELL_ON_EARTH
                && rite.livingOffering() != null && !offerLiving(level, rite, heart)) return;
        offeringStep++;
        ItemRiteRecipe.Offering stone = rite.findOptionalWaystone(level, heart);
        if (stone == null) return;
        var location = WaystoneItem.location(stone.stack());
        var origin = stone.position();
        if (location == null || !location.dimension().equals(level.dimension())) {
            level.sendParticles(ParticleTypes.SMOKE, origin.x, origin.y + 0.2, origin.z, 8, 0.2, 0.22, 0.2, 0.02);
            level.playSound(null, BlockPos.containing(origin), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.BLOCKS, 1, 1);
            return;
        }
        waystoneTarget = location.position();
        sacrificed.add(new Sacrifice(stone.stack().copyWithCount(1), BlockPos.containing(stone.position())));
        stone.consume().run();
        itemOfferingEffect(level, origin);
    }

    private void advanceStorm(ServerLevel level) {
        storm = storm.pulse(level);
        if (storm == null) {
            sacrificed.clear();
            nextCast(level);
        }
    }

    private void advanceToadRain(ServerLevel level) {
        if (covenSize < 1) {
            abort(level, "message.witchery.rite.toad_rain_coven");
            return;
        }
        BlockPos heart = getBlockPos();
        if (toadRainStage < RainOfToads.LAST_LIGHTNING_STAGE) {
            RainOfToads.lightningPulse(level, heart, ++toadRainStage);
            return;
        }
        var ready = new LandscapeRites.Progress(ItemRiteRecipe.Action.RAIN_TOADS, null, heart, toadRainStage, null, false);
        var progress = LandscapeRites.pulse(level, heart, ready, covenSize, null, terrainChanges::add);
        if (progress != null) upkeep.add(RiteUpkeep.Timed.landscape(activeRite, covenSize, initiator, progress));
        sacrificed.clear();
        nextCast(level);
    }

    @Nullable
    private LandscapeRites.Progress prepareLandscape(ServerLevel level, ItemRiteRecipe rite) {
        BlockPos heart = getBlockPos();
        if (rite.action() == ItemRiteRecipe.Action.ICE_SHELL && covenSize < 2) {
            abort(level, "message.witchery.rite.ice_shell_coven");
            return null;
        }
        if (rite.action() == ItemRiteRecipe.Action.CALL_BEASTS && covenSize < 3) {
            abort(level, "message.witchery.rite.call_beasts_coven");
            return null;
        }
        if (rite.action() == ItemRiteRecipe.Action.CLIMATE_CHANGE) {
            String failure = ClimateChange.failure(level, heart, covenSize);
            if (failure != null) {
                abort(level, failure);
                return null;
            }
        }
        BlockPos target = waystoneTarget == null ? heart : waystoneTarget;
        if (!LandscapeRites.canReach(level, heart, target, covenSize)) {
            abort(level, "message.witchery.rite.landscape_too_far");
            return null;
        }
        LandscapeRites.Species species = null;
        if (rite.action() == ItemRiteRecipe.Action.FORESTATION) {
            species = sacrificed.stream().map(offering -> LandscapeRites.Species.forStack(offering.stack()))
                    .filter(Objects::nonNull).findFirst().orElse(null);
            if (species == null) {
                abort(level, "message.witchery.rite.missing_ingredient");
                return null;
            }
        } else if (rite.action().isVolcano()) {
            if (!LandscapeRites.hasLavaBelow(level, target)) {
                abort(level, "message.witchery.rite.missing_lava");
                return null;
            }
            LandscapeRites.playRiteStart(level, target);
        }
        BlockPos firstOffering = sacrificed.isEmpty() ? heart.north() : sacrificed.get(0).origin();
        var fissure = rite.action() == ItemRiteRecipe.Action.PART_EARTH
                ? EarthRites.Fissure.start(heart, firstOffering, level.random.nextLong()) : null;
        return new LandscapeRites.Progress(rite.action(), species, target, 0, fissure,
                waystoneTarget == null && rite.action().raisedEarthRadius() > 0);
    }

    private static void finishVampireCure(ServerLevel level, BlockPos pos, ServerPlayer vampire) {
        boolean cured = level.random.nextInt(4) != 0;
        if (cured) Vampirism.cure(vampire);
        level.sendParticles(cured ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.FLAME,
                pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 24, 0.5, 0.6, 0.5, 0.035);
        level.playSound(null, pos, cured ? SoundEvents.PLAYER_LEVELUP : SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.BLOCKS, 1, 1);
    }

    @Nullable
    private String wolfRiteFailure(ServerLevel level, BlockPos pos, @Nullable LivingEntity target, boolean inflict) {
        if (!Lycanthropy.isFullMoon(level)) return "message.witchery.rite.wolf.requires_full_moon";
        ServerPlayer caster = initiator == null ? null : level.getServer().getPlayerList().getPlayer(initiator);
        if (caster == null || caster.level() != level || !FamiliarBinding.hasCurseMastery(caster)) {
            return "message.witchery.rite.requires_curse_mastery";
        }
        if (covenSize < CovenWitch.MAX_MEMBERS) return "message.witchery.rite.requires_full_coven";
        if (target == null) return "message.witchery.rite.no_taglock_target";
        if (inflict) {
            WitchHunterPursuit.noticeBlackMagic(caster);
            if (HunterClothingItem.resistsCurse(target)) return "message.witchery.rite.hunter_curse_protection";
            if (target instanceof WereVillager || target instanceof Wolfman) return "message.witchery.rite.wolf.already_affected";
            if (target instanceof Villager) return null;
            if (!(target instanceof ServerPlayer victim)) return "message.witchery.rite.wolf.human_required";
            if (VoodooPoppetItem.deflectRite(caster, victim, 3)) return "message.witchery.rite.wolf.warded";
            if (!WitcheryServerConfig.ALLOW_VAMPIRE_WEREWOLF_HYBRIDS.get() && Vampirism.isVampire(victim)) {
                return "message.witchery.rite.wolf.hybrids_disabled";
            }
            if (Lycanthropy.rank(victim) > 0) return "message.witchery.rite.wolf.already_affected";
        } else {
            if (target instanceof WereVillager || target instanceof Wolfman wolfman && wolfman.hasFormerVillager()) return null;
            if (!(target instanceof ServerPlayer victim) || Lycanthropy.rank(victim) == 0) {
                return "message.witchery.rite.wolf.unaffected";
            }
            if (Lycanthropy.rank(victim) > 1 && (victim.level() != level
                    || victim.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > WOLF_CURE_RANGE * WOLF_CURE_RANGE)) {
                return "message.witchery.rite.wolf.too_far";
            }
        }
        return null;
    }

    private static boolean finishWolfRite(ServerLevel level, BlockPos pos, LivingEntity target, boolean inflict) {
        boolean cursed = inflict;
        if (target instanceof ServerPlayer player) {
            if (inflict) Lycanthropy.infect(player);
            else if (level.random.nextInt(4) == 0) cursed = true;
            else Lycanthropy.setRank(player, 0);
        } else if (inflict && target instanceof Villager villager) {
            if (!WerewolfVillagers.infect(villager, false)) return false;
        } else if (target instanceof WereVillager villager) {
            if (!WerewolfVillagers.cleanse(villager)) return false;
        } else if (target instanceof Wolfman wolfman && !WerewolfVillagers.revert(wolfman, false)) return false;
        level.sendParticles(cursed ? ParticleTypes.FLAME : ParticleTypes.INSTANT_EFFECT,
                pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 16, 0.5, 0.6, 0.5, 0.035);
        level.playSound(null, pos, cursed ? SoundEvents.ENDER_DRAGON_GROWL : SoundEvents.PLAYER_LEVELUP,
                SoundSource.BLOCKS, 1, 1);
        return true;
    }

    @Nullable
    private LivingEntity boundOffering(ServerLevel level) {
        return sacrificed.stream().map(Sacrifice::stack).filter(stack -> stack.getItem() instanceof TaglockItem)
                .map(stack -> TaglockItem.resolveTarget(level, stack))
                .filter(target -> target != null && target.isAlive()).findFirst().orElse(null);
    }

    static List<ServerPlayer> nearbyPlayers(ServerLevel level, BlockPos pos, int range) {
        return level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(range, 0, range),
                player -> player.isAlive() && !player.isSpectator()
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= range * range);
    }

    private static boolean hasObstructedSummoningArea(ServerLevel level, BlockPos center) {
        int obstructions = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, 1, -3), center.offset(3, 3, 2))) {
            int x = Math.abs(pos.getX() - center.getX());
            int z = Math.abs(pos.getZ() - center.getZ());
            if (x + z <= 4 && level.getBlockState(pos).blocksMotion()) {
                if (++obstructions > 1 || x + z == 0) return true;
            }
        }
        return false;
    }

    private void abort(ServerLevel level, String message) {
        ServerPlayer player = initiator == null ? null : level.getServer().getPlayerList().getPlayer(initiator);
        if (player != null) {
            player.displayClientMessage(Component.translatable(message), true);
        }
        refundSacrifices(level);
        nextCast(level);
        failSound(level);
    }

    public void refundSacrifices(ServerLevel level) {
        restoreOfferingReturns(level);
        for (Sacrifice sacrifice : sacrificed) {
            dropOffering(level, sacrifice.origin(), sacrifice.stack());
        }
        sacrificed.clear();
    }

    private void restoreOfferingReturns(ServerLevel level) {
        if (!legacyOfferingReturns) return;
        legacyOfferingReturns = false;
        if (level.getRecipeManager().byKey(activeRite).orElse(null) instanceof ItemRiteRecipe rite) {
            sacrificed.removeIf(sacrifice -> {
                ItemStack returned = rite.returnedOffering(sacrifice.stack());
                return !returned.isEmpty() && dropOffering(level, sacrifice.origin(), returned);
            });
        }
        setChanged();
    }

    private static boolean dropOffering(ServerLevel level, BlockPos origin, ItemStack stack) {
        var drop = new ItemEntity(level, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, stack);
        drop.setDefaultPickUpDelay();
        return level.addFreshEntity(drop);
    }

    private void nextCast(ServerLevel level) {
        activeRite = queuedRites.pollFirst();
        if (activeRite == null) {
            initiator = null;
            covenSize = 0;
            covenSummoned = 0;
        }
        stepTicks = 0;
        resumeRelativePulse = false;
        offeringStep = 0;
        livingOffered = false;
        altarPowerOffered = false;
        legacyOfferingReturns = false;
        legacyWaystoneMatch = false;
        waystoneTarget = null;
        storm = null;
        toadRainStage = -1;
        updateActiveState(level);
        setChanged();
    }

    private void updateActiveState(ServerLevel level) {
        BlockPos pos = getBlockPos();
        BlockState current = level.getBlockState(pos);
        if (current.hasProperty(RiteHeartBlock.ACTIVE) && current.getValue(RiteHeartBlock.ACTIVE) != isRiteActive()) {
            level.setBlock(pos, current.setValue(RiteHeartBlock.ACTIVE, isRiteActive()), Block.UPDATE_CLIENTS);
        }
    }

    private boolean isRiteActive() {
        return activeRite != null || !upkeep.isEmpty();
    }

    private void failSound(ServerLevel level) {
        level.playSound(null, getBlockPos(), SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 0.9F, 1.0F);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        activeRite = tag.contains("Rite", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("Rite")) : null;
        initiator = tag.hasUUID("Initiator") ? tag.getUUID("Initiator") : null;
        covenSize = Mth.clamp(tag.getInt("CovenSize"), 0, CovenWitch.MAX_MEMBERS);
        covenSummoned = Mth.clamp(tag.getInt("CovenSummoned"), 0, covenSize);
        sacrificed.clear();
        for (Tag entry : tag.getList("Sacrifices", Tag.TAG_COMPOUND)) {
            CompoundTag saved = (CompoundTag) entry;
            ItemStack stack = WitcheryIngredient.loadInventoryStack(saved);
            if (!stack.isEmpty()) {
                BlockPos origin = saved.contains("OfferingPos", Tag.TAG_LONG) ? BlockPos.of(saved.getLong("OfferingPos"))
                        : saved.contains("GrassperPos", Tag.TAG_LONG) ? BlockPos.of(saved.getLong("GrassperPos")) : getBlockPos();
                sacrificed.add(new Sacrifice(stack, origin));
            }
        }
        queuedRites.clear();
        for (Tag entry : tag.getList("QueuedRites", Tag.TAG_STRING)) {
            ResourceLocation queued = ResourceLocation.tryParse(entry.getAsString());
            if (queued != null) queuedRites.addLast(queued);
        }
        var savedUpkeep = new ArrayList<SavedUpkeep>();
        for (Tag entry : tag.getList("ProtectionRites", Tag.TAG_COMPOUND)) {
            CompoundTag wardTag = (CompoundTag) entry;
            var ward = ProtectionRites.Upkeep.load(wardTag);
            if (ward != null) savedUpkeep.add(new SavedUpkeep(new RiteUpkeep.Ward(ward), upkeepOrder(wardTag)));
        }
        var primary = RiteUpkeep.Timed.load(tag);
        if (primary != null) {
            savedUpkeep.add(new SavedUpkeep(primary, upkeepOrder(tag)));
            activeRite = null;
        }
        for (Tag entry : tag.getList("UpkeepRites", Tag.TAG_COMPOUND)) {
            CompoundTag ongoing = (CompoundTag) entry;
            var rite = RiteUpkeep.Timed.load(ongoing);
            if (rite != null) savedUpkeep.add(new SavedUpkeep(rite, upkeepOrder(ongoing)));
        }
        upkeep.clear();
        savedUpkeep.sort(Comparator.comparingInt(SavedUpkeep::order));
        for (var ongoing : savedUpkeep) upkeep.add(ongoing.rite());
        if (activeRite == null && upkeep.isEmpty()) loadLegacyUpkeep(tag);
        if (activeRite == null) activeRite = queuedRites.pollFirst();
        waystoneTarget = activeRite != null && tag.contains("WaystoneTarget", Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong("WaystoneTarget")) : null;
        storm = activeRite == null ? null : WeatherRites.Storm.load(tag.getCompound("Storm"));
        toadRainStage = activeRite != null && tag.contains("ToadRainStage", Tag.TAG_ANY_NUMERIC)
                ? Mth.clamp(tag.getInt("ToadRainStage"), 0, RainOfToads.LAST_LIGHTNING_STAGE) : -1;
        legacyWaystoneMatch = RiteActivation.TRAVEL_TO_LOCATION.equals(activeRite)
                && (!tag.contains("QueuedRites", Tag.TAG_LIST) || tag.getBoolean("LegacyWaystoneMatch"));
        boolean hasOfferingStep = tag.contains("OfferingStep", Tag.TAG_ANY_NUMERIC);
        offeringStep = hasOfferingStep ? Math.max(0, tag.getInt("OfferingStep")) : sacrificed.size();
        livingOffered = tag.getBoolean("LivingOffered");
        altarPowerOffered = tag.getBoolean("AltarPowerOffered");
        legacyOfferingReturns = !hasOfferingStep && activeRite != null && !sacrificed.isEmpty();
        stepTicks = Mth.clamp(tag.getInt("StepTicks"), 0, relativeInterval() - 1);
        glyphPhase = Math.floorMod(tag.getInt("GlyphPhase"), GLYPH_CLOCK_PERIOD);
        resumeRelativePulse = usesGlyphClock() && !tag.contains("GlyphPhase", Tag.TAG_ANY_NUMERIC);
    }

    private static int upkeepOrder(CompoundTag tag) {
        return tag.contains("UpkeepOrder", Tag.TAG_ANY_NUMERIC) ? tag.getInt("UpkeepOrder") : Integer.MAX_VALUE;
    }

    private void loadLegacyUpkeep(CompoundTag tag) {
        byte[] rites = tag.getByteArray("Rituals");
        byte[] stages = tag.getByteArray("RitualStages");
        byte[] covens = tag.getByteArray("RitualCovens");
        ListTag locations = tag.getList("Locations", Tag.TAG_COMPOUND);
        ListTag initiators = tag.getList("Initiators", Tag.TAG_STRING);
        for (int index = 0; index < Math.min(rites.length, stages.length); index++) {
            int rite = rites[index];
            // Long rites have valid stages beyond the signed byte range.
            int stage = rite == 39 || rite == 43 || rite == 66 || rite == 68 ? Byte.toUnsignedInt(stages[index]) : stages[index];
            int members = index < covens.length ? Mth.clamp(covens[index], 0, CovenWitch.MAX_MEMBERS) : 0;
            var kind = ProtectionRites.Kind.fromLegacy(rite);
            if (kind != null) {
                BlockPos target = kind.acceptsWaystone() ? readLegacyTarget(locations.getCompound(index)) : getBlockPos();
                int range = 50 + 50 * members;
                var ward = ProtectionRites.Upkeep.fromLegacy(kind, target, stage);
                if (ward != null && getBlockPos().distSqr(target) <= range * range) upkeep.add(new RiteUpkeep.Ward(ward));
                continue;
            }
            RiteUpkeep.Timed timed;
            if (rite == 4 && stage == 0) {
                timed = RiteUpkeep.Timed.recharge(ResourceLocation.fromNamespaceAndPath(Witchery.MODID,
                        "rite_of_infusion_recharging"), members, null);
            } else if (rite == 7) {
                if (stage <= 0 || stage >= RiteOfTransposition.oreDepthLimit(members)) continue;
                timed = RiteUpkeep.Timed.transposeOres(RiteOfTransposition.ORE_RECIPE, members, null, stage);
            } else {
                LandscapeRites.Species species = LandscapeRites.Species.forLegacyRite(rite);
                ItemRiteRecipe.Action action = switch (rite) {
                    case 13 -> ItemRiteRecipe.Action.RAISE_PORTABLE_VOLCANO;
                    case 19 -> ItemRiteRecipe.Action.PART_EARTH;
                    case 20 -> ItemRiteRecipe.Action.RAISE_EARTH;
                    case 35, 36 -> ItemRiteRecipe.Action.FERTILITY;
                    case 37 -> ItemRiteRecipe.Action.BLIGHT;
                    case 38 -> ItemRiteRecipe.Action.AREA_BLINDNESS;
                    case 39 -> ItemRiteRecipe.Action.HELL_ON_EARTH;
                    case 43 -> ItemRiteRecipe.Action.NATURES_POWER;
                    case 46 -> ItemRiteRecipe.Action.RAISE_EARTH_MEDIUM;
                    case 47 -> ItemRiteRecipe.Action.RAISE_EARTH_LARGE;
                    case 64 -> members >= 4 ? ItemRiteRecipe.Action.CLIMATE_CHANGE : null;
                    case 65 -> members >= 2 ? ItemRiteRecipe.Action.ICE_SHELL : null;
                    case 66 -> members >= 1 && stage > RainOfToads.LAST_LIGHTNING_STAGE ? ItemRiteRecipe.Action.RAIN_TOADS : null;
                    case 68 -> members >= 3 ? ItemRiteRecipe.Action.CALL_BEASTS : null;
                    case 77 -> ItemRiteRecipe.Action.RAISE_VOLCANO;
                    default -> species == null ? null : ItemRiteRecipe.Action.FORESTATION;
                };
                if (action == null) continue;
                BlockPos remote = action.usesWaystoneTarget() ? legacyTarget(locations.getCompound(index)) : null;
                BlockPos target = remote == null ? getBlockPos() : remote;
                // Legacy saves retained the stage but discarded the offering location and generated path.
                var fissure = action == ItemRiteRecipe.Action.PART_EARTH ? new EarthRites.Fissure(target.asLong(), 0) : null;
                var progress = new LandscapeRites.Progress(action, species, target, stage, fissure,
                        remote == null && action.raisedEarthRadius() > 0);
                if (stage <= 0 || stage >= progress.lastStage(members)) continue;
                ResourceLocation recipe = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, switch (action) {
                    case CLIMATE_CHANGE -> "rite_of_climate_change";
                    case ICE_SHELL -> "rite_of_ice_shell";
                    case RAISE_VOLCANO -> "rite_of_raising_a_volcano";
                    case RAISE_PORTABLE_VOLCANO -> "rite_of_raising_a_volcano_portable";
                    case PART_EARTH -> "rite_of_broken_earth";
                    case RAISE_EARTH -> "rite_of_moving_earth";
                    case RAISE_EARTH_MEDIUM -> "rite_of_moving_earth_medium";
                    case RAISE_EARTH_LARGE -> "rite_of_moving_earth_large";
                    case FERTILITY -> rite == 35 ? "rite_of_fertility" : "rite_of_fertility_portable";
                    case BLIGHT -> "curse_of_blight";
                    case AREA_BLINDNESS -> "curse_of_blindness";
                    case HELL_ON_EARTH -> "rite_of_hell_on_earth";
                    case RAIN_TOADS -> RainOfToads.RECIPE.getPath();
                    case NATURES_POWER -> "rite_of_natures_power";
                    case CALL_BEASTS -> "rite_of_calling_the_beasts";
                    default -> "forestation/" + species.name().toLowerCase(Locale.ROOT);
                });
                timed = RiteUpkeep.Timed.landscape(recipe, members, null, progress);
            }
            timed.restoreInitiatorName(initiators.getString(index));
            upkeep.add(timed);
        }
        for (var ongoing : upkeep) {
            if (ongoing instanceof RiteUpkeep.Timed timed) {
                timed.restoreTicks(tag.getInt("StepTicks"));
                break;
            }
        }
    }

    private BlockPos readLegacyTarget(CompoundTag position) {
        return Objects.requireNonNullElse(legacyTarget(position), getBlockPos());
    }

    @Nullable
    private static BlockPos legacyTarget(CompoundTag position) {
        return position.contains("posX", Tag.TAG_ANY_NUMERIC) && position.contains("posY", Tag.TAG_ANY_NUMERIC)
                && position.contains("posZ", Tag.TAG_ANY_NUMERIC)
                ? new BlockPos(position.getInt("posX"), position.getInt("posY"), position.getInt("posZ")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (activeRite != null) {
            tag.putString("Rite", activeRite.toString());
            tag.putInt("StepTicks", stepTicks);
            if (usesGlyphClock() && !resumeRelativePulse) tag.putInt("GlyphPhase", glyphPhase);
            if (!legacyOfferingReturns) tag.putInt("OfferingStep", offeringStep);
            if (livingOffered) tag.putBoolean("LivingOffered", true);
            if (altarPowerOffered) tag.putBoolean("AltarPowerOffered", true);
            ListTag queued = new ListTag();
            for (var id : queuedRites) queued.add(StringTag.valueOf(id.toString()));
            tag.put("QueuedRites", queued);
            if (legacyWaystoneMatch) tag.putBoolean("LegacyWaystoneMatch", true);
            if (waystoneTarget != null) tag.putLong("WaystoneTarget", waystoneTarget.asLong());
            if (storm != null) tag.put("Storm", storm.save());
            if (toadRainStage >= 0) tag.putInt("ToadRainStage", toadRainStage);
            if (covenSize > 0) {
                tag.putInt("CovenSize", covenSize);
                tag.putInt("CovenSummoned", covenSummoned);
            }
            if (initiator != null) {
                tag.putUUID("Initiator", initiator);
            }
        }
        ListTag wards = new ListTag();
        ListTag background = new ListTag();
        boolean rootHasRite = activeRite != null;
        for (int index = 0; index < upkeep.size(); index++) {
            var ongoing = upkeep.get(index);
            if (ongoing instanceof RiteUpkeep.Ward ward) {
                CompoundTag wardTag = ward.progress().save();
                wardTag.putInt("UpkeepOrder", index);
                wards.add(wardTag);
            } else if (ongoing instanceof RiteUpkeep.Timed timed) {
                // The first timed rite retains the existing root fields whenever there is no foreground cast.
                CompoundTag progress = rootHasRite ? new CompoundTag() : tag;
                timed.save(progress);
                progress.putInt("UpkeepOrder", index);
                if (rootHasRite) background.add(progress);
                rootHasRite = true;
            }
        }
        if (!wards.isEmpty()) tag.put("ProtectionRites", wards);
        if (!background.isEmpty()) tag.put("UpkeepRites", background);
        ListTag held = new ListTag();
        for (Sacrifice sacrifice : sacrificed) {
            CompoundTag saved = sacrifice.stack().save(new CompoundTag());
            saved.putLong("OfferingPos", sacrifice.origin().asLong());
            held.add(saved);
        }
        tag.put("Sacrifices", held);
    }
}
