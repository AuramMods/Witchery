package art.arcane.witchery.entity;

import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class Illusion extends Monster {
    public enum Kind {
        CREEPER(null), SPIDER(SoundEvents.SPIDER_AMBIENT), ZOMBIE(SoundEvents.ZOMBIE_AMBIENT);

        @Nullable
        private final SoundEvent sound;

        Kind(@Nullable SoundEvent sound) {
            this.sound = sound;
        }
    }

    private static final EntityDataAccessor<String> VICTIM =
            SynchedEntityData.defineId(Illusion.class, EntityDataSerializers.STRING);
    private final Kind kind;
    private int illusionType;

    public Illusion(EntityType<? extends Illusion> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 0);
    }

    public static boolean spawnNear(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        var random = player.getRandom();
        var type = switch (random.nextInt(3)) {
            case 0 -> WitcheryEntities.ILLUSION_CREEPER;
            case 1 -> WitcheryEntities.ILLUSION_SPIDER;
            default -> WitcheryEntities.ILLUSION_ZOMBIE;
        };
        Illusion illusion = type.get().create(level);
        if (illusion == null) return false;
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 12; attempt++) {
            int x = random.nextInt(19) - 9;
            int z = random.nextInt(19) - 9;
            if (x * x + z * z < 16 || x * x + z * z > 81) continue;
            BlockPos.MutableBlockPos position = origin.offset(x, 7, z).mutable();
            if (!level.isLoaded(position) || !level.getWorldBorder().isWithinBounds(position)) continue;
            while (position.getY() > Math.max(origin.getY() - 8, level.getMinBuildHeight())
                    && !level.getBlockState(position.below()).blocksMotion()) {
                position.move(0, -1, 0);
            }
            if (!level.getBlockState(position.below()).blocksMotion()) continue;
            illusion.setPos(position.getX() + 0.5, position.getY() + 0.05, position.getZ() + 0.5);
            if (!level.noCollision(illusion) || level.containsAnyLiquid(illusion.getBoundingBox())) continue;
            illusion.entityData.set(VICTIM, player.getGameProfile().getName());
            illusion.setTarget(player);
            return level.addFreshEntity(illusion);
        }
        return false;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1, false));
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VICTIM, "");
    }

    public String victimName() { return entityData.get(VICTIM); }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel level) || !isAlive()) return;
        if (random.nextInt(15) == 0) {
            if (getHealth() <= 1.5F) {
                discard();
                return;
            }
            setHealth(getHealth() - 1);
        }

        var victim = level.getServer().getPlayerList().getPlayerByName(victimName());
        boolean active = victim != null && victim.isAlive() && victim.level() == level;
        LivingEntity target = active ? victim : null;
        if (getTarget() != target) setTarget(target);
        if (active && kind.sound != null && distanceToSqr(victim) < 64 && random.nextInt(40) == 0) {
            victim.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(kind.sound),
                    SoundSource.HOSTILE, getX(), getY(), getZ(), 1, 1, random.nextLong()));
        }
    }

    @Override public boolean isPushable() { return false; }
    @Override public boolean doHurtTarget(Entity entity) { return true; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return (source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD))
                && super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Victim", victimName());
        tag.putInt("IllusionType", illusionType);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(VICTIM, tag.getString("Victim"));
        illusionType = tag.getInt("IllusionType");
    }
}
