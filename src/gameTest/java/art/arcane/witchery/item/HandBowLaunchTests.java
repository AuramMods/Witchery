package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitcheryBolt;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class HandBowLaunchTests {
    // Measured on Witchery 0.24.1 at yaw 31 and pitch -17 with projectile seed 4096.
    private static final List<Vec3> OFFSETS = List.of(
            new Vec3(-0.157061771, -0.300000001, -0.030522076),
            new Vec3(-0.137149751, -0.300000001, -0.082401119),
            new Vec3(-0.100694433, -0.300000001, -0.124340780));
    private static final List<Vec3> QUICK_MOTION = List.of(
            new Vec3(-0.507453155, 0.816483108, 2.617153244),
            new Vec3(-1.376458533, 0.816483059, 2.283614686),
            new Vec3(-2.078973082, 0.816483059, 1.672966189));
    private static final List<Vec3> FULL_MOTION = List.of(
            new Vec3(-0.543214794, 0.874022949, 2.801591332),
            new Vec3(-1.473461404, 0.874022897, 2.444547381),
            new Vec3(-2.225484112, 0.874022897, 1.790864782));

    @GameTest(template = "qa_coven_arena", batch = "witchery_crossbow_launch", setupTicks = 5)
    public static void quickAndFullShotsMatchTheOriginalLaunch(GameTestHelper helper) {
        var level = helper.getLevel();
        var crossbow = (HandBowItem) WitcheryItems.getRequired("handbow").get();
        var shooter = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "HandBowLaunch"));
        var origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 6, 5)));
        shooter.setPos(origin);
        shooter.setYRot(31);
        shooter.setXRot(-17);
        shooter.setOnGround(false);
        shooter.setDeltaMovement(0.4, -0.2, 0.8);

        var projectiles = new ArrayList<WitcheryBolt>();
        var sounds = new ArrayList<PlayLevelSoundEvent.AtPosition>();
        Consumer<EntityEvent.EntityConstructing> seed = event -> {
            if (event.getEntity() instanceof WitcheryBolt bolt && bolt.level() == level) {
                RandomSource random = ObfuscationReflectionHelper.getPrivateValue(Entity.class, bolt, "f_19796_");
                random.setSeed(4096);
            }
        };
        Consumer<EntityJoinLevelEvent> track = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof WitcheryBolt bolt && bolt.getOwner() == shooter) {
                projectiles.add(bolt);
            }
        };
        Consumer<PlayLevelSoundEvent.AtPosition> hear = event -> {
            if (event.getLevel() == level && event.getSound().value() == SoundEvents.ARROW_SHOOT) sounds.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(seed);
        MinecraftForge.EVENT_BUS.addListener(track);
        MinecraftForge.EVENT_BUS.addListener(hear);
        try {
            for (int ammo : new int[] {1, 4}) {
                boolean splitting = ammo == 4;
                for (int elapsed : new int[] {0, 19, 20, 35}) {
                    projectiles.clear();
                    sounds.clear();
                    var stack = new ItemStack(crossbow);
                    stack.getOrCreateTag().putInt("WITCBoltTypeCurrent", ammo);
                    shooter.setItemInHand(InteractionHand.MAIN_HAND, stack);
                    helper.assertTrue(crossbow.use(level, shooter, InteractionHand.MAIN_HAND).getResult().consumesAction(),
                            "A loaded Crossbow starts its ordinary use");
                    crossbow.releaseUsing(stack, level, shooter, crossbow.getUseDuration(stack) - elapsed);
                    shooter.stopUsingItem();

                    boolean full = elapsed >= 20;
                    List<Vec3> motion = full ? FULL_MOTION : QUICK_MOTION;
                    helper.assertTrue(projectiles.size() == (splitting ? 3 : 1), "Expected the original number of Bolts");
                    for (int i = 0; i < projectiles.size(); i++) {
                        var bolt = projectiles.get(i);
                        int expected = splitting ? i : 1;
                        helper.assertTrue(bolt.position().distanceTo(shooter.getEyePosition().add(OFFSETS.get(expected))) < 0.000002,
                                "The muzzle follows each Bolt's original yaw offset: " + bolt.position().subtract(shooter.getEyePosition()));
                        helper.assertTrue(bolt.getDeltaMovement().distanceTo(motion.get(expected)) < 0.000002,
                                "Expected the original seeded launch vector at " + elapsed + " ticks: " + bolt.getDeltaMovement());
                        helper.assertTrue(bolt.isCritArrow() == full && bolt.getBaseDamage() == (splitting ? 1 : 2)
                                        && bolt.pickup == AbstractArrow.Pickup.ALLOWED,
                                "A shot retains the original damage, critical state and pickup mode");
                        bolt.discard();
                    }
                    helper.assertTrue(stack.getDamageValue() == 2 && HandBowItem.loaded(stack) == null,
                            "A normal shot spends the ammo and two durability");
                    helper.assertTrue(sounds.size() == 1 && sounds.get(0).getPosition().equals(origin)
                                    && sounds.get(0).getSource() == SoundSource.PLAYERS && sounds.get(0).getNewVolume() == 1,
                            "The launch cue plays once at the shooter's exact position");
                }
            }
        } finally {
            projectiles.forEach(WitcheryBolt::discard);
            MinecraftForge.EVENT_BUS.unregister(seed);
            MinecraftForge.EVENT_BUS.unregister(track);
            MinecraftForge.EVENT_BUS.unregister(hear);
        }
        helper.succeed();
    }
}
