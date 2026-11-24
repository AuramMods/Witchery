package art.arcane.witchery.ritual;

import art.arcane.witchery.block.BarrierBlock;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.entity.CovenWitch;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ProtectionRites {
    private static final int BARRIER_PULSE = 20;
    private static final int BARRIER_LIFETIME = 30;
    private static final int PORTABLE_EXPIRY_TICK = 59 * BARRIER_PULSE;
    private static final TicketType<BlockPos> BARRIER_TICKET =
            TicketType.create("witchery_ritual_barrier", Comparator.comparingLong(BlockPos::asLong), 60);

    private ProtectionRites() {}

    public enum Kind {
        PROTECTION(8, 4, 0, 0.8F, false),
        IMPRISONMENT(9, 4, 0, 0.8F, false),
        BARRIER(10, 4, 5, 1.2F, false),
        GREATER_BARRIER(11, 6, 6, 1.4F, true),
        PORTABLE_BARRIER(12, 6, 4, 0, true);

        private final int legacyId;
        private final int radius;
        private final float power;
        private final boolean blocksPlayers;
        private final List<BlockPos> shell;

        Kind(int legacyId, int radius, int height, float power, boolean blocksPlayers) {
            this.legacyId = legacyId;
            this.radius = radius;
            this.power = power;
            this.blocksPlayers = blocksPlayers;
            shell = height == 0 ? List.of() : cylinder(radius, height);
        }

        public boolean acceptsWaystone() {
            return this == BARRIER || this == GREATER_BARRIER;
        }

        public int powerPerSecond() {
            return Math.round(power * 20);
        }

        @Nullable
        public static Kind fromLegacy(int id) {
            for (Kind kind : values()) {
                if (kind.legacyId == id) return kind;
            }
            return null;
        }
    }

    public static final class Upkeep {
        private final Kind kind;
        @Nullable private final BlockPos boundTarget;
        private final int coven;
        private int ticks;
        private int resumeTicks;
        @Nullable private AltarBlockEntity powerSource;

        public Upkeep(Kind kind, @Nullable BlockPos boundTarget, int coven) {
            this(kind, boundTarget, coven, 0, 0);
        }

        private Upkeep(Kind kind, @Nullable BlockPos boundTarget, int coven, int ticks, int resumeTicks) {
            this.kind = kind;
            this.boundTarget = boundTarget == null ? null : boundTarget.immutable();
            this.coven = coven;
            this.ticks = ticks;
            this.resumeTicks = resumeTicks;
        }

        public boolean tick(ServerLevel level, BlockPos heart) {
            BlockPos target = boundTarget == null ? heart : boundTarget;
            boolean portable = kind == Kind.PORTABLE_BARRIER;
            // Earlier Forge wards did not retain the coven that determined their Waystone range.
            if (boundTarget != null && coven >= 0 && !LandscapeRites.canReach(level, heart, target, coven)) {
                level.playSound(null, heart, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 0.9F, 1);
                return false;
            }
            if (!level.isInWorldBounds(target) || !level.getWorldBorder().isWithinBounds(target)) return false;
            if (resumeTicks > 0) {
                if (--resumeTicks > 0) return true;
                level.playSound(null, heart, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1);
            }
            if (portable && ticks >= PORTABLE_EXPIRY_TICK) return false;
            if (kind.power > 0 && !consumePower(level, heart)) {
                level.playSound(null, heart, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 0.9F, 1);
                return false;
            }

            if (kind.shell.isEmpty()) moveCreatures(level, target, kind.radius, kind == Kind.IMPRISONMENT);
            else if (ticks % BARRIER_PULSE == 0) refreshBarrier(level, target);

            ticks = portable ? ticks + 1 : (ticks + 1) % BARRIER_PULSE;
            return true;
        }

        private boolean consumePower(ServerLevel level, BlockPos heart) {
            if (powerSource == null || powerSource.isRemoved() || !powerSource.isInRange(heart)
                    || level.random.nextInt(5) == 0 || !powerSource.isCore(level)) {
                powerSource = AltarPower.nearest(level, heart);
            }
            return powerSource != null && powerSource.consumePower(kind.power);
        }

        private void refreshBarrier(ServerLevel level, BlockPos target) {
            ChunkPos min = new ChunkPos(target.offset(-kind.radius, 0, -kind.radius));
            ChunkPos max = new ChunkPos(target.offset(kind.radius, 0, kind.radius));
            for (int x = min.x; x <= max.x; x++) {
                for (int z = min.z; z <= max.z; z++) {
                    level.getChunkSource().addRegionTicket(BARRIER_TICKET, new ChunkPos(x, z), 2, target);
                }
            }
            var barrier = WitcheryBlocks.getRequired("barrier").get();
            for (BlockPos offset : kind.shell) {
                BlockPos pos = target.offset(offset);
                if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)) continue;
                var state = level.getBlockState(pos);
                if (state.is(barrier) || state.canBeReplaced()) {
                    BarrierBlock.place(level, pos, null, kind.blocksPlayers, BARRIER_LIFETIME);
                }
            }
        }

        public CompoundTag save(BlockPos heart) {
            var tag = new CompoundTag();
            tag.putString("Kind", kind.name());
            tag.putLong("Target", (boundTarget == null ? heart : boundTarget).asLong());
            if (boundTarget == null) tag.putBoolean("FollowsHeart", true);
            if (coven >= 0) tag.putInt("CovenSize", coven);
            if (resumeTicks > 0) tag.putInt("ResumeTicks", resumeTicks);
            tag.putInt("Ticks", ticks);
            return tag;
        }

        @Nullable
        public static Upkeep load(CompoundTag tag) {
            if (!tag.contains("Target", Tag.TAG_LONG)) return null;
            Kind kind;
            try {
                kind = Kind.valueOf(tag.getString("Kind"));
            } catch (IllegalArgumentException unknown) {
                return null;
            }
            int ticks = tag.getInt("Ticks");
            int max = kind == Kind.PORTABLE_BARRIER ? PORTABLE_EXPIRY_TICK : BARRIER_PULSE - 1;
            int coven = tag.contains("CovenSize", Tag.TAG_ANY_NUMERIC) ? Mth.clamp(tag.getInt("CovenSize"), 0, CovenWitch.MAX_MEMBERS) : -1;
            BlockPos boundTarget = tag.getBoolean("FollowsHeart") ? null : BlockPos.of(tag.getLong("Target"));
            int resumeTicks = Mth.clamp(tag.getInt("ResumeTicks"), 0, BARRIER_PULSE);
            return ticks >= 0 && ticks <= max ? new Upkeep(kind, boundTarget, coven, ticks, resumeTicks) : null;
        }

        @Nullable
        public static Upkeep fromLegacy(Kind kind, @Nullable BlockPos boundTarget, int stage, int coven) {
            if (stage < 0 || kind == Kind.PORTABLE_BARRIER && stage >= 60) return null;
            return new Upkeep(kind, boundTarget, coven, kind == Kind.PORTABLE_BARRIER ? stage * BARRIER_PULSE : 0, BARRIER_PULSE);
        }
    }

    private static void moveCreatures(ServerLevel level, BlockPos pos, int radius, boolean attract) {
        Vec3 center = Vec3.atLowerCornerOf(pos);
        for (PathfinderMob mob : level.getEntitiesOfClass(PathfinderMob.class, new AABB(center, center).inflate(radius),
                creature -> creature.isAlive() && creature.distanceToSqr(center) < radius * radius)) {
            Vec3 offset = mob.position().subtract(center);
            if (attract) {
                if (offset.add(mob.getDeltaMovement()).lengthSqr() >= (radius - 1) * (radius - 1)) {
                    // The original rotation uses 180 radians.
                    Vec3 inward = repulsion(offset).yRot(180.0F);
                    mob.setDeltaMovement(inward.x, 0, inward.z);
                    mob.hasImpulse = true;
                }
            } else {
                Vec3 outward = repulsion(offset);
                mob.push(outward.x, outward.y, outward.z);
            }
        }
    }

    public static Vec3 repulsion(Vec3 offset) {
        double distanceSquared = offset.lengthSqr();
        if (distanceSquared < 1.0E-6) return Vec3.ZERO;
        double vertical = offset.y * 4.32 / (distanceSquared * distanceSquared);
        return new Vec3(Math.signum(offset.x) * 0.22, vertical > 0.2 || vertical < -0.1 ? 0.12 : vertical,
                Math.signum(offset.z) * 0.22);
    }

    private static List<BlockPos> cylinder(int radius, int height) {
        Set<BlockPos> positions = new LinkedHashSet<>();
        int x = radius;
        int z = 0;
        int error = 1 - radius;
        while (x >= z) {
            for (int xSign = -1; xSign <= 1; xSign += 2) {
                for (int zSign = -1; zSign <= 1; zSign += 2) {
                    for (int y = 0; y < height; y++) {
                        positions.add(new BlockPos(xSign * x, y, zSign * z));
                        positions.add(new BlockPos(xSign * z, y, zSign * x));
                    }
                }
            }
            for (int y : new int[]{-1, height}) {
                for (int column = -x; column <= x; column++) {
                    positions.add(new BlockPos(column, y, z));
                    positions.add(new BlockPos(column, y, -z));
                }
                for (int column = -z; column <= z; column++) {
                    positions.add(new BlockPos(column, y, x));
                    positions.add(new BlockPos(column, y, -x));
                }
            }
            z++;
            if (error < 0) error += 2 * z + 1;
            else {
                x--;
                error += 2 * (z - x + 1);
            }
        }
        return List.copyOf(positions);
    }
}
