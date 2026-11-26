package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewIceTests {
    private static final BlockPos CENTER = new BlockPos(5, 5, 5);
    private static final String BATCH = "witchery_brew_ice";

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void fixedIceFreezesTheLegacyFluidsInConnectedOrder(GameTestHelper helper) {
        var actor = actor(helper);
        var level = helper.getLevel();
        var origin = helper.absolutePos(CENTER);
        var liquid = origin.east(3);
        helper.setBlock(CENTER, Blocks.STONE);
        helper.setBlock(CENTER.east(), WitcheryBlocks.getRequired("hollowtears").get());
        helper.setBlock(CENTER.east(2), WitcheryBlocks.getRequired("spiritflowing").get());
        helper.setBlock(CENTER.east(4), Blocks.WATER);
        helper.setBlock(CENTER.west(), Blocks.WATER);
        helper.setBlock(CENTER.north(), WitcheryBlocks.getRequired("disease").get());
        helper.setBlock(CENTER.south(), WitcheryBlocks.getRequired("brewgas").get());
        releaseLiquid(helper, liquid, actor);
        List<BlockPos> placements = new ArrayList<>();
        Consumer<BlockEvent.EntityPlaceEvent> seePlacement = event -> {
            if (event.getEntity() == actor && event.getPlacedBlock().is(Blocks.ICE)) placements.add(event.getPos().immutable());
        };
        MinecraftForge.EVENT_BUS.addListener(seePlacement);
        try {
            impact(actor, new BlockHitResult(Vec3.atCenterOf(origin), Direction.UP, origin, false));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(seePlacement);
        }
        helper.assertTrue(placements.equals(List.of(origin.east(), origin.east(2), liquid, origin.west())),
                "Ice follows the original east branch before freezing water west of the impact");
        helper.assertTrue(level.getBlockEntity(liquid) == null, "The frozen Brew Liquid loses its active payload");
        helper.assertBlockPresent(Blocks.WATER, CENTER.east(4));
        helper.assertBlockPresent(Blocks.AIR, CENTER.above());
        helper.assertBlockPresent(WitcheryBlocks.getRequired("disease").get(), CENTER.north());
        helper.assertBlockPresent(WitcheryBlocks.getRequired("brewgas").get(), CENTER.south());
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void cauldronColdPreservesAVetoedActiveBrewAndFreezesItsOriginalFringe(GameTestHelper helper) {
        var actor = actor(helper);
        var level = helper.getLevel();
        var origin = helper.absolutePos(CENTER);
        var liquid = origin.east();
        var cold = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(),
                WitcheryIngredient.ICY_NEEDLE.item().getDefaultInstance(), Items.GUNPOWDER.getDefaultInstance()));
        helper.setBlock(CENTER, Blocks.STONE);
        releaseLiquid(helper, liquid, actor);
        helper.setBlock(CENTER.east(2), WitcheryBlocks.getRequired("spiritflowing").get());
        helper.setBlock(CENTER.east(3), Blocks.WATER);
        Consumer<BlockEvent.EntityPlaceEvent> cancel = event -> {
            if (event.getEntity() == actor && event.getPos().equals(liquid) && event.getPlacedBlock().is(Blocks.ICE)) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(cancel);
        try {
            cold.applySplashToBlock(level, origin, Direction.UP, Vec3.atCenterOf(origin), actor);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancel);
        }
        helper.assertTrue(level.getBlockEntity(liquid) instanceof BrewFluidBlockEntity brew && brew.hasContents()
                        && brew.color() == 0x123456,
                "The placement veto restores the brew block entity and its payload");
        helper.assertBlockPresent(WitcheryBlocks.getRequired("spiritflowing").get(), CENTER.east(2));
        cold.applySplashToBlock(level, origin, Direction.UP, Vec3.atCenterOf(origin), actor);
        helper.assertBlockPresent(Blocks.ICE, CENTER.east());
        helper.assertBlockPresent(Blocks.ICE, CENTER.east(2));
        helper.assertBlockPresent(Blocks.WATER, CENTER.east(3));
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void aFixedIceResistantBlazeGetsWaterInTheNether(GameTestHelper helper) {
        var nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        helper.assertTrue(nether != null, "The dedicated test server has a Nether");
        var feet = helper.absolutePos(CENTER).atY(172);
        nether.getChunkAt(feet);
        var actor = actor(nether);
        var blaze = EntityType.BLAZE.create(nether);
        helper.assertTrue(blaze != null, "The test blaze exists");
        blaze.setPos(Vec3.atLowerCornerOf(feet).add(-0.25, 0, 0.2));
        try {
            impact(actor, new EntityHitResult(blaze));
            helper.assertTrue(nether.getBlockState(feet).is(Blocks.WATER),
                    "The original direct hit places water at the rounded feet even in the Nether");
            helper.assertTrue(nether.isEmptyBlock(feet.above(2)), "The resistant blaze gets water without an ice cage");
        } finally {
            nether.removeBlock(feet, false);
            blaze.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void iceWorldRestoresLegacyShapesAndPlacementOrder(GameTestHelper helper) {
        var actor = actor(helper);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var topSlab = center.west(2);
        var doubleSlab = center.south(2);
        var stair = center.north(2);
        var gate = center.east(2);
        var door = center.offset(2, 0, 2);
        var protectedSlab = center.offset(-2, 0, 2);
        var edge = center.offset(4, 2, 1);
        var outside = center.offset(4, 2, 2);
        var materials = List.of(topSlab, doubleSlab, stair, gate, door, protectedSlab, edge, outside);

        level.setBlockAndUpdate(topSlab, Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        level.setBlockAndUpdate(doubleSlab, Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE));
        level.setBlockAndUpdate(stair, Blocks.BIRCH_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, Half.TOP));
        level.setBlockAndUpdate(gate, Blocks.OAK_FENCE_GATE.defaultBlockState()
                .setValue(FenceGateBlock.FACING, Direction.EAST).setValue(FenceGateBlock.OPEN, true));
        level.setBlockAndUpdate(door.below(), Blocks.DEEPSLATE.defaultBlockState());
        var openDoor = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST)
                .setValue(DoorBlock.OPEN, true).setValue(DoorBlock.HINGE, DoorHingeSide.RIGHT);
        level.setBlock(door, openDoor, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        level.setBlock(door.above(), openDoor.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        level.setBlockAndUpdate(protectedSlab, Blocks.BIRCH_SLAB.defaultBlockState());
        level.setBlockAndUpdate(edge, Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(outside, Blocks.DIRT.defaultBlockState());

        List<BlockPos> placements = new ArrayList<>();
        List<PlayLevelSoundEvent.AtPosition> sounds = new ArrayList<>();
        Consumer<BlockEvent.EntityPlaceEvent> placement = event -> {
            if (event.getEntity() != actor || !materials.contains(event.getPos())) return;
            if (event.getPos().equals(protectedSlab)) event.setCanceled(true);
            else placements.add(event.getPos().immutable());
        };
        Consumer<PlayLevelSoundEvent.AtPosition> sound = event -> {
            if (event.getLevel() == level && event.getSound() != null && event.getSound().value() == SoundEvents.EXPERIENCE_ORB_PICKUP) {
                sounds.add(event);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(placement);
        MinecraftForge.EVENT_BUS.addListener(sound);
        try {
            var brew = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(), Items.NETHER_STAR.getDefaultInstance(),
                    Items.GUNPOWDER.getDefaultInstance(), Items.ENDER_EYE.getDefaultInstance()));
            helper.assertTrue(brew.hasTerrainEffects() && brew.used() == 5, "The brew has capacity for Ice World");
            brew.applySplashToBlock(level, center, Direction.UP, Vec3.atCenterOf(center), actor);
            var slab = WitcheryBlocks.getRequired("iceslab").get();
            var frozenTop = level.getBlockState(topSlab);
            var frozenDouble = level.getBlockState(doubleSlab);
            helper.assertTrue(frozenTop.is(slab) && frozenTop.getValue(SlabBlock.TYPE) == SlabType.BOTTOM,
                    "A frozen single slab starts in its original bottom position");
            helper.assertTrue(frozenDouble.is(slab) && frozenDouble.getValue(SlabBlock.TYPE) == SlabType.DOUBLE,
                    "A frozen double slab occupies the full block");
            var frozenStair = level.getBlockState(stair);
            helper.assertTrue(frozenStair.is(WitcheryBlocks.getRequired("icestairs").get())
                            && frozenStair.getValue(StairBlock.FACING) == Direction.WEST && frozenStair.getValue(StairBlock.HALF) == Half.TOP,
                    "Stairs keep their original facing and half");
            var frozenGate = level.getBlockState(gate);
            helper.assertTrue(frozenGate.is(WitcheryBlocks.getRequired("icefencegate").get())
                            && frozenGate.getValue(FenceGateBlock.FACING) == Direction.SOUTH && !frozenGate.getValue(FenceGateBlock.OPEN),
                    "The frozen gate resets to the original closed south-facing state");
            var frozenDoor = level.getBlockState(door);
            helper.assertTrue(frozenDoor.is(WitcheryBlocks.getRequired("icedoor").get())
                            && level.getBlockState(door.above()).equals(frozenDoor.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER))
                            && frozenDoor.getValue(DoorBlock.FACING) == Direction.WEST && !frozenDoor.getValue(DoorBlock.OPEN)
                            && frozenDoor.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT,
                    "The frozen door keeps its facing with its original closed placement and both matching halves");
            helper.assertTrue(level.getBlockState(protectedSlab).is(Blocks.BIRCH_SLAB) && level.getBlockState(edge).is(Blocks.SNOW_BLOCK)
                            && level.getBlockState(outside).is(Blocks.DIRT),
                    "The placement veto and the strict original sphere exclude the protected and outer blocks");
            helper.assertTrue(placements.equals(List.of(door, topSlab, stair, doubleSlab, gate, edge)),
                    "Doors transform first, then the remaining blocks in X, Z, Y order, actual " + placements);
            helper.assertTrue(sounds.size() == 1 && sounds.get(0).getPosition().equals(Vec3.atLowerCornerOf(center))
                            && sounds.get(0).getNewVolume() == 0.5F && sounds.get(0).getNewPitch() == 2.0F,
                    "The finished cast plays the original orb cue at the lower corner");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(placement);
            MinecraftForge.EVENT_BUS.unregister(sound);
            actor.discard();
        }
        helper.succeed();
    }

    private static void releaseLiquid(GameTestHelper helper, BlockPos pos, FakePlayer actor) {
        var items = List.of(Items.NETHER_WART.getDefaultInstance(), Items.SUGAR.getDefaultInstance(),
                WitcheryIngredient.WORMWOOD.item().getDefaultInstance());
        var mixture = BrewMixture.from(items);
        var block = (BrewDispersalBlock) WitcheryBlocks.getRequired("brewliquid").get();
        block.release(helper.getLevel(), pos, Direction.UP, BrewMixture.ingredientTag(items, 0x123456),
                mixture, 0x123456, actor);
        helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof BrewFluidBlockEntity brew && brew.hasContents(),
                "An active Brew Liquid cell is ready");
    }

    private static FakePlayer actor(GameTestHelper helper) {
        return actor(helper.getLevel());
    }

    private static FakePlayer actor(ServerLevel level) {
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "IceFixture"));
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }

    private static void impact(FakePlayer player, HitResult hit) {
        var brew = new ThrownBrew(player, player.level());
        var item = (ThrowableBrewItem) WitcheryIngredient.BREW_OF_ICE.item();
        brew.setItem(item.getDefaultInstance());
        item.impact(brew, hit);
    }
}
