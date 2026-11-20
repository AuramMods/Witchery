package art.arcane.witchery.familiar;

import art.arcane.witchery.entity.WitchCat;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class FamiliarBinding {
    private static final String FAMILIAR = "WITCFamiliar";
    private static final int CAT = 1;
    private static final int RANGE = 7;
    private static final String[] CAT_NAMES = {"Pyewackett", "Salem", "Gobbolino", "Sabbath", "Norris", "Crookshanks",
            "Binx", "Voodoo", "Raven", "Simpkin", "Fishbone", "Kismet"};

    private FamiliarBinding() {}

    public static boolean hasActiveFamiliar(ServerPlayer player) {
        CompoundTag record = record(player);
        return record.getBoolean("FamiliarSummoned") && record.getInt("FamiliarType") > 0;
    }

    public static String familiarName(ServerPlayer player) {
        return record(player).getString("FamiliarName");
    }

    public static boolean bindNear(ServerLevel level, BlockPos circle) {
        Set<UUID> boundOwners = new HashSet<>();
        Vec3 center = Vec3.atBottomCenterOf(circle);
        for (Cat pet : level.getEntitiesOfClass(Cat.class, new AABB(circle).inflate(RANGE, 0, RANGE),
                cat -> cat.isAlive() && cat.isTame() && cat.distanceToSqr(center) <= RANGE * RANGE)) {
            if (pet.getOwner() instanceof ServerPlayer owner && owner.isAlive() && !owner.isSpectator()
                    && owner.distanceToSqr(center) <= RANGE * RANGE && !boundOwners.contains(owner.getUUID())
                    && bind(owner, pet)) {
                boundOwners.add(owner.getUUID());
            }
        }
        return !boundOwners.isEmpty();
    }

    private static boolean bind(ServerPlayer owner, Cat pet) {
        WitchCat previous = find(owner);
        WitchCat familiar;
        if (pet instanceof WitchCat witchCat) {
            familiar = witchCat;
            familiar.setFamiliar(true);
        } else {
            familiar = WitcheryEntities.WITCH_CAT.get().create(owner.serverLevel());
            if (familiar == null) return false;
            UUID id = familiar.getUUID();
            familiar.restoreFrom(pet);
            familiar.setUUID(id);
            familiar.setBlackCoat();
            familiar.setFamiliar(true);
            if (!owner.serverLevel().addFreshEntity(familiar)) return false;
            pet.discard();
        }
        if (previous != null && previous != familiar) previous.setFamiliar(false);
        if (!familiar.hasCustomName()) familiar.setCustomName(Component.literal(CAT_NAMES[owner.getRandom().nextInt(CAT_NAMES.length)]));
        store(owner, familiar);
        owner.serverLevel().broadcastEntityEvent(familiar, (byte)7);
        return true;
    }

    public static boolean callNear(ServerLevel level, BlockPos circle) {
        Vec3 center = Vec3.atBottomCenterOf(circle);
        boolean called = false;
        for (ServerPlayer owner : level.getEntitiesOfClass(ServerPlayer.class, new AABB(circle).inflate(RANGE, 0, RANGE),
                player -> player.isAlive() && !player.isSpectator() && player.distanceToSqr(center) <= RANGE * RANGE)) {
            CompoundTag record = record(owner);
            if (record.getInt("FamiliarType") != CAT) continue;
            WitchCat existing = find(owner);
            if (existing != null) {
                called |= bringTo(owner, existing);
            } else if (!record.getBoolean("FamiliarSummoned")) {
                WitchCat familiar = WitcheryEntities.WITCH_CAT.get().create(level);
                if (familiar == null) continue;
                familiar.moveTo(center.x, center.y, center.z, 0, 0);
                if (!level.noCollision(familiar)) continue;
                familiar.tame(owner);
                familiar.setBlackCoat();
                if (!record.getString("FamiliarName").isEmpty()) {
                    familiar.setCustomName(Component.literal(record.getString("FamiliarName")));
                }
                familiar.setFamiliar(true);
                if (level.addFreshEntity(familiar)) {
                    store(owner, familiar);
                    effects(level, familiar.position());
                    called = true;
                }
            }
        }
        return called;
    }

    private static boolean bringTo(ServerPlayer owner, WitchCat familiar) {
        ServerLevel destination = owner.serverLevel();
        Vec3 arrival = owner.position();
        if (familiar.level() == destination) {
            familiar.teleportTo(arrival.x, arrival.y, arrival.z);
        } else if (familiar.changeDimension(destination, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerLevel origin, ServerLevel target, float yaw,
                                      Function<Boolean, Entity> repositionEntity) {
                Entity moved = repositionEntity.apply(false);
                if (moved != null) moved.moveTo(arrival.x, arrival.y, arrival.z, yaw, entity.getXRot());
                return moved;
            }
        }) == null) {
            return false;
        }
        effects(destination, arrival);
        return true;
    }

    public static void dismiss(ServerPlayer owner, WitchCat familiar) {
        CompoundTag record = record(owner);
        if (!familiar.getUUID().equals(familiarId(record))) return;
        record.putBoolean("FamiliarSummoned", false);
        record.putString("FamiliarName", familiar.hasCustomName() ? familiar.getName().getString() : "");
        effects((ServerLevel) familiar.level(), familiar.position());
        familiar.discard();
    }

    public static void shareDamage(ServerPlayer owner, LivingHurtEvent event) {
        if (!hasActiveFamiliar(owner)) return;
        WitchCat familiar = find(owner);
        boolean near = familiar != null && owner.level() == familiar.level() && owner.distanceToSqr(familiar) <= 576;
        float redirected = event.getAmount() * (near ? 0.1F : 0.01F);
        event.setAmount(event.getAmount() - redirected);
        if (familiar != null && redirected >= 1) familiar.hurt(event.getSource(), redirected);
    }

    public static void handleDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof WitchCat cat && cat.isFamiliar() && cat.level() instanceof ServerLevel level) {
            ServerPlayer owner = cat.getOwnerUUID() == null ? null : level.getServer().getPlayerList().getPlayer(cat.getOwnerUUID());
            if (owner == null) {
                cat.setHealth(1);
                event.setCanceled(true);
            } else if (cat.getUUID().equals(familiarId(record(owner)))) {
                dismiss(owner, cat);
                event.setCanceled(true);
                owner.hurt(owner.damageSources().fellOutOfWorld(), owner.getMaxHealth() * 2);
            }
        } else if (entity instanceof ServerPlayer player) {
            WitchCat familiar = find(player);
            if (familiar != null) dismiss(player, familiar);
        }
    }

    @Nullable
    private static WitchCat find(ServerPlayer owner) {
        CompoundTag record = record(owner);
        UUID id = record.getBoolean("FamiliarSummoned") ? familiarId(record) : null;
        if (id == null) return null;
        for (ServerLevel level : owner.server.getAllLevels()) {
            if (level.getEntity(id) instanceof WitchCat cat && cat.isAlive() && owner.getUUID().equals(cat.getOwnerUUID())) return cat;
        }
        return null;
    }

    @Nullable
    private static UUID familiarId(CompoundTag record) {
        return record.contains("UUIDMost", Tag.TAG_ANY_NUMERIC) && record.contains("UUIDLeast", Tag.TAG_ANY_NUMERIC)
                ? new UUID(record.getLong("UUIDMost"), record.getLong("UUIDLeast")) : null;
    }

    private static CompoundTag record(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(FAMILIAR);
    }

    private static void store(ServerPlayer owner, WitchCat cat) {
        CompoundTag record = new CompoundTag();
        record.putLong("UUIDMost", cat.getUUID().getMostSignificantBits());
        record.putLong("UUIDLeast", cat.getUUID().getLeastSignificantBits());
        record.putInt("FamiliarType", CAT);
        record.putByte("FamiliarColor", (byte)0);
        record.putString("FamiliarName", cat.hasCustomName() ? cat.getName().getString() : "");
        record.putBoolean("FamiliarSummoned", true);
        CompoundTag persistent = owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persistent.put(FAMILIAR, record);
        owner.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
    }

    private static void effects(ServerLevel level, Vec3 position) {
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, position.x, position.y + 0.5, position.z, 16, 0.4, 0.5, 0.4, 0.04);
        level.playSound(null, position.x, position.y, position.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.8F, 1.2F);
    }
}
