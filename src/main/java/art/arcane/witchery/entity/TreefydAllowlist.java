package art.arcane.witchery.entity;

import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class TreefydAllowlist {
    private static final List<EntityType<?>> VANILLA_HERDS = List.of(EntityType.VILLAGER, EntityType.SHEEP, EntityType.COW,
            EntityType.MOOSHROOM, EntityType.CHICKEN, EntityType.PIG, EntityType.HORSE);
    private final Map<String, UUID> players = new LinkedHashMap<>();
    private final Map<String, ResourceLocation> species = new LinkedHashMap<>();
    private final Map<UUID, String> creatures = new LinkedHashMap<>();

    boolean knows(LivingEntity entity) {
        if (entity instanceof Player player) {
            return matchingName(players, player.getGameProfile().getName(), player.getUUID()) != null;
        }
        if (!(entity instanceof Mob mob)) return false;
        ResourceLocation type = mob.hasCustomName() ? null : BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        return matchingName(species, mob.getName().getString(), type) != null
                || creatures.containsKey(WerewolfVillagers.originalIdentity(mob));
    }

    boolean update(ItemStack taglock, Player owner, UUID guardian, boolean remove) {
        String player = TaglockItem.boundPlayerName(taglock);
        if (!player.isEmpty()) {
            UUID id = TaglockItem.boundPlayerId(taglock);
            if (id != null ? id.equals(owner.getUUID()) : player.equalsIgnoreCase(owner.getGameProfile().getName())) return false;
            return updateNamed(players, player, id, remove);
        }
        UUID id = TaglockItem.boundCreatureId(taglock);
        if (id == null || id.equals(guardian)) return false;
        String name = TaglockItem.boundName(taglock);
        ResourceLocation type = TaglockItem.boundCreatureType(taglock);
        EntityType<?> herd = herdFor(type, name);
        if (herd != null) {
            return updateNamed(species, name, BuiltInRegistries.ENTITY_TYPE.getKey(herd), remove);
        }
        if (remove) return creatures.remove(id) != null;
        return creatures.putIfAbsent(id, name) == null;
    }

    @Nullable
    private static EntityType<?> herdFor(@Nullable ResourceLocation id, String name) {
        if (id != null) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
            return VANILLA_HERDS.contains(type) || type == WitcheryEntities.GOBLIN.get() ? type : null;
        }
        for (EntityType<?> type : VANILLA_HERDS) {
            if (type.getDescription().getString().equals(name)) return type;
        }
        EntityType<?> goblin = WitcheryEntities.GOBLIN.get();
        return goblin.getDescription().getString().equals(name) ? goblin : null;
    }

    private static <T> boolean updateNamed(Map<String, T> values, String name, @Nullable T id, boolean remove) {
        String oldName = matchingName(values, name, id);
        if (remove) {
            if (oldName == null) return false;
            values.remove(oldName);
        } else {
            if (oldName != null) return false;
            values.put(name, id);
        }
        return true;
    }

    @Nullable
    private static <T> String matchingName(Map<String, T> values, String name, @Nullable T id) {
        for (var entry : values.entrySet()) {
            if (id != null && id.equals(entry.getValue())
                    || (id == null || entry.getValue() == null) && name.equals(entry.getKey())) return entry.getKey();
        }
        return null;
    }

    void copyFrom(TreefydAllowlist original) {
        players.putAll(original.players);
        species.putAll(original.species);
        creatures.putAll(original.creatures);
    }

    List<String> names() {
        List<String> names = new ArrayList<>(players.keySet());
        for (String type : species.keySet()) names.add("#" + type);
        names.addAll(creatures.values());
        return names;
    }

    void save(CompoundTag tag) {
        ListTag namedPlayers = new ListTag();
        players.forEach((name, id) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("PlayerName", name);
            if (id != null) entry.putUUID("PlayerUUID", id);
            namedPlayers.add(entry);
        });
        ListTag namedSpecies = new ListTag();
        species.forEach((name, id) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("CreatureTypeName", name);
            if (id != null) entry.putString("CreatureType", id.toString());
            namedSpecies.add(entry);
        });
        ListTag namedCreatures = new ListTag();
        creatures.forEach((id, name) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("CreatureMost", id.getMostSignificantBits());
            entry.putLong("CreatureLeast", id.getLeastSignificantBits());
            entry.putString("CreatureName", name);
            namedCreatures.add(entry);
        });
        if (!namedPlayers.isEmpty()) tag.put("KnownPlayers", namedPlayers);
        if (!namedSpecies.isEmpty()) tag.put("KnownCreatureTypes", namedSpecies);
        if (!namedCreatures.isEmpty()) tag.put("KnownCreatures", namedCreatures);
    }

    void load(CompoundTag tag) {
        players.clear();
        species.clear();
        creatures.clear();
        for (Tag stored : tag.getList("KnownPlayers", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) stored;
            String name = entry.getString("PlayerName");
            if (!name.isEmpty()) players.put(name, entry.hasUUID("PlayerUUID") ? entry.getUUID("PlayerUUID") : null);
        }
        for (Tag stored : tag.getList("KnownCreatureTypes", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) stored;
            String name = entry.getString("CreatureTypeName");
            String id = entry.getString("CreatureType");
            if (!name.isEmpty()) species.put(name, id.isEmpty() ? null : ResourceLocation.tryParse(id));
        }
        for (Tag stored : tag.getList("KnownCreatures", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) stored;
            long most = entry.getLong("CreatureMost");
            long least = entry.getLong("CreatureLeast");
            if (most != 0 || least != 0) creatures.put(new UUID(most, least), entry.getString("CreatureName"));
        }
    }
}
