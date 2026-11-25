package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.entity.SleepingBody;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyDreamSaveImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void returnedGhostCanWakeWithLegacyInventories(GameTestHelper helper) throws Exception {
        verifyParkedInventories(helper, false);
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_earlier_dream_inventory")
    public static void earlierPortSaveRecoversBothParkedInventories(GameTestHelper helper) throws Exception {
        verifyParkedInventories(helper, true);
    }

    private static void verifyParkedInventories(GameTestHelper helper, boolean earlierPort) throws Exception {
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
        var trackedBody = WitcheryEntities.SLEEPING_BODY.get().create(overworld);
        helper.assertTrue(trackedBody != null, "Expected a client-tracked body");
        trackedBody.getEntityData().assignValues(body.getEntityData().getNonDefaultValues());
        helper.assertTrue("LegacyDreamer".equals(trackedBody.ownerName()) && trackedBody.ownerId().isEmpty(),
                "Legacy owner name must reach tracking clients without inventing an owner UUID");
        body.setPos(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5);
        helper.assertTrue(overworld.addFreshEntity(body), "The sleeping body joins the Overworld");

        var profile = new GameProfile(UUID.randomUUID(), "LegacyDreamer");
        var player = new FakePlayer(overworld, profile);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(overworld.getEntitiesOfClass(SleepingBody.class,
                        new AABB(anchor).inflate(3), tracked -> tracked.belongsTo(player)).contains(body),
                "The legacy sleeping body is visible to the wake search"))
                .thenExecute(() -> {
                    try {
                        verifyWake(helper, earlierPort, dream, anchor, body, player);
                    } catch (IOException | CommandSyntaxException failure) {
                        throw new IllegalStateException("Could not load the Dream save fixture", failure);
                    }
                }).thenSucceed();
    }

    private static void verifyWake(GameTestHelper helper, boolean earlierPort, ServerLevel dream, BlockPos anchor,
                                   SleepingBody body, FakePlayer player) throws IOException, CommandSyntaxException {
        var overworld = helper.getLevel();
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
                                         {Slot:1b,id:"minecraft:wool",Damage:14s,Count:3b}],
                     SpiritInventory:[{Slot:3b,id:35s,Damage:11s,Count:4b},{Slot:4b,id:7001s,Damage:112s,Count:2b}]}}}}
                    """.formatted(anchor.getX() + 0.5, anchor.getY() + 0.05, anchor.getZ() + 0.5,
                                  anchor.getX(), anchor.getY(), anchor.getZ()));
            if (earlierPort) {
                legacy.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
                legacy.putString("Dimension", "minecraft:overworld");
                var needles = new ItemStack(WitcheryIngredient.ICY_NEEDLE.item(), 2).save(new CompoundTag());
                needles.putByte("Slot", (byte) 0);
                legacy.getList("Inventory", Tag.TAG_COMPOUND).set(0, needles);
                var parked = legacy.getCompound("ForgeData").getCompound(Player.PERSISTED_NBT_TAG)
                        .getCompound("WITCSpiritWorld").getList("OverworldInventory", Tag.TAG_COMPOUND);
                parked.add(TagParser.parseTag("""
                        {Slot:2b,id:"minecraft:netherite_leggings",Count:1b,tag:{Damage:41,
                         display:{Name:'{"text":"Earlier port leggings","italic":false}'},Marker:"preserve"}}
                        """));
            }
            NbtIo.writeCompressed(legacy, access.getLevelPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getUUID() + ".dat").toFile());
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
            if (earlierPort) {
                ItemStack leggings = player.getInventory().getItem(2);
                helper.assertTrue(leggings.is(Items.NETHERITE_LEGGINGS) && leggings.getDamageValue() == 41
                                && leggings.getHoverName().getString().equals("Earlier port leggings")
                                && leggings.getTag().getString("Marker").equals("preserve"),
                        "The already converted Overworld slot keeps its wear and styled name");
            }
            var session = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("WITCSpiritWorld");
            helper.assertTrue(player.getHealth() == 14 && player.getFoodData().getFoodLevel() == 11,
                    "Waking restores health and hunger");
            helper.assertTrue(body.isRemoved(), "Waking removes the original sleeping body");
            helper.assertTrue(!session.contains("OverworldInventory") && session.getList("SpiritInventory", Tag.TAG_COMPOUND).size() == 1,
                    "Waking consumes the body inventory record and parks ordinary Dream loot");
        } finally {
            body.discard();
            player.discard();
            FileUtils.deleteDirectory(root.toFile());
        }
    }

    private static void useNeedle(GameTestHelper helper, FakePlayer player) {
        var needle = WitcheryIngredient.ICY_NEEDLE.item();
        helper.assertTrue(player.getMainHandItem().is(needle), "The Icy Needle must survive the current transition");
        needle.use(player.level(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(DreamTravel.processRequests(player), "The Icy Needle must request a Dream transition");
    }
}
