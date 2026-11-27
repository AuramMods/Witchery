package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class NaturalSpawnEligibilityTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_natural_spawn_light", timeoutTicks = 200)
    public static void nativeLightRulesAndCovenWeightUseTheForgeSpawnPath(GameTestHelper helper) {
        var level = helper.getLevel();
        var darkGrass = helper.absolutePos(new BlockPos(3, 228, 5));
        var dimStone = darkGrass.east(4);
        var litStone = dimStone.east(4);
        room(level, darkGrass, Blocks.GRASS_BLOCK, 0);
        room(level, dimStone, Blocks.STONE, 11);
        room(level, litStone, Blocks.STONE, 15);

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getMaxLocalRawBrightness(darkGrass) == 0
                        && level.getMaxLocalRawBrightness(dimStone) == 11 && level.getMaxLocalRawBrightness(litStone) == 15,
                "The three spawn sites have their intended light levels")).thenExecute(() -> {
            var witch = WitcheryEntities.COVEN_WITCH.get();
            var spawnType = MobSpawnType.NATURAL;
            var rolls = new CountingRandom();
            var config = WitcheryServerConfig.COVEN_WITCH_SPAWN_WEIGHT;
            int previous = config.get();
            try {
                config.set(1);
                helper.assertTrue(SpawnPlacements.checkSpawnRules(witch, level, spawnType, darkGrass, rolls),
                        "Clear dark grass accepts the Coven Witch");
                helper.assertTrue(SpawnPlacements.checkSpawnRules(witch, level, spawnType, litStone, rolls),
                        "Bright solid ground also accepts the Coven Witch");
                helper.assertTrue(rolls.calls == 0, "Weight one skips the random draw");
                config.set(2);
                helper.assertTrue(!SpawnPlacements.checkSpawnRules(witch, level, spawnType, dimStone, rolls) && rolls.calls == 0,
                        "Light eleven on stone is rejected before the roll");
                helper.assertTrue(SpawnPlacements.checkSpawnRules(witch, level, spawnType, darkGrass, rolls) && rolls.calls == 1,
                        "Another eligible site gets a single weight roll");
                level.setBlockAndUpdate(darkGrass, Blocks.WATER.defaultBlockState());
                helper.assertTrue(!SpawnPlacements.checkSpawnRules(witch, level, spawnType, darkGrass, rolls) && rolls.calls == 1,
                        "Liquid blocks the spawn before the weight roll");
            } finally {
                config.set(previous);
            }

            var hound = WitcheryEntities.HELLHOUND.get().create(level);
            hound.setPos(litStone.getX() + 0.5, litStone.getY(), litStone.getZ() + 0.5);
            helper.assertTrue(hound.getWalkTargetValue(litStone, level) < 0,
                    "The control site is too bright for the inherited monster path rule");
            helper.assertTrue(ForgeEventFactory.checkSpawnPosition(hound, level, spawnType),
                    "Hellhound spawning accepts the same bright site");
        }).thenSucceed();
    }

    private static void room(ServerLevel level, BlockPos pos, Block floor, int light) {
        for (var block : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 2, 1))) {
            boolean interior = block.getX() == pos.getX() && block.getZ() == pos.getZ()
                    && (block.getY() == pos.getY() || block.getY() == pos.getY() + 1);
            level.setBlockAndUpdate(block, interior ? Blocks.AIR.defaultBlockState() : Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(pos.below(), floor.defaultBlockState());
        if (light > 0) level.setBlockAndUpdate(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, light));
    }

    private static final class CountingRandom extends LegacyRandomSource {
        private int calls;

        private CountingRandom() {
            super(0);
        }

        @Override
        public int nextInt(int bound) {
            calls++;
            return 0;
        }
    }
}
