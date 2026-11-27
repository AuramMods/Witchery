package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrambleTests {
    private static final Field LEVEL_RANDOM = ObfuscationReflectionHelper.findField(Level.class, "f_46441_");

    @GameTest(template = "qa_coven_arena", batch = "witchery_bramble_harvest")
    public static void survivalHarvestSpreadsOnlyWildBrambleWithoutShears(GameTestHelper helper) throws IllegalAccessException {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(7, 4, 7));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrambleHarvester"));
        var dropStates = List.of(wild(), wild(), wild().setValue(BrambleBlock.WILD, false));
        var tools = List.of(ItemStack.EMPTY, new ItemStack(Items.SHEARS), ItemStack.EMPTY);
        var planted = new ArrayList<BlockPos>();
        var drops = new ArrayList<ItemStack>();
        Consumer<BlockEvent.EntityPlaceEvent> observe = event -> {
            if (event.getLevel() == level && event.getEntity() == player) planted.add(event.getPos().immutable());
        };
        Consumer<EntityJoinLevelEvent> collect = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item && item.getItem().is(wild().getBlock().asItem())) {
                drops.add(item.getItem().copy());
            }
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        MinecraftForge.EVENT_BUS.addListener(collect);
        var previous = level.random;
        try {
            for (int sample = 0; sample < tools.size(); sample++) {
                resetPlot(level, pos);
                planted.clear();
                drops.clear();
                BlockState state = dropStates.get(sample);
                level.setBlockAndUpdate(pos, state);
                player.setItemInHand(InteractionHand.MAIN_HAND, tools.get(sample));
                var random = new SpreadRolls(false);
                LEVEL_RANDOM.set(level, random);
                helper.assertTrue(player.gameMode.destroyBlock(pos) && level.isEmptyBlock(pos), "Player harvest clears the original bramble");
                helper.assertTrue(drops.size() == 1 && BrambleBlock.isWild(drops.get(0)) == state.getValue(BrambleBlock.WILD),
                        "Harvest returns one matching item for each tool and variant");
                var destinations = sample == 0 ? List.of(pos.east(), pos.south().below(), pos.west().below(), pos.north().above(),
                        pos.south().east().below(), pos.north().west().below(), pos.south().west().below(), pos.north().east().below())
                        : List.<BlockPos>of();
                helper.assertTrue(planted.equals(destinations), "Harvest spread matches the original ordered columns: " + planted);
                helper.assertTrue(destinations.stream().allMatch(cell -> level.getBlockState(cell).equals(wild())),
                        "The spread replaces snow and short grass, and leaves the water under the northern plant");
                helper.assertTrue(level.getBlockState(pos.north()).is(Blocks.WATER), "Wild Bramble never replaces water");
                if (sample == 0) {
                    helper.assertTrue(random.bounds.equals(List.of(2, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2, 3, 2, 3)),
                            "Each eligible column consumes the original spread and stop rolls");
                } else {
                    helper.assertTrue(random.bounds.isEmpty(), "Shears and Ender Bramble skip all spread rolls");
                }
                LEVEL_RANDOM.set(level, previous);
            }
        } finally {
            LEVEL_RANDOM.set(level, previous);
            MinecraftForge.EVENT_BUS.unregister(observe);
            MinecraftForge.EVENT_BUS.unregister(collect);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_bramble_support")
    public static void spreadCanClimbAndBothVariantsNeedNonAirSupport(GameTestHelper helper) throws IllegalAccessException {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(7, 4, 7));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrambleSupport"));
        resetPlot(level, pos);
        level.setBlockAndUpdate(pos, wild());
        Consumer<BlockEvent.EntityPlaceEvent> protect = event -> {
            if (event.getLevel() == level && event.getEntity() == player && event.getPos().equals(pos.east())) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(protect);
        var previous = level.random;
        try {
            LEVEL_RANDOM.set(level, new SpreadRolls(true));
            helper.assertTrue(player.gameMode.destroyBlock(pos), "Harvest the spreading Wild Bramble");
        } finally {
            LEVEL_RANDOM.set(level, previous);
            MinecraftForge.EVENT_BUS.unregister(protect);
        }
        helper.assertTrue(level.getBlockState(pos.east()).is(Blocks.SNOW_BLOCK)
                        && level.getBlockState(pos.east().above()).equals(wild()),
                "The veto keeps the original snow and the continuation roll can still plant the next level");
        helper.assertTrue(level.getBlockState(pos.south().below()).equals(wild())
                        && level.getBlockState(pos.south()).equals(wild()) && level.getBlockState(pos.south().above()).equals(wild()),
                "A continuation roll can plant three successive blocks in one column");

        for (boolean isWild : List.of(false, true)) {
            BlockState bramble = wild().setValue(BrambleBlock.WILD, isWild);
            level.setBlockAndUpdate(pos.below(), Blocks.WATER.defaultBlockState());
            level.setBlockAndUpdate(pos, bramble);
            helper.assertTrue(bramble.canSurvive(level, pos) && level.getBlockState(pos).equals(bramble),
                    "Either variant can use water as support");
            helper.assertTrue(BrambleBlock.isWild(bramble.getBlock().getCloneItemStack(level, pos, bramble)) == isWild,
                    "Pick block keeps the original variant");
            level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
            helper.assertTrue(level.isEmptyBlock(pos), "Losing non-air support removes either variant");
        }
        helper.succeed();
    }

    private static void resetPlot(Level level, BlockPos pos) {
        for (BlockPos cell : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            level.setBlock(cell, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (BlockPos cell : BlockPos.betweenClosed(pos.offset(-1, -2, -1), pos.offset(1, -2, 1))) {
            level.setBlock(cell, Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos.east().below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos.east(), Blocks.SNOW_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos.south().below(), Blocks.SNOW_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos.west().below(), Blocks.GRASS.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos.west(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos.north().below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos.north(), Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private static BlockState wild() {
        return WitcheryBlocks.getRequired("bramble").get().defaultBlockState();
    }

    private static final class SpreadRolls extends LegacyRandomSource {
        private final List<Integer> bounds = new ArrayList<>();
        private final boolean continueColumn;

        private SpreadRolls(boolean continueColumn) {
            super(0L);
            this.continueColumn = continueColumn;
        }

        @Override
        public int nextInt(int bound) {
            bounds.add(bound);
            return bound == 3 && !continueColumn ? 1 : 0;
        }
    }
}
