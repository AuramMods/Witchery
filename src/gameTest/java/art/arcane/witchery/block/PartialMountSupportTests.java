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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
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
public final class PartialMountSupportTests {
    @GameTest(template = "qa_coven_arena")
    public static void chalkAndArtifactsUseSolidPartialSupports(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "PartialMount"));
        var sites = List.of(
                new Mount("circle", WitcheryItems.getRequired("chalkheart").get()),
                new Mount("circleglyphritual", WitcheryItems.getRequired("chalkritual").get()),
                new Mount("circleglyphotherwhere", WitcheryItems.getRequired("chalkotherwhere").get()),
                new Mount("circleglyphinfernal", WitcheryItems.getRequired("chalkinfernal").get()),
                new Mount("candelabra", WitcheryIngredient.CANDELABRA.item()),
                new Mount("chalice", WitcheryIngredient.FILLED_CHALICE.item()),
                new Mount("crystalball", WitcheryBlocks.getRequired("crystalball").get().asItem()),
                new Mount("placeditem", WitcheryItems.getRequired("arthana").get()));
        for (int i = 0; i < sites.size(); ++i) {
            var entry = sites.get(i);
            var support = helper.absolutePos(new BlockPos(2 + i % 4, 3, 2 + 2 * (i / 4)));
            var position = support.above();
            var base = entry.block().equals("placeditem") ? WitcheryBlocks.getRequired("altar").get() : Blocks.STONE_SLAB;
            level.setBlockAndUpdate(support, base.defaultBlockState());
            level.setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
            useOn(player, entry.item(), support, Direction.UP);
            var mounted = level.getBlockState(position);
            helper.assertTrue(mounted.is(WitcheryBlocks.getRequired(entry.block()).get()),
                    "The ordinary item use places " + entry.block() + " above its solid support");
            if (mounted.hasProperty(ChaliceBlock.FILLED)) {
                helper.assertTrue(mounted.getValue(ChaliceBlock.FILLED), "The placed chalice remains filled");
            }
            level.setBlockAndUpdate(support, Blocks.OAK_SLAB.defaultBlockState());
            helper.assertTrue(level.getBlockState(position) == mounted, "A solid slab update preserves " + entry.block());
            level.setBlockAndUpdate(support, Blocks.AIR.defaultBlockState());
            helper.assertTrue(level.isEmptyBlock(position), "Removing support detaches " + entry.block());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void altarMountDropsOnlyOnHarvestOrLossOfSupport(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(4, 3, 4));
        var level = helper.getLevel();
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(pos),
                "The chunk is tracking dropped items"))
                .thenExecute(() -> verifyArtifactDrops(helper, pos)).thenSucceed();
    }

    private static void verifyArtifactDrops(GameTestHelper helper, BlockPos pos) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ArtifactMount"));
        var altar = WitcheryBlocks.getRequired("altar").get();
        var placed = WitcheryBlocks.getRequired("placeditem").get();
        var arthana = new ItemStack(WitcheryItems.getRequired("arthana").get());
        arthana.setDamageValue(14);
        arthana.getOrCreateTag().putString("MountMarker", "original item");
        for (String operation : List.of("survival", "creative", "support", "replacement")) {
            level.setBlockAndUpdate(pos.below(), altar.defaultBlockState());
            useOn(player, arthana.copy(), pos.below(), Direction.UP);
            helper.assertTrue(level.getBlockState(pos).is(placed) && player.getMainHandItem().isEmpty(),
                    "Mount the Arthana before " + operation);
            switch (operation) {
                case "survival" -> helper.assertTrue(player.gameMode.destroyBlock(pos), "Survival harvest removes the mount");
                case "creative" -> {
                    player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
                    helper.assertTrue(player.gameMode.destroyBlock(pos), "Creative breaking removes the mount");
                    player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                }
                case "support" -> level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
                case "replacement" -> level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
            }
            helper.assertFalse(level.getBlockState(pos).is(placed), "The mount is gone after " + operation);
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1));
            if (operation.equals("survival") || operation.equals("support")) {
                helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().getCount() == 1
                                && ItemStack.isSameItemSameTags(drops.get(0).getItem(), arthana),
                        operation + " returns exactly the mounted item with its wear and tag");
                drops.get(0).discard();
            } else {
                helper.assertTrue(drops.isEmpty(), operation + " does not drop the mounted item");
            }
        }

        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        var pentacles = new ItemStack(WitcheryIngredient.KOBOLDITE_PENTACLE.item(), 4);
        pentacles.getOrCreateTag().putString("MountMarker", "original pentacles");
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        useOn(player, pentacles.copy(), pos.below(), Direction.UP);
        helper.assertTrue(level.getBlockState(pos).is(placed) && player.getMainHandItem().isEmpty(),
                "Mount the full held stack, including in creative");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        helper.assertTrue(player.gameMode.destroyBlock(pos), "Recover the Pentacle mount in survival");
        var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1));
        helper.assertTrue(drops.size() == 1 && ItemStack.matches(drops.get(0).getItem(), pentacles),
                "Harvest returns the full stack with its tag");
    }

    @GameTest(template = "qa_coven_arena")
    public static void dreamCatcherKeepsItsWeaveOnPartialWallUpdates(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "WeaveMount"));
        var wall = helper.absolutePos(new BlockPos(4, 3, 4));
        var north = wall.north();
        level.setBlockAndUpdate(wall, Blocks.STONE_SLAB.defaultBlockState());
        helper.assertTrue(!level.getBlockState(wall).isFaceSturdy(level, wall, Direction.NORTH),
                "The slab fixture has a partial side");
        useOn(player, DreamWeave.NIGHTMARES.item(), wall, Direction.NORTH);
        var mounted = level.getBlockState(north);
        helper.assertTrue(mounted.is(WitcheryBlocks.getRequired("dreamcatcher").get())
                        && mounted.getValue(DreamCatcherBlock.FACING) == Direction.NORTH
                        && mounted.getValue(DreamCatcherBlock.WEAVE) == DreamWeave.NIGHTMARES,
                "The original item placement keeps its facing and Nightmares weave on a partial wall");
        level.setBlockAndUpdate(wall, Blocks.OAK_SLAB.defaultBlockState());
        helper.assertTrue(level.getBlockState(north) == mounted, "Replacing the slab with another solid slab keeps the weave");
        level.setBlockAndUpdate(wall, Blocks.COBWEB.defaultBlockState());
        helper.assertTrue(level.getBlockState(north) == mounted, "Cobweb has a solid legacy material and retains the weave");
        level.setBlockAndUpdate(wall, Blocks.AIR.defaultBlockState());
        helper.assertTrue(level.isEmptyBlock(north), "Removing its support releases the Dream Catcher");
        var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(north).inflate(1),
                item -> item.getItem().is(DreamWeave.NIGHTMARES.item()));
        helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().getCount() == 1,
                "The unsupported mount releases exactly the fitted weave");
        helper.succeed();
    }

    private static void useOn(FakePlayer player, Item item, BlockPos support, Direction face) {
        useOn(player, new ItemStack(item), support, face);
    }

    private static void useOn(FakePlayer player, ItemStack stack, BlockPos support, Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var hit = new BlockHitResult(Vec3.atCenterOf(support).relative(face, 0.5), face, support, false);
        player.gameMode.useItemOn(player, player.serverLevel(), stack, InteractionHand.MAIN_HAND, hit);
    }

    private record Mount(String block, Item item) {}
}
