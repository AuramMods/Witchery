package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.BrazierBlockEntity;
import art.arcane.witchery.block.entity.DistilleryBlockEntity;
import art.arcane.witchery.block.entity.SpinningWheelBlockEntity;
import art.arcane.witchery.crafting.BrazierRecipe;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.menu.DistilleryMenu;
import art.arcane.witchery.menu.SpinningWheelMenu;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WorkstationPowerTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_distillery_glow")
    public static void distilleryKeepsItsOriginalGlowThrottleDuringPowerChanges(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 4, 3));
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("distilleryidle").get().defaultBlockState());
        var distillery = (DistilleryBlockEntity) level.getBlockEntity(pos);
        var altar = placeAltar(helper, pos.east(3));
        distillery.setItem(0, new ItemStack(Items.BLAZE_POWDER));
        distillery.setItem(1, new ItemStack(Items.GUNPOWDER));
        distillery.setItem(2, new ItemStack(WitcheryIngredient.CLAY_JAR.item()));
        var player = helper.makeMockPlayer();
        var menu = (DistilleryMenu) distillery.createMenu(1, player.getInventory(), player);

        int progress = 0;
        boolean lit = true;
        for (int age = 1; age <= 44; ++age) {
            boolean supply = age == 1 || age == 23 || age == 25 || age == 43;
            setAltarPower(altar, supply ? 1 : 0);
            if (age == 2) distillery.setItem(2, new ItemStack(WitcheryIngredient.CLAY_JAR.item(), 2));
            if (age == 22) lit = false;
            if (age == 43) lit = true;
            if (age == 44) {
                distillery.setItem(0, ItemStack.EMPTY);
                progress = 0;
                lit = false;
            }
            if (supply) progress++;
            DistilleryBlockEntity.serverTick(level, pos, distillery.getBlockState(), distillery);
            var state = level.getBlockState(pos);
            helper.assertTrue(state.getValue(DistilleryBlock.LIT) == lit && menu.cookTime() == progress
                            && (age == 44 || menu.powered() == supply),
                    "Match the 0.24.1 glow, progress and power at loaded tick " + age);
            helper.assertTrue(state.getValue(DistilleryBlock.VISIBLE_JARS) == (age == 1 ? 1 : 2),
                    "The jar display updates while a glow transition is pending");
        }
        helper.succeed();
    }

    @GameTest(template = "qa_rite_terrain", batch = "witchery_workstation_altar_switch")
    public static void burningWorkstationsSwitchToCloserAltar(GameTestHelper helper) {
        var level = helper.getLevel();
        var row = helper.absolutePos(new BlockPos(6, 4, 5));
        var wheelPos = row.east(2);
        var brazierPos = row.east(4);
        level.setBlockAndUpdate(row, WitcheryBlocks.getRequired("distilleryidle").get().defaultBlockState());
        level.setBlockAndUpdate(wheelPos, WitcheryBlocks.getRequired("spinningwheel").get().defaultBlockState());
        var distillery = (DistilleryBlockEntity) level.getBlockEntity(row);
        var wheel = (SpinningWheelBlockEntity) level.getBlockEntity(wheelPos);
        var brazier = placeBrazier(helper, brazierPos);

        distillery.setItem(0, new ItemStack(Items.BLAZE_POWDER));
        distillery.setItem(1, new ItemStack(Items.GUNPOWDER));
        distillery.setItem(2, new ItemStack(WitcheryIngredient.CLAY_JAR.item()));
        wheel.setItem(0, new ItemStack(Items.STRING, 8));
        for (var ingredient : BrazierRecipe.MIST.ingredients()) brazier.insert(new ItemStack(ingredient));
        brazier.ignite();
        BlockEntity[] workshop = {distillery, wheel, brazier};

        // Advance each actual ticker together so natural altar recharge cannot mask a power debit.
        tickWorkshop(helper, 0, workshop);
        var far = placeAltar(helper, row.west(3).south(12));
        setAltarPower(far, 100);
        tickWorkshop(helper, 1, workshop);
        var near = placeAltar(helper, row.east(2).south(3));
        tickWorkshop(helper, 2, workshop);
        helper.assertTrue(Math.abs(far.power() - 95.6F) < 0.001F && near.power() == 0,
                "Each batch draws from its previous altar for one more tick");

        tickWorkshop(helper, 2, workshop);
        helper.assertTrue(Math.abs(far.power() - 95.6F) < 0.001F && near.power() == 0,
                "A closer empty altar pauses all three batches without debiting the farther one");
        setAltarPower(near, 100);
        tickWorkshop(helper, 3, workshop);
        tickWorkshop(helper, 4, workshop);
        helper.assertTrue(Math.abs(far.power() - 95.6F) < 0.001F && Math.abs(near.power() - 95.6F) < 0.001F,
                "The same three batches resume using only the closer altar");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_idle_brazier_power")
    public static void idleBrazierSavesItsConnectionWithoutDrawingCharge(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        var brazier = placeBrazier(helper, pos);
        var altar = placeAltar(helper, pos.south(3));
        for (int tick = 1; tick < 40; tick++) {
            BrazierBlockEntity.serverTick(level, pos, brazier.getBlockState(), brazier);
        }
        helper.assertTrue(brazier.saveWithoutMetadata().getInt("PowerLevel") == 0,
                "The first 39 idle ticks leave the saved connection unchanged");
        BrazierBlockEntity.serverTick(level, pos, brazier.getBlockState(), brazier);
        var saved = brazier.saveWithoutMetadata();
        brazier.load(saved);
        helper.assertTrue(saved.getInt("PowerLevel") == 1 && saved.getLong("CookTime") == 0
                        && brazier.saveWithoutMetadata().getInt("PowerLevel") == 1 && altar.power() == 0,
                "Idle tick 40 connects to an empty altar and retains that flag across an NBT round trip");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_workstation_power")
    public static void idlePowerAndBlockedOutputs(GameTestHelper helper) {
        var level = helper.getLevel();
        var altarPos = helper.absolutePos(new BlockPos(5, 4, 5));
        var altar = placeAltar(helper, altarPos);
        var distilleryPos = altarPos.west(2);
        var wheelPos = altarPos.north(2);
        level.setBlockAndUpdate(distilleryPos, WitcheryBlocks.getRequired("distilleryidle").get().defaultBlockState());
        level.setBlockAndUpdate(wheelPos, WitcheryBlocks.getRequired("spinningwheel").get().defaultBlockState());
        var distillery = (DistilleryBlockEntity) level.getBlockEntity(distilleryPos);
        var wheel = (SpinningWheelBlockEntity) level.getBlockEntity(wheelPos);
        var player = helper.makeMockPlayer();
        var distilleryMenu = (DistilleryMenu) distillery.createMenu(1, player.getInventory(), player);
        var wheelMenu = (SpinningWheelMenu) wheel.createMenu(2, player.getInventory(), player);

        helper.startSequence().thenWaitUntil(() -> {
            setAltarPower(altar, 0);
            helper.assertTrue(distilleryMenu.powered() && wheelMenu.powered(),
                    "Idle workstation power indicators should report a connected altar with no charge");
        }).thenExecute(() -> {
            distillery.setItem(0, new ItemStack(Items.BLAZE_POWDER));
            distillery.setItem(1, new ItemStack(Items.GUNPOWDER));
            distillery.setItem(2, new ItemStack(WitcheryIngredient.CLAY_JAR.item()));
            wheel.setItem(0, new ItemStack(Items.STRING, 8));
            var distilling = distillery.saveWithoutMetadata();
            distilling.putInt("CookTime", 140);
            distillery.load(distilling);
            var spinning = wheel.saveWithoutMetadata();
            spinning.putInt("CookTime", 90);
            wheel.load(spinning);

            DistilleryBlockEntity.serverTick(level, distilleryPos, distillery.getBlockState(), distillery);
            SpinningWheelBlockEntity.serverTick(level, wheelPos, wheel.getBlockState(), wheel);
            helper.assertTrue(!distilleryMenu.powered() && distilleryMenu.cookTime() == 140
                            && !wheelMenu.powered() && wheelMenu.progressWidth(300) == 90,
                    "Loss of altar charge should pause both batches at their saved positions");

            setAltarPower(altar, 5);
            DistilleryBlockEntity.serverTick(level, distilleryPos, distillery.getBlockState(), distillery);
            SpinningWheelBlockEntity.serverTick(level, wheelPos, wheel.getBlockState(), wheel);
            helper.assertTrue(distilleryMenu.cookTime() == 141 && wheelMenu.progressWidth(300) == 91
                            && Math.abs(altar.power() - 3.8F) < 0.001F,
                    "Each resumed machine should spend one 0.6-power tick");

            distillery.setItem(3, new ItemStack(Items.GLOWSTONE_DUST, 64));
            wheel.setItem(2, new ItemStack(Items.COBWEB, 64));
            DistilleryBlockEntity.serverTick(level, distilleryPos, distillery.getBlockState(), distillery);
            SpinningWheelBlockEntity.serverTick(level, wheelPos, wheel.getBlockState(), wheel);
            helper.assertTrue(distilleryMenu.cookTime() == 0 && wheelMenu.progressWidth(300) == 0
                            && distillery.getItem(3).getCount() == 64 && wheel.getItem(2).getCount() == 64
                            && Math.abs(altar.power() - 3.8F) < 0.001F,
                    "A full output should clear progress without modifying the stack or spending charge");

            distillery.setItem(3, new ItemStack(Items.COBBLESTONE, 7));
            var incompatible = distillery.saveWithoutMetadata();
            incompatible.putInt("CookTime", 799);
            distillery.load(incompatible);
            DistilleryBlockEntity.serverTick(level, distilleryPos, distillery.getBlockState(), distillery);
            helper.assertTrue(distilleryMenu.cookTime() == 0 && distillery.getItem(0).is(Items.BLAZE_POWDER)
                            && distillery.getItem(2).is(WitcheryIngredient.CLAY_JAR.item())
                            && distillery.getItem(3).is(Items.COBBLESTONE) && distillery.getItem(4).isEmpty(),
                    "An incompatible stack should block the batch without wasting any output or input");
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_workstation_power")
    public static void directRecipeSwapsKeepProgress(GameTestHelper helper) {
        var level = helper.getLevel();
        var altarPos = helper.absolutePos(new BlockPos(5, 4, 5));
        var altar = placeAltar(helper, altarPos);
        setAltarPower(altar, 30);
        var distilleryPos = altarPos.west(2);
        var wheelPos = altarPos.north(2);
        level.setBlockAndUpdate(distilleryPos, WitcheryBlocks.getRequired("distilleryidle").get().defaultBlockState());
        level.setBlockAndUpdate(wheelPos, WitcheryBlocks.getRequired("spinningwheel").get().defaultBlockState());
        var distillery = (DistilleryBlockEntity) level.getBlockEntity(distilleryPos);
        var wheel = (SpinningWheelBlockEntity) level.getBlockEntity(wheelPos);

        distillery.setItem(0, new ItemStack(WitcheryIngredient.DEMON_HEART.item()));
        distillery.setItem(1, new ItemStack(WitcheryIngredient.DIAMOND_VAPOUR.item()));
        distillery.setItem(2, new ItemStack(WitcheryIngredient.CLAY_JAR.item(), 4));
        wheel.setItem(0, new ItemStack(Items.STRING, 8));
        wheel.setItem(1, new ItemStack(WitcheryIngredient.WHIFF_OF_MAGIC.item()));
        var savedDistillery = distillery.saveWithoutMetadata();
        savedDistillery.putInt("CookTime", 797);
        savedDistillery.putString("Recipe", "witchery:distilling/infernal_blood_from_diamond_vapour");
        distillery.load(savedDistillery);
        var savedWheel = wheel.saveWithoutMetadata();
        savedWheel.putInt("CookTime", 297);
        savedWheel.putString("Recipe", "witchery:spinning/cobweb");
        wheel.load(savedWheel);
        DistilleryBlockEntity.serverTick(level, distilleryPos, distillery.getBlockState(), distillery);
        SpinningWheelBlockEntity.serverTick(level, wheelPos, wheel.getBlockState(), wheel);

        distillery.setItem(1, new ItemStack(Items.NETHERRACK));
        wheel.setItem(0, new ItemStack(Items.HAY_BLOCK));
        DistilleryBlockEntity.serverTick(level, distilleryPos, distillery.getBlockState(), distillery);
        SpinningWheelBlockEntity.serverTick(level, wheelPos, wheel.getBlockState(), wheel);
        helper.assertTrue(distillery.saveWithoutMetadata().getInt("CookTime") == 799
                        && wheel.saveWithoutMetadata().getInt("CookTime") == 299,
                "Switching directly to another matching recipe should preserve elapsed ticks");

        DistilleryBlockEntity.serverTick(level, distilleryPos, distillery.getBlockState(), distillery);
        SpinningWheelBlockEntity.serverTick(level, wheelPos, wheel.getBlockState(), wheel);
        helper.assertTrue(distillery.getItem(3).is(Items.SOUL_SAND)
                        && distillery.getItem(4).is(WitcheryIngredient.INFERNAL_BLOOD.item()) && distillery.getItem(4).getCount() == 1
                        && distillery.getItem(5).is(WitcheryIngredient.INFERNAL_BLOOD.item()) && distillery.getItem(5).getCount() == 1
                        && distillery.getItem(2).getCount() == 2 && distillery.getItem(1).isEmpty(),
                "The Distillery should finish the Netherrack recipe with its own two-jar cost and outputs");
        helper.assertTrue(wheel.getItem(2).is(WitcheryIngredient.GOLDEN_THREAD.item()) && wheel.getItem(2).getCount() == 3
                        && wheel.getItem(0).isEmpty() && wheel.getItem(1).isEmpty(),
                "The Wheel should consume the Hay Bale and Whiff of Magic and make three Golden Threads");
        helper.succeed();
    }

    private static AltarBlockEntity placeAltar(GameTestHelper helper, BlockPos pos) {
        var level = helper.getLevel();
        var state = WitcheryBlocks.getRequired("altar").get().defaultBlockState();
        for (BlockPos part : BlockPos.betweenClosed(pos, pos.offset(2, 0, 1))) {
            level.setBlockAndUpdate(part, state);
        }
        AltarBlockEntity.form(level, pos);
        return (AltarBlockEntity) level.getBlockEntity(pos);
    }

    private static BrazierBlockEntity placeBrazier(GameTestHelper helper, BlockPos pos) {
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("brazier").get().defaultBlockState());
        return (BrazierBlockEntity) level.getBlockEntity(pos);
    }

    private static void tickWorkshop(GameTestHelper helper, int progress, BlockEntity... workshop) {
        var level = helper.getLevel();
        for (BlockEntity machine : workshop) {
            var pos = machine.getBlockPos();
            var state = machine.getBlockState();
            if (machine instanceof DistilleryBlockEntity distillery) {
                DistilleryBlockEntity.serverTick(level, pos, state, distillery);
            } else if (machine instanceof SpinningWheelBlockEntity wheel) {
                SpinningWheelBlockEntity.serverTick(level, pos, state, wheel);
            } else if (machine instanceof BrazierBlockEntity brazier) {
                BrazierBlockEntity.serverTick(level, pos, state, brazier);
            }
            helper.assertTrue(machine.saveWithoutMetadata().getLong("CookTime") == progress,
                    "Unexpected batch progress at " + pos.toShortString() + ", expected " + progress);
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_workstation_power")
    public static void spinningWheelKeepsImportedPowerIndicator(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("spinningwheel").get().defaultBlockState());
        var wheel = (SpinningWheelBlockEntity) level.getBlockEntity(pos);
        var original = new CompoundTag();
        original.putShort("PowerLevel", (short) 1);
        original.putShort("CookTime", (short) 90);
        wheel.load(original);
        var player = helper.makeMockPlayer();
        var menu = (SpinningWheelMenu) wheel.createMenu(1, player.getInventory(), player);
        var saved = wheel.saveWithoutMetadata();
        helper.assertTrue(menu.powered() && menu.progressWidth(300) == 90 && saved.getShort("PowerLevel") == 1,
                "The original short PowerLevel should survive loading, menu creation and saving");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_spinning_wheel_inventory")
    public static void spinningWheelRestoresSlotOrderAndFullBlockBounds(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 4, 3));
        var state = WitcheryBlocks.getRequired("spinningwheel").get().defaultBlockState()
                .setValue(SpinningWheelBlock.FACING, Direction.EAST);
        level.setBlockAndUpdate(pos, state);
        var wheel = (SpinningWheelBlockEntity) level.getBlockEntity(pos);
        var player = helper.makeMockPlayer();
        var menu = (SpinningWheelMenu) wheel.createMenu(1, player.getInventory(), player);
        wheel.setItem(0, new ItemStack(Items.STRING, 8));
        wheel.setItem(1, new ItemStack(Items.DIRT));
        wheel.setItem(4, new ItemStack(Items.HAY_BLOCK, 5));
        player.getInventory().setItem(0, new ItemStack(Items.HAY_BLOCK));
        menu.quickMoveStack(player, 32);
        helper.assertTrue(player.getInventory().getItem(0).isEmpty() && wheel.getItem(3).is(Items.HAY_BLOCK)
                        && wheel.getItem(3).getCount() == 1 && wheel.getItem(4).getCount() == 5,
                "Blocked fibre must use the first available individual modifier slot");
        player.getInventory().setItem(0, new ItemStack(Items.STRING).setHoverName(Component.literal("Spare string")));
        menu.quickMoveStack(player, 32);
        helper.assertTrue(player.getInventory().getItem(0).hasCustomHoverName() && wheel.getItem(0).getCount() == 8,
                "Named matching fibre tries the fibre slot and respects its normal stack merge rules");
        helper.assertTrue(state.getShape(level, pos).bounds().getSize() == 1.0
                        && state.getCollisionShape(level, pos).bounds().getSize() == 1.0
                        && !state.isFaceSturdy(level, pos, Direction.UP),
                "The wheel has full outline and collision and no sturdy top");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_spinning_wheel_inventory")
    public static void spinningWheelAddsToNamedOutputAndUsesFirstMatchingModifier(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 4, 3));
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("spinningwheel").get().defaultBlockState());
        var wheel = (SpinningWheelBlockEntity) level.getBlockEntity(pos);
        var altar = placeAltar(helper, pos.east(3));
        var name = Component.literal("Saved golden thread");
        wheel.setItem(0, new ItemStack(Items.HAY_BLOCK, 2));
        wheel.setItem(1, new ItemStack(WitcheryIngredient.WHIFF_OF_MAGIC.item()));
        wheel.setItem(2, new ItemStack(WitcheryIngredient.GOLDEN_THREAD.item()).setHoverName(name));
        wheel.setItem(3, new ItemStack(WitcheryIngredient.WHIFF_OF_MAGIC.item()));
        var saved = wheel.saveWithoutMetadata();
        saved.putInt("CookTime", 299);
        wheel.load(saved);
        setAltarPower(altar, 1);
        SpinningWheelBlockEntity.serverTick(level, pos, wheel.getBlockState(), wheel);
        helper.assertTrue(wheel.getItem(2).getCount() == 4 && wheel.getItem(2).getHoverName().equals(name)
                        && wheel.getItem(0).getCount() == 1 && wheel.getItem(1).isEmpty()
                        && wheel.getItem(3).getCount() == 1,
                "One batch adds three threads to the named stack and consumes only the first modifier");
        helper.succeed();
    }

    private static void setAltarPower(AltarBlockEntity altar, float power) {
        var saved = altar.saveWithoutMetadata();
        saved.putFloat("Power", power);
        altar.load(saved);
    }
}
