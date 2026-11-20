package art.arcane.witchery.data;

import art.arcane.witchery.block.CritterSnareBlock;
import art.arcane.witchery.block.BrambleBlock;

import art.arcane.witchery.block.ChaliceBlock;
import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.DreamWeave;
import art.arcane.witchery.block.entity.LeechSamples;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.block.WitchCropBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.block.WolfHeadBlock;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
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
                case "distilleryburning" -> dropOther(block, WitcheryBlocks.getRequired("distilleryidle").get());
                case "chalice" -> dropChalice(block);
                case "demonheart" -> dropOther(block, WitcheryIngredient.DEMON_HEART.item());
                case "dreamcatcher" -> dropDreamWeave(block);
                case "coffinblock" -> add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(WitcheryItems.getRequired("coffin").get())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BedBlock.PART, BedPart.FOOT)))))));
                case "witchsapling", "witchlog", "witchwood" -> dropWitchWood(block);
                case "crittersnare" -> dropCritterSnare(block);
                case "wolftrap" -> add(block, LootTable.lootTable());
                case "wolfhead" -> add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1)).add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(WitcheryItems.getRequired("hellhound_head").get()).when(
                                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(
                                                StatePropertiesPredicate.Builder.properties().hasProperty(WolfHeadBlock.HELLHOUND, true))),
                                LootItem.lootTableItem(block))))));
                case "leechchest" -> add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(block)
                                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                        .copy(LeechSamples.PLAYERS_TAG, LeechSamples.PLAYERS_TAG))))));
                case "bramble" -> add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(block).apply(CopyBlockState.copyState(block).copy(BrambleBlock.WILD))))));
                case "witchleaves" -> dropWitchLeaves(block);
                default -> {
                    if (block instanceof SlabBlock) {
                        add(block, createSlabItemTable(block));
                    } else if (block instanceof WitchCropBlock crop) {
                        dropWitchCrop(crop);
                    } else {
                        dropSelf(block);
                    }
                }
            }
        }
    }

    private void dropDreamWeave(Block block) {
        LootPoolEntryContainer.Builder<?>[] entries = java.util.Arrays.stream(DreamWeave.values())
                .map(weave -> LootItem.lootTableItem(weave.item()).when(
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(DreamCatcherBlock.WEAVE, weave))))
                .toArray(LootPoolEntryContainer.Builder<?>[]::new);
        add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1)).add(AlternativesEntry.alternatives(entries)))));
    }

    private void dropChalice(Block block) {
        LootItemCondition.Builder filled = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ChaliceBlock.FILLED, true));
        add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1)).add(AlternativesEntry.alternatives(
                        LootItem.lootTableItem(WitcheryIngredient.FILLED_CHALICE.item()).when(filled), LootItem.lootTableItem(block))))));
    }

    private void dropWitchCrop(WitchCropBlock block) {
        WitchCrop crop = block.crop();
        ItemLike seed = crop.seed();
        LootItemCondition.Builder mature = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(CropBlock.AGE, crop.maxAge()));
        if (crop == WitchCrop.MINDRAKE) {
            add(block, applyExplosionDecay(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(seed)))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(mature)
                            .when(LootItemRandomChanceCondition.randomChance(0.25F)).add(LootItem.lootTableItem(seed)))));
            return;
        }

        ItemLike produce = switch (crop) {
            case BELLADONNA -> WitcheryIngredient.BELLADONNA_FLOWER.item();
            case MANDRAKE -> WitcheryIngredient.MANDRAKE_ROOT.item();
            case ARTICHOKE -> WitcheryIngredient.GLOBE_ARTICHOKE.item();
            case SNOWBELL -> Items.SNOWBALL;
            case WORMWOOD -> WitcheryIngredient.WORMWOOD.item();
            case WOLFSBANE -> WitcheryIngredient.WOLFSBANE.item();
            default -> seed;
        };
        float seedChance = 8.0F / 15;
        LootTable.Builder table = LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(AlternativesEntry.alternatives(LootItem.lootTableItem(produce).when(mature), LootItem.lootTableItem(seed))))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(mature)
                        .add(LootItem.lootTableItem(seed)
                                .apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, seedChance)))
                                .apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, seedChance, 0))));
        if (crop == WitchCrop.SNOWBELL) {
            table.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(mature)
                    .when(LootItemRandomChanceCondition.randomChance(0.2F))
                    .add(LootItem.lootTableItem(WitcheryIngredient.ICY_NEEDLE.item())));
        }
        if (crop == WitchCrop.SNOWBELL) {
            table.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(mature)
                    .when(LootItemRandomChanceCondition.randomChance(0.2F))
                    .add(LootItem.lootTableItem(WitcheryIngredient.ICY_NEEDLE.item())));
        }
        add(block, applyExplosionDecay(block, table));
    }

    private void dropWitchWood(Block block) {
        add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(block).apply(copyWoodType(block))))));
    }

    private void dropCritterSnare(Block snare) {
        LootItemCondition.Builder empty = LootItemBlockStatePropertyCondition.hasBlockStateProperties(snare)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(CritterSnareBlock.CRITTER, CritterSnareBlock.Critter.EMPTY));
        add(snare, LootTable.lootTable().withPool(applyExplosionCondition(snare, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(snare).apply(CopyBlockState.copyState(snare)
                        .copy(CritterSnareBlock.CRITTER).copy(CritterSnareBlock.STOCKED).when(empty.invert()))))));
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
