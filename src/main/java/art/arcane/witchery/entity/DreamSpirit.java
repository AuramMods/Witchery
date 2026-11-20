package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumSet;

public final class DreamSpirit extends PathfinderMob {
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(DreamSpirit.class, EntityDataSerializers.INT);
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
        moveControl = new FlyingMoveControl(this, 20, true);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 4).add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FLYING_SPEED, 0.4).add(Attributes.FOLLOW_RANGE, 16);
    }

    public static boolean canSpawn(EntityType<DreamSpirit> type, ServerLevelAccessor level, MobSpawnType reason,
                                   BlockPos pos, RandomSource random) {
        if (!level.getLevel().dimension().equals(WitcheryDimensions.DREAM) || pos.getY() < 60) return false;
        var ground = level.getBlockState(pos.below());
        return (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.SAND) || ground.is(Blocks.RED_SAND))
                && level.getRawBrightness(pos, 0) > 8 && random.nextInt(10) == 0;
    }

    public static boolean release(ServerLevel level, Vec3 position, Kind kind) {
        DreamSpirit spirit = WitcheryEntities.SPIRIT.get().create(level);
        if (spirit == null) return false;
        spirit.setPos(position);
        if (!level.noCollision(spirit)) return false;
        spirit.kind = kind;
        spirit.setPersistenceRequired();
        if (kind != Kind.SPIRIT) {
            spirit.lifeTicks = GUIDE_TICKS;
            spirit.destination = level.findNearestMapStructure(level.dimension().equals(Level.NETHER)
                    ? FORTRESSES : StructureTags.VILLAGE, spirit.blockPosition(), 100, false);
        }
        if (!level.addFreshEntity(spirit)) return false;
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, position.x, position.y, position.z, 16, 0.3, 0.3, 0.3, 0.04);
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COLOR, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new GuideGoal());
        goalSelector.addGoal(3, new TemptGoal(this, 0.6, Ingredient.of(WitcheryIngredient.FOCUSED_WILL.item()), true));
        goalSelector.addGoal(10, new WaterAvoidingRandomFlyingGoal(this, 0.8));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10, 0.2F));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        var flying = new FlyingPathNavigation(this, level);
        flying.setCanFloat(true);
        return flying;
    }

    public int color() {
        return entityData.get(COLOR);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel server && isAlive() && lifeTicks >= 0 && --lifeTicks <= 0) {
            server.sendParticles(ParticleTypes.POOF, getX(), getY(0.5), getZ(), 16, 0.28, 0.24, 0.28, 0.02);
            dropSpirit();
            discard();
        } else if (level().isClientSide && isAlive()) {
            int color = color() == 0 ? 0xFFCC00 : color();
            Vector3f tint = Vec3.fromRGB24(color).toVector3f();
            level().addParticle(new DustParticleOptions(tint, 0.6F), getRandomX(0.6), getRandomY() + 0.1,
                    getRandomZ(0.6), 0, 0, 0);
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
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
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
        entityData.set(COLOR, tag.getInt("FeatherColor") & 0xFFFFFF);
        destination = tag.contains("GuideDestination", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("GuideDestination")) : null;
        if (destination == null && kind != Kind.SPIRIT && tag.contains("WITCWaypoint", Tag.TAG_COMPOUND)
                && tag.contains("HomeX", Tag.TAG_ANY_NUMERIC)
                && tag.contains("HomeY", Tag.TAG_ANY_NUMERIC) && tag.contains("HomeZ", Tag.TAG_ANY_NUMERIC)) {
            destination = BlockPos.containing(tag.getDouble("HomeX"), tag.getDouble("HomeY"), tag.getDouble("HomeZ"));
        }
    }

    private final class GuideGoal extends Goal {
        private GuideGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
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
        public void tick() {
            if (destination == null) return;
            Vec3 facing = Vec3.atCenterOf(destination).subtract(position()).multiply(1, 0, 1);
            double distance = facing.horizontalDistance();
            if (distance < 0.5) return;
            Vec3 next = position().add(facing.scale(Math.min(4, distance) / distance));
            if (!level().isEmptyBlock(BlockPos.containing(next))) next = next.add(0, 1, 0);
            moveControl.setWantedPosition(next.x, next.y, next.z, 0.8);
        }
    }
}
