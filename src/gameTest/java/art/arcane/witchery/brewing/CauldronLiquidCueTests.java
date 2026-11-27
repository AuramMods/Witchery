package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.infusion.SymbolTerrain;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CauldronLiquidCueTests {
    @GameTest(template = "qa_coven_arena")
    public static void handTransfersPlaySwimAtBrewer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var cauldron = placeCauldron(helper);
        BlockHitResult hit = hit(cauldron.getBlockPos());
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "CauldronBrewer"));
        player.setPos(Vec3.atBottomCenterOf(hit.getBlockPos().south(2)));
        var cues = new ArrayList<PlayLevelSoundEvent.AtEntity>();
        Consumer<PlayLevelSoundEvent.AtEntity> capture = event -> {
            if (event.getEntity() == player) cues.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            use(player, hit, new ItemStack(Items.WATER_BUCKET));
            helper.assertTrue(player.getMainHandItem().is(Items.BUCKET) && volume(cauldron) == 1000,
                    "The full bucket adds one bucket of water");
            assertCue(helper, cues, "pour a water bucket");

            use(player, hit, new ItemStack(Items.GLASS_BOTTLE));
            ItemStack water = player.getMainHandItem();
            helper.assertTrue(PotionUtils.getPotion(water) == Potions.WATER && volume(cauldron) == 0,
                    "The glass bottle withdraws one bucket of water");
            assertCue(helper, cues, "fill a water bottle");

            use(player, hit, water);
            assertCue(helper, cues, "pour a water bottle");
            use(player, hit, new ItemStack(Items.BUCKET));
            helper.assertTrue(player.getMainHandItem().is(Items.WATER_BUCKET) && volume(cauldron) == 0,
                    "The empty bucket withdraws the water added by the bottle");
            assertCue(helper, cues, "fill a water bucket");

            loadBrew(cauldron, 3000, true);
            use(player, hit, new ItemStack(Items.BUCKET));
            ItemStack brewBucket = player.getMainHandItem();
            helper.assertTrue(brewBucket.is(WitcheryItems.getRequired("bucketbrew").get())
                            && brewBucket.hasTag() && volume(cauldron) == 2000,
                    "The empty bucket withdraws one bucket of tagged brew");
            assertCue(helper, cues, "fill a brew bucket");
            use(player, hit, brewBucket);
            helper.assertTrue(player.getMainHandItem().is(Items.BUCKET) && volume(cauldron) == 3000,
                    "Pouring the tagged brew back refills the cauldron");
            assertCue(helper, cues, "pour a brew bucket");

            var inventory = player.getInventory();
            for (int slot = 1; slot < inventory.items.size(); slot++) {
                inventory.setItem(slot, new ItemStack(Items.STONE, 64));
            }
            use(player, hit, new ItemStack(Items.BUCKET, 2));
            helper.assertTrue(player.getMainHandItem().getCount() == 2 && volume(cauldron) == 3000 && cues.isEmpty(),
                    "A stacked draw with no room for the filled bucket keeps the brew and both buckets");
            inventory.clearContent();

            use(player, hit, new ItemStack(Items.GLASS_BOTTLE));
            helper.assertTrue(player.getMainHandItem().is(WitcheryItems.getRequired("brewbottle").get())
                            && volume(cauldron) == 0, "A glass bottle withdraws the prepared brew");
            assertCue(helper, cues, "fill a brew bottle");
            use(player, hit, new ItemStack(Items.BUCKET));
            helper.assertTrue(player.getMainHandItem().is(Items.BUCKET) && cues.isEmpty(),
                    "A draw from an empty cauldron stays quiet");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void automaticTransfersPlaySwimAtCauldronCorner(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var cauldron = placeCauldron(helper);
        BlockPos pos = cauldron.getBlockPos();
        BlockHitResult hit = hit(pos);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "CauldronCaster"));
        var bottle = new ItemStack(WitcheryItems.getRequired("brew.water").get());
        player.setItemInHand(InteractionHand.MAIN_HAND, bottle);
        var context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
        var cues = new ArrayList<PlayLevelSoundEvent.AtPosition>();
        Consumer<PlayLevelSoundEvent.AtPosition> capture = event -> {
            if (event.getPosition().equals(Vec3.atLowerCornerOf(pos))) cues.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            bottle.useOn(context);
            helper.assertTrue(volume(cauldron) == 3000 && bottle.getDamageValue() == 1, "Endless Water fills the cauldron");
            assertCue(helper, cues, "pour Endless Water");
            bottle.useOn(context);
            helper.assertTrue(bottle.getDamageValue() == 1 && cues.isEmpty(), "A full vessel stays quiet");
            var tank = cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
            tank.drain(3000, IFluidHandler.FluidAction.EXECUTE);
            SymbolTerrain.pourWater(level, player, hit, 1);
            helper.assertTrue(volume(cauldron) == 3000, "Aguamenti fills the cauldron");
            assertCue(helper, cues, "cast Aguamenti");

            var dispenserPos = pos.north();
            var dispenserState = Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST);
            level.setBlockAndUpdate(dispenserPos, dispenserState);
            var dispenser = (DispenserBlockEntity) level.getBlockEntity(dispenserPos);
            for (int amount : new int[]{3000, 2000}) {
                loadBrew(cauldron, amount, true);
                dispenser.setItem(0, new ItemStack(Items.GLASS_BOTTLE));
                dispenserState.tick(level, dispenserPos, level.random);
                helper.assertTrue(volume(cauldron) == 0 && dispenser.isEmpty() == (amount == 3000),
                        "The dispenser drains either batch and spends a bottle only for the full portion");
                assertCue(helper, cues, "dispense from " + amount + " mB");
            }
            loadBrew(cauldron, 3000, false);
            dispenserState.tick(level, dispenserPos, level.random);
            helper.assertTrue(volume(cauldron) == 3000 && !dispenser.isEmpty() && cues.isEmpty(),
                    "An unpowered batch keeps its contents and stays quiet");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
        helper.succeed();
    }

    private static WitchesCauldronBlockEntity placeCauldron(GameTestHelper helper) {
        var local = new BlockPos(5, 4, 5);
        helper.setBlock(local, WitcheryBlocks.getRequired("cauldron").get());
        return (WitchesCauldronBlockEntity) helper.getBlockEntity(local);
    }

    private static BlockHitResult hit(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
    }

    private static void use(FakePlayer player, BlockHitResult hit, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var level = player.serverLevel();
        level.getBlockState(hit.getBlockPos()).use(level, player, InteractionHand.MAIN_HAND, hit);
    }

    private static void loadBrew(WitchesCauldronBlockEntity cauldron, int amount, boolean powered) {
        var saved = new CompoundTag();
        saved.putString("FluidName", "witchery:brew");
        saved.putInt("Amount", amount);
        saved.putInt("TicksHeated", 100);
        saved.putBoolean("Powered", powered);
        saved.put("Tag", BrewMixture.ingredientTag(List.of(new ItemStack(Items.RED_WOOL)), null));
        cauldron.load(saved);
    }

    private static int volume(WitchesCauldronBlockEntity cauldron) {
        return cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new)
                .getFluidInTank(0).getAmount();
    }

    private static void assertCue(GameTestHelper helper, List<? extends PlayLevelSoundEvent> cues, String action) {
        helper.assertTrue(cues.size() == 1, "Expected exactly one cue to " + action + ", got " + cues.size());
        PlayLevelSoundEvent cue = cues.remove(0);
        helper.assertTrue(cue.getSound() != null && cue.getSound().value() == SoundEvents.GENERIC_SWIM
                        && cue.getNewVolume() == 0.5F && cue.getNewPitch() >= 0.3333333F && cue.getNewPitch() <= 0.5F,
                "Expected the original quiet swim cue to " + action);
    }
}
