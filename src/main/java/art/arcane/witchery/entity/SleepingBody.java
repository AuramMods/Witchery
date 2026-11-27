package art.arcane.witchery.entity;

import art.arcane.witchery.world.DreamTravel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class SleepingBody extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(SleepingBody.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> OWNER_NAME =
            SynchedEntityData.defineId(SleepingBody.class, EntityDataSerializers.STRING);

    public SleepingBody(EntityType<? extends SleepingBody> type, Level level) {
        super(type, level);
        setNoAi(true);
        setNoGravity(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER_UUID, Optional.empty());
        entityData.define(OWNER_NAME, "");
    }

    public void attachOwner(ServerPlayer owner) {
        entityData.set(OWNER_NAME, owner.getGameProfile().getName());
        entityData.set(OWNER_UUID, Optional.of(owner.getUUID()));
        setCustomName(owner.getName());
    }

    public String ownerName() {
        return entityData.get(OWNER_NAME);
    }

    public Optional<UUID> ownerId() {
        return entityData.get(OWNER_UUID);
    }

    public boolean belongsTo(ServerPlayer player) {
        return ownerId().map(id -> id.equals(player.getUUID()))
                .orElseGet(() -> ownerName().equalsIgnoreCase(player.getGameProfile().getName()));
    }

    @Nullable
    private ServerPlayer owner() {
        if (!(level() instanceof ServerLevel server)) {
            return null;
        }
        var players = server.getServer().getPlayerList();
        return ownerId().map(players::getPlayer).orElseGet(() -> players.getPlayerByName(ownerName()));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        // The sleeping body is anchored where the dreamer fell asleep.
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        return (level().isClientSide || owner() != null || source.getEntity() instanceof Player player && player.getAbilities().instabuild)
                && super.hurt(source, damage);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        ServerPlayer player = owner();
        if (player != null && DreamTravel.isSpiritWalking(player) && isDeadOrDying()) {
            DreamTravel.requestWake(player);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount % 20 == 0) {
            ServerPlayer player = owner();
            if (player != null && !DreamTravel.isSpiritWalking(player)) {
                discard();
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Owner", ownerName());
        ownerId().ifPresent(id -> tag.putUUID("OwnerUuid", id));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(OWNER_NAME, tag.getString("Owner"));
        entityData.set(OWNER_UUID, tag.hasUUID("OwnerUuid") ? Optional.of(tag.getUUID("OwnerUuid")) : Optional.empty());
        setNoAi(true);
        setNoGravity(true);
        setPersistenceRequired();
    }
}
