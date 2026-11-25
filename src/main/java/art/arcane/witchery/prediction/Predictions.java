package art.arcane.witchery.prediction;

import art.arcane.witchery.brewing.BrewTerrain;
import art.arcane.witchery.entity.BabaYaga;
import art.arcane.witchery.entity.Ent;
import art.arcane.witchery.entity.WitchOwl;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public final class Predictions {
    private static final String SKILL = "WITCFTeller";
    private static final String ROOT = "WITCPredict";
    private static final String LIST = "WITCPreList";
    private static final String ID = "WITCPreID";
    private static final String TIME = "WITCPreTime";
    private static final String VISITED_NETHER = "WITCVisitedNether";
    private static final String MESSAGE = "witchery.prediction.";
    private static final int CHECK_INTERVAL = 100;
    private static final long DUE_AFTER = 9_600;

    enum Kind {
        ZOMBIE(1, 13, "zombie"),
        ARROW(2, 13, "arrowhit"),
        ENT(3, 3, "ent"),
        FALL(4, 13, "fall"),
        IRON(5, 8, "iron", 1_212),
        DIAMOND(6, 3, "diamond", 1_208),
        EMERALD(7, 3, "emerald", 1_208),
        TREASURE(8, 2, "treasure", 1_210),
        LOVE(9, 2, "love", 1_210),
        BABA_HOSTILE(10, 2, "bababad"),
        BABA_ALLY(11, 2, "babagood"),
        FRIEND(12, 3, "friend"),
        OWL_RESCUE(13, 13, "rescued", 1_208),
        WOLF_RESCUE(14, 13, "rescued", 1_208),
        WET(15, 13, "wet"),
        NETHER(16, 3, "tothenether"),
        COAL(17, 13, "coal", 1_208);

        private final int id;
        private final int weight;
        private final String message;
        private final long dueAfter;

        Kind(int id, int weight, String message) {
            this(id, weight, message, DUE_AFTER);
        }

        Kind(int id, int weight, String message, long dueAfter) {
            this.id = id;
            this.weight = weight;
            this.message = message;
            this.dueAfter = dueAfter;
        }

        @Nullable
        static Kind byId(int id) {
            for (Kind kind : values()) {
                if (kind.id == id) return kind;
            }
            return null;
        }

        double tickChance(long elapsed) {
            return switch (this) {
                case LOVE -> elapsed > dueAfter ? 0.05 : 0.01;
                case ZOMBIE, ARROW, ENT, FALL, BABA_HOSTILE, BABA_ALLY, FRIEND, WET, NETHER -> elapsed > dueAfter ? 0.05 : 0;
                default -> 0;
            };
        }
    }

    private Predictions() {}

    public static void attune(ServerPlayer player) {
        CompoundTag persisted = persisted(player);
        persisted.putBoolean(SKILL, true);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static void read(ServerPlayer subject, ServerPlayer fortuneTeller) {
        if (!subject.isCreative() && !persisted(fortuneTeller).getBoolean(SKILL)) {
            subject.sendSystemMessage(Component.translatable(MESSAGE + "unskilled").withStyle(ChatFormatting.RED));
            return;
        }

        CompoundTag persisted = persisted(subject);
        CompoundTag root = persisted.getCompound(ROOT);
        ListTag readings = root.getList(LIST, Tag.TAG_COMPOUND);
        readings.removeIf(entry -> Kind.byId(((CompoundTag) entry).getInt(ID)) == null);
        if (readings.isEmpty()) {
            Kind kind = choose(subject);
            if (kind != null) {
                CompoundTag reading = new CompoundTag();
                reading.putInt(ID, kind.id);
                reading.putLong(TIME, timeInTicks());
                readings.add(reading);
                root.put(LIST, readings);
                persisted.put(ROOT, root);
                subject.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
            }
        }
        var reported = EnumSet.noneOf(Kind.class);
        for (int index = 0; index < readings.size(); index++) {
            Kind kind = Kind.byId(readings.getCompound(index).getInt(ID));
            if (reported.add(kind)) {
                subject.sendSystemMessage(Component.translatable(MESSAGE + kind.message, subject.getDisplayName())
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
        if (reported.isEmpty()) {
            subject.sendSystemMessage(Component.translatable(MESSAGE + "none", subject.getDisplayName())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    public static void onHurt(ServerPlayer player, DamageSource source) {
        ListTag readings = readings(player);
        for (int index = readings.size() - 1; index >= 0; index--) {
            Kind kind = Kind.byId(readings.getCompound(index).getInt(ID));
            if (kind == null) continue;
            boolean fulfilled = switch (kind) {
                case ZOMBIE -> source.getEntity() instanceof Zombie;
                case ARROW -> source.getDirectEntity() instanceof AbstractArrow;
                case ENT -> source.getEntity() instanceof Ent;
                case FALL -> source.is(DamageTypeTags.IS_FALL);
                case BABA_HOSTILE, BABA_ALLY -> source.getEntity() instanceof BabaYaga;
                case FRIEND -> source.getEntity() instanceof Wolf;
                case OWL_RESCUE, WOLF_RESCUE -> rescue(kind, player, source);
                default -> false;
            };
            if (fulfilled) readings.remove(index);
        }
    }

    public static void onNetherVisit(ServerPlayer player) {
        CompoundTag persisted = persisted(player);
        persisted.putBoolean(VISITED_NETHER, true);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static void onHarvest(ServerPlayer player, BlockPos pos, BlockState state, List<ItemStack> drops) {
        ServerLevel level = player.serverLevel();
        // A real harvest reaches loot generation after the block is removed. Loot previews leave it in place.
        if (player.isCreative() || player.isSpectator() || level.getBlockState(pos).is(state.getBlock())
                || !level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) return;

        ListTag readings = readings(player);
        for (int index = readings.size() - 1; index >= 0; index--) {
            CompoundTag reading = readings.getCompound(index);
            Kind kind = Kind.byId(reading.getInt(ID));
            if (kind == null) continue;
            long elapsed = timeInTicks() - reading.getLong(TIME);
            double chance = elapsed > kind.dueAfter ? 0.05 : 0.01;
            boolean fulfilled = switch (kind) {
                case IRON, DIAMOND, EMERALD, COAL -> MiningPredictions.addOre(kind, level.getRandom(), state, drops,
                        chance, elapsed > 36_000);
                case TREASURE -> MiningPredictions.buryChest(player, pos, state, chance);
                default -> false;
            };
            if (fulfilled) readings.remove(index);
        }
    }

    public static void tick(ServerPlayer player) {
        if (player.level().getGameTime() % CHECK_INTERVAL != 0 || player.isSpectator() || !player.isAlive()) return;
        ListTag readings = readings(player);
        long now = timeInTicks();
        for (Tag pending : List.copyOf(readings)) {
            var reading = (CompoundTag) pending;
            int index = readings.lastIndexOf(reading);
            if (index < 0) continue;
            Kind kind = Kind.byId(reading.getInt(ID));
            if (kind == null || kind == Kind.WET && player.isInWaterRainOrBubble()
                    || kind == Kind.NETHER && player.level().dimension().equals(Level.NETHER)) {
                readings.remove(index);
                continue;
            }
            double chance = kind.tickChance(now - reading.getLong(TIME));
            if (chance > 0 && player.level().getRandom().nextDouble() < chance) {
                // Remove the reading before effects that can post another player event.
                readings.remove(index);
                if (!fulfill(kind, player)) readings.add(Math.min(index, readings.size()), reading);
            }
        }
    }

    @Nullable
    private static Kind choose(ServerPlayer subject) {
        boolean canPredictNether = !subject.level().dimension().equals(Level.NETHER) && persisted(subject).getBoolean(VISITED_NETHER);
        int weight = 0;
        for (Kind kind : Kind.values()) {
            if (kind != Kind.NETHER || canPredictNether) weight += kind.weight;
        }
        int choice = subject.level().getRandom().nextInt(weight);
        for (Kind kind : Kind.values()) {
            if (kind == Kind.NETHER && !canPredictNether) continue;
            choice -= kind.weight;
            if (choice < 0) return kind;
        }
        return null;
    }

    private static boolean fulfill(Kind kind, ServerPlayer player) {
        return switch (kind) {
            case ZOMBIE -> hostileEncounter(player, EntityType.ZOMBIE);
            case ARROW -> hostileEncounter(player, EntityType.SKELETON);
            case ENT -> hostileEncounter(player, WitcheryEntities.ENT.get());
            case FALL -> sinkGround(player, false);
            case LOVE -> startCourtship(player);
            case BABA_HOSTILE -> hostileEncounter(player, WitcheryEntities.BABA_YAGA.get());
            case BABA_ALLY -> encounter(player, WitcheryEntities.BABA_YAGA.get(), 4, baba -> baba.bind(player));
            case FRIEND -> encounter(player, EntityType.WOLF, 4, wolf -> wolf.tame(player));
            case WET -> sinkGround(player, true);
            case NETHER -> startNetherForecast(player);
            default -> false;
        };
    }

    private static boolean rescue(Kind kind, ServerPlayer player, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == player || !attacker.isAlive()) return false;
        if (kind == Kind.OWL_RESCUE) {
            return encounter(player, WitcheryEntities.OWL.get(), 4, owl -> owl.attackTemporarily(attacker, 300));
        }
        return encounter(player, EntityType.WOLF, 4, wolf -> {
            wolf.setTarget(attacker);
            wolf.setLastHurtByMob(attacker);
        });
    }

    private static boolean hostileEncounter(ServerPlayer player, EntityType<? extends Mob> type) {
        return encounter(player, type, 4, mob -> mob.setTarget(player));
    }

    private static boolean startCourtship(ServerPlayer player) {
        if (!encounter(player, EntityType.VILLAGER, 6, villager -> PredictedCourtship.start(villager, player))) return false;
        WitcherySounds.playLegacyPlayerCue(player, WitcherySounds.FORECAST_LOVE.get(), SoundSource.NEUTRAL, 0.5F);
        return true;
    }

    private static <T extends Mob> boolean encounter(ServerPlayer player, EntityType<T> type, int radius, Consumer<T> setup) {
        ServerLevel level = player.serverLevel();
        RandomSource random = level.getRandom();
        BlockPos start = player.blockPosition().offset(encounterOffset(random, radius), 0, encounterOffset(random, radius));
        BlockPos ground = encounterGround(level, start);
        BlockPos feet = ground.above();
        if (level.isOutsideBuildHeight(feet) || !level.getWorldBorder().isWithinBounds(feet) || level.isEmptyBlock(ground)) return false;
        if (type == EntityType.VILLAGER && !level.getBlockState(ground.below()).isSolidRender(level, ground.below())) return false;
        T mob = type.create(level);
        if (mob == null || mob instanceof Monster && level.getDifficulty() == Difficulty.PEACEFUL) return false;
        int requiredAir = Mth.ceil(mob.getBbHeight());
        if (requiredAir > 6) return false;
        for (int y = 0; y < requiredAir; y++) {
            BlockPos air = feet.above(y);
            if (level.isOutsideBuildHeight(air) || !level.isEmptyBlock(air)) return false;
        }
        Vec3 arrival = Vec3.atBottomCenterOf(feet).add(0, 0.05, 0);
        mob.moveTo(arrival.x, arrival.y, arrival.z, 0, 0);
        if (!level.noCollision(mob)) return false;
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(feet), MobSpawnType.EVENT, null, null);
        setup.accept(mob);
        if (!level.addFreshEntity(mob)) return false;
        WitcheryNetwork.sendPredictionArrival(level, mob.position());
        return true;
    }

    private static BlockPos encounterGround(ServerLevel level, BlockPos start) {
        BlockPos.MutableBlockPos ground = start.mutable();
        while (ground.getY() < start.getY() + 8 && !level.isEmptyBlock(ground)) ground.move(Direction.UP);
        while (ground.getY() > level.getMinBuildHeight() && level.isEmptyBlock(ground)) ground.move(Direction.DOWN);
        return ground.immutable();
    }

    private static int encounterOffset(RandomSource random, int radius) {
        int offset = random.nextInt(5);
        return offset <= 2 ? offset - radius : offset + radius - 4;
    }

    private static boolean sinkGround(ServerPlayer player, boolean water) {
        ServerLevel level = player.serverLevel();
        BlockPos feet = player.blockPosition();
        // Modern farmland's collision surface is slightly below the top of its block.
        BlockPos ground = feet.atY(player.onGround() ? player.getOnPos().getY() : feet.getY() - 1);
        int depth = water ? 2 : 5;
        // Keep the original clearance above the bottom of the dimension.
        if (ground.getY() - level.getMinBuildHeight() <= depth + 3
                || water && level.dimensionType().ultraWarm()) return false;
        var field = BlockPos.betweenClosed(ground.offset(-1, 0, -1), ground.offset(1, 0, 1));
        for (BlockPos pos : field) {
            BlockState soil = level.getBlockState(pos);
            if (!soil.is(BlockTags.DIRT) && !soil.is(Blocks.FARMLAND)) return false;
        }
        if (!BrewTerrain.placeGroup(level, player, Direction.UP, () -> {
            for (BlockPos surface : field) {
                if (!level.setBlockAndUpdate(surface, Blocks.GRAVEL.defaultBlockState())) return false;
            }
            return true;
        })) return false;

        BlockState fill = (water ? Blocks.WATER : Blocks.AIR).defaultBlockState();
        for (BlockPos surface : field) {
            for (int depthOffset = 1; depthOffset <= depth; depthOffset++) {
                BlockPos pos = surface.below(depthOffset);
                if (!BrewTerrain.isProtectedTerrain(level.getBlockState(pos))) {
                    BrewTerrain.setBlock(player, level, pos, fill, Direction.UP);
                }
            }
        }
        return true;
    }

    private static boolean startNetherForecast(ServerPlayer player) {
        ServerLevel source = player.serverLevel();
        if (source.dimension().equals(Level.NETHER)) return false;
        player.sendSystemMessage(Component.translatable(MESSAGE + "tothenether.summoned").withStyle(ChatFormatting.LIGHT_PURPLE));
        // The original forecast simulates one tick of portal contact. Vanilla handles warm-up and cooldown.
        player.handleInsidePortal(player.blockPosition());

        RandomSource random = source.getRandom();
        BlockPos start = player.blockPosition().below().offset(encounterOffset(random, 4), 0, encounterOffset(random, 4));
        BlockPos ground = encounterGround(source, start);
        if (!source.isOutsideBuildHeight(ground) && source.getWorldBorder().isWithinBounds(ground)
                && source.isEmptyBlock(ground.above()) && source.isEmptyBlock(ground.above(2))) {
            var blaze = EntityType.BLAZE.create(source);
            if (blaze != null) {
                blaze.moveTo(ground.getX(), ground.getY(), ground.getZ(), 0, 0);
                source.addFreshEntity(blaze);
            }
        }
        return true;
    }

    private static CompoundTag persisted(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static ListTag readings(ServerPlayer player) {
        return persisted(player).getCompound(ROOT).getList(LIST, Tag.TAG_COMPOUND);
    }

    private static long timeInTicks() {
        return System.currentTimeMillis() / 50L;
    }
}
