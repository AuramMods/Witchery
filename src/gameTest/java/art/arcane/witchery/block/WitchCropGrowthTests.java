package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WitchCropGrowthTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_snowbell_harvest", setupTicks = 5)
    public static void snowbellsLimitNeedlesAndIgnoreToolFortuneWhenPlayerHarvests(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 3, 5));
        var crop = (WitchCropBlock) WitchCrop.SNOWBELL.block();
        var miner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SnowbellHarvester"));
        var pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.enchant(Enchantments.BLOCK_FORTUNE, 3);
        miner.getInventory().setItem(0, pick);
        level.setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
        var area = new AABB(pos).inflate(1);
        var needle = WitcheryIngredient.ICY_NEEDLE.item();
        int needles = 0;
        for (int sample = 0; sample < 128; sample++) {
            // Early port worlds can contain ages above the crop's maturity threshold.
            var state = crop.getStateForAge(crop.getMaxAge() + sample % 4);
            level.setBlockAndUpdate(pos, state);
            helper.assertTrue(miner.gameMode.destroyBlock(pos), "The survival player can harvest a mature Snowbell");
            var entities = level.getEntitiesOfClass(ItemEntity.class, area);
            var drops = entities.stream().map(ItemEntity::getItem).toList();
            helper.assertTrue(count(drops, Items.SNOWBALL) == 1 && count(drops, crop.crop().seed()) <= 3,
                    "Every mature Snowbell yields one Snowball and up to three seeds even with a Fortune III tool");
            int needleCount = count(drops, needle);
            helper.assertTrue(needleCount <= 1, "A mature Snowbell can yield at most one Icy Needle");
            needles += needleCount;
            entities.forEach(ItemEntity::discard);
        }
        helper.assertTrue(needles > 0 && pick.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE) == 3,
                "Normal harvests can yield Icy Needles and leave the player's Fortune enchantment intact");
        var immature = Block.getDrops(crop.getStateForAge(3), level, pos, null);
        helper.assertTrue(immature.size() == 1 && count(immature, crop.crop().seed()) == 1,
                "An immature Snowbell drops one seed");

        // Harvesting brew explicitly queries the drops with its bonus, as in the original port target.
        boolean brewBonus = false;
        for (int sample = 0; sample < 128 && !brewBonus; sample++) {
            var brewDrops = Block.getDrops(crop.getStateForAge(crop.getMaxAge()), level, pos, null, null, pick);
            brewBonus = count(brewDrops, crop.crop().seed()) > 3;
        }
        helper.assertTrue(brewBonus, "The explicit Harvesting brew loot query can still return more than three seeds");
        helper.succeed();
    }

    private static int count(List<ItemStack> drops, Item item) {
        return drops.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    @GameTest(template = "qa_coven_arena")
    public static void witchCropsUseOriginalNaturalGrowthRolls(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(6, 4, 5)).atY(196);
        var mindrake = (WitchCropBlock) WitchCrop.MINDRAKE.block();
        var mandrake = (WitchCropBlock) WitchCrop.MANDRAKE.block();
        var artichoke = (WitchCropBlock) WitchCrop.ARTICHOKE.block();
        var hydrated = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);

        setSoils(level, pos, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        assertGrowthRoll(helper, pos, mandrake, 13);
        level.setBlockAndUpdate(pos.below(), Blocks.WATER.defaultBlockState());
        assertGrowthRoll(helper, pos, artichoke, 13);
        setSoils(level, pos, Blocks.WATER.defaultBlockState());
        assertGrowthRoll(helper, pos, artichoke, 7);

        setSoils(level, pos, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        assertGrowthRoll(helper, pos, mindrake, 19);
        level.setBlockAndUpdate(pos.below(), hydrated);
        assertGrowthRoll(helper, pos, mindrake, 10);
        setSoils(level, pos, hydrated);
        assertGrowthRoll(helper, pos, mindrake, 4);
        level.setBlockAndUpdate(pos.south().east(), mindrake.defaultBlockState());
        assertGrowthRoll(helper, pos, mindrake, 8);
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", timeoutTicks = 180)
    public static void witchCropsSurviveDarknessButNeedLightAboveToGrow(GameTestHelper helper) {
        var level = helper.getLevel();
        var dark = helper.absolutePos(new BlockPos(4, 4, 5)).atY(196);
        var lit = dark.east(5);
        var crop = (WitchCropBlock) WitchCrop.MANDRAKE.block();
        for (var pos : List.of(dark, lit)) {
            for (var shell : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 1, 2))) {
                if (!shell.equals(pos)) level.setBlockAndUpdate(shell, Blocks.STONE.defaultBlockState());
            }
            level.setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
        }
        level.setBlockAndUpdate(lit.north(), Blocks.GLOWSTONE.defaultBlockState());

        helper.startSequence().thenWaitUntil(() -> {
            int darkLight = level.getRawBrightness(dark, 0);
            int sideLight = level.getRawBrightness(lit, 0);
            int topLight = level.getRawBrightness(lit.above(), 0);
            helper.assertTrue(darkLight < 8 && sideLight >= 9 && topLight < 9,
                    "Expected darkness and side light below a dark ceiling. Observed "
                            + darkLight + ", " + sideLight + ", " + topLight);
        }).thenExecute(() -> {
            for (var pos : List.of(dark, lit)) {
                level.setBlockAndUpdate(pos, crop.defaultBlockState());
                var random = new GrowthRoll();
                crop.randomTick(level.getBlockState(pos), level, pos, random);
                helper.assertTrue(random.bounds.isEmpty() && crop.getAge(level.getBlockState(pos)) == 0,
                        "A dark upper cell prevents growth even when the crop itself receives side light");
            }
            level.setBlockAndUpdate(dark.above(), Blocks.OBSIDIAN.defaultBlockState());
            helper.assertTrue(level.getBlockState(dark).is(crop),
                    "An ordinary neighbor update leaves the original crop on valid soil in darkness");
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void naturalMindrakeGrowthHonorsForgeDecisions(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(6, 4, 5)).atY(196);
        var crop = (WitchCropBlock) WitchCrop.MINDRAKE.block();
        level.setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
        level.setBlockAndUpdate(pos, crop.defaultBlockState());
        var decisions = new ArrayDeque<>(List.of(Event.Result.DENY, Event.Result.ALLOW));
        Consumer<BlockEvent.CropGrowEvent.Pre> listener = event -> {
            if (event.getLevel() == level && event.getPos().equals(pos)) event.setResult(decisions.removeFirst());
        };

        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            crop.randomTick(level.getBlockState(pos), level, pos, new GrowthRoll());
            helper.assertTrue(crop.getAge(level.getBlockState(pos)) == 0 && decisions.size() == 1,
                    "Forge can veto a successful natural Mindrake roll");
            crop.randomTick(level.getBlockState(pos), level, pos, new GrowthRoll(1));
            helper.assertTrue(crop.getAge(level.getBlockState(pos)) == 1 && decisions.isEmpty(),
                    "Forge can force growth after a missed roll");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void wormwoodGrowthKeepsOriginalPlantSupports(GameTestHelper helper) {
        var level = helper.getLevel();
        var root = helper.absolutePos(new BlockPos(6, 4, 5)).atY(196);
        var wormwood = (WitchCropBlock) WitchCrop.WORMWOOD.block();
        var mandrake = (WitchCropBlock) WitchCrop.MANDRAKE.block();
        level.setBlockAndUpdate(root.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(root, wormwood.getStateForAge(wormwood.getMaxAge()));
        wormwood.randomTick(level.getBlockState(root), level, root, new GrowthRoll());
        helper.assertTrue(level.getBlockState(root.above()).equals(wormwood.defaultBlockState()),
                "Mature Wormwood grows one new tip at stage zero");

        level.setBlockAndUpdate(root.above(), wormwood.getStateForAge(wormwood.getMaxAge()));
        wormwood.randomTick(level.getBlockState(root.above()), level, root.above(), new GrowthRoll());
        helper.assertTrue(level.isEmptyBlock(root.above(2)), "The mature tip leaves Wormwood at two blocks tall");

        level.setBlockAndUpdate(root.above(), mandrake.defaultBlockState());
        helper.assertTrue(level.getBlockState(root.above()).canSurvive(level, root.above()),
                "The original land crops can grow on Wormwood");
        level.setBlockAndUpdate(root, mandrake.getStateForAge(mandrake.getMaxAge()));
        helper.assertTrue(level.getBlockState(root.above()).is(mandrake),
                "The original land crops also stay on a matching crop below");
        helper.succeed();
    }

    private static void setSoils(ServerLevel level, BlockPos pos, BlockState soil) {
        for (var soilPos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, -1, 1))) {
            level.setBlockAndUpdate(soilPos, soil);
        }
    }

    private static void assertGrowthRoll(GameTestHelper helper, BlockPos pos, WitchCropBlock crop, int bound) {
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, crop.defaultBlockState());
        var random = new GrowthRoll();
        crop.randomTick(level.getBlockState(pos), level, pos, random);
        helper.assertTrue(random.bounds.equals(List.of(bound)) && crop.getAge(level.getBlockState(pos)) == 1,
                "Expected one growth roll out of " + bound + " for " + crop.crop() + " but observed " + random.bounds);
    }

    private static final class GrowthRoll extends LegacyRandomSource {
        private final List<Integer> bounds = new ArrayList<>();
        private final int result;

        private GrowthRoll() {
            this(0);
        }

        private GrowthRoll(int result) {
            super(0);
            this.result = result;
        }

        @Override
        public int nextInt(int bound) {
            bounds.add(bound);
            return result;
        }
    }
}
