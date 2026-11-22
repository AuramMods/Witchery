package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
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

    private static void assertMutation(GameTestHelper helper, FakePlayer player, BlockPos pos,
                                       BlockState input, WitcheryIngredient ingredient) {
        helper.setBlock(pos, input);
        var powder = new ItemStack(ingredient.item());
        helper.assertTrue(use(helper, player, pos, powder).consumesAction() && powder.isEmpty()
                && helper.getBlockState(pos) != input, "Mutandis should consume one dose and change " + input);
    }

    private static InteractionResult use(GameTestHelper helper, FakePlayer player, BlockPos relative, ItemStack powder) {
        BlockPos pos = helper.absolutePos(relative);
        player.setItemInHand(InteractionHand.MAIN_HAND, powder);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
        return powder.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }
}
