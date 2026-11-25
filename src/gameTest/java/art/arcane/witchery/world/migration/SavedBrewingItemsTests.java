package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.KettleBlockEntity;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
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

    private static CompoundTag tile(CompoundTag chunk, int index) {
        return chunk.getList("block_entities", Tag.TAG_COMPOUND).getCompound(index);
    }

    private static List<String> itemIds(CompoundTag root, String key) {
        return root.getList(key, Tag.TAG_COMPOUND).stream().map(CompoundTag.class::cast)
                .map(item -> item.getString("id")).toList();
    }
}
