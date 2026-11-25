package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CallOfTheBeastsTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_call_beasts_landing", timeoutTicks = 160)
    public static void firstPulseKeepsOriginalLandingsInSolidBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos glyph = helper.absolutePos(new BlockPos(14, 0, 14)).atY(210);
        Vec3 center = Vec3.atLowerCornerOf(glyph);
        for (BlockPos block : BlockPos.betweenClosed(glyph.offset(-2, 0, -2), glyph.offset(2, 1, 2))) {
            level.setBlockAndUpdate(block, Blocks.STONE.defaultBlockState());
        }

        var inhibited = pig(level, center.add(-8.25, -2, -8.25));
        inhibited.addEffect(new MobEffectInstance(WitcheryEffects.ENDER_INHIBITION.get(), 200, 1));
        var candidates = List.of(pig(level, center.add(-8.25, -2, -9.25)),
                pig(level, center.add(-9.25, -2, -9.25)), pig(level, center.add(-10.25, -2, -9.25)));
        Vec3 inhibitedOrigin = inhibited.position();
        var source = new AABB(glyph).inflate(12, 4, 12);
        helper.startSequence().thenWaitUntil(() -> {
            var tracked = level.getEntitiesOfClass(Pig.class, source);
            helper.assertTrue(tracked.contains(inhibited) && tracked.containsAll(candidates), "All test animals are tracked");
        }).thenExecute(() -> {
            try {
                level.random.setSeed(0);
                CallOfTheBeasts.pulse(level, glyph, 1);
                var landings = candidates.stream().map(Pig::position).filter(pos -> pos.y == center.y).toList();
                helper.assertTrue(landings.size() == 2 && landings.containsAll(List.of(center.add(-2, 0, 1), center.add(2, 0, 0))),
                        "The first pulse puts exactly two pigs at the original integer coordinates inside stone");
                helper.assertTrue(inhibited.position().equals(inhibitedOrigin), "Ender Inhibition protects the other pig");
            } finally {
                inhibited.discard();
                candidates.forEach(Pig::discard);
                for (BlockPos block : BlockPos.betweenClosed(glyph.offset(-2, 0, -2), glyph.offset(2, 1, 2))) {
                    level.removeBlock(block, false);
                }
            }
        }).thenSucceed();
    }

    private static Pig pig(ServerLevel level, Vec3 position) {
        Pig pig = EntityType.PIG.create(level);
        pig.setPos(position);
        pig.setNoAi(true);
        pig.setNoGravity(true);
        level.addFreshEntity(pig);
        return pig;
    }
}
