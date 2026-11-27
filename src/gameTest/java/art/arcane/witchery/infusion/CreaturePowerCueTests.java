package art.arcane.witchery.infusion;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
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
        var sequence = new ArrayList<String>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getEntity() instanceof Projectile shot && shot.getOwner() == player) {
                shots.add(shot);
                sequence.add("projectile");
            }
        };
        Consumer<PlayLevelSoundEvent.AtEntity> sound = event -> {
            if (event.getEntity() == player && event.getSound().value() == SoundEvents.ARROW_SHOOT) sequence.add("bow");
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        MinecraftForge.EVENT_BUS.addListener(sound);
        try {
            for (var power : new CreaturePower[]{CreaturePower.CAVE_SPIDER, CreaturePower.SPIDER, CreaturePower.SKELETON}) {
                power.absorb(player);
                level.getRandom().setSeed(42);
                hand.getItem().releaseUsing(hand, level, player, hand.getUseDuration() - 20);
                helper.assertTrue(level.getRandom().nextInt(1000) == 763,
                        "The bow pitch advances the world's random source by one float");
            }
            helper.assertTrue(shots.size() == 3, "All three cast powers spawn one projectile each");
            helper.assertTrue(sequence.equals(List.of("bow", "projectile", "bow", "projectile", "bow", "projectile")),
                    "Each bow cue plays before the corresponding projectile spawns");
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
            MinecraftForge.EVENT_BUS.unregister(sound);
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
            var worldRandom = level.getRandom();
            worldRandom.setSeed(42);
            item.onUseTick(level, player, hand, duration - 19);
            helper.assertTrue(cues.isEmpty(), "Aiming stays quiet between the original twenty-tick samples");
            item.onUseTick(level, player, hand, duration - 20);
            assertCue(helper, cues.remove(0), SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 1F / 3, 0.5F);
            helper.assertTrue(worldRandom.nextInt(1000) == 130, "A clear aim cue leaves the world random source alone");

            player.moveTo(player.getX(), level.getMaxBuildHeight() + 2, player.getZ(), 0, -90);
            worldRandom.setSeed(42);
            item.onUseTick(level, player, hand, duration - 40);
            assertCue(helper, cues.remove(0), SoundEvents.ITEM_PICKUP, 0.5F, 1F / 3, 0.5F);
            helper.assertTrue(worldRandom.nextInt(1000) == 130, "A missed aim cue leaves the world random source alone");
            worldRandom.setSeed(42);
            item.releaseUsing(hand, level, player, duration - 40);
            assertCue(helper, cues.remove(0), SoundEvents.NOTE_BLOCK_SNARE.value(), 0.5F, 1F / 3, 0.5F);
            helper.assertTrue(worldRandom.nextInt(1000) == 130, "Release rejection leaves the world random source alone");

            player.moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 0, 0);
            player.setShiftKeyDown(true);
            worldRandom.setSeed(42);
            item.onUseTick(level, player, hand, duration - 60);
            assertCue(helper, cues.remove(0), SoundEvents.NOTE_BLOCK_PLING.value(), 0.5F, 1F / 3, 0.5F);
            helper.assertTrue(worldRandom.nextInt(1000) == 130, "The recall-ready cue leaves the world random source alone");
            worldRandom.setSeed(42);
            item.releaseUsing(hand, level, player, duration - 60);
            assertCue(helper, cues.remove(0), SoundEvents.FIRE_EXTINGUISH, 0.5F, 1F / 3, 0.5F);
            helper.assertTrue(worldRandom.nextInt(1000) == 248, "The recall confirmation takes its original double draw");
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
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void squidCuePrecedesBlindnessAndMissesStayQuiet(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SquidCaster"));
        player.setPos(helper.absolutePos(new BlockPos(3, 2, 3)).getCenter());
        var pig = EntityType.PIG.create(level);
        var minecart = EntityType.MINECART.create(level);
        InfusionEnergy.grant(player, InfernalInfusion.ID, 20);
        CreaturePower.SQUID.absorb(player);

        var sequence = new ArrayList<String>();
        var cues = new ArrayList<PlayLevelSoundEvent.AtEntity>();
        Consumer<PlayLevelSoundEvent.AtEntity> hear = cue -> {
            if (cue.getEntity() == player) {
                sequence.add("fizz");
                cues.add(cue);
            }
        };
        Consumer<MobEffectEvent.Added> apply = event -> {
            if (event.getEntity() == pig && event.getEffectInstance().getEffect() == MobEffects.BLINDNESS) {
                sequence.add("blindness");
            }
        };
        MinecraftForge.EVENT_BUS.addListener(hear);
        MinecraftForge.EVENT_BUS.addListener(apply);
        try {
            level.getRandom().setSeed(42);
            CreaturePower.SQUID.use(player, 20, new EntityHitResult(pig));
            helper.assertTrue(sequence.equals(List.of("fizz", "blindness")), "The Squid cue precedes the effect");
            float pitch = 1.1706793F;
            assertCue(helper, cues.get(0), SoundEvents.FIRE_EXTINGUISH, 1, pitch, pitch);
            var blindness = pig.getEffect(MobEffects.BLINDNESS);
            helper.assertTrue(blindness != null && blindness.getDuration() == 200 && blindness.getAmplifier() == 0
                            && level.getRandom().nextInt(1000) == 763,
                    "A living hit applies the original effect and advances the world random source by one float");

            var miss = BlockHitResult.miss(player.position(), Direction.UP, player.blockPosition());
            for (var aim : List.of(miss, new EntityHitResult(minecart))) {
                level.getRandom().setSeed(42);
                CreaturePower.SQUID.use(player, 20, aim);
                helper.assertTrue(sequence.size() == 2 && level.getRandom().nextInt(1000) == 130,
                        "A miss and a nonliving hit play no cue and consume no random draw");
            }
            var data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            helper.assertTrue(data.getInt("witcheryInfusionCharges") == 17 && CreaturePower.charges(player) == 7,
                    "Every use spends one infusion charge and one creature charge");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(hear);
            MinecraftForge.EVENT_BUS.unregister(apply);
        }
        helper.succeed();
    }

    private static void assertCue(GameTestHelper helper, PlayLevelSoundEvent.AtEntity cue, SoundEvent sound, float volume,
                                  float minPitch, float maxPitch) {
        SoundSource source = SoundSource.PLAYERS;
        if (sound == SoundEvents.FIRE_EXTINGUISH) source = SoundSource.BLOCKS;
        else if (sound == SoundEvents.NOTE_BLOCK_SNARE.value() || sound == SoundEvents.NOTE_BLOCK_PLING.value()) {
            source = SoundSource.RECORDS;
        }
        helper.assertTrue(cue.getSound().value() == sound && cue.getSource() == source
                        && cue.getNewVolume() == volume && cue.getNewPitch() >= minPitch && cue.getNewPitch() <= maxPitch,
                "Expected the original sound, category, volume and pitch range for " + sound.getLocation());
    }
}
