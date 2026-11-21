package art.arcane.witchery.block.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.DreamSpirit;
import art.arcane.witchery.entity.SleepingBody;
import art.arcane.witchery.familiar.Familiar;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class FetishAlarm {
    enum Mode {
        UNKNOWN_PLAYERS, LISTED_PLAYERS, UNKNOWN_CREATURES, MISSING_ANY, MISSING_ALL, OFF;

        static Mode load(CompoundTag tag) {
            int id = tag.contains("AlarmMode", Tag.TAG_ANY_NUMERIC) ? tag.getInt("AlarmMode") : -1;
            return id >= 0 && id < values().length ? values()[id] : OFF;
        }
    }

    record Detection(boolean triggered, List<LivingEntity> intruders) {}
    private record Group(@Nullable ResourceLocation id, String name) {}

    private Mode mode = Mode.OFF;
    private final Set<String> players = new LinkedHashSet<>();
    private final Set<Group> groups = new LinkedHashSet<>();
    private final Map<UUID, String> creatures = new LinkedHashMap<>();

    Detection detect(ServerLevel level, BlockPos pos, int radius) {
        if (mode == Mode.OFF) return new Detection(false, List.of());

        Vec3 center = Vec3.atCenterOf(pos);
        AABB bounds = new AABB(center, center).inflate(radius);
        boolean playersOnly = mode == Mode.UNKNOWN_PLAYERS || mode == Mode.LISTED_PLAYERS;
        List<LivingEntity> intruders = new ArrayList<>();
        Set<String> observedPlayers = new HashSet<>();
        Set<Group> observedGroups = new HashSet<>();
        Set<UUID> observedCreatures = new HashSet<>();
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (candidate.isSpectator()) continue;
            if (candidate instanceof Player player) {
                String name = player.getGameProfile().getName();
                boolean known = players.contains(name);
                if (known) observedPlayers.add(name);
                if (mode == Mode.LISTED_PLAYERS ? known : !known) intruders.add(player);
            } else if (!playersOnly && candidate instanceof Mob mob && !ignored(mob)) {
                ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
                UUID identity = WerewolfVillagers.originalIdentity(mob);
                String name = mob.getDisplayName().getString();
                boolean known = false;
                for (Group group : groups) {
                    if (group.id != null ? group.id.equals(type) : group.name.equals(name)) {
                        observedGroups.add(group);
                        known = true;
                    }
                }
                if (creatures.containsKey(identity)) {
                    observedCreatures.add(identity);
                    known = true;
                }
                if (!known) intruders.add(mob);
            }
        }

        boolean triggered = switch (mode) {
            case UNKNOWN_PLAYERS, LISTED_PLAYERS, UNKNOWN_CREATURES -> !intruders.isEmpty();
            case MISSING_ANY -> observedPlayers.size() != players.size() || observedGroups.size() != groups.size()
                    || observedCreatures.size() != creatures.size();
            case MISSING_ALL -> observedPlayers.isEmpty() && observedGroups.isEmpty() && observedCreatures.isEmpty();
            case OFF -> false;
        };
        return new Detection(triggered, mode == Mode.MISSING_ANY || mode == Mode.MISSING_ALL ? List.of() : intruders);
    }

    private static boolean ignored(Mob mob) {
        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        return mob instanceof DreamSpirit || mob instanceof SleepingBody
                || type.getNamespace().equals(Witchery.MODID) && type.getPath().startsWith("illusion")
                || mob instanceof Familiar familiar && familiar.isFamiliar();
    }

    void cycle() {
        mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
    }

    Mode mode() {
        return mode;
    }

    boolean toggle(ItemStack stack, ServerLevel level) {
        String player = TaglockItem.boundPlayerName(stack);
        if (!player.isEmpty()) {
            if (!players.remove(player)) players.add(player);
            return true;
        }
        UUID id = TaglockItem.boundCreatureId(stack);
        if (id == null) return false;

        String name = TaglockItem.boundName(stack);
        ResourceLocation type = TaglockItem.boundCreatureType(stack);
        if (type == null && TaglockItem.resolveTarget(level, stack) instanceof Mob mob && !mob.hasCustomName()) {
            type = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        }
        var group = groupFor(type, name);
        if (group != null) {
            if (!groups.removeIf(known -> group.id.equals(known.id) || known.id == null && known.name.equals(group.name))) {
                groups.add(group);
            }
        } else if (creatures.remove(id) == null) {
            creatures.put(id, name);
        }
        return true;
    }

    @Nullable
    private static Group groupFor(@Nullable ResourceLocation type, String name) {
        for (EntityType<?> candidate : List.of(EntityType.VILLAGER, EntityType.SHEEP, EntityType.COW, EntityType.MOOSHROOM,
                EntityType.CHICKEN, EntityType.PIG, EntityType.HORSE, EntityType.BAT, EntityType.SQUID,
                WitcheryEntities.COVEN_WITCH.get(), WitcheryEntities.all().get("goblin").get())) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(candidate);
            if (type != null ? type.equals(id) : candidate.getDescription().getString().equals(name)
                    || candidate == EntityType.MOOSHROOM && name.equals("Mooshroom")) {
                return new Group(id, name);
            }
        }
        return null;
    }

    String subjects() {
        List<String> names = new ArrayList<>(players);
        for (Group group : groups) names.add("#" + group.name);
        names.addAll(creatures.values());
        return String.join(", ", names);
    }

    void clearSubjects() {
        players.clear();
        groups.clear();
        creatures.clear();
    }

    void load(CompoundTag tag) {
        clearSubjects();
        mode = Mode.load(tag);
        ListTag names = tag.getList("KnownPlayers", Tag.TAG_COMPOUND);
        for (int i = 0; i < names.size(); i++) {
            String name = names.getCompound(i).getString("PlayerName");
            if (!name.isEmpty()) players.add(name);
        }
        ListTag storedGroups = tag.getList("KnownCreatureTypes", Tag.TAG_COMPOUND);
        for (int i = 0; i < storedGroups.size(); i++) {
            CompoundTag entry = storedGroups.getCompound(i);
            String name = entry.getString("CreatureTypeName");
            String type = entry.getString("CreatureTypeId");
            ResourceLocation id = type.isEmpty() ? null : ResourceLocation.tryParse(type);
            if (!name.isEmpty()) {
                Group recognized = groupFor(id, name);
                groups.add(recognized != null ? recognized : new Group(id, name));
            }
        }
        ListTag individuals = tag.getList("KnownCreatures", Tag.TAG_COMPOUND);
        for (int i = 0; i < individuals.size(); i++) {
            CompoundTag entry = individuals.getCompound(i);
            long most = entry.getLong("CreatureMost");
            long least = entry.getLong("CreatureLeast");
            if (most != 0 || least != 0) creatures.put(new UUID(most, least), entry.getString("CreatureName"));
        }
    }

    void save(CompoundTag tag) {
        tag.putInt("AlarmMode", mode.ordinal());
        ListTag names = new ListTag();
        for (String name : players) {
            CompoundTag entry = new CompoundTag();
            entry.putString("PlayerName", name);
            names.add(entry);
        }
        ListTag storedGroups = new ListTag();
        for (Group group : groups) {
            CompoundTag entry = new CompoundTag();
            entry.putString("CreatureTypeName", group.name);
            if (group.id != null) entry.putString("CreatureTypeId", group.id.toString());
            storedGroups.add(entry);
        }
        ListTag individuals = new ListTag();
        creatures.forEach((id, name) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("CreatureMost", id.getMostSignificantBits());
            entry.putLong("CreatureLeast", id.getLeastSignificantBits());
            entry.putString("CreatureName", name);
            individuals.add(entry);
        });
        if (!names.isEmpty()) tag.put("KnownPlayers", names);
        if (!storedGroups.isEmpty()) tag.put("KnownCreatureTypes", storedGroups);
        if (!individuals.isEmpty()) tag.put("KnownCreatures", individuals);
    }
}
