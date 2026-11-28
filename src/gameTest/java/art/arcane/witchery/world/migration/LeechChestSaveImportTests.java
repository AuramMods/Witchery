package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.LegacyPairingChestBlock;
import art.arcane.witchery.block.entity.RefillingChestBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LeechChestSaveImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void legacyHalvesReconnectWithoutLosingTheirInventories(GameTestHelper helper) {
        verifyImportedPair(helper, "leechchest");
    }

    @GameTest(template = "qa_coven_arena")
    public static void legacyRefillingChestHalvesReconnect(GameTestHelper helper) {
        verifyImportedPair(helper, "refillingchest");
    }

    @GameTest(template = "qa_coven_arena")
    public static void earlierPortChestsKeepTheirNamesAndContents(GameTestHelper helper) throws Exception {
        var original = TagParser.parseTag("""
                {xPos:0,zPos:0,block_entities:[
                  {id:"witchery:leechchest",x:1,y:4,z:1,CustomName:"Leech Samples",
                    WITCPlayers:[{Player:"Keeper"}],Items:[{id:"minecraft:diamond",Count:2b,Slot:5b}]},
                  {id:"witchery:refillingchest",x:2,y:4,z:1,CustomName:"Torment Cache",
                    WITCLifeTicks:71998L,Items:[{id:"minecraft:diamond",Count:2b,Slot:5b}]},
                  {id:"witchery:leechchest",x:3,y:4,z:1,CustomName:'{"text":"Styled Chest","color":"gold"}',
                    Items:[{id:"minecraft:diamond",Count:2b,Slot:5b}]}]}
                """);
        original.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        var level = helper.getLevel();
        var root = Files.createTempDirectory("witchery-chest-names");
        try (var storage = new ChunkStorage(root.resolve("region"), level.getServer().getFixerUpper(), true)) {
            var repaired = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, original, Optional.empty());
            var tiles = repaired.getList("block_entities", Tag.TAG_COMPOUND);
            var names = List.of(Component.literal("Leech Samples"), Component.literal("Torment Cache"),
                    Component.literal("Styled Chest").withStyle(ChatFormatting.GOLD));
            for (int index = 0; index < names.size(); index++) {
                var tile = tiles.getCompound(index);
                var state = WitcheryBlocks.getRequired(tile.getString("id").substring("witchery:".length())).get().defaultBlockState();
                var chest = (ChestBlockEntity) BlockEntity.loadStatic(new BlockPos(tile.getInt("x"), 4, 1), state, tile);
                helper.assertTrue(chest != null && names.get(index).equals(chest.getCustomName())
                                && chest.getItem(5).is(Items.DIAMOND) && chest.getItem(5).getCount() == 2,
                        "Keep the name and inventory of " + tile.getString("id") + " at " + index);
                tiles.set(index, chest.saveWithFullMetadata());
            }
            helper.assertTrue(tiles.getCompound(0).getList("WITCPlayers", Tag.TAG_COMPOUND).getCompound(0)
                            .getString("Player").equals("Keeper") && tiles.getCompound(1).getLong("WITCLifeTicks") == 71998L,
                    "Keep the Leech Chest sample and the Refilling Chest timer on save");
            var reopened = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, repaired.copy(), Optional.empty());
            helper.assertTrue(tiles.equals(reopened.getList("block_entities", Tag.TAG_COMPOUND)),
                    "The corrected chest data survives another chunk load");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    private static void verifyImportedPair(GameTestHelper helper, String path) {
        var level = helper.getLevel();
        var registry = WitcheryBlocks.getRequired(path);
        var chest = (ChestBlock) registry.get();
        var pending = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),
                LegacyChunkUpgrade.restoreState(registry.getId(), 2, 0));
        BlockPos left = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockPos right = left.east();
        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        level.setBlock(left, pending, flags);
        level.setBlock(right, pending, flags);
        ((ChestBlockEntity) level.getBlockEntity(left)).setItem(5, new ItemStack(Items.DIAMOND));
        ((ChestBlockEntity) level.getBlockEntity(right)).setItem(7, new ItemStack(Items.GOLD_INGOT));

        // Chunk post-processing resolves one block at a time and can see an already resolved neighbor.
        level.setBlock(left, Block.updateFromNeighbourShapes(pending, level, left), flags);
        level.setBlock(right, Block.updateFromNeighbourShapes(pending, level, right), flags);
        var leftState = level.getBlockState(left);
        var rightState = level.getBlockState(right);
        helper.assertTrue(leftState.getValue(ChestBlock.TYPE) == ChestType.LEFT && rightState.getValue(ChestBlock.TYPE) == ChestType.RIGHT
                        && !leftState.getValue(LegacyPairingChestBlock.LEGACY_PAIRING)
                        && !rightState.getValue(LegacyPairingChestBlock.LEGACY_PAIRING),
                "An original pair resolves into complementary vanilla chest halves");
        var joined = ChestBlock.getContainer(chest, rightState, level, right, true);
        if (path.equals("leechchest")) {
            helper.assertTrue(joined == level.getBlockEntity(right) && joined.getContainerSize() == 27
                            && joined.getItem(7).is(Items.GOLD_INGOT), "The opened half exposes only its 27 slots");
            verifyLeechHalf(helper, chest, left, right);
        } else {
            helper.assertTrue(joined != null && joined.getContainerSize() == 54 && joined.getItem(7).is(Items.GOLD_INGOT)
                            && joined.getItem(32).is(Items.DIAMOND), "The joined cache exposes both original inventories");
        }

        level.setBlockAndUpdate(left, Blocks.AIR.defaultBlockState());
        helper.assertTrue(level.getBlockState(right).getValue(ChestBlock.TYPE) == ChestType.SINGLE
                        && ((ChestBlockEntity) level.getBlockEntity(right)).getItem(7).is(Items.GOLD_INGOT),
                "Removing one half keeps the other's contents and returns it to a single chest");
        level.setBlock(left, pending, flags);
        var single = Block.updateFromNeighbourShapes(pending, level, left);
        helper.assertTrue(single.getValue(ChestBlock.TYPE) == ChestType.SINGLE
                        && !single.getValue(LegacyPairingChestBlock.LEGACY_PAIRING),
                "An unpaired original chest leaves a neighboring single chest independent");
        helper.succeed();
    }

    private static void verifyLeechHalf(GameTestHelper helper, ChestBlock chest, BlockPos left, BlockPos right) {
        var level = helper.getLevel();
        var leftState = level.getBlockState(left);
        var rightState = level.getBlockState(right);
        var leftChest = (ChestBlockEntity) level.getBlockEntity(left);
        var player = helper.makeMockSurvivalPlayer();
        player.setPos(Vec3.atBottomCenterOf(left));
        var sounds = new ArrayList<PlayLevelSoundEvent.AtPosition>();
        Consumer<PlayLevelSoundEvent.AtPosition> listener = event -> {
            if (event.getLevel() == level && event.getPosition().distanceToSqr(Vec3.atCenterOf(left)) < 1) sounds.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            var menu = (ChestMenu) chest.getMenuProvider(leftState, level, left).createMenu(0, player.getInventory(), player);
            try {
                helper.assertTrue(menu.getContainer() == leftChest && menu.getRowCount() == 3
                                && leftState.getSignal(level, left, Direction.NORTH) == 1
                                && leftState.getDirectSignal(level, left, Direction.UP) == 1
                                && leftState.getDirectSignal(level, left, Direction.DOWN) == 0
                                && rightState.getSignal(level, right, Direction.NORTH) == 0,
                        "The opened half provides the inventory and its own trapped-chest power");
            } finally {
                menu.removed(player);
            }
            helper.assertTrue(leftState.getSignal(level, left, Direction.NORTH) == 0 && sounds.size() == 2
                            && sounds.stream().allMatch(sound -> sound.getPosition().equals(Vec3.atCenterOf(left)))
                            && sounds.stream().map(sound -> sound.getSound().value()).toList()
                            .equals(List.of(SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE)),
                    "An adjacent chest has its own opening and closing sounds at its center");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        level.setBlockAndUpdate(right.above(), Blocks.GLASS.defaultBlockState());
        helper.assertTrue(chest.getMenuProvider(leftState, level, left) != null,
                "A transparent cover leaves the neighboring Leech Chest openable");
        level.setBlock(right.above(), Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 8),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        helper.assertTrue(chest.getMenuProvider(leftState, level, left) == null,
                "A full snow layer blocks the neighboring lid");
        level.setBlockAndUpdate(right.above(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(chest.getMenuProvider(leftState, level, left) == null
                        && leftState.getAnalogOutputSignal(level, left) == 0,
                "An obstruction above an adjacent Leech Chest blocks the menu and comparator");
        var handler = leftChest.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN).orElseThrow(IllegalStateException::new);
        helper.assertTrue(handler.getSlots() == 27 && handler.getStackInSlot(5).is(Items.DIAMOND),
                "Automation still reads the local inventory when the lid is blocked");
        level.removeBlock(right.above(), false);
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_refilling_chest_clock")
    public static void refillingChestUsesTheOriginalClock(GameTestHelper helper) {
        var level = helper.getLevel();
        var torment = level.getServer().getLevel(WitcheryDimensions.TORMENT);
        helper.assertTrue(torment != null, "Torment is loaded");
        var pos = new BlockPos(15, 80, 15);
        var state = WitcheryBlocks.getRequired("refillingchest").get().defaultBlockState();
        var before = new CompoundTag();
        before.putLong("WITCLifeTicks", 71_998);
        var cache = new RefillingChestBlockEntity(pos, state);
        cache.setLevel(torment);
        cache.load(before);
        RefillingChestBlockEntity.tick(torment, pos, state, cache);
        helper.assertTrue(cache.isEmpty() && cache.saveWithoutMetadata().getLong("WITCLifeTicks") == 71_999,
                "The earlier tick keeps the cache empty");
        cache.load(cache.saveWithoutMetadata());
        RefillingChestBlockEntity.tick(torment, pos, state, cache);
        helper.assertTrue(!cache.isEmpty() && cache.saveWithoutMetadata().getLong("WITCLifeTicks") == 72_000,
                "The resumed clock refills on the tick that reaches one hour");
        var outside = new RefillingChestBlockEntity(pos, state);
        outside.setLevel(level);
        before.putLong("WITCLifeTicks", Long.MAX_VALUE);
        outside.load(before);
        RefillingChestBlockEntity.tick(level, pos, state, outside);
        helper.assertTrue(outside.isEmpty() && outside.saveWithoutMetadata().getLong("WITCLifeTicks") == 2,
                "The counter wraps outside Torment and the inventory remains empty");
        helper.succeed();
    }
}
