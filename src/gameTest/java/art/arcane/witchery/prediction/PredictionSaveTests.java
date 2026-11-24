package art.arcane.witchery.prediction;

import art.arcane.witchery.Witchery;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PredictionSaveTests {
    @GameTest(template = "qa_coven_arena")
    public static void readingsKeepRegisteredLegacyForecasts(GameTestHelper helper) {
        List<String> messages = new ArrayList<>();
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "SavedFortune")) {
            @Override
            public void sendSystemMessage(Component component) {
                if (component.getContents() instanceof TranslatableContents translated) {
                    messages.add(translated.getKey());
                }
            }
        };
        var iron = forecast(5, 1_200L);
        iron.putString("FixtureMarker", "original");
        var coal = forecast(17, 1_600L);
        var entries = store(player, forecast(999, 1_000L), iron, coal, iron.copy());
        Predictions.attune(player);

        Predictions.read(player, player);

        helper.assertTrue(List.copyOf(entries).equals(List.of(iron, coal, iron)),
                "A reading discards obsolete IDs and keeps the original records");
        helper.assertTrue(messages.equals(List.of("witchery.prediction.iron", "witchery.prediction.coal")),
                "A reading reports each registered forecast once");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void obsoleteForecastsCannotPreventNewReadings(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "NewFortune"));
        var entries = store(player, forecast(999, 1_000L));
        player.tickCount = 100;
        Predictions.tick(player);
        helper.assertTrue(entries.isEmpty(), "The player tick clears obsolete forecast IDs");

        entries.add(forecast(999, 1_000L));
        Predictions.attune(player);
        Predictions.read(player, player);
        helper.assertTrue(entries.size() == 1 && Predictions.Kind.byId(entries.getCompound(0).getInt("WITCPreID")) != null
                        && entries.getCompound(0).getLong("WITCPreTime") > 1_000L,
                "An obsolete forecast cannot occupy the Crystal Ball's reading slot");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void overdueFallWorksAboveFarmlandDuringAJump(GameTestHelper helper) {
        var level = helper.getLevel();
        var ground = prepareSoil(helper);
        level.setBlockAndUpdate(ground, Blocks.FARMLAND.defaultBlockState());
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastFall"));
        player.setPos(Vec3.atBottomCenterOf(ground.above()).add(0, 0.25, 0));
        player.setOnGround(false);
        var entries = store(player, forecast(4, 1_000L));
        BlockPos warded = ground.north().below(2);
        level.setBlockAndUpdate(ground.south().below(), Blocks.BEDROCK.defaultBlockState());
        Consumer<BlockEvent.BreakEvent> veto = event -> {
            if (event.getPlayer() == player && event.getPos().equals(warded)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            rollOverdueForecast(player);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }

        helper.assertTrue(entries.isEmpty(), "The Fall forecast can fulfill during a jump over farmland");
        helper.assertTrue(level.getBlockState(ground).is(Blocks.GRAVEL), "The cultivated roof becomes gravel");
        for (int depth = 1; depth <= 5; depth++) {
            helper.assertTrue(level.isEmptyBlock(ground.below(depth)), "The Fall cavity is five blocks deep");
        }
        helper.assertTrue(level.getBlockState(ground.below(6)).is(Blocks.STONE), "The floor stays below the cavity");
        helper.assertTrue(level.getBlockState(warded).is(Blocks.STONE)
                        && level.getBlockState(ground.south().below()).is(Blocks.BEDROCK),
                "The cavity skips protected blocks under the roof");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void overdueWetKeepsItsReadingWhenTheRoofIsProtected(GameTestHelper helper) {
        var level = helper.getLevel();
        var ground = prepareSoil(helper);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastWet"));
        player.setPos(Vec3.atBottomCenterOf(ground.above()));
        player.setOnGround(true);
        var entries = store(player, forecast(15, 1_000L));
        Consumer<BlockEvent.EntityMultiPlaceEvent> veto = event -> {
            if (event.getEntity() == player) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            rollOverdueForecast(player);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }
        helper.assertTrue(entries.size() == 1 && level.getBlockState(ground).is(Blocks.DIRT)
                        && level.getBlockState(ground.below()).is(Blocks.STONE),
                "A veto on the gravel roof leaves the soil and active forecast intact");

        level.setBlockAndUpdate(ground, Blocks.FARMLAND.defaultBlockState());
        player.setPos(Vec3.atBottomCenterOf(ground.above()).add(0, -0.0625, 0));
        rollOverdueForecast(player);
        helper.assertTrue(entries.isEmpty(), "The same Wet forecast fulfills while standing on the shorter modern farmland surface");
        for (BlockPos surface : BlockPos.betweenClosed(ground.offset(-1, 0, -1), ground.offset(1, 0, 1))) {
            helper.assertTrue(level.getBlockState(surface).is(Blocks.GRAVEL), "The Wet forecast replaces the whole roof");
            helper.assertTrue(level.getBlockState(surface.below()).equals(Blocks.WATER.defaultBlockState())
                            && level.getBlockState(surface.below(2)).equals(Blocks.WATER.defaultBlockState())
                            && level.getBlockState(surface.below(3)).is(Blocks.STONE),
                    "The Wet forecast fills exactly two layers with water sources");
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void buriedTreasureFillsACoveredAirPocket(GameTestHelper helper) {
        var level = helper.getLevel();
        var surface = prepareSoil(helper);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastTreasure"));
        var entries = store(player, forecast(8, 1_000L));
        var chestPos = surface.below();
        level.setBlockAndUpdate(surface, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(chestPos, Blocks.AIR.defaultBlockState());
        var drops = new ArrayList<>(List.of(new ItemStack(Items.DIRT)));

        harvestForTreasure(player, surface, drops);

        helper.assertTrue(entries.isEmpty() && level.getBlockEntity(chestPos) instanceof ChestBlockEntity,
                "The buried chest appears in the covered air pocket and clears the forecast");
        var data = level.getBlockEntity(chestPos).saveWithoutMetadata();
        var contents = data.getList("Items", Tag.TAG_COMPOUND);
        var originalLoot = Set.of("minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:redstone",
                "minecraft:lapis_lazuli", "minecraft:diamond", "minecraft:coal", "minecraft:bread",
                "minecraft:iron_pickaxe", "minecraft:rail", "minecraft:melon_seeds", "minecraft:pumpkin_seeds",
                "minecraft:saddle", "minecraft:iron_horse_armor", "minecraft:enchanted_book");
        helper.assertTrue(!data.contains("LootTable") && !contents.isEmpty() && contents.size() <= 6
                        && contents.stream().allMatch(tag -> originalLoot.contains(((CompoundTag) tag).getString("id"))),
                "The chest immediately saves loot from the original mineshaft pool");
        helper.assertTrue(drops.size() == 1 && drops.get(0).is(Items.DIRT), "The dirt harvest keeps its normal drop");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void buriedTreasureKeepsItsReadingWhenTheSiteIsOpenOrProtected(GameTestHelper helper) {
        var level = helper.getLevel();
        var surface = prepareSoil(helper);
        var chestPos = surface.below();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ProtectedTreasure"));
        var entries = store(player, forecast(8, 1_000L));
        level.setBlockAndUpdate(surface, Blocks.AIR.defaultBlockState());
        BlockPos wall = chestPos.relative(Direction.EAST);
        level.setBlockAndUpdate(wall, Blocks.AIR.defaultBlockState());
        harvestForTreasure(player, surface, List.of());
        helper.assertTrue(entries.size() == 1 && level.getBlockState(chestPos).is(Blocks.STONE),
                "An exposed site leaves the forecast active");
        level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());

        Consumer<BlockEvent.BreakEvent> denyBreak = event -> {
            if (event.getPlayer() == player && event.getPos().equals(chestPos)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(denyBreak);
        try {
            harvestForTreasure(player, surface, List.of());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(denyBreak);
        }
        helper.assertTrue(entries.size() == 1 && level.getBlockState(chestPos).is(Blocks.STONE),
                "A Forge break veto preserves the site and the forecast");

        Consumer<BlockEvent.EntityPlaceEvent> denyChest = event -> {
            if (event.getEntity() == player && event.getPos().equals(chestPos)
                    && event.getPlacedBlock().is(Blocks.CHEST)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(denyChest);
        try {
            harvestForTreasure(player, surface, List.of());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(denyChest);
        }
        helper.assertTrue(entries.size() == 1 && level.getBlockState(chestPos).is(Blocks.STONE)
                        && level.getBlockEntity(chestPos) == null,
                "A Forge chest placement veto restores the site, with no chest or spent forecast");

        level.setBlockAndUpdate(chestPos, Blocks.WATER.defaultBlockState());
        harvestForTreasure(player, surface, List.of());
        helper.assertTrue(entries.isEmpty() && level.getBlockEntity(chestPos) instanceof ChestBlockEntity,
                "The same forecast fulfills in a covered water pocket");
        helper.succeed();
    }

    private static void harvestForTreasure(FakePlayer player, BlockPos pos, List<ItemStack> drops) {
        player.serverLevel().getRandom().setSeed(5120);
        Predictions.onHarvest(player, pos, Blocks.DIRT.defaultBlockState(), drops);
    }

    private static BlockPos prepareSoil(GameTestHelper helper) {
        var ground = helper.absolutePos(new BlockPos(6, 8, 6));
        var level = helper.getLevel();
        for (BlockPos pos : BlockPos.betweenClosed(ground.offset(-1, -6, -1), ground.offset(1, -1, 1))) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        for (BlockPos pos : BlockPos.betweenClosed(ground.offset(-1, 0, -1), ground.offset(1, 0, 1))) {
            level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        }
        return ground;
    }

    private static void rollOverdueForecast(FakePlayer player) {
        player.tickCount = 100;
        player.getRandom().setSeed(5120);
        Predictions.tick(player);
    }

    private static CompoundTag forecast(int id, long time) {
        var reading = new CompoundTag();
        reading.putInt("WITCPreID", id);
        reading.putLong("WITCPreTime", time);
        return reading;
    }

    private static ListTag store(Player player, CompoundTag... stored) {
        var entries = new ListTag();
        entries.addAll(List.of(stored));
        var prediction = new CompoundTag();
        prediction.put("WITCPreList", entries);
        var data = new CompoundTag();
        data.put("WITCPredict", prediction);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
        return entries;
    }
}
