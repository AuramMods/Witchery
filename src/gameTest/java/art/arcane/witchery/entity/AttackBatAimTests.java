package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class AttackBatAimTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_attack_bat_aim")
    public static void batUsesTheCastersEntityAimAndItsOwnClearance(GameTestHelper helper) {
        var world = helper.getLevel();
        var sample = helper.absolutePos(new BlockPos(5, 0, 5));
        var chunk = new ChunkPos(sample);
        var center = new BlockPos(chunk.getMiddleBlockX(), world.getMaxBuildHeight() - 8, chunk.getMiddleBlockZ());
        var overhang = center.offset(3, 1, 3);
        var caster = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "AttackBatAim"));
        caster.setPos(Vec3.atBottomCenterOf(center.north(5)));
        caster.setYRot(0);
        caster.setXRot(5);
        world.addNewPlayer(caster);

        var target = EntityType.COW.create(world);
        var scout = EntityType.BAT.create(world);
        var bat = WitcheryEntities.ATTACK_BAT.get().create(world);
        target.setPos(Vec3.atBottomCenterOf(center.south(5)));
        target.setNoAi(true);
        world.addFreshEntity(target);
        bat.setPos(Vec3.atBottomCenterOf(center.offset(4, 0, 3)));
        bat.joinSwarm(caster);
        world.addFreshEntity(bat);

        try {
            for (int height = 0; height < 3; height++) world.setBlockAndUpdate(center.above(height), Blocks.STONE.defaultBlockState());
            Vec3 eyes = caster.getEyePosition();
            var hit = world.clip(new ClipContext(eyes, eyes.add(caster.getLookAngle().scale(32)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
            helper.assertTrue(hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(center.above()),
                    "The caster looks through the fixture wall at the cow");
            helper.assertTrue(bat.hasLineOfSight(target), "The fixture wall leaves the bat's approach to the cow clear");
            var initial = new Vec3(0.20, 0.02, 0.05);
            var original = new Vec3(0.11055728, 0.02000000, 0.09472136);
            bat.setDeltaMovement(initial);
            bat.customServerAiStep();
            helper.assertTrue(bat.getDeltaMovement().distanceToSqr(original) < 1e-12,
                    "Follow the cow through the caster's wall with the original change in velocity");

            target.setPos(target.position().add(0, 0.2, 0));
            bat.setPos(bat.position().add(0, 0.2, 0));
            world.setBlockAndUpdate(overhang, Blocks.STONE.defaultBlockState());
            helper.assertTrue(bat.hasLineOfSight(target), "The eye trace passes under the overhang");
            bat.setDeltaMovement(initial);
            bat.customServerAiStep();
            helper.assertTrue(bat.getDeltaMovement().distanceToSqr(original) > 1e-6,
                    "A full bat cannot follow the aimed course through the low overhang");
            world.removeBlock(overhang, false);

            scout.setPos(Vec3.atBottomCenterOf(center.north(2)).add(0, 1, 0));
            world.addFreshEntity(scout);
            bat.setDeltaMovement(initial);
            bat.customServerAiStep();
            helper.assertTrue(bat.getDeltaMovement().distanceToSqr(original) > 1e-6,
                    "A nearer bat on the caster's aim ray prevents pursuit of the cow behind it");
        } finally {
            world.removeBlock(overhang, false);
            for (int height = 0; height < 3; height++) world.removeBlock(center.above(height), false);
            scout.discard();
            target.discard();
            bat.discard();
            caster.discard();
        }
        helper.succeed();
    }
}
