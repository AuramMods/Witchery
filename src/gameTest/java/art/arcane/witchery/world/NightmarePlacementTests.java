package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Nightmare;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class NightmarePlacementTests {
    @GameTest(template = "qa_coven_arena")
    public static void wakingNightmareUsesTheOriginalColumnAndClearance(GameTestHelper helper) {
        var level = helper.getLevel();
        var victim = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "NightmarePlacement"));
        BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4)).atY(-10);
        Vec3 feet = Vec3.atBottomCenterOf(origin);
        victim.moveTo(feet.x, feet.y, feet.z, 0, 0);

        // Seed 88 selects the apparition, then draws 4 and 5 from the nine slots.
        BlockPos column = origin.offset(-2, 0, 3);
        for (int y = -30; y <= 1; ++y) {
            level.setBlockAndUpdate(column.atY(y), y == -30 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        var searches = new AABB(column.atY(-30)).expandTowards(0, 31, 0);
        var rule = level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING);
        boolean wasSpawning = rule.get();
        rule.set(true, level.getServer());
        try {
            victim.getRandom().setSeed(88);
            DreamTravel.spawnWakingNightmare(victim, 5);
            var lower = level.getEntitiesOfClass(Nightmare.class, searches, nightmare -> nightmare.targets(victim));
            helper.assertTrue(lower.size() == 1 && lower.get(0).position().distanceToSqr(feet.add(-2, -18.95, 3)) < 0.000001,
                    "The chosen column reaches a floor more than eight blocks down, including below Y=0");
            lower.get(0).discard();

            for (int y = -10; y <= -2; ++y) {
                level.setBlockAndUpdate(column.atY(y), Blocks.STONE.defaultBlockState());
            }
            victim.getRandom().setSeed(88);
            DreamTravel.spawnWakingNightmare(victim, 5);
            var upper = level.getEntitiesOfClass(Nightmare.class, searches, nightmare -> nightmare.targets(victim));
            helper.assertTrue(upper.size() == 1 && upper.get(0).position().distanceToSqr(feet.add(-2, 9.05, 3)) < 0.000001,
                    "A solid start scans eight blocks up and places the Nightmare above the resulting floor");
            upper.get(0).discard();

            level.setBlockAndUpdate(column.atY(0), Blocks.STONE.defaultBlockState());
            victim.getRandom().setSeed(88);
            DreamTravel.spawnWakingNightmare(victim, 5);
            helper.assertTrue(level.getEntitiesOfClass(Nightmare.class, searches, nightmare -> nightmare.targets(victim)).isEmpty(),
                    "The original column rejects one block of headroom");
        } finally {
            rule.set(wasSpawning, level.getServer());
            for (Nightmare nightmare : level.getEntitiesOfClass(Nightmare.class, searches, mob -> mob.targets(victim))) {
                nightmare.discard();
            }
            victim.discard();
        }
        helper.succeed();
    }
}
