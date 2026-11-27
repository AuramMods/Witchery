package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PatronCombatTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_patron_arrows")
    public static void mogShootsFromInFrontAndUsesTargetImpulse(GameTestHelper helper) {
        var level = helper.getLevel();
        var mog = helper.spawn(WitcheryEntities.MOG.get(), new BlockPos(4, 5, 4));
        mog.setNoAi(true);
        mog.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        var target = EntityType.PIG.create(level);
        target.setPos(mog.getX() + 6, mog.getY(), mog.getZ() + 8);

        var shots = new ArrayList<Arrow>();
        Consumer<EntityJoinLevelEvent> record = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof Arrow shot && shot.getOwner() == mog) {
                shots.add(shot);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(record);
        try {
            for (boolean grounded : new boolean[] {false, true}) {
                for (boolean impulse : new boolean[] {false, true}) {
                    target.setOnGround(grounded);
                    target.hasImpulse = impulse;
                    int start = shots.size();
                    for (int shot = 0; shot < 16; shot++) mog.performRangedAttack(target, 0.75F);
                    var volley = shots.subList(start, shots.size());
                    helper.assertTrue(volley.size() == 16, "Mog should fire one arrow per attack");
                    helper.assertTrue(volley.stream().allMatch(shot ->
                                    Math.abs(shot.getX() - mog.getX() - 0.6) < 1.0E-6
                                    && Math.abs(shot.getY() - mog.getEyeY() + 0.1F) < 1.0E-6
                                    && Math.abs(shot.getZ() - mog.getZ() - 0.8) < 1.0E-6),
                            "The arrow starts one block toward the target at Mog's firing height");
                    double meanSpeed = volley.stream().mapToDouble(shot -> shot.getDeltaMovement().length())
                            .average().orElseThrow();
                    helper.assertTrue(Math.abs(meanSpeed - (impulse ? 4.0 : 2.4)) < 0.25,
                            "The target's impulse should select arrow speed independently of ground contact");
                }
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(record);
            shots.forEach(Arrow::discard);
            mog.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_patron_dying_partner")
    public static void patronProtectionLastsThroughPartnerDeathAnimation(GameTestHelper helper) {
        var level = helper.getLevel();
        var mog = helper.spawn(WitcheryEntities.MOG.get(), new BlockPos(4, 5, 4));
        var gulg = helper.spawn(WitcheryEntities.GULG.get(), new BlockPos(6, 5, 4));
        mog.setNoAi(true);
        gulg.setNoAi(true);
        helper.runAfterDelay(2, () -> {
            helper.assertFalse(mog.hurt(level.damageSources().magic(), 10), "A living partner protects Mog");
            gulg.setHealth(0);
            helper.assertFalse(gulg.isAlive(), "Gulg should be dying");
            helper.assertFalse(mog.hurt(level.damageSources().magic(), 10), "A dying partner still protects Mog");
            gulg.discard();
            helper.assertTrue(mog.hurt(level.damageSources().magic(), 10) && mog.getHealth() == 390,
                    "Mog becomes vulnerable after the partner leaves the world");
            mog.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_patron_teleport")
    public static void patronStalledPathTeleportUsesOriginalCadence(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(3, 8, 8));
        Vec3 origin = Vec3.atBottomCenterOf(base);
        var target = EntityType.PIG.create(level);
        target.setPos(origin.add(30, 16, 0));
        for (int x = 0; x <= 21; x++) {
            for (int z = -5; z <= 5; z++) level.setBlockAndUpdate(base.offset(x, -1, z), Blocks.STONE.defaultBlockState());
        }

        for (var type : new EntityType<?>[] {WitcheryEntities.MOG.get(), WitcheryEntities.GULG.get()}) {
            var god = (HobgoblinGod) type.create(level);
            god.goalSelector.removeAllGoals(goal -> true);
            god.targetSelector.removeAllGoals(goal -> true);
            god.setNoGravity(true);
            god.setTarget(target);
            int interval = god.isMog() ? 301 : 201;
            for (int age : new int[] {1, interval - 1, interval, interval * 2 - 1, interval * 2}) {
                checkTravel(helper, god, origin, age, age % interval == 0);
            }
            var save = new CompoundTag();
            god.addAdditionalSaveData(save);
            save.putInt("TeleportDelay", 2);
            god.readAdditionalSaveData(save);
            god.setTarget(target);
            checkTravel(helper, god, origin, interval * 2 + 1, false);
            checkTravel(helper, god, origin, interval * 2 + 2, true);
        }
        helper.succeed();
    }

    private static void checkTravel(GameTestHelper helper, HobgoblinGod god, Vec3 origin, int age, boolean expected) {
        god.setPos(origin);
        god.getNavigation().stop();
        god.getRandom().setSeed(23);
        god.tickCount = age;
        god.aiStep();
        helper.assertTrue((god.position().distanceTo(origin) > 1) == expected,
                "The stalled boss should teleport on its original schedule at tick " + age);
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_patron_statue")
    public static void statueWaitsForThePreviousGodToFinishDying(GameTestHelper helper) {
        var level = helper.getLevel();
        var base = helper.absolutePos(new BlockPos(5, 5, 5));
        level.setBlockAndUpdate(base.below(), Blocks.STONE.defaultBlockState());
        var patron = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "PatronTest"));
        var mog = helper.spawn(WitcheryEntities.MOG.get(), new BlockPos(9, 5, 5));
        mog.setNoAi(true);
        var encounterArea = AABB.ofSize(Vec3.atCenterOf(base), 32, 32, 32);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.getEntitiesOfClass(HobgoblinGod.class, encounterArea).contains(mog),
                "The previous boss is present in the statue's encounter area")).thenExecute(() -> {
            mog.setHealth(0);
            helper.assertFalse(HobgoblinGodEncounter.summon(level, base, patron, 16, 0),
                    "A dying boss should keep the statue's summon unavailable");
            mog.discard();
            helper.assertTrue(HobgoblinGodEncounter.summon(level, base, patron, 16, 0),
                    "The statue should summon its pair after the previous boss is removed");
        }).thenSucceed();
    }
}
