package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.BrewItemCurse;
import art.arcane.witchery.registry.WitcheryEffects;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyItemCurseImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void originalAndPreviouslyImportedItemCursesStillCast(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-item-curse-import");
        var level = helper.getLevel();
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var snapshot = TagParser.parseTag("""
                    {ItemData:[{K:"\u0002minecraft:iron_pickaxe",V:257},{K:"\u0002minecraft:nether_wart",V:372},
                               {K:"\u0002minecraft:spider_eye",V:375},{K:"\u0002minecraft:skull",V:397},
                               {K:"\u0002witchery:ingredient",V:4100}]}
                    """);
            var world = access.getLevelPath(LevelResource.ROOT);
            NbtIo.writeCompressed(snapshot, world.resolve("witchery-legacy-ids.dat").toFile());
            var original = TagParser.parseTag("""
                    {Level:{xPos:0,zPos:0,TerrainPopulated:1b,TileEntities:[{id:"Chest",x:1,y:4,z:1,Items:[
                     {id:257s,Damage:27s,Count:1b,Slot:0b,tag:{Fixture:"keepers",display:{Name:"Stored Pickaxe"},
                      WITCCurse:{Count:2,Delay:1L,Curse:{Color:123456,Splash:1b,Items:[
                       {id:372s,Damage:0s,Count:1b},{id:375s,Damage:0s,Count:1b},
                       {id:397s,Damage:2s,Count:1b},{id:4100s,Damage:156s,Count:1b}]}}}}]}]}}
                    """);
            var raw = original.getCompound("Level").getList("TileEntities", Tag.TAG_COMPOUND).getCompound(0)
                    .getList("Items", Tag.TAG_COMPOUND);
            var oldIngredients = BrewItemCurse.savedBrew(raw.getCompound(0).getCompound("tag")).getList("Items", Tag.TAG_COMPOUND);

            try (var storage = new ChunkStorage(world.resolve("region"), level.getServer().getFixerUpper(), true)) {
                var imported = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, original, Optional.empty());
                assertReady(helper, importedPickaxe(imported));
                BrewItemCurse.savedBrew(importedPickaxe(imported).getCompound("tag")).put("Items", oldIngredients.copy());
                var repaired = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, imported, Optional.empty());
                assertReady(helper, importedPickaxe(repaired));
            }

            var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LegacyCurse"));
            var file = access.getLevelPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getStringUUID() + ".dat").toFile();
            Files.createDirectories(file.toPath().getParent());
            var legacyPlayer = TagParser.parseTag("{Dimension:0,Health:20f}");
            legacyPlayer.put("Inventory", raw.copy());
            NbtIo.writeCompressed(legacyPlayer, file);
            var storage = access.createPlayerStorage();
            storage.load(player);
            var held = player.getMainHandItem();
            assertReady(helper, held.save(new CompoundTag()));
            MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND));
            var curse = held.getTag().getCompound("WITCCurse");
            helper.assertTrue(player.hasEffect(MobEffects.POISON) && !player.hasEffect(WitcheryEffects.WOLFSBANE.get())
                            && curse.getInt("Count") == 1 && curse.getLong("Delay") > 1,
                    "The two-level recipe applies Poison, skips the remaining Wolfsbane, and saves its next charge and lockout");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    private static CompoundTag importedPickaxe(CompoundTag chunk) {
        return chunk.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0).getList("Items", Tag.TAG_COMPOUND).getCompound(0);
    }

    private static void assertReady(GameTestHelper helper, CompoundTag saved) {
        var stack = ItemStack.of(saved);
        var tag = stack.getTag();
        helper.assertTrue(stack.is(Items.IRON_PICKAXE) && stack.getDamageValue() == 27 && tag != null
                        && tag.getString("Fixture").equals("keepers") && stack.getHoverName().getString().equals("Stored Pickaxe"),
                "The cursed tool keeps its wear, name and unrelated tag");
        var curse = tag.getCompound("WITCCurse");
        var brew = BrewItemCurse.savedBrew(tag);
        var ids = brew.getList("Items", Tag.TAG_COMPOUND).stream().map(entry -> ((CompoundTag) entry).getString("id")).toList();
        helper.assertTrue(curse.getInt("Count") == 2 && curse.getLong("Delay") == 1 && brew.getInt("Color") == 123456
                        && ids.equals(List.of("minecraft:nether_wart", "minecraft:spider_eye", "minecraft:zombie_head",
                        "witchery:wolfsbane_flower")), "The curse keeps its two charges, precise delay, color and ordered ingredients");
    }
}
