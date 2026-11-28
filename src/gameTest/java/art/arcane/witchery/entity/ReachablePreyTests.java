package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ReachablePreyTests {
    private static final Field TARGET_GOALS = ObfuscationReflectionHelper.findField(Mob.class, "f_21346_");

    @GameTest(template = "qa_coven_arena")
    public static void wildMindrakesHuntReachablePlayersWithoutSight(GameTestHelper helper) throws IllegalAccessException {
        Arena arena = new Arena(helper);
        arena.floor(false);
        Mindrake mindrake = arena.predator(WitcheryEntities.MINDRAKE.get());
        FakePlayer player = arena.player(6);
        try {
            arena.check("open ground", mindrake, player, 3, true, true);
            arena.wall(Blocks.STONE.defaultBlockState());
            arena.check("wall with a way around", mindrake, player, 3, false, true);
            arena.cage();
            arena.check("sealed enclosure", mindrake, player, 3, false, false);
            arena.move(player, -2);
            mindrake.setTame(true);
            arena.check("tamed Mindrake", mindrake, player, 3, true, false);
        } finally {
            arena.remove(player);
            mindrake.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void hellhoundsHuntVisibleReachablePreyInOrder(GameTestHelper helper) throws IllegalAccessException {
        Arena arena = new Arena(helper);
        arena.floor(false);
        Hellhound hound = arena.predator(WitcheryEntities.HELLHOUND.get());
        FakePlayer player = arena.player(4);
        var follower = arena.prey(WitcheryEntities.FOLLOWER.get(), 4);
        var sheep = arena.prey(EntityType.SHEEP, 8);
        try {
            arena.check("player at four blocks", hound, player, 3, true, true);
            arena.check("Follower at four blocks", hound, follower, 4, true, true);
            arena.move(player, 5);
            arena.move(follower, 5);
            arena.check("player at five blocks", hound, player, 3, true, false);
            arena.check("Follower at five blocks", hound, follower, 4, true, false);
            arena.check("sheep beyond player range", hound, sheep, 2, true, true);

            arena.move(player, 4);
            arena.wall(Blocks.STONE.defaultBlockState());
            arena.check("player hidden by a short wall", hound, player, 3, false, false);
            arena.wall(Blocks.AIR.defaultBlockState());
            arena.floor(true);
            arena.move(follower, 4);
            arena.move(sheep, 6);
            arena.check("player across a gap", hound, player, 3, true, false);
            arena.check("Follower across a gap", hound, follower, 4, true, false);
            arena.check("sheep across a gap", hound, sheep, 2, true, false);

            arena.move(player, -2);
            arena.checkPriority(player);
            arena.floor(false);
            arena.checkPriority(sheep);
        } finally {
            arena.remove(player);
            hound.discard();
            follower.discard();
            sheep.discard();
        }
        helper.succeed();
    }

    private static final class Arena {
        private final GameTestHelper helper;
        private final ServerLevel level;
        private final BlockPos origin;

        private Arena(GameTestHelper helper) {
            this.helper = helper;
            level = helper.getLevel();
            origin = helper.absolutePos(new BlockPos(8, 0, 5)).atY(208);
        }

        private void floor(boolean gap) {
            for (int x = -5; x <= 11; x++) {
                for (int z = -5; z <= 5; z++) {
                    set(x, -1, z, gap && x >= 1 && x <= 3 ? Blocks.AIR.defaultBlockState() : Blocks.STONE.defaultBlockState());
                }
            }
        }

        private void wall(BlockState state) {
            for (int y = 0; y < 4; y++) {
                for (int z = -1; z <= 1; z++) set(2, y, z, state);
            }
        }

        private void cage() {
            for (int y = 0; y < 4; y++) {
                for (int x = 3; x <= 9; x++) {
                    for (int z = -3; z <= 3; z++) {
                        if (x == 3 || x == 9 || z == -3 || z == 3) set(x, y, z, Blocks.STONE.defaultBlockState());
                    }
                }
            }
        }

        private void set(int x, int y, int z, BlockState state) {
            level.setBlockAndUpdate(origin.offset(x, y, z), state);
        }

        private <T extends Mob> T predator(EntityType<T> type) {
            T mob = type.create(level);
            move(mob, 0);
            mob.setOnGround(true);
            return mob;
        }

        private <T extends Mob> T prey(EntityType<T> type, int x) {
            T mob = type.create(level);
            move(mob, x);
            mob.setNoAi(true);
            helper.assertTrue(level.addFreshEntity(mob), "The prey entered the fixture");
            return mob;
        }

        private FakePlayer player(int x) {
            FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "PreyTester"));
            move(player, x);
            level.addNewPlayer(player);
            return player;
        }

        private void move(LivingEntity entity, int x) {
            entity.setPos(Vec3.atBottomCenterOf(origin.east(x)));
        }

        private void remove(FakePlayer player) {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }

        private void check(String name, Mob hunter, LivingEntity candidate, int priority,
                           boolean visible, boolean expected) throws IllegalAccessException {
            hunter.getSensing().tick();
            helper.assertTrue(hunter.hasLineOfSight(candidate) == visible, "Unexpected visibility for " + name);
            var goal = goals(hunter).getAvailableGoals().stream()
                    .filter(entry -> entry.getPriority() == priority).findFirst().orElseThrow();
            helper.assertTrue(goal.canUse() == expected, "Unexpected target eligibility for " + name);
            if (expected) {
                goal.start();
                helper.assertTrue(hunter.getTarget() == candidate, "Unexpected target picked for " + name);
                goal.stop();
            }
        }

        private void checkPriority(LivingEntity expected) throws IllegalAccessException {
            Hellhound hunter = predator(WitcheryEntities.HELLHOUND.get());
            try {
                goals(hunter).tick();
                helper.assertTrue(hunter.getTarget() == expected, "Unexpected highest-priority reachable prey");
            } finally {
                hunter.discard();
            }
        }

        private static GoalSelector goals(Mob mob) throws IllegalAccessException {
            return (GoalSelector) TARGET_GOALS.get(mob);
        }
    }
}
