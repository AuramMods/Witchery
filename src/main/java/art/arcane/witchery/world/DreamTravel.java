package art.arcane.witchery.world;

import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.DreamWeave;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

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
    private static final String LAST_NIGHTMARE_KILL = "LastNightmareKillTime";
    private static final int TRANSITION_USE_COOLDOWN = 20;

    private DreamTravel() {
    }

    public static void requestSleep(LivingEntity drinker) {
        if (drinker instanceof ServerPlayer player && player.level().dimension().equals(Level.OVERWORLD)
                && !isSpiritWalking(player) && !isGhost(player)) {
            player.getPersistentData().putBoolean(REQUEST_SLEEP, true);
        }
    }

    public static void requestWake(ServerPlayer player) {
        persistent(player).putBoolean(FORCE_AWAKEN, true);
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
        long lastKillTime = persistent(player).getCompound(SPIRIT_WORLD).getLong(LAST_NIGHTMARE_KILL);
        if (lastKillTime > System.currentTimeMillis() - 30_000
                || !level.getEntitiesOfClass(Nightmare.class, player.getBoundingBox().inflate(18),
                        nightmare -> nightmare.isAlive() && nightmare.targets(player)).isEmpty()) {
            return;
        }
        BlockPos near = player.blockPosition();
        int dx = player.getRandom().nextInt(9) - 4;
        int dz = player.getRandom().nextInt(9) - 4;
        BlockPos column = near.offset(dx + 2 * Integer.signum(dx), 0, dz + 2 * Integer.signum(dz));
        for (int offset = 0; offset <= 8; offset++) {
            BlockPos above = column.above(offset);
            BlockPos below = column.below(offset);
            BlockPos position = canStand(level, above) ? above : canStand(level, below) ? below : null;
            if (position != null && !level.getBlockState(position.below()).getFluidState().isSource()) {
                Nightmare nightmare = WitcheryEntities.NIGHTMARE.get().create(level);
                if (nightmare == null) return;
                nightmare.moveTo(position.getX() + 0.5, position.getY(), position.getZ() + 0.5, 0, 0);
                if (level.noCollision(nightmare) && !level.containsAnyLiquid(nightmare.getBoundingBox())) {
                    nightmare.assignVictim(player);
                    level.addFreshEntity(nightmare);
                }
                return;
            }
        }
    }

    private static boolean isGhost(ServerPlayer player) {
        return WitcheryPlayerDataProvider.get(player).map(data -> data.isGhost()).orElse(false);
    }

    public static boolean processRequests(ServerPlayer player) {
        if (!player.isAlive()) {
            return false;
        }
        CompoundTag data = player.getPersistentData();
        CompoundTag persistent = data.getCompound(Player.PERSISTED_NBT_TAG);
        boolean walking = persistent.getBoolean(SPIRIT_WALKING);
        boolean inDream = player.level().dimension().equals(WitcheryDimensions.DREAM);
        if (persistent.getBoolean(FORCE_AWAKEN) || walking && !inDream && !isGhost(player)) {
            persistent.remove(FORCE_AWAKEN);
            if (walking) {
                wake(player, persistent);
            } else if (inDream) {
                WitcheryDimensionTravelHooks.routePlayer(player, WitcheryDimensionTravelHooks.TravelTrigger.RETURN_TO_OVERWORLD);
            }
            return true;
        }
        if (data.getBoolean(REQUEST_SLEEP)) {
            data.remove(REQUEST_SLEEP);
            if (player.level().dimension().equals(Level.OVERWORLD) && !walking && !isGhost(player)) {
                sleep(player);
            }
            return true;
        }
        return false;
    }

    private static void sleep(ServerPlayer player) {
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
        DreamAmbience ambience = DreamAmbience.around(overworld, anchor);
        boolean nightmare = player.getRandom().nextDouble() < ambience.nightmareChance();
        boolean demonic = nightmare && player.getRandom().nextDouble() < ambience.demonicChance();

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
        updateNightmare(player, demonic ? 2 : nightmare ? 1 : 0);
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
            return;
        }
        ServerLevel overworld = player.server.overworld();
        BlockPos anchor = new BlockPos(session.getInt("OverworldBodyX"), session.getInt("OverworldBodyY"), session.getInt("OverworldBodyZ"));
        BlockPos arrival = findArrival(overworld, anchor);
        ListTag spiritItems = player.getInventory().save(new ListTag());
        List<ItemStack> carried = takeItems(spiritItems, DreamTravel::carriesOut);
        CompoundTag hunger = new CompoundTag();
        player.getFoodData().addAdditionalSaveData(hunger);
        float health = Math.max(10, player.getHealth());

        move(player, overworld, arrival, session.getFloat("BodyYaw"), session.getFloat("BodyPitch"));
        if (player.serverLevel() != overworld) {
            requestWake(player);
            return;
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
        player.clearFire();
        updateNightmare(player, 0);
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
        player.teleportTo(destination, arrival.getX() + 0.5, arrival.getY() + 0.05, arrival.getZ() + 0.5, yaw, pitch);
        player.resetFallDistance();
    }

    private static BlockPos findArrival(ServerLevel level, BlockPos near) {
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 2;
        int start = Mth.clamp(near.getY(), minY, maxY);
        for (int offset = 0; offset <= Math.max(start - minY, maxY - start); offset++) {
            BlockPos above = near.atY(start + offset);
            if (above.getY() <= maxY && canStand(level, above)) {
                return above;
            }
            BlockPos below = near.atY(start - offset);
            if (offset > 0 && below.getY() >= minY && canStand(level, below)) {
                return below;
            }
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, near);
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

    private static void updateNightmare(ServerPlayer player, int nightmare) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            data.applyPlayerStyle(data.getOtherPlayerSkin(), data.getGrotesqueTicks(), nightmare, false,
                    data.getCreatureTypeOrdinal(), data.getHumanBlood());
            data.bumpSyncRevision();
            WitcheryNetwork.sendPartialExtendedPlayerSync(player, data);
        });
    }
}
