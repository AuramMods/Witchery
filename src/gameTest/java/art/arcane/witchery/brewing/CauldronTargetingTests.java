package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.CovenWitch;
import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.ritual.RitualCircle;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CauldronTargetingTests {
    private static final BlockPos CENTER = new BlockPos(5, 4, 5);
    private static final String TEMPLATE = "qa_coven_arena";

    @GameTest(template = TEMPLATE, batch = "witchery_cauldron_attendance")
    public static void witchAttendanceUsesTheOriginalCenteredBounds(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(CENTER);
        var center = Vec3.atCenterOf(origin);
        var owner = addOwner(level, center.add(-10, 0, 0));
        var witch = addWitch(level, center, owner);
        var offered = speedAt(level, origin.east(33));
        record Attendance(double x, double y, boolean counted) {}
        var positions = List.of(new Attendance(0, 6.4, true), new Attendance(0, 6.75, false),
                new Attendance(0, -7.2, true), new Attendance(0, -7.6, false),
                new Attendance(6, 0.5, true), new Attendance(6.1, 0.5, false));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(witch.getUUID()) == witch,
                "The cauldron can see its attendant")).thenExecute(() -> {
            try {
                for (var position : positions) {
                    witch.setPos(center.x + position.x(), origin.getY() + position.y(), center.z);
                    var result = cast(level, origin, offered, false);
                    helper.assertTrue(result == (position.counted() ? BrewRituals.Result.COMPLETE : BrewRituals.Result.TOO_FAR),
                            "The resident at " + position + " counts only within the original bounds");
                }
            } finally {
                witch.discard();
                level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
            }
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_cauldron_dimension_reach")
    public static void dimensionalTravelNeedsSixWitchesAndLeonard(GameTestHelper helper) {
        var level = helper.getLevel();
        var nether = level.getServer().getLevel(Level.NETHER);
        var origin = helper.absolutePos(CENTER);
        var center = Vec3.atCenterOf(origin);
        var owner = addOwner(level, center.add(-10, 0, 0));
        var witches = new ArrayList<CovenWitch>();
        for (int i = 0; i < 6; i++) witches.add(addWitch(level, center.add(i - 2, 0, 0), owner));
        var leonard = helper.spawnWithNoFreeWill(WitcheryEntities.LEONARD.get(), CENTER.south(2));
        leonard.setNoGravity(true);
        var acrossDimensions = speedAt(nether, origin.atY(180));
        var distant = speedAt(level, origin.east(1200));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.getEntity(leonard.getUUID()) == leonard
                        && witches.stream().allMatch(witch -> level.getEntity(witch.getUUID()) == witch),
                "The coven and Leonard are tracked before testing range")).thenExecute(() -> {
            try {
                leonard.setHealth(1);
                helper.assertTrue(cast(level, origin, distant, BrewRituals.leonardNearby(level, origin)) == BrewRituals.Result.COMPLETE,
                        "Six attendants remove the same-dimension range limit");
                helper.assertTrue(cast(level, origin, acrossDimensions, BrewRituals.leonardNearby(level, origin)) == BrewRituals.Result.TOO_FAR,
                        "Leonard at one health does not enable a cross-dimension cast");
                leonard.setHealth(2);
                helper.assertTrue(cast(level, origin, acrossDimensions, BrewRituals.leonardNearby(level, origin)) == BrewRituals.Result.COMPLETE,
                        "Six attendants and a healthy Leonard enable a cross-dimension cast");
                witches.get(5).setPos(center.add(7, 0, 0));
                helper.assertTrue(cast(level, origin, acrossDimensions, BrewRituals.leonardNearby(level, origin)) == BrewRituals.Result.TOO_FAR,
                        "Moving the sixth attendant out of range blocks the cross-dimension cast");
            } finally {
                witches.forEach(Entity::discard);
                leonard.discard();
                level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
            }
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_taglock_ring_reach")
    public static void taglockHellGateCanUseARemoteInfernalCircle(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(CENTER);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, CENTER.east(2));
        pig.setNoGravity(true);
        var trigger = new ItemStack(WitcheryItems.getRequired("taglockkit").get());
        TaglockItem.bindSignature(trigger, pig);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(pig.getUUID()) == pig,
                "The Taglock can resolve its nearby target")).thenExecute(() -> {
            for (var ringWorld : List.of(level, level.getServer().getLevel(Level.NETHER))) {
                var ring = origin.east(80).atY(200);
                drawMediumRing(ringWorld, ring, "circleglyphinfernal");
                var offerings = List.of(new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item()), new ItemStack(Items.NETHER_WART),
                        new ItemStack(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()),
                        new ItemStack(WitcheryIngredient.REFINED_EVIL.item()), stone(ringWorld, ring), trigger);
                helper.assertTrue(cast(level, origin, offerings, false) == BrewRituals.Result.COMPLETE
                                && pig.hasEffect(WitcheryEffects.HELLISH_AURA.get()),
                        "The remote ring validates Hell Gate while the Taglock targets the nearby pig");
                pig.removeEffect(WitcheryEffects.HELLISH_AURA.get());
                var glyph = ring.offset(RitualCircle.MEDIUM.offsets().get(0));
                ringWorld.setBlockAndUpdate(glyph, Blocks.AIR.defaultBlockState());
                helper.assertTrue(cast(level, origin, offerings, false) == BrewRituals.Result.INVALID_CIRCLES
                                && !pig.hasEffect(WitcheryEffects.HELLISH_AURA.get()),
                        "The missing glyph invalidates the cast in either dimension");
            }
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_hell_gate_clearance")
    public static void hellGateRequiresAirInItsOriginalSummonColumn(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(CENTER).atY(250);
        var ceiling = origin.atY(level.getMaxBuildHeight() - 1);
        drawMediumRing(level, origin, "circleglyphinfernal");
        drawMediumRing(level, ceiling, "circleglyphinfernal");
        BrewRituals.loadArea(level, origin, 4);
        long seed = 117;
        var random = RandomSource.create(seed);
        var column = origin.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
        var topColumn = column.atY(ceiling.getY());
        level.setBlockAndUpdate(column, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(column.above(2), Blocks.COBWEB.defaultBlockState());
        level.setBlockAndUpdate(topColumn, Blocks.STONE.defaultBlockState());
        var offerings = List.of(WitcheryIngredient.MANDRAKE_ROOT.item().getDefaultInstance(), Items.NETHER_WART.getDefaultInstance(),
                WitcheryIngredient.TEAR_OF_THE_GODDESS.item().getDefaultInstance(), Items.GUNPOWDER.getDefaultInstance(),
                WitcheryIngredient.REFINED_EVIL.item().getDefaultInstance(), WitcheryIngredient.DOG_TONGUE.item().getDefaultInstance());
        List<Demon> demons = new ArrayList<>();
        Consumer<EntityJoinLevelEvent> record = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof Demon demon) demons.add(demon);
        };
        MinecraftForge.EVENT_BUS.addListener(record);
        try {
            level.getRandom().setSeed(seed);
            helper.assertTrue(cast(level, origin, offerings, false) == BrewRituals.Result.COMPLETE && demons.isEmpty(),
                    "A non-solid cobweb closes the original two-air-block opening");

            level.removeBlock(column.above(2), false);
            level.setBlockAndUpdate(column.above(3), Blocks.STONE.defaultBlockState());
            level.getRandom().setSeed(seed);
            helper.assertTrue(cast(level, origin, offerings, false) == BrewRituals.Result.COMPLETE && demons.size() == 1
                            && demons.get(0).position().equals(Vec3.atBottomCenterOf(column.above()).add(0, 0.05, 0)),
                    "The Demon emerges at the original offset in a pocket with two open blocks");

            level.getRandom().setSeed(seed);
            helper.assertTrue(cast(level, ceiling, offerings, false) == BrewRituals.Result.COMPLETE && demons.size() == 2
                            && demons.get(1).position().equals(Vec3.atBottomCenterOf(topColumn.above()).add(0, 0.05, 0)),
                    "The final buildable layer can support a Demon in the open sky");

            int bottom = level.getMinBuildHeight();
            int update = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
            List<BlockState> savedColumn = new ArrayList<>();
            try {
                for (int y = bottom; y <= column.getY(); y++) {
                    BlockPos pos = column.atY(y);
                    savedColumn.add(level.getBlockState(pos));
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), update);
                }
                level.getRandom().setSeed(seed);
                helper.assertTrue(cast(level, origin, offerings, false) == BrewRituals.Result.COMPLETE && demons.size() == 3
                                && demons.get(2).position().equals(Vec3.atBottomCenterOf(column.atY(bottom + 1)).add(0, 0.05, 0)),
                        "A clear void column uses the world's minimum build height");
            } finally {
                for (int i = 0; i < savedColumn.size(); i++) level.setBlock(column.atY(bottom + i), savedColumn.get(i), update);
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(record);
            demons.forEach(Entity::discard);
            level.removeBlock(column, false);
            level.removeBlock(column.above(2), false);
            level.removeBlock(column.above(3), false);
            level.removeBlock(topColumn, false);
            var chalk = WitcheryBlocks.getRequired("circleglyphinfernal").get();
            RitualCircle.MEDIUM.erase(level, origin, chalk);
            RitualCircle.MEDIUM.erase(level, ceiling, chalk);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_cauldron_transpose_destination")
    public static void terrainTransposeCanReachADistantSecondSite(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(CENTER).atY(200);
        var source = origin.east(12);
        var destination = origin.east(80);
        drawMediumRing(level, source, "circleglyphotherwhere");
        level.setBlockAndUpdate(source, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(destination, Blocks.AIR.defaultBlockState());
        var offerings = List.of(new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item()), new ItemStack(Items.NETHER_WART),
                new ItemStack(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()), stone(level, source),
                new ItemStack(Items.ENDER_PEARL), stone(level, destination), new ItemStack(Items.GUNPOWDER),
                new ItemStack(WitcheryIngredient.DOG_TONGUE.item()));
        helper.assertTrue(cast(level, origin, offerings, false) == BrewRituals.Result.COMPLETE
                        && level.isEmptyBlock(source) && level.getBlockState(destination).is(Blocks.STONE),
                "The first Waystone is in range and the move goes to the second Waystone");
        helper.succeed();
    }

    private static FakePlayer addOwner(ServerLevel level, Vec3 position) {
        var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrewCovenOwner"));
        owner.setPos(position);
        owner.setNoGravity(true);
        level.addNewPlayer(owner);
        return owner;
    }

    private static CovenWitch addWitch(ServerLevel level, Vec3 position, FakePlayer owner) {
        var witch = WitcheryEntities.COVEN_WITCH.get().create(level);
        var tag = new CompoundTag();
        tag.putUUID("Owner", owner.getUUID());
        tag.putBoolean("Sitting", false);
        witch.readAdditionalSaveData(tag);
        witch.setPos(position);
        witch.setNoAi(true);
        witch.setNoGravity(true);
        level.addFreshEntity(witch);
        return witch;
    }

    private static ItemStack stone(ServerLevel level, BlockPos position) {
        var stack = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
        WaystoneItem.bindLocation(stack, level, position);
        return stack;
    }

    private static List<ItemStack> speedAt(ServerLevel level, BlockPos position) {
        return List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.SUGAR), new ItemStack(Items.GUNPOWDER),
                stone(level, position), new ItemStack(WitcheryIngredient.DOG_TONGUE.item()));
    }

    private static BrewRituals.Result cast(ServerLevel level, BlockPos origin, List<ItemStack> offerings, boolean leonard) {
        var mixture = BrewMixture.from(offerings, null);
        return BrewRituals.pulse(level, origin, mixture, mixture.bottleTag(offerings), offerings.get(offerings.size() - 1), 1, leonard);
    }

    private static void drawMediumRing(ServerLevel level, BlockPos center, String chalk) {
        var state = WitcheryBlocks.getRequired(chalk).get().defaultBlockState();
        for (var offset : RitualCircle.MEDIUM.offsets()) {
            BlockPos position = center.offset(offset);
            level.setBlockAndUpdate(position.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(position, state);
        }
    }
}
