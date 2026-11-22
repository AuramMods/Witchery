package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.HornedHuntsman;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WickerEffigyTests {
    private static final String[] FIGURE = {"..##..", "..##..", ".####.", ".####.", ".####.", "..##..", "..##.."};

    @GameTest(template = "qa_rite_terrain", batch = "witchery_wicker", timeoutTicks = 120)
    public static void wickerEffigiesIgniteInBothAxes(GameTestHelper helper) {
        var player = playerWithFlint(helper);
        var east = new BlockPos(8, 14, 8);
        var south = new BlockPos(24, 14, 23);
        var huntsmen = List.of(ignite(helper, player, east, Direction.EAST), ignite(helper, player, south, Direction.SOUTH));
        helper.succeedWhen(() -> {
            for (var huntsman : huntsmen) {
                helper.assertTrue(helper.getLevel().getEntity(huntsman.getUUID()) == huntsman,
                        "The spawned Huntsman should be visible once its chunk is tracked");
            }
            huntsmen.forEach(HornedHuntsman::discard);
        });
    }

    @GameTest(template = "qa_rite_terrain", batch = "witchery_wicker")
    public static void wickerClicksKeepOriginalAlignmentRules(GameTestHelper helper) {
        var player = playerWithFlint(helper);
        var base = new BlockPos(8, 14, 8);
        var block = WitcheryBlocks.getRequired("wickerbundle").get();
        buildEffigy(helper, base, Direction.EAST);
        var outerArm = base.west().above(4);
        helper.setBlock(outerArm.north(), block.defaultBlockState());
        helper.assertTrue(use(helper, player, outerArm) == InteractionResult.PASS,
                "Even a plain bundle on the other axis prevents lighting the complete effigy from that arm");
        helper.assertBlockPresent(block, base);

        helper.setBlock(base.above(6), block.defaultBlockState());
        helper.assertTrue(use(helper, player, base) == InteractionResult.CONSUME,
                "An aligned figure with a plain head handles the click without igniting");
        helper.assertBlockPresent(block, base);

        helper.setBlock(base.north(), block.defaultBlockState());
        helper.assertTrue(use(helper, player, base) == InteractionResult.PASS,
                "Wicker neighbors on both axes leave the Flint and Steel interaction to vanilla");
        helper.assertBlockPresent(block, base);
        helper.succeed();
    }

    private static HornedHuntsman ignite(GameTestHelper helper, Player player, BlockPos base, Direction axis) {
        buildEffigy(helper, base, axis);
        var uncheckedLeg = base.relative(axis).above();
        helper.setBlock(uncheckedLeg, Blocks.STONE);
        var level = helper.getLevel();
        var foot = helper.absolutePos(base);
        var area = new AABB(foot).inflate(4);
        var summons = new ArrayList<HornedHuntsman>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof HornedHuntsman huntsman
                    && area.contains(huntsman.position())) summons.add(huntsman);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            var clicked = axis == Direction.EAST ? base.west().above(4) : base.relative(axis);
            helper.assertTrue(use(helper, player, clicked) == InteractionResult.CONSUME,
                    "A valid effigy lights when the clicked bundle leads to its feet in " + axis);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
        helper.assertTrue(summons.size() == 1, "Exactly one Huntsman should join when lighting the effigy in " + axis);
        var huntsman = summons.get(0);
        var saved = huntsman.saveWithoutId(new CompoundTag());
        var spawn = Vec3.atBottomCenterOf(foot).add(axis.getStepX() * 0.5, 0.1, axis.getStepZ() * 0.5);
        helper.assertTrue(huntsman.position().distanceToSqr(spawn) < 0.00001 && huntsman.getHealth() == 100
                        && saved.getInt("Invul") == 150 && !saved.getBoolean("explosiveEntrance")
                        && !saved.getBoolean("PlayerCreated"),
                "The Huntsman enters at the feet with the original health and 150 ticks, without an explosion");
        var block = WitcheryBlocks.getRequired("wickerbundle").get();
        helper.assertBlockNotPresent(block, base);
        helper.assertBlockNotPresent(Blocks.STONE, uncheckedLeg);
        helper.assertBlockPresent(block, base.above(5));
        return huntsman;
    }

    private static void buildEffigy(GameTestHelper helper, BlockPos base, Direction axis) {
        var block = WitcheryBlocks.getRequired("wickerbundle").get();
        for (int y = 0; y < FIGURE.length; y++) {
            for (int column = 0; column < FIGURE[y].length(); column++) {
                if (FIGURE[y].charAt(column) == '#') {
                    helper.setBlock(base.above(y).relative(axis, column - 2), block.defaultBlockState()
                            .setValue(WickerBundleBlock.BUNDLE_TYPE, WickerBundleBlock.Type.BLOODIED)
                            .setValue(RotatedPillarBlock.AXIS, y == 4 ? axis.getAxis() : Direction.Axis.Y));
                }
            }
        }
        helper.setBlock(base.below(), Blocks.STONE);
        helper.setBlock(base.relative(axis).below(), Blocks.STONE);
    }

    private static Player playerWithFlint(GameTestHelper helper) {
        var player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
        return player;
    }

    private static InteractionResult use(GameTestHelper helper, Player player, BlockPos pos) {
        var clicked = helper.absolutePos(pos);
        var hit = new BlockHitResult(Vec3.atCenterOf(clicked), Direction.NORTH, clicked, false);
        return helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
    }
}
