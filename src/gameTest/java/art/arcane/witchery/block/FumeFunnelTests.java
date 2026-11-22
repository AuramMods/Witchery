package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.OptionalInt;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class FumeFunnelTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_fume_funnels")
    public static void funnelsOpenTheirOvenInLegacyOrder(GameTestHelper helper) {
        var level = helper.getLevel();
        var opened = new MenuProvider[1];
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "FumeFunnelTester")) {
            @Override
            public OptionalInt openMenu(MenuProvider provider) {
                opened[0] = provider;
                return OptionalInt.of(1);
            }
        };
        Block idle = WitcheryBlocks.getRequired("witchesovenidle").get();
        Block burning = WitcheryBlocks.getRequired("witchesovenburning").get();
        for (boolean filtered : new boolean[]{false, true}) {
            var funnel = WitcheryBlocks.getRequired(filtered ? "filteredfumefunnel" : "fumefunnel").get();
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                BlockPos pos = new BlockPos(6, 5, 6);
                Direction first = facing.getAxis() == Direction.Axis.Z ? Direction.EAST : Direction.SOUTH;
                var state = funnel.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
                helper.setBlock(pos, state);
                helper.setBlock(pos.relative(first), burning);
                helper.setBlock(pos.relative(first.getOpposite()), idle);
                helper.setBlock(pos.below(), idle);
                helper.setBlock(pos.relative(facing), idle);

                BlockPos actual = helper.absolutePos(pos);
                var hit = new BlockHitResult(Vec3.atCenterOf(actual).add(0, 0.5, 0), Direction.UP, actual, false);
                for (BlockPos target : new BlockPos[]{pos.relative(first), pos.relative(first.getOpposite()), pos.below()}) {
                    opened[0] = null;
                    state.use(level, player, InteractionHand.MAIN_HAND, hit);
                    helper.assertTrue(opened[0] != null && opened[0] == helper.getBlockEntity(target),
                            "The funnel should open the next oven in legacy order " + facing + " at " + target);
                    helper.setBlock(target, Blocks.AIR);
                }
                opened[0] = null;
                helper.assertTrue(state.use(level, player, InteractionHand.MAIN_HAND, hit).consumesAction()
                                && opened[0] == null,
                        "The funnel should consume the click without opening an oven along its facing");
                helper.setBlock(pos.relative(facing), Blocks.AIR);
            }
        }
        helper.succeed();
    }
}
