package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
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
public final class LegacyDreamSaveImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void returnedGhostCanWakeWithLegacyInventories(GameTestHelper helper) throws Exception {
        var overworld = helper.getLevel();
        var dream = overworld.getServer().getLevel(WitcheryDimensions.DREAM);
        helper.assertTrue(dream != null, "Expected Dream dimension");
        var anchor = helper.absolutePos(new BlockPos(3, 2, 3));
        overworld.setBlockAndUpdate(anchor.below(), Blocks.STONE.defaultBlockState());
        overworld.setBlockAndUpdate(anchor, Blocks.AIR.defaultBlockState());
        overworld.setBlockAndUpdate(anchor.above(), Blocks.AIR.defaultBlockState());
        var body = WitcheryEntities.SLEEPING_BODY.get().create(overworld);
        helper.assertTrue(body != null, "Expected a sleeping body");
        body.load(TagParser.parseTag("{Owner:\"LegacyDreamer\",Health:14f}"));
        body.setPos(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5);
        overworld.addFreshEntity(body);

        var profile = new GameProfile(UUID.randomUUID(), "LegacyDreamer");
        var player = new FakePlayer(overworld, profile);
        var root = Files.createTempDirectory("witchery-dream-import-test");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var registry = TagParser.parseTag("""
                    {ItemData:[{K:"%sminecraft:wool",V:35},{K:"%sminecraft:diamond_sword",V:276},
                               {K:"%switchery:ingredient",V:7001}]}
                    """.formatted("\u0002", "\u0002", "\u0002"));
            NbtIo.writeCompressed(registry, access.getLevelPath(LevelResource.ROOT).resolve("witchery-legacy-ids.dat").toFile());
            var storage = access.createPlayerStorage();
            var legacy = TagParser.parseTag("""
                    {Dimension:0,Health:10f,Pos:[%s,%s,%s],Inventory:[{Slot:0b,id:7001s,Damage:78s,Count:2b}],
                     ForgeData:{PlayerPersisted:{WITCSpiritWalking:1b,WITCManifested:1b,WITCManifestDuration:60,
                     WITCSpiritWorld:{Nightmare:1b,Demonic:1b,OverworldBodyX:%d,OverworldBodyY:%d,OverworldBodyZ:%d,
                     OverworldHealth:14f,OverworldHunger:{foodLevel:11,foodSaturationLevel:2.5f,foodTickTimer:0},
                     OverworldInventory:[{Slot:0b,id:276s,Damage:17s,Count:1b,tag:{Keepsake:1b}},
                                         {Slot:1b,id:35s,Damage:14s,Count:3b}],
                     SpiritInventory:[{Slot:3b,id:35s,Damage:11s,Count:4b},{Slot:4b,id:7001s,Damage:112s,Count:2b}]}}}}
                    """.formatted(anchor.getX() + 0.5, anchor.getY() + 0.05, anchor.getZ() + 0.5,
                                  anchor.getX(), anchor.getY(), anchor.getZ()));
            NbtIo.writeCompressed(legacy, access.getLevelPath(LevelResource.PLAYER_DATA_DIR).resolve(profile.getId() + ".dat").toFile());
            storage.load(player);
            WitcheryPlayerDataProvider.get(player).ifPresent(data -> DreamTravel.restoreStyle(player, data));
            helper.assertTrue(WitcheryPlayerDataProvider.get(player).map(data -> data.isGhost() && data.getNightmareLevel() == 2).orElse(false),
                    "Restore the manifested appearance and demonic nightmare on load");

            useNeedle(helper, player);
            helper.assertTrue(player.serverLevel() == dream && !DreamTravel.isGhost(player)
                            && player.getInventory().countItem(Items.BLUE_WOOL) == 4,
                    "Returning to Dream restores the parked inventory and original wool variant");
            storage.save(player);
            storage.load(player);
            useNeedle(helper, player);
            var sword = player.getInventory().getItem(0);
            helper.assertTrue(player.serverLevel() == overworld && player.blockPosition().equals(anchor)
                            && sword.is(Items.DIAMOND_SWORD) && sword.getDamageValue() == 17 && !DreamTravel.isSpiritWalking(player)
                            && sword.getTag() != null && sword.getTag().getBoolean("Keepsake")
                            && player.getInventory().countItem(Items.RED_WOOL) == 3
                            && player.getInventory().countItem(WitcheryIngredient.SUBDUED_SPIRIT.item()) == 2
                            && player.getInventory().countItem(Items.BLUE_WOOL) == 0,
                    "Waking restores the body position, item wear, and Overworld slots while carrying only permitted Dream items");
            var session = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("WITCSpiritWorld");
            helper.assertTrue(player.getHealth() == 14 && player.getFoodData().getFoodLevel() == 11 && body.isRemoved()
                            && !session.contains("OverworldInventory") && session.getList("SpiritInventory", Tag.TAG_COMPOUND).size() == 1,
                    "Waking consumes the body inventory record, parks ordinary Dream loot, and restores health and hunger");
        } finally {
            body.discard();
            player.discard();
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    private static void useNeedle(GameTestHelper helper, FakePlayer player) {
        var needle = WitcheryIngredient.ICY_NEEDLE.item();
        helper.assertTrue(player.getMainHandItem().is(needle), "The Icy Needle must survive the current transition");
        needle.use(player.level(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(DreamTravel.processRequests(player), "The Icy Needle must request a Dream transition");
    }
}
