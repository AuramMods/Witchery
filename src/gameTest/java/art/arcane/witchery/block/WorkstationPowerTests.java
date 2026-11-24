package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.DistilleryBlockEntity;
import art.arcane.witchery.block.entity.SpinningWheelBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.menu.DistilleryMenu;
import art.arcane.witchery.menu.SpinningWheelMenu;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WorkstationPowerTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_workstation_power")
    public static void idlePowerAndBlockedOutputs(GameTestHelper helper) {
        var level = helper.getLevel();
        var altarPos = helper.absolutePos(new BlockPos(5, 4, 5));
        var altarState = WitcheryBlocks.getRequired("altar").get().defaultBlockState();
        for (BlockPos part : BlockPos.betweenClosed(altarPos, altarPos.offset(2, 0, 1))) {
            level.setBlockAndUpdate(part, altarState);
        }
        AltarBlockEntity.form(level, altarPos);
        var altar = (AltarBlockEntity) level.getBlockEntity(altarPos);
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

    private static void setAltarPower(AltarBlockEntity altar, float power) {
        var saved = altar.saveWithoutMetadata();
        saved.putFloat("Power", power);
        altar.load(saved);
    }
}
