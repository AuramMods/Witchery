package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitcheryBolt;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyBoltImportTests {
    private static final int WITCH_LOG_ID = 1450;

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_bolts")
    public static void importsLegacyBoltInItsFlattenedBlock(GameTestHelper helper) throws Exception {
        var original = TagParser.parseTag("""
                {Level:{xPos:-14,zPos:-68,TerrainPopulated:1b,Entities:[
                  {id:"witchery.bolt",UUIDMost:6391217800094107146L,UUIDLeast:-8683977079136577233L,
                   Pos:[-216.015d,12.5d,-1079.5d],Motion:[0.2d,0.0d,0.0d],Rotation:[90.0f,0.0f],
                   xTile:-216s,yTile:12s,zTile:-1080s,inTile:98b,inData:1b,inGround:1b,player:1b,damage:3.75d,boltType:3},
                  {id:"witchery.bolt",Pos:[-215.015d,12.5d,-1079.5d],Motion:[0.2d,0.0d,0.0d],Rotation:[90.0f,0.0f],
                   xTile:-215s,yTile:12s,zTile:-1080s,inTile:-97b,inData:14b,inGround:1b,pickup:0b,player:1b,boltType:1},
                  {id:"witchery.bolt",Pos:[-214.05d,12.5d,-1079.5d],Motion:[0.2d,0.0d,0.0d],Rotation:[90.0f,0.0f],
                   xTile:-214s,yTile:12s,zTile:-1080s,inTile:-86b,inData:6b,inGround:1b,pickup:1b,boltType:3}]}}
                """);
        // The projectile only wrote the low byte. The original section has the actual block ID.
        int index = (12 << 8) | ((-1080 & 15) << 4) | (-214 & 15);
        var section = new CompoundTag();
        var blocks = new byte[4096];
        var high = new byte[2048];
        var metadata = new byte[2048];
        blocks[index] = (byte) WITCH_LOG_ID;
        high[index >> 1] = (byte) (WITCH_LOG_ID >> 8);
        metadata[index >> 1] = 6;
        section.putByteArray("Blocks", blocks);
        section.putByteArray("Add", high);
        section.putByteArray("Data", metadata);
        section.putByteArray("SkyLight", new byte[2048]);
        section.putByteArray("BlockLight", new byte[2048]);
        section.putByte("Y", (byte) 0);
        var sections = new ListTag();
        sections.add(section);
        original.getCompound("Level").put("Sections", sections);
        var root = Files.createTempDirectory("witchery-legacy-bolts");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        var level = helper.getLevel();
        CompoundTag imported;
        try {
            writeBlockMapping(snapshot, "witchlog", WITCH_LOG_ID);
            var fixer = level.getServer().getFixerUpper();
            var intermediate = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, original, -1, 1493,
                    LegacyWorldIds.find(root));
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            imported = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            LegacyChunkUpgrade.finish(imported, Level.OVERWORLD);
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }

        var entities = imported.getList("entities", Tag.TAG_COMPOUND);
        helper.assertTrue(entities.size() == 3 && entities.stream().allMatch(entity -> ((CompoundTag) entity)
                .getString("id").equals("witchery:bolt")), "The imported projectiles must remain Witchery bolts");
        var bolt = WitcheryEntities.BOLT.get().create(level);
        bolt.load(entities.getCompound(0));
        var saved = bolt.saveWithoutId(new CompoundTag());
        helper.assertTrue(saved.getCompound("inBlockState").getString("Name").equals("minecraft:mossy_stone_bricks")
                        && saved.getBoolean("inGround") && bolt.kind() == WitcheryBolt.Kind.HOLY
                        && bolt.pickup == AbstractArrow.Pickup.ALLOWED && bolt.getBaseDamage() == 3.75
                        && bolt.getUUID().equals(new UUID(6391217800094107146L, -8683977079136577233L))
                        && bolt.getDeltaMovement().equals(new Vec3(0.2, 0, 0)),
                "A saved Holy Bolt must keep its original block variant, UUID, damage, movement and pickup mode");
        var restricted = WitcheryEntities.BOLT.get().create(level);
        restricted.load(entities.getCompound(1));
        helper.assertTrue(entities.getCompound(1).getCompound("inBlockState").getString("Name").equals("minecraft:red_terracotta")
                        && restricted.pickup == AbstractArrow.Pickup.DISALLOWED,
                "Unsigned block IDs retain their metadata and an explicit pickup mode takes precedence");
        restricted.load(TagParser.parseTag("{player:1b}"));
        helper.assertTrue(restricted.pickup == AbstractArrow.Pickup.ALLOWED,
                "Ordinary loading also honors the older pickup flag");
        var nativeWitchLog = entities.getCompound(2).getCompound("inBlockState");
        helper.assertTrue(nativeWitchLog.getString("Name").equals("witchery:witchlog")
                        && nativeWitchLog.getCompound("Properties").getString("wood_type").equals("hawthorn")
                        && nativeWitchLog.getCompound("Properties").getString("axis").equals("x"),
                "The native impact cell recovers the full Witchery block number, variant and axis");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_converted_bolt_anchors")
    public static void repairsEarlierBoltAnchorsAndRetainsUnresolvedBlockIds(GameTestHelper helper) throws Exception {
        var entries = TagParser.parseTag("""
                {Entities:[
                  {id:"witchery.bolt",inTile:-94b,inData:5b,inGround:1b,pickup:1b,shake:7b,boltType:3,damage:5.25d},
                  {id:"witchery:bolt",inTile:"witchery:witchlog",inData:6b,inGround:1b},
                  {id:"witchery:bolt",inTile:98b,inData:1b,inGround:1b,inBlockState:{Name:"minecraft:gold_block"}},
                  {id:"witchery:bolt",inTile:1450,inData:6b,inGround:1b,pickup:1b}
                ]}
                """).getList("Entities", Tag.TAG_COMPOUND);
        LegacyChunkEntities.restore(entries);
        LegacyWorldIds.repairNamedStoredItems(entries, helper.getLevel().getServer().getFixerUpper(), true);

        var level = helper.getLevel();
        var log = WitcheryEntities.BOLT.get().create(level);
        log.load(entries.getCompound(0));
        var savedLog = log.saveWithoutId(new CompoundTag());
        helper.assertTrue(savedLog.getCompound("inBlockState").getString("Name").equals("minecraft:dark_oak_log")
                        && savedLog.getCompound("inBlockState").getCompound("Properties").getString("axis").equals("x")
                        && savedLog.getInt("shake") == 7 && log.getBaseDamage() == 5.25
                        && log.kind() == WitcheryBolt.Kind.HOLY && log.pickup == AbstractArrow.Pickup.ALLOWED,
                "An ordinary load restores the embedded block and preserves the projectile's saved state");
        helper.assertTrue(entries.getCompound(1).getCompound("inBlockState").getCompound("Properties")
                        .getString("wood_type").equals("hawthorn") && entries.getCompound(2).getCompound("inBlockState")
                        .getString("Name").equals("minecraft:gold_block"),
                "Named original variants recover and an explicit current block state takes precedence");

        var unknown = WitcheryEntities.BOLT.get().create(level);
        unknown.load(entries.getCompound(3));
        var reopened = new CompoundTag();
        helper.assertTrue(unknown.save(reopened) && reopened.getCompound(LegacyBoltAnchors.PENDING)
                .getInt("Block") == WITCH_LOG_ID, "The unresolved block number survives the bolt's regular save");
        var root = Files.createTempDirectory("witchery-bolt-anchor-registry");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        try {
            writeBlockMapping(snapshot, "witchlog", WITCH_LOG_ID);
            LegacyWorldIds.find(root).repairStoredItems(reopened, level.getServer().getFixerUpper());
            helper.assertTrue(reopened.getCompound("inBlockState").getString("Name").equals("witchery:witchlog")
                            && !reopened.contains(LegacyBoltAnchors.PENDING),
                    "Reopening the save with the captured registry resolves the pending anchor");
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_saved_bolt_attachment", setupTicks = 5)
    public static void keepsTheOriginalImpactCellUntilItsBlockBreaks(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var impact = helper.absolutePos(new BlockPos(6, 4, 5));
        var outside = Vec3.atLowerCornerOf(impact).add(-0.05, 0.48, 0.5);
        var tag = TagParser.parseTag("""
                {id:"witchery:bolt",inTile:"witchery:statueofworship",inData:3b,inGround:1b,pickup:1b,
                 xTile:%d,yTile:%d,zTile:%d,Motion:[0.792d,-0.05d,0.0d]}
                """.formatted(impact.getX(), impact.getY(), impact.getZ()));
        LegacyWorldIds.repairNamedStoredItems(tag, level.getServer().getFixerUpper(), true);
        var state = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), tag.getCompound("inBlockState"));
        level.setBlockAndUpdate(impact, state);
        var bolt = WitcheryEntities.BOLT.get().create(level);
        bolt.load(tag);
        bolt.setPos(outside);
        level.addFreshEntity(bolt);
        try {
            bolt.tick();
            var attached = bolt.saveWithoutId(new CompoundTag());
            helper.assertTrue(attached.getBoolean("inGround") && bolt.position().equals(outside)
                            && attached.getLong(LegacyBoltAnchors.POSITION) == impact.asLong(),
                    "A recovered bolt stays at its original position outside the inset Forge shape");
            level.removeBlock(impact, false);
            bolt.tick();
            var released = bolt.saveWithoutId(new CompoundTag());
            helper.assertTrue(!released.getBoolean("inGround") && !released.contains(LegacyBoltAnchors.POSITION),
                    "Breaking the original impact block releases the bolt and clears its saved attachment");
        } finally {
            bolt.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_boundary_bolt_anchor", setupTicks = 5)
    public static void usesTheLoadedImpactCellToResolveAByteSharedAcrossChunks(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var inside = helper.absolutePos(new BlockPos(6, 4, 5));
        var chunk = new ChunkPos(inside);
        var impact = new BlockPos(chunk.getMinBlockX(), inside.getY(), inside.getZ());
        var outside = Vec3.atLowerCornerOf(impact).add(-0.05, 0.48, 0.5);
        var source = new ChunkPos(BlockPos.containing(outside));
        var original = TagParser.parseTag("""
                {Level:{xPos:%d,zPos:%d,TerrainPopulated:1b,Entities:[
                  {id:"witchery.bolt",xTile:%d,yTile:%d,zTile:%d,inTile:-97b,inData:3b,inGround:1b,
                   Pos:[%sd,%sd,%sd], Motion:[0.792d,-0.05d,0.0d]}
                ]}}
                """.formatted(source.x, source.z, impact.getX(), impact.getY(), impact.getZ(), outside.x, outside.y, outside.z));
        var root = Files.createTempDirectory("witchery-boundary-bolt");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        CompoundTag saved;
        try {
            writeBlockMapping(snapshot, "statueofworship", 415);
            var ids = LegacyWorldIds.find(root);
            var fixer = level.getServer().getFixerUpper();
            var intermediate = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, original, -1, 1493, ids);
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            var imported = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            LegacyChunkUpgrade.finish(imported, Level.OVERWORLD);
            var entry = imported.getList("entities", Tag.TAG_COMPOUND).getCompound(0);
            var bolt = WitcheryEntities.BOLT.get().create(level);
            bolt.load(entry);
            saved = bolt.saveWithoutId(new CompoundTag());
            ids.repairStoredItems(saved, fixer);
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }
        var candidates = saved.getCompound(LegacyBoltAnchors.PENDING)
                .getList(LegacyBoltAnchors.BOUNDARY_STATES, Tag.TAG_COMPOUND);
        var terracotta = Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
        var statue = LegacySchematicBlocks.read("witchery:statueofworship", 3, 0);
        helper.assertTrue(candidates.size() == 2 && candidates.contains(NbtUtils.writeBlockState(terracotta))
                        && candidates.contains(NbtUtils.writeBlockState(statue)) && !saved.contains("inBlockState"),
                "The original and saved Bolt keep both possible mappings without choosing by numeric ID alone");
        var previous = level.getBlockState(impact);
        try {
            for (var state : List.of(statue, terracotta, Blocks.STONE.defaultBlockState())) {
                level.setBlockAndUpdate(impact, state);
                var bolt = WitcheryEntities.BOLT.get().create(level);
                bolt.load(saved);
                helper.assertTrue(bolt.canResolveLegacyBoundaryAnchor(), "The loaded impact cell lets the pending Bolt tick");
                bolt.tick();
                var resolved = bolt.saveWithoutId(new CompoundTag());
                helper.assertTrue(!resolved.contains(LegacyBoltAnchors.PENDING) && !bolt.canResolveLegacyBoundaryAnchor(),
                        "The checked impact cell clears the pending match and its tick exemption");
                if (state.is(Blocks.STONE)) {
                    helper.assertTrue(!resolved.getBoolean("inGround") && !resolved.contains(LegacyBoltAnchors.POSITION),
                            "A different block releases the saved Bolt");
                } else {
                    helper.assertTrue(resolved.getBoolean("inGround") && bolt.position().equals(outside)
                                    && resolved.getCompound("inBlockState").equals(NbtUtils.writeBlockState(state)),
                            "The saved Bolt stays at its original position and records the matching impact state");
                }
            }
        } finally {
            level.setBlockAndUpdate(impact, previous);
        }
        helper.succeed();
    }

    private static void writeBlockMapping(Path target, String name, int id) throws Exception {
        var entry = new CompoundTag();
        entry.putString("K", "\u0001witchery:" + name);
        entry.putInt("V", id);
        var entries = new ListTag();
        entries.add(entry);
        var snapshot = new CompoundTag();
        snapshot.put("ItemData", entries);
        NbtIo.writeCompressed(snapshot, target.toFile());
    }
}
