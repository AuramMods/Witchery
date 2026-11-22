package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WitchWoodSlabTests {
    @GameTest(template = "qa_coven_arena")
    public static void slabStacksKeepTheirWoodThroughPlacementAndLoot(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var slab = WitcheryBlocks.getRequired("witchwoodslab").get();
        var doubleSlab = WitcheryBlocks.getRequired("witchwooddoubleslab").get();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SlabPlacement"));
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 4, 2.5)));
        var base = new BlockPos(6, 4, 5);
        helper.setBlock(base.below(), Blocks.STONE);
        var pos = helper.absolutePos(base);
        var alder = WitcheryIngredient.loadInventoryStack(TagParser.parseTag("{id:\"witchery:witchwoodslab\",Count:2b,Damage:1s}"));
        place(helper, player, alder, pos.below());
        helper.assertTrue(level.getBlockState(pos).getValue(WitchWoodType.PROPERTY) == WitchWoodType.ALDER,
                "The first placement should use the imported Alder subtype");
        var hawthorn = WitchWoodType.HAWTHORN.stack(slab);
        place(helper, player, hawthorn, pos);
        helper.assertTrue(level.getBlockState(pos).getValue(SlabBlock.TYPE) == SlabType.BOTTOM
                        && level.getBlockState(pos.above()).getValue(WitchWoodType.PROPERTY) == WitchWoodType.HAWTHORN,
                "A different species should be placed above the slab without merging");
        helper.setBlock(base.above(), Blocks.AIR);
        place(helper, player, alder, pos);
        var doubled = level.getBlockState(pos);
        helper.assertTrue(doubled.is(slab) && doubled.getValue(SlabBlock.TYPE) == SlabType.DOUBLE
                        && doubled.getValue(WitchWoodType.PROPERTY) == WitchWoodType.ALDER && alder.isEmpty(),
                "The second Alder slab should merge and consume its one slab item");
        var pick = slab.getCloneItemStack(level, pos, doubled);
        helper.assertTrue(pick.is(doubleSlab.asItem()) && WitchWoodType.fromStack(pick) == WitchWoodType.ALDER,
                "The old double slab item keeps its wood when picked");
        assertDrop(helper, player, pos, slab, WitchWoodType.ALDER);
        var legacy = WitcheryIngredient.loadInventoryStack(TagParser.parseTag(
                "{id:\"witchery:witchwooddoubleslab\",Count:1b,Damage:2s}"));
        helper.setBlock(base.east(2).below(), Blocks.STONE);
        var oldPos = helper.absolutePos(base.east(2));
        place(helper, player, legacy, oldPos.below());
        var placed = level.getBlockState(oldPos);
        helper.assertTrue(placed.is(doubleSlab) && placed.getValue(SlabBlock.TYPE) == SlabType.DOUBLE
                        && placed.getValue(WitchWoodType.PROPERTY) == WitchWoodType.HAWTHORN,
                "The old full slab item should place as a Hawthorn double slab");
        assertDrop(helper, player, oldPos, slab, WitchWoodType.HAWTHORN);
        helper.succeed();
    }

    private static void assertDrop(GameTestHelper helper, FakePlayer player, BlockPos pos, Block slab, WitchWoodType wood) {
        var level = helper.getLevel();
        var drops = Block.getDrops(level.getBlockState(pos), level, pos, null, player, Items.IRON_AXE.getDefaultInstance());
        helper.assertTrue(drops.size() == 1 && drops.get(0).is(slab.asItem()) && drops.get(0).getCount() == 2
                        && WitchWoodType.fromStack(drops.get(0)) == wood,
                "Mining the double slab should give back two slabs of the same wood");
    }

    private static void place(GameTestHelper helper, FakePlayer player, ItemStack stack, BlockPos surface) {
        var item = (BlockItem) stack.getItem();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var hit = new BlockHitResult(Vec3.atCenterOf(surface).add(0, 0.5, 0), Direction.UP, surface, false);
        helper.assertTrue(item.place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit)).consumesAction(),
                "The slab should place through the block item");
    }
}
