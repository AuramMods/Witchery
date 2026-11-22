package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.LegacyRegistryData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class LegacyWorldEffects {
    private static final int FIRST_WITCHERY_ID = 32;
    private static final Pattern START = Pattern.compile("\\s*I:PotionStartID\\s*=\\s*(-?\\d+)\\s*");
    private static final Pattern ID = Pattern.compile("\\s*I:\"?witchery:(potion\\.[a-z]+)\"?\\s*=\\s*(-?\\d+)\\s*");
    private final Map<Integer, String> names = new HashMap<>();

    LegacyWorldEffects(CompoundTag saved) {
        int next = Math.max(FIRST_WITCHERY_ID, saved.getInt("Start"));
        for (String effect : LegacyRegistryData.EFFECTS) {
            while (names.containsKey(next)) next++;
            if (next > 255) break;
            int id = saved.contains(effect, Tag.TAG_ANY_NUMERIC) ? saved.getInt(effect) : next;
            if (id >= FIRST_WITCHERY_ID && id <= 255) names.put(id, Witchery.MODID + ":" + effect);
        }
    }

    static CompoundTag capture(List<String> config) {
        var saved = new CompoundTag();
        for (String line : config) {
            var start = START.matcher(line);
            if (start.matches()) saved.putInt("Start", Integer.parseInt(start.group(1)));
            var id = ID.matcher(line);
            if (id.matches() && LegacyRegistryData.EFFECTS.contains(id.group(1))) {
                saved.putInt(id.group(1), Integer.parseInt(id.group(2)));
            }
        }
        return saved;
    }

    void upgrade(ListTag effects) {
        for (Tag value : effects) {
            CompoundTag effect = (CompoundTag) value;
            if (effect.contains("forge:id", Tag.TAG_STRING) || !effect.contains("Id", Tag.TAG_ANY_NUMERIC)) continue;
            String name = names.get(effect.getByte("Id") & 255);
            if (name != null) effect.putString("forge:id", name);
        }
    }
}
