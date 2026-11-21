package art.arcane.witchery.world;

import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.block.entity.MirrorBlockEntity;
import art.arcane.witchery.entity.Reflection;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class MirrorTravel {
    private static final String ENTRY = "MirrorWorldEntryPoint";
    private static final String COOLDOWN = "WITCMirrorTravelCooldown";
    private static final String PENDING = "WITCPendingMirror";
    private static final int TRANSIT_COOLDOWN = 60;

    private MirrorTravel() {
    }

    public static void reconnect(ServerLevel level, MirrorBlockEntity source) {
        if (source.isRoomExit() || level.dimension().equals(WitcheryDimensions.MIRROR)) return;
        ServerLevel mirror = level.getServer().getLevel(WitcheryDimensions.MIRROR);
        MirrorBlockEntity exit = linkedFrame(mirror, source);
        if (exit == null || !exit.isRoomExit() || exit.isConnected() && !position(level, source).equals(exit.counterpart())) return;
        UUID connection = source.connectionId() == null ? UUID.randomUUID() : source.connectionId();
        source.bind(position(mirror, exit), connection);
        exit.bind(position(level, source), connection);
        if (exit.demonSlain()) source.markDemonSlain();
        if (source.demonSlain()) exit.markDemonSlain();
    }

    public static void unlink(ServerLevel level, MirrorBlockEntity source) {
        GlobalPos destination = source.counterpart();
        if (destination == null) return;
        MirrorBlockEntity other = linkedFrame(level.getServer().getLevel(destination.dimension()), source);
        if (other != null && position(level, source).equals(other.counterpart())) other.setDisconnected();
    }

    public static void walkThrough(ServerLevel level, MirrorBlockEntity frame, Entity actor) {
        if (actor.level() != level || actor.tickCount % 5 != 1 || !(actor instanceof LivingEntity || actor instanceof ItemEntity)
                || !actor.isAlive() || actor.isSpectator() || actor.isPassenger() || actor.isVehicle()
                || actor.getPersistentData().getLong(COOLDOWN) > level.getGameTime()) {
            return;
        }
        if (actor instanceof ServerPlayer) {
            CompoundTag pending = new CompoundTag();
            pending.putLong("Pos", frame.getBlockPos().asLong());
            pending.putString("Dimension", level.dimension().location().toString());
            actor.getPersistentData().put(PENDING, pending);
        } else {
            traverse(level, frame, actor);
        }
    }

    public static boolean processPlayerCrossing(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(PENDING, Tag.TAG_COMPOUND)) return false;
        CompoundTag pending = data.getCompound(PENDING);
        data.remove(PENDING);
        ServerLevel level = player.serverLevel();
        BlockPos upper = BlockPos.of(pending.getLong("Pos"));
        if (!player.isAlive() || !level.dimension().location().toString().equals(pending.getString("Dimension"))
                || upper.distSqr(player.blockPosition()) > 9) return false;
        MirrorBlockEntity frame = frameAt(level, upper);
        return frame != null && traverse(level, frame, player);
    }

    private static boolean traverse(ServerLevel level, MirrorBlockEntity frame, Entity actor) {
        boolean inMirror = level.dimension().equals(WitcheryDimensions.MIRROR);
        if (!frame.isRoomExit() && (inMirror || frame.demonSlain()) && tryBridge(level, frame, actor, inMirror)) return true;
        if (!(actor instanceof ServerPlayer player)) return false;
        if (inMirror && frame.isRoomExit() && (entry(player) == null || frame.getBlockPos().equals(entry(player)))) {
            return leaveRoom(level, frame, player);
        } else if (!inMirror && !frame.isRoomExit()) {
            return enterRoom(level, frame, player);
        }
        return false;
    }

    private static boolean enterRoom(ServerLevel exterior, MirrorBlockEntity source, ServerPlayer player) {
        ServerLevel mirror = exterior.getServer().getLevel(WitcheryDimensions.MIRROR);
        if (mirror == null) return false;
        MirrorBlockEntity exit = linkedFrame(mirror, source);
        if (exit != null && !position(exterior, source).equals(exit.counterpart())) return false;
        if (exit == null) exit = createRoom(exterior, mirror, source);
        if (exit == null || !exit.isRoomExit()) return false;
        if (move(player, mirror, front(exit), exit.facing().toYRot())) {
            rememberEntry(player, exit.getBlockPos());
            if (source.demonSlain()) exit.markDemonSlain();
            return true;
        }
        return false;
    }

    public static void tickRoom(Level level, BlockPos pos, BlockState state, MirrorBlockEntity exit) {
        if (!(level instanceof ServerLevel mirror) || !mirror.dimension().equals(WitcheryDimensions.MIRROR)
                || exit.demonSlain() || mirror.getGameTime() % 10 != 0
                || !mirror.areEntitiesLoaded(ChunkPos.asLong(pos))) {
            return;
        }
        mirror.getEntitiesOfClass(ServerPlayer.class, Reflection.roomBounds(pos),
                        player -> player.isAlive() && !player.isSpectator() && pos.equals(entry(player)))
                .stream().findFirst().ifPresent(player -> Reflection.spawnForRoom(mirror, exit, player));
    }

    @Nullable
    private static MirrorBlockEntity createRoom(ServerLevel exterior, ServerLevel mirror, MirrorBlockEntity source) {
        RoomCounter counter = mirror.getDataStorage().computeIfAbsent(RoomCounter::load, RoomCounter::new, "witchery_mirror_rooms");
        MirrorBlock inner = (MirrorBlock) WitcheryBlocks.getRequired("mirrorblock2").get();
        int top = MirrorChunkGenerator.chamberTop();
        for (int attempt = 0; attempt < 45; attempt++) {
            BlockPos pos = counter.next(top);
            if (!mirror.getBlockState(pos.below(3)).is(WitcheryBlocks.getRequired("mirrorwall").get())) {
                continue;
            }
            if (inner.placePair(mirror, pos, Direction.EAST) && mirror.getBlockEntity(pos) instanceof MirrorBlockEntity exit) {
                UUID connection = source.connectionId() == null ? UUID.randomUUID() : source.connectionId();
                source.bind(position(mirror, exit), connection);
                exit.bind(position(exterior, source), connection);
                return exit;
            }
        }
        return null;
    }

    private static boolean leaveRoom(ServerLevel mirror, MirrorBlockEntity exit, ServerPlayer player) {
        GlobalPos destination = exit.counterpart();
        if (destination == null) return false;
        ServerLevel exterior = mirror.getServer().getLevel(destination.dimension());
        MirrorBlockEntity source = linkedFrame(exterior, exit);
        boolean left = false;
        if (source != null && position(mirror, exit).equals(source.counterpart())) {
            left = move(player, exterior, front(source), source.facing().toYRot());
        } else {
            GlobalPos room = position(mirror, exit);
            for (ServerPlayer carrier : mirror.getServer().getPlayerList().getPlayers()) {
                if (carrier.serverLevel() != mirror && carrier.isAlive()
                        && (carrier.getInventory().items.stream().anyMatch(stack -> MirrorBlockEntity.carriesLink(stack, room, exit.connectionId()))
                        || MirrorBlockEntity.carriesLink(carrier.getOffhandItem(), room, exit.connectionId()))) {
                    left = leaveBesideCarrier(player, carrier);
                    if (left) break;
                }
            }
            if (!left && exterior != null) {
                Vec3 origin = nearbyFooting(exterior, destination.pos().below());
                left = origin != null && move(player, exterior, origin, player.getYRot());
            }
        }
        if (left) persist(player).remove(ENTRY);
        return left;
    }

    private static boolean leaveBesideCarrier(ServerPlayer player, ServerPlayer carrier) {
        ServerLevel exterior = carrier.serverLevel();
        for (BlockPos candidate : BlockPos.withinManhattan(carrier.blockPosition(), 2, 1, 2)) {
            if (hasFooting(exterior, candidate) && move(player, exterior, Vec3.atBottomCenterOf(candidate), carrier.getYRot())) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryBridge(ServerLevel level, MirrorBlockEntity source, Entity actor, boolean roomWorld) {
        BlockPos upper = source.getBlockPos();
        Direction behind = source.facing().getOpposite();
        for (int distance = 1; distance < 32; distance++) {
            MirrorBlockEntity other = frameAt(level, upper.relative(behind, distance));
            if (other != null && !other.isRoomExit() && other.facing() == behind) {
                return move(actor, level, front(other), behind.toYRot());
            }
        }
        if (roomWorld) {
            for (int distance = 1; distance < 10; distance++) {
                BlockPos spot = upper.relative(behind, distance);
                if (level.isEmptyBlock(spot) && level.isEmptyBlock(spot.below())) {
                    BlockPos roomExit = new BlockPos((spot.getX() & ~15) + 4, (spot.getY() & ~15) + 8, (spot.getZ() & ~15) + 8);
                    MirrorBlockEntity room = frameAt(level, roomExit);
                    if (room != null && room.isRoomExit() && (!(actor instanceof ServerPlayer player) || !roomExit.equals(entry(player)))) return false;
                    var altar = AltarPower.nearest(level, upper);
                    if (altar != null && altar.power() >= 3000
                            && move(actor, level, Vec3.atBottomCenterOf(spot), behind.toYRot())) {
                        altar.consumePower(3000);
                        return true;
                    }
                    return false;
                }
            }
        } else {
            for (int sign : new int[]{1, -1}) {
                for (int distance = 2; distance < 16; distance++) {
                    BlockPos candidate = upper.offset(0, sign * distance, 0);
                    if (level.getBlockState(candidate).hasProperty(MirrorBlock.HALF)
                            && level.getBlockState(candidate).getValue(MirrorBlock.HALF) == DoubleBlockHalf.LOWER) {
                        candidate = candidate.above();
                    }
                    MirrorBlockEntity other = frameAt(level, candidate);
                    if (other != null && !other.isRoomExit() && other.facing() == source.facing()) {
                        return move(actor, level, front(other), other.facing().toYRot());
                    }
                }
            }
        }
        return false;
    }

    public static void reflectionSlain(ServerLevel mirror, BlockPos exitPos) {
        MirrorBlockEntity exit = frameAt(mirror, exitPos);
        if (exit == null || !exit.isRoomExit()) return;
        exit.markDemonSlain();
        GlobalPos source = exit.counterpart();
        MirrorBlockEntity exterior = source == null ? null : linkedFrame(mirror.getServer().getLevel(source.dimension()), exit);
        if (exterior != null) exterior.markDemonSlain();
    }

    public static boolean queueEscapePhrase(ServerPlayer player, String message) {
        if (!player.level().dimension().equals(WitcheryDimensions.MIRROR)) return false;
        String phrase = message.toLowerCase(Locale.ROOT).replace("'", "").replace(",", "").strip();
        boolean entryPhrase = phrase.startsWith("mirror mirror send me home");
        if (!entryPhrase && !phrase.startsWith("mirror mirror i give up")) return false;
        // Forge decorates submitted chat asynchronously. Move the player on the server thread.
        player.server.execute(() -> escape(player, entryPhrase));
        return true;
    }

    private static void escape(ServerPlayer player, boolean entryPhrase) {
        ServerLevel here = player.serverLevel();
        if (!player.isAlive() || player.isRemoved() || !here.dimension().equals(WitcheryDimensions.MIRROR)) return;
        CompoundTag data = persist(player);
        String key = entryPhrase ? "MirrorEscape1" : "MirrorEscape2";
        long tick = here.getGameTime();
        long duration = entryPhrase ? 6_000 : 72_000;
        if (data.contains(key, Tag.TAG_ANY_NUMERIC) && data.getLong(key) + duration > tick) {
            player.displayClientMessage(Component.translatable("message.witchery.mirror.escape_cooldown",
                    (data.getLong(key) + duration - tick + 19) / 20), false);
            return;
        }
        boolean moved = false;
        if (entryPhrase) {
            BlockPos entrance = entry(player);
            if (entrance != null) {
                moved = move(player, here, Vec3.atBottomCenterOf(entrance).add(0, 0.01, 0), Direction.EAST.toYRot());
            }
        } else {
            ServerLevel home = player.server.getLevel(player.getRespawnDimension());
            BlockPos spawn = player.getRespawnPosition();
            Vec3 arrival = home != null && home != here && spawn != null
                    ? Player.findRespawnPositionAndUseSpawnBlock(home, spawn, player.getRespawnAngle(), player.isRespawnForced(), true).orElse(null)
                    : null;
            if (arrival == null) {
                home = player.server.overworld();
                arrival = nearbyFooting(home, home.getSharedSpawnPos());
                if (arrival == null) arrival = Vec3.atBottomCenterOf(home.getSharedSpawnPos());
            }
            moved = move(player, home, arrival, player.getRespawnAngle());
            if (moved) persist(player).remove(ENTRY);
        }
        if (moved) data.putLong(key, tick);
    }

    @Nullable
    private static MirrorBlockEntity linkedFrame(@Nullable ServerLevel level, MirrorBlockEntity source) {
        GlobalPos link = source.counterpart();
        if (level == null || link == null || !level.dimension().equals(link.dimension())) return null;
        MirrorBlockEntity candidate = frameAt(level, link.pos());
        return candidate != null && (candidate.connectionId() == null || source.connectionId() == null
                || Objects.equals(candidate.connectionId(), source.connectionId())) ? candidate : null;
    }

    @Nullable
    private static MirrorBlockEntity frameAt(ServerLevel level, BlockPos upper) {
        if (!level.isInWorldBounds(upper)) return null;
        BlockState state = level.getBlockState(upper);
        return state.getBlock() instanceof MirrorBlock && state.getValue(MirrorBlock.HALF) == DoubleBlockHalf.UPPER
                && level.getBlockState(upper.below()).is(state.getBlock())
                && level.getBlockEntity(upper) instanceof MirrorBlockEntity frame ? frame : null;
    }

    private static GlobalPos position(ServerLevel level, MirrorBlockEntity frame) {
        return GlobalPos.of(level.dimension(), frame.getBlockPos());
    }

    private static Vec3 front(MirrorBlockEntity frame) {
        Direction facing = frame.facing();
        return Vec3.atBottomCenterOf(frame.getBlockPos().below()).add(facing.getStepX() * 0.9, 0.02, facing.getStepZ() * 0.9);
    }

    private static boolean move(Entity actor, ServerLevel destination, Vec3 arrival, float yaw) {
        if (!destination.getWorldBorder().isWithinBounds(BlockPos.containing(arrival))) return false;
        Vec3 shift = arrival.subtract(actor.position());
        if (!destination.noCollision(actor, actor.getBoundingBox().move(shift))) return false;
        ServerLevel origin = (ServerLevel) actor.level();
        Vec3 location = actor.position();
        if (actor instanceof ServerPlayer player) {
            player.teleportTo(destination, arrival.x, arrival.y, arrival.z, yaw, player.getXRot());
            player.connection.resetPosition();
        } else if (origin == destination) {
            actor.teleportTo(arrival.x, arrival.y, arrival.z);
            actor.setYRot(yaw);
        } else {
            return false;
        }
        if (actor.level() != destination) return false;
        actor.getPersistentData().putLong(COOLDOWN, destination.getGameTime() + TRANSIT_COOLDOWN);
        actor.resetFallDistance();
        effects(origin, location);
        effects(destination, actor.position());
        return true;
    }

    private static void effects(ServerLevel level, Vec3 position) {
        level.sendParticles(ParticleTypes.SPLASH, position.x, position.y + 0.7, position.z, 16, 0.35, 0.5, 0.35, 0.04);
        level.playSound(null, position.x, position.y, position.z, SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 0.9F, 1);
    }

    @Nullable
    private static Vec3 nearbyFooting(ServerLevel level, BlockPos near) {
        for (BlockPos pos : BlockPos.withinManhattan(near, 2, 2, 2)) {
            if (hasFooting(level, pos)) {
                return Vec3.atBottomCenterOf(pos);
            }
        }
        return null;
    }

    private static boolean hasFooting(ServerLevel level, BlockPos pos) {
        BlockPos floor = pos.below();
        return level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())
                && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP);
    }

    private static CompoundTag persist(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static void rememberEntry(ServerPlayer player, BlockPos pos) {
        CompoundTag data = persist(player);
        CompoundTag entry = new CompoundTag();
        entry.putInt("posX", pos.getX());
        entry.putInt("posY", pos.getY());
        entry.putInt("posZ", pos.getZ());
        data.put(ENTRY, entry);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
    }

    @Nullable
    private static BlockPos entry(ServerPlayer player) {
        CompoundTag data = persist(player).getCompound(ENTRY);
        return data.contains("posY", Tag.TAG_ANY_NUMERIC)
                ? new BlockPos(data.getInt("posX"), data.getInt("posY"), data.getInt("posZ")) : null;
    }

    private static final class RoomCounter extends SavedData {
        private static final int ROOMS_PER_COLUMN = 15;
        private int next;

        static RoomCounter load(CompoundTag data) {
            RoomCounter counter = new RoomCounter();
            counter.next = Math.max(0, data.getInt("NextCell"));
            return counter;
        }

        BlockPos next(int top) {
            int tier = next % ROOMS_PER_COLUMN;
            if (tier * 16 + 8 >= top) {
                next += ROOMS_PER_COLUMN - tier;
                tier = 0;
            }
            int column = next++ / ROOMS_PER_COLUMN;
            setDirty();
            // Keep newer room allocation away from terrain generated near the old End-like spawn.
            int chunkX = 32 + column % 32;
            int chunkZ = column / 32;
            return new BlockPos(chunkX * 16 + 4, tier * 16 + 8, chunkZ * 16 + 8);
        }

        @Override
        public CompoundTag save(CompoundTag data) {
            data.putInt("NextCell", next);
            return data;
        }
    }
}
