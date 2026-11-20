package art.arcane.witchery.familiar;

import art.arcane.witchery.entity.WitchCat;
import art.arcane.witchery.entity.WitchOwl;
import art.arcane.witchery.entity.WitchPet;
import art.arcane.witchery.entity.WitchToad;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FamiliarBinding {
    private static final String FAMILIAR = "WITCFamiliar";
    private static final int RANGE = 7;

    private enum Type {
        CAT(1, () -> WitcheryEntities.WITCH_CAT.get(), List.of("Pyewackett", "Salem", "Gobbolino", "Sabbath", "Norris",
                "Crookshanks", "Binx", "Voodoo", "Raven", "Simpkin", "Fishbone", "Kismet")),
        TOAD(2, () -> WitcheryEntities.TOAD.get(), List.of("Casper", "Wart", "Langston", "Croaker", "Prince Charming",
                "Frog-n-stien", "Randolph", "Evileye", "Churchill", "Santa", "Dillinger", "Spuds")),
        OWL(3, () -> WitcheryEntities.OWL.get(), List.of("Archimedes", "Dumbledornithologist", "Al Travis", "Baltimore",
                "Cornelius", "Hadwig", "Hoot", "Merlin", "Owl Capone", "Pigwidgeon", "Athena", "Albertine"));

        private final int id;
        private final Supplier<? extends EntityType<? extends TamableAnimal>> entity;
        private final List<String> names;

        Type(int id, Supplier<? extends EntityType<? extends TamableAnimal>> entity, List<String> names) {
            this.id = id;
            this.entity = entity;
            this.names = names;
        }

        @Nullable
        private static Type forId(int id) {
            return switch (id) {
                case 1 -> CAT;
                case 2 -> TOAD;
                case 3 -> OWL;
                default -> null;
            };
        }

        @Nullable
        private static Type forAnimal(TamableAnimal animal) {
            if (animal instanceof Cat) return CAT;
            if (animal instanceof WitchToad) return TOAD;
            if (animal instanceof WitchOwl) return OWL;
            return null;
        }
    }

    private FamiliarBinding() {}

    public static boolean hasActiveFamiliar(ServerPlayer player) {
        CompoundTag record = record(player);
        return record.getBoolean("FamiliarSummoned") && record.getInt("FamiliarType") > 0;
    }

    public static boolean hasCurseMastery(ServerPlayer player) {
        return hasMastery(player, Type.CAT);
    }

    public static boolean hasBrewMastery(ServerPlayer player) {
        return hasMastery(player, Type.TOAD);
    }

    private static boolean hasMastery(ServerPlayer player, Type type) {
        CompoundTag record = record(player);
        return record.getBoolean("FamiliarSummoned") && record.getInt("FamiliarType") == type.id;
    }

    public static String familiarName(ServerPlayer player) {
        return record(player).getString("FamiliarName");
    }

    public static boolean bindNear(ServerLevel level, BlockPos circle) {
        Set<UUID> boundOwners = new HashSet<>();
        Vec3 center = Vec3.atBottomCenterOf(circle);
        for (TamableAnimal pet : level.getEntitiesOfClass(TamableAnimal.class, new AABB(circle).inflate(RANGE, 0, RANGE),
                animal -> animal.isAlive() && animal.isTame() && animal.distanceToSqr(center) <= RANGE * RANGE
                        && !(animal instanceof WitchPet witchPet && witchPet.isTemporary()))) {
            Type type = Type.forAnimal(pet);
            if (type != null && pet.getOwner() instanceof ServerPlayer owner && owner.isAlive() && !owner.isSpectator()
                    && owner.distanceToSqr(center) <= RANGE * RANGE && !boundOwners.contains(owner.getUUID())
                    && bind(owner, pet, type)) {
                boundOwners.add(owner.getUUID());
            }
        }
        return !boundOwners.isEmpty();
    }

    private static boolean bind(ServerPlayer owner, TamableAnimal pet, Type type) {
        TamableAnimal previous = find(owner);
        TamableAnimal familiar = pet;
        if (pet instanceof Cat && !(pet instanceof WitchCat)) {
            WitchCat cat = WitcheryEntities.WITCH_CAT.get().create(owner.serverLevel());
            if (cat == null) return false;
            UUID id = cat.getUUID();
            cat.restoreFrom(pet);
            cat.setUUID(id);
            cat.setBlackCoat();
            if (!owner.serverLevel().addFreshEntity(cat)) return false;
            familiar = cat;
            pet.discard();
        }
        if (!(familiar instanceof Familiar marked)) return false;
        marked.setFamiliar(true);
        if (previous != familiar && previous instanceof Familiar old) old.setFamiliar(false);
        if (!familiar.hasCustomName()) familiar.setCustomName(Component.literal(type.names.get(owner.getRandom().nextInt(type.names.size()))));
        store(owner, familiar, type);
        owner.serverLevel().broadcastEntityEvent(familiar, (byte)7);
        return true;
    }

    public static boolean callNear(ServerLevel level, BlockPos circle) {
        Vec3 center = Vec3.atBottomCenterOf(circle);
        boolean called = false;
        for (ServerPlayer owner : level.getEntitiesOfClass(ServerPlayer.class, new AABB(circle).inflate(RANGE, 0, RANGE),
                player -> player.isAlive() && !player.isSpectator() && player.distanceToSqr(center) <= RANGE * RANGE)) {
            CompoundTag record = record(owner);
            Type type = Type.forId(record.getInt("FamiliarType"));
            if (type == null) continue;
            TamableAnimal existing = find(owner);
            if (existing != null) {
                called |= bringTo(owner, existing);
            } else if (!record.getBoolean("FamiliarSummoned")) {
                TamableAnimal familiar = type.entity.get().create(level);
                if (!(familiar instanceof Familiar marked)) continue;
                familiar.moveTo(center.x, center.y, center.z, 0, 0);
                if (!level.noCollision(familiar)) continue;
                familiar.tame(owner);
                if (familiar instanceof WitchCat cat) cat.setBlackCoat();
                if (familiar instanceof WitchPet pet) pet.setColor(record.getByte("FamiliarColor"));
                if (!record.getString("FamiliarName").isEmpty()) {
                    familiar.setCustomName(Component.literal(record.getString("FamiliarName")));
                }
                marked.setFamiliar(true);
                if (level.addFreshEntity(familiar)) {
                    store(owner, familiar, type);
                    effects(level, familiar.position());
                    called = true;
                }
            }
        }
        return called;
    }

    private static boolean bringTo(ServerPlayer owner, TamableAnimal familiar) {
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

    public static void dismiss(ServerPlayer owner, TamableAnimal familiar) {
        CompoundTag record = record(owner);
        if (!(familiar.level() instanceof ServerLevel level) || !familiar.getUUID().equals(familiarId(record))) return;
        record.putBoolean("FamiliarSummoned", false);
        record.putString("FamiliarName", familiar.hasCustomName() ? familiar.getName().getString() : "");
        if (familiar instanceof WitchPet pet) record.putByte("FamiliarColor", (byte) pet.color());
        if (familiar instanceof WitchOwl owl && !owl.getMainHandItem().isEmpty()) {
            owl.spawnAtLocation(owl.getMainHandItem());
            owl.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        effects(level, familiar.position());
        familiar.discard();
    }

    public static void shareDamage(ServerPlayer owner, LivingHurtEvent event) {
        if (!hasActiveFamiliar(owner)) return;
        TamableAnimal familiar = find(owner);
        boolean near = familiar != null && owner.level() == familiar.level() && owner.distanceToSqr(familiar) <= 576;
        float redirected = event.getAmount() * (near ? 0.1F : 0.01F);
        event.setAmount(event.getAmount() - redirected);
        if (familiar != null && redirected >= 1) familiar.hurt(event.getSource(), redirected);
    }

    public static void handleDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof TamableAnimal familiar && familiar instanceof Familiar marked && marked.isFamiliar()
                && familiar.level() instanceof ServerLevel level) {
            UUID ownerId = familiar.getOwnerUUID();
            ServerPlayer owner = ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
            if (owner == null) {
                familiar.setHealth(1);
                event.setCanceled(true);
            } else if (familiar.getUUID().equals(familiarId(record(owner)))) {
                dismiss(owner, familiar);
                event.setCanceled(true);
                owner.hurt(owner.damageSources().fellOutOfWorld(), owner.getMaxHealth() * 2);
            }
        } else if (entity instanceof ServerPlayer player) {
            TamableAnimal familiar = find(player);
            if (familiar != null) dismiss(player, familiar);
        }
    }

    @Nullable
    private static TamableAnimal find(ServerPlayer owner) {
        CompoundTag record = record(owner);
        UUID id = record.getBoolean("FamiliarSummoned") ? familiarId(record) : null;
        if (id == null) return null;
        for (ServerLevel level : owner.server.getAllLevels()) {
            if (level.getEntity(id) instanceof TamableAnimal animal && animal instanceof Familiar
                    && animal.isAlive() && owner.getUUID().equals(animal.getOwnerUUID())) return animal;
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

    private static void store(ServerPlayer owner, TamableAnimal familiar, Type type) {
        CompoundTag record = new CompoundTag();
        record.putLong("UUIDMost", familiar.getUUID().getMostSignificantBits());
        record.putLong("UUIDLeast", familiar.getUUID().getLeastSignificantBits());
        record.putInt("FamiliarType", type.id);
        record.putByte("FamiliarColor", familiar instanceof WitchPet pet ? (byte) pet.color() : 0);
        record.putString("FamiliarName", familiar.hasCustomName() ? familiar.getName().getString() : "");
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
