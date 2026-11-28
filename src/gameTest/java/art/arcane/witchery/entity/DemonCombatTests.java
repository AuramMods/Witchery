package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DemonCombatTests {
    private static final Field GOALS = ObfuscationReflectionHelper.findField(Mob.class, "f_21345_");

    @GameTest(template = "qa_coven_arena", batch = "witchery_demon_melee")
    public static void demonPursuitsAndStrikesUseSeparateRanges(GameTestHelper helper) {
        var origin = helper.absolutePos(new BlockPos(6, 0, 5)).atY(212);
        for (int x = -3; x <= 8; x++) {
            for (int z = -3; z <= 3; z++) {
                helper.getLevel().setBlockAndUpdate(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState());
            }
        }
        helper.runAfterDelay(21, () -> {
            checkMelee(helper, origin, 1.8, false, true, true);
            checkMelee(helper, origin, 2.8, false, true, false);
            checkMelee(helper, origin, 3, false, true, false);
            checkMelee(helper, origin, 3.1, false, false, false);
            checkMelee(helper, origin, 1.8, true, false, false);
            helper.succeed();
        });
    }

    private static void checkMelee(GameTestHelper helper, BlockPos origin, double distance, boolean charmed,
                                   boolean expectsPursuit, boolean expectsHit) {
        var level = helper.getLevel();
        var demon = WitcheryEntities.DEMON.get().create(level);
        var target = EntityType.VILLAGER.create(level);
        try {
            Vec3 start = Vec3.atBottomCenterOf(origin);
            demon.setPos(start);
            demon.setOnGround(true);
            target.setPos(start.add(distance, 0, 0));
            if (charmed) target.setItemInHand(InteractionHand.MAIN_HAND, tongueCharm());
            demon.setTarget(target);
            var selector = (GoalSelector) GOALS.get(demon);
            var melee = selector.getAvailableGoals().stream().filter(goal -> goal.getGoal() instanceof MeleeAttackGoal)
                    .findFirst().orElseThrow();
            helper.assertTrue(melee.canUse() == expectsPursuit, "Unexpected Demon pursuit at " + distance);
            if (expectsPursuit) {
                melee.start();
                melee.tick();
                helper.assertTrue((target.getHealth() < target.getMaxHealth()) == expectsHit,
                        "Unexpected Demon strike reach at " + distance);
                target.setItemInHand(InteractionHand.MAIN_HAND, tongueCharm());
                helper.assertTrue(!melee.canContinueToUse(), "Holding the charm stops an existing pursuit");
            }
        } catch (IllegalAccessException error) {
            throw new IllegalStateException(error);
        } finally {
            demon.discard();
            target.discard();
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_demon_fireballs")
    public static void demonChecksTheWorldRollAndPreservesFireballSpread(GameTestHelper helper) {
        var level = helper.getLevel();
        var demon = WitcheryEntities.DEMON.get().create(level);
        var target = EntityType.VILLAGER.create(level);
        Vec3 origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 12, 4)));
        demon.setPos(origin);
        demon.setYRot(0);
        demon.setXRot(0);
        demon.setYHeadRot(90);
        target.setPos(origin.add(12, 4, 0));
        var shots = new ArrayList<LargeFireball>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getEntity() instanceof LargeFireball shot && shot.getOwner() == demon) shots.add(shot);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            target.setItemInHand(InteractionHand.MAIN_HAND, tongueCharm());
            for (long seed : new long[]{42, 4640}) {
                RandomSource reference = RandomSource.create(seed);
                boolean expectsShot = reference.nextDouble() < 0.05;
                level.getRandom().setSeed(seed);
                int before = shots.size();
                demon.performRangedAttack(target, 0);
                helper.assertTrue(shots.size() - before == (expectsShot ? 1 : 0)
                                && level.getRandom().nextDouble() == reference.nextDouble(),
                        "Only the world roll decides whether a charm holder is shot");
            }
            shots.forEach(Entity::discard);
            shots.clear();
            target.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            for (int attack = 0; attack < 128; attack++) demon.performRangedAttack(target, 0);
            Vec3 launch = origin.add(0, demon.getBbHeight() / 2 + 0.5, 1);
            helper.assertTrue(shots.size() == 128, "Every unprotected attack launches a fireball");
            for (LargeFireball shot : shots) {
                Vec3 acceleration = new Vec3(shot.xPower, shot.yPower, shot.zPower);
                helper.assertTrue(shot.getType() == EntityType.FIREBALL && shot.position().distanceTo(launch) < 1.0E-6
                                && Math.abs(acceleration.length() - 0.1) < 1.0E-6,
                        "Unexpected fireball launch at " + shot.position() + " with acceleration " + acceleration);
            }
            double aimHeight = target.getY(0.5) - demon.getY(0.5);
            double average = shots.stream().mapToDouble(shot -> shot.yPower / shot.xPower * 12).average().orElseThrow();
            double variance = shots.stream().mapToDouble(shot -> Math.pow(shot.yPower / shot.xPower * 12 - average, 2))
                    .average().orElseThrow();
            helper.assertTrue(Math.abs(average - aimHeight) < 0.18 && variance > 0.06,
                    "Fireballs aim from the midpoint and retain independent vertical spread");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
            shots.forEach(Entity::discard);
            demon.discard();
            target.discard();
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_demon_bargain")
    public static void demonBargainsKeepCombatAndPreserveEscapeTiming(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(6, 0, 5)).atY(212);
        for (int x = -3; x <= 8; x++) {
            for (int z = -3; z <= 3; z++) {
                level.setBlockAndUpdate(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState());
            }
        }
        helper.runAfterDelay(21, () -> {
            var demon = WitcheryEntities.DEMON.get().create(level);
            var target = EntityType.VILLAGER.create(level);
            var customer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "DemonCustomer"));
            var start = Vec3.atBottomCenterOf(origin);
            try {
                demon.setPos(start);
                demon.setOnGround(true);
                target.setPos(start.add(6, 0, 0));
                customer.setPos(start.add(4, 0, 0));
                customer.containerMenu = new MerchantMenu(1, customer.getInventory(), demon);
                demon.setTradingPlayer(customer);
                demon.setTarget(target);
                var selector = (GoalSelector) GOALS.get(demon);
                var bargain = goalAt(selector, 3);
                helper.assertTrue(bargain.canUse() && goalAt(selector, 4).canUse(),
                        "A grounded Demon bargains and looks at a customer exactly four blocks away");
                demon.getRandom().setSeed(48);
                helper.assertTrue(goalAt(selector, 2).canUse() && goalAt(selector, 5).canUse(),
                        "Ranged combat and pursuit are eligible while a trade is open");
                helper.assertTrue(goalAt(selector, 6).getGoal() instanceof MoveThroughVillageGoal
                                && goalAt(selector, 7).getGoal() instanceof MoveTowardsRestrictionGoal,
                        "The Demon registers village and home movement after pursuit");

                customer.setPos(start.add(4.25, 0, 0));
                helper.assertTrue(!bargain.canUse(), "A customer outside four blocks cannot hold the bargain goal");
                customer.setPos(start.add(2, 0, 0));
                demon.setOnGround(false);
                helper.assertTrue(!bargain.canUse(), "An airborne Demon cannot stand still to bargain");
                demon.setOnGround(true);
                demon.hurtMarked = true;
                helper.assertTrue(!bargain.canUse(), "Knockback interrupts the bargain");
                demon.hurtMarked = false;

                helper.assertTrue(demon.getNavigation().moveTo(target, 1), "The Demon has an open path to its opponent");
                bargain.start();
                helper.assertTrue(demon.getNavigation().isDone(), "Starting the bargain stops the current path");
                bargain.stop();
                helper.assertTrue(demon.getTradingPlayer() == null, "Stopping the bargain releases its customer");

                checkEscapeTiming(helper, demon);
                helper.succeed();
            } catch (IllegalAccessException error) {
                throw new IllegalStateException(error);
            } finally {
                customer.discard();
                target.discard();
                demon.discard();
            }
        });
    }

    private static WrappedGoal goalAt(GoalSelector selector, int priority) {
        return selector.getAvailableGoals().stream().filter(goal -> goal.getPriority() == priority).findFirst().orElseThrow();
    }

    private static void checkEscapeTiming(GameTestHelper helper, Demon demon) {
        demon.setNoAi(true);
        var explosions = new AtomicInteger();
        Consumer<ExplosionEvent.Start> capture = event -> {
            if (event.getExplosion().getDirectSourceEntity() == demon) {
                explosions.incrementAndGet();
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            demon.notifyTrade(new MerchantOffer(new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.EMERALD), 1, 0, 0));
            for (int tick = 0; tick < 50; tick++) demon.aiStep();
            helper.assertTrue(demon.saveWithoutId(new CompoundTag()).getInt("EscapeTicks") == 0 && explosions.get() == 0,
                    "The escape timer reaches zero without exploding on the fiftieth update");
            demon.aiStep();
            helper.assertTrue(demon.saveWithoutId(new CompoundTag()).getInt("EscapeTicks") == -1 && explosions.get() == 1,
                    "A successful nether trade triggers one escape explosion on the fifty-first update");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
    }

    private static ItemStack tongueCharm() {
        return WitcheryItems.getRequired("devilstonguecharm").get().getDefaultInstance();
    }
}
