package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.BrazierBlockEntity;
import art.arcane.witchery.block.entity.CrystalBallBlockEntity;
import art.arcane.witchery.crafting.BrazierRecipe;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.prediction.Predictions;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class AltarCoreTests {
    private static final BlockPos CORNER = new BlockPos(4, 4, 4);

    @GameTest(template = "qa_coven_arena", batch = "witchery_altar_core")
    public static void importedAltarKeepsChargedCoreAndItsRange(GameTestHelper helper) {
        var level = helper.getLevel();
        var block = WitcheryBlocks.getRequired("altar").get();
        var corner = helper.absolutePos(CORNER);
        var savedCore = corner.offset(2, 0, 1);
        for (BlockPos part : BlockPos.betweenClosed(corner, savedCore)) {
            level.setBlockAndUpdate(part, block.defaultBlockState().setValue(AltarBlock.FORMED, true));
            var imported = new CompoundTag();
            imported.putInt("CoreX", savedCore.getX());
            imported.putInt("CoreY", savedCore.getY());
            imported.putInt("CoreZ", savedCore.getZ());
            if (part.equals(savedCore)) {
                imported.putFloat("Power", 110.5F);
                imported.putFloat("MaxPower", 250.0F);
                imported.putInt("PowerScale", 1);
                imported.putInt("RechargeScale", 1);
            }
            altar(helper, part).load(imported);
        }
        placeNature(helper);
        var core = altar(helper, savedCore);
        helper.assertTrue(altar(helper, corner).resolveCore(level) == core && core.power() == 110.5F,
                "The original charged tile resolves from the opposite corner immediately after loading");
        helper.assertTrue(AltarPower.nearest(level, savedCore.east(16)) == core,
                "The saved core powers the original boundary of its range");
        helper.runAfterDelay(25, () -> {
            var reloaded = core.saveWithFullMetadata();
            core.load(reloaded);
            helper.assertTrue(core.isCore(level) && core.power() >= 120 && altar(helper, corner).power() == 0
                            && reloaded.getInt("CoreX") == savedCore.getX() && reloaded.getInt("CoreZ") == savedCore.getZ(),
                    "Only the original core recharges and remains authoritative across its modern save round trip");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_altar_placement")
    public static void finalPlacedBlockFormsCoreAndBreakingDischargesIt(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "AltarBuilder"));
        var block = WitcheryBlocks.getRequired("altar").get();
        var corner = helper.absolutePos(CORNER);
        var last = corner.offset(2, 0, 1);
        placeNature(helper);
        for (BlockPos part : BlockPos.betweenClosed(corner, last)) {
            placeBlock(helper, player, part, block);
        }
        var core = altar(helper, last);
        helper.assertTrue(altar(helper, corner).resolveCore(level) == core && level.getBlockState(last).getValue(AltarBlock.FORMED),
                "The last placed altar block becomes the core and forms the altar immediately");
        useArtifact(helper, player, corner, new ItemStack(WitcheryItems.getRequired("mysticbranch").get()));
        useArtifact(helper, player, corner.south(), new ItemStack(WitcheryIngredient.KOBOLDITE_PENTACLE.item()));

        helper.runAfterDelay(25, () -> {
            helper.assertTrue(core.power() > 0 && core.rechargeMultiplier() == 2
                            && core.saveWithFullMetadata().getInt("EnhancementLevel") == 1,
                    "The mounted Branch and Pentacle update the last-placed core");
            level.removeBlock(corner.above(), false);
        });
        helper.runAfterDelay(50, () -> {
            helper.assertTrue(core.saveWithFullMetadata().getInt("EnhancementLevel") == 0,
                    "Removing the Mystic Branch clears its enhancement");
            level.removeBlock(corner, false);
            helper.assertTrue(altar(helper, last).resolveCore(level) == null && core.power() == 0
                            && !level.getBlockState(last).getValue(AltarBlock.FORMED),
                    "Breaking a member immediately discharges the linked core");
            placeBlock(helper, player, corner, block);
            helper.assertTrue(altar(helper, last).resolveCore(level) == altar(helper, corner),
                    "Completing the repaired altar makes the newly placed member its core");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_altar_scan_timing")
    public static void formationAndInspectionSampleNatureWhileLoadedTicksRecharge(GameTestHelper helper) {
        var level = helper.getLevel();
        var corePos = helper.absolutePos(CORNER).atY(level.getMaxBuildHeight() - 4);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "AltarInspection"));
        var altarBlock = WitcheryBlocks.getRequired("altar").get();
        var egg = corePos.north(3);
        level.setBlockAndUpdate(egg.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(egg, Blocks.DRAGON_EGG.defaultBlockState());
        for (BlockPos part : BlockPos.betweenClosed(corePos.offset(-2, 0, -1), corePos)) {
            placeBlock(helper, player, part, altarBlock);
        }
        var core = altar(helper, corePos);
        helper.assertTrue(core.capacity() == 250 && core.power() == 0,
                "The last placement samples nature before the first recharge");
        tickAltar(helper, core, 19);
        helper.assertTrue(core.power() == 0, "Recharge waits for loaded tick 20");
        tickAltar(helper, core, 1);
        helper.assertTrue(core.power() == 10, "The twentieth loaded tick adds ten power");

        level.setBlockAndUpdate(egg.north(), Blocks.OAK_LOG.defaultBlockState());
        tickAltar(helper, core, 80);
        var otherMember = corePos.west(2);
        useBlock(player, otherMember, ItemStack.EMPTY);
        helper.assertTrue(core.capacity() == 250 && core.power() == 50,
                "Opening on the hundredth loaded tick still uses the previous scan");
        tickAltar(helper, core, 1);
        useBlock(player, otherMember, ItemStack.EMPTY);
        helper.assertTrue(core.capacity() == 252 && core.power() == 50,
                "Opening a different member after the throttle refreshes the core");

        level.removeBlock(egg, false);
        tickAltar(helper, core, 119);
        helper.assertTrue(core.capacity() == 252 && core.power() == 110,
                "An unattended altar keeps its capacity and continues recharging");
        useBlock(player, corePos, ItemStack.EMPTY);
        helper.assertTrue(core.capacity() == 2 && core.power() == 110,
                "Inspection lowers capacity immediately and leaves stored charge until recharge");
        tickAltar(helper, core, 20);
        helper.assertTrue(core.power() == 2, "The next recharge clamps the stored power to the new capacity");
        helper.succeed();
    }

    @GameTest(template = "qa_rite_terrain", batch = "witchery_arthana_consumers", timeoutTicks = 180)
    public static void brazierAndCrystalBallHonorMountedArthana(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "FortuneTellerQA"));
        var corner = helper.absolutePos(new BlockPos(1, 4, 14));
        var corePos = corner.offset(2, 0, 1);
        var block = WitcheryBlocks.getRequired("altar").get();
        for (BlockPos part : BlockPos.betweenClosed(corner, corePos)) placeBlock(helper, player, part, block);
        var core = altar(helper, corePos);
        level.setBlockAndUpdate(corePos.above(), WitcheryBlocks.getRequired("infinityegg").get().defaultBlockState());
        level.setBlockAndUpdate(corePos.south(3).below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(corePos.south(3), Blocks.DRAGON_EGG.defaultBlockState());
        var brazierPos = corePos.east(32);
        var ballPos = corePos.east(31);
        var outside = corePos.east(33);
        var ball = placeCrystalBall(helper, ballPos);
        var distantBall = placeCrystalBall(helper, outside);
        var brazierBlock = WitcheryBlocks.getRequired("brazier").get();
        level.setBlockAndUpdate(brazierPos, brazierBlock.defaultBlockState());
        var brazier = (BrazierBlockEntity) level.getBlockEntity(brazierPos);
        for (var ingredient : BrazierRecipe.MIST.ingredients()) {
            useBlock(player, brazierPos, new ItemStack(ingredient));
        }
        useBlock(player, brazierPos, new ItemStack(Items.FLINT_AND_STEEL));
        tickBrazier(helper, brazier);
        Predictions.attune(player);
        useBlock(player, ballPos, ItemStack.EMPTY);
        helper.assertTrue(ball.ready(level.getGameTime()) && brazier.saveWithoutMetadata().getLong("CookTime") == 0,
                "The unmodified altar cannot power the two blocks at this distance");

        useArtifact(helper, player, corner, new ItemStack(WitcheryItems.getRequired("arthana").get()));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(core.isInRange(brazierPos),
                "The mounted blade extends the altar's reach to 32 blocks")).thenExecute(() -> {
            // The fixture supplies stored charge so the check does not wait for the altar to refill.
            var charged = core.saveWithoutMetadata();
            charged.putFloat("Power", 1_000);
            core.load(charged);
            useBlock(player, outside, ItemStack.EMPTY);
            helper.assertTrue(distantBall.ready(level.getGameTime()) && core.power() == 1_000,
                    "A Crystal Ball 33 blocks away cannot draw power");
            useBlock(player, ballPos, ItemStack.EMPTY);
            helper.assertTrue(!ball.ready(level.getGameTime()) && core.power() == 500,
                    "A trained reader starts the cooldown and draws 500 power from the extended altar");
        }).thenWaitUntil(() -> {
            // The range fixture can place the Brazier outside the test's ticking chunks.
            if (brazier.saveWithoutMetadata().getLong("CookTime") == 0) tickBrazier(helper, brazier);
            helper.assertTrue(brazier.saveWithoutMetadata().getLong("CookTime") > 0,
                    "The loaded Brazier begins burning at the 32-block boundary");
        }).thenExecute(() ->
                level.removeBlock(corner.above(), false)
        ).thenWaitUntil(() -> helper.assertTrue(!core.isInRange(brazierPos), "The altar rescans the removed blade"))
                .thenExecute(() -> {
                    long progress = brazier.saveWithoutMetadata().getLong("CookTime");
                    tickBrazier(helper, brazier);
                    var paused = brazier.saveWithoutMetadata();
                    helper.assertTrue(paused.getLong("CookTime") == progress && paused.getInt("PowerLevel") == 0,
                            "The Brazier stops drawing from its cached altar as soon as range shrinks");
                }).thenSucceed();
    }

    private static CrystalBallBlockEntity placeCrystalBall(GameTestHelper helper, BlockPos pos) {
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("crystalball").get().defaultBlockState());
        var ball = (CrystalBallBlockEntity) level.getBlockEntity(pos);
        ball.readAt(level.getGameTime() - 101);
        return ball;
    }

    private static void useBlock(FakePlayer player, BlockPos pos, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.level().getBlockState(pos).use(player.level(), player, InteractionHand.MAIN_HAND, hit(pos));
    }

    private static void tickBrazier(GameTestHelper helper, BrazierBlockEntity brazier) {
        BrazierBlockEntity.serverTick(helper.getLevel(), brazier.getBlockPos(), brazier.getBlockState(), brazier);
    }

    private static void tickAltar(GameTestHelper helper, AltarBlockEntity core, int count) {
        for (int tick = 0; tick < count; tick++) {
            AltarBlockEntity.serverTick(helper.getLevel(), core.getBlockPos(), core.getBlockState(), core);
        }
    }

    private static void placeNature(GameTestHelper helper) {
        BlockPos pos = CORNER.west(2);
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, Blocks.DRAGON_EGG);
    }

    private static void placeBlock(GameTestHelper helper, FakePlayer player, BlockPos part, Block block) {
        helper.getLevel().setBlockAndUpdate(part.below(), Blocks.STONE.defaultBlockState());
        var stack = new ItemStack(block);
        var context = context(player, part.below(), stack);
        helper.assertTrue(stack.useOn(context).consumesAction() && stack.isEmpty(), "A survival placement consumes one altar block");
    }

    private static void useArtifact(GameTestHelper helper, FakePlayer player, BlockPos part, ItemStack stack) {
        var context = context(player, part, stack);
        helper.assertTrue(stack.getItem().onItemUseFirst(stack, context).consumesAction() && stack.isEmpty(),
                "An altar artifact is mounted through its item interaction");
    }

    private static UseOnContext context(FakePlayer player, BlockPos support, ItemStack item) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        return new UseOnContext(player, InteractionHand.MAIN_HAND, hit(support));
    }

    private static BlockHitResult hit(BlockPos support) {
        return new BlockHitResult(Vec3.atCenterOf(support).add(0, 0.5, 0), Direction.UP, support, false);
    }

    private static AltarBlockEntity altar(GameTestHelper helper, BlockPos pos) {
        return (AltarBlockEntity) helper.getLevel().getBlockEntity(pos);
    }
}
