package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitchBroom;
import art.arcane.witchery.entity.WingedMonkey;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
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
    @GameTest(template = "qa_coven_arena", batch = "witchery_earlier_saved_mount")
    public static void restoresAnEarlierPlayersWingedMonkeyMount(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-earlier-mount");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var owner = new GameProfile(UUID.randomUUID(), "MountedLegacyOwner");
            var player = new FakePlayer(helper.getLevel(), owner);
            var mountId = UUID.randomUUID();
            var mount = TagParser.parseTag("""
                    {id:"witchery.wingedmonkey",Pos:[5.5d,4.0d,5.5d],OwnerUUID:"%s",FeatherColor:9b,
                      CustomName:"Morrow Carrier",UUIDMost:%dL,UUIDLeast:%dL,
                      Equipment:[{id:"minecraft:wooden_sword",Count:1b,Damage:7s},{id:"minecraft:leather_boots",Count:1b,Damage:6s}],
                      DropChances:[1.1f,1.2f],HandItems:[{},{id:"minecraft:shield",Count:1b}]}
                    """.formatted(owner.getId(), mountId.getMostSignificantBits(), mountId.getLeastSignificantBits()));
            var vehicle = new CompoundTag();
            vehicle.putUUID("Attach", mountId);
            vehicle.put("Entity", mount);
            var saved = new CompoundTag();
            saved.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            saved.put("RootVehicle", vehicle);
            var storage = access.createPlayerStorage();
            var path = access.getLevelPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getStringUUID() + ".dat");
            NbtIo.writeCompressed(saved, path.toFile());

            var loaded = storage.load(player).getCompound("RootVehicle");
            var entity = EntityType.loadEntityRecursive(loaded.getCompound("Entity"), helper.getLevel(), Function.identity());
            helper.assertTrue(entity instanceof WingedMonkey monkey && monkey.getUUID().equals(loaded.getUUID("Attach"))
                            && monkey.getName().getString().equals("Morrow Carrier")
                            && monkey.isTame() && owner.getId().equals(monkey.getOwnerUUID()) && monkey.color() == 9,
                    "The player storage restores the mount type, attachment, name, owner, and color without a captured registry");
            var monkey = (WingedMonkey) entity;
            var boots = monkey.getItemBySlot(EquipmentSlot.FEET);
            helper.assertTrue(boots.is(Items.LEATHER_BOOTS) && boots.getDamageValue() == 6,
                    "The legacy boots must keep their type and wear in the restored armor slots");
            helper.assertTrue(monkey.getMainHandItem().isEmpty() && monkey.getOffhandItem().is(Items.SHIELD),
                    "Explicit modern hand slots must keep the empty main hand and shield");
            var equipment = monkey.saveWithoutId(new CompoundTag());
            var hand = equipment.getList("HandDropChances", Tag.TAG_FLOAT);
            var armor = equipment.getList("ArmorDropChances", Tag.TAG_FLOAT);
            helper.assertTrue(hand.getFloat(0) == 1.1F && hand.getFloat(1) == Mob.DEFAULT_EQUIPMENT_DROP_CHANCE
                            && armor.getFloat(0) == 1.2F && armor.getFloat(3) == Mob.DEFAULT_EQUIPMENT_DROP_CHANCE,
                    "The original drop chances must keep their slot order and missing slots use the modern default");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void convertsOriginalPlayerRidingChain(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-player-vehicle-import");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var snapshot = TagParser.parseTag("{ItemData:[{K:\"%switchery:witchwood\",V:1900}]}".formatted("\u0001"));
            NbtIo.writeCompressed(snapshot, access.getLevelPath(LevelResource.ROOT).resolve("witchery-legacy-ids.dat").toFile());
            var original = TagParser.parseTag("""
                    {Health:20f,Dimension:0,Riding:{
                      id:"witchery.broom",UUIDMost:1L,UUIDLeast:2L,CustomName:"Legacy Sky Rider",BrushColor:6b,
                      Pos:[5.5d,4.0d,5.5d],Rotation:[43f,0f],
                      Riding:{id:"Boat",UUIDMost:3L,UUIDLeast:4L,Pos:[5.5d,4.0d,5.5d]}}}
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
            helper.assertTrue(boat.getFirstPassenger().getYRot() == 133,
                    "The modern renderer's heading offset preserves the imported broom's direction");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }
}
