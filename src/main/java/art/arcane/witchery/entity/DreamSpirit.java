package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.WitcheryCreatureSpawns;
import art.arcane.witchery.world.WitcheryDimensions;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public final class DreamSpirit extends FlyingSpirit {
    private static final TagKey<Structure> FORTRESSES = TagKey.create(Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "spirit_fortresses"));
    private static final int GUIDE_TICKS = 200;

    public enum Kind {
        SPIRIT, VILLAGE, GUIDE
    }

    private Kind kind = Kind.SPIRIT;
    private int lifeTicks = -1;
    @Nullable
    private BlockPos destination;

    public DreamSpirit(EntityType<? extends DreamSpirit> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 4).add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FLYING_SPEED, 0.4).add(Attributes.FOLLOW_RANGE, 16);
    }

    public static boolean canSpawn(EntityType<DreamSpirit> type, ServerLevelAccessor level, MobSpawnType reason,
                                   BlockPos pos, RandomSource random) {
        if (!level.getLevel().dimension().equals(WitcheryDimensions.DREAM) || pos.getY() < 60) return false;
        if (!WitcheryCreatureSpawns.passesTameableLightCheck(level, pos)
                || !WitcheryCreatureSpawns.isClearForSpawn(level, type, pos) || random.nextInt(10) != 0) return false;
        var ground = level.getBlockState(pos.below());
        return (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.SAND) || ground.is(Blocks.RED_SAND))
                && level.getRawBrightness(pos, 0) > 8;
    }

    public static boolean release(ServerLevel level, BlockPos start, Kind kind, @Nullable Player player) {
        BlockPos ground = WitcheryTeleport.findEncounterGround(level, start);
        BlockPos landing = ground.above();
        if (level.isOutsideBuildHeight(landing.above()) || level.isEmptyBlock(ground)
                || !level.isEmptyBlock(landing) || !level.isEmptyBlock(landing.above())
                || player != null && !level.mayInteract(player, landing)) return false;
        Vec3 position = Vec3.atBottomCenterOf(landing).add(0, 0.05, 0);
        DreamSpirit spirit = WitcheryEntities.SPIRIT.get().create(level);
        if (spirit == null) return false;
        if (!WitcheryTeleport.canArrive(spirit, level, position)) return false;
        spirit.setPos(position);
        spirit.kind = kind;
        spirit.setPersistenceRequired();
        if (kind != Kind.SPIRIT) {
            spirit.lifeTicks = GUIDE_TICKS;
            spirit.destination = findGuideDestination(level, spirit.blockPosition());
        }
        if (!level.addFreshEntity(spirit)) return false;
        WitcheryNetwork.sendSpiritRelease(level, position, spirit.getBbHeight());
        return true;
    }

    @Nullable
    private static BlockPos findGuideDestination(ServerLevel level, BlockPos origin) {
        if (!level.getServer().getWorldData().worldGenOptions().generateStructures()) return null;
        var targetKind = level.dimension().equals(Level.NETHER) ? FORTRESSES : StructureTags.VILLAGE;
        var targets = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(targetKind).orElse(null);
        if (targets == null) return null;
        var source = level.getChunkSource();
        var nearest = source.getGenerator().findNearestMapStructure(level, targets, origin, 100, false);
        if (nearest == null) return null;
        var pos = nearest.getFirst();
        var chunk = new ChunkPos(pos);
        var start = source.getChunk(chunk.x, chunk.z, ChunkStatus.STRUCTURE_STARTS, true)
                .getStartForStructure(nearest.getSecond().value());
        // The locate result may be a chunk position at y=0. The original guide aimed at the first structure piece.
        return start != null && start.isValid() ? start.getPieces().get(0).getLocatorPosition() : pos.atY(origin.getY());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(3, new FlyingCompanionGoals.Tempt(this, stack -> stack.is(WitcheryIngredient.FOCUSED_WILL.item())));
        goalSelector.addGoal(8, new GuideGoal());
        goalSelector.addGoal(9, new FlyingCompanionGoals.Land(this));
        goalSelector.addGoal(10, new FlyingCompanionGoals.Wander(this));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10, 0.2F));
    }

    public boolean hasTimedLife() {
        return lifeTicks >= 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel server && isAlive() && hasTimedLife() && --lifeTicks <= 0) {
            WitcheryNetwork.sendSpiritDeparture(server, position());
            dropSpirit();
            discard();
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        dropSpirit();
    }

    private void dropSpirit() {
        if (kind != Kind.GUIDE) {
            spawnAtLocation((kind == Kind.VILLAGE ? WitcheryIngredient.SUBDUED_VILLAGE_SPIRIT
                    : WitcheryIngredient.SUBDUED_SPIRIT).item());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SuicideIn", lifeTicks);
        tag.putInt("SpiritType", kind.ordinal());
        tag.putInt("FeatherColor", color());
        if (destination != null) tag.putLong("GuideDestination", destination.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lifeTicks = tag.contains("SuicideIn", Tag.TAG_ANY_NUMERIC) ? Math.max(-1, tag.getInt("SuicideIn")) : -1;
        kind = switch (tag.getInt("SpiritType")) {
            case 1 -> Kind.VILLAGE;
            case 2 -> Kind.GUIDE;
            default -> Kind.SPIRIT;
        };
        if (kind != Kind.SPIRIT && tag.getBoolean("Sitting")) setNoAi(true);
        setColor(tag.getInt("FeatherColor") & 0xFFFFFF);
        destination = tag.contains("GuideDestination", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("GuideDestination")) : null;
        if (destination == null && kind != Kind.SPIRIT && tag.contains("WITCWaypoint", Tag.TAG_COMPOUND)
                && tag.contains("HomeX", Tag.TAG_ANY_NUMERIC)
                && tag.contains("HomeY", Tag.TAG_ANY_NUMERIC) && tag.contains("HomeZ", Tag.TAG_ANY_NUMERIC)) {
            destination = BlockPos.containing(tag.getDouble("HomeX"), tag.getDouble("HomeY"), tag.getDouble("HomeZ"));
        }
    }

    private final class GuideGoal extends Goal {
        private int courseDelay;

        private GuideGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return destination != null && lifeTicks > 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            // An interrupted guide clears the waypoint and sits through the rest of its timer.
            if (lifeTicks > 0) setNoAi(true);
            destination = null;
            courseDelay = 0;
        }

        @Override
        public void tick() {
            if (destination == null) return;
            if (--courseDelay <= 0) {
                Vec3 course = Vec3.atLowerCornerOf(destination).subtract(position());
                if (!FlyingCompanionGoals.clearCourse(DreamSpirit.this, course)) {
                    double detourX = (level().random.nextDouble() * 4 - 2) * 6;
                    double detourY = (level().random.nextDouble() * 2 - 1) * 4;
                    double detourZ = (level().random.nextDouble() * 4 - 2) * 6;
                    if (course.lengthSqr() <= 1 && getY() > destination.getY() && getY() + detourY > 0) {
                        detourY = -2 * getY() - detourY;
                    }
                    course = level().random.nextInt(2) == 0
                            ? new Vec3(course.x, detourY, course.z) : new Vec3(detourX, detourY, detourZ);
                }
                double ceiling = Math.min(destination.getY() + 32, level().getMaxBuildHeight() - 1);
                FlyingCompanionGoals.steer(DreamSpirit.this, course, 0.2, getY() < ceiling ? 0.1 : 0);
                courseDelay = 10;
            }
            FlyingCompanionGoals.faceVelocity(DreamSpirit.this);
        }
    }
}
