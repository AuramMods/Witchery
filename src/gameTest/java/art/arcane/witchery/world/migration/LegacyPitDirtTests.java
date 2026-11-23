package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.PitDirtBlock;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantments;
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
public final class LegacyPitDirtTests {
    @GameTest(template = "qa_coven_arena")
    public static void importedPitDirtKeepsPodzolAndTracksItsCover(GameTestHelper helper) throws Exception {
        var entry = WitcheryBlocks.getRequired("pitdirt");
        for (int metadata = 0; metadata < 16; metadata++) {
            var state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),
                    LegacyChunkUpgrade.restoreState(entry.getId(), metadata, 0));
            helper.assertTrue(state.is(entry.get()) && state.getValue(PitDirtBlock.PODZOL) == (metadata == 2),
                    "Metadata two uses Podzol and other metadata uses ordinary Pit Dirt");
        }

        var imported = WitcheryIngredient.loadInventoryStack(TagParser.parseTag(
                "{id:\"witchery:pitdirt\",Count:2b,Damage:2s,tag:{Marker:\"kept\"}}"));
        helper.assertTrue(imported.getCount() == 2 && PitDirtBlock.isPodzol(imported)
                        && imported.getTag().getString("Marker").equals("kept"),
                "The imported stack preserves the variant, count and custom marker");
        var modern = WitcheryIngredient.loadInventoryStack(TagParser.parseTag(
                "{id:\"witchery:pitdirt\",Count:1b,Damage:2s,tag:{BlockStateTag:{podzol:\"false\"}}}"));
        helper.assertTrue(!PitDirtBlock.isPodzol(modern), "An explicit modern state takes precedence over the legacy damage value");

        var level = helper.getLevel();
        var base = new BlockPos(5, 3, 5);
        helper.setBlock(base, Blocks.STONE);
        var surface = helper.absolutePos(base);
        var pos = surface.above();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "PitDirtPlacement"));
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 4, 2.5)));
        player.setItemInHand(InteractionHand.MAIN_HAND, imported);
        var hit = new BlockHitResult(Vec3.atCenterOf(surface).add(0, 0.5, 0), Direction.UP, surface, false);
        var item = (BlockItem) imported.getItem();
        helper.assertTrue(item.place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, imported, hit)).consumesAction(),
                "The imported Podzol stack places through its block item");
        var state = level.getBlockState(pos);
        helper.assertTrue(state.getValue(PitDirtBlock.PODZOL) && state.getCollisionShape(level, pos).isEmpty(),
                "Podzol Pit Dirt retains the pass-through collision");

        level.setBlockAndUpdate(pos.above(), Blocks.GRASS_BLOCK.defaultBlockState());
        helper.assertTrue(level.getBlockState(pos).getValue(PitDirtBlock.COVER) == PitDirtBlock.Cover.SOIL,
                "Grass above the trap gives it plain dirt sides");
        level.setBlockAndUpdate(pos.above(), Blocks.SNOW_BLOCK.defaultBlockState());
        helper.assertTrue(level.getBlockState(pos).getValue(PitDirtBlock.COVER) == PitDirtBlock.Cover.SNOW,
                "Snow above the trap gives it snowy sides");
        level.removeBlock(pos.above(), false);
        state = level.getBlockState(pos);
        helper.assertTrue(state.getValue(PitDirtBlock.COVER) == PitDirtBlock.Cover.NONE
                        && PitDirtBlock.isPodzol(entry.get().getCloneItemStack(level, pos, state)),
                "Removing the cover reveals the sides and pick block still retains the Podzol variant");

        var shovel = Items.IRON_SHOVEL.getDefaultInstance();
        var drops = Block.getDrops(state, level, pos, null, player, shovel);
        helper.assertTrue(drops.size() == 1 && drops.get(0).is(item) && !PitDirtBlock.isPodzol(drops.get(0)),
                "Ordinary mining drops plain Pit Dirt");
        shovel.enchant(Enchantments.SILK_TOUCH, 1);
        drops = Block.getDrops(state, level, pos, null, player, shovel);
        helper.assertTrue(drops.size() == 1 && drops.get(0).is(item) && PitDirtBlock.isPodzol(drops.get(0)),
                "Silk Touch preserves the Podzol variant");
        helper.succeed();
    }
}
