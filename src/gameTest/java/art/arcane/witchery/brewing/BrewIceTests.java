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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
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
