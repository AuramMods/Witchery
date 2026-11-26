package art.arcane.witchery.vampirism;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class VampireWeakeningTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_vampire_weakening")
    public static void starvationAndDaylightApplyOriginalEffects(GameTestHelper helper) {
        var world = helper.getLevel();
        var nether = world.getServer().getLevel(Level.NETHER);
        Vec3 position = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 2, 3)).atY(300));
        long time = world.getDayTime();
        float rain = world.getRainLevel(1);
        var starved = vampire(nether, Vec3.ZERO, 5, 0, 0);
        var daylight = vampire(world, position, 5, 400, 20);
        var both = vampire(world, position, 5, 0, 0);
        var lowRank = vampire(world, position, 4, 400, 20);
        try {
            world.setDayTime(6000);
            world.setRainLevel(0);
            world.updateSkyBrightness();
            helper.assertTrue(Vampirism.isInSunlight(daylight), "The fixture is exposed to daylight");

            Vampirism.tick(starved);
            assertEffects(helper, starved, 8, 1, true);

            Vampirism.tick(daylight);
            assertEffects(helper, daylight, 3, 0, false);
            var data = WitcheryPlayerDataProvider.get(daylight).orElseThrow(IllegalStateException::new);
            helper.assertTrue(data.getBloodPower() == 340 && !daylight.isOnFire(),
                    "A rank-five pulse spends sixty blood without igniting a fed vampire");

            Vampirism.tick(both);
            assertEffects(helper, both, 8, 1, false);
            helper.assertTrue(both.isOnFire(), "An exposed vampire with no blood burns");

            Vampirism.tick(lowRank);
            helper.assertTrue(lowRank.isOnFire() && lowRank.getActiveEffects().isEmpty(),
                    "Daylight below rank five ignites without applying the tolerance effects");
        } finally {
            world.setDayTime(time);
            world.setRainLevel(rain);
            world.updateSkyBrightness();
            starved.discard();
            daylight.discard();
            both.discard();
            lowRank.discard();
        }
        helper.succeed();
    }

    private static FakePlayer vampire(ServerLevel world, Vec3 position, int rank, int blood, int food) {
        var player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "VampireWeakening"));
        player.setPos(position);
        player.tickCount = 1;
        player.getFoodData().setFoodLevel(food);
        var data = WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new);
        data.setVampireLevel(rank);
        data.setBloodPower(blood);
        return player;
    }

    private static void assertEffects(GameTestHelper helper, FakePlayer vampire, int weakness, int slowdown, boolean ambientWeakness) {
        assertEffect(helper, vampire, MobEffects.WEAKNESS, weakness, ambientWeakness);
        assertEffect(helper, vampire, MobEffects.MOVEMENT_SLOWDOWN, slowdown, true);
        assertEffect(helper, vampire, MobEffects.DIG_SLOWDOWN, slowdown, true);
        helper.assertTrue(vampire.getActiveEffects().size() == 3, "The pulse applies exactly the original three effects");
    }

    private static void assertEffect(GameTestHelper helper, FakePlayer vampire, MobEffect effect, int amplifier, boolean ambient) {
        var instance = vampire.getEffect(effect);
        helper.assertTrue(instance != null && instance.getAmplifier() == amplifier && instance.getDuration() == 200
                        && instance.isAmbient() == ambient && instance.isVisible(),
                "The status effect matches the original amplifier, duration and presentation: " + effect.getDescriptionId());
    }
}
