package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
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
public final class EndlessWaterTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_endless_water_player")
    public static void playerPourPostsOnePlacementAndSpendsOneUseOnSuccess(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos support = helper.absolutePos(new BlockPos(5, 3, 5));
        BlockPos target = support.above();
        level.setBlockAndUpdate(support, Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(target, Blocks.GRASS.defaultBlockState());
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "WaterWitness"));
        player.setPos(Vec3.atBottomCenterOf(support.west().above()));
        ItemStack bottle = bottle();
        player.setItemInHand(InteractionHand.MAIN_HAND, bottle);
        var hit = new BlockHitResult(Vec3.atCenterOf(support).add(0, 0.5, 0), Direction.UP, support, false);
        var context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);

        List<BlockEvent.EntityPlaceEvent> placements = new ArrayList<>();
        List<PlayLevelSoundEvent.AtEntity> cues = new ArrayList<>();
        Consumer<BlockEvent.EntityPlaceEvent> veto = event -> {
            if (event.getPos().equals(target)) {
                placements.add(event);
                if (event.getPlacedBlock().is(Blocks.WATER)) event.setCanceled(true);
            }
        };
        Consumer<PlayLevelSoundEvent.AtEntity> sound = event -> { if (event.getEntity() == player) cues.add(event); };
        MinecraftForge.EVENT_BUS.addListener(veto);
        MinecraftForge.EVENT_BUS.addListener(sound);
        try {
            bottle.useOn(context);
            helper.assertTrue(placements.size() == 1 && placements.get(0).getPlacedBlock().is(Blocks.WATER),
                    "Forge receives one placement event with the actual proposed water");
            helper.assertTrue(level.getBlockState(target).is(Blocks.GRASS) && bottle.getDamageValue() == 95 && cues.isEmpty(),
                    "A placement veto keeps the grass, the bottle's uses and silence");
            MinecraftForge.EVENT_BUS.unregister(veto);

            Consumer<BlockEvent.BreakEvent> protectGrass = event -> {
                if (event.getPos().equals(target)) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(protectGrass);
            try {
                bottle.useOn(context);
                helper.assertTrue(level.getBlockState(target).is(Blocks.GRASS) && bottle.getDamageValue() == 95,
                        "The original break check protects replaceable grass");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(protectGrass);
            }

            bottle.useOn(context);
            helper.assertTrue(level.getFluidState(target).isSource() && bottle.getDamageValue() == 96,
                    "An allowed pour places source water and spends exactly one use");
            helper.assertTrue(cues.size() == 1 && cues.get(0).getSound().value() == SoundEvents.GENERIC_SPLASH
                            && cues.get(0).getNewVolume() == 0.5F && cues.get(0).getNewPitch() >= 1F / 3F
                            && cues.get(0).getNewPitch() <= 0.5F,
                    "A successful player pour makes the original splash at the player");
            bottle.useOn(context);
            helper.assertTrue(bottle.getDamageValue() == 97, "A hand pour over an existing source spends one use");
            level.setBlockAndUpdate(target, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 4));
            bottle.useOn(context);
            helper.assertTrue(level.getFluidState(target).isSource() && bottle.getDamageValue() == 98,
                    "A hand pour restores a flowing water block to a source");
            level.setBlockAndUpdate(target, Blocks.LAVA.defaultBlockState());
            bottle.useOn(context);
            helper.assertTrue(level.getBlockState(target).is(Blocks.WATER) && bottle.getDamageValue() == 99,
                    "A hand pour can replace lava");
            bottle.useOn(context);
            helper.assertTrue(bottle.isEmpty(), "The fifth use breaks the weakest bottle");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
            MinecraftForge.EVENT_BUS.unregister(sound);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_endless_water_vessels")
    public static void dispenserDistinguishesWitcheryVesselsFromVanillaCauldrons(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 6));
        BlockPos target = pos.east();
        var dispenser = dispenser(level, pos);
        level.setBlockAndUpdate(target, Blocks.CAULDRON.defaultBlockState());
        dispense(level, pos);
        helper.assertTrue(level.getBlockState(target).is(Blocks.CAULDRON) && dispenser.getItem(0).getDamageValue() == 95,
                "The original dispenser leaves a vanilla cauldron empty and preserves uses");

        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "WaterBottler"));
        ItemStack held = bottle();
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        held.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false)));
        var full = level.getBlockState(target);
        helper.assertTrue(full.is(Blocks.WATER_CAULDRON) && full.getValue(LayeredCauldronBlock.LEVEL) == 3
                && held.getDamageValue() == 96, "A player can still fill the same vanilla cauldron by hand");

        level.setBlockAndUpdate(target.below(), Blocks.STONE.defaultBlockState());
        int expectedDamage = 95;
        for (String name : List.of("kettle", "cauldron")) {
            level.setBlockAndUpdate(target, WitcheryBlocks.getRequired(name).get().defaultBlockState());
            var tank = level.getBlockEntity(target).getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
            dispense(level, pos);
            expectedDamage++;
            helper.assertTrue(tank.getFluidInTank(0).getAmount() == tank.getTankCapacity(0)
                            && dispenser.getItem(0).getDamageValue() == expectedDamage,
                    "The dispenser fills the Witchery " + name + " and spends one use");
            dispense(level, pos);
            helper.assertTrue(dispenser.getItem(0).getDamageValue() == expectedDamage, "A full vessel consumes no use");
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_endless_water_dispenser")
    public static void dispenserKeepsItsWaterAndBottleWhenAPlacementIsDenied(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 6));
        BlockPos target = pos.east();
        var dispenser = dispenser(level, pos);
        level.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
        Consumer<BlockEvent.EntityPlaceEvent> veto = event -> {
            if (event.getPos().equals(target) && event.getPlacedBlock().is(Blocks.WATER)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            dispense(level, pos);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }
        helper.assertTrue(level.isEmptyBlock(target) && dispenser.getItem(0).getDamageValue() == 95,
                "A veto preserves the empty target and bottle's uses");
        dispense(level, pos);
        helper.assertTrue(level.getFluidState(target).isSource() && dispenser.getItem(0).getDamageValue() == 96,
                "The dispenser places source water and uses one charge once allowed");
        dispense(level, pos);
        helper.assertTrue(dispenser.getItem(0).getDamageValue() == 96, "Existing water does not consume another dispenser use");
        level.setBlockAndUpdate(target, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 4));
        dispense(level, pos);
        helper.assertTrue(!level.getFluidState(target).isSource() && dispenser.getItem(0).getDamageValue() == 96,
                "The dispenser also leaves flowing water alone");
        level.setBlockAndUpdate(target, Blocks.LAVA.defaultBlockState());
        dispense(level, pos);
        helper.assertTrue(level.getBlockState(target).is(Blocks.WATER) && dispenser.getItem(0).getDamageValue() == 97,
                "The dispenser can replace lava with source water");
        helper.succeed();
    }

    private static ItemStack bottle() {
        var bottle = new ItemStack(WitcheryItems.getRequired("brew.water").get());
        bottle.setDamageValue(95);
        return bottle;
    }

    private static DispenserBlockEntity dispenser(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST));
        var dispenser = (DispenserBlockEntity) level.getBlockEntity(pos);
        dispenser.setItem(0, bottle());
        return dispenser;
    }

    private static void dispense(ServerLevel level, BlockPos pos) {
        level.getBlockState(pos).tick(level, pos, level.getRandom());
    }
}
