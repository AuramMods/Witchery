package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.GrassperBlockEntity;
import art.arcane.witchery.block.entity.SilverVatBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class OfferingBlockTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_offering_blocks")
    public static void silverVatRemembersNonemptyGoldOnlyWhileLoaded(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(8, 8, 8));
        var vatState = WitcheryBlocks.getRequired("silvervat").get().defaultBlockState();
        level.setBlockAndUpdate(pos, vatState);
        level.setBlockAndUpdate(pos.west(), Blocks.FURNACE.defaultBlockState());
        level.setBlockAndUpdate(pos.east(), Blocks.BLAST_FURNACE.defaultBlockState());
        var vat = (SilverVatBlockEntity) level.getBlockEntity(pos);
        var west = (Container) level.getBlockEntity(pos.west());
        var east = (Container) level.getBlockEntity(pos.east());

        notifyGold(level, west, 4);
        notifyGold(level, east, 1);
        helper.assertTrue(vat.getItem(0).getCount() == 2, "Each furnace's first nonempty result can produce dust");
        notifyGold(level, west, 0);
        notifyGold(level, west, 1);
        helper.assertTrue(vat.getItem(0).getCount() == 2, "An empty furnace leaves the old observation in place");
        notifyGold(level, west, 2);
        helper.assertTrue(vat.getItem(0).getCount() == 3, "A smaller nonempty result updates the observed count");

        var stored = new ItemStack(WitcheryIngredient.SILVER_DUST.item(), 3).setHoverName(Component.literal("Saved dust"));
        vat.setItem(0, stored);
        var oldSave = vat.saveWithoutMetadata();
        oldSave.putIntArray("ObservedGold", new int[] {0, 2, 0, 1});
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos, vatState);
        var reloaded = (SilverVatBlockEntity) level.getBlockEntity(pos);
        reloaded.load(oldSave);
        notifyGold(level, west, 2);
        notifyGold(level, west, 2);
        helper.assertTrue(reloaded.getItem(0).getCount() == 4
                        && reloaded.getItem(0).getHoverName().getString().equals("Saved dust"),
                "A reopened vat starts observing again and grows its saved dust stack in place");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_offering_blocks")
    public static void grassperReleasesTheOfferingForImmediatePickup(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(8, 8, 8));
        var state = WitcheryBlocks.getRequired("grassper").get().defaultBlockState();
        level.setBlockAndUpdate(pos, state);
        var grassper = (GrassperBlockEntity) level.getBlockEntity(pos);
        var player = helper.makeMockPlayer();
        var offering = new ItemStack(Items.DIAMOND, 3).setHoverName(Component.literal("Grassper offering"));
        player.setItemInHand(InteractionHand.MAIN_HAND, offering);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        state.use(level, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(player.getMainHandItem().getCount() == 2 && grassper.getItem(0).getCount() == 1,
                "A player offers one item from the held stack");
        state.use(level, player, InteractionHand.MAIN_HAND, hit);
        var released = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos), item -> item.getItem().is(Items.DIAMOND));
        helper.assertTrue(grassper.isEmpty() && released.size() == 1, "A second use releases only the offering");
        helper.assertTrue(!released.get(0).hasPickUpDelay() && released.get(0).getItem().getCount() == 1
                        && released.get(0).getItem().getHoverName().getString().equals("Grassper offering"),
                "The released item keeps its tag and can be picked up immediately");
        helper.succeed();
    }

    private static void notifyGold(ServerLevel level, Container furnace, int count) {
        furnace.setItem(2, count == 0 ? ItemStack.EMPTY : new ItemStack(Items.GOLD_INGOT, count));
        level.random.setSeed(0);
        furnace.setChanged();
    }
}
