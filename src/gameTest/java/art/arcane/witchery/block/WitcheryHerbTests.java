package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WitcheryHerbTests {
    @GameTest(template = "qa_coven_arena")
    public static void mossAndGlintWeedSpreadAndKeepTheirOriginalHarvestAndLight(GameTestHelper helper) {
        var level = helper.getLevel();
        Block glint = WitcheryBlocks.getRequired("glintweed").get();
        Block ember = WitcheryBlocks.getRequired("embermoss").get();
        BlockPos glintPos = helper.absolutePos(new BlockPos(5, 4, 5));
        BlockPos emberPos = helper.absolutePos(new BlockPos(18, 4, 5));
        fillGround(helper, glintPos, Blocks.STONE.defaultBlockState());
        fillGround(helper, emberPos, Blocks.COARSE_DIRT.defaultBlockState());
        level.setBlockAndUpdate(glintPos, glint.defaultBlockState());
        level.setBlockAndUpdate(emberPos, ember.defaultBlockState());

        var random = RandomSource.create(140);
        for (int attempt = 0; attempt < 256; attempt++) {
            level.getBlockState(glintPos).randomTick(level, glintPos, random);
            level.getBlockState(emberPos).randomTick(level, emberPos, random);
        }
        helper.assertTrue(countNearby(helper, glintPos, glint) == 1 && glint.defaultBlockState().canSurvive(level, glintPos),
                "Glint Weed survives on stone without spreading there");
        helper.assertTrue(countNearby(helper, emberPos, ember) == 5, "Ember Moss spreads on soil and stops at five nearby plants");
        fillGround(helper, glintPos, Blocks.RED_SAND.defaultBlockState());
        for (int attempt = 0; attempt < 256; attempt++) level.getBlockState(glintPos).randomTick(level, glintPos, random);
        helper.assertTrue(countNearby(helper, glintPos, glint) == 5, "Glint Weed also spreads on red sand");
        helper.assertTrue(glint.defaultBlockState().getLightEmission() == 14 && ember.defaultBlockState().getLightEmission() == 6,
                "Both plants emit the original light levels");
        var hand = Block.getDrops(ember.defaultBlockState(), level, emberPos, null, null, ItemStack.EMPTY);
        var sheared = Block.getDrops(ember.defaultBlockState(), level, emberPos, null, null, new ItemStack(Items.SHEARS));
        helper.assertTrue(hand.isEmpty() && sheared.size() == 1 && sheared.get(0).is(ember.asItem()),
                "Ember Moss harvest requires shears");
        helper.assertTrue(Block.getDrops(glint.defaultBlockState(), level, glintPos, null).get(0).is(glint.asItem()),
                "Glint Weed can be picked without shears");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void herbsHonorTheOriginalSupportAndSpreadRolls(GameTestHelper helper) {
        var level = helper.getLevel();
        var glint = WitcheryBlocks.getRequired("glintweed").get().defaultBlockState();
        var ember = WitcheryBlocks.getRequired("embermoss").get().defaultBlockState();
        BlockPos hanging = helper.absolutePos(new BlockPos(5, 8, 5));
        level.setBlockAndUpdate(hanging.below(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(hanging.above(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(glint.canSurvive(level, hanging) && !ember.canSurvive(level, hanging),
                "Only Glint Weed can hang from a ceiling without a floor");
        level.setBlockAndUpdate(hanging, glint);
        level.setBlockAndUpdate(hanging.above(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(level.isEmptyBlock(hanging), "Removing the ceiling immediately removes the unsupported plant");

        var herbs = new BlockState[]{glint, ember};
        for (int i = 0; i < herbs.length; i++) {
            BlockPos origin = helper.absolutePos(new BlockPos(6 + i * 12, 5, 6));
            fillGround(helper, origin, Blocks.DIRT.defaultBlockState());
            level.setBlockAndUpdate(origin, herbs[i]);
            var offsets = RandomSource.create(42);
            level.random.setSeed(42);
            herbs[i].randomTick(level, origin, offsets);
            helper.assertTrue(countNearby(helper, origin, herbs[i].getBlock()) == 1
                            && level.random.nextInt(1000) == 763 && offsets.nextInt(1000) == 130,
                    "A failed world roll never consumes an offset");

            offsets.setSeed(42);
            level.random.setSeed(0);
            herbs[i].randomTick(level, origin, offsets);
            helper.assertTrue(level.getBlockState(origin.offset(-2, 0, 2)).is(herbs[i].getBlock())
                            && level.random.nextInt(1000) == 948 && offsets.nextInt(1000) == 400,
                    "The same successful rolls reach the same dirt cell as Witchery 0.24.1");
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void emberMossIgnitesLivingContacts(GameTestHelper helper) {
        var level = helper.getLevel();
        var ember = WitcheryBlocks.getRequired("embermoss").get();
        BlockPos relative = new BlockPos(5, 3, 5);
        helper.setBlock(relative.below(), Blocks.STONE);
        helper.setBlock(relative, ember);
        var pig = helper.spawn(EntityType.PIG, relative.above());
        var creative = helper.makeMockPlayer();
        BlockPos pos = helper.absolutePos(relative);
        ember.defaultBlockState().entityInside(level, pos, creative);
        helper.assertFalse(creative.isOnFire(), "Creative players are exempt from contact fire");
        helper.succeedWhen(() -> helper.assertTrue(pig.isOnFire() && pig.getRemainingFireTicks() <= 60,
                "A pig in contact gets the original brief fire"));
    }

    @GameTest(template = "qa_coven_arena")
    public static void spanishMossAttachesGrowsAndShearsWithoutBeingClimbable(GameTestHelper helper) {
        var level = helper.getLevel();
        var moss = (SpanishMossBlock) WitcheryBlocks.getRequired("spanishmoss").get();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "MossPlacement"));
        BlockPos wall = helper.absolutePos(new BlockPos(9, 5, 5));
        level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        BlockPos hanger = wall.west();
        placeOn(helper, player, new ItemStack(moss), wall, Direction.WEST);
        BlockState attached = level.getBlockState(hanger);
        helper.assertTrue(attached.is(moss) && attached.getValue(VineBlock.EAST) && !attached.getValue(VineBlock.UP),
                "A wall placement keeps the side it clings to");
        helper.assertFalse(moss.isLadder(attached, level, hanger, player), "Spanish Moss is not climbable");
        helper.assertTrue(moss.getFlammability(attached, level, hanger, Direction.EAST) == 100
                        && moss.getFireSpreadSpeed(attached, level, hanger, Direction.EAST) == 15,
                "Its original fire spread values are retained");

        BlockPos ceiling = wall.east(6);
        level.setBlockAndUpdate(ceiling, Blocks.STONE.defaultBlockState());
        placeOn(helper, player, new ItemStack(moss), ceiling, Direction.DOWN);
        helper.assertTrue(level.getBlockState(ceiling.below()).getValue(VineBlock.UP),
                "Moss also attaches to a ceiling");
        helper.assertTrue(moss.mutatedState(level, hanger).getValue(VineBlock.EAST),
                "Mutandis uses the real wall when it turns a nearby plant into moss");
        BlockPos placeholder = wall.east(12).below();
        level.setBlockAndUpdate(placeholder, moss.defaultBlockState());
        level.setBlockAndUpdate(placeholder.west(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(level.getBlockState(placeholder).getValue(VineBlock.WEST),
                "A saved decorative Spanish Moss can acquire a real attachment");

        var random = RandomSource.create(910);
        for (int attempt = 0; attempt < 256 && !level.getBlockState(hanger.below()).is(moss); attempt++) {
            level.getBlockState(hanger).randomTick(level, hanger, random);
        }
        BlockState lower = level.getBlockState(hanger.below());
        helper.assertTrue(lower.is(moss) && lower.getValue(VineBlock.EAST),
                "The vine can hang downward without a side support of its own");
        var hand = Block.getDrops(lower, level, hanger.below(), null, null, ItemStack.EMPTY);
        var sheared = Block.getDrops(lower, level, hanger.below(), null, null, new ItemStack(Items.SHEARS));
        helper.assertTrue(hand.isEmpty() && sheared.size() == 1 && sheared.get(0).is(moss.asItem()),
                "The hanging strand drops one Spanish Moss with shears");
        level.setBlockAndUpdate(wall, Blocks.AIR.defaultBlockState());
        helper.assertTrue(level.isEmptyBlock(hanger) && level.isEmptyBlock(hanger.below()),
                "Removing the support also removes its hanging strand");
        helper.succeed();
    }

    private static void fillGround(GameTestHelper helper, BlockPos origin, BlockState ground) {
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-4, -1, -4), origin.offset(4, -1, 4))) {
            helper.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(pos, ground);
        }
    }

    private static int countNearby(GameTestHelper helper, BlockPos origin, Block plant) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-4, -1, -4), origin.offset(4, 1, 4))) {
            if (helper.getLevel().getBlockState(pos).is(plant)) count++;
        }
        return count;
    }

    private static void placeOn(GameTestHelper helper, FakePlayer player, ItemStack stack, BlockPos support, Direction face) {
        var hit = new BlockHitResult(Vec3.atCenterOf(support).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5)),
                face, support, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.assertTrue(stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction(),
                "Place Spanish Moss on the selected support");
    }
}
