package art.arcane.witchery.data;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class WitcheryBlockLoot extends BlockLootSubProvider {
    public WitcheryBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        for (RegistryObject<Block> block : WitcheryBlocks.LEGACY_BLOCKS.values()) {
            Block value = block.get();
            if (value.asItem() != Items.AIR) {
                dropSelf(value);
            }
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return WitcheryBlocks.LEGACY_BLOCKS.values().stream()
                .map(RegistryObject::get)
                .toList();
    }
}
