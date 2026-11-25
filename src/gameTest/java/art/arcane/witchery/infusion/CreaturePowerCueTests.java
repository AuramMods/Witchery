package art.arcane.witchery.infusion;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CreaturePowerCueTests {
    @GameTest(template = "qa_coven_arena")
    public static void infernalWebsAndArrowsUseTheOriginalLaunchOrigin(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "InfernalMarksman"));
        var origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 0, 4)).atY(200));
        player.moveTo(origin.x, origin.y, origin.z, 0, 0);
        player.setDeltaMovement(1.2, 0.6, -0.35);
        InfusionEnergy.grant(player, InfernalInfusion.ID, 20);
        var hand = new ItemStack(WitcheryItems.getRequired("witchhand").get());
        var shots = new ArrayList<Projectile>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getEntity() instanceof Projectile shot && shot.getOwner() == player) shots.add(shot);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            for (var power : new CreaturePower[]{CreaturePower.CAVE_SPIDER, CreaturePower.SPIDER, CreaturePower.SKELETON}) {
                power.absorb(player);
                hand.getItem().releaseUsing(hand, level, player, hand.getUseDuration() - 20);
            }
            helper.assertTrue(shots.size() == 3, "All three cast powers spawn one projectile each");
            var muzzle = new Vec3(origin.x - 0.16, player.getEyeY() - 0.1, origin.z);
            for (var shot : shots) {
                var motion = shot.getDeltaMovement();
                float speed = shot instanceof Arrow ? 3 : 1.5F;
                helper.assertTrue(shot.position().distanceToSqr(muzzle) < 0.000001
                                && Math.abs(motion.x) < 0.08 && Math.abs(motion.y) < 0.08
                                && Math.abs(motion.z - speed) < 0.08,
                        "The original shoulder offset and aim exclude the caster's movement");
            }
            helper.assertTrue(shots.get(0) instanceof ThrownBrew caveWeb && caveWeb.getItem().is(WitcheryIngredient.WEB.item())
                            && shots.get(1) instanceof ThrownBrew spiderWeb && spiderWeb.getItem().is(WitcheryIngredient.WEB.item())
                            && shots.get(2) instanceof Arrow arrow && arrow.isCritArrow() && arrow.getBaseDamage() == 3,
                    "Both spider powers fire Thrown Web and the charged Skeleton power fires its critical arrow");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
            shots.forEach(Projectile::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void restoresOtherwhereHandChargeCues(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "OtherwhereCue"));
        var origin = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 0, 0);
        level.setBlockAndUpdate(origin.offset(0, 1, 3), Blocks.STONE.defaultBlockState());
        InfusionEnergy.grant(player, OtherwhereInfusion.ID, 20);
        var hand = new ItemStack(WitcheryItems.getRequired("witchhand").get());
        var item = hand.getItem();
        int duration = hand.getUseDuration();
        var cues = new ArrayList<PlayLevelSoundEvent.AtEntity>();
        Consumer<PlayLevelSoundEvent.AtEntity> capture = cue -> {
            if (cue.getEntity() == player) cues.add(cue);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            item.onUseTick(level, player, hand, duration - 19);
            helper.assertTrue(cues.isEmpty(), "Aiming stays quiet between the original twenty-tick samples");
            item.onUseTick(level, player, hand, duration - 20);
            assertCue(helper, cues.remove(0), SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 1F / 3, 0.5F);

            player.moveTo(player.getX(), level.getMaxBuildHeight() + 2, player.getZ(), 0, -90);
            item.onUseTick(level, player, hand, duration - 40);
            assertCue(helper, cues.remove(0), SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, 0.5F, 1F / 3, 0.5F);
            item.releaseUsing(hand, level, player, duration - 40);
            assertCue(helper, cues.remove(0), SoundEvents.NOTE_BLOCK_SNARE.value(), 0.5F, 1F / 3, 0.5F);

            player.moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 0, 0);
            player.setShiftKeyDown(true);
            item.onUseTick(level, player, hand, duration - 60);
            assertCue(helper, cues.remove(0), SoundEvents.NOTE_BLOCK_PLING.value(), 0.5F, 1F / 3, 0.5F);
            item.releaseUsing(hand, level, player, duration - 60);
            assertCue(helper, cues.remove(0), SoundEvents.FIRE_EXTINGUISH, 0.5F, 1F / 3, 0.5F);
            var data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            helper.assertTrue(cues.isEmpty() && data.getDouble("WITCRecallY") == origin.getY()
                            && data.getInt("witcheryInfusionCharges") == 20,
                    "Rejection and setting the recall point preserve the infusion reserve");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void restoresInfernalPowerFeedback(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "CreaturePowerCue"));
        var origin = helper.absolutePos(BlockPos.ZERO).getCenter();
        player.setPos(origin.x, origin.y + 2, origin.z);
        InfusionEnergy.grant(player, InfernalInfusion.ID, 20);
        var cues = new ArrayList<PlayLevelSoundEvent.AtEntity>();
        Consumer<PlayLevelSoundEvent.AtEntity> capture = cue -> {
            if (cue.getEntity() == player) cues.add(cue);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            InfernalInfusion.release(player, 5);
            assertCue(helper, cues.remove(0), SoundEvents.NOTE_BLOCK_SNARE.value(), 0.5F, 1F / 3, 0.5F);

            for (CreaturePower power : new CreaturePower[]{CreaturePower.SLIME, CreaturePower.MAGMA_CUBE, CreaturePower.TOAD}) {
                power.absorb(player);
                InfernalInfusion.release(player, 5);
                assertCue(helper, cues.remove(0), SoundEvents.FIRE_EXTINGUISH, 0.5F, 1F / 3, 0.5F);
                var jump = player.getEffect(MobEffects.JUMP);
                helper.assertTrue(jump != null && jump.getAmplifier() == 3, "The power still grants Jump Boost IV");
                var fall = new LivingFallEvent(player, 14, 1);
                MinecraftForge.EVENT_BUS.post(fall);
                helper.assertTrue(fall.getDistance() == 0 && CreaturePower.charges(player) == 9,
                        "Landing prevents fall damage and spends no reserve charges");
            }

            var infusion = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            infusion.putInt("WITCBeastPowerCharges", 0);
            InfernalInfusion.release(player, 5);
            assertCue(helper, cues.remove(0), SoundEvents.NOTE_BLOCK_SNARE.value(), 0.5F, 1F / 3, 0.5F);
            helper.assertTrue(infusion.getInt("witcheryInfusionCharges") == 17, "Refused activations preserve infusion charge");

            var pig = EntityType.PIG.create(level);
            CreaturePower.SQUID.absorb(player);
            CreaturePower.SQUID.use(player, 5, new EntityHitResult(pig));
            assertCue(helper, cues.remove(0), SoundEvents.FIRE_EXTINGUISH, 1, 1.125F, 1.334F);
            helper.assertTrue(pig.hasEffect(MobEffects.BLINDNESS) && cues.isEmpty(),
                    "Squid power blinds its target and plays only the full-volume cue");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
        helper.succeed();
    }

    private static void assertCue(GameTestHelper helper, PlayLevelSoundEvent.AtEntity cue, SoundEvent sound, float volume,
                                  float minPitch, float maxPitch) {
        helper.assertTrue(cue.getSound().value() == sound && cue.getSource() == SoundSource.PLAYERS
                        && cue.getNewVolume() == volume && cue.getNewPitch() >= minPitch && cue.getNewPitch() <= maxPitch,
                "Expected the original sound, volume and pitch range for " + sound.getLocation());
    }
}
