package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ScryEyeTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_imported_scry_eye")
    public static void anImportedEyeStillExpiresWithoutAnActiveCameraSession(GameTestHelper helper) {
        var record = new CompoundTag();
        record.putString("id", "witchery:eye");
        var eye = EntityType.create(record, helper.getLevel()).orElseThrow();
        eye.tickCount = ScryEye.LIFETIME;
        double before = eye.getY();
        eye.tick();
        helper.assertTrue(!eye.isRemoved() && eye.getY() == before + 0.5,
                "An orphaned eye still moves during its two hundredth tick");
        eye.tickCount++;
        eye.tick();
        helper.assertTrue(eye.isRemoved(), "An orphaned eye expires on its original final tick");
        helper.succeed();
    }
}
