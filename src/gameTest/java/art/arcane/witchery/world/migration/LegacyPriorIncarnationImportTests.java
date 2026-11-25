package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyPriorIncarnationImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void preservesExpiredDeathDropsFromOriginalAndPreviouslyImportedPlayers(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-prior-incarnation-import");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var registry = TagParser.parseTag("""
                    {ItemData:[{K:"%sminecraft:wool",V:35},{K:"%sminecraft:diamond_sword",V:276},
                               {K:"%switchery:ingredient",V:4096}]}
                    """.formatted("\u0002", "\u0002", "\u0002"));
            var world = access.getLevelPath(LevelResource.ROOT);
            NbtIo.writeCompressed(registry, world.resolve("witchery-legacy-ids.dat").toFile());
            var storage = access.createPlayerStorage();
            var directory = access.getLevelPath(LevelResource.PLAYER_DATA_DIR);
            var original = TagParser.parseTag("""
                    {Dimension:0,Health:20f,ForgeData:{PlayerPersisted:{WITCPriIncLocX:11.25d,
                     WITCPriIncLocY:64d,WITCPriIncLocZ:-32.75d,WITCPriIncInv:[
                      {id:35s,Count:4b,Damage:14s},
                      {id:276s,Count:1b,Damage:27s,tag:{ench:[{id:16s,lvl:3s}],LegacyKeepsake:1b}},
                      {id:4096s,Count:3b,Damage:26s,tag:{LegacyKeepsake:1b}}]}}}
                    """);
            var converted = original.copy();
            converted.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            converted.putString("Dimension", "minecraft:overworld");
            var drops = converted.getCompound("ForgeData").getCompound(Player.PERSISTED_NBT_TAG)
                    .getList("WITCPriIncInv", Tag.TAG_COMPOUND);
            drops.set(0, TagParser.parseTag("{id:\"minecraft:wool\",Count:4b,Damage:14s}"));
            drops.set(2, TagParser.parseTag("{id:\"witchery:soft_clay_jar\",Count:3b,tag:{LegacyKeepsake:1b}}"));
            drops.add(TagParser.parseTag("{id:\"minecraft:netherite_sword\",Count:1b,tag:{Damage:9}}"));

            var originalPlayer = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "OriginalDrops"));
            NbtIo.writeCompressed(original, directory.resolve(originalPlayer.getStringUUID() + ".dat").toFile());
            storage.load(originalPlayer);
            assertRecovery(helper, originalPlayer, false);

            var convertedPlayer = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ImportedDrops"));
            NbtIo.writeCompressed(converted, directory.resolve(convertedPlayer.getStringUUID() + ".dat").toFile());
            storage.load(convertedPlayer);
            assertRecovery(helper, convertedPlayer, true);
            storage.save(convertedPlayer);
            storage.load(convertedPlayer);
            assertRecovery(helper, convertedPlayer, true);
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    private static void assertRecovery(GameTestHelper helper, FakePlayer player, boolean modernStack) {
        var data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        var drops = data.getList("WITCPriIncInv", Tag.TAG_COMPOUND);
        helper.assertTrue(drops.size() == (modernStack ? 4 : 3) && player.getInventory().isEmpty()
                        && data.getDouble("WITCPriIncLocX") == 11.25 && data.getDouble("WITCPriIncLocY") == 64
                        && data.getDouble("WITCPriIncLocZ") == -32.75,
                "Preserve the expired drops and exact death location without adding items to the living inventory");
        var wool = ItemStack.of(drops.getCompound(0));
        helper.assertTrue(wool.is(Items.RED_WOOL) && wool.getCount() == 4,
                "The expired vanilla wool must retain its original color and count");
        var sword = ItemStack.of(drops.getCompound(1));
        helper.assertTrue(sword.is(Items.DIAMOND_SWORD) && sword.getDamageValue() == 27
                        && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, sword) == 3
                        && sword.getOrCreateTag().getBoolean("LegacyKeepsake"),
                "The expired sword must retain its wear, enchantment, and tag");
        var jars = ItemStack.of(drops.getCompound(2));
        helper.assertTrue(jars.is(WitcheryIngredient.SOFT_CLAY_JAR.item()) && jars.getCount() == 3
                        && jars.getOrCreateTag().getBoolean("LegacyKeepsake"),
                "The expired Witchery stack must keep its subtype, count, and tag");
        if (modernStack) {
            var modern = ItemStack.of(drops.getCompound(3));
            helper.assertTrue(modern.is(Items.NETHERITE_SWORD) && modern.getDamageValue() == 9,
                    "An already modern record must retain its original wear");
        }
    }
}
