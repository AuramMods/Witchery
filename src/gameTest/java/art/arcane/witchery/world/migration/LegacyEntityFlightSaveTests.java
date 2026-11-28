package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.entity.WitchBroom;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyEntityFlightSaveTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_restored_entity_flight")
    public static void restoresBroomHeadingAndRetainedBrewPayloads(GameTestHelper helper) throws Exception {
        var directory = Files.createTempDirectory("witchery-entity-flight");
        var chunk = new ChunkPos(0, 0);
        try {
            var records = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {Position:[I;0,0],Entities:[
                      {id:"witchery.broom",CustomName:"Original Broom",BrushColor:6b,Rotation:[43f,0f]},
                      {id:"minecraft:boat",Passengers:[{id:"minecraft:witchery.Broom",CustomName:"Nested Broom",
                        BrushColor:3b,Rotation:[-20f,0f]}]},
                      {id:"witchery:broom",CustomName:'{"text":"Current Broom"}',Rotation:[23f,0f]},
                      {id:"witchery:thrown_brew",CustomName:'{"text":"Fixed Web"}',damageValue:56,Motion:[0.25d,-0.5d,0.5d]},
                      {id:"witchery:thrown_brew",CustomName:'{"text":"Stored Spell"}',Spell:1b,ownerName:"EarlierBrewer",
                       Brew:{id:"witchery:brewbottle",Count:1b,tag:{Color:1193046}}},
                      {id:"witchery:thrown_brew",CustomName:'{"text":"Gravity Control"}',Spell:1b,NoGravity:0b,
                       Brew:{id:"witchery:brewbottle",Count:1b,tag:{Color:6636321}}},
                      {id:"witchery:thrown_brew",CustomName:'{"text":"Current Payload"}',Spell:1b,
                       Item:{id:"witchery:brew_of_ink",Count:1b},Brew:{id:"witchery:brewbottle",Count:1b}}]}
                    """));
            try (var region = new RegionFile(directory.resolve("r.0.0.mca"), directory, true);
                 var stream = region.getChunkDataOutputStream(chunk)) {
                NbtIo.write(records, stream);
            }
            try (var storage = new EntityStorage(helper.getLevel(), directory, DataFixers.getDataFixer(), true, Runnable::run)) {
                var entities = storage.loadEntities(chunk).join().getEntities().toList();
                var broom = (WitchBroom) named(entities, "Original Broom");
                var boat = entities.stream().filter(entity -> entity.getType() == EntityType.BOAT).findFirst().orElseThrow();
                var nested = (WitchBroom) boat.getFirstPassenger();
                helper.assertTrue(broom.getYRot() == 133 && broom.brushColor() == 6 && nested != null
                                && nested.getYRot() == 70 && nested.brushColor() == 3
                                && named(entities, "Current Broom").getYRot() == 23,
                        "Dotted Broom IDs recover the original heading even inside a current passenger tree");
                var savedBroom = new CompoundTag();
                broom.saveAsPassenger(savedBroom);
                LegacyChunkEntities.restoreEntity(savedBroom);
                helper.assertTrue(EntityType.create(savedBroom, helper.getLevel()).orElseThrow().getYRot() == 133,
                        "Once saved under its current ID, the Broom keeps its recovered heading");

                var fixed = (ThrownBrew) named(entities, "Fixed Web");
                var spell = (ThrownBrew) named(entities, "Stored Spell");
                var gravity = (ThrownBrew) named(entities, "Gravity Control");
                var current = (ThrownBrew) named(entities, "Current Payload");
                helper.assertTrue(fixed.getItem().is(WitcheryIngredient.WEB.item()) && !fixed.isNoGravity()
                                && fixed.getDeltaMovement().equals(new Vec3(0.25, -0.5, 0.5)),
                        "The fixed recipe retains its item, gravity and saved motion");
                helper.assertTrue(spell.getItem().is(WitcheryItems.getRequired("brewbottle").get()) && spell.isNoGravity()
                                && spell.getItem().getTag().getInt("Color") == 1193046
                                && spell.saveWithoutId(new CompoundTag()).getString("ownerName").equals("EarlierBrewer"),
                        "The original bottle, spell flight and pending thrower survive the normalized entity ID");
                helper.assertTrue(gravity.getItem().is(WitcheryItems.getRequired("brewbottle").get()) && !gravity.isNoGravity()
                                && current.getItem().is(WitcheryIngredient.BREW_OF_INK.item()) && !current.isNoGravity(),
                        "Explicit current flight and projectile payloads keep precedence");
            }
        } finally {
            FileUtils.deleteDirectory(directory.toFile());
        }
        helper.succeed();
    }

    private static Entity named(List<Entity> entities, String name) {
        return entities.stream().filter(entity -> entity.getName().getString().equals(name)).findFirst().orElseThrow();
    }
}
