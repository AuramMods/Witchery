package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.world.WitcheryDimensions;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class NightmareCombatTests {
    @GameTest(template = "qa_coven_arena")
    public static void swipeTossesArmorWithTheWorldRollAndPreservesRejectedDrops(GameTestHelper helper) {
        var level = helper.getLevel();
        Vec3 origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 2, 3)).atY(250));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "NightmareArmor"));
        player.setPos(origin);
        var nightmare = WitcheryEntities.NIGHTMARE.get().create(level);
        nightmare.setPos(origin.add(0, 0, 2));
        var boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.setHoverName(Component.literal("Dream Boots"));
        boots.setDamageValue(7);
        player.setItemSlot(EquipmentSlot.FEET, boots);

        Consumer<EntityJoinLevelEvent> reject = event -> {
            if (event.getEntity() instanceof ItemEntity item && ItemStack.matches(item.getItem(), boots)) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(reject);
        try {
            level.random.setSeed(4096);
            nightmare.doHurtTarget(player);
            helper.assertTrue(ItemStack.matches(player.getItemBySlot(EquipmentSlot.FEET), boots),
                    "A rejected item spawn keeps the original armor equipped");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(reject);
        }

        var charm = new ItemStack(WitcheryIngredient.CHARM_OF_DISRUPTED_DREAMS.item());
        player.getInventory().setItem(20, charm);
        level.random.setSeed(4096);
        nightmare.doHurtTarget(player);
        helper.assertTrue(ItemStack.matches(player.getItemBySlot(EquipmentSlot.FEET), boots)
                        && level.random.nextInt(4) == 0,
                "The carried charm blocks the swipe before it spends an armor roll");
        player.getInventory().setItem(20, ItemStack.EMPTY);

        RandomSource expected = RandomSource.create(4096);
        expected.nextInt(4);
        float speed = expected.nextFloat() * 0.5F;
        float direction = expected.nextFloat() * Mth.TWO_PI;
        var motion = new Vec3(-Mth.sin(direction) * speed, 0.2F, Mth.cos(direction) * speed);
        level.random.setSeed(4096);
        nightmare.doHurtTarget(player);
        var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(origin, origin).inflate(3),
                item -> ItemStack.matches(item.getItem(), boots));
        helper.assertTrue(player.getItemBySlot(EquipmentSlot.FEET).isEmpty() && drops.size() == 1,
                "The world roll takes one equipped stack and preserves its name and wear");
        var drop = drops.get(0);
        helper.assertTrue(drop.position().distanceTo(new Vec3(origin.x, player.getEyeY() - 0.3F, origin.z)) < 1e-6
                        && drop.getDeltaMovement().distanceTo(motion) < 1e-6
                        && drop.saveWithoutId(new CompoundTag()).getShort("PickupDelay") == 40,
                "The original toss starts below the eyes with two directional draws and a forty-tick pickup delay");
        drop.discard();
        nightmare.discard();
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void bothRealmsApplyTheWeaponEffectsAfterAnAcceptedStrike(GameTestHelper helper) {
        ServerLevel waking = helper.getLevel();
        ServerLevel dream = waking.getServer().getLevel(WitcheryDimensions.DREAM);
        helper.assertTrue(dream != null, "Dream must be available");
        Vec3 origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 2, 4)).atY(250));
        verifyWeaponStrike(helper, waking, origin, 0.5F);
        verifyWeaponStrike(helper, dream, origin, 4);
        helper.succeed();
    }

    private static void verifyWeaponStrike(GameTestHelper helper, ServerLevel level, Vec3 origin, float baseDamage) {
        var nightmare = WitcheryEntities.NIGHTMARE.get().create(level);
        nightmare.setPos(origin.add(0, 0, 2));
        nightmare.setYRot(0);
        var pig = EntityType.PIG.create(level);
        pig.setPos(origin);
        helper.assertTrue(nightmare.doHurtTarget(pig) && pig.getHealth() == 10 - baseDamage,
                "The plain strike uses the base damage for its dimension");
        var ordinaryMotion = pig.getDeltaMovement();
        pig.setHealth(10);
        pig.invulnerableTime = 0;
        pig.setDeltaMovement(Vec3.ZERO);

        var weapon = new ItemStack(Items.DIAMOND_SWORD);
        weapon.enchant(Enchantments.SHARPNESS, 2);
        weapon.enchant(Enchantments.KNOCKBACK, 1);
        weapon.enchant(Enchantments.FIRE_ASPECT, 1);
        nightmare.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        nightmare.setDeltaMovement(1, 0, 1);
        float bonus = EnchantmentHelper.getDamageBonus(weapon, pig.getMobType());
        helper.assertTrue(nightmare.doHurtTarget(pig) && pig.getHealth() == 10 - baseDamage - bonus
                        && pig.getRemainingFireTicks() == 80,
                "The strike adds the Forge enchantment bonus and starts four seconds of fire");
        helper.assertTrue(pig.getDeltaMovement().subtract(ordinaryMotion).distanceTo(new Vec3(0, 0.1, 0.5)) < 1e-6
                        && nightmare.getDeltaMovement().equals(new Vec3(0.6, 0, 0.6)),
                "Knockback adds the original upward and forward impulse and slows the attacker");
        pig.clearFire();
        pig.setInvulnerable(true);
        helper.assertFalse(nightmare.doHurtTarget(pig) || pig.isOnFire(),
                "A rejected strike leaves the target unlit");
        helper.assertTrue(nightmare.canBreatheUnderwater(), "The Nightmare keeps its air under water");
        nightmare.discard();
        pig.discard();
    }

    @GameTest(template = "qa_coven_arena")
    public static void rejectedHitStillActivatesNightmareGuard(GameTestHelper helper) {
        ServerLevel waking = helper.getLevel();
        ServerLevel dream = waking.getServer().getLevel(WitcheryDimensions.DREAM);
        helper.assertTrue(dream != null, "Dream must be available");
        BlockPos position = helper.absolutePos(new BlockPos(3, 2, 3)).atY(250);
        verifyVeto(helper, waking, position, false, 40);
        verifyVeto(helper, waking, position, true, 30);
        verifyVeto(helper, dream, position, false, 80);
        verifyVeto(helper, dream, position, true, 40);
        helper.succeed();
    }

    private static void verifyVeto(GameTestHelper helper, ServerLevel level, BlockPos position, boolean spear, int ticks) {
        var nightmare = WitcheryEntities.NIGHTMARE.get().create(level);
        nightmare.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        var attacker = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "NightmareAttack"));
        if (spear) {
            attacker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(WitcheryItems.getRequired("huntsmanspear").get()));
        }
        var attacks = new ArrayList<Integer>();
        Consumer<LivingAttackEvent> reject = event -> {
            if (event.getEntity() != nightmare) return;
            CompoundTag data = new CompoundTag();
            nightmare.addAdditionalSaveData(data);
            attacks.add(data.getInt("DefenseTicks"));
            event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(reject);
        try {
            var source = level.damageSources().playerAttack(attacker);
            helper.assertFalse(nightmare.hurt(source, 50), "Forge rejected the attack");
            helper.assertTrue(nightmare.isDefended() && nightmare.getHealth() == 100,
                    "The rejected hit opens the guard window without damaging the Nightmare");
            helper.assertFalse(nightmare.hurt(source, 50), "Guard blocks follow-up damage");
            helper.assertTrue(attacks.equals(List.of(ticks)), "The first attack sees the original guard duration");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(reject);
            nightmare.discard();
            attacker.discard();
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void playsTheOriginalHitAndDeathSounds(GameTestHelper helper) {
        var level = helper.getLevel();
        var wounded = WitcheryEntities.NIGHTMARE.get().create(level);
        var dying = WitcheryEntities.NIGHTMARE.get().create(level);
        BlockPos position = helper.absolutePos(new BlockPos(3, 2, 3)).atY(250);
        wounded.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        dying.setPos(wounded.position());
        dying.setHealth(1);
        var sounds = new ArrayList<SoundEvent>();
        Consumer<PlayLevelSoundEvent.AtPosition> capture = event -> {
            if (event.getPosition().equals(wounded.position()) && event.getSound() != null) {
                sounds.add(event.getSound().value());
            }
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            var source = level.damageSources().generic();
            helper.assertTrue(wounded.hurt(source, 50) && wounded.getHealth() == 85,
                    "A fresh attack is capped at fifteen damage");
            helper.assertTrue(dying.hurt(source, 5) && dying.isDeadOrDying(), "A lethal attack finishes the Nightmare");
            helper.assertTrue(sounds.equals(List.of(WitcherySounds.NIGHTMARE_DEAD.get(), WitcherySounds.NIGHTMARE_HIT.get())),
                    "Expected dead cue on hurt and hit cue on death, heard " + sounds);
            helper.assertTrue(wounded.getAmbientSoundInterval() == 160, "Ambient calls have the original interval");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
            wounded.discard();
            dying.discard();
        }
        helper.succeed();
    }
}
