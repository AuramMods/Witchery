package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.DeathBrewEffects;
import art.arcane.witchery.registry.WitcheryEffects;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyDeathBrewSaveImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void customLegacyEffectsAndCachedInventorySurviveDeath(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-death-brew-import-test");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var world = access.getLevelPath(LevelResource.ROOT);
            var registry = TagParser.parseTag("""
                    {ItemData:[{K:"%sminecraft:wool",V:35},{K:"%sminecraft:iron_helmet",V:306}]}
                    """.formatted("\u0002", "\u0002"));
            registry.put("Potions", LegacyWorldEffects.capture(List.of("I:PotionStartID=96",
                    "I:\"witchery:potion.keepinventory\"=173", "I:\"witchery:potion.sinking\"=174")));
            NbtIo.writeCompressed(registry, world.resolve("witchery-legacy-ids.dat").toFile());
            var storage = access.createPlayerStorage();
            var playerDirectory = access.getLevelPath(LevelResource.PLAYER_DATA_DIR);

            var active = new DropTester(helper.getLevel(), "LegacyBrew");
            var activeRecord = TagParser.parseTag("""
                    {Dimension:0,Health:20f,Inventory:[{Slot:2b,id:388s,Damage:0s,Count:7b}],
                     ActiveEffects:[{Id:-83b,Amplifier:1b,Duration:333,Ambient:1b,ShowParticles:0b},
                                    {Id:1b,Amplifier:0b,Duration:240}],
                     ForgeData:{PlayerPersisted:{WITCPoSpawn:[{Id:-82b,Amplifier:2b,Duration:225}]}}}
                    """);
            NbtIo.writeCompressed(activeRecord, playerDirectory.resolve(active.getStringUUID() + ".dat").toFile());
            storage.load(active);
            var effect = active.getEffect(WitcheryEffects.KEEP_INVENTORY.get());
            helper.assertTrue(effect != null && effect.getAmplifier() == 1 && effect.getDuration() == 333
                            && effect.isAmbient() && !effect.isVisible() && active.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "Restore a configured active effect with its level, duration, and flags alongside a vanilla effect");
            DeathBrewEffects.restoreEffects(active);
            var sinking = active.getEffect(WitcheryEffects.SINKING.get());
            helper.assertTrue(sinking != null && sinking.getAmplifier() == 2 && sinking.getDuration() == 225,
                    "Restore a configured Witchery effect queued before respawn");

            active.captureDrops(new ArrayList<>());
            active.dropEquipmentAtDeath();
            helper.assertTrue(active.captureDrops(null).isEmpty(), "The imported Keep Inventory effect suppresses death drops");
            var restored = new DropTester(helper.getLevel(), "LegacyBrew");
            restored.restoreFrom(active, false);
            helper.assertTrue(restored.getInventory().getItem(2).is(Items.EMERALD)
                            && restored.getInventory().getItem(2).getCount() == 7,
                    "Normal Forge cloning retains the imported player's inventory at death");

            var pending = new DropTester(helper.getLevel(), "LegacyPending");
            var pendingRecord = TagParser.parseTag("""
                    {Dimension:0,Health:0f,WitcheryExtendedPlayer:{CanRestoreInventory:1b,CachedInventory2:[
                      {Slot:5b,id:35s,Damage:14s,Count:4b},{Slot:103b,id:306s,Damage:13s,Count:1b,tag:{Keepsake:1b}}]}}
                    """);
            NbtIo.writeCompressed(pendingRecord, playerDirectory.resolve(pending.getStringUUID() + ".dat").toFile());
            storage.load(pending);
            DeathBrewEffects.restorePendingInventory(pending);
            helper.assertTrue(pending.getInventory().isEmpty(), "A dead player keeps their pending snapshot until respawn");
            storage.save(pending);
            storage.load(pending);
            var afterRestart = new DropTester(helper.getLevel(), "LegacyPending");
            afterRestart.restoreFrom(pending, false);
            DeathBrewEffects.restorePendingInventory(afterRestart);
            var wool = afterRestart.getInventory().getItem(5);
            var helmet = afterRestart.getItemBySlot(EquipmentSlot.HEAD);
            helper.assertTrue(wool.is(Items.RED_WOOL) && wool.getCount() == 4 && helmet.is(Items.IRON_HELMET)
                            && helmet.getDamageValue() == 13 && helmet.getTag() != null && helmet.getTag().getBoolean("Keepsake"),
                    "The pending inventory restores the original slots, metadata, and tagged armor wear");
            afterRestart.getInventory().clearContent();
            DeathBrewEffects.restorePendingInventory(afterRestart);
            helper.assertTrue(afterRestart.getInventory().isEmpty(), "The pending inventory can only be restored once");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void keepsCustomPotionContentsAndRepairsQueuedEffects(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-custom-potion-import-test");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var world = access.getLevelPath(LevelResource.ROOT);
            var registry = TagParser.parseTag("{ItemData:[{K:\"%sminecraft:potion\",V:373}]}".formatted("\u0002"));
            registry.put("Potions", LegacyWorldEffects.capture(List.of("I:PotionStartID=96",
                    "I:\"witchery:potion.paralysed\"=119", "I:\"witchery:potion.sinking\"=43")));
            NbtIo.writeCompressed(registry, world.resolve("witchery-legacy-ids.dat").toFile());
            var storage = access.createPlayerStorage();
            var player = new DropTester(helper.getLevel(), "LegacyPotions");
            var path = access.getLevelPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getStringUUID() + ".dat").toFile();
            var original = TagParser.parseTag("""
                    {Health:20f,Dimension:0,Inventory:[
                      {Slot:0b,id:373s,Damage:0s,Count:1b,tag:{CustomPotionEffects:[
                       {Id:119b,Amplifier:2b,Duration:240,Ambient:1b,ShowParticles:0b},
                       {Id:12b,Amplifier:0b,Duration:800}]}},
                      {Slot:1b,id:"minecraft:potion",Damage:16384s,Count:1b,tag:{CustomPotionEffects:[
                       {Id:43b,Amplifier:1b,Duration:450}]}}]}
                    """);
            NbtIo.writeCompressed(original, path);
            storage.load(player);
            var drink = player.getInventory().getItem(0);
            var splash = player.getInventory().getItem(1);
            var drinkEffects = PotionUtils.getCustomEffects(drink);
            helper.assertTrue(drink.is(Items.POTION) && splash.is(Items.SPLASH_POTION) && drinkEffects.size() == 2,
                    "Both old potion forms and both custom effects must survive the item data fixer");
            var paralysis = drinkEffects.get(0);
            helper.assertTrue(paralysis.getEffect() == WitcheryEffects.PARALYSIS.get() && paralysis.getAmplifier() == 2
                            && paralysis.getDuration() == 240 && paralysis.isAmbient() && !paralysis.isVisible()
                            && drinkEffects.get(1).getEffect() == MobEffects.FIRE_RESISTANCE,
                    "Keep the named Witchery effect, its display flags and the companion vanilla effect");
            var splashEffects = PotionUtils.getCustomEffects(splash);
            helper.assertTrue(splashEffects.size() == 1 && splashEffects.get(0).getEffect() == WitcheryEffects.SINKING.get(),
                    "Resolve the effect previously stored in the old splash bottle");

            var converted = TagParser.parseTag("""
                    {Health:20f,Dimension:"minecraft:overworld",ForgeData:{PlayerPersisted:{WITCPoSpawn:[
                      {Id:43b,Amplifier:3b,Duration:910},
                      {Id:43b,"forge:id":"minecraft:fire_resistance",Amplifier:0b,Duration:720}
                    ]}}}
                    """);
            converted.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            NbtIo.writeCompressed(converted, path);
            storage.load(player);
            DeathBrewEffects.restoreEffects(player);
            var sinking = player.getEffect(WitcheryEffects.SINKING.get());
            helper.assertTrue(sinking != null && sinking.getAmplifier() == 3 && sinking.getDuration() == 910
                            && player.hasEffect(MobEffects.FIRE_RESISTANCE),
                    "Repair the pending numeric effect and preserve the already named effect in a 1.20.1 record");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    private static final class DropTester extends FakePlayer {
        DropTester(ServerLevel level, String name) {
            super(level, new GameProfile(UUID.randomUUID(), name));
        }

        void dropEquipmentAtDeath() {
            dropEquipment();
        }
    }
}
