package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitcheryBolt;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyBoltImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_bolts")
    public static void importsLegacyBoltInItsFlattenedBlock(GameTestHelper helper) throws Exception {
        var original = TagParser.parseTag("""
                {Level:{xPos:-14,zPos:-68,TerrainPopulated:1b,Entities:[
                  {id:"witchery.bolt",UUIDMost:6391217800094107146L,UUIDLeast:-8683977079136577233L,
                   Pos:[-216.015d,12.5d,-1079.5d],Motion:[0.2d,0.0d,0.0d],Rotation:[90.0f,0.0f],
                   xTile:-216s,yTile:12s,zTile:-1080s,inTile:98b,inData:1b,inGround:1b,pickup:1b,damage:3.75d,boltType:3},
                  {id:"witchery.bolt",Pos:[-215.015d,12.5d,-1079.5d],Motion:[0.2d,0.0d,0.0d],Rotation:[90.0f,0.0f],
                   xTile:-215s,yTile:12s,zTile:-1080s,inTile:-97b,inData:14b,inGround:1b,pickup:0b,boltType:1}]}}
                """);
        var root = Files.createTempDirectory("witchery-legacy-bolts");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        var level = helper.getLevel();
        CompoundTag imported;
        try {
            NbtIo.writeCompressed(new CompoundTag(), snapshot.toFile());
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
        helper.assertTrue(entities.size() == 2 && entities.stream().allMatch(entity -> ((CompoundTag) entity)
                .getString("id").equals("witchery:bolt")), "Both imported projectiles must remain Witchery bolts");
        var bolt = WitcheryEntities.BOLT.get().create(level);
        bolt.load(entities.getCompound(0));
        var saved = bolt.saveWithoutId(new CompoundTag());
        helper.assertTrue(saved.getCompound("inBlockState").getString("Name").equals("minecraft:mossy_stone_bricks")
                        && saved.getBoolean("inGround") && bolt.kind() == WitcheryBolt.Kind.HOLY
                        && bolt.pickup == AbstractArrow.Pickup.ALLOWED && bolt.getBaseDamage() == 3.75
                        && bolt.getUUID().equals(new UUID(6391217800094107146L, -8683977079136577233L))
                        && bolt.getDeltaMovement().equals(new Vec3(0.2, 0, 0)),
                "A saved Holy Bolt must keep its original block variant, UUID, damage, movement and pickup mode");
        helper.assertTrue(entities.getCompound(1).getCompound("inBlockState").getString("Name").equals("minecraft:red_terracotta"),
                "The saved block byte must be unsigned so IDs above 127 retain their metadata");
        helper.succeed();
    }
}
