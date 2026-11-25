package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.DreamCatcherBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyBlockReplacementTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_replacement")
    public static void survivalPlacementReplacesLegacyBlocks(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = player(helper);
        var ids = List.of("circle", "circleglyphritual", "circleglyphotherwhere", "circleglyphinfernal",
                "snowpressureplate", "csnowpressureplate", "snowslab", "snowdoubleslab");
        for (int index = 0; index < ids.size(); index++) {
            var pos = helper.absolutePos(new BlockPos(3 + index * 2, 0, 3)).atY(160);
            level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos, state(ids.get(index)));
            var held = new ItemStack(Items.STONE, 2);
            helper.assertTrue(place(player, pos, Direction.UP, 0.5, held).consumesAction()
                            && held.getCount() == 1 && level.getBlockState(pos).is(Blocks.STONE)
                            && level.getBlockState(pos.above()).isAir(),
                    "The stone should replace the clicked " + ids.get(index));
        }

        var pos = helper.absolutePos(new BlockPos(5, 0, 7)).atY(160);
        var catcher = state("dreamcatcher");
        level.setBlockAndUpdate(pos.south(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos.north(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos, catcher);
        var imported = new CompoundTag();
        imported.putInt("WITCWeaveID", 3);
        var tile = (DreamCatcherBlockEntity) level.getBlockEntity(pos);
        tile.load(imported);
        var weave = new ItemStack(DreamWeave.NIGHTMARES.item());
        helper.assertTrue(ItemStack.matches(catcher.getBlock().getCloneItemStack(level, pos, catcher), weave)
                        && Block.getDrops(catcher, level, pos, tile).stream().anyMatch(drop -> ItemStack.matches(drop, weave)),
                "An earlier port's saved weave should be available to picking and loot before the tile ticks");
        var held = new ItemStack(Items.STONE, 2);
        Consumer<BlockEvent.EntityPlaceEvent> veto = event -> {
            if (event.getEntity() == player && event.getPos().equals(pos)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            helper.assertTrue(place(player, pos, Direction.NORTH, 0.5, held) == InteractionResult.FAIL
                            && held.getCount() == 2 && level.getBlockState(pos).equals(catcher)
                            && level.getBlockEntity(pos) instanceof DreamCatcherBlockEntity restored
                            && restored.weave() == DreamWeave.NIGHTMARES && drops(helper, pos).isEmpty(),
                    "A placement veto should preserve the catcher and give no weave");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }
        helper.assertTrue(place(player, pos, Direction.NORTH, 0.5, held).consumesAction()
                        && held.getCount() == 1 && level.getBlockState(pos).is(Blocks.STONE)
                        && level.getBlockEntity(pos) == null && level.getBlockState(pos.north()).isAir(),
                "An approved replacement should occupy the mounted catcher's cell");
        helper.succeedWhen(() -> {
            var dropped = drops(helper, pos);
            helper.assertTrue(dropped.size() == 1 && ItemStack.matches(dropped.get(0).getItem(), weave),
                    "The old weave should drop exactly once");
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_replacement")
    public static void snowSlabsMergeByFaceAndReuseTheClickedCell(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = player(helper);
        var bottom = state("snowslab");
        var top = bottom.setValue(SlabBlock.TYPE, SlabType.TOP);
        var doubleState = bottom.setValue(SlabBlock.TYPE, SlabType.DOUBLE);
        var legacyDouble = state("snowdoubleslab");
        var pos = helper.absolutePos(new BlockPos(5, 0, 6)).atY(160);
        var held = new ItemStack(bottom.getBlock(), 8);
        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos.east(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos, bottom);
        place(player, pos, Direction.UP, 0.5, held);
        helper.assertTrue(level.getBlockState(pos).equals(doubleState) && held.getCount() == 7,
                "Clicking the exposed top face should merge two snow slabs");
        place(player, pos, Direction.UP, 1, held);
        helper.assertTrue(level.getBlockState(pos).equals(bottom) && level.getBlockState(pos.above()).isAir()
                        && held.getCount() == 6, "A third slab should replace the double with one bottom slab");
        place(player, pos, Direction.EAST, 0.3, held);
        helper.assertTrue(held.getCount() == 6 && level.getBlockState(pos.east()).isAir(),
                "Clicking the occupied side of the same half should spend no slab");
        level.setBlockAndUpdate(pos.east(), top);
        place(player, pos, Direction.EAST, 0.3, held);
        helper.assertTrue(level.getBlockState(pos).equals(bottom) && level.getBlockState(pos.east()).equals(doubleState)
                        && held.getCount() == 5, "An adjacent half should merge on a side click");
        place(player, pos, Direction.DOWN, 0, held);
        helper.assertTrue(level.getBlockState(pos).equals(top) && held.getCount() == 4,
                "Clicking the underside of a bottom slab should replace it with a top slab");
        place(player, pos, Direction.DOWN, 0.5, held);
        helper.assertTrue(level.getBlockState(pos).equals(doubleState) && held.getCount() == 3,
                "Clicking the exposed underside of a top slab should merge it");
        level.setBlockAndUpdate(pos, legacyDouble);
        place(player, pos, Direction.UP, 1, held);
        helper.assertTrue(level.getBlockState(pos).equals(bottom) && held.getCount() == 2,
                "The legacy double-slab ID should be replaceable by one slab too");
        var full = new ItemStack(legacyDouble.getBlock());
        place(player, pos, Direction.UP, 0.5, full);
        helper.assertTrue(level.getBlockState(pos).equals(legacyDouble) && full.isEmpty() && drops(helper, pos).isEmpty(),
                "The legacy double-slab item should replace a single slab without a drop");
        helper.succeed();
    }

    private static List<ItemEntity> drops(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.5));
    }

    private static BlockState state(String path) {
        return WitcheryBlocks.getRequired(path).get().defaultBlockState();
    }

    private static FakePlayer player(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "LegacyReplacementQA"));
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        return player;
    }

    static InteractionResult place(FakePlayer player, BlockPos pos, Direction face, double hitY, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var hit = new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + hitY, pos.getZ() + 0.5), face, pos, false);
        return player.gameMode.useItemOn(player, player.level(), stack, InteractionHand.MAIN_HAND, hit);
    }
}
