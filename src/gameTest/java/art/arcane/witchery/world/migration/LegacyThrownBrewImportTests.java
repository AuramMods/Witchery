package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyThrownBrewImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_thrown_brews")
    public static void legacyThrownBrewsKeepPayloadModeAndOwner(GameTestHelper helper) throws Exception {
        var registry = TagParser.parseTag("{ItemData:[{V:289},{V:372},{V:5000},{V:5001}]}");
        var entries = registry.getList("ItemData", Tag.TAG_COMPOUND);
        entries.getCompound(0).putString("K", "\u0002minecraft:gunpowder");
        entries.getCompound(1).putString("K", "\u0002minecraft:nether_wart");
        entries.getCompound(2).putString("K", "\u0002witchery:brewbottle");
        entries.getCompound(3).putString("K", "\u0002witchery:ingredient");
        var original = TagParser.parseTag("""
                {Level:{xPos:0,zPos:0,TerrainPopulated:1b,Entities:[
                  {id:"witchery.brew",damageValue:60,Pos:[4d,110d,4d],Motion:[0d,0d,0d],Rotation:[0f,0f]},
                  {id:"witchery.brew2",Spell:0b,ownerName:"LegacyBrewer",Pos:[6d,110d,4d],Motion:[0.125d,-0.25d,0.5d],Rotation:[45f,10f],
                   Brew:{id:5000s,Count:1b,Damage:0s,tag:{Color:-654829330,Items:[{id:372s,Count:1b,Damage:0s},
                     {id:5001s,Count:1b,Damage:156s},{id:289s,Count:1b,Damage:0s}]} }},
                  {id:"witchery.brew2",Spell:1b,Pos:[8d,110d,4d],Motion:[0.5d,0d,0d],Rotation:[0f,0f],
                   Brew:{id:5000s,Count:1b,Damage:0s,tag:{Color:1193046,Items:[{id:372s,Count:1b,Damage:0s},
                     {id:5001s,Count:1b,Damage:156s},{id:289s,Count:1b,Damage:0s}]}}}]}}
                """);
        var root = Files.createTempDirectory("witchery-legacy-thrown-brews");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        CompoundTag imported;
        try {
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var fixer = helper.getLevel().getServer().getFixerUpper();
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
                        .getString("id").equals("witchery:thrown_brew")),
                "The original two projectile IDs should resolve to the active thrown brew type");

        var level = helper.getLevel();
        var ink = WitcheryEntities.THROWN_BREW.get().create(level);
        ink.load(entities.getCompound(0));
        helper.assertTrue(ink.getItem().is(WitcheryIngredient.BREW_OF_INK.item()) && !ink.isNoGravity(),
                "Original metadata 60 should retain the fixed Ink brew and gravity");

        var ordinary = WitcheryEntities.THROWN_BREW.get().create(level);
        ordinary.load(entities.getCompound(1));
        var brew = ordinary.getItem().getTag();
        var mixture = BrewMixture.from(brew);
        helper.assertTrue(ordinary.getItem().is(WitcheryItems.getRequired("brewbottle").get()) && mixture != null
                        && mixture.splash() && brew.getInt("Color") == -654829330 && !ordinary.isNoGravity()
                        && ordinary.getDeltaMovement().equals(new Vec3(0.125, -0.25, 0.5)),
                "The original bottle should retain its working mixture, exact color, gravity and velocity");

        var spell = WitcheryEntities.THROWN_BREW.get().create(level);
        spell.load(entities.getCompound(2));
        helper.assertTrue(spell.isNoGravity() && spell.getItem().getTag().getInt("Color") == 1193046,
                "The original spell keeps its no-gravity flight and brew color");
        var beforeLogin = ordinary.saveWithoutId(new CompoundTag());
        helper.assertTrue(ordinary.getOwner() == null && beforeLogin.getString("ownerName").equals("LegacyBrewer"),
                "An offline thrower's name survives a save until the player is available");
        ordinary.load(beforeLogin);
        var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LegacyBrewer"));
        level.addNewPlayer(owner);
        try {
            helper.assertTrue(ordinary.getOwner() == owner
                            && ordinary.saveWithoutId(new CompoundTag()).getUUID("Owner").equals(owner.getUUID()),
                    "A matching player in this level becomes the saved UUID owner");
        } finally {
            owner.discard();
        }
        helper.succeed();
    }
}
