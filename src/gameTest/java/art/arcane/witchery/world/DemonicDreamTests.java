package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DemonicDreamTests {
    @GameTest(template = "qa_coven_arena")
    public static void dreamChoiceConsumesOnlyOriginalRolls(GameTestHelper helper) {
        var noRolls = new DreamRolls();
        helper.assertTrue(!new DreamTravel.DreamAmbience(0.0, 0.0).rollNightmare(noRolls),
                "A certain Dream uses no random roll");
        var certain = new DreamTravel.DreamAmbience(1.0, 0.0);
        helper.assertTrue(certain.rollNightmare(noRolls) && !certain.rollDemonic(noRolls),
                "A certain ordinary Nightmare uses no random roll");

        var ordinary = new DreamTravel.DreamAmbience(0.998, 0.0);
        var oneRoll = new DreamRolls(0.997);
        helper.assertTrue(ordinary.rollNightmare(oneRoll) && !ordinary.rollDemonic(oneRoll) && oneRoll.index == 1,
                "An ordinary Nightmare only rolls for the Dream outcome");
        var eligible = new DreamTravel.DreamAmbience(0.748, 0.35);
        var twoRolls = new DreamRolls(0.5, 0.34);
        helper.assertTrue(eligible.rollNightmare(twoRolls) && eligible.rollDemonic(twoRolls) && twoRolls.index == 2,
                "An eligible Nightmare uses the second world roll for the demonic outcome");
        helper.assertTrue(!ordinary.rollNightmare(new DreamRolls(0.998)) && !eligible.rollDemonic(new DreamRolls(0.35)),
                "A roll equal to its chance does not select the outcome");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", timeoutTicks = 220)
    public static void demonicDreamUsesOriginalHazardRollsAndMonsterCap(GameTestHelper helper) {
        var dream = helper.getLevel().getServer().getLevel(WitcheryDimensions.DREAM);
        helper.assertTrue(dream != null, "Dream level available");
        BlockPos anchor = helper.absolutePos(new BlockPos(4, 2, 4)).atY(210);
        BlockPos gathering = anchor.offset(10, 0, 12);
        BlockPos landing = anchor.offset(-4, 0, -6);
        var chunks = List.of(new ChunkPos(anchor), new ChunkPos(gathering), new ChunkPos(landing)).stream().distinct().toList();
        var forced = chunks.stream().filter(chunk -> dream.setChunkForced(chunk.x, chunk.z, true)).toList();
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(dream.isPositionEntityTicking(gathering) && dream.getGameTime() % 100 == 0,
                        "Wait for the crowd to be trackable during a demonic Dream pulse"))
                .thenExecute(() -> {
                    try {
                        verifyHazards(helper, dream, anchor, gathering, landing);
                    } finally {
                        forced.forEach(chunk -> dream.setChunkForced(chunk.x, chunk.z, false));
                    }
                })
                .thenSucceed();
    }

    private static void verifyHazards(GameTestHelper helper, ServerLevel dream, BlockPos anchor, BlockPos gathering, BlockPos landing) {
        var dreamer = new FakePlayer(dream, new GameProfile(UUID.randomUUID(), "DemonicDreamQA"));
        Vec3 feet = Vec3.atLowerCornerOf(anchor).add(0.25, 0, 0.75);
        Vec3 crowd = Vec3.atBottomCenterOf(gathering);
        dreamer.setPos(feet);
        var persistent = new CompoundTag();
        var session = new CompoundTag();
        session.putBoolean("Nightmare", true);
        session.putBoolean("Demonic", true);
        persistent.put("WITCSpiritWorld", session);
        dreamer.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
        DreamTravel.markNightmareKilled(dreamer);
        dream.setBlockAndUpdate(landing.below(), Blocks.STONE.defaultBlockState());
        for (BlockPos cell : BlockPos.betweenClosed(landing, landing.above(2))) {
            dream.setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
        }

        var fixtures = new ArrayList<Entity>();
        var spawned = new ArrayList<Entity>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getLevel() == dream && (event.getEntity() instanceof Demon || event.getEntity() instanceof AbstractHurtingProjectile)) {
                spawned.add(event.getEntity());
            }
        };
        var spawning = dream.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING);
        boolean previous = spawning.get();
        spawning.set(true, dream.getServer());
        try {
            for (int index = 0; index < 2; ++index) {
                fixtures.add(addNeighbor(helper, dream, WitcheryEntities.DEMON.get().create(dream), crowd));
            }
            for (int index = 0; index < 10; ++index) {
                fixtures.add(addNeighbor(helper, dream, EntityType.GHAST.create(dream), crowd));
            }
            MinecraftForge.EVENT_BUS.addListener(capture);
            dreamer.getRandom().setSeed(4096);
            DreamTravel.spawnNightmare(dreamer);
            helper.assertTrue(spawned.size() == 1 && spawned.get(0) instanceof SmallFireball ball
                            && atLaunch(ball, feet.add(2, 15, -1)),
                    "The first roll launches the original downward small fireball fifteen blocks above the dreamer");
            dreamer.getRandom().setSeed(1024);
            DreamTravel.spawnNightmare(dreamer);
            helper.assertTrue(spawned.size() == 2 && spawned.get(1) instanceof LargeFireball ball
                            && atLaunch(ball, feet.add(0, 15, 1)),
                    "The second roll launches the original downward large fireball");
            dreamer.getRandom().setSeed(0);
            DreamTravel.spawnNightmare(dreamer);
            helper.assertTrue(spawned.size() == 3 && spawned.get(2) instanceof Demon demon
                            && demon.position().distanceToSqr(Vec3.atBottomCenterOf(landing).add(0, 0.05, 0)) < 0.000001,
                    "Existing demons and ghasts do not count against the original ground monster cap");
            for (int index = 0; index < 10; ++index) {
                fixtures.add(addNeighbor(helper, dream, EntityType.ZOMBIE.create(dream), crowd));
            }
            var zombies = dream.getEntitiesOfClass(Zombie.class, new AABB(feet, feet).inflate(18));
            helper.assertTrue(zombies.size() >= 10, "The Dream's active entity lookup sees the ten neighboring zombies");
            spawned.get(2).discard();
            dreamer.getRandom().setSeed(0);
            DreamTravel.spawnNightmare(dreamer);
            helper.assertTrue(spawned.size() == 3, "Ten zombies suppress the demon summon");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
            spawning.set(previous, dream.getServer());
            fixtures.forEach(Entity::discard);
            spawned.forEach(Entity::discard);
            dreamer.discard();
        }
    }

    private static Mob addNeighbor(GameTestHelper helper, ServerLevel level, Mob mob, Vec3 position) {
        mob.setPos(position);
        mob.setNoAi(true);
        helper.assertTrue(level.addFreshEntity(mob), "Neighbor joined the fixture");
        return mob;
    }

    private static boolean atLaunch(AbstractHurtingProjectile ball, Vec3 position) {
        return ball.position().equals(position) && Math.abs(ball.yPower + 0.1) < 0.0001
                && ball.xPower == 0 && ball.zPower == 0;
    }

    private static final class DreamRolls extends LegacyRandomSource {
        private final double[] rolls;
        private int index;

        private DreamRolls(double... rolls) {
            super(0);
            this.rolls = rolls;
        }

        @Override
        public double nextDouble() {
            return rolls[index++];
        }
    }
}
