package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.DeathClothingItem;
import art.arcane.witchery.item.DeathHandItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DeathBossTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_death", timeoutTicks = 160)
    public static void deathCapsIncomingHitsHealsAndDropsTheOriginalRewards(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = new BlockPos(4, 3, 4);
        var boss = helper.spawn(WitcheryEntities.DEATH.get(), center);
        boss.setNoAi(true);
        var target = helper.spawn(EntityType.IRON_GOLEM, center.east(2));
        target.setNoAi(true);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "DeathTester"));
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(center.south(3))));
        var strike = level.damageSources().playerAttack(player);
        var lootArea = new AABB(helper.absolutePos(center)).inflate(2);

        helper.assertTrue(boss.hurt(strike, 100) && boss.getHealth() == 985,
                "A 100-point incoming hit is capped at 15");
        helper.assertTrue(boss.doHurtTarget(target) && target.getHealth() == 85,
                "Death's touch takes 15 percent of a 100-health victim's maximum health");

        helper.startSequence()
                .thenWaitUntil(() -> awaitAge(helper, boss, 19))
                .thenExecute(() -> assertHealth(helper, boss, 985))
                .thenWaitUntil(() -> awaitAge(helper, boss, 20))
                .thenExecute(() -> assertHealth(helper, boss, 986))
                .thenWaitUntil(() -> awaitAge(helper, boss, 39))
                .thenExecute(() -> assertHealth(helper, boss, 986))
                .thenWaitUntil(() -> awaitAge(helper, boss, 40))
                .thenExecute(() -> {
                    assertHealth(helper, boss, 987);
                    boss.setHealth(10);
                    helper.assertTrue(boss.hurt(strike, 15) && boss.isDeadOrDying(),
                            "An ordinary lethal strike defeats Death");
                })
                .thenWaitUntil(() -> helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, lootArea).stream()
                        .anyMatch(entity -> entity.getItem().is(Items.ENCHANTED_BOOK)), "Death's loot is tracked"))
                .thenExecute(() -> {
                    var drops = level.getEntitiesOfClass(ItemEntity.class, lootArea).stream()
                            .map(ItemEntity::getItem).toList();
                    var binky = WitcheryIngredient.BINKY_HEAD.item();
                    helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(Items.SKELETON_SKULL))
                                    && drops.stream().anyMatch(stack -> stack.is(Items.BONE) && stack.getCount() == 5)
                                    && drops.stream().filter(stack -> stack.getItem() instanceof DeathClothingItem
                                            || stack.getItem() instanceof DeathHandItem || stack.is(binky)).count() == 1
                                    && drops.stream().anyMatch(stack -> stack.is(Items.SADDLE))
                                            == drops.stream().anyMatch(stack -> stack.is(binky)),
                            "Death drops the guaranteed loot and one original reward, with a saddle for Binky");
                    target.discard();
                }).thenSucceed();
    }

    private static void awaitAge(GameTestHelper helper, DeathBoss boss, int age) {
        helper.assertTrue(boss.tickCount >= age, "Waiting for Death to tick to age " + age);
        helper.assertTrue(boss.tickCount == age, "Expected Death at age " + age + ", found " + boss.tickCount);
    }

    private static void assertHealth(GameTestHelper helper, DeathBoss boss, float expected) {
        helper.assertTrue(boss.getHealth() == expected,
                "Death has " + boss.getHealth() + " health at tick " + boss.tickCount + ", expected " + expected);
    }
}
