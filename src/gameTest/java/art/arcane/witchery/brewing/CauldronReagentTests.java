package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryFluids;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CauldronReagentTests {
    @GameTest(template = "qa_coven_arena")
    public static void droppedQuicklimeDetonatesColdBrewAndSparesNearestPlayer(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(6, 4, 6));
        var cauldron = placeCauldron(helper, pos);
        var tank = tank(cauldron);
        var fluid = WitcheryFluids.getRequiredSource("brew").get();
        var contents = BrewMixture.ingredientTag(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.SUGAR),
                new ItemStack(Items.STRUCTURE_VOID)), 0x99afae);
        helper.assertTrue(tank.fill(new FluidStack(fluid, 2000, contents), IFluidHandler.FluidAction.EXECUTE) == 2000,
                "The cauldron accepts a cold brew with two recognized actions and an unknown saved item");

        Vec3 center = Vec3.atCenterOf(pos).add(0, 1, 0);
        var unattended = dropAt(helper, center.add(0, -1, 0), WitcheryIngredient.QUICKLIME, 3);
        level.getBlockState(pos).entityInside(level, pos, unattended);
        helper.assertTrue(!unattended.isRemoved() && tank.getFluidInTank(0).getAmount() == 2000,
                "Quicklime has no effect without a nearby player");

        var nearest = addPlayer(helper, center.add(1.3, 0, 0));
        var other = addPlayer(helper, center.add(-2, 0, 0));
        var within = helper.spawn(EntityType.PIG, helper.relativeVec(center.add(0, 0, 3.25)));
        var beyond = helper.spawn(EntityType.PIG, helper.relativeVec(center.add(0, 0, -4.5)));
        within.setNoAi(true);
        within.setNoGravity(true);
        beyond.setNoAi(true);
        beyond.setNoGravity(true);
        level.setBlockAndUpdate(pos.above(3), Blocks.GLASS.defaultBlockState());
        var quicklime = dropAt(helper, center.add(0, 0.3, 0), WitcheryIngredient.QUICKLIME, 3);
        quicklime.setDeltaMovement(0, -0.4, 0);
        level.addFreshEntity(quicklime);

        helper.runAfterDelay(5, () -> {
            try {
                helper.assertTrue(quicklime.isRemoved() && tank.getFluidInTank(0).isEmpty(),
                        "A dropped stack of Quicklime detonates the entire cold brew");
                helper.assertTrue(nearest.getDeltaMovement().lengthSqr() == 0 && other.getDeltaMovement().x < 0,
                        "The explosion excludes the nearest player and knocks back the other player");
                helper.assertTrue(within.getHealth() < within.getMaxHealth() && beyond.getHealth() == beyond.getMaxHealth(),
                        "Two recognized actions yield power two and the unknown item adds no power. Pig health: "
                                + within.getHealth() + " at 3.25 blocks, " + beyond.getHealth() + " at 4.5 blocks");
                helper.assertTrue(level.getBlockState(pos.above(3)).is(Blocks.GLASS), "The blast preserves nearby blocks");
                var state = level.getBlockState(pos);
                helper.assertTrue(state.getValue(WitchesCauldronBlock.WATER) == 0 && cauldron.comparatorSignal() == 0,
                        "The cauldron empties its block state and comparator signal");
                cauldron.load(cauldron.saveWithFullMetadata());
                helper.assertTrue(tank.getFluidInTank(0).isEmpty(), "The cauldron saves without a residual brew");
                helper.succeed();
            } finally {
                level.removePlayerImmediately(nearest, Entity.RemovalReason.DISCARDED);
                level.removePlayerImmediately(other, Entity.RemovalReason.DISCARDED);
            }
        });
    }

    @GameTest(template = "qa_coven_arena", timeoutTicks = 140)
    public static void quicklimeInterruptsCookingRitual(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(6, 4, 6));
        var cauldron = placeCauldron(helper, pos);
        var tank = tank(cauldron);
        tank.fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE);
        level.setBlockAndUpdate(pos.below(2), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(pos.below(), Blocks.FIRE.defaultBlockState());

        helper.runAfterDelay(105, () -> {
            Vec3 center = Vec3.atCenterOf(pos);
            var rawPork = new ItemEntity(level, center.x, center.y, center.z, new ItemStack(Items.PORKCHOP));
            level.getBlockState(pos).entityInside(level, pos, rawPork);
            helper.assertTrue(rawPork.isRemoved() && cauldron.saveWithFullMetadata().getString("Ritual")
                    .equals("witchery:porkchop_from_cauldron"), "Raw Porkchop starts the original cooking ritual");

            var player = addPlayer(helper, center.add(1.5, 1, 0));
            var target = helper.spawn(EntityType.PIG, helper.relativeVec(center.add(0, 1, 2.5)));
            try {
                var quicklime = dropAt(helper, center, WitcheryIngredient.QUICKLIME, 1);
                level.getBlockState(pos).entityInside(level, pos, quicklime);
                helper.assertTrue(quicklime.isRemoved() && tank.getFluidInTank(0).isEmpty()
                                && !cauldron.saveWithFullMetadata().contains("Ritual"),
                        "Quicklime consumes the entire ritual and resets its saved state");
                helper.assertTrue(target.getHealth() < target.getMaxHealth(), "The cooking trigger increases the blast power to 1.5");
                helper.succeed();
            } finally {
                level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
            }
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void plainLiquidsResistQuicklimeAndGypsumUsesWholeStack(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(6, 4, 6));
        var tank = tank(placeCauldron(helper, pos));
        Vec3 center = Vec3.atCenterOf(pos);
        var player = addPlayer(helper, center.add(2, 1, 0));
        try {
            for (var fluid : List.of(Fluids.WATER, WitcheryFluids.getRequiredSource("brew").get())) {
                tank.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
                var quicklime = dropAt(helper, center, WitcheryIngredient.QUICKLIME, 4);
                level.getBlockState(pos).entityInside(level, pos, quicklime);
                helper.assertTrue(!quicklime.isRemoved() && quicklime.getItem().getCount() == 4
                                && tank.getFluidInTank(0).getAmount() == 1000,
                        "Quicklime leaves plain water and unprepared brew alone even with a player present");

                var gypsum = dropAt(helper, center, WitcheryIngredient.GYPSUM, 4);
                level.getBlockState(pos).entityInside(level, pos, gypsum);
                helper.assertTrue(gypsum.isRemoved() && tank.getFluidInTank(0).isEmpty(),
                        "A whole dropped stack of Gypsum clears the cold liquid");
            }

            var dyed = BrewMixture.ingredientTag(List.of(), 0xc74636);
            tank.fill(new FluidStack(WitcheryFluids.getRequiredSource("brew").get(), 1000, dyed), IFluidHandler.FluidAction.EXECUTE);
            var quicklime = dropAt(helper, center, WitcheryIngredient.QUICKLIME, 1);
            level.getBlockState(pos).entityInside(level, pos, quicklime);
            helper.assertTrue(quicklime.isRemoved() && tank.getFluidInTank(0).isEmpty(), "A dye-only brew still reacts");
            helper.succeed();
        } finally {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }

    private static WitchesCauldronBlockEntity placeCauldron(GameTestHelper helper, BlockPos pos) {
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        return (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
    }

    private static IFluidHandler tank(WitchesCauldronBlockEntity cauldron) {
        return cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
    }

    private static FakePlayer addPlayer(GameTestHelper helper, Vec3 position) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "CauldronTester"));
        player.setPos(position);
        level.addNewPlayer(player);
        return player;
    }

    private static ItemEntity dropAt(GameTestHelper helper, Vec3 position, WitcheryIngredient ingredient, int count) {
        var item = new ItemEntity(helper.getLevel(), position.x, position.y, position.z, new ItemStack(ingredient.item(), count));
        item.setDefaultPickUpDelay();
        return item;
    }
}
