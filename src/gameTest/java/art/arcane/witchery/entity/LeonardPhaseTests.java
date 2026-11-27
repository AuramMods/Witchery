package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LeonardPhaseTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_leonard_entrance")
    public static void leonardTakesCappedDamageDuringTheEntrance(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LeonardTest"));
        var leonard = WitcheryEntities.LEONARD.get().create(level);
        leonard.beginSummoning();
        helper.assertTrue(leonard.hurt(level.damageSources().playerAttack(player), 20) && leonard.getHealth() == 138,
                "Leonard takes twelve damage from the first direct player hit during his entrance");
        var iceTarget = WitcheryEntities.LEONARD.get().create(level);
        iceTarget.beginSummoning();
        iceTarget.harmedByIce(player, 30);
        helper.assertTrue(iceTarget.getHealth() == 135 && iceTarget.hasActiveEntrance(),
                "The cold weakness still takes fifteen health during the entrance");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_leonard_shield")
    public static void dyingDiagonalSoulShieldsOnlyAnEngagedLeonard(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos at = helper.absolutePos(new BlockPos(2, 7, 2));
        Vec3 origin = Vec3.atBottomCenterOf(at);
        var leonard = WitcheryEntities.LEONARD.get().create(level);
        leonard.setPos(origin);
        leonard.setNoGravity(true);
        leonard.goalSelector.removeAllGoals(goal -> true);
        leonard.targetSelector.removeAllGoals(goal -> true);
        var tag = new CompoundTag();
        tag.putInt("SoulDelay", 7);
        leonard.readAdditionalSaveData(tag);
        leonard.setHealth(260);
        var soul = WitcheryEntities.LOST_SOUL.get().create(level);
        var diagonal = at.offset(14, 0, 14);
        level.getChunk(diagonal);
        soul.setPos(Vec3.atBottomCenterOf(diagonal));
        soul.setNoAi(true);
        level.addFreshEntity(soul);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.getEntitiesOfClass(LostSoul.class, new AABB(diagonal)).contains(soul), "The diagonal Soul is tracked"))
                .thenExecute(() -> {
                    leonard.tickCount = 2;
                    leonard.aiStep();
                    helper.assertFalse(shielded(leonard), "An idle Leonard does not gain a shield by proximity alone");
                    soul.setHealth(0);
                    var target = EntityType.PIG.create(level);
                    target.setPos(origin.add(1, 0, 0));
                    leonard.setTarget(target);
                    leonard.tickCount = 22;
                    leonard.aiStep();
                    helper.assertTrue(shielded(leonard), "A dying diagonal Soul still shields an engaged Leonard");
                    soul.discard();
                    leonard.tickCount = 42;
                    leonard.aiStep();
                    helper.assertFalse(shielded(leonard), "The next phase check sees that the Soul has left the world");
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_leonard_dispel")
    public static void leonardDispelsTheBrewLiquidCellInsideTheOriginalSphere(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos at = helper.absolutePos(new BlockPos(5, 6, 5));
        var leonard = WitcheryEntities.LEONARD.get().create(level);
        leonard.setPos(Vec3.atBottomCenterOf(at));
        leonard.setNoGravity(true);
        leonard.goalSelector.removeAllGoals(goal -> true);
        leonard.targetSelector.removeAllGoals(goal -> true);
        var liquid = WitcheryBlocks.getRequired("brewliquid").get();
        var gas = WitcheryBlocks.getRequired("brewgas").get();
        BlockPos inside = at.offset(3, 4, 1);
        BlockPos boundary = at.offset(4, 2, 0);
        level.setBlockAndUpdate(inside, liquid.defaultBlockState());
        level.setBlockAndUpdate(boundary, gas.defaultBlockState());
        var griefing = level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING);
        boolean previous = griefing.get();
        try {
            griefing.set(true, level.getServer());
            level.random.setSeed(0);
            leonard.tickCount = 2;
            leonard.aiStep();
            helper.assertTrue(level.isEmptyBlock(inside) && level.getFluidState(inside).isEmpty(),
                    "Dispel removes both the liquid block and its fluid");
            helper.assertTrue(level.getBlockState(boundary).is(gas), "Gas exactly four blocks out survives the pulse");
        } finally {
            griefing.set(previous, level.getServer());
        }
        helper.succeed();
    }

    private static boolean shielded(Leonard leonard) {
        return leonard.saveWithoutId(new CompoundTag()).getBoolean("Shielded");
    }
}
