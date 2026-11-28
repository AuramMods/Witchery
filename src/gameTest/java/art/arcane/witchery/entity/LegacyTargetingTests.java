package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyTargetingTests {
    private static final Field TARGET_GOALS = ObfuscationReflectionHelper.findField(Mob.class, "f_21346_");
    private static final Field MELEE_GOALS = ObfuscationReflectionHelper.findField(Mob.class, "f_21345_");

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_targeting")
    public static void allLegacyPathCheckingMobsRejectUnreachableTargets(GameTestHelper helper) {
        Arena arena = new Arena(helper);
        var corners = List.of(arena.origin.offset(-5, 0, -5), arena.origin.offset(11, 0, -5),
                arena.origin.offset(-5, 0, 5), arena.origin.offset(11, 0, 5));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                corners.stream().allMatch(arena.level::isPositionEntityTicking), "The targeting arena can track its prey"))
                .thenExecute(() -> {
                    for (var kind : hunters()) {
                        arena.check(kind, Layout.OPEN, kind.prey(), "normal", true, true);
                        arena.check(kind, Layout.WALL, kind.prey(), "normal", false, kind != Hunter.GOBLIN);
                        arena.check(kind, Layout.GAP, kind.prey(), "normal", true, false);
                    }
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_targeting_filters")
    public static void legacyCreatureFiltersSurvivePathChecking(GameTestHelper helper) throws IllegalAccessException {
        Arena arena = new Arena(helper);
        arena.check(Hunter.TREEFYD, Layout.OPEN, null, "owner", true, false);
        arena.check(Hunter.IMP, Layout.OPEN, null, "tamed", true, false);
        arena.check(Hunter.IMP, Layout.OPEN, EntityType.SHEEP, "commanded", true, true);
        arena.check(Hunter.GOBLIN, Layout.OPEN, EntityType.VILLAGER, "small-party", true, false);
        arena.check(Hunter.VAMPIRE, Layout.OPEN, EntityType.VILLAGER, "no-visit", true, false);
        arena.check(Hunter.VAMPIRE, Layout.OPEN, null, "no-visit", true, false);
        arena.check(Hunter.GUARD, Layout.OPEN, WitcheryEntities.GOBLIN.get(), "normal", true, true);
        arena.check(Hunter.GUARD, Layout.OPEN, WitcheryEntities.VAMPIRE.get(), "normal", true, false);
        arena.check(Hunter.GUARD, Layout.OPEN, EntityType.CREEPER, "normal", true, false);
        arena.check(Hunter.GUARD, Layout.OPEN, WitcheryEntities.WITCH_HUNTER.get(), "normal", true, false);
        arena.check(Hunter.BABA_YAGA, Layout.OPEN, null, "owner", true, false);
        arena.check(Hunter.WITCH_HUNTER, Layout.OPEN, EntityType.SHEEP, "normal", true, false);
        arena.check(Hunter.NIGHTMARE, Layout.OPEN, null, "other-victim", true, false);
        arena.check(Hunter.NIGHTMARE, Layout.WALL, null, "named-victim", false, true);
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_wolfman_targeting")
    public static void wolfmanAcquiresItsOriginalPrey(GameTestHelper helper) throws IllegalAccessException {
        Arena arena = new Arena(helper);
        for (Layout layout : Layout.values()) {
            arena.checkWolfman(layout, EntityType.VILLAGER, 0, true);
            arena.checkWolfman(layout, null, 0, layout != Layout.WALL);
        }
        arena.checkWolfman(Layout.OPEN, null, 1, false);
        arena.checkWolfman(Layout.OPEN, null, 2, false);
        arena.checkWolfman(Layout.OPEN, WitcheryEntities.WITCH_HUNTER.get(), 0, false);
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_wolfman_melee")
    public static void wolfmanRetaliatesWithMeleeOnlyAgainstOriginalPrey(GameTestHelper helper)
            throws IllegalAccessException {
        Arena arena = new Arena(helper);
        arena.prepare(Layout.OPEN);
        Wolfman wolfman = WitcheryEntities.WOLFMAN.get().create(helper.getLevel());
        wolfman.setPos(Vec3.atBottomCenterOf(arena.origin));
        wolfman.setOnGround(true);
        var targetGoals = (GoalSelector) TARGET_GOALS.get(wolfman);
        var meleeGoals = (GoalSelector) MELEE_GOALS.get(wolfman);
        helper.runAfterDelay(21, () -> {
            for (var type : List.of(WitcheryEntities.WITCH_HUNTER.get(), EntityType.SKELETON)) {
                Mob aggressor = arena.spawn(type, 6, 0);
                try {
                    wolfman.tickCount++;
                    wolfman.setLastHurtByMob(aggressor);
                    targetGoals.tick();
                    helper.assertTrue(wolfman.getTarget() == aggressor,
                            "The Wolfman remembers the attacking " + type);
                    helper.assertTrue(meleeGoals.getAvailableGoals().stream()
                                    .filter(goal -> goal.getGoal() instanceof MeleeAttackGoal).anyMatch(goal -> goal.canUse())
                                    == (type == WitcheryEntities.WITCH_HUNTER.get()),
                            "Unexpected Wolfman melee response to " + type);
                    targetGoals.getAvailableGoals().forEach(goal -> goal.stop());
                } finally {
                    aggressor.discard();
                }
            }
            wolfman.discard();
            helper.succeed();
        });
    }

    private static List<Hunter> hunters() {
        return List.of(Hunter.TREEFYD, Hunter.IMP, Hunter.GOBLIN, Hunter.VAMPIRE, Hunter.GUARD,
                Hunter.BABA_YAGA, Hunter.WITCH_HUNTER, Hunter.NIGHTMARE);
    }

    private enum Layout { OPEN, WALL, GAP }

    private record Hunter(String name, Supplier<? extends EntityType<? extends Mob>> type,
                          int priority, EntityType<? extends Mob> prey) {
        private static final Hunter TREEFYD = new Hunter("Treefyd", WitcheryEntities.TREEFYD, 3, null);
        private static final Hunter IMP = new Hunter("Imp", WitcheryEntities.IMP, 4, null);
        private static final Hunter GOBLIN = new Hunter("Goblin", WitcheryEntities.GOBLIN, 2, EntityType.VILLAGER);
        private static final Hunter VAMPIRE = new Hunter("Vampire", WitcheryEntities.VAMPIRE, 2, EntityType.VILLAGER);
        private static final Hunter GUARD = new Hunter("VillageGuard", WitcheryEntities.VILLAGE_GUARD, 3, EntityType.ZOMBIE);
        private static final Hunter BABA_YAGA = new Hunter("BabaYaga", WitcheryEntities.BABA_YAGA, 2, null);
        private static final Hunter WITCH_HUNTER = new Hunter("WitchHunter", WitcheryEntities.WITCH_HUNTER, 2, EntityType.ZOMBIE);
        private static final Hunter NIGHTMARE = new Hunter("Nightmare", WitcheryEntities.NIGHTMARE, 2, null);
    }

    private static final class Arena {
        private final GameTestHelper helper;
        private final ServerLevel level;
        private final BlockPos origin;

        private Arena(GameTestHelper helper) {
            this.helper = helper;
            this.level = helper.getLevel();
            this.origin = helper.absolutePos(new BlockPos(8, 0, 5)).atY(210);
        }

        private void prepare(Layout layout) {
            for (int x = -5; x <= 11; x++) {
                for (int z = -5; z <= 5; z++) {
                    level.setBlockAndUpdate(origin.offset(x, -1, z),
                            (layout == Layout.GAP && x >= 1 && x <= 3 ? Blocks.AIR : Blocks.STONE).defaultBlockState());
                }
            }
            for (int y = 0; y < 4; y++) {
                for (int z = -1; z <= 1; z++) {
                    level.setBlockAndUpdate(origin.offset(2, y, z),
                            (layout == Layout.WALL ? Blocks.STONE : Blocks.AIR).defaultBlockState());
                }
            }
        }

        private void checkWolfman(Layout layout, EntityType<? extends Mob> type, int form, boolean expected)
                throws IllegalAccessException {
            prepare(layout);
            Wolfman wolfman = WitcheryEntities.WOLFMAN.get().create(level);
            wolfman.setPos(Vec3.atBottomCenterOf(origin));
            LivingEntity candidate = type == null ? player("WolfmanPrey", 6) : spawn(type, 6, 0);
            try {
                if (form != 0) WitcheryPlayerDataProvider.get((Player) candidate).orElseThrow(AssertionError::new)
                        .setCreatureTypeOrdinal(form);
                String label = layout + " against " + candidate.getType() + " in form " + form;
                helper.assertTrue(wolfman.hasLineOfSight(candidate) == (layout != Layout.WALL),
                        "Unexpected Wolfman line of sight " + label);
                ((GoalSelector) TARGET_GOALS.get(wolfman)).tick();
                helper.assertTrue(wolfman.getTarget() == (expected ? candidate : null),
                        "Unexpected Wolfman target " + label);
            } finally {
                if (candidate instanceof FakePlayer player) level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
                else candidate.discard();
                wolfman.discard();
            }
        }

        private void check(Hunter kind, Layout layout, EntityType<? extends Mob> preyType, String mode,
                           boolean visible, boolean eligible) {
            prepare(layout);
            long savedTime = level.getDayTime();
            Mob hunter = kind.type().get().create(level);
            Vec3 start = Vec3.atBottomCenterOf(origin);
            hunter.setPos(start);
            hunter.setOnGround(true);
            int distance = kind == Hunter.GOBLIN ? 5 : 6;
            var actors = new ArrayList<Mob>();
            LivingEntity prey = preyType == null ? player("LegacyTarget", distance) : spawn(preyType, distance, 0);
            if (prey instanceof Mob creature) actors.add(creature);
            try {
                if (kind == Hunter.GOBLIN) {
                    hunter.setNoAi(true);
                    helper.assertTrue(level.addFreshEntity(hunter), "The raiding Hobgoblin joined its party");
                    for (int i = 0; i < (mode.equals("small-party") ? 1 : 2); i++) {
                        actors.add(spawn(WitcheryEntities.GOBLIN.get(), -2, i + 2));
                    }
                }
                if (kind == Hunter.VAMPIRE) {
                    level.setDayTime(18_000);
                    level.updateSkyBrightness();
                    var saved = new CompoundTag();
                    saved.putBoolean("Hunting", !mode.equals("no-visit"));
                    hunter.readAdditionalSaveData(saved);
                }
                if (mode.equals("owner")) {
                    if (hunter instanceof BabaYaga witch) witch.bind((FakePlayer) prey);
                    else {
                        var saved = new CompoundTag();
                        saved.putString("Owner", ((FakePlayer) prey).getGameProfile().getName());
                        hunter.readAdditionalSaveData(saved);
                    }
                }
                if (mode.equals("tamed") || mode.equals("commanded")) {
                    ((TamableAnimal) hunter).setTame(true);
                    if (mode.equals("commanded")) hunter.setTarget(prey);
                }
                if (mode.equals("named-victim")) ((Nightmare) hunter).assignVictim((FakePlayer) prey);
                if (mode.equals("other-victim")) {
                    var saved = new CompoundTag();
                    saved.putString("Victim", "DifferentPlayer");
                    hunter.readAdditionalSaveData(saved);
                }

                String label = kind.name() + " " + layout + " " + mode + " vs " + prey.getType();
                helper.assertTrue(hunter.hasLineOfSight(prey) == visible, "Unexpected visibility for " + label);
                GoalSelector goals = ObfuscationReflectionHelper.getPrivateValue(Mob.class, hunter, "f_21346_");
                var goal = goals.getAvailableGoals().stream()
                        .filter(entry -> entry.getPriority() == kind.priority()
                                && entry.getGoal() instanceof NearestAttackableTargetGoal<?>)
                        .findFirst().orElseThrow();
                helper.assertTrue(goal.canUse() == eligible, "Unexpected target selection for " + label);
                if (eligible) {
                    goal.start();
                    helper.assertTrue(hunter.getTarget() == prey, "Unexpected acquired prey for " + label);
                }
                if (kind == Hunter.VAMPIRE && prey instanceof FakePlayer) {
                    goals.tick();
                    helper.assertTrue(hunter.getTarget() == null, "An idle vampire leaves a nearby player alone");
                }
            } finally {
                if (prey instanceof FakePlayer player) level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
                actors.forEach(Entity::discard);
                hunter.discard();
                if (kind == Hunter.VAMPIRE) {
                    level.setDayTime(savedTime);
                    level.updateSkyBrightness();
                }
            }
        }

        private Mob spawn(EntityType<? extends Mob> type, int x, int z) {
            Mob entity = type.create(level);
            entity.setNoAi(true);
            entity.setPos(Vec3.atBottomCenterOf(origin.offset(x, 0, z)));
            helper.assertTrue(level.addFreshEntity(entity), "The fixture mob joined the world");
            return entity;
        }

        private FakePlayer player(String name, int distance) {
            FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), name));
            player.setPos(Vec3.atBottomCenterOf(origin.east(distance)));
            level.addNewPlayer(player);
            return player;
        }
    }
}
