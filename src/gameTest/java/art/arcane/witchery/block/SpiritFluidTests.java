package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.infusion.InfusedBrews;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SpiritFluidTests {
    @GameTest(template = "qa_coven_arena", timeoutTicks = 220)
    public static void bothDreamFluidsSpreadFourCellsAndRecede(GameTestHelper helper) {
        var level = helper.getLevel();
        var spirit = WitcheryBlocks.getRequired("spiritflowing").get();
        var tears = WitcheryBlocks.getRequired("hollowtears").get();
        BlockPos source = helper.absolutePos(new BlockPos(3, 3, 3));
        BlockPos second = source.south(4);
        for (BlockPos start : new BlockPos[]{source, second}) {
            // Keep falling terrain from replacing either source while the fluid ticks.
            for (int distance = 0; distance <= 5; distance++) {
                level.setBlockAndUpdate(start.east(distance).above(), Blocks.STONE.defaultBlockState());
            }
            for (int distance = 0; distance <= 5; distance++) {
                var cell = start.east(distance);
                level.setBlockAndUpdate(cell.below(), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(cell.north(), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(cell.south(), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
            }
            level.setBlockAndUpdate(start.west(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(start.east(6), Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(source, spirit.defaultBlockState());
        level.setBlockAndUpdate(second, tears.defaultBlockState());
        helper.startSequence().thenWaitUntil(() -> {
            assertLane(helper, source, spirit);
            assertLane(helper, second, tears);
        }).thenExecuteAfter(30, () -> {
            assertLane(helper, source, spirit);
            assertLane(helper, second, tears);
            level.setBlockAndUpdate(source, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(second, Blocks.AIR.defaultBlockState());
        }).thenWaitUntil(() -> {
            for (int distance = 1; distance <= 5; distance++) {
                helper.assertTrue(level.isEmptyBlock(source.east(distance)) && level.isEmptyBlock(second.east(distance)),
                        "Both lanes must drain completely after removing their sources");
            }
        }).thenSucceed();
    }

    private static void assertLane(GameTestHelper helper, BlockPos source, Block fluid) {
        var level = helper.getLevel();
        int[] levels = {0, 2, 3, 5, 6};
        for (int distance = 0; distance < levels.length; distance++) {
            var pos = source.east(distance);
            var state = level.getBlockState(pos);
            helper.assertTrue(state.is(fluid) && state.getValue(LiquidBlock.LEVEL) == levels[distance],
                    "Expected " + fluid + " at level " + levels[distance] + " at " + pos + ", found " + state);
        }
        helper.assertTrue(level.isEmptyBlock(source.east(5)), "The fifth cell beyond the source must remain dry");
    }

    @GameTest(template = "qa_coven_arena")
    public static void preservesOtherLiquidsAndFeedsDiagonalRunoff(GameTestHelper helper) {
        var level = helper.getLevel();
        var spirit = WitcheryBlocks.getRequired("spiritflowing").get().defaultBlockState();
        var tears = WitcheryBlocks.getRequired("hollowtears").get().defaultBlockState();
        BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
        for (BlockPos wall : new BlockPos[]{origin.west(), origin.north(), origin.south()}) {
            level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        }
        for (BlockState fluid : new BlockState[]{spirit, tears}) {
            level.setBlockAndUpdate(origin.below(), Blocks.WATER.defaultBlockState());
            level.setBlockAndUpdate(origin.east(), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(origin, fluid);
            fluid.getFluidState().tick(level, origin);
            helper.assertTrue(level.getBlockState(origin.below()).is(Blocks.WATER)
                            && level.getBlockState(origin.east()).is(fluid.getBlock()),
                    "A source spills sideways over water and preserves the other liquid");

            BlockPos runoff = origin.above(3);
            level.setBlockAndUpdate(runoff.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(runoff.above(), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(runoff.east().above(), fluid);
            // Level seven is an earlier 1.20.1 port state outside the original four runoff levels.
            var weaker = fluid.setValue(LiquidBlock.LEVEL, 7);
            level.setBlockAndUpdate(runoff, weaker);
            weaker.getFluidState().tick(level, runoff);
            helper.assertTrue(level.getBlockState(runoff) == fluid.setValue(LiquidBlock.LEVEL, 2),
                    "A diagonal feed must replenish the weak cell without giving it a falling state");

            BlockPos falling = runoff.above(3);
            level.setBlockAndUpdate(falling.below(), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(falling, fluid);
            fluid.getFluidState().tick(level, falling);
            helper.assertTrue(level.getBlockState(falling.below()) == fluid.setValue(LiquidBlock.LEVEL, 8),
                    "A downward drop must keep the Forge falling state used by original region imports");
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void dreamFluidContactUsesOriginalCreatureClasses(GameTestHelper helper) {
        var level = helper.getLevel();
        var spirit = WitcheryBlocks.getRequired("spiritflowing").get();
        var tears = WitcheryBlocks.getRequired("hollowtears").get();
        var pos = helper.absolutePos(new BlockPos(4, 3, 4));
        var pig = EntityType.PIG.create(level);
        var zombie = EntityType.ZOMBIE.create(level);
        var blaze = EntityType.BLAZE.create(level);
        var imp = WitcheryEntities.IMP.get().create(level);
        var nightmare = WitcheryEntities.NIGHTMARE.get().create(level);
        var vampire = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SpiritVampire"));
        WitcheryPlayerDataProvider.get(vampire).orElseThrow(IllegalStateException::new).setVampireLevel(1);
        var grave = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SpiritGrave"));
        InfusedBrews.applyGrave(grave);
        for (LivingEntity target : new LivingEntity[]{zombie, blaze, imp, nightmare, vampire, grave}) {
            spirit.entityInside(spirit.defaultBlockState(), level, pos, target);
            assertEffect(helper, target, MobEffects.WEAKNESS, 300);
            target.removeAllEffects();
        }
        spirit.entityInside(spirit.defaultBlockState(), level, pos, pig);
        assertEffect(helper, pig, MobEffects.REGENERATION, 100);
        pig.removeAllEffects();
        for (LivingEntity target : new LivingEntity[]{blaze, imp, vampire, grave}) {
            tears.entityInside(tears.defaultBlockState(), level, pos, target);
            assertEffect(helper, target, MobEffects.REGENERATION, 100);
        }
        for (LivingEntity target : new LivingEntity[]{pig, nightmare}) {
            tears.entityInside(tears.defaultBlockState(), level, pos, target);
            assertEffect(helper, target, MobEffects.WEAKNESS, 100);
        }
        blaze.removeAllEffects();
        blaze.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 2));
        spirit.entityInside(spirit.defaultBlockState(), level, pos, blaze);
        var existing = blaze.getEffect(MobEffects.WEAKNESS);
        helper.assertTrue(existing.getDuration() == 140 && existing.getAmplifier() == 2,
                "Fluid contact leaves an existing effect intact");
        helper.succeed();
    }

    private static void assertEffect(GameTestHelper helper, LivingEntity target, MobEffect effect, int duration) {
        var applied = target.getEffect(effect);
        helper.assertTrue(applied != null && applied.getAmplifier() == 1 && applied.getDuration() == duration,
                "Original fluid effect and duration on " + target.getType().getDescriptionId());
    }
}
