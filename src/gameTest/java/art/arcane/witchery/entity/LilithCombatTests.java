package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LilithCombatTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_lilith")
    public static void innateArmorReducesCappedHitsAndAllowsArmorBypass(GameTestHelper helper) {
        var level = helper.getLevel();
        Lilith lilith = helper.spawnWithNoFreeWill(WitcheryEntities.LILITH.get(), new BlockPos(7, 3, 5));
        var arrow = EntityType.ARROW.create(level);
        try {
            lilith.setHealth(100);
            helper.assertTrue(lilith.hurt(level.damageSources().arrow(arrow, null), 40), "A heavy arrow hits Lilith");
            helper.assertTrue(Math.abs(lilith.getHealth() - 91.84F) < 0.001F,
                    "12 capped damage is reduced to 8.16 by eight points of innate armor");

            lilith.invulnerableTime = 0;
            helper.assertTrue(lilith.hurt(level.damageSources().arrow(arrow, null), 5), "A small arrow hits Lilith");
            helper.assertTrue(Math.abs(lilith.getHealth() - 88.44F) < 0.001F,
                    "The same armor reduces a five-point hit to 3.4");

            lilith.invulnerableTime = 0;
            helper.assertTrue(lilith.hurt(level.damageSources().magic(), 7), "Magic hits Lilith");
            helper.assertTrue(Math.abs(lilith.getHealth() - 81.44F) < 0.001F,
                    "Armor-bypassing magic deals the full seven damage");
        } finally {
            arrow.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_lilith")
    public static void lilithClearsCurableEffectsWhenDefeated(GameTestHelper helper) {
        var level = helper.getLevel();
        Lilith lilith = helper.spawnWithNoFreeWill(WitcheryEntities.LILITH.get(), new BlockPos(7, 3, 5));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LilithVictor"));
        player.setPos(lilith.position().add(3, 0, 0));
        try {
            lilith.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 440));
            lilith.addEffect(new MobEffectInstance(WitcheryEffects.MORTAL_COIL.get(), 600));
            lilith.setHealth(1);
            lilith.hurt(level.damageSources().playerAttack(player), 12);
            helper.assertTrue(lilith.isAlive() && lilith.friendly(), "The final hit changes Lilith into her friendly form");
            helper.assertTrue(!lilith.hasEffect(MobEffects.WEAKNESS)
                            && lilith.hasEffect(WitcheryEffects.MORTAL_COIL.get()),
                    "Defeat clears Weakness and carries the incurable Mortal Coil into the friendly form");
        } finally {
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_lilith")
    public static void lilithPreservesBookCountAndTagsWhenEnchanting(GameTestHelper helper) {
        var level = helper.getLevel();
        Lilith lilith = helper.spawnWithNoFreeWill(WitcheryEntities.LILITH.get(), new BlockPos(7, 3, 5));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LilithReader"));
        player.setPos(lilith.position().add(3, 0, 0));
        var saved = lilith.saveWithoutId(new CompoundTag());
        saved.putBoolean("Friendly", true);
        lilith.readAdditionalSaveData(saved);
        ItemStack offering = new ItemStack(Items.BOOK, 2);
        offering.setHoverName(Component.literal("Field Notes"));
        offering.getOrCreateTag().putString("FixtureMarker", "bound manuscript");
        player.setItemInHand(InteractionHand.MAIN_HAND, offering);
        try {
            lilith.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.getMainHandItem().isEmpty() && lilith.isRemoved(),
                    "The bargain consumes the offering and dismisses Lilith");
        } finally {
            player.discard();
        }
        helper.succeedWhen(() -> {
            var rewards = level.getEntitiesOfClass(ItemEntity.class, lilith.getBoundingBox().inflate(8),
                    drop -> drop.getItem().is(Items.ENCHANTED_BOOK));
            helper.assertTrue(rewards.size() == 2, "Lilith drops one enchanted book for each book in the offering");
            for (ItemEntity drop : rewards) {
                ItemStack reward = drop.getItem();
                helper.assertTrue(!EnchantedBookItem.getEnchantments(reward).isEmpty(), "The reward stores an enchantment");
                helper.assertTrue(reward.getCount() == 1 && reward.getHoverName().getString().equals("Field Notes")
                                && "bound manuscript".equals(reward.getTag().getString("FixtureMarker")),
                        "Each enchanted book respects the stack limit and preserves the offering's name and custom tag");
            }
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_lilith", timeoutTicks = 280)
    public static void reflectedFireballSuppressesLilithHealingWithoutDealingFireDamage(GameTestHelper helper) {
        var level = helper.getLevel();
        Lilith lilith = helper.spawnWithNoFreeWill(WitcheryEntities.LILITH.get(), new BlockPos(7, 20, 5));
        lilith.setNoGravity(true);
        var ghast = EntityType.GHAST.create(level);
        ghast.setPos(lilith.position().add(-3, 0, 0));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LilithDefender"));
        player.setPos(lilith.position().add(-5, 0, 0));
        player.setYRot(-90);
        var fireball = new LargeFireball(level, ghast, -1, 0, 0, 1) {
            void hitLilith() {
                onHitEntity(new EntityHitResult(lilith));
            }
        };

        int[] impactTick = new int[1];
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(lilith.tickCount >= 27, "Lilith has passed a healing pulse"))
                .thenExecute(() -> {
                    try {
                        lilith.setHealth(100);
                        fireball.hitLilith();
                        helper.assertTrue(lilith.getHealth() == 100 && weaknessTicks(lilith) == 0,
                                "An unreflected Large Fireball does not harm Lilith or suppress her healing");
                        helper.assertTrue(fireball.hurt(level.damageSources().playerAttack(player), 1)
                                        && fireball.getOwner() == player && fireball.getDeltaMovement().dot(new Vec3(1, 0, 0)) > 0,
                                "A player attack reflects the vanilla Large Fireball and takes ownership");
                        fireball.hitLilith();
                        impactTick[0] = lilith.tickCount;
                        helper.assertTrue(lilith.getHealth() == 100 && weaknessTicks(lilith) == 200 - lilith.tickCount % 20,
                                "A reflected Large Fireball interrupts the healing cycle without dealing fire damage");
                    } finally {
                        ghast.discard();
                        player.discard();
                        fireball.discard();
                    }
                }).thenWaitUntil(() -> helper.assertTrue(lilith.tickCount >= impactTick[0] + 175,
                        "Lilith has advanced 175 ticks since impact"))
                .thenExecute(() -> helper.assertTrue(lilith.getHealth() == 100 && weaknessTicks(lilith) > 0,
                        "The boss does not heal during the reflection window, health=" + lilith.getHealth()
                                + ", remaining=" + weaknessTicks(lilith)))
                .thenWaitUntil(() -> helper.assertTrue(lilith.getHealth() == 105 && weaknessTicks(lilith) == 0,
                        "The next healing pulse after the window restores five health"))
                .thenSucceed();
    }

    private static int weaknessTicks(Lilith lilith) {
        return lilith.saveWithoutId(new CompoundTag()).getInt("WeaknessTicks");
    }
}
