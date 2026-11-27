package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.EnvironmentalCurseEffect;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class IllusionTests {
    private static final Field LEVEL_RANDOM = ObfuscationReflectionHelper.findField(Level.class, "f_46441_");

    @GameTest(template = "qa_coven_arena", batch = "witchery_insanity")
    public static void usesOneLegacyColumnAndWorldRolls(GameTestHelper helper) throws IllegalAccessException {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(11, 0, 11)).atY(250);
        var victim = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "IllusionVictim"));
        victim.setPos(Vec3.atBottomCenterOf(origin));
        var columns = List.of(origin.offset(-9, 0, -9), origin.offset(9, 0, 9), origin.offset(-4, 0, 5),
                origin.offset(-4, 0, -4), origin.offset(-9, 0, 9));
        for (BlockPos column : columns) {
            for (int y = -19; y <= 10; y++) {
                level.setBlockAndUpdate(column.above(y), Blocks.AIR.defaultBlockState());
            }
        }
        level.setBlockAndUpdate(origin.offset(-9, -20, -9), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(origin.offset(9, -1, 9), Blocks.STONE.defaultBlockState());
        for (int y = -1; y <= 7; y++) level.setBlockAndUpdate(origin.offset(-4, y, 5), Blocks.STONE.defaultBlockState());
        for (int y = -1; y <= 10; y++) level.setBlockAndUpdate(origin.offset(-4, y, -4), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(origin.offset(-9, -1, 9), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(origin.offset(-9, 1, 9), Blocks.STONE.defaultBlockState());
        var effect = WitcheryEffects.INSANITY.get();
        helper.assertTrue(effect.isDurationEffectTick(33, 0) && effect.isDurationEffectTick(13, 2)
                        && !effect.isDurationEffectTick(12, 2) && !effect.isDurationEffectTick(14, 2),
                "The original timed potion pulses on duration 13 modulo 20");

        var arrivals = new ArrayList<Illusion>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof Illusion illusion) arrivals.add(illusion);
        };
        var previous = level.getRandom();
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            var deep = new Rolls(0, 0, 0, 0);
            LEVEL_RANDOM.set(level, deep);
            effect.applyEffectTick(victim, 0);
            assertSpawn(helper, arrivals, WitcheryEntities.ILLUSION_CREEPER.get(), origin.offset(-9, -20, -9), victim);
            helper.assertTrue(deep.bounds.equals(List.of(35, 3, 11, 11)), "Base potion uses the four legacy draws");

            var corner = new Rolls(0, 1, 10, 10);
            LEVEL_RANDOM.set(level, corner);
            EnvironmentalCurseEffect.spreadInsanity(victim, 2);
            assertSpawn(helper, arrivals, WitcheryEntities.ILLUSION_SPIDER.get(), origin.offset(9, -1, 9), victim);
            helper.assertTrue(corner.bounds.equals(List.of(30, 3, 11, 11)), "A curse uses its original corner and roll");

            var raised = new Rolls(0, 2, 5, 6);
            LEVEL_RANDOM.set(level, raised);
            effect.applyEffectTick(victim, 2);
            assertSpawn(helper, arrivals, WitcheryEntities.ILLUSION_ZOMBIE.get(), origin.offset(-4, 7, 5), victim);
            helper.assertTrue(raised.bounds.equals(List.of(25, 3, 11, 11)), "Rank three uses the original split draws");

            var blocked = new Rolls(0, 1, 5, 5);
            LEVEL_RANDOM.set(level, blocked);
            effect.applyEffectTick(victim, 3);
            helper.assertTrue(arrivals.isEmpty() && blocked.bounds.equals(List.of(25, 3, 11, 11)),
                    "A blocked selected column spends its single spawn attempt without rolling for sound");

            var lowCeiling = new Rolls(0, 2, 0, 10);
            LEVEL_RANDOM.set(level, lowCeiling);
            effect.applyEffectTick(victim, 2);
            helper.assertTrue(arrivals.isEmpty() && lowCeiling.bounds.equals(List.of(25, 3, 11, 11)),
                    "A single empty block cannot admit an apparition");
        } finally {
            LEVEL_RANDOM.set(level, previous);
            MinecraftForge.EVENT_BUS.unregister(capture);
            arrivals.forEach(Illusion::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_insanity")
    public static void retainsSignedIllusionTypeAndConsumesBothLifetimeRolls(GameTestHelper helper) throws IllegalAccessException {
        var level = helper.getLevel();
        var spider = WitcheryEntities.ILLUSION_SPIDER.get().create(level);
        var tag = new CompoundTag();
        tag.putString("Victim", "IllusionVictim");
        tag.putInt("IllusionType", 255);
        spider.readAdditionalSaveData(tag);
        spider.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 5, 4))));
        spider.setHealth(2);
        var previous = level.getRandom();
        try {
            var alive = new Rolls(0, 1);
            LEVEL_RANDOM.set(level, alive);
            spider.tick();
            var saved = spider.saveWithoutId(new CompoundTag());
            helper.assertTrue(!spider.isRemoved() && spider.getHealth() == 1 && alive.bounds.equals(List.of(15, 40))
                            && saved.getInt("IllusionType") == -1 && saved.getString("Victim").equals("IllusionVictim"),
                    "The original rolls and signed type are retained while the spider is alive");

            var expired = new Rolls(0, 1);
            LEVEL_RANDOM.set(level, expired);
            spider.tick();
            helper.assertTrue(spider.isRemoved() && expired.bounds.equals(List.of(15, 40)),
                    "The final health roll discards the spider and the independent sound roll still runs");
        } finally {
            LEVEL_RANDOM.set(level, previous);
        }
        helper.succeed();
    }

    private static void assertSpawn(GameTestHelper helper, List<Illusion> arrivals, EntityType<?> expected,
                                    BlockPos floor, FakePlayer victim) {
        helper.assertTrue(arrivals.size() == 1, "Expected one " + expected + " above " + floor + ", got " + arrivals.size());
        var illusion = arrivals.remove(0);
        try {
            helper.assertTrue(illusion.getType() == expected, "The original variant was " + expected + ", got " + illusion.getType());
            Vec3 position = Vec3.atBottomCenterOf(floor).add(0, 1.05, 0);
            helper.assertTrue(illusion.position().distanceTo(position) < 1.0E-8,
                    "Expected original column and height " + position + ", got " + illusion.position());
            helper.assertTrue(illusion.getTarget() == victim && illusion.victimName().equals(victim.getGameProfile().getName()),
                    "The apparition follows its named victim");
            helper.assertTrue(illusion.getBbWidth() == 0.6F && illusion.getBbHeight() == 1.8F
                            && illusion.isPushable() && illusion.fireImmune() && illusion.getMaxFallDistance() == 22,
                    "The original creature profile has 0.6 by 1.8 bounds, pushability, fire immunity and a 22-block drop");
        } finally {
            illusion.discard();
        }
    }

    private static final class Rolls extends LegacyRandomSource {
        private final int[] values;
        private final List<Integer> bounds = new ArrayList<>();

        private Rolls(int... values) {
            super(0);
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            int index = bounds.size();
            bounds.add(bound);
            if (index >= values.length) throw new AssertionError("Unexpected random bound " + bound + " in " + bounds);
            return values[index];
        }
    }
}
