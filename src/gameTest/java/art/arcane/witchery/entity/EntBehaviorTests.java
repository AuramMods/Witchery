package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class EntBehaviorTests {
    private static final Field TARGET_GOALS = ObfuscationReflectionHelper.findField(Mob.class, "f_21346_");
    private static final Field LEVEL_RANDOM = ObfuscationReflectionHelper.findField(Level.class, "f_46441_");

    @GameTest(template = "qa_coven_arena")
    public static void entSelectsAReachablePlayerBeyondAWall(GameTestHelper helper) throws IllegalAccessException {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(8, 0, 5)).atY(205);
        for (int x = -6; x <= 12; x++) {
            for (int z = -5; z <= 5; z++) {
                level.setBlockAndUpdate(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState());
            }
        }

        FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "EntTarget"));
        player.setPos(Vec3.atBottomCenterOf(origin.east(6)));
        level.addNewPlayer(player);
        try {
            checkTarget(helper, "open", origin, player, true, true);
            for (int y = 0; y < 4; y++) {
                for (int z = -1; z <= 1; z++) {
                    level.setBlockAndUpdate(origin.offset(3, y, z), Blocks.STONE.defaultBlockState());
                }
            }
            checkTarget(helper, "wall with a route around it", origin, player, false, true);

            for (int y = 0; y < 4; y++) {
                for (int x = 4; x <= 9; x++) {
                    level.setBlockAndUpdate(origin.offset(x, y, -2), Blocks.STONE.defaultBlockState());
                    level.setBlockAndUpdate(origin.offset(x, y, 2), Blocks.STONE.defaultBlockState());
                }
                for (int z = -1; z <= 1; z++) {
                    level.setBlockAndUpdate(origin.offset(4, y, z), Blocks.STONE.defaultBlockState());
                    level.setBlockAndUpdate(origin.offset(9, y, z), Blocks.STONE.defaultBlockState());
                }
            }
            checkTarget(helper, "closed enclosure", origin, player, false, false);
        } finally {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    private static void checkTarget(GameTestHelper helper, String layout, BlockPos origin, FakePlayer player, boolean visible,
                                    boolean reachable) throws IllegalAccessException {
        Ent ent = WitcheryEntities.ENT.get().create(helper.getLevel());
        ent.setPos(Vec3.atBottomCenterOf(origin));
        ent.setOnGround(true);
        try {
            GoalSelector selector = (GoalSelector) TARGET_GOALS.get(ent);
            var goal = selector.getAvailableGoals().stream()
                    .filter(candidate -> candidate.getGoal() instanceof NearestAttackableTargetGoal<?>)
                    .findFirst().orElseThrow();
            helper.assertTrue(ent.hasLineOfSight(player) == visible, "Unexpected visibility from " + layout);
            helper.assertTrue(goal.canUse() == reachable, "Unexpected target eligibility from " + layout);
            if (reachable) {
                goal.start();
                ent.setNoAi(true);
                ent.aiStep();
                helper.assertTrue(ent.getTarget() == player && ent.isScreaming(),
                        "The Ent visibly reacts to the selected player");
            }
        } finally {
            ent.discard();
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void entFertilizesOneBlockBelowItsPositionUsingTheWorldRoll(GameTestHelper helper) throws IllegalAccessException {
        ServerLevel level = helper.getLevel();
        BlockPos crop = helper.absolutePos(new BlockPos(13, 0, 4)).atY(205);
        level.setBlockAndUpdate(crop.below(), Blocks.FARMLAND.defaultBlockState());
        level.setBlockAndUpdate(crop, Blocks.WHEAT.defaultBlockState());

        Ent ent = WitcheryEntities.ENT.get().create(level);
        ent.setPos(Vec3.atBottomCenterOf(crop.above()).add(0, 0.54, 0));
        ent.setNoAi(true);
        ent.setNoGravity(true);
        var previous = level.getRandom();
        try {
            WorldRoll miss = new WorldRoll(300, 1);
            LEVEL_RANDOM.set(level, miss);
            ent.aiStep();
            helper.assertTrue(miss.draws == 1 && level.getBlockState(crop).getValue(CropBlock.AGE) == 0,
                    "A missed world roll leaves the crop untouched");

            WorldRoll hit = new WorldRoll(300, 0);
            LEVEL_RANDOM.set(level, hit);
            ent.aiStep();
            helper.assertTrue(hit.draws == 1 && level.getBlockState(crop).getValue(CropBlock.AGE) >= 2,
                    "The Ent fertilizes the crop directly below its fractional position after a successful world roll");
        } finally {
            LEVEL_RANDOM.set(level, previous);
            ent.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void entDropsOneBranchAndTheWorldSelectedSapling(GameTestHelper helper) throws IllegalAccessException {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(15, 0, 4)).atY(205);
        Ent ent = WitcheryEntities.ENT.get().create(level);
        ent.setPos(Vec3.atBottomCenterOf(origin));
        var previous = level.getRandom();
        var drops = new ArrayList<ItemEntity>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item
                    && item.blockPosition().equals(origin)) drops.add(item);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            List<WitchWoodType> variants = List.of(WitchWoodType.ROWAN, WitchWoodType.ALDER, WitchWoodType.HAWTHORN);
            for (int variant = 0; variant < variants.size(); variant++) {
                WorldRoll roll = new WorldRoll(3, variant);
                LEVEL_RANDOM.set(level, roll);
                ent.dropCustomDeathLoot(ent.damageSources().generic(), 3, true);
                helper.assertTrue(roll.draws == 1 && drops.size() == 2,
                        "The original death callback drops two items and makes one world roll");
                var branch = drops.get(0).getItem();
                var sapling = drops.get(1).getItem();
                helper.assertTrue(branch.is(WitcheryIngredient.ENT_BRANCH.item()) && branch.getCount() == 1
                                && sapling.is(WitcheryBlocks.getRequired("witchsapling").get().asItem())
                                && sapling.getCount() == 1 && WitchWoodType.fromStack(sapling) == variants.get(variant),
                        "The world roll chooses the original sapling subtype");
                drops.forEach(Entity::discard);
                drops.clear();
            }
        } finally {
            LEVEL_RANDOM.set(level, previous);
            MinecraftForge.EVENT_BUS.unregister(capture);
            drops.forEach(Entity::discard);
            ent.discard();
        }
        helper.succeed();
    }

    private static final class WorldRoll extends LegacyRandomSource {
        private final int bound;
        private final int value;
        private int draws;

        private WorldRoll(int bound, int value) {
            super(0);
            this.bound = bound;
            this.value = value;
        }

        @Override
        public int nextInt(int bound) {
            if (bound != this.bound) return super.nextInt(bound);
            draws++;
            return value;
        }
    }
}
