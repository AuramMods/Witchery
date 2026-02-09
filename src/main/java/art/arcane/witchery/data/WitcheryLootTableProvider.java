package art.arcane.witchery.data;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public class WitcheryLootTableProvider extends LootTableProvider {
    public WitcheryLootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(new SubProviderEntry(WitcheryBlockLoot::new, LootContextParamSets.BLOCK)));
    }
}
