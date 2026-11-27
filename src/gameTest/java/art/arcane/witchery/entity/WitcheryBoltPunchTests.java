package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WitcheryBoltPunchTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_bolt_punch", setupTicks = 5)
    public static void punchPushesAResistantTargetInTheOriginalDirection(GameTestHelper helper) {
        var level = helper.getLevel();
        var chunk = new ChunkPos(helper.absolutePos(new BlockPos(5, 0, 5)));
        var lane = new BlockPos(chunk.getMiddleBlockX(), level.getMaxBuildHeight() - 40, chunk.getMinBlockZ() + 2);
        var targets = List.of(EntityType.PIG.create(level), EntityType.PIG.create(level),
                EntityType.PIG.create(level), EntityType.PIG.create(level));
        for (int i = 0; i < targets.size(); i++) {
            var target = targets.get(i);
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(Vec3.atBottomCenterOf(lane.south(3 * i)));
            target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            helper.assertTrue(level.addFreshEntity(target), "The resistant pig spawns in the test lane");
        }

        helper.startSequence().thenWaitUntil(() -> {
            for (Pig target : targets) {
                helper.assertTrue(level.getEntitiesOfClass(Pig.class, target.getBoundingBox()).contains(target),
                        "The pig is tracked for projectile collision");
            }
        }).thenExecute(() -> {
            try {
                checkHit(helper, targets.get(0), WitcheryEntities.BOLT.get().create(level), 0,
                        new Vec3(1.6, 0, 0), Vec3.ZERO);
                checkHit(helper, targets.get(1), WitcheryEntities.BOLT.get().create(level), 2,
                        new Vec3(1.6, 0, 0), new Vec3(1.200000030, 0.1, 0));
                checkHit(helper, targets.get(2), WitcheryEntities.BOLT.get().create(level), 2,
                        new Vec3(1.2, 0, -1.6), new Vec3(0.720000029, 0.1, -0.960000038));
                checkHit(helper, targets.get(3), EntityType.ARROW.create(level), 2,
                        new Vec3(1.6, 0, 0), Vec3.ZERO);
            } finally {
                targets.forEach(Pig::discard);
            }
        }).thenSucceed();
    }

    private static void checkHit(GameTestHelper helper, Pig target, AbstractArrow arrow, int punch, Vec3 velocity, Vec3 expected) {
        arrow.setPos(target.position().add(0, 0.4, 0).subtract(velocity.normalize().scale(1.34)));
        arrow.setDeltaMovement(velocity);
        arrow.setBaseDamage(2);
        arrow.setKnockback(punch);
        try {
            float health = target.getHealth();
            arrow.tick();
            helper.assertTrue(arrow.isRemoved() && target.getHealth() == health - 4, "The moving projectile hits the pig");
            helper.assertTrue(target.getDeltaMovement().distanceTo(expected) < 0.000002,
                    "Expected push " + expected + ", got " + target.getDeltaMovement());
        } finally {
            arrow.discard();
        }
    }
}
