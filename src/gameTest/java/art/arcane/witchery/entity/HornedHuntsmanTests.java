package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class HornedHuntsmanTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_huntsman")
    public static void extraBowShotScalesWithActualDistance(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 8, 3));
        var origin = Vec3.atBottomCenterOf(pos);
        var huntsman = WitcheryEntities.HORNED_HUNTSMAN.get().create(level);
        var target = EntityType.PIG.create(level);
        huntsman.goalSelector.removeAllGoals(goal -> true);
        huntsman.targetSelector.removeAllGoals(goal -> true);
        huntsman.setNoGravity(true);
        huntsman.setPos(origin);
        var arrows = new ArrayList<Arrow>();
        Consumer<EntityJoinLevelEvent> observe = event -> {
            if (event.getEntity() instanceof Arrow arrow && arrow.getOwner() == huntsman) arrows.add(arrow);
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            for (int distance : new int[] {4, 18}) {
                target.setPos(origin.add(distance, 0, 0));
                huntsman.getSensing().tick();
                huntsman.setTarget(target);
                huntsman.tickCount = 20;
                huntsman.getRandom().setSeed(23);
                level.random.setSeed(0);
                huntsman.aiStep();
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(observe);
        }
        helper.assertTrue(arrows.size() == 2, "A visible target triggers the seeded extra shot at each distance");
        helper.assertTrue(Math.abs(arrows.get(1).getBaseDamage() - arrows.get(0).getBaseDamage() - (18 - 4) * 8 / 30.0) < 0.001,
                "The extra shot gains damage in proportion to distance");
        for (Arrow arrow : arrows) {
            helper.assertTrue(arrow.position().distanceTo(origin.add(1, huntsman.getEyeHeight() - 0.1, 0)) < 0.001,
                    "The arrow starts one block toward the target and just below eye height");
            arrow.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_huntsman")
    public static void blockedHuntsmanWaitsBeforeEachTeleport(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 8, 8));
        var origin = Vec3.atBottomCenterOf(pos);
        var huntsman = WitcheryEntities.HORNED_HUNTSMAN.get().create(level);
        var target = EntityType.PIG.create(level);
        huntsman.goalSelector.removeAllGoals(goal -> true);
        huntsman.targetSelector.removeAllGoals(goal -> true);
        huntsman.setNoGravity(true);
        target.setPos(origin.add(18, 10, 0));
        huntsman.setTarget(target);
        for (int x = 0; x < 13; x++) {
            for (int z = -5; z <= 5; z++) {
                level.setBlockAndUpdate(pos.offset(x, -1, z), Blocks.STONE.defaultBlockState());
            }
        }
        for (int age : new int[] {199, 200, 201, 401, 402}) {
            huntsman.setPos(origin);
            huntsman.getNavigation().stop();
            huntsman.tickCount = age;
            huntsman.getRandom().setSeed(23);
            huntsman.aiStep();
            boolean shouldMove = age == 201 || age == 402;
            helper.assertTrue((huntsman.position().distanceTo(origin) > 1) == shouldMove,
                    "Teleport timing at age " + age + " follows the original 201-tick spacing");
        }
        helper.succeed();
    }
}
