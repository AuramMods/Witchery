package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.PoppetShelfBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyBlockHarvestTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_workstation_harvest")
    public static void originalPickaxeBlocksReturnTheirExpectedItems(GameTestHelper helper) {
        var level = helper.getLevel();
        var miner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "WorkstationMiner"));
        var pos = helper.absolutePos(new BlockPos(5, 2, 4));
        var area = new AABB(pos).inflate(1);
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        var hardness = Map.ofEntries(Map.entry("beartrap", 5F), Map.entry("bloodcrucible", 2.5F),
                Map.entry("brazier", 3.5F), Map.entry("candelabra", 2F), Map.entry("chalice", 3F),
                Map.entry("crystalball", 2F), Map.entry("cstonepressureplate", 0.5F),
                Map.entry("daylightcollector", 3.5F), Map.entry("kettle", 2F), Map.entry("fumefunnel", 3.5F),
                Map.entry("filteredfumefunnel", 3.5F), Map.entry("poppetshelf", 2.5F), Map.entry("wolftrap", 5F));
        var poppet = new ItemStack(WitcheryItems.getRequired("water_protection_poppet").get());
        poppet.getOrCreateTag().putString("Keepsake", "shelf contents");
        for (var entry : hardness.entrySet()) {
            String id = entry.getKey();
            var state = state(id);
            if (id.equals("chalice")) state = state.setValue(ChaliceBlock.FILLED, true);
            level.setBlockAndUpdate(pos, state);
            helper.assertTrue(level.getBlockState(pos).is(state.getBlock()), "Place " + id + " for harvesting");
            if (level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity shelf) {
                shelf.setItem(0, poppet.copy());
                shelf.keepChunkLoaded();
                helper.assertTrue(PoppetShelfBlockEntity.activeShelves(level.getServer()).contains(shelf),
                        "The populated shelf can protect before harvesting");
            }

            miner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            float byHand = state.getDestroyProgress(miner, level, pos);
            helper.assertTrue(state.getDestroySpeed(level, pos) == entry.getValue() && byHand > 0,
                    id + " matches the original hardness");
            helper.assertFalse(miner.hasCorrectToolForDrops(state), id + " needs a pickaxe to qualify for drops");
            miner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
            helper.assertTrue(miner.hasCorrectToolForDrops(state) && state.getDestroyProgress(miner, level, pos) > byHand,
                    "An iron pickaxe qualifies and speeds up mining " + id);
            helper.assertTrue(miner.gameMode.destroyBlock(pos) && level.isEmptyBlock(pos), "Break " + id + " in survival");

            var drops = level.getEntitiesOfClass(ItemEntity.class, area);
            var expected = switch (id) {
                case "chalice" -> WitcheryIngredient.FILLED_CHALICE.item();
                case "cstonepressureplate" -> Items.STONE_PRESSURE_PLATE;
                default -> state.getBlock().asItem();
            };
            int count = drops.stream().filter(item -> item.getItem().is(expected)).mapToInt(item -> item.getItem().getCount()).sum();
            helper.assertTrue(count == (id.equals("wolftrap") ? 0 : 1), id + " returns exactly its original drop count");
            if (id.equals("poppetshelf")) {
                helper.assertTrue(drops.stream().anyMatch(item -> ItemStack.isSameItemSameTags(item.getItem(), poppet)),
                        "Mining the shelf also releases its stored poppet with its item data");
                helper.assertFalse(PoppetShelfBlockEntity.activeShelves(level.getServer()).stream()
                        .anyMatch(shelf -> shelf.getBlockPos().equals(pos) && shelf.getLevel() == level),
                        "A harvested shelf is no longer registered for protection");
            }
            drops.forEach(ItemEntity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_block_special_harvest")
    public static void specialHarvestRulesKeepTheirItemsAndToolRestrictions(GameTestHelper helper) {
        var level = helper.getLevel();
        var miner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SpecialMiner"));
        var pos = helper.absolutePos(new BlockPos(5, 2, 4));
        var area = new AABB(pos).inflate(1);
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        miner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        var heart = state("demonheart");
        level.setBlockAndUpdate(pos, heart);
        helper.assertTrue(miner.hasCorrectToolForDrops(heart) && miner.gameMode.destroyBlock(pos), "A hand can recover the mounted heart");
        helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, area).stream()
                .anyMatch(item -> item.getItem().is(WitcheryIngredient.DEMON_HEART.item()) && item.getItem().getCount() == 1),
                "The mounted heart returns the Demon Heart ingredient");

        var dark = state("shadedglass_active").setValue(ShadedGlassBlock.COLOR, ShadedGlassBlock.Color.GREEN);
        level.setBlockAndUpdate(pos.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos, dark);
        helper.assertFalse(miner.hasCorrectToolForDrops(dark), "Darkened glass requires a pickaxe");
        var pickaxe = new ItemStack(Items.IRON_PICKAXE);
        miner.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        helper.assertTrue(miner.hasCorrectToolForDrops(dark), "A pickaxe qualifies against the redstone-darkened state");
        helper.assertTrue(miner.gameMode.destroyBlock(pos) && level.getEntitiesOfClass(ItemEntity.class, area).stream()
                .noneMatch(item -> item.getItem().is(dark.getBlock().asItem())), "Ordinary mining still shatters darkened glass");
        level.setBlockAndUpdate(pos, dark);
        pickaxe.enchant(Enchantments.SILK_TOUCH, 1);
        helper.assertTrue(miner.gameMode.destroyBlock(pos) && level.getEntitiesOfClass(ItemEntity.class, area).stream()
                .anyMatch(item -> item.getItem().is(dark.getBlock().asItem())
                        && ShadedGlassBlock.Color.fromStack(item.getItem()) == ShadedGlassBlock.Color.GREEN),
                "Silk Touch still returns the darkened pane and its color");

        miner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        var iceSlab = state("iceslab");
        level.setBlockAndUpdate(pos, iceSlab);
        helper.assertTrue(iceSlab.getFriction(level, pos, miner) == 0.6F && miner.hasCorrectToolForDrops(iceSlab)
                && miner.gameMode.destroyBlock(pos)
                && level.getEntitiesOfClass(ItemEntity.class, area).stream().anyMatch(item -> item.getItem().is(iceSlab.getBlock().asItem())),
                "Perpetual Ice Slabs retain their original hand drops");
        var web = state("web");
        level.setBlockAndUpdate(pos, web);
        int itemCount = level.getEntitiesOfClass(ItemEntity.class, area).size();
        helper.assertTrue(miner.hasCorrectToolForDrops(web) && miner.gameMode.destroyBlock(pos)
                && level.getEntitiesOfClass(ItemEntity.class, area).size() == itemCount, "Brew webs use the original hand speed and drop nothing");

        var cursedSnow = state("csnowpressureplate");
        level.setBlockAndUpdate(pos, cursedSnow);
        helper.assertFalse(miner.hasCorrectToolForDrops(cursedSnow), "The cursed snow plate shares the plain snow plate's tool rule");
        miner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SHOVEL));
        helper.assertTrue(miner.hasCorrectToolForDrops(cursedSnow) && miner.gameMode.destroyBlock(pos)
                && level.getEntitiesOfClass(ItemEntity.class, area).stream()
                .anyMatch(item -> item.getItem().is(state("snowpressureplate").getBlock().asItem())),
                "A shovel recovers the original uncursed snow plate");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void recoverOriginalAxeSpeedsAndChalkHardness(GameTestHelper helper) {
        var level = helper.getLevel();
        var miner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "WoodMiner"));
        var pos = helper.absolutePos(new BlockPos(5, 2, 4));
        for (String id : List.of("bramble", "circle", "circleglyphritual", "circleglyphotherwhere", "circleglyphinfernal",
                "cwoodendoor", "cwoodpressureplate", "dreamcatcher", "icefencegate", "plantmine", "spanishmoss",
                "spinningwheel", "wickerbundle")) {
            var state = state(id);
            miner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            float byHand = state.getDestroyProgress(miner, level, pos);
            miner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
            helper.assertTrue(state.getDestroyProgress(miner, level, pos) > byHand, "An axe speeds up mining " + id);
        }
        helper.assertTrue(state("spinningwheel").getDestroySpeed(level, pos) == 3.5F, "Restore the wheel's 3.5 hardness");
        for (String id : List.of("circleglyphritual", "circleglyphotherwhere", "circleglyphinfernal")) {
            helper.assertTrue(state(id).getDestroySpeed(level, pos) == 2, "Restore the hardness of " + id);
        }
        helper.assertTrue(state("wolfhead").getDestroySpeed(level, pos) == 0, "Wolf trophies break at their original speed");
        helper.succeed();
    }

    private static BlockState state(String id) {
        return WitcheryBlocks.getRequired(id).get().defaultBlockState();
    }
}
