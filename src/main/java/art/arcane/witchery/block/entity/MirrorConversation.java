package art.arcane.witchery.block.entity;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.entity.MirrorFace;
import art.arcane.witchery.entity.WitcheryFollower;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

final class MirrorConversation {
    private static final int FAIREST_COOLDOWN = 2_400;
    private static final String MESSAGE = "message.witchery.mirror.";
    private final Set<String> playersSeen = new HashSet<>();
    @Nullable private GameProfile favorite;
    @Nullable private UUID fairest;
    private long lastFairestSpawn;

    boolean rememberVisitors(ServerLevel level, BlockPos pos, Direction front) {
        AABB viewingArea = new AABB(pos).inflate(front.getAxis() == Direction.Axis.Z ? 1 : 0, 0,
                front.getAxis() == Direction.Axis.X ? 1 : 0).expandTowards(front.getStepX() * 4, 0, front.getStepZ() * 4);
        boolean changed = false;
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, viewingArea,
                candidate -> candidate.isAlive() && !candidate.isSpectator())) {
            changed |= playersSeen.add(player.getGameProfile().getName());
        }
        return changed;
    }

    boolean ask(ServerLevel level, BlockPos pos, Direction front, ServerPlayer player) {
        if (!showFace(level, pos, front)) return false;
        Vec3 center = Vec3.atCenterOf(pos);
        Component prompt = Component.translatable(MESSAGE + "prompt", player.getGameProfile().getName());
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(center) <= 64 * 64) viewer.sendSystemMessage(prompt);
        }

        WitcheryFollower subject = nearbyFairest(level, pos);
        if (subject != null) {
            tellFairest(player, center, subject);
        } else if (favorite != null && !isFavorite(player)) {
            fairest = null;
            tell(player, "another_player");
            ServerPlayer chosen = favorite.getId() == null ? level.getServer().getPlayerList().getPlayerByName(favorite.getName())
                    : level.getServer().getPlayerList().getPlayer(favorite.getId());
            if (chosen != null && chosen.level() == level) tellBearing(player, center, chosen);
        } else {
            favorite = new GameProfile(player.getUUID(), player.getGameProfile().getName());
            fairest = null;
            if (level.getGameTime() - lastFairestSpawn > FAIREST_COOLDOWN
                    && level.random.nextDouble() < WitcheryServerConfig.FAIREST_SPAWN_CHANCE.get()) {
                subject = spawnFairest(level, pos);
            }
            if (subject == null) {
                tell(player, "you");
            } else {
                fairest = subject.getUUID();
                lastFairestSpawn = level.getGameTime();
                tellFairest(player, center, subject);
            }
        }

        String seen = playersSeen.stream().filter(name -> !name.equals(player.getGameProfile().getName()))
                .sorted().collect(Collectors.joining(", "));
        if (seen.isEmpty()) tell(player, "players_not_seen");
        else tell(player, "players_seen", seen);
        if (isFavorite(player)) playersSeen.clear();
        return true;
    }

    private boolean isFavorite(ServerPlayer player) {
        return favorite != null && (favorite.getId() != null ? favorite.getId().equals(player.getUUID())
                : favorite.getName().equalsIgnoreCase(player.getGameProfile().getName()));
    }

    private static boolean showFace(ServerLevel level, BlockPos pos, Direction front) {
        if (!level.getEntitiesOfClass(MirrorFace.class, new AABB(pos)).isEmpty()) return false;
        MirrorFace face = WitcheryEntities.MIRROR_FACE.get().create(level);
        if (face == null) return false;
        double x = pos.getX() + 0.5 - front.getStepX() * 0.4;
        double y = pos.getY() + 0.1;
        double z = pos.getZ() + 0.5 - front.getStepZ() * 0.4;
        float yaw = front.toYRot();
        face.moveTo(x, y, z, yaw, 0);
        face.setYBodyRot(yaw);
        face.setYHeadRot(yaw);
        if (!level.addFreshEntity(face)) return false;
        level.playSound(null, pos, WitcherySounds.MIRROR_SPEECH.get(), SoundSource.NEUTRAL, 1, 1);
        for (int i = 0; i < 16; i++) {
            level.sendParticles(ParticleTypes.ENTITY_EFFECT, x + (level.random.nextDouble() - 0.5) * 0.5,
                    y + level.random.nextDouble() * 0.5, z + (level.random.nextDouble() - 0.5) * 0.5,
                    0, 119 / 255.0, 119 / 255.0, 1, 1);
        }
        return true;
    }

    @Nullable
    private WitcheryFollower nearbyFairest(ServerLevel level, BlockPos pos) {
        return fairest != null && level.getEntity(fairest) instanceof WitcheryFollower follower
                && follower.isAlive() && follower.getBoundingBox().intersects(new AABB(pos).inflate(100, 32, 100))
                ? follower : null;
    }

    @Nullable
    private static WitcheryFollower spawnFairest(ServerLevel level, BlockPos pos) {
        int top = Math.min(pos.getY() + 20, level.getMaxBuildHeight() - 3);
        int bottom = Math.max(pos.getY() - 20, level.getMinBuildHeight());
        for (int attempt = 0; attempt < 25; attempt++) {
            int x = pos.getX() + (level.random.nextBoolean() ? 1 : -1) * (50 + level.random.nextInt(50));
            int z = pos.getZ() + (level.random.nextBoolean() ? 1 : -1) * (50 + level.random.nextInt(50));
            if (!level.getWorldBorder().isWithinBounds(new BlockPos(x, top, z))) continue;
            BlockPos.MutableBlockPos ground = new BlockPos.MutableBlockPos();
            for (int y = top; y >= bottom; y--) {
                ground.set(x, y, z);
                if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) continue;
                BlockPos feet = ground.above();
                if (!level.isEmptyBlock(feet) || !level.isEmptyBlock(feet.above())) continue;
                WitcheryFollower follower = WitcheryFollower.spawnFairest(level, feet);
                if (follower != null) return follower;
            }
        }
        return null;
    }

    private static void tellFairest(ServerPlayer player, Vec3 center, WitcheryFollower subject) {
        tell(player, subject.followerType() == 4 ? "another_male" : "another_female");
        tellBearing(player, center, subject);
    }

    private static void tellBearing(ServerPlayer player, Vec3 center, LivingEntity subject) {
        double angle = Math.toDegrees(Math.atan2(subject.getX() - center.x, center.z - subject.getZ()));
        int bearing = (int) (((angle + 360) % 360) / 45);
        tell(player, "bearing." + bearing);
    }

    private static void tell(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(Component.translatable(MESSAGE + key, args).withStyle(ChatFormatting.AQUA));
    }

    void load(CompoundTag root) {
        CompoundTag profile = root.getCompound("Favorite");
        String name = profile.getString("Name");
        UUID id = profile.hasUUID("Id") ? profile.getUUID("Id") : parseUuid(profile.getString("Id"));
        favorite = id == null && name.isEmpty() ? null : new GameProfile(id, name);
        fairest = parseUuid(root.getString("Fairest"));
        lastFairestSpawn = root.getLong("LastFairestSpawnTime");
        ListTag visitors = root.getList("PlayersSeen", Tag.TAG_STRING);
        playersSeen.clear();
        for (int i = 0; i < visitors.size(); i++) playersSeen.add(visitors.getString(i));
    }

    @Nullable
    private static UUID parseUuid(String value) {
        if (value.isEmpty()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    void save(CompoundTag root) {
        if (favorite != null) root.put("Favorite", NbtUtils.writeGameProfile(new CompoundTag(), favorite));
        if (fairest != null) root.putString("Fairest", fairest.toString());
        if (lastFairestSpawn != 0) root.putLong("LastFairestSpawnTime", lastFairestSpawn);
        if (!playersSeen.isEmpty()) {
            ListTag visitors = new ListTag();
            playersSeen.stream().sorted().map(StringTag::valueOf).forEach(visitors::add);
            root.put("PlayersSeen", visitors);
        }
    }
}
