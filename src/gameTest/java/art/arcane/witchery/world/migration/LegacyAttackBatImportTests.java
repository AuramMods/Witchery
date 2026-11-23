package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyAttackBatImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_attack_bat")
    public static void importsLegacyAttackBatOwnerProfile(GameTestHelper helper) throws Exception {
        var original = TagParser.parseTag("""
                {Level:{xPos:-14,zPos:-68,TerrainPopulated:1b,Entities:[
                  {id:"witchery.attackbat",UUIDMost:6033448471933179444L,UUIDLeast:-6983649014379437463L,
                   Pos:[-211.5d,95.5d,-1077.5d],Motion:[0d,0d,0d],Rotation:[0f,0f],BatFlags:1b,Health:6s,
                   Owner:{Name:"LegacyBatOwner",Id:"25315451-cd27-4f35-a7e1-e7cef7ec05e9",
                          Properties:{fixture:[{Value:"legacy-swarm",Signature:"fixture-signature"}]}}}]}}
                """);
        var root = Files.createTempDirectory("witchery-legacy-attack-bat");
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

        var savedEntity = imported.getList("entities", Tag.TAG_COMPOUND).getCompound(0);
        helper.assertTrue(savedEntity.getString("id").equals("witchery:attackbat"), "Keep the original Attack Bat entity type");
        var bat = WitcheryEntities.ATTACK_BAT.get().create(level);
        bat.load(savedEntity);
        var saved = bat.saveWithoutId(new CompoundTag());
        var loaded = WitcheryEntities.ATTACK_BAT.get().create(level);
        loaded.load(saved);
        var owner = loaded.saveWithoutId(new CompoundTag()).getCompound("Owner");
        helper.assertTrue(loaded.getUUID().equals(new UUID(6033448471933179444L, -6983649014379437463L)) && loaded.isResting(),
                "The imported bat should keep its own UUID and resting pose");
        helper.assertTrue(owner.getString("Name").equals("LegacyBatOwner") && owner.hasUUID("Id")
                        && owner.getUUID("Id").equals(UUID.fromString("25315451-cd27-4f35-a7e1-e7cef7ec05e9")),
                "The original GameProfile UUID must survive so the bat can find its caster");
        var property = owner.getCompound("Properties").getList("fixture", Tag.TAG_COMPOUND).getCompound(0);
        helper.assertTrue(property.getString("Value").equals("legacy-swarm")
                        && property.getString("Signature").equals("fixture-signature"),
                "Keep the serialized profile properties when converting the UUID");
        helper.succeed();
    }
}
