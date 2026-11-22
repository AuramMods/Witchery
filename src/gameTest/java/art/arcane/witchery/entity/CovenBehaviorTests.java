package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.RiteHeartBlock;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CovenBehaviorTests {
    private static final String TEMPLATE = "qa_coven_arena";
    private static final BlockPos CENTER = new BlockPos(7, 3, 5);
    private static final String MEMBER = "Agnes Morrow";

    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void finishesDrinkingFireResistanceAfterReload(GameTestHelper helper) {
        CovenWitch witch = helper.spawn(WitcheryEntities.COVEN_WITCH.get(), CENTER);
        witch.setSecondsOnFire(30);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, CENTER.east(2));
        helper.startSequence().thenWaitUntil(() -> {
            helper.assertTrue(witch.getMainHandItem().is(Items.POTION)
                            && PotionUtils.getPotion(witch.getMainHandItem()) == Potions.FIRE_RESISTANCE
                            && witch.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0,
                    "A burning Coven Witch should stop to drink Fire Resistance");
        }).thenExecute(() -> {
            CompoundTag saved = witch.saveWithoutId(new CompoundTag());
            witch.load(saved);
            witch.performRangedAttack(pig, 1);
            var potions = helper.getLevel().getEntitiesOfClass(ThrownPotion.class, witch.getBoundingBox().inflate(4));
            helper.assertTrue(saved.getInt("DrinkTicks") > 0 && witch.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0
                            && potions.isEmpty(), "Reloading should preserve the drinking delay and prevent a potion throw");
        }).thenWaitUntil(() -> {
            helper.assertTrue(witch.hasEffect(MobEffects.FIRE_RESISTANCE) && witch.getMainHandItem().isEmpty()
                            && witch.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0.25,
                    "The resumed drink should apply Fire Resistance and restore normal walking speed");
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 520)
    public static void healsAndDrinksSwiftnessForADistantTarget(GameTestHelper helper) {
        CovenWitch witch = helper.spawn(WitcheryEntities.COVEN_WITCH.get(), CENTER);
        witch.setHealth(18);
        var husk = helper.spawnWithNoFreeWill(EntityType.HUSK, CENTER.east(14));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(witch.getHealth() > 18,
                "An injured witch should use her healing potion")).thenExecute(() -> {
            witch.setHealth(witch.getMaxHealth());
            witch.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(CENTER)));
            witch.setTarget(husk);
        }).thenWaitUntil(() -> helper.assertTrue(witch.hasEffect(MobEffects.MOVEMENT_SPEED),
                "A witch facing a distant target should finish a Swiftness potion")).thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void throwsTheLegacyStrengthAndDuration(GameTestHelper helper) {
        CovenWitch witch = helper.spawnWithNoFreeWill(WitcheryEntities.COVEN_WITCH.get(), CENTER);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, CENTER.east(9));
        assertThrownEffect(helper, witch, pig, MobEffects.MOVEMENT_SLOWDOWN, 1_351);
        pig.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100));
        assertThrownEffect(helper, witch, pig, MobEffects.POISON, 676);
        pig.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
        assertThrownEffect(helper, witch, pig, MobEffects.HARM, 1);
        pig.setHealth(7);
        pig.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(CENTER.east(2))));
        long seed = 0;
        while (RandomSource.create(seed).nextFloat() >= 0.25F) seed++;
        witch.getRandom().setSeed(seed);
        assertThrownEffect(helper, witch, pig, MobEffects.WEAKNESS, 1_351);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 150)
    public static void visitsABedAndKeepsTheExistingMembersLifetime(GameTestHelper helper) {
        FakePlayer owner = owner(helper);
        var level = helper.getLevel();
        BlockPos home = helper.absolutePos(CENTER);
        long seed = 0;
        while (RandomSource.create(seed).nextInt(20) != 0) seed++;
        long visitSeed = seed;
        helper.startSequence().thenWaitUntil(() ->
                helper.assertTrue(level.getGameTime() % 100 == 0, "Wait for the bed visit check")).thenExecute(() -> {
            helper.assertTrue(level.getBlockState(home.offset(-2, -1, -2)).isFaceSturdy(level, home.offset(-2, -1, -2), Direction.UP),
                    "The visit fixture should have a solid landing surface");
            owner.setRespawnPosition(Level.NETHER, home, 0, true, false);
            owner.getRandom().setSeed(visitSeed);
            CovenWitch.tickVisits(owner);
            helper.assertTrue(witches(helper).isEmpty(), "The respawn point must be in the current dimension");
            owner.setRespawnPosition(level.dimension(), home, 0, false, false);
            owner.setPos(Vec3.atBottomCenterOf(home.east(20)));
            owner.getRandom().setSeed(visitSeed);
            CovenWitch.tickVisits(owner);
            AABB area = owner.getBoundingBox().inflate(4);
            helper.assertTrue(level.getEntitiesOfClass(CovenWitch.class, area).isEmpty(), "Visits should stay within sixteen blocks of the bed");
            owner.setPos(Vec3.atBottomCenterOf(home));
            var config = WitcheryServerConfig.ALLOW_COVEN_WITCH_VISITS;
            boolean enabled = config.get();
            try {
                config.set(false);
                owner.getRandom().setSeed(visitSeed);
                CovenWitch.tickVisits(owner);
                helper.assertTrue(witches(helper).isEmpty(), "The server toggle should stop automatic visits");
                config.set(true);
                owner.getRandom().setSeed(visitSeed);
                CovenWitch.tickVisits(owner);
                var members = witches(helper);
                helper.assertTrue(members.size() == 1 && members.get(0).getName().getString().equals(MEMBER),
                        "The next check should call the saved member");
                CovenWitch witch = members.get(0);
                var state = witch.saveWithoutId(new CompoundTag());
                helper.assertTrue(state.getInt("SuicideIn") == 1_800 && state.getUUID("Owner").equals(owner.getUUID()),
                        "A new visit should last ninety seconds");
                state.putInt("SuicideIn", 1_650);
                witch.load(state);
                witch.moveTo(Vec3.atBottomCenterOf(home.east(4)));
                owner.getRandom().setSeed(visitSeed);
                CovenWitch.tickVisits(owner);
                helper.assertTrue(witches(helper).equals(List.of(witch)) && witch.blockPosition().equals(home.offset(-2, 0, -2))
                                && witch.saveWithoutId(new CompoundTag()).getInt("SuicideIn") == 1_650,
                        "A second visit should reuse the same witch without resetting the timer");
            } finally {
                config.set(enabled);
            }
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void replacesAnActiveVisitAtTheRiteAndRemovesADeadMember(GameTestHelper helper) {
        var level = helper.getLevel();
        FakePlayer owner = owner(helper);
        level.addNewPlayer(owner);
        try {
            BlockPos center = helper.absolutePos(CENTER);
            helper.assertTrue(CovenWitch.covenSize(owner) == 1 && owner.blockPosition().equals(center),
                    "The owner should keep the coven and test position after joining");
            BlockPos floor = center.offset(-2, -1, -2);
            helper.assertTrue(level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP),
                    "The ritual fixture should have a solid landing surface");
            helper.assertTrue(CovenWitch.recallMember(owner, 0), "The Seer Stone call should find a clear position");
            CovenWitch visitor = witches(helper).get(0);
            placeTalismanRite(helper);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(WitcheryIngredient.SEER_STONE.item()));
            var state = level.getBlockState(center);
            state.use(level, owner, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(center), Direction.UP, center, false));
            helper.assertTrue(level.getBlockState(center).getValue(RiteHeartBlock.ACTIVE) && visitor.isRemoved()
                            && CovenWitch.covenSize(owner) == 1,
                    "Starting a real rite should dismiss the visit and retain the roster");
            helper.assertTrue(CovenWitch.summonToRite(owner, center, 0), "The witch should take the first ritual seat");
            CovenWitch summoned = witches(helper).get(0);
            var saved = summoned.saveWithoutId(new CompoundTag());
            helper.assertTrue(!summoned.getUUID().equals(visitor.getUUID()) && saved.getBoolean("Sitting") && !summoned.isNoAi()
                            && saved.getInt("SuicideIn") == 300 && summoned.blockPosition().equals(center.offset(-2, 0, -2)),
                    "The ritual should use a seated witch who can still drink and has the original fifteen-second lifetime");
            saved.remove("Sitting");
            saved.putBoolean("NoAI", true);
            summoned.load(saved);
            helper.assertTrue(summoned.saveWithoutId(new CompoundTag()).getBoolean("Sitting") && !summoned.isNoAi(),
                    "Older port saves should recover seated state from the former NoAI flag");
            summoned.hurt(level.damageSources().genericKill(), 1000);
            helper.assertTrue(CovenWitch.covenSize(owner) == 0, "A real death should remove the member from the online owner's coven");
        } finally {
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    private static void assertThrownEffect(GameTestHelper helper, CovenWitch witch, LivingEntity target,
                                           MobEffect effect, int duration) {
        witch.performRangedAttack(target, 1);
        var potions = helper.getLevel().getEntitiesOfClass(ThrownPotion.class, witch.getBoundingBox().inflate(2));
        helper.assertTrue(potions.size() == 1, "Each attack should throw one splash potion");
        var effects = PotionUtils.getMobEffects(potions.get(0).getItem());
        helper.assertTrue(effects.size() == 1 && effects.get(0).getEffect() == effect
                        && effects.get(0).getDuration() == duration && effects.get(0).getAmplifier() == 0,
                "The splash should use the original effect, duration and strength");
        potions.get(0).discard();
    }

    private static void placeTalismanRite(GameTestHelper helper) {
        var glyph = WitcheryBlocks.getRequired("circleglyphritual").get();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                int outer = Math.max(Math.abs(x), Math.abs(z));
                int inner = Math.min(Math.abs(x), Math.abs(z));
                if (outer == 3 && inner <= 1 || outer == 2 && inner == 2) helper.setBlock(CENTER.offset(x, 0, z), glyph);
            }
        }
        helper.setBlock(CENTER, WitcheryBlocks.getRequired("circle").get());
        var level = helper.getLevel();
        Vec3 center = Vec3.atCenterOf(helper.absolutePos(CENTER));
        for (var item : List.of(WitcheryItems.getRequired("circletalisman").get(), Items.REDSTONE)) {
            var entity = new ItemEntity(level, center.x, center.y, center.z, new ItemStack(item));
            entity.setNoGravity(true);
            entity.setNeverPickUp();
            level.addFreshEntity(entity);
        }
    }

    private static FakePlayer owner(GameTestHelper helper) {
        var owner = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "CovenBehaviorQA"));
        owner.setPos(Vec3.atBottomCenterOf(helper.absolutePos(CENTER)));
        var member = new CompoundTag();
        member.putString("WitchName", MEMBER);
        member.putInt("Skin", 3);
        var coven = new ListTag();
        coven.add(member);
        var data = new CompoundTag();
        data.put("WITCCoven", coven);
        owner.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
        return owner;
    }

    private static List<CovenWitch> witches(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(CovenWitch.class, new AABB(helper.absolutePos(CENTER)).inflate(8, 3, 4),
                LivingEntity::isAlive);
    }
}
