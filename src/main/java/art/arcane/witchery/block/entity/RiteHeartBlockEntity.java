package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.RiteHeartBlock;
import art.arcane.witchery.brewing.WakingNightmareEffect;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.entity.CovenWitch;
import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.entity.WereVillager;
import art.arcane.witchery.entity.WitchHunterPursuit;
import art.arcane.witchery.entity.Wolfman;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.item.PoppetProtectionItem;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.VoodooPoppetItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.prediction.Predictions;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryRecipes;
import art.arcane.witchery.ritual.ItemRiteRecipe;
import art.arcane.witchery.vampirism.Vampirism;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WaystoneTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RiteHeartBlockEntity extends BlockEntity {
    private static final int STEP_TICKS = 20;
    private static final int MANIFEST_RANGE = 5;
    private static final int MANIFEST_DURATION = 150;
    private static final int INFUSION_RANGE = 4;
    private static final int INFUSION_CAPACITY = 200;
    private static final int VAMPIRE_CURE_RANGE = 8;
    private static final int WOLF_CURE_RANGE = 8;
    private static final int RECHARGE_AMOUNT = 10;
    private static final int RECHARGE_POWER_COST = 40;
    private final List<Sacrifice> sacrificed = new ArrayList<>();

    private record Sacrifice(ItemStack stack, @Nullable BlockPos grassper) {
    }
    @Nullable
    private ResourceLocation activeRite;
    @Nullable
    private UUID initiator;
    private int stepTicks;
    private int covenSize;
    private int covenSummoned;
    private boolean recharging;

    public RiteHeartBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.CIRCLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RiteHeartBlockEntity heart) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(RiteHeartBlock.POWERED)) {
            level.setBlock(pos, state.setValue(RiteHeartBlock.POWERED, powered), Block.UPDATE_CLIENTS);
            if (powered) {
                heart.toggle(server, null);
            }
        }
        heart.updateActiveState(server);
        if (heart.activeRite != null) {
            if (++heart.stepTicks >= STEP_TICKS) {
                heart.stepTicks = 0;
                heart.advance(server);
            }
            level.blockEntityChanged(pos);
        }
    }

    public void toggle(ServerLevel level, @Nullable Player player) {
        toggle(level, player, false);
    }

    public void toggle(ServerLevel level, @Nullable Player player, boolean callCoven) {
        if (activeRite != null) {
            abort(level, "message.witchery.rite.cancelled");
            return;
        }
        BlockPos pos = getBlockPos();
        String reason = "message.witchery.rite.unknown";
        for (ItemRiteRecipe rite : level.getRecipeManager().getAllRecipesFor(WitcheryRecipes.ITEM_RITE.get())) {
            if (rite.hasCircles(level, pos) && rite.findSacrifices(level, pos, 0) != null) {
                String failure = rite.startFailure(level);
                if (failure != null) {
                    reason = failure;
                    continue;
                }
                activeRite = rite.id();
                initiator = player == null ? null : player.getUUID();
                covenSize = callCoven && player instanceof ServerPlayer serverPlayer ? CovenWitch.covenSize(serverPlayer) : 0;
                covenSummoned = 0;
                stepTicks = 0;
                updateActiveState(level);
                setChanged();
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                return;
            }
        }
        if (player != null) {
            player.displayClientMessage(Component.translatable(reason), true);
        }
        failSound(level);
    }

    private void advance(ServerLevel level) {
        if (!(level.getRecipeManager().byKey(activeRite).orElse(null) instanceof ItemRiteRecipe rite)
                || sacrificed.size() > rite.sacrifices().size()) {
            abort(level, "message.witchery.rite.unknown");
            return;
        }
        if (recharging && rite.action() == ItemRiteRecipe.Action.RECHARGE_INFUSION) {
            rechargeNearby(level);
            return;
        }
        if (covenSummoned < covenSize) {
            ServerPlayer caller = initiator == null ? null : level.getServer().getPlayerList().getPlayer(initiator);
            if (caller != null && caller.level() == level) CovenWitch.summonToRite(caller, getBlockPos(), covenSummoned);
            covenSummoned++;
            return;
        }
        BlockPos pos = getBlockPos();
        if (sacrificed.size() == rite.sacrifices().size()) {
            finish(level, rite);
            return;
        }
        List<ItemRiteRecipe.Offering> remaining = rite.findSacrifices(level, pos, sacrificed.size());
        if (remaining == null) {
            abort(level, "message.witchery.rite.missing_ingredient");
            return;
        }
        ItemRiteRecipe.Offering offering = remaining.get(0);
        sacrificed.add(new Sacrifice(offering.stack().copyWithCount(1), offering.grassper()));
        offering.consume().run();
        var location = offering.position();
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, location.x, location.y + 0.2, location.z,
                8, 0.2, 0.22, 0.2, 0.02);
    }

    private void finish(ServerLevel level, ItemRiteRecipe rite) {
        BlockPos pos = getBlockPos();
        AltarBlockEntity altar = rite.power() == 0 ? null : AltarPower.nearest(level, pos);
        if (rite.power() > 0 && (altar == null || altar.power() < rite.power())) {
            abort(level, "message.witchery.rite.missing_power");
            return;
        }
        Mob living = rite.findLivingOffering(level, pos);
        if (rite.livingOffering() != null && living == null) {
            abort(level, "message.witchery.rite.missing_living_offering");
            return;
        }
        List<ServerPlayer> recipients = List.of();
        Entity result = null;
        LivingEntity curseTarget = null;
        ServerPlayer vampireCureTarget = null;
        LivingEntity wolfRiteTarget = null;
        ServerPlayer corruptionCaster = null;
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
        } else if (rite.action() == ItemRiteRecipe.Action.SUMMON_DEMON || rite.action() == ItemRiteRecipe.Action.SUMMON_IMP) {
            boolean impRite = rite.action() == ItemRiteRecipe.Action.SUMMON_IMP;
            if (impRite && covenSize == 0) {
                abort(level, "message.witchery.rite.coven_required");
                return;
            }
            Mob summon = impRite ? WitcheryEntities.IMP.get().create(level) : WitcheryEntities.DEMON.get().create(level);
            if (summon == null) return;
            summon.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
            if (hasObstructedSummoningArea(level, pos) || level.getBlockCollisions(summon, summon.getBoundingBox()).iterator().hasNext()) {
                abort(level, "message.witchery.rite.obstructed");
                return;
            }
            if (summon instanceof Demon demon) demon.summonedByRite();
            else summon.setPersistenceRequired();
            result = summon;
        } else if (rite.action() == ItemRiteRecipe.Action.CURSE_WAKING_NIGHTMARE
                || rite.action() == ItemRiteRecipe.Action.CURE_WAKING_NIGHTMARE) {
            curseTarget = boundOffering(level);
            if (curseTarget == null) {
                abort(level, "message.witchery.rite.no_taglock_target");
                return;
            }
            if (rite.action() == ItemRiteRecipe.Action.CURSE_WAKING_NIGHTMARE) {
                ServerPlayer caster = initiator == null ? null : level.getServer().getPlayerList().getPlayer(initiator);
                if (caster != null) WitchHunterPursuit.noticeBlackMagic(caster);
                if (HunterClothingItem.resistsCurse(curseTarget)) {
                    abort(level, "message.witchery.rite.hunter_curse_protection");
                    return;
                }
            }
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
                || rite.action() == ItemRiteRecipe.Action.BIND_CREATURE_WAYSTONE) {
            ItemStack output = rite.result().copy();
            if (rite.action() == ItemRiteRecipe.Action.BIND_WAYSTONE) {
                output = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
                WaystoneItem.bindLocation(output, level, pos);
            } else if (rite.action() == ItemRiteRecipe.Action.BIND_CREATURE_WAYSTONE) {
                LivingEntity target = WaystoneTravel.findBindingTarget(level, pos, false);
                if (target == null) {
                    abort(level, "message.witchery.rite.no_waystone_target");
                    return;
                }
                output = new ItemStack(WitcheryIngredient.CREATURE_BOUND_WAYSTONE.item());
                TaglockItem.bindSignature(output, target);
            }
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, output, 0, 0.3, 0);
            drop.setDefaultPickUpDelay();
            result = drop;
        }
        if (result != null && !level.addFreshEntity(result)) {
            return;
        }
        if (altar != null && !altar.consumePower(rite.power())) {
            if (result != null) result.discard();
            abort(level, "message.witchery.rite.missing_power");
            return;
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
        if (initiator != null && result instanceof ItemEntity drop
                && drop.getItem().is(WitcheryBlocks.getRequired("crystalball").get().asItem())) {
            ServerPlayer caster = level.getServer().getPlayerList().getPlayer(initiator);
            if (caster != null) Predictions.attune(caster);
        }
        if (living != null) {
            level.sendParticles(ParticleTypes.PORTAL, living.getX(), living.getY(0.5), living.getZ(), 16, 0.4, 0.6, 0.4, 0.05);
            living.discard();
        }
        if (corruptionCaster != null) {
            if (curseTarget != null) PoppetProtectionItem.corrupt(corruptionCaster, curseTarget);
            level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                    16, 0.45, 0.5, 0.45, 0.04);
            level.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 0.7F, 1);
        } else if (curseTarget != null) {
            WakingNightmareEffect.changeCurse(curseTarget, rite.action() == ItemRiteRecipe.Action.CURSE_WAKING_NIGHTMARE);
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
        for (Sacrifice sacrifice : sacrificed) {
            ItemStack offering = sacrifice.stack();
            if (offering.is(WitcheryIngredient.NECROMANTIC_STONE.item())) {
                Block.popResource(level, pos, offering);
            }
        }
        sacrificed.clear();
        if (rite.action() == ItemRiteRecipe.Action.RECHARGE_INFUSION) {
            recharging = true;
            setChanged();
        } else {
            clear(level);
        }
        level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                16, 0.4, 0.4, 0.4, 0.05);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.9F, 1.35F);
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

    private static List<ServerPlayer> nearbyPlayers(ServerLevel level, BlockPos pos, int range) {
        return level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(range, 0, range),
                player -> player.isAlive() && !player.isSpectator()
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= range * range);
    }

    private void rechargeNearby(ServerLevel level) {
        AltarBlockEntity altar = AltarPower.nearest(level, getBlockPos());
        if (altar == null || !altar.consumePower(RECHARGE_POWER_COST)) {
            abort(level, "message.witchery.rite.missing_power");
            return;
        }
        for (ServerPlayer player : nearbyPlayers(level, getBlockPos(), INFUSION_RANGE)) {
            if (InfusionEnergy.recharge(player, RECHARGE_AMOUNT)) {
                level.sendParticles(ParticleTypes.INSTANT_EFFECT, player.getX(), player.getY(0.5), player.getZ(),
                        8, 0.35, 0.65, 0.35, 0);
                player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.8F, 1.0F);
            }
        }
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
        clear(level);
        failSound(level);
    }

    public void refundSacrifices(ServerLevel level) {
        for (Sacrifice sacrifice : sacrificed) {
            BlockPos origin = sacrifice.grassper() == null ? getBlockPos() : sacrifice.grassper();
            Block.popResource(level, origin, sacrifice.stack());
        }
        sacrificed.clear();
    }

    private void clear(ServerLevel level) {
        activeRite = null;
        initiator = null;
        stepTicks = 0;
        covenSize = 0;
        covenSummoned = 0;
        recharging = false;
        updateActiveState(level);
        setChanged();
    }

    private void updateActiveState(ServerLevel level) {
        BlockPos pos = getBlockPos();
        BlockState current = level.getBlockState(pos);
        if (current.hasProperty(RiteHeartBlock.ACTIVE) && current.getValue(RiteHeartBlock.ACTIVE) != (activeRite != null)) {
            level.setBlock(pos, current.setValue(RiteHeartBlock.ACTIVE, activeRite != null), Block.UPDATE_CLIENTS);
        }
    }

    private void failSound(ServerLevel level) {
        level.playSound(null, getBlockPos(), SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 0.9F, 1.0F);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        activeRite = tag.contains("Rite", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("Rite")) : null;
        initiator = tag.hasUUID("Initiator") ? tag.getUUID("Initiator") : null;
        stepTicks = Mth.clamp(tag.getInt("StepTicks"), 0, STEP_TICKS - 1);
        covenSize = Mth.clamp(tag.getInt("CovenSize"), 0, CovenWitch.MAX_MEMBERS);
        covenSummoned = Mth.clamp(tag.getInt("CovenSummoned"), 0, covenSize);
        recharging = tag.getBoolean("Recharging");
        sacrificed.clear();
        for (Tag entry : tag.getList("Sacrifices", Tag.TAG_COMPOUND)) {
            CompoundTag saved = (CompoundTag) entry;
            ItemStack stack = WitcheryIngredient.loadInventoryStack(saved);
            if (!stack.isEmpty()) {
                BlockPos origin = saved.contains("GrassperPos", Tag.TAG_LONG) ? BlockPos.of(saved.getLong("GrassperPos")) : null;
                sacrificed.add(new Sacrifice(stack, origin));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (activeRite != null) {
            tag.putString("Rite", activeRite.toString());
            tag.putInt("StepTicks", stepTicks);
            if (recharging) tag.putBoolean("Recharging", true);
            if (covenSize > 0) {
                tag.putInt("CovenSize", covenSize);
                tag.putInt("CovenSummoned", covenSummoned);
            }
            if (initiator != null) {
                tag.putUUID("Initiator", initiator);
            }
        }
        ListTag held = new ListTag();
        for (Sacrifice sacrifice : sacrificed) {
            CompoundTag saved = sacrifice.stack().save(new CompoundTag());
            if (sacrifice.grassper() != null) saved.putLong("GrassperPos", sacrifice.grassper().asLong());
            held.add(saved);
        }
        tag.put("Sacrifices", held);
    }
}
