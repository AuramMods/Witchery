package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitchBroom;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Function;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyPlayerVehicleImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void convertsOriginalPlayerRidingChain(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-player-vehicle-import");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var snapshot = TagParser.parseTag("{ItemData:[{K:\"%switchery:witchwood\",V:1900}]}".formatted("\u0001"));
            NbtIo.writeCompressed(snapshot, access.getLevelPath(LevelResource.ROOT).resolve("witchery-legacy-ids.dat").toFile());
            var original = TagParser.parseTag("""
                    {Health:20f,Dimension:0,Riding:{
                      id:"witchery.broom",UUIDMost:1L,UUIDLeast:2L,CustomName:"Legacy Sky Rider",BrushColor:6b,
                      Pos:[5.5d,4.0d,5.5d],Riding:{id:"Boat",UUIDMost:3L,UUIDLeast:4L,Pos:[5.5d,4.0d,5.5d]}}}
                    """);
            var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "MountedImport"));
            var storage = access.createPlayerStorage();
            var path = access.getLevelPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getStringUUID() + ".dat");
            NbtIo.writeCompressed(original, path.toFile());

            var loaded = storage.load(player);
            var vehicle = loaded.getCompound("RootVehicle");
            var attachment = new UUID(1L, 2L);
            helper.assertTrue(vehicle.hasUUID("Attach") && vehicle.getUUID("Attach").equals(attachment),
                    "The login must attach the player to the broom inside the vehicle tree");
            var boat = EntityType.loadEntityRecursive(vehicle.getCompound("Entity"), helper.getLevel(), Function.identity());
            helper.assertTrue(boat != null && boat.getType() == EntityType.BOAT && boat.getUUID().equals(new UUID(3L, 4L))
                            && boat.getPassengers().size() == 1,
                    "The outer mount must keep its original type and identity");
            helper.assertTrue(boat.getFirstPassenger() instanceof WitchBroom broom && broom.getUUID().equals(attachment)
                            && broom.brushColor() == 6 && broom.getName().getString().equals("Legacy Sky Rider"),
                    "The inner broom must retain its dye, custom name and identity");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }
}
