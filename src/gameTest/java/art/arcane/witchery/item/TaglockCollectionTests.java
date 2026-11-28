package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class TaglockCollectionTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_taglock_collection")
    public static void playerCollectionUsesHeadFacingAndTheWorldRoll(GameTestHelper helper) {
        var collector = player(helper, "TaglockCollector");
        var target = player(helper, "TaglockTarget");
        collector.setYRot(180);
        target.setYRot(0);
        var kit = kit();
        // Two native seeds on either side of each chance, starting with the standing, visible case.
        long[][] close = {{6432, 6464}, {6432, 6464}, {1696, 1728}, {1696, 1728}};
        long[][] wide = {{5312, 5136}, {4240, 4224}, {4296, 4096}, {7296, 7309}};
        float[][] headings = {{350, 15}, {0, 44.99F}, {0, 45}, {0, 180}};
        var worldRandom = helper.getLevel().random;
        try {
            for (int angle = 0; angle < headings.length; angle++) {
                collector.setYHeadRot(headings[angle][0]);
                target.setYHeadRot(headings[angle][1]);
                for (int state = 0; state < close.length; state++) {
                    collector.setShiftKeyDown(state >= 2);
                    collector.setInvisible(state % 2 == 1);
                    long[] seeds = (angle < 2 ? close : wide)[state];
                    for (int result = 0; result < seeds.length; result++) {
                        boolean accepted = result == 1;
                        var held = new ItemStack(kit);
                        collector.setItemInHand(InteractionHand.MAIN_HAND, held);
                        worldRandom.setSeed(seeds[result]);
                        var expected = RandomSource.create(seeds[result]);
                        expected.nextDouble();
                        if (accepted) expected.nextDouble();
                        helper.assertTrue(kit.collectFromEntity(collector, InteractionHand.MAIN_HAND, target).consumesAction(),
                                "A collection attempt consumes the interaction");
                        helper.assertTrue(TaglockItem.isBoundTo(held, target) == accepted,
                                "Taglock outcome matches the original for angle " + angle + ", state " + state
                                        + ", seed " + seeds[result]);
                        helper.assertTrue(worldRandom.nextInt(1000) == expected.nextInt(1000),
                                "Only the stealth roll and a successful sample's pitch advance the world RNG");
                    }
                }
            }
        } finally {
            collector.discard();
            target.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_taglock_collection")
    public static void kitSplitsAndBedFailureKeepOriginalDataAndCues(GameTestHelper helper) {
        var level = helper.getLevel();
        var collector = player(helper, "TaglockKits");
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(7, 3, 5));
        var kit = kit();
        var hand = InteractionHand.MAIN_HAND;
        var cues = new ArrayList<PlayLevelSoundEvent.AtEntity>();
        var drops = new ArrayList<ItemEntity>();
        Consumer<PlayLevelSoundEvent.AtEntity> soundListener = event -> {
            if (event.getEntity() == collector) cues.add(event);
        };
        Consumer<EntityJoinLevelEvent> dropListener = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item && item.getItem().is(kit)) drops.add(item);
        };
        MinecraftForge.EVENT_BUS.addListener(soundListener);
        MinecraftForge.EVENT_BUS.addListener(dropListener);
        try {
            for (int count : new int[]{2, 1}) {
                var held = new ItemStack(kit, count);
                held.getOrCreateTag().putInt("FixtureMarker", 42);
                collector.setItemInHand(hand, held);
                collector.getInventory().setItem(1, ItemStack.EMPTY);
                level.random.setSeed(42);
                kit.collectFromEntity(collector, hand, pig);
                var filled = count == 2 ? collector.getInventory().getItem(1) : held;
                helper.assertTrue(held.getCount() == 1 && held.getTag().getInt("FixtureMarker") == 42
                                && TaglockItem.isBound(held) == (count == 1),
                        "The original stack loses one kit or is filled in place");
                helper.assertTrue(pig.getUUID().equals(TaglockItem.boundCreatureId(filled))
                                && filled.getTag().getInt("FixtureMarker") == (count == 1 ? 42 : 0) && pig.isPersistenceRequired(),
                        "A split kit only carries the new creature sample and the creature stays persistent");
                assertCue(helper, cues, true);
            }

            for (int slot = 1; slot < 36; slot++) collector.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
            collector.setItemInHand(hand, new ItemStack(kit, 2));
            kit.collectFromEntity(collector, hand, pig);
            helper.assertTrue(drops.size() == 1
                            && drops.get(0).position().equals(collector.position().add(0.5, 1.5, 0.5))
                            && pig.getUUID().equals(TaglockItem.boundCreatureId(drops.get(0).getItem())),
                    "A full inventory drops the filled kit at the original position");
            cues.clear();

            var foot = helper.absolutePos(new BlockPos(5, 3, 5));
            var bed = Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.EAST);
            level.setBlock(foot, bed, Block.UPDATE_CLIENTS);
            collector.setItemInHand(hand, new ItemStack(kit));
            collector.setShiftKeyDown(true);
            var use = new UseOnContext(collector, hand, new BlockHitResult(Vec3.atCenterOf(foot), Direction.UP, foot, false));
            level.random.setSeed(42);
            helper.assertTrue(kit.onItemUseFirst(collector.getMainHandItem(), use).consumesAction()
                            && !TaglockItem.isBound(collector.getMainHandItem()),
                    "Even a crouching collector needs a vanilla bed head opposite a foot");
            assertCue(helper, cues, false);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(dropListener);
            MinecraftForge.EVENT_BUS.unregister(soundListener);
            drops.forEach(ItemEntity::discard);
            pig.discard();
            collector.discard();
        }
        helper.succeed();
    }

    private static void assertCue(GameTestHelper helper, List<PlayLevelSoundEvent.AtEntity> cues, boolean success) {
        helper.assertTrue(cues.size() == 1, "Collection emits one cue");
        var cue = cues.remove(0);
        helper.assertTrue(cue.getSound() != null
                        && cue.getSound().value() == (success ? SoundEvents.EXPERIENCE_ORB_PICKUP : SoundEvents.NOTE_BLOCK_SNARE.value())
                        && cue.getSource() == (success ? SoundSource.PLAYERS : SoundSource.RECORDS)
                        && cue.getNewVolume() == 0.5F && Math.abs(cue.getNewPitch() - 0.36662757F) < 0.000001F
                        && helper.getLevel().random.nextInt(1000) == 248,
                "The cue uses the original mixer, volume, seed-42 pitch and next world roll");
    }

    private static TaglockItem kit() {
        return (TaglockItem) WitcheryItems.getRequired("taglockkit").get();
    }

    private static FakePlayer player(GameTestHelper helper, String name) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 3, 4))));
        return player;
    }
}
