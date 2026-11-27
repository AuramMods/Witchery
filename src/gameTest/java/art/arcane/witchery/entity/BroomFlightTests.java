package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BroomFlightTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_broom_motion")
    public static void livingRiderSteersAndAnEmptyBroomKeepsGroundedLift(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = clearAir(helper);
        var broom = WitcheryEntities.BROOM.get().create(level);
        var rider = EntityType.COW.create(level);
        broom.setPos(origin);
        rider.setYRot(91.25F);
        rider.setXRot(-35.5F);
        rider.zza = 1;
        helper.assertTrue(rider.startRiding(broom, true), "The cow can ride the broom");
        broom.setDeltaMovement(0.01, 0.04, -0.02);
        broom.tick();
        helper.assertTrue(broom.getDeltaMovement().distanceTo(new Vec3(0.002971649205, 0.080485431261, -0.019951178444)) < 1e-8,
                "The original broom responds to a living rider's forward input, yaw, and pitch");

        rider.stopRiding();
        broom.setPos(origin);
        broom.setOnGround(true);
        broom.setDeltaMovement(0.015, 0.13, -0.1);
        broom.tick();
        helper.assertTrue(Math.abs(broom.getY() - (origin.y + 0.13)) < 1e-8,
                "A grounded empty broom retains its vertical velocity");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_broom_collision")
    public static void occupiedBroomsPushEachOtherAndProtectTheirRiders(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = clearAir(helper);
        var broom = WitcheryEntities.BROOM.get().create(level);
        var other = WitcheryEntities.BROOM.get().create(level);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BroomLanding"));
        var cow = EntityType.COW.create(level);
        broom.setPos(origin);
        other.setPos(origin.add(1.3, 0, 0));
        helper.assertTrue(player.startRiding(broom, true) && cow.startRiding(other, true), "Both brooms have riders");
        helper.assertTrue(level.addFreshEntity(other), "The nearby broom enters the level's entity index");
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(other.getUUID()) == other,
                "The nearby broom is tracked before the collision")).thenExecute(() -> {
            try {
                other.setPos(origin.add(1.3, 0, 0));
                other.setDeltaMovement(Vec3.ZERO);
                broom.tick();
                helper.assertTrue(broom.getDeltaMovement().x < -0.04 && other.getDeltaMovement().x > 0.04,
                        "Both occupied brooms receive the original horizontal push");
                helper.assertTrue(ForgeHooks.onLivingFall(player, 16, 1)[0] == 0,
                        "A player riding the broom lands with zero fall distance");
                player.stopRiding();
                helper.assertTrue(ForgeHooks.onLivingFall(player, 16, 1)[0] == 16,
                        "The same player's fall distance is ordinary after dismounting");
            } finally {
                other.discard();
            }
        }).thenSucceed();
    }

    private static Vec3 clearAir(GameTestHelper helper) {
        var origin = helper.absolutePos(new BlockPos(6, 190, 6));
        for (var pos : BlockPos.betweenClosed(origin.offset(-2, -2, -2), origin.offset(3, 2, 2))) {
            helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        return Vec3.atBottomCenterOf(origin);
    }
}
