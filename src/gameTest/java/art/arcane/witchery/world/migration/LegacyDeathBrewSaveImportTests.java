package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.DeathBrewEffects;
import art.arcane.witchery.capability.WitcheryCapabilities;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEffects;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
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
    @GameTest(template = "qa_coven_arena", batch = "witchery_named_player_payloads")
    public static void namedPlayerPayloadsSurviveWithoutCapturedIds(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-named-player-payloads");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var storage = access.createPlayerStorage();
            var dead = new DropTester(helper.getLevel(), "NamedPayloads");
            var record = TagParser.parseTag("""
                    {Dimension:"witchery:mirror",Health:0f,WitcheryExtendedPlayer:{CanRestoreInventory:1b,
                     MirrorWorldEntryPoint:{posX:-22,posY:48,posZ:39},MirrorEscape1:6412L,MirrorEscape2:9020L,
                     CachedInventory2:[{Slot:5b,id:"minecraft:wool",Damage:14s,Count:4b},
                                       {Slot:9b,id:"witchery:ingredient",Damage:22s,Count:3b}]},
                     ForgeData:{PlayerPersisted:{MirrorEscape2:7200L,
                       WITCPriIncInv:[{id:"minecraft:iron_sword",Damage:11s,Count:1b,tag:{Damage:17}}],
                       WITCSpiritWorld:{OverworldInventory:[{Slot:7b,id:"minecraft:planks",Damage:1s,Count:2b}],
                         SpiritInventory:[{Slot:8b,id:"minecraft:copper_ingot",Count:2b,tag:{Marker:1b}}]}}}}
                    """);
            record.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            var path = access.getLevelPath(LevelResource.PLAYER_DATA_DIR).resolve(dead.getStringUUID() + ".dat");
            NbtIo.writeCompressed(record, path.toFile());
            storage.load(dead);
            DeathBrewEffects.restorePendingInventory(dead);
            var persisted = dead.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            helper.assertTrue(persisted.getCompound("MirrorWorldEntryPoint").getInt("posY") == 48
                            && persisted.getLong("MirrorEscape1") == 6412 && persisted.getLong("MirrorEscape2") == 7200,
                    "Recover the saved entrance and legacy timer alongside the active current timer");
            helper.assertTrue(dead.getInventory().isEmpty() && DeathBrewEffects.pendingLegacyInventory(persisted).size() == 2,
                    "Keep both named inventory formats pending while the player is dead");
            var dream = persisted.getCompound("WITCSpiritWorld");
            helper.assertTrue(dream.getList("OverworldInventory", Tag.TAG_COMPOUND).getCompound(0).getString("id")
                            .equals("minecraft:spruce_planks")
                            && dream.getList("SpiritInventory", Tag.TAG_COMPOUND).getCompound(0).getCompound("tag").getBoolean("Marker")
                            && persisted.getList("WITCPriIncInv", Tag.TAG_COMPOUND).getCompound(0).getCompound("tag").getInt("Damage") == 17,
                    "Repair named Dream and Prior Incarnation stacks and keep saved modern data");

            storage.save(dead);
            storage.load(dead);
            var respawned = new DropTester(helper.getLevel(), "NamedRespawn");
            respawned.restoreFrom(dead, false);
            DeathBrewEffects.restorePendingInventory(respawned);
            var wool = respawned.getInventory().getItem(5);
            var rootStack = respawned.getInventory().getItem(9);
            helper.assertTrue(wool.is(Items.RED_WOOL) && wool.getCount() == 4
                            && rootStack.is(WitcheryIngredient.MANDRAKE_ROOT.item()) && rootStack.getCount() == 3,
                    "Respawn restores the rebuilt named cache from the ordinary save");
            respawned.getInventory().clearContent();
            DeathBrewEffects.restorePendingInventory(respawned);
            helper.assertTrue(respawned.getInventory().isEmpty(), "A completed restore consumes the pending inventory");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_unresolved_retained_inventory")
    public static void unresolvedRetainedInventoryRemainsAvailableForRecovery(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-retained-items-with-unknown-ids");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var storage = access.createPlayerStorage();
            var player = new DropTester(helper.getLevel(), "UnknownPayload");
            var record = TagParser.parseTag("""
                    {Dimension:"minecraft:overworld",Health:20f,Inventory:[{Slot:0b,id:"minecraft:paper",Count:1b}],
                     ForgeData:{PlayerPersisted:{WITCLegacyRetainedInventory:[{Slot:4b,id:31234s,Damage:22s,Count:3b}]}}}
                    """);
            record.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            var path = access.getLevelPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getStringUUID() + ".dat");
            NbtIo.writeCompressed(record, path.toFile());
            storage.load(player);
            DeathBrewEffects.restorePendingInventory(player);
            var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            helper.assertTrue(player.getInventory().getItem(0).is(Items.PAPER)
                            && DeathBrewEffects.pendingLegacyInventory(persisted).getCompound(0).getInt("id") == 31234,
                    "A cache with an unresolved stack keeps the current inventory and the original numeric record");
            storage.save(player);

            var registry = TagParser.parseTag("{ItemData:[{K:\"%switchery:ingredient\",V:31234}]}".formatted("\u0002"));
            NbtIo.writeCompressed(registry, access.getLevelPath(LevelResource.ROOT).resolve("witchery-legacy-ids.dat").toFile());
            access.createPlayerStorage().load(player);
            DeathBrewEffects.restorePendingInventory(player);
            helper.assertTrue(player.getInventory().getItem(0).isEmpty()
                            && player.getInventory().getItem(4).is(WitcheryIngredient.MANDRAKE_ROOT.item())
                            && player.getInventory().getItem(4).getCount() == 3,
                    "The original cached inventory becomes restorable when the saved numeric ID can be decoded");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void customLegacyEffectsAndCachedInventorySurviveDeath(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-death-brew-import-test");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var world = access.getLevelPath(LevelResource.ROOT);
            var registry = TagParser.parseTag("""
                    {ItemData:[{K:"%sminecraft:wool",V:35},{K:"%sminecraft:iron_helmet",V:306}]}
                    """.formatted("\u0002", "\u0002"));
            registry.put("Potions", LegacyWorldEffects.capture(List.of("I:PotionStartID=96",
                    "I:\"witchery:potion.keepinventory\"=173", "I:\"witchery:potion.sinking\"=174",
                    "I:\"witchery:potion.paralysed\"=43")));
            NbtIo.writeCompressed(registry, world.resolve("witchery-legacy-ids.dat").toFile());
            var storage = access.createPlayerStorage();
            var playerDirectory = access.getLevelPath(LevelResource.PLAYER_DATA_DIR);

            var active = new DropTester(helper.getLevel(), "LegacyBrew");
            var activeRecord = TagParser.parseTag("""
                    {Dimension:0,Health:20f,Inventory:[{Slot:2b,id:388s,Damage:0s,Count:7b}],
                     ActiveEffects:[{Id:-83b,Amplifier:1b,Duration:333,Ambient:1b,ShowParticles:0b},
                                    {Id:43b,Amplifier:2b,Duration:1800},
                                    {Id:1b,Amplifier:0b,Duration:240}],
                     ForgeData:{PlayerPersisted:{WITCPoSpawn:[{Id:-82b,Amplifier:2b,Duration:225}]}}}
                    """);
            NbtIo.writeCompressed(activeRecord, playerDirectory.resolve(active.getStringUUID() + ".dat").toFile());
            storage.load(active);
            var effect = active.getEffect(WitcheryEffects.KEEP_INVENTORY.get());
            helper.assertTrue(effect != null && effect.getAmplifier() == 1 && effect.getDuration() == 333
                            && effect.isAmbient() && !effect.isVisible() && active.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "Restore a configured active effect with its level, duration, and flags alongside a vanilla effect");
            helper.assertTrue(active.hasEffect(WitcheryEffects.PARALYSIS.get())
                            && active.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0,
                    "The original player upgrade retains the Paralysis modifier through vanilla data fixing");
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

    @GameTest(template = "qa_coven_arena", batch = "witchery_earlier_retained_inventory")
    public static void earlierPortRespawnRecoversBothRetainedInventoryFormats(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-earlier-retained-inventory");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var registry = TagParser.parseTag("""
                    {ItemData:[{K:"%sminecraft:wool",V:35},{K:"%sminecraft:iron_helmet",V:306}]}
                    """.formatted("\u0002", "\u0002"));
            NbtIo.writeCompressed(registry, access.getLevelPath(LevelResource.ROOT).resolve("witchery-legacy-ids.dat").toFile());
            var storage = access.createPlayerStorage();
            var directory = access.getLevelPath(LevelResource.PLAYER_DATA_DIR);
            String capabilityId = WitcheryCapabilities.playerDataId().toString();
            var legacy = TagParser.parseTag("""
                    {PotionThrowing:37,CanRestoreInventory:1b,CachedInventory2:[
                      {Slot:5b,id:35s,Damage:14s,Count:4b},
                      {Slot:103b,id:"minecraft:iron_helmet",Damage:13s,Count:1b,tag:{Keepsake:1b}},
                      {Slot:6b,id:"minecraft:copper_ingot",Count:2b,tag:{AlreadyModern:1b}}]}
                    """);
            var earlier = TagParser.parseTag("{Health:0f,Dimension:\"minecraft:overworld\"}");
            earlier.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());

            var rootRecord = earlier.copy();
            rootRecord.put("WitcheryExtendedPlayer", legacy.copy());
            var wrappedRecord = earlier.copy();
            var caps = new CompoundTag();
            var wrapper = new CompoundTag();
            wrapper.put("WitcheryExtendedPlayer", legacy.copy());
            caps.put(capabilityId, wrapper);
            wrappedRecord.put("ForgeCaps", caps);

            var pendingRecord = earlier.copy();
            var stale = legacy.copy();
            stale.getList("CachedInventory2", Tag.TAG_COMPOUND).getCompound(0).putShort("Damage", (short) 11);
            pendingRecord.put("WitcheryExtendedPlayer", stale);
            var forgeData = new CompoundTag();
            var persisted = new CompoundTag();
            persisted.put("WITCLegacyRetainedInventory", legacy.getList("CachedInventory2", Tag.TAG_COMPOUND).copy());
            forgeData.put(Player.PERSISTED_NBT_TAG, persisted);
            pendingRecord.put("ForgeData", forgeData);
            pendingRecord.put("ForgeCaps", TagParser.parseTag("{\"%s\":{PotionThrowing:64}}".formatted(capabilityId)));

            for (var record : List.of(pendingRecord, rootRecord, wrappedRecord)) {
                var dead = new DropTester(helper.getLevel(), "EarlierDead");
                NbtIo.writeCompressed(record, directory.resolve(dead.getStringUUID() + ".dat").toFile());
                storage.load(dead);
                DeathBrewEffects.restorePendingInventory(dead);
                helper.assertTrue(dead.getInventory().isEmpty(), "The earlier save keeps its inventory pending while dead");
                int throwing = record == pendingRecord ? 64 : 37;
                helper.assertTrue(WitcheryPlayerDataProvider.get(dead).orElseThrow(IllegalStateException::new)
                                .serializeNBT().getInt("PotionThrowing") == throwing,
                        "Keep the existing capability or recover the earlier throwing skill");
                storage.save(dead);
                storage.load(dead);
                var respawned = new DropTester(helper.getLevel(), "EarlierRespawn");
                respawned.restoreFrom(dead, false);
                DeathBrewEffects.restorePendingInventory(respawned);
                var wool = respawned.getInventory().getItem(5);
                var helmet = respawned.getItemBySlot(EquipmentSlot.HEAD);
                var copper = respawned.getInventory().getItem(6);
                helper.assertTrue(wool.is(Items.RED_WOOL) && wool.getCount() == 4
                                && helmet.is(Items.IRON_HELMET) && helmet.getDamageValue() == 13 && helmet.getTag().getBoolean("Keepsake")
                                && copper.is(Items.COPPER_INGOT) && copper.getCount() == 2 && copper.getTag().getBoolean("AlreadyModern"),
                        "Respawn restores both legacy item layouts and the modern stack to their original slots");
                respawned.getInventory().clearContent();
                storage.save(respawned);
                storage.load(respawned);
                DeathBrewEffects.restorePendingInventory(respawned);
                helper.assertTrue(respawned.getInventory().isEmpty(), "A consumed cache stays consumed after saving and loading");
            }

            var dormant = new DropTester(helper.getLevel(), "DormantCache");
            var dormantRecord = earlier.copy();
            dormantRecord.putFloat("Health", 20);
            var disabled = legacy.copy();
            disabled.putBoolean("CanRestoreInventory", false);
            dormantRecord.put("WitcheryExtendedPlayer", disabled);
            NbtIo.writeCompressed(dormantRecord, directory.resolve(dormant.getStringUUID() + ".dat").toFile());
            storage.load(dormant);
            DeathBrewEffects.restorePendingInventory(dormant);
            helper.assertTrue(dormant.getInventory().isEmpty(), "A cache without the original restore flag stays dormant");
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

    @GameTest(template = "qa_coven_arena", batch = "witchery_converted_potion_items")
    public static void repairsNumericPotionEffectsInConvertedPlayerInventory(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-converted-potion-test");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var world = access.getLevelPath(LevelResource.ROOT);
            var registry = TagParser.parseTag("{Potions:{\"potion.sinking\":120}}");
            NbtIo.writeCompressed(registry, world.resolve("witchery-legacy-ids.dat").toFile());
            var storage = access.createPlayerStorage();
            var player = new DropTester(helper.getLevel(), "ConvertedPotions");
            var path = access.getLevelPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getStringUUID() + ".dat").toFile();
            var converted = TagParser.parseTag("""
                    {Health:20f,Dimension:"minecraft:overworld",Inventory:[
                      {Slot:0b,id:"minecraft:potion",Count:1b,tag:{CustomPotionEffects:[
                        {Id:120b,Amplifier:1b,Duration:900,Ambient:1b,ShowParticles:0b},
                        {Id:12b,Amplifier:0b,Duration:800}]}},
                      {Slot:1b,id:"minecraft:splash_potion",Count:1b,tag:{CustomPotionEffects:[{Id:120b,Duration:900}]}},
                      {Slot:2b,id:"minecraft:lingering_potion",Count:1b,tag:{CustomPotionEffects:[{Id:120b,Duration:900}]}},
                      {Slot:3b,id:"minecraft:tipped_arrow",Count:16b,tag:{CustomPotionEffects:[{Id:120b,Duration:900}]}},
                      {Slot:4b,id:"minecraft:potion",Count:1b,tag:{CustomPotionEffects:[
                        {Id:32b,Duration:1000},{Id:33b,Duration:200}]}},
                      {Slot:5b,id:"minecraft:potion",Count:1b,tag:{CustomPotionEffects:[
                        {Id:120b,"forge:id":"minecraft:fire_resistance",Duration:705},{Id:120,Duration:690}]}}
                    ]}
                    """);
            converted.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            NbtIo.writeCompressed(converted, path);
            storage.load(player);
            var inventory = player.getInventory();
            var forms = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW);
            for (int slot = 0; slot < forms.size(); slot++) {
                var stack = inventory.getItem(slot);
                var effects = PotionUtils.getCustomEffects(stack);
                helper.assertTrue(stack.is(forms.get(slot)) && !effects.isEmpty()
                                && effects.get(0).getEffect() == WitcheryEffects.SINKING.get() && effects.get(0).getDuration() == 900,
                        "Recover the captured 1.7.10 effect in item slot " + slot);
            }
            var drink = PotionUtils.getCustomEffects(inventory.getItem(0));
            helper.assertTrue(drink.get(0).getAmplifier() == 1 && drink.get(0).isAmbient() && !drink.get(0).isVisible()
                            && drink.get(1).getEffect() == MobEffects.FIRE_RESISTANCE && inventory.getItem(3).getCount() == 16,
                    "Keep the amplifier, display flags, companion effect and arrow count");
            var vanilla = PotionUtils.getCustomEffects(inventory.getItem(4));
            helper.assertTrue(vanilla.size() == 2 && vanilla.get(0).getEffect() == MobEffects.HERO_OF_THE_VILLAGE
                            && vanilla.get(1).getEffect() == MobEffects.DARKNESS,
                    "Preserve the modern vanilla effects sharing original Witchery's numbers");
            var originalControl = converted.getList("Inventory", Tag.TAG_COMPOUND).getCompound(5).getCompound("tag");
            helper.assertTrue(originalControl.equals(inventory.getItem(5).getTag()),
                    "Keep the explicit registered name and modern integer encoding unchanged");
            var remainder = inventory.getItem(0).finishUsingItem(helper.getLevel(), player);
            var applied = player.getEffect(WitcheryEffects.SINKING.get());
            helper.assertTrue(remainder.is(Items.GLASS_BOTTLE) && applied != null && applied.getAmplifier() == 1
                            && applied.getDuration() == 900 && player.hasEffect(MobEffects.FIRE_RESISTANCE),
                    "Drinking the restored potion applies both effects and returns its glass bottle");
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
