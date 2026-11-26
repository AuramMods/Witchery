package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.block.entity.MirrorBlockEntity;
import art.arcane.witchery.item.DoorKeyItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.WitcheryDimensions;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyBoundItemImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_bound_items")
    public static void restoresCustomDimensionBindingsInModernSaves(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-bound-items");
        try {
            var snapshot = TagParser.parseTag("{Dimensions:{torment:-83}}");
            NbtIo.writeCompressed(snapshot, root.resolve("witchery-legacy-ids.dat").toFile());
            var ids = LegacyWorldIds.find(root);
            var level = helper.getLevel();
            var fixer = level.getServer().getFixerUpper();
            int version = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
            var savedPlayer = TagParser.parseTag("""
                    {Inventory:[{id:"witchery:door_keyring",Slot:5b,Count:1b,tag:{Label:"Original ring",doorKeys:[
                       {doorD:-83,doorX:2,doorY:64,doorZ:3},
                       {doorD:-83,doorDimension:"minecraft:the_nether",doorX:5,doorY:64,doorZ:3},
                       {doorD:-902,doorX:8,doorY:64,doorZ:3}]}}]}
                    """);
            var repaired = ids.upgradePlayer(savedPlayer, fixer, version);
            var ring = ItemStack.of(repaired.getList("Inventory", Tag.TAG_COMPOUND).getCompound(0));
            var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ImportedDoorKey"));
            player.getInventory().setItem(5, ring);
            var door = new BlockPos(2, 64, 3);
            var torment = level.getServer().getLevel(WitcheryDimensions.TORMENT);
            var nether = level.getServer().getLevel(Level.NETHER);
            helper.assertTrue(DoorKeyItem.canOpen(player, torment, door) && !DoorKeyItem.canOpen(player, level, door),
                    "An older numeric address opens only the intended door in the custom Torment dimension");
            helper.assertTrue(DoorKeyItem.canOpen(player, nether, new BlockPos(5, 64, 3))
                            && !DoorKeyItem.canOpen(player, torment, new BlockPos(8, 64, 3))
                            && ring.getTag().getString("Label").equals("Original ring"),
                    "Explicit namespaced and unrecognized dimensions retain their existing behavior and item data");

            var savedChunk = TagParser.parseTag("""
                    {xPos:0,zPos:0,block_entities:[{id:"minecraft:chest",x:4,y:64,z:2,Items:[
                      {id:"witchery:bound_waystone",Count:2b,Slot:3b,tag:{PosX:7,PosY:70,PosZ:9,PosD:-83,Label:"Original stone"}}]}]}
                    """);
            savedChunk.putInt("DataVersion", version);
            try (var storage = new ChunkStorage(root.resolve("region"), fixer, true)) {
                var upgraded = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, savedChunk, Optional.empty());
                var item = upgraded.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0)
                        .getList("Items", Tag.TAG_COMPOUND).getCompound(0);
                var stack = ItemStack.of(item);
                helper.assertTrue(new WaystoneItem.Location(WitcheryDimensions.TORMENT, new BlockPos(7, 70, 9))
                                .equals(WaystoneItem.location(stack)) && item.getByte("Slot") == 3 && stack.getCount() == 2
                                && stack.getTag().getString("Label").equals("Original stone"),
                        "A vanilla chest repairs the Waystone location and retains the original stack and slot");
            }
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_mirror_exit_dimensions")
    public static void repairsMirrorExitsWithOriginalDimensionNumbers(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-mirror-exit-dimensions");
        try {
            var snapshot = TagParser.parseTag("{Dimensions:{dream:-47,torment:-48,mirror:64}}");
            NbtIo.writeCompressed(snapshot, root.resolve("witchery-legacy-ids.dat").toFile());
            var saved = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {xPos:0,zPos:0,block_entities:[
                      {id:"witchery:mirrorblock2",x:4,y:64,z:4,DemonSlain:1b,PlayersSeen:["Traveler"],
                       DimCoords:{posX:94,posY:121,posZ:31,Dimension:-47}},
                      {id:"witchery:mirrorblock2",x:8,y:64,z:4,
                       DimCoords:{posX:102,posY:121,posZ:31,Dimension:-48}},
                      {id:"witchery:mirrorblock2",x:4,y:64,z:8,
                       DimCoords:{posX:42,posY:50,posZ:43,Dimension:-47,DimensionId:"minecraft:the_nether"}}]}
                    """));
            var level = helper.getLevel();
            try (var storage = new ChunkStorage(root.resolve("region"), level.getServer().getFixerUpper(), true)) {
                var loaded = storage.upgradeChunkTag(WitcheryDimensions.MIRROR, level::getDataStorage, saved, Optional.empty());
                var tiles = loaded.getList("block_entities", Tag.TAG_COMPOUND);
                var block = (MirrorBlock) WitcheryBlocks.getRequired("mirrorblock2").get();
                var frame = (MirrorBlockEntity) block.newBlockEntity(new BlockPos(4, 64, 4), block.defaultBlockState());
                frame.load(tiles.getCompound(0));
                helper.assertTrue(GlobalPos.of(WitcheryDimensions.DREAM, new BlockPos(94, 121, 31)).equals(frame.counterpart())
                                && frame.demonSlain()
                                && frame.saveWithoutMetadata().getList("PlayersSeen", Tag.TAG_STRING).getString(0).equals("Traveler"),
                        "Restore the Dream address and keep the conversation and defeated Reflection state");
                frame.load(tiles.getCompound(1));
                helper.assertTrue(GlobalPos.of(WitcheryDimensions.TORMENT, new BlockPos(102, 121, 31)).equals(frame.counterpart()),
                        "Restore the Torment address from its captured original number");
                frame.load(tiles.getCompound(2));
                helper.assertTrue(GlobalPos.of(Level.NETHER, new BlockPos(42, 50, 43)).equals(frame.counterpart()),
                        "Keep an explicitly saved current dimension");
            }
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }
}
