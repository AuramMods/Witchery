package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.power.AltarPower;
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
        for (BlockPos part : BlockPos.betweenClosed(corner, last)) {
            placeBlock(helper, player, part, block);
        }
        var core = altar(helper, last);
        helper.assertTrue(altar(helper, corner).resolveCore(level) == core && level.getBlockState(last).getValue(AltarBlock.FORMED),
                "The last placed altar block becomes the core and forms the altar immediately");
        placeNature(helper);
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
        var hit = new BlockHitResult(Vec3.atCenterOf(support).add(0, 0.5, 0), Direction.UP, support, false);
        return new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
    }

    private static AltarBlockEntity altar(GameTestHelper helper, BlockPos pos) {
        return (AltarBlockEntity) helper.getLevel().getBlockEntity(pos);
    }
}
