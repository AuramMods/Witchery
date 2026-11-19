package art.arcane.witchery.data;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class WitcheryBlockLoot extends BlockLootSubProvider {
    public WitcheryBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        for (RegistryObject<Block> entry : WitcheryBlocks.all()) {
            Block block = entry.get();
            if (block.getLootTable().equals(BuiltInLootTables.EMPTY)) {
                continue;
            }
            switch (entry.getId().getPath()) {
                case "witchwooddoubleslab" -> dropDoubleSlab(block, "witchwoodslab");
                case "icedoubleslab" -> dropDoubleSlab(block, "iceslab");
                case "snowdoubleslab" -> dropDoubleSlab(block, "snowslab");
                default -> {
                    if (block instanceof SlabBlock) {
                        add(block, createSlabItemTable(block));
                    } else {
                        dropSelf(block);
                    }
                }
            }
        }
    }

    private void dropDoubleSlab(Block block, String slabId) {
        Block slab = WitcheryBlocks.getRequired(slabId).get();
        add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(applyExplosionDecay(slab, LootItem.lootTableItem(slab)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2)))))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return WitcheryBlocks.LEGACY_BLOCKS.values().stream()
                .map(RegistryObject::get)
                .toList();
    }
}
