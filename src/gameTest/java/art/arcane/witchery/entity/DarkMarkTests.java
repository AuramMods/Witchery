package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DarkMarkTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_dark_mark")
    public static void keepsLegacyPhysicsAndAge(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 6, 4));
        var mark = WitcheryEntities.DARK_MARK.get().create(level);
        var legacy = new CompoundTag();
        legacy.putLong("WITCTicksAlive", 5998);
        mark.readAdditionalSaveData(legacy);
        Vec3 origin = Vec3.atBottomCenterOf(pos);
        mark.setPos(origin);
        mark.setDeltaMovement(0.1, 0.15, -0.2);
        mark.tick();
        helper.assertTrue(mark.position().distanceTo(origin.add(0.1, 0.15, -0.2)) < 1.0E-6
                        && mark.getDeltaMovement().distanceTo(new Vec3(0.091000003, 0, -0.182000005)) < 1.0E-6,
                "An imported mark moves on its first tick and retains only horizontal drift");
        var saved = mark.saveWithoutId(new CompoundTag());
        var restored = WitcheryEntities.DARK_MARK.get().create(level);
        restored.load(saved);
        restored.tick();
        helper.assertTrue(!restored.isRemoved() && restored.saveWithoutId(new CompoundTag()).getLong("WITCTicksAlive") == 6000,
                "An ordinary reload retains the last active tick");
        restored.tick();
        helper.assertTrue(restored.isRemoved(), "Only the tick beyond age 6000 expires the mark");

        for (var water : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 3, 1))) {
            level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        }
        var wet = WitcheryEntities.DARK_MARK.get().create(level);
        wet.setPos(origin);
        wet.setAirSupply(200);
        wet.tick();
        helper.assertTrue(wet.getAirSupply() == 200 && wet.fireImmune() && wet.isIgnoringBlockTriggers()
                        && wet.getAmbientSoundInterval() == 80 && wet.getVoicePitch() == 1,
                "A submerged mark keeps air and retains the original fire, trigger and ambient properties");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_dark_mark")
    public static void clientRepulsionDoesNotMoveServerCreatures(GameTestHelper helper) {
        var level = helper.getLevel();
        Vec3 origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 6, 4)));
        var mark = WitcheryEntities.DARK_MARK.get().create(level);
        var legacy = new CompoundTag();
        legacy.putLong("WITCTicksAlive", 3);
        mark.readAdditionalSaveData(legacy);
        mark.setPos(origin);
        var pig = EntityType.PIG.create(level);
        pig.setPos(origin.add(2, 0, 0));
        level.addFreshEntity(pig);
        try {
            mark.tick();
            helper.assertTrue(pig.getDeltaMovement().equals(Vec3.ZERO),
                    "The original four-tick pulse affects the client without moving the server pig");
        } finally {
            pig.discard();
        }
        helper.succeed();
    }
}
