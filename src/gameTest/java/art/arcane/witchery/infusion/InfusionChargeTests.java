package art.arcane.witchery.infusion;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.event.WitcheryMobDrops;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class InfusionChargeTests {
    private static final String CHARGE = "witcheryInfusionCharges";
    private static final String CAPACITY = "witcheryInfusionChargesMax";

    @GameTest(template = "qa_coven_arena")
    public static void lightCloakClearsChargeOnItsFirstUnaffordablePulse(GameTestHelper helper) {
        FakePlayer caster = caster(helper);
        InfusionEnergy.grant(caster, LightInfusion.ID, 1);
        ItemStack hand = new ItemStack(WitcheryItems.getRequired("witchhand").get());
        hand.getItem().onUseTick(caster.level(), caster, hand, hand.getUseDuration() - 30);
        helper.assertTrue(caster.hasEffect(MobEffects.INVISIBILITY) && infusion(caster).contains(CHARGE)
                        && infusion(caster).getInt(CHARGE) == 0,
                "An exact spend leaves the zero charge field while the cloak is active");
        hand.getItem().onUseTick(caster.level(), caster, hand, hand.getUseDuration() - 60);
        helper.assertTrue(!caster.hasEffect(MobEffects.INVISIBILITY) && !infusion(caster).contains(CHARGE)
                        && InfusionEnergy.type(caster) == LightInfusion.ID && infusion(caster).getInt(CAPACITY) == 1,
                "The unaffordable pulse clears its charge field and cloak while keeping the infusion and capacity");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void branchAndContractKeepAnUnaffordableBalance(GameTestHelper helper) {
        FakePlayer caster = caster(helper);
        InfusionEnergy.grant(caster, OverworldInfusion.ID, 9);
        ItemStack contract = new ItemStack(WitcheryIngredient.CONTRACT_OF_TORMENT.item());
        SymbolSpell.NOX.cast(helper.getLevel(), caster);
        contract.getItem().onUseTick(caster.level(), caster, contract, contract.getUseDuration());
        helper.assertTrue(infusion(caster).getInt(CHARGE) == 9 && contract.getCount() == 1,
                "The Branch and Contract leave all nine charges available after their failed attempts");
        caster.getAbilities().instabuild = true;
        infusion(caster).remove(CHARGE);
        helper.assertTrue(!InfusionEnergy.canSpend(caster, 10),
                "The Contract and other ordinary consumers still require an infusion charge field in creative");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void passiveCreatureDefenseIgnoresAnUnaffordableHit(GameTestHelper helper) {
        FakePlayer caster = caster(helper);
        InfusionEnergy.grant(caster, InfernalInfusion.ID, 5);
        CreaturePower.SKELETON.absorb(caster);
        infusion(caster).putInt(CHARGE, 4);
        var sounds = new AtomicInteger();
        Consumer<PlayLevelSoundEvent.AtEntity> capture = cue -> {
            if (cue.getEntity() == caster) sounds.incrementAndGet();
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            var drowning = caster.damageSources().drown();
            var unaffordable = new LivingHurtEvent(caster, drowning, 2);
            MinecraftForge.EVENT_BUS.post(unaffordable);
            helper.assertTrue(!unaffordable.isCanceled() && infusion(caster).getInt(CHARGE) == 4 && sounds.get() == 0,
                    "The passive Skeleton power keeps four charges and stays quiet on an unaffordable hit");
            infusion(caster).putInt(CHARGE, 5);
            var affordable = new LivingHurtEvent(caster, drowning, 2);
            MinecraftForge.EVENT_BUS.post(affordable);
            helper.assertTrue(affordable.isCanceled() && infusion(caster).contains(CHARGE)
                            && infusion(caster).getInt(CHARGE) == 0 && caster.hasEffect(MobEffects.WATER_BREATHING),
                    "Five available charges pay for the original drowning defense");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void witchHandUsesIndependentFailureCues(GameTestHelper helper) {
        FakePlayer caster = caster(helper);
        var level = helper.getLevel();
        var pig = EntityType.PIG.create(level);
        pig.setPos(caster.getX() + 1, caster.getY(), caster.getZ());
        ItemStack hand = new ItemStack(WitcheryItems.getRequired("witchhand").get());
        caster.setItemInHand(InteractionHand.MAIN_HAND, hand);
        List<PlayLevelSoundEvent.AtEntity> sounds = new ArrayList<>();
        Consumer<PlayLevelSoundEvent.AtEntity> capture = cue -> {
            if (cue.getEntity() == caster) sounds.add(cue);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            Runnable release = () -> {
                hand.getItem().use(level, caster, InteractionHand.MAIN_HAND);
                caster.releaseUsingItem();
            };
            Runnable attack = () -> caster.attack(pig);
            assertHandCues(helper, sounds, release, 1, "An uninfused release");
            assertHandCues(helper, sounds, attack, 1, "An uninfused attack");
            helper.assertTrue(!infusion(caster).contains(CHARGE) && pig.getHealth() == pig.getMaxHealth(),
                    "The Hand intercepts melee and keeps an uninfused player uncharged");
            hand.getOrCreateTag().putLong("WITCCooldown", System.currentTimeMillis() + 60_000);
            assertHandCues(helper, sounds, release, 0, "A cooldown release");
            assertHandCues(helper, sounds, attack, 1, "An attack during release cooldown");
            hand.removeTagKey("WITCCooldown");

            InfusionEnergy.grant(caster, LightInfusion.ID, 4);
            assertHandCues(helper, sounds, attack, 1, "An unaffordable lift");
            helper.assertTrue(!infusion(caster).contains(CHARGE) && InfusionEnergy.type(caster) == LightInfusion.ID,
                    "A failed Light lift removes only the charge field");

            InfusionEnergy.grant(caster, OtherwhereInfusion.ID, 1);
            caster.setShiftKeyDown(true);
            assertHandCues(helper, sounds, release, 1, "Missing recall coordinates");
            helper.assertTrue(infusion(caster).getInt(CHARGE) == 1, "An unset recall point keeps the balance");
            CompoundTag data = infusion(caster);
            data.putInt("WITCRecallD", 0);
            data.putDouble("WITCRecallX", caster.getX() + 2);
            data.putDouble("WITCRecallY", caster.getY());
            data.putDouble("WITCRecallZ", caster.getZ());
            assertHandCues(helper, sounds, release, 2, "An unaffordable recall");
            helper.assertTrue(!data.contains(CHARGE), "An unaffordable recall clears the balance");
            data.putInt(CHARGE, 3);
            assertHandCues(helper, sounds, attack, 2, "An unaffordable companion recall");
            helper.assertTrue(!data.contains(CHARGE), "An unaffordable companion recall clears the balance");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void witchHandDropsAtTheWitchOnTheOriginalWorldRoll(GameTestHelper helper) {
        FakePlayer caster = caster(helper);
        var level = helper.getLevel();
        var witch = EntityType.WITCH.create(level);
        var coven = WitcheryEntities.COVEN_WITCH.get().create(level);
        witch.setPos(caster.getX() + 1, caster.getY(), caster.getZ());
        coven.setPos(caster.getX() + 2, caster.getY(), caster.getZ());
        var melee = level.damageSources().playerAttack(caster);
        var arrow = new Arrow(level, caster);

        assertHandDrop(helper, witch, melee, 4096, true, true);
        assertHandDrop(helper, witch, melee, 12288, false, true);
        caster.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(WitcheryItems.getRequired("arthana").get()));
        assertHandDrop(helper, witch, melee, 12288, true, true);
        assertHandDrop(helper, witch, melee, 42, false, true);
        assertHandDrop(helper, coven, melee, 12288, true, true);
        assertHandDrop(helper, witch, level.damageSources().arrow(arrow, caster), 4096, false, false);

        var mobLoot = level.getGameRules().getRule(GameRules.RULE_DOMOBLOOT);
        boolean previousLoot = mobLoot.get();
        try {
            mobLoot.set(false, level.getServer());
            level.random.setSeed(12288);
            witch.die(melee);
            helper.assertTrue(handDrops(witch).size() == 1, "The Hand drops during ordinary death with mob loot disabled");
        } finally {
            mobLoot.set(previousLoot, level.getServer());
            handDrops(witch).forEach(ItemEntity::discard);
        }
        helper.succeed();
    }

    private static void assertHandCues(GameTestHelper helper, List<PlayLevelSoundEvent.AtEntity> sounds,
                                       Runnable action, int count, String label) {
        sounds.clear();
        var random = helper.getLevel().random;
        random.setSeed(42);
        action.run();
        helper.assertTrue(random.nextInt(1000) == RandomSource.create(42).nextInt(1000),
                label + " leaves the world random stream alone");
        helper.assertTrue(sounds.size() == count && sounds.stream().allMatch(cue ->
                        cue.getSound().value() == SoundEvents.NOTE_BLOCK_SNARE.value() && cue.getSource() == SoundSource.RECORDS
                                && cue.getNewVolume() == 0.5F && cue.getNewPitch() >= 1F / 3F && cue.getNewPitch() <= 0.5F),
                label + " plays " + count + " half-volume Snare cues");
    }

    private static void assertHandDrop(GameTestHelper helper, LivingEntity witch, net.minecraft.world.damagesource.DamageSource source,
                                       long seed, boolean expectedDrop, boolean drawsRoll) {
        var level = helper.getLevel();
        level.random.setSeed(seed);
        WitcheryMobDrops.dropWitchHand(new LivingDeathEvent(witch, source));
        RandomSource control = RandomSource.create(seed);
        if (drawsRoll) control.nextDouble();
        helper.assertTrue(level.random.nextInt(1000) == control.nextInt(1000),
                "The death callback takes one world draw for a direct player kill");
        var drops = handDrops(witch);
        if (expectedDrop) {
            helper.assertTrue(drops.size() == 1 && drops.get(0).position().equals(witch.position())
                            && !drops.get(0).hasPickUpDelay(),
                    "The Hand appears at the witch's feet and can be picked up immediately");
        } else {
            helper.assertTrue(drops.isEmpty(), "The original roll or damage source yields no Hand");
        }
        drops.forEach(ItemEntity::discard);
    }

    private static List<ItemEntity> handDrops(LivingEntity witch) {
        return witch.level().getEntitiesOfClass(ItemEntity.class, new AABB(witch.position(), witch.position()).inflate(0.25),
                drop -> drop.getItem().is(WitcheryItems.getRequired("witchhand").get()));
    }

    private static FakePlayer caster(GameTestHelper helper) {
        FakePlayer caster = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ChargeCaster"));
        Vec3 site = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 0, 6)).atY(200));
        caster.moveTo(site.x, site.y, site.z, 0, 0);
        return caster;
    }

    private static CompoundTag infusion(FakePlayer caster) {
        return caster.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG);
    }
}
