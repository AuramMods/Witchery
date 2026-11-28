package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.mixin.MobGoalSelectorAccessor;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SpectralPursuitTests {
    private static final String TEMPLATE = "qa_coven_arena";

    @GameTest(template = TEMPLATE, batch = "witchery_spectral_pursuit")
    public static void poltergeistMeleeRequiresAPlayerAndBansheeStaysInShade(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos start = prepareFloor(helper);
        Vec3 origin = Vec3.atBottomCenterOf(start);
        var poltergeist = WitcheryEntities.POLTERGEIST.get().create(level);
        poltergeist.setPos(origin);
        poltergeist.setOnGround(true);
        var pig = EntityType.PIG.create(level);
        pig.setPos(origin.add(2, 0, 0));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SpectralPursuit"));
        player.setPos(origin.add(2, 0, 0));
        var goals = ((MobGoalSelectorAccessor) (Mob) poltergeist).witchery$goals();
        var melee = goals.getAvailableGoals().stream().map(wrapped -> wrapped.getGoal())
                .filter(MeleeAttackGoal.class::isInstance).findFirst().orElseThrow();
        poltergeist.setTarget(pig);
        helper.assertFalse(melee.canUse(), "A provoked Poltergeist cannot start melee pursuit of an animal");
        poltergeist.setTarget(player);
        helper.assertTrue(melee.canUse(), "The same Poltergeist can start a clear melee route to a player");
        melee.start();
        poltergeist.setTarget(pig);
        helper.assertFalse(melee.canContinueToUse(), "Switching to an animal stops the player's melee pursuit");
        poltergeist.discard();
        pig.discard();
        player.discard();

        level.setBlockAndUpdate(start.above(4), Blocks.STONE.defaultBlockState());
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(!level.canSeeSky(start)
                        && level.canSeeSky(start.east()), "The roof casts shade on the route's first cell"))
                .thenExecute(() -> {
                    for (var kind : SpectralUndead.Kind.values()) {
                        var spirit = kind.type().create(level);
                        spirit.setPos(origin);
                        spirit.setOnGround(true);
                        var navigation = (GroundPathNavigation) spirit.getNavigation();
                        boolean follows = navigation.moveTo(origin.x + 5, origin.y, origin.z, 1);
                        helper.assertTrue(follows == (kind != SpectralUndead.Kind.BANSHEE),
                                kind + " follows its original rule for a route from shade into sunlight");
                        helper.assertTrue(navigation.getNodeEvaluator().canOpenDoors() == (kind != SpectralUndead.Kind.SPECTRE),
                                kind + " has the original closed wooden door routing rule");
                        spirit.discard();
                    }
                }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_spectral_retaliation")
    public static void aProvokedBansheeAlertsOnlyBanshees(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = Vec3.atBottomCenterOf(prepareFloor(helper));
        var provoked = WitcheryEntities.BANSHEE.get().create(level);
        var otherBanshee = WitcheryEntities.BANSHEE.get().create(level);
        var poltergeist = WitcheryEntities.POLTERGEIST.get().create(level);
        var spectre = WitcheryEntities.SPECTRE.get().create(level);
        var pig = EntityType.PIG.create(level);
        var actors = List.of(provoked, otherBanshee, poltergeist, spectre, pig);
        for (int index = 0; index < actors.size(); index++) {
            var actor = actors.get(index);
            actor.setPos(origin.add(index, 0, 0));
            actor.setNoAi(actor != provoked);
            helper.assertTrue(level.addFreshEntity(actor), "Each retaliation subject can join the arena");
        }
        provoked.tickCount = 20;
        provoked.setLastHurtByMob(pig);
        var retaliation = provoked.targetSelector.getAvailableGoals().stream()
                .map(wrapped -> wrapped.getGoal())
                .filter(HurtByTargetGoal.class::isInstance).findFirst().orElseThrow();
        helper.assertTrue(retaliation.canUse(), "The Banshee can retaliate against the creature that hit it");
        retaliation.start();
        helper.assertTrue(provoked.getTarget() == pig && otherBanshee.getTarget() == pig
                        && poltergeist.getTarget() == null && spectre.getTarget() == null,
                "Only other Banshees respond to the revenge alert");
        actors.forEach(Mob::discard);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_banshee_contact")
    public static void bansheePulseReachesItsTargetAndOrdinaryMeleeCanKnockBack(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = Vec3.atBottomCenterOf(prepareFloor(helper));
        var banshee = WitcheryEntities.BANSHEE.get().create(level);
        banshee.setPos(origin);
        banshee.setNoAi(true);
        var target = EntityType.PIG.create(level);
        target.setPos(origin.add(2, 0, 0));
        var lastAttacker = EntityType.PIG.create(level);
        lastAttacker.setPos(origin.add(3, 0, 0));
        level.addFreshEntity(target);
        level.addFreshEntity(lastAttacker);
        banshee.setTarget(target);
        banshee.setLastHurtByMob(lastAttacker);
        banshee.tickCount = 100;
        banshee.aiStep();
        helper.assertTrue(banshee.screaming() && target.getHealth() == 9 && lastAttacker.getHealth() == 10,
                "The curse pulse reaches the selected creature and spares another mob that attacked earlier");

        lastAttacker.setDeltaMovement(Vec3.ZERO);
        helper.assertTrue(banshee.doHurtTarget(lastAttacker) && lastAttacker.getHealth() == 10
                        && lastAttacker.getDeltaMovement().horizontalDistanceSqr() > 0.1,
                "The ordinary unarmed melee blow adds knockback and deals zero health damage");
        target.discard();
        lastAttacker.discard();
        banshee.discard();
        helper.succeed();
    }

    private static BlockPos prepareFloor(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos start = helper.absolutePos(new BlockPos(4, 2, 4)).atY(190);
        for (BlockPos floor : BlockPos.betweenClosed(start.offset(-2, -1, -2), start.offset(7, -1, 2))) {
            level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        }
        return start;
    }
}
