package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class MirrorFaceTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_mirror_face")
    public static void checksForTheApparitionInTheUpperPanel(GameTestHelper helper) {
        var level = helper.getLevel();
        var upper = helper.absolutePos(new BlockPos(5, 5, 4));
        var lower = upper.below();
        var mirror = (MirrorBlock) WitcheryBlocks.getRequired("mirrorblock").get();
        helper.assertTrue(mirror.placePair(level, upper, Direction.NORTH), "Place the inhabited frame");
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "MirrorVisitor"));

        var stray = WitcheryEntities.MIRROR_FACE.get().create(level);
        stray.setPos(upper.getX() + 0.5, upper.getY() + 0.1, upper.getZ() + 0.2);
        level.addFreshEntity(stray);
        var upperHit = new BlockHitResult(Vec3.atCenterOf(upper), Direction.NORTH, upper, false);
        level.getBlockState(upper).use(level, player, InteractionHand.MAIN_HAND, upperHit);

        var panel = level.getBlockState(upper).getCollisionShape(level, upper).bounds().move(upper);
        var faces = level.getEntitiesOfClass(MirrorFace.class, panel);
        helper.assertTrue(faces.size() == 1 && faces.get(0) != stray,
                "A stray face inside the block but outside its panel does not prevent a conversation");
        var face = faces.get(0);
        helper.assertTrue(face.position().distanceTo(Vec3.atLowerCornerOf(upper).add(0.5, 0.1, 0.5 + 0.4F)) < 1.0E-8,
                "The original position uses the upper panel's center and offsets");
        helper.assertTrue(face.getYRot() >= 0 && face.getYRot() < Mth.TWO_PI,
                "The face gets the inherited original yaw range");

        var lowerHit = new BlockHitResult(Vec3.atCenterOf(lower), Direction.NORTH, lower, false);
        level.getBlockState(lower).use(level, player, InteractionHand.MAIN_HAND, lowerHit);
        helper.assertTrue(level.getEntitiesOfClass(MirrorFace.class, new AABB(upper)).size() == 2,
                "Using the other half keeps the same apparition while it overlaps the upper panel");
        face.discard();
        stray.discard();
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_mirror_face")
    public static void preservesImportedAgeAndOriginalMotion(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 5, 4));
        var face = WitcheryEntities.MIRROR_FACE.get().create(level);
        var imported = new CompoundTag();
        imported.putLong("WITCTicksAlive", 198);
        face.readAdditionalSaveData(imported);
        Vec3 start = Vec3.atBottomCenterOf(pos).add(0, 0.1, 0);
        face.setPos(start);
        face.setDeltaMovement(0.1, 0.15, -0.2);
        face.tick();
        helper.assertTrue(!face.isRemoved() && face.position().distanceTo(start.add(0.1, 0.15, -0.2)) < 1.0E-6
                        && face.getDeltaMovement().distanceTo(new Vec3(0.091000003, 0, -0.182000005)) < 1.0E-6,
                "The first loaded tick uses its saved motion and clears only the vertical component");
        var afterTick = face.saveWithoutId(new CompoundTag());
        var reloaded = WitcheryEntities.MIRROR_FACE.get().create(level);
        reloaded.load(afterTick);
        reloaded.tick();
        helper.assertTrue(!reloaded.isRemoved() && reloaded.saveWithoutId(new CompoundTag()).getLong("WITCTicksAlive") == 200,
                "Reloading preserves the last active tick");
        reloaded.tick();
        helper.assertTrue(reloaded.isRemoved(), "The face expires when its total age exceeds 200");

        for (BlockPos water : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        }
        var submerged = WitcheryEntities.MIRROR_FACE.get().create(level);
        submerged.setPos(start);
        submerged.setAirSupply(200);
        submerged.tick();
        helper.assertTrue(submerged.getAirSupply() == 200 && submerged.isPickable() && submerged.isIgnoringBlockTriggers(),
                "The original face retains its air and can be selected without triggering pressure plates");
        helper.succeed();
    }
}
