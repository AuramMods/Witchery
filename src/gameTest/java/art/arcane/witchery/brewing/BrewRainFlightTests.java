package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.BrewDroplet;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewRainFlightTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_rain_flight")
    public static void rainFinishesItsOriginalDeepWaterFlightAfterLoading(GameTestHelper helper) {
        var level = helper.getLevel();
        var floor = helper.absolutePos(new BlockPos(5, 4, 5)).atY(40);
        level.setBlock(floor, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        for (int y = 41; y <= 211; y++) level.setBlock(floor.atY(y), Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);

        var drop = coldDrop(level, Vec3.atBottomCenterOf(floor.atY(210)));
        for (int tick = 0; tick < 400; tick++) level.tickNonPassenger(drop);
        helper.assertTrue(drop.isAlive() && Math.abs(drop.getY() - 88.47442) < 0.00001,
                "The original droplet is still descending at Y 88.47442 on tick 400, got " + drop.getY());
        var saved = drop.saveWithoutId(new CompoundTag());
        drop.discard();
        var resumed = WitcheryEntities.BREW_DROPLET.get().create(level);
        resumed.load(saved);
        helper.assertTrue(resumed.position().equals(drop.position()) && resumed.getDeltaMovement().equals(drop.getDeltaMovement())
                        && resumed.color() == drop.color(), "Loading preserves the position, momentum and tint");

        for (int tick = 0; tick < 149; tick++) level.tickNonPassenger(resumed);
        helper.assertTrue(resumed.isAlive() && level.getBlockState(floor.above()).is(Blocks.WATER),
                "The reopened drop leaves the column unchanged through flight tick 549");
        level.tickNonPassenger(resumed);
        helper.assertTrue(resumed.isRemoved() && level.getBlockState(floor.above()).is(Blocks.ICE)
                        && level.getBlockState(floor.above(2)).is(Blocks.ICE),
                "At tick 550 the saved Cold brew hits the floor and freezes both water cells");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_rain_flight")
    public static void rainUsesTheOriginalWaterSampleAndCurrent(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(5, 4, 5)).atY(180);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -1, -1), origin.offset(2, 0, 1))) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (int dx = -1; dx <= 1; dx++) {
            var water = Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, dx + 2);
            level.setBlock(origin.offset(dx, 0, 0), water, Block.UPDATE_CLIENTS);
        }

        Vec3 mid = Vec3.atBottomCenterOf(origin);
        var drySample = coldDrop(level, mid.add(0, 0.05, 0));
        var wetSample = coldDrop(level, mid.add(0, 0.38, 0));
        level.tickNonPassenger(drySample);
        level.tickNonPassenger(wetSample);
        helper.assertTrue(!drySample.isInWater() && drySample.getX() == mid.x && drySample.getDeltaMovement().x == 0,
                "A drop outside the legacy water sample receives no sideways current");
        helper.assertTrue(wetSample.isInWater() && Math.abs(wetSample.getX() - mid.x - 0.014) < 1e-8
                        && Math.abs(wetSample.getDeltaMovement().x - 0.0112) < 1e-8 && wetSample.getDeltaMovement().z == 0,
                "The original current moves a wet drop 0.014 blocks before water drag");
        helper.succeed();
    }

    private static BrewDroplet coldDrop(ServerLevel level, Vec3 start) {
        var ingredients = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(WitcheryIngredient.ICY_NEEDLE.item()),
                new ItemStack(WitcheryIngredient.WORMWOOD.item()));
        var drop = WitcheryEntities.BREW_DROPLET.get().create(level);
        drop.setBrew(BrewMixture.ingredientTag(ingredients, 0x442233));
        drop.setPos(start);
        return drop;
    }
}
