package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class HellhoundConversionTests {
    @GameTest(template = "qa_coven_arena")
    public static void reloadsTheEffectsOfAnActiveCure(GameTestHelper helper) {
        var type = WitcheryEntities.HELLHOUND.get();
        var level = helper.getLevel();
        var weakened = type.create(level);
        weakened.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300));
        var saved = weakened.saveWithoutId(new CompoundTag());

        var loaded = type.create(level);
        loaded.load(saved);
        helper.assertTrue(!loaded.isConverting() && loaded.hasEffect(MobEffects.WEAKNESS),
                "Reloading an untreated Hellhound should preserve its Weakness");

        int remaining = 1200;
        saved.putInt("ConversionTime", remaining);
        var curing = type.create(level);
        curing.load(saved);
        var strength = curing.getEffect(MobEffects.DAMAGE_BOOST);
        helper.assertTrue(curing.isConverting() && !curing.hasEffect(MobEffects.WEAKNESS),
                "Reloading an active cure should remove Weakness");
        helper.assertTrue(strength != null && strength.getAmplifier() == 0 && strength.getDuration() == remaining,
                "Reloading an active cure should grant Strength for the remaining ticks");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void curingHellhoundFindsBarsFromTheOriginalNegativeCoordinateOrigin(GameTestHelper helper) {
        var level = helper.getLevel();
        var hound = WitcheryEntities.HELLHOUND.get().create(level);
        int x = helper.absolutePos(new BlockPos(8, 0, 0)).getX();
        hound.moveTo(x + 0.5, 180, -0.5, 0, 0);
        hound.setNoAi(true);
        var saved = hound.saveWithoutId(new CompoundTag());
        saved.putInt("ConversionTime", 100);
        hound.load(saved);
        var bars = new BlockPos(x, 180, 3);
        var previous = level.getBlockState(bars);
        level.setBlockAndUpdate(bars, Blocks.IRON_BARS.defaultBlockState());
        long seed = 0;
        var random = RandomSource.create();
        while (!accelerates(random, seed)) seed++;
        hound.getRandom().setSeed(seed);
        try {
            hound.tick();
            helper.assertTrue(hound.saveWithoutId(new CompoundTag()).getInt("ConversionTime") == 98,
                    "The bar at the edge of the original cure search accelerates the countdown");
        } finally {
            level.setBlockAndUpdate(bars, previous);
        }
        helper.succeed();
    }

    private static boolean accelerates(RandomSource random, long seed) {
        random.setSeed(seed);
        return random.nextFloat() < 0.01F && random.nextFloat() < 0.3F;
    }
}
