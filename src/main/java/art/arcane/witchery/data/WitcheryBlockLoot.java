package art.arcane.witchery.data;

import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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
                case "witchesovenburning" -> dropOther(block, WitcheryBlocks.getRequired("witchesovenidle").get());
                case "witchsapling", "witchlog", "witchwood" -> dropWitchWood(block);
                case "witchleaves" -> dropWitchLeaves(block);
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

    private void dropWitchWood(Block block) {
        add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(block).apply(copyWoodType(block))))));
    }

    private void dropWitchLeaves(Block leaves) {
        Block sapling = WitcheryBlocks.getRequired("witchsapling").get();
        LootItemCondition.Builder takesLeaves = HAS_SHEARS.or(HAS_SILK_TOUCH);
        add(leaves, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(leaves).apply(copyWoodType(leaves)).when(takesLeaves),
                                applyExplosionCondition(sapling, LootItem.lootTableItem(sapling)
                                        .apply(copyWoodType(leaves))
                                        .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE,
                                                1.0F / 20, 1.0F / 16, 1.0F / 12, 1.0F / 10))))))
                .withPool(applyExplosionCondition(WitcheryIngredient.ROWAN_BERRIES.item(), LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1)).when(takesLeaves.invert())
                        .when(woodTypeIs(leaves, WitchWoodType.ROWAN))
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE,
                                1.0F / 200, 1.0F / 180, 1.0F / 160, 1.0F / 120, 1.0F / 40))
                        .add(LootItem.lootTableItem(WitcheryIngredient.ROWAN_BERRIES.item())))));
    }

    private static CopyBlockState.Builder copyWoodType(Block block) {
        return CopyBlockState.copyState(block).copy(WitchWoodType.PROPERTY)
                .when(woodTypeIs(block, WitchWoodType.ROWAN).invert());
    }

    private static LootItemCondition.Builder woodTypeIs(Block block, WitchWoodType type) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(WitchWoodType.PROPERTY, type));
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
