package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
}
