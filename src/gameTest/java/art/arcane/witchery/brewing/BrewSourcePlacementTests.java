package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewSourcePlacementTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_source_materials")
    public static void bothBottlesUseTheOriginalMaterialReplacement(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 6, 5));
        var caster = FakePlayerFactory.getMinecraft(level);
        var replaceable = List.of(Blocks.AIR, Blocks.GRASS, Blocks.FERN, Blocks.DEAD_BUSH, Blocks.VINE,
                Blocks.FIRE, Blocks.SNOW, Blocks.WATER, Blocks.LAVA, WitcheryBlocks.getRequired("spiritflowing").get(),
                WitcheryBlocks.getRequired("dreamcatcher").get(), WitcheryBlocks.getRequired("snowpressureplate").get());
        var protectedBlocks = List.of(Blocks.COBWEB, Blocks.WHEAT, Blocks.TORCH, Blocks.REDSTONE_WIRE,
                Blocks.POPPY, Blocks.STONE, WitcheryBlocks.getRequired("brewgas").get());
        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), flags);
        for (boolean gas : List.of(true, false)) {
            var block = (BrewDispersalBlock) WitcheryBlocks.getRequired(gas ? "brewgas" : "brewliquid").get();
            var brew = brew(gas, Items.SUGAR);
            var tag = BrewMixture.ingredientTag(brew, 0x226644);
            var mixture = BrewMixture.from(brew);
            for (Block material : replaceable) {
                level.setBlock(pos, material.defaultBlockState(), flags);
                helper.assertTrue(level.getBlockState(pos).is(material), "The replacement fixture placed " + material);
                block.release(level, pos, Direction.UP, tag, mixture, 0x226644, caster);
                helper.assertTrue(level.getBlockState(pos).is(block)
                                && level.getBlockEntity(pos) instanceof BrewFluidBlockEntity source && source.hasContents(),
                        "A bottled " + block + " replaces " + material);
            }
            for (Block material : protectedBlocks) {
                level.setBlock(pos, material.defaultBlockState(), flags);
                helper.assertTrue(level.getBlockState(pos).is(material), "The preservation fixture placed " + material);
                block.release(level, pos, Direction.UP, tag, mixture, 0x226644, caster);
                helper.assertTrue(level.getBlockState(pos).is(material), "A bottled " + block + " preserves " + material);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_source_refresh")
    public static void liquidBottlesRefreshExistingSourcesAndHonorBothVetoes(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 6, 5));
        var block = (BrewDispersalBlock) WitcheryBlocks.getRequired("brewliquid").get();
        var caster = FakePlayerFactory.getMinecraft(level);
        var first = brew(false, Items.SUGAR);
        var second = brew(false, Items.PUMPKIN);
        var oldTag = BrewMixture.ingredientTag(first, 0x226644);
        var newTag = BrewMixture.ingredientTag(second, 0x773333);
        var secondMixture = BrewMixture.from(second);
        level.setBlockAndUpdate(pos, block.defaultBlockState());
        var fluid = (BrewFluidBlockEntity) level.getBlockEntity(pos);
        fluid.initialize(oldTag, BrewMixture.from(first), 0x226644, caster);
        fluid.tickAge();
        var initial = fluid.saveWithFullMetadata();
        Consumer<BlockEvent.BreakEvent> vetoBreak = event -> {
            if (event.getLevel() == level && event.getPos().equals(pos) && event.getPlayer() == caster) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(vetoBreak);
        try {
            block.release(level, pos, Direction.UP, newTag, secondMixture, 0x773333, caster);
            helper.assertTrue(fluid.saveWithFullMetadata().equals(initial), "A break veto leaves the existing source intact");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(vetoBreak);
        }
        block.release(level, pos, Direction.UP, newTag, secondMixture, 0x773333, caster);
        var refreshed = fluid.saveWithoutMetadata();
        helper.assertTrue(refreshed.getCompound("Effect").equals(newTag) && refreshed.getInt("UpdateCount") == 1,
                "Releasing over a source replaces its recipe and retains the running age");

        var flowing = block.defaultBlockState().setValue(LiquidBlock.LEVEL, 3);
        level.setBlockAndUpdate(pos, flowing);
        Consumer<BlockEvent.EntityPlaceEvent> vetoPlace = event -> {
            if (event.getLevel() == level && event.getPos().equals(pos) && event.getEntity() == caster) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(vetoPlace);
        try {
            block.release(level, pos, Direction.UP, oldTag, BrewMixture.from(first), 0x226644, caster);
            helper.assertTrue(level.getBlockState(pos).equals(flowing)
                            && level.getBlockEntity(pos).saveWithoutMetadata().equals(refreshed),
                    "A placement veto restores the flowing level and its original recipe");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(vetoPlace);
        }
        block.release(level, pos, Direction.UP, oldTag, BrewMixture.from(first), 0x226644, caster);
        helper.assertTrue(level.getBlockState(pos).equals(block.defaultBlockState())
                        && level.getBlockEntity(pos).saveWithoutMetadata().getCompound("Effect").equals(oldTag),
                "An approved bottle establishes a new source over flowing brew liquid");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_source_spread")
    public static void spreadingGasStillRecognizesOnlyAirAndSnow(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 6, 5));
        var block = (BrewDispersalBlock) WitcheryBlocks.getRequired("brewgas").get();
        for (var cell : List.of(pos, pos.north(), pos.south(), pos.west(), pos.east())) {
            level.setBlockAndUpdate(cell.below(), Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(pos.south().below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(pos.north(), Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(pos.south(), Blocks.GRASS.defaultBlockState());
        level.setBlockAndUpdate(pos.west(), Blocks.SNOW.defaultBlockState());
        level.setBlockAndUpdate(pos.east(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(level.getBlockState(pos.south()).is(Blocks.GRASS), "The grass has valid footing");
        var ingredients = brew(true, Items.SUGAR);
        block.release(level, pos, Direction.UP, BrewMixture.ingredientTag(ingredients, 0x226644),
                BrewMixture.from(ingredients), 0x226644, null);
        block.tick(level.getBlockState(pos), level, pos, RandomSource.create(0));
        helper.assertTrue(level.getBlockState(pos.north()).is(Blocks.WATER) && level.getBlockState(pos.south()).is(Blocks.GRASS)
                        && level.getBlockState(pos.east()).is(block) && level.getBlockState(pos.west()).is(block),
                "Spreading gas fills air and snow while preserving water and grass");
        helper.succeed();
    }

    private static List<ItemStack> brew(boolean gas, Item effect) {
        return List.of(Items.NETHER_WART.getDefaultInstance(), effect.getDefaultInstance(),
                (gas ? WitcheryIngredient.BAT_WOOL : WitcheryIngredient.WORMWOOD).item().getDefaultInstance());
    }
}
