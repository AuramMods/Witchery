package art.arcane.witchery.infusion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public enum SpellKnowledge {
    CARNOSA_DIEM("carnosadiem"),
    IGNIANIMA("ignianima"),
    MORSMORDRE("morsmordre"),
    TORMENTUM("tormentum");

    private static final String BOOK = "WITCSpellBook";
    private final String key;

    SpellKnowledge(String key) {
        this.key = key;
    }

    public void learn(LivingEntity drinker) {
        if (!(drinker instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag spells = persisted.getCompound(BOOK);
        spells.putBoolean(key, true);
        persisted.put(BOOK, spells);
        data.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static List<String> known(ServerPlayer player) {
        CompoundTag book = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(BOOK);
        return book.getAllKeys().stream()
                .filter(name -> book.contains(name, Tag.TAG_BYTE) && book.getBoolean(name))
                .sorted().toList();
    }
}
