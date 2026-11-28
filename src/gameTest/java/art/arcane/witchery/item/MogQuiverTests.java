package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class MogQuiverTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_mog_quiver", setupTicks = 5)
    public static void quiverLaunchMatchesTheOriginalAndSurvivesArrowReload(GameTestHelper helper) {
        var level = helper.getLevel();
        var shooter = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "MogQuiverTest"));
        var origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 6, 5)));
        shooter.setPos(origin);
        shooter.setYRot(31);
        shooter.setXRot(-17);
        shooter.setOnGround(false);
        shooter.setDeltaMovement(0.7, 0.6, 0.4);
        shooter.setItemSlot(EquipmentSlot.CHEST, WitcheryItems.getRequired("quiverofmog").get().getDefaultInstance());
        var shots = new ArrayList<Arrow>();
        var sounds = new ArrayList<PlayLevelSoundEvent.AtPosition>();
        Consumer<EntityEvent.EntityConstructing> seed = event -> {
            if (event.getEntity() instanceof Arrow arrow && arrow.level() == level) {
                RandomSource random = ObfuscationReflectionHelper.getPrivateValue(Entity.class, arrow, "f_19796_");
                random.setSeed(4096);
            }
        };
        Consumer<EntityJoinLevelEvent> track = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof Arrow arrow && arrow.getOwner() == shooter) {
                shots.add(arrow);
            }
        };
        Consumer<PlayLevelSoundEvent.AtPosition> hear = event -> {
            if (event.getLevel() == level && event.getSound().value() == SoundEvents.ARROW_SHOOT) sounds.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(seed);
        MinecraftForge.EVENT_BUS.addListener(track);
        MinecraftForge.EVENT_BUS.addListener(hear);
        try {
            // The launch vectors from Witchery 0.24.1 at yaw 31, pitch -17 and projectile seed 4096.
            var muzzle = new Vec3(-0.137149751, -0.100000001, -0.082401119);
            var fullDraw = new Vec3(-2.21019211, 1.31103434, 3.66682107);
            var motions = new Vec3[] {Vec3.ZERO, Vec3.ZERO, new Vec3(-0.23759567, 0.14093621, 0.39418330),
                    new Vec3(-1.14929997, 0.68173790, 1.90674708), fullDraw, fullDraw};
            var draws = new int[] {0, 2, 3, 12, 20, 32};
            var bow = (BowItem) Items.BOW;
            for (int index = 0; index < draws.length; index++) {
                int charge = draws[index];
                var held = new ItemStack(bow);
                shooter.setItemInHand(InteractionHand.MAIN_HAND, held);
                helper.assertTrue(bow.use(level, shooter, InteractionHand.MAIN_HAND).getResult().consumesAction()
                                && shooter.isUsingItem() && shooter.getProjectile(held).isEmpty(),
                        "Mog's Quiver draws the ordinary Bow with no ammunition");
                level.getRandom().setSeed(42);
                bow.releaseUsing(held, level, shooter, bow.getUseDuration(held) - charge);
                shooter.stopUsingItem();
                boolean released = charge >= 3;
                helper.assertTrue(shots.size() == (released ? 1 : 0) && sounds.size() == shots.size()
                                && held.getDamageValue() == (released ? 1 : 0),
                        "The original charge threshold applies to the shot, cue and wear at charge " + charge);
                helper.assertTrue(level.getRandom().nextInt(1000) == RandomSource.create(42).nextInt(1000),
                        "The shot leaves the world's random stream unchanged");
                if (!released) continue;
                var arrow = shots.remove(0);
                var sound = sounds.remove(0);
                try {
                    helper.assertTrue(arrow.position().distanceTo(shooter.getEyePosition().add(muzzle)) < 0.000002
                                    && arrow.getDeltaMovement().distanceTo(motions[index]) < 0.000002,
                            "Expected the original muzzle and launch vector at charge " + charge + ": " + arrow.getDeltaMovement());
                    helper.assertTrue(arrow.getType() == EntityType.ARROW && arrow.isCritArrow() == (charge >= 20)
                                    && arrow.pickup == AbstractArrow.Pickup.CREATIVE_ONLY,
                            "The shot uses the vanilla arrow type and original critical and pickup states");
                    helper.assertTrue(sound.getPosition().equals(origin) && sound.getSource() == SoundSource.NEUTRAL
                                    && sound.getNewVolume() == 1,
                            "The shot cue uses the shooter's exact position and original volume");
                    if (charge == 20) {
                        var save = new CompoundTag();
                        helper.assertTrue(arrow.save(save) && save.getString("id").equals("minecraft:arrow")
                                        && save.getCompound("ForgeData").getBoolean("WITCMogged"),
                                "The ordinary arrow save records the original marker");
                        var restored = (Arrow) EntityType.create(save, level).orElseThrow();
                        helper.assertTrue(restored.getUUID().equals(arrow.getUUID())
                                        && restored.pickup == AbstractArrow.Pickup.CREATIVE_ONLY,
                                "Ordinary serialization preserves the arrow identity and pickup rule");
                        verifyHits(helper, shooter, restored);
                        restored.discard();
                    }
                } finally {
                    arrow.discard();
                }
            }
        } finally {
            shots.forEach(Arrow::discard);
            MinecraftForge.EVENT_BUS.unregister(seed);
            MinecraftForge.EVENT_BUS.unregister(track);
            MinecraftForge.EVENT_BUS.unregister(hear);
        }
        helper.succeed();
    }

    private static void verifyHits(GameTestHelper helper, FakePlayer shooter, Arrow restored) {
        var level = helper.getLevel();
        var unmarked = new Arrow(level, shooter);
        for (boolean impulse : new boolean[] {false, true}) {
            for (boolean grounded : new boolean[] {false, true}) {
                for (Arrow arrow : new Arrow[] {unmarked, restored}) {
                    var target = EntityType.PIG.create(level);
                    target.setOnGround(grounded);
                    target.hasImpulse = impulse;
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 999, 2));
                    var hurt = new LivingHurtEvent(target, level.damageSources().arrow(arrow, shooter), 2);
                    MinecraftForge.EVENT_BUS.post(hurt);
                    var weakness = target.getEffect(MobEffects.WEAKNESS);
                    boolean marked = arrow == restored;
                    helper.assertTrue(hurt.getAmount() == (marked && impulse ? 6 : 2)
                                    && weakness.getDuration() == (marked ? 200 : 999)
                                    && weakness.getAmplifier() == (marked ? 0 : 2),
                            "A saved Mog arrow triples damage with an impulse and resets Weakness");
                }
            }
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void transformedPlayersCannotNockAnOrdinaryBow(GameTestHelper helper) {
        var level = helper.getLevel();
        var shooter = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "MogQuiverForm"));
        var data = WitcheryPlayerDataProvider.get(shooter).orElseThrow(IllegalStateException::new);
        var bow = (BowItem) Items.BOW;
        shooter.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bow));
        shooter.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW));
        var quiver = WitcheryItems.getRequired("quiverofmog").get().getDefaultInstance();
        for (var chest : new ItemStack[] {ItemStack.EMPTY, quiver}) {
            shooter.setItemSlot(EquipmentSlot.CHEST, chest);
            for (int form : new int[] {0, 1, 3}) {
                data.setCreatureTypeOrdinal(form);
                var action = bow.use(level, shooter, InteractionHand.MAIN_HAND);
                helper.assertTrue(action.getResult().consumesAction() == (form == 0) && shooter.isUsingItem() == (form == 0),
                        "The Bow draws only in the original form, even with ammunition and the quiver");
                shooter.stopUsingItem();
            }
        }
        helper.succeed();
    }
}
