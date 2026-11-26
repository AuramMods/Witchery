package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class IceShellDrainTests {
    private static final BlockPos CENTER = new BlockPos(11, 6, 11);
    private static final String BATCH = "witchery_ice_shell_drain";

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void frozenHeartScansPastTheRimAndRespectsIceAndForgeVetoes(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(CENTER);
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "FrozenHeartFixture"));
        caster.setGameMode(GameType.SURVIVAL);
        var ice = WitcheryBlocks.getRequired("perpetualice").get();
        var gas = (BrewDispersalBlock) WitcheryBlocks.getRequired("brewgas").get();
        var liquid = (BrewDispersalBlock) WitcheryBlocks.getRequired("brewliquid").get();
        helper.setBlock(CENTER.south(), ice);
        helper.setBlock(CENTER.south(2), Blocks.WATER);
        helper.setBlock(CENTER.east(4), Blocks.WATER);
        helper.setBlock(CENTER.east(5), Blocks.WATER);
        helper.setBlock(CENTER.west(), Blocks.CHEST);
        release(helper, origin.north(), gas, caster);
        release(helper, origin.north(3), gas, caster);
        release(helper, origin.north().east(), liquid, caster);
        release(helper, origin.north().west(), gas, caster);
        var warded = origin.north().west();
        var payload = level.getBlockEntity(warded).saveWithFullMetadata();
        Consumer<BlockEvent.BreakEvent> veto = event -> {
            if (event.getPlayer() == caster && event.getPos().equals(warded)) event.setCanceled(true);
        };
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, CENTER);
        pig.setPos(Vec3.atLowerCornerOf(origin).add(0.25, 0, 0.25));
        var recipe = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(),
                WitcheryIngredient.TEAR_OF_THE_GODDESS.item().getDefaultInstance(), Items.NETHER_STAR.getDefaultInstance(),
                Items.GLOWSTONE_DUST.getDefaultInstance(),
                Items.BLAZE_ROD.getDefaultInstance(), WitcheryIngredient.CHARGED_ATTUNED_STONE.item().getDefaultInstance(),
                WitcheryIngredient.FROZEN_HEART.item().getDefaultInstance(), Items.GUNPOWDER.getDefaultInstance()));
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            recipe.applySplash(pig, caster, caster, 1, false);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }
        helper.assertBlockPresent(Blocks.WATER, CENTER.south(2));
        helper.assertBlockPresent(Blocks.AIR, CENTER.east(4));
        helper.assertBlockPresent(Blocks.WATER, CENTER.east(5));
        helper.assertBlockPresent(Blocks.CHEST, CENTER.west());
        helper.assertBlockPresent(ice, CENTER.north(3));
        helper.assertBlockPresent(Blocks.AIR, CENTER.north());
        helper.assertBlockPresent(Blocks.AIR, CENTER.north().east());
        helper.assertBlockPresent(gas, CENTER.north().west());
        helper.assertTrue(level.getBlockEntity(warded).saveWithFullMetadata().equals(payload),
                "The break veto leaves the cloud and its complete payload in place");
        pig.discard();
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void circleShellDrainsWaterInTheOriginalAsymmetricRows(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(CENTER);
        var ice = WitcheryBlocks.getRequired("perpetualice").get();
        var gas = (BrewDispersalBlock) WitcheryBlocks.getRequired("brewgas").get();
        helper.setBlock(CENTER.south(), ice);
        helper.setBlock(CENTER.south(2), Blocks.WATER);
        helper.setBlock(CENTER.east(9), Blocks.WATER);
        helper.setBlock(CENTER.west(9), Blocks.WATER);
        helper.setBlock(CENTER.west(8), Blocks.SNOW_BLOCK);
        helper.setBlock(CENTER.east(8).below(), Blocks.BEDROCK);
        helper.setBlock(CENTER.east(8), Blocks.SNOW);
        release(helper, origin.north(), gas, null);
        var iceAtScanHeight = origin.east().atY(1);
        var before = level.getBlockState(iceAtScanHeight);
        level.setBlockAndUpdate(iceAtScanHeight, ice.defaultBlockState());
        helper.setBlock(CENTER.east(2), Blocks.WATER);
        try {
            IceShell.pulse(level, origin, 4, 2, null);
            helper.assertBlockPresent(Blocks.WATER, CENTER.east(2));
            helper.assertBlockPresent(Blocks.WATER, CENTER.south(2));
            helper.assertBlockPresent(Blocks.WATER, CENTER.east(9));
            helper.assertBlockPresent(Blocks.AIR, CENTER.west(9));
            helper.assertBlockPresent(ice, CENTER.west(8));
            helper.assertBlockPresent(Blocks.SNOW, CENTER.east(8));
            helper.assertBlockPresent(gas, CENTER.north());
        } finally {
            level.setBlockAndUpdate(iceAtScanHeight, before);
        }
        helper.succeed();
    }

    private static void release(GameTestHelper helper, BlockPos pos, BrewDispersalBlock block, FakePlayer caster) {
        var items = List.of(Items.NETHER_WART.getDefaultInstance(), Items.SUGAR.getDefaultInstance(),
                (block.isGas() ? WitcheryIngredient.BAT_WOOL : WitcheryIngredient.WORMWOOD).item().getDefaultInstance());
        var mixture = BrewMixture.from(items);
        block.release(helper.getLevel(), pos, Direction.UP, mixture.bottleTag(items), mixture, 0x123456, caster);
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(block), "An active brew was placed at " + pos);
    }
}
