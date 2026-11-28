package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BatBallAndWolfsbaneTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_bat_ball_and_wolfsbane")
    public static void batBallUseThrowsOneFreshItemInSurvivalAndCreative(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = player(helper);
        var drops = new ArrayList<ItemEntity>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item
                    && item.getItem().is(WitcheryIngredient.BAT_BALL.item())) drops.add(item);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            for (var mode : new GameType[]{GameType.SURVIVAL, GameType.CREATIVE}) {
                player.setGameMode(mode);
                var held = markedBatBalls(2);
                player.setItemInHand(InteractionHand.MAIN_HAND, held);
                level.random.setSeed(42);
                var reference = RandomSource.create(42);
                double spread = 0.0075F;
                var motion = new Vec3(reference.nextGaussian() * spread, reference.nextGaussian() * spread,
                        1 + reference.nextGaussian() * spread);
                helper.assertTrue(player.gameMode.useItem(player, level, held, InteractionHand.MAIN_HAND).consumesAction()
                                && held.getCount() == 1 && held.getTag().getInt("FixtureMarker") == 42,
                        "Normal held use spends one marked serving in " + mode);
                helper.assertTrue(drops.size() == 1, "One dropped item enters the world");
                var drop = drops.remove(0);
                var position = new Vec3(player.getX() - 0.16F, player.getEyeY() + 0.125 - 0.1F, player.getZ());
                helper.assertTrue(drop.position().equals(position) && drop.getDeltaMovement().equals(motion)
                                && drop.getItem().getCount() == 1 && !drop.getItem().hasTag()
                                && drop.saveWithoutId(new CompoundTag()).getShort("PickupDelay") == 5
                                && level.random.nextInt(1000) == reference.nextInt(1000),
                        "A fresh Bat Ball follows the original origin, speed, world spread and pickup delay");
                drop.discard();
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            drops.forEach(ItemEntity::discard);
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_bat_ball_and_wolfsbane")
    public static void damageDropsTaggedStacksAndHonorsTossVeto(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = player(helper);
        var inventory = player.getInventory();
        inventory.setItem(2, markedBatBalls(3));
        inventory.setItem(12, markedBatBalls(5));
        player.setItemInHand(InteractionHand.OFF_HAND, markedBatBalls(7));
        inventory.setItem(13, new ItemStack(Items.STICK));
        inventory.setItem(14, markedBatBalls(11));
        var drops = new ArrayList<ItemEntity>();
        Consumer<ItemTossEvent> listener = event -> {
            if (event.getPlayer() != player) return;
            if (event.getEntity().getItem().getCount() == 5) event.setCanceled(true);
            else if (event.getEntity().getItem().getCount() != 11) drops.add(event.getEntity());
        };
        Consumer<EntityJoinLevelEvent> spawnVeto = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity drop
                    && drop.getItem().is(WitcheryIngredient.BAT_BALL.item()) && drop.getItem().getCount() == 11) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        MinecraftForge.EVENT_BUS.addListener(spawnVeto);
        try {
            helper.assertTrue(player.hurt(level.damageSources().fellOutOfWorld(), 1), "The real damage path accepted the hit");
            helper.assertTrue(inventory.getItem(2).isEmpty() && player.getOffhandItem().isEmpty()
                            && inventory.getItem(12).getCount() == 5 && inventory.getItem(14).getCount() == 11
                            && inventory.getItem(13).is(Items.STICK),
                    "Damage empties carried balls including the offhand and preserves both vetoed stacks");
            helper.assertTrue(drops.size() == 2 && drops.get(0).getItem().getCount() == 3 && drops.get(1).getItem().getCount() == 7
                            && drops.stream().allMatch(drop -> drop.isAddedToWorld() && drop.getDeltaMovement().z > 0.28
                            && drop.getItem().getTag().getInt("FixtureMarker") == 42
                            && drop.saveWithoutId(new CompoundTag()).getShort("PickupDelay") == 40
                            && drop.saveWithoutId(new CompoundTag()).getUUID("Thrower").equals(player.getUUID())),
                    "The hit spills both tagged stacks forward with the original toss delay and owner");
            helper.assertTrue(!WitcheryIngredient.BAT_BALL.item().use(level, player, InteractionHand.OFF_HAND).getResult().consumesAction(),
                    "A use interrupted by a damage drop cannot throw another ball from the emptied hand");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            MinecraftForge.EVENT_BUS.unregister(spawnVeto);
            drops.forEach(ItemEntity::discard);
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_bat_ball_and_wolfsbane")
    public static void batBallSpillsOnNonlethalSpiritDamageAndStaysOnLethalDamage(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int damage : new int[]{1, 40}) {
            var player = player(helper);
            var spirit = new CompoundTag();
            spirit.putBoolean("WITCSpiritWalking", true);
            spirit.putBoolean("WITCManifested", true);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, spirit);
            player.setItemInHand(InteractionHand.MAIN_HAND, markedBatBalls(2));
            var drops = new ArrayList<ItemEntity>();
            Consumer<ItemTossEvent> listener = event -> {
                if (event.getPlayer() == player) drops.add(event.getEntity());
            };
            MinecraftForge.EVENT_BUS.addListener(listener);
            try {
                helper.assertTrue(player.hurt(level.damageSources().fellOutOfWorld(), damage), "The manifested player took damage");
                helper.assertTrue(damage == 1 ? drops.size() == 1 && player.getMainHandItem().isEmpty()
                                : drops.isEmpty() && player.getMainHandItem().getCount() == 2,
                        "The spirit drops Bat Balls only when the final hit leaves it alive");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
                drops.forEach(ItemEntity::discard);
                player.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_bat_ball_and_wolfsbane")
    public static void wolfsbaneChoosesTheNearestEntityEvenBehindAStoneBlock(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = player(helper);
        var flower = new ItemStack(WitcheryItems.getRequired("wolfsbane_flower").get(), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, flower);
        var wolfman = helper.spawnWithNoFreeWill(WitcheryEntities.WOLFMAN.get(), new BlockPos(5, 3, 6));
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(5, 3, 5));
        pig.setPos(player.position().add(0, 1.25, 0.8));
        level.setBlockAndUpdate(helper.absolutePos(new BlockPos(5, 4, 5)), Blocks.STONE.defaultBlockState());
        var cues = new ArrayList<PlayLevelSoundEvent.AtEntity>();
        Consumer<PlayLevelSoundEvent.AtEntity> listener = event -> {
            if (event.getEntity() == player) cues.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            level.random.setSeed(42);
            helper.assertTrue(player.gameMode.useItem(player, level, flower, InteractionHand.MAIN_HAND).consumesAction()
                            && flower.getCount() == 2 && cues.size() == 1,
                    "The nearer pig takes precedence over the wolfman");
            var cue = cues.remove(0);
            helper.assertTrue(cue.getSound().value() == SoundEvents.NOTE_BLOCK_SNARE.value() && cue.getSource() == SoundSource.RECORDS
                            && cue.getNewVolume() == 0.5F && Math.abs(cue.getNewPitch() - 0.36662757F) < 0.000001F
                            && level.random.nextInt(1000) == 248,
                    "The ordinary animal produces the original negative cue and spends one pitch draw");
            pig.discard();
            player.setGameMode(GameType.CREATIVE);
            level.random.setSeed(42);
            helper.assertTrue(player.gameMode.useItem(player, level, flower, InteractionHand.MAIN_HAND).consumesAction()
                            && flower.getCount() == 1 && cues.isEmpty(),
                    "The wolfman through stone uses the particle packet and spends a flower in Creative");
            helper.assertTrue(level.random.nextInt(1000) == RandomSource.create(42).nextInt(1000),
                    "The packet leaves the dedicated server random stream unchanged");
            wolfman.setPos(player.position().add(0, 0, 3));
            level.random.setSeed(42);
            helper.assertTrue(!player.gameMode.useItem(player, level, flower, InteractionHand.MAIN_HAND).consumesAction()
                            && flower.getCount() == 1 && cues.isEmpty()
                            && level.random.nextInt(1000) == RandomSource.create(42).nextInt(1000),
                    "An entity outside the two-block trace leaves the flower and RNG untouched");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            wolfman.discard();
            pig.discard();
            player.discard();
        }
        helper.succeed();
    }

    private static ItemStack markedBatBalls(int count) {
        var stack = new ItemStack(WitcheryIngredient.BAT_BALL.item(), count);
        stack.getOrCreateTag().putInt("FixtureMarker", 42);
        return stack;
    }

    private static FakePlayer player(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "BatBallTester")) {
            @Override
            public boolean isInvulnerableTo(DamageSource source) { return false; }
        };
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 3, 4))));
        player.setYRot(0);
        player.setXRot(0);
        return player;
    }
}
