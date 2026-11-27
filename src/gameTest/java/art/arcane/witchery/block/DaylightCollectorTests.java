package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DaylightCollectorTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_daylight_detector_order")
    public static void readsTheFirstNormalDetectorOnEveryNeighborUpdate(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 2, 4));
        var directions = List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
        var partial = collector().setValue(DaylightCollectorBlock.CHARGE, 2);
        for (int first = 0; first < directions.size(); first++) {
            for (int second = first + 1; second < directions.size(); second++) {
                var primary = pos.relative(directions.get(first));
                var secondary = pos.relative(directions.get(second));
                for (boolean primaryMatches : List.of(false, true)) {
                    for (Direction side : directions) level.setBlock(pos.relative(side), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.setBlock(pos, partial, Block.UPDATE_CLIENTS);
                    level.setBlock(primary, detector(primaryMatches ? 3 : 2), Block.UPDATE_CLIENTS);
                    level.setBlockAndUpdate(secondary, detector(primaryMatches ? 8 : 3));
                    helper.assertTrue(level.getBlockState(pos).getValue(DaylightCollectorBlock.CHARGE) == (primaryMatches ? 3 : 2),
                            directions.get(first) + " controls the collector when " + directions.get(second) + " sends an update");
                }
            }
        }

        level.setBlock(pos, partial, Block.UPDATE_CLIENTS);
        for (Direction side : directions) level.setBlock(pos.relative(side), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos.north(), detector(3).setValue(DaylightDetectorBlock.INVERTED, true), Block.UPDATE_CLIENTS);
        level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(level.getBlockState(pos).equals(partial), "An inverted detector cannot advance the sphere");
        level.setBlock(pos.south(), detector(3), Block.UPDATE_CLIENTS);
        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(level.getBlockState(pos).getValue(DaylightCollectorBlock.CHARGE) == 3,
                "A vertical block update still reads the first normal horizontal detector");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_daylight_collector_serving")
    public static void consumesOneSphereAndReturnsAnImmediatelyCollectibleItem(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 2, 4));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(pos),
                "The collector chunk can track its outputs")).thenExecute(() -> {
            var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "CollectorUser"));
            player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 2.5);
            var hit = new BlockHitResult(Vec3.atBottomCenterOf(pos).add(0, 0.8, 0), Direction.UP, pos, false);
            var spheres = new ItemStack(WitcheryIngredient.QUARTZ_SPHERE.item(), 2);
            player.getAbilities().instabuild = true;
            player.setItemInHand(InteractionHand.MAIN_HAND, spheres);
            level.setBlockAndUpdate(pos, collector());
            helper.assertTrue(player.gameMode.useItemOn(player, level, spheres, InteractionHand.MAIN_HAND, hit).consumesAction(),
                    "Using the collector in Creative accepts the sphere");
            helper.assertTrue(spheres.getCount() == 1 && level.getBlockState(pos).getValue(DaylightCollectorBlock.CHARGE) == 1,
                    "The original insertion consumes one sphere even in Creative");

            player.getAbilities().instabuild = false;
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            for (int charge : List.of(7, 15)) {
                level.setBlockAndUpdate(pos, collector().setValue(DaylightCollectorBlock.CHARGE, charge));
                player.gameMode.useItemOn(player, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
                var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4));
                var expected = charge == 15 ? WitcheryItems.getRequired("sungrenade").get() : WitcheryIngredient.QUARTZ_SPHERE.item();
                helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().is(expected) && drops.get(0).getItem().getCount() == 1,
                        "Charge " + charge + " gives one original output");
                ItemEntity drop = drops.get(0);
                helper.assertTrue(drop.getY() == player.getY() + 1 && !drop.hasPickUpDelay()
                                && level.getBlockState(pos).getValue(DaylightCollectorBlock.CHARGE) == 0,
                        "The output starts one block above the user with no pickup delay and the stand empties");
                drop.discard();
            }
        }).thenSucceed();
    }

    private static BlockState collector() {
        return WitcheryBlocks.getRequired("daylightcollector").get().defaultBlockState();
    }

    private static BlockState detector(int power) {
        return Blocks.DAYLIGHT_DETECTOR.defaultBlockState().setValue(DaylightDetectorBlock.POWER, power);
    }
}
