package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.AnimalMerchant;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import java.util.List;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CritterSnareTests {
    @GameTest(template = "qa_coven_arena")
    public static void stockedLegacySnareReleasesABatWithStock(GameTestHelper helper) throws Exception {
        var player = helper.makeMockSurvivalPlayer();
        var stack = WitcheryIngredient.loadInventoryStack(TagParser.parseTag("{id:\"witchery:crittersnare\",Count:1b,Damage:9s}"));
        var relative = new BlockPos(5, 3, 5);
        helper.setBlock(relative.below(), Blocks.STONE);
        BlockPos pos = helper.absolutePos(relative);
        BlockPos support = pos.below();
        player.moveTo(Vec3.atCenterOf(pos.south(2)));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var hit = new BlockHitResult(Vec3.atCenterOf(support).add(0, 0.5, 0), Direction.UP, support, false);
        helper.assertTrue(stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction(),
                "Place the original stocked Critter Snare");
        var level = helper.getLevel();
        var placed = level.getBlockState(pos);
        helper.assertTrue(placed.getValue(CritterSnareBlock.CRITTER) == CritterSnareBlock.Critter.BAT
                && placed.getValue(CritterSnareBlock.STOCKED), "The captive and its stock survive item migration and placement");
        player.setShiftKeyDown(true);
        helper.useBlock(relative, player);
        var bats = level.getEntitiesOfClass(Bat.class, new AABB(pos).inflate(2));
        helper.assertTrue(bats.size() == 1 && bats.get(0).getPersistentData().contains(AnimalMerchant.STOCK_TAG, Tag.TAG_COMPOUND)
                        && level.getBlockState(pos).getValue(CritterSnareBlock.CRITTER) == CritterSnareBlock.Critter.EMPTY,
                "Sneak-use empties the snare and returns a bat with its trade stock marker");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_critter_capture_parity")
    public static void capturesSubclassesOnPartialSupportAndKeepsVetoedCaptives(GameTestHelper helper) {
        var level = helper.getLevel();
        var relative = new BlockPos(5, 3, 5);
        var pos = helper.absolutePos(relative);
        var snare = WitcheryBlocks.getRequired("crittersnare").get().defaultBlockState();
        var player = helper.makeMockSurvivalPlayer();
        player.moveTo(pos.getX() - 2.5, pos.getY(), pos.getZ() + 2.5);
        player.setShiftKeyDown(true);
        for (var support : List.of(Blocks.STONE_SLAB, Blocks.GLASS, Blocks.OAK_FENCE, Blocks.FARMLAND)) {
            level.setBlockAndUpdate(pos.below(), support.defaultBlockState());
            helper.assertTrue(snare.canSurvive(level, pos), "The original snare accepts " + support);
        }
        level.setBlockAndUpdate(pos.below(), Blocks.WATER.defaultBlockState());
        helper.assertFalse(snare.canSurvive(level, pos), "Water cannot support a snare");
        level.setBlockAndUpdate(pos.below(), Blocks.STONE_SLAB.defaultBlockState());
        level.setBlockAndUpdate(pos, snare);

        var customBat = new Bat(EntityType.BAT, level) {
            @Override public EntityType<?> getType() { return EntityType.PIG; }
        };
        snare.entityInside(level, pos, customBat);
        var occupied = level.getBlockState(pos);
        helper.assertTrue(customBat.isRemoved() && occupied.getValue(CritterSnareBlock.CRITTER) == CritterSnareBlock.Critter.BAT,
                "Capture recognizes the bat class even when its registry type differs");

        var observed = new boolean[1];
        Consumer<EntityJoinLevelEvent> veto = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof Bat bat
                    && bat.distanceToSqr(Vec3.atBottomCenterOf(pos).add(0, 1.5, 0)) < 0.001) {
                observed[0] = level.getBlockState(pos).equals(snare);
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            helper.useBlock(relative, player);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }
        helper.assertTrue(observed[0] && level.getBlockState(pos).equals(occupied),
                "The original release empties the snare before spawning, and Forge spawn vetoes restore the captive");
        level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(level.isEmptyBlock(pos), "Removing partial support removes the snare");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_critter_release_fallback")
    public static void slimeReleaseUsesTheOriginalTwentyAttemptsAndFallbackItems(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 3, 5));
        var lastAttempt = new SizeRolls(20);
        var slime = new Slime(EntityType.SLIME, level) {
            @Override public LegacyRandomSource getRandom() { return lastAttempt; }
        };
        slime.setSize(4, true);
        helper.assertTrue(CritterSnareBlock.resolveSlimeRelease(slime, pos) == slime && slime.isTiny() && lastAttempt.attempts == 20,
                "A tiny result on attempt twenty is released normally");

        var noSlime = new SizeRolls(-1);
        var failedSlime = new Slime(EntityType.SLIME, level) {
            @Override public LegacyRandomSource getRandom() { return noSlime; }
        };
        var noCube = new SizeRolls(-1);
        var failedCube = new MagmaCube(EntityType.MAGMA_CUBE, level) {
            @Override public LegacyRandomSource getRandom() { return noCube; }
        };
        var slimeBall = (ItemEntity) CritterSnareBlock.resolveSlimeRelease(failedSlime, pos);
        var magmaCream = (ItemEntity) CritterSnareBlock.resolveSlimeRelease(failedCube, pos);
        var center = Vec3.atBottomCenterOf(pos).add(0, 1.5, 0);
        helper.assertTrue(noSlime.attempts == 20 && noCube.attempts == 20
                        && slimeBall.getItem().is(Items.SLIME_BALL) && magmaCream.getItem().is(Items.MAGMA_CREAM)
                        && slimeBall.position().equals(center) && magmaCream.position().equals(center)
                        && !slimeBall.hasPickUpDelay() && !magmaCream.hasPickUpDelay(),
                "Twenty misses return the original item at the center above the snare");
        helper.succeed();
    }

    private static final class SizeRolls extends LegacyRandomSource {
        private final int firstTiny;
        private int attempts;

        private SizeRolls(int firstTiny) {
            super(0);
            this.firstTiny = firstTiny;
        }

        @Override public int nextInt(int bound) {
            if (bound != 3) throw new IllegalArgumentException("Unexpected slime size range " + bound);
            return ++attempts == firstTiny ? 0 : 1;
        }
    }
}
