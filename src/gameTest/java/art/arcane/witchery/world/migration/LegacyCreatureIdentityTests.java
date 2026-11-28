package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.function.Function;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyCreatureIdentityTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_earlier_creature_leashes")
    public static void restoresNamedCreaturesAndTheirOriginalLeashes(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var position = helper.absolutePos(new BlockPos(4, 2, 4));
        var fence = position.east(3);
        for (int i = 0; i <= 3; i++) {
            BlockPos spot = position.east(i);
            level.setBlockAndUpdate(spot.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(spot, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(spot.above(), Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(fence, Blocks.OAK_FENCE.defaultBlockState());
        var toadId = UUID.randomUUID();
        var saved = TagParser.parseTag("""
                {Entities:[
                  {id:"witchery:toad",CustomName:"Keeper of Reeds",NoAI:1b,UUIDMost:1L,UUIDLeast:2L},
                  {id:"witchery.goblin",CustomName:"Nettle Cartwright",NoAI:1b,Leash:{UUIDMost:%dL,UUIDLeast:%dL}},
                  {id:"witchery:owl",NoAI:1b,Leash:{X:%d,Y:%d,Z:%d,UUIDMost:3L,UUIDLeast:4L}}
                ]}
                """.formatted(toadId.getMostSignificantBits(), toadId.getLeastSignificantBits(),
                fence.getX(), fence.getY(), fence.getZ()));
        var entries = saved.getList("Entities", Tag.TAG_COMPOUND);
        var name = Component.literal("Bright Keeper").withStyle(ChatFormatting.GREEN);
        entries.getCompound(0).putUUID("UUID", toadId);
        entries.getCompound(0).putString("CustomName", Component.Serializer.toJson(name));

        LegacyChunkEntities.restore(entries);
        var toad = (Mob) EntityType.loadEntityRecursive(entries.getCompound(0), level, Function.identity());
        var goblin = (Mob) EntityType.loadEntityRecursive(entries.getCompound(1), level, Function.identity());
        var owl = (Mob) EntityType.loadEntityRecursive(entries.getCompound(2), level, Function.identity());
        toad.setPos(Vec3.atBottomCenterOf(position));
        goblin.setPos(Vec3.atBottomCenterOf(position.east()));
        owl.setPos(Vec3.atBottomCenterOf(position.east(2)));
        level.addFreshEntity(toad);
        level.addFreshEntity(goblin);
        level.addFreshEntity(owl);

        helper.succeedWhen(() -> {
            helper.assertTrue(toad.getUUID().equals(toadId) && name.equals(toad.getCustomName()),
                    "Current UUIDs and styled names take precedence");
            helper.assertTrue(goblin.getName().getString().equals("Nettle Cartwright") && goblin.getLeashHolder() == toad,
                    "The original literal name and creature leash resolve after loading both creatures");
            helper.assertTrue(owl.getLeashHolder() instanceof LeashFenceKnotEntity knot && knot.getPos().equals(fence),
                    "A fence attachment takes precedence over old UUID halves");
            toad.discard();
            goblin.discard();
            owl.discard();
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_earlier_vanilla_taglocks")
    public static void restoresVanillaCreatureIdentityForTaglocks(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var pigId = UUID.randomUUID();
        var cowId = UUID.randomUUID();
        var original = TagParser.parseTag("""
                {Entities:[
                  {id:"minecraft:pig",CustomName:"Orchard Pig",NoAI:1b,UUIDMost:%dL,UUIDLeast:%dL,
                   Leash:{UUIDMost:%dL,UUIDLeast:%dL}},
                  {id:"minecraft:cow",NoAI:1b,UUIDMost:1L,UUIDLeast:2L}
                ]}
                """.formatted(pigId.getMostSignificantBits(), pigId.getLeastSignificantBits(),
                cowId.getMostSignificantBits(), cowId.getLeastSignificantBits()));
        var records = original.getList("Entities", Tag.TAG_COMPOUND);
        var cowName = Component.literal("Kestrel Cow").withStyle(ChatFormatting.AQUA);
        records.getCompound(1).putUUID("UUID", cowId);
        records.getCompound(1).putString("CustomName", Component.Serializer.toJson(cowName));

        LegacyChunkEntities.restore(records);
        var pig = (Mob) EntityType.loadEntityRecursive(records.getCompound(0), level, Function.identity());
        var cow = (Mob) EntityType.loadEntityRecursive(records.getCompound(1), level, Function.identity());
        var position = helper.absolutePos(new BlockPos(4, 2, 4));
        pig.setPos(Vec3.atBottomCenterOf(position));
        cow.setPos(Vec3.atBottomCenterOf(position.east()));
        level.addFreshEntity(pig);
        level.addFreshEntity(cow);

        var sample = new ItemStack(WitcheryItems.getRequired("taglockkit").get());
        var signature = sample.getOrCreateTag();
        signature.putLong("WITCEntityIDm1", pigId.getMostSignificantBits());
        signature.putLong("WITCEntityIDl1", pigId.getLeastSignificantBits());
        signature.putString("WITCDisplayName1", "Orchard Pig");
        helper.succeedWhen(() -> {
            helper.assertTrue(TaglockItem.resolveTarget(level, sample) == pig && pig.getName().getString().equals("Orchard Pig"),
                    "The old Taglock still finds its original named vanilla creature");
            helper.assertTrue(pig.getLeashHolder() == cow && cow.getUUID().equals(cowId) && cowName.equals(cow.getCustomName()),
                    "The old leash resolves to the current holder, whose saved UUID and styled name take precedence");
            pig.discard();
            cow.discard();
        });
    }
}
