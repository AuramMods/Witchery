package art.arcane.witchery.block.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class LeechSamples {
    public static final String PLAYERS_TAG = "WITCPlayers";

    private final List<String> players = new ArrayList<>();
    private final int capacity;

    LeechSamples(int capacity) {
        this.capacity = capacity;
    }

    boolean record(ServerPlayer player) {
        return add(player.getGameProfile().getName());
    }

    private boolean add(String name) {
        if (name.isEmpty() || players.contains(name)) return false;
        players.add(name);
        if (players.size() > capacity) players.remove(0);
        return true;
    }

    @Nullable
    ServerPlayer take(ServerPlayer collector, boolean excludeCollector) {
        String collectorName = collector.getGameProfile().getName();
        if (excludeCollector && players.size() == 1 && players.get(0).equals(collectorName)) {
            collector.sendSystemMessage(Component.translatable("message.witchery.leechchest.self")
                    .withStyle(ChatFormatting.RED));
            return null;
        }
        List<String> missing = new ArrayList<>();
        for (int i = players.size() - 1; i >= 0; i--) {
            String name = players.get(i);
            if (excludeCollector && name.equals(collectorName)) continue;
            ServerPlayer subject = collector.server.getPlayerList().getPlayerByName(name);
            if (subject != null && subject.serverLevel() == collector.serverLevel()) {
                players.remove(i);
                return subject;
            }
            missing.add(name);
        }
        if (!missing.isEmpty()) {
            collector.sendSystemMessage(Component.translatable("message.witchery.leechchest.absent", String.join(", ", missing))
                    .withStyle(ChatFormatting.RED));
        }
        return null;
    }

    int size() {
        return players.size();
    }

    void load(CompoundTag tag) {
        players.clear();
        for (String name : names(tag)) add(name);
    }

    void save(CompoundTag tag) {
        if (players.isEmpty()) return;
        ListTag entries = new ListTag();
        for (String name : players) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Player", name);
            entries.add(entry);
        }
        tag.put(PLAYERS_TAG, entries);
    }

    public static List<String> names(@Nullable CompoundTag tag) {
        List<String> names = new ArrayList<>();
        if (tag != null) {
            ListTag entries = tag.getList(PLAYERS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < entries.size(); i++) {
                String name = entries.getCompound(i).getString("Player");
                if (!name.isEmpty()) names.add(name);
            }
        }
        return names;
    }
}
