package art.arcane.witchery.world.migration;

import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.world.entity.EntityType;

import java.util.Set;

public final class LegacyCreatureNames {
    private static final Set<String> HOBGOBLIN_NAMES = Set.of("Duende", "Хобгоблин", "妖精");
    private static final Set<String> COVEN_WITCH_NAMES = Set.of(
            "Zirkelhexe", "Strega del Circolo", "Bruja del Aquelarre", "Шабашная ведьма", "聚会女巫");

    private LegacyCreatureNames() {
    }

    public static boolean matches(EntityType<?> type, String name) {
        return type.getDescription().getString().equals(name)
                || type == EntityType.MOOSHROOM && name.equals("Mooshroom")
                || type == WitcheryEntities.GOBLIN.get() && HOBGOBLIN_NAMES.contains(name)
                || type == WitcheryEntities.COVEN_WITCH.get() && COVEN_WITCH_NAMES.contains(name);
    }
}
