package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.RiteHeartBlock;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.SeerStoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
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

    @GameTest(template = TEMPLATE, setupTicks = 5)
    public static void turnsInLegacyPetTrophies(GameTestHelper helper) {
        FakePlayer player = owner(helper);
        var level = helper.getLevel();
        for (int quest = 0; quest < 2; quest++) {
            boolean spider = quest == 0;
            BlockPos pos = CENTER.offset(quest * 6 - 3, 0, 0);
            var witch = helper.spawnWithNoFreeWill(WitcheryEntities.COVEN_WITCH.get(), pos.north(2));
            witch.setCustomName(Component.literal(spider ? "Spider Witch" : "Zombie Witch"));
            var offer = witch.saveWithoutId(new CompoundTag());
            offer.putInt("QuestType", quest);
            offer.putInt("QuestItemsNeeded", 1);
            offer.putBoolean("QuestAccepted", true);
            offer.putString("QuestOffered", player.getGameProfile().getName());
            witch.load(offer);

            var pet = spider ? helper.spawnWithNoFreeWill(EntityType.SPIDER, pos)
                    : helper.spawnWithNoFreeWill(EntityType.ZOMBIE, pos);
            var trophy = new CompoundTag();
            if (spider) trophy.putShort("id", (short) 375);
            else trophy.putString("id", "minecraft:rotten_flesh");
            trophy.putByte("Count", (byte) 1);
            trophy.putShort("Damage", (short) 0);
            var display = new CompoundTag();
            String name = spider ? "Legacy Pet Eye" : "Legacy Pet Flesh";
            display.putString("Name", name);
            var binding = new CompoundTag();
            binding.put("display", display);
            binding.putLong("WITCQuestOwnerIDMost", witch.getUUID().getMostSignificantBits());
            binding.putLong("WITCQuestOwnerIDLeast", witch.getUUID().getLeastSignificantBits());
            trophy.put("tag", binding);
            var drops = new ListTag();
            drops.add(trophy);
            pet.getPersistentData().put(CovenWitch.EXTRA_DROPS, drops);
            pet.kill();

            var trophies = level.getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(pos)).inflate(1),
                    item -> item.getItem().getHoverName().getString().equals(name));
            helper.assertTrue(trophies.size() == 1, "Pet should drop its named legacy trophy");
            ItemStack stack = trophies.get(0).getItem();
            helper.assertTrue(stack.is(spider ? Items.SPIDER_EYE : Items.ROTTEN_FLESH), "Trophy should retain its original item");
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            witch.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(witch.isRemoved() && stack.isEmpty(), "The quest giver should accept the recovered trophy");
            trophies.get(0).discard();
        }
        helper.assertTrue(CovenWitch.covenSize(player) == 3, "Both quest givers should join the player's coven");
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
                CovenWitch.dismissNearbyForRite(owner);
            }
        }).thenSucceed();
    }

    // Give the newly forced test chunks time to register their entity sections.
    @GameTest(template = TEMPLATE, batch = "witchery_coven_rite", setupTicks = 5)
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
            CovenWitch visitor = ritualMember(helper, owner, "Seer Stone recall");
            placeTalismanRite(helper);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(WitcheryIngredient.SEER_STONE.item()));
            var state = level.getBlockState(center);
            state.use(level, owner, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(center), Direction.UP, center, false));
            helper.assertTrue(level.getBlockState(center).getValue(RiteHeartBlock.ACTIVE) && visitor.isRemoved()
                            && CovenWitch.covenSize(owner) == 1,
                    "Starting a real rite should dismiss the visit and retain the roster");
            helper.assertTrue(CovenWitch.summonToRite(owner, center, 0), "The witch should take the first ritual seat");
            CovenWitch summoned = ritualMember(helper, owner, "first ritual seat");
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
            CovenWitch.dismissNearbyForRite(owner);
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_seer_stone_reuse", timeoutTicks = 160)
    public static void callsOneCovenMemberPerSecondAndReusesThemAtARite(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = owner(helper);
        BlockPos center = owner.blockPosition().atY(192);
        owner.setPos(Vec3.atBottomCenterOf(center));
        for (BlockPos floor : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, -1, 3))) {
            level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        }
        BlockPos pedestal = center.above(4);
        level.setBlockAndUpdate(pedestal, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pedestal.above(), WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var members = owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getList("WITCCoven", Tag.TAG_COMPOUND);
        var second = new CompoundTag();
        second.putString("WitchName", "Isobel Sterling");
        second.putInt("Skin", 1);
        members.add(second);

        var stone = (SeerStoneItem) WitcheryIngredient.SEER_STONE.item();
        var stack = new ItemStack(stone);
        var called = new ArrayList<CovenWitch>();
        var corners = List.of(center.offset(-3, 0, -3), center.offset(-3, 0, 3),
                center.offset(3, 0, -3), center.offset(3, 0, 3));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(corners.stream().allMatch(level::isPositionEntityTicking),
                "The summoning platform is ticking")).thenExecute(() -> {
            stone.onUseTick(level, owner, stack, 1181);
            helper.assertTrue(ownedWitches(helper, owner).isEmpty(), "Nineteen ticks do not call a member");
            stone.onUseTick(level, owner, stack, 1180);
        }).thenWaitUntil(() -> {
            var first = ritualMember(helper, owner, "One second of use");
            helper.assertTrue(first.getName().getString().equals(MEMBER) && first.skin() == 3 && !first.isNoAi(),
                    "The Seer Stone calls the first saved member with their original skin and AI");
            called.add(first);
        }).thenExecute(() -> stone.onUseTick(level, owner, stack, 1160)).thenWaitUntil(() -> {
            var both = ownedWitches(helper, owner);
            helper.assertTrue(both.size() == 2 && both.contains(called.get(0))
                            && !both.get(0).blockPosition().equals(both.get(1).blockPosition()),
                    "Two seconds call both members into separate positions");
            var other = both.stream().filter(witch -> witch.getName().getString().equals("Isobel Sterling"))
                    .findFirst().orElseThrow();
            called.add(other);
        }).thenExecute(() -> {
            var first = called.get(0);
            var other = called.get(1);
            helper.assertTrue(CovenWitch.summonToRite(owner, center, 0) && CovenWitch.summonToRite(owner, center, 1)
                            && first.blockPosition().equals(center.offset(-2, 0, -2))
                            && other.blockPosition().equals(center.offset(2, 0, -2))
                            && ownedWitches(helper, owner).size() == 2 && !first.isNoAi() && !other.isNoAi(),
                    "The rite seats the existing members and keeps their AI running");
            for (var witch : called) {
                var saved = witch.saveWithoutId(new CompoundTag());
                helper.assertTrue(saved.getBoolean("Sitting") && saved.getInt("SuicideIn") == 300
                                && saved.getUUID("Owner").equals(owner.getUUID()),
                        "The rite saves each member's seat, owner and fifteen-second lifetime");
                saved.putInt("SuicideIn", 3);
                witch.load(saved);
            }
        }).thenWaitUntil(() -> helper.assertTrue(called.stream().allMatch(CovenWitch::isRemoved) && CovenWitch.covenSize(owner) == 2,
                "Both members leave when their time expires and stay in the coven")).thenSucceed();
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

    private static CovenWitch ritualMember(GameTestHelper helper, FakePlayer owner, String stage) {
        var members = ownedWitches(helper, owner);
        helper.assertTrue(members.size() == 1, stage + " should leave one active witch near the rite, found " + members.size());
        return members.get(0);
    }

    private static List<CovenWitch> ownedWitches(GameTestHelper helper, FakePlayer owner) {
        return helper.getLevel().getEntitiesOfClass(CovenWitch.class, owner.getBoundingBox().inflate(12), witch -> {
            var saved = witch.saveWithoutId(new CompoundTag());
            return witch.isAlive() && saved.hasUUID("Owner") && owner.getUUID().equals(saved.getUUID("Owner"));
        });
    }
}
