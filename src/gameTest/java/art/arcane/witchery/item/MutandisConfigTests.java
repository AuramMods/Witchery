package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.WitcheryDimensions;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class MutandisConfigTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_mutandis_config")
    public static void defaultAndDisabledMutandisExtras(GameTestHelper helper) {
        var config = WitcheryServerConfig.MUTANDIS_ADDITIONAL_BLOCKS;
        var previous = config.get();
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "Mutandis"));
        var pos = new BlockPos(4, 4, 4);
        helper.setBlock(pos.below(), Blocks.GRASS_BLOCK);
        var fern = Blocks.FERN.defaultBlockState();
        var glintweed = WitcheryBlocks.getRequired("glintweed").get().defaultBlockState();
        try {
            config.set(config.getDefault());
            assertMutation(helper, player, pos, fern, WitcheryIngredient.MUTANDIS);
            assertMutation(helper, player, pos, glintweed, WitcheryIngredient.MUTANDIS_EXTREMIS);

            config.set(List.of());
            helper.setBlock(pos, fern);
            var powder = new ItemStack(WitcheryIngredient.MUTANDIS.item());
            helper.assertTrue(use(helper, player, pos, powder) == InteractionResult.PASS && powder.getCount() == 1
                    && helper.getBlockState(pos) == fern, "An empty list removes the default extras");
            assertMutation(helper, player, pos, Blocks.POPPY.defaultBlockState(), WitcheryIngredient.MUTANDIS);
        } finally {
            config.set(previous);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_mutandis_config")
    public static void configuredLegacyAndModernStatesCanMutate(GameTestHelper helper) {
        var config = WitcheryServerConfig.MUTANDIS_ADDITIONAL_BLOCKS;
        var previous = config.get();
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "Mutandis"));
        var pos = new BlockPos(4, 4, 4);
        helper.setBlock(pos.below(), Blocks.GRASS_BLOCK);
        var horizontalLog = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        try {
            config.set(List.of("tallgrass,2", "minecraft:wool,14", "minecraft:brick_block", "minecraft:oak_log[axis=x]",
                    "missing:block", "minecraft:fern,broken", "minecraft:stone{unexpected:1}"));
            assertMutation(helper, player, pos, Blocks.FERN.defaultBlockState(), WitcheryIngredient.MUTANDIS);
            assertMutation(helper, player, pos, Blocks.RED_WOOL.defaultBlockState(), WitcheryIngredient.MUTANDIS_EXTREMIS);
            assertMutation(helper, player, pos, Blocks.BRICKS.defaultBlockState(), WitcheryIngredient.MUTANDIS);
            assertMutation(helper, player, pos, horizontalLog, WitcheryIngredient.MUTANDIS);

            helper.setBlock(pos, Blocks.OAK_LOG);
            var powder = new ItemStack(WitcheryIngredient.MUTANDIS.item());
            helper.assertTrue(use(helper, player, pos, powder) == InteractionResult.PASS && powder.getCount() == 1,
                    "A configured horizontal log leaves the vertical state outside the outcome pool");
        } finally {
            config.set(previous);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_mutandis_seeded")
    public static void seededMutandisUsesTheOriginalPlantOrder(GameTestHelper helper) {
        var overworld = helper.getLevel();
        var dream = overworld.getServer().getLevel(WitcheryDimensions.DREAM);
        var pos = helper.absolutePos(new BlockPos(4, 4, 4)).atY(170);
        var sapling = WitcheryBlocks.getRequired("witchsapling").get().defaultBlockState();
        var moss = WitcheryBlocks.getRequired("spanishmoss").get().defaultBlockState().setValue(VineBlock.SOUTH, true);
        var glintweed = WitcheryBlocks.getRequired("glintweed").get().defaultBlockState();
        var config = WitcheryServerConfig.MUTANDIS_ADDITIONAL_BLOCKS;
        var previous = config.get();
        try {
            config.set(config.getDefault());
            var grass = Blocks.GRASS.defaultBlockState();
            assertRolls(helper, overworld, pos, grass, WitcheryIngredient.MUTANDIS, List.of(
                    new LegacyRoll(0, sapling, 948),
                    new LegacyRoll(6, sapling.setValue(WitchWoodType.PROPERTY, WitchWoodType.ALDER), 876),
                    new LegacyRoll(4, sapling.setValue(WitchWoodType.PROPERTY, WitchWoodType.HAWTHORN), 452),
                    new LegacyRoll(10, WitcheryBlocks.getRequired("embermoss").get().defaultBlockState(), 380),
                    new LegacyRoll(14, Blocks.BROWN_MUSHROOM.defaultBlockState(), 884),
                    new LegacyRoll(29, Blocks.POPPY.defaultBlockState(), 84),
                    new LegacyRoll(1, moss, 588),
                    new LegacyRoll(7, glintweed, 164),
                    new LegacyRoll(5, Blocks.FERN.defaultBlockState(), 92)));
            assertRolls(helper, dream, pos, grass, WitcheryIngredient.MUTANDIS, List.of(
                    new LegacyRoll(18, moss, 4),
                    new LegacyRoll(16, glintweed, 932),
                    new LegacyRoll(20, Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, 3), 436)));
        } finally {
            config.set(previous);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_mutandis_seeded")
    public static void extremisLimitsWitcheryCropsToTheirOriginalMaturity(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 4, 4)).atY(170);
        var carrots = Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, 6);
        var config = WitcheryServerConfig.MUTANDIS_ADDITIONAL_BLOCKS;
        var previous = config.get();
        try {
            config.set(config.getDefault());
            assertRolls(helper, level, pos, carrots, WitcheryIngredient.MUTANDIS_EXTREMIS, List.of(
                    new LegacyRoll(35, matureCrop("belladonna"), 43),
                    new LegacyRoll(1, matureCrop("mandrake"), 588),
                    new LegacyRoll(22, matureCrop("artichoke"), 860)));
        } finally {
            config.set(previous);
        }
        helper.succeed();
    }

    private static BlockState matureCrop(String id) {
        var crop = (CropBlock) WitcheryBlocks.getRequired(id).get();
        return crop.getStateForAge(4);
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_mutandis_terrain")
    public static void extremisKeepsTheOriginalTerrainRollAndDirtVariants(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ExtremisTerrain"));
        var center = helper.absolutePos(new BlockPos(4, 4, 4)).atY(170);
        var wet = center.east(8);
        var cross = List.of(wet, wet.east(), wet.west(), wet.south(), wet.north());
        var soils = List.of(Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL);
        for (int index = 0; index < cross.size(); index++) {
            BlockPos pos = cross.get(index);
            level.setBlock(pos, soils.get(index).defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(pos.above(), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, index),
                    Block.UPDATE_CLIENTS);
        }
        for (long seed : new long[]{0, 4096}) {
            boolean converts = seed == 4096;
            level.setBlock(center, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.random.setSeed(seed);
            applyPowder(helper, player, center, WitcheryIngredient.MUTANDIS_EXTREMIS);
            helper.assertTrue(level.getBlockState(center).is(converts ? Blocks.MYCELIUM : Blocks.GRASS_BLOCK),
                    "Extremis changes grass only when the original draw returns zero");
            helper.assertTrue(level.random.nextInt(1000) == (converts ? 479 : 948),
                    "The grass conversion retains the original following draw");
            level.random.setSeed(seed);
            applyPowder(helper, player, wet, WitcheryIngredient.MUTANDIS_EXTREMIS);
            helper.assertTrue(converts || level.random.nextInt(1000) == 948,
                    "A rejected clay conversion keeps the original following draw");
            for (int index = 0; index < cross.size(); index++) {
                helper.assertTrue(level.getBlockState(cross.get(index)).is(converts ? Blocks.CLAY : soils.get(index)),
                        "The clay cross visits each original wet dirt variant");
            }
        }
        helper.succeed();
    }

    private static void assertRolls(GameTestHelper helper, ServerLevel level, BlockPos pos, BlockState input,
                                    WitcheryIngredient ingredient, List<LegacyRoll> rolls) {
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SeededMutandis"));
        Block ground = input.getBlock() instanceof CropBlock ? Blocks.FARMLAND : Blocks.GRASS_BLOCK;
        level.setBlock(pos.below(), ground.defaultBlockState(), Block.UPDATE_CLIENTS);
        for (var side : Direction.Plane.HORIZONTAL) {
            level.setBlock(pos.relative(side), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        for (var roll : rolls) {
            level.setBlock(pos, input, Block.UPDATE_CLIENTS);
            level.random.setSeed(roll.seed());
            applyPowder(helper, player, pos, ingredient);
            helper.assertTrue(level.getBlockState(pos) == roll.state() && level.random.nextInt(1000) == roll.next(),
                    "Matches the original outcome and following draw for world seed " + roll.seed());
        }
    }

    private record LegacyRoll(long seed, BlockState state, int next) {}

    private static void applyPowder(GameTestHelper helper, FakePlayer player, BlockPos pos, WitcheryIngredient ingredient) {
        var powder = new ItemStack(ingredient.item());
        helper.assertTrue(use(player, pos, powder).consumesAction() && powder.isEmpty(), "The mutation consumes one dose");
    }

    private static void assertMutation(GameTestHelper helper, FakePlayer player, BlockPos pos,
                                       BlockState input, WitcheryIngredient ingredient) {
        helper.setBlock(pos, input);
        var powder = new ItemStack(ingredient.item());
        helper.assertTrue(use(helper, player, pos, powder).consumesAction() && powder.isEmpty()
                && helper.getBlockState(pos) != input, "Mutandis should consume one dose and change " + input);
    }

    private static InteractionResult use(GameTestHelper helper, FakePlayer player, BlockPos relative, ItemStack powder) {
        return use(player, helper.absolutePos(relative), powder);
    }

    private static InteractionResult use(FakePlayer player, BlockPos pos, ItemStack powder) {
        player.setItemInHand(InteractionHand.MAIN_HAND, powder);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
        return powder.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }
}
