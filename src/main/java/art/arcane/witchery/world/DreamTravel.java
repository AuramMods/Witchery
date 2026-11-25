package art.arcane.witchery.world;

import art.arcane.witchery.block.entity.FetishBlockEntity;
import art.arcane.witchery.ritual.FetishBinding;

import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.DreamWeave;
import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.entity.Nightmare;
import art.arcane.witchery.entity.SleepingBody;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class DreamTravel {
    private static final String SPIRIT_WORLD = "WITCSpiritWorld";
    private static final String SPIRIT_WALKING = "WITCSpiritWalking";
    private static final String FORCE_AWAKEN = "WITCForceAwaken";
    private static final String OVERWORLD_INVENTORY = "OverworldInventory";
    private static final String SPIRIT_INVENTORY = "SpiritInventory";
    private static final String OVERWORLD_HEALTH = "OverworldHealth";
    private static final String SPIRIT_HEALTH = "SpiritHealth";
    private static final String OVERWORLD_HUNGER = "OverworldHunger";
    private static final String SPIRIT_HUNGER = "SpiritHunger";
    private static final String BODY_ENTITY = "BodyEntity";
    private static final String REQUEST_SLEEP = "WITCDrankSleepingBrew";
    private static final String REQUEST_MANIFEST = "WITCEnteredSpiritPortal";
    private static final String REQUEST_GHOST_RETURN = "WITCLeavingManifestation";
    private static final String MANIFESTED = "WITCManifested";
    private static final String MANIFEST_DURATION = "WITCManifestDuration";
    private static final String MANIFEST_SKIP = "WITCManifestSkipTick";
    private static final String MANIFEST_REENTRY = "WITCManifestReentryTick";
    private static final String LAST_NIGHTMARE_KILL = "LastNightmareKillTime";
    private static final int TRANSITION_USE_COOLDOWN = 20;
    // A pending Brew of Sleeping was saved as boolean true in earlier versions of the port.
    private static final byte BREW_SLEEP = 1;
    private static final byte FORCED_NIGHTMARE = 2;

    private DreamTravel() {
    }

    public static void requestSleep(LivingEntity drinker) {
        requestSleep(drinker, BREW_SLEEP);
    }

    public static void requestNightmare(LivingEntity eater) {
        requestSleep(eater, FORCED_NIGHTMARE);
    }

    private static void requestSleep(LivingEntity sleeper, byte request) {
        if (sleeper instanceof ServerPlayer player && player.level().dimension().equals(Level.OVERWORLD)
                && !isSpiritWalking(player) && !isGhost(player)) {
            CompoundTag data = player.getPersistentData();
            if (data.getByte(REQUEST_SLEEP) < request) data.putByte(REQUEST_SLEEP, request);
        }
    }

    public static void requestWake(ServerPlayer player) {
        persistent(player).putBoolean(FORCE_AWAKEN, true);
    }

    public static void requestManifest(ServerPlayer player) {
        if (player.level().dimension().equals(WitcheryDimensions.DREAM) && isSpiritWalking(player)
                && manifestSeconds(player) >= 5
                && player.level().getGameTime() >= player.getPersistentData().getLong(MANIFEST_REENTRY)) {
            player.getPersistentData().putBoolean(REQUEST_MANIFEST, true);
        }
    }

    public static void requestReturnFromGhost(ServerPlayer player) {
        player.getPersistentData().putBoolean(REQUEST_GHOST_RETURN, true);
    }

    public static void grantManifestation(ServerPlayer player, int seconds) {
        persistent(player).putInt(MANIFEST_DURATION, seconds);
    }

    public static void deferManifestationCountdown(ServerPlayer player) {
        if (isGhost(player) && manifestSeconds(player) >= 5) persistent(player).putBoolean(MANIFEST_SKIP, true);
    }

    public static int manifestSeconds(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getInt(MANIFEST_DURATION);
    }

    public static boolean isSpiritWalking(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(SPIRIT_WALKING);
    }

    public static boolean isNightmare(ServerLevel level) {
        return level.dimension().equals(WitcheryDimensions.DREAM) && level.players().stream().anyMatch(DreamTravel::isNightmare);
    }

    public static boolean isNightmare(ServerPlayer player) {
        return player.level().dimension().equals(WitcheryDimensions.DREAM)
                && player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(SPIRIT_WORLD).getBoolean("Nightmare");
    }

    public static boolean isDemonicNightmare(ServerLevel level) {
        return level.dimension().equals(WitcheryDimensions.DREAM)
                && level.players().stream().anyMatch(DreamTravel::isDemonicNightmare);
    }

    private static boolean isDemonicNightmare(ServerPlayer player) {
        return isNightmare(player)
                && player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(SPIRIT_WORLD).getBoolean("Demonic");
    }

    public static void markNightmareKilled(ServerPlayer player) {
        CompoundTag persistent = persistent(player);
        CompoundTag session = persistent.getCompound(SPIRIT_WORLD);
        session.putLong(LAST_NIGHTMARE_KILL, System.currentTimeMillis());
        persistent.put(SPIRIT_WORLD, session);
    }

    public static void spawnNightmare(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (level.getGameTime() % 100 != 0 || !player.isAlive() || player.isSpectator()
                || player.getAbilities().instabuild || !isNightmare(player) || level.getDifficulty() == Difficulty.PEACEFUL
                || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }
        AABB nearby = player.getBoundingBox().inflate(18);
        if (isDemonicNightmare(player)) {
            spawnDemonicHazard(player, nearby);
        }
        long lastKillTime = persistent(player).getCompound(SPIRIT_WORLD).getLong(LAST_NIGHTMARE_KILL);
        if (lastKillTime > System.currentTimeMillis() - 30_000
                || !level.getEntitiesOfClass(Nightmare.class, nearby,
                        nightmare -> nightmare.isAlive() && nightmare.targets(player)).isEmpty()) {
            return;
        }
        Nightmare nightmare = WitcheryEntities.NIGHTMARE.get().create(level);
        if (nightmare != null) {
            nightmare.assignVictim(player);
            spawnNear(player, nightmare, 2, 6);
        }
    }

    public static void spawnWakingNightmare(LivingEntity victim, int intensity) {
        if (!(victim.level() instanceof ServerLevel level) || !victim.isAlive() || level.dimension().equals(WitcheryDimensions.DREAM)
                || level.getDifficulty() == Difficulty.PEACEFUL || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                || victim instanceof Player player && (player.isSpectator() || player.getAbilities().instabuild)) {
            return;
        }
        int interval = intensity > 4 ? 30 : intensity > 2 ? 60 : 180;
        if (victim.getRandom().nextInt(interval) != 0) return;
        AABB nearby = victim.getBoundingBox().inflate(16, 8, 16);
        if (!level.getEntitiesOfClass(Nightmare.class, nearby, nightmare -> nightmare.isAlive() && nightmare.targets(victim)).isEmpty()) {
            return;
        }
        Nightmare nightmare = WitcheryEntities.NIGHTMARE.get().create(level);
        if (nightmare != null) {
            nightmare.assignVictim(victim);
            spawnNear(victim, nightmare, 2, 6);
        }
    }

    private static void spawnDemonicHazard(ServerPlayer player, AABB nearby) {
        ServerLevel level = player.serverLevel();
        double chance = player.getRandom().nextDouble();
        if (chance < 0.65) {
            double x = player.getX() - 2 + player.getRandom().nextInt(5);
            double y = player.getY() + 15;
            double z = player.getZ() - 2 + player.getRandom().nextInt(5);
            if (chance < 0.5) {
                level.addFreshEntity(new SmallFireball(level, x, y, z, 0, -0.2, 0));
            } else {
                LargeFireball ball = EntityType.FIREBALL.create(level);
                if (ball != null) {
                    ball.setPos(x, y, z);
                    ball.yPower = -0.1;
                    level.addFreshEntity(ball);
                }
            }
        } else if (chance < 0.75) {
            List<Mob> monsters = level.getEntitiesOfClass(Mob.class, nearby, mob -> mob.isAlive() && (mob instanceof Enemy || mob instanceof Demon));
            if (monsters.size() < 10 && monsters.stream().filter(Demon.class::isInstance).count() < 2) {
                Demon demon = WitcheryEntities.DEMON.get().create(level);
                if (demon != null) {
                    spawnNear(player, demon, 4, 8);
                }
            }
        }
    }

    private static void spawnNear(LivingEntity target, Mob mob, int minRange, int maxRange) {
        ServerLevel level = (ServerLevel) target.level();
        BlockPos near = target.blockPosition();
        int span = maxRange - minRange;
        int dx = target.getRandom().nextInt(span * 2 + 1) - span;
        int dz = target.getRandom().nextInt(span * 2 + 1) - span;
        BlockPos column = near.offset(dx + minRange * Integer.signum(dx), 0, dz + minRange * Integer.signum(dz));
        for (int offset = 0; offset <= 8; offset++) {
            BlockPos above = column.above(offset);
            BlockPos below = column.below(offset);
            BlockPos position = canStand(level, above) ? above : canStand(level, below) ? below : null;
            if (position != null && !level.getBlockState(position.below()).getFluidState().isSource()) {
                mob.moveTo(position.getX() + 0.5, position.getY(), position.getZ() + 0.5, 0, 0);
                if (level.noCollision(mob) && !level.containsAnyLiquid(mob.getBoundingBox())) {
                    level.addFreshEntity(mob);
                }
                return;
            }
        }
    }

    public static boolean isGhost(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(MANIFESTED);
    }

    public static void restoreStyle(ServerPlayer player, WitcheryPlayerData data) {
        if (!isSpiritWalking(player)) return;
        CompoundTag session = persistent(player).getCompound(SPIRIT_WORLD);
        applyStyle(data, nightmareLevel(session), isGhost(player));
    }

    public static boolean processRequests(ServerPlayer player) {
        if (!player.isAlive()) {
            return false;
        }
        CompoundTag data = player.getPersistentData();
        CompoundTag persistent = data.getCompound(Player.PERSISTED_NBT_TAG);
        boolean walking = persistent.getBoolean(SPIRIT_WALKING);
        boolean inDream = player.level().dimension().equals(WitcheryDimensions.DREAM);
        boolean ghost = isGhost(player);
        if (persistent.getBoolean(FORCE_AWAKEN) || walking && !inDream && !ghost) {
            persistent.remove(FORCE_AWAKEN);
            if (walking) {
                wake(player, persistent);
            } else if (inDream) {
                WitcheryDimensionTravelHooks.returnToOverworld(player);
            }
            return true;
        }
        if (data.getBoolean(REQUEST_MANIFEST)) {
            data.remove(REQUEST_MANIFEST);
            if (walking && inDream && !ghost && manifestSeconds(player) >= 5) {
                manifest(player, persistent);
                return true;
            }
        }
        if (ghost) {
            boolean requested = data.getBoolean(REQUEST_GHOST_RETURN);
            data.remove(REQUEST_GHOST_RETURN);
            if (requested || manifestExpired(player, persistent)) {
                returnFromGhost(player, persistent);
                return true;
            }
        }
        byte sleepRequest = data.getByte(REQUEST_SLEEP);
        if (sleepRequest != 0) {
            data.remove(REQUEST_SLEEP);
            if (player.level().dimension().equals(Level.OVERWORLD) && !walking && !isGhost(player)) {
                sleep(player, sleepRequest == FORCED_NIGHTMARE);
            }
            return true;
        }
        return false;
    }

    private static void sleep(ServerPlayer player, boolean forceNightmare) {
        ServerLevel overworld = player.serverLevel();
        ServerLevel dream = player.server.getLevel(WitcheryDimensions.DREAM);
        if (dream == null) {
            return;
        }

        SleepingBody body = WitcheryEntities.SLEEPING_BODY.get().create(overworld);
        if (body == null) {
            return;
        }
        BlockPos anchor = player.blockPosition();
        BlockPos arrival = findArrival(dream, anchor);
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        body.attachOwner(player);
        body.moveTo(anchor.getX() + 0.5, player.getY(), anchor.getZ() + 0.5, yaw, 0);
        body.setHealth(Math.max(1, player.getHealth()));
        if (!overworld.addFreshEntity(body)) {
            return;
        }

        ListTag bodyItems = player.getInventory().save(new ListTag());
        List<ItemStack> carried = takeItems(bodyItems, DreamTravel::carriesIn);
        CompoundTag hunger = new CompoundTag();
        player.getFoodData().addAdditionalSaveData(hunger);
        float health = Math.max(1, player.getHealth());
        boolean nightmare = true;
        boolean demonic = false;
        if (!forceNightmare) {
            DreamAmbience ambience = DreamAmbience.around(overworld, anchor);
            nightmare = player.getRandom().nextDouble() < ambience.nightmareChance();
            demonic = nightmare && player.getRandom().nextDouble() < ambience.demonicChance();
        }

        move(player, dream, arrival, yaw, pitch);
        if (player.serverLevel() != dream) {
            body.discard();
            return;
        }

        CompoundTag persistent = persistent(player);
        CompoundTag session = persistent.getCompound(SPIRIT_WORLD);
        session.put(OVERWORLD_INVENTORY, bodyItems);
        session.put(OVERWORLD_HUNGER, hunger);
        session.putFloat(OVERWORLD_HEALTH, health);
        session.putInt("OverworldBodyX", anchor.getX());
        session.putInt("OverworldBodyY", anchor.getY());
        session.putInt("OverworldBodyZ", anchor.getZ());
        session.putFloat("BodyYaw", yaw);
        session.putFloat("BodyPitch", pitch);
        session.putUUID(BODY_ENTITY, body.getUUID());
        session.putBoolean("Nightmare", nightmare);
        session.putBoolean("Demonic", demonic);
        persistent.put(SPIRIT_WORLD, session);
        persistent.putBoolean(SPIRIT_WALKING, true);

        restoreInventory(player, session.getList(SPIRIT_INVENTORY, Tag.TAG_COMPOUND), carried);
        player.getCooldowns().addCooldown(WitcheryIngredient.ICY_NEEDLE.item(), TRANSITION_USE_COOLDOWN);
        session.remove(SPIRIT_INVENTORY);
        if (session.contains(SPIRIT_HEALTH, Tag.TAG_ANY_NUMERIC)) {
            player.setHealth(Math.max(10, session.getFloat(SPIRIT_HEALTH)));
            session.remove(SPIRIT_HEALTH);
        }
        if (session.contains(SPIRIT_HUNGER, Tag.TAG_COMPOUND)) {
            player.getFoodData().readAdditionalSaveData(session.getCompound(SPIRIT_HUNGER));
            player.getFoodData().eat(16, 0.8F);
            session.remove(SPIRIT_HUNGER);
        }
        syncStyle(player, demonic ? 2 : nightmare ? 1 : 0, false);
        pushArrival(player);
    }

    private record DreamAmbience(double nightmareChance, double demonicChance) {
        private static final double BASE_NIGHTMARE_CHANCE = 0.998;
        private static final int RADIUS = 8;

        static DreamAmbience around(ServerLevel level, BlockPos anchor) {
            int cotton = 0;
            int sources = 0;
            int hearts = 0;
            int fire = 0;
            boolean nightmareWeave = false;
            Block cottonBlock = WitcheryBlocks.getRequired("somniancotton").get();
            Block spirit = WitcheryBlocks.getRequired("spiritflowing").get();
            Block heart = WitcheryBlocks.getRequired("demonheart").get();
            for (BlockPos pos : BlockPos.betweenClosed(anchor.offset(-RADIUS, -RADIUS, -RADIUS),
                    anchor.offset(RADIUS, RADIUS, RADIUS))) {
                if (!level.hasChunkAt(pos)) continue;
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof DreamCatcherBlock && state.getValue(DreamCatcherBlock.WEAVE) == DreamWeave.NIGHTMARES) {
                    nightmareWeave = true;
                } else if (state.is(cottonBlock) && cotton < 2) {
                    cotton++;
                } else if (state.is(spirit) && sources < 3 && state.getFluidState().isSource()) {
                    sources++;
                } else if (state.is(heart) && hearts < 2) {
                    hearts++;
                } else if (state.is(Blocks.FIRE) && fire < 3) {
                    fire++;
                }
            }
            if (!nightmareWeave) return new DreamAmbience(BASE_NIGHTMARE_CHANCE, 0);
            double danger = 0.35 * hearts + 0.1 * fire;
            double nightmare = Mth.clamp(BASE_NIGHTMARE_CHANCE - 0.5 - 0.1 * (sources + cotton) + danger, 0, 1);
            return new DreamAmbience(nightmare, hearts > 0 && sources > 0 ? danger : 0);
        }
    }

    private static void wake(ServerPlayer player, CompoundTag persistent) {
        CompoundTag session = persistent.getCompound(SPIRIT_WORLD);
        if (!session.contains(OVERWORLD_INVENTORY, Tag.TAG_LIST)) {
            persistent.remove(SPIRIT_WALKING);
            persistent.remove(MANIFESTED);
            syncStyle(player, 0, false);
            return;
        }
        ServerLevel origin = player.serverLevel();
        Vec3 originPos = player.position();
        boolean ghost = isGhost(player);
        ServerLevel overworld = player.server.overworld();
        BlockPos anchor = new BlockPos(session.getInt("OverworldBodyX"), session.getInt("OverworldBodyY"), session.getInt("OverworldBodyZ"));
        BlockPos arrival = findArrival(overworld, anchor);
        ListTag activeItems = player.getInventory().save(new ListTag());
        ListTag spiritItems = ghost ? session.getList(SPIRIT_INVENTORY, Tag.TAG_COMPOUND).copy() : activeItems;
        List<ItemStack> carried = takeItems(spiritItems, DreamTravel::carriesOut);
        if (ghost) {
            carried.addAll(takeItems(activeItems, DreamTravel::carriesOut));
        }
        CompoundTag hunger = new CompoundTag();
        player.getFoodData().addAdditionalSaveData(hunger);
        float health = Math.max(10, player.getHealth());

        move(player, overworld, arrival, session.getFloat("BodyYaw"), session.getFloat("BodyPitch"));
        if (player.serverLevel() != overworld) {
            requestWake(player);
            return;
        }

        if (ghost) {
            dropGhostInventory(origin, originPos, activeItems);
        }
        restoreInventory(player, session.getList(OVERWORLD_INVENTORY, Tag.TAG_COMPOUND), carried);
        player.getCooldowns().addCooldown(WitcheryIngredient.BREW_OF_SLEEPING.item(), TRANSITION_USE_COOLDOWN);
        session.remove(OVERWORLD_INVENTORY);
        session.put(SPIRIT_INVENTORY, spiritItems);
        session.put(SPIRIT_HUNGER, hunger);
        session.putFloat(SPIRIT_HEALTH, health);
        if (session.contains(OVERWORLD_HUNGER, Tag.TAG_COMPOUND)) {
            player.getFoodData().readAdditionalSaveData(session.getCompound(OVERWORLD_HUNGER));
            session.remove(OVERWORLD_HUNGER);
        }
        if (session.contains(OVERWORLD_HEALTH, Tag.TAG_ANY_NUMERIC)) {
            player.setHealth(Math.max(1, session.getFloat(OVERWORLD_HEALTH)));
            session.remove(OVERWORLD_HEALTH);
        }
        removeBody(player, overworld, session, anchor);
        session.putBoolean("Nightmare", false);
        session.putBoolean("Demonic", false);
        persistent.putBoolean(SPIRIT_WALKING, false);
        persistent.remove(MANIFESTED);
        player.clearFire();
        syncStyle(player, 0, false);
        pushArrival(player);
    }

    private static void manifest(ServerPlayer player, CompoundTag persistent) {
        CompoundTag session = persistent.getCompound(SPIRIT_WORLD);
        if (!session.contains(OVERWORLD_INVENTORY, Tag.TAG_LIST)) {
            return;
        }
        ServerLevel overworld = player.server.overworld();
        BlockPos arrival = findArrival(overworld, player.blockPosition());
        ListTag dreamItems = player.getInventory().save(new ListTag());
        List<ItemStack> carried = takeItems(dreamItems, DreamTravel::icyNeedle);
        move(player, overworld, arrival, player.getYRot(), player.getXRot());
        if (player.serverLevel() != overworld) {
            return;
        }
        session.put(SPIRIT_INVENTORY, dreamItems);
        session.putFloat(SPIRIT_HEALTH, Math.max(1, player.getHealth()));
        persistent.putBoolean(MANIFESTED, true);
        restoreInventory(player, new ListTag(), carried);
        player.getCooldowns().addCooldown(WitcheryIngredient.ICY_NEEDLE.item(), TRANSITION_USE_COOLDOWN);
        syncStyle(player, nightmareLevel(session), true);
    }

    private static void returnFromGhost(ServerPlayer player, CompoundTag persistent) {
        ServerLevel dream = player.server.getLevel(WitcheryDimensions.DREAM);
        if (dream == null) {
            requestWake(player);
            return;
        }
        ServerLevel origin = player.serverLevel();
        Vec3 originPos = player.position();
        CompoundTag session = persistent.getCompound(SPIRIT_WORLD);
        ListTag items = player.getInventory().save(new ListTag());
        List<ItemStack> carried = takeItems(items, stack -> icyNeedle(stack) || isBoundFetish(stack));
        BlockPos arrival = findArrival(dream, player.blockPosition());
        move(player, dream, arrival, player.getYRot(), player.getXRot());
        if (player.serverLevel() != dream) {
            requestReturnFromGhost(player);
            return;
        }
        dropGhostInventory(origin, originPos, items);
        restoreInventory(player, session.getList(SPIRIT_INVENTORY, Tag.TAG_COMPOUND), carried);
        session.remove(SPIRIT_INVENTORY);
        session.remove(SPIRIT_HEALTH);
        persistent.remove(MANIFESTED);
        player.getPersistentData().putLong(MANIFEST_REENTRY, dream.getGameTime() + TRANSITION_USE_COOLDOWN);
        player.getCooldowns().addCooldown(WitcheryIngredient.ICY_NEEDLE.item(), TRANSITION_USE_COOLDOWN);
        syncStyle(player, nightmareLevel(session), false);
    }

    private static boolean manifestExpired(ServerPlayer player, CompoundTag persistent) {
        if (player.serverLevel().getGameTime() % 100 != 0) {
            return false;
        }
        if (persistent.getBoolean(MANIFEST_SKIP)) {
            persistent.remove(MANIFEST_SKIP);
            return false;
        }
        int seconds = Math.max(0, persistent.getInt(MANIFEST_DURATION) - 5);
        if (seconds == 0) {
            persistent.remove(MANIFEST_DURATION);
            return true;
        }
        persistent.putInt(MANIFEST_DURATION, seconds);
        if (seconds == 60 || seconds == 30 || seconds == 15) {
            player.displayClientMessage(Component.translatable("message.witchery.manifest.remaining", seconds), false);
        }
        return false;
    }

    private static boolean icyNeedle(ItemStack stack) {
        return stack.is(WitcheryIngredient.ICY_NEEDLE.item());
    }

    private static boolean isBoundFetish(ItemStack stack) {
        return FetishBinding.isFetish(stack) && FetishBlockEntity.itemEffect(stack) > 0;
    }

    private static void dropGhostInventory(ServerLevel level, Vec3 position, ListTag items) {
        for (Tag slot : items) {
            ItemStack stack = WitcheryIngredient.loadInventoryStack((CompoundTag) slot);
            if (!stack.isEmpty()) {
                ItemEntity entity = new ItemEntity(level, position.x, position.y + 0.25, position.z, stack);
                entity.setDefaultPickUpDelay();
                level.addFreshEntity(entity);
            }
        }
    }

    private static CompoundTag persistent(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return data.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static void removeBody(ServerPlayer player, ServerLevel overworld, CompoundTag session, BlockPos anchor) {
        if (session.hasUUID(BODY_ENTITY) && overworld.getEntity(session.getUUID(BODY_ENTITY)) instanceof SleepingBody body) {
            body.discard();
        } else {
            overworld.getEntitiesOfClass(SleepingBody.class, new net.minecraft.world.phys.AABB(anchor).inflate(3),
                    body -> body.belongsTo(player)).forEach(Entity::discard);
        }
        session.remove(BODY_ENTITY);
        session.remove("OverworldBodyX");
        session.remove("OverworldBodyY");
        session.remove("OverworldBodyZ");
        session.remove("BodyYaw");
        session.remove("BodyPitch");
    }

    private static void move(ServerPlayer player, ServerLevel destination, BlockPos arrival, float yaw, float pitch) {
        player.teleportTo(destination, arrival.getX() + 0.5, arrival.getY() + 0.1, arrival.getZ() + 0.5, yaw, pitch);
        if (player.connection != null) player.connection.resetPosition();
        player.resetFallDistance();
    }

    private static void pushArrival(ServerPlayer player) {
        player.setDeltaMovement(0, 0.1, 0);
        if (player.connection != null && player.connection.connection.isConnected()) {
            WitcheryNetwork.sendPushTarget(player, player.getId(), 0, 0.1, 0);
        }
    }

    private static BlockPos findArrival(ServerLevel level, BlockPos near) {
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 2;
        int start = Mth.clamp(near.getY(), minY, maxY);
        BlockPos local = findStandingInColumn(level, near.atY(start), minY, maxY, Math.max(start - minY, maxY - start));
        if (local != null) {
            return local;
        }
        for (int radius = 1; radius <= 2; radius++) {
            for (BlockPos column : BlockPos.betweenClosed(near.offset(-radius, 0, -radius), near.offset(radius, 0, radius))) {
                if (Math.max(Math.abs(column.getX() - near.getX()), Math.abs(column.getZ() - near.getZ())) != radius) continue;
                BlockPos nearby = findStandingInColumn(level, column.atY(start), minY, maxY, 4);
                if (nearby != null) {
                    return nearby;
                }
            }
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, near);
    }

    private static BlockPos findStandingInColumn(ServerLevel level, BlockPos near, int minY, int maxY,
                                                int maxOffset) {
        int start = near.getY();
        for (int offset = 0; offset <= maxOffset; offset++) {
            BlockPos above = near.atY(start + offset);
            if (above.getY() <= maxY && canStand(level, above)) {
                return above;
            }
            BlockPos below = near.atY(start - offset);
            if (offset > 0 && below.getY() >= minY && canStand(level, below)) {
                return below;
            }
        }
        return null;
    }

    private static boolean canStand(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())
                && (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) || level.getFluidState(below).is(FluidTags.WATER));
    }

    private static boolean carriesIn(ItemStack stack) {
        return stack.is(WitcheryIngredient.ICY_NEEDLE.item()) || stack.is(WitcheryIngredient.MUTANDIS.item());
    }

    private static boolean carriesOut(ItemStack stack) {
        return stack.is(WitcheryIngredient.ICY_NEEDLE.item()) || stack.is(WitcheryIngredient.DISTURBED_COTTON.item())
                || stack.is(WitcheryIngredient.MELLIFLUOUS_HUNGER.item())
                || stack.is(WitcheryIngredient.BREW_OF_FLOWING_SPIRIT.item())
                || stack.is(WitcheryIngredient.SUBDUED_SPIRIT.item())
                || stack.is(WitcheryBlocks.getRequired("somniancotton").get().asItem());
    }

    private static List<ItemStack> takeItems(ListTag inventory, Predicate<ItemStack> mover) {
        List<ItemStack> carried = new ArrayList<>();
        var entries = inventory.iterator();
        while (entries.hasNext()) {
            ItemStack stack = WitcheryIngredient.loadInventoryStack((CompoundTag) entries.next());
            if (mover.test(stack)) {
                carried.add(stack);
                entries.remove();
            }
        }
        return carried;
    }

    private static void restoreInventory(ServerPlayer player, ListTag inventory, List<ItemStack> carried) {
        ListTag converted = new ListTag();
        for (Tag entry : inventory) {
            CompoundTag original = (CompoundTag) entry;
            ItemStack stack = WitcheryIngredient.loadInventoryStack(original);
            if (!stack.isEmpty()) {
                CompoundTag slot = stack.save(new CompoundTag());
                slot.putByte("Slot", original.getByte("Slot"));
                converted.add(slot);
            }
        }
        player.getInventory().load(converted);
        for (ItemStack stack : carried) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        player.inventoryMenu.broadcastChanges();
    }

    private static int nightmareLevel(CompoundTag session) {
        return session.getBoolean("Nightmare") ? session.getBoolean("Demonic") ? 2 : 1 : 0;
    }

    private static void syncStyle(ServerPlayer player, int nightmare, boolean ghost) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            applyStyle(data, nightmare, ghost);
            data.bumpSyncRevision();
            WitcheryNetwork.sendPartialExtendedPlayerSync(player, data);
            for (ServerPlayer viewer : player.serverLevel().players()) {
                if (viewer == player) continue;
                WitcheryNetwork.sendPlayerStyle(viewer, player.getGameProfile().getName(), data.getGrotesqueTicks(),
                        nightmare, ghost, data.getCreatureTypeOrdinal(), data.getHumanBlood(), data.getOtherPlayerSkin());
            }
        });
    }

    private static void applyStyle(WitcheryPlayerData data, int nightmare, boolean ghost) {
        data.applyPlayerStyle(data.getOtherPlayerSkin(), data.getGrotesqueTicks(), nightmare, ghost,
                data.getCreatureTypeOrdinal(), data.getHumanBlood());
    }
}
