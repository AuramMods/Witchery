package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.KettleBlockEntity;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SavedBrewingItemsTests {
    @GameTest(template = "qa_coven_arena")
    public static void restoresRawStacksInConvertedWorkstations(GameTestHelper helper) throws Exception {
        var world = Files.createTempDirectory("witchery-saved-brewing-items");
        var level = helper.getLevel();
        var fixer = level.getServer().getFixerUpper();
        try {
            var snapshot = TagParser.parseTag("""
                    {ItemData:[{K:"\u0002minecraft:nether_wart",V:372},{K:"\u0002minecraft:spider_eye",V:375},
                     {K:"\u0002minecraft:potion",V:373},{K:"\u0002witchery:ingredient",V:4100}]}
                    """);
            NbtIo.writeCompressed(snapshot, world.resolve("witchery-legacy-ids.dat").toFile());
            var chunk = TagParser.parseTag("""
                    {xPos:0,zPos:0,block_entities:[
                     {id:"witchery:cauldron",x:1,y:4,z:1,Tank:{FluidName:"witchery:brew",Amount:2500},Color:123456,HeatTicks:100,
                      Ingredients:[{id:372s,Count:1b,Damage:0s},{id:4100s,Count:1b,Damage:156s}]},
                     {id:"witchery:cbuttonstone",x:2,y:4,z:1,Count:1,Thrower:"LegacyBrewer",Color:654321,Duration:20,Expansion:6,
                      Effect:{Splash:1b,Items:[{id:372s,Count:1b,Damage:0s},{id:375s,Count:1b,Damage:0s},
                        {id:"minecraft:skull",Count:1b,Damage:2s}]}},
                     {id:"witchery:kettle",x:3,y:4,z:1,Tank:{FluidName:"minecraft:water",Amount:1000},Items:[
                      {id:373s,Count:1b,Damage:8198s,Slot:1b,tag:{Fixture:"old-potion"}},
                      {id:"witchery:wolfsbane_flower",Count:1b,Slot:3b,tag:{Fixture:"modern",
                       display:{Name:'{"text":"Keep this style","color":"gold"}'}}}]}]}
                    """);
            chunk.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            try (var storage = new ChunkStorage(world.resolve("region"), fixer, true)) {
                var updated = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, chunk, Optional.empty());
                var pot = tile(updated, 0);
                var expected = List.of("minecraft:nether_wart", "witchery:wolfsbane_flower");
                helper.assertTrue(itemIds(pot, "Ingredients").equals(expected) && pot.getInt("Color") == 123456
                                && pot.getInt("HeatTicks") == 100 && pot.getCompound("Tank").getInt("Amount") == 2500,
                        "The recovered cauldron retains its ordered brew, heat, color and fluid amount");

                var control = tile(updated, 1);
                var effect = control.getCompound("Effect");
                helper.assertTrue(itemIds(effect, "Items").equals(List.of("minecraft:nether_wart", "minecraft:spider_eye",
                                "minecraft:zombie_head")) && BrewMixture.from(effect).delivery() == BrewMixture.Delivery.TRIGGERED
                                && control.getInt("Count") == 1 && control.getInt("Color") == 654321
                                && control.getInt("Duration") == 20 && control.getInt("Expansion") == 6
                                && control.getString("Thrower").equals("LegacyBrewer"),
                        "The recovered control keeps its complete brew and saved trigger state");
                var buttonSite = new BlockPos(5, 2, 4);
                var button = WitcheryBlocks.getRequired("cbuttonstone").get();
                helper.setBlock(buttonSite.below(), Blocks.STONE);
                helper.setBlock(buttonSite, button.defaultBlockState().setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR));
                helper.getBlockEntity(buttonSite).load(control);
                var player = helper.makeMockSurvivalPlayer();
                helper.useBlock(buttonSite, player);
                helper.assertTrue(player.hasEffect(MobEffects.POISON) && helper.getBlockState(buttonSite).is(Blocks.STONE_BUTTON),
                        "The recovered button releases Poison and becomes a vanilla button on its last charge");

                var kettleSite = new BlockPos(9, 2, 4);
                helper.setBlock(kettleSite, WitcheryBlocks.getRequired("kettle").get());
                var kettle = (KettleBlockEntity) helper.getBlockEntity(kettleSite);
                kettle.load(tile(updated, 2));
                var inventory = kettle.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
                var potion = inventory.getStackInSlot(1);
                helper.assertTrue(potion.is(Items.POTION) && PotionUtils.getPotion(potion) == Potions.NIGHT_VISION
                                && potion.getTag().getString("Fixture").equals("old-potion"),
                        "The kettle keeps the legacy potion variant and its original slot and tag");
                var current = inventory.getStackInSlot(3);
                helper.assertTrue(current.is(WitcheryIngredient.WOLFSBANE.item()) && current.getTag().getString("Fixture").equals("modern")
                                && current.getTagElement("display").getString("Name").contains("\"color\":\"gold\""),
                        "An adjacent modern stack keeps its explicit style and data");
                var reopened = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, updated.copy(), Optional.empty());
                helper.assertTrue(updated.get("block_entities").equals(reopened.get("block_entities")),
                        "The repaired workstation tags stay identical on a second load");
            }
        } finally {
            FileUtils.deleteDirectory(world.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void recoversAPendingCastFromABrewBucketInAVanillaChest(GameTestHelper helper) throws Exception {
        var world = Files.createTempDirectory("witchery-saved-brew-bucket");
        var level = helper.getLevel();
        var fixer = level.getServer().getFixerUpper();
        try {
            var snapshot = TagParser.parseTag("""
                    {ItemData:[{K:"\u0002minecraft:nether_wart",V:372},{K:"\u0002minecraft:sugar",V:353},
                     {K:"\u0002witchery:taglockkit",V:4097}]}
                    """);
            NbtIo.writeCompressed(snapshot, world.resolve("witchery-legacy-ids.dat").toFile());
            var chunk = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {xPos:0,zPos:0,block_entities:[{id:"minecraft:chest",x:0,y:4,z:0,Items:[
                      {id:"witchery:bucketbrew",Count:1b,Slot:4b,tag:{Color:123456,RitualTriggered:1b,Fixture:"Saved batch",Items:[
                        {id:372s,Count:1b,Damage:0s},{id:353s,Count:2b,Damage:0s},
                        {id:4097s,Count:1b,Damage:0s,tag:{WITCEntityIDm1:42L,WITCEntityIDl1:117L,WITCEntityName1:"Bound pig"}}]}},
                      {id:"minecraft:book",Count:1b,Slot:5b,tag:{Fixture:"Unrelated book"}}]}]}
                    """));
            try (var storage = new ChunkStorage(world.resolve("region"), fixer, true)) {
                var updated = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, chunk, Optional.empty());
                var inventory = tile(updated, 0).getList("Items", Tag.TAG_COMPOUND);
                var stored = inventory.getCompound(0);
                var tag = stored.getCompound("tag");
                helper.assertTrue(stored.getInt("Slot") == 4 && tag.getInt("Color") == 123456
                                && tag.getString("Fixture").equals("Saved batch") && tag.getBoolean("RitualTriggered")
                                && inventory.getCompound(1).getCompound("tag").getString("Fixture").equals("Unrelated book"),
                        "The chest keeps the Brew Bucket's slot, color and pending cast alongside the ordinary stack");
                var bucket = ItemStack.of(stored);
                var bowl = new BlockPos(5, 2, 4);
                helper.setBlock(bowl, WitcheryBlocks.getRequired("cauldron").get());
                var player = helper.makeMockSurvivalPlayer();
                player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
                helper.useBlock(bowl, player);
                var saved = helper.getBlockEntity(bowl).saveWithoutMetadata();
                var ingredients = saved.getList("Ingredients", Tag.TAG_COMPOUND);
                helper.assertTrue(player.getMainHandItem().is(Items.BUCKET) && saved.getInt("Color") == 123456
                                && saved.getBoolean("LegacyRitualPending") && saved.getCompound("Tank").getInt("Amount") == 1000
                                && itemIds(saved, "Ingredients").equals(List.of("minecraft:nether_wart", "minecraft:sugar", "witchery:taglockkit"))
                                && ingredients.getCompound(1).getByte("Count") == 2
                                && ingredients.getCompound(2).getCompound("tag").equals(tag.getList("Items", Tag.TAG_COMPOUND)
                                        .getCompound(2).getCompound("tag")),
                        "Pouring the recovered bucket transfers the full ordered cast, including the bound Taglock. Found " + saved);
                var reopened = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, updated.copy(), Optional.empty());
                helper.assertTrue(tile(updated, 0).equals(tile(reopened, 0)), "Reopening the chest keeps both saved stacks");
            }
        } finally {
            FileUtils.deleteDirectory(world.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_mixed_projectile_save")
    public static void repairsAnOriginalBottleStillInsideAConvertedProjectile(GameTestHelper helper) throws Exception {
        var world = Files.createTempDirectory("witchery-saved-brew-projectile");
        var level = helper.getLevel();
        ThrownBrew projectile;
        try {
            var snapshot = TagParser.parseTag("""
                    {ItemData:[{K:"\u0002minecraft:nether_wart",V:372},{K:"\u0002minecraft:gunpowder",V:289},
                     {K:"\u0002witchery:ingredient",V:4096},{K:"\u0002witchery:brewbottle",V:4100}]}
                    """);
            NbtIo.writeCompressed(snapshot, world.resolve("witchery-legacy-ids.dat").toFile());
            var directory = Files.createDirectories(world.resolve("entities"));
            var chunk = new ChunkPos(0, 0);
            var earlier = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {Position:[I;0,0],Entities:[{id:"witchery:brew2",Pos:[4.5d,70.3d,4.5d],Motion:[0.5d,0d,0d],Spell:1b,
                      Brew:{id:4100s,Count:1b,Damage:0s,tag:{Color:654321,Splash:1b,Items:[
                        {id:372s,Count:1b,Damage:0s},{id:4096s,Count:1b,Damage:156s},{id:289s,Count:1b,Damage:0s}]}}}]}
                    """));
            try (var region = new RegionFile(directory.resolve("r.0.0.mca"), directory, true);
                 var stream = region.getChunkDataOutputStream(chunk)) {
                NbtIo.write(earlier, stream);
            }
            try (var storage = new EntityStorage(level, directory, level.getServer().getFixerUpper(), true, Runnable::run)) {
                projectile = (ThrownBrew) storage.loadEntities(chunk).join().getEntities().findFirst().orElseThrow();
                var tag = projectile.getItem().getOrCreateTag();
                helper.assertTrue(projectile.isNoGravity() && projectile.getDeltaMovement().equals(new Vec3(0.5, 0, 0))
                                && tag.getInt("Color") == 654321
                                && itemIds(tag, "Items").equals(List.of("minecraft:nether_wart", "witchery:wolfsbane_flower", "minecraft:gunpowder")),
                        "The original bottle, color, trajectory and spell mode survive ordinary entity storage load. Found " + tag);
            }
        } finally {
            FileUtils.deleteDirectory(world.toFile());
        }
        var target = new BlockPos(6, 3, 4);
        var center = helper.absolutePos(target);
        var launch = Vec3.atBottomCenterOf(center).add(-1, 0.3, 0);
        var pig = EntityType.PIG.create(level);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.isPositionEntityTicking(center) && level.isPositionEntityTicking(BlockPos.containing(launch)),
                "The impact and launch chunks both track entities")).thenExecute(() -> {
            level.setBlockAndUpdate(center, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(BlockPos.containing(launch), Blocks.AIR.defaultBlockState());
            pig.setPos(Vec3.atBottomCenterOf(center));
            pig.setNoAi(true);
            pig.setNoGravity(true);
            helper.assertTrue(level.addFreshEntity(pig), "The splash target joins the game normally");
            projectile.setPos(launch);
            projectile.setDeltaMovement(2, 0, 0);
            helper.assertTrue(level.addFreshEntity(projectile), "The migrated projectile joins the game normally");
        }).thenWaitUntil(() -> helper.assertTrue(!projectile.isAlive() && pig.hasEffect(WitcheryEffects.WOLFSBANE.get()),
                "The migrated projectile applies its original brew on impact")).thenSucceed();
    }

    private static CompoundTag tile(CompoundTag chunk, int index) {
        return chunk.getList("block_entities", Tag.TAG_COMPOUND).getCompound(index);
    }

    private static List<String> itemIds(CompoundTag root, String key) {
        return root.getList(key, Tag.TAG_COMPOUND).stream().map(CompoundTag.class::cast)
                .map(item -> item.getString("id")).toList();
    }
}
