package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchLogBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DuplicationStaffTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_duplication_staff", setupTicks = 5)
    public static void duplicationStaffCopiesAndRotatesTheInterior(GameTestHelper helper) {
        var level = helper.getLevel();
        var first = helper.absolutePos(new BlockPos(2, 12, 2));
        var second = first.offset(4, 4, 3);
        var source = first.offset(1, 1, 1);
        var player = player(helper);
        var staff = new ItemStack(WitcheryItems.getRequired("dupstaff").get());
        staff.getOrCreateTag().putBoolean("Keepsake", true);
        try {
            placeMarker(helper, player, staff, second);
            helper.assertTrue(staff.getMaxStackSize() == 1 && staff.getRarity() == Rarity.EPIC
                    && staff.getTag().getCompound("Marker").getInt("posX") == second.getX(),
                    "The creative staff stores the first clicked corner");
            placeMarker(helper, player, staff, second.west());
            helper.assertTrue(staff.getTag().contains("Marker") && !staff.getTag().contains("SavedSchematic"),
                    "A selection with an empty dimension keeps its corner");
            level.setBlockAndUpdate(source, Blocks.OAK_LOG.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            level.setBlockAndUpdate(source.offset(2, 0, 1), Blocks.GOLD_BLOCK.defaultBlockState());
            var chestPos = source.offset(1, 1, 1);
            level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));
            var chest = (Container) level.getBlockEntity(chestPos);
            chest.setItem(0, new ItemStack(Items.DIAMOND, 3));
            placeMarker(helper, player, staff, first);
            var saved = staff.getTag().getCompound("SavedSchematic");
            helper.assertTrue(!staff.getTag().contains("Marker") && saved.getInt("xMax") == 3
                    && saved.getInt("yMax") == 3 && saved.getInt("zMax") == 2,
                    "Capture excludes all marker planes and works with reversed corners");

            BlockPos[] logOffsets = {new BlockPos(0, 0, 2), new BlockPos(2, 0, 1), new BlockPos(1, 0, 0), BlockPos.ZERO};
            BlockPos[] farOffsets = {new BlockPos(1, 0, 0), BlockPos.ZERO, new BlockPos(0, 0, 2), new BlockPos(2, 0, 1)};
            for (int i = 0; i < 4; i++) {
                BlockPos click = first.offset(7 + i * 5, 0, 1);
                BlockPos base = click.above();
                var log = base.offset(logOffsets[i]);
                level.setBlockAndUpdate(log.above(), Blocks.OBSIDIAN.defaultBlockState());
                placeMarker(helper, player, staff, click);
                var state = level.getBlockState(log);
                helper.assertTrue(state.is(Blocks.OAK_LOG) && state.getValue(RotatedPillarBlock.AXIS)
                                == ((i & 1) == 0 ? Direction.Axis.Z : Direction.Axis.X)
                                && level.getBlockState(base.offset(farOffsets[i])).is(Blocks.GOLD_BLOCK)
                                && level.isEmptyBlock(log.above()) && staff.getTag().getInt("Rotation") == ((i + 1) & 3),
                        "The placed pattern, axis and air match rotation " + ((i + 1) & 3));
            }
            placeMarker(helper, player, staff, first.offset(7, 0, 6));
            player.setShiftKeyDown(true);
            var unrotated = first.offset(13, 0, 7);
            placeMarker(helper, player, staff, unrotated);
            var copy = level.getBlockEntity(unrotated.above().offset(1, 1, 1));
            helper.assertTrue(staff.getTag().getInt("Rotation") == 0 && copy instanceof Container inventory
                            && inventory.isEmpty() && chest.getItem(0).getCount() == 3,
                    "Sneak-placement resets rotation and copies the chest block with an empty inventory");

            player.moveTo(unrotated.getX() + 0.5, level.getMaxBuildHeight() - 8, unrotated.getZ() + 0.5, 0, -90);
            var result = player.gameMode.useItem(player, level, staff, InteractionHand.MAIN_HAND);
            helper.assertTrue(result.consumesAction()
                            && !staff.getTag().contains("SavedSchematic") && staff.getTag().getBoolean("Keepsake"),
                    "Sneak-using in open air clears the capture and keeps unrelated tag data, result=" + result
                            + ", sneaking=" + player.isShiftKeyDown() + ", keys=" + staff.getTag().getAllKeys());
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_duplication_legacy", setupTicks = 5)
    public static void duplicationStaffPlacesOriginalMetadataAndSkipsMissingBlocks(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var click = helper.absolutePos(new BlockPos(4, 10, 5));
        var base = click.above();
        var staff = WitcheryIngredient.loadInventoryStack(TagParser.parseTag("""
                {id:"witchery:dupstaff",Damage:0s,Count:1b,tag:{Keepsake:1b,SavedSchematic:{
                  xMax:3,yMax:2,zMax:2,blocks:[
                    {n:"minecraft:log",m:4},{n:"minecraft:air"},
                    {n:"minecraft:wooden_door",m:4},{n:"minecraft:wooden_door",m:11},
                    {n:"minecraft:wool",m:14},{n:"witchery:stairswoodalder",m:4},
                    {n:"missing:slab"},{n:"minecraft:air"},
                    {n:"witchery:witchlog",m:9},{n:"minecraft:air"},
                    {n:"minecraft:diamond_block"},{n:"witchery:witchlog",m:14}]}}}
                """));
        var player = player(helper);
        try {
            level.setBlockAndUpdate(base.offset(1, 0, 1), Blocks.GOLD_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(base.offset(0, 1, 2), Blocks.OBSIDIAN.defaultBlockState());
            placeMarker(helper, player, staff, click);
            var oak = level.getBlockState(base.south(2));
            helper.assertTrue(oak.is(Blocks.OAK_LOG) && oak.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Z
                            && level.isEmptyBlock(base.south(2).above())
                            && level.getBlockState(base.south()).is(Blocks.RED_WOOL)
                            && level.getBlockState(base.east()).is(Blocks.DIAMOND_BLOCK),
                    "The saved list keeps vanilla metadata, air, and original ordering through rotation");
            var lower = level.getBlockState(base.offset(1, 0, 2));
            var upper = level.getBlockState(base.offset(1, 1, 2));
            helper.assertTrue(lower.is(Blocks.OAK_DOOR) && upper.is(Blocks.OAK_DOOR)
                            && lower.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER
                            && upper.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER
                            && lower.getValue(DoorBlock.FACING) == Direction.NORTH && upper.getValue(DoorBlock.FACING) == Direction.NORTH
                            && lower.getValue(DoorBlock.OPEN) && upper.getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT,
                    "The old door halves share their rotated facing and preserve open and hinge state");
            var stairs = level.getBlockState(base.south().above());
            var alder = level.getBlockState(base);
            var bark = level.getBlockState(base.east().above());
            helper.assertTrue(stairs.is(WitcheryBlocks.getRequired("stairswoodalder").get())
                            && stairs.getValue(StairBlock.FACING) == Direction.NORTH && stairs.getValue(StairBlock.HALF) == Half.TOP
                            && alder.getValue(WitchWoodType.PROPERTY) == WitchWoodType.ALDER
                            && alder.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.X
                            && level.getBlockState(base.offset(1, 0, 1)).is(Blocks.GOLD_BLOCK),
                    "Witchery metadata survives rotation and an unknown block preserves the destination");
            helper.assertTrue(bark.is(WitcheryBlocks.getRequired("witchlog").get()) && bark.getValue(WitchLogBlock.BARK)
                            && bark.getValue(WitchWoodType.PROPERTY) == WitchWoodType.HAWTHORN
                            && bark.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y,
                    "A rotated legacy schematic keeps its all-bark log");
            helper.assertTrue(staff.getTag().getBoolean("Keepsake")
                            && staff.getTag().getCompound("SavedSchematic").contains("blocks"),
                    "Placing an older schematic preserves its original data");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    private static FakePlayer player(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "DuplicationQA"));
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        return player;
    }

    private static void placeMarker(GameTestHelper helper, FakePlayer player, ItemStack staff, BlockPos pos) {
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        player.setPos(Vec3.atBottomCenterOf(pos.above(2)));
        player.setItemInHand(InteractionHand.MAIN_HAND, staff);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        helper.assertTrue(player.gameMode.useItemOn(player, level, staff, InteractionHand.MAIN_HAND, hit).consumesAction(),
                "Clicking a block uses the staff");
    }
}
